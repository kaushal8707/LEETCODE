# How to Choose Kafka Partitions

Choosing the right number of Kafka partitions is one of the most important Kafka design decisions because partitions determine:

- Consumer parallelism
- Maximum processing concurrency
- Ordering boundaries
- Throughput
- Scalability
- Broker storage/network distribution
- Future flexibility

A senior-level answer should not be:

> "Choose partitions based on message count."

Instead:

> **Choose partitions based on throughput, consumer parallelism, ordering requirements, and future growth, while validating broker and downstream capacity.**

---

## 1. First understand what a partition controls

Suppose:

```
Topic: orders
```

```
        Kafka Topic
             |
    +--------+--------+--------+
    |                 |        |
   P0                P1       P2
```

Each partition is an independent ordered log.

Consumers in the same consumer group can process different partitions concurrently:

```
P0 ──────> Consumer 1
P1 ──────> Consumer 2
P2 ──────> Consumer 3
```

Therefore:

> **Partitions are Kafka's fundamental unit of parallelism.**

---

## 2. The most important formula

A useful starting point is:

```
Required partitions
≈
max(
    throughput requirement,
    consumer parallelism requirement
)
```

But this is only a starting point.

You also need to consider:

```
Partitions
   ↓
Producer throughput
   ↓
Consumer throughput
   ↓
Downstream capacity
   ↓
Ordering requirements
   ↓
Broker capacity
   ↓
Future growth
```

---

## 3. Method 1 — Calculate based on throughput

Suppose your topic receives:

```
100 MB/sec
```

You benchmark your consumer and discover:

```
1 consumer processing 1 partition
→ 20 MB/sec
```

Then:

```
Required partitions
=
100 / 20
=
5
```

So you need at least approximately:

```
5 partitions
```

You might choose:

```
6 or 8 partitions
```

to provide headroom.

---

## 4. Example

Suppose:

```
Incoming traffic = 200 MB/sec

One consumer can process
= 25 MB/sec per partition
```

Then:

```
200 / 25 = 8
```

So:

```
8 partitions
```

is the theoretical minimum based on that benchmark.

For production, you might choose:

```
12 partitions
```

if future growth and operational flexibility justify it.

---

## 5. But throughput isn't enough

Imagine:

```
Traffic = 100 MB/sec
Consumer capacity = 50 MB/sec
```

You might calculate:

```
100 / 50 = 2 partitions
```

But suppose you want:

```
8 consumer instances
```

for parallel processing and failover.

Two partitions won't work.

You would need at least:

```
8 partitions
```

because:

```
1 partition
→ max 1 active consumer in a consumer group
```

Therefore:

```
Required partitions
>= desired active consumers
```

---

## 6. Consumer parallelism rule

Suppose:

```
Partitions = 6
Consumers = 10
```

You get approximately:

```
P0 → C1
P1 → C2
P2 → C3
P3 → C4
P4 → C5
P5 → C6

C7 → idle
C8 → idle
C9 → idle
C10 → idle
```

So:

```
Maximum active consumers = 6
```

Not 10.

---

## 7. What if consumers are fewer than partitions?

Suppose:

```
Partitions = 12
Consumers = 4
```

Then:

```
C1 → P0 P1 P2
C2 → P3 P4 P5
C3 → P6 P7 P8
C4 → P9 P10 P11
```

This is perfectly valid.

So you don't need:

```
consumers == partitions
```

You need enough partitions to support the **maximum desired parallelism**.

---

## 8. The second major factor: ordering

This is where partition selection becomes a system-design decision.

Suppose your requirement is:

> All events for one order must be processed in order.

Events:

```
OrderCreated
PaymentCompleted
OrderShipped
OrderDelivered
```

You should use:

```
key = orderId
```

Then:

```
Order 101
    ↓
Partition 3

Order 102
    ↓
Partition 7

Order 103
    ↓
Partition 1
```

Now:

```
Order 101:
Created
   ↓
Paid
   ↓
Shipped
   ↓
Delivered
```

remains ordered within its partition.

---

## 9. Therefore, ask this question first

Before calculating partitions, ask:

> **What entity needs ordering?**

Examples:

| Requirement | Good partition key |
|---|---|
| Order events ordered | `orderId` |
| Customer events ordered | `customerId` |
| Account transactions ordered | `accountId` |
| Device events ordered | `deviceId` |
| Shipment events ordered | `shipmentId` |

Then:

```
Entity requiring ordering
          ↓
      Partition key
          ↓
    Partition count
```

---

## 10. The partition key must also distribute traffic

Suppose you choose:

```
country
```

as the key.

You have:

```
India → 70% traffic
USA   → 10%
UK    → 5%
...
```

Now one partition may receive a huge amount of traffic.

```
P0 → 70% traffic 🔥
P1 → 5%
P2 → 5%
P3 → 5%
...
```

This creates a:

```
Hot partition
```

You may have 20 partitions but effectively only one is doing most of the work.

---

## 11. Hot partition problem

Suppose:

```
20 partitions
```

but:

```
Partition 7 → 60% traffic
Other 19 → 40%
```

Your theoretical parallelism is:

```
20
```

but your effective throughput is limited by:

```
Partition 7
```

This is why:

> Number of partitions alone does not guarantee parallelism.

The key must distribute traffic reasonably well.

---

## 12. The ideal partition key

A good partition key should satisfy three things:

```
               Good Key
                  |
        +---------+---------+
        |         |         |
     Ordering  Distribution Stability
```

### 1. Ordering

Same entity → same partition.

### 2. Distribution

Traffic should be spread across partitions.

### 3. Stability

The key should represent a stable business identity.

For example:

```
orderId
```

is usually better than:

```
orderStatus
```

---

## 13. Example: Order Service

Suppose:

```
Traffic = 500,000 events/sec
```

Requirement:

```
Order events must be ordered.
```

Choose:

```
key = orderId
```

Now benchmark:

```
1 partition
→ 50,000 events/sec consumer processing capacity
```

Then:

```
500,000 / 50,000
=
10 partitions
```

Theoretical minimum:

```
10 partitions
```

You might choose:

```
12 or 16
```

depending on expected growth and operational requirements.

---

## 14. Think about future growth

Suppose today:

```
100,000 events/sec
```

but you expect:

```
500,000 events/sec
```

in two years.

You could initially calculate:

```
100,000 / 20,000
=
5 partitions
```

But choosing exactly 5 may leave little room.

You might choose:

```
12 or 16 partitions
```

if the broker and operational cost are acceptable.

The goal is not:

> "Choose the smallest possible number."

The goal is:

> **Choose enough partitions for current requirements plus reasonable future growth without creating unnecessary operational overhead.**

---

## 15. Why not simply choose 1,000 partitions?

Because more partitions are not free.

More partitions mean more:

- Metadata
- File handles
- Replication work
- Leader management
- Network connections
- Recovery work
- Rebalance work
- Broker resources

For example:

```
Topic
1000 partitions
RF=3
```

means:

```
1000 × 3
=
3000 replicas
```

That's a significant operational footprint.

So:

> More partitions provide potential parallelism, but also increase Kafka's operational overhead.

---

## 16. Partition count and replication

Suppose:

```
Partitions = 100
Replication Factor = 3
```

Then Kafka maintains:

```
100 × 3
=
300 replicas
```

distributed across brokers.

If you increase to:

```
Partitions = 500
RF = 3
```

you now have:

```
500 × 3
=
1500 replicas
```

So partition count affects replication overhead significantly.

---

## 17. Partition count and brokers

Suppose you have:

```
6 brokers
```

and:

```
120 partitions
RF=3
```

Kafka distributes replicas across brokers.

Conceptually:

```
Broker 1 → replicas
Broker 2 → replicas
Broker 3 → replicas
Broker 4 → replicas
Broker 5 → replicas
Broker 6 → replicas
```

You want a reasonably balanced distribution of:

```
Partition leaders
+
Partition replicas
+
Traffic
```

---

## 18. Leader distribution matters

Remember:

> Each partition has one leader.

Client traffic normally goes to the partition leader.

Suppose:

```
100 partitions
```

but most leaders are concentrated poorly on a broker.

You can end up with:

```
Broker 1 → 80 leaders 🔥
Broker 2 → 5
Broker 3 → 5
Broker 4 → 5
Broker 5 → 5
```

Then Broker 1 becomes a bottleneck.

So partition planning should consider:

```
Partition count
+
Replica placement
+
Leader distribution
```

---

## 19. Producer throughput also matters

Don't only benchmark consumers.

Suppose:

```
Producer
→ 500 MB/sec
```

and each partition can handle approximately:

```
50 MB/sec
```

Then you need roughly:

```
500 / 50
=
10 partitions
```

Again, this is only a starting point because actual throughput depends on:

- batch size
- compression
- message size
- acknowledgements
- broker hardware
- replication
- network
- producer configuration.

---

## 20. Message size matters

Consider:

```
1 million messages/sec
```

### Case A:

```
1 KB/message
```

Traffic:

```
~1 GB/sec
```

### Case B:

```
10 KB/message
```

