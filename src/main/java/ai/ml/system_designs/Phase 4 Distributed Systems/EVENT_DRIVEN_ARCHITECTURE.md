# Event-Driven Architecture (EDA) and Microservices

These two concepts are closely related, but they are not the same thing.

- **Microservices** is about how we split an application into independently deployable services.
- **Event-Driven Architecture (EDA)** is about how those services communicate using events.

A very common modern architecture is:

```
                EVENT-DRIVEN MICROSERVICES

 ┌────────────┐
 │   Client   │
 └─────┬──────┘
       │
       ↓
 ┌────────────┐
 │ Order      │
 │ Service    │
 └─────┬──────┘
       │
       │ OrderCreated
       ↓
 ┌────────────────────┐
 │ Kafka / Message    │
 │ Broker             │
 └──────┬─────────────┘
        │
   ┌────┼──────────────┬────────────┐
   ↓    ↓              ↓            ↓
Payment Inventory   Notification  Analytics
Service Service      Service       Service
```

Let's understand this from scratch.

---

## 1. What Is an Event?

An event is a record that says:

> "Something happened."

For example:

- OrderCreated
- PaymentCompleted
- PaymentFailed
- InventoryReserved
- InventoryReservationFailed
- ShipmentCreated
- OrderCancelled

An event describes something that has already happened.

For example:

```json
{
  "eventType": "OrderCreated",
  "orderId": "ORD-1001",
  "customerId": "CUST-101",
  "amount": 5000
}
```

The important wording is:

```
OrderCreated
```

not:

```
CreateOrder
```

Why?

Because:

```
CreateOrder
```

sounds like a command:

> "Please create this order."

Whereas:

```
OrderCreated
```

means:

> "The order has already been created."

---

## 2. What Is Event-Driven Architecture?

In traditional synchronous communication:

```
Order Service
      |
      | HTTP Request
      ↓
Payment Service
      |
      | HTTP Response
      ↓
Order Service
```

Order Service directly calls Payment Service.

In Event-Driven Architecture:

```
Order Service
      |
      | OrderCreated
      ↓
   Kafka
      |
      ├────────→ Payment Service
      ├────────→ Inventory Service
      ├────────→ Notification Service
      └────────→ Analytics Service
```

The Order Service publishes an event.

Other services consume the event.

---

## 3. Real-Time Example — E-Commerce

Imagine you buy a laptop for ₹80,000.

You click:

```
Place Order
```

We have these microservices:

- Order Service
- Payment Service
- Inventory Service
- Shipping Service
- Notification Service

Each service owns its own responsibility.

### Step 1 — Create Order

Client:

```
POST /orders
```

Order Service:

```
Order DB

ORD-1001
Status = PENDING
```

Then it publishes:

```
OrderCreated
```

to Kafka.

```
Order Service
      |
      | OrderCreated
      ↓
    Kafka
```

---

## 4. Multiple Services Consume the Same Event

This is one of the most powerful ideas in EDA.

Suppose:

```
OrderCreated
```

is published.

Multiple services can independently consume it:

```
                         OrderCreated
                              |
                              ↓
                            Kafka
                              |
             ┌────────────────┼─────────────────┐
             ↓                ↓                 ↓
       Payment Service  Inventory Service  Notification
```

Payment Service:

```
Process payment
```

Inventory Service:

```
Reserve product
```

Notification Service:

```
Send order confirmation
```

Analytics Service:

```
Record order metric
```

The Order Service doesn't need to call each service individually.

---

## 5. This Creates Loose Coupling

Without events:

```
Order Service
   |
   ├────→ Payment Service
   |
   ├────→ Inventory Service
   |
   ├────→ Notification Service
   |
   └────→ Analytics Service
```

Order Service knows about all these services.

This creates stronger coupling.

With EDA:

```
Order Service
      |
      ↓
 OrderCreated
      |
      ↓
    Kafka
   /  |  |  \
  ↓   ↓  ↓   ↓
 P    I   N   A
```

Order Service only needs to know:

> "I need to publish OrderCreated."

It doesn't need to know who consumes it.

---

## 6. What Is a Message Broker?

