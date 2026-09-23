# Saga Orchestration Pattern

Saga Orchestration is a pattern used to manage a business transaction that spans multiple microservices.

The basic idea is:

> Instead of trying to use one database transaction across multiple services, we have a central component called the Saga Orchestrator that tells each service what to do and tells them what compensating action to perform if something fails.

---

## 1. Why Do We Need Saga Orchestration?

Consider an e-commerce system.

A customer places an order:

```
₹1,000 Product
```

Several services are involved:

- Order Service
- Payment Service
- Inventory Service
- Notification Service

The workflow is:

```
Create Order
     ↓
Reserve Inventory
     ↓
Process Payment
     ↓
Confirm Order
     ↓
Send Notification
```

These services may have their own databases:

```
Order Service       → Order DB
Payment Service     → Payment DB
Inventory Service   → Inventory DB
```

We can't easily do:

```sql
BEGIN TRANSACTION

Order DB
Payment DB
Inventory DB

COMMIT
```

as one normal local database transaction.

That's where Saga comes in.

---

## 2. What Is a Saga?

A Saga breaks one large business transaction into multiple local transactions.

For example:

```
                 Saga
                  |
      ┌───────────┼───────────┐
      ↓           ↓           ↓
 Create Order   Payment   Reserve Inventory
```

Each service performs its own transaction.

If everything succeeds:

```
Order        ✅
Inventory    ✅
Payment      ✅
```

If something fails, we execute **compensating transactions**.

---

## 3. What Is Saga Orchestration?

In orchestration, a central component called the:

**Saga Orchestrator**

controls the workflow.

```
                  Saga Orchestrator
                         |
          ┌──────────────┼──────────────┐
          ↓              ↓              ↓
    Order Service   Inventory Service  Payment Service
```

The orchestrator says:

```
"Create the order."
        ↓
"Reserve inventory."
        ↓
"Process payment."
        ↓
"Confirm order."
```

The services don't need to know the entire workflow.

---

## 4. Real-Time E-Commerce Example

Let's take Amazon-like order placement.

Customer wants:

```
iPhone
₹80,000
```

The flow could be:

```
Client
  |
  ↓
Order Service
  |
  ↓
Saga Orchestrator
  |
  ├──→ Inventory Service
  |       |
  |       ↓
  |    Reserve Item
  |       |
  |       ↓
  |      SUCCESS
  |
  ├──→ Payment Service
  |       |
  |       ↓
  |    Charge ₹80,000
  |       |
  |       ↓
  |      SUCCESS
  |
  └──→ Order Service
          |
          ↓
       CONFIRM
```

Final state:

```
Order      → CONFIRMED
Inventory  → RESERVED
Payment    → SUCCESS
```

---

## 5. Let's Understand Every Step

### Step 1 — Customer Places Order

Client sends:

```
POST /orders
{
  "productId": "IPHONE-17",
  "quantity": 1,
  "amount": 80000
}
```

Order Service creates:

```
Order ID = ORD-1001
Status   = PENDING
```

Notice:

We don't immediately say CONFIRMED.

It's:

```
PENDING
```

because the Saga hasn't completed.

---

## 6. Step 2 — Orchestrator Starts

The Order Service starts a Saga:

```
Saga ID = SAGA-5001
Order ID = ORD-1001
```

The orchestrator now controls the workflow.

```
Saga Orchestrator
       |
       ↓
"Reserve inventory for ORD-1001"
```

---

## 7. Step 3 — Inventory Service

Inventory Service receives:

```
Reserve:
Product = IPHONE-17
Quantity = 1
Order = ORD-1001
```

It checks:

```
Stock = 10
```

Then:

```
Stock = 9
Reserved = 1
```

and returns:

```
RESERVED
```

The orchestrator receives:

```
Inventory → SUCCESS
```

---

## 8. Step 4 — Payment

The orchestrator now says:

```
Process payment
Amount = ₹80,000
Order = ORD-1001
```

Payment Service:

```
Payment
   ↓
₹80,000
   ↓
SUCCESS
```

The orchestrator receives:

```
Payment → SUCCESS
```

