# Kafka Transactions

Since we just covered Exactly-Once Semantics, **Kafka Transactions** are the mechanism you need to understand next.

A Kafka transaction allows multiple Kafka operations — especially producing records and committing consumer offsets — to be treated as one atomic unit.

The core idea is:

```
Transaction
    |
    +---- Produce records
    |
    +---- Commit consumed offsets
    |
    v
  COMMIT
```

> Either the transaction succeeds as a whole, or its results are not exposed as committed.

---

## 1. Why do we need Kafka Transactions?

Consider this Kafka pipeline:

```
Orders Topic
     |
     v
Order Consumer
     |
     v
Process Order
     |
     v
Payments Topic
```

Suppose the consumer reads:

```
Offset 100 → OrderCreated
```

and produces:

```
PaymentRequested
```

A normal implementation might do:

```
1. Consume OrderCreated
2. Process it
3. Produce PaymentRequested
4. Commit offset 100
```

Now imagine:

```
Produce PaymentRequested ✓
       |
       X
Consumer crashes
       |
Commit offset ❌
```

After restart:

```
Offset 100
    ↓
OrderCreated processed again
    ↓
PaymentRequested produced again
```

Now the output topic could contain:

```
PaymentRequested
PaymentRequested   ❌
```

This is the classic at-least-once duplicate problem.

---

## 2. Kafka Transaction solves this

Instead, Kafka lets us group:

```
Produce output
      +
Commit input offset
```

inside one transaction.

```
BEGIN TRANSACTION
       |
       v
Consume OrderCreated
       |
       v
Process
       |
       +------------------+
       |                  |
       v                  v
Produce output       Commit offset
       |                  |
       +--------+---------+
                |
                v
          COMMIT TRANSACTION
```

Now they succeed or fail together.

---

## 3. The atomicity idea

Suppose:

```
Input Topic

Offset 100 → OrderCreated
```

We want:

```
Output Topic

PaymentRequested
```

and:

```
Consumer Group

Offset = 101
```

to be treated as one logical operation:

```
Transaction
+--------------------------------+
|                                |
| PaymentRequested               |
|                                |
| Consumer Offset = 101          |
|                                |
+--------------------------------+
```

### Success

```
Transaction
    ↓
COMMIT
    ↓
Output visible
+
Offset committed
```

### Failure

```
Transaction
    ↓
ABORT
    ↓
Output not committed
+
Offset not committed as part of transaction
```

That's the fundamental principle.

---

## 4. Basic Kafka transaction APIs

A transactional producer conceptually works like this:

```java
producer.initTransactions();

producer.beginTransaction();

producer.send(record1);
producer.send(record2);

producer.sendOffsetsToTransaction(
    offsets,
    consumerGroupMetadata
);

producer.commitTransaction();
```

If something goes wrong:

```java
producer.abortTransaction();
```

The flow is:

```
initTransactions()
        ↓
beginTransaction()
        ↓
send()
        ↓
send()
        ↓
sendOffsetsToTransaction()
        ↓
commitTransaction()
```

---

## 5. `transactional.id`

To use Kafka transactions, configure a transactional identity:

```
transactional.id=order-service-instance
```

For example:

```java
Properties props = new Properties();

props.put(
    ProducerConfig.TRANSACTIONAL_ID_CONFIG,
    "order-service-1"
);
```

Then:

```java
producer.initTransactions();
```

The `transactional.id` gives Kafka a stable identity for the transactional producer.

---

## 6. Why does Kafka need `transactional.id`?

Imagine an application crashes:

```
Producer A
    |
    | transactional.id = payment-service
    v
Kafka
```

Then a new instance starts:

```
Producer B
    |
    | transactional.id = payment-service
    v
Kafka
```

Kafka needs to know:

> Who is the current producer?

This enables Kafka to handle **producer fencing**.

---

## 7. Producer Fencing

This is an important senior-level Kafka interview concept.

Suppose two instances accidentally use the same transactional ID:

```
Instance A
transactional.id = payment-service
```

and:

```
Instance B
transactional.id = payment-service
```

Kafka can establish a newer producer epoch for the newer instance and fence the old producer.

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

Kafka says:

```
A = old producer ❌
B = current producer ✓
```

The old producer is prevented from continuing to participate as the valid transactional producer.

### Interview answer

> **Producer fencing** prevents a stale producer instance from continuing to write after another producer has taken over the same transactional identity.

---

## 8. Transactional Producer Internally

At a high level, Kafka uses internal transaction-management machinery to coordinate transactional state.

