# Database Transactions

A transaction is a group of database operations that are treated as one logical unit of work.

The key idea is:

**Either all operations in a transaction succeed, or none of them should take effect.**

Transactions are fundamental when building systems such as banking, payments, order processing, inventory, booking, and financial applications.

---

## 1. Simple Example

Suppose Rahul wants to transfer ₹1,000 from Account A to Account B.

Initial state:

    Account A = ₹10,000
    Account B = ₹5,000

The transfer requires two operations:

1. Deduct ₹1,000 from Account A
2. Add ₹1,000 to Account B

After the transaction:

    Account A = ₹9,000
    Account B = ₹6,000

The important requirement is:

    A: -₹1,000
    B: +₹1,000

Both operations must succeed together.

---

## 2. What Happens Without a Transaction?

Imagine the application executes:

```sql
UPDATE accounts
SET balance = balance - 1000
WHERE id = 1;
```

This succeeds:

    Account A = ₹9,000

Then:

```sql
UPDATE accounts
SET balance = balance + 1000
WHERE id = 2;
```

Suppose the database crashes before this operation completes.

Now we have:

    Account A = ₹9,000
    Account B = ₹5,000

₹1,000 has effectively disappeared.

**That's a serious consistency problem.**

---

## 3. With a Transaction

We instead execute:

```sql
BEGIN;

UPDATE accounts
SET balance = balance - 1000
WHERE id = 1;

UPDATE accounts
SET balance = balance + 1000
WHERE id = 2;

COMMIT;
```

Conceptually:

```
BEGIN
  │
  ├── Deduct ₹1,000
  │
  ├── Add ₹1,000
  │
  ▼
COMMIT
```

If everything succeeds:

```
COMMIT
   ↓
Changes become permanent
```

If something fails:

```
ROLLBACK
   ↓
Undo transaction changes
```

---

## 4. COMMIT

COMMIT means:

**"Make all changes made by this transaction permanent."**

Example:

```sql
BEGIN;

UPDATE accounts
SET balance = balance - 1000
WHERE id = 1;

UPDATE accounts
SET balance = balance + 1000
WHERE id = 2;

COMMIT;
```

After COMMIT:

    Account A = ₹9,000
    Account B = ₹6,000

The transaction has successfully completed.

---

## 5. ROLLBACK

ROLLBACK means:

**"Cancel the transaction and undo its changes."**

Example:

```sql
BEGIN;

UPDATE accounts
SET balance = balance - 1000
WHERE id = 1;

-- Something goes wrong

ROLLBACK;
```

The database returns to the previous state:

    Account A = ₹10,000
    Account B = ₹5,000

---

## 6. Transaction Lifecycle

A transaction typically follows this flow:

```
             BEGIN
               │
               ▼
        ┌──────────────┐
        │ Execute SQL  │
        └──────┬───────┘
               │
          Everything OK?
           /          \
         YES           NO
          │             │
          ▼             ▼
       COMMIT        ROLLBACK
          │             │
          ▼             ▼
     Permanent       Changes
       changes        undone
```

---

## 7. ACID Properties

Transactions are closely associated with ACID.

You already studied ACID; transactions are where these guarantees become especially important.

```
ACID
 │
 ├── Atomicity
 ├── Consistency
 ├── Isolation
 └── Durability
```

Let's understand each one using our bank transfer.

---

## 8. Atomicity

**Atomicity = All or Nothing**

Our transaction contains:

1. Deduct ₹1,000
2. Add ₹1,000

Atomicity means:

```
Both succeed
     OR
Both are rolled back
```

Not:

```
Deduct succeeds
Add fails
```

So:

```
Atomicity
    ↓
All operations succeed
       OR
No operation takes effect
```

---

## 9. Consistency

Consistency means the transaction takes the database from one valid state to another valid state, preserving defined constraints and rules.

Before:

    A = ₹10,000
    B = ₹5,000

    Total = ₹15,000

After transferring ₹1,000:

    A = ₹9,000
    B = ₹6,000

    Total = ₹15,000

The system's business/database invariants remain valid.

