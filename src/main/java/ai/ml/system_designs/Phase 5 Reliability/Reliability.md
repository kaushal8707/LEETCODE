# Reliability in System Design

Since you're going through **Fault Tolerance → High Availability → Reliability**, it's important to understand that these are related but different concepts.

---

## 1. What is Reliability?

Reliability is the ability of a system to **consistently perform its intended function correctly** over a specified period of time, under specified conditions.

**In simple words:**

> Reliability = Does the system keep doing the right thing correctly and consistently?

High Availability mainly asks:

> "Is the system available?"

Reliability asks:

> "Is the system working correctly and consistently?"

---

## 2. Simple Example

Imagine a payment system.

```
User
  |
  v
Payment Service
  |
  v
Bank
```

Suppose the Payment Service is available 99.99% of the time.

But occasionally it charges the customer twice:

```
Customer pays ₹1,000

Payment 1 → ₹1,000 deducted
Payment retry → ₹1,000 deducted again
```

> The system was available, but it wasn't reliable.

So:

```
Availability
    ↓
System is reachable

Reliability
    ↓
System behaves correctly
```

---

## 3. Reliability vs Availability

This distinction is extremely important for interviews.

| Concept | Question |
|---|---|
| Availability | Is the system accessible? |
| Reliability | Does the system work correctly and consistently? |
| Fault Tolerance | Can the system continue despite failures? |
| Resilience | Can the system recover/adapt from failures? |
| Durability | Will data survive failures? |

### Example

Suppose an API returns:

```
HTTP 200
```

but returns incorrect data.

```
Availability → ✅
Reliability → ❌
```

Suppose the API is temporarily unavailable:

```
Availability → ❌
Reliability → potentially ❌
```

---

## 4. Reliability in Real-World Systems

Consider an e-commerce system:

```
                  User
                    |
                    v
              Load Balancer
                    |
          +---------+---------+
          |         |         |
        App 1     App 2     App 3
          |         |         |
          +---------+---------+
                    |
                  Kafka
                    |
          +---------+---------+
          |                   |
       Payment             Order DB
```

Reliability means ensuring:

- Orders aren't lost
- Payments aren't duplicated
- Messages aren't lost
- Data isn't corrupted
- Requests aren't incorrectly processed
- Failed operations can recover
- Retries don't create duplicate operations
- System behavior remains predictable

---

## 5. Reliability Has Multiple Dimensions

Reliability isn't just "server doesn't crash."

Think about:

```
                    RELIABILITY
                         |
        +----------------+----------------+
        |                |                |
      Compute           Data           Communication
        |                |                |
     Servers           Database          Kafka
     Services          Storage           APIs
        |                |                |
        +----------------+----------------+
                         |
                    Correctness
                         |
                    Recovery
```

---

## 6. Reliability and Failure

In distributed systems:

> **Failures are inevitable.**

Examples:

- Server crashes
- Database fails
- Network packet is lost
- Kafka broker fails
- Request times out
- Dependency becomes slow
- Disk fails
- Availability Zone fails
- Region fails

A reliable system expects these failures.

For example:

```
Request
   |
   v
Payment Service
   |
   X
Network failure
   |
   v
Retry
   |
   v
Payment Service
   |
   v
Success
```

But retries alone aren't enough.

---

## 7. Reliability Through Redundancy

**Instead of:**

```
Application
     |
     v
Single Server
```

**use:**

```
          Load Balancer
          /     |     \
         /      |      \
      App 1   App 2   App 3
```

If one fails:

```
App 1 ❌

App 2 ✅
App 3 ✅
```

The system continues.

> Redundancy improves both availability and reliability.

---

## 8. Reliability Through Replication

For data:

```
              Primary DB
              /        \
             /          \
        Replica 1     Replica 2
```

If the primary fails:

```
Primary ❌
   |
   v
Replica 1
```

A replica can be promoted.