Conceptually:

```
Application
    |
    v
Transactional Producer
    |
    +---- Producer ID
    |
    +---- Producer Epoch
    |
    v
Kafka Transaction Coordinator
    |
    v
Transaction state
```

The **transaction coordinator** is a broker responsible for managing transaction state for a particular transactional ID.

The producer communicates with the coordinator to initialize and manage its transaction.

---

## 9. Transaction Coordinator

This is an important internal concept.

When a producer uses:

```
transactional.id = payment-service-1
```

Kafka maps that transactional identity to a transaction coordinator.

Conceptually:

```
Producer
   |
   | transactional.id
   v
Transaction Coordinator
   |
   +---- Transaction state
   |
   +---- Producer identity/epoch
   |
   +---- Transaction completion
```

The coordinator helps manage:

```
BEGIN
COMMIT
ABORT
```

for the transaction.

---

## 10. But where are the transaction results?

The actual application records are still written to their normal Kafka partitions.

For example:

```
Orders Topic
Partition 0
     |
     v
PaymentRequested
     |
     v
Kafka log
```

The transaction coordinator doesn't become the storage location for your application data.

It coordinates the transaction **state**.

Think:

```
Transaction Coordinator
        |
        | coordinates
        v
Partition Leaders
        |
        | store records
        v
Kafka Logs
```

---

## 11. Transaction flow internally

Let's look at a simplified flow.

### Step 1 — Producer starts

```
Producer
   |
   | initTransactions
   v
Transaction Coordinator
```

Kafka establishes transactional producer state.

### Step 2 — Begin transaction

```
Producer
   |
   | beginTransaction
   v
Coordinator
```

Transaction becomes active.

### Step 3 — Produce records

```
Producer
   |
   +---- Topic A / Partition 0
   |
   +---- Topic B / Partition 2
   |
   +---- Topic C / Partition 1
```

The transaction can span multiple partitions and topics.

### Step 4 — Send offsets to transaction

The consumer's offsets are added to the transaction:

```
Transaction
   |
   +---- Output Record A
   |
   +---- Output Record B
   |
   +---- Consumer Offset
```

### Step 5 — Commit

```
commitTransaction()
       |
       v
Transaction Coordinator
       |
       v
COMMIT
```

The transaction is completed.

---

## 12. Transaction Markers

Kafka uses **transaction markers** in the log to indicate transaction completion.

Conceptually:

```
Kafka Partition

Record A
Record B
Record C
COMMIT marker
```

or:

```
Record A
Record B
ABORT marker
```

These markers help consumers determine which transactional records belong to committed or aborted transactions.

---

## 13. `read_committed`

Now we arrive at a very important consumer configuration:

```
isolation.level=read_committed
```

A consumer can use:

```
read_uncommitted
```

or:

```
read_committed
```

### `read_uncommitted`

The consumer can see records regardless of whether their transaction ultimately commits.

### `read_committed`

The consumer only exposes records from committed transactions.

So:

```
Transaction T1
    |
    +---- A
    +---- B
    |
    X ABORT
```

A `read_committed` consumer won't expose A and B as valid committed transactional results.

---

## 14. Why `read_committed` matters for Exactly-Once

Imagine:

```
Producer
   |
   | Transaction
   v
Kafka
   |
   | ABORT
   v
Consumer
```

If the consumer uses:

```
read_committed
```

it doesn't process the aborted transaction's records.

Therefore:

```
Failed attempt
     ↓
ABORT
     ↓
Output not visible as committed
```

Then the application can retry.

---

## 15. Kafka-to-Kafka Exactly-Once

This is the most important practical example.

Architecture:

```
Topic A
   |
   v
Consumer
   |
   v
Process
   |
   v
Topic B
```

Suppose:

```
Topic A
Offset 100 → OrderCreated
```

Consumer processes it and produces:

```
Topic B
PaymentRequested
```

Transaction:

```
BEGIN
   |
   +---- PaymentRequested
   |
   +---- Commit Offset 101
   |
COMMIT
```

Now both are part of the same transaction.

```
Input offset
     +
Output record
     ↓
Atomic
```

This is the heart of Kafka EOS.

---

## 16. Failure during the transaction

Suppose:

```
BEGIN
  |
  v
Consume OrderCreated
  |
  v
Produce PaymentRequested
  |
  X
Crash
```

The transaction doesn't successfully commit.

After recovery:

```
Input offset
     ↓
still needs processing
     ↓
Process again
     ↓
New transaction
     ↓
Produce PaymentRequested
     ↓
Commit
```

