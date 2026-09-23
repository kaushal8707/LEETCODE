# Kafka Consumer Rebalancing

**Rebalancing** is the process Kafka uses to redistribute partitions among consumers in a consumer group when the group membership or partition assignment needs to change.

A simple definition:

> **Rebalancing = Kafka recalculating and redistributing partition ownership among consumers in a consumer group.**

---

## Basic example

Suppose we have a topic with 6 partitions:

```
Topic: order-events

P0 P1 P2 P3 P4 P5
```

And 3 consumers:

```
Consumer Group: payment-service

C1 C2 C3
```

Kafka might assign:

```
P0 ──> C1
P1 ──> C1

P2 ──> C2
P3 ──> C2

P4 ──> C3
P5 ──> C3
```

This is the current partition assignment.

---

## What happens when a consumer crashes?

Suppose C2 crashes:

```
P0 ──> C1
P1 ──> C1

P2 ──> C2  ❌
P3 ──> C2  ❌

P4 ──> C3
P5 ──> C3
```

Kafka detects that C2 is no longer an active group member.

Now a **rebalance** happens.

Kafka needs to redistribute P2 and P3.

For example:

```
P0 ──> C1
P1 ──> C1
P2 ──> C3

P3 ──> C3

P4 ──> C3
P5 ──> C3
```

The exact assignment depends on the assignment strategy and group state.

The important point is:

```
C2 failed
   ↓
Group membership changed
   ↓
Rebalance
   ↓
Partitions redistributed
```

---

## What can trigger a rebalance?

Several events can cause a rebalance.

### Consumer joins

Before:

```
C1
C2
```

A new consumer joins:

```
C1
C2
C3 ← joins
```

Kafka may rebalance the partitions.

### Consumer leaves

```
C1
C2
C3 ← leaves
```

Kafka redistributes C3's partitions.

### Consumer crashes

```
C1
C2 ← crashes
C3
```

Kafka detects the failure and redistributes the partitions.

### Consumer is considered unresponsive

For example, a consumer may stop calling `poll()` frequently enough and exceed the configured `max.poll.interval.ms`.

Kafka can then consider it unable to continue participating normally in the group, leading to reassignment.

### Topic partitions change

If partitions are added to a topic, the consumer group's assignment may need to be recalculated.

---

## Why does Kafka need rebalancing?

Because Kafka has this important rule:

> One partition can be assigned to only one consumer within a consumer group at a time.

Suppose:

```
4 partitions

P0 P1 P2 P3
```

and:

```
C1
C2
```

Assignment:

```
C1 → P0 P1
C2 → P2 P3
```

If C2 disappears:

```
C1 → P0 P1
C2 → P2 P3  ❌
```

Someone needs to take ownership of P2 and P3.

Therefore:

```
         Rebalance
            ↓
C1 → P0 P1 P2 P3
```

---

## Rebalancing when a new consumer joins

Initially:

```
6 partitions
3 consumers

C1 → P0 P1
C2 → P2 P3
C3 → P4 P5
```

Now C4 joins:

```
C1
C2
C3
C4 ← new
```

Kafka can rebalance:

```
C1 → P0 P1
C2 → P2 P3
C3 → P4
C4 → P5
```

Or another balanced assignment depending on the assignor.

The goal is generally to distribute the partitions appropriately.

---

## Rebalancing is not necessarily bad

Rebalancing is necessary for:

- fault tolerance
- scaling consumers
- handling consumer failures
- maintaining correct partition ownership

But **frequent or expensive rebalances can hurt performance**.

Why?

Because partition ownership can change and consumers may need to stop/revoke and reacquire partitions depending on the rebalancing protocol.

---

## The problem with frequent rebalancing

Imagine:

```
Consumer Group

C1
C2
C3
C4
```

Every few seconds:

```
C4 joins
   ↓
Rebalance

C4 leaves
   ↓
Rebalance

C4 joins
   ↓
Rebalance
```

You can get:

```
Processing
   ↓
Rebalance
   ↓
Pause / partition movement
   ↓
Processing
   ↓
Rebalance
   ↓
Pause / partition movement
```

