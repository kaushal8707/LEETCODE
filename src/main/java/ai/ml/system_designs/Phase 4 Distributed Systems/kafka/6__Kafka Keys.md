# Kafka Keys

In Kafka, a **key** is a value attached to a record that helps Kafka determine which partition the record should be written to.

The key becomes especially important when you need **message ordering for a particular business entity**.

> **Key → determines partition → partition provides ordering**

---

## 1. Basic Kafka record

A Kafka message/record can conceptually contain:

- Key
- Value
- Headers
- Timestamp
- Partition
- Offset

For example:

```json
{
  "key": "ORDER-1001",
  "value": {
    "orderId": "ORDER-1001",
    "status": "CREATED"
  }
}
```

Here:

```
Key   = ORDER-1001
Value = Order information
```

---

## 2. Why do we need a key?

Suppose we have:

```
Topic: order-events

P0
P1
P2
```

We receive:

```
Order 1001 - Created
Order 1001 - Paid
Order 1001 - Shipped
```

If we don't intentionally partition by `orderId`, these records could potentially be distributed across different partitions.

That can cause a problem:

```
P0 → OrderCreated

P1 → PaymentCompleted

P2 → OrderShipped
```

Now there is no single partition containing the lifecycle of Order 1001.

If we use:

```
key = orderId
```

Kafka can consistently route records for the same key to the same partition.

```
OrderCreated       key=1001
PaymentCompleted   key=1001
OrderShipped       key=1001
```

Conceptually:

```
                 key = 1001
                     |
                     v
                    P1
                     |
       +-------------+-------------+
       |             |             |
       v             v             v
    Created        Paid         Shipped
```

Therefore, their relative order can be maintained within that partition.

---

## 3. How does Kafka choose the partition?

Conceptually, when a record has a key:

```
key
 |
 v
Partitioner
 |
 v
hash(key)
 |
 v
partition
```

A simplified model is:

```
partition = hash(key) % numberOfPartitions
```

For example:

```
key = ORDER-1001

hash(ORDER-1001)
       |
       v
     value
       |
       v
     P2
```

The exact partitioner behavior depends on the Kafka producer/client configuration and version, so the formula above is best understood as the **conceptual model**.

---

## 4. Same key → same partition

Suppose:

```
Partitions = 3
```

And we send:

```
key = ORDER-1001
```

multiple times:

```
ORDER-1001 → Created
ORDER-1001 → Paid
ORDER-1001 → Shipped
ORDER-1001 → Delivered
```

The producer's partitioning logic normally maps the same key consistently to the same partition for a given partition layout:

```
ORDER-1001
     |
     v
    P2
```

So:

```
P2:

Offset 10 → Created
Offset 11 → Paid
Offset 12 → Shipped
Offset 13 → Delivered
```

This is the main reason keys are important for ordering.

---

## 5. Different keys can go to different partitions

Suppose:

```
Order 1001
Order 1002
Order 1003
```

Use:

```
key = orderId
```

Kafka may distribute them:

```
ORDER-1001 → P0
ORDER-1002 → P2
ORDER-1003 → P1
```

Now you get:

```
P0 → Order 1001 events
P1 → Order 1003 events
P2 → Order 1002 events
```

This gives you both:

- **Ordering per order**

and

- **Parallelism across different orders.**

That's a very common Kafka design.

---

## 6. Key = business entity

A good Kafka key is usually something that represents the entity for which ordering matters.

Examples:

| Use case | Possible key |
|---|---|
| Orders | `orderId` |
| Customer events | `customerId` |
| Account transactions | `accountId` |
| Payment events | `paymentId` |
| Shipment events | `shipmentId` |
| Device events | `deviceId` |
| User activity | `userId` |

For example, banking:

```
accountId = ACC-123
```

Events:

```
Deposit
Withdrawal
Transfer
BalanceUpdated
```

Use:

```
key = ACC-123
```

Then the account's events are routed consistently to the same partition.

---

## 7. Real-world example: Banking

