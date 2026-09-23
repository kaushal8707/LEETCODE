# CPU-Bound vs I/O-Bound

This is a very important concept for System Design and performance tuning because it helps you understand where your application is actually spending its time.

The simplest way to remember it:

```
CPU-bound → waiting for CPU computation
I/O-bound → waiting for external operations
```

---

## 1. What Does "Bound" Mean?

When we say an application is CPU-bound, the CPU is the limiting resource.

When we say an application is I/O-bound, I/O operations are the limiting factor.

Think of it as:

```
CPU-Bound
    ↓
CPU is busy doing work
    ↓
More CPU capacity can improve performance
```

```
I/O-Bound
    ↓
Application is waiting for I/O
    ↓
Faster I/O / concurrency / async processing can improve performance
```

---

## 2. CPU-Bound

A CPU-bound operation spends most of its time performing calculations.

Examples:

- Complex mathematical calculations
- Image/video processing
- Encryption/decryption
- Compression
- Machine learning inference
- Large sorting operations
- Parsing huge datasets
- Generating hashes

Example:

```java
long result = 0;

for (long i = 0; i < 10_000_000_000L; i++) {
    result += i;
}
```

The application is spending most of its time using the CPU.

```
Application
     |
     v
   CPU 🔥
     |
     v
Calculation
```

---

## 3. Real-Time CPU-Bound Example

Imagine an application that generates a PDF containing millions of complex calculations.

```
Request
   |
   v
Application
   |
   v
Calculate 10 million values
   |
   v
Generate PDF
   |
   v
Response
```

The CPU might be:

```
CPU Usage = 95%
```

while the application isn't waiting much on the database or network.

That's a **CPU-bound** workload.

---

## 4. Another CPU-Bound Example — Image Processing

Suppose your application receives:

```
10 MB image
```

and needs to resize it:

```
Original image
      |
      v
Decode
      |
      v
Resize
      |
      v
Compress
      |
      v
Save
```

A significant portion of the work can be CPU-intensive.

If CPU usage is already very high:

```
CPU
████████████████████ 95%
```

adding more application instances or CPU cores may help.

---

## 5. I/O-Bound

An I/O-bound operation spends a significant amount of time waiting for external systems or devices.

I/O can include:

- Database
- Network
- HTTP API
- Disk
- File system
- Object storage
- Message broker

Example:

```java
User user = userRepository.findById(id);
```

The application sends a query:

```
Application
    |
    | SQL
    v
 Database
    |
    | Waiting...
    |
    v
 Result
```

During the database wait, the CPU may not be doing much useful work for that request.

---

## 6. Real-Time I/O-Bound Example

Imagine an Order Service.

A request does:

```
Create Order
     |
     v
Call Payment Service
     |
     | Waiting
     v
Call Inventory Service
     |
     | Waiting
     v
Save to Database
     |
     | Waiting
     v
Return Response
```

The application may spend most of its time waiting for:

- Payment API
- Inventory API
- Database

That's an **I/O-bound** workload.

---

## 7. Simple Timeline

This makes the difference very clear.

### CPU-bound

```
Time →

CPU: █████████████████████████████
      Computing continuously
```

### I/O-bound

```
Time →

CPU: ███       ███       ███
         WAIT       WAIT
         I/O        I/O
```

For I/O-bound workloads, the application frequently spends time waiting.

---

## 8. Real-World Example: E-Commerce

Suppose you have:

```
GET /products/123
```

Your application does:

```
1. Validate request
2. Check Redis
3. Query database if cache miss
4. Call inventory service
5. Build response
```

Potentially:

```
Validation       → CPU
Redis            → I/O
Database         → I/O
Inventory API    → I/O
Response mapping → CPU
```

So a real application is often **mixed**.

```
CPU work
   +
I/O work
```

The important question is:

> **Which resource is the bottleneck?**

---

## 9. CPU-Bound vs I/O-Bound

| Feature | CPU-Bound | I/O-Bound |
|---|---|---|
| Bottleneck | CPU is the bottleneck | I/O/waiting is the bottleneck |
| Nature | Heavy computation | Heavy waiting |
| CPU utilization | Often high | May be relatively low |
| How to improve | More CPU can help | Concurrency/faster I/O can help |
| Type of work | CPU-intensive algorithms | DB/network/file operations |
| Example 1 | Image processing | Database queries |
| Example 2 | Encryption | HTTP calls |
| Example 3 | Compression | Reading files |

---

## 10. How Does This Affect Thread Pools?

This is extremely important in Java/Spring Boot.

Suppose you have:

```
100 requests
```

and each request makes a slow database call.

```
Thread 1 → DB → WAIT
Thread 2 → DB → WAIT
Thread 3 → DB → WAIT
...
Thread 100 → DB → WAIT
```

Your CPU might only be:

```
CPU Usage = 20%
```

but your threads may be heavily occupied waiting for I/O.

This is an **I/O-bound** workload.

---

## 11. CPU-Bound Thread Pool

For CPU-heavy tasks, having an enormous number of threads usually isn't useful.

Suppose your machine has:

```
8 CPU cores
```

and you have:

```
100 CPU-intensive threads
```

They compete for the same CPU resources.

```
8 CPU cores
     ↑
100 threads competing
```

This can cause:

- Context switching
- CPU contention
- Scheduling overhead

A CPU-bound workload often benefits from a thread pool sized around the available CPU parallelism, with the exact number depending on the workload and runtime.

Conceptually:

```
8 CPU cores
    ↓
roughly 8-way CPU parallelism
```

Not:

```
8 CPU cores
    ↓
1000 CPU-heavy threads
```

---

## 12. I/O-Bound Thread Pool

Now consider:

```
8 CPU cores
```

but your application spends most of its time waiting for:

- Database
- Network
- HTTP services

You may be able to have more concurrent tasks than CPU cores because many tasks are waiting rather than actively consuming CPU.

Conceptually:

```
Thread 1 → DB WAIT
Thread 2 → DB WAIT
Thread 3 → HTTP WAIT
Thread 4 → DB WAIT
Thread 5 → Processing
Thread 6 → DB WAIT
...
```

While some threads wait, others can use the CPU.

However, there is still a practical limit based on:

- Database connection pool
- Downstream service capacity
- Memory
- Network
- Thread overhead
- Request latency

So:

> **I/O-bound does not mean "create unlimited threads."**

---

## 13. Why Connection Pooling Matters

This connects directly to the topic you learned earlier.

Suppose:

```
1000 concurrent requests
```

but your database connection pool has:

```
20 connections
```

Then:

```
1000 Requests
      |
      v
Connection Pool
      |
      +---- 20 DB connections
      |
      v
Database
```

Many requests will wait for a connection.

So your bottleneck might be:

> **Database connection pool**

rather than CPU.

---

## 14. Why Caching Helps I/O-Bound Workloads

This connects to your previous topic.

Suppose:

```
1000 requests/sec
```

**Without caching:**

```
1000 requests
     |
     v
Database
```

**With caching:**

```
1000 requests
     |
     v
Redis
  /     \
HIT     MISS
 |        |
 v        v
Response Database
```

Suppose 900 requests are cache hits:

```
900 → Redis
100 → Database
```

Database I/O drops significantly.

Therefore:

```
Caching
   ↓
Less database I/O
   ↓
Lower latency
   ↓
Higher throughput
```

---

## 15. Synchronous vs Asynchronous I/O

I/O-bound workloads are where asynchronous programming can become valuable.

### Synchronous

```
Thread
  |
  v
Call Database
  |
  | WAIT
  | WAIT
  | WAIT
  v
Response
```

The thread remains associated with the operation while waiting, depending on the programming model.

### Asynchronous

Conceptually:

```
Request
   |
   v
Start I/O
   |
   v
Do other work
   |
   v
I/O completes
   |
   v
Continue processing
```

This can improve resource utilization for high-concurrency I/O workloads.

---

## 16. CPU-Bound Example in Java

Consider:

```java
public long calculate() {

    long result = 0;

    for (long i = 0; i < 1_000_000_000L; i++) {
        result += i * i;
    }

    return result;
}
```

The application is mostly:

```
Loop
 ↓
Arithmetic
 ↓
Arithmetic
 ↓
Arithmetic
 ↓
Arithmetic
```

CPU is doing the work.

Therefore:

> **CPU-Bound**

---

## 17. I/O-Bound Example in Java

Consider:

```java
public User getUser(Long id) {

    return userRepository.findById(id)
            .orElseThrow();
}
```

Conceptually:

```
Java Application
      |
      | SQL
      v
Database
      |
      | WAIT
      |
      v
Result
```

The application spends significant time waiting for the database.

Therefore:

> **I/O-Bound**

---

## 18. Mixed Workload

Most production applications are not purely CPU-bound or I/O-bound.

For example:

```
HTTP Request
     |
     v
Validate JSON          → CPU
     |
     v
Redis                  → I/O
     |
     v
Database               → I/O
     |
     v
Calculate discount     → CPU
     |
     v
Call Payment API       → I/O
     |
     v
Serialize JSON         → CPU
     |
     v
Response
```

