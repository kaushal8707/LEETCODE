# Event-Driven Architecture (EDA)

Event-Driven Architecture is an architectural style where services communicate by producing and consuming events instead of directly calling each other for every operation.

It is one of the most important concepts for microservices, Kafka, distributed systems, scalability, and asynchronous communication.

---

## 1. What is an Event?

An event is a record that something has already happened.

Examples:

- OrderCreated
- PaymentCompleted
- PaymentFailed
- InventoryReserved
- UserRegistered
- ShipmentCreated
- OrderCancelled

An event usually contains:

```json
{
  "eventId": "evt-123",
  "eventType": "OrderCreated",
  "orderId": "ORD-1001",
  "customerId": "CUS-101",
  "amount": 2500,
  "timestamp": "2026-09-11T10:30:00Z"
}
```

The important point is:

> An event represents a fact, not a command.

For example:

```
OrderCreated
```

means:

> "The order has been created."

Whereas:

```
CreateOrder
```

means:

> "Please create an order."

---

## 2. Traditional Microservices Communication

Suppose we have an e-commerce system.

Without event-driven architecture:

```
              ┌─────────────┐
              │ Order        │
              │ Service      │
              └──────┬──────┘
                     │
              REST   │
                     ▼
              ┌─────────────┐
              │ Payment     │
              │ Service     │
              └──────┬──────┘
                     │
              REST   │
                     ▼
              ┌─────────────┐
              │ Inventory   │
              │ Service     │
              └──────┬──────┘
                     │
              REST   │
                     ▼
              ┌─────────────┐
              │ Notification│
              │ Service     │
              └─────────────┘
```

Order Service knows about:

- Payment Service
- Inventory Service
- Notification Service

This creates tight coupling.

---

## 3. Event-Driven Architecture

Instead, Order Service publishes an event:

```
                    Order Service
                         │
                         │ OrderCreated
                         ▼
                  ┌───────────────┐
                  │ Event Broker  │
                  │    Kafka      │
                  └───────┬───────┘
                          │
          ┌───────────────┼────────────────┐
          │               │                │
          ▼               ▼                ▼
     Payment          Inventory       Notification
     Service           Service          Service
```

Now Order Service doesn't need to know exactly who consumes the event.

It simply says:

> "OrderCreated happened."

Interested services react to it.

---

## 4. Real-Time Example — E-Commerce Order

Imagine a customer places an order.

### Step 1 — Customer

```
Customer
   │
   │ POST /orders
   ▼
Order Service
```

Order Service stores:

```
Order ID = ORD-1001
Status   = CREATED
Amount   = ₹2,500
```

Then publishes:

```
OrderCreated
```

### Step 2 — Kafka

```
Order Service
      │
      │ OrderCreated
      ▼
   Kafka
```

Kafka stores the event.

```
Topic: order-events

Partition 0
─────────────────────────────
OrderCreated
OrderCreated
OrderCancelled
OrderCreated
```

### Step 3 — Payment Service

Payment Service consumes:

```
OrderCreated
```

and processes payment.

```
OrderCreated
      │
      ▼
Payment Service
      │
      ▼
Process Payment
      │
      ▼
PaymentCompleted
```

Then it publishes:

```
PaymentCompleted
```

### Step 4 — Inventory Service

Inventory Service may consume:

```
OrderCreated
```

or a later event such as:

```
PaymentCompleted
```

Then:

```
PaymentCompleted
        │
        ▼
Inventory Service
        │
        ▼
Reserve Inventory
        │
        ▼
InventoryReserved
```

### Step 5 — Notification Service

Notification Service can consume:

```
PaymentCompleted
InventoryReserved
```

and send:

- Email
- SMS
- Push Notification

The overall flow becomes:

