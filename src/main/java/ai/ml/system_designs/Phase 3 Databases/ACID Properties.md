# ACID Properties in Database

ACID is a set of properties that ensures database transactions are reliable, consistent, and safe, especially when multiple users/services are accessing the database simultaneously.

**ACID stands for:**

- **A** — Atomicity
- **C** — Consistency
- **I** — Isolation
- **D** — Durability

Let's understand each with a real-world example.

---

## 1. Atomicity — "All or Nothing"

Atomicity means a transaction is treated as one indivisible unit.

Either:

- Everything succeeds, or
- Everything is rolled back

### Real-Time Example: Bank Transfer

Suppose you transfer ₹1,000 from Account A to Account B.

There are two database operations:

1. Deduct ₹1,000 from Account A
2. Add ₹1,000 to Account B

The transaction should behave like:

```sql
BEGIN TRANSACTION

A.balance = A.balance - 1000
B.balance = B.balance + 1000

COMMIT
```

### What If Step 2 Fails?

**Without Atomicity:**

```
Account A: ₹10,000 → ₹9,000
Account B: ₹5,000  → ₹5,000

₹1,000 disappeared ❌
```

**With Atomicity:**

```
Step 1 → SUCCESS
Step 2 → FAILURE

ROLLBACK

Account A → ₹10,000
Account B → ₹5,000
```

So:

> **Atomicity = Either the complete transaction happens or none of it happens.**

---

## 2. Consistency — "Valid State → Valid State"

Consistency means a transaction must take the database from one valid state to another valid state.

The database constraints and business rules must remain valid.

### Example

Suppose:

```
Account A = ₹10,000
Account B = ₹5,000
```

Total money:

```
₹15,000
```

Transfer ₹1,000:

```
A = ₹9,000
B = ₹6,000
```

Total:

```
₹15,000
```

The database remains consistent.

### Database Constraints

Consistency can be enforced through things such as:

- PRIMARY KEY
- FOREIGN KEY
- UNIQUE
- NOT NULL
- CHECK

For example:

```sql
CREATE TABLE Account (
    id INT PRIMARY KEY,
    balance DECIMAL(10,2) CHECK (balance >= 0)
);
```

If a transaction tries to make:

```
balance = -500
```

the database can reject the transaction.

So:

> **Consistency = Database rules and constraints must remain valid after the transaction.**

---

## 3. Isolation — "Transactions Should Not Interfere"

Isolation means that when multiple transactions execute at the same time, they should not incorrectly interfere with each other.

### Real-Time Example

Suppose your bank account has:

```
Balance = ₹10,000
```

Two transactions happen simultaneously.

```
Transaction T1:
Withdraw ₹7,000

Transaction T2:
Withdraw ₹5,000
```

If both transactions read the balance at exactly the same time:

```
T1 reads → ₹10,000
T2 reads → ₹10,000
```

Both might think they have enough money.

**Without proper isolation:**

```
T1 → withdraw ₹7,000
T2 → withdraw ₹5,000

Total withdrawal = ₹12,000

Available = ₹10,000 ❌
```

Isolation mechanisms prevent such incorrect concurrent behavior.

### Isolation Levels

Most relational databases provide different isolation levels:

1. READ UNCOMMITTED
2. READ COMMITTED
3. REPEATABLE READ
4. SERIALIZABLE

Generally:

```
READ UNCOMMITTED
       ↓
READ COMMITTED
       ↓
REPEATABLE READ
       ↓
SERIALIZABLE
```

As isolation increases:

```
Consistency / safety ↑
Concurrency ↓
Potential performance ↓
```

We'll cover these isolation levels separately because they are very important for system design and interviews.

---

## 4. Durability — "Committed Data Will Survive"

Durability means once a transaction is successfully committed, the data should not be lost, even if there is a system crash.

### Example

You purchase something online.

```
Payment successful
        ↓
Order created
        ↓
Transaction COMMIT
```

The database confirms:

```
COMMIT SUCCESS
```

Immediately after that:

```
💥 Database server crashes
```

When the database comes back:

```
Order should still exist
Payment information should still exist
```

The committed transaction must survive.

Databases achieve durability using mechanisms such as:

- Transaction logs
- Write-ahead logging (WAL)
- Disk persistence
- Replication
- Recovery mechanisms

So:

> **Durability = Once committed, data survives failures.**

---

## ACID Using One Example

Consider an Amazon-like order system.

You place an order for a laptop.

A transaction might perform:

1. Create Order
2. Reduce Inventory
3. Record Payment

### Atomicity

Either:

```
Order created
+ Inventory reduced
+ Payment recorded
```

or:

```
Everything rolled back
```

### Consistency

Business rules must remain valid:

- Inventory cannot become negative
- Payment must be valid
- Order must reference an existing customer

### Isolation

Suppose 1 laptop remains and two customers try to purchase it simultaneously.

```
Customer A → Buy laptop
Customer B → Buy laptop
```

Isolation/concurrency control prevents both transactions from incorrectly claiming the same last item.

### Durability

After the order is committed:

```
Order = CONFIRMED
```

Even if the database crashes:

```
Database crash 💥
       ↓
Database restart
       ↓
Order still exists ✅
```

---

## ACID in One Table

| Property | Meaning | Simple Example |
|---|---|---|
| Atomicity | All or nothing | Transfer succeeds completely or rolls back |
| Consistency | Rules remain valid | Balance cannot become invalid |
| Isolation | Concurrent transactions don't incorrectly interfere | Two withdrawals handled safely |
| Durability | Committed data survives failures | Order remains after DB crash |

---

## ACID vs Transaction

These two concepts are related but different.

### Transaction

A transaction is a group of database operations treated as one unit.

```sql
BEGIN

INSERT ...
UPDATE ...
UPDATE ...

COMMIT
```

### ACID

ACID defines the properties that make that transaction reliable.

```
             Transaction
                  │
        ┌─────────┴─────────┐
        ↓                   ↓
     Operations          ACID Properties
                            │
              ┌─────────────┼─────────────┐
              ↓             ↓             ↓
         Atomicity     Consistency    Isolation
                                             +
                                         Durability
```

---

## ACID in System Design

This becomes particularly important when deciding between:

> SQL Database

and

> NoSQL / Distributed Database

Traditional relational databases such as PostgreSQL, MySQL, and Oracle strongly emphasize transactional ACID guarantees.

In distributed systems, things become more complicated because maintaining strong consistency across multiple nodes can introduce:

```
Higher latency
        +
Coordination
        +
Reduced availability in some failure scenarios
```

For example, imagine:

```
Order Service
      ↓
Payment Service
      ↓
Inventory Service
```

If these are separate databases, you cannot simply assume one ACID transaction spans all three services.

That's where concepts such as:

- Distributed Transactions
- 2-Phase Commit (2PC)
- Saga Pattern
- Transactional Outbox
- Eventual Consistency

become important.

---

## Interview-Friendly Definition

> ACID is a set of properties that guarantees database transactions are processed reliably: **Atomicity** ensures all-or-nothing execution, **Consistency** preserves database rules, **Isolation** controls interference between concurrent transactions, and **Durability** ensures committed data survives failures.
