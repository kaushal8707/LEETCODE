# Kafka Backpressure

**Backpressure** is a mechanism where a slower downstream component tells an upstream component, directly or indirectly:

> "I'm processing data slower than you're producing it. Slow down, buffer, or apply a controlled load-management strategy."

This is extremely important in Kafka because Kafka can handle very high throughput, while your consumer, database, REST API, or downstream service may not.

---

## 1. The basic problem

Imagine:

```
Kafka Producer
      |
      | 100,000 msg/sec
      v
   Kafka Topic
      |
      | 100,000 msg/sec
      v
   Consumer
      |
      | 10,000 msg/sec
      v
   Database
```

Your database can process only:

```
10,000 records/sec
```

but Kafka is receiving:

```
100,000 records/sec
```

Now:

```
Incoming rate > Processing rate
```

So the system starts accumulating work.

```
Kafka
 |
 | 100K/sec
 v
Consumer
 |
 | 10K/sec
 v
Database
```

The unprocessed messages accumulate as:

```
Consumer Lag ↑↑↑
```

This is the Kafka manifestation of backpressure.

---

## 2. What is Consumer Lag?

Suppose Kafka has:

```
Latest offset = 1,000,000
```

Consumer has processed:

```
950,000
```

Then approximately:

```
Lag = 1,000,000 - 950,000
    = 50,000
```

Conceptually:

```
Kafka
-------------------------------------------------->
950000                         1000000
   ^                               ^
   |                               |
Consumer position              Latest record
```

So:

```
Consumer Lag = Producer/Log position - Consumer position
```

More precisely, Kafka monitoring often uses the difference between the partition's **log end offset** and the consumer group's **committed/current position**.

---

## 3. Why does backpressure happen?

Common reasons:

### 1. Consumer is slow

```
Kafka → Consumer
          ↓
       CPU heavy
```

### 2. Database is slow

```
Consumer
   ↓
Database
   ↓
Slow queries
```

### 3. External API is slow

```
Consumer
   ↓
Payment API
   ↓
Latency = 2 seconds
```

### 4. Downstream rate limiting

```
API → HTTP 429
```

### 5. Traffic spike

Normal:

```
10K msg/sec
```

Suddenly:

```
500K msg/sec
```

### 6. Consumer GC / CPU saturation

```
CPU → 100%
GC → high
Throughput → drops
```

---

## 4. Kafka has a very interesting advantage

Kafka itself can act as a **buffer**.

Instead of:

```
Producer
   ↓
Database
```

you have:

```
Producer
   ↓
Kafka
   ↓
Consumer
   ↓
Database
```

If the database temporarily slows down:

```
Producer → Kafka → Consumer → Database
              ↑
         buffer builds
```

Kafka retains the messages.

The consumer can process them later.

This gives you **temporal decoupling**.

---

## 5. Example: Traffic spike

Normal traffic:

```
Producer = 10K/sec
Consumer = 10K/sec
```

Lag:

```
~0
```

Now traffic spikes:

```
Producer = 100K/sec
Consumer = 10K/sec
```

After one second:

```
Produced = 100K
Processed = 10K

Lag ≈ 90K
```

After 10 seconds:

```
Produced ≈ 1,000K
Processed ≈ 100K

Lag ≈ 900K
```

Kafka is buffering the workload.

---

## 6. Is consumer lag itself backpressure?

Not exactly.

This distinction is important.

**Backpressure** is the load-control concept.

**Consumer lag** is one important signal that the consumer is unable to keep up.

Think:

```
Backpressure
     |
     +-- Consumer can't keep up
             |
             v
        Consumer Lag ↑
```

But lag can also increase because of:

- consumer restart
- rebalance
- partition reassignment
- network issues
- consumer bugs
- downstream failures
- insufficient partitions/consumers.

So don't automatically conclude:

> "Lag means backpressure."

Instead:

> Increasing lag is a strong indicator that consumption capacity is below incoming workload or that the consumer is temporarily unable to progress.

---

## 7. How does Kafka handle backpressure?

Kafka doesn't work exactly like some reactive-stream systems where the producer is automatically blocked whenever a downstream consumer is slow.

