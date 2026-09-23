# Database Replication

Database replication means keeping copies of the same data on multiple database servers.

The main reasons are:

- High availability
- Fault tolerance
- Read scalability
- Disaster recovery
- Reducing load on the primary database

A simple way to remember it:

> **Sharding splits data. Replication copies data.**

---

## 1. Without Replication

Suppose our application has only one database:

```
              Application
                   |
                   v
             +-----------+
             | Database  |
             +-----------+
```

All requests go to this database:

```
10,000 READ requests
        +
1,000 WRITE requests
        |
        v
   Single Database
```

If the database goes down:

```
              Application
                   |
                   X
                   |
             +-----------+
             | DB DOWN ❌ |
             +-----------+
```

The application may become unavailable.

---

## 2. With Replication

Now create multiple copies:

```
                 Application
                      |
               +------+------+
               |             |
             WRITE          READ
               |             |
               v             v
          +---------+    +---------+
          | Primary |    | Replica |
          +---------+    +---------+
               |
               | Replication
               v
          +---------+
          | Replica |
          +---------+
```

The Primary handles writes.

The Replicas contain copies of the data and can usually serve reads.

---

## 3. Real-Time Example — Amazon-like E-Commerce

Suppose we have:

- Users
- Orders
- Products
- Payments

A customer places an order:

```
POST /orders
```

The application sends the write to the primary:

```
Application
     |
     | INSERT
     v
+-------------+
|   Primary   |
+-------------+
      |
      | Replicate
      +------------+
      |            |
      v            v
+-----------+  +-----------+
| Replica 1 |  | Replica 2 |
+-----------+  +-----------+
```

Primary:

```sql
INSERT INTO orders
(order_id, user_id, amount)
VALUES
(101, 5001, 2500);
```

The change is then propagated to the replicas.

---

## 4. Read Scaling

Imagine your application receives:

```
100,000 READ requests
10,000 WRITE requests
```

If everything goes to one database:

```
                  110,000 requests
                         |
                         v
                  +-----------+
                  |  Primary  |
                  +-----------+
                         🔥
```

Instead:

```
                       Application
                            |
             +--------------+--------------+
             |              |              |
           WRITE           READ           READ
             |              |              |
             v              v              v
        +---------+    +---------+    +---------+
        | Primary |    |Replica 1|    |Replica 2|
        +---------+    +---------+    +---------+
```

Now reads are distributed.

This is called:

> **Read scaling**

---

## 5. Primary-Replica Architecture

This is probably the most important replication architecture to understand.

```
                   Application
                       |
                 Load Balancer
                       |
              +--------+--------+
              |                 |
            WRITE              READ
              |                 |
              v                 v
        +-----------+     +-----------+
        |  Primary  |---->| Replica 1 |
        +-----------+     +-----------+
              |
              +-----------> +-----------+
                            | Replica 2 |
                            +-----------+
```

**Primary:**

```
INSERT
UPDATE
DELETE
```

**Replicas:**

```
SELECT
```

---

## 6. What Exactly Gets Replicated?

Suppose the primary contains:

**Users**

```
ID    Name
------------
1     John
2     Alice
3     Bob
```

**Replica 1:**

```
Users

ID    Name
------------
1     John
2     Alice
3     Bob
```

**Replica 2:**

```
Users

ID    Name
------------
1     John
2     Alice
3     Bob
```

They are copies of the same logical data.

---

## 7. How Does Replication Actually Happen?

At a high level:

```
Application
     |
     | UPDATE
     v
 Primary DB
     |
     | Write change to replication log
     v
Replication Log
     |
     +------------+
     |            |
     v            v
 Replica 1    Replica 2
     |            |
     v            v
Apply change  Apply change
```

Different databases implement this differently.

For example, relational databases commonly use a transaction/change log that replicas consume.

You don't need to memorize the implementation details initially.

The important concept is:

> **Primary records the change → replicas receive the change → replicas apply the change.**

---

## 8. Synchronous vs Asynchronous Replication

This is a very important system-design interview topic.

There are two major approaches:

```
Replication
    |
    +-------------------+
    |                   |
    v                   v
Synchronous       Asynchronous
```

---

## 9. Synchronous Replication

The primary waits until the replica confirms the write.

```
Application
    |
    | WRITE
    v
Primary
    |
    | replicate
    v
Replica
    |
    | ACK
    v
Primary
    |
    | success
    v
Application
```

