# Transaction + Isolation Levels

Transactions and Isolation Levels are closely related.

A transaction tells us:

**Which database operations should be treated as one logical unit?**

Isolation level tells us:

**When multiple transactions run at the same time, what data is each transaction allowed to see?**

This is extremely important in system design, especially for banking, payments, inventory, booking, and order systems.

---

## 1. First Understand the Problem: Concurrent Transactions

Imagine we have:

    Account A
    Balance = ₹10,000

Two transactions execute at the same time:

```
T1                         T2
│                          │
│ Read balance             │
│                          │ Read balance
│                          │
│ Update balance           │
│                          │
│                          │ Update balance
│                          │
└──────────┬───────────────┘
           ▼
       Conflict?
```

If the database doesn't control this properly, we can get incorrect results.

That's why databases provide **Isolation Levels**.

---

## 2. What Does Isolation Mean?

Isolation means that concurrently executing transactions should not improperly interfere with each other.

Ideally, you want:

```
T1
 │
 ├── Operation 1
 ├── Operation 2
 └── Operation 3

T2
 │
 ├── Operation 1
 ├── Operation 2
 └── Operation 3
```

to behave as though their execution is appropriately isolated, while still allowing useful concurrency.

But complete isolation can be expensive.

So databases provide different levels of isolation.

---

## 3. The Four Standard Isolation Levels

The commonly discussed SQL isolation levels are:

```
┌──────────────────────┐
│  READ UNCOMMITTED    │
├──────────────────────┤
│  READ COMMITTED      │
├──────────────────────┤
│  REPEATABLE READ     │
├──────────────────────┤
│  SERIALIZABLE        │
└──────────────────────┘
```

As isolation increases:

```
More isolation
      ↑
      │
SERIALIZABLE
      │
REPEATABLE READ
      │
READ COMMITTED
      │
READ UNCOMMITTED
      │
      └──────────────→ More concurrency / generally less coordination
```

The exact implementation and behavior can vary by database engine, so don't assume every database implements these levels identically.

---

## 4. Problems Isolation Levels Protect Against

There are several classic concurrency problems:

### 1. Dirty Read

Reading data that another transaction has changed but not committed.

### 2. Non-Repeatable Read

Reading the same row twice in one transaction and getting different values because another transaction committed an update between the reads.

### 3. Phantom Read

Running the same query twice and getting a different set of rows because another transaction inserted/deleted matching rows.

### 4. Lost Update

Two transactions update the same data and one update unintentionally overwrites the other.

Let's understand each one.

---

## 5. Dirty Read

Suppose:

    Balance = ₹10,000

Transaction T1:

```
T1
│
├── UPDATE balance = ₹8,000
│
│   ❌ NOT COMMITTED YET
```

Transaction T2 reads the balance:

```
T2
│
└── SELECT balance
        ↓
      ₹8,000
```

But then T1 fails:

```
T1
│
└── ROLLBACK
```

The actual balance returns to:

    ₹10,000

But T2 already saw:

    ₹8,000

T2 read data that was never committed.

That's a:

**Dirty Read**

---

## 6. READ UNCOMMITTED

At this level, a transaction may be allowed to see uncommitted changes made by another transaction.

Example:

    Initial balance = ₹10,000

```
T1                          T2
│                           │
│ UPDATE → ₹8,000           │
│                           │
│                           │ READ
│                           │ ↓
│                           │ ₹8,000
│                           │
│ ROLLBACK                   │
│                           │
```

T2 saw:

    ₹8,000

even though that value was rolled back.

### Characteristics

| Problem | Status |
|---|---|
| Dirty Read | ❌ Possible |
| Non-repeatable | ❌ Possible |
| Phantom Read | ❌ Possible |

It's the lowest isolation level.

**When would you use it?**

Very rarely for correctness-sensitive business operations.

---

## 7. READ COMMITTED

At this level:

**A transaction can only read data that has been committed.**

Let's repeat the previous example.

    Initial = ₹10,000

```
T1                          T2
│                           │
│ UPDATE → ₹8,000           │
│                           │
│                           │ READ
│                           │ ↓
│                           │ ₹10,000
│                           │
│ COMMIT                     │
│                           │
│                           │ READ again
│                           │ ↓
│                           │ ₹8,000
```

