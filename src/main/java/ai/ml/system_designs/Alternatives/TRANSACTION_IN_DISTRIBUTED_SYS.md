# Transaction in Distributed Systems

Managing a transaction in a distributed system is one of the most important and difficult System Design topics.

The key problem is this:

> In a monolith, multiple operations can often be handled by one database transaction. In a distributed system, those operations may belong to different services and different databases, so a normal ACID transaction cannot span them easily.

Let's understand this with a real-world Order + Payment + Inventory example.

---

## 1. The Problem

Imagine an e-commerce system:

```
Client
  |
  ↓
Order Service
  |
  ├────────→ Payment Service
  |
  └────────→ Inventory Service
```

A customer buys a ₹1,000 product.

We need to perform:

1. Create Order
2. Charge Payment ₹1,000
3. Reduce Inventory

Ideally, we want:

```
ALL SUCCESS
      OR
ALL ROLLBACK
```

In a monolithic application, this can potentially be:

```sql
BEGIN TRANSACTION

Create Order
Charge Payment
Reduce Inventory

COMMIT
```

If something fails:

```sql
ROLLBACK
```

But in a distributed system:

```
Order Service  → Order DB

Payment Service → Payment DB

Inventory Service → Inventory DB
```

Now there isn't one database transaction controlling everything.

---

## 2. Why Is It Difficult?

Suppose:

```
Order       ✅
Payment     ✅
Inventory   ❌
```

The customer has been charged, but the product wasn't reserved.

What do we do?

```
Customer
   |
   | ₹1,000
   ↓
Payment DB       ✅

Inventory DB     ❌
```

We need some mechanism to bring the system back to a valid state.

There are several approaches.

---

## 3. Main Approaches

The major approaches you should learn are:

```
Distributed Transactions
        |
        ├── 2-Phase Commit (2PC)
        |
        ├── Saga Pattern
        |
        │     ├── Choreography
        │     └── Orchestration
        |
        └── Eventual Consistency
```

In modern distributed systems, Saga + eventual consistency is very common for business workflows.

---

## 4. Approach 1 — Two-Phase Commit (2PC)

2PC stands for:

**Two-Phase Commit**

There is a coordinator and multiple participants.

Example:

```
                 Coordinator
                      |
          ┌───────────┼───────────┐
          ↓           ↓           ↓
      Order DB    Payment DB   Inventory DB
```

The coordinator manages the transaction.

### Phase 1 — Prepare

The coordinator asks everyone:

> "Can you commit?"

```
Coordinator
    |
    ├──→ Order DB       "Can you commit?"
    ├──→ Payment DB     "Can you commit?"
    └──→ Inventory DB   "Can you commit?"
```

Responses:

```
Order DB       → YES
Payment DB     → YES
Inventory DB   → YES
```

Now everyone is prepared.

### Phase 2 — Commit

Coordinator says:

```
COMMIT
```

```
Coordinator
    |
    ├──→ Order DB       COMMIT
    ├──→ Payment DB     COMMIT
    └──→ Inventory DB   COMMIT
```

Transaction succeeds.

---

## 5. What If One Fails?

Suppose:

```
Order DB       → YES
Payment DB     → YES
Inventory DB   → NO
```

Coordinator says:

```
ROLLBACK
```

```
Coordinator
    |
    ├──→ Order DB       ROLLBACK
    ├──→ Payment DB     ROLLBACK
    └──→ Inventory DB   ROLLBACK
```

Conceptually:

```
ALL COMMIT
     OR
ALL ROLLBACK
```

---

## 6. Problem With 2PC

2PC provides strong consistency, but it has significant drawbacks.

### Blocking

Participants may have to wait for the coordinator.

```
Coordinator
     |
     X
  CRASHED
```

Participants may not know whether they should commit or rollback.

### Performance

There are multiple network round trips:

```
Coordinator
    ↓
Prepare
    ↓
Participants
    ↓
Responses
    ↓
Commit
```

Network communication makes it slower.

### Availability

If the coordinator becomes unavailable, the transaction can get stuck.

Therefore:

> 2PC is generally expensive and can be a poor fit for large, highly available distributed systems.

---

## 7. Approach 2 — Saga Pattern

The Saga Pattern is extremely important for System Design interviews.

Instead of trying to create one giant distributed transaction, we break it into a sequence of local transactions.