Traffic:

```
~10 GB/sec
```

Same message count, completely different infrastructure requirements.

Therefore don't estimate partitions using message count alone.

Consider:

```
messages/sec
+
average message size
+
batching
+
compression
+
replication
```

---

## 21. Consumer processing time matters

Suppose each message takes:

```
10 ms
```

to process.

If processing involves:

```
Kafka
 ↓
DB
 ↓
REST API
 ↓
Kafka
```

your throughput may be limited by downstream I/O.

Adding partitions may help:

```
1 partition → 100 msg/sec
4 partitions → ~400 msg/sec
```

until the DB/API becomes saturated.

Then:

```
8 partitions
→ DB overloaded
```

So:

> Kafka partition parallelism cannot compensate indefinitely for downstream bottlenecks.

---

## 22. A useful practical formula

For a first estimate:

```
P_throughput =
ceil(
    required throughput /
    sustainable throughput per partition
)
```

Then:

```
P_parallelism =
desired maximum consumer concurrency
```

Then start with:

```
P =
max(
    P_throughput,
    P_parallelism
)
```

Then validate:

```
P
 ↓
Ordering
 ↓
Key distribution
 ↓
Broker capacity
 ↓
Replication
 ↓
Downstream capacity
 ↓
Future growth
```

---

## 23. Real production example

Suppose you're designing:

```
Payment Events
```

Requirements:

```
Peak = 200,000 events/sec

Average event size = 2 KB

Ordering = per account

Consumer processing capacity =
20,000 events/sec per partition

Maximum desired consumers = 16
```

### Step 1: Throughput

```
200,000 / 20,000
=
10 partitions
```

### Step 2: Consumer parallelism

Need:

```
16 consumers
```

Therefore:

```
P >= 16
```

### Step 3: Choose

A reasonable starting point could be:

```
16 partitions
```

or potentially:

```
24 partitions
```

if growth and operational capacity justify it.

### Step 4: Key

Use:

```
accountId
```

because:

```
Same account
    ↓
Same partition
    ↓
Ordered processing
```

### Step 5: Validate distribution

Check whether:

```
accountId
```

produces reasonably balanced traffic.

If one account generates enormous traffic:

```
Account A → 40% traffic
```

you have a hot-key problem.

---

## 24. The hot-key dilemma

This is a senior-level interview topic.

Suppose:

```
accountId = A
```

generates:

```
40% of all events
```

If all events must be ordered:

```
A
 ↓
one partition
```

You cannot simply split A across multiple partitions without changing your ordering guarantee.

You have a trade-off:

```
Strong ordering
       vs
Higher parallelism
```

Possible approaches depend on business requirements.

For example:

```
A → shard-0
A → shard-1
A → shard-2
```

can increase parallelism, but now events for A may be processed concurrently/out of order.

So you might instead redesign the ordering requirement:

> Do we really need global ordering for the entire account, or only ordering for individual transactions/orders?

This is a business requirement question, not merely a Kafka configuration question.

---

## 25. Partition count and ordering

This is another important point.

Suppose:

```
orders topic
4 partitions
```

You increase it to:

```
8 partitions
```

Existing records don't magically move.

For future records, key-based partitioning can map keys differently because the partition count changed.

Therefore:

> Changing partition count can affect key-to-partition mapping for future records.

This matters when you rely heavily on key-based ordering.

---

## 26. Can you reduce partitions?

This is an important operational consideration.

Kafka supports **increasing** a topic's partition count, but **reducing** partition count is not a normal supported operation.

Therefore:

```
Choose too few
     ↓
Later increase
     ↓
Possible
```

But:

```
Choose too many
     ↓
Later reduce
     ↓
Not normally supported
```

This is another reason partition planning matters.

---

## 27. How many partitions should I choose?

There is no universal number such as:

- "Always use 10"
- "Always use 50"
- "Always use 100"

Anyone giving you a universal number is oversimplifying.

Instead use this process:

```
Step 1
↓
Determine peak throughput

Step 2
↓
Determine message size

Step 3
↓
Benchmark throughput per partition

Step 4
↓
Calculate partitions for throughput

Step 5
↓
Determine required consumer parallelism

Step 6
↓
Choose ordering key

Step 7
↓
Check key distribution / hot partitions

Step 8
↓
Check broker + replication capacity

Step 9
↓
Add reasonable growth headroom

Step 10
↓
Load test and monitor
```

---

## 28. What should you monitor after deployment?

Partition planning doesn't end after creating the topic.

Monitor:

- Consumer lag
- Partition throughput
- Partition size
- Broker CPU
- Broker disk I/O
- Network utilization
- Leader distribution
- Replica distribution
- Under-replicated partitions
- Hot partitions
- Consumer rebalance frequency
- Producer latency

Especially:

```
Partition traffic distribution
```

Example:

```
P0 → 10 MB/s
P1 → 11 MB/s
P2 → 9 MB/s
P3 → 10 MB/s
P4 → 10 MB/s
```

Good.

But:

```
P0 → 80 MB/s 🔥
P1 → 5 MB/s
P2 → 5 MB/s
P3 → 4 MB/s
...
```

You have a key distribution problem.

---

## 29. Partition count vs consumer count

Remember this table:

| Partitions | Consumers | Active consumers |
|---|---|---|
| 3 | 1 | 1 |
| 3 | 3 | 3 |
| 3 | 5 | 3 |
| 10 | 5 | 5 |
| 10 | 10 | 10 |
| 10 | 20 | 10 |

Therefore:

```
Active consumers
≤
Partitions
```

for a single consumer group.

---

## 30. A very important distinction

Don't confuse:

```
Partitions
```

with:

```
Consumer threads
```

Suppose:

```
8 partitions
2 consumer instances
```

Each instance can own multiple partitions.

```
Instance 1
 ├── P0
 ├── P1
 ├── P2
 └── P3

Instance 2
 ├── P4
 ├── P5
 ├── P6
 └── P7
```

You can also have multiple consumer threads per JVM, but Kafka's partition assignment remains the fundamental unit of parallel consumption.

---

## 31. Interview scenario

### Interviewer:

> "We have 1 million messages per second. How many partitions will you create?"

Don't immediately answer:

> "100 partitions"

Instead say:

> "I first need to know the average message size, peak rather than average throughput, producer and consumer processing capacity per partition, ordering requirements, desired consumer parallelism, replication factor, broker capacity, and expected growth. I'd benchmark sustainable throughput per partition and calculate the partition count from both throughput and concurrency requirements."

Then:

```
P =
max(
    peak throughput / per-partition capacity,
    required consumer parallelism
)
```

Then add appropriate headroom and validate through load testing.

That's a much stronger 10-year experience interview answer.

---

## 32. Partition selection decision tree

```
                 Start
                   |
                   v
          What needs ordering?
                   |
                   v
            Identify entity
                   |
                   v
          Choose partition key
                   |
                   v
       Is key distribution balanced?
             /             \
           No               Yes
           |                 |
     Change key /      Continue
     redesign ordering
                             |
                             v
                  Calculate throughput
                             |
                             v
                  Benchmark per partition
                             |
                             v
                  Calculate P_throughput
                             |
                             v
                 Desired consumer count
                             |
                             v
                    Calculate P_parallelism
                             |
                             v
                 P = max(Pt, Pc)
                             |
                             v
                  Add growth headroom
                             |
                             v
             Validate broker/RF capacity
                             |
                             v
                       Load test
                             |
                             v
                      Finalize count
```

---

## 33. The 5 questions I would ask before choosing partitions

For any Kafka topic, ask:

### 1. What is the peak throughput?

```
events/sec
MB/sec
```

### 2. What entity needs ordering?

```
orderId?
customerId?
accountId?
deviceId?
```

### 3. How much can one partition sustainably handle?

You should benchmark, not blindly assume a fixed number.

### 4. How much consumer parallelism do we need?

```
10 consumers?
50?
100?
```

### 5. Is the partition key evenly distributed?

Check for:

- hot keys
- skew
- hot partitions

---

## 34. Golden rule

For your system-design interviews, remember:

> **Partition count is primarily a scalability and parallelism decision, while partition key is primarily an ordering and distribution decision.**

Then combine them:

```
Partition Key
     ↓
Ordering + Distribution

Partition Count
     ↓
Throughput + Parallelism

Together
     ↓
Scalable Kafka Design
```

---

## Final mental model

```
                    Kafka Topic
                        |
                 Choose Partition Key
                        |
              +---------+---------+
              |                   |
          Ordering           Distribution
              |                   |
              +---------+---------+
                        |
                  Choose Count
                        |
              +---------+---------+
              |                   |
          Throughput         Parallelism
              |                   |
              +---------+---------+
                        |
                 Broker Capacity
                        |
                 Replication / RF
                        |
                  Future Growth
                        |
                    Load Test
```

---

## In one sentence:

> **Choose the partition key based on the business entity that requires ordering, then choose enough partitions to satisfy peak throughput and desired consumer parallelism, while ensuring the key distributes traffic well and the brokers/downstream systems can handle the resulting load.**

