# Distributed Transactions

A distributed transaction is a transaction that involves multiple services, databases, or machines but needs to behave like one logical transaction.

This is one of the most important concepts in distributed systems and microservices.

---

## 1. Start With a Normal Transaction

Suppose we have a single database:

    Transfer ₹1,000 from Account A to Account B

1. Deduct ₹1,000 from Account A
2. Add ₹1,000 to Account B
3. Commit

Both operations happen inside one database transaction:

```sql
BEGIN TRANSACTION

UPDATE account
SET balance = balance - 1000
WHERE id = 'A';

UPDATE account
SET balance = balance + 1000
WHERE id = 'B';

COMMIT;
```

If something fails:

    ROLLBACK

The database can guarantee **Atomicity**.

Either both operations happen, or neither happens.

---

## 2. What Changes in Microservices?

Now imagine an e-commerce application:

```
                Order Service
                     |
          -----------------------
          |                     |
      Order DB             Payment Service
                                |
                           Payment DB
                                |
                         Inventory Service
                                |
                          Inventory DB
```

Suppose a customer places an order.

We need to:

1. Create Order
2. Charge Payment
3. Reserve Inventory

But each service owns its own database:

```
Order Service       → Order DB
Payment Service     → Payment DB
Inventory Service   → Inventory DB
```

Now we have a **distributed transaction**.

---

## 3. The Problem

Imagine this sequence:

```
Order Service
     |
     | Create Order
     ↓
 Order DB
     |
     | SUCCESS
     ↓
Payment Service
     |
     | Charge ₹1,000
     ↓
 Payment DB
     |
     | SUCCESS
     ↓
Inventory Service
     |
     | Reserve product
     ↓
 Inventory DB
     |
     | FAILURE
```

Now we have:

```
Order     → SUCCESS
Payment   → SUCCESS
Inventory → FAILURE
```

The customer has been charged.

But the product wasn't reserved.

**This is the fundamental problem distributed transactions try to solve.**

---

## 4. What Do We Want?

Ideally:

    Order
    Payment
    Inventory

should behave like one transaction:

```
SUCCESS
   ↓
Commit everything
```

or:

```
FAILURE
   ↓
Undo everything
```

Conceptually:

```
                 Distributed Transaction
                         |
          ---------------------------------
          |               |               |
       Order DB       Payment DB      Inventory DB
          |               |               |
       Commit          Commit           Commit
```

The challenge is coordinating these independent systems.

---

## 5. Why Is It Difficult?

In a distributed system, failures can happen at many places.

For example:

```
Order DB       → SUCCESS
Payment DB     → SUCCESS
Network        → FAILURE
Inventory DB   → UNKNOWN
```

The coordinator might not know whether Inventory committed.

This creates an important problem:

**Did the operation fail, or did the response get lost?**

For example:

```
Coordinator
     |
     | Reserve Inventory
     ↓
Inventory Service
     |
     | Commit
     ↓
Inventory DB
     |
     X
   Network failure
     |
     ↓
Coordinator
```

The coordinator receives:

    "No response"

But the inventory operation may actually have succeeded.

This is why distributed transactions are much harder than local database transactions.

---

## 6. Two Major Approaches

There are two important approaches you should understand:

### Approach 1: Two-Phase Commit (2PC)

```
Coordinator
     |
     +---- Order DB
     |
     +---- Payment DB
     |
     +---- Inventory DB
```

The coordinator controls the transaction.

### Approach 2: Saga Pattern

Instead of keeping one global transaction open, each service performs a local transaction.

If something fails, previously completed operations are compensated.

```
Order
  ↓
Payment
  ↓
Inventory
  X
  ↓
Compensate Payment
  ↓
Cancel Order
```

Sagas are extremely common in microservices.

---

## 7. Two-Phase Commit (2PC)

Let's understand 2PC first.

2PC has:

- **Coordinator**
- **Participants**

Example:

```
              Coordinator
             /     |      \
            /      |       \
       Order DB Payment DB Inventory DB
```

There are two phases.

---

## 8. Phase 1 — Prepare

The coordinator asks every participant:

**"Can you commit this transaction?"**

For example:

```
Coordinator
     |
     | PREPARE
     |
     +------> Order DB
     |
     +------> Payment DB
     |
     +------> Inventory DB
```

Each database checks whether it can perform the operation.

Responses:

```
Order DB       → YES
Payment DB     → YES
Inventory DB   → YES
```

Now the coordinator knows everyone is ready.

---

## 9. Phase 2 — Commit

The coordinator sends:

    COMMIT

to everyone.

```
Coordinator
     |
     | COMMIT
     |
     +------> Order DB
     |
     +------> Payment DB
     |
     +------> Inventory DB
```

All participants commit.

```
Order     → COMMITTED
Payment   → COMMITTED
Inventory → COMMITTED
```

Transaction succeeds.

---

## 10. What If Prepare Fails?

Suppose:

```
Order DB       → YES
Payment DB     → YES
Inventory DB   → NO
```

The coordinator sends:

    ROLLBACK

to everyone.

```
Coordinator
     |
     | ROLLBACK
     |
     +------> Order DB
     |
     +------> Payment DB
     |
     +------> Inventory DB
```

Result:

```
Order     → ROLLBACK
Payment   → ROLLBACK
Inventory → ROLLBACK
```

---

## 11. 2PC Flow

```
                  Coordinator
                       |
                 1. PREPARE
                       |
        --------------------------------
        |              |               |
     Order DB      Payment DB     Inventory DB
        |              |               |
       YES            YES             YES
        |              |               |
        -------- Coordinator ----------
                       |
                  2. COMMIT
                       |
        --------------------------------
        |              |               |
     COMMIT          COMMIT          COMMIT
```

If any participant says NO:

```
                  Coordinator
                       |
                  PREPARE
                       |
        --------------------------------
        |              |               |
       YES            YES              NO
        |              |               |
        -------- Coordinator ----------
                       |
                    ABORT
                       |
        --------------------------------
        |              |               |
     ROLLBACK        ROLLBACK        ROLLBACK
```

---

## 12. Advantages of 2PC

### Strong consistency

All participants either commit or abort.

### Atomicity

The transaction behaves as one logical operation.

### Easy mental model

```
ALL → COMMIT
ANY → ROLLBACK
```

---

## 13. Disadvantages of 2PC

This is extremely important for interviews.

### 1. Blocking

Suppose:

```
Coordinator
     |
     | PREPARE
     ↓
Participants
```

Everyone says:

    YES

Then the coordinator crashes before sending COMMIT.

Participants may remain in a prepared state:

```
Order DB       → WAITING
Payment DB     → WAITING
Inventory DB   → WAITING
```

They don't know whether they should commit or rollback.

### 2. Performance

Distributed communication is expensive.

Instead of:

    Local transaction

we have:

```
Network
Network
Network
Disk
Network
Network
```

Latency increases.

### 3. Coordinator is critical

The coordinator becomes an important dependency.

```
             Coordinator
             /    |    \
            /     |     \
           DB     DB     DB
```

If the coordinator fails at the wrong moment, participants may be blocked.

### 4. Poor scalability

2PC can become expensive when hundreds or thousands of services participate.

---

## 14. Saga Pattern

Because 2PC has these problems, microservices frequently use the **Saga Pattern**.

A Saga breaks one large distributed transaction into a sequence of local transactions.

For example:

```
Create Order
     ↓
Charge Payment
     ↓
Reserve Inventory
     ↓
Ship Order
```

Each operation commits independently.

---

## 15. Saga With Compensation

Suppose:

```
Create Order       → SUCCESS
Charge Payment     → SUCCESS
Reserve Inventory  → FAILURE
```

Instead of a database rollback, we execute **compensating transactions**.

```
Reserve Inventory
       ↓
     FAILED
       ↓
Refund Payment
       ↓
Cancel Order
```

So:

```
T1: Create Order
T2: Charge Payment
T3: Reserve Inventory

If T3 fails:

C2: Refund Payment
C1: Cancel Order
```

Where:

    T = forward transaction
    C = compensating transaction

---

## 16. Important: Compensation Is Not Rollback

This distinction is very important.

A database rollback:

```sql
UPDATE balance = balance - 1000

ROLLBACK
```

can restore the previous database state.

But in a Saga:

    Charge Payment

might be compensated by:

    Refund Payment

The refund is a **new business operation**, not a database rollback.

For example:

```
Charge ₹1,000
     ↓
Payment successful
     ↓
Inventory fails
     ↓
Refund ₹1,000
```

The payment system may have already sent notifications, generated transaction IDs, etc.

So Saga provides **business-level compensation**, not true atomic rollback.

---

## 17. Saga Types

