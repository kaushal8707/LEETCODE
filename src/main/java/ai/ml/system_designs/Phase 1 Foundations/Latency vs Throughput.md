# Latency vs Throughput

This is one of the most important System Design fundamentals.

You have already covered:

```
Client
  ↓
DNS
  ↓
IP
  ↓
Port / Socket
  ↓
TCP / UDP
  ↓
TLS
  ↓
HTTP
  ↓
REST API
```

Now we need to understand how we measure the performance of such a system.

The two most important metrics are:

```
Latency    = How long does one request take?
Throughput = How many requests can the system process in a given amount of time?
```

---

## 1. Simple Example

Imagine an API:

```
GET /products/101
```

You send one request.

It takes:

```
100 ms
```

That is **latency**.

Now suppose your server can process:

```
1,000 requests/second
```

That is **throughput**.

So:

```
Latency   → Time per request
Throughput → Requests per unit of time
```

---

## 2. Real-World Analogy — Restaurant

Imagine a restaurant.

### Latency

You enter the restaurant and order food.

```
Order placed
     ↓
Wait 20 minutes
     ↓
Food arrives
```

Your waiting time:

```
20 minutes
```

That's similar to latency.

### Throughput

Suppose the restaurant serves:

```
100 customers/hour
```

That's throughput.

So:

```
Latency
= How long one customer waits

Throughput
= How many customers the restaurant serves per hour
```

---

## 3. API Example

Suppose:

```
Client → REST API → Server → Database
```

Request:

```
GET /users/101
```

Timeline:

```
Client
  |
  | Request
  ↓
Server
  |
  | Database query
  ↓
Database
  |
  | Response
  ↓
Server
  |
  | Response
  ↓
Client
```

Suppose total time is:

```
Network         20 ms
Server          10 ms
Database        30 ms
Network         20 ms
----------------------
Total           80 ms
```

Then:

```
Latency = 80 ms
```

---

## 4. Throughput Example

Suppose the same server processes:

```
1000 requests/second
```

Then:

```
Throughput = 1000 RPS
```

RPS means:

**Requests Per Second**

You may also see:

```
QPS = Queries Per Second
TPS = Transactions Per Second
```

depending on what is being measured.

---

## 5. The Difference

| Metric | Question |
|---|---|
| Latency | How long does one operation take? |
| Throughput | How many operations can we process? |

Example:

```
Latency = 50 ms
Throughput = 10,000 requests/sec
```

This means:

Individual requests typically take around 50 ms, while the system can process around 10,000 requests per second under the stated workload/conditions.

---

## 6. Very Important: Low Latency ≠ High Throughput

These are related, but not the same thing.

Consider:

```
System A
Latency = 10 ms
Throughput = 100 RPS
```

and:

```
System B
Latency = 100 ms
Throughput = 10,000 RPS
```

System A has:

```
Lower latency
```

System B has:

```
Higher throughput
```

Which is better?

**It depends on the use case.**

---

## 7. Example — Payment API

Imagine:

```
POST /payments
```

For payments, you may care heavily about:

- Latency
- Correctness
- Reliability
- Consistency

You might prefer:

```
100 ms
```

over:

```
1 second
```

if the system can maintain correctness and reliability.

---

## 8. Example — Video Processing

Imagine a video-processing system:

```
Upload Video
     ↓
Video Processing
     ↓
Store Result
```

Suppose processing one video takes:

```
30 seconds
```

That may be acceptable.

But you might want:

```
10,000 videos/hour
```

So **throughput** becomes extremely important.

---

## 9. Example — Kafka

Suppose you're processing events:

```
OrderCreated
PaymentCompleted
ShipmentCreated
```

A consumer might process:

```
100,000 events/second
```

That's throughput.

But suppose each event takes:

```
200 ms
```

from arrival until processing completes.

That's latency.

You may care about both:

```
Throughput = 100K events/sec
Processing latency = 200 ms
```

