# Failover in System Design

Failover is the process of switching from a failed or unhealthy component to a healthy backup component so that the system can continue operating.

**In simple words:**

> Failover = Primary fails → Backup takes over.

It is one of the most important mechanisms used to achieve **High Availability** and **Fault Tolerance**.

---

## 1. Simple Example

Suppose we have:

```
              Application
                   |
                   v
              Primary DB
                   |
                   |
              Replica DB
```

**Normally:**

```
Application → Primary DB ✅
```

Now Primary fails:

```
Application
     |
     v
Primary DB ❌
```

**Failover happens:**

```
Application
     |
     v
Replica DB
     |
     v
New Primary ✅
```

The application continues operating.

---

## 2. Why Do We Need Failover?

**Without failover:**

```
User
 |
 v
Application
 |
 v
Server ❌
 |
 v
System DOWN
```

**With failover:**

```
              User
                |
                v
          Load Balancer
           /          \
          /            \
      Server 1       Server 2
         ❌              ✅
                       |
                       v
                   Continue
```

The failure of one component doesn't necessarily cause a system-wide outage.

---

## 3. Failover vs Redundancy

These terms are related but different.

### Redundancy

Means:

> We have backup components.

```
Server 1
Server 2
Server 3
```

### Failover

Means:

> We actually switch to the backup when the active component fails.

```
Server 1 ❌
    |
    v
Server 2 becomes active
```

So:

```
Redundancy
    +
Failure Detection
    +
Switching
    =
Failover
```

---

## 4. Basic Failover Flow

A typical failover process is:

```
        Primary
           |
           v
       Failure ❌
           |
           v
    Detect Failure
           |
           v
    Select Backup
           |
           v
    Promote Backup
           |
           v
    Redirect Traffic
           |
           v
      System Continues
```

For example:

```
Primary DB
    |
    X
    |
Health check detects failure
    |
    v
Replica selected
    |
    v
Replica promoted
    |
    v
Application redirected
```

---

## 5. Automatic vs Manual Failover

There are two major types.

### Automatic Failover

The system detects the failure and switches automatically.

```
Primary ❌
    |
    v
Health Check
    |
    v
Automatic Promotion
    |
    v
Replica becomes Primary
```

**Advantages**

- Very fast
- Minimal human intervention
- Good for high-availability systems

**Disadvantages**

- More complex
- Incorrect failure detection can cause unnecessary failovers
- Split-brain must be prevented

### Manual Failover

An administrator performs the switch.

```
Primary ❌
    |
    v
Alert
    |
    v
Administrator
    |
    v
Promote Replica
```

**Advantages**

- More control
- Useful for complicated recovery decisions

**Disadvantages**

- Slower
- Requires human intervention
- Greater recovery time

---

## 6. Active-Passive Failover

This is one of the most common patterns.

```
             Load Balancer
                  |
                  v
              Primary
               ACTIVE
                  |
                  |
               Backup
               PASSIVE
```

Only the primary handles traffic.

If it fails:

```
Primary ❌
    |
    v
Backup
  ACTIVE
    |
    v
Traffic redirected
```

### Example

```
DB Primary → ACTIVE
DB Replica → PASSIVE
```

After failure:

```
DB Primary → ❌
DB Replica → ACTIVE
```

---

## 7. Active-Active Failover

In active-active architecture, multiple nodes serve traffic simultaneously.

```
             Load Balancer
               /        \
              /          \
          Server 1      Server 2
           ACTIVE        ACTIVE
```

If Server 1 fails:

```
Server 1 ❌

Server 2 continues serving
```

> There isn't necessarily a "backup waiting idle"; the remaining healthy node takes more traffic.

**Advantages**

- Better resource utilization
- Higher capacity
- Fast failure handling

**Challenges**

- Data synchronization
- State management
- Conflict resolution
- More complex architecture

---

## 8. Application Server Failover

This is usually straightforward.