```
                    ┌───────────────┐
                    │ Order Service │
                    └───────┬───────┘
                            │
                     OrderCreated
                            │
                            ▼
                     ┌────────────┐
                     │   Kafka    │
                     └─────┬──────┘
                           │
             ┌─────────────┼──────────────┐
             │             │              │
             ▼             ▼              ▼
         Payment       Inventory     Notification
         Service        Service        Service
             │             │
             ▼             ▼
     PaymentCompleted  InventoryReserved
             │             │
             └──────┬──────┘
                    ▼
             Notification
```

---

## 5. Main Components

EDA typically contains four major components.

```
Producer
   │
   ▼
Event Broker
   │
   ▼
Consumer
   │
   ▼
Business Processing
```

### Producer

Creates/publishes events.

Examples:

- Order Service
- Payment Service
- User Service
- Inventory Service

### Event Broker

Stores and distributes events.

Examples:

- Kafka
- RabbitMQ
- AWS EventBridge
- AWS SNS/SQS
- Azure Event Grid
- Google Pub/Sub

### Consumer

Consumes events.

Examples:

- Payment Service
- Inventory Service
- Notification Service
- Analytics Service
- Fraud Service

### Event

The message representing something that happened.

---

## 6. Why Event-Driven Architecture?

The biggest reason is:

> Decoupling.

Consider this traditional design:

```
Order Service
   │
   ├── REST → Payment
   ├── REST → Inventory
   ├── REST → Notification
   ├── REST → Fraud
   └── REST → Analytics
```

Order Service depends on five services.

Now consider:

```
Order Service
     │
     │ OrderCreated
     ▼
   Kafka
     │
     ├── Payment
     ├── Inventory
     ├── Notification
     ├── Fraud
     └── Analytics
```

Order Service only needs to know about the event contract.

---

## 7. Synchronous vs Event-Driven

### Synchronous

```
Order
  │
  │ REST
  ▼
Payment
  │
  │ REST
  ▼
Inventory
```

Order Service waits.

For example:

```
paymentService.processPayment();
```

The caller expects an immediate response.

### Asynchronous

```
Order
  │
  │ publish
  ▼
Kafka
  │
  ├── Payment
  ├── Inventory
  └── Notification
```

The producer doesn't need to wait for every consumer.

Conceptually:

```
kafkaTemplate.send("order-events", orderCreatedEvent);
```

The actual processing happens asynchronously.

---

## 8. Event-Driven Architecture Is Not Just Kafka

This is an important interview point.

Kafka is a technology.

Event-Driven Architecture is an architectural style.

You can implement EDA using:

- Kafka
- RabbitMQ
- AWS EventBridge
- AWS SNS/SQS
- Azure Event Grid
- Google Pub/Sub

Therefore:

```
EDA ≠ Kafka
```

Instead:

```
EDA
 │
 ├── Kafka
 ├── RabbitMQ
 ├── EventBridge
 ├── Pub/Sub
 └── Other brokers
```

---

## 9. Event vs Message vs Command

These concepts are frequently confused.

### Event

Something already happened.

```
OrderCreated
PaymentCompleted
UserRegistered
```

Usually named in past tense.

### Command

Someone is asking another component to perform an action.

```
CreateOrder
ProcessPayment
ReserveInventory
SendNotification
```

Usually imperative.

### Message

A general term for data exchanged between components.

An event or command can be carried as a message.

```
Message
 ├── Event
 └── Command
```

---

## 10. Event Chaining

One event can cause another event.

For example:

```
OrderCreated
     │
     ▼
Payment Service
     │
     ▼
PaymentCompleted
     │
     ▼
Inventory Service
     │
     ▼
InventoryReserved
     │
     ▼
Shipping Service
     │
     ▼
ShipmentCreated
```

This is a very common microservices architecture.

---

## 11. Event-Driven Architecture and Saga

EDA is heavily used for distributed transactions.

Suppose an order requires:

- Order
- Payment
- Inventory
- Shipping

We cannot easily use one database transaction across all services.

Instead:

```
OrderCreated
     ↓
PaymentCompleted
     ↓
InventoryReserved
     ↓
ShipmentCreated
```

If something fails:

```
PaymentCompleted
       ↓
InventoryReserved
       ↓
ShippingFailed
```

We can execute a compensating action:

```
ShippingFailed
       ↓
Release Inventory
       ↓
Refund Payment
       ↓
Cancel Order
```

This is the basis of a Saga.

---

## 12. EDA + Transactional Outbox

There is an important consistency problem.

Suppose Order Service does:

```
1. Save Order
2. Publish OrderCreated
```

What if:

```
Save Order → SUCCESS
Publish Event → FAILURE
```

Then:

```
Database:
Order exists ✅

Kafka:
OrderCreated missing ❌
```

Now the system is inconsistent.

A common solution is the Transactional Outbox Pattern:

```
                Order Service
                     │
              ┌──────┴──────┐
              │             │
              ▼             ▼
          Orders DB     Outbox Table
                            │
                            │
                       Outbox Relay
                            │
                            ▼
                          Kafka
```

The order and outbox event are stored in the same database transaction.

Then a relay publishes the event to Kafka.

This is extremely important in real-world microservices.

---

## 13. Eventual Consistency

EDA commonly introduces eventual consistency.

For example:

```
Order DB
   │
   │ OrderCreated
   ▼
Kafka
   │
   ├── Payment DB
   ├── Inventory DB
   └── Notification system
```

The databases may not update at exactly the same instant.

For a short period:

```
Order = CREATED
Payment = PROCESSING
Inventory = NOT_RESERVED
```

A few milliseconds/seconds later:

```
Order = CONFIRMED
Payment = COMPLETED
Inventory = RESERVED
```

Therefore:

> Event-driven systems often trade immediate consistency for scalability, availability, and loose coupling.

---

## 14. Consumer Failure

What happens if Payment Service is down?

Kafka can retain:

```
OrderCreated
```

The consumer can process it later.

```
Order Service
      │
      ▼
    Kafka
      │
      │ OrderCreated
      ▼
Payment Service ❌
```

Kafka retains the event.

Later:

```
Payment Service
      │
      │ restart
      ▼
Consumes OrderCreated
```

This is one of the major advantages of durable event brokers.

---

## 15. Consumer Lag

Suppose Kafka receives:

```
10,000 events/sec
```

but the consumer processes:

```
7,000 events/sec
```

Then:

```
Producer
   │
   ▼
Kafka
   │
   │ 10K/sec
   ▼
Consumer
   │
   │ 7K/sec
   ▼
Processing
```

The backlog grows.

This is called:

```
Consumer Lag
```

You can increase consumer parallelism, usually by adding consumers up to the number of partitions.

---

## 16. Multiple Consumers

One powerful property of event-driven architecture is that multiple independent consumers can react to the same business event.

```
                 OrderCreated
                      │
                      ▼
                    Kafka
                      │
        ┌─────────────┼──────────────┐
        ▼             ▼              ▼
     Payment       Analytics       Fraud
     Service        Service        Service
```

The Order Service doesn't need to change when Analytics or Fraud is added.

This is loose coupling.

---

## 17. Adding a New Consumer

Imagine today we have:

```
OrderCreated
    ↓
Payment
    ↓
Inventory
```

Tomorrow the business wants fraud detection.

Instead of changing Order Service:

```
Order Service
     │
     ▼
Kafka
     │
     ├── Payment
     ├── Inventory
     └── Fraud ← New
```

The new consumer can independently subscribe to the event.

This is one of the strongest reasons to use EDA.

---

## 18. Event Replay

Another major Kafka/EDA capability is event replay.

Suppose Analytics Service has a bug.

Historical events may still exist in Kafka:

```
OrderCreated
OrderCreated
PaymentCompleted
OrderCreated
...
```

After fixing the service:

```
Kafka
  │
  │ replay historical events
  ▼
Analytics Service
```

Analytics can rebuild its state.

This is particularly useful for:

- Analytics
- Audit systems
- Materialized views
- Data pipelines
- Event-sourced systems

---

