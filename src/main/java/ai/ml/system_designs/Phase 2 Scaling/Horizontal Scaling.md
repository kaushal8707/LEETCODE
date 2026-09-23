# Horizontal Scaling

Since we're building your System Design fundamentals from scratch, Horizontal Scaling is the natural next concept after Vertical Scaling.

---

## 1. What is Horizontal Scaling?

Horizontal Scaling = **Scale Out**

Instead of making one server bigger, we add more servers to handle increased traffic.

### Example

Initially:

```
              Users
                |
                v
          +-----------+
          |  Server 1 |
          |  4 CPU    |
          |  8 GB RAM |
          +-----------+
```

Traffic increases.

Instead of upgrading Server 1 to 32 CPU / 64 GB RAM, we add more servers:

```
                 Users
                   |
                   v
             Load Balancer
              /     |     \
             /      |      \
            v       v       v
       +--------+ +--------+ +--------+
       |Server 1| |Server 2| |Server 3|
       | 4 CPU  | | 4 CPU  | | 4 CPU  |
       | 8 GB   | | 8 GB   | | 8 GB   |
       +--------+ +--------+ +--------+
```

This is **Horizontal Scaling**.

> Horizontal scaling means increasing system capacity by adding more machines/instances.

---

## 2. Scale Up vs Scale Out

This is one of the most important System Design concepts.

### Vertical Scaling — Scale Up

```
             Server
        +-------------+
        |  4 CPU      |
        |  8 GB RAM   |
        +-------------+

              ↓

        +-------------+
        |  32 CPU     |
        |  128 GB RAM |
        +-------------+
```

Make one server bigger.

### Horizontal Scaling — Scale Out

```
        +--------+
        |Server 1|
        +--------+

              ↓

        +--------+  +--------+  +--------+
        |Server 1|  |Server 2|  |Server 3|
        +--------+  +--------+  +--------+
```

Add more servers.

---

## 3. Why Do We Need Horizontal Scaling?

Imagine your application receives:

```
1,000 requests/sec
```

One server can handle it:

```
              1,000 req/sec
                    |
                    v
               +---------+
               | Server  |
               +---------+
```

Now traffic becomes:

```
10,000 requests/sec
```

One server might not be able to handle it.

Instead:

```
              10,000 req/sec
                    |
                    v
              Load Balancer
              /     |     \
             /      |      \
            v       v       v
         Server1 Server2 Server3
         3,300    3,300    3,400
         req/s    req/s    req/s
```

The workload is distributed across multiple servers.

---

## 4. Load Balancer

Horizontal scaling almost always introduces an important component:

> **Load Balancer**

Its job is to distribute incoming requests among available servers.

```
                    Users
                      |
                      v
              +---------------+
              | Load Balancer |
              +---------------+
                /      |      \
               /       |       \
              v        v        v
          Server 1 Server 2 Server 3
```

For example:

```
Request 1 → Server 1
Request 2 → Server 2
Request 3 → Server 3
Request 4 → Server 1
Request 5 → Server 2
```

The exact distribution depends on the load-balancing algorithm and server health.

---

## 5. Real-World Example

Suppose you have a Spring Boot application:

```
                  Client
                    |
                    v
              Load Balancer
               /          \
              v            v
       Spring Boot      Spring Boot
        Instance 1       Instance 2
```

Both instances run the same application.

If traffic increases:

```
                  Client
                    |
                    v
              Load Balancer
            /      |       \
           v       v        v
        App-1    App-2    App-3
```

You have horizontally scaled your application.

---

## 6. Horizontal Scaling and Stateless Services

This is extremely important.

Suppose your application stores user session information inside the server's memory.

```
User
 |
 v
Server 1
 |
 +-- Session = user123
```

Then the next request goes to Server 2:

```
User
 |
 v
Server 2
 |
 +-- ❌ Session not found
```

This creates a problem.

For horizontal scaling, we generally prefer **stateless application servers**.

Instead of:

```
Server 1
   |
   +-- User Session
```

we can store shared state externally:

```
                  Load Balancer
                 /      |      \
                v       v       v
             App 1   App 2   App 3
                \       |      /
                 \      |     /
                  v     v    v
                 +-----------+
                 |   Redis   |
                 +-----------+
```

Or use a database/shared storage depending on the use case.

This connects directly to the Stateless vs Stateful Services topic you were studying.

---

## 7. Horizontal Scaling Improves Availability

Suppose you have only one server:

```
             Users
               |
               v
           +--------+
           |Server 1|
           +--------+
```

Server 1 crashes:

```
             Users
               |
               X
           Server 1
             DOWN
```

Your application is unavailable.

With multiple servers:

```
              Load Balancer
              /     |     \
             v      v      v
         Server 1 Server 2 Server 3
            ❌       ✅       ✅
```

If Server 1 crashes:

```
              Load Balancer
                 /      \
                v        v
            Server 2  Server 3
               ✅        ✅
```

The load balancer can stop sending traffic to the unhealthy instance.

Therefore:

> **Horizontal scaling can improve both scalability and availability.**

---

## 8. Auto Scaling

Modern systems don't necessarily require us to manually add servers.

Suppose:

```
CPU < 50%
```

We might run:

```
2 instances
```

Traffic increases:

```
CPU > 70%
```

Auto-scaling adds another instance:

```
2 → 3
```

Traffic increases further:

```
CPU > 70%
```

Then:

```
3 → 4
```

Architecture:

```
                    Users
                      |
                      v
                Load Balancer
                      |
          +-----------+-----------+
          |           |           |
          v           v           v
       App 1       App 2       App 3
                                  |
                              Auto Scaling
                                  |
                                  v
                               App 4
```

When traffic decreases, instances can also be removed.

```
4 → 3 → 2
```

This is often called **elastic scaling**.

---

## 9. Horizontal Scaling Doesn't Mean Only Application Servers

You can horizontally scale different components.

### Application layer

```
App 1
App 2
App 3
App 4
```

### Database

Depending on the database and workload:

```
Primary
   |
   +---- Read Replica 1
   |
   +---- Read Replica 2
```

### Kafka consumers

For example:

```
Kafka Topic
     |
     +---- Consumer 1
     +---- Consumer 2
     +---- Consumer 3
```

Consumers can process partitions in parallel.

### Microservices

You can have:

```
Order Service
    ├── Instance 1
    ├── Instance 2
    └── Instance 3

Payment Service
    ├── Instance 1
    └── Instance 2
```

Each service can scale independently.

---

## 10. Horizontal Scaling and Microservices

Imagine your application has:

```
Order Service
Payment Service
Notification Service
User Service
```

Initially:

```
Order       → 2 instances
Payment     → 2 instances
Notification→ 2 instances
User        → 2 instances
```

Suppose orders suddenly increase significantly.

You don't necessarily need to scale everything.

You can scale only Order Service:

```
Order Service
    |
    +-- Instance 1
    +-- Instance 2
    +-- Instance 3
    +-- Instance 4
    +-- Instance 5
```

while keeping:

```
Payment Service      → 2
Notification Service → 2
User Service         → 2
```

This is one of the major advantages of horizontally scalable architectures.

---

## 11. The Big Challenge: Shared State

Horizontal scaling becomes difficult when servers depend on local state.

**Bad design:**

```
             Load Balancer
              /         \
             v           v
          Server 1     Server 2
             |            |
         Local State   Local State
```

Now Server 1 and Server 2 don't necessarily have the same state.

**Better:**

```
             Load Balancer
              /         \
             v           v
          Server 1     Server 2
              \         /
               \       /
                 Redis
                   |
                   v
                Database
```

Shared state is moved outside the application instances.

---

## 12. Horizontal Scaling and Database

Here's an important interview point:

> **Adding application servers doesn't automatically solve database bottlenecks.**

For example:

```
                    Load Balancer
                  /      |      \
                 v       v       v
               App1    App2    App3
                 \       |      /
                  \      |     /
                   v     v    v
                    Database
```

You added 10 application servers:

```
App1 ... App10
```

But they all send requests to one database:

```
                 Database
                    ↑
        +-----------+-----------+
        |     |     |     |     |
       App1  App2  App3  ... App10
```