This can reduce throughput and increase latency.

This is why Kafka provides mechanisms such as **cooperative rebalancing** and **static membership** to reduce unnecessary disruption.

---

## Eager Rebalancing

Historically, Kafka's **eager rebalancing** approach works roughly like this:

Existing assignment:

```
C1 → P0 P1
C2 → P2 P3
C3 → P4 P5
```

A rebalance occurs.

Consumers revoke their existing assignments:

```
C1 → nothing
C2 → nothing
C3 → nothing
```

Then Kafka calculates a new assignment:

```
C1 → P0 P1
C2 → P2 P3
C3 → P4 P5
```

Even if the final assignment doesn't change much, partitions may temporarily be revoked.

This can cause a **stop-and-reassign** effect.

---

## Cooperative Rebalancing

Cooperative rebalancing tries to make the process more incremental.

Suppose:

Before:

```
C1 → P0 P1 P2
C2 → P3 P4 P5
```

C3 joins.

Instead of unnecessarily revoking everything, Kafka can incrementally move only the partitions that need to move.

For example:

```
C1 → P0 P1
C2 → P3 P4
C3 → P2 P5
```

The exact assignment and transition depend on the group state and assignor.

The important concept is:

> Cooperative rebalancing minimizes unnecessary partition movement.

This is why **CooperativeStickyAssignor** is important in Kafka interviews.

---

## What is `max.poll.interval.ms`?

This is an important configuration for understanding rebalancing.

A consumer application typically does:

```java
while (true) {
    ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));

    process(records);
}
```

Kafka expects the consumer to continue calling `poll()`.

If processing takes too long:

```
poll()
  ↓
process
  ↓
processing takes 10 minutes
  ↓
poll() not called
```

If the time between `poll()` calls exceeds `max.poll.interval.ms`, Kafka can consider the consumer unable to continue participating normally in the group.

This can result in its partitions being reassigned.

---

## `session.timeout.ms`

Another important setting is:

```
session.timeout.ms
```

It relates to how long the broker can wait without receiving the expected heartbeats from a consumer before considering that consumer failed.

Conceptually:

```
Consumer
   |
   | heartbeat
   |
   | heartbeat
   |
   | heartbeat
   X
```

If heartbeats stop for long enough:

```
Kafka
   ↓
Consumer considered dead
   ↓
Rebalance
```

---

## `heartbeat.interval.ms`

The consumer sends heartbeats to help maintain its membership in the group.

Conceptually:

```
Consumer
   |
   | heartbeat
   ↓
Coordinator
   |
   | heartbeat
   ↓
Coordinator
```

A common configuration relationship is:

```
heartbeat.interval.ms  <  session.timeout.ms
```

For example:

```
heartbeat.interval.ms = 3 seconds
session.timeout.ms    = 10 seconds
```

The exact values should be chosen based on the workload and Kafka version/configuration rather than blindly copying these numbers.

---

## Very important distinction

Don't confuse these three:

### `heartbeat.interval.ms`

How frequently the consumer sends heartbeats.

### `session.timeout.ms`

How long the coordinator can go without valid heartbeats before considering the consumer failed.

### `max.poll.interval.ms`

Maximum allowed time between successful `poll()` calls before the consumer is considered unable to continue normal processing.

A useful mental model:

```
             Consumer
                |
      +---------+---------+
      |                   |
  heartbeat              poll()
      |                   |
      v                   v
 session timeout   max.poll.interval
      |                   |
      v                   v
  membership          processing
   liveness            progress
```

---

## Rebalancing and offsets

This is critical.

Suppose C1 owns P0:

```
P0

100 → OrderCreated
101 → Payment
102 → Shipped
103 → Delivered
```

C1 processes through offset 102.

Suppose C1 commits:

```
committed offset = 103
```

Then C1 crashes.

After rebalance:

```
P0 → C2
```

C2 can use the committed offset to determine where to resume:

```
P0

100
101
102
103 ← resume around here
```

This is why **offset management** and **rebalancing** are closely connected.