T2 does not see the uncommitted ₹8,000.

After T1 commits, T2 can see ₹8,000 on a later read.

So:

    Dirty Read → Prevented

But there is another problem.

---

## 8. Non-Repeatable Read

Suppose T1 reads:

    Balance = ₹10,000

Then T2 updates and commits:

    Balance = ₹8,000

T1 reads again:

    Balance = ₹8,000

Within the same transaction:

    First read  → ₹10,000
    Second read → ₹8,000

Same row.

Different result.

That's:

**Non-Repeatable Read**

Conceptually:

```
T1                         T2
│                          │
│ READ → ₹10,000           │
│                          │
│                          │ UPDATE → ₹8,000
│                          │ COMMIT
│                          │
│ READ → ₹8,000            │
│                          │
```

READ COMMITTED prevents dirty reads, but non-repeatable reads can still occur.

---

## 9. REPEATABLE READ

REPEATABLE READ provides a stronger guarantee:

**If you read a row during a transaction, repeated reads of that row should return a consistent result according to the database's isolation semantics.**

Conceptually:

```
T1                         T2
│                          │
│ READ → ₹10,000           │
│                          │
│                          │ UPDATE → ₹8,000
│                          │ COMMIT
│                          │
│ READ → ₹10,000           │
│                          │
```

T1 continues to see the appropriate consistent version of the row.

Therefore:

    Dirty Read           → Prevented
    Non-repeatable Read  → Prevented

But traditionally:

    Phantom Read → May still occur

However, this is database-specific. For example, PostgreSQL's implementation of Repeatable Read provides stronger behavior than the minimum required by the SQL standard and prevents phantom reads under its snapshot-isolation implementation.

---

## 10. Phantom Read

A phantom read is different from a non-repeatable read.

Suppose T1 executes:

```sql
SELECT *
FROM orders
WHERE amount > 1000;
```

It gets:

    Order 1
    Order 2

Now T2 inserts:

    Order 3
    amount = ₹2,000

and commits.

T1 executes the same query again:

```sql
SELECT *
FROM orders
WHERE amount > 1000;
```

Now it gets:

    Order 1
    Order 2
    Order 3

A new matching row appeared.

That's a:

**Phantom Read**

Think:

```
First query:
[Order 1, Order 2]

       ↓

Another transaction inserts Order 3

       ↓

Second query:
[Order 1, Order 2, Order 3]
```

The set of rows changed.

---

## 11. SERIALIZABLE

Serializable is the strongest standard isolation level.

The goal is:

**Concurrent transactions should produce a result equivalent to some serial execution of those transactions.**

Imagine:

```
T1
 │
 ▼
Execute completely
 │
 ▼
Commit

T2
 │
 ▼
Execute completely
 │
 ▼
Commit
```

as if they were executed one after another.

Conceptually:

```
T1 ──────────────►
                    T2 ──────────────►
```

instead of allowing problematic interleavings:

```
T1 ──────┐
         ├───────
T2 ──────┘
```

The database may use locking, serialization checks, or MVCC-based conflict detection, depending on the engine.

If the database detects a conflict, it may:

```
ROLLBACK
   ↓
Retry transaction
```

---

## 12. Isolation Level Comparison

A simplified comparison:

| Isolation Level | Dirty Read | Non-Repeatable Read | Phantom Read |
|---|---|---|---|
| Read Uncommitted | ❌ Possible | ❌ Possible | ❌ Possible |
| Read Committed | ✅ Prevented | ❌ Possible | ❌ Possible |
| Repeatable Read | ✅ Prevented | ✅ Prevented | ⚠️ Database-dependent |
| Serializable | ✅ Prevented | ✅ Prevented | ✅ Prevented |

Here:

    ✅ = prevented
    ❌ = possible
    ⚠️ = depends on implementation/database

---

## 13. Simple Memory Trick

Remember:

```
READ UNCOMMITTED
      ↓
"Anything can be read"

READ COMMITTED
      ↓
"Only committed data"

REPEATABLE READ
      ↓
"Same row → consistent result"

SERIALIZABLE
      ↓
"Act as if transactions ran one-by-one"
```

