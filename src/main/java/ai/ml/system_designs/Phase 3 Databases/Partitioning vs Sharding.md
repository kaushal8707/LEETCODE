# Partitioning vs Sharding

Since you're learning System Design from scratch, the most important thing is to understand Partitioning vs Sharding conceptually first, and then see how they are used in real systems.

A simple way to remember:

> **Partitioning** = splitting data into smaller pieces.
> **Sharding** = splitting data across multiple database servers.

Sharding is essentially horizontal partitioning across multiple database instances/servers.

---

## 1. What Is Database Partitioning?

Suppose we have an orders table containing 1 billion orders.

```
Orders
------------------------------------------------
order_id | user_id | order_date | amount
------------------------------------------------
1        | 101     | 2024-01-10 | 500
2        | 102     | 2024-01-11 | 700
...
1 billion rows
```

Searching through 1 billion rows can become expensive.

Instead, we can divide the table into smaller partitions.

### Example: Partition by Year

```
Orders Table
       |
       +------------------+
       |                  |
   2024 Orders        2025 Orders
       |                  |
   Partition 1        Partition 2
```

For example:

```
orders_2024
orders_2025
orders_2026
```

But logically, the application still sees:

> Orders

The database internally knows which partition contains the data.

---

## 2. Real-Time Example — E-Commerce Orders

Imagine an e-commerce company has:

> 5 billion orders

Most queries are like:

```sql
SELECT *
FROM orders
WHERE order_date >= '2026-01-01'
AND order_date < '2026-02-01';
```

If the table isn't partitioned, the database may need to consider a huge amount of data.

With date-based partitioning:

```
Orders
│
├── 2024
│   ├── Jan
│   ├── Feb
│   └── ...
│
├── 2025
│   ├── Jan
│   ├── Feb
│   └── ...
│
└── 2026
    ├── Jan
    ├── Feb
    └── ...
```

The database can perform **partition pruning**.

For a query on January 2026:

```
Query
  │
  ▼
orders
  │
  ├── 2024 ❌
  ├── 2025 ❌
  └── 2026
       │
       └── January ✅
```

It doesn't need to scan irrelevant partitions.

### Benefits

- Faster queries
- Easier data management
- Easier archival/deletion
- Smaller indexes per partition
- Better maintenance

---

## 3. Types of Partitioning

There are several common strategies.

### Range Partitioning

Data is divided based on ranges.

```
user_id 1 - 1,000,000
user_id 1,000,001 - 2,000,000
user_id 2,000,001 - 3,000,000
```

Or:

```
2024
2025
2026
```

### List Partitioning

Data is divided based on predefined values.

For example:

```
India
USA
UK
Germany
```

```
Orders
│
├── India
├── USA
├── UK
└── Germany
```

### Hash Partitioning

A hash function determines the partition.

```
partition = hash(user_id) % 4
```

For example:

```
user_id = 101

hash(101) % 4 = 1

          ↓

Partition 1
```

---

## 4. What Is Database Sharding?

Now let's take the same example.

Suppose our database server has become too large.

```
Application
     |
     ▼
Database Server
     |
     └── Orders
          |
          └── 5 billion rows
```

We can split the data across multiple database servers.

```
                    Application
                         |
                    Sharding Logic
                         |
          +--------------+--------------+
          |              |              |
          ▼              ▼              ▼
       DB-01           DB-02          DB-03
       Users            Users          Users
       1-1M             1M-2M          2M-3M
```

Now each server owns only a portion of the data.

That's **sharding**.

---

## 5. Real-Time Example — Banking System

Imagine a banking system has:

> 500 million customers

A single database might become a bottleneck.

We could shard based on:

> customer_id

For example:

```
customer_id 1 - 100M
        ↓
      DB-01

customer_id 100M - 200M
        ↓
      DB-02

customer_id 200M - 300M
        ↓
      DB-03

customer_id 300M - 400M
        ↓
      DB-04

customer_id 400M - 500M
        ↓
      DB-05
```

Now:

```
Customer 245678123
       |
       ▼
Shard calculation
       |
       ▼
DB-03
```

The request goes directly to DB-03.

---

## 6. Partitioning vs Sharding — The Critical Difference

This is the distinction you should remember for interviews.

### Partitioning

Usually:

```
One database server
        |
        +---------+
        |         |
   Partition 1  Partition 2
```

The data is divided **inside** the database.

### Sharding

```
                    Database
                       |
          +------------+------------+
          |            |            |
        DB-01        DB-02        DB-03
          |            |            |
       Shard 1      Shard 2      Shard 3
```

The data is divided **across multiple database servers**.

---

## 7. A Very Simple Real-Life Analogy

Imagine an Amazon warehouse.

You have:

> 1,000,000 products

### Partitioning

You have one warehouse, but organize products into sections:

```
Warehouse
│
├── Electronics
├── Clothing
├── Shoes
├── Books
└── Furniture
```

That's similar to partitioning.

> One warehouse → multiple sections.

### Sharding

Now one warehouse isn't enough.

You create:

```
Warehouse A
Warehouse B
Warehouse C
Warehouse D
```

Products are distributed across different warehouses.

That's similar to sharding.

> Multiple warehouses → data distributed across servers.

---

## 8. Real System Design Example

Let's design a large social-media application.

Suppose we have:

```
Users       → 500 million
Posts       → 100 billion
Likes       → 500 billion
Comments    → 200 billion
```