The middle component is generally called a:

**Message Broker / Event Broker**

Examples:

- Apache Kafka
- RabbitMQ
- Amazon SQS/SNS
- Google Pub/Sub
- Azure Service Bus

Conceptually:

```
Producer
   |
   ↓
┌──────────────────┐
│ Message Broker   │
│                  │
│ OrderCreated     │
│ PaymentCompleted │
│ OrderCancelled   │
└───────┬──────────┘
        |
        ↓
Consumers
```

The producer publishes.

Consumers subscribe.

---

## 7. Microservices + Events

Now let's connect this with microservices.

A microservice typically:

- owns a specific business capability
- owns its data
- can be deployed independently
- can scale independently

For example:

```
┌─────────────────────┐
│ Order Service       │
│                     │
│ Order DB            │
└─────────────────────┘

┌─────────────────────┐
│ Payment Service     │
│                     │
│ Payment DB          │
└─────────────────────┘

┌─────────────────────┐
│ Inventory Service   │
│                     │
│ Inventory DB        │
└─────────────────────┘
```

They can communicate using events:

```
Order Service
     |
     ↓
 OrderCreated
     |
     ↓
   Kafka
     |
     ├────→ Payment
     ├────→ Inventory
     └────→ Notification
```

That's an:

> Event-driven microservices architecture.

---

## 8. Real-Time Banking Example

Suppose a customer transfers ₹10,000.

You might have:

- Account Service
- Fraud Service
- Notification Service
- Transaction Service
- Analytics Service

Account Service processes the transaction:

```
₹50,000
   ↓
₹40,000
```

Then publishes:

```
MoneyTransferred
```

```
Account Service
      |
      | MoneyTransferred
      ↓
    Kafka
      |
      ├────→ Fraud Service
      |
      ├────→ Notification Service
      |
      ├────→ Analytics Service
      |
      └────→ Transaction Service
```

Fraud Service:

```
Analyze transaction
```

Notification:

```
Send SMS / Push
```

Analytics:

```
Record transaction
```

Transaction Service:

```
Update transaction history
```

The Account Service doesn't have to synchronously wait for all of them.

---

## 9. Event Flow

A typical event-driven workflow looks like this:

```
                    Client
                       |
                       ↓
                 Order Service
                       |
                       | 1. Create order
                       ↓
                    Order DB
                       |
                       | 2. Publish event
                       ↓
                  OrderCreated
                       |
                       ↓
                     Kafka
                  /    |    \
                 ↓     ↓     ↓
            Payment Inventory Notification
              |         |          |
              ↓         ↓          ↓
          Payment DB Inventory DB Email/SMS
```

This is **asynchronous communication**.

---

## 10. Event vs Command

This distinction is very important.

### Command

A command tells another component:

> "Do something."

Example:

- ProcessPayment
- ReserveInventory
- CancelOrder

### Event

An event says:

> "Something already happened."

Example:

- PaymentCompleted
- InventoryReserved
- OrderCancelled

Think:

```
Command → DO something
Event   → SOMETHING happened
```

---

## 11. Event-Driven Architecture and Saga

This connects directly to what we discussed earlier.

Saga Choreography commonly uses EDA.

For example:

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
     |
     | InventoryReserved
     ↓
   Kafka
     |
     ↓
Shipping Service
```

There is no central orchestrator.

The events drive the workflow.

That's:

> Saga Choreography + Event-Driven Architecture

---

## 12. Saga Orchestration Can Also Use Events

Don't confuse EDA with choreography.

You can also have:

```
                 Saga Orchestrator
                        |
                        ↓
                      Kafka
                   /    |    \
                  ↓     ↓     ↓
             Payment Inventory Order
```

The orchestrator can publish commands through Kafka and receive events back.

So:

> Event-driven architecture can be used with both Saga Orchestration and Saga Choreography.

---

## 13. What Happens If a Consumer Is Down?

Suppose:

```
Order Service
      |
      ↓
OrderCreated
      |
      ↓
Kafka
      |
      ↓
Payment Service ❌ DOWN
```

The Order Service doesn't necessarily fail just because Payment Service is temporarily unavailable.

The event remains available according to the broker's retention/delivery configuration.

When Payment Service comes back:

```
Payment Service
      |
      ↓