But reliability requires more than just having replicas.

We also need to think about:

- Replication lag
- Data consistency
- Split brain
- Failover correctness
- Data loss
- Recovery

---

## 9. Reliability Through Idempotency

This is extremely important in distributed systems.

Suppose:

```
Client
  |
  v
Payment Service
```

Client sends:

```
Pay ₹1,000
```

Payment succeeds.

But the response is lost:

```
Payment → Success
     X
Network response lost
```

Client doesn't know whether payment succeeded.

So it retries:

```
Pay ₹1,000
```

**Without idempotency:**

```
₹1,000 deducted
₹1,000 deducted again

Total = ₹2,000 ❌
```

**With an idempotency key:**

```
Idempotency-Key: PAYMENT-12345
```

The service recognizes the duplicate request:

```
First request
   ↓
PAYMENT-12345
   ↓
Process payment ✅

Retry
   ↓
PAYMENT-12345
   ↓
Already processed
   ↓
Return previous result
```

This dramatically improves reliability.

---

## 10. Reliability and Kafka

This connects directly with what you've been studying.

Suppose:

```
Order Service
     |
     v
Kafka
     |
     v
Payment Consumer
```

What happens if the consumer crashes after processing but before committing its offset?

```
Read message
    ↓
Process payment ✅
    ↓
Consumer crashes ❌
    ↓
Offset not committed
```

Kafka may deliver the message again.

```
Message
   ↓
Payment Consumer
   ↓
Processed again
```

If payment isn't idempotent:

```
Customer charged twice ❌
```

**Therefore:**

> Kafka's delivery semantics + idempotent processing are critical to application reliability.

This is why topics such as:

- At-most-once
- At-least-once
- Exactly-once
- Idempotent producer
- Transactions
- Consumer offsets

matter so much.

---

## 11. Reliability Through Transactions

Suppose Order Service performs:

```
1. Save order
2. Reduce inventory
3. Create payment
```

If step 1 succeeds:

```
Order saved ✅
```

but step 2 fails:

```
Inventory update ❌
```

you can end up with inconsistent state.

Reliable systems need mechanisms such as:

- Database transactions
- Saga pattern
- Transactional Outbox
- Idempotency
- Compensation

For example:

```
Order
  |
  v
Inventory
  |
  v
Payment
```

If Payment fails:

```
Payment ❌
   |
   v
Compensating action
   |
   v
Restore inventory
   |
   v
Cancel order
```

---

## 12. Reliability Through Timeouts

Suppose:

```
Order Service
     |
     v
Payment Service
```

Payment Service becomes unresponsive.

**Without timeout:**

```
Request
  |
  v
Waiting...
  |
  v
Waiting...
  |
  v
Waiting...
```

Eventually application threads may be exhausted.

**With timeout:**

```
Request
  |
  v
Payment Service
  |
  X
5-second timeout
  |
  v
Failure handling
```

> Timeouts prevent one unhealthy dependency from consuming all resources.

---

## 13. Reliability Through Retries

Transient failures can be handled with retries.

```
Request
   |
   v
Service
   |
   X
Temporary failure
   |
   v
Retry
   |
   v
Success
```

But don't blindly retry.

Use:

```
Retry
 +
Exponential Backoff
 +
Jitter
 +
Maximum Retry Limit
```

Example:

```
Attempt 1 → failure
     ↓
  100 ms

Attempt 2 → failure
     ↓
  200 ms

Attempt 3 → failure
     ↓
  400 ms

Attempt 4 → success
```

---

## 14. Circuit Breaker

If the dependency is continuously failing:

```
Service A
    |
    v
Service B ❌
```

Don't keep sending requests forever.

Use:

```
Service A
    |
    v
Circuit Breaker
    |
    v
Service B
```

After repeated failures:

```
CLOSED
   |
   | failures
   v
OPEN
   |
   | wait
   v
HALF-OPEN
```

