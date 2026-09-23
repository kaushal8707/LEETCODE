# Kafka Message Ordering

**Message ordering** means the order in which Kafka records are written to and consumed from a partition.

The most important Kafka rule is:

> **Kafka guarantees message ordering within a single partition, but does not guarantee ordering across multiple partitions of a topic.**

This is one of the most important Kafka interview concepts.

---

## 1. Ordering within a partition

Suppose we have one partition:

```
Topic: order-events

Partition 0

Offset
  0  → OrderCreated
  1  → PaymentCompleted
  2  → OrderShipped
  3  → OrderDelivered
```

Kafka stores these records in this sequence.

A consumer reads them in offset order:

```
OrderCreated
      ↓
PaymentCompleted
      ↓
OrderShipped
      ↓
OrderDelivered
```

So:

```
P0:
0 → 1 → 2 → 3
```

is ordered.

---

## 2. Kafka does NOT guarantee ordering across partitions

Suppose:

```
Topic: order-events

P0:
0 → A
1 → B
2 → C

P1:
0 → X
1 → Y
2 → Z
```

Kafka guarantees:

```
A → B → C
```

within P0.

And:

```
X → Y → Z
```

within P1.

But Kafka does **not** guarantee:

```
A → X → B → Y → C → Z
```

There is no global ordering between P0 and P1.

Think of it like two independent queues:

```
P0: A → B → C

P1: X → Y → Z

      ↓
No guaranteed ordering between P0 and P1
```

---

## 3. Why does Kafka use partitions?

Because partitions provide **parallelism**.

Suppose we have:

```
Topic
 |
 +-- P0
 +-- P1
 +-- P2
 +-- P3
```

Consumers can process them concurrently:

```
P0 → Consumer 1
P1 → Consumer 2
P2 → Consumer 3
P3 → Consumer 4
```

This gives high throughput.

But there is a trade-off:

```
More partitions
      ↓
More parallelism
      ↓
But no global ordering
```

So you need to decide:

> Do I need ordering, or do I need maximum parallelism?

---

## 4. Real-world example — Order lifecycle

Consider an e-commerce order:

```
OrderCreated
PaymentCompleted
OrderPacked
OrderShipped
OrderDelivered
```

These events must happen in the correct order.

You don't want:

```
OrderShipped
      ↓
OrderCreated
```

That would be incorrect.

One common approach is to use:

```
key = orderId
```

For example:

```
OrderCreated       key=ORD-100
PaymentCompleted   key=ORD-100
OrderShipped       key=ORD-100
OrderDelivered     key=ORD-100
```

Kafka's partitioning mechanism will route records with the same key to the same partition under the normal keyed-partitioning model.

So:

```
ORD-100
   |
   v
Partition 3

P3:
100 → OrderCreated
101 → PaymentCompleted
102 → OrderShipped
103 → OrderDelivered
```

Now the events for that order maintain their relative order within that partition.

---

## 5. Why use a message key?

The key is extremely important for ordering.

Suppose we have:

```
Order 100
Order 101
Order 102
```

We send:

```
key = orderId
```

Conceptually:

```
Order 100 ──> P0
Order 101 ──> P1
Order 102 ──> P2
```

But all events for Order 100 use the same key:

```
OrderCreated       key=100
PaymentCompleted   key=100
OrderShipped       key=100
```

They are routed consistently to the same partition.

Therefore:

```
P0

OrderCreated
PaymentCompleted
OrderShipped
```

Their relative order is preserved.

---

## 6. Same key does NOT mean global ordering

This is an important distinction.

Suppose:

```
Order 100 → P0
Order 101 → P1
```

Then:

```
P0:
Order100 Created
Order100 Paid
Order100 Shipped

P1:
Order101 Created
Order101 Paid
Order101 Shipped
```

Kafka guarantees ordering for:

```
Order 100
```

and separately for:

```
Order 101
```

But it doesn't guarantee whether:

```
Order100 Paid
```

happens before or after:

```
Order101 Created
```

from a global topic perspective.

---

## 7. Very important interview example

Suppose you have:

```
Topic: payments

P0
P1
P2
```

And these events:

```
PaymentCreated
PaymentAuthorized
PaymentCaptured
PaymentRefunded
```

If these events belong to the same payment, use:

```
key = paymentId
```

Then:

```
paymentId = PAY-123

PaymentCreated
PaymentAuthorized
PaymentCaptured
PaymentRefunded
```

will be routed consistently to the same partition.

Conceptually:

```
                  paymentId
                     |
                   PAY-123
                     |
                     v
                   P1
                     |
       +-------------+-------------+
       |             |             |
       v             v             v
   Created       Authorized     Captured
```

The consumer processes P1 sequentially.

---

## 8. What happens with multiple consumers?

Suppose:

```
P0
P1
P2
```

Consumer group:

```
C1
C2
C3
```

Assignment:

```
P0 → C1
P1 → C2
P2 → C3
```

Each consumer processes its partition in order.

```
C1:
A → B → C

C2:
D → E → F

C3:
G → H → I
```

But there is no guarantee that:

```
A → D → B → E → C
```

will be the actual cross-partition processing order.

---

## 9. What happens if the same partition moves to another consumer?

Suppose:

```
P0 → C1
```

C1 processes:

```
Offset 100
Offset 101
Offset 102
```

Then C1 crashes.

Kafka rebalances:

```
P0 → C2
```

C2 continues from the appropriate committed offset.

For example:

```
P0

100
101
102
103
104
```

If the committed position indicates that 102 was successfully committed, C2 can continue from the next position according to Kafka's offset semantics.

So partition ownership can change, but the partition itself remains an ordered log.

