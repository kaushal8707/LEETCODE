# How to Choose a Kafka Partition Key

This is one of the most important Kafka design decisions.

A simple rule is:

> **Choose the partition key based on the business entity for which you need ordering, then verify that the key distributes traffic evenly enough to avoid hot partitions.**

Think about it as:

```
Partition Key
     |
     +---- Ordering requirement
     |
     +---- Distribution
     |
     +---- Parallelism
     |
     +---- Hot-key risk
```

---

## 1. First question: What needs to be ordered?

Before choosing a key, ask:

> "For which entity must Kafka preserve event ordering?"

Examples:

| Requirement | Recommended key |
|---|---|
| Order events | `orderId` |
| Customer events | `customerId` |
| Bank account transactions | `accountId` |
| Shipment events | `shipmentId` |
| Device telemetry | `deviceId` |
| User activity | `userId` |
| Payment lifecycle | `paymentId` |

For example, suppose:

```
Order 101
  |
  +-- OrderCreated
  +-- PaymentCompleted
  +-- OrderShipped
  +-- OrderDelivered
```

If these events must remain ordered:

```
key = orderId
```

Then:

```
orderId=101
       |
       v
   Partition 3
       |
       +-- Created
       +-- Paid
       +-- Shipped
       +-- Delivered
```

That's the primary reason for choosing `orderId`.

---

## 2. Kafka guarantees ordering within a partition

Kafka does **not** guarantee global ordering across a topic.

Suppose:

```
Topic: orders

P0
Order 101 → Created
Order 101 → Paid

P1
Order 102 → Created
Order 102 → Paid
```

Kafka guarantees:

```
P0:
Created → Paid
```

and:

```
P1:
Created → Paid
```

But there is no guaranteed ordering between:

```
Order 101
```

and:

```
Order 102
```

across different partitions.

Therefore:

> If events for an entity need ordering, all events for that entity should normally use the same partition key.

---

## 3. Example: Order System

Suppose you have:

```
OrderCreated
PaymentCompleted
OrderShipped
OrderDelivered
```

and:

```
orderId = 1001
```

Use:

```
key = orderId
```

Conceptually:

```
hash(orderId) → partition
```

So:

```
1001 → P3

OrderCreated       → P3
PaymentCompleted   → P3
OrderShipped       → P3
OrderDelivered     → P3
```

Now Kafka can preserve their ordering within P3.

---

## 4. Don't choose the key based only on the data you have

Suppose your event looks like:

```json
{
  "orderId": "1001",
  "customerId": "C123",
  "country": "IN",
  "status": "SHIPPED"
}
```

You have several possible keys:

- `orderId`
- `customerId`
- `country`
- `status`

Don't ask:

> "Which field is easiest to use?"

Ask:

> **"Which business entity needs ordering?"**

If the requirement is:

> All events for an order must be ordered.

Then:

```
orderId
```

is the correct candidate.

---

## 5. Second question: Will the key distribute traffic?

Choosing `orderId` is good for ordering.

But you must also check:

> Does `orderId` distribute events reasonably evenly?

Suppose:

```
1 million orders
```

and traffic is reasonably distributed:

```
Order 1 → 100 events
Order 2 → 50 events
Order 3 → 80 events
...
```

Great.

But suppose one key generates enormous traffic:

```
orderId=999999
      |
      +---- 40% of all events
```

Then:

```
orderId=999999
       |
       v
   Partition 7 🔥
```

You have a hot partition.

---

## 6. Hot key is the biggest partition-key problem

Imagine 8 partitions:

```
P0 → 12%
P1 → 11%
P2 → 13%
P3 → 10%
P4 → 12%
P5 → 11%
P6 → 10%
P7 → 21%
```

Not perfect, but potentially manageable.

Now imagine:

```
P0 → 5%
P1 → 5%
P2 → 5%
P3 → 5%
P4 → 5%
P5 → 5%
P6 → 5%
P7 → 65% 🔥
```

You have 8 partitions but your effective capacity is heavily constrained by P7.

So:

> A high partition count cannot fix a bad partition key.

---

## 7. Bad partition keys

### `country`

Suppose:

```
India → 70%
USA   → 10%
UK    → 5%
Others → 15%
```

Using:

```
key = country
```

can cause severe skew.

### `eventType`

Suppose:

```
OrderCreated → 80%
PaymentFailed → 10%
OrderShipped → 5%
Others → 5%
```