Consume OrderCreated
      |
      ↓
Process Payment
```

This gives the architecture better resilience.

---

## 14. Handling Traffic Spikes

This also connects to your earlier question:

> Client sends 100 requests/sec but server processes only 10/sec.

With asynchronous processing:

```
Producer
100/sec
   |
   ↓
Kafka
   |
   ↓
Consumers
10/sec
```

Kafka acts as a buffer.

If necessary, scale consumers:

```
                    Kafka
                      |
        ┌─────────────┼─────────────┐
        ↓             ↓             ↓
     Consumer 1    Consumer 2    Consumer 3
       10/sec        10/sec         10/sec

               Total = 30/sec
```

If traffic continues at 100/sec, you still need enough capacity or backpressure.

---

## 15. Consumer Groups

Kafka introduces an important concept called a **consumer group**.

Suppose we have:

```
OrderCreated
```

and Payment Service has three instances:

```
Payment Consumer 1
Payment Consumer 2
Payment Consumer 3
```

They can belong to the same consumer group:

```
Payment Consumer Group
 ├── Consumer 1
 ├── Consumer 2
 └── Consumer 3
```

A given Kafka partition's message is processed by only one consumer within that group at a time.

This allows horizontal scaling.

---

## 16. Different Services Can Have Different Consumer Groups

Suppose:

```
OrderCreated
```

needs to be consumed by:

- Payment
- Inventory
- Notification
- Analytics

They can have separate consumer groups:

```
                    OrderCreated
                         |
                       Kafka
                    /    |    \
                   /     |     \
                  ↓      ↓      ↓
             Payment  Inventory Notification
              Group     Group      Group
```

Each group gets its own logical consumption of the event.

That's extremely powerful.

---

## 17. Important Problem: Duplicate Events

In real distributed systems, don't assume:

> "The event will always be delivered exactly once."

A consumer might receive:

```
PaymentCompleted
PaymentCompleted
```

Therefore:

```
Consumer
   |
   ↓
Check event ID
   |
   ├── Already processed → Ignore
   |
   └── New event → Process
```

This is where **idempotency** comes in.

For example:

```json
{
  "eventId": "EVT-123",
  "eventType": "PaymentCompleted",
  "orderId": "ORD-1001"
}
```

Consumer stores:

```
EVT-123 → PROCESSED
```

If it sees the same event again:

```
EVT-123
   ↓
Already processed
   ↓
Don't execute again
```

---

## 18. Outbox Pattern

There's another important problem.

Suppose Order Service does:

1. Save Order to DB
2. Publish OrderCreated to Kafka

What if:

```
Save Order → SUCCESS
Publish Kafka → FAILURE
```

Now:

```
Order exists
BUT
OrderCreated event was never published
```

This is a serious consistency problem.

A common solution is the **Transactional Outbox Pattern**.

```
Order Service
      |
      ↓
┌───────────────────────┐
│ Order DB              │
│                       │
│ Order                 │
│ Outbox Event          │
└───────────────────────┘
      |
      ↓
Outbox Publisher
      |
      ↓
Kafka
```

Order and outbox record are written in the same local DB transaction.

Then a publisher reliably sends the outbox event to Kafka.

This is an important next-level concept in event-driven microservices.

---

## 19. Eventual Consistency

EDA often means the system becomes eventually consistent.

For example:

Immediately after order creation:

```
Order DB
Order = CREATED

Payment DB
Payment = PROCESSING

Inventory DB
Inventory = PROCESSING
```

A few seconds later:

```
Order = CONFIRMED
Payment = SUCCESS
Inventory = RESERVED
```

So there may be a temporary period where different services have different views.

That's:

> Eventual consistency.

---

## 20. Advantages of Event-Driven Microservices

### Loose coupling

Services don't need direct dependencies on every consumer.

### Scalability

Consumers can be scaled independently.

### Resilience

Temporary consumer failures don't necessarily stop producers.

### Asynchronous processing

Long-running work can happen in the background.

### Extensibility

You can add a new consumer without modifying the producer.

For example:

```
OrderCreated
     ↓