Instead, Kafka provides mechanisms that allow consumers to control their consumption and lets the log absorb bursts.

The architecture is roughly:

```
                  Producer
                     |
                     v
                  Kafka
                     |
              +------+------+
              |             |
          Consumer A     Consumer B
              |
              v
          Database
```

If Consumer A is slow:

```
Kafka continues retaining records
             |
             v
Consumer A catches up later
```

---

## 8. The first strategy: Consumer naturally slows down

Suppose:

```
Consumer
   ↓
DB
```

DB becomes slow.

The consumer can process fewer messages:

```
Kafka → Consumer → DB
                   ↑
                 slow
```

Kafka does not have to delete the records because they remain in the partition until retention/compaction policies remove them.

Lag grows:

```
Lag:
100
500
2,000
10,000
...
```

Once the database recovers:

```
Consumer throughput ↑
       ↓
Lag decreases
```

This is a basic form of buffering/backpressure handling.

---

## 9. Second strategy: Pause consumption

Some Kafka consumer frameworks allow consumers to **pause partitions**.

Conceptually:

```
Consumer
   |
   +-- P0 → running
   +-- P1 → paused
   +-- P2 → running
```

Why?

Suppose:

```
P1 → messages are causing downstream overload
```

You can temporarily stop fetching/processing from P1.

Later:

```
Database recovers
       ↓
Resume P1
```

This can be useful for controlled flow.

---

## 10. Third strategy: Limit concurrency

Suppose one consumer receives messages and invokes a downstream service.

Without control:

```
Kafka
 ↓
Consumer
 ├── API call
 ├── API call
 ├── API call
 ├── API call
 ├── API call
 ├── ...
 └── 10,000 concurrent calls 🔥
```

You can introduce **bounded concurrency**:

```
Kafka
 ↓
Consumer
 ↓
Concurrency = 20
 ↓
Downstream API
```

Now:

```
Maximum concurrent requests = 20
```

This protects the downstream service.

---

## 11. Fourth strategy: Batch processing

Suppose you're processing:

```
10,000 Kafka records
```

and performing one DB operation per message:

```
10,000 messages
      ↓
10,000 DB calls
```

This can be expensive.

Instead:

```
Kafka
 ↓
Consumer
 ↓
Batch 1 → 500 records
Batch 2 → 500 records
Batch 3 → 500 records
```

Then:

```
500 Kafka records
       ↓
Batch DB operation
```

Batching can significantly improve throughput and reduce per-message overhead.

But batch size must be bounded because excessively large batches increase:

- memory
- latency
- transaction duration
- retry blast radius.

---

## 12. Fifth strategy: Scale consumers

Suppose:

```
Topic = 12 partitions

Consumers = 3
```

Potential assignment:

```
C1 → P0 P1 P2 P3
C2 → P4 P5 P6 P7
C3 → P8 P9 P10 P11
```

Now increase to:

```
Consumers = 12
```

Potentially:

```
C1 → P0
C2 → P1
C3 → P2
...
C12 → P11
```

More consumers can increase processing capacity.

But:

> Consumer count cannot provide more active parallelism than the number of partitions in that consumer group.

---

## 13. Important limitation: downstream may be the bottleneck

Suppose:

```
Kafka = 1M msg/sec capacity
Consumers = 100
```

Looks powerful.

But:

```
Database = 20K writes/sec
```

Then:

```
Kafka
 ↓
100 Consumers
 ↓
Database
 ↓
20K/sec
```

Adding another 100 consumers doesn't solve the fundamental problem.

You may simply turn:

```
Database
```

into:

```
Database 🔥🔥🔥
```

Therefore:

> Backpressure is about protecting the slowest component in the pipeline, not simply maximizing Kafka consumer throughput.

---

## 14. Sixth strategy: Rate limiting

Suppose downstream API allows:

```
5,000 requests/sec
```

but consumer can generate:

```
20,000 requests/sec
```

Introduce a rate limiter:

```
Kafka
 ↓
Consumer
 ↓
Rate Limiter
 ↓
5K req/sec
 ↓
API
```

Then:

```
Consumer capacity > API capacity
```

