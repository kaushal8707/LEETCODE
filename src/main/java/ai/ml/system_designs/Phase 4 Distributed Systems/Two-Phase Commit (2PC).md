# Two-Phase Commit (2PC)

Two-Phase Commit (2PC) is a distributed transaction protocol used when one transaction needs to update multiple independent systems/databases and we want all of them to either commit together or roll back together.

It is mainly used to achieve **atomicity across distributed systems**.

---

## 1. Why Do We Need Two-Phase Commit?

Imagine an e-commerce order:

```
Customer places an order
        |
        +---- Order DB
        |
        +---- Payment DB
        |
        +---- Inventory DB
```

Suppose we have:

```
Order DB       → Order created ✅
Payment DB     → Payment successful ✅
Inventory DB   → Failed ❌
```

Now we have an inconsistent system:

```
Order = CREATED
Payment = PAID
Inventory = NOT RESERVED
```

That's a problem.

Ideally, we want:

```
ALL SUCCESS
    ↓
COMMIT EVERYTHING

OR

ANY FAILURE
    ↓
ROLLBACK EVERYTHING
```

This is the fundamental idea behind Two-Phase Commit.

---

## 2. Real-Time Example

Consider a bank transfer:

```
Account A
    |
    | Debit ₹1,000
    ↓
Bank A Database

Account B
    |
    | Credit ₹1,000
    ↓
Bank B Database
```

We don't want:

```
Bank A → ₹1,000 deducted ✅
Bank B → ₹1,000 not credited ❌
```

We want:

```
Bank A → Debit
Bank B → Credit

Both COMMIT
       OR
Both ROLLBACK
```

2PC tries to coordinate this decision.

---

## 3. Components of 2PC

There are two important types of participants.

### Coordinator

The Coordinator controls the distributed transaction.

```
                Coordinator
                     |
          +----------+----------+
          |                     |
          ↓                     ↓
    Participant 1         Participant 2
      Order DB             Payment DB
```

The coordinator asks participants:

    "Can you commit?"

And then tells them:

    "Commit" or "Rollback".

### Participants

Participants are the systems/databases involved in the transaction.

For example:

```
Coordinator
    |
    +---- Order DB
    |
    +---- Payment DB
    |
    +---- Inventory DB
```

Each participant manages its own local transaction.

---

## 4. Two Phases

There are exactly two major phases:

    Phase 1 → Prepare
    Phase 2 → Commit / Rollback

Let's understand them carefully.

---

## 5. Phase 1 — Prepare

The coordinator sends:

    PREPARE

to all participants.

Example:

```
             Coordinator
                  |
          PREPARE TRANSACTION
          /        |        \
         ↓         ↓         ↓
      Order     Payment   Inventory
        DB        DB         DB
```

Each participant checks:

**"Can I successfully commit this transaction?"**

It performs the necessary work locally.

For example:

```
Order DB:
    Validate order
    Write changes
    Lock required resources
    Prepare transaction

Payment DB:
    Validate payment
    Write changes
    Lock required resources
    Prepare transaction

Inventory DB:
    Check stock
    Reserve stock
    Prepare transaction
```

But they do **not** commit yet.

Instead, they respond:

    YES / PREPARED

or

    NO / ABORT

---

## 6. Example: Everyone Says YES

Suppose:

```
Order DB      → YES
Payment DB    → YES
Inventory DB  → YES
```

The coordinator now knows:

    Everyone is ready.

Therefore:

    COMMIT

---

## 7. Phase 2 — Commit

The coordinator sends:

    COMMIT

to all participants.

```
             Coordinator
                  |
               COMMIT
          /        |        \
         ↓         ↓         ↓
      Order     Payment   Inventory
        DB        DB         DB
```

Participants commit their local transactions:

```
Order DB      → COMMIT ✅
Payment DB    → COMMIT ✅
Inventory DB  → COMMIT ✅
```

The distributed transaction succeeds.

---

## 8. What If One Participant Says NO?

Suppose:

```
Order DB      → YES
Payment DB    → YES
Inventory DB  → NO ❌
```

The coordinator cannot commit.

Therefore:

```
             Coordinator
                  |
               ROLLBACK
          /        |        \
         ↓         ↓         ↓
      Order     Payment   Inventory
        DB        DB         DB
```

All participants roll back.

Result:

```
Order DB      → ROLLBACK
Payment DB    → ROLLBACK
Inventory DB  → ROLLBACK
```

This maintains **atomicity**.

---

## 9. Complete Flow