Kafka
     |
     ├── Payment
     ├── Inventory
     ├── Notification
     └── Recommendation Service ← NEW
```

Order Service doesn't necessarily need to change.

---

## 21. Disadvantages

EDA isn't automatically better.

### 1. Eventual consistency

Data may not be immediately synchronized.

### 2. Debugging complexity

Instead of:

```
A → B → C
```

you might have:

```
A → Kafka → B → Kafka → C
       ↓
       D
       ↓
       E
```

You need good distributed tracing and correlation IDs.

### 3. Duplicate processing

Consumers must handle duplicates.

### 4. Ordering problems

Events may need ordering guarantees.

For example:

```
OrderCreated
OrderCancelled
```

must not be processed as:

```
OrderCancelled
OrderCreated
```

for workflows where ordering matters.

### 5. Operational complexity

You now need to manage:

- Kafka
- Topics
- Partitions
- Consumer Groups
- Offsets
- Retry
- Dead Letter Queues
- Monitoring
- Schema Evolution

---

## 22. Synchronous Microservices vs Event-Driven Microservices

### Synchronous

```
Order Service
      |
      | HTTP
      ↓
Payment Service
      |
      | Response
      ↓
Order Service
```

Characteristics:

```
Request → Wait → Response
```

### Event-driven

```
Order Service
      |
      | OrderCreated
      ↓
Kafka
      |
      ↓
Payment Service
```

Characteristics:

```
Publish → Continue
```

---

## 23. When Should You Use Event-Driven Architecture?

Good candidates:

- Order Created
- Payment Completed
- User Registered
- Shipment Created
- Money Transferred
- File Uploaded
- Video Uploaded
- Notification Requested
- Analytics Events

Especially when:

> The producer doesn't need the consumer's result immediately.

---

## 24. When Should You NOT Use It?

Don't introduce Kafka/events just because you have microservices.

For example:

```
GET /users/123
```

If you need the user information immediately:

```
Client
  ↓
User Service
  ↓
Response
```

Synchronous REST/gRPC is usually simpler.

Likewise:

- Validate OTP
- Get Account Balance
- Check Product Details

often benefit from synchronous communication.

---

## 25. The Complete Architecture

Here's a production-style mental model:

```
                           CLIENT
                              |
                              ↓
                        API Gateway
                              |
                              ↓
                       Order Service
                              |
                    ┌─────────┴─────────┐
                    ↓                   ↓
                 Order DB          Outbox Table
                                        |
                                        ↓
                                Outbox Publisher
                                        |
                                        ↓
                                   ┌────────┐
                                   │ Kafka  │
                                   └───┬────┘
                         ┌──────────────┼──────────────┐
                         ↓              ↓              ↓
                    Payment         Inventory      Notification
                    Service          Service          Service
                         |              |              |
                         ↓              ↓              ↓
                    Payment DB      Inventory DB    Notification
                                                        |
                                                        ↓
                                                       User
```

This architecture combines several concepts you've been learning:

```
Microservices
     ↓
Each service owns its data
     ↓
Event-Driven Architecture
     ↓
Kafka / Message Broker
     ↓
Asynchronous Communication
     ↓
Saga
     ↓
Compensating Transactions
     ↓
Retry
     ↓
Idempotency
     ↓
Outbox Pattern
     ↓
Eventual Consistency
```

---

## The Simplest Way to Remember It

**Microservices** answer: "How do I split my system?"

**Event-Driven Architecture** answers: "How do these services communicate when something happens?"

And:

```
                 MICROSERVICES
                       |
                       ↓
          ┌────────────┴────────────┐
          ↓                         ↓
   Synchronous                  Asynchronous
   REST / gRPC                  Events / Kafka
          |                         |
          ↓                         ↓
     Request/Response          Publish/Subscribe
                                      |
                                      ↓
                                Event-Driven
                                 Architecture
```

The next concepts that naturally follow from this are Kafka → Topic → Partition → Consumer Group → Offset → Retry → Dead Letter Queue → Outbox Pattern, because these are the pieces you need to turn the above architecture into a production-grade system.