For example, if the database has:

    balance >= 0

a valid transaction should not violate that constraint.

---

## 10. Isolation

Imagine two users simultaneously transfer money.

    Transaction T1
    Transaction T2

Both are executing at the same time.

Isolation controls how concurrent transactions interact with each other and what intermediate states they can observe.

Without proper isolation, you can get problems such as:

- Dirty Read
- Non-Repeatable Read
- Phantom Read
- Lost Update

This is a major topic in database system design.

---

## 11. Durability

Once:

    COMMIT

successfully completes, the changes should survive a crash or restart, subject to the database's durability guarantees and configuration.

For example:

```
Transaction
    │
    ▼
 COMMIT
    │
    ▼
Database crashes
    │
    ▼
Database restarts
    │
    ▼
Committed data remains
```

This is **Durability**.

---

## 12. Real-World Example — Order Creation

Consider an e-commerce application.

When a user places an order, you might need to:

1. Create Order
2. Create Order Items
3. Reduce Inventory
4. Record Payment-related state

Conceptually:

```
BEGIN TRANSACTION

Create Order
     ↓
Create Order Items
     ↓
Reduce Inventory
     ↓
Save required payment/order state
     ↓
COMMIT
```

If inventory update fails:

    ROLLBACK

The system should not leave behind a partially created order unless the architecture intentionally supports such intermediate states.

---

## 13. Transaction in Spring Boot

Since you're working with Java/Spring, you'll frequently encounter:

```java
@Transactional
public void transferMoney(
        Long fromAccount,
        Long toAccount,
        BigDecimal amount) {

    debit(fromAccount, amount);
    credit(toAccount, amount);
}
```

`@Transactional` tells Spring to execute the method within a transaction under the configured transaction manager.

Conceptually:

```
@Transactional
      │
      ▼
BEGIN
      │
      ├── debit()
      │
      ├── credit()
      │
      ▼
   Success?
    /     \
  YES      NO
   │        │
   ▼        ▼
COMMIT   ROLLBACK
```

There are many details around propagation, isolation, rollback rules, and transaction managers, but this is the core idea.

---

## 14. Transaction Boundaries

One of the most important design decisions is:

**Where should the transaction start and end?**

For example:

```java
@Transactional
public void createOrder() {
    createOrderRecord();
    createOrderItems();
    updateInventory();
}
```

The entire operation is treated as one transaction.

But you shouldn't automatically put `@Transactional` on every method.

A transaction has costs:

- Locks may be held
- Database connections may remain occupied
- Resources are consumed
- Long transactions can increase contention
- Rollbacks can become expensive

Therefore, transactions should generally be as short as practical while still covering the required atomic business operation.

---

## 15. Long-Running Transactions

Avoid doing this:

```
BEGIN TRANSACTION
       │
       ▼
Call external API
       │
       ▼
Wait 5 seconds
       │
       ▼
Call another service
       │
       ▼
Wait 10 seconds
       │
       ▼
COMMIT
```

You are holding database resources while waiting for external systems.

A better architecture often separates local database transactions from distributed workflows:

```
Database Transaction
       │
       ▼
Commit local state
       │
       ▼
Publish event
       │
       ▼
Other service processes event
```

Patterns such as **Transactional Outbox** and **Saga** become relevant here.

---

## 16. Database Transaction vs Distributed Transaction

This distinction is very important in system design.

### Local Database Transaction

Operations happen inside the same transactional database boundary:

```
Order Service
     │
     ▼
 PostgreSQL
     │
 ├── Orders
 ├── Order Items
 └── Inventory
```

A database transaction can potentially cover these operations.

### Distributed Transaction

Now imagine:

```
Order Service
     │
     ▼
Order DB

Payment Service
     │
     ▼
Payment DB

Inventory Service
     │
     ▼
Inventory DB
```

You can't simply assume one normal local database transaction can atomically cover all three databases.

You now have a **distributed consistency problem**.

This is where patterns such as:

- Saga
- Transactional Outbox
- Event-driven architecture
- 2-Phase Commit

can become relevant depending on requirements.

---

