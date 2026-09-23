# Kafka Retry Topics

A **Retry Topic** is a Kafka topic used to temporarily hold messages that failed processing so they can be retried after a delay, instead of immediately retrying the message in a tight loop.

This is especially useful when the failure is **temporary**, such as a downstream API outage, database timeout, or rate limiting.

---

## 1. Why do we need Retry Topics?

Consider:

```
Kafka
  |
  v
Order Consumer
  |
  v
Payment Service
  |
  X
HTTP 503
```

If the consumer immediately retries:

```
Order → Payment API → 503
Order → Payment API → 503
Order → Payment API → 503
Order → Payment API → 503
...
```

This creates:

- retry storms
- excessive CPU usage
- excessive network traffic
- more load on the failing service
- potentially increasing Kafka consumer lag
- cascading failures

Instead, use a retry topic:

```
orders
  |
  v
Consumer
  |
  X
Temporary failure
  |
  v
orders.retry
  |
  | wait
  v
Consumer
  |
  v
Payment Service
```

The key idea is:

> **Retry Topics provide delayed/asynchronous retries without continuously blocking the main processing path.**

---

## 2. Retry Topic vs DLT

You just learned about DLT, so this distinction is important.

### Retry Topic

Used when:

> "This might succeed later."

Examples:

- HTTP 503
- Database timeout
- Connection timeout
- Rate limit
- Temporary dependency outage

### Dead-Letter Topic

Used when:

> "Normal retries have failed or this message is not expected to succeed automatically."

Examples:

- Invalid JSON
- Invalid schema
- Missing required field
- Invalid business data
- Unsupported event

So:

```
                 Processing Failure
                        |
              +---------+---------+
              |                   |
          Temporary            Permanent
              |                   |
              v                   v
        Retry Topic              DLT
              |
           Retry
              |
         +----+----+
         |         |
      Success    Failure
         |         |
         v         v
      Commit      Retry
                  ...
                    |
                max retries
                    |
                    v
                   DLT
```

---

## 3. Why not simply retry in memory?

Suppose the consumer does:

```java
try {
    process(message);
} catch(Exception e) {
    sleep(30 seconds);
    retry(message);
}
```

This has several problems.

### Consumer thread is occupied

```
Consumer
   |
   +-- Message A → waiting 30 sec
   |
   +-- Message B
   +-- Message C
```

If many messages fail:

```
A → wait
B → wait
C → wait
D → wait
...
```

Your consumer can become blocked.

### Application restart loses in-memory retry state

If the application crashes:

```
Retry in memory
      |
      X
Application crashes
      |
      v
Retry state lost
```

### Scaling is harder

Retry state lives inside application memory rather than Kafka.

> Retry topics solve this by making retry state durable and distributed through Kafka.

---

## 4. Basic Retry Topic architecture

A simple architecture:

```
                    +----------------+
                    |  orders topic  |
                    +-------+--------+
                            |
                            v
                    +---------------+
                    | Order Consumer |
                    +-------+-------+
                            |
                     Process Order
                            |
                    +-------+-------+
                    |               |
                 Success          Failure
                    |               |
                    v               v
                 Commit        orders.retry
                                    |
                                  delay
                                    |
                                    v
                             Order Consumer
                                    |
                                    v
                              Process Again
```

---

## 5. Multiple Retry Topics

In production, you often need multiple retry levels.

For example:

```
orders
   |
   v
Consumer
   |
   X
failure
   |
   v
orders.retry.1
   |
   | 1 second
   v
Consumer
   |
   X
failure
   |
   v
orders.retry.2
   |
   | 10 seconds
   v
Consumer
   |
   X
failure
   |
   v
orders.retry.3
   |
   | 1 minute
   v
Consumer
   |
   X
failure
   |
   v
orders.DLT
```

This is commonly called a **retry ladder** or **retry pipeline**.

---

## 6. Example retry schedule

Suppose:

```
orders
```

Message:

```
OrderCreated(orderId=1001)
```

Payment service is temporarily unavailable.

You could configure:

```
Attempt 1
Immediate
     ↓
failure

Retry 1
wait 1 second
     ↓
failure

Retry 2
wait 10 seconds
     ↓
failure

Retry 3
wait 1 minute
     ↓
failure

DLT
```

Conceptually:

```
orders
  |
  v
consumer
  |
  X
  |
  v
retry-1 ── 1 sec ──> consumer
                         |
                         X
                         |
                         v
retry-2 ── 10 sec ─> consumer
                         |
                         X
                         |
                         v
retry-3 ── 1 min ──> consumer
                         |
                         X
                         |
                         v
                       DLT
```

