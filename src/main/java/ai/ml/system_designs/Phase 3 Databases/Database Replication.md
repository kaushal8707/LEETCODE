# Database Replication

Database Replication is the process of copying data from one database server to one or more other database servers.

The main purpose is to improve:

- **Availability**
- **Fault tolerance**
- **Read scalability**
- **Disaster recovery**

A simple example:

```
                 Application
                      │
                      ▼
                 Primary DB
                 /        \
                /          \
               ▼            ▼
          Replica DB 1   Replica DB 2
```

The same data is replicated across multiple database servers.

---

## 1. Why Do We Need Replication?

Imagine your application has only one database:

```
        Application
             │
             ▼
        ┌─────────┐
        │   DB    │
        └─────────┘
```

If the database crashes:

```
        Application
             │
             ▼
        ┌─────────┐
        │   DB    │ ❌ DOWN
        └─────────┘
```

Your application may become unavailable.

Now introduce replication:

```
                 Application
                      │
                      ▼
                 Primary DB
                  /       \
                 ▼         ▼
             Replica 1   Replica 2
```

If the primary fails:

```
              Primary DB
                     ❌
                     │
                     ▼
              Replica 1
                     │
                     ▼
                 Application
```

Another replica can potentially be promoted to primary.

This improves **fault tolerance**.

---

## 2. Primary and Replica

The most common replication architecture is:

```
                ┌──────────────┐
                │  Primary DB  │
                └──────┬───────┘
                       │
                  Replication
                       │
             ┌─────────┴─────────┐
             ▼                   ▼
       ┌──────────┐        ┌──────────┐
       │ Replica 1│        │ Replica 2│
       └──────────┘        └──────────┘
```

### Primary

The Primary (also called leader/master in some systems):

- Accepts writes
- Maintains the authoritative write state
- Sends changes to replicas

### Replica

A Replica (also called follower/slave in older terminology):

- Receives replicated changes
- Can often serve reads
- Can potentially be promoted if the primary fails

---

## 3. Read Scaling

Replication is particularly useful when your application has many more reads than writes.

Suppose:

    100 requests/second

and:

    90 → Reads
    10 → Writes

Instead of sending everything to one database:

```
                    Application
                         │
                  ┌──────┴──────┐
                  │             │
                Writes         Reads
                  │             │
                  ▼             ▼
              Primary       Replicas
```

For example:

```
                     Application
                          │
             ┌────────────┴────────────┐
             ▼                         ▼
          Write                    Read
             │                         │
             ▼                         ▼
        Primary DB           ┌─────────┼─────────┐
                             ▼         ▼         ▼
                          Replica1 Replica2 Replica3
```

This distributes read traffic.

---

## 4. Primary-Replica Replication

Consider an e-commerce application.

Initially:

    Product stock = 100

The primary database contains:

    Primary DB
    stock = 100

A customer buys one product:

```sql
UPDATE products
SET stock = 99
WHERE product_id = 10;
```

The primary changes:

    Primary → stock = 99

Then the change is replicated:

```
Primary
stock = 99
   │
   ├──────────────► Replica 1
   │                    stock = 99
   │
   └──────────────► Replica 2
                        stock = 99
```

Now all replicas eventually contain the updated value.

---

## 5. Synchronous Replication

With synchronous replication, the primary waits for one or more replicas to acknowledge the replicated change before considering the operation successfully committed, depending on the database's configuration.

Conceptually:

```
                    WRITE
                      │
                      ▼
                 Primary DB
                  /       \
                 ▼         ▼
            Replica 1   Replica 2
                 │         │
                 └────┬────┘
                      ▼
                  ACKNOWLEDGE
                      │
                      ▼
                  Application
```

### Advantage

Better protection against losing a committed change if the primary immediately fails.

### Disadvantage

Higher latency.

If a replica is slow:

```
Primary
   │
   ├── Replica 1 → Fast
   │
   └── Replica 2 → Slow ❌
```

The write may have to wait depending on the configured synchronous-commit policy.

---

## 6. Asynchronous Replication

With asynchronous replication, the primary can acknowledge the write without waiting for replicas to confirm that they have received/applied it.

```
Application
     │
     ▼
Primary DB
     │
     └──────────────► Replica
                         │
                         ▼
                      Apply later
```

The application gets a response quickly.

