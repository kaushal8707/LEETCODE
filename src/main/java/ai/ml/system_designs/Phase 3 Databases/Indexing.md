# Database Indexing

Database Indexing is a technique used to speed up data retrieval from a database.

The easiest analogy is a book's index.

Imagine a 1,000-page book. If you want to find the topic "Database Indexing", you don't read every page from 1 to 1,000.

You go to the index:

    Database Indexing ........ Page 735

A database index works in a similar way.

---

## 1. Without an Index

Suppose we have a users table containing 10 million records:

```
users
--------------------------------
id | name     | email
--------------------------------
1  | Rahul    | rahul@gmail.com
2  | Amit     | amit@gmail.com
3  | Neha     | neha@gmail.com
...
10,000,000 records
```

Now execute:

```sql
SELECT *
FROM users
WHERE email = 'amit@gmail.com';
```

If there is no index on email, the database may need to scan many/all rows:

```
Row 1     → No
Row 2     → YES
Row 3     → No
Row 4     → No
...
Row 10M   → No
```

This is called a:

**Full Table Scan**

Conceptually:

```
10 Million Rows
       │
       ▼
┌─────────────────────────────┐
│ Scan Row 1                  │
│ Scan Row 2                  │
│ Scan Row 3                  │
│ Scan Row 4                  │
│ ...                         │
│ Scan Row 10,000,000         │
└─────────────────────────────┘
       │
       ▼
   Find Record
```

For a large table, this can be expensive.

---

## 2. With an Index

Now create an index:

```sql
CREATE INDEX idx_users_email
ON users(email);
```

The database creates a separate data structure for the indexed column.

Conceptually:

```
Index
────────────────────────────
amit@gmail.com  → Row 2
neha@gmail.com  → Row 3
rahul@gmail.com → Row 1
...
```

Now:

```sql
SELECT *
FROM users
WHERE email = 'amit@gmail.com';
```

The database can use the index to quickly locate the corresponding row.

```
Query
  │
  ▼
Index on email
  │
  ▼
amit@gmail.com
  │
  ▼
Row 2
  │
  ▼
Actual record
```

Instead of scanning millions of rows, the database can perform a much more efficient lookup.

---

## 3. How Does an Index Work?

Most relational databases commonly use a B-tree/B+ tree-style structure for general-purpose indexes.

Conceptually:

```
                    [50]
                   /    \
                 /        \
             [20, 30]     [70, 90]
             /  |  \       /  |  \
            /   |   \     /   |   \
          ...  ...  ...  ...  ...  ...
```

Suppose you search:

    WHERE id = 70

Instead of checking every value:

```
1
2
3
4
5
...
70
```

the database navigates through the tree:

```
              [50]
                │
                ▼
           70 > 50
                │
                ▼
          [70, 90]
                │
                ▼
               70
```

This dramatically reduces the amount of data that needs to be examined.

---

## 4. Time Complexity

For a balanced B-tree-style index, lookup is approximately:

    O(log N)

where N is the number of indexed entries.

A table scan is approximately:

    O(N)

Conceptually:

```
Without index:

10,000,000 rows
       ↓
   O(N) scan


With index:

10,000,000 entries
       ↓
   O(log N) lookup
```

The exact behavior depends on the database, query, index structure, data distribution, and optimizer decisions.

---

## 5. Real-Time Example — E-Commerce

Imagine Amazon-like application with:

```
products
------------------------------------------------
id | name | category | price | brand
------------------------------------------------
```

Suppose there are:

    100 million products

You frequently execute:

```sql
SELECT *
FROM products
WHERE category = 'Laptops';
```

Without an index:

```
100 Million rows
       │
       ▼
Full table scan
       │
       ▼
Find category = Laptops
```

Create an index:

```sql
CREATE INDEX idx_products_category
ON products(category);
```

Now the database has an efficient access path for category lookups.

---

## 6. Indexes Are Not Free

This is extremely important for system design interviews.

You might think:

    "If indexes make queries faster, let's create indexes on every column."

**Don't.**

Indexes have costs.

### Cost 1: Additional Storage

The index itself requires disk space.

```
Database
├── Table
│
└── Index
```

Large tables can have very large indexes.

### Cost 2: Slower Writes

Suppose:

```sql
INSERT INTO users ...
```

Without an index:

```
INSERT → Table
```

With an index:

```
INSERT
  │
  ├──→ Table
  │
  └──→ Update Index
```

Similarly:

```
UPDATE
  │
  ├──→ Table
  │
  └──→ Possibly update Index
```

and:

```
DELETE
  │
  ├──→ Table
  │
  └──→ Update Index
```

Therefore:

**Indexes improve reads but add overhead to writes.**

---

## 7. Which Columns Should Be Indexed?

A good candidate is a column that frequently appears in:

### WHERE