but the rate limiter prevents overload.

---

## 15. Seventh strategy: Retry with backoff

Suppose downstream service returns:

```
HTTP 503
```

Don't immediately do:

```
retry
retry
retry
retry
```

Instead:

```
Failure
  ↓
Retry after 1 sec
  ↓
Retry after 5 sec
  ↓
Retry after 30 sec
  ↓
DLT
```

This is especially important because aggressive retries can make a struggling service even worse.

---

## 16. Retry storm

This is a classic distributed-systems failure.

Imagine:

```
Kafka
 ↓
10,000 consumers
 ↓
Payment Service
```

Payment service goes down.

Every consumer immediately retries:

```
10,000 requests
 ↓
Payment Service
 ↓
FAIL
 ↓
10,000 retries
 ↓
FAIL
 ↓
20,000 retries
```

The service may never recover.

This is called a **retry storm**.

Backpressure + exponential backoff helps:

```
Kafka
 ↓
Consumer
 ↓
Bounded concurrency
 ↓
Rate limiter
 ↓
Exponential backoff
 ↓
Payment Service
```

---

## 17. Backpressure + DLT

You just learned DLT.

These concepts work together.

Suppose:

```
Kafka
 ↓
Consumer
 ↓
Downstream API
```

Temporary failure:

```
503
 ↓
Retry
 ↓
Backoff
```

Permanent failure:

```
Invalid payload
 ↓
DLT
```

So:

```
                   Consumer
                      |
                 Error handling
                 /            \
          Temporary          Permanent
              |                  |
              v                  v
        Retry + Backoff         DLT
```

---

## 18. Backpressure + consumer lag

This is one of the most important production monitoring relationships.

Suppose:

```
Incoming:
100K/sec

Consumer:
50K/sec
```

Then:

```
Lag ↑
```

If consumer capacity improves:

```
Consumer:
120K/sec
```

while incoming remains:

```
100K/sec
```

then:

```
Lag ↓
```

Therefore:

```
Producer Rate > Consumer Rate
        ↓
      Lag ↑

Producer Rate < Consumer Rate
        ↓
      Lag ↓
```

---

## 19. Lag recovery rate

This is a useful senior-level concept.

Suppose:

```
Incoming = 100K/sec
Consumer = 120K/sec
```

Then consumer has:

```
20K/sec
```

of excess capacity to catch up.

If lag is:

```
1,000,000
```

roughly:

```
Recovery time ≈ 1,000,000 / 20,000
             ≈ 50 seconds
```

This is simplified because real systems have batching, partition skew, variable throughput, etc., but it's useful for capacity planning.

---

## 20. Partition-level backpressure

Remember:

> Kafka parallelism comes from partitions.

Suppose:

```
P0 → 10K/sec
P1 → 10K/sec
P2 → 10K/sec
P3 → 100K/sec 🔥
```

Overall consumer capacity may look okay, but P3 becomes a bottleneck.

This is a **hot partition** problem.

So monitoring should look at:

- Topic lag
- Partition lag
- Consumer throughput
- Partition throughput

not just aggregate lag.

---

## 21. Backpressure and partition key

This connects directly to your previous question.

Suppose you choose:

```
key = customerId
```

and one customer generates:

```
50% of traffic
```

Then:

```
Customer A
    ↓
Partition 7
    ↓
Consumer
    ↓
Slow
```

Other consumers may be idle:

```
C1 → P0 → normal
C2 → P1 → normal
C3 → P2 → normal
...
C8 → P7 → 🔥
```

Adding consumers doesn't help if the hot partition remains assigned to one consumer.

Therefore:

> Bad partition-key distribution can create backpressure even when the cluster has plenty of unused capacity.

---

## 22. Backpressure and consumer group scaling

Suppose:

```
12 partitions
3 consumers
```

and lag is increasing.

First possibility:

```
Consumer capacity insufficient
```

Scale:

```
3 → 6 consumers
```

Now:

```
12 partitions
6 consumers
```

Potentially better.

But if you already have:

```
12 partitions
12 consumers
```

then adding:

```
13th consumer
```

doesn't increase partition-level parallelism.

```
12 partitions
12 active consumers
1 idle consumer
```

