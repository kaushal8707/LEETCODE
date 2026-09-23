# Kafka Partition Assignment

**Partition assignment** is the process by which Kafka decides which consumer in a consumer group should consume which partitions.

This is a very important Kafka concept because it determines **parallelism, load distribution, rebalancing, and consumer scalability**.

---

## 1. Start with the basic picture

Suppose we have a topic:

```
Topic: order-events

P0
P1
P2
P3
P4
P5
```

And one consumer group:

```
Consumer Group: payment-service

C1
C2
C3
```

Kafka assigns partitions to consumers:

```
P0 ─────> C1
P1 ─────> C1

P2 ─────> C2
P3 ─────> C2

P4 ─────> C3
P5 ─────> C3
```

Now each consumer can process its assigned partitions independently.

---

## 2. The most important rule

Within a consumer group:

> A partition can be assigned to only one consumer at a time.

For example:

```
P0 ──> C1
```

You cannot have:

```
P0 ──> C1
P0 ──> C2
```

within the same consumer group.

However, another consumer group can independently consume P0:

```
                 P0
                /  \
               /    \
              v      v
       Payment CG   Analytics CG
          C1           C1
```

That's why multiple consumer groups can consume the same Kafka topic independently.

---

## 3. Who performs partition assignment?

This is where Kafka internals become interesting.

Kafka consumers belong to a consumer group.

One consumer in the group acts as the group coordinator participant that performs assignment using the configured assignor (with modern Kafka group protocols, assignment can also be coordinated differently depending on the protocol).

For the classic consumer group protocol, conceptually:

```
Consumer Group
       |
       v
   Coordinator
       |
       v
 Group leader consumer
       |
       v
Partition Assignor
       |
       v
Partition assignments
```

For interview purposes, understand the classic flow first because it explains the traditional partition-assignment strategies.

---

## 4. Example

Suppose:

```
Topic = order-events

Partitions = 6

P0 P1 P2 P3 P4 P5
```

Consumer group:

```
payment-service

C1
C2
C3
```

Kafka needs to decide:

- Who gets P0?
- Who gets P1?
- Who gets P2?
- ...

A **partition assignment strategy** answers those questions.

---

## 5. What happens when consumers join?

Initially:

```
P0 P1 P2 P3 P4 P5

       C1
```

C1 consumes all partitions:

```
P0 ──┐
P1 ──┤
P2 ──┤
P3 ──┤──> C1
P4 ──┤
P5 ──┘
```

Now C2 starts.

Kafka detects a group membership change.

A **rebalance** occurs.

Possible result:

```
P0 P1 P2 ──> C1
P3 P4 P5 ──> C2
```

Now C3 joins:

```
P0 P1 ──> C1
P2 P3 ──> C2
P4 P5 ──> C3
```

So partition assignment can change whenever group membership or subscription information changes.

---

## 6. What is a rebalance?

A **rebalance** happens when Kafka needs to redistribute partitions among consumers in a consumer group.

Common triggers include:

### Consumer joins

```
C1
C2  <-- new
```

### Consumer leaves

```
C1
C2  <-- gone
```

### Consumer crashes

```
C1
C2  X
```

### Consumer exceeds `max.poll.interval.ms`

A consumer may be considered unhealthy if it doesn't call `poll()` within the configured interval.

### Topic partition count changes

If partitions are added, assignment may need to be recalculated.

---

## 7. Example of rebalance

Before:

```
Topic: order-events

P0 ──> C1
P1 ──> C1
P2 ──> C2
P3 ──> C2
```

C2 crashes:

```
P0 ──> C1
P1 ──> C1
P2 ──> ???
P3 ──> ???
```

Kafka detects C2's failure.

Then rebalances:

```
P0 ──> C1
P1 ──> C1
P2 ──> C1
P3 ──> C1
```

Now C1 owns all four partitions.

---

## 8. Partition assignment strategies

This is an important interview topic.

Kafka has historically provided several assignment strategies, including:

- **RangeAssignor**
- **RoundRobinAssignor**
- **StickyAssignor**
- **CooperativeStickyAssignor**

Modern Kafka also has newer consumer-group protocol capabilities, so exact defaults and mechanics depend on the Kafka client/broker version and configuration.

Let's understand the classic strategies.

---

## 9. RangeAssignor

Suppose:

Partitions:

```
P0
P1
P2
P3
P4
P5
```

Consumers:

```
C1
C2
C3
```

Range assignment divides partitions into ranges.

Result might be:

```
C1 -> P0 P1
C2 -> P2 P3
C3 -> P4 P5
```

Simple.

### Problem

