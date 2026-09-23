# Kafka Producer Retries

**Producer retries** are Kafka's mechanism for automatically retrying a failed produce request.

The basic idea is:

> If a producer sends a record and the request fails due to a retryable error, the Kafka producer can send the request again automatically.

This is important for handling temporary failures such as:

- broker temporarily unavailable
- network timeout
- leader change
- transient connection failure
- request timeout

---

## 1. Simple example

Suppose:

```
Producer
   |
   | OrderCreated
   v
Broker 1
Leader P0
```

The producer sends:

```
OrderCreated
```

But the network fails before the producer receives the response:

```
Producer
   |
   | OrderCreated
   v
Broker 1
   X
 Network failure
```

The producer doesn't know whether Kafka actually received the message.

It can retry:

```
Producer
   |
   | Retry
   v
Broker 1
   |
   v
ACK
```

Conceptually:

```
Send
 ↓
Failure
 ↓
Retry
 ↓
Success
```

---

## 2. Why are retries necessary?

Distributed systems have transient failures all the time.

Imagine:

```
Producer → Broker
```

The broker successfully writes the record:

```
Broker:
OrderCreated ✓
```

But the ACK gets lost:

```
Broker ───X───> Producer
                  ACK lost
```

The producer thinks:

> "Maybe the message wasn't written."

So it retries:

```
Producer
   |
   | OrderCreated again
   v
Broker
```

Now you have a potential duplicate unless Kafka's idempotent producer mechanism is used.

This leads to a very important relationship:

```
Producer Retries
       ↓
Potential duplicate records
       ↓
Idempotent Producer
       ↓
Avoid duplicate writes caused by producer retries
```

---

## 3. `retries` configuration

Kafka producers have retry-related configuration.

Historically, you could configure:

```
retries=3
```

meaning the producer could retry failed requests.

Modern Kafka producers generally default to effectively allowing retries for retryable failures, subject to delivery timeout and request-level timeout settings.

So in modern Kafka, don't think of `retries` in isolation.

The important settings include:

- `retries`
- `delivery.timeout.ms`
- `request.timeout.ms`

---

## 4. `delivery.timeout.ms`

This is particularly important.

Conceptually:

> `delivery.timeout.ms` limits how long the producer will attempt to successfully deliver a record, including retries.

For example:

```
delivery.timeout.ms = 120000
```

means the producer has roughly a 120-second delivery deadline.

Conceptually:

```
Send
 ↓
Failure
 ↓
Retry
 ↓
Failure
 ↓
Retry
 ↓
Failure
 ↓
...
 ↓
Delivery timeout
 ↓
Final failure
```

So:

```
retries
+
timeouts
```

work together.

---

## 5. `request.timeout.ms`

This controls how long the producer waits for a response to an individual request before considering that request failed/timed out.

For example:

```
request.timeout.ms = 30 seconds
```

Conceptually:

```
Producer
   |
   | Request
   v
Broker
   |
   | no response
   |
   X
30 seconds
   |
   v
Request timeout
   |
   v
Retry
```

But remember:

> `request.timeout.ms` is about an individual request, while `delivery.timeout.ms` is the overall delivery deadline for the record.

---

## 6. The biggest problem with retries: duplicates

Consider:

```
Producer
   |
   | PaymentCompleted
   v
Kafka
```

Kafka writes it successfully:

```
Kafka:
PaymentCompleted ✓
```

But the producer doesn't receive the ACK:

```
Kafka
   X
   |
   | ACK lost
   v
Producer
```

Producer retries:

```
Producer
   |
   | PaymentCompleted
   v
Kafka
```

Without idempotence, you could end up with:

```
Partition:

Offset 100 → PaymentCompleted
Offset 101 → PaymentCompleted
```

Same logical event twice.

This is why **retries** and **idempotence** are closely related.

---

## 7. Idempotent Producer

Kafka supports an **idempotent producer**.

Enable it with:

```
enable.idempotence=true
```

The producer gets an identity and uses sequencing information so Kafka can detect duplicate retry attempts from the same producer session/partition sequence.

Conceptually:

```
Producer
   |
   | sequence 100
   v
Broker
   |
   | record written
   X
 ACK lost
   |
   v
Producer retries
   |
   | sequence 100 again
   v
Broker
   |
   | duplicate detected
   |
   X
```

Kafka can avoid appending the duplicate record.

So:

```
Retry
  +
Idempotence
  =
Safer producer delivery
```

---

## 8. Producer ID and sequence numbers

Internally, idempotent production uses concepts such as:

- Producer ID (PID)
- Sequence number

Conceptually:

```
Producer
   |
   +-- PID = 500
   |
   +-- P0
        |
        +-- Sequence 0
        +-- Sequence 1
        +-- Sequence 2
```

Suppose:

```
Sequence 10 → Event A
```

The broker receives it and writes it.

The ACK is lost.

Producer retries:

```
Sequence 10 → Event A
```

Kafka can recognize that the sequence has already been processed for that producer/partition and avoid appending the duplicate.

This is one of the key mechanisms behind Kafka's idempotent producer.

---

## 9. Retries and message ordering

This is another important interview topic.

Suppose:

```
P0

A
B
C
```

Producer sends:

```
A → success
B → failure
C → success?
```

Kafka's producer implementation has ordering guarantees around retries when idempotence is enabled.

For reliable ordered production, an important configuration is:

```
enable.idempotence=true
```

and Kafka enforces compatible producer settings.

With idempotence enabled, Kafka can preserve the correct sequence of records while handling retries.

Without idempotence, aggressive retries and multiple in-flight requests historically could create ordering problems.

So the modern recommendation is:

> Use idempotence when you care about reliable, ordered producer delivery.

---

## 10. `max.in.flight.requests.per.connection`

This configuration is related to producer ordering and retries.

Suppose the producer has multiple requests in flight:

```
Producer
   |
   +---- Request 1 → A
   |
   +---- Request 2 → B
```

Imagine:

```
Request 1 → fails
Request 2 → succeeds
```

Then retrying Request 1 later could potentially cause:

```
B
A
```

rather than:

```
A
B
```

Modern Kafka's idempotent producer handles ordering safely within its supported constraints.

The key interview point is:

> Producer retries and multiple in-flight requests can interact with ordering; idempotence is the mechanism you should enable when you need reliable ordering across retries.

---

## 11. Retries and `acks`

Retries also interact with producer acknowledgements.

### `acks=0`

Producer doesn't wait for a broker acknowledgement.

```
Producer
   |
   | message
   v
Broker
```

Because the producer doesn't wait for an ACK, there is little opportunity to know that a request failed at the broker level.

This gives lower latency but weaker delivery guarantees.

### `acks=1`

Leader acknowledges the record after accepting it according to the leader's write semantics.

```
Producer
   |
   v
Leader
   |
   v
ACK
```

If the leader fails before followers have replicated the record, durability depends on the replication state and configuration.

### `acks=all`

The producer waits for the required in-sync replicas according to Kafka's replication acknowledgement rules and `min.insync.replicas`.

```
Producer
   |
   v
Leader
  / \
 v   v
F1   F2
  \ /
   |
  ACK
```

This provides stronger durability.

---

## 12. Retry + `acks=all` + ISR

Now connect everything you've learned.

Suppose:

```
Replication Factor = 3
min.insync.replicas = 2
acks = all
```

Cluster:

```
P0

B1 → Leader
B2 → Follower
B3 → Follower

ISR = B1, B2, B3
```

Producer sends:

```
OrderCreated
```

If there is a temporary failure:

```
Producer
   |
   v
B1
   |
   +----> B2
   |
   +----> B3
```

If the request fails before the producer gets the required acknowledgement:

```
Failure
  ↓
Retry
```

The producer retries within its delivery timeout.

If idempotence is enabled:

```
Retry
 ↓
Duplicate detection
 ↓
No duplicate append
```

This is the robust production model.

---

## 13. What happens during a leader change?

This is a very common real-world scenario.

Initially:

```
P0

B1 → Leader
B2 → Follower
B3 → Follower
```

Producer sends:

```
OrderCreated
```

Then B1 fails:

```
B1 → ❌
```

Kafka elects B2:

```
B2 → New Leader
```

The producer may initially try to communicate with B1 and receive a failure/metadata-related error.

The producer refreshes metadata:

```
Old metadata
   ↓
Leader B1
   X
   ↓
Metadata refresh
   ↓
New metadata
   ↓
Leader B2
   ↓
Retry
```

Then:

```
Producer → B2 → success
```

This is one of the most important practical uses of producer retries.

---

## 14. Retryable vs non-retryable errors

Not every error should simply be retried forever.

### Retryable/transient examples

Conceptually:

- Temporary network problem
- Leader changed
- Broker temporarily unavailable
- Request timeout

Producer can retry.

### Non-retryable examples

Some errors indicate a permanent problem with the request or configuration.

For example:

- Invalid record
- Authorization failure
- Invalid configuration
- Message too large

Repeatedly retrying such an error doesn't solve the underlying problem.

So Kafka distinguishes retryable conditions from errors that should ultimately fail the send.

---

## 15. Retry flow

The overall flow:

```
                    Producer
                       |
                       v
                   Send record
                       |
                       v
                    Broker
                       |
              +--------+--------+
              |                 |
            Success           Failure
              |                 |
              v                 v
             ACK          Is it retryable?
                               |
                    +----------+----------+
                    |                     |
                   Yes                    No
                    |                     |
                    v                     v
                  Retry              Final failure
                    |
                    v
               Delivery timeout?
                    |
              +-----+-----+
              |           |
             No          Yes
              |           |
              v           v
            Retry      Final failure
```

---

## 16. Retry and idempotence

This is the relationship to memorize:

```
                Producer Retry
                     |
                     v
              Could duplicate
                     |
                     v
            enable.idempotence
                     |
                     v
          PID + sequence numbers
                     |
                     v
        Duplicate retry detection
```

So:

> Retries improve availability, while idempotence prevents retry-related duplicate records.

---

## 17. Retry and exactly-once semantics

Retries alone do not mean exactly-once processing.

This is extremely important.

```
Retries ≠ Exactly Once
```

For example:

```
Producer
   |
   | Event
   v
Kafka
   |
   | stored
   X
 ACK lost
   |
   v
Producer retries
```

Idempotence helps prevent duplicate writes from producer retries.

But **exactly-once processing semantics** involve additional Kafka mechanisms, such as transactions, depending on the architecture.

So:

```
Retry
   ↓
At-least-once style delivery behavior
```

while:

```
Idempotent Producer
   ↓
Duplicate-safe producer retries
```

and:

```
Transactions
   ↓
Exactly-once semantics for supported Kafka processing/production workflows
```

---

## 18. Real-world payment example

Imagine a payment service:

```
Payment Service
      |
      | PaymentCompleted
      v
    Kafka
```

Network problem occurs:

```
Payment Service
      |
      | PaymentCompleted
      v
Kafka
      X
    timeout
```

Producer retries:

```
Payment Service
      |
      | retry
      v
Kafka
```

With idempotence enabled:

```
First request → written
ACK → lost

Retry → duplicate request detected
       ↓
No duplicate record appended
```

This is much safer for payment-related event publishing.

---

## 19. Recommended modern producer configuration

A common reliable baseline is conceptually:

```
enable.idempotence=true
acks=all
```

Then tune:

- `delivery.timeout.ms`
- `request.timeout.ms`

according to your application's latency and failure requirements.

Don't blindly set an enormous retry count.

The goal is:

```
Reliable
   +
Idempotent
   +
Bounded retry time
   +
Strong acknowledgement
```

---

## 20. Interview questions

### Q1. Why does Kafka producer retry?

> To recover from transient failures such as network errors, broker failures, leader changes, and request timeouts.

### Q2. Can retries cause duplicate messages?

> Yes, if the broker accepted the message but the producer didn't receive the acknowledgement.

### Q3. How does Kafka prevent duplicates caused by retries?

> With the idempotent producer, Kafka uses producer identity and sequence information to detect duplicate retry attempts.

### Q4. Does `retries=3` mean Kafka will always retry exactly three times?

> No. Modern Kafka producer delivery is governed by retryable errors and timeout settings such as `delivery.timeout.ms`; the retry count should not be considered independently.

### Q5. Does producer retry guarantee exactly-once processing?

**No.**

> Retries and idempotence address producer delivery behavior. Exactly-once processing requires the appropriate transactional/idempotent architecture.

### Q6. What happens when the partition leader changes?

```
Leader failure
      ↓
New leader elected
      ↓
Producer receives error / cannot reach old leader
      ↓
Metadata refresh
      ↓
Retry
      ↓
New leader
```

---

## 21. The complete Kafka reliability picture

Now connect your previous topics:

```
                    Kafka Producer
                         |
                      Key
                         |
                         v
                    Partitioner
                         |
                         v
                     Partition
                         |
                         v
                    Leader
                   /       \
                  /         \
             Follower     Follower
                  \         /
                   \       /
                      ISR
                       |
                 acks=all
                       |
              min.insync.replicas
                       |
                    ACK
                       |
              +--------+--------+
              |                 |
           Success           Failure
                                |
                              Retry
                                |
                         Idempotence
                                |
                        Duplicate protection
```

---

## ⭐ Interview-ready answer

> Kafka producer retries allow a producer to automatically retry transiently failed produce requests. A retry can happen when the broker is unavailable, the leader changes, or a request times out. However, retries can create duplicates if the broker successfully writes the record but the acknowledgement is lost. Kafka's idempotent producer uses producer IDs and sequence numbers to prevent duplicate appends caused by retries. For reliable production, a common configuration is `enable.idempotence=true` with `acks=all`, while `delivery.timeout.ms` and `request.timeout.ms` control how long delivery attempts are allowed to continue.