---

## 10. Latency is Not Always One Number

This is very important in real systems.

Suppose you have 1 million requests.

Their latencies are:

```
10 ms
11 ms
12 ms
...
50 ms
...
500 ms
...
2 seconds
```

If you calculate only the average:

```
Average = 50 ms
```

you might think:

> "Great! Our API is fast."

But perhaps 5% of users experience:

```
500+ ms
```

That's why system designers care about **percentiles**.

---

## 11. P50, P95, P99

These are extremely important.

### P50

50% of requests complete within this latency.

Example:

```
P50 = 40 ms
```

Half the requests are ≤ 40 ms.

This is also called the **median**.

### P95

95% of requests complete within:

```
P95 = 100 ms
```

Meaning:

```
95% → ≤ 100 ms
5%  → > 100 ms
```

### P99

```
P99 = 300 ms
```

Meaning:

```
99% → ≤ 300 ms
1%  → > 300 ms
```

---

## 12. Why P99 Matters

Imagine:

```
1,000,000 requests
```

If:

```
P99 = 2 seconds
```

then approximately:

```
10,000 requests
```

experience latency above 2 seconds.

For a large system, that's a lot of users.

So in production you might hear:

> "Our API has a P99 latency of 300 ms."

That's much more useful than simply saying:

> "Our average latency is 50 ms."

---

## 13. Latency Components

For a REST API, latency might be:

```
Client
  |
  | Network
  ↓
Load Balancer
  |
  | Network
  ↓
API Server
  |
  | DB query
  ↓
Database
  |
  ↓
API Server
  |
  ↓
Load Balancer
  |
  ↓
Client
```

Total latency can include:

- DNS
- TCP connection
- TLS handshake
- Network transmission
- Load balancer
- Application processing
- Database
- Cache
- External API
- Serialization/deserialization

---

## 14. Example Latency Calculation

Suppose:

```
DNS              = 10 ms
TCP              = 20 ms
TLS              = 30 ms
Load Balancer    = 5 ms
Application      = 20 ms
Database         = 40 ms
Response network = 20 ms
```

Approximate total:

```
10 + 20 + 30 + 5 + 20 + 40 + 20

= 145 ms
```

So:

```
Latency ≈ 145 ms
```

However, real measurements need to account for connection reuse, parallelism, queueing, retries, and how exactly each component's latency is measured.

---

## 15. How to Reduce Latency

Common techniques include:

### 1. Caching

Instead of:

```
API → Database
```

use:

```
API → Redis
```

If the data is cached:

```
API → Redis
```

might be much faster than:

```
API → Database
```

### 2. CDN

For static content:

```
User
  ↓
CDN
```

instead of:

```
User
  ↓
Origin Server
```

The CDN can serve content from a location closer to the user.

### 3. Connection Reuse

Instead of establishing a new TCP/TLS connection for every request:

```
Request
 ↓
TCP
 ↓
TLS
 ↓
Request
 ↓
Close
```

reuse the connection:

```
TCP + TLS
    |
    ├── Request 1
    ├── Request 2
    ├── Request 3
    └── Request 4
```

HTTP keep-alive and HTTP/2 are examples of mechanisms that can help reduce connection overhead.

### 4. Database Optimization

For example:

```
Bad query → 500 ms
Optimized query → 20 ms
```

Database indexes, query optimization, appropriate schema design, and caching can dramatically affect latency.

### 5. Reduce Network Calls

Instead of:

```
Service A
   ↓
Service B
   ↓
Service C
   ↓
Service D
```

you may redesign the workflow to avoid unnecessary sequential calls.

Every network hop adds potential latency and failure points.

---

## 16. Throughput

Now let's focus on throughput.

Suppose:

Server processes:

```
100 requests/sec
```

Then:

```
Throughput = 100 RPS
```

If you have:

```
10 servers
```

and each can process:

```
100 RPS
```

the theoretical aggregate capacity is:

```
10 × 100

= 1,000 RPS
```

assuming the workload scales well and there aren't bottlenecks elsewhere.

---

## 17. Horizontal Scaling

This is where throughput becomes extremely important.

Suppose:

```
One server
     |
     ↓
1,000 RPS
```

Traffic increases:

```
10,000 RPS
```

You can add servers:

```
                 Load Balancer
                 /     |     \
                ↓      ↓      ↓
             Server  Server  Server
               1       2       3
             1000    1000    1000 RPS
```

Add more:

```
10 servers × 1000 RPS

= 10,000 RPS
```

That's **horizontal scaling**.

---

## 18. But Throughput Has Bottlenecks

Suppose:

```
API Servers
10,000 RPS
      |
      ↓
Database
1,000 RPS
```

Your system's effective throughput may be limited by the database.

```
API
10K RPS
  ↓
DB
1K RPS
```

The database becomes the **bottleneck**.

This is one of the most important system-design concepts:

> **The throughput of the overall system is constrained by its bottleneck.**

---

## 19. Queueing

Suppose incoming traffic is:

```
2,000 requests/sec
```

but your service can process:

```
1,000 requests/sec
```

Then:

```
Incoming
  ↓
2000 RPS
  ↓
Queue
  ↓
1000 RPS processing
```

The queue starts growing.

As the queue grows:

```
Latency ↑
```

This is a very important relationship.

---

## 20. The Latency-Throughput Relationship

Consider:

```
Traffic increases
       ↓
CPU utilization increases
       ↓
Queueing increases
       ↓
Latency increases
```

So a system may behave like:

```
Low load:
Latency = 20 ms

Moderate load:
Latency = 30 ms

High load:
Latency = 100 ms

Near saturation:
Latency = 1 second

Overloaded:
Requests queue/time out
```

This is why simply saying:

> "Our server can handle 10,000 RPS"

is incomplete.

You should ask:

> **At what latency?**

---

## 21. Very Important Interview Question

> **"What is the difference between latency and throughput?"**

A strong answer:

> Latency is the time required to complete an individual request or operation, while throughput is the amount of work a system can process per unit of time, such as requests per second. A system can have low latency but relatively low throughput, or high throughput with higher latency, so both metrics must be evaluated based on the workload and requirements.

---

## 22. Another Important Concept: Concurrency

Don't confuse:

- Latency
- Throughput
- Concurrency

They are different.

### Latency

How long does one request take?

### Throughput

How many requests per second can we process?

### Concurrency

How many requests are being processed/in flight at the same time?

---

## 23. Example

Suppose:

```
Latency = 100 ms
Throughput = 1,000 RPS
```

A useful approximation from **Little's Law** is:

```
Concurrency ≈ Throughput × Latency
```

Convert latency:

```
100 ms = 0.1 sec
```

Therefore:

```
Concurrency ≈ 1,000 × 0.1

≈ 100
```

So approximately:

```
100 requests
```

would be in flight at steady state.

This is a simplified application of Little's Law:

```
L = λW
```

where:

```
L = average number of items in the system
λ = throughput
W = average time in system
```

---

## 24. Example with an API

Suppose:

```
Traffic = 5,000 RPS
Average latency = 200 ms
```

Convert:

```
200 ms = 0.2 sec
```

Then:

```
Concurrency ≈ 5,000 × 0.2

≈ 1,000
```

So approximately:

```
1,000 requests
```

are in flight.

This helps you reason about:

- thread pools
- connection pools
- memory
- server capacity
- concurrency limits

---

## 25. Throughput vs Capacity

These terms are related but shouldn't be treated as identical.

Suppose a server is capable of:

```
10,000 RPS
```

That's its approximate capacity under a particular workload and latency/SLO target.

But current traffic may be:

```
2,000 RPS
```

So:

```
Current throughput = 2,000 RPS
Capacity = 10,000 RPS
```