```sql
SELECT *
FROM users
WHERE email = 'abc@gmail.com';
```

Index:

```sql
CREATE INDEX idx_users_email
ON users(email);
```

### JOIN

```sql
SELECT *
FROM orders o
JOIN users u
ON o.user_id = u.id;
```

An index on appropriate join columns can significantly help query performance.

### ORDER BY

For example:

```sql
SELECT *
FROM orders
ORDER BY created_at DESC;
```

An appropriate index can sometimes help avoid an expensive sort.

### GROUP BY

For example:

```sql
SELECT customer_id, COUNT(*)
FROM orders
GROUP BY customer_id;
```

An index may help depending on the database and execution plan.

---

## 8. High Cardinality vs Low Cardinality

Cardinality roughly refers to the number of distinct values in a column.

### High Cardinality

Example:

    email
    user_id
    passport_number
    order_id

Suppose:

    10 million users
    10 million unique emails

An index on email can be highly selective.

### Low Cardinality

Example:

    gender
    status
    is_active

Suppose:

    10 million records

    ACTIVE   → 9.5 million
    INACTIVE → 500,000

An index may be less useful for certain queries because a large percentage of rows still match.

However, low cardinality does not automatically mean "never index it." The optimizer considers selectivity, data distribution, query shape, table size, and other factors.

---

## 9. Composite Index

Sometimes queries filter on multiple columns.

Example:

```sql
SELECT *
FROM orders
WHERE customer_id = 100
AND status = 'PAID';
```

We can create:

```sql
CREATE INDEX idx_orders_customer_status
ON orders(customer_id, status);
```

This is called a **Composite Index** or **Multi-Column Index**.

Conceptually:

```
Index
(customer_id, status)

100, CANCELLED
100, PAID
100, PENDING
101, PAID
102, PAID
...
```

---

## 10. Column Order Matters

This is a very important interview topic.

Consider:

```sql
CREATE INDEX idx_orders
ON orders(customer_id, status);
```

The index is ordered primarily by:

    customer_id

and then by:

    status

Therefore, queries involving the leftmost part of the index are generally more likely to benefit.

For example:

```sql
WHERE customer_id = 100
```

Good candidate.

```sql
WHERE customer_id = 100
AND status = 'PAID'
```

Good candidate.

But:

```sql
WHERE status = 'PAID'
```

may not be able to use this composite index as effectively, depending on the database and query plan.

This is often referred to as the **leftmost-prefix principle** for B-tree indexes.

---

## 11. Index Selectivity

Suppose we have:

    10 Million Users

Query:

    WHERE email = 'abc@gmail.com'

Only one record matches.

That's highly selective:

    10,000,000 → 1

Excellent index candidate.

Now:

    WHERE country = 'India'

Suppose:

    10,000,000 → 3,000,000

That's much less selective.

The database may decide that using the index isn't worth it and perform another access strategy, potentially including a table scan.

**Important:** Creating an index doesn't guarantee the database will use it.

The **query optimizer** decides.

---

## 12. Covering Index

A covering index contains all the columns required to answer a query, so the database may not need to access the base table.

Suppose:

```sql
SELECT customer_id, status
FROM orders
WHERE customer_id = 100;
```

An index such as:

```sql
CREATE INDEX idx_orders_customer_status
ON orders(customer_id, status);
```

may contain everything needed for the query.

Conceptually:

```
Query
  │
  ▼
Index
  │
  ├── customer_id
  └── status
       │
       ▼
   Return result
```

The database may avoid fetching the actual table rows.

This can significantly improve performance for suitable workloads.

---

## 13. Unique Index

A unique index ensures that indexed values are unique.

For example:

```sql
CREATE UNIQUE INDEX idx_users_email
ON users(email);
```

Now:

    abc@gmail.com

can belong to only one user.

Trying to insert another record with the same email will violate the uniqueness constraint.

In many relational databases, a UNIQUE constraint is implemented using a unique index or an equivalent index structure.

---

## 14. Primary Key and Index

When you define:

```sql
CREATE TABLE users (
    id BIGINT PRIMARY KEY,
    name VARCHAR(100)
);
```

the database generally creates/enforces an index structure associated with the primary key.

Therefore:

```sql
SELECT *
FROM users
WHERE id = 12345;
```

can be very efficient.

But the exact physical implementation depends on the database engine.

---

## 15. Index and Query Optimizer

When you execute:

```sql
SELECT *
FROM users
WHERE email = 'abc@gmail.com';
```

the database doesn't blindly say:

    "There is an index, so I must use it."

Instead, the **query optimizer** evaluates possible execution plans.

Conceptually:

```
                 SQL Query
                    │
                    ▼
              Query Optimizer
               /           \
              /             \
             ▼               ▼
      Table Scan         Index Scan
          │                  │
          ▼                  ▼
       Cost = X            Cost = Y
              \             /
               \           /
                ▼         ▼
                 Choose
                cheaper
                  plan
```