---

## 7. How is the delay implemented?

This is an important Kafka interview question.

Kafka itself fundamentally stores records in an ordered log. A Kafka topic does **not** simply provide a generic "deliver this record exactly 30 seconds later" primitive.

So applications/frameworks implement delayed retry using patterns such as:

### Approach 1 — Retry topics

```
main topic
    ↓
retry topic
    ↓
consumer checks timing
```

### Approach 2 — Multiple retry topics

```
retry-1 → short delay
retry-2 → medium delay
retry-3 → long delay
```

### Approach 3 — Timestamp-based retry processing

A retry record contains something like:

```
availableAt = 10:35:00
```

The retry consumer processes it when it becomes eligible.

### Approach 4 — Framework-managed retry

Frameworks such as Spring Kafka can provide retry-topic abstractions that manage much of this routing.

---

## 8. Retry metadata

A retry record should carry enough information to understand its history.

For example:

```
originalTopic = orders
originalPartition = 3
originalOffset = 10521

retryAttempt = 2

firstFailureTime = ...
lastFailureTime = ...

exceptionType = TimeoutException

nextRetryTime = ...
```

Conceptually:

```
+----------------------------------+
| Retry Message                    |
+----------------------------------+
| Original Topic                   |
| Original Partition               |
| Original Offset                  |
| Original Key                     |
| Original Payload                 |
| Retry Attempt                    |
| Exception Type                   |
| Error Message                    |
| First Failure Time               |
| Last Failure Time                |
| Next Retry Time                  |
+----------------------------------+
```

This is extremely useful for debugging.

---

## 9. Retry topics and offsets

Suppose:

```
orders

P0:
100
101
102
103
```

Message 102 fails.

If you put it into a retry topic:

```
orders
  |
  +-- 100 → success
  +-- 101 → success
  +-- 102 → retry topic
  +-- 103 → can continue
```

The original consumer can commit its progress after successfully publishing the retry record.

Then:

```
orders.retry
    |
    v
retry consumer
    |
    v
process 102 again
```

This is one of the major advantages of retry topics:

> The main consumer doesn't necessarily have to remain blocked behind a temporarily failing message.

---

## 10. But there is an ordering problem

This is a very important senior-level issue.

Suppose one partition contains:

```
P0:

100 → OrderCreated
101 → PaymentCompleted ❌
102 → OrderShipped
```

You send 101 to a retry topic:

```
101 → retry
```

and continue:

```
102 → process
```

Now:

```
OrderShipped
```

might be processed before:

```
PaymentCompleted
```

That's potentially incorrect.

Therefore:

> Retry topics can change the original processing sequence.

You must decide whether the business workflow can tolerate that.

---

## 11. When retry topics are safe

Suppose these are independent events:

```
UserLoggedIn
ProductViewed
SearchPerformed
```

If:

```
ProductViewed
```

fails temporarily, processing:

```
SearchPerformed
```

may be perfectly acceptable.

Retry topic:

```
events
  |
  +-- A → success
  +-- B → retry
  +-- C → success
```

is fine.

---

## 12. When retry topics can be dangerous

Consider:

```
OrderCreated
PaymentCompleted
OrderShipped
OrderDelivered
```

If:

```
PaymentCompleted
```

goes to retry:

```
OrderCreated
      ↓
PaymentCompleted → retry
      ↓
OrderShipped → processed
```

You may violate the business lifecycle.

For these cases you may need:

- partition pausing
- blocking retries
- ordered retry design
- smaller ordering boundaries
- state-aware processing
- application-level sequencing.

---

## 13. Retry Topic + Partition Key

Retry topics should be designed carefully with partitioning.

Suppose:

```
orders
key = orderId
```

You may want:

```
orders
    key=O101
       ↓
orders.retry
    key=O101
```

Preserving the key helps maintain predictable partitioning.

However, moving a message to another topic means it is now governed by the retry topic's partitioning and consumption model.

So you should explicitly design:

```
Original topic partitioning
          +
Retry topic partitioning
          +
Ordering requirement
```

---

## 14. Retry topic and consumer groups

You can have:

```
orders
   |
   v
Order Consumer Group
```

and:

```
orders.retry
   |
   v
Retry Consumer Group
```

The retry consumer reads retry records and eventually sends them back to the processing path.

Conceptually:

```
Main Group
    |
    v
orders
    |
    v
Application


Retry Group
    |
    v
orders.retry
    |
    v
Application
```

The exact group/topic topology depends on the framework and retry architecture.

