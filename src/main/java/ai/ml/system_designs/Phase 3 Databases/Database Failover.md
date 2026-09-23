# Database Failover

Database Failover is the process of switching database operations from a failed or unhealthy database server to another available database server.

It is mainly used to achieve:

- **High Availability**
- **Fault Tolerance**
- **Business Continuity**
- **Reduced Downtime**
- **Disaster Recovery**

A simple example:

Before failure:

```
                 Application
                      │
                      ▼
                 Primary DB
                  /       \
                 ▼         ▼
            Replica 1   Replica 2
```

After Primary failure:

```
                 Application
                      │
                      ▼
                 Replica 1
                (New Primary)
                      │
                      ▼
                  Replica 2
```

The important idea is:

**Replication creates the backup database copy; failover switches traffic to that copy when the current primary fails.**

---

## 1. Why Do We Need Database Failover?

Imagine an application with only one database:

```
Application
     │
     ▼
 Primary DB
```

If the database crashes:

```
Application
     │
     ▼
 Primary DB ❌
```

The application cannot perform database operations.

This can cause:

- Users unable to login
- Orders cannot be created
- Payments may fail
- Transactions cannot complete

Now introduce a replica:

```
                 Primary DB
                /          \
               ▼            ▼
          Replica 1     Replica 2
```

If the primary fails:

```
                 Primary DB ❌
                       │
                       X

                 Replica 1
                       │
                       ▼
                  New Primary
```

Traffic can be redirected to Replica 1.

---

## 2. Replication vs Failover

These concepts are closely related but different.

### Replication

Copies data:

```
Primary
   │
   ├────────► Replica 1
   │
   └────────► Replica 2
```

### Failover

Changes which database serves as the primary:

```
Before:

Primary → DB1
Replica → DB2


After:

Primary → DB2
Replica → DB1
```

So:

**Replication prepares the backup. Failover activates the backup.**

---

## 3. Basic Failover Architecture

A typical architecture:

```
                    Application
                         │
                         ▼
                  DB Endpoint
                         │
                         ▼
                    Primary DB
                   /          \
                  ▼            ▼
             Replica 1     Replica 2
```

The application should ideally connect through a stable database endpoint, proxy, or service-discovery mechanism, rather than hardcoding a particular database server.

When the primary fails:

```
                    Application
                         │
                         ▼
                  DB Endpoint
                         │
                         ▼
                    Replica 1
                  (Promoted)
```

The application doesn't necessarily need to know that the underlying database changed.

---

## 4. What Causes Failover?

Failover can happen because of:

- **Hardware Failure** — Server → Power failure
- **Database Process Failure** — Database process → Crash
- **Network Failure** — Application ──X── Database
- **Disk Failure** — Storage → Failure
- **Operating System Failure** — Server → OS crash
- **Availability Zone Failure** — AZ-1 → DOWN
- **Region Failure** — Region A → DOWN
- **Planned Maintenance**

Failover doesn't always mean something went wrong.

You may deliberately perform a planned failover during maintenance.

---

## 5. Automatic Failover

In production systems, failover is often automated.

A simplified architecture:

```
                  Monitoring
                      │
                      ▼
                 Health Check
                      │
              Is Primary Healthy?
                 /           \
               YES            NO
                │              │
                ▼              ▼
          Continue          Promote
          Primary           Replica
                               │
                               ▼
                         Redirect Traffic
```

---

## 6. Step-by-Step Automatic Failover

Suppose:

    DB1 = Primary
    DB2 = Replica

Normal operation:

```
Application
     │
     ▼
    DB1
 Primary
     │
     ▼
    DB2
 Replica
```

### Step 1 — Health Monitoring

A monitoring system continuously checks DB1:

```
DB1
 │
 ├── Is server alive?
 ├── Is database responding?
 ├── Is replication healthy?
 └── Is database accepting operations?
```

### Step 2 — Primary Failure

Suppose DB1 crashes:

    DB1 ❌

Monitoring detects:

    Primary unavailable

### Step 3 — Select a Replica

The system chooses a suitable replica:

```
DB2
 │
 ├── Is it healthy?
 ├── How far behind is it?
 └── Is it eligible for promotion?
```

### Step 4 — Promote Replica

DB2 changes role:

```
DB2
 │
 └── Replica → Primary
```

### Step 5 — Redirect Traffic

Application traffic moves to DB2:

```
Application
     │
     ▼
    DB2
 New Primary
```

### Step 6 — Rebuild Replication

After the original DB1 is repaired:

    DB2 = Primary
    DB1 = Replica

Replication is re-established:

```
             DB2
          New Primary
               │
               ▼
              DB1
            Replica
```

---

## 7. Failover Time

The time required to switch to another database is called **failover time**.

For example:

```
Primary failure
      │
      ▼
Detection
      │
      ▼
Promotion
      │
      ▼
DNS / endpoint update
      │
      ▼
Application reconnect
      │
      ▼
Service restored
```

Suppose the entire process takes:

    30 seconds

Then users may experience approximately 30 seconds of database unavailability, although the exact impact depends on connection pooling, retries, transaction behavior, and the failover mechanism.

---

## 8. RTO and RPO

Two very important concepts for failover are:

### RTO — Recovery Time Objective

How quickly must the system recover?

Example:

    RTO = 1 minute

Means:

    The system should recover within approximately 1 minute.

### RPO — Recovery Point Objective

How much data loss is acceptable?

Example:

    RPO = 5 minutes

Means:

    In a worst-case disaster, losing up to about 5 minutes of data may be acceptable.

---

## 9. RTO vs RPO

Remember:

```
RTO
 ↓
How long can we be DOWN?

RPO
 ↓
How much DATA can we lose?
```

Example:

Company requirement:

    RTO = 30 seconds
    RPO = 0 seconds

This means:

    Downtime → ≤ 30 sec
    Data loss → ideally 0

Achieving very low RTO/RPO usually requires additional infrastructure and cost.

---

## 10. Asynchronous Replication and Data Loss

This is a very important system-design scenario.

Suppose:

```
Primary DB
Order #100
Order #101
Order #102
```

Replica has:

```
Replica DB
Order #100
Order #101
```

Replication is behind by one order.

Now primary crashes:

    Primary ❌

Replica gets promoted:

```
New Primary

Order #100
Order #101
```

Order #102 may be lost from the surviving database.

This is possible with asynchronous replication.

Therefore:

**Replication does not automatically guarantee zero data loss.**

---

## 11. Synchronous Replication and Failover

With synchronous replication, the system can require replication acknowledgements before considering certain writes committed.

Conceptually:

```
Application
     │
     ▼
Primary
     │
     ▼
Replica
     │
     ▼
ACK
     │
     ▼
Application receives success
```

If the primary fails:

```
Primary ❌

Replica
   │
   ▼
New Primary
```

The risk of losing acknowledged writes can be lower, depending on the exact synchronous-commit configuration.

But synchronous replication can increase:

- Write latency
- Dependency on network connectivity
- Availability trade-offs

---

## 12. Failover and Split-Brain

One of the most dangerous problems is **split-brain**.

Imagine:

```
          Network Partition
              X
              X
              X

        DB1             DB2
      Primary?        Primary?
```

Both databases may incorrectly believe:

```
DB1 → "I am Primary"
DB2 → "I am Primary"
```

Now both accept writes:

```
DB1:
Balance = ₹8,000

DB2:
Balance = ₹7,000
```

Now the system has conflicting data.

This is:

**Split-brain**

---

## 13. How Do We Prevent Split-Brain?

Distributed database systems may use mechanisms such as:

- Consensus
- Quorum
- Leader election
- Fencing
- Distributed coordination systems

The goal is to ensure that:

**Only one node is authorized to act as the primary at a time.**

Conceptually:

```
          Coordinator
               │
        ┌──────┴──────┐
        ▼             ▼
       DB1           DB2
        │             │
      Primary        Replica
```

If DB1 fails:

```
          Coordinator
               │
               ▼
              DB2
               │
               ▼
            Primary
```

The exact mechanism depends on the database/platform.

---

## 14. Connection Handling During Failover

There's another important problem.

Suppose the application has:

```
Connection Pool
     │
     ├── Connection 1 → DB1
     ├── Connection 2 → DB1
     ├── Connection 3 → DB1
     └── Connection 4 → DB1
```

DB1 fails:

    DB1 ❌

Existing connections may fail.

The application needs to:

```
Detect failure
     ↓
Close invalid connections
     ↓
Reconnect
     ↓
Connect to new primary
```

This is why connection pools and retry strategies are important in failover design.

---

## 15. Retry During Failover

Suppose the application sends:

```sql
UPDATE account
SET balance = ...
```

At exactly that moment:

    Primary fails

The application receives:

    Connection error

Should it retry?

**Careful!**

The transaction might actually have committed before the connection failed.

For example:

```
Application → DB
              │
              ├── COMMIT SUCCESS
              │
              X
              │
Connection lost
```

Application doesn't know whether the transaction succeeded.

If it blindly retries:

```
Transaction
     ↓
Retry
     ↓
Possible duplicate operation ❌
```

This is especially dangerous for:

- Payments
- Orders
- Money transfers
- Inventory updates

Solutions may include:

- Idempotency keys
- Transaction IDs
- Unique constraints
- Carefully designed retry policies
- Application-level reconciliation

---

## 16. Database Failover + Idempotency

Consider a payment:

    POST /payments

Client sends:

    idempotency-key = ABC123

Database transaction executes:

    Payment ABC123 → SUCCESS

But the response is lost:

```
DB → SUCCESS
 │
 X
 │
Application ❌ no response
```

Application retries:

```
POST /payments
idempotency-key = ABC123
```

The system checks:

    ABC123 already processed

and doesn't charge the customer twice.

**This is extremely important in distributed systems.**

---

## 17. Manual vs Automatic Failover

### Manual Failover

An engineer performs the promotion:

```
Failure
   ↓
Engineer investigates
   ↓
Promotes replica
   ↓
Redirects traffic
```

**Advantages:**

- More control
- Lower risk of incorrect automatic decisions

**Disadvantages:**

- Slower recovery
- Requires human intervention

### Automatic Failover

```
Failure
   ↓
Detect
   ↓
Promote
   ↓
Redirect
```

**Advantages:**

- Faster recovery
- Less human intervention

**Disadvantages:**

- More complexity
- Incorrect failure detection can cause serious problems
- Split-brain must be prevented

---

## 18. Same Availability Zone vs Different Availability Zone

Suppose:

```
                    Region
                      │
           ┌──────────┴──────────┐
           ▼                     ▼
         AZ-1                   AZ-2
           │                     │
        Primary                Replica
```

If AZ-1 fails:

```
AZ-1 ❌

AZ-2
Replica → Primary
```

This provides better protection than keeping both database nodes in the same failure domain.

---

## 19. Cross-Region Failover

For disaster recovery, databases can be replicated across regions:

```
             Region A
             Primary
                │
                │ Replication
                ▼
             Region B
             Replica
```

If Region A fails:

```
Region A ❌

Region B
   │
   ▼
Promote Replica
   │
   ▼
New Primary
```

This provides protection against large-scale regional failures.

However, cross-region replication typically introduces:

- Higher network latency
- Potential replication lag
- More complex failover
- More complex data consistency considerations

---

## 20. Failover vs Disaster Recovery

These concepts overlap but aren't identical.

### Failover

Usually focuses on quickly switching from one active database node to another.

```
DB1 ❌
 ↓
DB2 → Primary
```

### Disaster Recovery

Covers recovering the system after a major failure.

```
Region A ❌
     ↓
Region B
     ↓
Restore / Promote
     ↓
System Recovery
```

Disaster recovery can include:

- Replication
- Backups
- Cross-region infrastructure
- Data restoration
- DNS/traffic management
- Infrastructure provisioning
- Recovery procedures

---

## 21. Failover vs Backup

Another common interview question.

### Failover

```
Primary ❌
    ↓
Replica → Primary
```

Purpose:

    Fast availability

### Backup

```
Database
    │
    ▼
Backup
```

Purpose:

    Data recovery

If someone accidentally executes:

```sql
DROP TABLE customers;
```

and the change is replicated:

```
Primary
   ↓
Replica
```

the replica may also lose the table.

A backup can potentially help recover it.

Therefore:

**Replication and failover do not replace backups.**

---

## 22. Database Failover in a Production System

A typical architecture:

```
                         Users
                           │
                           ▼
                    Load Balancer
                           │
                           ▼
                    Application
                           │
                           ▼
                  Database Endpoint
                           │
                    ┌──────┴──────┐
                    ▼             ▼
                Primary        Replica
                    │             │
                    └──────┬──────┘
                           │
                      Monitoring
                           │
                    Primary failure
                           │
                           ▼
                     Promote Replica
                           │
                           ▼
                       New Primary
```

The database endpoint/proxy/service-discovery layer helps applications locate the current primary without embedding a fixed database host everywhere.

---

## 23. Database Failover Flow

The complete process:

```
             Primary DB
                 │
                 ▼
             Health Check
                 │
                 ▼
          Is Primary Healthy?
             /          \
           YES           NO
            │             │
            ▼             ▼
        Continue      Detect Failure
                            │
                            ▼
                    Select Replica
                            │
                            ▼
                    Promote Replica
                            │
                            ▼
                    Update Endpoint
                            │
                            ▼
                    Reconnect Apps
                            │
                            ▼
                       Resume Traffic
                            │
                            ▼
                    Rebuild Replication
```

---

## 24. Important Metrics

For production database failover, monitor things such as:

### Replication Lag

```
Primary position
      -
Replica position
      =
Replication Lag
```

### Failover Time

    Failure → Service restored

### Recovery Point

    How much committed data could be lost?

### Connection Errors

    Application → DB connection failures

### Transaction Failures

    Failed transactions during failover

### Replica Health

```
Is replica:
- reachable?
- healthy?
- caught up?
- eligible for promotion?
```

---

## 25. Common Failover Problems

### Problem 1 — Replication Lag

```
Primary = latest
Replica = behind
```

Promotion may lose recently replicated data.

### Problem 2 — Split-Brain

```
DB1 thinks → Primary
DB2 thinks → Primary
```

Can cause conflicting writes.

### Problem 3 — Connection Pool Stale Connections

Applications may still try to use the failed primary.

### Problem 4 — Retry Duplicates

A failed network response doesn't necessarily mean the transaction failed.

### Problem 5 — Long Failover Time

If detection and promotion take too long:

    Users → errors/timeouts

### Problem 6 — No Backup

Replication cannot protect against every kind of data corruption or accidental deletion.

---

## 26. How to Design Good Database Failover

A production system should consider:

```
1. Primary DB
       ↓
2. Replicas
       ↓
3. Health monitoring
       ↓
4. Automatic/manual leader election
       ↓
5. Safe promotion
       ↓
6. Stable database endpoint
       ↓
7. Connection pool recovery
       ↓
8. Retry strategy
       ↓
9. Idempotency
       ↓
10. Backups + disaster recovery
```

---

## 27. Interview Answer

**If an interviewer asks:**

### "What is database failover?"

A strong answer is:

> Database failover is the process of switching database traffic from a failed or unhealthy primary database to a healthy replica or standby database. It is used to provide high availability and reduce downtime. A typical architecture uses replication to maintain one or more replicas, health monitoring to detect failures, and a promotion mechanism to make a replica the new primary. During failover, we also need to handle connection pool recovery, replication lag, retries, idempotency, and split-brain prevention.

---

## 28. Important System Design Distinction

Remember these four concepts:

```
Replication
     │
     └── Copies data

Failover
     │
     └── Switches traffic/role after failure

Backup
     │
     └── Enables recovery from data loss/corruption

Disaster Recovery
     │
     └── Recovers the overall system from major failures
```

And the complete architecture:

```
                    APPLICATION
                         │
                         ▼
                  DB ENDPOINT
                         │
                         ▼
                    PRIMARY
                   /       \
                  ▼         ▼
             REPLICA 1   REPLICA 2
                  │         │
                  └────┬────┘
                       │
                  REPLICATION
                       │
                       ▼
                  MONITORING
                       │
                 Primary fails
                       │
                       ▼
                SAFE PROMOTION
                       │
                       ▼
                  REPLICA 1
                 New Primary
                       │
                       ▼
                  APPLICATION
```

---

### Final Mental Model

**Replication** answers: "Where is my backup copy?"

**Failover** answers: "What happens when my current database fails?"

**RTO** answers: "How quickly must I recover?"

**RPO** answers: "How much data can I afford to lose?"

**Idempotency** answers: "How do I safely retry operations during failures?"

These concepts together are fundamental for designing highly available database systems.
