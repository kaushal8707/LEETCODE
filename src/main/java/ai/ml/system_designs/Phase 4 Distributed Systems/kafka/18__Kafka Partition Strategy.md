# Kafka Partition Strategy

**Partition strategy** is the approach you use to decide which partition a Kafka record should go to.

This decision is extremely important because partitioning directly affects:

- Ordering
- Parallelism
- Throughput
- Load distribution
- Consumer scalability
- Hot partitions
- Future scalability

For a senior/system-design interview, think of partition strategy as:

> How do I distribute events across partitions while preserving the ordering guarantees my business requires?

---

## 1. First understand the flow

Suppose we have:

```
Producer
   |
   | Record
   | key = orderId
   v
Partitioner
   |
   +--------+--------+--------+
   |        |        |        |
   v        v        v        v
  P0       P1       P2       P3
```

The producer's partitioner determines the destination partition.

Conceptually:

```
Record
  |
  +-- topic
  +-- key
  +-- value
  +-- headers
  |
  v
Partitioner
  |
  v
Partition
```

---

## 2. Main partitioning strategies

The major strategies you should know are:

1. Key-based partitioning
2. Round-robin / non-key distribution
3. Custom partitioning
4. Sticky partitioning for null keys
5. Random/hash-based strategies

In real systems, **key-based partitioning** is usually the most important one to understand.

---

## 3. Strategy #1 — Key-based partitioning

Suppose:

```
topic = orders
key = orderId
```

The producer uses the key to determine the partition.

Conceptually:

```
partition = hash(key) % numberOfPartitions
```

For example:

```
orderId = ORD-101
       ↓
     hash()
       ↓
     78234
       ↓
78234 % 4
       ↓
      P2
```

So:

```
ORD-101 → P2
```

---

## 4. Why use a key?

The biggest reason is **ordering**.

Suppose an order has these events:

```
OrderCreated
PaymentCompleted
OrderShipped
OrderDelivered
```

You want:

```
Created
   ↓
PaymentCompleted
   ↓
Shipped
   ↓
Delivered
```

If you use:

```
key = orderId
```

then all events for that order are normally routed to the same partition.

```
OrderCreated
     |
     | key = ORD-101
     v
    P2

PaymentCompleted
     |
     | key = ORD-101
     v
    P2

OrderShipped
     |
     | key = ORD-101
     v
    P2
```

Kafka preserves their order within P2.

---

## 5. Same key → same partition

Suppose:

```
Partitions = 4
```

and:

```
ORD-101
ORD-102
ORD-103
ORD-104
```

The mapping could look like:

```
ORD-101 → P2
ORD-102 → P0
ORD-103 → P3
ORD-104 → P1
```

Then:

```
P0 → ORD-102
P1 → ORD-104
P2 → ORD-101
P3 → ORD-103
```

Different orders can therefore be processed in parallel.

This gives you:

> **Ordering per entity + parallelism across entities**

That's one of the most important Kafka partitioning patterns.

---

## 6. Real-world example: Payment

Imagine:

```
paymentId = PAY-1001
```

Events:

```
PaymentCreated
PaymentAuthorized
PaymentCaptured
PaymentCompleted
```

Use:

```
key = paymentId
```

Then:

```
PAY-1001
   |
   v
Hash
   |
   v
P4
```

All events go to:

```
P4
```

Therefore:

```
P4
 |
 +-- PaymentCreated
 +-- PaymentAuthorized
 +-- PaymentCaptured
 +-- PaymentCompleted
```

while other payments can go to other partitions.

---

## 7. Choosing the right key

This is where senior-level design becomes important.

The question isn't:

> "What key can I use?"

The question is:

> **"What entity must remain ordered?"**

Examples:

| Requirement | Good key |
|---|---|
| Order events ordered | `orderId` |
| Payment events ordered | `paymentId` |
| Customer events ordered | `customerId` |
| Account transactions ordered | `accountId` |
| Shipment events ordered | `shipmentId` |
| Device telemetry ordered | `deviceId` |
| User activity ordered | `userId` |

---

## 8. The key should represent the ordering boundary

Suppose the requirement is:

> All events for the same customer must be processed in order.

Then:

```
key = customerId
```

Not:

```
key = eventType
```

and usually not:

```
key = timestamp
```

because the business ordering requirement is based on the customer.

Conceptually:

```
Customer A
   |
   +-- Created
   +-- Updated
   +-- Payment
   +-- AddressChanged
   |
   v
Same partition
```

---

## 9. Strategy #2 — Null key

What happens when:

```
key = null
```

?

The producer uses its configured partitioning behavior for records without keys. In modern Kafka producers, this commonly involves **sticky partitioning** for batching rather than assuming simple round-robin behavior.

Conceptually:

```
Record A → P1
Record B → P1
Record C → P1
Record D → P1
       ↓
   Batch becomes full
       ↓
Producer switches partition
       ↓
Record E → P3
Record F → P3
```

The exact behavior depends on the Kafka client/version/configuration, so don't state in an interview that:

> "Null keys always use round-robin."

That is an outdated oversimplification.

---

## 10. Why sticky partitioning?

The goal is **better batching**.

Instead of:

```
A → P0
B → P1
C → P2
D → P3
```

the producer can build a larger batch:

```
A → P0
B → P0
C → P0
D → P0
```

Then:

```
Batch
  ↓
Compress
  ↓
Send
```

This can improve:

- batching
- compression
- network efficiency
- throughput

---

## 11. Strategy #3 — Round-robin

Conceptually:

```
P0
P1
P2
P3
```

Records are distributed across partitions:

```
R1 → P0
R2 → P1
R3 → P2
R4 → P3
R5 → P0
R6 → P1
```

The goal is:

```
Even distribution
```

But there's an important downside.

Suppose:

```
Order 101
```

has:

```
Created
Paid
Shipped
```

If these records are distributed independently:

```
Created → P0
Paid    → P1
Shipped → P2
```

you lose per-order partition ordering.

So round-robin/non-key distribution is appropriate when:

- ordering isn't required, and
- distributing load evenly is more important.

---

## 12. Strategy #4 — Custom partitioner

Sometimes the standard key-based approach isn't enough.

You can implement business-specific partitioning logic.

For example:

```
if customerType == VIP:
    partition = 0

else:
    partition = hash(customerId) % N
```

But be very careful.

This can create:

```
P0 → 70% traffic
P1 → 10%
P2 → 10%
P3 → 10%
```

Now P0 becomes a **hot partition**.

Custom partitioners should therefore have a strong business reason.

---

## 13. Hot partition problem

This is one of the most important partition-strategy problems.

Suppose:

```
10 partitions
```

and your key distribution is:

```
Customer A → 80% of all events
Customer B → 5%
Customer C → 5%
Others     → 10%
```

Hashing might result in:

```
P0 → 80%
P1 → 3%
P2 → 2%
P3 → 3%
...
```

Then:

```
P0
 |
 v
Consumer C0
 |
 X
Overloaded
```

while:

```
P1 → C1 → mostly idle
P2 → C2 → mostly idle
P3 → C3 → mostly idle
```

So although you have 10 partitions:

```
Theoretical parallelism = 10
Effective parallelism ≈ much lower
```

---

## 14. Example: celebrity/customer hotspot

Imagine a social-media application.

You choose:

```
key = userId
```

Normally that's reasonable.

But suppose one celebrity has:

```
userId = 999999
```

and generates:

```
40% of all events
```

Then all those events may go to one partition:

```
Celebrity User
      |
      v
    hash
      |
      v
     P7
      |
      v
  Consumer C7
      |
      X
  HOT PARTITION
```

Increasing consumers won't fix this because:

```
P7
```

can still be assigned to only one consumer in that group.

---

## 15. How do you solve a hot partition?

This is a tricky design problem because you often have two conflicting requirements:

```
Ordering
   vs
Parallelism
```

One approach is to **split the hot key**.

For example:

```
userId = 999999
```

could become:

```
999999-0
999999-1
999999-2
999999-3
```

Then:

```
999999-0 → P1
999999-1 → P4
999999-2 → P6
999999-3 → P9
```

Now processing can be parallelized.

But you've changed the ordering guarantee.

You no longer automatically have:

```
All events for user 999999
        ↓
One ordered stream
```

Instead:

```
999999-0 → P1
999999-1 → P4
999999-2 → P6
999999-3 → P9
```

Therefore, this approach is only safe if the application can tolerate partial ordering or has another mechanism to reconstruct order.

---

## 16. Don't blindly choose high-cardinality keys

You might hear:

> "Always choose a high-cardinality key."

That's incomplete.

A good partition key should satisfy three things:

```
             Good Key
                |
       +--------+--------+
       |        |        |
       v        v        v
 Ordering   Distribution  Stability
```

### 1. Ordering

Does it represent the entity that must be ordered?

### 2. Distribution

Does it distribute traffic reasonably evenly?

### 3. Stability

Will the key remain stable and meaningful throughout the entity lifecycle?

---

## 17. Bad key example

Suppose you use:

```
key = country
```

for an international application.

You might get:

```
India       → 50%
USA         → 20%
UK          → 10%
Germany     → 5%
Others      → 15%
```

This may create uneven partition utilization.

If the requirement is order-level ordering, `country` is also the wrong ordering boundary.

Better:

```
key = orderId
```

---

## 18. Another bad key: event type

Suppose:

```
Created
Updated
Deleted
```

and you use:

```
key = eventType
```

Then:

```
Created → P0
Updated → P1
Deleted → P2
```

This does not preserve the lifecycle order of a particular entity.

Instead:

```
key = entityId
```

is usually more appropriate.

---

## 19. Partition strategy and parallelism

Suppose:

```
Topic = orders
Partitions = 8
Consumers = 8
```

With a good key:

```
Order A → P0
Order B → P1
Order C → P2
Order D → P3
...
```

You can get:

```
8-way parallel processing
```

But if your key distribution is bad:

```
P0 → 70%
P1 → 5%
P2 → 5%
...
```

your actual throughput is limited by P0.

Therefore:

> Number of partitions gives you potential parallelism; partition-key distribution determines how much of that potential you actually use.

---

## 20. Partition strategy and ordering

Think about these three designs:

### Design A — One partition

```
P0
 ↓
All events
```

Advantages:

```
Strong ordering
```

Disadvantage:

```
Very limited parallelism
```

### Design B — Many partitions + random distribution

```
P0 P1 P2 P3
 |  |  |  |
 C0 C1 C2 C3
```

Advantages:

```
High parallelism
```

Disadvantage:

```
No per-entity ordering
```

### Design C — Many partitions + business key

```
Order A → P0
Order B → P2
Order C → P1
Order D → P3
```

Advantages:

```
Ordering per order
+
Parallelism across orders
```

This is usually the best design for entity-based event processing.

---

## 21. Partition count and strategy

Suppose you choose:

```
12 partitions
```

Then maximum consumer parallelism for one consumer group consuming that topic is roughly:

```
12 consumers
```

If you have:

```
20 consumers
```

then:

```
12 active
8 idle
```

So partition count should be chosen based on expected:

- throughput
- consumer processing capacity
- future scaling
- ordering requirements
- broker resources

---

## 22. Can you increase partitions later?

Yes.

But be careful.

Suppose initially:

```
4 partitions
```

and:

```
hash(key) % 4
```

Then:

```
ORD-101 → P2
```

Later you increase to:

```
8 partitions
```

The effective mapping can change:

```
hash(key) % 8
```

and:

```
ORD-101 → P6
```

for future records.

This is important because historical records remain in their original partitions, while future records may map differently.

Therefore:

> Don't treat increasing partition count as completely transparent when your application depends on key-based ordering.

---

## 23. Partition strategy for different use cases

### Order processing

```
key = orderId
```

Goal:

```
Order-level ordering
+
parallelism across orders
```

### Payment processing

```
key = paymentId
```

Goal:

```
Payment lifecycle ordering
```

### Bank account transactions

```
key = accountId
```

Goal:

```
All transactions for an account
→ same partition
```

This is especially important if account-level ordering is required.

### User activity

```
key = userId
```

Goal:

```
User event ordering
```

But watch for high-volume users creating hot partitions.

### Logs

If ordering isn't important:

```
key = null
```

can allow the producer's non-key partitioning strategy to distribute data.

### Analytics

Often:

```
key = customerId
```

or another dimension, depending on how downstream processing needs to aggregate/order the data.

---

## 24. Partition strategy decision tree

Use this mental model in system-design interviews:

```
             Do I need ordering?
                    |
              +-----+-----+
              |           |
             YES          NO
              |           |
              v           v
        What entity       Focus on
        must be ordered?  distribution
              |
              v
        Choose entity key
              |
              v
      Is distribution balanced?
          /           \
        YES            NO
         |              |
         v              v
       Good key      Reconsider key
                         |
                         v
                  Hot partition?
                         |
                    +----+----+
                    |         |
                   YES        NO
                    |         |
                    v         v
              Split key?    Good
              Relax order?
```

---

## 25. Senior-level design example

Suppose you're designing:

```
Amazon-like order processing system
```

Traffic:

```
5 million orders/hour
```

Requirements:

1. Events for the same order must be ordered.
2. Different orders should process in parallel.
3. Consumer service should scale horizontally.
4. No partition should become a major hotspot.

Design:

```
Topic: orders
```

Use:

```
key = orderId
```

Partitioning:

```
hash(orderId) → partition
```

Architecture:

```
                Order Service
                     |
                     |
               key = orderId
                     |
                     v
             +---------------+
             | Kafka orders  |
             +---------------+
              | | | | | | |
              v v v v v v v
             P0 P1 P2 P3 P4 P5 P6
              |  |  |  |  |  |  |
              v  v  v  v  v  v  v
             C0 C1 C2 C3 C4 C5 C6
```

Result:

```
Same order
    ↓
Same partition
    ↓
Ordered

Different orders
    ↓
Different partitions
    ↓
Parallel processing
```

Then monitor:

- Partition traffic
- Consumer lag
- Processing latency
- Partition skew
- Hot partitions
- Rebalances

---

## 26. The most important trade-off

Kafka partition strategy is fundamentally a balance between:

```
        ORDERING
           ↕
       PARTITION KEY
           ↕
      DISTRIBUTION
           ↕
       PARALLELISM
```

You cannot always maximize all three.

For example:

```
Single partition
→ excellent global ordering
→ poor parallelism
```

while:

```
Many partitions + random distribution
→ excellent parallelism
→ poor entity ordering
```

and:

```
Many partitions + business key
→ entity ordering
→ parallelism across entities
→ usually the best compromise
```

---

## 27. Interview questions you should be ready for

### Q1. What is a Kafka partition strategy?

> It is the mechanism used to determine which partition receives a record. The strategy should balance ordering requirements, load distribution, and scalability.

### Q2. What is the most common strategy?

> Key-based partitioning, where the producer uses the record key and a partitioning algorithm to consistently route records with the same key to the same partition.

### Q3. Why use `orderId` as a key?

> To ensure events belonging to the same order are routed to the same partition, preserving their order while allowing different orders to be processed in parallel.

### Q4. What happens if you choose a bad key?

> You can get uneven partition distribution, creating hot partitions and reducing effective parallelism.

### Q5. Can Kafka guarantee global ordering with multiple partitions?

**No.** Kafka guarantees ordering within a partition. Global ordering generally requires a single partition.

### Q6. How do you achieve ordering and parallelism together?

> Partition by the business entity that requires ordering, such as `orderId`. Events for the same entity stay in one partition, while different entities can be distributed across multiple partitions.

### Q7. What is a hot partition?

> A partition receiving disproportionately high traffic compared with other partitions, often caused by skewed key distribution or a very high-volume key.

### Q8. What happens if you increase partition count?

> It increases potential consumer parallelism, but it can change the key-to-partition mapping for future records, so applications relying on key-based ordering need to consider the consequences.

---

## 28. The mental model to remember

```
                 PARTITION STRATEGY
                        |
          +-------------+-------------+
          |             |             |
          v             v             v
        KEY          DISTRIBUTION   PARALLELISM
          |             |             |
          v             v             v
     Ordering       Hot partition   Consumers
          |             |             |
          +-------------+-------------+
                        |
                        v
                  THROUGHPUT
```

And the golden rule:

> **Choose the partition key based on the business entity that needs ordering, then verify that the key distributes traffic evenly enough to avoid hot partitions.**

---

For Kafka interviews, this connects directly to the next level: **custom partitioner → partition assignment → consumer concurrency → hot partitions → rebalancing → `max.poll.records` → `max.poll.interval.ms` → throughput tuning**.

