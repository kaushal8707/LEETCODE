# SQL vs NoSQL

Since you're learning System Design from scratch, understanding SQL vs NoSQL is important because database choice is one of the major decisions in system architecture.

---

## 1. What Is SQL?

SQL = **Structured Query Language**

SQL databases are relational databases where data is stored in tables consisting of rows and columns.

**Examples:**

- PostgreSQL
- MySQL
- Oracle Database
- Microsoft SQL Server

### Example

Suppose we have an e-commerce application.

**Customer table**

| id | name | email |
|---|---|---|
| 101 | Rahul | rahul@gmail.com |
| 102 | Amit | amit@gmail.com |

**Order table**

| order_id | customer_id | amount |
|---|---|---|
| 5001 | 101 | 2000 |
| 5002 | 101 | 3500 |
| 5003 | 102 | 1500 |

Here, `customer_id` connects the two tables.

You can query:

```sql
SELECT c.name, o.order_id, o.amount
FROM customer c
JOIN orders o
  ON c.id = o.customer_id
WHERE c.id = 101;
```

The database understands the relationship between entities.

---

## 2. What Is NoSQL?

NoSQL = **Not Only SQL**

NoSQL databases generally don't require the traditional relational table structure.

Common types include:

| Type | Examples |
|---|---|
| Document | MongoDB |
| Key-Value | Redis, DynamoDB |
| Wide Column | Cassandra |
| Graph | Neo4j |

For example, an order could be stored as one document:

```json
{
  "orderId": 5001,
  "customerId": 101,
  "customerName": "Rahul",
  "items": [
    {
      "productId": 1001,
      "name": "Laptop",
      "quantity": 1,
      "price": 60000
    },
    {
      "productId": 1002,
      "name": "Mouse",
      "quantity": 2,
      "price": 1000
    }
  ],
  "totalAmount": 62000
}
```

The order and its items can be stored together.

---

## 3. Biggest Difference

The easiest way to remember:

> **SQL** → relationships and structured data
> **NoSQL** → flexibility and large-scale distributed workloads

But this is only a rule of thumb. Modern SQL databases can scale horizontally, and many NoSQL databases support transactions.

---

## 4. SQL vs NoSQL

| Feature | SQL | NoSQL |
|---|---|---|
| Data model | Tables | Document / Key-Value / Column / Graph |
| Schema | Usually predefined | Usually flexible |
| Relationships | Excellent | Usually handled differently |
| JOINs | Strong support | Often avoided |
| Transactions | Strong ACID support | Varies by database |
| Scaling | Traditionally vertical; horizontal possible | Often designed for horizontal scaling |
| Data structure | Structured | Flexible/semi-structured |
| Query language | SQL | Database-specific APIs/query languages |
| Consistency | Usually strong | Varies; often configurable |
| Schema changes | More controlled | Usually easier |
| Best for | Transactions & relationships | High scale/flexible data |
| Examples | PostgreSQL, MySQL, Oracle | MongoDB, Cassandra, DynamoDB, Redis |

---

## 5. Schema Difference

This is one of the most important differences.

### SQL

You generally define the schema first.

```sql
CREATE TABLE customer (
    id BIGINT PRIMARY KEY,
    name VARCHAR(100),
    email VARCHAR(200)
);
```

Then insert data:

```sql
INSERT INTO customer
VALUES (101, 'Rahul', 'rahul@gmail.com');
```

The database expects the defined structure.

### NoSQL

For a document database such as MongoDB, you can have:

```json
{
  "id": 101,
  "name": "Rahul",
  "email": "rahul@gmail.com"
}
```

Another document could contain additional information:

```json
{
  "id": 102,
  "name": "Amit",
  "email": "amit@gmail.com",
  "phone": "9876543210"
}
```

The documents don't necessarily need to have exactly the same fields.

That's what we mean by **schema flexibility**.

---

## 6. SQL Example: Banking System

Imagine a banking application.

You transfer:

> ₹10,000

from:

> Account A

to:

> Account B

We need something like:

```
Debit Account A
        ↓
Credit Account B
        ↓
Commit
```

What if debit succeeds but credit fails?

We don't want:

```
Account A = -₹10,000
Account B = unchanged
```

We need the entire transaction to succeed or fail.

This is where **ACID transactions** are extremely important.

SQL databases are traditionally a very strong choice for:

- Banking
- Payments
- Accounting
- Orders
- Inventory
- Financial transactions

---

## 7. NoSQL Example: Social Media

Imagine Instagram-like functionality.

A user may have:

```
User
 ├── Posts
 ├── Followers
 ├── Likes
 ├── Comments
 └── Notifications
```

Suppose we have:

> 500 million users

and millions of posts/likes/comments being generated continuously.

The system may need:

```
High write throughput
        +
Horizontal scaling
        +
High availability
        +
Distributed storage
```

A NoSQL database such as Cassandra or DynamoDB can be a good fit for certain workloads.

---

## 8. Another Real-World Example: Amazon Cart

Consider an online shopping cart.

A cart could look like:

```json
{
  "userId": 101,
  "items": [
    {
      "productId": 1001,
      "quantity": 2
    },
    {
      "productId": 2001,
      "quantity": 1
    }
  ]
}
```

A document-oriented or key-value database can be convenient because the application frequently asks:

> Get cart for user 101

and:

> Update cart for user 101

The access pattern is simple:

```
userId → cart
```

This is a classic situation where a NoSQL/key-value approach can work well.

---

## 9. Scaling Difference

This is extremely important in System Design interviews.

### SQL

