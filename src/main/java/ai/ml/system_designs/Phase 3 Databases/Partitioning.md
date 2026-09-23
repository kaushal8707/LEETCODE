# Database Partitioning

Since we're building your system-design fundamentals step by step, Partitioning is an important concept to understand before going deeper into sharding.

Database partitioning means dividing a large table into smaller logical pieces called **partitions**, while the database still treats them as one logical table.

The key difference from what we just discussed:

> **Partitioning** = split data inside a database.
> **Sharding** = split data across multiple database servers.
> **Replication** = copy the same data to multiple servers.

---

## 1. Why Do We Need Partitioning?

Imagine an e-commerce application has an orders table:

```
orders
--------------------------------
1 billion rows
--------------------------------
order_id
user_id
amount
status
created_at
```

A query such as:

```sql
SELECT *
FROM orders
WHERE created_at >= '2026-08-01';
```

may have to deal with a huge amount of data.

Instead of keeping everything as one massive physical structure, we can partition the table.

```
                  orders
                    |
        +-----------+-----------+
        |           |           |
        v           v           v
   Partition 1 Partition 2 Partition 3
   Jan-Mar     Apr-Jun       Jul-Sep
```

The application still sees:

> orders

but internally the database stores the data in different partitions.

---

## 2. Real-Time Example — E-Commerce Orders

Suppose Amazon-like application has:

**orders**

```
order_id | user_id | amount | created_at
---------+---------+--------+------------
1        | 101     | 500    | Jan 2026
2        | 102     | 800    | Feb 2026
3        | 103     | 300    | Mar 2026
...
```

We could partition by `created_at`.

```
orders
   |
   +---- Partition 2026-Q1
   |
   +---- Partition 2026-Q2
   |
   +---- Partition 2026-Q3
   |
   +---- Partition 2026-Q4
```

For example:

```
Partition Q1
Jan → Mar

Partition Q2
Apr → Jun

Partition Q3
Jul → Sep

Partition Q4
Oct → Dec
```

---

## 3. Partition Pruning

This is one of the biggest benefits of partitioning.

Suppose we execute:

```sql
SELECT *
FROM orders
WHERE created_at >= '2026-07-01'
  AND created_at < '2026-10-01';
```

The database can determine:

```
Query
  |
  v
created_at = Q3
  |
  v
Scan only Q3
```

Instead of:

```
Scan Q1 ❌
Scan Q2 ❌
Scan Q3 ✅
Scan Q4 ❌
```

This is called:

> **Partition pruning**

It can significantly reduce the amount of data that needs to be scanned.

---

## 4. Partitioning Is Still One Database

This is very important.

**With partitioning:**

```
                Application
                     |
                     v
              +-------------+
              |   Database  |
              +-------------+
                     |
          +----------+----------+
          |          |          |
          v          v          v
       Part-1     Part-2     Part-3
```

All partitions belong to the same database system.

**With sharding:**

```
                Application
                     |
                Shard Router
                     |
          +----------+----------+
          |          |          |
          v          v          v
       DB Server  DB Server  DB Server
       Shard 1    Shard 2    Shard 3
```

These are independent database instances/nodes.

---

## 5. Types of Partitioning

The three major types you should know are:

```
Partitioning
     |
     +----------------+
     |                |
     v                v
   Range            List
     |
     +------+
            |
            v
          Hash
```

Let's understand each.

---

## 6. Range Partitioning

Data is divided according to a range of values.

For example:

**order_id** could be divided:

```
1 - 1,000,000       → Partition 1
1,000,001 - 2M      → Partition 2
2M - 3M              → Partition 3
```

Or more commonly for historical data:

**created_at**

```
Jan-Mar → Partition 1
Apr-Jun → Partition 2
Jul-Sep → Partition 3
```

### Example

```
orders
   |
   +---- 2026-Q1
   |
   +---- 2026-Q2
   |
   +---- 2026-Q3
   |
   +---- 2026-Q4
```

### Best Use Case

Time-series or historical data.

Examples:

- Orders
- Logs
- Transactions
- Events
- Sensor data

---

## 7. Real-World Example — Application Logs

Suppose your application generates:

> 10 million logs/day

After 3 years:

```
10 million × ~1,095 days
≈ 10.95 billion rows
```

Partition by date:

```
logs
 |
 +-- 2026-01
 +-- 2026-02
 +-- 2026-03
 +-- ...
```

Query:

```sql
SELECT *
FROM logs
WHERE created_at >= '2026-08-01'
AND created_at < '2026-09-01';
```

