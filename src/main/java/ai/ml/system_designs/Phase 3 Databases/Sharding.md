# Database Sharding

Sharding = splitting one large logical database into multiple smaller databases, called **shards**, based on a **shard key**.

---

## 1. Why Do We Need Database Sharding?

Imagine an e-commerce application like Amazon.

Initially, you might have:

```
Application
     |
     v
+-------------+
|   MySQL DB  |
+-------------+
| Users       |
| Orders      |
| Payments    |
| Products    |
+-------------+
```

Suppose you have:

- 10 million users
- 500 million orders
- 2 billion order items

Eventually, one database may become a bottleneck because of:

- CPU
- RAM
- Disk I/O
- Storage
- Connections
- Lock contention
- Query throughput

You can first try vertical scaling:

```
Small DB
   |
   v
Bigger DB
```

But eventually there is a physical limit and/or the cost becomes excessive.

Then you can use sharding:

```
                    Application
                         |
                 +-------+-------+
                 | Shard Router  |
                 +-------+-------+
                  /       |       \
                 /        |        \
                v         v         v
           +--------+ +--------+ +--------+
           |Shard 1 | |Shard 2 | |Shard 3 |
           +--------+ +--------+ +--------+
           | Users  | | Users  | | Users  |
           | Orders | | Orders | | Orders |
           +--------+ +--------+ +--------+
```

Each shard contains part of the data.

---

## 2. What Exactly Is a Shard?

Suppose we have:

**Users**

```
user_id
-------
1
2
3
4
5
6
7
8
9
10
```

We could distribute them:

```
Shard 1       Shard 2       Shard 3
-------       -------       -------
1             5             8
2             6             9
3             7             10
4
```

All three databases together represent the logical Users database.

The application doesn't think of them as three separate user systems.

It thinks:

> Users

while the infrastructure decides:

> Which shard contains this user?

---

## 3. Sharding vs Partitioning

These terms are often confused.

### Partitioning

Data is split **inside the same database system**.

```
             MySQL
               |
       +-------+-------+
       |       |       |
       v       v       v
   Partition Partition Partition
       1        2        3
```

### Sharding

Data is split **across different database instances**.

```
             Application
                  |
       +----------+----------+
       |          |          |
       v          v          v
     DB-1       DB-2       DB-3
    Shard 1    Shard 2    Shard 3
```

A useful interview definition:

> Partitioning divides data logically; sharding distributes those partitions across independent database nodes.

---

## 4. The Most Important Concept: Shard Key

The shard key determines where a particular record should live.

For example:

```
user_id = 12345
```

could be your shard key.

You might use:

```
hash(user_id) % 4
```

to determine the shard.

For example:

```
hash(12345) % 4 = 2
```

Therefore:

> user 12345 → Shard 2

---

## 5. Real-Time Example: E-Commerce

Let's design an Amazon-like order system.

Suppose we have:

**Orders**

```
order_id
user_id
product_id
amount
status
created_at
```

We have:

> 1 billion orders

One database becomes too large.

We decide to shard by:

> user_id

For example:

```
                    Orders
                       |
                 Shard Router
                       |
          +------------+------------+
          |            |            |
          v            v            v
       Shard 1      Shard 2      Shard 3
       Users        Users        Users

      user 1-1M    user 1M-2M   user 2M-3M
```

But instead of simple ranges, we'd commonly use hashing:

```
hash(user_id)
      |
      +----> Shard 1
      +----> Shard 2
      +----> Shard 3
      +----> Shard 4
```

Now suppose:

```
user_id = 5001
```

The router calculates:

```
hash(5001) → Shard 3
```

Therefore:

```
Create Order
     |
     v
user_id = 5001
     |
     v
Shard 3
     |
     v
INSERT order
```

---

## 6. Why user_id Can Be a Very Good Shard Key

Suppose users frequently ask:

> Give me all orders for user 5001

Query:

```sql
SELECT *
FROM orders
WHERE user_id = 5001;
```

If we shard by user_id, the application knows exactly where to look.

```
user_id = 5001
       |
       v
Shard 3
       |
       v
Query only Shard 3
```

That's called a **targeted query**.

This is extremely valuable.

---

## 7. What Happens If We Choose the Wrong Shard Key?

Suppose instead we shard orders by:

> order_id

Now the query is:

```sql
SELECT *
FROM orders
WHERE user_id = 5001;
```

Where is user 5001's data?

Potentially:

```
Shard 1 → some orders
Shard 2 → some orders
Shard 3 → some orders
Shard 4 → some orders
```

So the application has to query:

```
Shard 1
Shard 2
Shard 3
Shard 4
```

Then combine the results.

This is called a:

> **Scatter-Gather Query**

```
                 Query
                   |
           +-------+-------+
           |       |       |
           v       v       v
        Shard1  Shard2  Shard3
           |       |       |
           +---+---+---+---+
               |
               v
            Combine
               |
               v
             Result
```

This is usually much more expensive.

---

## 8. The Golden Rule for Choosing a Shard Key

The best shard key is generally not simply the column with the most unique values.

Instead ask:

> What queries are most important, and can the shard key route those queries to a single shard?

This is the most important principle.

---

## 9. How to Choose the Best Shard Key

I recommend evaluating a shard key using 5 major criteria.

### ① High Cardinality

Cardinality means:

> How many distinct values does the field have?

For example:

```
country
-------
India
USA
UK
Germany
```

Low cardinality.

But:

```
user_id
-------
1001
1002
1003
1004
...
```

Very high cardinality.

Generally:

```
High cardinality
       ↓
Better distribution potential
```

But high cardinality alone is not sufficient.

---

## 10. Example of Bad Low-Cardinality Shard Key

Suppose:

> country

is the shard key.

You might get:

```
Shard 1 → India
Shard 2 → USA
Shard 3 → UK
Shard 4 → Germany
```

Now imagine India has 70% of your users.

```
Shard 1
---------
70% traffic

Shard 2
---------
10%

Shard 3
---------
10%

Shard 4
---------
10%
```

Shard 1 becomes a **hot shard**.

This is called:

> **Data skew / hotspot**

---

## 11. High Cardinality Doesn't Guarantee Good Distribution

Consider:

> created_at

It has lots of values.

But if your sharding strategy uses ranges:

```
2026-08-01 → Shard 1
2026-08-02 → Shard 1
2026-08-03 → Shard 1
...
2026-08-31 → Shard 2
```

New writes may all go to the newest shard.

```
              Writes
                 |
                 v
              Shard 4
            🔥🔥🔥🔥🔥
```

Older shards are mostly idle.

So we care about:

> Cardinality + distribution + access pattern.

---

## 12. ② Even Distribution

A good shard key should distribute data reasonably evenly.

**Bad:**

```
Shard 1 → 80 GB
Shard 2 → 10 GB
Shard 3 → 5 GB
Shard 4 → 5 GB
```

**Good:**

```
Shard 1 → 25 GB
Shard 2 → 24 GB
Shard 3 → 26 GB
Shard 4 → 25 GB
```

Similarly for traffic:

```
Shard 1 → 25% requests
Shard 2 → 25%
Shard 3 → 26%
Shard 4 → 24%
```

This prevents hotspots.

---

## 13. ③ Query Locality

This is arguably the most important criterion.

Suppose your most common query is:

> Get orders for user

Then:

> user_id

is a strong candidate.

If your most common query is:

> Get all orders for merchant

then:

> merchant_id

might be better.

The shard key should ideally match the natural access pattern.

---

## 14. ④ Avoid Hotspots

Imagine a social media application.

You shard posts using:

> celebrity_id

Taylor Swift has millions of followers and huge traffic.

Then:

```
Taylor Swift
     |
     v
Shard 7
     |
     v
🔥🔥🔥🔥🔥🔥
```

While:

```
Shard 1 → low traffic
Shard 2 → low traffic
Shard 3 → low traffic
```

That's a bad distribution.

A good shard key should prevent one extremely popular entity from overwhelming a single shard.

---

## 15. ⑤ Stable Shard Key

A shard key should ideally not change.

For example:

> user_id

is excellent because:

> user_id = 123

normally remains:

> 123

But suppose you choose:

> user_address

and the user moves:

> Mumbai → Bangalore

Now the shard assignment may change.

You potentially need:

```
Old Shard
    |
    v
Move data
    |
    v
New Shard
```

That creates unnecessary complexity.

---

## 16. A Practical Shard-Key Decision Framework

When designing a system, I would evaluate candidates like this:

| Criterion | Question |
|---|---|
| Cardinality | Does it have many distinct values? |
| Distribution | Will data be evenly distributed? |
| Query pattern | Does it match common queries? |
| Hotspots | Can one value receive huge traffic? |
| Stability | Does the value remain unchanged? |
| Routing | Can we determine the shard quickly? |
| Growth | Will it continue working as data grows? |

---

## 17. Real-World Example: Uber-Like System

Suppose we have:

**Rides**

```
ride_id
rider_id
driver_id
pickup_location
drop_location
created_at
```

Which shard key should we choose?

It depends on the dominant queries.

**If the dominant query is:**
> Get rides for rider

Use:

> rider_id

**If dominant query is:**
> Get rides for driver

Use:

> driver_id

**If dominant query is:**
> Find nearby active rides/drivers