---

## 23. Backpressure and database connection pools

This is a very common production issue.

Suppose:

```
Kafka Consumer concurrency = 100
```

but:

```
DB connection pool = 20
```

You effectively have:

```
100 consumers
      ↓
20 DB connections
      ↓
80 waiting
```

This can increase:

- latency
- memory
- thread usage
- consumer lag

A better design may be:

```
Kafka
 ↓
bounded consumer concurrency
 ↓
DB connection pool
 ↓
Database
```

The concurrency should be chosen with the downstream capacity in mind.

---

## 24. Backpressure and memory

A dangerous implementation is:

```
Kafka
 ↓
Consumer
 ↓
Read everything
 ↓
Store millions of messages in memory
 ↓
Process slowly
```

Eventually:

```
Heap → 🔥
GC → 🔥
OOM → 💥
```

Instead, use **bounded buffering**:

```
Kafka
 ↓
Consumer
 ↓
Queue / buffer
 ↓
Maximum 1,000 messages
 ↓
Workers
 ↓
Database
```

When the buffer is full:

```
stop/slow intake
```

This is classic backpressure.

---

## 25. Backpressure doesn't always mean "stop consuming"

There are multiple strategies:

```
Backpressure
   |
   +-- Slow consumption
   |
   +-- Pause partitions
   |
   +-- Bound concurrency
   |
   +-- Bound internal queue
   |
   +-- Rate limit downstream calls
   |
   +-- Batch processing
   |
   +-- Scale consumers
   |
   +-- Retry with backoff
   |
   +-- Buffer in Kafka
```

The correct choice depends on where the bottleneck is.

---

## 26. Real-world payment example

Imagine:

```
Payment Events
      |
      v
Kafka
      |
      v
Payment Consumer
      |
      v
Payment Gateway
```

Normal:

```
Kafka = 20K events/sec
Consumer = 20K/sec
Gateway = 20K/sec
```

Suddenly gateway capacity drops:

```
Gateway = 5K/sec
```

If consumer continues sending 20K/sec:

```
Gateway
   ↓
Overloaded
   ↓
429 / 503
   ↓
Retries
   ↓
More overload
```

Better architecture:

```
Kafka
  ↓
Consumer
  ↓
Bounded concurrency
  ↓
Rate limiter: 5K/sec
  ↓
Payment Gateway
```

And for temporary failures:

```
Retry + exponential backoff
```

For permanent failures:

```
DLT
```

Kafka absorbs the excess:

```
Incoming = 20K/sec
Processing = 5K/sec

Lag increases temporarily
```

When gateway recovers:

```
Processing = 25K/sec
Incoming = 20K/sec

Lag decreases
```

This is a healthy recovery pattern.

---

## 27. Backpressure vs load shedding

These are related but different.

### Backpressure

Try to slow down the upstream flow:

```
Producer/Consumer
       ↓
slow down
```

### Load shedding

Intentionally discard/reject lower-priority work:

```
Too much traffic
      ↓
Drop non-critical events
```

For example:

```
Critical:
PaymentCompleted → NEVER drop

Non-critical:
UserTypingEvent → potentially droppable
```

Kafka's durable log is often used to absorb bursts, while load shedding may be implemented at application/API layers where dropping is acceptable.

---

## 28. Backpressure vs throttling

### Throttling

Explicitly limit rate:

```
10K requests/sec
```

### Backpressure

React to downstream capacity:

```
Downstream slowing
      ↓
Reduce intake/concurrency
```

Throttling can be one mechanism used to implement backpressure.

---

## 29. How to troubleshoot increasing Kafka lag

For a production system, don't immediately say:

> "Add more consumers."

Use this sequence:

```
Lag increasing
      |
      v
Check incoming rate
      |
      v
Check consumer processing rate
      |
      v
Check partition-level lag
      |
      v
Check consumer CPU / memory / GC
      |
      v
Check downstream DB/API latency
      |
      v
Check partition skew / hot keys
      |
      v
Check rebalances
      |
      v
Check network / broker health
```

Then choose the solution.

---

## 30. A useful diagnostic table