## 19. Event Ordering

Ordering is another important consideration.

Suppose:

```
OrderCreated
PaymentCompleted
OrderCancelled
```

A consumer should not process:

```
OrderCancelled
OrderCreated
PaymentCompleted
```

when ordering matters.

With Kafka, ordering is generally guaranteed within a partition.

For example:

```
Partition 0
────────────────────────────
OrderCreated
PaymentCompleted
OrderCancelled
```

A common strategy is to use:

```
orderId
```

as the partition key.

Then events for the same order go to the same partition.

```
hash(orderId) → Partition
```

---

## 20. Idempotency

Event-driven systems frequently use at-least-once delivery.

Therefore an event can potentially be processed more than once.

Example:

```
PaymentCompleted
PaymentCompleted
```

If the payment consumer charges the customer twice, that's a serious problem.

Therefore consumers should be idempotent.

For example:

```
eventId = EVT-123
```

Store processed event IDs:

```
ProcessedEvents
----------------
EVT-123
EVT-456
```

If EVT-123 arrives again:

```
Already processed
       ↓
Ignore
```

---

## 21. Retry and Dead Letter Queue

Suppose:

```
Payment Service
       │
       ▼
OrderCreated
       │
       ▼
Processing fails
```

We can retry:

```
Retry 1
   ↓
Retry 2
   ↓
Retry 3
```

If it still fails:

```
Dead Letter Topic / Queue
```

Architecture:

```
Kafka
 │
 ▼
Consumer
 │
 ├── SUCCESS → Done
 │
 └── FAILURE
       │
       ▼
     Retry
       │
       ▼
     Retry
       │
       ▼
      DLQ
```

---

## 22. EDA Failure Scenarios

You should understand these for interviews.

### Producer failure

```
Order Service → Kafka ❌
```

Possible solutions:

- producer retries
- idempotent producer
- transactional producer
- transactional outbox

### Consumer failure

```
Kafka → Payment Service ❌
```

Kafka retains the event, allowing later processing depending on retention and consumer position.

### Duplicate event

```
OrderCreated
OrderCreated
```

Solution:

- Idempotent consumer

### Out-of-order events

```
PaymentCompleted
OrderCreated
```

Solutions may include:

- partitioning by entity key
- sequence numbers
- timestamps/version checks
- state validation

### Poison message

A particular event repeatedly fails processing.

```
Event
 ↓
Fail
 ↓
Retry
 ↓
Fail
 ↓
Retry
 ↓
Fail
```

Eventually:

```
DLQ
```

---

## 23. EDA and Backpressure

Suppose:

```
Producer = 100K events/sec
Consumer = 20K events/sec
```

Then:

```
             100K/sec
Producer ──────────────► Kafka
                           │
                           │ 20K/sec
                           ▼
                        Consumer
```

The backlog increases.

This is where backpressure becomes important.

Possible strategies:

- increase consumers
- increase partitions
- batch processing
- rate limiting
- pause/resume consumption
- optimize consumer processing
- control producer rate

---

## 24. EDA and Scaling

Suppose Kafka has:

```
10 partitions
```

and consumers:

```
Consumer 1
Consumer 2
Consumer 3
Consumer 4
```

The workload can be distributed:

```
Kafka
 ├── P0 ──► Consumer 1
 ├── P1 ──► Consumer 2
 ├── P2 ──► Consumer 3
 ├── P3 ──► Consumer 4
 ├── P4 ──► Consumer 1
 ├── P5 ──► Consumer 2
 ...
```

This allows horizontal scaling.

---

## 25. EDA vs REST

| Feature | REST | Event-Driven |
|---|---|---|
| Communication | Usually synchronous | Usually asynchronous |
| Coupling | Higher | Lower |
| Response | Immediate | Usually delayed |
| Failure propagation | Can propagate | Often isolated |
| Scalability | Good | Excellent for async workloads |
| Complexity | Lower | Higher |
| Debugging | Easier | Harder |
| Consistency | Often immediate | Often eventual |
| Replay | Usually no | Often possible |
| Best for | Queries/commands | Events/workflows |

