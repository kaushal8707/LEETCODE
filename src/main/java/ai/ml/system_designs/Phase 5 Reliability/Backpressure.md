# Backpressure

**Backpressure** is a mechanism that allows a system to slow down, limit, or reject incoming work when the downstream component cannot process work as fast as it is arriving.

The simplest definition is:

> **Backpressure** = when the consumer is slower than the producer, the consumer tells the producer to slow down.

This is extremely important in distributed systems, Kafka, messaging systems, reactive programming, APIs, and high-throughput applications.

---

## 1. The basic problem

Imagine:

```
Producer
   │
   │ 10,000 requests/sec
   ▼
Queue
   │
   │ 1,000 requests/sec
   ▼
Consumer
```

The producer is generating:

```
10,000/sec
```

but the consumer can process only:

```
1,000/sec
```

Therefore:

```
Incoming = 10,000/sec
Processing = 1,000/sec

Backlog growth = 9,000/sec
```

Eventually:

```
Queue
  ↓
Memory increases
  ↓
Queue becomes full
  ↓
Requests/messages rejected
  ↓
System protects itself
```

Without backpressure, the system may eventually crash.

---

## 2. Real-world example

Consider an e-commerce application:

```
                    Order Service
                         │
                         ▼
                       Kafka
                         │
                         ▼
                  Payment Consumer
```

Suppose Order Service produces:

```
20,000 events/sec
```

Payment Consumer can process:

```
5,000 events/sec
```

Then:

```
Producer = 20,000/sec
Consumer = 5,000/sec

Lag increases by approximately:
15,000 messages/sec
```

If this continues for a long time:

```
Kafka Consumer Lag
        ↑
        ↑
        ↑
        ↑
        ↑
```

The consumer is falling behind.

A backpressure strategy might cause the producer or intermediate layer to slow down, limit concurrency, pause consumption, queue work, or reject lower-priority traffic.

---

## 3. Why Backpressure is necessary

**Without backpressure:**

```
Fast Producer
      │
      ▼
Slow Consumer
      │
      ▼
Unlimited Queue
      │
      ▼
Memory / Threads / Connections exhausted
      │
      ▼
System crashes
```

**With backpressure:**

```
Fast Producer
      │
      ▼
Backpressure
      │
      ├── Slow producer
      ├── Limit concurrency
      ├── Queue bounded
      ├── Pause consumption
      └── Reject work
```

The goal is:

> Keep the system within its sustainable processing capacity.

---

## 4. Producer vs Consumer

This is the fundamental concept.

```
Producer
   │
   │ produces work
   ▼
Buffer / Queue
   │
   │ consumes work
   ▼
Consumer
```

Suppose:

```
Producer = 1,000 msg/sec
Consumer = 100 msg/sec
```

Backlog:

```
900 msg/sec
```

If this continues:

```
1 sec  → 900 backlog
10 sec → 9,000 backlog
1 min  → 54,000 backlog
```

The queue keeps growing.

Backpressure tries to prevent unlimited growth.

---

## 5. Backpressure vs Rate Limiting

These are closely related but not the same.

### Rate Limiting

You explicitly define:

```
Maximum = 1,000 requests/sec
```

The limiter controls the incoming rate.

```
Client
  │
  ▼
Rate Limiter
  │
  ├── allowed
  └── rejected
```

### Backpressure

Backpressure is based on the system's ability to keep up.

For example:

```
Consumer capacity = 1,000/sec
Incoming traffic = 5,000/sec
```

The system effectively says:

> "I can't process this much work right now."

Then it may:

- slow down
- queue
- pause
- reject
- shed load

### Easy distinction

```
Rate Limiting
→ "You are allowed only X requests/sec."

Backpressure
→ "I'm getting overwhelmed; slow down."
```

---

## 6. Backpressure vs Bulkhead

Since we discussed Bulkhead earlier:

### Bulkhead

Limits resource consumption.

```
Payment
   ↓
Maximum 20 concurrent calls
```

### Backpressure

Controls work entering or flowing through the system.

```
Consumer overloaded
       ↓
Slow/reject/pause producer
```

So:

```
Bulkhead
→ Protect resources

Backpressure
→ Control work flow
```

They often work together.

---

## 7. Backpressure in synchronous APIs

Consider:

```
Client
   │
   ▼
API
   │
   ▼
Database
```

Suppose the DB can handle:

```
1,000 operations/sec
```

but API receives:

```
10,000 requests/sec
```

The API should not blindly create 10,000 database operations.

Instead:

```
Client
   │
   ▼
API
   │
   ▼
Concurrency Limit
   │
   ├── Process
   │
   └── Reject / Delay
```

For example:

```
Maximum DB concurrency = 100
```

Once all 100 slots are occupied:

```
Request 101
    ↓
No capacity
    ↓
Reject / queue / retry later
```

This is backpressure.

---

## 8. Backpressure in asynchronous systems

This becomes even more important with queues.

```
Producer
    │
    ▼
Message Queue
    │
    ▼
Consumer
```

Suppose:

```
Producer = 50,000 msg/sec
Consumer = 5,000 msg/sec
```

You might allow the queue to absorb a temporary burst:

```
Burst
 ↓
Queue grows
 ↓
Consumer catches up
```

That's perfectly reasonable.

But if the difference persists:

```
50,000/sec
       ↓
Queue
       ↓
5,000/sec
```

the backlog grows indefinitely.

Backpressure needs to kick in.

---

## 9. Common Backpressure strategies

There isn't one universal implementation.

Common approaches are:

### 1. Slow down producer

```
Producer
   ↓
"Slow down"
```

For example:

```
10,000/sec
   ↓
2,000/sec
```

### 2. Limit concurrency

```
Maximum concurrent operations = 100
```

Additional work waits or gets rejected.

### 3. Bounded queue

Instead of:

```
Unlimited Queue
```

use:

```
Queue capacity = 10,000
```

When full:

```
Queue FULL
   ↓
Reject / drop / block
```

### 4. Pause consumption

In messaging systems, a consumer can temporarily stop pulling more work.

Conceptually:

```
Consumer overloaded
       ↓
Pause consumption
       ↓
Process existing work
       ↓
Resume
```

### 5. Load shedding

Discard work that isn't essential.

For example:

```
Critical:
Payment → KEEP

Non-critical:
Analytics → DROP/DELAY
```

### 6. Queue for later

Instead of processing immediately:

```
Request
  ↓
Queue
  ↓
Worker
  ↓
Processing
```

This converts synchronous pressure into asynchronous processing.

---

## 10. Bounded vs Unbounded Queues

This is a very important system-design concept.

### Unbounded queue

```
Queue
████████████████████████████████████
████████████████████████████████████
████████████████████████████████████
             ...
```

Looks safe initially.

But eventually:

```
Queue grows
   ↓
Memory grows
   ↓
GC pressure
   ↓
Latency
   ↓
OOM
   ↓
Crash
```

### Bounded queue

```
Queue capacity = 10,000

████████████████████
       FULL
```

Once full:

```
New work
   ↓
Queue FULL
   ↓
Backpressure
   ↓
Reject / block / shed
```

A bounded queue makes overload **explicit** and **controllable**.

---

## 11. Backpressure and Kafka

Since you're learning Kafka deeply, this is especially important.

Kafka provides a durable buffer:

```
Producer
    │
    ▼
Kafka
    │
    ▼
Consumer
```

Suppose:

```
Producer → 100,000 messages/sec
Consumer → 20,000 messages/sec
```

Kafka retains the messages according to its retention configuration.

Consumer lag increases:

```
Lag
 ↑
 │        /
 │      /
 │    /
 │  /
 │/
 └────────────────→ Time
```

Kafka therefore absorbs bursts, but Kafka itself doesn't magically make the consumer capable of processing more.

Eventually you need to address the bottleneck.

---

## 12. Kafka backpressure strategies

For Kafka consumers, you can deal with overload by:

### Increase consumer parallelism

Increase the number of consumers in the consumer group, subject to partition count.

```
10 partitions
      ↓
up to 10 active consumers
```

If your workload is parallelizable, this can increase throughput.

### Increase partitions

If the existing partition count prevents sufficient consumer parallelism:

```
10 partitions
    ↓
20 partitions
```

But partition increases have implications for ordering and partition-key distribution.

### Batch processing

Instead of:

```
Message → DB call
Message → DB call
Message → DB call
```

process batches:

```
100 messages
      ↓
Batch operation
      ↓
DB
```

This can significantly improve throughput.

### Pause/resume

If downstream processing is temporarily overloaded:

```
Consumer
   ↓
Pause
   ↓
Process existing workload
   ↓
Resume
```