Range assignment can cause uneven distribution in some situations.

For example, suppose:

```
Topic A = 5 partitions
Topic B = 5 partitions

Consumers:
C1
C2
C3
```

Range assignment is performed **per topic**, which can result in one consumer receiving more partitions overall.

---

## 10. RoundRobinAssignor

Round robin distributes partitions sequentially.

Suppose:

```
P0 P1 P2 P3 P4 P5
```

Consumers:

```
C1 C2 C3
```

Assignment:

```
P0 -> C1
P1 -> C2
P2 -> C3
P3 -> C1
P4 -> C2
P5 -> C3
```

So:

```
C1 -> P0 P3
C2 -> P1 P4
C3 -> P2 P5
```

This generally gives a more balanced distribution.

---

## 11. StickyAssignor

Sticky assignment tries to achieve two things simultaneously:

1. Balance partitions.
2. Minimize unnecessary partition movement during rebalances.

Suppose initially:

```
C1 -> P0 P1
C2 -> P2 P3
C3 -> P4 P5
```

Now C3 leaves.

A naive strategy might completely reshuffle:

```
C1 -> P2 P4
C2 -> P0 P1 P3 P5
```

That causes a lot of movement.

Sticky assignment tries to preserve existing assignments where possible:

```
C1 -> P0 P1 P4
C2 -> P2 P3 P5
```

The exact result depends on the assignor's algorithm and subscriptions.

The important idea is:

> Keep existing assignments whenever possible while maintaining balance.

---

## 12. CooperativeStickyAssignor

This is particularly important for modern Kafka deployments.

Traditional rebalancing can involve a **stop-the-world** style reassignment where consumers temporarily revoke many partitions.

**Cooperative rebalancing** attempts to make partition movement more incremental.

For example:

Before:

```
C1 -> P0 P1 P2
C2 -> P3 P4 P5
```

C3 joins.

Instead of immediately revoking everything, cooperative rebalancing can incrementally move only the partitions that need to move:

```
C1 -> P0 P1
C2 -> P3 P4
C3 -> P2 P5
```

The exact intermediate steps depend on the group state and assignment.

### Why is this useful?

**Less disruption.**

Potential benefits include:

- less processing interruption
- fewer unnecessary partition revocations
- better availability
- smoother rebalances

---

## 13. Consumer count vs partition count

This is one of the most frequently asked interview questions.

### Case 1: Consumers < partitions

```
6 partitions
3 consumers
```

Possible:

```
C1 -> P0 P1
C2 -> P2 P3
C3 -> P4 P5
```

Every consumer gets work.

### Case 2: Consumers = partitions

```
6 partitions
6 consumers
```

Possible:

```
C1 -> P0
C2 -> P1
C3 -> P2
C4 -> P3
C5 -> P4
C6 -> P5
```

Maximum parallelism for that group is achieved.

### Case 3: Consumers > partitions

```
6 partitions
8 consumers
```

Some consumers will be idle:

```
C1 -> P0
C2 -> P1
C3 -> P2
C4 -> P3
C5 -> P4
C6 -> P5

C7 -> idle
C8 -> idle
```

Therefore:

> Adding more consumers than partitions does not increase parallelism for that consumer group.

---

## 14. Very important: Partition assignment is per consumer group

Suppose:

```
Topic:
P0 P1 P2 P3
```

Consumer Group A:

```
C1 -> P0 P1
C2 -> P2 P3
```

Consumer Group B:

```
C3 -> P0 P1 P2 P3
```

That's completely valid.

So the same partition can be consumed by:

```
Payment Group    -> P0
Analytics Group  -> P0
Fraud Group      -> P0
```

at the same time.

The restriction is:

> One partition → one consumer within a particular consumer group.

---

## 15. Partition assignment and ordering

Suppose:

```
P0

Offset 100 -> OrderCreated
Offset 101 -> PaymentCompleted
Offset 102 -> OrderShipped
```

If C1 owns P0:

```
P0 -> C1
```

C1 processes the partition in order.

If later P0 moves to C2:

```
P0 -> C2
```

C2 continues from the consumer group's committed offset.

This is why partition assignment is directly related to **message ordering**.

Kafka guarantees ordering within a partition, not globally across a topic with multiple partitions.

---

## 16. How Kafka decides which partition to assign

Don't confuse two different concepts:

### Partition selection

Producer asks:

> Which partition should this record go to?

For example:

```
Producer
   |
   +-- key = customerId
   |
   v
Partitioner
   |
   v
P2
```

### Partition assignment

Consumer group asks:

> Which consumer should consume P2?