A geographic strategy may make more sense, potentially using a geo-based partitioning key rather than rider/driver ID.

This illustrates a critical system-design principle:

> **There is no universally best shard key. The best shard key depends on the application's access patterns.**

---

## 18. Hash Sharding

One of the most common approaches is hash-based sharding.

Suppose:

> 4 shards

We calculate:

```
hash(user_id) % 4
```

Example:

```
user 101 → hash → 1
user 102 → hash → 3
user 103 → hash → 0
user 104 → hash → 2
```

Therefore:

```
Shard 0 → user 103
Shard 1 → user 101
Shard 2 → user 104
Shard 3 → user 102
```

### Advantage

Very good distribution.

### Disadvantage

Adding/removing shards can be painful with naive modulo hashing.

Suppose:

> 4 shards

```
hash(key) % 4
```

becomes:

> 8 shards

```
hash(key) % 8
```

Many records get a different shard.

That means **massive data movement**.

---

## 19. Consistent Hashing

A more scalable approach is consistent hashing.

Instead of:

```
hash(key) % number_of_shards
```

we map nodes and keys onto a hash ring.

```
              Hash Ring

          Shard 1
             |
       +-------------+
      /               \
 Shard 4             Shard 2
      \               /
       +-------------+
             |
          Shard 3
```

When you add a shard:

> Shard 5

only a portion of the keys need to move.

That's why consistent hashing is useful in large distributed systems.

---

## 20. Range-Based Sharding

Another approach:

```
user_id 1 - 1,000,000
       → Shard 1

user_id 1,000,001 - 2,000,000
       → Shard 2

user_id 2,000,001 - 3,000,000
       → Shard 3
```

**Advantages:**

- Easy to understand
- Range queries are efficient

For example:

```sql
WHERE user_id BETWEEN 100000 AND 200000
```

can potentially target one shard.

But there is a major problem.

If IDs are sequential and all new users are inserted at the end:

```
New users
   ↓
Shard 3
   ↓
🔥 HOT SHARD
```

---

## 21. Directory-Based Sharding

Another approach is maintaining a lookup table.

**User ID → Shard**

```
1001 → Shard A
1002 → Shard C
1003 → Shard B
1004 → Shard A
```

The application asks:

> Where does user 1003 live?

Directory says:

> Shard B

Then:

```
Application
     |
     v
Shard Directory
     |
     v
Shard B
```

### Advantage

Very flexible.

### Disadvantage

The shard directory itself becomes another distributed-system component that must be highly available and consistent.

---

## 22. Composite Shard Keys

Sometimes one field isn't enough.

Suppose we have:

```
tenant_id
user_id
```

For a SaaS application.

You could use:

> tenant_id + user_id

as the logical shard key.

For example:

```
Tenant A + User 101
Tenant A + User 102
Tenant B + User 101
Tenant B + User 102
```

This can help maintain tenant locality while still distributing users.

But be careful with very large tenants.

One tenant could still become a hotspot.

---

## 23. Important Real-World Problem: "Celebrity Tenant"

Suppose you're building Slack-like software.

Most customers are small:

```
Tenant A → 100 users
Tenant B → 200 users
Tenant C → 150 users
```

But one customer has:

```
Tenant X → 10 million users
```

If you shard purely by:

> tenant_id

then:

```
Tenant X
   |
   v
Shard 7
   |
   v
🔥🔥🔥🔥🔥
```

You may need a special strategy for large tenants.

For example:

> tenant_id + user_id

or:

> tenant_id + hash(user_id)

depending on access patterns.

---

## 24. Cross-Shard Queries

Sharding makes some queries much harder.

Suppose:

```sql
SELECT COUNT(*)
FROM orders
WHERE status = 'PENDING';
```

If orders are distributed across 10 shards:

```
Shard 1 → 100
Shard 2 → 150
Shard 3 → 90
...
Shard 10 → 120
```

You need:

```
Query all shards
      ↓
Get partial results
      ↓
Aggregate
      ↓
Return final result
```

This is another form of scatter-gather.

Therefore, before sharding, ask:

> What queries will need data from multiple shards?

---

## 25. Cross-Shard Transactions

This is another major disadvantage.

Suppose:

```
Order → Shard 1
Payment → Shard 2
```

Now you need:

```
Create Order
+
Create Payment
```

as one atomic transaction.

A normal local transaction doesn't span independent databases easily.

You may need distributed transactions, but they add complexity and can hurt performance.

In modern distributed architectures, you will often see patterns such as:

- Saga
- Outbox
- Event-driven workflows
- Idempotency

instead of relying heavily on distributed ACID transactions.

---

## 26. Sharding + Replication

Don't confuse these concepts.

### Sharding

Answers:

> How do we split data?

### Replication

Answers:

> How do we create copies of data?

You can use both.

For example:

```
                 Orders
                    |
          +---------+---------+
          |         |         |
          v         v         v
       Shard 1   Shard 2   Shard 3
          |         |         |
       +--+--+   +--+--+   +--+--+
       |     |   |     |   |     |
     Primary Replica Primary Replica
```

So a large production database often uses:

> **Sharding + Replication**

---

## 27. Sharding vs Read Replicas

These solve different problems.

### Read Replicas

```
             DB Primary
             /        \
            v          v
        Replica 1   Replica 2
```

Used mainly to scale:

> **READ traffic**

### Sharding

```
             Router
          /     |     \
         v      v      v
      Shard1 Shard2 Shard3
```

Used to distribute:

> **DATA + READ + WRITE**

across multiple database partitions.

Often you use both.

---

## 28. The Best Approach to Choose a Shard Key

For system-design interviews, I'd recommend this thought process.

### Step 1 — Identify Your Dominant Queries

Example:

```
Q1: Get orders for user
Q2: Get order by order_id
Q3: Get recent orders for user
Q4: Get pending orders globally
```

### Step 2 — Identify Candidate Keys

```
user_id
order_id
created_at
status
```

### Step 3 — Evaluate Distribution

Ask:

> Will the data be balanced?

### Step 4 — Evaluate Query Locality

Ask:

> Can the common queries hit one shard?

### Step 5 — Evaluate Hotspots

Ask:

> Can one user/tenant/category become extremely popular?

### Step 6 — Evaluate Growth

Ask:

> What happens when we go from 10 shards → 100 shards?

### Step 7 — Evaluate Operational Complexity

Ask:

> How difficult will rebalancing be?
> How difficult will migrations be?
> How difficult will cross-shard queries be?

---

## 29. My Preferred Mental Model

When choosing a shard key, remember:

```
                SHARD KEY
                    |
       +------------+------------+
       |            |            |
       v            v            v
   Distribution   Queries     Hotspots
       |            |            |
       v            v            v
   Balanced?    Localizable?   Avoided?
       \            |            /
        \           |           /
         +----------+----------+
                    |
                    v
             Good Shard Key
```

And prioritize approximately:

> Query locality + even distribution + hotspot avoidance + stability + operational simplicity.

---

## 30. A Very Important Interview Answer

If an interviewer asks:

> "How would you choose a shard key?"

A strong answer would be:

> "I would first analyze the application's dominant read and write patterns rather than choosing a key purely based on cardinality. I would prefer a high-cardinality, stable key that distributes data and traffic evenly and allows the most important queries to be routed to a single shard. I'd also evaluate the risk of hotspots, cross-shard queries, range-query requirements, future growth, and rebalancing. For example, in an order system where most queries are 'get orders for a user', user_id would be a strong candidate because it provides good query locality and can distribute users across shards using hashing."

That is much stronger than simply saying:

> "Use user_id because it has high cardinality."

---

## 31. One Final Real-World Example

Consider a food-delivery application:

- Customers
- Restaurants
- Orders
- Drivers
- Payments

Suppose we shard orders.

Possible keys:

| Shard Key | Distribution | Query Locality | Hotspot Risk | Overall |
|---|---|---|---|---|
| order_id | ⭐⭐⭐⭐⭐ | ⭐⭐ | ⭐ | Good |
| user_id | ⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐ | Excellent if user queries dominate |
| restaurant_id | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ | Risky for popular restaurants |
| city_id | ⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | Usually risky |
| created_at | ⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐⭐ | Risky with range-based approach |

If the primary requirement is:

> "Show me all orders placed by this customer"

I'd lean toward:

> user_id

If the primary requirement is:

> "Show restaurant's recent orders"

I'd consider:

> restaurant_id

But then I'd specifically investigate hot restaurants.

---

## The Key Takeaway

Don't memorize:

> Best shard key = user_id

Instead memorize:

> **Best shard key = a key that aligns with the dominant access pattern while providing good distribution, avoiding hotspots, remaining stable, and minimizing cross-shard operations.**

---

## Learning Path: What to Study Next

For your system-design learning path, the natural next topics after Database Sharding are:

```
Database Sharding
       ↓
Shard Key Selection
       ↓
Hash vs Range Sharding
       ↓
Consistent Hashing
       ↓
Hot Partitions / Data Skew
       ↓
Rebalancing
       ↓
Cross-Shard Queries
       ↓
Cross-Shard Transactions
       ↓
Replication + Sharding
       ↓
Distributed Databases
```

That sequence will make the database-scaling part of system design much easier to reason about.
