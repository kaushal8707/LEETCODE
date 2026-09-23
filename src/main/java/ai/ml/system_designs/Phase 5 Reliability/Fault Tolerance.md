# Fault Tolerance

Since you're learning System Design from scratch, Fault Tolerance is one of the most important concepts to understand before moving into high availability, replication, distributed systems, Kafka, and microservices.

---

## 1. What is Fault Tolerance?

Fault tolerance is the ability of a system to continue working correctly even when one or more components fail.

**In simple words:**

> A component can fail, but the overall system should continue providing service.

### Real-world example

Imagine an e-commerce system:

```
                 Users
                   |
              Load Balancer
             /      |      \
            /       |       \
        Server 1  Server 2  Server 3
           |         |         |
           +---------+---------+
                     |
                  Database
```

Suppose **Server 2 crashes**.

A fault-tolerant system does:

```
User
  |
Load Balancer
  |
  +---- Server 1  ✅
  |
  +---- Server 2  ❌ FAILED
  |
  +---- Server 3  ✅
```

Users can still access the application through Server 1 or Server 3.

---

## 2. Why Fault Tolerance Is Important

Distributed systems have many components:

```
Client
   |
Load Balancer
   |
API Service
   |
Cache
   |
Database
   |
Kafka
   |
Downstream Services
```

Every component can fail.

For example:

- Server crashes
- Database becomes unavailable
- Network fails
- Disk fails
- Kafka broker crashes
- Cache goes down
- External API becomes unavailable
- CPU reaches 100%
- Memory is exhausted
- Deployment introduces a bug
- Availability zone becomes unavailable

Therefore:

> ** Failure is not an exceptional event in distributed systems. Failure is expected. **

Good system design assumes failures will happen and designs around them.

---

## 3. Fault Tolerance vs High Availability

These concepts are related but not identical.

### Fault Tolerance

** Focus: **

> Can the system continue operating when something fails ?

### High Availability

** Focus: **

> Is the system available to users most of the time ?

For example :
```
Server A ❌
    |
    v
Server B ✅
    |
    v
Application continues
```

This provides both fault tolerance and high availability.

---

## 4. Single Point of Failure

One of the most important concepts.

A ** Single Point of Failure (SPOF) ** is a component whose failure can bring down the entire system.

### Bad architecture

```
             Users
                |
          Load Balancer
                |
          Application
                |
          Single DB
                |
             Storage
```

If the database fails:

```
Database ❌
    |
    v
Application cannot work
    |
    v
System DOWN ❌
```

The database is a single point of failure.

---

## 5. Removing Single Points of Failure

We can introduce redundancy.

```
                 Users
                   |
             Load Balancer
              /           \
             /             \
        Server 1         Server 2
             \             /
              \           /
               DB Primary
                    |
               DB Replica
```

If Server 1 fails:

```
Server 1 ❌

Server 2 ✅
   |
   v
Users continue
```

If the primary database fails:

```
Primary DB ❌
     |
     v
Replica DB promoted
     |
     v
System continues
```

---

## 6. Redundancy

Redundancy is one of the fundamental techniques for fault tolerance.

Instead of having one component:

```
Server
```

have multiple:

```
Server 1
Server 2
Server 3
```

If one fails:

```
Server 1 ❌
Server 2 ✅
Server 3 ✅
```

The system continues operating.

### Types of redundancy

** Hardware redundancy **

```
Disk 1
Disk 2
Disk 3
```

** Server redundancy **

```
App 1
App 2
App 3
```

** Database redundancy **

```
Primary
   |
Replica 1
Replica 2
```

** Network redundancy **

```
Network A
Network B
```

** Geographic redundancy **

```
Mumbai Region
      |
      +--- DC1
      +--- DC2

Hyderabad Region
      |
      +--- DC1
      +--- DC2
```

## 7. Failover

Failover means switching from a failed component to a healthy backup component.

Example:

```
             Application
                  |
              DB Primary
                  |
               ❌ DOWN
                  |
                  v
             DB Replica
                  |
                 ✅
```