The optimizer considers statistics and estimated costs.

---

## 16. Index Scan vs Table Scan

### Table Scan

```
Table
 │
 ├── Row 1
 ├── Row 2
 ├── Row 3
 ├── ...
 └── Row N
```

Potentially examines a large portion of the table.

### Index Scan / Lookup

```
Index
 │
 ▼
Matching key
 │
 ▼
Relevant row(s)
```

Potentially examines far fewer entries.

---

## 17. Common Types of Indexes

Different databases support different index types, but commonly encountered types include:

### B-tree / B+ tree

Good general-purpose choice for:

    =
    <
    >
    <=
    >=
    BETWEEN
    ORDER BY

### Hash Index

Designed primarily for equality lookups in systems that support it:

    WHERE user_id = 100

Conceptually:

```
Hash(user_id)
     │
     ▼
 Bucket
     │
     ▼
Record
```

It generally isn't suitable for ordered/range access in the way a B-tree is.

### Full-Text Index

Useful for text searching:

    "distributed database architecture"

Rather than simple exact equality.

---

## 18. Database Index Example

Suppose we have:

```
orders
--------------------------------------------------
id | customer_id | status | amount | created_at
--------------------------------------------------
1  | 101         | PAID   | 500    | ...
2  | 102         | NEW    | 700    | ...
3  | 101         | PAID   | 300    | ...
...
```

Frequently executed query:

```sql
SELECT *
FROM orders
WHERE customer_id = 101
AND status = 'PAID';
```

Create:

```sql
CREATE INDEX idx_orders_customer_status
ON orders(customer_id, status);
```

Now the database has a much better access path for this query.

---

## 19. Indexing in System Design

Suppose you're designing an Order Service.

```
                    Order Service
                         │
                         ▼
                    PostgreSQL
                         │
              ┌──────────┴──────────┐
              │                     │
          orders table          indexes
```

Common queries:

```sql
SELECT *
FROM orders
WHERE user_id = ?;
```

You might consider:

```sql
CREATE INDEX idx_orders_user_id
ON orders(user_id);
```

Another query:

```sql
SELECT *
FROM orders
WHERE user_id = ?
ORDER BY created_at DESC;
```

You might consider a composite index such as:

```sql
CREATE INDEX idx_orders_user_created
ON orders(user_id, created_at);
```

This is where indexing becomes a **system design decision**, not just a database optimization.

---

## 20. Don't Over-Index

Suppose a table has:

    50 columns

and you create:

    30 indexes

You may make reads faster for certain queries, but you also increase:

```
Storage
   +
Write overhead
   +
Index maintenance
   +
Memory/cache pressure
   +
Database maintenance complexity
```

Therefore:

**Create indexes based on actual query patterns, not simply because a column exists.**

---

## 21. How to Decide Whether to Add an Index

In a real project, follow this process:

```
1. Identify slow query
        ↓
2. Understand WHERE / JOIN / ORDER BY
        ↓
3. Check existing indexes
        ↓
4. Examine execution plan
        ↓
5. Identify suitable index
        ↓
6. Test with realistic data
        ↓
7. Measure improvement
        ↓
8. Monitor write/storage overhead
```

For example, in PostgreSQL:

```sql
EXPLAIN ANALYZE
SELECT *
FROM orders
WHERE customer_id = 100;
```

This helps you understand how the database actually executes the query.

---

## 22. Database Indexing — Interview Perspective

**If you're asked:**

### "Why do we use database indexes?"

A strong answer:

> Database indexes are data structures that provide an efficient access path to rows, reducing the amount of data the database needs to scan for suitable queries. They can significantly improve read performance, often from a linear table scan toward logarithmic tree-based lookup. However, indexes consume storage and add overhead to INSERT, UPDATE, and DELETE operations, so they should be created based on actual query patterns and workload.

---

## 23. Most Important Points to Remember

```
                 DATABASE INDEX
                       │
           ┌───────────┴───────────┐
           │                       │
        Faster                   Costs
         Reads                     │
           │              ┌────────┼────────┐
           │              │        │        │
           ▼              ▼        ▼        ▼
      WHERE/JOIN       Storage   Writes  Maintenance
      ORDER BY
      Range queries
```

### Remember these 7 points:

1. **Index = Faster data lookup**
2. **Without index → potentially full table scan**
3. **B-tree indexes commonly provide ~O(log N) lookup**
4. **Indexes consume additional storage**
5. **Indexes can slow INSERT/UPDATE/DELETE**
6. **Composite index column order matters**
7. **Always validate with the database's execution plan**

---

### One-line definition

> A database index is an auxiliary data structure that allows the database to locate matching rows efficiently instead of scanning the entire table.
