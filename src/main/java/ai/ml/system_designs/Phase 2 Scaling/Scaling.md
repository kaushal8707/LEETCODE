# Database Scaling

Database scaling means increasing a database's ability to handle more:

- 👥 Users
- 📥 Requests
- 💾 Data
- ⚡ Queries
- 🔄 Concurrent transactions

As your application grows, the database often becomes one of the biggest bottlenecks.

A typical system starts like this:

```
                Users
                  |
                  v
             Load Balancer
                  |
                  v
            App Server
                  |
                  v
              Database
```

Initially, this may work perfectly.

But imagine:

```
10 users
   ↓
100 users
   ↓
10,000 users
   ↓
1,000,000 users
```

Eventually:

```
Application
     |
     | Millions of queries
     v
+------------------+
|    Database      |
|   CPU = 100%     |
|   Connections ↑  |
|   I/O ↑          |
+------------------+
```

Now we need database scaling.

---

## 1. Types of Database Scaling

There are two fundamental approaches:

```
                    Database Scaling
                          |
              +-----------+-----------+
              |                       |
              v                       v
       Vertical Scaling        Horizontal Scaling
          (Scale Up)              (Scale Out)
```

---

## 2. Vertical Scaling — Scale Up

Vertical scaling means increasing the resources of the existing database server.

For example:

**Before:**

```
+----------------------+
| Database Server      |
| 4 CPU                |
| 16 GB RAM            |
| 500 GB SSD           |
+----------------------+
```

Upgrade it:

**After:**

```
+----------------------+
| Database Server      |
| 32 CPU               |
| 128 GB RAM           |
| 4 TB SSD             |
+----------------------+
```

You are making the same database machine more powerful.

### Advantages

- Simple
- Easy to implement
- No application changes in many cases
- Good starting point

### Disadvantages

- Hardware has limits
- Can become expensive
- Single-server failure can still be a problem
- Doesn't scale indefinitely

---

## 3. Horizontal Scaling — Scale Out

Horizontal scaling means adding more database machines.

Instead of:

```
             Database
                |
             Server 1
```

you have:

```
             Database
            /        \
       Server 1     Server 2
```

Or:

```
             Database
          /      |      \
         v       v       v
       DB1     DB2     DB3
```

This is much more powerful, but also more complicated.

---

## 4. Read Replicas

One of the most common database scaling techniques is read replication.

Suppose your application has:

- 100,000 reads
- 10,000 writes

The database is receiving a lot of read traffic.

We can create replicas:

```
                    Application
                         |
                         v
                  Primary Database
                    /          \
                   /            \
                  v              v
             Read Replica 1   Read Replica 2
```

The primary handles writes:

- `INSERT`
- `UPDATE`
- `DELETE`

Replicas handle reads:

- `SELECT`

For example:

```
                    Application
                    /         \
                   /           \
              WRITE           READ
                 |               |
                 v               v
             Primary       Read Replicas
                           /          \
                          v            v
                        DB1           DB2
```

---

## 5. Real-World Example

Imagine an e-commerce application.

Users perform:

```
GET /products
GET /products/123
GET /products/456
GET /categories
GET /reviews
```

These are mostly read operations.

But writes might be:

```
POST /orders
POST /payments
UPDATE inventory
```

We can route them differently:

```
                    Application
                   /            \
                  /              \
             WRITE              READ
               |                  |
               v                  v
          Primary DB        Read Replica
                                  |
                                  v
                            Read Replica
```

This can dramatically reduce the workload on the primary database.

---

## 6. Replication

How does the replica get the data?

The primary database sends changes to replicas.

```
              Primary
                 |
       Replication Stream
          /            \
         v              v
      Replica 1      Replica 2
```

For example:

Primary:

```
User ID = 101
Name = Kaushal
```

The replicas eventually receive the same change.

This introduces an important concept:

**Replication Lag**

---

## 7. Replication Lag

Suppose:

```
10:00:00 → User updates profile
```

The write goes to the primary:

```
Primary
  |
  | UPDATE
  v
Name = "John"
```

But the replica hasn't received the update yet:

```
Replica
Name = "Kaushal"
```

A subsequent read might return stale data.

```
Write → Primary
          |
          | replication delay
          v
       Replica
          |
          v
      Old value
```

This is called **replication lag**.

It is one of the most important trade-offs when using read replicas.

---

## 8. Primary-Replica Architecture

A common architecture:

```
                         Application
                        /           \
                       /             \
                  Writes             Reads
                     |                 |
                     v                 v
              +------------+     +------------+
              |  Primary   |---->|  Replica 1 |
              +------------+     +------------+
                     |             |
                     +-----------> |
                                   |
                             +------------+
                             |  Replica 2 |
                             +------------+
```

The primary is sometimes called:

- Primary
- Leader
- Master

Replicas may be called:

- Read replicas
- Followers
- Slaves — older terminology

---

## 9. Database Sharding

Read replicas primarily help with read scalability.

But what if the database contains enormous amounts of data and receives huge numbers of reads and writes?

Then we may use:

**Sharding**

Sharding means splitting data across multiple databases.

For example:

```
             Application
                  |
                  v
             Shard Router
          /       |       \
         /        |        \
        v         v         v
      Shard 1   Shard 2   Shard 3
      Users     Users     Users
      1-1M      1M-2M     2M-3M
```

Each shard contains only part of the data.

---

## 10. Example of Sharding

Suppose we have 300 million users.

Instead of:

```
              One Database
          300 Million Users
```

we can split:

```
Shard 1 → User ID 1 - 100M
Shard 2 → User ID 100M - 200M
Shard 3 → User ID 200M - 300M
```

Now:

```
             Application
                  |
                  v
             Shard Router
            /      |      \
           v       v       v
         DB1      DB2     DB3
```

Each database handles a smaller amount of data and traffic.

---

## 11. Sharding Key

The value used to determine which shard stores the data is called the:

**Shard Key**

For example:

```
userId
```

could be the shard key.

Suppose:

```
userId = 12345
```

The application/router determines:

```
12345 → Shard 1
```

Another:

```
userId = 245678901
```

might go to:

```
Shard 3
```

---

## 12. Hash-Based Sharding

A common approach is:

```
shard = hash(userId) % numberOfShards
```

Suppose:

```
userId = 12345
numberOfShards = 4
```

Conceptually:

```
hash(12345) % 4 = 2
```

Therefore:

```
User → Shard 2
```

This can distribute users relatively evenly.

---

## 13. Range-Based Sharding

Another approach is range-based:

```
Shard 1 → 1 - 1,000,000
Shard 2 → 1,000,001 - 2,000,000
Shard 3 → 2,000,001 - 3,000,000
```

Example:

```
User ID = 1,500,000

             |
             v
          Shard 2
```

### Problem

Data can become uneven.

For example, if newer users are much more active:

```
Shard 1 → Low traffic
Shard 2 → Low traffic
Shard 3 → VERY HIGH traffic
```

This is called a **hot shard**.

---

## 14. Consistent Hashing

Consistent hashing can help when the number of shards changes.

Normal hashing:

```
hash(key) % 3
```

If you change:

```
3 shards → 4 shards
```

many keys may map to different shards.

That means lots of data movement.

Consistent hashing reduces the amount of data that needs to move when nodes are added or removed.

This concept is especially important in distributed systems.

---

## 15. Database Indexing

Before immediately scaling the database horizontally, we should optimize queries.

One of the most important techniques is:

**Indexing**

Suppose:

```sql
SELECT *
FROM users
WHERE email = 'kaushal@example.com';
```

Without an index, the database might scan many rows:

```
Row 1
Row 2
Row 3
...
Row 10,000,000
```

With an index:

```
                    Index
                      |
email ───────────────> User ID
```

The database can find the row much faster.

For example:

```sql
CREATE INDEX idx_users_email
ON users(email);
```

---

## 16. Query Optimization

Sometimes the problem isn't database capacity.

The problem is a bad query.

For example:

```sql
SELECT *
FROM orders;
```

If the table contains:

```
500 million rows
```

you're asking the database to potentially process an enormous amount of data.

Better:

```sql
SELECT order_id, total
FROM orders
WHERE user_id = 12345
LIMIT 20;
```

So before scaling:

```
Bad Query
   ↓
Optimize Query
   ↓
Add Index
   ↓
Caching
   ↓
Read Replicas
   ↓
Sharding
   ↓
Vertical / Horizontal Scaling
```

---

## 17. Database Connection Pooling

Another major database bottleneck is connections.

Suppose you have:

```
100 application servers
```

and each opens:

```
100 DB connections
```

Then:

```
100 × 100 = 10,000 connections
```

The database may not be able to handle that many.

Connection pooling allows applications to reuse connections.

```
Application
    |
    v
Connection Pool
 /  |  |  \
v   v  v   v
DB connections
    |
    v
 Database
```