There are two major Saga approaches:

### 1. Choreography

Services communicate using events.

```
Order Service
     |
 OrderCreated
     ↓
Payment Service
     |
PaymentCompleted
     ↓
Inventory Service
     |
InventoryReserved
     ↓
Shipping Service
```

There is no central coordinator.

### 2. Orchestration

A central Saga Orchestrator controls the workflow.

```
             Saga Orchestrator
              /      |       \
             ↓       ↓        ↓
          Order    Payment  Inventory
          Service  Service   Service
```

The orchestrator says:

```
Create Order
     ↓
Charge Payment
     ↓
Reserve Inventory
```

If inventory fails:

```
Refund Payment
     ↓
Cancel Order
```

---

## 18. 2PC vs Saga

| Feature | 2PC | Saga |
|---|---|---|
| Consistency | Stronger | Usually eventual |
| Atomicity | Yes | Business-level |
| Rollback | Database rollback | Compensation |
| Performance | Lower | Better |
| Scalability | Limited | Better |
| Blocking | Possible | Generally avoids 2PC blocking |
| Microservices | Less common | Very common |
| Complexity | Infrastructure complexity | Business/workflow complexity |

---

## 19. Real-World E-Commerce Example

Suppose:

    Customer buys iPhone
    Price = ₹80,000

Services:

- Order Service
- Payment Service
- Inventory Service
- Shipping Service

### Saga:

```
1. Create Order
        ↓
2. Payment
        ↓
3. Reserve Inventory
        ↓
4. Create Shipment
```

Suppose shipment creation fails.

We may execute:

```
Create Order          SUCCESS
Payment               SUCCESS
Inventory Reservation SUCCESS
Shipment              FAILED
        ↓
Cancel Inventory Reservation
        ↓
Refund Payment
        ↓
Cancel Order
```

This is a **Saga transaction**.

---

## 20. Another Important Problem: Retry

Distributed systems frequently experience temporary failures.

For example:

```
Payment Service
     ↓
Timeout
```

The client might retry:

```
Payment Request #1 → Timeout
Payment Request #2 → Success
```

But what if request #1 actually succeeded and only the response was lost?

Now:

```
Request #1 → Payment successful
Request #2 → Payment successful
```

Customer could be charged twice.

Therefore distributed transactions often require **idempotency**.

For example:

    Idempotency-Key: ORDER-12345

Payment Service stores the result associated with that key.

If the same request arrives again:

    ORDER-12345

it returns the existing result rather than charging again.

---

## 21. Distributed Transaction + Outbox Pattern

Another common problem:

    Order Service

1. Save Order
2. Publish OrderCreated event

What if:

```
Save Order     → SUCCESS
Publish Event  → FAILURE
```

Now:

```
Order exists
BUT
event doesn't exist
```

The **Transactional Outbox Pattern** helps solve this.

```
             Order Service
                   |
             DB Transaction
              /          \
             ↓            ↓
        Order Table   Outbox Table
             |            |
          Order       OrderCreated
                          |
                          ↓
                       Kafka
```

The order and outbox event are written in the same local database transaction.

A separate publisher then sends the outbox event to Kafka.

This is often used together with Saga-based architectures.

---

## 22. The Big Picture

For system design interviews, think about distributed transactions like this:

```
              DISTRIBUTED TRANSACTION
                       |
          ---------------------------
          |                         |
         2PC                       Saga
          |                         |
   Central Coordinator       Local Transactions
          |                         |
 Prepare + Commit           Compensation
          |                         |
 Stronger consistency       Eventual consistency
          |                         |
 Blocking possible          Better scalability
```

### When to consider 2PC

Use it when:

- Strong atomicity is essential.
- The participants support the protocol.
- The number of participants is manageable.
- Performance and availability trade-offs are acceptable.

### When to consider Saga

Use it when:

- You're building microservices.
- Services own separate databases.
- Long-running workflows are involved.
- High scalability is required.
- Business operations can be compensated.

---

### Interview-Level Mental Model

Remember these five points:

1. **Local transaction** → one database can rollback atomically.
2. **Distributed transaction** → multiple independent systems must coordinate.
3. **2PC** → prepare → commit/abort.
4. **Saga** → local transactions + compensating transactions.
5. **Idempotency** → essential because distributed systems retry operations.

For a modern microservices system, **Saga + Outbox + Idempotency + retries/timeouts** is a very common combination.
