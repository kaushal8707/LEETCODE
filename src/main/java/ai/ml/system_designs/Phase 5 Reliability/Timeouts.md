# Timeouts in System Design

**Timeout** is the maximum amount of time a system is willing to wait for an operation to complete.

**In simple words:**

> Timeout = "Don't wait forever."

Timeouts are one of the most important mechanisms for building reliable, highly available distributed systems.

---

## 1. Why Do We Need Timeouts?

Consider:

```
Client
  |
  v
Order Service
  |
  v
Payment Service
```

Order Service calls Payment Service.

**Normally:**

```
Order → Payment
          |
          v
       Response
          |
          v
        Order
```

But suppose Payment Service becomes slow:

```
Order
  |
  v
Payment
  |
  X
No response
```

**Without a timeout:**

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
   |
   v
Waiting...
```

Eventually, Order Service can consume all its threads/connections waiting for Payment.

Then:

```
Payment Service ❌
      ↓
Order Service becomes unhealthy
      ↓
More requests fail
      ↓
Entire system becomes unhealthy
```

> This can become a **cascading failure**.

---

## 2. Timeout Prevents This

With a 3-second timeout:

```
Order Service
     |
     | Request
     v
Payment Service
     |
     | No response
     |
     v
3 seconds
     |
     v
TIMEOUT
```

The Order Service stops waiting.

It can then:

- Retry
- Return an error
- Use fallback
- Put the operation into a queue
- Open a circuit breaker

For example:

```
Request
   |
   v
Payment Service
   |
   X
3 sec timeout
   |
   v
Retry / Fallback / Error
```

---

## 3. Timeout vs Failure

A timeout does **not** necessarily mean the downstream service is dead.

This is very important.

Suppose:

```
Order Service
      |
      | Request
      v
Payment Service
      |
      | Processing
      |
      | 10 seconds
      v
Response
```

But Order Service has:

```
Timeout = 3 seconds
```

Then:

```
3 seconds → timeout
```

even though Payment Service eventually succeeds.

**So:**

> Timeout means "I stopped waiting," not necessarily "the other service failed."

---

## 4. Types of Timeouts

In real systems, there isn't just one timeout.

A request can involve:

```
Client
  |
  v
Load Balancer
  |
  v
Application
  |
  v
Connection Pool
  |
  v
Database
```

Each layer can have its own timeout.

**Important timeout types include:**

- Connection timeout
- Read timeout
- Write timeout
- Request/response timeout
- Database query timeout
- Connection pool timeout
- Load balancer timeout
- Kafka-related time limits

---

## 5. Connection Timeout

A **connection timeout** controls how long we wait to establish a connection.

Example:

```
Application
    |
    | Connect
    v
Database
```

If the database is unreachable:

```
Connect
  |
  v
Waiting...
  |
  v
Waiting...
```

With:

```
Connection timeout = 2 seconds
```

we stop trying after 2 seconds.

```
2 sec
 ↓
Connection timeout
 ↓
Failure handling
```

This prevents the application from waiting indefinitely.

---

## 6. Read Timeout

A connection may succeed, but the server may take too long to respond.

```
Application
    |
    | Connection established ✅
    |
    v
Payment Service
    |
    | Processing...
    | Processing...
    | Processing...
```

A **read timeout** determines how long the client waits to receive data.

Example:

```
Read timeout = 5 seconds
```

If no response arrives within that time:

```
5 sec
 ↓
Read timeout
```

---

## 7. Write Timeout

A **write timeout** controls how long the client waits while sending data.

For example:

```
Application
    |
    | Upload request
    v
Service
```

If the network becomes slow:

```
Sending...
Sending...
Sending...
```

A write timeout prevents indefinite waiting.

---

## 8. Request Timeout

A **request timeout** is the maximum amount of time allowed for the overall operation.

For example:

```
Client
  |
  v
API
  |
  +---- Database
  |
  +---- Redis
  |
  +---- Payment
```

Suppose:

```
Request timeout = 5 seconds
```

The complete request should finish within approximately 5 seconds.

If it doesn't:

```
Request
   |
   v
5 seconds
   |
   v
Timeout
```

---

## 9. Database Query Timeout

Suppose:

```
Application
     |
     v
Database
     |
     v
SELECT ...
```

The query accidentally takes:

```
60 seconds
```

while your application expects:

```
2 seconds
```

A **query timeout** can stop it:

```
Query
 |
 v
