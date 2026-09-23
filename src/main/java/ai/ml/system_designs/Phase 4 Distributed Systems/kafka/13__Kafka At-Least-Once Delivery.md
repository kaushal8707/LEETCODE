# Kafka At-Least-Once Delivery

**At-least-once delivery** means:

> Every message should be processed **at least one time**, but the same message may be processed more than once.

The key trade-off is:

```
At-least-once
      ↓
Avoid message loss
      ↓
Duplicates are possible
```

This is one of the most commonly used delivery semantics in real-world Kafka systems.

---

## 1. The three delivery semantics

| Delivery Semantic | Message Loss | Duplicate |
|---|---|---|
| At-most-once | ✅ Possible | ❌ No |
| At-least-once | ❌ Normally avoided | ✅ Possible |
| Exactly-once | ❌ Within transactional scope | ❌ Within transactional scope |

The easiest way to remember:

```
At-most-once
    ↓
No duplicate
    ↓
May lose message


At-least-once
    ↓
Don't lose message
    ↓
May process twice


Exactly-once
    ↓
No loss
    +
No duplicate
```

---

## 2. How At-Least-Once works

The basic strategy is:

> **Process the message first, then commit the Kafka offset.**

Flow:

```
Kafka
  |
  | Message
  v
Consumer
  |
  | Process
  v
Business Logic
  |
  | SUCCESS
  v
Commit Offset
```

The important ordering is:

```
PROCESS
   ↓
COMMIT OFFSET
```

---

## 3. Why does this prevent message loss?

Suppose Kafka sends:

```
Offset 100 → OrderCreated
```

Consumer receives it:

```
Kafka
  |
  | Offset 100
  v
Consumer
```

Consumer processes it:

```
Process OrderCreated
       ↓
      SUCCESS
```

Then commits:

```
Commit offset 101
```

Everything is good:

```
Process ✓
Commit  ✓
```

---

## 4. What if the consumer crashes?

This is where at-least-once becomes important.

Suppose:

```
Kafka
  |
  | OrderCreated
  v
Consumer
  |
  | Process
  v
Database
  |
  | SUCCESS
  |
  X
Consumer crashes
```

The consumer successfully processed the message.

But it didn't commit the offset.

So Kafka still thinks:

```
Offset 100
```

is uncommitted.

When the consumer restarts:

```
Kafka
  |
  | Offset 100 again
  v
Consumer
```

The message is processed again.

```
First processing:
OrderCreated → SUCCESS

Second processing:
OrderCreated → SUCCESS
```

Therefore:

> At-least-once guarantees the message gets another chance to be processed, but duplicates can occur.

---

## 5. The classic failure scenario

This is the most important diagram to understand:

```
             Kafka
               |
               | Message offset 100
               v
           Consumer
               |
               | Process
               v
         Business Logic
               |
               | SUCCESS
               |
               X
          Consumer crashes
               |
               |
          Offset NOT committed
               |
          Consumer restarts
               |
               v
             Kafka
               |
               | offset 100 again
               v
           Consumer
               |
               v
          Process again
```

Result:

```
Message processed twice
```

But:

```
Message was NOT permanently lost
```

That's at-least-once.

---

## 6. Why commit AFTER processing?

Because committing first creates a message-loss scenario.

### At-most-once

```
Consume
   ↓
Commit
   ↓
Process
```

If crash occurs:

```
Commit ✓
Process ❌

Message LOST
```

### At-least-once

```
Consume
   ↓
Process
   ↓
Commit
```

If crash occurs:

```
Process ✓
Commit ❌

Message processed AGAIN
```

So the fundamental difference is:

```
At-most-once:

Commit → Process
            ↑
        Loss possible


At-least-once:

Process → Commit
            ↑
       Duplicate possible
```

---

## 7. Real-world Order Processing Example

Suppose you have:

```
Order Service
      |
      v
    Kafka
      |
      v
Order Consumer
      |
      v
Payment Service
```

Kafka contains:

```
Offset 500 → OrderCreated
```

Consumer receives it.

It calls:

```
Payment Service
      |
      v
Create Payment
```

Payment is successfully created:

```
Payment DB:
Order 123 → PAYMENT_CREATED
```

But immediately afterward:

```
Consumer crashes
```

Before:

```java
commitSync()
```

So offset 500 is still uncommitted.

After restart:

```
Kafka
  |
  | Offset 500
  v
Consumer
  |
  v
Payment Service
```

It tries to create the payment again.

Now you have a potential duplicate.

---

## 8. This is why idempotent consumers are important

At-least-once delivery almost always leads to this design requirement:

> Consumers should be idempotent whenever duplicate processing is possible.

For example, Kafka event:

```
eventId = EVT-10001
orderId = ORD-500
eventType = OrderCreated
```

Consumer maintains:

```
ProcessedEvents

EVT-10001
EVT-10002
EVT-10003
```

When the event arrives:

```
EVT-10001
    |
    v
Already processed?
    |
   YES
    |
    v
Skip duplicate
```

So:

```
Kafka
  ↓
At-least-once
  ↓
Possible duplicate
  ↓
Idempotent Consumer
  ↓
Safe business operation
```

---

## 9. Database Idempotency

A common approach is using a unique constraint.

Suppose:

```
event_id = EVT-10001
```

Database:

```sql
CREATE TABLE processed_events (
    event_id VARCHAR(100) PRIMARY KEY
);
```

First processing:

```
EVT-10001
     ↓
INSERT
     ↓
SUCCESS
```

Duplicate processing:

```
EVT-10001
     ↓
INSERT
     ↓
Duplicate key
     ↓
Already processed
```

This can prevent the business operation from being applied twice, provided the deduplication and business update are designed transactionally.

---

## 10. Kafka Consumer Offset

Kafka tracks consumer progress using offsets.

Suppose:

```
Partition 0

Offset
 100 → A
 101 → B
 102 → C
 103 → D
```

Consumer has processed A:

```
A → Processed
```

Then commits the next position:

```
Committed offset = 101
```

Meaning conceptually:

```
Everything before 101
       ↓
already consumed
```

Consumer continues:

```
101 → B
102 → C
```

---

## 11. Crash scenario

Suppose:

```
Offset 100 → A
```

Consumer processes A:

```
A → SUCCESS
```

But crashes before committing.

Kafka still has:

```
Committed offset = 100
```

After restart:

```
Consumer
   |
   v
Offset 100
   |
   v
A processed again
```

That's the source of the duplicate.

---

## 12. `enable.auto.commit`

For reliable at-least-once processing, you typically need to carefully control offset commits.

A common conceptual configuration is:

```
enable.auto.commit=false
```

Then:

```java
consumer.poll();

processRecords();

consumer.commitSync();
```

Conceptually:

```
poll
 ↓
process
 ↓
commit
```

However, the exact implementation must also account for batching, asynchronous processing, failures, partition assignment/rebalancing, and the framework being used.

---

## 13. Auto Commit and At-Least-Once

Suppose:

```
enable.auto.commit=true
```

Kafka periodically commits offsets automatically.

Imagine:

```
Message received
      ↓
Auto commit happens
      ↓
Consumer crashes
      ↓
Message not processed
```

Depending on timing, this can lead to message loss.

Therefore, for strict processing guarantees, applications usually use manual offset management and commit only after successful processing.

---

## 14. At-Least-Once + Consumer Groups

Suppose:

```
Topic
  |
  +-- Partition 0
  +-- Partition 1
  +-- Partition 2
```

Consumer group:

```
Consumer A → Partition 0
Consumer B → Partition 1
Consumer C → Partition 2
```

Now Consumer B crashes:

```
Consumer B ❌
```

Kafka performs a rebalance.

Partition 1 may move to another consumer:

```
Partition 1
     |
     v
Consumer C
```

The new consumer starts from the last committed offset.

If the previous consumer had processed a message but hadn't committed its offset:

```
Processed ✓
Committed ❌
```

the new consumer processes it again.

Again:

```
At-least-once
     ↓
Duplicate possible
```

This is why offset management and rebalancing are closely related.

---

## 15. At-Least-Once + External Database

This is where distributed-system problems become interesting.

Consider:

```
Kafka
  |
  v
Consumer
  |
  +----> Database
  |
  +----> Commit Kafka Offset
```

You have two separate systems:

- Kafka
- Database

Suppose:

```
1. Consume message
2. Update DB → SUCCESS
3. Commit Kafka offset → FAILURE
```

After restart:

```
Message processed again
```

Database may receive the same operation again.

That's why you often need:

```
At-least-once delivery
        +
Idempotent consumer
        +
Database transaction / unique constraint
```

---

## 16. Transactional Outbox connection

This connects directly with distributed systems concepts you've been studying.

Suppose:

```
Order Service
     |
     +---- Order DB
     |
     +---- Kafka
```

You don't want:

```
DB update ✓
Kafka publish ❌
```

Transactional Outbox solves the producer-side dual-write problem.

Then Kafka consumer processing may still need:

```
Kafka
  ↓
Consumer
  ↓
Database
```

with idempotency or Kafka transactions depending on your architecture.

So these patterns solve different failure boundaries.

---

## 17. At-Least-Once vs Producer Idempotence

Very important distinction:

### Producer idempotence