---

## 9. Step 5 — Confirm Order

Now everything has succeeded:

```
Inventory → SUCCESS
Payment   → SUCCESS
```

So the orchestrator tells Order Service:

```
Confirm Order ORD-1001
```

Order becomes:

```
CONFIRMED
```

Final state:

```
Order       → CONFIRMED
Inventory   → RESERVED
Payment     → SUCCESS
```

---

## 10. What If Payment Fails?

This is where Saga becomes really useful.

Suppose:

```
Order       → SUCCESS
Inventory   → SUCCESS
Payment     → FAILED
```

We now have a problem.

The inventory has already been reserved.

We need to compensate.

The orchestrator knows the workflow:

```
Create Order
      ↓
Reserve Inventory
      ↓
Payment ❌
```

So it sends:

```
Release Inventory
```

to Inventory Service.

---

## 11. Compensation

The workflow becomes:

```
Reserve Inventory
       ↓
Payment FAILED
       ↓
Release Inventory
       ↓
Cancel Order
```

So:

```
             Saga Orchestrator
                    |
                    ↓
              Payment FAILED
                    |
          ┌─────────┴─────────┐
          ↓                   ↓
 Release Inventory        Cancel Order
```

Final state:

```
Order       → CANCELLED
Inventory   → AVAILABLE
Payment     → FAILED
```

Notice something very important:

> We didn't rollback the database transactions.
>
> Instead, we performed compensating business operations.

---

## 12. Compensation Is NOT Rollback

This distinction is extremely important in interviews.

### Traditional Database

```sql
BEGIN
   Operation 1
   Operation 2
   Operation 3
ROLLBACK
```

### Saga

```
Operation 1
    ↓
Operation 2
    ↓
Operation 3 ❌
    ↓
Compensate Operation 2
    ↓
Compensate Operation 1
```

For example:

```
Reserve Inventory
       ↓
Payment Failed
       ↓
Release Inventory
```

`ReleaseInventory()` isn't a database rollback.

It's a **new business transaction**.

---

## 13. Another Failure: Inventory Fails

Suppose:

```
Create Order       ✅
Reserve Inventory  ❌
```

Then the orchestrator doesn't need to compensate payment because payment hasn't happened yet.

It can simply:

```
Inventory Failed
      ↓
Cancel Order
```

Final:

```
Order → CANCELLED
Inventory → NOT RESERVED
Payment → NOT PROCESSED
```

---

## 14. Another Failure: Payment Succeeds but Confirmation Fails

Consider:

```
Create Order       ✅
Reserve Inventory  ✅
Payment            ✅
Confirm Order      ❌
```

Now:

```
₹80,000 charged
Inventory reserved
Order not confirmed
```

The orchestrator needs to decide what compensation is appropriate.

For example:

```
Confirm Order failed
       ↓
Refund Payment
       ↓
Release Inventory
       ↓
Cancel Order
```

Final:

```
Order       → CANCELLED
Payment     → REFUNDED
Inventory   → AVAILABLE
```

This is why designing compensation correctly is one of the hardest parts of Saga.

---

## 15. Complete Flow

Here's the whole workflow:

```
                         Client
                           |
                           ↓
                     Order Service
                           |
                           ↓
                  Saga Orchestrator
                           |
                           ↓
                 Create Order/PENDING
                           |
                           ↓
                 Reserve Inventory
                           |
                      SUCCESS?
                    /           \
                  YES            NO
                   |              |
                   ↓              ↓
              Process Payment   Cancel Order
                   |
              SUCCESS?
             /         \
           YES          NO
            |            |
            ↓            ↓
       Confirm Order  Release Inventory
            |            |
            ↓            ↓
          SUCCESS     Cancel Order
```

---

## 16. Why Is the Orchestrator Useful?

Without orchestration, every service might need to know about other services.

For example:

```
Order Service
    |
    ├── Payment Service
    |
    ├── Inventory Service
    |
    └── Notification Service
```

And then Payment Service might need to know:

```
Payment
   ↓
Inventory?
   ↓
Order?
```

This can become difficult to manage.