2 seconds
 |
 v
Query timeout
```

> This protects database and application resources.

---

## 10. Connection Pool Timeout

This is another important one.

Suppose your application has:

```
Connection pool = 20
```

And all 20 connections are busy:

```
Connection 1 → busy
Connection 2 → busy
...
Connection 20 → busy
```

A new request arrives:

```
Request
   |
   v
Connection Pool
   |
   X
No connection available
```

**Without a pool timeout:**

```
Waiting...
Waiting...
Waiting...
```

**With:**

```
Pool acquisition timeout = 1 second
```

the request fails quickly.

---

## 11. Timeout Propagation

This is a very important System Design interview topic.

Consider:

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

Suppose:

```
Client timeout = 10 sec
Order timeout = 8 sec
Payment timeout = 6 sec
Bank timeout = 5 sec
```

This gives the system a **timeout budget**.

```
Client
10 sec
 |
Order
8 sec
 |
Payment
6 sec
 |
Bank
5 sec
```

> The deeper dependency should generally have less time available than the caller.

Otherwise you can get this:

```
Client timeout = 5 sec
Order timeout = 10 sec
```

The client gives up at 5 seconds while Order continues processing until 10 seconds.

> That wastes resources.

---

## 12. Timeout Budget

Suppose an API has:

```
Total request budget = 2 seconds
```

The request needs:

```
Order Service
    |
    +---- Database → 500 ms
    |
    +---- Redis → 100 ms
    |
    +---- Payment → 800 ms
```

You need to allocate the budget carefully.

**Conceptually:**

```
             2 seconds
                 |
       +---------+---------+
       |         |         |
      DB       Redis     Payment
    500ms      100ms      800ms
```

Also reserve time for:

- Network latency
- Serialization
- Application processing
- Retries
- Queueing

---

## 13. Timeout + Retry

Timeouts and retries commonly work together.

Example:

```
Request
   |
   v
Payment
   |
   X
Timeout
   |
   v
Retry
   |
   v
Payment
   |
   v
Success
```

For example:

```
Attempt 1
   |
   +--- timeout after 500 ms
             |
             v
          wait 100 ms
             |
Attempt 2
   |
   +--- success
```

> But retries must be controlled.

---

## 14. The Dangerous Combination: Timeout + Retry

Imagine:

```
100 clients
```

All call a slow service.

Each request:

```
Timeout = 1 sec
Retry = 3
```

The service is overloaded.

Then every client does:

```
Request → timeout
Retry → timeout
Retry → timeout
```

Now the already-overloaded service receives even more traffic.

This is called a:

> **Retry storm**

It can make an outage much worse.

---

## 15. Timeout + Exponential Backoff

**Instead of immediately retrying:**

```
Attempt 1 → timeout
Attempt 2 → immediately
Attempt 3 → immediately
```

**use:**

```
Attempt 1 → timeout
      ↓
    wait 100ms

Attempt 2 → timeout
      ↓
    wait 200ms

Attempt 3 → timeout
      ↓
    wait 400ms
```

This is **exponential backoff**.

Usually add **jitter**:

```
Client A → wait 183ms
Client B → wait 241ms
Client C → wait 157ms
```

> This prevents clients from retrying simultaneously.

---

## 16. Timeout + Circuit Breaker

These two patterns work extremely well together.

```
Order Service
      |
      v
Circuit Breaker
      |
      v
Payment Service
```

Payment becomes slow:

```
Request
   |
   v
Payment
   |
   X
Timeout
```

Repeated timeouts occur:

```
Timeout
Timeout
Timeout
Timeout
```

Circuit breaker opens:

```
CLOSED
   |
   | failures
   v
OPEN
```

Now requests fail fast:

```
Order
  |
  v
Circuit OPEN
  |
  X
Payment
```

> This protects the system.

---

## 17. Timeout + Bulkhead

Suppose Payment Service is slow.

**Without isolation:**

```
100 application threads
       |
       v
Payment requests
       |
       X
Payment slow
```

All threads may become blocked.

**With a bulkhead:**

```
Application
|
+--- Payment → 20 threads
|
+--- Orders  → 30 threads
|
+--- Users   → 30 threads
|
+--- Other   → 20 threads
```

Payment can consume only its allocated resources.

**Combine that with timeouts:**

```
Bulkhead
   +
Timeout
   +
Circuit Breaker
```

and the system becomes much more resilient.

---

## 18. Timeout and Asynchronous Processing

Sometimes a task simply takes too long to perform synchronously.

**Bad:**

```
Client
  |
  v