```
                 Load Balancer
                /      |      \
               /       |       \
            App 1    App 2    App 3
```

Suppose:

```
App 2 ❌
```

The load balancer detects this through health checks:

```
App 1 → Healthy
App 2 → Unhealthy
App 3 → Healthy
```

Traffic becomes:

```
Users
  |
  v
Load Balancer
  |
  +---- App 1 ✅
  |
  +---- App 3 ✅
```

This is **application-level failover**.

---

## 9. Database Failover

Database failover is more complicated.

Consider:

```
              Application
                   |
                   v
              Primary DB
               /      \
              /        \
             v          v
        Replica 1    Replica 2
```

Primary fails:

```
Primary ❌
```

The system must:

- Detect the failure
- Determine which replica is healthy
- Select a candidate
- Promote it
- Redirect writes
- Update clients/connections
- Ensure the old primary doesn't continue accepting writes

**Result:**

```
              Application
                   |
                   v
              Replica 1
              NEW PRIMARY
```

---

## 10. Failover and Replication Lag

This is an important interview question.

Suppose:

```
Primary
  |
  | replication
  v
Replica
```

Primary has:

```
Order 101
Order 102
Order 103
Order 104
```

Replica has only:

```
Order 101
Order 102
Order 103
```

Now Primary crashes:

```
Primary ❌
```

Replica becomes primary.

But:

```
Order 104
```

may be missing.

> This is **replication lag** and can cause data loss depending on the replication model.

Therefore:

> Failover can improve availability but potentially affect consistency or durability.

This is a critical System Design trade-off.

---

## 11. Synchronous vs Asynchronous Replication

### Synchronous

```
Application
    |
    v
Primary
    |
    v
Replica
    |
    v
ACK
```

The write is considered committed only after the required replica acknowledgement.

**Advantages:**

- Lower risk of data loss

**Disadvantages:**

- Higher latency
- Replica/network failure can affect writes

### Asynchronous

```
Application
    |
    v
Primary
    |
    v
ACK

Primary → Replica
        asynchronously
```

The primary can acknowledge before the replica receives the data.

**Advantages:**

- Lower write latency
- Better performance

**Disadvantage:**

- Recent writes may be lost during failover

---

## 12. Failover and Split-Brain

One of the most dangerous failover problems is **split-brain**.

Imagine:

```
Primary
   |
Network partition
   X
   |
Replica
```

The primary thinks:

> "I'm still primary."

The replica thinks:

> "Primary is dead. I should become primary."

Now:

```
Primary A → accepting writes
Primary B → accepting writes
```

We have:

> Two leaders simultaneously.

This can cause inconsistent or conflicting data.

```
             Network Partition
                  X
          /               \
     Primary A          Primary B
      WRITE               WRITE
        |                   |
        +------ Conflict ---+
```

> Preventing split-brain is a major reason distributed systems use **leader election, fencing, quorum, and consensus mechanisms**.

---

## 13. Health Checks and Failover

Failover depends on correctly detecting failure.

For example:

```
Primary
   |
Health Check
   |
   +--- Response → Healthy
   |
   +--- Timeout → Suspect failure
```

But here's the problem:

> A timeout doesn't always mean the server is dead.

The network itself may be broken.

```
Application
     |
     X
 Network failure
     |
     v
Primary DB
```

The DB might actually be healthy.

If the system incorrectly promotes a replica, we can create split-brain.

> Therefore failure detection is not trivial in distributed systems.

---

## 14. Failover Time

Failover isn't instantaneous.

Suppose:

```
Primary fails
     ↓
Failure detection = 5 sec
     ↓
Election = 2 sec
     ↓
Promotion = 3 sec
     ↓
Connection refresh = 2 sec
```

**Total:**

```
Failover ≈ 12 seconds
```

During this period, users may experience:

- Timeouts
- Errors
- Connection failures

Therefore we care about:

> **Recovery Time Objective (RTO)**

---

## 15. RTO and Failover

