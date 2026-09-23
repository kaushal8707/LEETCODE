# Kafka Dead-Letter Topics (DLT / DLQ)

A **Dead-Letter Topic (DLT)** is a Kafka topic where a consumer sends a message that cannot be successfully processed after the configured retry attempts or because the error is considered non-retryable.

The simplest mental model:

```
Kafka Main Topic
      |
      v
   Consumer
      |
      v
   Processing
   /        \
Success     Failure
  |           |
  v           v
Commit      Retry
              |
          still fails?
              |
              v
       Dead-Letter Topic
```

> DLT prevents one bad message from continuously blocking or destabilizing normal processing.

---

## 1. Why do we need a Dead-Letter Topic?

Imagine an `orders` topic:

```
OrderCreated
OrderCreated
OrderCreated
OrderCreated
```

Consumer processes them:

```
Order 101 → SUCCESS
Order 102 → SUCCESS
Order 103 → SUCCESS
Order 104 → INVALID JSON ❌
Order 105 → SUCCESS
```

Suppose Order 104 keeps failing.

If your consumer continuously retries:

```
104 → fail
104 → retry
104 → fail
104 → retry
104 → fail
...
```

You can create a **poison-pill** problem.

A DLT gives you:

```
Main Topic
    |
    +-- 101 → success
    +-- 102 → success
    +-- 103 → success
    |
    +-- 104 → failure
             |
             v
        orders.DLT
             |
             +-- 104
```

The main processing pipeline can continue while the failed message is isolated for investigation/reprocessing.

---

## 2. What is a poison-pill message?

A **poison pill** is a message that repeatedly causes the consumer to fail.

Examples:

### Invalid payload

```json
{
  "orderId": 101,
  "amount": "INVALID"
}
```

Application expects:

```
amount = number
```

### Missing required field

```json
{
  "customerId": "C101"
}
```

but application requires:

```
orderId
customerId
amount
```

### Business validation failure

```
amount = -500
```

### Downstream incompatibility

Consumer calls:

```
Payment Service
```

and the payload violates the downstream API contract.

---

## 3. DLT is not the same as retry

This distinction is very important.

### Retry

Means:

> "The failure might be temporary. Try the same message again."

### DLT

Means:

> "Normal processing has failed enough times, or the error is not worth retrying automatically. Move the message aside for later handling."

So:

```
Failure
   |
   v
Retry
   |
   +---- success → commit
   |
   +---- failure
           |
           v
        Retry again
           |
           v
      Max attempts?
           |
           v
          DLT
```

---

## 4. Retryable vs non-retryable errors

Not every exception should be sent immediately to a DLT.

### Retryable errors

These are usually temporary:

- Database temporarily unavailable
- HTTP 503
- Network timeout
- Kafka broker temporarily unavailable
- Rate limiting
- Temporary connection failure

Example:

```
Consumer
   |
   v
Payment API
   |
   v
503 Service Unavailable
   |
   v
Retry
```

### Non-retryable errors

These usually won't become successful by simply trying again:

- Invalid JSON
- Missing required field
- Invalid enum
- Schema violation
- Invalid business data
- Unsupported event type

Example:

```
Invalid JSON
    |
    v
Retry ❌
    |
    v
Retry ❌
    |
    v
DLT
```

This distinction is critical for designing a good Kafka consumer.

---

## 5. Typical architecture

A production architecture might look like:

```
                 +----------------+
                 | orders topic   |
                 +-------+--------+
                         |
                         v
                 +---------------+
                 | Order Consumer |
                 +-------+-------+
                         |
                 +-------+-------+
                 |               |
              Success          Failure
                 |               |
                 v               v
              Commit           Retry
                                 |
                         +-------+-------+
                         |               |
                      Success          Failure
                         |               |
                         v               v
                      Commit           DLT
                                         |
                                         v
                              +-------------------+
                              | orders.DLT        |
                              +---------+---------+
                                        |
                           +------------+------------+
                           |                         |
                        Investigate               Replay
```

---

## 6. What happens to the Kafka offset?

This is where DLT design becomes interesting.

Suppose:

```
Partition 0

100 → success
101 → success
102 → failed
103 → success
```

Kafka consumers process partitions in offset order.

If offset 102 fails and you simply don't commit it:

```
Consumer offset = 102
```

the consumer can repeatedly encounter:

```
102 → failure
102 → retry
102 → failure
```

Potentially blocking progress on that partition.

A common strategy is:

```
102 fails permanently
      |
      v
Publish 102 to DLT
      |
      v
Commit original offset 102
      |
      v
Continue with 103
```

Conceptually:

```
Main Topic:

100 → processed
101 → processed
102 → DLT
103 → processed
104 → processed
```

This is one of the major purposes of DLT.

---

## 7. Very important: DLT does not magically solve ordering

Suppose:

```
Partition 0

101 → OrderCreated
102 → PaymentCompleted ❌
103 → OrderShipped

```

If you send 102 to DLT and commit past it:

```
101 → processed
102 → DLT
103 → processed
```

you have effectively allowed later processing despite a failed event.

That may or may not be acceptable.

For some workflows:

```
OrderCreated
    ↓
PaymentCompleted
    ↓
OrderShipped
```

processing `OrderShipped` without successfully handling `PaymentCompleted` could be incorrect.

Therefore:

> DLT strategy must be designed together with ordering and business dependencies.

---

## 8. DLT and at-least-once processing

You previously learned at-least-once delivery.

Consider:

```
Consume message
     ↓
Process
     ↓
Fails
     ↓
Retry
```

If the message eventually succeeds:

```
Process
   ↓
Commit offset
```

If it permanently fails:

```
Process
   ↓
Retry
   ↓
Retry
   ↓
DLT
   ↓
Commit original offset
```

This allows the main consumer to continue.

But there is an important failure scenario:

```
Process DLT publish
        ↓
SUCCESS
        ↓
Commit original offset
        ↓
CRASH before commit
```

On restart:

```
Original message
       ↓
processed again
       ↓
DLT message may be published again
```

So DLT publishing itself can be subject to at-least-once behavior.

You should therefore design DLT consumers and replay workflows to tolerate duplicates.

---

## 9. DLT message should contain useful metadata

Don't just put the original payload into the DLT.

A useful DLT record should preserve information such as:

- Original topic
- Original partition
- Original offset
- Original key
- Original payload
- Exception/error type
- Error message
- Failure timestamp
- Retry count
- Consumer/application name
- Stack trace or diagnostic information

Conceptually:

```
DLT Record
+--------------------------------+
| originalTopic                  |
| originalPartition              |
| originalOffset                 |
| originalKey                    |
| originalPayload                |
| exceptionType                  |
| errorMessage                   |
| retryCount                     |
| failedAt                       |
| consumerName                   |
+--------------------------------+
```

This makes troubleshooting and replay much easier.

---

## 10. Example DLT record

Original:

```
Topic:
orders

Partition:
3

Offset:
10521
```

Failed because:

```
JsonMappingException
```

DLT metadata could conceptually contain:

```json
{
  "originalTopic": "orders",
  "originalPartition": 3,
  "originalOffset": 10521,
  "errorType": "JsonMappingException",
  "retryCount": 3,
  "failedAt": "2026-09-03T10:30:00Z",
  "originalPayload": "..."
}
```

This allows engineers to answer:

> "Exactly which Kafka record failed and why?"

---

## 11. DLT naming strategies

A common approach is:

```
orders
orders.DLT
```

or:

```
orders
orders-dlt
```

For multiple consumers:

```
orders
orders.payment-service.DLT
orders.inventory-service.DLT
```

The exact naming convention is a team/design choice.

The important point is that the DLT should identify:

```
source topic
+
processing context
```

when multiple consumers process the same topic.

---

## 12. One DLT or multiple DLTs?

### Option 1: One DLT

```
orders
   |
   v
orders.DLT
```

Simple.

### Option 2: Consumer-specific DLT

```
orders
   |
   +---- Payment Consumer
   |          |
   |          v
   |     orders.payment.DLT
   |
   +---- Inventory Consumer
              |
              v
         orders.inventory.DLT
```

This is often easier to operate when different consumers have different failure/replay requirements.

---

## 13. DLT should not become a garbage dump

A common anti-pattern:

```
Everything that fails
       ↓
DLT
       ↓
Forget about it
```

That's dangerous.

A DLT should be an **operational workflow**, not a permanent graveyard.

You need:

```
DLT
 ↓
Monitor
 ↓
Alert
 ↓
Investigate
 ↓
Fix problem
 ↓
Replay
```

---

## 14. DLT monitoring

Monitor:

- DLT message count
- DLT growth rate
- Oldest DLT message age
- Error types
- Retry counts
- Replay success rate

For example:

```
orders.DLT

Today:
10 messages → normal investigation

Suddenly:
50,000 messages/hour 🔥
```

This probably indicates:

- Schema deployment problem
- Consumer bug
- Downstream outage
- Bad producer deployment

DLT metrics can therefore be an important production signal.

---

## 15. Retry topic vs DLT

For temporary failures, you may not want to send the message directly to the DLT.

Instead:

```
orders
   |
   v
consumer
   |
   v
retry-1
   |
   v
retry-2
   |
   v
retry-3
   |
   v
orders.DLT
```

For example:

```
Immediate retry
    ↓
wait 1 second

Retry
    ↓
wait 10 seconds

Retry
    ↓
wait 1 minute

Still fails
    ↓
DLT
```

This is called a **retry topic** pattern.

---

## 16. Why delayed retries are useful

Suppose the downstream payment service is temporarily unavailable:

```
Payment API
    ↓
HTTP 503
```

Immediately retrying thousands of messages can make things worse:

```
Consumer
   ↓
Retry
   ↓
Payment API 🔥
   ↓
503
   ↓
Retry
   ↓
Payment API 🔥🔥
```

This can create a **retry storm**.

Instead:

```
Main Topic
    ↓
Retry Topic
    ↓
Wait
    ↓
Retry
```

This gives the downstream system time to recover.

---

## 17. Exponential backoff

A common retry strategy:

```
Attempt 1 → immediately
Attempt 2 → 1 sec
Attempt 3 → 5 sec
Attempt 4 → 30 sec
Attempt 5 → 2 min
        ↓
       DLT
```

The exact values depend on your system.

The idea is:

> Increase the delay between retries rather than hammering the failing dependency.

---

## 18. DLT vs Retry Topic

| Feature | Retry Topic | DLT |
|---|---|---|
| Purpose | Temporary failure | Permanent/unresolved failure |
| Message expected to succeed automatically | Usually yes | Usually no |
| Delay | Often yes | Usually no |
| Consumer processing | Retry later | Investigate/replay |
| Example | HTTP 503 | Invalid payload |
| Operational action | Wait/retry | Fix/reprocess |

---

## 19. Example: Payment processing

Suppose:

```
PaymentCreated
```

Consumer calls:

```
Payment Gateway
```

### Scenario A — Gateway timeout

```
PaymentCreated
     ↓
Gateway timeout
     ↓
Retry after 5 sec
     ↓
Success
```

Don't send to DLT immediately.

### Scenario B — Invalid payment currency

```
currency = XYZ
     ↓
Validation failure
     ↓
Retry won't fix it
     ↓
DLT
```

---

## 20. DLT and replay

One of the most valuable DLT capabilities is:

> **Fix the problem and replay the failed messages.**

Architecture:

```
                 orders.DLT
                     |
                     v
               Investigation
                     |
                 Fix consumer
                     |
                     v
                  Replay
                     |
                     v
                orders topic
                     |
                     v
                 Consumer
```

Example:

```
1000 DLT messages
       ↓
Bug fixed
       ↓
Replay
       ↓
orders
       ↓
Consumer
       ↓
Success
```

---

## 21. Replay strategies

### Strategy 1 — Replay to original topic

```
DLT
 ↓
Original topic
 ↓
Consumer
```

Simple, but be careful about:

- duplicates
- ordering
- current production traffic
- side effects.

### Strategy 2 — Replay to a separate topic

```
DLT
 ↓
orders.replay
 ↓
Replay Consumer
```

This provides more isolation.

### Strategy 3 — Fix and manually produce

For a few records:

```
DLT
 ↓
Inspect
 ↓
Fix payload
 ↓
Publish
```

Useful for small volumes.

---

## 22. DLT and idempotency

This connects to the delivery semantics you learned.

Suppose:

```
Original message
   ↓
processing succeeds
   ↓
DLT publish/offset handling has failure
   ↓
message processed again
```

You may get duplicates.

Therefore your consumer should ideally have:

```
Idempotent processing
```

For example:

```
eventId = 12345
```

Database:

```
processed_events

eventId
--------
12345
```

Before applying a business operation:

```
if eventId already exists
       ↓
     skip
else
       ↓
process
       ↓
store eventId
```

This is especially important for payments, orders, inventory, and financial operations.

---

## 23. DLT + database transaction

Suppose:

```
Kafka
  ↓
Consumer
  ↓
Database
```

Consumer receives:

```
PaymentCompleted
```

Database update succeeds:

```
DB → SUCCESS
```

Then offset commit fails:

```
Kafka commit → FAILURE
```

Message is processed again.

Therefore:

```
At-least-once
+
Idempotent DB operation
```

