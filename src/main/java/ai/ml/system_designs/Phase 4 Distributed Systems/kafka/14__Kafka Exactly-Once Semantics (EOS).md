# Kafka Exactly-Once Semantics (EOS)

**Exactly-once** means:

> For a given transactional processing flow, Kafka ensures that a record's effect is visible only once, even if failures and retries occur.

The important point is that Exactly-once is **not** simply "no duplicates." It is about making a Kafka processing pipeline **atomic** so that you don't end up with a partial result.

---

## 1. First understand the problem

Suppose we have:

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
Payment Topic
```

Consumer reads:

```
OrderCreated
```

and produces:

```
PaymentRequested
```

A naive implementation is:

```
Kafka Input
    ↓
Consume
    ↓
Process
    ↓
Produce Output
    ↓
Commit Offset
```

Now imagine:

```
Consume OrderCreated
        ↓
Produce PaymentRequested ✓
        ↓
Consumer crashes
        ↓
Offset NOT committed
```

After restart:

```
Consume OrderCreated AGAIN
        ↓
Produce PaymentRequested AGAIN
```

You could get:

```
PaymentRequested
PaymentRequested   ← duplicate
```

That's at-least-once behavior.

---

## 2. Exactly-once solves this using Kafka Transactions

Kafka provides **transactions** that allow a producer to atomically:

- Produce output records
- Commit the consumed offsets

Conceptually:

```
       Kafka Input Topic
              |
              v
          Consumer
              |
              v
          Processing
              |
       +------+------+
       |             |
       v             v
 Output Topic    Consumer Offset
       |             |
       +------+------+
              |
              v
        Kafka Transaction
              |
          COMMIT / ABORT
```

The key idea:

```
Output records
      +
Consumed offsets
      ↓
Atomic transaction
```

---

## 3. The classic failure problem

Consider:

```
Input Topic

Offset 100 → OrderCreated
```

Consumer processes it:

```
OrderCreated
      ↓
PaymentRequested
```

Suppose output is successfully written:

```
Payment Topic

PaymentRequested ✓
```

But before committing offset 101:

```
Consumer CRASH ❌
```

After restart:

```
Offset 100
   ↓
Process again
   ↓
PaymentRequested
```

Without transactions:

```
Payment Topic:

PaymentRequested
PaymentRequested ❌
```

---

## 4. With Exactly-Once

With a Kafka transaction:

```
BEGIN TRANSACTION
       |
       v
Read OrderCreated
       |
       v
Process
       |
       v
Write PaymentRequested
       |
       v
Commit consumed offset
       |
       v
COMMIT TRANSACTION
```

The output and offset commit become one atomic unit.

```
Transaction
+--------------------------------+
|                                |
| PaymentRequested               |
|                                |
| Consumed offset = 101          |
|                                |
+--------------------------------+
              |
              v
           COMMIT
```

If everything succeeds:

```
Output ✓
Offset ✓
```

If something fails:

```
Output ✗
Offset ✗
```

That's the core of Kafka EOS.

---

## 5. What happens during a crash?

Suppose:

```
BEGIN TRANSACTION
       ↓
Process OrderCreated
       ↓
Produce PaymentRequested
       ↓
CRASH ❌
```

The transaction doesn't successfully commit.

Kafka can abort the transaction / make the transactional records unavailable to `read_committed` consumers.

After recovery:

```
Input offset
     ↓
still needs processing
     ↓
Process OrderCreated again
     ↓
Produce PaymentRequested
     ↓
Commit transaction
```

The consumer of the output topic using:

```
isolation.level=read_committed
```

sees only committed transactional records.

Therefore, the failed attempt's output doesn't become a visible duplicate to that consumer.

---

## 6. Transactional Producer

To use Kafka transactions, the producer needs a:

```
transactional.id=my-service-1
```

and transactions are initialized through the producer API.

Conceptually:

```java
producer.initTransactions();

producer.beginTransaction();

producer.send(outputRecord);

producer.sendOffsetsToTransaction(
    offsets,
    consumerGroupMetadata
);

producer.commitTransaction();
```

The important sequence is:

```
beginTransaction()
       ↓