**RTO = Recovery Time Objective**

It defines how quickly the service must recover.

Example:

```
RTO = 30 seconds
```

Means:

> After a failure, the system should recover within approximately 30 seconds.

If your failover takes:

```
5 minutes
```

then you've violated your RTO.

---

## 16. Failover and RPO

**RPO = Recovery Point Objective**

It defines how much data loss is acceptable.

Example:

```
RPO = 0
```

means:

> Ideally, no committed data should be lost.

Whereas:

```
RPO = 5 minutes
```

means losing up to approximately 5 minutes of recent data may be acceptable.

Failover strategy must therefore consider both:

```
                Failover
                   |
          +--------+--------+
          |                 |
         RTO               RPO
          |                 |
   How fast to recover?  How much data
                         can be lost?
```

---

## 17. Kafka Failover

This connects directly to the Kafka topics you've been studying.

Suppose:

```
Topic: orders
Partition: 0

Broker 1 → Leader
Broker 2 → Follower
Broker 3 → Follower
```

Normally:

```
Producer
   |
   v
Broker 1
 Leader
```

Broker 1 fails:

```
Broker 1 ❌
```

Kafka can elect another replica as leader:

```
Broker 2 → NEW Leader
Broker 3 → Follower
```

Then:

```
Producer
   |
   v
Broker 2
 NEW Leader
```

This is **Kafka partition leader failover**.

Key concepts involved:

- Replication Factor
- Leader
- Followers
- ISR
- Leader Election
- `acks=all`
- Unclean leader election

---

## 18. Kafka and Unclean Leader Election

Suppose:

```
Leader
  |
  | latest messages
  v
Follower A → caught up
Follower B → behind
```

Leader fails.

Kafka should ideally choose:

```
Follower A
```

because it's up to date.

But if no in-sync replica is available, allowing an out-of-sync replica to become leader can improve availability at the potential cost of data loss.

This is the trade-off:

```
Availability
     ↕
Potential Data Loss
```

> This is why Kafka's leader-election configuration matters.

---

## 19. DNS Failover

Failover can also happen at the DNS/routing level.

For example:

```
api.example.com
        |
        v
     DNS
    /   \
   /     \
Region A Region B
```

If Region A fails:

```
Region A ❌
```

Traffic can be directed toward:

```
Region B ✅
```

This is commonly used in multi-region architectures.

---

## 20. Load Balancer Failover

Even the load balancer itself shouldn't necessarily become a SPOF.

**Bad:**

```
Users
  |
Single Load Balancer ❌
```

**Better:**

```
             Users
               |
       +-------+-------+
       |               |
      LB1             LB2
       |               |
       +-------+-------+
               |
          App Servers
```

If LB1 fails:

```
LB1 ❌
LB2 continues
```

---

## 21. Cascading Failure and Failover

Failover must be designed carefully.

Suppose:

```
Service A
   |
   v
Service B
   |
   v
Database
```

Database fails.

If every service aggressively retries:

```
A → B → DB ❌
A → B → DB ❌
A → B → DB ❌
```

the system may become overloaded.

**Better:**

```
Timeout
   ↓
Limited Retry
   ↓
Backoff
   ↓
Circuit Breaker
   ↓
Failover / Fallback
```

> Failover is only one part of resilient system design.

---

## 22. Failover in a Microservices System

Consider:

```
                 API Gateway
                      |
             +--------+--------+
             |                 |
         Order Service     User Service
             |
             v
       Payment Service
```

Payment Service fails.

**Possible strategy:**

```
Payment Service
      ❌
      |
      v
Circuit Breaker
      |
      v
Retry
      |
      v
Fallback / Queue
```

For a payment system, you should not blindly create another payment attempt because duplicate charging is possible.

**Instead:**

```
Idempotency Key
       +
Payment State
       +
Retry
       +
Failover
```

This demonstrates an important point:

> **Failover must preserve correctness, not just availability.**