API
  |
  v
Generate Report
  |
  | 5 minutes
  v
Response
```

The client may timeout.

**Better:**

```
Client
  |
  v
API
  |
  v
Queue
  |
  v
Background Worker
```

API immediately returns:

```
202 Accepted
```

Then:

```
Worker
  |
  v
Generate report
  |
  v
Store result
```

The client can later retrieve the result.

> This is an important technique for avoiding long synchronous timeouts.

---

## 19. Timeouts in Kafka

Kafka has several time-related configurations, but don't think of Kafka as having one universal "timeout."

For example, producer operations can have limits associated with:

- Request timeout
- Delivery timeout
- Retry behavior

**Conceptually:**

```
Producer
   |
   v
Kafka Broker
   |
   X
No response
   |
   v
Timeout
   |
   v
Retry / Failure
```

Consumer-side processing also needs careful timeout/heartbeat configuration.

For example:

```
Consumer
   |
   v
poll()
   |
   v
Process message
   |
   | long processing
   |
   v
poll again
```

If processing takes too long relative to the consumer group's expectations, Kafka may consider the consumer unhealthy and trigger a rebalance.

So when you're designing Kafka consumers, you need to consider:

- Poll interval
- Heartbeat
- Session timeout
- Processing time
- Retry time

> These must be compatible with each other.

---

## 20. Timeouts and Idempotency

This is extremely important for payments and other write operations.

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

Payment succeeds:

```
₹1,000 deducted ✅
```

But response doesn't reach the client:

```
Payment → Success
     X
Network
```

Client experiences:

```
TIMEOUT
```

Client retries.

**Without idempotency:**

```
₹1,000 deducted
₹1,000 deducted again
```

**With idempotency:**

```
Request
idempotency_key = P123
```

Second request:

```
P123 already processed?
       |
      YES
       |
       v
Return previous result
```

**So:**

> A timeout does not tell you whether a write succeeded.

For non-idempotent operations, this distinction is critical.

---

## 21. Timeout and "Unknown Outcome"

This is one of the most important concepts to understand.

Consider:

```
Client
  |
  | Request
  v
Payment Service
  |
  | Process
  v
Bank
  |
  v
SUCCESS
```

But the response is lost:

```
Bank → Payment Service → Client
                         X
```

Client sees:

```
TIMEOUT
```

**What does timeout mean?**

It could mean:

1. Request never reached server
2. Server received but didn't process
3. Server processed and response was lost
4. Server processed but response was delayed

**Therefore:**

> Timeout creates uncertainty about the outcome.

For critical operations, use:

- Idempotency keys
- Transaction IDs
- Status APIs
- Durable state
- Reconciliation

---

## 22. Timeout and Cascading Failures

Consider:

```
Service A
   |
   v
Service B
   |
   v
Service C
```

C becomes slow.

**Without proper timeouts:**

```
C slow
 ↓
B waits
 ↓
B threads exhausted
 ↓
A waits
 ↓
A threads exhausted
 ↓
System-wide outage
```

**With timeouts:**

```
C slow
 ↓
Timeout
 ↓
B stops waiting
 ↓
Fallback / error
 ↓
A remains healthy
```

> Timeouts therefore act as a **failure boundary**.

---

## 23. How to Choose a Timeout?

This is an excellent interview question.

**Don't simply say:**

> "Set timeout to 5 seconds."

**Instead, consider:**

### 1. Business requirement

How quickly does the user need an answer?

### 2. Downstream latency

What is the normal latency?

For example:

```
P50 = 50 ms
P95 = 100 ms
P99 = 200 ms
```

### 3. SLA/SLO

What latency does your service promise?

### 4. Network latency

Include network overhead.

### 5. Retry budget

If you retry, leave enough time for retries.

### 6. Resource limits

Don't allow requests to hold threads/connections indefinitely.

### 7. Operation type

Read and write operations may need different handling.

---

## 24. Don't Set Timeout Too Low

Suppose normal latency is:

```
P99 = 500 ms
```

and you set:

```
Timeout = 100 ms
```

Then many healthy requests will timeout.

```
Healthy request
     |
     v
200 ms
     |
     X
100 ms timeout
```

> This causes unnecessary failures.

---

## 25. Don't Set Timeout Too High

Suppose:

```
Timeout = 60 seconds
```

and your service receives:

```
10,000 requests
```

Many requests may remain blocked for a long time.

```
Request
  |
  v