is often a practical strategy.

DLT doesn't eliminate the need for idempotency.

---

## 24. DLT and exactly-once semantics

You learned that Kafka EOS does not automatically make external side effects exactly-once.

Similarly:

> A DLT does not automatically provide exactly-once failure handling.

For Kafka-to-Kafka workflows, transactions can atomically coordinate:

```
output record
+
consumer offset
```

But if your workflow is:

```
Kafka
 ↓
Database
 ↓
DLT
```

you still need to think about the database transaction and DLT/offset consistency.

---

## 25. Critical failure scenario

Consider:

```
Main Topic
    ↓
Consumer
    ↓
Processing fails
    ↓
Publish to DLT
    ↓
SUCCESS
    ↓
Commit original offset
```

What if:

```
DLT publish SUCCESS
       ↓
Consumer crashes
       ↓
Before offset commit
```

Then after restart:

```
Original message
       ↓
again
       ↓
DLT publish again
```

Now:

```
DLT:
message 101
message 101
```

Potential duplicate.

This is why DLT consumers/replay mechanisms should be designed to tolerate duplicates.

---

## 26. DLT and ordering — advanced issue

Suppose one partition contains:

```
P0:

100 → OrderCreated
101 → PaymentCompleted ❌
102 → OrderShipped
103 → OrderDelivered
```

If you send 101 to DLT and commit through it:

```
100 → processed
101 → DLT
102 → processed
103 → processed
```

you may have violated the business sequence.

Therefore some applications may choose:

```
101 fails
   ↓
Pause partition
   ↓
Retry 101
   ↓
DLT only after business decision
```

or use a retry mechanism that preserves ordering.

This is why DLT design cannot be separated from partition ordering requirements.

---

## 27. DLT design for senior system design

A robust architecture often looks like:

```
                         Kafka
                           |
                    orders topic
                           |
                           v
                  +----------------+
                  | Order Consumer |
                  +-------+--------+
                          |
                     Processing
                          |
              +-----------+-----------+
              |                       |
           Success                  Failure
              |                       |
              v                       v
          Commit Offset           Error Classify
                                      |
                          +-----------+-----------+
                          |                       |
                     Retryable              Non-retryable
                          |                       |
                          v                       v
                    Retry Topic                DLT
                          |
                       delay
                          |
                          v
                    Consumer Retry
                          |
                     +----+----+
                     |         |
                  Success    Failure
                     |         |
                     v         v
                  Commit      DLT
```

---

## 28. Error classification

A good consumer should classify failures.

```
Exception
    |
    +-----------------------+
    |                       |
Transient                Permanent
    |                       |
    v                       v
Retry                    DLT
```

Examples:

### Transient

- Timeout
- 503
- Connection refused
- Rate limit

### Permanent

- Malformed payload
- Missing required field
- Invalid business rule
- Unsupported schema

---

## 29. DLT partitioning

A DLT is itself a Kafka topic.

Therefore it also has:

- Partitions
- Keys
- Offsets
- Replication
- Retention

You should think about how to partition the DLT as well.

A common approach is to preserve the original key:

```
Original:
key = orderId

DLT:
key = orderId
```

This can make replay and ordering behavior easier to reason about.

---

## 30. DLT retention

Don't assume DLT data should live forever.

You can configure retention:

```
orders.DLT
    |
    +-- retention = 30 days
```

The appropriate value depends on:

- Investigation SLA
- Replay requirements
- Compliance
- Storage
- Business requirements

For some systems:

```
DLT retention = 7 days
```

For others:

```
DLT retention = 90 days
```

There is no universal value.

---

## 31. DLT vs traditional DLQ

You'll often hear:

```
DLQ = Dead Letter Queue
DLT = Dead Letter Topic
```

The concept is similar.

In traditional messaging:

```
Queue
  ↓
Failed message
  ↓
DLQ
```

In Kafka:

```
Topic
  ↓
Failed message
  ↓
DLT
```

Kafka typically uses a topic, so "Dead-Letter Topic" is the more Kafka-specific term.

---

## 32. Common mistakes

### ❌ Send every exception directly to DLT

This can hide temporary outages.

Better:

```
Transient → retry
Permanent → DLT
```

### ❌ Retry forever

```
retry
retry
retry
retry
...
```

This can block partitions and create retry storms.

Use:

```
max attempts
+
backoff
+
DLT
```

### ❌ Ignore DLT

A DLT without monitoring becomes a graveyard.

Use:

```
DLT monitoring
+
alerts
+
replay process
```