### Control downstream concurrency

For example:

```
Kafka Consumer
      ↓
Bulkhead
      ↓
Maximum 50 concurrent DB calls
```

This prevents the consumer from overwhelming the database.

---

## 13. Consumer Lag is an important signal

In Kafka:

```
Consumer Lag =
Latest available offset - Consumer's processed/committed position
```

Conceptually:

```
Kafka
───────────────────────────────>
             ↑              ↑
         Consumer        Latest
          offset          offset
```

The distance between them is lag.

If lag continuously increases:

```
Producer rate > Consumer processing rate
```

That is a strong indication that your consumers cannot keep up.

But don't automatically conclude "Kafka is slow."

The bottleneck may be:

- Consumer CPU
- Database
- External API
- Network
- Thread pool
- Connection pool
- Serialization
- Downstream service

---

## 14. Backpressure with Database

This is one of the most common real-world scenarios.

Suppose:

```
Kafka
  ↓
Order Consumer
  ↓
Database
```

Kafka receives:

```
50,000 events/sec
```

Database safely handles:

```
5,000 writes/sec
```

If the consumer blindly processes all messages concurrently:

```
50,000 DB operations/sec
        ↓
Connection pool exhausted
        ↓
DB overloaded
        ↓
Timeouts
        ↓
Retries
        ↓
More DB load
        ↓
Database collapse
```

A better design:

```
Kafka
  ↓
Consumer
  ↓
Bounded concurrency
  ↓
Batching
  ↓
Database
```

For example:

```
DB concurrency = 100
Batch size = 100
```

Now the consumer doesn't overwhelm the database.

---

## 15. Backpressure + Retry can be dangerous

This is an advanced and important point.

Suppose:

```
Consumer
   ↓
Database
   ↓
Timeout
```

You retry:

```
Request
 ↓
DB → timeout
 ↓
retry
 ↓
DB → timeout
 ↓
retry
```

If thousands of consumers do this simultaneously:

```
Failure
 ↓
Retry
 ↓
More traffic
 ↓
More failure
 ↓
More retry
 ↓
System collapse
```

This is sometimes called a **retry storm**.

Therefore:

```
Backpressure
+
Bounded concurrency
+
Timeout
+
Limited retries
+
Exponential backoff
+
Jitter
```

is much safer.

---

## 16. Backpressure and Circuit Breaker

These also complement each other.

Imagine:

```
Kafka Consumer
      │
      ▼
Bulkhead
      │
      ▼
Circuit Breaker
      │
      ▼
Payment Service
```

Payment becomes unhealthy.

**Circuit breaker:**

```
Payment failing
     ↓
Circuit OPEN
     ↓
Stop calls
```

**Backpressure:**

```
Consumer cannot process payment
     ↓
Slow/pause/queue/reject work
```

So:

```
Circuit Breaker
→ "Don't call the unhealthy dependency."

Backpressure
→ "Don't keep producing work that we cannot process."
```

---

## 17. Backpressure and Load Shedding

Sometimes you cannot slow the producer.

For example:

```
Internet traffic
```

You cannot tell the entire Internet:

> "Please slow down."

So you use load shedding.

```
Incoming traffic
      │
      ▼
Capacity = 10,000/sec
      │
      ├── Critical → ACCEPT
      │
      └── Excess → REJECT
```

For HTTP:

```
429 Too Many Requests
```

or sometimes:

```
503 Service Unavailable
```

depending on the semantics and architecture.

---

## 18. Priority-based backpressure

A mature system may prioritize work.

Example:

```
                    Incoming Work
                         │
             ┌───────────┼───────────┐
             ▼           ▼           ▼
          Critical     Normal      Background
             │           │           │
             ▼           ▼           ▼
           Keep        Limit        Drop
```

During overload:

```
Payment
→ High priority

Order creation
→ High priority

Email
→ Medium priority

Analytics
→ Low priority
```

This allows the system to preserve critical functionality.

---

## 19. Reactive systems and Backpressure

Backpressure is especially important in **Reactive Programming**.

Conceptually:

```
Publisher
    │
    │ produces data
    ▼
Subscriber
```

Instead of the publisher sending unlimited data, the subscriber can communicate demand.

For example:

Subscriber says:

```
"I can process 100 items."
```

Publisher sends:

```
100 items
```

Then subscriber requests another:

```
100 items
```