Waiting 60 sec
```

Resources become exhausted.

**So the timeout needs to be:**

> Long enough for legitimate requests, but short enough to protect system resources and meet the user-facing latency budget.

---

## 26. Timeout Hierarchy

A good distributed system often has layered timeouts.

For example:

```
Client timeout
       ↓
API Gateway timeout
       ↓
Service timeout
       ↓
Database timeout
```

**Example:**

```
Client      = 10 sec
API Gateway = 9 sec
Order       = 8 sec
Payment     = 5 sec
Database    = 2 sec
```

The exact values depend on the system, but the important principle is:

> Don't let downstream operations outlive the caller's useful deadline.

---

## 27. Deadline Propagation

In more advanced distributed systems, instead of each service independently choosing a timeout, the caller propagates a **deadline**.

**Example:**

```
Client
Deadline = 10 sec
    |
    v
Order Service
Remaining = 8 sec
    |
    v
Payment Service
Remaining = 5 sec
    |
    v
Database
Remaining = 2 sec
```

Each service knows:

> "I have only this much time left to complete the request."

> This is generally better than blindly assigning fixed timeouts at every layer.

---

## 28. Availability and Timeouts

Timeouts help improve availability.

**Without timeout:**

```
Dependency fails
     ↓
Threads wait
     ↓
Resources exhausted
     ↓
Service fails
     ↓
Availability decreases
```

**With timeout:**

```
Dependency fails
     ↓
Timeout
     ↓
Release resources
     ↓
Fallback / failure response
     ↓
Service remains available
```

**So:**

```
Timeout
   ↓
Resource Protection
   ↓
Failure Isolation
   ↓
Higher Availability
```

---

## 29. Reliability and Timeouts

Timeouts also improve reliability.

**Without timeout:**

```
System waits indefinitely
```

**With timeout:**

```
Failure detected
     ↓
Controlled recovery
```

**Combined with:**

```
Timeout
+
Retry
+
Backoff
+
Circuit Breaker
+
Idempotency
```

you get a much more reliable distributed system.

---

## 30. Real-Time Example

Imagine an online shopping system:

```
                         User
                           |
                           v
                     API Gateway
                           |
                           v
                     Order Service
                    /      |       \
                   /       |        \
                Redis   Inventory   Payment
                           |           |
                           |           v
                           |          Bank
                           |
                         DB
```

Suppose Bank becomes slow.

**Bad behavior:**

```
User
 ↓
Order
 ↓
Payment
 ↓
Bank
 ↓
Waiting 60 sec
```

Thousands of requests can pile up.

**Better:**

```
User
 ↓
Order
 ↓
Payment
 ↓
Bank
 ↓
Timeout 3 sec
 ↓
Circuit Breaker
 ↓
Fallback / Pending Payment
```

For a payment workflow, you may return something like:

```
Order Status = PAYMENT_PENDING
```

and process/reconcile asynchronously rather than incorrectly declaring the payment failed.

---

## 31. Interview Answer

If the interviewer asks:

> "What is a timeout and why is it important?"

**A strong answer:**

> A timeout defines the maximum amount of time a system will wait for an operation or dependency to complete. It prevents threads, connections, and other resources from being blocked indefinitely when a dependency becomes slow or unavailable. In distributed systems, timeouts are essential for preventing cascading failures and improving availability. They are typically combined with limited retries, exponential backoff, circuit breakers, bulkheads, and deadline propagation. For write operations, we also need idempotency because a timeout does not tell us whether the operation actually succeeded.

---

## 32. Timeout Mental Model

Remember this:

```
                         TIMEOUT
                            |
                            v
                    Don't wait forever
                            |
                            v
                    Protect resources
                            |
             +--------------+--------------+
             |              |              |
          Threads       Connections     Memory
             |              |              |
             +--------------+--------------+
                            |
                            v
                     Failure boundary
                            |
              +-------------+-------------+
              |             |             |
            Retry        Fallback    Circuit Breaker
              |             |             |
          Backoff       Graceful      Fail Fast
                         Degradation
                            |
                            v
                       Resilience
```

### One-line memory trick

> **Timeout** prevents waiting forever; **retry** handles transient failures; **backoff** prevents retry storms; **circuit breaker** prevents repeated calls to an unhealthy dependency; **idempotency** protects correctness when a timed-out operation may have actually succeeded.