For example:

```
Application
     |
     v
Primary
     |
     | Write
     v
Replica
     |
     | ACK
     v
Primary
     |
     v
Success
```

### Advantage

Very strong consistency between primary and replica.

### Disadvantage

Higher latency.

If the replica is slow:

```
Primary
   |
   | waiting...
   v
Replica
   |
   | slow network
   |
   |-------->
```

the write becomes slower.

---

## 10. Asynchronous Replication

The primary doesn't wait for replicas to confirm.

```
Application
     |
     v
Primary
     |
     | success
     v
Application

Primary
   |
   | replicate asynchronously
   v
Replica
```

The user gets the response quickly.

### Advantage

Lower write latency.

### Disadvantage

There can be a delay before replicas receive the latest data.

This creates:

> **Replication lag**

---

## 11. Replication Lag

Suppose:

**Primary:**

```
Balance = ₹10,000
```

Customer withdraws ₹2,000.

Primary immediately becomes:

```
Balance = ₹8,000
```

But replica hasn't received the update yet:

**Replica:**

```
Balance = ₹10,000
```

For a short period:

```
Primary → ₹8,000
Replica → ₹10,000
```

This is **replication lag**.

---

## 12. Real-World Problem with Replication Lag

Consider an e-commerce application.

Customer buys the last iPhone.

```
POST /orders
```

**Primary:**

```
stock = 0
```

But replica still says:

```
stock = 1
```

If the next request reads from the replica:

```
GET /products/iphone
```

it might incorrectly show:

```
Only 1 left
```

even though the primary says:

```
Out of stock
```

This is one reason you need to understand consistency when using replicas.

---

## 13. Read-After-Write Consistency Problem

This is a very common interview scenario.

User updates their profile:

```
PUT /users/5001

name = "Kaushal"
```

Write goes to:

> Primary

Immediately afterward:

```
GET /users/5001
```

Load balancer sends the read to:

> Replica

But replication hasn't caught up.

The user sees the old name.

```
WRITE
  |
  v
Primary
  |
  | not replicated yet
  X
Replica
  |
  v
READ
  |
  v
Old data ❌
```

This is called a **read-after-write consistency issue**.

---

## 14. One Common Solution

After a write, route subsequent reads temporarily to the primary.

```
WRITE
  |
  v
Primary
  |
  v
READ
  |
  v
Primary
```

Once replication catches up, reads can return to replicas.

Another option is to use a consistency mechanism that ensures the chosen replica has caught up sufficiently before serving the read.

---

## 15. What Happens If the Primary Fails?

This is where replication becomes extremely useful.

Initially:

```
             Primary
                |
        +-------+-------+
        |               |
        v               v
    Replica 1       Replica 2
```

Primary crashes:

```
             Primary ❌
                |
        +-------+-------+
        |               |
        v               v
    Replica 1       Replica 2
```

A replica can be promoted:

```
             Primary ❌

                 ↓

             Replica 1
             NEW PRIMARY
                 |
                 v
             Replica 2
```

This is called:

> **Failover**

---

## 16. Automatic Failover

In production systems, you generally don't want a human to manually say:

> "Primary is down. Please make Replica 1 primary."

Instead, a cluster management system can detect failure:

```
                Monitor
                  |
        +---------+---------+
        |         |         |
        v         v         v
     Primary   Replica1  Replica2
        |
        X
      DOWN
```

The system can elect/promote a new primary:

```
Replica 1
    ↓
New Primary
```

The application then needs to discover the new primary.

---

## 17. Replication vs Sharding

This distinction is extremely important.

### Replication

Copies the same data.

```
       Same Data
       /   |   \
      v    v    v
   DB 1  DB 2  DB 3
```

### Sharding

Splits the data.

```
         All Data
            |
    +-------+-------+
    |       |       |
    v       v       v
 Shard 1 Shard 2 Shard 3

 Data A   Data B   Data C
```

---

## 18. You Can Combine Both

Large production systems commonly use:

```
                 Application
                      |
                Shard Router
                      |
        +-------------+-------------+
        |             |             |
        v             v             v
     Shard 1       Shard 2       Shard 3
        |             |             |
     +--+--+       +--+--+       +--+--+
     |     |       |     |       |     |
     v     v       v     v       v     v
 Primary Replica Primary Replica Primary Replica
```

This gives you:

**Sharding**

Scales the amount of data and distributes writes.

**Replication**