---

## 15. Exponential backoff

A retry system often uses **exponential backoff**.

For example:

```
Attempt 1 → 1 sec
Attempt 2 → 2 sec
Attempt 3 → 4 sec
Attempt 4 → 8 sec
Attempt 5 → 16 sec
```

Formula:

```
delay = initialDelay × 2^(attempt-1)
```

You may also use a maximum:

```
delay = min(initialDelay × 2^(attempt-1), maxDelay)
```

For example:

```
initial = 1 sec
max = 60 sec
```

Then:

```
1
2
4
8
16
32
60
60
60
```

---

## 16. Add jitter

A senior-level improvement is **jitter**.

Imagine 100,000 messages fail at exactly:

```
10:00:00
```

Without jitter:

```
Retry after 10 sec
      ↓
10:00:10
      ↓
100K messages retry simultaneously 🔥
```

Instead:

```
10:00:08
10:00:11
10:00:13
10:00:15
...
```

This spreads the load.

So:

```
Exponential backoff
        +
Jitter
        ↓
Better retry distribution
```

---

## 17. Retry storm

This is one of the most common distributed-system failure scenarios.

Suppose:

```
Kafka
 ↓
1,000 consumers
 ↓
Payment Service
```

Payment service goes down.

All 1,000 consumers retry immediately:

```
Payment Service
      ↑
1,000 retries
      ↑
failure
      ↑
2,000 retries
      ↑
failure
```

The service becomes even less likely to recover.

Retry topics help spread retry traffic over time:

```
Kafka
 ↓
Consumer
 ↓
Retry Topic
 ↓
Delay
 ↓
Consumer
 ↓
Payment Service
```

---

## 18. Retry topics vs in-memory retry

| | In-memory retry | Retry topic |
|---|---|---|
| Durable | No/limited | Yes |
| Survives restart | Usually no | Yes |
| Distributed | Harder | Yes |
| Delay support | Easy | Requires design/framework |
| Main consumer blocked | Often | Can avoid |
| Scalable | Limited | Better |
| Operational visibility | Lower | Higher |
| Kafka-native | No | Yes |

---

## 19. Retry Topic vs DLT

| | Retry Topic | DLT |
|---|---|---|
| Purpose | Temporary failure | Final/unresolved failure |
| Expected outcome | Eventually succeeds | Requires investigation/fix |
| Delay | Usually yes | Usually no |
| Example | HTTP 503 | Invalid JSON |
| Number of attempts | Limited | After retries exhausted |
| Replay | Automatic | Usually controlled/manual |

---

## 20. Retry topics and backpressure

This directly connects to your previous topic.

Suppose:

```
Kafka
 ↓
Consumer
 ↓
Payment Service
```

Payment service slows down.

Without retry management:

```
Consumer
 ↓
Payment Service
 ↓
timeout
 ↓
immediate retry
 ↓
timeout
 ↓
immediate retry
```

With retry topics:

```
Consumer
 ↓
Payment Service
 ↓
timeout
 ↓
Retry Topic
 ↓
delay
 ↓
Retry
```

So retry topics are one mechanism for implementing controlled backpressure around transient failures.

---

## 21. Retry topics and DLT together

A robust architecture:

```
                         orders
                           |
                           v
                     Order Consumer
                           |
                     Process Message
                           |
                 +---------+---------+
                 |                   |
              Success              Failure
                 |                   |
                 v                   v
              Commit             Classify
                                     |
                           +---------+---------+
                           |                   |
                      Transient             Permanent
                           |                   |
                           v                   v
                       Retry-1                DLT
                           |
                         delay
                           |
                           v
                       Retry-2
                           |
                         delay
                           |
                           v
                       Retry-3
                           |
                           v
                         DLT
```

---

## 22. Real-world payment example

Suppose:

```
Topic:
payment-events
```

Event:

```json
{
  "paymentId": "P1001",
  "amount": 5000,
  "currency": "INR"
}
```

Consumer calls:

```
Payment Gateway
```

### Attempt 1

```
Gateway → HTTP 503
```

Route to:

```
payment-events.retry.1
```

Wait:

```
5 seconds
```

### Attempt 2

```
Gateway → HTTP 503
```

Route to:

```
payment-events.retry.2
```

Wait:

```
30 seconds
```

### Attempt 3

```
Gateway → SUCCESS
```

Then:

```
Commit
```

No DLT required.

---

## 23. Permanent failure example

Suppose instead:

```
currency = INVALID
```

Consumer determines:

```
ValidationException
```

No reason to retry 5 times.