| Symptom | Possible cause | Solution |
|---|---|---|
| Lag increasing everywhere | Consumer capacity too low | Scale consumers / optimize processing |
| One partition lagging | Hot partition | Revisit partition key |
| DB latency high | DB bottleneck | Tune DB / batch / reduce concurrency |
| API 429 | Rate limit | Throttle |
| API 503 | Temporary outage | Retry + backoff |
| CPU 100% | CPU-bound consumer | Optimize / scale |
| GC high | Too much memory/concurrency | Bound buffers/concurrency |
| Consumers idle | Too few partitions | Increase partitions if appropriate |
| Frequent rebalances | Unstable consumers / long processing | Tune consumer behavior, processing model |
| Lag spikes during traffic bursts | Capacity mismatch | Kafka buffering + autoscaling |

---

## 31. Very important: Don't blindly increase `max.poll.records`

`max.poll.records` controls the maximum number of records returned from a `poll()` call.

Increasing it can improve batching in some workloads, but it can also increase:

- processing time
- memory
- batch size

and potentially make consumer responsiveness worse.

Similarly, don't blindly increase:

- `fetch.max.bytes`
- `max.partition.fetch.bytes`

Tune based on:

- record size
- batch size
- processing latency
- memory
- throughput

---

## 32. `max.poll.interval.ms` and backpressure

This is an important Kafka consumer consideration.

Suppose processing takes too long:

```
poll()
 ↓
Process huge batch
 ↓
5 minutes
 ↓
next poll()
```

If the processing interval exceeds the configured maximum poll interval, Kafka can consider the consumer unhealthy and trigger a rebalance.

So:

```
Backpressure
   ↓
Processing gets slower
   ↓
Poll interval increases
   ↓
Potential rebalance
```

This can make the situation even worse.

Therefore consumer processing must be designed so that the consumer continues polling within appropriate limits, or the application must use an architecture/framework that handles long-running processing safely.

---

## 33. Backpressure feedback loop

The whole system can be viewed as:

```
          Incoming traffic
                 |
                 v
              Kafka
                 |
                 v
             Consumer
                 |
          +------+------+
          |             |
       Fast          Slow
          |             |
          v             v
       Process       Backpressure
                        |
             +----------+----------+
             |          |          |
           Pause      Limit      Retry
           fetch    concurrency  backoff
```

Kafka's log provides the buffer between production and consumption.

---

## 34. Senior interview answer

If an interviewer asks:

> "What is backpressure in Kafka?"

A strong answer is:

> "Backpressure occurs when the rate at which records arrive exceeds the rate at which consumers or downstream systems can process them. Kafka's durable log naturally absorbs temporary bursts, while consumer lag indicates that processing is falling behind. In production, I'd manage backpressure using bounded consumer concurrency, batching, rate limiting, partition pausing where appropriate, retry with exponential backoff, and consumer scaling. I'd also investigate partition-level lag and hot keys because simply adding consumers won't help if one partition is the bottleneck. The key is to protect downstream dependencies while allowing the system to catch up when capacity recovers."

---

## 35. The most important mental model

Remember this:

```
             Producer
                |
                | 100K/sec
                v
             Kafka
                |
          Durable Buffer
                |
                | 10K/sec
                v
            Consumer
                |
                v
           Database
```

If:

```
Producer rate > Consumer rate
```

then:

```
             Kafka
                |
                v
         Consumer Lag ↑
```

The solution isn't always:

> "Add consumers!"

Instead ask:

```
Where is the bottleneck?
       |
       +-- Kafka?
       +-- Partition?
       +-- Consumer CPU?
       +-- Consumer concurrency?
       +-- Database?
       +-- External API?
       +-- Network?
       +-- Hot partition?
```

Then apply the appropriate control.

---

## Golden rule

> **Backpressure is about preventing a fast producer or consumer pipeline from overwhelming a slower downstream component while preserving system stability and allowing the backlog to recover later.**

And for Kafka specifically:

```
Backpressure
     +
Kafka retention
     +
Consumer lag
     +
Bounded concurrency
     +
Retry/backoff
     +
DLT
     +
Good partition strategy
     ↓
Resilient Kafka system
```