Provides:

- High availability
- Failover
- Read scaling
- Fault tolerance

---

## 19. Replication Doesn't Automatically Solve Everything

Suppose you have:

```
Primary
   |
   +--- Replica 1
   +--- Replica 2
```

If the entire data center goes down:

```
Data Center ❌
```

all three could become unavailable.

Therefore, production systems may distribute replicas across:

```
Region A
   |
Primary

Region B
   |
Replica

Region C
   |
Replica
```

This helps with disaster recovery.

---

## 20. Replication and Consistency

There is a trade-off:

```
Strong consistency
       ↕
Higher latency / coordination

Eventual consistency
       ↕
Lower latency / temporary stale reads
```

Synchronous replication generally gives stronger consistency guarantees but can increase latency and reduce availability under certain failures.

Asynchronous replication usually gives better performance and availability but allows temporary stale reads.

This connects directly to the **CAP theorem** and distributed-system consistency models.

---

## 21. Real-Time Example: Banking

Suppose:

```
Account balance = ₹50,000
```

Customer withdraws:

```
₹20,000
```

The primary becomes:

```
₹30,000
```

For a banking transaction, you don't want a stale replica to tell another critical operation:

```
Balance = ₹50,000
```

So critical financial operations generally need carefully designed consistency guarantees and should not blindly read from an asynchronously replicated replica.

---

## 22. Real-Time Example: Social Media

Suppose you're viewing Instagram-like posts.

Millions of users are performing:

```
GET /feed
```

These are mostly reads.

Instead of:

```
             Primary
             🔥🔥🔥🔥
```

you can use:

```
              Primary
                 |
        +--------+--------+
        |        |        |
        v        v        v
     Replica  Replica  Replica
        |        |        |
       READ     READ     READ
```

This dramatically increases read capacity.

---

## 23. When Should You Use Replication?

A typical progression is:

```
Single Database
      |
      v
Vertical Scaling
      |
      v
Read Replicas
      |
      v
Caching
      |
      v
Sharding
      |
      v
Sharding + Replication
```

Don't immediately shard a database just because your application is growing.

Often, replication + caching + proper indexing can handle a substantial amount of traffic before sharding becomes necessary.

---

## 24. Important Interview Question

> "Why do we need replication if we already have sharding?"

Because they solve different problems.

Suppose:

```
Shard 1
Shard 2
Shard 3
```

If Shard 2 crashes:

```
Shard 2 ❌
```

all data belonging to Shard 2 may become unavailable.

Instead:

```
Shard 1
 ├── Primary
 └── Replica

Shard 2
 ├── Primary
 └── Replica

Shard 3
 ├── Primary
 └── Replica
```

Now if:

```
Shard 2 Primary ❌
```

you can promote:

```
Shard 2 Replica
       ↓
New Primary
```

So:

> **Sharding gives scalability; replication gives redundancy and availability.**

---

## 25. Quick Comparison

| Feature | Replication | Sharding |
|---|---|---|
| Purpose | Copy data | Split data |
| Data | Same data | Different data |
| Read scaling | ✅ | ✅ |
| Write scaling | Limited | ✅ |
| Storage scaling | Limited | ✅ |
| High availability | ✅ | Not by itself |
| Failover | ✅ | Not by itself |
| Complexity | Medium | High |
| Cross-node queries | Sometimes | Frequently more complex |

---

## 26. The Mental Model to Remember

Think about a library.

### Replication

You photocopy the entire library:

```
Library A
    ↓ copy
Library B
    ↓ copy
Library C
```

Every library has the same books.

### Sharding

You divide the books:

```
Library A → Books A-M
Library B → Books N-Z
```

Each library has different books.

### Sharding + Replication

You divide the books and then make copies:

```
             All Books
                 |
       +---------+---------+
       |         |         |
       v         v         v
     A-M       N-T       U-Z
      |         |         |
    copy      copy      copy
```

That's essentially:

> **Shard the data, then replicate each shard.**

---

## What You Should Learn Next

Since you're building your system-design foundation, I'd go in this order:

```
Database Scaling
      ↓
Read Replication
      ↓
Primary-Replica Architecture
      ↓
Sync vs Async Replication
      ↓
Replication Lag
      ↓
Read-after-write consistency
      ↓
Failover & Leader Election
      ↓
Sharding
      ↓
Sharding + Replication
      ↓
CAP Theorem
      ↓
Consistency Models
```