So:

```
payment-events
       |
       v
Consumer
       |
ValidationException
       |
       v
payment-events.DLT
```

This is much more efficient.

---

## 24. Retry topic implementation approaches

There are several ways to implement this.

### Approach A — Framework-managed retry topics

For example, with Spring Kafka, retry-topic support can manage:

```
main topic
    ↓
retry topic
    ↓
delayed retry
    ↓
DLT
```

This is convenient for Spring-based applications.

### Approach B — Application-managed retry topics

Your application explicitly publishes:

```
orders.retry.1
orders.retry.2
orders.retry.3
```

and consumers route messages according to retry metadata.

This gives more control but adds implementation complexity.

### Approach C — Delay/queue infrastructure outside Kafka

Sometimes an architecture uses:

```
Kafka
 ↓
Delay mechanism
 ↓
Kafka
```

or another messaging system designed around delayed delivery.

The correct approach depends on operational requirements.

---

## 25. Important Kafka limitation

A common interview mistake is saying:

> "Kafka supports delayed messages."

That's too simplistic.

Kafka's core log is not a general-purpose delayed-message queue.

Kafka provides:

```
ordered append-only logs
```

and retry-topic architectures/frameworks build delayed retry behavior on top of that.

That's why you see designs such as:

```
main topic
     ↓
retry-5s
     ↓
retry-30s
     ↓
retry-5m
     ↓
DLT
```

---

## 26. What happens if the application crashes during retry?

Because retry state is stored in Kafka:

```
Main Topic
    ↓
Retry Topic
```

the retry message remains durable according to Kafka's storage/retention configuration.

If the consumer crashes:

```
Consumer
   ↓
Crash
   ↓
Restart
   ↓
Read retry topic
```

The retry work isn't merely lost from application memory.

This is one of the major advantages of topic-based retry.

---

## 27. Important failure scenario: retry publish fails

Suppose:

```
Original message
       ↓
processing fails
       ↓
publish to retry topic
       ↓
Kafka unavailable ❌
```

What happens?

You must **not** blindly commit the original offset.

Otherwise:

```
Retry publish → FAILED
Offset commit → SUCCESS
```

The original record could become skipped.

A safer conceptual sequence is:

```
Process
   ↓
Failure
   ↓
Publish retry record
   ↓
Confirm success
   ↓
Commit original offset
```

And even this must be designed carefully for duplicates.

---

## 28. Retry publish + offset commit atomicity

For Kafka-to-Kafka retry flows, Kafka transactions can be useful.

Conceptually:

```
Consume original
       |
       v
Begin Kafka transaction
       |
       +---- Produce retry record
       |
       +---- Commit consumed offset
       |
       v
Commit transaction
```

Now:

```
retry record
     +
source offset
```

can be committed atomically within the Kafka transactional scope.

This is a powerful senior-level design.

---

## 29. Retry topic and exactly-once

Be careful with terminology.

> A retry topic does not automatically mean exactly-once.

You may still see:

```
Original
   ↓
Retry
   ↓
Retry
```

duplicates due to failures.

Therefore:

```
Retry Topics
    +
Idempotent Consumer
```

is still important.

For Kafka-to-Kafka processing, transactions can provide stronger guarantees within the transactional scope.

---

## 30. Retry topics and ordering

This deserves special emphasis.

Normal Kafka:

```
P0:
A
B
C
D
```

has:

```
A < B < C < D
```

processing order within that partition.

But if:

```
B → Retry Topic
```

you could get:

```
Main:
A → C → D

Retry:
B
```

So processing may become:

```
A → C → D → B
```

depending on the retry design.

Therefore:

> Retry topics are not automatically ordering-preserving.

If ordering is critical, explicitly design the retry mechanism around that requirement.

---

## 31. Senior production design

For a high-volume order system:

```
                     Kafka
                       |
                 orders topic
                       |
                       v
                Order Consumer
                       |
                  Processing
                       |
              +--------+--------+
              |                 |
           Success            Failure
              |                 |
              v                 v
           Commit          Error classifier
                                |
                     +----------+----------+
                     |                     |
                  Transient             Permanent
                     |                     |
                     v                     v
               retry-1 topic              DLT
                     |
                  delay
                     |
                     v
               retry-2 topic
                     |
                  delay
                     |
                     v
               retry-3 topic
                     |
                     v
                    DLT
```

With:

```
Retry policy
+
Exponential backoff
+
Jitter
+
Bounded attempts
+
Idempotent processing
+
DLT
+
Monitoring
+
Controlled replay
```

---