---

## 26. System Design Example

Imagine:

```
                    Users
                      |
                      ↓
                 Load Balancer
                      |
          ┌───────────┼───────────┐
          ↓           ↓           ↓
       Server 1    Server 2    Server 3
       2K RPS      2K RPS      2K RPS
          \           |           /
           \          |          /
                    DB
                  5K RPS
```

Application capacity:

```
2K × 3 = 6K RPS
```

But database capacity:

```
5K RPS
```

Therefore the database is the bottleneck.

Effective system throughput may be around:

```
5K RPS
```

depending on the workload and database operation mix.

---

## 27. What If We Add More API Servers?

Suppose:

```
10 API servers
```

Each:

```
2K RPS
```

API capacity:

```
20K RPS
```

But DB:

```
5K RPS
```

Now:

```
API = 20K
DB  = 5K
```

Adding more API servers doesn't solve the bottleneck.

You need to address the database:

- Caching
- Read replicas
- Partitioning/sharding
- Better indexes
- Query optimization
- Database scaling

depending on the workload.

---

## 28. Latency Optimization vs Throughput Optimization

They often require different approaches.

### To Improve Latency:

- Caching
- CDN
- Reduce network hops
- Connection reuse
- Database indexes
- Optimize queries
- Reduce payload size
- Parallelize independent operations

### To Improve Throughput:

- Horizontal scaling
- Load balancing
- Batching
- Asynchronous processing
- Queues
- Partitioning
- Sharding
- Connection pools
- Efficient algorithms
- Caching

Some techniques improve both, but not always.

---

## 29. Batching

Suppose you need to process:

```
1,000 database records
```

Option 1:

```
1 request → DB
1 request → DB
1 request → DB
...
1000 requests
```

Potentially inefficient.

Batch:

```
1000 records
      ↓
1 batch operation
      ↓
Database
```

Batching can improve throughput significantly, although it may increase the latency of an individual batch or introduce other trade-offs.

---

## 30. Asynchronous Processing

Suppose a user uploads a video.

Instead of:

```
Client
  ↓
API
  ↓
Process video
  ↓
Wait 30 seconds
  ↓
Response
```

you could:

```
Client
  ↓
API
  ↓
Queue
  ↓
Return 202 Accepted
```

Then:

```
Queue
  ↓
Workers
  ↓
Video processing
```

The initial API response has low latency, while the background processing system can be scaled for high throughput.

This is a classic system-design trade-off.

---

## 31. Synchronous vs Asynchronous

### Synchronous

```
Client
  ↓
API
  ↓
Service
  ↓
Database
  ↓
Response
```

The client waits.

**Latency matters heavily.**

### Asynchronous

```
Client
  ↓
API
  ↓
Queue
  ↓
202 Accepted

             ↓
           Worker
             ↓
          Database
```

The client doesn't wait for the entire operation.

**Throughput and eventual completion become more important.**

---

## 32. What Should You Mention in System Design Interviews?

When someone asks:

> "How will you scale this system?"

Don't just say:

> "Add more servers."

Think:

```
Traffic
  ↓
RPS
  ↓
Latency requirement
  ↓
Throughput requirement
  ↓
CPU / Memory
  ↓
Database capacity
  ↓
Network capacity
  ↓
Bottleneck
  ↓
Scaling strategy
```

---

## 33. Example Interview Scenario

Suppose the interviewer says:

> "Design an API that handles 100,000 requests per second."

Immediately think:

```
100K RPS
```

Then ask/clarify:

- What is the expected P95/P99 latency?
- Read/write ratio?
- Request size?
- Response size?
- Consistency requirement?
- Peak traffic?
- Geographic distribution?

For example:

```
100K RPS
P99 < 200 ms
90% reads
10% writes
```

Now you can start making architecture decisions.

---

## 34. Latency Budget

Another important system-design concept is the **latency budget**.

Suppose:

Requirement:

```
P99 < 300 ms
```

You might allocate approximately:

```
Network          50 ms
Load Balancer    10 ms
Application      50 ms
Cache/DB         100 ms
Other overhead   50 ms
----------------------
Total            260 ms
```

You have some remaining headroom.

This helps you reason about whether a design can meet an SLO.

---

## 35. Tail Latency

Suppose:

```
P50 = 20 ms
P95 = 40 ms
P99 = 80 ms
P99.9 = 500 ms
```

Most requests are fast.

But a small percentage are extremely slow.

That's called **tail latency**.

Tail latency becomes particularly important in distributed systems because one slow dependency can delay an entire user request.

---

## 36. Fan-Out Makes Latency Worse

Imagine:

```
API
 |
 ├── Service A
 ├── Service B
 ├── Service C
 └── Service D
```

Suppose all four calls happen in parallel:

```
A = 20 ms
B = 30 ms
C = 25 ms
D = 100 ms
```

The API may have to wait approximately:

```
max(20,30,25,100)

= 100 ms
```

plus overhead.

One slow dependency determines the response time.

If you have many dependencies, tail latency becomes especially important.

---

## 37. If Calls are Sequential

Much worse:

```
API
 ↓
A = 20 ms
 ↓
B = 30 ms
 ↓
C = 25 ms
 ↓
D = 100 ms
```

Total approximately:

```
20 + 30 + 25 + 100

= 175 ms
```

So, where business logic permits, parallelizing independent calls can reduce latency.

---

## 38. The Key System Design Relationship

Keep this diagram in your mind:

```
                 LOAD
                  |
                  ↓
             Requests/sec
                  |
                  ↓
             Server Capacity
                  |
          ┌───────┴───────┐
          ↓               ↓
      Throughput        Queueing
          |               |
          |               ↓
          |            Latency
          |               |
          └───────┬───────┘
                  ↓
              User SLO
```

When load approaches system capacity:

```
Utilization ↑
     ↓
Queueing ↑
     ↓
Latency ↑
```

Eventually:

```
Timeouts
Errors
Dropped requests
```

---

## 39. One Simple Way to Remember

### Latency

Think:

```
             REQUEST
                |
                ↓
        ┌─────────────┐
        │    SERVER   │
        └─────────────┘
                |
                ↓
             RESPONSE

        <---- 50 ms ---->
```

**How long?**

### Throughput

Think:

```
REQUEST REQUEST REQUEST REQUEST REQUEST
   ↓       ↓       ↓       ↓       ↓
             SERVER
               ↓
        1000 requests/sec
```

**How many?**

---

## 40. Final Interview-Ready Summary

| Concept | Meaning | Example |
|---|---|---|
| Latency | Time for an operation | 50 ms |
| Throughput | Work processed per unit time | 10K RPS |
| Concurrency | Work in flight at once | 500 requests |
| P50 | Median latency | 30 ms |
| P95 | 95th percentile | 80 ms |
| P99 | 99th percentile | 150 ms |
| Tail latency | Slowest portion of requests | P99/P99.9 |
| Capacity | Maximum sustainable workload under a target | 10K RPS |

### The Most Important Mental Model:

```
LATENCY
   ↓
"How long does one request take?"

THROUGHPUT
   ↓
"How much work can the system process?"

CONCURRENCY
   ↓
"How many requests are in flight?"

CAPACITY
   ↓
"How much load can the system sustainably handle?"
```

And for system design, always connect them:

```
100K RPS traffic
       ↓
Can my system sustain 100K RPS?
       ↓
At what P95/P99 latency?
       ↓
Where is the bottleneck?
       ↓
CPU / DB / Network / Cache / Queue?
       ↓
How do I scale it?
```

This is the foundation you'll need before moving into **Load Balancing**, **Caching**, **Database Scaling**, **CAP Theorem**, **Replication**, **Sharding**, **Message Queues**, and **Rate Limiting**.