The database may now become the bottleneck.

So horizontal scaling usually requires thinking about the entire system, not just the application layer.

---

## 13. Advantages of Horizontal Scaling

### 1. Very high scalability

You can go from:

```
2 servers
 ↓
10 servers
 ↓
100 servers
 ↓
1000 servers
```

subject to architecture, infrastructure, and workload constraints.

### 2. Better fault tolerance

If one server fails:

```
Server 1 ❌

Server 2 ✅
Server 3 ✅
```

the system can continue serving traffic.

### 3. Better availability

Multiple instances can be distributed across:

- Availability Zones
- Regions
- Data Centers

This reduces the impact of infrastructure failures.

### 4. Elasticity

You can dynamically increase or decrease the number of instances based on demand.

```
Low Traffic
   ↓
2 servers

High Traffic
   ↓
10 servers

Low Traffic
   ↓
2 servers
```

---

## 14. Disadvantages

Horizontal scaling introduces complexity.

You may need:

- Load Balancer
- Service Discovery
- Health Checks
- Distributed Caching
- Distributed Sessions
- Monitoring
- Logging
- Tracing
- Data Replication
- Deployment Strategies

And distributed systems introduce problems such as:

- Network failures
- Partial failures
- Data consistency
- Race conditions
- Duplicate requests
- Distributed transactions

This is why horizontal scaling is powerful but more complex.

---

## 15. Vertical vs Horizontal Scaling

| Feature | Vertical | Horizontal |
|---|---|---|
| Other name | Scale Up | Scale Out |
| Approach | Bigger machine | More machines |
| Complexity | Low | Higher |
| Scalability | Limited | Very high |
| Fault tolerance | Lower | Higher |
| SPOF | Possible | Can reduce SPOF |
| Load Balancer | Usually not required | Usually required |
| Stateless architecture | Not necessarily required | Highly beneficial |
| Auto scaling | Limited | Very common |
| Microservices | Less flexible | Very useful |
| Database | Often useful | Possible, but more complex |

---

## 16. Simple Real-World Analogy

Imagine a supermarket.

### Vertical Scaling

You have one checkout counter:

```
Customers
   |
   v
+-----------+
| Checkout  |
+-----------+
```

You make that checkout counter faster.

```
1 cashier
   ↓
2 cashiers at the same counter
```

You're increasing the capacity of the existing unit.

### Horizontal Scaling

You open more checkout counters:

```
Customers
   |
   v
+-----------------------+
| Checkout 1            |
| Checkout 2            |
| Checkout 3            |
| Checkout 4            |
+-----------------------+
```

Customers are distributed across them.

That's horizontal scaling.

---

## 17. Interview Scenario

Suppose the interviewer asks:

> "Our Spring Boot application currently runs on one server. Traffic has increased 10×. What would you do?"

A strong answer would be:

```
1. Identify the bottleneck
       ↓
2. If one server is insufficient,
   create multiple application instances
       ↓
3. Put a Load Balancer in front
       ↓
4. Make application instances stateless
       ↓
5. Move shared state to Redis/DB/etc.
       ↓
6. Add health checks
       ↓
7. Configure auto-scaling
       ↓
8. Monitor CPU, memory, latency,
   throughput and error rate
       ↓
9. Check database bottlenecks separately
```

Architecture:

```
                         Users
                           |
                           v
                    +-------------+
                    |Load Balancer|
                    +-------------+
                     /     |     \
                    /      |      \
                   v       v       v
               +------+ +------+ +------+
               | App1 | | App2 | | App3 |
               +------+ +------+ +------+
                   \       |       /
                    \      |      /
                     v     v     v
                   +-----------+
                   |   Redis   |
                   +-----------+
                         |
                         v
                    +---------+
                    |    DB   |
                    +---------+
```

---

## 18. One-Line Memory Trick

Remember these two together:

```
VERTICAL SCALING
       ↓
Make the machine BIGGER
       ↓
Scale UP
```

```
HORIZONTAL SCALING
       ↓
Add MORE machines
       ↓
Scale OUT
```