---

## 10. Ordering and consumer processing

There's another subtle issue.

Kafka can deliver records to a consumer in partition order, but your application must not destroy that ordering through concurrent processing.

Suppose Kafka gives:

```
P0:

100 → A
101 → B
102 → C
```

Your application does:

```
Thread 1 → A
Thread 2 → B
Thread 3 → C
```

It is possible for processing to complete:

```
B
C
A
```

even though Kafka delivered:

```
A
B
C
```

So:

> Kafka's partition ordering does not automatically guarantee ordered completion of your application's business processing if you introduce concurrency.

---

## 11. Example of a common mistake

Imagine:

```
P0:

OrderCreated
PaymentCompleted
OrderCancelled
```

Your consumer receives them in that order.

But you submit them to a thread pool:

```
Thread 1 → OrderCreated
Thread 2 → PaymentCompleted
Thread 3 → OrderCancelled
```

Processing finishes:

```
PaymentCompleted
OrderCancelled
OrderCreated
```

Now your business state can become incorrect.

Therefore, if strict ordering matters, your consumer processing design must preserve it.

---

## 12. Ordering and retries

Another important interview scenario.

Suppose:

```
P0:

100 → A
101 → B
102 → C
```

Consumer processes A successfully.

Then B fails:

```
A ✓
B ✗
C ?
```

If you immediately process C while B is being retried, you could potentially violate the required business ordering.

So ordered processing often requires careful handling of:

- retries
- blocking
- dead-letter topics
- asynchronous processing
- offset commits

---

## 13. Ordering vs throughput

This is a classic system-design trade-off.

### Requirement: "I need strict ordering for every event."

You could use fewer partitions, potentially even one partition:

```
Topic
 |
 P0
 |
 A → B → C → D
```

Ordering is straightforward.

But throughput and parallelism are limited.

### Requirement: "I need extremely high throughput."

Use many partitions:

```
P0 → Consumer 1
P1 → Consumer 2
P2 → Consumer 3
P3 → Consumer 4
P4 → Consumer 5
```

You get high parallelism, but only per-partition ordering.

### Real-world solution

Usually you don't need global ordering.

You need ordering **per business entity**.

For example:

```
Customer A → ordered
Customer B → ordered
Customer C → ordered
```

Use:

```
key = customerId
```

Then:

```
Customer A → P0
Customer B → P1
Customer C → P2
```

You get:

```
Ordering per customer
+
Parallelism across customers
```

This is often the best design.

---

## 14. The key design pattern

For many event-driven systems:

```
Business entity
      |
      v
Use entity ID as Kafka key
      |
      v
Same entity → same partition
      |
      v
Ordering preserved for that entity
      |
      +
Other entities → other partitions
      |
      v
Parallel processing
```

For example:

```
orderId = 101 → P0
orderId = 102 → P1
orderId = 103 → P2
orderId = 104 → P0
```

Within P0:

```
Order 101 Created
Order 101 Paid
Order 101 Shipped

Order 104 Created
Order 104 Paid
Order 104 Shipped
```

The exact interleaving between different keys in the same partition is determined by their arrival/append order, but each key's records maintain their partition order.

---

## 15. What if we increase partitions?

Suppose initially:

```
Topic = 3 partitions

P0
P1
P2
```

Later you increase to:

```
P0
P1
P2
P3
P4
P5
```

Be careful when relying on key-to-partition mapping.

For the standard hash-based partitioning behavior, changing the number of partitions can cause a given key to map to a different partition.

That can complicate assumptions about historical ordering across the old and new partition layout.

Therefore:

> Partition count should be chosen carefully when per-key ordering is an important requirement.

---

## 16. Kafka ordering guarantees — interview answer

If the interviewer asks:

> Does Kafka guarantee message ordering?

Answer:

> Kafka guarantees ordering within a partition. It does not guarantee ordering across partitions. If ordering is required for a particular business entity, such as an order or customer, we typically use that entity's ID as the record key so that its events are routed to the same partition. We also need to ensure our consumer processing and retry logic don't introduce out-of-order completion.

That's a strong answer.

---

## 17. Common interview questions

### Q1. Does Kafka guarantee global ordering?

**No.**

Ordering is guaranteed only within a partition.

### Q2. How do you maintain ordering for an order?

Use:

```
key = orderId
```

so events for the same order are routed to the same partition.

### Q3. Can two partitions be processed simultaneously?

**Yes.**

That's one of Kafka's main scalability mechanisms.

### Q4. Why not use one partition for everything?

Because one partition limits consumer-group parallelism and can become a throughput bottleneck.

### Q5. Can increasing partitions affect ordering?

**Yes.** Changing partition count can affect key-to-partition mapping, so applications that depend on stable per-key partitioning need to account for this.

### Q6. Does Kafka guarantee ordering if I use multiple consumer threads?

Kafka preserves partition order, but your application can break ordering if it processes records concurrently without coordination.

---

## 18. The most important mental model

Remember this:

```
                     KAFKA TOPIC
                         |
              +----------+----------+
              |          |          |
             P0         P1         P2
              |          |          |
              ↓          ↓          ↓
          A → B → C   D → E → F   G → H → I
```

Kafka guarantees:

```
P0: A → B → C       ✓
P1: D → E → F       ✓
P2: G → H → I       ✓
```

Kafka does not guarantee:

```
A → D → G → B → E → H
```

across the partitions.

So the core formula is:

> **Partition = ordering boundary + unit of parallelism**

And for real-world systems:

> Choose a meaningful partition key (often the business entity ID) when you need ordering for that entity, while using multiple partitions to retain scalability.