The key is:

> Don't replace REST with events everywhere.

Use the right communication style for the requirement.

---

## 26. When Should You Use EDA?

EDA is a good fit when:

### 1. Asynchronous processing

Example:

```
Order → Email notification
```

The user doesn't need to wait for email delivery.

### 2. High throughput

Example:

```
Millions of transactions
```

Kafka-style architectures work well for high-volume event streams.

### 3. Multiple consumers

Example:

```
OrderCreated
    ├── Payment
    ├── Inventory
    ├── Fraud
    ├── Analytics
    └── Notification
```

### 4. Loose coupling

Producer should not know all downstream consumers.

### 5. Event replay

Useful for rebuilding state or analytics.

### 6. Long-running workflows

Examples:

```
Order → Payment → Inventory → Shipping
```

Saga-style workflows can use events.

---

## 27. When Should You NOT Use EDA?

EDA introduces complexity.

Avoid unnecessary events for simple operations.

For example:

```
GET /users/123
```

A synchronous REST call is usually simpler.

Don't create:

```
GetUserRequested
      ↓
Kafka
      ↓
UserService
      ↓
UserFound
      ↓
Kafka
```

for something that needs an immediate response.

Use:

```
Client → User Service → Response
```

instead.

---

## 28. Event-Driven Architecture in a Real Microservices System

A production architecture may look like:

```
                         ┌──────────────┐
                         │    Client    │
                         └──────┬───────┘
                                │
                                ▼
                         ┌──────────────┐
                         │ API Gateway  │
                         └──────┬───────┘
                                │
                    ┌───────────┼───────────┐
                    ▼           ▼           ▼
                  Order       User        Product
                 Service     Service      Service
                    │
                    │ OrderCreated
                    ▼
              ┌───────────────┐
              │     Kafka     │
              └───────┬───────┘
                      │
        ┌─────────────┼─────────────────┐
        ▼             ▼                 ▼
    Payment       Inventory          Notification
    Service        Service             Service
        │             │
        │             │
        ▼             ▼
   Payment DB     Inventory DB
        │
        ▼
 PaymentCompleted
        │
        ▼
      Kafka
        │
        ├──────────► Order Service
        ├──────────► Analytics
        └──────────► Fraud Service
```

This is a very common microservices + Kafka + EDA architecture.

---

## 29. EDA and Service Ownership

A good microservices design follows:

```
Service
   │
   ├── Owns business logic
   ├── Owns database
   └── Publishes business events
```

For example:

```
Payment Service
     │
     ├── Payment DB
     └── PaymentCompleted
```

Other services should not directly modify the Payment database.

They react to the event.

---

## 30. Event Contract

Events become contracts between services.

For example:

```json
{
  "eventId": "EVT-1001",
  "eventType": "PaymentCompleted",
  "eventVersion": 1,
  "timestamp": "2026-09-11T10:30:00Z",
  "data": {
    "paymentId": "PAY-1001",
    "orderId": "ORD-1001",
    "amount": 2500,
    "currency": "INR"
  }
}
```

Important fields often include:

- eventId
- eventType
- eventVersion
- timestamp
- producer
- correlationId
- data

Schema evolution becomes very important as systems grow.

---

## 31. Correlation ID

Imagine one request generates many events:

```
Request
   ↓
OrderCreated
   ↓
PaymentCompleted
   ↓
InventoryReserved
   ↓
ShipmentCreated
```

Use:

```
correlationId = CORR-123
```

across the workflow.

Then distributed tracing/logging can answer:

> "Show me everything that happened for this order/request."

This is extremely important for production debugging.

---

## 32. EDA and Observability

Distributed event-driven systems are harder to debug.

You need:

- Logs
- Metrics
- Distributed Tracing
- Correlation IDs
- Consumer Lag
- DLQ Monitoring
- Retry Monitoring

For example:

```
OrderCreated
   │
   │ correlationId=CORR-123
   ▼
Kafka
   │
   ├── Payment
   │      │
   │      └── PaymentCompleted
   │
   └── Inventory
          │
          └── InventoryReserved
```

Tracing allows you to follow the entire flow.

---

## 33. Biggest Advantages

### Loose coupling

Services don't directly depend on every downstream service.

### Scalability

Consumers can scale independently.

### Resilience

Temporary consumer failures don't necessarily lose events.

### Extensibility

New consumers can be added without changing producers.

### Asynchronous processing

Long-running work can happen in the background.

### Event replay

Historical events can sometimes be replayed.

### Better integration

Multiple systems can consume the same business events.

---

## 34. Biggest Disadvantages

EDA is not free.

### 1. Complexity

You introduce:

- Kafka
- Topics
- Partitions
- Consumer Groups
- Offsets
- Retries
- DLQs
- Schemas
- Monitoring

### 2. Eventual consistency

Data may not be immediately consistent.

### 3. Debugging

Instead of:

```
Service A → Service B
```

you may have:

```
A → Kafka → B → Kafka → C → Kafka → D
```

### 4. Duplicate processing

Consumers need idempotency.

### 5. Ordering challenges

Events may need careful partitioning/order management.

### 6. Schema evolution

Changing an event can break consumers.

### 7. Operational complexity

Kafka/brokers and consumers need monitoring and capacity planning.

---

## 35. Most Important Interview Concept

If the interviewer asks:

> "Why would you choose Event-Driven Architecture?"

A strong answer is:

> "I would choose event-driven architecture when services need loose coupling, asynchronous processing, independent scaling, high throughput, or multiple consumers reacting to the same business event. A producer publishes a business event to an event broker such as Kafka, and independent consumers process that event. This improves scalability and decoupling, but introduces eventual consistency, duplicate processing, ordering, retry, observability, and schema-management challenges."

---

## 36. EDA vs Event Sourcing

Don't confuse these.

### Event-Driven Architecture

Describes how services communicate.

```
Service → Event Broker → Consumer
```

### Event Sourcing

Describes how state is stored.

Instead of storing only:

```
Account Balance = ₹10,000
```

we store events:

```
AccountCreated
MoneyDeposited ₹15,000
MoneyWithdrawn ₹5,000
```

Current state can be reconstructed from events.

Therefore:

```
EDA ≠ Event Sourcing
```

You can use EDA without Event Sourcing.

---

## 37. EDA + Kafka + Microservices

For your system-design learning, connect these concepts together:

```
                MICROservices
                     │
                     ▼
          Event-Driven Architecture
                     │
                     ▼
                   Kafka
                     │
        ┌────────────┼────────────┐
        ▼            ▼            ▼
    Producer      Broker       Consumer
        │            │            │
        │       Partitions       │
        │       Replication      │
        │       Retention        │
        │            │            │
        └────────────┼────────────┘
                     ▼
              Consumer Groups
                     │
                     ▼
                 Offsets
                     │
                     ▼
              Consumer Lag
                     │
                     ▼
            Retry / DLQ / Backpressure
```

And for distributed consistency:

```
Microservices
      │
      ▼
     EDA
      │
      ├── Saga
      │
      ├── Transactional Outbox
      │
      ├── Idempotency
      │
      ├── Eventual Consistency
      │
      └── Compensating Transactions
```

---

## 38. The Mental Model to Remember

Think of EDA as a newspaper/news system.

```
Order Service
     │
     │ "Order Created!"
     ▼
  Newspaper
     │
     ├── Payment team reads it
     ├── Inventory team reads it
     ├── Fraud team reads it
     ├── Analytics team reads it
     └── Notification team reads it
```

The Order Service doesn't call everyone individually.

It publishes a fact:

> "OrderCreated."

Interested services decide what to do with that fact.

The five words to remember:

```
Publish → Broker → Consume → Process → React
```

And for production-grade EDA:

