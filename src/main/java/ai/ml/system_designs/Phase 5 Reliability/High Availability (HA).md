# High Availability (HA)

Since you're building your System Design fundamentals from scratch, High Availability is the natural next topic after Fault Tolerance.

---

## 1. What is High Availability?

High Availability means designing a system so that it remains accessible and operational for a very high percentage of the time.

**In simple terms:**

> The system should be available to users even when some components fail.

For example, an e-commerce application should continue accepting orders even if one application server or one database node fails.

---

## 2. Availability Percentage

Availability is usually expressed as a percentage.

```
Availability = Uptime / Total Time × 100
```

For example:

| Availability | Approx. downtime/year |
|---|---|
| 99% | 3.65 days |
| 99.9% | 8.76 hours |
| 99.99% | 52.6 minutes |
| 99.999% | 5.26 minutes |
| 99.9999% | 31.5 seconds |

This is often called the **number of nines**.

### Example

If someone says:

> "Our service has four nines availability."

That means:

```
99.99%
```

Approximately:

```
52 minutes 35 seconds
```

of downtime per year.

---

## 3. High Availability vs Fault Tolerance

They are closely related.

### Fault Tolerance

Asks:

> Can the system continue functioning when something fails?

### High Availability

Asks:

> Can users continue accessing the system with minimal downtime?

Example:

```
                 Load Balancer
                 /           \
                /             \
           Server 1          Server 2
             ❌                 ✅
```

Server 1 fails.

Server 2 continues serving requests.

This gives us fault tolerance, which helps us achieve high availability.

So:

```
Fault Tolerance
      |
      v
Failure handling
      |
      v
High Availability
      |
      v
Minimal service downtime
```

---

## 4. Single Point of Failure

The biggest enemy of High Availability is the **Single Point of Failure (SPOF)**.

Consider:

```
Users
  |
Load Balancer
  |
App Server
  |
Database
```

Suppose there is only one database.

```
Database ❌
    |
    v
Application ❌
    |
    v
Users ❌
```

The database is a single point of failure.

---

## 5. Remove the Single Point of Failure

Instead:

```
                 Load Balancer
                /             \
               /               \
          App Server 1      App Server 2
               \               /
                \             /
                 Database
```

Now if App Server 1 fails:

```
App Server 1 ❌

App Server 2 ✅
```

Users can continue using the system.

But we still have a problem:

```
              App Servers
                   |
                   v
              Single DB ❌
```

So the database also needs redundancy.

---

## 6. Highly Available Architecture

A more realistic design:

```
                         Users
                           |
                           v
                    Global Load Balancer
                           |
             +-------------+-------------+
             |                           |
            AZ-1                        AZ-2
             |                           |
       +-----+-----+               +-----+-----+
       |           |               |           |
     App 1       App 2           App 3       App 4
       |           |               |           |
       +-----------+---------------+-----------+
                           |
                      DB Cluster
                       /      \
                      /        \
                Primary       Replica
```

Now we have redundancy at multiple levels.

---

## 7. Load Balancing

Load balancing is one of the fundamental building blocks of HA.

**Without load balancing:**

```
Users
  |
  v
Server 1
```

If Server 1 fails:

```
Server 1 ❌
    |
    v
System DOWN
```

**With multiple servers:**

```
              Load Balancer
             /      |      \
            /       |       \
        Server 1  Server 2  Server 3
```

If Server 2 fails:

```
Server 1 ✅
Server 2 ❌
Server 3 ✅
```

The load balancer routes traffic to healthy servers.

---

## 8. Health Checks

The load balancer needs to know:

> "Is this server healthy?"

For example:

```
GET /health
```

Response:

```
HTTP 200 OK
```

If the server stops responding:

```
Server 2
   |
   X
Health check failed
```

The load balancer removes it from rotation:

```
Server 1 ✅  ← Traffic
Server 2 ❌  ← No traffic
Server 3 ✅  ← Traffic
```

---

## 9. Database High Availability

The database is usually one of the most critical HA components.

A common architecture is:

```
             Application
                  |
                  v
             DB Primary
              /       \
             /         \
            v           v
        Replica 1    Replica 2
```

If Primary fails:

```
Primary ❌
   |
   v
Replica 1 promoted
   |
   v
New Primary
```

This is called **database failover**.

---

## 10. Active-Passive vs Active-Active

This is an important interview topic.

### Active-Passive

```
          Primary
           ACTIVE
              |
              |
          Replica
          PASSIVE
```

Normally:

```
Traffic → Primary
```

If Primary fails:

```
Primary ❌
   |
   v
Replica becomes ACTIVE
```

**Advantages**

- Easier to implement
- Easier consistency model

**Disadvantage**