Imagine:

```
Account: ACC-100
```

Events:

```
Deposit ₹10,000
Withdrawal ₹2,000
Transfer ₹3,000
```

If ordering matters:

```
key = ACC-100
```

Kafka:

```
ACC-100
   |
   v
Partition 4
```

```
P4:

100 → Deposit
101 → Withdrawal
102 → Transfer
```

The consumer can process:

```
Deposit
   ↓
Withdrawal
   ↓
Transfer
```

in partition order.

---

## 8. Key vs Value

This distinction is important.

Consider:

```json
{
  "key": "ORD-123",
  "value": {
    "customerId": "C-500",
    "amount": 2500,
    "status": "CREATED"
  }
}
```

### Key

```
ORD-123
```

Used primarily for partitioning and therefore often ordering/grouping semantics.

### Value

```json
{
  "customerId": "C-500",
  "amount": 2500,
  "status": "CREATED"
}
```

Contains the actual event/business data.

Think:

```
              Kafka Record
             /            \
            /              \
         Key               Value
          |                  |
    Partitioning        Business data
    / Ordering
```

---

## 9. What happens if the key is null?

This is another common interview question.

If the record has no key:

```
key = null
```

Kafka does not have a business key to use for keyed partitioning.

The producer's partitioner uses its configured behavior for null-key records. In modern Kafka clients, this commonly involves a **sticky partitioning** behavior intended to improve batching rather than simply using a fixed round-robin pattern.

So don't memorize:

> "Null key always means round robin."

That is an outdated oversimplification for modern Kafka clients.

The important point is:

> With a null key, you lose the explicit per-key partitioning guarantee.

Therefore, if you need ordering for a business entity, use a meaningful key.

---

## 10. Key and message ordering

This is the relationship you should remember:

```
Business Entity ID
        |
        v
      Kafka Key
        |
        v
   Partitioning
        |
        v
 Same entity → same partition
        |
        v
Ordering within partition
```

For example:

```
orderId = 101
Created  → key=101
Paid     → key=101
Shipped  → key=101
Delivered→ key=101
```

Potentially:

```
P2

Created
Paid
Shipped
Delivered
```

---

## 11. But keys do NOT guarantee global ordering

Suppose:

```
Order 101 → P0
Order 102 → P1
```

You have:

```
P0:
Created
Paid
Shipped
```

and:

```
P1:
Created
Paid
Shipped
```

Kafka guarantees the order within each partition.

It does not guarantee:

```
Order101 Created
Order102 Created
Order101 Paid
Order102 Paid
...
```

globally.

So:

> Key gives you a way to achieve **per-key ordering**, not global topic ordering.

---

## 12. Choosing the right key

This is a very important system-design interview question.

Suppose your requirement is:

> "Events belonging to the same order must be processed in order."

Choose:

```
key = orderId
```

Not:

```
key = customerId
```

unless the requirement is:

> "All events for a customer must be ordered."

### Example

Customer C1 has:

```
Order 101
Order 102
Order 103
```

If:

```
key = customerId
```

then:

```
C1
 |
 v
P2

Order101
Order102
Order103
```

All orders of that customer are serialized through one partition.

That may unnecessarily reduce parallelism.

If ordering is only needed per order:

```
key = orderId
```

you might get:

```
Order101 → P0
Order102 → P1
Order103 → P2
```

Now those orders can be processed in parallel.

### Rule

> Choose the **smallest business entity** for which ordering is actually required.

---

## 13. Bad key selection — hot partition problem

Suppose you use:

```
key = country
```

and 80% of your traffic is:

```
country = IN
```

You could end up with:

```
IN → P2
```

and P2 receives a huge amount of traffic.

```
P0 → 10%
P1 → 10%
P2 → 80%  🔥
P3 → 0%
```

This is called **partition skew** or a **hot partition**.

Your Kafka cluster may have many partitions, but one partition becomes the bottleneck.

Therefore:

> A good key should satisfy the ordering requirement while providing a reasonably balanced distribution.

---

## 14. Key selection is a trade-off

You need to balance:

```
Ordering requirement
        +
Distribution
        +
Parallelism
```

For example:

```
key = orderId
```

might provide:

```
✓ Order-level ordering
✓ Good distribution
✓ High parallelism
```

Whereas:

```
key = country
```

might provide:

```
✓ Country-level ordering
✗ Potential skew
✗ Reduced parallelism
```

---

## 15. Changing the number of partitions

Be careful with this.

Suppose:

```
Topic = 3 partitions
```

and:

```
key = ORDER-100
```

The key maps to a particular partition.

If you increase the topic to:

```
6 partitions
```

the key-to-partition mapping can change because the partition count is part of the partitioning calculation.

Therefore, if you rely heavily on stable key-based partitioning and ordering, changing partition counts requires careful design consideration.

---

## 16. Java example

With Spring Kafka:

```java
kafkaTemplate.send(
    "order-events",
    order.getOrderId(),
    orderCreatedEvent
);
```

Here:

- **Topic** = `order-events`
- **Key** = `order.getOrderId()`
- **Value** = `orderCreatedEvent`

Conceptually:

```
orderId
   |
   v
Partitioner
   |
   v
Partition
   |
   v
Kafka
```

---

## 17. Producer flow with a key

The complete producer-side flow:

```
                  Producer
                     |
                     v
              Kafka Record
             /            \
            /              \
         Key               Value
          |                  |
     orderId              Event data
          |
          v
      Partitioner
          |
          v
      Partition
          |
          v
       Kafka Broker
          |
          v
     Append to log
```

Example:

```
Key:
ORDER-123

Value:
{
   "status": "PAYMENT_COMPLETED"
}
```

The producer determines the target partition using the partitioning logic.

---

## 18. Very important: Key and Consumer Group are different

Don't confuse:

### Key

Determines where the record is written.

### Consumer Group

Determines which consumer gets a partition.

Flow:

```
Producer
   |
   | key = orderId
   v
Partitioner
   |
   v
Partition P2
   |
   v
Consumer Group
   |
   v
Consumer C1
```

So:

```
Key
 ↓
Partition

Consumer Group
 ↓
Consumer
```

---

## 19. Interview questions

### Q1. What is a Kafka key?

> A key is a value associated with a Kafka record that is commonly used by the producer's partitioner to determine the target partition. It is especially useful for maintaining ordering for a business entity.

### Q2. Why use `orderId` as the key?

> To ensure events for the same order are consistently routed to the same partition, allowing their relative order to be preserved within that partition.

### Q3. Does the same key always mean the same partition?

> For a given partitioning configuration and partition count, keyed records are normally mapped consistently to the same partition. However, changing the partition count can change the mapping.

### Q4. What happens if key is null?

> There is no explicit business-key-based partitioning. The producer uses its configured null-key partitioning behavior, so you should not rely on null keys for per-entity ordering.

### Q5. Can different keys go to the same partition?

**Yes.**

For example:

```
Order101 → P0
Order102 → P0
Order103 → P1
```

Multiple keys can share a partition.

---

## 20. The most important mental model

Remember:

```
                 Kafka Record
                      |
                +-----+-----+
                |           |
               Key         Value
                |
                v
           Partitioner
                |
                v
            Partition
                |
                v
         Ordered Log
```

And:

```
Same business key
       ↓
Same partition
       ↓
Ordering for that entity
```

while:

```
Different keys
       ↓
Potentially different partitions
       ↓
Parallel processing
```

---

## The interview-ready answer

> Kafka keys are used by the producer's partitioning logic to determine where a record is written. We typically choose a business identifier, such as `orderId`, as the key when events for that entity must remain ordered. Records with the same key are normally routed consistently to the same partition, giving us per-key ordering while allowing different keys to be distributed across partitions for parallelism. However, a poor key can create partition skew or hot partitions, and changing the partition count can change key-to-partition mapping.