The replica becomes the new primary.

This is called **failover**.

### Types

**Automatic failover**

```
Primary ❌
   |
   v
System detects failure
   |
   v
Replica promoted
```

**Manual failover**

An administrator performs the switch.

> Automatic failover is generally preferred for highly available systems.

---

## 8. Replication

Replication means keeping multiple copies of data.

Example:

```
              Primary DB
             /          \
            /            \
       Replica 1      Replica 2
```

If the primary fails:

```
Primary ❌

Replica 1 ✅
Replica 2 ✅
```

One replica can be promoted.

Replication is commonly used for:

- Databases
- Kafka
- Distributed storage
- Caches
- Search systems

## 9. Health Checks

How does the system know that a server has failed?

Through **health checks**.

Example:

```
Load Balancer
     |
     +---- Server 1
     |
     +---- Server 2
     |
     +---- Server 3
```

The load balancer periodically asks:

```
GET /health
```

Server responds:

```
200 OK
```

If:

```
Server 2 → timeout
Server 2 → timeout
Server 2 → timeout
```

the load balancer can mark it unhealthy:

```
Server 1 ✅
Server 2 ❌
Server 3 ✅
```

and stop sending traffic to Server 2.

---

## 10. Timeout

Imagine Service A calls Service B:

```
Service A
    |
    | HTTP request
    v
Service B
```

Suppose Service B never responds.

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

Eventually Service A may exhaust all its threads/connections.

**With a timeout:**

```
Service A
   |
   | Request
   v
Service B
   |
   | No response
   |
   v
5 seconds timeout
   |
   v
Failure handled
```

> Timeouts prevent one slow component from blocking the entire system.

---

## 11. Retry

Sometimes failures are temporary.

Example:

```
Service A → Service B
             |
             ❌ Network failure
```

Service A can retry:

```
Attempt 1 ❌
Attempt 2 ❌
Attempt 3 ✅
```

But retries must be used carefully.

### Bad retry

```
Request fails
    ↓
Retry immediately
    ↓
Fails
    ↓
Retry immediately
    ↓
Fails
    ↓
Retry immediately
```

Thousands of clients doing this can overload Service B.

This can create a:

> **Retry storm**

---

## 12. Exponential Backoff

Instead of retrying immediately:

```
Retry 1 → wait 100ms
Retry 2 → wait 200ms
Retry 3 → wait 400ms
Retry 4 → wait 800ms
```

This is called **exponential backoff**.

Often some randomness (**jitter**) is added:

```
Client A → 237ms
Client B → 184ms
Client C → 291ms
```

This prevents many clients from retrying simultaneously.

---

## 13. Circuit Breaker

This is extremely important in microservices.

Suppose:

```
Order Service
     |
     v
Payment Service ❌
```

Payment Service is continuously failing.

**Without a circuit breaker:**

```
Order → Payment ❌
Order → Payment ❌
Order → Payment ❌
Order → Payment ❌
Order → Payment ❌
```

The Order Service keeps wasting resources.

A circuit breaker changes this behavior.

### States

```
             failures
 CLOSED -----------------> OPEN
   ^                         |
   |                         |
   | success                 | timeout
   |                         v
   +-------------------- HALF OPEN
```

**CLOSED**

Normal operation:

```
Request → Payment
```

**OPEN**

Too many failures:

```
Request
   |
   X
Circuit breaker
```

Request doesn't even reach Payment Service.

**HALF-OPEN**

After some time, the circuit breaker allows a few test requests.

If successful:

```
HALF-OPEN → CLOSED
```

If failures continue:

```
HALF-OPEN → OPEN
```

---

## 14. Bulkhead Pattern

Imagine one application has:

```
100 threads
```

Payment requests consume all 100 threads.

Then:

```
Order requests
Inventory requests
User requests
```

cannot execute.

A **bulkhead** isolates resources.

For example:

```
Application
|
+--- Payment Pool: 30 threads
|
+--- Order Pool: 30 threads
|
+--- Inventory Pool: 20 threads
|
+--- User Pool: 20 threads
```

