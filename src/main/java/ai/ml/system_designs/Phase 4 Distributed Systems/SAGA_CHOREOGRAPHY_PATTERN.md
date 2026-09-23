# Saga Choreography Pattern

Saga Choreography is a way to manage a distributed transaction across multiple microservices without having a central coordinator.

The key idea is:

> Each service performs its local transaction and publishes an event. Other services listen to those events and decide what action to perform next.

So instead of:

```
                    Orchestrator
                   /     |      \
                  ↓      ↓       ↓
             Order   Payment  Inventory
```

we have:

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

There is no central controller telling everyone what to do.

---

## 1. Real-Time Example — E-Commerce Order

Imagine you are ordering an iPhone for ₹80,000.

We have:

- Order Service
- Payment Service
- Inventory Service
- Shipping Service
- Notification Service

Each service has its own database:

```
Order Service       → Order DB
Payment Service     → Payment DB
Inventory Service   → Inventory DB
Shipping Service    → Shipping DB
```

The business transaction is:

```
Place Order
    ↓
Reserve Inventory
    ↓
Process Payment
    ↓
Create Shipment
    ↓
Notify Customer
```

The challenge is that these are different services and different databases.

We can't simply do:

```sql
BEGIN TRANSACTION

Order DB
Payment DB
Inventory DB
Shipping DB

COMMIT
```

Instead, we can use a Saga.

---

## 2. Saga Choreography

With choreography, there is no Saga Orchestrator.

Each service reacts to events.

```
                  Kafka / Message Broker

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
     |
     | InventoryReserved
     ↓
   Kafka
     |
     ↓
Shipping Service
     |
     | ShipmentCreated
     ↓
   Kafka
     |
     ↓
Notification Service
```

Each service says:

> "I received an event. Based on that event, I'll perform my local transaction and publish another event."

---

## 3. Step-by-Step Example

### Step 1 — Customer Creates Order

Client sends:

```
POST /orders
{
  "productId": "IPHONE-17",
  "quantity": 1,
  "amount": 80000
}
```

Order Service performs its local transaction:

```
Order DB

Order ID = ORD-1001
Status   = PENDING
```

Then publishes:

```
OrderCreated
```

to Kafka/message broker.

```
Order Service
      |
      | OrderCreated
      ↓
    Kafka
```

Order Service doesn't tell Payment Service directly:

```
❌ Order Service → Payment Service
```

Instead:

```
✅ Order Service → Kafka → Payment Service
```

---

## 4. Step 2 — Payment Service Listens

Payment Service subscribes to:

```
OrderCreated
```

When it receives:

```
OrderCreated
```

it processes payment:

```
₹80,000
   ↓
Payment Gateway
   ↓
SUCCESS
```

Payment DB:

```
Payment ID = PAY-5001
Order ID   = ORD-1001
Status     = SUCCESS
```

Then it publishes:

```
PaymentCompleted
```

```
Payment Service
      |
      | PaymentCompleted
      ↓
    Kafka
```

---

## 5. Step 3 — Inventory Service Listens

Inventory Service is subscribed to:

```
PaymentCompleted
```

It receives:

```
PaymentCompleted
Order = ORD-1001
```

It checks stock:

```
iPhone stock = 10
```

It reserves one:

```
Stock = 9
Reserved = 1
```

Then publishes:

```
InventoryReserved
```

```
Inventory Service
       |
       | InventoryReserved
       ↓
     Kafka
```

---

## 6. Step 4 — Shipping Service Listens

Shipping Service listens for:

```
InventoryReserved
```

It creates shipment:

```
Shipment ID = SHIP-9001
Order       = ORD-1001
Status      = CREATED
```

Then publishes:

```
ShipmentCreated
```

---

## 7. Step 5 — Notification Service

Notification Service listens for:

```
ShipmentCreated
```

and sends:

- Email
- SMS
- Push Notification

Customer receives:

```
"Your order ORD-1001 has been confirmed and shipped."
```

---

## 8. Complete Successful Flow