---

## 14. Isolation vs Performance

There is a trade-off.

Generally:

```
More Isolation
      ↓
More coordination/conflict handling
      ↓
Potentially less concurrency
      ↓
Potentially lower throughput
```

While:

```
Less Isolation
      ↓
More concurrency
      ↓
Higher throughput potential
      ↓
More anomalies may be allowed
```

So you shouldn't simply say:

    "Always use SERIALIZABLE."

Instead ask:

    What consistency does the business operation actually require?

---

## 15. Real-Time Example — Bank Transfer

Suppose:

    Account A = ₹10,000

Two withdrawals happen concurrently:

    T1 → Withdraw ₹8,000
    T2 → Withdraw ₹7,000

If both transactions independently read:

    ₹10,000

they may both conclude that the withdrawal is possible.

You could end up with an invalid result.

This is a classic concurrency-control problem.

The system needs appropriate transaction isolation and/or explicit locking.

---

## 16. Real-Time Example — Ticket Booking

Suppose:

    Seat A10 = AVAILABLE

Two users try to book it:

    T1 → User A
    T2 → User B

Without appropriate concurrency control:

```
T1                          T2
│                           │
│ Read A10 = AVAILABLE      │
│                           │ Read A10 = AVAILABLE
│                           │
│ Book A10                  │
│                           │ Book A10
│                           │
└───────────┬───────────────┘
            ▼
       Double Booking ❌
```

A properly designed system must ensure that only one transaction can successfully claim the seat.

Depending on the design, this might involve:

- Appropriate isolation
- Row-level locking
- Optimistic concurrency control
- Unique constraints
- Conditional updates

For example:

```sql
UPDATE seats
SET status = 'BOOKED'
WHERE seat_id = 10
  AND status = 'AVAILABLE';
```

Then check the number of affected rows.

If:

    affected rows = 1

booking succeeded.

If:

    affected rows = 0

someone else already claimed it.

This can sometimes be a better approach than simply choosing a very high isolation level for the entire transaction.

---

## 17. Optimistic vs Pessimistic Concurrency

Isolation levels are not the only way to handle concurrent updates.

Two common approaches are:

### Pessimistic

"Assume conflicts are likely; lock the data."

Conceptually:

```
T1
 │
 ├── Lock row
 │
 ├── Update
 │
 └── Commit
       │
       ▼
     Unlock
       │
       ▼
T2 can proceed
```

Example:

```sql
SELECT *
FROM seats
WHERE seat_id = 10
FOR UPDATE;
```

The exact locking behavior is database-dependent.

### Optimistic

"Assume conflicts are uncommon; detect conflicts when updating."

Example:

```
Seat:
id = 10
status = AVAILABLE
version = 5
```

Application reads:

    version = 5

Then updates conditionally:

```sql
UPDATE seats
SET status = 'BOOKED',
    version = 6
WHERE id = 10
AND version = 5;
```

If:

    affected rows = 1

Success.

If:

    affected rows = 0

Someone else modified the row.

---

## 18. Transaction + Isolation + Locking

These concepts work together:

```
                 Transaction
                     │
                     ▼
             Multiple operations
                     │
                     ▼
                Isolation
                     │
          ┌──────────┴──────────┐
          ▼                     ▼
    Visibility rules       Concurrency
                                │
                       ┌────────┴────────┐
                       ▼                 ▼
                  Pessimistic       Optimistic
                    locking         concurrency
```

Don't think that isolation level = locking.

Isolation is the guarantee; the database can implement that guarantee using different mechanisms such as locking, MVCC, snapshots, conflict detection, or combinations of these.

---

## 19. Spring Boot Example

In Spring:

```java
@Transactional(
    isolation = Isolation.READ_COMMITTED
)
public void transferMoney(
        Long fromAccount,
        Long toAccount,
        BigDecimal amount) {

    debit(fromAccount, amount);
    credit(toAccount, amount);
}
```

You can configure an isolation level for the transaction.

Common Spring options include:

- `Isolation.READ_UNCOMMITTED`
- `Isolation.READ_COMMITTED`
- `Isolation.REPEATABLE_READ`
- `Isolation.SERIALIZABLE`

The actual behavior ultimately depends on the underlying database and transaction manager.

---

## 20. Default Isolation Level

Don't assume that every database uses the same default isolation level.

For example, common defaults include:

| Database | Default Isolation Level |
|---|---|
| PostgreSQL | READ COMMITTED |
| MySQL InnoDB | REPEATABLE READ |

Therefore, in a real project:

**Always know the database engine and its transaction/isolation semantics before making assumptions.**

---

## 21. How to Choose an Isolation Level

A practical decision process:

```
                 Business Operation
                         │
                         ▼
                What can go wrong?
                         │
          ┌──────────────┼──────────────┐
          ▼              ▼              ▼
       Stale data     Lost update    Double booking
          │              │              │
          └──────────────┼──────────────┘
                         ▼
                 Choose concurrency
                    strategy
                         │
             ┌───────────┴───────────┐
             ▼                       ▼
        Isolation level         Explicit locking /
                                optimistic control
```

Don't choose an isolation level based purely on performance.

**Choose it based on business invariants and concurrency requirements.**

---

## 22. Example: Different Business Requirements

### Social Media Likes

    Like count = 1,000

If it temporarily becomes:

    User A sees 1,001
    User B sees 1,000

that's usually acceptable.

You don't need the strongest transaction isolation for every operation.

### Bank Transfer

    A → -₹1,000
    B → +₹1,000

The operation must preserve important financial invariants.

You need much stronger transactional/concurrency guarantees.

### Seat Booking

    A10 = AVAILABLE

Only one user should successfully reserve it.

You need appropriate concurrency control to prevent double booking.

---

## 23. Isolation Levels in One Diagram

```
                    ISOLATION
                        │
        ┌───────────────┼────────────────┐
        │               │                │
        ▼               ▼                ▼
 Dirty Read      Non-Repeatable      Phantom
                     Read             Read
        │               │                │
        └───────────────┼────────────────┘
                        │
                        ▼
              Different Isolation
                    Levels
                        │
      ┌─────────────────┼──────────────────┐
      ▼                 ▼                  ▼
Read Uncommitted  Read Committed    Repeatable Read
                                           │
                                           ▼
                                     Serializable
```

---

## 24. Interview Answer

**If the interviewer asks:**

### "What are transaction isolation levels?"

You can answer:

> Transaction isolation levels define how concurrently executing transactions interact with each other and what changes made by other transactions they can observe. The standard levels are Read Uncommitted, Read Committed, Repeatable Read, and Serializable. As isolation increases, more concurrency anomalies are prevented, but the system may require more coordination and can experience lower concurrency or higher contention.

**If asked:**

### "Which isolation level should I use?"

A good answer is:

> It depends on the business requirement. I would start with the database's default isolation level, identify the consistency guarantees required by the operation, and then use stronger isolation or explicit concurrency mechanisms such as row locks or optimistic locking where necessary. I would avoid using SERIALIZABLE everywhere because it can unnecessarily reduce concurrency.

---

## 25. Final Mental Model

For system design, remember this hierarchy:

```
TRANSACTION
     │
     │ Groups operations
     ▼
  ACID
     │
     ├── Atomicity
     ├── Consistency
     ├── Isolation
     └── Durability
            │
            ▼
     ISOLATION LEVEL
            │
     ┌──────┼──────────────┐
     ▼      ▼              ▼
   What    What can       How much
   can     others see?    concurrency?
   happen?
            │
            ▼
 ┌───────────────────────────────┐
 │ READ UNCOMMITTED              │
 │ READ COMMITTED                │
 │ REPEATABLE READ               │
 │ SERIALIZABLE                  │
 └───────────────────────────────┘
```

---

### The Three Concepts to Keep Separate

**Transaction**

"These operations belong together."

**Isolation Level**

"What can concurrent transactions see?"

**Locking / Concurrency Control**

"How does the database prevent conflicting operations?"

Once you understand these three separately, topics like row-level locking, optimistic locking, MVCC, deadlocks, and distributed transactions become much easier to understand.