---

## 23. Failover and Idempotency

Suppose:

```
Payment Service A
       |
       v
Payment Gateway
```

Payment succeeds:

```
₹1,000 deducted ✅
```

But the service crashes before returning the response:

```
Payment Service A ❌
```

Failover sends the request to Payment Service B.

**If B blindly processes the request:**

```
₹1,000
+
₹1,000
=
₹2,000 ❌
```

**With idempotency:**

```
payment_id = P123
```

Service B checks:

```
P123 already processed?
        |
       YES
        |
        v
Return existing result
```

No duplicate payment.

> This is reliable failover.

---

## 24. Failover vs Disaster Recovery

Don't confuse these.

### Failover

Usually handles a component or infrastructure failure:

```
Server 1 ❌
   ↓
Server 2
```

### Disaster Recovery

Handles a major disaster:

```
Entire Region A ❌
       |
       v
Restore / switch to Region B
```

**Think:**

```
Small failure
     ↓
Failover

Large-scale disaster
     ↓
Disaster Recovery
```

---

## 25. Failover Architecture

A typical highly available system:

```
                         Users
                           |
                           v
                    Global Routing
                     /           \
                    /             \
                Region A       Region B
                   |                |
              Load Balancer    Load Balancer
                /     \          /     \
               /       \        /       \
            App 1     App 2   App 3     App 4
               |        |        |        |
               +--------+--------+--------+
                              |
                           Kafka
                              |
                        DB Cluster
                       /          \
                  Primary        Replica
                     |
                  Failure
                     |
                     v
              Replica promoted
                     |
                     v
                 New Primary
```

---

## 26. Failover Checklist

When designing failover, ask:

```
1. What can fail?
        ↓
2. How do we detect the failure?
        ↓
3. What is the backup?
        ↓
4. How do we select the backup?
        ↓
5. How do we promote it?
        ↓
6. How do clients discover it?
        ↓
7. How do we prevent split-brain?
        ↓
8. What happens to in-flight requests?
        ↓
9. Can data be lost?
        ↓
10. How quickly can we recover?
```

> These questions are excellent for System Design interviews.

---

## 27. Important Failover Scenarios

| Component | Failover Strategy |
|---|---|
| Application server | Load balancer routes to another instance |
| Load balancer | Redundant LB / managed service |
| Database primary | Replica promotion |
| Kafka broker | Partition leader election |
| Cache node | Replica / cluster failover |
| Availability Zone | Route traffic to another AZ |
| Region | Global traffic failover |
| Network path | Alternate network path |
| Service dependency | Circuit breaker / fallback |
| Deployment | Rollback / previous version |

---

## 28. Interview Answer

If an interviewer asks:

> "What is Failover?"

**A strong answer:**

> Failover is the process of automatically or manually switching traffic or responsibility from a failed or unhealthy primary component to a healthy redundant component. It is a key mechanism for achieving high availability and fault tolerance. A robust failover design requires failure detection, backup capacity, promotion or routing logic, connection handling, split-brain prevention, and consideration of RTO, RPO, data consistency, and replication lag.

---

## 29. Most Important Mental Model

Remember:

```
                    FAILOVER
                       |
                       v
                Primary Failure
                       |
                       v
                Detect Failure
                       |
                       v
                Select Backup
                       |
                       v
             Promote / Activate Backup
                       |
                       v
               Redirect Traffic
                       |
                       v
               Recover Connections
                       |
                       v
                Continue Service
```

And the bigger picture:

```
             HIGH AVAILABILITY
                    |
                    v
               Redundancy
                    |
                    v
               Replication
                    |
                    v
             Failure Detection
                    |
                    v
                 Failover
                    |
          +---------+---------+
          |                   |
       RTO/RPO          Consistency
          |                   |
    Recovery speed      Data correctness
```

### One-line memory trick

> **Redundancy** gives you a backup; **failure detection** tells you when to use it; **failover** switches to it.