The database can potentially scan only:

> 2026-08 partition

rather than billions of rows.

---

## 8. List Partitioning

In list partitioning, you explicitly define categories.

Suppose:

> country

We could have:

```
India       → Partition 1
USA         → Partition 2
UK          → Partition 3
Germany     → Partition 4
```

Conceptually:

```
users
 |
 +---- India
 |
 +---- USA
 |
 +---- UK
 |
 +---- Germany
```

A query:

```sql
SELECT *
FROM users
WHERE country = 'India';
```

can potentially target only the India partition.

---

## 9. Problem with List Partitioning

What happens if:

```
India → 70% of users
USA   → 10%
UK    → 10%
Others → 10%
```

Then:

```
India Partition
🔥🔥🔥🔥🔥🔥🔥🔥
70% data
```

while others are much smaller.

This creates **data skew**.

So you need to be careful when choosing the partition key.

---

## 10. Hash Partitioning

Hash partitioning uses a hash function.

Suppose:

> user_id

is the partition key.

Conceptually:

```
hash(user_id) % 4
```

Then:

```
user 101 → Partition 2
user 102 → Partition 0
user 103 → Partition 3
user 104 → Partition 1
```

This tends to distribute data more evenly.

```
Partition 0 → ~25%
Partition 1 → ~25%
Partition 2 → ~25%
Partition 3 → ~25%
```

---

## 11. Range vs Hash Partitioning

| Feature | Range | Hash |
|---|---|---|
| Distribution | Can be uneven | Usually more even |
| Range queries | ⭐⭐⭐⭐⭐ | ⭐ |
| Equality queries | ⭐⭐⭐ | ⭐⭐⭐⭐ |
| Hotspot risk | Higher | Lower |
| Time-series data | Excellent | Poor |
| Easy to understand | Excellent | Good |

For example:

Query:

```sql
WHERE created_at BETWEEN
'2026-01-01' AND '2026-01-31'
```

Range partitioning is excellent.

But:

```sql
WHERE user_id = 12345
```

hash partitioning can be useful.

---

## 12. Composite / Multi-Level Partitioning

You can also combine strategies.

For example:

```
orders
   |
   +---- 2026-Q1
   |       |
   |       +--- Hash partition 1
   |       +--- Hash partition 2
   |       +--- Hash partition 3
   |
   +---- 2026-Q2
   |       |
   |       +--- Hash partition 1
   |       +--- Hash partition 2
   |       +--- Hash partition 3
```

For a very large order system, you might conceptually use:

```
created_at → range
user_id    → hash
```

This can give you both:

- efficient time-based queries
- better distribution within a time period

---

## 13. Partitioning vs Indexing

These are different.

### Index

An index helps the database find rows faster.

```
orders
   |
   +---- Index on user_id
```

### Partitioning

Partitioning divides the table into separate physical/logical pieces.

```
orders
   |
   +---- Partition 1
   +---- Partition 2
   +---- Partition 3
```

You can use both:

```
Partitioning
     +
Indexes
     =
Better query performance
```

For example:

```
orders
 |
 +-- Partition Jan
 |      |
 |      +-- Index(user_id)
 |
 +-- Partition Feb
 |      |
 |      +-- Index(user_id)
 |
 +-- Partition Mar
        |
        +-- Index(user_id)
```

---

## 14. Partitioning vs Sharding

This is a very common interview question.

### Partitioning

```
               One DB
                 |
        +--------+--------+
        |        |        |
        v        v        v
      Part 1   Part 2   Part 3
```

### Sharding

```
             Application
                  |
        +---------+---------+
        |         |         |
        v         v         v
       DB1       DB2       DB3
     Shard 1   Shard 2   Shard 3
```

The biggest distinction:

> Partitioning is generally within a database system; sharding distributes data across separate database nodes/instances.

---

## 15. Partitioning vs Replication

Also remember:

### Replication

```
             Primary
            /       \
           v         v
       Replica 1  Replica 2
```

> Same data

### Partitioning

```
             Database
                 |
        +--------+--------+
        |        |        |
        v        v        v
      Part 1   Part 2   Part 3
```

> Different subsets of data

So:

- **Replication** → COPY
- **Partitioning** → SPLIT
- **Sharding** → DISTRIBUTE

---

## 16. Can We Combine All Three?

Yes—and large systems often do.

Imagine a massive order platform:

```
                    Application
                         |
                    Shard Router
                         |
          +--------------+--------------+
          |              |              |
          v              v              v
       Shard 1        Shard 2        Shard 3
          |              |              |
     Partitioning   Partitioning   Partitioning
          |              |              |
       +--+--+        +--+--+        +--+--+
       |     |        |     |        |     |
       v     v        v     v        v     v
     Primary Replica Primary Replica Primary Replica
```

Here:

**Sharding**

Splits data across servers.

**Partitioning**

Splits each shard's data into manageable pieces.

**Replication**

Creates copies for availability/read scaling.

---

## 17. Real-World Example

Imagine an e-commerce system has:

> 10 billion orders

Architecture could be:

```
                  Orders
                     |
               Shard by user_id
                     |
          +----------+----------+
          |          |          |
          v          v          v
       Shard 1    Shard 2    Shard 3
          |          |          |
      Partition   Partition   Partition
       by date     by date     by date
          |          |          |
       +--+--+    +--+--+    +--+--+
       |     |    |     |    |     |
       v     v    v     v    v     v
    Primary Replica ...
```

Now each technique has a different responsibility:

```
Sharding
   ↓
Scale across machines

Partitioning
   ↓
Manage large tables / improve pruning

Replication
   ↓
Availability + read scaling
```

---

## 18. When Should You Use Partitioning?

Partitioning is particularly useful when:

### 1. Table is extremely large

> Billions of rows

### 2. Queries naturally filter on a partition key

For example:

```sql
WHERE created_at >= ...
```

### 3. Data has a natural lifecycle

For example:

```
Current data
   ↓
1 year old
   ↓
Archive
   ↓
Delete
```

With time partitions, you can potentially drop an old partition instead of deleting millions/billions of rows individually.

---

## 19. A Very Useful Real-World Scenario: Deleting Old Data

Suppose logs are partitioned monthly:

```
logs
 |
 +-- Jan
 +-- Feb
 +-- Mar
 +-- ...
 +-- Aug
```

Your retention policy says:

> Keep only 6 months.

Instead of:

```sql
DELETE FROM logs
WHERE created_at < '2026-03-01';
```

which could touch a huge number of rows, a partitioned design can allow you to remove the old partition as a unit, depending on the database.

Conceptually:

```
Jan Partition → DROP
Feb Partition → DROP
```

This can be much more operationally efficient.

---

## 20. How Do I Choose a Partition Key?

Similar to shard-key selection, start with query patterns.

Ask:

### Question 1

What field do queries commonly filter on?

- created_at?
- user_id?
- tenant_id?
- country?

### Question 2

Can partition pruning happen?

```sql
WHERE created_at = ...
```

### Question 3

Will partitions remain reasonably balanced?

### Question 4

Will one partition become extremely large?

### Question 5

How does data grow over time?

---

## 21. Partition Key vs Shard Key

This distinction is subtle but important.

Suppose:

**orders** has:

```
user_id
created_at
```

You might choose:

**Shard Key:**

> user_id

because users' orders should be colocated.

Then inside each shard:

**Partition Key:**

> created_at

because orders are queried by date and old data needs to be archived.

So:

```
                 Orders
                    |
              SHARD BY user_id
                    |
       +------------+------------+
       |            |            |
       v            v            v
    Shard 1      Shard 2      Shard 3
       |            |            |
   PARTITION     PARTITION     PARTITION
   BY DATE       BY DATE       BY DATE
       |            |            |
    Jan/Feb/... Jan/Feb/... Jan/Feb/...
```

This is a very realistic architecture.

---

## 22. Interview Answer

If an interviewer asks:

> "What is database partitioning?"

You can say:

> "Database partitioning is the process of dividing a large logical table into smaller physical or logical partitions based on a partition key. The database still exposes the table as a single logical table, but internally data is divided into partitions. This can improve query performance through partition pruning, simplify data lifecycle management, and make very large tables easier to maintain. Common strategies include range, list, and hash partitioning."

If they ask:

> "What's the difference between partitioning, sharding, and replication?"

A concise answer is:

> Partitioning splits data within a database, sharding distributes data across database nodes, and replication creates copies of data across nodes.

```
Partitioning
     ↓
SPLIT within DB

Sharding
     ↓
DISTRIBUTE across DBs

Replication
     ↓
COPY across DBs
```

---

## The Bigger Picture

You've now covered three fundamental database-scaling concepts:

```
                 Database Scaling
                       |
       +---------------+---------------+
       |               |               |
       v               v               v
 Partitioning       Sharding       Replication
       |               |               |
    Split data      Distribute       Copy data
    within DB       across DBs       across DBs
```