If Payment Service fails:

```
Payment Pool ❌
```

other operations can still work.

> This is similar to compartments in a ship: damage to one compartment doesn't sink the entire ship.

---

## 15. Graceful Degradation

Sometimes we cannot provide the complete functionality.

Instead of completely failing, provide a reduced experience.

Example:

```
Product Service ✅
Recommendation Service ❌
```

Instead of:

```
Product page → ERROR ❌
```

we can return:

```
Product page
    +
Product details
    +
Price
    +
Inventory

Recommendations unavailable
```

This is **graceful degradation**.

---

## 16. Fail Fast

Don't wait indefinitely for a component that is known to be unhealthy.

Example:

```
Request
   |
   v
Payment Service ❌
   |
   v
Circuit breaker
   |
   v
Fail immediately
```

This protects system resources.

---

## 17. Queue-Based Fault Tolerance

Queues can decouple services.

Instead of:

```
Order Service
     |
     | synchronous
     v
Email Service
```

use:

```
Order Service
     |
     v
Kafka / Queue
     |
     v
Email Service
```

If Email Service is temporarily down:

```
Order Service ✅
      |
      v
Kafka ✅
      |
      v
Email Service ❌
```

Messages remain in Kafka.

When Email Service recovers:

```
Kafka
  |
  v
Email Service
  |
  v
Process messages
```

This provides resilience against temporary downstream failures.

---

## 18. Dead Letter Queue

What if a message repeatedly fails?

```
Kafka
  |
  v
Consumer
  |
  ❌
  |
Retry
  |
  ❌
  |
Retry
  |
  ❌
  |
DLQ
```

The **Dead Letter Queue (DLQ)** stores messages that cannot be successfully processed.

> This prevents one poison message from blocking processing indefinitely.

---

## 19. Database Fault Tolerance

A common architecture:

```
             Application
                  |
                  v
             DB Primary
              /       \
             /         \
            v           v
       Replica 1     Replica 2
```

If primary fails:

```
DB Primary ❌
     |
     v
Replica 1
     |
     v
Promoted to Primary
```

However, there are important considerations:

- Replication lag
- Data loss during failover
- Split-brain
- Leader election
- Consistency
- Recovery time

---

## 20. Kafka Fault Tolerance

Since you're currently studying Kafka, this is particularly important.

Kafka achieves fault tolerance primarily through **replication**.

Example:

```
Topic: orders

Partition 0

Broker 1 → Leader
Broker 2 → Follower
Broker 3 → Follower
```

If Broker 1 fails:

```
Broker 1 ❌

Broker 2 → New Leader
Broker 3 → Follower
```

Consumers can continue consuming from the new leader.

This is why Kafka's:

- Replication Factor
- ISR
- Leader/Follower
- `acks=all`

are important for fault tolerance.

---

## 21. Availability Zones

A highly available system shouldn't put everything in one physical location.

**Bad:**

```
Region
 |
 +--- AZ1
      |
      +--- All servers
```

If AZ1 fails:

```
Entire system ❌
```

**Better:**

```
              Load Balancer
              /           \
             /             \
           AZ1             AZ2
           |                |
       Servers           Servers
           |                |
        Database         Database
```

If AZ1 fails:

```
AZ1 ❌

AZ2 ✅
```

The system can continue operating.

---

## 22. Region-Level Fault Tolerance

For very critical systems:

```
          Global DNS / Load Balancer
               /             \
              /               \
       Mumbai Region       Hyderabad Region
            |                    |
         Servers              Servers
            |                    |
         Database              Database
```

If one region becomes unavailable:

```
Mumbai ❌

Hyderabad ✅
```

Traffic can be redirected.

This provides stronger disaster recovery.

---

## 23. Fault Tolerance vs Disaster Recovery

These are related but different.

### Fault tolerance

Handles failures while keeping the system running.

Example:

```
Server 1 ❌
Server 2 continues
```

### Disaster recovery

Handles major failures and restores service.

Example:

```
Entire region ❌
      |
      v
Backup region
      |
      v
Restore service
```

Important DR concepts:

### RTO

**Recovery Time Objective**

> How quickly must the system recover?

Example:

```
RTO = 30 minutes
```

The system should recover within 30 minutes.

### RPO

**Recovery Point Objective**

> How much data loss is acceptable?

Example:

```
RPO = 5 minutes
```

At worst, you may lose the last 5 minutes of data.

---

## 24. Fault Tolerance Architecture

A typical production system could look like:

```
                         Users
                           |
                           v
                    Global DNS / LB
                           |
              +------------+------------+
              |                         |
             AZ1                       AZ2
              |                         |
        +-----+-----+             +-----+-----+
        |           |             |           |
      App 1       App 2         App 3       App 4
        |           |             |           |
        +-----+-----+-------------+-----+-----+
                           |
                         Cache
                           |
                         Kafka
                           |
                    Database Cluster
                    /              \
                Primary           Replica
```

### Fault tolerance mechanisms:

```
Load Balancer       → traffic failover
Multiple servers    → server redundancy
Health checks       → failure detection
Timeouts            → prevent hanging
Retries             → transient failures
Backoff             → avoid retry storms
Circuit breaker     → isolate failures
Bulkheads           → resource isolation
Kafka               → asynchronous buffering
Replication         → data redundancy
Failover            → switch to healthy node
Multi-AZ            → infrastructure failure protection
Multi-region        → regional disaster protection
Backups             → disaster recovery
Monitoring          → detect failures
```

---

## 25. Most Important Principle

A common mistake in system design interviews is saying:

> "We will make the system fault tolerant by adding more servers."

That's incomplete.

You need to think about **every failure point**.

Ask:

> **What happens if...**

- Server fails?
- Database fails?
- Cache fails?
- Kafka broker fails?
- Network fails?
- Dependency becomes slow?
- Dependency returns errors?
- Message processing fails?
- Deployment fails?
- Availability Zone fails?
- Entire region fails?

Then design a recovery strategy for each.

---

## 26. Fault-Tolerance Design Checklist

When designing a system, think through:

| Failure | Typical Solution |
|---|---|
| Server failure | Multiple instances |
| Instance overload | Load balancing + autoscaling |
| Database failure | Replication + failover |
| Cache failure | Cache fallback / rebuild |
| Kafka broker failure | Replication |
| Network failure | Retry + timeout |
| Temporary API failure | Retry + backoff |
| Persistent API failure | Circuit breaker |
| Slow dependency | Timeout |
| Resource exhaustion | Bulkhead |
| Message failure | Retry + DLQ |
| AZ failure | Multi-AZ |
| Region failure | Multi-region |
| Data corruption | Backups |
| Deployment failure | Rollback / blue-green / canary |
| Traffic spike | Autoscaling + queueing |

---

## 27. Interview Definition

If an interviewer asks:

> "What is fault tolerance?"

A strong answer is:

> Fault tolerance is the ability of a system to continue providing its intended functionality despite failures of individual components. We achieve it using redundancy, replication, health checks, failover, timeouts, retries with backoff, circuit breakers, bulkheads, asynchronous queues, and multi-AZ or multi-region deployments. The goal is to prevent a component failure from becoming a system-wide failure.

---

## 28. Fault Tolerance Mental Model

Remember this flow:

```
                 FAILURE
                    |
                    v
              Detect failure
                    |
                    v
              Isolate failure
                    |
                    v
             Recover / Failover
                    |
                    v
             Continue serving
                    |
                    v
              Monitor system
```

And the five most important concepts to remember:

```
             FAULT TOLERANCE
                    |
       +------------+------------+
       |            |            |
   Redundancy    Failover    Isolation
       |            |            |
  Replication   Health check  Circuit Breaker
                             Bulkhead
       |
       +-----------------------------+
       |                             |
   Recovery                      Resilience
       |                             |
   Retry + Backoff              Graceful Degradation
   Queue / DLQ                   Timeouts
   Backup                        Multi-AZ
```