### ❌ Replay blindly

If you replay:

```
10 million messages
```

into production without considering:

- downstream capacity
- duplicates
- ordering

you can create another outage.

### ❌ Assume DLT means exactly once

It doesn't.

DLT handling can itself have failures and duplicates.

---

## 33. DLT vs Kafka retention

These solve different problems.

```
Kafka retention
      |
      ↓
How long should Kafka retain normal data?


DLT
      |
      ↓
Where should permanently failed messages go?
```

A DLT itself also has retention.

So:

```
Main Topic
    ↓
normal retention

DLT
    ↓
DLT retention
```

---

## 34. DLT vs Log Compaction

Also don't confuse these.

### DLT

```
Failed messages
      ↓
Separate topic
```

### Compaction

```
Same key
      ↓
Keep latest value
```

They can technically coexist as Kafka topic configurations, but they solve entirely different problems.

---

## 35. Production checklist

For a production Kafka consumer, consider:

- ✅ Retry policy
- ✅ Retryable exception classification
- ✅ Maximum retry attempts
- ✅ Backoff strategy
- ✅ DLT
- ✅ DLT metadata
- ✅ DLT monitoring
- ✅ DLT retention
- ✅ Replay mechanism
- ✅ Idempotent processing
- ✅ Ordering requirements
- ✅ Downstream capacity
- ✅ Alerting

---

## 36. Interview question: "Why do we need a DLT?"

Strong answer:

> A Dead-Letter Topic isolates messages that cannot be successfully processed after retries or are considered non-retryable. It prevents poison messages from repeatedly blocking normal processing, preserves the failed payload and diagnostic metadata for investigation, and provides a controlled mechanism for later replay after the underlying problem is fixed.

---

## 37. Interview question: "Should every failed message go to DLT?"

Answer:

> No. We should distinguish transient and permanent failures. Transient failures such as timeouts or temporary 503 responses should generally be retried with backoff. Permanent failures such as malformed payloads or validation errors can be routed to the DLT.

---

## 38. Interview question: "What happens to the original Kafka offset when a message goes to DLT?"

A strong answer:

> Once the application has successfully published the failed record to the DLT, it can commit the original offset so that the consumer can move forward. However, the publish-to-DLT and offset-commit operations need careful failure handling because they are not automatically atomic unless they're included in an appropriate Kafka transaction.

That's an important senior-level nuance.

---

## 39. Interview question: "What if DLT publishing succeeds but offset commit fails?"

Answer:

```
DLT publish
    ↓
SUCCESS
    ↓
offset commit
    ↓
FAILURE
```

The original record may be processed again:

```
Original
   ↓
DLT
   ↓
retry processing
   ↓
DLT again
```

Potential duplicate in DLT.

Therefore:

> DLT processing should generally be designed with at-least-once behavior and idempotency in mind.

---

## 40. Interview question: "How would you design retries and DLT?"

A strong senior answer:

```
                    Main Topic
                         |
                         v
                     Consumer
                         |
                  Classify error
                    /         \
             Transient       Permanent
                |                |
                v                v
          Retry + Backoff       DLT
                |
          max attempts
                |
          +-----+-----+
          |           |
       Success      Failure
          |           |
          v           v
       Commit        DLT
```

Then mention:

> "I would preserve original topic/partition/offset and failure metadata in the DLT, monitor DLT depth and age, provide a controlled replay mechanism, and make the consumer idempotent because DLT publication and offset management can still result in duplicates."

---

## 41. Final mental model

You can remember Kafka error handling as:

```
                     Kafka Topic
                         |
                         v
                      Consumer
                         |
                   Process message
                         |
                +--------+--------+
                |                 |
             SUCCESS            FAILURE
                |                 |
                v                 v
          Commit offset      Classify error
                                  |
                     +------------+------------+
                     |                         |
                  Retryable                Permanent
                     |                         |
                     v                         v
                Retry Topic                  DLT
                     |
                  Backoff
                     |
                     v
                 Consumer
                     |
                +----+----+
                |         |
             Success    Failure
                |         |
                v         v
             Commit      DLT
```

---

## The senior-level principle

> **Retries are for failures that may recover; DLTs are for failures that should be isolated. The design must also account for offset commits, ordering, duplicates, idempotency, replay, and downstream capacity.**

And connect this with what you've already learned:

```
At-least-once
      +
Retries
      +
Idempotent Consumer
      +
Dead-Letter Topic
      +
Replay
      ↓
Robust Kafka Consumer
```