process
       ↓
send output
       ↓
sendOffsetsToTransaction()
       ↓
commitTransaction()
```

If something fails:

```java
producer.abortTransaction();
```

---

## 7. `transactional.id`

`transactional.id` is important because it gives the transactional producer a stable identity.

For example:

```
transactional.id=payment-service-instance-1
```

Kafka uses transactional producer identity to manage transactional state and producer epochs.

A very important production concept is **fencing**.

---

## 8. Producer Fencing

Suppose two instances accidentally use the same transactional identity:

```
Instance A
transactional.id = payment-service

Instance B
transactional.id = payment-service
```

Kafka must prevent both from independently continuing the same transactional identity.

Conceptually:

```
Instance A
   |
   | transactional.id = X
   v
 Kafka
   ^
   |
   | transactional.id = X
   |
Instance B
```

A newer producer instance can cause the older one to become **fenced**.

This prevents a stale producer from continuing to write as if it were still the valid transactional producer.

Interview keyword:

> **Producer fencing** prevents an old/stale producer instance from continuing to participate in a transaction after a newer producer instance has taken over the same transactional identity.

---

## 9. `read_committed`

Transactions introduce another important consumer setting:

```
isolation.level=read_committed
```

There are two important modes:

- `read_uncommitted`
- `read_committed`

### `read_uncommitted`

Consumer can see records from transactions that haven't successfully committed.

### `read_committed`

Consumer sees only committed transactional records.

For an EOS pipeline:

```
Producer
   ↓
Transaction
   ↓
Kafka
   ↓
read_committed Consumer
```

is the typical model.

---

## 10. `read_committed` vs `read_uncommitted`

Imagine Kafka contains:

```
Transaction T1
    |
    +-- PaymentRequested
    |
    X ABORTED
```

With:

```
read_uncommitted
```

the consumer may observe transactional records that were written but later aborted.

With:

```
read_committed
```

the aborted records are not delivered to the application.

So:

```
read_committed
      ↓
Only committed transactional results
```

---

## 11. Exactly-once is NOT simply idempotence

This distinction is extremely important.

### Idempotent Producer

Protects:

```
Producer
   ↓
Kafka
```

against duplicate appends caused by retries.

```
Retry
  ↓
PID + Sequence Number
  ↓
Duplicate append prevented
```

### Exactly-Once Transactions

Coordinate:

```
Input consumption
       +
Output production
       +
Offset commit
```

atomically.

So:

```
Idempotence
    ↓
Duplicate-safe producer


Transactions
    ↓
Atomic output + offset
```

---

## 12. Exactly-once vs At-Least-Once

### At-least-once

```
Consume
   ↓
Process
   ↓
Produce output
   ↓
Commit offset
```

Failure:

```
Produce ✓
Commit ❌
Crash
```

Then:

```
Process again
   ↓
Produce again
```

Potential duplicate output.

### Exactly-once

```
BEGIN TRANSACTION
       ↓
Consume/process
       ↓
Produce output
       +
Commit offset
       ↓
COMMIT
```

Failure:

```
Transaction fails
       ↓
Output not committed
       +
Offset not committed
       ↓
Retry
```

This avoids exposing the failed attempt as a committed duplicate.

---

## 13. Real-world payment example

Imagine:

```
Orders Topic
     |
     v
Payment Service
     |
     v
Payments Topic
```

Input:

```
OrderCreated
orderId=ORD-100
```

Processing:

```
OrderCreated
      ↓
Calculate payment
      ↓
PaymentRequested
```

Exactly-once transaction:

```
BEGIN
  |
  +-- Produce PaymentRequested
  |
  +-- Commit input offset
  |
COMMIT
```

If transaction commits:

```
Payments Topic:

PaymentRequested
```

If service crashes before commit:

```
Transaction aborted
```

On restart:

```
OrderCreated
      ↓
Process again
      ↓
New transaction
      ↓
PaymentRequested
      ↓
COMMIT
```

The downstream `read_committed` consumer sees the committed result rather than two committed outputs from the failed and successful attempts.

---

## 14. Exactly-once is especially useful for Kafka → Kafka pipelines

The strongest Kafka EOS use case is:

```
Topic A
   ↓