- The passive node may sit mostly unused.

---

## 11. Active-Active

Both nodes actively serve traffic.

```
             Load Balancer
              /          \
             /            \
          DB-1           DB-2
         ACTIVE          ACTIVE
```

Traffic can be distributed between both.

**Advantages**

- Better resource utilization
- Can provide very high availability
- Can handle more traffic

**Challenges**

- Data consistency
- Conflict resolution
- Replication
- Failover complexity

---

## 12. Multi-AZ Architecture

Running everything in one Availability Zone is risky.

**Bad:**

```
Region
 |
 +--- AZ-1
       |
       +--- App 1
       +--- App 2
       +--- DB
```

If AZ-1 has an infrastructure failure:

```
AZ-1 ❌

Entire application ❌
```

**Better:**

```
              Load Balancer
                /        \
               /          \
             AZ-1         AZ-2
              |             |
           App 1          App 2
           App 2          App 3
              |             |
              +------+------+
                     |
                 DB Cluster
```

If AZ-1 fails:

```
AZ-1 ❌

AZ-2 ✅
```

The application can continue serving requests.

---

## 13. Multi-Region High Availability

For extremely critical systems, we can distribute infrastructure across regions.

```
                    Users
                      |
                Global DNS/LB
                  /       \
                 /         \
          Region A         Region B
             |                |
          App Servers      App Servers
             |                |
          Database         Database
```

If Region A fails:

```
Region A ❌

      ↓

Traffic → Region B
```

This protects against large-scale regional failures.

---

## 14. Stateless Application Servers

Stateless services make HA much easier.

Suppose:

```
Server 1
Server 2
Server 3
```

Any server can process any request.

```
Request 1 → Server 1
Request 2 → Server 3
Request 3 → Server 2
```

If Server 2 dies:

```
Server 2 ❌

Requests → Server 1 / Server 3
```

No user-specific state is lost from the application server itself.

Session/state can be stored externally:

```
App Servers
     |
     +---- Redis
     |
     +---- Database
```

---

## 15. Caching and HA

Suppose:

```
Application
    |
   Redis
```

Redis fails.

If your application absolutely depends on Redis:

```
Redis ❌
   |
   v
Application ❌
```

That's bad HA design.

**Instead:**

```
Application
    |
   Redis
    |
    X
    |
Database fallback
```

The application can continue working, perhaps with reduced performance.

> This is also **graceful degradation**.

---

## 16. Queues Improve Availability

**Synchronous architecture:**

```
Order Service
      |
      v
Email Service
```

If Email Service is down:

```
Order Service
      |
      v
Email Service ❌
```

This can cause order processing to fail if email is a mandatory synchronous dependency.

**Instead:**

```
Order Service
      |
      v
Kafka
      |
      v
Email Service
```

Now:

```
Email Service ❌
      |
      v
Kafka stores messages
      |
      v
Email Service recovers
      |
      v
Messages processed
```

This increases resilience and allows services to remain available independently.

---

## 17. Retry + Timeout

Suppose:

```
Order Service
      |
      v
Payment Service
```

Payment temporarily fails.

Use:

```
Timeout
   +
Retry
   +
Exponential Backoff
```

Example:

```
Attempt 1 → Timeout
             ↓
          wait 100ms

Attempt 2 → Timeout
             ↓
          wait 200ms

Attempt 3 → Success
```

But retries should not continue forever.

That's where the **Circuit Breaker** comes in.

---

## 18. Circuit Breaker

```
Order Service
      |
      v
Circuit Breaker
      |
      v
Payment Service
```

If Payment continuously fails:

```
Payment ❌
Payment ❌
Payment ❌
Payment ❌
```

Circuit opens:

```
Order
  |
  v
Circuit OPEN
  |
  X
Payment
```

This prevents cascading failures and protects overall availability.

---

## 19. Cascading Failure

This is a very important system-design concept.

Imagine:

```
Service A
   |
   v
Service B
   |
   v
Service C
```

Service C becomes slow.

Then:

```
C slow
 ↓
B waits for C
 ↓
B becomes slow
 ↓
A waits for B
 ↓
A becomes slow
 ↓
Users experience outage
```

This is a **cascading failure**.

Techniques to prevent it include:

- Timeouts
- Circuit breakers
- Bulkheads
- Rate limiting
- Backpressure
- Load shedding
- Queues

---

## 20. Rate Limiting

Suppose your application normally handles:

```
10,000 requests/sec
```

Suddenly:

```
100,000 requests/sec
```

arrive.

**Without protection:**

```
Traffic spike
     ↓
CPU 100%
     ↓
Memory exhaustion
     ↓
Servers crash
     ↓
Service unavailable
```

Rate limiting can protect the system:

```
Client
  |
Rate Limiter
  |
  +--- Allowed → Application
  |
  +--- Rejected → 429 Too Many Requests
```

This protects availability.

---

## 21. Autoscaling

Another important HA technique.

Suppose:

```
Normal traffic
    |
    v
3 servers
```

Traffic increases:

```
High traffic
    |
    v
3 servers → 6 servers
```

Traffic decreases:

```
Low traffic
    |
    v
6 servers → 3 servers
```

Autoscaling helps prevent overload.

---

## 22. Deployment and High Availability

Even your deployment strategy can affect availability.

### Bad deployment

```
Stop all servers
      ↓
Deploy
      ↓
Start servers
```

This creates downtime.

### Rolling deployment

```
Server 1 → Deploy
Server 2 → Serving
Server 3 → Serving

Server 1 → Serving
Server 2 → Deploy
Server 3 → Serving
```

Users continue getting service.

Other approaches include:

- Blue/Green deployment
- Canary deployment
- Rolling deployment

---

## 23. Availability Calculation

For a single component:

```
Availability = MTBF / (MTBF + MTTR)
```

Where:

- **MTBF** = Mean Time Between Failures
- **MTTR** = Mean Time To Repair/Recover

Example:

```
MTBF = 1000 hours
MTTR = 1 hour
```

Then:

```
Availability
= 1000 / (1000 + 1)
≈ 99.90%
```

> Reducing MTTR improves availability.

---

## 24. Redundancy and Availability

Suppose one server has:

```
Availability = 99%
```

Two independent servers in active-active configuration can have combined availability:

```
1 - (1 - 0.99)²

= 1 - 0.0001

= 99.99%
```

This illustrates why redundancy is powerful.

> **But:** real systems aren't perfectly independent. Shared dependencies such as networking, power, storage, DNS, or a common control plane can become correlated failure points.

---

## 25. High Availability Architecture Example

Let's design a highly available e-commerce system:

```
                         Users
                           |
                           v
                    Global DNS / LB
                     /           \
                    /             \
                Region A       Region B
                  /                \
               LB                   LB
             /   \                /   \
           App1  App2           App3  App4
             \   /                \   /
              \ /                  \ /
             Cache                 Cache
                \                  /
                 +-------+---------+
                         |
                       Kafka
                         |
                Database Cluster
                 /            \
             Primary         Replica
```

Now consider failures.

### App server fails

```
App1 ❌
```

Load balancer routes to App2.

### Cache fails

Application falls back to database.

### Kafka broker fails

Kafka replication allows another broker to serve the partition.

### Database primary fails

Replica can be promoted.

### Availability Zone fails

Traffic shifts to another AZ.

### Region fails

Global routing sends traffic to another region.

This is the essence of High Availability architecture.

---

## 26. HA Does Not Mean "Nothing Ever Fails"

This is an important distinction.

A highly available system can still experience component failures.

The goal is:

```
Component failure
       ↓
System detects failure
       ↓
System isolates failure
       ↓
Failover / recovery
       ↓
Users continue receiving service
```

So:

> **High Availability doesn't eliminate failures. It minimizes the impact of failures on users.**

---

## 27. Fault Tolerance vs High Availability vs Disaster Recovery

Remember this distinction:

| Concept | Main Goal |
|---|---|
| Fault Tolerance | Continue operating despite component failures |
| High Availability | Minimize downtime |
| Disaster Recovery | Recover from major disasters |
| Backup | Restore lost/corrupted data |
| Replication | Maintain redundant copies |
| Failover | Switch to a healthy component |

A good architecture often uses all of them.

---

## 28. Interview Answer

If an interviewer asks:

> "What is High Availability?"

You can answer:

> High Availability is the ability of a system to remain accessible and operational for a very high percentage of time, even when individual components fail. We achieve HA using redundancy, load balancing, health checks, replication, automatic failover, multi-AZ or multi-region deployment, stateless services, caching, queues, timeouts, circuit breakers, rate limiting, and resilient deployment strategies. The key objective is to eliminate single points of failure and minimize downtime.

---

## 29. HA Mental Model

For system-design interviews, remember:

```
                    HIGH AVAILABILITY
                           |
          +----------------+----------------+
          |                |                |
      Redundancy        Detection        Recovery
          |                |                |
    Multiple servers   Health checks     Failover
    DB replicas       Monitoring         Retries
    Multi-AZ                              Backups
    Multi-region
          |
          +----------------+----------------+
                           |
                      RESILIENCE
                           |
       +-------------------+-------------------+
       |                   |                   |
   Load Balancer       Circuit Breaker      Bulkhead
   Rate Limiting       Timeout              Queue
   Autoscaling         Backoff              DLQ
```