This prevents cascading failures.

---

## 15. Bulkhead

Suppose your application has 100 threads.

Payment requests consume all 100:

```
100 threads
    |
    v
Payment Service ❌
```

Now:

```
Order requests ❌
Inventory requests ❌
User requests ❌
```

Use resource isolation:

```
Application
|
+--- Payment → 30 threads
|
+--- Orders → 30 threads
|
+--- Inventory → 20 threads
|
+--- Users → 20 threads
```

Payment failure doesn't consume resources needed by other operations.

---

## 16. Reliability Through Queues

**Synchronous:**

```
Order Service
     |
     v
Email Service
```

If Email Service fails:

```
Order Service
     |
     v
Email Service ❌
```

**With Kafka:**

```
Order Service
     |
     v
Kafka
     |
     v
Email Service
```

If Email Service fails:

```
Order Service ✅
     |
     v
Kafka stores message
     |
     v
Email Service ❌
```

After recovery:

```
Kafka
  |
  v
Email Service
  |
  v
Process messages
```

This improves reliability by decoupling components.

---

## 17. Dead Letter Queue

Suppose a message repeatedly fails:

```
Message
   |
Consumer
   |
   X
Retry
   |
   X
Retry
   |
   X
Retry
```

Eventually:

```
             DLQ
              ↑
              |
          Failed Message
```

> The Dead Letter Queue prevents a poison message from continuously blocking processing.

---

## 18. Monitoring and Observability

A reliable system needs to detect problems quickly.

You need:

### Metrics

- CPU
- Memory
- Latency
- Throughput
- Error rate
- Consumer lag
- DB connections
- Queue depth

### Logs

```
ERROR Payment failed
ERROR Database connection timeout
```

### Traces

```
Request
  |
  +--> Order Service
        |
        +--> Inventory
        |
        +--> Payment
```

> Distributed tracing helps identify where failures occur.

---

## 19. Reliability Metrics

Some important metrics:

### Error Rate

```
Error Rate =
Failed Requests / Total Requests
```

Example:

```
100 failed
1,000,000 total

Error Rate = 0.01%
```

### Latency

How long does a request take?

```
P50 = 100 ms
P95 = 250 ms
P99 = 500 ms
```

### MTBF

**Mean Time Between Failures**

> How frequently does the system fail?

### MTTR

**Mean Time To Recovery**

> How quickly can the system recover?

Lower MTTR generally improves availability and operational reliability.

---

## 20. Reliability vs Durability

These are often confused.

### Reliability

System performs correctly.

### Durability

Once data is successfully stored, it remains stored despite failures.

Example:

```
User saves order
       |
       v
Database
       |
       v
Server crashes
       |
       v
Order still exists
```

That's durability.

For example, databases and distributed storage systems use replication, WALs, backups, and durable storage to protect data.

---

## 21. Reliability vs Resilience

Another important interview distinction.

### Reliability

System continues performing correctly over time.

### Resilience

System can withstand failures, recover, and return to normal operation.

Think:

```
Reliability
     |
     v
Correct operation
     |
     +
     |
Resilience
     |
     v
Failure
     |
     v
Recovery
     |
     v
Normal operation
```

---

## 22. Reliability Architecture

A production-grade system might look like:

```
                         Users
                           |
                           v
                    Global Load Balancer
                      /             \
                     /               \
                Region A           Region B
                   |                  |
                 Load               Load
                Balancer           Balancer
                 /  \               /  \
               App  App           App  App
                 \  /               \  /
                  Cache              Cache
                     \               /
                      +------ Kafka--+
                              |
                         Consumers
                              |
                       Database Cluster
                       /             \
                  Primary          Replica
```

### Reliability mechanisms:

```
Load Balancing
      ↓
Redundancy
      ↓
Replication
      ↓
Timeouts
      ↓
Retries + Backoff
      ↓
Circuit Breakers
      ↓
Bulkheads
      ↓
Idempotency
      ↓
Transactions
      ↓
Queues
      ↓
DLQ
      ↓
Monitoring
      ↓
Automatic Failover
      ↓
Backup / Disaster Recovery
```

---

## 23. Reliability in a Payment System

This is a great interview example.

Suppose:

```
Client
   |
   v
Order Service
   |
   v
Payment Service
   |
   v
Bank
```

A reliable design might use:

```
Client
   |
   v
Order Service
   |
   +---- Idempotency Key
   |
   v
Payment Service
   |
   +---- Timeout
   |
   +---- Retry + Backoff
   |
   +---- Circuit Breaker
   |
   v
Bank
```

And store:

```
payment_id
idempotency_key
status
amount
transaction_reference
```

If the client retries:

```
Same idempotency_key
       |
       v
Already processed?
       |
    +--+--+
   Yes    No
    |      |
Return    Process
result
```

This prevents duplicate payments.

---

## 24. Reliability in Kafka

For Kafka-based systems:

```
Producer
   |
   v
Kafka
   |
   v
Consumer
```

Reliability depends on multiple things:

```
Producer
  |
  +-- acks=all
  +-- idempotence
  +-- retries
  |
  v
Kafka
  |
  +-- Replication Factor
  +-- ISR
  +-- Leader/Follower
  |
  v
Consumer
  |
  +-- Offset Management
  +-- Idempotent Processing
  +-- Retry Topics
  +-- DLQ
```

This is why Kafka reliability isn't simply:

> "Kafka has replication."

You need to consider the entire end-to-end processing pipeline.

---

## 25. Reliability vs High Availability vs Fault Tolerance

Keep this table in your interview notes:

| Concept | Meaning | Example |
|---|---|---|
| Reliability | Performs correctly and consistently | Payment isn't duplicated |
| Availability | System is accessible | API continues responding |
| Fault Tolerance | Continues despite component failure | App survives server crash |
| Resilience | Recovers from failures | Service recovers after dependency outage |
| Durability | Data survives failures | Order isn't lost after DB crash |

A useful mental model:

```
                 SYSTEM QUALITY
                       |
        +--------------+--------------+
        |              |              |
   Availability    Reliability    Durability
        |              |              |
    "Is it up?"    "Is it right?"  "Is data safe?"
        |
        +--------------+
                       |
                Fault Tolerance
                       |
                   Resilience
                       |
                    Recovery
```

---

## 26. Interview Answer

If an interviewer asks:

> "What is Reliability?"

A strong 10-year-experience answer would be:

> Reliability is the ability of a system to consistently perform its intended functions correctly over time, including under failure conditions. In distributed systems, reliability is achieved through redundancy, replication, idempotency, transactional processing, retries with backoff, timeouts, circuit breakers, bulkheads, durable messaging, monitoring, and recovery mechanisms. High availability focuses primarily on minimizing downtime, while reliability also focuses on correctness, consistency, and preventing data loss or incorrect operations.

---

## 27. The Most Important Mental Model

For System Design interviews, think about reliability in this order:

```
                  RELIABLE SYSTEM
                        |
                        v
                Prevent mistakes
                        |
                 Idempotency
                 Transactions
                 Validation
                        |
                        v
                Handle failures
                        |
              Timeout / Retry
              Circuit Breaker
              Bulkhead
                        |
                        v
                Protect data
                        |
              Replication
              Transactions
              Backups
                        |
                        v
                Protect messages
                        |
                Kafka / Queue
                Retry / DLQ
                        |
                        v
                Recover quickly
                        |
                 Failover
                 Automation
                 Monitoring
```

### One-line memory trick

```
Availability     = "Is it up?"
Reliability      = "Does it work correctly?"
Fault Tolerance  = "Can it survive failure?"
Resilience       = "Can it recover?"
Durability       = "Will the data survive?"
```