```
P2
 |
 v
Partition Assignor
 |
 v
Consumer C2
```

These are completely different operations.

Producer side:

```
Record
  |
  v
Partitioner
  |
  v
Partition
```

Consumer side:

```
Partition
  |
  v
Assignment strategy
  |
  v
Consumer
```

This distinction is very important in interviews.

---

## 17. Complete flow

Let's put everything together.

```
                  PRODUCER
                      |
                      v
                Partitioner
                      |
              +-------+-------+
              |       |       |
             P0      P1      P2
              |       |       |
              +-------+-------+
                      |
                 Kafka Cluster
                      |
                      v
              Consumer Group
                      |
                Partition
                 Assignor
                      |
          +-----------+-----------+
          |           |           |
          v           v           v
         C1          C2          C3
```

The producer determines:

> Which partition?

The consumer group determines:

> Which consumer?

---

## 18. Interview question: "What happens when a consumer dies?"

A strong answer:

> When a consumer in a consumer group fails, Kafka detects that the consumer has left or stopped heartbeating according to the group membership mechanism. The group membership changes, triggering a rebalance. The group's partition assignment is recalculated, and the partitions previously owned by the failed consumer are assigned to other consumers. Those consumers continue processing from the appropriate committed offsets.

Example:

Before:

```
P0 -> C1
P1 -> C1
P2 -> C2
P3 -> C2
```

C2 dies:

```
P0 -> C1
P1 -> C1
P2 -> ?
P3 -> ?
```

After rebalance:

```
P0 -> C1
P1 -> C1
P2 -> C1
P3 -> C1
```

---

## 19. Interview question: "What happens when a new consumer joins?"

Before:

```
P0 -> C1
P1 -> C1
P2 -> C1
P3 -> C2
P4 -> C2
P5 -> C2
```

C3 joins.

Rebalance occurs.

Possible result:

```
P0 -> C1
P1 -> C1

P2 -> C2
P3 -> C2

P4 -> C3
P5 -> C3
```

The goal is to distribute partitions among the available consumers according to the assignment strategy.

---

## 20. Interview question: "Can two consumers consume the same partition?"

### Same consumer group?

**No, not concurrently as assigned members.**

```
P0 -> C1
```

not:

```
P0 -> C1
P0 -> C2
```

### Different consumer groups?

**Yes.**

```
             P0
           /    \
          v      v
        CG-A    CG-B
         C1      C2
```

---

## 21. The key relationship

Remember this equation:

> **Partitions = Parallelism**

More accurately:

> For a single consumer group, the number of partitions places an upper bound on how many consumers can actively consume partitions in parallel.

For example:

```
Topic
  |
  +-- 12 partitions
          |
          v
Consumer Group
  |
  +-- 12 active consumers maximum
```

Adding a 13th consumer doesn't give you a 13th partition to process.

---

## 22. Partition assignment vs rebalancing

These two concepts should be kept separate:

### Partition Assignment

```
P0 -> C1
P1 -> C1
P2 -> C2
P3 -> C2
```

It answers:

> Who owns what?

### Rebalancing

```
C2 dies
   |
   v
assignment changes
   |
   v
P2 -> C1
P3 -> C1
```

It answers:

> When membership changes, how do we redistribute ownership?

---

## 23. Interview-ready answer

If the interviewer asks:

> What is Kafka partition assignment?

You can answer:

> Partition assignment is the mechanism by which Kafka assigns topic partitions to consumers within a consumer group. Each partition is assigned to at most one consumer in a group at a time. Kafka uses an assignment strategy such as Range, RoundRobin, Sticky, or CooperativeSticky to distribute partitions. When consumers join, leave, fail, or group membership changes, Kafka can rebalance the assignments. The number of partitions effectively determines the maximum parallelism available to a consumer group.

---

## The mental model to remember

```
             TOPIC
               |
       +-------+-------+
       |       |       |
      P0      P1      P2
       |       |       |
       +-------+-------+
               |
        Consumer Group
               |
        Partition Assignor
               |
       +-------+-------+
       |       |       |
      C1      C2      C3
```

- **Producer** → chooses partition
- **Consumer Group** → gets partition assignment
- **Partition** → unit of parallelism
- **Offset** → position within partition
- **Rebalance** → changes the assignment
- **Consumer Group** → isolates consumption from other groups

---

The next topic that naturally follows is **Kafka Consumer Rebalancing**, including eager vs cooperative rebalancing, heartbeats, `session.timeout.ms`, `heartbeat.interval.ms`, `max.poll.interval.ms`, static membership, and what exactly happens internally when a consumer crashes.