---

## Rebalancing and duplicate processing

Suppose:

```
P0

Offset 100
Offset 101
Offset 102
```

Consumer processes offset 102:

```
process(102)
```

but crashes before committing the offset.

```
process(102)
   ↓
CRASH
   ↓
offset not committed
```

After rebalance, another consumer may start from the previous committed offset and process 102 again.

```
102 → processed again
```

This is one reason Kafka applications commonly need to handle **at-least-once delivery** and **duplicate processing** safely.

---

## Rebalancing and consumer lag

Suppose:

```
Producer
   ↓
 Kafka
   ↓
Consumer
```

Consumer is processing normally.

Then a rebalance happens:

```
Processing
   ↓
Rebalance
   ↓
Partition reassignment
   ↓
Processing resumes
```

During the reassignment period, consumption can temporarily slow or pause.

Therefore:

```
Rebalance frequency ↑
        ↓
Processing disruption ↑
        ↓
Consumer lag may ↑
```

Frequent unnecessary rebalances are something you monitor carefully in production.

---

## Static Membership

Kafka also supports **static membership** using:

```
group.instance.id
```

The idea is to give a consumer a stable identity within the consumer group.

This can reduce unnecessary rebalances when consumers restart temporarily.

For example:

```
Consumer: instance ID = payment-instance-1
```

If it restarts quickly, Kafka can recognize that it is the same logical group member rather than treating every restart as an entirely new member.

This is particularly useful in environments where applications restart frequently.

---

## Complete rebalancing flow

Here's the interview-level mental model:

```
         Consumer Group
                |
                v
         Group Coordinator
                |
         Membership changes
                |
      +---------+---------+
      |         |         |
   Join       Leave     Failure
      |         |         |
      +---------+---------+
                |
                v
            Rebalance
                |
                v
         Partition Assignor
                |
                v
      New Partition Assignment
                |
      +---------+---------+
      |         |         |
      v         v         v
     C1        C2        C3
      |         |         |
     P0 P1     P2 P3     P4 P5
```

---

## Important interview questions

### Q1. What is Kafka rebalancing?

> Rebalancing is the process of redistributing topic partitions among active consumers in a consumer group when group membership or assignment changes.

### Q2. When does rebalancing occur?

Common triggers include:

- consumer joins
- consumer leaves
- consumer failure
- consumer session expiration
- consumer exceeding `max.poll.interval.ms`
- relevant subscription/partition changes

### Q3. Can two consumers in the same group consume the same partition?

> No, a partition is assigned to at most one consumer in a group at a time.

### Q4. What happens when a consumer crashes?

```
Consumer failure
   ↓
Coordinator detects membership loss
   ↓
Rebalance
   ↓
Partitions reassigned
   ↓
New consumer resumes from committed offsets
```

### Q5. What is the difference between eager and cooperative rebalancing?

- **Eager:** broadly revoke existing assignments and then redistribute.
- **Cooperative:** incrementally revoke and move only the partitions necessary for the new assignment.

### Q6. Why can rebalancing hurt performance?

> Because partition revocation/reassignment can temporarily interrupt consumption and cause processing pauses, increasing latency and consumer lag.

---

## Most important concepts to connect

You should now connect these concepts together:

```
Partition
   ↓
Partition Assignment
   ↓
Consumer Group
   ↓
Consumer Membership
   ↓
Rebalancing
   ↓
Offset
   ↓
Processing Continuity
   ↓
At-Least-Once Delivery
```

And the overall Kafka flow is:

```
Producer
   |
   v
 Topic
   |
   +---- P0
   +---- P1
   +---- P2
   +---- P3
   |
   v
Consumer Group
   |
   v
Partition Assignment
   |
   v
Consumers
   |
   v
 poll()
   |
   v
Process Event
   |
   v
Commit Offset
```

---

## The key interview sentence

> **Kafka rebalancing ensures that partitions are redistributed among active consumers in a consumer group when membership or assignment changes. It provides fault tolerance and scalability, but frequent rebalances can cause processing disruption, increased latency, and consumer lag.**