Consumer
   ↓
Processing
   ↓
Topic B
```

For example:

```
Orders
  ↓
Order Processor
  ↓
Enriched Orders
```

or:

```
Transactions
      ↓
Fraud Processor
      ↓
Fraud Results
```

Kafka transactions can atomically coordinate the output records and consumed offsets.

---

## 15. What about Kafka → Database?

This is a very important interview trap.

Suppose:

```
Kafka
  ↓
Consumer
  ↓
MySQL
```

**Kafka transactions alone do not automatically make the Kafka + MySQL operation exactly-once.**

Why?

Because now you have two different systems:

```
Kafka
  +
MySQL
```

Kafka transaction cannot automatically make an arbitrary database transaction atomic with Kafka's transaction.

For example:

```
Kafka consume
     ↓
MySQL update ✓
     ↓
Kafka offset commit ❌
```

After restart:

```
Kafka message processed again
```

Your database operation may be repeated.

You need additional patterns such as:

```
Idempotent consumer
      +
Database transaction
      +
Unique constraint / deduplication
```

or architecture-specific transaction integration.

So don't say:

> "Kafka exactly-once guarantees exactly-once across my entire distributed system."

It doesn't.

---

## 16. Exactly-once has a scope

This is perhaps the most important advanced concept.

Exactly-once means:

> **Exactly-once semantics within a defined transactional boundary and for consumers that observe committed transactional results.**

For example:

```
Kafka Topic A
      ↓
Kafka Streams
      ↓
Kafka Topic B
```

Kafka can provide strong EOS guarantees for this processing topology.

But:

```
Kafka
  ↓
REST API
  ↓
External Database
  ↓
Email Provider
```

is a different problem.

You cannot simply say:

```
Kafka EOS
    ↓
Entire distributed system
    ↓
Exactly once
```

That's incorrect.

---

## 17. Kafka Streams and Exactly-Once

Kafka Streams provides higher-level support for exactly-once processing.

Conceptually:

```
Input Topic
     ↓
Kafka Streams
     ↓
Transform
     ↓
Output Topic
```

Kafka Streams can use Kafka transactions to coordinate:

```
Input processing
      +
State store updates
      +
Output records
      +
Offsets
```

This is one of the major practical uses of Kafka's EOS capabilities.

---

## 18. Exactly-once and retries

Consider:

```
Producer
   |
   | Message
   v
Kafka
   |
   | ACK lost
   X
Producer
   |
   | Retry
   v
Kafka
```

Idempotence prevents duplicate producer appends.

Now add transactions:

```
Producer
   |
   | Transaction
   v
Kafka
   |
   +-- Output
   |
   +-- Offset
   |
   v
COMMIT
```

So the reliability chain becomes:

```
Retries
   ↓
Idempotence
   ↓
Transactions
   ↓
Atomic output + offsets
   ↓
Exactly-once Kafka processing
```

---

## 19. Exactly-once and ISR

You previously learned ISR.

They are related but solve different problems.

```
ISR
 ↓
Replication / durability
```

while:

```
Idempotence
 ↓
Duplicate-safe producer retries
```

and:

```
Transactions
 ↓
Atomic processing
```

So:

```
Replication
     ↓
Can the data survive broker failure?


Idempotence
     ↓
Can producer retries avoid duplicate appends?


Transactions
     ↓
Can output + offsets be committed atomically?
```

These are different dimensions of Kafka reliability.

---

## 20. At-most-once vs At-least-once vs Exactly-once

This table is worth memorizing:

| | At-Most-Once | At-Least-Once | Exactly-Once |
|---|---|---|---|
| Message loss | Possible | Minimized/avoided | Avoided within scope |
| Duplicate processing | No | Possible | Prevented within transactional scope |
| Offset | Commit before processing | Commit after processing | Offset committed atomically with transaction |
| Failure behavior | May skip | May reprocess | Transaction abort/retry |
| Complexity | Low | Medium | High |
| Performance | Generally simpler | Generally efficient | More overhead |
| Typical use | Non-critical data | Most business processing | Critical Kafka processing pipelines |

---

## 21. The three diagrams to memorize

### At-most-once

```
Consume
   ↓