Then:

```
key = eventType
```

can create hot partitions.

### `status`

For example:

```
ACTIVE → 90%
INACTIVE → 10%
```

Terrible candidate if you want balanced distribution.

---

## 8. Usually avoid low-cardinality keys

A good partition key generally has enough distinct values.

**Bad:**

- `country`
- `gender`
- `status`
- `eventType`
- `currency`

**Potentially good:**

- `orderId`
- `customerId`
- `accountId`
- `transactionId`
- `deviceId`
- `userId`

Why?

Because:

```
High cardinality
       ↓
Many possible keys
       ↓
Better distribution potential
```

But high cardinality alone isn't enough.

You still need to consider traffic distribution.

---

## 9. Third question: Does the key match your ordering boundary?

This is extremely important.

Suppose:

```
customer C1
   |
   +-- Order 101
   +-- Order 102
   +-- Order 103
```

What needs ordering?

### Requirement A

Events for each order must be ordered.

Use:

```
key = orderId
```

### Requirement B

All events for a customer must be ordered globally.

Use:

```
key = customerId
```

These are very different designs.

---

## 10. Example: Customer vs Order

Suppose:

```
Customer C1

Order 101:
Created → Paid → Shipped

Order 102:
Created → Paid → Shipped
```

If you use:

```
key = customerId
```

everything goes to one partition:

```
C1
 |
 v
P3

Order 101 events
Order 102 events
Order 103 events
...
```

This guarantees ordering across all C1 events, but reduces parallelism for that customer.

If you use:

```
key = orderId
```

you could have:

```
Order 101 → P2
Order 102 → P5
Order 103 → P1
```

Now different orders can be processed in parallel.

Therefore:

> **Choose the smallest business entity that actually requires ordering.**

This is a very strong senior-level design principle.

---

## 11. "Smallest ordering boundary"

Consider:

```
Company
   |
   +-- Customer
          |
          +-- Account
                 |
                 +-- Transaction
```

Ask:

> What exactly needs ordering?

If only transactions for the same account need ordering:

```
key = accountId
```

Don't unnecessarily use:

```
key = companyId
```

because that would put many unrelated accounts into the same partition.

You would sacrifice parallelism.

---

## 12. Fourth question: Can the key create a hot key?

Suppose:

```
key = customerId
```

Normally good.

But one customer is a massive enterprise customer:

```
Customer A → 50% traffic
```

Then:

```
Customer A
    ↓
Partition 4 🔥
```

Now you have a fundamental trade-off.

You want:

```
Ordering
```

but also:

```
Parallelism
```

---

## 13. The hot-key dilemma

Suppose:

```
Account A
```

generates:

```
100,000 events/sec
```

and all events must be strictly ordered.

If you use:

```
key = accountId
```

then:

```
Account A
     ↓
One partition
```

You cannot distribute those events across 10 partitions while maintaining strict ordering across all of them.

You have:

```
Strict ordering
       VS
Parallelism
```

You cannot magically get both without changing the business requirement or processing model.

---

## 14. One possible solution: composite/sharded key

You could create:

```
accountId + shard
```

For example:

```
A-0
A-1
A-2
A-3
```

Then:

```
A-0 → P1
A-1 → P3
A-2 → P5
A-3 → P7
```

This increases parallelism.

But now:

```
A-0
A-1
A-2
A-3
```

can be processed concurrently.

Therefore you lose global ordering for account A unless your application has another ordering mechanism.

This is why you should **never blindly** solve hot partitions by adding random suffixes.

---

## 15. Ask whether you really need global ordering

This is often the better solution.

Suppose the requirement originally says:

> "Account events must be ordered."

Clarify:

> Does every event for the account need ordering, or only transactions for the same payment/order?

Maybe the real requirement is:

```
Payment 101:
Created → Authorized → Captured
```

but Payment 102 can happen independently.

Then:

```
key = paymentId
```

could provide much better parallelism than:

```
key = accountId
```

This is a **business requirement** optimization, not just a Kafka optimization.

---

## 16. Fifth question: Does the key remain stable?

Your key should normally represent a stable identity.

**Good:**

- `orderId`
- `customerId`
- `accountId`
- `deviceId`

**Bad candidates can include mutable fields:**

- `orderStatus`
- `customerAddress`
- `customerTier`

For example:

```
Order 101
status=CREATED

Order 101
status=PAID

Order 101
status=SHIPPED
```

If you use:

```
key = status
```

the same order's events can land on different partitions.

That's terrible for ordering.

---

## 17. Compacted topics make key selection even more important

You just learned log compaction.

Suppose:

```
customer-profile
cleanup.policy=compact
```

You want:

```
customerId → latest profile
```

Therefore:

```
key = customerId
```

Example:

```
101 → Delhi
101 → Mumbai
101 → Pune
```

Kafka recognizes:

```
key = 101
```

and can eventually compact older values.

If you instead use:

```
key = city
```

you might get:

```
Delhi   → customer 101
Mumbai  → customer 101
Pune    → customer 101
```

Now the key doesn't represent the entity whose state you want to compact.

So:

> For compacted topics, the key should normally represent the entity whose latest state you want to retain.

---

## 18. Partition key and consumer parallelism

Suppose:

```
Topic
10 partitions
```

and:

```
key = customerId
```

You might have:

```
Consumer 1 → P0
Consumer 2 → P1
Consumer 3 → P2
...
Consumer 10 → P9
```

Customers distributed across partitions can be processed concurrently.

```
Customer A → P0 → Consumer 1
Customer B → P4 → Consumer 5
Customer C → P7 → Consumer 8
```

Therefore a good key enables:

```
Ordering per entity
        +
Parallel processing across entities
```

That's usually the ideal Kafka design.

---

## 19. Partition key vs partition count

Don't mix these two decisions.

### Partition key determines:

> Which partition does this record go to?

### Partition count determines:

> How many partitions are available?

Think:

```
                Kafka Topic
                    |
          +---------+---------+
          |                   |
    Partition Key        Partition Count
          |                   |
          ↓                   ↓
Ordering + Distribution   Parallelism
```

---

## 20. Good partition-key selection process

For every topic, follow this sequence.

### Step 1 — Identify the ordering requirement

```
What must be ordered?
```

### Step 2 — Identify the business entity

```
Order?
Customer?
Account?
Payment?
Device?
```

### Step 3 — Select that entity's stable ID

```
orderId
customerId
accountId
```

### Step 4 — Analyze cardinality

Ask:

```
How many unique values exist?
```

### Step 5 — Analyze traffic distribution

Ask:

```
Do some keys generate much more traffic?
```

### Step 6 — Check for hot keys

```
Top 1% keys → what percentage of traffic?
```

### Step 7 — Check partition count

Make sure there are enough partitions for the desired parallelism.

### Step 8 — Load test

Measure:

- partition throughput
- consumer lag
- partition skew
- broker load

---

## 21. Real-world example: E-commerce

Suppose:

```
Topic = order-events

Events:
OrderCreated
PaymentCompleted
OrderShipped
OrderDelivered
```

Requirement:

> Events for the same order must be ordered.

Choose:

```
key = orderId
```

Architecture:

```
                 orderId
                    |
                    v
                Partitioner
                    |
          +---------+---------+
          |         |         |
          v         v         v
         P0        P1        P2
          |         |         |
       Orders     Orders    Orders
```

For:

```
orderId=101
```

all events go to one partition:

```
P1

101 → Created
101 → Paid
101 → Shipped
101 → Delivered
```

Meanwhile:

```
orderId=102 → P2
orderId=103 → P0
```

can process concurrently.

---

## 22. Real-world example: Banking

Requirement:

> Transactions for the same bank account must be processed in order.

Use:

```
key = accountId
```

Example:

```
Account A
  |
  +-- Deposit
  +-- Withdrawal
  +-- Withdrawal
  +-- Deposit
```

All go to:

```
P5
```

while:

```
Account B → P2
Account C → P7
```

can process concurrently.

This prevents problematic ordering such as:

```
Withdrawal
   ↓
Deposit
   ↓
Balance calculation
```

being processed in the wrong sequence for the same account.

---

## 23. Real-world example: IoT

Suppose:

```
10 million devices
```

and each device sends telemetry.

Requirement:

> Preserve ordering for each device.

Use:

```
key = deviceId
```

Then:

```
Device A → P4
Device B → P1
Device C → P7
Device D → P2
```

This gives:

```
Per-device ordering
        +
Cross-device parallelism
```

This is generally much better than:

```
key = deviceType
```

because there may be only a few device types and traffic could become skewed.

---

## 24. What about UUID as a key?

A random UUID can distribute traffic very well:

```
UUID
 ↓
hash
 ↓
partition
```

But ask:

> Do I need ordering for a business entity?

If every event gets a completely unique random key:

```
event1 → random UUID
event2 → random UUID
event3 → random UUID
```

then related events may go to different partitions.

So UUID can be good for distribution but bad for entity ordering if it isn't the entity identity.

---

## 25. Key should represent the ordering boundary

This is probably the single most important interview statement.

Suppose:

```
Order
 |
 +-- Payment
 +-- Shipment
 +-- Invoice
```

If all events for an order must be ordered:

```
key = orderId
```

If only payment lifecycle needs ordering:

```
key = paymentId
```

The more broadly you choose the key:

```
companyId
   ↓
customerId
   ↓
accountId
   ↓
orderId
   ↓
paymentId
```

the more events you may force into the same partition.

Therefore:

> **Choose the narrowest entity that satisfies the ordering requirement.**

---

## 26. Common mistakes

### Mistake 1: Choosing `eventType`

```
OrderCreated
PaymentCompleted
OrderShipped
```

Why?

Because event types don't normally define the ordering boundary and can create skew.

### Mistake 2: Choosing a low-cardinality field

```
country
status
eventType
currency
```

Can create hot partitions.

### Mistake 3: Choosing a random UUID for everything

Good distribution:

```
✅
```

But potentially destroys business-entity ordering:

```
❌
```

### Mistake 4: Using a very broad key

For example:

```
key = companyId
```

when only order-level ordering is required.

This reduces parallelism unnecessarily.

### Mistake 5: Ignoring hot keys

Even a technically correct key like:

```
customerId
```

can create a hot partition if one customer produces enormous traffic.

### Mistake 6: Assuming more partitions solve hot keys

They don't.

If:

```
customer A → P3
```

and customer A generates 50% of traffic:

```
50 partitions
```

doesn't change the fact that A maps to one partition.

---

## 27. Senior-level decision matrix

| Question | What you're looking for |
|---|---|
| What must be ordered? | Business ordering boundary |
| What identifies that entity? | Stable ID |
| How many unique keys? | Cardinality |
| How is traffic distributed? | Skew analysis |
| Are there hot keys? | Top-key traffic percentage |
| How many partitions? | Parallelism capacity |
| Is the topic compacted? | Key must represent state entity |
| Can ordering be relaxed? | Potential hot-key solution |
| Does key remain stable? | Avoid mutable keys |
| Can downstream process in parallel? | Consumer scalability |

---

## 28. The decision formula

You can remember:

```
Good Partition Key
=
Correct Ordering Entity
+
Good Distribution
+
High Enough Cardinality
+
Stable Identity
-
Hot-Key Risk
```

Not literally a mathematical formula, but an excellent design checklist.

---

## 29. Interview question

### Interviewer:

> "How would you choose a partition key for an order topic?"

A strong senior answer:

> "First I'd identify the ordering requirement. If events for the same order must be processed in order, I'd use `orderId` as the key. This ensures all events for an order normally go to the same partition. Then I'd analyze the cardinality and traffic distribution of order IDs to identify hot keys or skew. I'd also make sure the partition count provides enough parallelism for different orders and that broker and downstream capacity can handle the load. If a few orders create extreme traffic, I'd investigate whether the business really requires global ordering for those orders before considering key sharding, because sharding a key can sacrifice ordering."

That is much stronger than:

> "I use `orderId` because Kafka uses keys for partitioning."

---

## 30. Golden rule for your interviews

Remember this:

```
             What needs ordering?
                     |
                     ↓
             Identify entity
                     |
                     ↓
             Choose entity ID
                     |
                     ↓
             Check distribution
                     |
                     ↓
               Hot key?
              /         \
            Yes          No
             |            |
     Revisit business     |
     ordering boundary   |
             |            |
             +-------> Final Key
```

---

## The one-line answer

> **Choose a Kafka partition key that represents the smallest stable business entity that requires ordering, and then verify that the key has enough cardinality and an even enough traffic distribution to avoid hot partitions.**

For your 10-year-experience system-design interviews, this distinction is worth memorizing:

```
Partition Key
     ↓
Ordering + Distribution

Partition Count
     ↓
Throughput + Parallelism
```

And together:

```
Correct Key
     +
Correct Partition Count
     +
Balanced Traffic
     +
Downstream Capacity
     ↓
Scalable Kafka Design
```