With an orchestrator:

```
                  Orchestrator
                 /      |      \
                /       |       \
               ↓        ↓        ↓
            Order   Inventory  Payment
```

The workflow is centralized.

---

## 17. Saga Orchestration vs Saga Choreography

This is an important System Design interview question.

### Orchestration

Central controller:

```
                 Orchestrator
                  /    |    \
                 ↓     ↓     ↓
              Order Payment Inventory
```

Orchestrator says:

```
Do this
 ↓
Do this
 ↓
Do this
```

### Choreography

No central controller.

Services communicate using events:

```
Order Service
      |
      | OrderCreated
      ↓
    Kafka
      |
      ↓
Payment Service
      |
      | PaymentCompleted
      ↓
    Kafka
      |
      ↓
Inventory Service
```

Each service reacts to events.

---

## 18. Orchestration vs Choreography

| | Orchestration | Choreography |
|---|---|---|
| Controller | Central orchestrator | No central controller |
| Workflow | Explicit | Event-driven |
| Complexity | Orchestrator can become complex | Event relationships can become complex |
| Debugging | Generally easier | Can be harder |
| Service coupling | Services depend on orchestrator commands | Services depend on events |
| Best for | Complex workflows | Simpler event-driven flows |

---

## 19. Real-Time Example: Food Delivery

Let's take another example.

You order food.

```
Order
₹500
```

Services:

- Order Service
- Restaurant Service
- Payment Service
- Delivery Service

Saga:

```
                    Orchestrator
                         |
                         ↓
                  Create Order
                         |
                         ↓
                Restaurant Accept?
                    /        \
                  YES         NO
                   |           |
                   ↓           ↓
             Process Payment Cancel Order
                   |
                   ↓
             Assign Delivery
                   |
                   ↓
             Confirm Order
```

Suppose restaurant accepts:

```
Restaurant → ACCEPTED
```

Payment succeeds:

```
Payment → SUCCESS
```

But delivery service cannot find a delivery partner:

```
Delivery → FAILED
```

The orchestrator might initiate:

```
Refund Payment
      ↓
Cancel Restaurant Order
      ↓
Cancel Customer Order
```

Again, these are **compensating transactions**.

---

## 20. Saga State

In a production system, the orchestrator usually needs to maintain Saga state.

For example:

```
Saga ID: SAGA-5001

Step 1: Create Order       → SUCCESS
Step 2: Reserve Inventory  → SUCCESS
Step 3: Payment            → SUCCESS
Step 4: Confirm Order      → PENDING
```

If the orchestrator crashes, it needs to recover.

So Saga state might be persisted:

```
┌──────────────────────────────────────┐
│ Saga State                           │
├──────────┬───────────────────────────┤
│ Saga ID  │ SAGA-5001                 │
│ Order ID │ ORD-1001                  │
│ Step     │ PAYMENT                   │
│ Status   │ SUCCESS                   │
└──────────┴───────────────────────────┘
```

---

## 21. What If the Orchestrator Crashes?

Suppose:

```
Orchestrator
     |
     ↓
Payment SUCCESS
     |
     X
  CRASH
```

The system shouldn't lose the workflow.

When the orchestrator restarts:

```
Orchestrator
     |
     ↓
Read Saga State
     |
     ↓
Payment already SUCCESS
     |
     ↓
Continue next step
```

This is why **durable Saga state** is important.

---

## 22. Idempotency Is Very Important Here

This connects directly to your previous question.

Suppose the orchestrator sends:

```
ReserveInventory(SAGA-5001)
```

But it doesn't receive the response.

It retries:

```
ReserveInventory(SAGA-5001)
```

Again:

```
ReserveInventory(SAGA-5001)
```

The Inventory Service might receive the same command twice.

Without idempotency:

```
Stock = 10

Request 1 → Reserve 1 → Stock = 9
Request 2 → Reserve 1 → Stock = 8 ❌
```

With idempotency:

```
Request 1 → SAGA-5001 → Reserve 1
Request 2 → SAGA-5001 → Already processed
```

Therefore:

> Saga + Retry requires idempotent operations.