A `read_committed` downstream consumer sees the committed result rather than a committed duplicate from the failed transaction.

---

## 17. Exactly-once is not magic

This is a critical distinction.

Kafka transactions can provide strong exactly-once semantics **within Kafka's transactional boundary**.

They don't automatically make this:

```
Kafka
  ↓
REST API
  ↓
External Database
  ↓
Email
```

exactly once.

Why?

Because now you have multiple independent systems:

- Kafka
- Database
- REST service
- Email provider

A Kafka transaction cannot automatically atomically commit all of them.

---

## 18. Kafka → Database example

Suppose:

```
Kafka
   |
   v
Consumer
   |
   v
MySQL
```

You might do:

```
Consume
   ↓
Update MySQL
   ↓
Commit Kafka offset
```

Failure:

```
Update MySQL ✓
     |
     X
Consumer crashes
     |
Kafka offset ❌
```

After restart:

```
Same Kafka message
      ↓
MySQL update again
```

Kafka transactions alone don't solve this cross-system atomicity problem.

You may need:

```
Idempotent consumer
+
Database transaction
+
Unique constraint / deduplication
```

depending on the use case.

---

## 19. Kafka Transactions vs Idempotent Producer

These are often confused.

### Idempotent producer

```
Producer
   |
   | retry
   v
Kafka
```

Uses producer identity and sequence numbers to prevent duplicate appends caused by retries.

```
Idempotence
     ↓
Duplicate-safe producer retries
```

### Kafka Transactions

```
Produce output
      +
Commit offset
      ↓
Atomic transaction
```

So:

```
Idempotence
   ↓
Producer-level duplicate protection


Transactions
   ↓
Atomic multi-operation Kafka processing
```

---

## 20. Transactions vs At-Least-Once

### At-least-once

```
Consume
   ↓
Process
   ↓
Produce
   ↓
Commit offset
```

Crash:

```
Produce ✓
Commit ❌
```

Retry:

```
Produce again
```

Potential duplicate.

### Transactions

```
BEGIN
   ↓
Consume
   ↓
Process
   ↓
Produce
   +
Commit offset
   ↓
COMMIT
```

Failure:

```
ABORT
```

Then retry safely.

---

## 21. Transactions vs At-Most-Once

At-most-once:

```
Consume
   ↓
Commit
   ↓
Process
```

Risk:

```
Message LOST
```

Transactions:

```
Consume
   ↓
Process
   ↓
Produce
   +
Commit
   ↓
Atomic commit
```

The transaction prevents the classic partial-commit problem within its supported Kafka scope.

---

## 22. Transaction Timeout

Transactions cannot remain open forever.

Kafka has transaction timeout-related configuration, notably:

```
transaction.timeout.ms
```

The producer must complete the transaction within the allowed timeout.

If a transaction takes too long:

```
BEGIN
  |
  | processing...
  |
  | timeout
  v
ABORT
```

Therefore, long-running business operations inside Kafka transactions need careful design.

---

## 23. Performance trade-off

Transactions provide stronger guarantees, but they can introduce additional coordination and latency.

Conceptually:

Normal producer:

```
Produce
  ↓
ACK
```

Transactional producer:

```
Begin
  ↓
Produce
  ↓
Coordinate
  ↓
Commit
```

Therefore:

```
More reliability
      +
More coordination
      ↓
Potentially more latency/overhead
```

You shouldn't enable transactions everywhere without understanding why they're required.

---

## 24. Important production configuration

A simplified transactional producer might use:

```
enable.idempotence=true
transactional.id=payment-service-1
acks=all
```

Consumer:

```
enable.auto.commit=false
isolation.level=read_committed
```

The exact configuration depends on the client/framework and architecture, but these settings illustrate the major concepts.

---

## 25. Important relationship

You should now think about Kafka reliability as layers:

```
                    Kafka Reliability
                           |
        +------------------+------------------+
        |                  |                  |
        v                  v                  v
   Replication          Idempotence       Transactions
        |                  |                  |
        v                  v                  v
       ISR             Producer retry      Atomicity
        |                  |                  |
        v                  v                  v
   Durability         No duplicate       Output + Offset
                                              |
                                              v
                                      Exactly-once Kafka
```

And consumer processing:

```
At-most-once
    ↓
Commit → Process
    ↓
Loss possible


At-least-once
    ↓
Process → Commit
    ↓
Duplicate possible


Exactly-once
    ↓
Process + Output + Offset
    ↓
Atomic Transaction
```

---