For example:

```
Create Order
     ↓
Process Payment
     ↓
Reserve Inventory
     ↓
Complete Order
```

Each service performs its own local transaction.

If something fails, we execute a **compensating transaction** to undo the business effect of previous steps.

---

## 8. Saga Example

Let's say:

```
Order = ₹1,000
```

Workflow:

```
Step 1
Create Order
     ↓
SUCCESS

Step 2
Payment
     ↓
SUCCESS

Step 3
Inventory
     ↓
FAILURE
```

We can't simply perform a database rollback across all services.

Instead, we compensate.

```
Inventory failed
       ↓
Refund Payment
       ↓
Cancel Order
```

So:

```
Create Order
     ↓
Payment
     ↓
Inventory ❌
     ↓
Refund Payment
     ↓
Cancel Order
```

These are called **compensating transactions**.

---

## 9. Saga Orchestration

One way to implement Saga is orchestration.

A central Saga Orchestrator controls the workflow:

```
                 Saga Orchestrator
                        |
          ┌─────────────┼─────────────┐
          ↓             ↓             ↓
    Order Service   Payment       Inventory
                     Service        Service
```

The orchestrator says:

1. Order Service → Create Order
2. Payment → Charge ₹1,000
3. Inventory → Reserve Product

If Inventory fails:

```
Inventory → FAILED
     ↓
Orchestrator
     ↓
Payment → Refund
     ↓
Order → Cancel
```

Diagram:

```
                 Orchestrator
                      |
                      ↓
                Create Order
                      |
                      ↓
                Process Payment
                      |
                      ↓
              Reserve Inventory
                      |
                      X
                   FAILURE
                      |
              ┌───────┴────────┐
              ↓                ↓
         Refund Payment    Cancel Order
```

This is easy to understand because the workflow is centralized.

---

## 10. Saga Choreography

Another approach is choreography.

There is no central orchestrator.

Services communicate using events.

For example:

```
Order Service
     |
     | OrderCreated
     ↓
Message Broker
     |
     ↓
Payment Service
     |
     | PaymentCompleted
     ↓
Message Broker
     |
     ↓
Inventory Service
```

For example:

```
OrderCreated
     ↓
Payment Service
     ↓
PaymentCompleted
     ↓
Inventory Service
     ↓
InventoryReserved
     ↓
OrderCompleted
```

If payment fails:

```
PaymentFailed
     ↓
Order Service
     ↓
Cancel Order
```

---

## 11. Orchestration vs Choreography

| Orchestration | Choreography |
|---|---|
| Central orchestrator | No central controller |
| Workflow is explicit | Workflow emerges from events |
| Easier to understand | Can become harder to trace |
| Easier to manage complex workflows | Good for loosely coupled event-driven systems |
| Orchestrator can become complex | Event chains can become complex |

---

## 12. What Is Eventual Consistency?

This is closely related.

In a distributed system, different services may temporarily have different states.

For example:

```
Order Service
Order = CONFIRMED

Payment Service
Payment = SUCCESS

Inventory Service
Inventory = PROCESSING
```

For a short period, the system isn't perfectly synchronized.

Eventually:

```
Order = CONFIRMED
Payment = SUCCESS
Inventory = RESERVED
```

This is **eventual consistency**.

The system guarantees that, assuming no further failures and successful processing, the different replicas/services will eventually converge to a consistent state.

---

## 13. Important Real-Time Example — Payment

Payment is where this becomes especially important.

Suppose:

```
Client
  |
  ↓
Order Service
  |
  ↓
Payment Service
```

Client requests:

```
Pay ₹1,000
```

Payment Service processes the payment:

```
₹1,000 deducted
```

But the response is lost:

```
Payment Service
      |
      | SUCCESS
      X
      |
   Network
      |
      ↓
Order Service
```

Order Service doesn't know whether payment succeeded.

If it blindly retries:

```
Retry Payment
     ↓
₹1,000 deducted AGAIN ❌
```

Potentially ₹2,000 could be charged.

This is why distributed transactions often require **idempotency**.

---

## 14. Idempotency

An operation is idempotent if performing it multiple times has the same business effect as performing it once.

For example:

```
paymentRequestId = ABC123
```

First request:

```
ABC123 → ₹1,000 → SUCCESS
```

Retry:

```
ABC123 → already processed
```

The payment service returns the previous result instead of charging again.

Conceptually:

```
Request
   |
   ↓
Idempotency Key
   |
   ↓
Payment Service
   |
   ├── Already processed?
   │       |
   │       └── YES → Return previous result
   │
   └── NO → Process payment
```

This is extremely important in payment systems.

---

## 15. How a Modern E-Commerce Transaction Might Work

A practical architecture could be:

```
                         Client
                           |
                           ↓
                     Order Service
                           |
                           ↓
                      Create Order
                           |
                           ↓
                    Publish Event
                           |
                           ↓
                    Message Broker
                    /             \
                   /               \
                  ↓                 ↓
          Payment Service     Inventory Service
                  |                 |
                  ↓                 ↓
            Payment DB        Inventory DB
                  |
                  ↓
          PaymentCompleted
                  |
                  ↓
            Message Broker
                  |
                  ↓
             Order Service
                  |
                  ↓
             Order Confirmed
```

This doesn't require one giant ACID transaction across all services.

Instead, each service owns its local transaction and communicates using events.

---

## 16. What Happens If Inventory Fails?

Suppose:

```
Order Created       ✅
Payment Successful  ✅
Inventory Failed    ❌
```

Then:

```
InventoryFailed
       ↓
Order Service
       ↓
Start compensation
       ↓
Refund Payment
       ↓
Cancel Order
```

So the final state becomes:

```
Order      → CANCELLED
Payment    → REFUNDED
Inventory  → NOT RESERVED
```

The system has reached a valid business state through compensation.

---

## 17. The Key Difference: Rollback vs Compensation

This is extremely important.

### Traditional Transaction

```sql
BEGIN

Operation 1
Operation 2
Operation 3

ROLLBACK
```

The database reverses the transaction.

### Distributed Saga

You can't necessarily do:

```
ROLLBACK EVERYTHING
```

Instead:

```
Operation 1
     ↓
Operation 2
     ↓
Operation 3 ❌
     ↓
Compensating Operation 2
     ↓
Compensating Operation 1
```

Example:

```
Charge Payment
     ↓
Inventory fails
     ↓
Refund Payment
```

Refund is not a database rollback.

It is a **business-level compensation**.

---

## 18. Which Approach Should You Use?

A useful decision framework:

### Simple Monolithic Application

Use:

```
Database Transaction
```

Example:

```sql
BEGIN
  Create Order
  Update Inventory
  Create Payment Record
COMMIT
```

### Distributed System Requiring Strong Coordination

Consider:

```
2PC
```

but understand its performance and availability trade-offs.

### Large Microservices / Event-Driven System

Often use:

```
Saga
+
Eventual Consistency
+
Idempotency
+
Message Broker
```

For example:

```
Order
  ↓
Payment
  ↓
Inventory
  ↓
Notification
```

with compensating actions when necessary.

---

## 19. The Big Picture

You can remember distributed transactions like this:

```
                 DISTRIBUTED TRANSACTION
                         |
          ┌──────────────┴──────────────┐
          |                             |
       2-Phase Commit                 Saga
          |                             |
     Stronger consistency        Local transactions
          |                             |
     Coordinator                 Compensation
          |                             |
      More blocking            Eventual consistency
      More overhead                  |
                              ┌───────┴────────┐
                              ↓                ↓
                        Orchestration    Choreography
```

And in real-world distributed systems, you'll frequently see:

```
Saga
  +
Eventual Consistency
  +
Message Queue/Kafka
  +
Idempotency
  +
Retries
  +
Timeouts
  +
Dead Letter Queue
```

---

## Most Important Interview Takeaway

If an interviewer asks:

> "How do you handle transactions across multiple microservices?"

A strong answer is:

> "We generally cannot use a normal local database transaction across multiple services. We can use a distributed transaction protocol such as 2PC when strong consistency is required and the environment supports it, but for many large-scale systems we prefer the Saga pattern. Each service performs its own local transaction, and failures are handled through compensating transactions. The workflow can be coordinated using orchestration or implemented through event-driven choreography. We also need idempotency, retries, timeouts, and eventual consistency to handle network failures and duplicate messages safely."

That is the foundation you need before moving into Saga Pattern in depth, especially Orchestration vs Choreography with a complete Order → Payment → Inventory example.