The complete 2PC flow looks like this:

```
                    Coordinator
                         |
              ┌──────────┴──────────┐
              |                     |
          PREPARE                 PREPARE
              |                     |
              ↓                     ↓
          Order DB              Payment DB
              |                     |
           YES/NO                 YES/NO
              |                     |
              └──────────┬──────────┘
                         |
                    Coordinator
                         |
               Are ALL participants
                     READY?
                    /       \
                  YES        NO
                   |          |
                   ↓          ↓
                COMMIT     ROLLBACK
                   |          |
             ┌─────┴───┐  ┌───┴─────┐
             ↓         ↓  ↓         ↓
          Order     Payment Order   Payment
```

---

## 10. The Most Important Rule

The coordinator follows:

```
ALL participants → YES
        ↓
     COMMIT
```

But:

```
ANY participant → NO
        ↓
     ROLLBACK
```

In simple terms:

**One NO is enough to abort the entire transaction.**

---

## 11. Detailed Example

Suppose we are placing an order for an iPhone.

We have:

- Order Service
- Payment Service
- Inventory Service

Transaction:

    T1001

### Step 1 — Start transaction

```
Coordinator
    |
    | Transaction T1001
    ↓
```

### Step 2 — Prepare

```
Coordinator
    |
    +---- PREPARE → Order Service
    |
    +---- PREPARE → Payment Service
    |
    +---- PREPARE → Inventory Service
```

Responses:

```
Order Service      → PREPARED
Payment Service    → PREPARED
Inventory Service  → PREPARED
```

### Step 3 — Coordinator decides

```
All participants prepared
             ↓
         COMMIT
```

### Step 4 — Commit

```
Coordinator
    |
    +---- COMMIT → Order
    |
    +---- COMMIT → Payment
    |
    +---- COMMIT → Inventory
```

Final state:

```
Order      → CREATED
Payment    → PAID
Inventory  → RESERVED
```

---

## 12. What Happens When Something Fails?

Suppose:

```
Order      → PREPARED
Payment    → PREPARED
Inventory  → ABORT
```

Coordinator decides:

    ROLLBACK

Then:

```
Order      → ROLLBACK
Payment    → ROLLBACK
Inventory  → ROLLBACK
```

Final state:

```
Order      → NOT CREATED
Payment    → NOT PAID
Inventory  → NOT RESERVED
```

---

## 13. The Biggest Problem with 2PC

2PC sounds perfect, but it has a major problem:

**Coordinator failure**

Consider:

```
Participant A
     |
     |
     ↓
Coordinator
     |
     |
     ↓
Participant B
```

During Phase 1:

```
A → PREPARED
B → PREPARED
```

Coordinator knows:

    ALL = PREPARED

Then coordinator decides:

    COMMIT

But before sending the COMMIT message:

    Coordinator 💥 CRASH

Now participants are stuck:

```
A → PREPARED
B → PREPARED
```

They don't know:

```
COMMIT?
       OR
ROLLBACK?
```

They cannot safely make the decision themselves.

---

## 14. Why Can't Participants Simply Rollback?

Because the coordinator may already have decided to commit.

Imagine:

```
Coordinator:

Decision = COMMIT
```

but:

    COMMIT message → lost

Participant doesn't know about the decision.

If participant independently chooses:

    ROLLBACK

while another participant has already committed:

```
Participant A → COMMIT
Participant B → ROLLBACK
```

We have **inconsistency**.

That's why participants may remain in a prepared/uncertain state until they can learn the final decision.

---

## 15. 2PC Can Block

This leads to the famous problem:

**Two-Phase Commit is a blocking protocol.**

A participant may have to hold:

- locks
- resources
- transaction state

while waiting for the coordinator.

For example:

```
Payment DB

Transaction T1001
        |
        ↓
PREPARED
        |
        ↓
Waiting for coordinator
        |
        ↓
Coordinator unavailable
        |
        ↓
Transaction blocked
```

If the coordinator is down for a long time, resources can remain locked.

---

## 16. Another Problem — Network Failure

Consider:

```
Coordinator
     |
     | COMMIT
     X
     |
Network failure
     |
     ↓
Participant
```

The participant doesn't know whether:

    COMMIT was sent?

or

    COMMIT was never sent?

Distributed systems are full of these ambiguous situations.

This is one reason distributed transactions are difficult.

---

## 17. 2PC vs Local Transaction

A normal database transaction is relatively simple:

```
Application
    |
    ↓
Database
    |
    ↓
BEGIN
    |
    ↓
UPDATE
    |
    ↓
COMMIT
```