## 26. Very important interview trap

### Question:

> Does Kafka transaction guarantee exactly-once delivery to an external REST API?

**Answer: No.**

Example:

```
Kafka Transaction
       |
       +---- Kafka Output ✓
       |
       +---- Kafka Offset ✓
       
       BUT

REST API
       |
       +---- Request already sent
```

The external system isn't automatically part of Kafka's transaction.

You need a separate strategy for external side effects.

---

## 27. Another interview trap

### Does Kafka transaction replace producer idempotence?

Not conceptually.

Transactions rely on idempotent producer behavior as part of Kafka's transactional processing model.

Think:

```
Transactions
      +
Idempotent producer machinery
      ↓
Reliable transactional writes
```

So don't think of them as mutually exclusive features.

---

## 28. Senior interview question

### What happens if the transaction coordinator fails?

Kafka is distributed.

The coordinator itself is associated with Kafka's internal transaction state and can fail over.

The transaction state is persisted in Kafka's internal infrastructure, allowing another broker to take over coordinator responsibility.

Conceptually:

```
Producer
   |
   v
Coordinator A
   |
   X FAILURE
   |
   v
Coordinator B
   |
   v
Continue transaction management
```

The exact recovery details depend on the transaction state and failure point, but the important interview concept is:

> The transaction coordinator is not a single permanent point of failure; Kafka can recover coordinator responsibility.

---

## 29. Senior interview question: What happens if a producer dies?

Suppose:

```
Producer
   |
   | BEGIN
   |
   | Produce
   |
   X CRASH
```

The transaction may remain unresolved until Kafka's transaction timeout/recovery mechanisms determine its outcome.

A new producer instance can take over the transactional identity and fence the old instance.

Then the application can retry the work.

Conceptually:

```
Old Producer
     X
     |
     v
New Producer
     |
     v
Same transactional identity
     |
     v
Old producer fenced
```

---

## 30. One complete example

Let's put everything together.

```
                 Orders Topic
                      |
                      v
              Order Consumer
                      |
                      v
              BEGIN TRANSACTION
                      |
            +---------+---------+
            |                   |
            v                   v
       Process Order       Produce Payment
                                |
                                v
                         Payments Topic
            |
            v
    sendOffsetsToTransaction()
            |
            v
       COMMIT TRANSACTION
```

If successful:

```
Payment output ✓
+
Offset committed ✓
```

If failure:

```
Transaction ABORT
       ↓
Output not committed
       ↓
Offset not committed
       ↓
Input can be processed again
```

Downstream:

```
isolation.level=read_committed
```

means only committed transactional results are consumed.

---

## 31. The most important mental model

Memorize this:

```
                  KAFKA TRANSACTION

                     BEGIN
                       |
                       v
                  Consume input
                       |
                       v
                    Process
                       |
             +---------+---------+
             |                   |
             v                   v
       Produce output       Commit offset
             |                   |
             +---------+---------+
                       |
                       v
                    COMMIT
```

Failure:

```
                    BEGIN
                       |
                       v
                    Process
                       |
                       v
                 Produce output
                       |
                       X
                     CRASH
                       |
                       v
                     ABORT
                       |
                       v
                  Retry later
```

---

## 32. Interview-ready answer

If an interviewer asks:

> Explain Kafka Transactions.

A strong senior-level answer is:

> Kafka Transactions allow a producer to group multiple Kafka operations into an atomic unit. The most important use case is Kafka-to-Kafka processing, where a consumer processes records, produces output records, and commits the consumed offsets as part of the same transaction. If the transaction commits, the output records and offsets are committed together. If it aborts, the transactional output isn't exposed as committed data and the input offset isn't advanced as part of that transaction, allowing the processing to be retried. Kafka uses a transactional ID, producer identity/epoch, transaction coordinator, and transaction markers to manage this behavior. Consumers using `read_committed` only consume committed transactional records. Transactions provide exactly-once semantics within their supported Kafka transactional boundary, but they do not automatically make external database, REST, or other side effects exactly-once.

---

## The sequence you should memorize

```
Idempotent Producer
       ↓
Safe producer retries
       ↓
Kafka Transactions
       ↓
Atomic Output + Offset
       ↓
read_committed
       ↓
Exactly-Once Kafka Processing
```

And the next logical topic is **`acks=0`, `acks=1`, `acks=all` + `min.insync.replicas`**, because that connects your Kafka concepts of Producer → Leader → Followers → ISR → Durability → Data Loss → Retries → Idempotence → Transactions into one complete reliability model.