Instead of repeatedly:

```
Open connection
Query
Close connection
```

you reuse existing connections.

---

## 18. Database Caching

You can also reduce database traffic using a cache such as Redis.

Architecture:

```
User
 |
 v
Application
 |
 v
Redis
 |
 | Cache HIT
 v
Response
```

If cache miss:

```
User
 |
 v
Application
 |
 v
Redis
 |
 | MISS
 v
Database
 |
 v
Redis
 |
 v
User
```

So:

```
Cache
 ↓
Database
```

This can dramatically reduce database reads.

---

## 19. Partitioning vs Sharding

These are often confused.

### Partitioning

Splitting data within the database.

```
             One Database
                  |
        +---------+---------+
        |         |         |
     Part 1     Part 2    Part 3
```

### Sharding

Splitting data across different database instances/servers.

```
             Application
                  |
        +---------+---------+
        |         |         |
        v         v         v
       DB1       DB2       DB3
```

Simple way to remember:

- **Partitioning** = split data.
- **Sharding** = distribute partitions across machines/databases.

---

## 20. Database Scaling Strategy

In a real system, you usually don't jump directly to sharding.

A reasonable progression is:

```
                    Database
                       |
                       v
                Optimize Queries
                       |
                       v
                  Add Indexes
                       |
                       v
                     Cache
                       |
                       v
              Connection Pooling
                       |
                       v
                Read Replicas
                       |
                       v
               Vertical Scaling
                       |
                       v
                   Sharding
```

The exact order depends on the workload.

---

## 21. Complete Scalable Architecture

Now combine the concepts we've discussed:

```
                         Users
                           |
                           v
                         CDN
                           |
                           v
                      Load Balancer
                           |
              +------------+------------+
              |            |            |
              v            v            v
            App 1        App 2        App 3
              |            |            |
              +------------+------------+
                           |
                           v
                         Redis
                           |
                    Cache Miss
                           |
                           v
                     DB Router
                    /    |    \
                   /     |     \
                  v      v      v
              Primary   Replica Replica
                DB        1       2
                 |
                 |
              Sharding
           /      |      \
          v       v       v
        Shard1  Shard2  Shard3
```

This is getting closer to a large-scale production architecture.

---

## 22. When Should You Use What?

| Problem | Possible Solution |
|---|---|
| Slow query | Query optimization |
| Full table scan | Index |
| Too many repeated reads | Cache |
| Too many DB connections | Connection pooling |
| Too many reads | Read replicas |
| Database CPU/RAM insufficient | Vertical scaling |
| Huge dataset | Partitioning / sharding |
| Huge read + write traffic | Sharding |
| Single DB failure | Replication / failover |
| Uneven shard traffic | Better shard key / rebalancing |

---

## 23. Most Important Trade-offs

Database scaling isn't simply:

> "Add more databases."

Every technique introduces trade-offs.

**Read replicas**
- ✅ Better read scalability
- ❌ Replication lag

**Sharding**
- ✅ Huge scalability
- ❌ Complex queries
- ❌ Cross-shard transactions
- ❌ Rebalancing complexity

**Caching**
- ✅ Very fast reads
- ❌ Stale data
- ❌ Cache invalidation complexity

**Vertical scaling**
- ✅ Simple
- ❌ Hardware limits
- ❌ Expensive at higher levels

---

## 24. Interview Perspective

If an interviewer asks:

> "Our database is becoming a bottleneck. How would you scale it?"

Don't immediately say:

> "I'll use sharding."

Instead, walk through the problem:

```
1. Identify bottleneck
       ↓
2. Analyze slow queries
       ↓
3. Add appropriate indexes
       ↓
4. Optimize queries/schema
       ↓
5. Add caching
       ↓
6. Use connection pooling
       ↓
7. Separate reads and writes
       ↓
8. Add read replicas
       ↓
9. Vertically scale if appropriate
       ↓
10. Shard when one database can no longer
    handle the workload/data
```

The key system-design principle is:

> **Scale only the component that is actually the bottleneck.**

### Mental Model

```
                DATABASE SCALING
                       |
        +--------------+--------------+
        |              |              |
        v              v              v
    Scale Up       Read Scaling    Write/Data Scaling
        |              |              |
        v              v              v
   More CPU/RAM   Read Replicas     Sharding
                                  Partitioning
```

- **Vertical scaling** → make one database stronger.
- **Read replicas** → handle more reads.
- **Sharding** → distribute data and traffic across databases.