This is often described as **demand-driven flow control**.

This concept is central to reactive-streams implementations.

---

## 20. Pull vs Push

Backpressure is often easier with pull-based systems.

### Push

Producer decides:

```
Producer
   ↓
Consumer
   ↓
Consumer
   ↓
Consumer
```

The producer may overwhelm the consumer.

### Pull

Consumer decides:

```
Consumer
   ↑
"I need 100 messages."
   │
   ▼
Producer / Queue
```

The consumer controls how much work it receives.

Kafka consumers are fundamentally **pull-based**: consumers fetch records from brokers rather than brokers pushing individual records to consumers.

That makes controlling consumption rate easier.

---

## 21. A complete production architecture

Combining the resilience patterns we've discussed:

```
                         CLIENTS
                            │
                            ▼
                          WAF
                            │
                            ▼
                     RATE LIMITER
                            │
                            ▼
                       API GATEWAY
                            │
                            ▼
                     LOAD BALANCER
                            │
                            ▼
                      APPLICATION
                            │
                    ┌───────┴───────┐
                    │               │
                 Bulkhead       Backpressure
                    │               │
                    └───────┬───────┘
                            │
                            ▼
                       TIMEOUT
                            │
                            ▼
                     CIRCUIT BREAKER
                            │
                            ▼
                          RETRY
                            │
                       Backoff/Jitter
                            │
                            ▼
                     DOWNSTREAM SERVICE
```

Each pattern solves a different problem.

---

## 22. Rate Limiting vs Backpressure vs Bulkhead

This distinction is extremely important for interviews.

| Pattern | Main Question |
|---|---|
| Rate Limiting | How many requests should be accepted per second/minute? |
| Backpressure | What should happen when consumers can't keep up? |
| Bulkhead | How much resource can one workload consume? |
| Circuit Breaker | Should we stop calling an unhealthy dependency? |
| Timeout | How long should we wait? |
| Retry | Should we try again? |
| Backoff | How long should we wait before retrying? |
| Load Shedding | What work can we reject/drop during overload? |

---

## 23. Interview scenario

**Interviewer:**

> Your Kafka consumer receives 50,000 events/sec, but your database can process only 5,000/sec. What would you do?

**A strong answer:**

> I would first identify the actual bottleneck and avoid allowing unbounded concurrency against the database. I would introduce bounded concurrency, batching where possible, and backpressure so the consumer doesn't continuously increase downstream pressure. Kafka can absorb a temporary burst, so consumer lag can increase temporarily, but if lag continuously grows, I would scale consumers subject to partition count, optimize database writes, and potentially partition workloads. I would also use timeouts, limited retries with exponential backoff and jitter, and circuit breaking for unhealthy downstream dependencies. If the system cannot process the incoming workload even after scaling, I would use load shedding or prioritization rather than allowing resource exhaustion.

That's a much stronger answer than simply saying:

> "Increase Kafka consumers."

---

## 24. The most important mental model

Think of a distributed system as a chain:

```
Producer
   ↓
Queue
   ↓
Consumer
   ↓
Database
```

Every component has a capacity.

For example:

```
Producer      = 50,000/sec
Kafka         = 100,000/sec
Consumer      = 20,000/sec
Database      = 5,000/sec
```

The real bottleneck is:

```
Database = 5,000/sec
```

Therefore the whole pipeline ultimately needs to respect that capacity.

```
50,000
   ↓
20,000
   ↓
5,000
   ↓
DATABASE
```

Backpressure prevents the upstream components from overwhelming the bottleneck.

---

## Final mental model

```
                 RESILIENCE PATTERNS

Rate Limiter
      │
      └── Controls incoming rate
               │
               ▼
Backpressure
      │
      └── Controls flow when downstream can't keep up
               │
               ▼
Bulkhead
      │
      └── Limits resource/concurrency consumption
               │
               ▼
Timeout
      │
      └── Limits waiting
               │
               ▼
Retry + Backoff + Jitter
      │
      └── Handles transient failures safely
               │
               ▼
Circuit Breaker
      │
      └── Stops calls to unhealthy dependencies
               │
               ▼
Load Shedding
      │
      └── Rejects/drops work when capacity is exhausted
```

### One sentence to remember

> Rate limiting controls how much traffic enters, backpressure controls how work flows through the system, bulkhead limits resource consumption, and circuit breaker prevents repeated calls to an unhealthy dependency.