### Advantage

- Lower write latency
- Better write availability
- Replicas can lag temporarily

### Disadvantage

If the primary crashes before the change reaches the replicas, the most recently committed data may not yet exist on the surviving replicas.

---

## 7. Synchronous vs Asynchronous

| Feature | Synchronous | Asynchronous |
|---|---|---|
| Write latency | Higher | Lower |
| Replica lag | Very low/controlled | Possible |
| Data-loss risk on primary failure | Lower | Higher |
| Availability during replica/network problems | Can decrease | Generally higher |
| Complexity | Higher | Lower |
| Useful for | Strong durability requirements | High-throughput systems |

The exact guarantees depend on the database and replication configuration.

---

## 8. Replication Lag

With asynchronous replication, replicas may be behind the primary.

Suppose:

    Primary → Order #101 created

But:

    Replica → Still processing up to Order #100

We have:

    Primary  → Order #101
    Replica  → Order #100

This is called:

**Replication Lag**

After some time:

    Primary  → Order #101
    Replica  → Order #101

---

## 9. Real-Time Example of Replication Lag

Suppose a user creates an order:

    POST /orders

The request goes to the primary:

    Primary DB
    Order #1001 → CREATED

Immediately after that, the user requests:

    GET /orders/1001

If the read goes to a lagging replica:

    Replica DB
    Order #1001 → NOT YET AVAILABLE

The user may see:

    Order not found

even though the order was successfully created.

This is a classic **read-after-write consistency** problem.

---

## 10. Read-After-Write Consistency

A common solution is to route a user's immediate reads to the primary after a write.

For example:

```
POST /orders
       │
       ▼
   Primary DB
       │
       ▼
 Order Created
       │
       ▼
GET /orders/1001
       │
       ▼
   Primary DB
```

After some time, reads can return to replicas:

```
                    Application
                         │
              ┌──────────┴──────────┐
              ▼                     ▼
          Recent write            Normal read
              │                     │
              ▼                     ▼
          Primary              Replica Pool
```

Other approaches include session/causal consistency mechanisms depending on the database and architecture.

---

## 11. Failover

Replication becomes especially valuable when the primary database fails.

Before failure:

```
              Primary
                 │
          ┌──────┴──────┐
          ▼             ▼
       Replica 1     Replica 2
```

Primary crashes:

    Primary ❌

A replica can be promoted:

```
              Replica 1
                  │
                  ▼
              New Primary
                  │
                  ▼
             Application
```

This process is called:

**Failover**

---

## 12. Automatic Failover

In a production system, you don't want engineers manually changing database configuration at 3 AM.

A typical architecture might look like:

```
                  Application
                       │
                       ▼
                DB Endpoint / Proxy
                       │
                       ▼
                  Primary DB
                 /          \
                ▼            ▼
           Replica 1      Replica 2
```

A monitoring/control system detects:

    Primary → ❌ unavailable

and promotes a suitable replica.

Then:

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

The application can continue operating after the failover process completes.

---

## 13. Replication Topologies

There are several common architectures.

### Primary → Multiple Replicas

```
              Primary
             /   |   \
            ▼    ▼    ▼
           R1    R2    R3
```

This is very common.

Good for:

- Read scaling
- High availability
- Geographic distribution

---

## 14. Chain Replication

Replication can also be chained:

```
Primary
   │
   ▼
Replica 1
   │
   ▼
Replica 2
   │
   ▼
Replica 3
```

This can reduce the direct replication load on the primary in some architectures, but it introduces additional dependencies and lag.

---

## 15. Multi-Primary Replication

Instead of one primary:

```
       DB A
      ↙    ↘
    DB B ←→ DB C
```

multiple databases can accept writes.

This is called:

**Multi-Primary / Multi-Leader Replication**

Example:

```
US DB  ←→  Europe DB
  ↑             ↑
Writes        Writes
```

Now you have a major problem:

**Write Conflicts**

Suppose both regions update the same record:

```
US DB:
name = Rahul

Europe DB:
name = Amit
```

How should the system resolve the conflict?

You need a **conflict-resolution strategy**.

---

## 16. Conflict Resolution

Possible strategies include:

### Last Write Wins

The latest timestamp wins.

```
Update A → 10:00:01
Update B → 10:00:03

Winner → Update B
```