A single database becomes difficult to scale.

We could use both partitioning and sharding.

```
                         Application
                              |
                    +---------+---------+
                    |                   |
               Shard Router         Cache
                    |
       +------------+------------+
       |            |            |
     DB-01        DB-02        DB-03
       |            |            |
    Users 1-     Users 2-     Users 3-
       |
       └── Partitioned tables
```

For example:

```
DB-01
│
├── users
│    ├── partition-1
│    ├── partition-2
│    └── partition-3
│
└── posts
     ├── partition-2024
     ├── partition-2025
     └── partition-2026
```

So you can have:

> **Sharding at the database-server level + partitioning inside each database.**

This is extremely common in large-scale system design.

---

## 9. Sharding Strategies

### A. Range-Based Sharding

```
Shard 1 → user_id 1 - 1M
Shard 2 → user_id 1M - 2M
Shard 3 → user_id 2M - 3M
```

### Problem: Hotspot

Suppose new users always get higher IDs.

Then:

```
Shard 3
████████████████████
```

while:

```
Shard 1
████
```

Shard 3 gets most traffic.

This is called a **hot shard / hotspot**.

---

## 10. Hash-Based Sharding

Instead:

```
shard = hash(user_id) % N
```

For example:

```
hash(user_id) % 4
```

Results:

```
User 101 → Shard 2
User 102 → Shard 0
User 103 → Shard 3
User 104 → Shard 1
User 105 → Shard 2
```

Distribution is generally more balanced.

### But There Is a Problem

Suppose:

> 4 shards

and later you add:

> 8 shards

The hash mapping can change dramatically.

This can require moving a lot of data.

That's one reason **consistent hashing** is important in distributed systems.

---

## 11. Choosing a Shard Key

This is one of the most important System Design interview questions.

Suppose:

```
Users
------
user_id
name
email
country
```

Possible shard keys:

```
user_id
country
email
```

A good shard key should generally provide:

### 1. Even Distribution

Avoid:

```
Shard 1 → 90% data
Shard 2 → 5%
Shard 3 → 3%
Shard 4 → 2%
```

Prefer:

```
Shard 1 → 25%
Shard 2 → 25%
Shard 3 → 25%
Shard 4 → 25%
```

### 2. Match Your Query Patterns

If most queries are:

```sql
SELECT *
FROM orders
WHERE user_id = ?;
```

then:

> user_id

is often a strong shard-key candidate.

### 3. Avoid Hotspots

Don't choose a key that causes most traffic to land on one shard.

### 4. High Cardinality

A shard key should generally have many possible values.

For example:

```
user_id ✅
order_id ✅
country ❌
```

If you have only:

> 10 countries

you have very limited distribution options.

---

## 12. The Most Important Problem: Cross-Shard Query

Suppose:

```
Shard 1 → users 1-1M
Shard 2 → users 1M-2M
Shard 3 → users 2M-3M
```

Now somebody asks:

```sql
SELECT COUNT(*)
FROM orders
WHERE amount > 10000;
```

Which shard contains the data?

We don't know.

The application may need to query:

```
        Query
          |
     +----+----+----+
     |         |    |
   DB-01     DB-02 DB-03
     |         |    |
   result    result result
     |         |    |
     +----+----+----+
          |
       Combine
          |
       Final result
```

This is called a **scatter-gather query**.

It can be expensive.

---

## 13. Partitioning vs Sharding Summary

| Feature | Partitioning | Sharding |
|---|---|---|
| Data split | Yes | Yes |
| Multiple DB servers | Usually no | Yes |
| Main purpose | Manage/query large tables | Scale database horizontally |
| Complexity | Lower | Higher |
| Cross-server query | Usually no | Yes |
| Scaling capacity | Limited | Much higher |
| Example | Orders by year | Users distributed across DB servers |

---

## 14. Interview Definition

If an interviewer asks:

> "What is the difference between partitioning and sharding?"

A strong answer is:

> Partitioning divides a large database table into smaller logical pieces, usually within the same database system. Sharding is a form of horizontal partitioning where those pieces are distributed across multiple database servers. Partitioning primarily helps with data management and query performance, while sharding is primarily used for horizontal scalability and distributing database load.

---

## 15. One Picture to Remember

```
                 PARTITIONING
                 
                 Database
                    |
             Orders Table
                    |
          +---------+---------+
          |         |         |
       Part-1    Part-2    Part-3
       2024      2025      2026


                 SHARDING

                 Application
                      |
                  Shard Router
                      |
          +-----------+-----------+
          |           |           |
        DB-01       DB-02       DB-03
          |           |           |
       Users       Users       Users
       1-1M        1-2M        2-3M
```

And in a large production system, you can combine them:

```
                       Application
                            |
                       Shard Router
                            |
             +--------------+--------------+
             |              |              |
           DB-01          DB-02          DB-03
             |              |              |
        Partitioned     Partitioned     Partitioned
          tables          tables          tables
             |              |              |
        2024|2025|2026  2024|2025|2026  2024|2025|2026
```

---

## Learning Path

The key progression for your System Design learning is:

```
Single DB
   ↓
Indexing
   ↓
Partitioning
   ↓
Read Replicas
   ↓
Caching
   ↓
Vertical Scaling
   ↓
Horizontal Scaling
   ↓
Sharding
   ↓
Distributed Database
```