---

## 23. Saga + Retry + Idempotency

A production Saga often looks like:

```
                Saga Orchestrator
                       |
                       | Command
                       ↓
                Payment Service
                       |
                  Timeout?
                     /   \
                   NO     YES
                   |       |
                   ↓       ↓
                Success   Retry
                            |
                            ↓
                    Same operation ID
                            |
                            ↓
                       Idempotency
```

This prevents duplicate business operations.

---

## 24. Saga + Message Broker

Orchestration can be implemented using synchronous calls or messaging.

A common design:

```
                    Saga Orchestrator
                           |
                           ↓
                    Message Broker
                    /      |       \
                   ↓       ↓        ↓
              Inventory  Payment   Order
```

Commands:

- ReserveInventory
- ProcessPayment
- ConfirmOrder

Events:

- InventoryReserved
- PaymentCompleted
- PaymentFailed
- OrderConfirmed

This provides more decoupling and resilience, but adds messaging complexity.

---

## 25. When Should You Use Saga Orchestration?

Use it when:

### 1. One business operation spans multiple services

```
Order
 ↓
Payment
 ↓
Inventory
```

### 2. You have separate databases

```
Order DB
Payment DB
Inventory DB
```

and need a coordinated business workflow.

### 3. Eventual consistency is acceptable

You may temporarily have:

```
Order = PENDING
Payment = SUCCESS
Inventory = PROCESSING
```

and eventually reach:

```
Order = CONFIRMED
Payment = SUCCESS
Inventory = RESERVED
```

### 4. The workflow is complex

For example:

```
Order
 ↓
Inventory
 ↓
Payment
 ↓
Shipping
 ↓
Notification
```

An orchestrator can make the workflow easier to understand and control.

---

## 26. When Should You NOT Use Saga?

Don't automatically use Saga for everything.

If everything is in one service and one database:

```
Order Service
      |
      ↓
   Order DB
```

just use a normal database transaction where appropriate.

```sql
BEGIN
   UPDATE order
   UPDATE inventory
COMMIT
```

A Saga would introduce unnecessary complexity.

---

## 27. Interview Answer

If the interviewer asks:

> "What is Saga Orchestration?"

You can answer:

> "Saga Orchestration is a distributed transaction pattern where a central Saga Orchestrator coordinates a sequence of local transactions across multiple services. Each service commits its own transaction independently. If a later step fails, the orchestrator invokes compensating transactions for previously completed steps. For example, in an e-commerce system, the orchestrator can create an order, reserve inventory, process payment, and confirm the order. If payment fails after inventory was reserved, the orchestrator sends a release-inventory command and cancels the order. Since retries and duplicate messages are possible, each operation should be designed to be idempotent."

---

## 28. The Complete Mental Model

You should remember this diagram:

```
                         CLIENT
                           |
                           ↓
                     ORDER SERVICE
                           |
                           ↓
                  ┌─────────────────┐
                  │      SAGA       │
                  │   ORCHESTRATOR  │
                  └────────┬────────┘
                           |
              ┌────────────┼────────────┐
              ↓            ↓            ↓
         INVENTORY      PAYMENT       ORDER
          SERVICE       SERVICE      SERVICE
              |            |            |
              ↓            ↓            ↓
          Inventory     Payment       Order
             DB            DB           DB
```

### SUCCESS:

```
Create Order
     ↓
Reserve Inventory ✅
     ↓
Process Payment ✅
     ↓
Confirm Order ✅
```

### FAILURE:

```
Create Order
     ↓
Reserve Inventory ✅
     ↓
Payment ❌
     ↓
Release Inventory ← Compensation
     ↓
Cancel Order      ← Compensation
```

---

## The 5 Concepts You Should Connect Together

```
Distributed Transaction
        ↓
      Saga
        ↓
 Orchestration
        ↓
Compensating Transactions
        ↓
Idempotency + Retry
```

Once you understand this flow, the next important System Design topic is Saga Choreography vs Saga Orchestration, because that's where you'll understand Kafka/events, loose coupling, failure handling, retries, and eventual consistency together.