Simple, but it can discard valid updates.

### Application-Level Merge

The application understands how to combine changes.

### Version-Based Conflict Detection

Use versions:

```
Version 10
   ↓
Version 11
```

If two clients update version 10 simultaneously, one can be rejected or retried.

---

## 17. Replication vs Sharding

These are often confused.

### Replication

Creates copies of the **same data**.

```
Primary
   │
   ├──→ Replica 1
   └──→ Replica 2
```

Same dataset

Purpose:

- Availability
- Read scaling
- Fault tolerance

### Sharding

Splits the dataset into **different partitions**.

```
              Database
                 │
       ┌─────────┼─────────┐
       ▼         ▼         ▼
    Shard 1   Shard 2   Shard 3
    Users      Users      Users
    1-1M       1M-2M      2M-3M
```

Purpose:

- Storage scaling
- Write scaling
- Data distribution

---

## 18. Replication + Sharding

In large-scale systems, you often use both.

For example:

```
                    Database Cluster
                           │
             ┌─────────────┼─────────────┐
             ▼             ▼             ▼
          Shard 1       Shard 2       Shard 3
             │             │             │
          ┌──┴──┐       ┌──┴──┐       ┌──┴──┐
          ▼     ▼       ▼     ▼       ▼     ▼
       Primary  R1    Primary  R1   Primary  R1
```

Now:

- **Sharding** distributes different data.
- **Replication** creates copies of each shard.

This is a very common pattern in large distributed databases.

---

## 19. Replication and CAP Theorem

Replication also connects to the CAP Theorem.

Suppose:

    Primary DB ←── network failure ──→ Replica

The nodes cannot communicate.

The system has to decide how to behave.

For example:

```
CP
│
└── Prefer consistency
    May reject/delay some operations

AP
│
└── Prefer availability
    Continue operating
    Temporary inconsistency may occur
```

Replication itself doesn't automatically make a system CP or AP.

The actual behavior depends on the database's replication and consistency mechanisms.

---

## 20. Replication and Consistency Models

This also connects to the previous topic.

### Stronger consistency

The system may ensure that reads observe the latest committed data according to its consistency guarantees.

### Eventual consistency

Replicas can temporarily differ:

```
Time 1:

Primary  → ₹8,000
Replica  → ₹10,000

       ↓ replication

Time 2:

Primary  → ₹8,000
Replica  → ₹8,000
```

This is why replication lag is closely related to eventual consistency in many distributed architectures.

---

## 21. Real-Time Example — Social Media

Imagine a social media platform:

```
                  User
                   │
                   ▼
              Application
                   │
                   ▼
               Primary
              /       \
             ▼         ▼
         Replica 1  Replica 2
```

A user posts:

    "Hello World"

The primary stores it:

    Post ID = 100

Replication happens asynchronously:

```
Primary
  │
  ├────────→ Replica 1
  │
  └────────→ Replica 2
```

A user immediately refreshes and gets routed to Replica 2.

If replication hasn't completed:

    Post 100 → Not visible yet

A moment later:

    Post 100 → Visible

This temporary inconsistency may be acceptable for a social feed.

---

## 22. Real-Time Example — Banking

For banking:

    Account Balance

you generally need much stronger correctness guarantees.

You don't want:

    Primary → ₹8,000
    Replica → ₹10,000

and then make a financial decision based on the stale replica.

Therefore, critical operations often use stronger consistency/coordination mechanisms and carefully control which replicas are allowed to serve particular reads.

---

## 23. Replication Strategy in System Design

When designing a system, ask:

### Question 1: How many reads vs writes?

If:

    Reads >> Writes

read replicas can help.

### Question 2: How much replication lag is acceptable?

If:

    Lag = milliseconds

maybe replicas are suitable for many reads.

If:

    Lag = unacceptable

you may need stronger consistency or primary reads.

### Question 3: What happens if the primary fails?

Define:

    Failover strategy

### Question 4: Can replicas serve stale data?

For example:

    Product recommendations → YES
    Bank balance → Usually NO

### Question 5: Where are replicas located?

Possibilities:

    Same machine ❌
    Same AZ
    Different AZ
    Different region
    Different continent

Geographic replication improves disaster resilience but increases network latency and replication complexity.

---

## 24. Typical Production Architecture

A scalable system might look like:

```
                       Users
                         │
                         ▼
                  Load Balancer
                         │
                         ▼
                  Application Tier
                         │
              ┌──────────┴──────────┐
              │                     │
           Writes                  Reads
              │                     │
              ▼                     ▼
        ┌───────────┐       ┌──────────────┐
        │ Primary DB│──────►│ Read Replicas│
        └───────────┘       └──────────────┘
              │                     │
              │                     ├── Replica 1
              │                     ├── Replica 2
              │                     └── Replica 3
              │
              ▼
          Backup / DR
```

This architecture provides:

- Write handling through the primary
- Read scaling through replicas
- Failover capability
- Better fault tolerance
- Potential disaster recovery

---

## 25. Replication vs Backup

These are not the same.

### Replication

```
Primary ─────→ Replica
```

Designed primarily for:

- Availability
- Read scaling
- Failover

If you accidentally execute:

```sql
DELETE FROM users;
```

the deletion may also be replicated.

So replication alone does not protect you from logical mistakes.

### Backup

```
Database ─────→ Backup
```

Used for:

- Point-in-time recovery
- Accidental deletion
- Corruption recovery
- Disaster recovery

**A production system typically needs both replication and backups.**

---

## 26. Important Interview Question

### "Why do we need replication if we already have backups?"

**Answer:**

> Backups primarily help with recovery from data loss, corruption, or accidental changes, while replication primarily improves availability, failover, and read scalability. A replica can quickly take over when a primary fails, whereas restoring a backup can take much longer. Replication and backups solve different problems and are often used together.

---

## 27. Replication + Failover

The complete picture:

```
                    Application
                         │
                         ▼
                  Database Endpoint
                         │
                         ▼
                    Primary DB
                   /          \
                  ▼            ▼
             Replica 1     Replica 2
                  │            │
                  └─────┬──────┘
                        │
                   Monitoring
                        │
                Primary failure?
                        │
                       YES
                        │
                        ▼
                Promote Replica
                        │
                        ▼
                  New Primary
```

---

## 28. Key Terms to Remember

| Term | Meaning |
|---|---|
| Primary | Usually accepts writes |
| Replica | Copy of primary data |
| Replication | Copying changes between nodes |
| Synchronous | Primary waits for required replica acknowledgement |
| Asynchronous | Replica catches up later |
| Replication Lag | Replica is behind primary |
| Failover | Switch to another node after failure |
| Read Replica | Replica primarily used for reads |
| Multi-Primary | Multiple nodes accept writes |
| Conflict Resolution | Handling concurrent conflicting writes |

---

## 29. Interview Answer

**If an interviewer asks:**

### "What is database replication?"

A strong answer would be:

> Database replication is the process of maintaining copies of database data across multiple database nodes. It is primarily used to improve availability, fault tolerance, read scalability, and disaster resilience. A common architecture has one primary handling writes and multiple replicas handling reads. Replication can be synchronous or asynchronous. With asynchronous replication, replicas can lag behind the primary, so the system needs to consider stale reads and read-after-write consistency.

---

## 30. Final Mental Model

Keep this picture in mind:

```
                     DATABASE REPLICATION
                              │
              ┌───────────────┼────────────────┐
              │               │                │
              ▼               ▼                ▼
          Availability    Read Scaling    Fault Tolerance
              │
              ▼
           Primary
              │
       ┌──────┼──────┐
       ▼      ▼      ▼
      R1     R2     R3
       │      │      │
       └──────┼──────┘
              │
              ▼
        Replication
              │
       ┌──────┴──────┐
       ▼             ▼
  Synchronous   Asynchronous
       │             │
       ▼             ▼
 Lower lag       Possible lag
 Higher latency  Lower latency
```

---

### The Most Important Distinction

**Replication** = copying the same data to multiple database nodes.

**Sharding** = splitting different data across multiple database nodes.

Replication improves availability/read scalability; sharding primarily improves data and write scalability.

And when you combine them:

```
                    Database Cluster
                           │
          ┌────────────────┼────────────────┐
          ▼                ▼                ▼
       Shard 1          Shard 2          Shard 3
          │                │                │
       Primary           Primary           Primary
          │                │                │
        Replicas         Replicas         Replicas
```

This **sharding + replication** architecture is one of the most important patterns you'll encounter in large-scale system design.