Commit
   ↓
Process

Risk:

LOSS
```

### At-least-once

```
Consume
   ↓
Process
   ↓
Commit

Risk:

DUPLICATE
```

### Exactly-once

```
Consume
   ↓
Process
   ↓
Produce output
   +
Commit offset
   ↓
Atomic Transaction
   ↓
COMMIT

Failure:

ABORT
   ↓
Retry
```

---

## 22. Interview Questions

### Q1. What is Kafka exactly-once semantics?

> Exactly-once semantics ensure that within Kafka's transactional processing boundary, a message's processing result is committed atomically with the consumed offset, so failures and retries don't result in multiple committed output results being observed by `read_committed` consumers.

### Q2. Does idempotent producer provide exactly-once?

**No.**

```
Idempotence
    ≠
Exactly-once
```

Idempotence primarily prevents duplicate producer appends caused by retries.

Transactions coordinate:

```
Output
+
Offset
```

atomically.

### Q3. What is `transactional.id`?

> It identifies a transactional producer identity and enables Kafka to manage transactional state and fencing for that producer.

### Q4. What is producer fencing?

> Fencing prevents an old producer instance from continuing to write when a newer producer instance has taken ownership of the same transactional identity.

### Q5. What is `read_committed`?

> It tells the consumer to return only records from successfully committed transactions, hiding aborted transactional records from the application.

### Q6. Can Kafka exactly-once guarantee exactly-once database updates?

**Not by itself.**

```
Kafka Transaction
       ❌
Kafka + arbitrary external DB atomicity
```

You need additional mechanisms.

### Q7. What happens if the consumer crashes during a transaction?

Conceptually:

```
BEGIN
 ↓
Process
 ↓
Produce
 ↓
CRASH
```

The transaction does not successfully commit.

After recovery, the input offset remains eligible for processing, and the transaction can be retried. Consumers using `read_committed` won't treat the aborted output as a committed result.

---

## 23. Senior-level interview answer

If the interviewer asks:

> Explain Kafka Exactly-Once Semantics internally.

A strong answer is:

> Kafka provides exactly-once semantics primarily through idempotent producers and transactions. The producer is assigned a transactional identity and Kafka tracks producer state to prevent stale producers through fencing. During a transaction, the application can produce output records and send the consumed offsets to the transaction. Kafka commits the output records and offsets atomically. If the transaction succeeds, both become committed; if it fails, the transaction is aborted and the input offset isn't advanced as part of that transaction. Consumers configured with `read_committed` only see committed transactional records. This prevents a failed attempt from producing a committed duplicate in a Kafka-to-Kafka processing pipeline. However, Kafka EOS does not automatically make operations involving arbitrary external systems such as databases or REST APIs exactly-once.

---

## 24. Complete Kafka reliability picture

Now you can connect the concepts you've learned:

```
                         KAFKA RELIABILITY
                                |
       +------------------------+------------------------+
       |                        |                        |
       v                        v                        v
   Replication              Idempotence             Transactions
       |                        |                        |
       v                        v                        v
      ISR                 Producer retries          Atomic output
       |                        |                   + offsets
       v                        v                        |
    Durability             No duplicate                 v
                         producer append            Exactly-once
                                                       processing
```

And the delivery semantics:

```
              Delivery Semantics
                     |
        +------------+------------+
        |            |            |
        v            v            v
   At-most-once At-least-once Exactly-once
        |            |            |
        v            v            v
    Loss risk    Duplicate     Transactional
                   risk          atomicity
```

The key sequence to remember for interviews is:

```
At-most-once
    Commit → Process

At-least-once
    Process → Commit

Exactly-once
    Process
       +
    Produce output
       +
    Commit offset
       ↓
    Atomic Kafka Transaction
```

The next concept that naturally follows is **Kafka `acks=0`, `acks=1`, and `acks=all`**, because it ties together producer reliability, leader/follower replication, ISR, durability, retries, and data loss.