2PC is:

```
                 Coordinator
                     |
             ┌───────┼───────┐
             ↓       ↓       ↓
           DB 1    DB 2    DB 3
```

Now we have to coordinate:

- multiple databases
- multiple network calls
- failures
- retries
- timeouts
- locks
- coordinator failures
- participant failures

That's why distributed transactions are much harder.

---

## 18. 2PC vs Saga

This is especially important in microservices system design.

| Feature | 2PC | Saga |
|---|---|---|
| Type | Distributed transaction | Distributed business transaction |
| Consistency | Strong atomicity | Eventual consistency |
| Coordination | Coordinator-based | Orchestration or choreography |
| Mechanism | Participants prepare/commit | Services execute local transactions |
| Blocking | Can block | Generally non-blocking |
| Resources | Holds locks/resources | Usually releases resources after local transaction |
| Consistency level | Strong consistency | Eventual consistency |
| Availability | Can have poor availability | Better availability |
| Coupling | Tightly coupled | More loosely coupled |
| Microservices | Often difficult across microservices | Common microservice approach |

For example:

### 2PC

```
Order
  ↓
Prepare
  ↓
Payment
  ↓
Prepare
  ↓
Inventory
  ↓
Prepare

ALL YES
  ↓
COMMIT
```

### Saga

```
Create Order
     ↓
Reserve Inventory
     ↓
Process Payment
     ↓
Confirm Order
```

If payment fails:

```
Payment FAILED
      ↓
Release Inventory
      ↓
Cancel Order
```

These are called **compensating transactions**.

---

## 19. 2PC vs Saga — When to Use Which?

### Use 2PC when:

- You genuinely require atomic distributed commits.
- Participants support a compatible transaction protocol.
- Strong consistency is more important than availability.
- The transaction scope is controlled.
- Blocking/resource locking is acceptable.

### Prefer Saga when:

- You're building microservices.
- Services own independent databases.
- High availability is important.
- Long-running business workflows are involved.
- Eventual consistency is acceptable.
- You can define compensating actions.

---

## 20. Where Does Kafka Fit?

A common misconception is:

    "Kafka provides distributed transactions, so Kafka solves 2PC."

**Not exactly.**

Kafka supports transactions within Kafka's ecosystem, but that doesn't automatically make this atomic:

```
Order DB
    +
Kafka
    +
Payment DB
```

For example:

```
DB transaction
      ↓
Save Order
      ↓
Publish Kafka Event
```

There can still be failure windows.

That's why patterns such as **Transactional Outbox** are commonly used:

```
Order Service
      |
      +----------------+
      |                |
      ↓                ↓
  Order DB         Outbox Table
      |                |
      +--- Same DB ----+
               |
               ↓
          Outbox Relay
               |
               ↓
             Kafka
```

This avoids trying to atomically commit an independent database and Kafka transaction using ordinary 2PC.

---

## 21. 2PC Interview Answer

**If an interviewer asks:**

### "What is Two-Phase Commit?"

A strong answer would be:

> Two-Phase Commit is a distributed transaction protocol used to ensure atomicity across multiple participants. It has a coordinator and multiple participants. In the first phase, called Prepare, the coordinator asks all participants whether they can commit. If every participant responds positively, the coordinator enters the second phase and sends Commit. If any participant fails or votes No, the coordinator sends Rollback. The major drawback is that 2PC is a blocking protocol and can suffer from coordinator failure, network failures, and resource locking, which is why Saga is often preferred for microservices.

---

## 22. Remember This Diagram

For system design interviews, remember:

```
                 COORDINATOR
                     |
              ┌──────┼──────┐
              ↓      ↓      ↓
             DB1    DB2    DB3
              |      |      |
           PREPARE PREPARE PREPARE
              |      |      |
             YES    YES    YES
              \      |      /
               \     |     /
                ↓    ↓    ↓
                 COMMIT
              /     |     \
             ↓      ↓      ↓
            DB1    DB2    DB3
```

The core rule:

```
              2PC

        PHASE 1
         PREPARE
            ↓
      Can everyone commit?
            ↓
       ┌────┴────┐
      YES        NO
       ↓          ↓
    PHASE 2    ROLLBACK
       ↓
    COMMIT
```

---

### Key Takeaway

> 2PC gives you atomic distributed commits, but the price is blocking, coordination overhead, and reduced availability under failures. In modern microservice architectures, **Saga + Outbox + idempotency** is often a more practical approach.