## 17. Transaction and Microservices

Suppose:

```
Order Service
      │
      ▼
Payment Service
      │
      ▼
Inventory Service
```

A common mistake is to think:

```
BEGIN
   ↓
Order DB
   ↓
Payment DB
   ↓
Inventory DB
   ↓
COMMIT
```

as if all three could participate in one simple local transaction.

In microservices, each service typically owns its own database:

```
Order Service ──→ Order DB

Payment Service ─→ Payment DB

Inventory Service → Inventory DB
```

Therefore, the system often uses distributed transaction patterns rather than one traditional local DB transaction.

---

## 18. Transaction + Isolation Levels

Transactions also introduce the question:

**What happens when multiple transactions run concurrently?**

For example:

```
T1                         T2
│                          │
│ Read balance ₹10,000     │
│                          │ Read balance ₹10,000
│                          │
│ Update ₹9,000            │
│                          │ Update ₹8,000
│                          │
└──────────────┬───────────┘
               ▼
          Conflict
```

Database isolation levels control the visibility and interaction of concurrent transactions.

The commonly discussed levels are:

- READ UNCOMMITTED
- READ COMMITTED
- REPEATABLE READ
- SERIALIZABLE

These are worth studying separately because they are a major interview topic.

---

## 19. Transaction vs Query

Don't confuse a query with a transaction.

A query can be one SQL operation:

```sql
SELECT *
FROM users
WHERE id = 100;
```

A transaction can contain multiple operations:

```sql
BEGIN;

UPDATE accounts ...;

INSERT INTO transactions ...;

UPDATE audit_log ...;

COMMIT;
```

So:

```
Query
  ↓
One database operation

Transaction
  ↓
One or more operations
treated as one logical unit
```

---

## 20. Transaction vs ACID

Think of the relationship like this:

```
              TRANSACTION
                   │
                   ▼
                 ACID
                   │
       ┌───────────┼───────────┐
       ▼           ▼           ▼
  Atomicity   Consistency   Isolation
                               │
                               ▼
                           Durability
```

A transaction gives you a mechanism for grouping operations, while ACID describes important guarantees associated with transactional behavior.

---

## 21. System Design Example

Imagine you're designing a **Ticket Booking System**.

Two users try to book the same seat:

```
             Seat A10
                │
        ┌───────┴───────┐
        ▼               ▼
      User A          User B
```

Initial state:

    A10 = AVAILABLE

Without proper transactional/concurrency control:

```
User A → reads AVAILABLE
User B → reads AVAILABLE

User A → BOOKED
User B → BOOKED
```

Now two users think they own the same seat.

A properly designed system needs appropriate transactional and concurrency controls so that the business invariant is preserved:

    A10 → only one successful booking

This is where **transactions + isolation + locking/concurrency control** work together.

---

## 22. Interview Answer

**If an interviewer asks:**

### "What is a database transaction?"

A strong answer is:

> A database transaction is a logical unit of work consisting of one or more database operations that are executed together. The transaction either commits all required changes or rolls them back, providing important guarantees such as atomicity, consistency, isolation, and durability. Transactions are especially important for operations such as payments, money transfers, order creation, inventory updates, and booking systems.

---

## 23. The Big Picture

You have now covered several important database/system-design concepts:

```
                    DATABASE
                       │
        ┌──────────────┼──────────────┐
        │              │              │
        ▼              ▼              ▼
    Indexing       Transactions    Replication
        │              │
        │              ▼
        │             ACID
        │              │
        │       ┌──────┼──────┐
        │       ▼      ▼      ▼
        │       A      C      I
        │              │      │
        │              │      └── Isolation Levels
        │              │
        │              └── Consistency
        │
        ▼
   Faster Reads
```

And this connects to your previous topic:

```
CAP Theorem
     │
     ├── Consistency
     ├── Availability
     └── Partition Tolerance
```

CAP Consistency and ACID Consistency are related concepts but not the same thing.

---

### Most Important Takeaway

> A transaction groups related database operations into a single logical unit so that the database can maintain correctness when operations succeed, fail, or execute concurrently.