```
                         Kafka
                           |
                           ↓
                    OrderCreated
                           |
                           ↓
                    Payment Service
                           |
                           ↓
                  PaymentCompleted
                           |
                           ↓
                  Inventory Service
                           |
                           ↓
                  InventoryReserved
                           |
                           ↓
                   Shipping Service
                           |
                           ↓
                   ShipmentCreated
                           |
                           ↓
                 Notification Service
```

Notice:

> No service is controlling the entire workflow.
>
> The events themselves drive the workflow.

That's why it's called **Choreography**.

Think of dancers in a performance: there isn't necessarily one person standing in the middle giving every dancer an instruction; each participant knows what to do when the music/event reaches the appropriate point.

---

## 9. What Happens If Payment Fails?

Now let's look at the important part: compensation.

Suppose:

```
OrderCreated       ✅
Payment            ❌
```

Payment Service publishes:

```
PaymentFailed
```

```
Payment Service
      |
      | PaymentFailed
      ↓
    Kafka
```

Order Service listens to:

```
PaymentFailed
```

and changes:

```
Order
PENDING
  ↓
CANCELLED
```

So:

```
OrderCreated
     ↓
PaymentFailed
     ↓
OrderCancelled
```

---

## 10. What If Inventory Fails?

Suppose:

```
OrderCreated        ✅
PaymentCompleted    ✅
InventoryReserved   ❌
```

Maybe there is no stock.

Inventory Service publishes:

```
InventoryReservationFailed
```

Now Payment Service listens for this event.

It performs compensation:

```
Refund ₹80,000
```

Then publishes:

```
PaymentRefunded
```

Order Service listens for:

```
PaymentRefunded
```

and changes:

```
Order
PENDING
  ↓
CANCELLED
```

So the compensation flow is:

```
OrderCreated
      ↓
PaymentCompleted
      ↓
InventoryReservationFailed
      ↓
RefundPayment
      ↓
PaymentRefunded
      ↓
OrderCancelled
```

---

## 11. Very Important: Compensation Is Not Rollback

Suppose:

```
PaymentCompleted
```

means:

```
₹80,000 charged
```

Then inventory fails.

We don't magically rollback the payment database transaction.

Instead, we execute a new business operation:

```
RefundPayment
```

So:

```
Original transaction:
Charge ₹80,000

Compensating transaction:
Refund ₹80,000
```

This is a key concept of Saga.

---

## 12. Real-Time Example — Food Delivery

Let's take another practical example.

Suppose you order food.

Services:

- Customer Service
- Order Service
- Restaurant Service
- Payment Service
- Delivery Service

The flow:

```
Customer
   |
   ↓
Order Service
   |
   | OrderCreated
   ↓
Kafka
   |
   ↓
Restaurant Service
```

Restaurant accepts:

```
RestaurantAccepted
```

Then:

```
Kafka
  ↓
Payment Service
```

Payment succeeds:

```
PaymentCompleted
```

Then:

```
Kafka
  ↓
Delivery Service
```

Delivery service assigns a driver:

```
DeliveryAssigned
```

Finally:

```
Kafka
  ↓
Notification Service
```

Customer receives:

```
"Your food is being prepared."
```

---

## 13. What If Restaurant Rejects the Order?

Restaurant receives:

```
OrderCreated
```

but says:

```
RestaurantRejected
```

Then:

```
Restaurant
     |
     | RestaurantRejected
     ↓
   Kafka
```

Payment Service receives it and performs:

```
RefundPayment
```

Order Service receives the relevant failure/refund event and marks:

```
Order → CANCELLED
```

So:

```
OrderCreated
     ↓
RestaurantRejected
     ↓
RefundPayment
     ↓
PaymentRefunded
     ↓
OrderCancelled
```

Again, no orchestrator is telling these services what to do.

They simply react to events.

---

## 14. Why Is It Called Choreography?

Think about a dance performance.

There isn't necessarily a central controller saying:

```
Dancer 1 → move
Dancer 2 → move
Dancer 3 → move
```

Instead:

```
Event
  ↓
Participant reacts
  ↓
New event
  ↓
Another participant reacts
```

In microservices:

```
OrderCreated
     ↓
Payment reacts
     ↓
PaymentCompleted
     ↓
Inventory reacts
     ↓
InventoryReserved
     ↓
Shipping reacts
```

Hence:

> Event-driven collaboration between services.

---

## 15. Saga Choreography vs Orchestration

This is one of the most important System Design interview questions.

### Orchestration

There is a central coordinator:

```
                   Orchestrator
                 /      |       \
                ↓       ↓        ↓
             Order   Payment  Inventory
```

The orchestrator says:

```
1. Do this
2. Do this
3. Do this
```

### Choreography

There is no coordinator:

```
Order
  |
  | OrderCreated
  ↓
Kafka
  |
  ↓
Payment
  |
  | PaymentCompleted
  ↓
Kafka
  |
  ↓
Inventory
```

Each service reacts to events.

---

## 16. Side-by-Side Comparison

| | Saga Orchestration | Saga Choreography |
|---|---|---|
| Central coordinator | ✅ Yes | ❌ No |
| Communication | Commands | Events |
| Workflow | Centralized | Distributed |
| Coupling | Services coupled to orchestrator | Services coupled to events |
| Simple workflows | Good | Excellent |
| Complex workflows | Often easier | Can become difficult |
| Debugging | Generally easier | More difficult |
| Failure handling | Centralized | Distributed |
| Number of services | Works well with many | Can become difficult with many |
| Risk | Orchestrator complexity | Event/dependency complexity |

---

## 17. Example Architecture

### Orchestration

```
                         ┌───────────────┐
                         │ Orchestrator  │
                         └───────┬───────┘
                                 |
                  ┌──────────────┼──────────────┐
                  ↓              ↓              ↓
               Order         Payment        Inventory
               Service       Service         Service
```

The orchestrator controls everything.

### Choreography

```
                 ┌────────────────────┐
                 │   Kafka / Broker    │
                 └────────────────────┘
                    ↑      ↑       ↑
                    |      |       |
                    ↓      ↓       ↓
                  Order  Payment  Inventory
                 Service Service  Service
```

```
Order
  |
  | OrderCreated
  ↓
Kafka
  |
  ↓
Payment
  |
  | PaymentCompleted
  ↓
Kafka
  |
  ↓
Inventory
```

The events drive the workflow.

---

## 18. The Biggest Problem With Choreography

Choreography sounds great:

- No central coordinator
- Loose coupling
- Event driven

But imagine 20 services.

```
OrderCreated
      ↓
Payment
      ↓
Inventory
      ↓
Shipping
      ↓
Tax
      ↓
Fraud
      ↓
Notification
      ↓
Analytics
      ↓
...
```

Eventually it can become difficult to understand:

> "Who is listening to which event?"

You can end up with a distributed workflow that's difficult to visualize.

This is sometimes called **event spaghetti**.

---

## 19. Debugging Problem

With orchestration:

```
Saga ID = SAGA-1001

1. Create Order       ✅
2. Reserve Inventory  ✅
3. Payment            ❌
4. Refund             ✅
5. Cancel Order       ✅
```

It's relatively easy to see the workflow.

With choreography:

```
OrderCreated
    ↓
Payment
    ↓
PaymentCompleted
    ↓
Inventory
    ↓
InventoryFailed
    ↓
PaymentRefundRequested
    ↓
PaymentRefunded
    ↓
OrderCancelled
```

You need good:

- Correlation ID
- Trace ID
- Saga ID
- Event IDs

to trace the complete business transaction.

---

## 20. Idempotency Is Extremely Important

This connects directly with what you asked earlier about idempotency and retries.

Suppose Kafka delivers:

```
PaymentCompleted
```

twice.

```
PaymentCompleted
PaymentCompleted
```

Inventory Service might receive both.

Without idempotency:

```
First event:
Reserve 1 iPhone

Second event:
Reserve another iPhone ❌
```

With idempotency:

```
Event ID = EVT-123

First:
EVT-123 → Process

Second:
EVT-123 → Already processed
```

Therefore:

> Saga Choreography + Messaging requires strong idempotency handling.

---

## 21. What If a Service Is Down?

Suppose:

```
Order Service
      ↓
OrderCreated
      ↓
Kafka
      ↓
Payment Service ❌ DOWN
```

The event can remain in the broker depending on the messaging system and configuration.

When Payment Service comes back:

```
Payment Service
      ↓
Consume OrderCreated
      ↓
Process Payment
```

This is one of the major advantages of asynchronous event-driven architecture.

> The producer doesn't have to wait for the consumer to be immediately available.

---

## 22. When Should We Use Saga Choreography?

Use it when:

### 1. Workflow is relatively simple

For example:

```
OrderCreated
   ↓
Payment
   ↓
Inventory
```

### 2. You already have an event-driven architecture

For example:

```
Kafka
  ↓
Many services
```

### 3. Services should be loosely coupled

You don't want:

```
Order Service
     ↓
Payment Service
```

direct synchronous dependency.

Instead:

```
Order Service
     ↓
OrderCreated
     ↓
Kafka
```

### 4. Eventual consistency is acceptable

The system may temporarily have:

```
Order       = PENDING
Payment     = PROCESSING
Inventory   = PROCESSING
```

and eventually:

```
Order       = CONFIRMED
Payment     = SUCCESS
Inventory   = RESERVED
```

---

## 23. When Should You Avoid Choreography?

If you have a very complex business workflow:

```
A
 ↓
B
 ↓
C
 ├──→ D
 │     ↓
 │     E
 ↓
F
 ├──→ G
 └──→ H
```

and many compensation rules, choreography can become difficult.

In that situation, Saga Orchestration can provide a clearer central workflow.

---

## 24. Interview Answer

If an interviewer asks:

> "What is Saga Choreography?"

A strong answer is:

> "Saga Choreography is a distributed transaction pattern where there is no central coordinator. Each microservice performs its own local transaction, publishes an event after success or failure, and other services react to those events. For example, in an e-commerce system, Order Service publishes OrderCreated, Payment Service consumes it and publishes PaymentCompleted, Inventory Service consumes that event and publishes InventoryReserved, and so on. If a step fails, the relevant services perform compensating transactions based on failure events. Since the communication is usually asynchronous, retries and duplicate events are expected, so consumers should be idempotent and events should have correlation or Saga IDs for tracing."

---

## 25. The Complete Mental Model

Remember this:

```
              SAGA CHOREOGRAPHY

                    Order Service
                         |
                         | OrderCreated
                         ↓
                  ┌──────────────┐
                  │ Kafka / Queue│
                  └──────┬───────┘
                         |
                         ↓
                  Payment Service
                         |
                         | PaymentCompleted
                         ↓
                  ┌──────────────┐
                  │ Kafka / Queue│
                  └──────┬───────┘
                         |
                         ↓
                 Inventory Service
                         |
                         | InventoryReserved
                         ↓
                  ┌──────────────┐
                  │ Kafka / Queue│
                  └──────┬───────┘
                         |
                         ↓
                  Shipping Service
```

### Failure:

```
Payment
   |
   X FAILED
   |
   ↓
PaymentFailed
   |
   ↓
Kafka
   |
   ├────────→ Order Service
   |              ↓
   |          Cancel Order
   |
   └────────→ Other interested services
```

---

## The Key Difference to Remember

### Orchestration:

> "I have a coordinator. I will tell each service what to do."

```
Orchestrator → Service A
Orchestrator → Service B
Orchestrator → Service C
```

### Choreography:

> "I don't have a coordinator. I'll publish an event, and whoever is interested will react."

```
Service A
   ↓
Event
   ↓
Service B
   ↓
Event
   ↓
Service C
```

And the concepts fit together like this:

```
Distributed Transaction
        ↓
       Saga
        ↓
 ┌──────┴─────────┐
 ↓                ↓
Orchestration   Choreography
 ↓                ↓
Commands         Events
 ↓                ↓
Central control  Distributed control
        ↓
Compensation
        ↓
Retry + Idempotency
        ↓
Eventual Consistency
```

This is the core foundation you need before moving into Kafka, message queues, exactly-once vs at-least-once delivery, outbox pattern, and distributed transaction failure scenarios.