So you might have:

```
CPU
 ├── Validation
 ├── Business logic
 └── Serialization

I/O
 ├── Redis
 ├── Database
 └── Payment API
```

The overall system may be I/O-dominant even though it contains CPU work.

---

## 19. How to Identify CPU vs I/O Bottleneck

In production, don't guess.

Look at metrics.

### CPU-bound indicators

```
CPU utilization → High
CPU saturation → High
Run queue → High
Database latency → Normal
Network wait → Low
```

Example:

```
CPU:        95%
Memory:     60%
DB latency: 10 ms
Network:    Normal
```

Likely:

> **CPU-bound.**

### I/O-bound indicators

```
CPU utilization → Low/Moderate
Database latency → High
Network latency → High
Threads waiting → High
Connection pool utilization → High
```

Example:

```
CPU:        25%
Memory:     50%
DB latency: 500 ms
Threads:    Many waiting
```

Likely:

> **I/O-bound.**

---

## 20. How Do You Scale Them?

This is very important in System Design interviews.

### CPU-bound

Possible solutions:

```
Optimize algorithm
        ↓
Reduce computation
        ↓
Use more CPU cores
        ↓
Horizontal scaling
        ↓
Parallel processing
```

Example:

```
1 CPU-heavy server
       ↓
4 CPU-heavy servers
```

### I/O-bound

Possible solutions:

- Caching
- Connection pooling
- Async processing
- Parallel I/O
- Batching
- Reduce network calls
- Optimize DB queries
- Read replicas
- Horizontal scaling

For example:

```
Slow DB query
     ↓
Optimize query
     ↓
Add index
     ↓
Cache result
     ↓
Reduce DB calls
```

---

## 21. System Design Example

Suppose you design:

> **Food Delivery System**

Request:

```
GET /restaurants/nearby
```

The application:

```
1. Find user's location
2. Query restaurant database
3. Calculate distance
4. Sort restaurants
5. Return results
```

Potential bottlenecks:

```
Database query       → I/O
Distance calculation → CPU
Sorting              → CPU
Network              → I/O
```

If there are millions of restaurants and distance calculations dominate:

> **CPU-bound**

If the database query takes 500 ms:

> **I/O-bound**

The solution depends on the bottleneck.

---

## 22. Very Important Interview Scenario

**Interviewer:**

> "Our API has 80% CPU utilization and response time is high. What would you investigate?"

Don't immediately say:

> "Add more servers."

First determine:

> **Why is CPU high?**

Maybe:

- Bad algorithm
- Infinite/repeated loops
- Expensive serialization
- Encryption
- Compression
- Large JSON processing
- Excessive garbage collection

Then optimize or scale appropriately.

---

## 23. Another Interview Scenario

**Interviewer:**

> "CPU is only 20%, but response time is 2 seconds. Why?"

Possible answer:

> Application is waiting for I/O.

Investigate:

- Database latency
- External API latency
- Redis latency
- Network latency
- Connection pool exhaustion
- Thread pool exhaustion
- Disk I/O

For example:

```
API
 |
 | 50 ms
 v
Redis
 |
 | 1.5 sec
 v
Database
 |
 | 100 ms
 v
Response
```

CPU may remain low while users experience high latency.

---

## 24. Connection to Everything You've Learned

You can now connect the concepts:

```
                         Users
                           |
                           v
                  Reverse Proxy / LB
                           |
                           v
                    App Instances
                           |
                +----------+----------+
                |                     |
                v                     v
              Cache                Database
                |                     |
              I/O                   I/O
                |
                v
             Response
```

Inside the application:

```
CPU Work
   ↓
Business Logic
   ↓
Serialization
   ↓
Computation
```

and:

```
I/O Work
   ↓
Redis
Database
HTTP APIs
Files
Message Brokers
```

---

## 25. The Mental Model

Remember this:

```
CPU-BOUND
==========

"Give me more computing power."

Application
     |
     v
    CPU 🔥
     |
     v
Calculations
```

```
I/O-BOUND
=========

"I'm waiting for something else."

Application
     |
     v
Database / Network / Redis
     |
     | WAIT...
     |
     v
Response
```

And the most important System Design principle:

> **Before scaling, identify the bottleneck.**

```
High CPU?
    ↓
CPU-bound?
    ↓
Optimize CPU work / scale CPU

Low CPU + High latency?
    ↓
I/O-bound?
    ↓
Investigate DB / network / external services /
connection pools / caching
```