Traditionally:

```
               SQL DB
                 |
          ┌──────┴──────┐
          |             |
       CPU/RAM       Storage
```

You can scale vertically:

```
Small Server
     ↓
Bigger Server
     ↓
Much Bigger Server
```

This is called:

> **Vertical Scaling**

But eventually you reach hardware limits.

Modern SQL systems can also scale horizontally using techniques such as:

- Read replicas
- Partitioning
- Sharding
- Distributed SQL

---

## 10. NoSQL

Many NoSQL systems are designed with horizontal scaling in mind.

```
              Application
                   |
          ┌────────┼────────┐
          ↓        ↓        ↓
       Node 1    Node 2    Node 3
          ↓        ↓        ↓
        Data     Data     Data
```

If traffic increases:

```
3 Nodes
   ↓
10 Nodes
   ↓
100 Nodes
```

You can add machines.

This is:

> **Horizontal Scaling**

---

## 11. CAP Theorem Connection

When learning System Design, SQL vs NoSQL also connects to the CAP theorem.

CAP says that in the presence of a network partition, a distributed system has to make a trade-off between:

- **Consistency**
- **Availability**
- **Partition Tolerance**

For example, many distributed NoSQL systems are designed around high availability and partition tolerance, with consistency characteristics that depend on the specific database and configuration.

Don't memorize:

> "SQL = Consistent, NoSQL = Eventually Consistent"

That is too simplistic.

Modern databases can support different consistency models.

---

## 12. When Should You Choose SQL?

Choose SQL when you have:

### Strong Relationships

```
Customer
   ↓
Orders
   ↓
Order Items
   ↓
Products
```

### Strong Transactions

For example:

- Payment
- Bank transfer
- Inventory update
- Financial transaction

### Complex Queries

For example:

- JOIN
- GROUP BY
- HAVING
- ORDER BY
- Aggregations

### Strong Consistency Requirements

Examples:

- Banking
- Payments
- Accounting
- Inventory

---

## 13. When Should You Choose NoSQL?

NoSQL can be a good choice when you need:

### Massive Scale

> Millions/Billions of records

### High Write Throughput

For example:

- Social media likes
- IoT events
- Application telemetry
- Click streams

### Flexible Schema

For example:

```
Product A → different attributes
Product B → completely different attributes
Product C → additional attributes
```

### Simple Access Patterns

For example:

```
userId → user profile
userId → shopping cart
deviceId → latest sensor data
```

### Distributed Architecture

When you need:

```
High Availability
+
Horizontal Scaling
+
Distributed Storage
```

---

## 14. Don't Think "SQL vs NoSQL"

A common System Design interview mistake is:

> "The system is huge, therefore I'll use NoSQL."

That's not necessarily correct.

Instead, ask:

### Step 1 — What Data Do I Have?

```
Users
Orders
Payments
Products
Events
```

### Step 2 — What Are My Access Patterns?

```
Get user by ID
Get orders by customer
Search products
Get latest events
```

### Step 3 — Do I Need Transactions?

```
YES → SQL may be a strong candidate
NO/limited → NoSQL may be suitable
```

### Step 4 — Do I Need Complex Relationships?

```
YES → SQL
NO → NoSQL may work
```

### Step 5 — What's My Scale?

```
10K users
     ↓
1M users
     ↓
100M users
```

### Step 6 — What Consistency Do I Need?

```
Strong consistency
        vs
Eventual consistency
```

### Step 7 — What Is the Read/Write Pattern?

```
Read-heavy?
Write-heavy?
Both?
```

---

## 15. Real System Design Example

Suppose you're designing Amazon-like e-commerce.

Don't necessarily choose one database for everything.

You might have:

```
                    E-Commerce System
                           |
        ┌──────────────────┼──────────────────┐
        ↓                  ↓                  ↓
     Users              Orders             Products
        ↓                  ↓                  ↓
       SQL                SQL              SQL/NoSQL
        |
        ↓
    Payments
        |
       SQL
```

For high-volume events:

```
Application
     ↓
   Kafka
     ↓
 Event Storage
     ↓
 Cassandra / DynamoDB / Data Lake
```

For caching:

```
Application
     ↓
    Redis
```

So a real production system often uses **polyglot persistence**:

> Use different databases for different problems.

---

## 16. Very Important Interview Concept

Instead of saying:

> "I'll use MongoDB because NoSQL scales better."

Say:

> "I'll choose the database based on the data model, access patterns, consistency requirements, transaction requirements, and expected scale."

That's a much stronger System Design answer.

---

## 17. Simple Decision Tree

```
                    Start
                      |
             Do you need strong
                transactions?
                 /          \
               YES           NO
                |             |
               SQL       Do you have
                         massive scale/
                         distributed data?
                           /      \
                         YES       NO
                          |         |
                       NoSQL     SQL/NoSQL
```

But remember: this is a guideline, not a hard rule.

---

## 18. Quick Memory Trick

Think of it this way:

```
SQL
 │
 ├── Tables
 ├── Relationships
 ├── JOIN
 ├── ACID
 ├── Transactions
 └── Structured data


NoSQL
 │
 ├── Flexible data model
 ├── Horizontal scaling
 ├── Distributed systems
 ├── High throughput
 ├── Large-scale workloads
 └── Access-pattern driven design
```

### One-Line Interview Answer

> SQL databases are generally preferred when structured data, relationships, complex queries, and strong transactional guarantees are important, while NoSQL databases are often preferred for flexible data models, massive distributed workloads, high throughput, and access patterns that don't require relational joins.