## 32. What should you monitor?

For retry topics, monitor:

- Retry topic message count
- Retry topic lag
- Retry attempt distribution
- Oldest retry message age
- DLT message count
- DLT growth rate
- Retry success rate
- Retry failure rate
- Exception types
- Downstream latency
- Downstream error rate

For example:

```
orders.retry.1
    ↓
10,000 messages

orders.retry.2
    ↓
50,000 messages

orders.retry.3
    ↓
100,000 messages 🔥
```

This tells you that the downstream dependency may be unhealthy.

---

## 33. Common mistakes

### ❌ Retry everything

```
Invalid JSON → retry 10 times
```

Wasteful.

Use:

```
Permanent → DLT
```

### ❌ Retry immediately

```
Failure
 ↓
Retry
 ↓
Failure
 ↓
Retry
```

Can create retry storms.

Use backoff.

### ❌ Retry forever

Always define:

```
maximum attempts
```

Then:

```
DLT
```

### ❌ Ignore ordering

Retry topics can reorder processing.

Always ask:

> Does this business flow require ordering?

### ❌ Unlimited concurrency during retry

When downstream recovers, millions of retry records might become eligible.

If you release them all at once:

```
Retry Topic
     |
     v
1M messages
     |
     v
Downstream 🔥
```

Use bounded concurrency/rate limiting.

### ❌ Blind replay from DLT

Before replaying:

```
Fix root cause
+
validate downstream capacity
+
consider duplicates
+
consider ordering
```

---

## 34. Retry Topic vs Backpressure vs DLT

These three concepts fit together:

```
                 Failure
                    |
                    v
             Is it temporary?
              /           \
            YES            NO
             |              |
             v              v
       Retry Topic          DLT
             |
           delay
             |
             v
          Retry
             |
        +----+----+
        |         |
     Success    Failure
        |         |
        v         v
      Commit    Retry
                   |
              max attempts
                   |
                   v
                  DLT
```

And backpressure controls the rate at which all this happens:

```
Backpressure
     |
     +-- bounded concurrency
     +-- rate limiting
     +-- pause/resume
     +-- retry backoff
     +-- Kafka buffering
```

---

## 35. Interview questions

### Q1. Why use retry topics instead of immediate retries?

> To avoid blocking consumers and creating retry storms. Failed messages can be moved to a durable Kafka topic and retried later with controlled delays.

### Q2. What is the difference between retry topic and DLT?

> Retry topics are for temporary failures where processing may succeed later. DLT is for messages that have exhausted retries or have permanent/non-retryable errors.

### Q3. Can retry topics break ordering?

> Yes. Moving a failed message to a separate retry topic can allow later messages from the original partition to be processed first. If strict ordering is required, the retry architecture must explicitly preserve that ordering.

### Q4. How do you implement exponential backoff?

> Use multiple retry levels or a delay mechanism, for example 1s, 5s, 30s, and 5m, with a maximum retry count. Adding jitter prevents synchronized retry spikes.

### Q5. What happens if publishing to the retry topic succeeds but offset commit fails?

> The original message may be consumed again and another retry record may be produced. Therefore the flow should tolerate duplicates; Kafka transactions can be used for atomic Kafka-to-Kafka retry publication plus offset commit.

### Q6. Should all exceptions go to retry topics?

**No.** Classify exceptions. Transient infrastructure failures should generally be retried, while permanent validation/schema/business errors should usually go directly to the DLT.

### Q7. What happens if millions of retry messages become available simultaneously?

> They can create a retry storm against the downstream service. Use bounded concurrency, rate limiting, backoff, jitter, and possibly staged retry topics.

---

## 36. Final mental model

Remember:

```
              MAIN TOPIC
                   |
                   v
                Consumer
                   |
             Process Event
                   |
          +--------+--------+
          |                 |
       SUCCESS            FAILURE
          |                 |
          v                 v
       Commit          Error Classification
                            |
                 +----------+----------+
                 |                     |
              Temporary             Permanent
                 |                     |
                 v                     v
             Retry Topic              DLT
                 |
               Delay
                 |
                 v
              Retry
                 |
           +-----+-----+
           |           |
        Success      Failure
           |           |
           v           v
        Commit      Next Retry
                         |
                    Max Attempts
                         |
                         v
                        DLT
```

---

## Golden rule

> **Retry topics are a durable, controlled way to delay and retry transiently failed Kafka messages. A good retry architecture combines bounded retries, exponential backoff, jitter, error classification, idempotency, ordering awareness, and a DLT for messages that ultimately cannot be processed.**