```
Producer
   |
   | retry
   v
Kafka
```

Prevents duplicate Kafka appends caused by producer retries.

### At-least-once consumer

```
Kafka
   |
   v
Consumer
   |
   | process
   ↓
commit offset
```

Protects against losing a message when processing fails before the offset is committed.

Therefore:

```
Producer Idempotence
        ↓
Producer → Kafka


At-Least-Once
        ↓
Kafka → Consumer
```

You can use both together.

---

## 18. At-Least-Once + Idempotent Producer

A robust architecture could look like:

```
              Producer
                  |
          enable.idempotence=true
                  |
                  v
               Kafka
                  |
                  v
              Consumer
                  |
              Process
                  |
                  v
         Idempotent Business Logic
                  |
                  v
              Database
                  |
                  v
           Commit Offset
```

Now you have protection at multiple stages:

```
Producer retry
      ↓
Idempotence
      ↓
No duplicate Kafka append

Consumer failure
      ↓
At-least-once
      ↓
Message processed again

Duplicate consumer processing
      ↓
Idempotent business logic
      ↓
No duplicate business effect
```

---

## 19. When should you use At-Least-Once?

For many business-critical event processing systems, at-least-once is a very good default.

Examples:

- OrderCreated
- PaymentCompleted
- PaymentFailed
- ShipmentCreated
- InvoiceGenerated
- InventoryUpdated
- CustomerRegistered

Because losing these events can be worse than processing one twice.

For example:

```
PaymentCompleted
       ↓
Message LOST
       ↓
Order remains "PAYMENT_PENDING"
```

That's potentially a serious business problem.

At-least-once instead gives:

```
PaymentCompleted
       ↓
Maybe duplicate
       ↓
Idempotent consumer
       ↓
Correct final state
```

---

## 20. At-Least-Once vs Exactly-Once

This is a very common interview question.

### At-least-once

```
Process
   ↓
Commit
```

Failure:

```
Process ✓
Commit ❌
   ↓
Process again
```

Therefore:

```
Duplicate possible
```

### Exactly-once

Kafka can use transactions to atomically coordinate Kafka record processing/output and offsets within the transactional model.

Conceptually:

```
Read
 ↓
Process
 ↓
Produce output + offset commit
 ↓
Atomic transaction
```

Either:

```
Everything succeeds
```

or:

```
Everything is aborted
```

So consumers don't expose partial transactional results.

---

## 21. Interview Questions

### Q1. What is at-least-once delivery?

Answer:

> At-least-once delivery means a message is guaranteed to be processed at least once, but it may be processed multiple times. The consumer processes the message before committing its offset. If the consumer crashes after processing but before committing, Kafka redelivers the message.

### Q2. Why can duplicates happen?

Because:

```
Process ✓
Commit ❌
Crash
```

After restart:

```
Same offset
   ↓
Same message
   ↓
Process again
```

### Q3. How do you handle duplicates?

Use an **idempotent consumer**.

Common techniques:

- `eventId`
- unique database constraint
- deduplication table
- upsert
- idempotent business operation

### Q4. Why is at-least-once preferred over at-most-once for payments?

Because losing a payment event is usually worse than processing it twice.

So:

```
At-least-once
      +
Idempotent consumer
```

is often preferable.

### Q5. Does at-least-once mean Kafka itself sends the message multiple times?

Not necessarily.

The duplicate usually occurs because:

```
Consumer processes
      ↓
Offset isn't committed
      ↓
Consumer crashes/rebalances
      ↓
Kafka delivers from last committed offset
      ↓
Message processed again
```

The important distinction is between **redelivery/reprocessing** and the **producer creating duplicate records**.

---

## 22. The most important mental model

Remember this:

```
              AT-LEAST-ONCE

Kafka
  |
  | Message
  v
Consumer
  |
  | Process
  v
Business Logic
  |
  | SUCCESS
  v
Commit Offset
```

Failure:

```
Kafka
  |
  v
Consumer
  |
  | Process ✓
  |
  X CRASH
  |
  | Commit ❌
  |
  v
Consumer Restart
  |
  v
Same Message
  |
  v
Process Again
```

Therefore:

```
At-Least-Once
      =
Process first
      +
Commit afterward
      +
Possible duplicate
```

---

## ⭐ Interview-ready answer

> Kafka at-least-once delivery means the consumer should not lose a successfully fetched message because the offset is committed only after successful processing. If the consumer crashes after processing but before committing the offset, Kafka will deliver the message again from the last committed offset. Therefore, at-least-once provides stronger protection against message loss but allows duplicate processing. In production, we commonly combine it with idempotent consumer logic, such as event IDs, unique constraints, or upserts, to make duplicate processing safe.

