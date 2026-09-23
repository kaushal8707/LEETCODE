# Event-Driven Architecture (EDA)

Since you're learning System Design from scratch, let's understand Event-Driven Architecture from the fundamentals and then move toward real-world microservices examples, Kafka, reliability, and interview-level design.

---

## 1. What is Event-Driven Architecture?

Event-Driven Architecture (EDA) is an architecture where services communicate by producing and consuming events.

An event is a statement that something has already happened.

For example:

- Order Created
- Payment Completed
- Payment Failed
- User Registered
- Product Shipped
- Wallet Updated

Instead of one service directly calling another service:

```text
Order Service
     |
     | REST API
     v
Payment Service
     |
     | REST API
     v
Notification Service
```

we can use events:

```text
              Event Broker
             (Kafka/RabbitMQ)
                   |
        +----------+----------+
        |          |          |
        v          v          v
   Payment     Inventory   Notification
   Service      Service      Service
        ^
        |
   Order Service
        |
        | publishes
        v
   OrderCreated
```

The Order Service doesn't need to know who consumes OrderCreated.

That's the key idea.

---

## 2. What is an Event?

An event represents something that happened in the system.

For example:

```json
{
  "eventType": "OrderCreated",
  "orderId": "ORD-12345",
  "customerId": "CUST-100",
  "amount": 2500,
  "timestamp": "2026-09-02T09:30:00Z"
}
```

Notice the name:

OrderCreated

Not:

CreateOrder

Why?

Because an event represents a fact, not a command.

Event

"Order was created."

Command

"Create this order."

This distinction becomes very important in distributed systems.

---

## 3. Traditional Request-Response Architecture

Suppose we are building an e-commerce application.

A user places an order.

A traditional architecture might look like:

```text
Client
  |
  v
Order Service
  |
  +----> Payment Service
  |
  +----> Inventory Service
  |
  +----> Notification Service
  |
  +----> Shipping Service
```

The Order Service has to call multiple services.

For example:

```text
POST /orders

Order Service
     |
     | POST /payment
     v
Payment Service
     |
     | response
     v
Order Service
     |
     | POST /inventory
     v
Inventory Service
     |
     | response
     v
Order Service
```

This creates tight coupling.

---

## 4. Event-Driven Approach

With EDA:

```text
Client
  |
  v
Order Service
  |
  | publishes
  v
+-------------------+
|   Event Broker    |
|      Kafka        |
+-------------------+
    |       |       |
    v       v       v
 Payment  Inventory Notification
 Service   Service    Service
```

Order Service simply publishes:

OrderCreated

Then different services consume it.

```text
OrderCreated
     |
     +------> Payment Service
     |
     +------> Inventory Service
     |
     +------> Notification Service
     |
     +------> Analytics Service
```

The Order Service doesn't need to know about these consumers.

---

## 5. Real-World Example — Amazon/Flipkart-like Order System

Imagine you purchase a phone.

Step 1 — User places order

```text
User
 |
 v
Order Service
```

Order Service stores:

```text
Order ID: ORD123
Product: iPhone
Amount: ₹80,000
Status: CREATED
```

Then publishes:

OrderCreated

Step 2 — Kafka receives the event

```text
Order Service
     |
     | OrderCreated
     v
    Kafka
```

Kafka stores the event.

Step 3 — Payment Service consumes it

```text
Kafka
  |
  | OrderCreated
  v
Payment Service
```

Payment Service processes payment.

Then publishes:

PaymentCompleted

Step 4 — Inventory Service consumes OrderCreated

At the same time:

```text
Kafka
  |
  | OrderCreated
  v
Inventory Service
```

Inventory Service reserves the product.

Step 5 — Notification Service

Notification Service can also consume:

OrderCreated

and send:

"Your order has been successfully created."

So the final flow becomes:

```text
                    +----------------+
                    |  Order Service |
                    +-------+--------+
                            |
                            | OrderCreated
                            v
                    +---------------+
                    |     Kafka     |
                    +-------+-------+
                            |
             +--------------+--------------+
             |              |              |
             v              v              v
       Payment Service  Inventory      Notification
                         Service         Service
             |              |              |
             v              v              v
       PaymentCompleted  StockReserved   Email/SMS
```

This is the core of Event-Driven Architecture.

---

## 6. Important Components

An EDA system generally contains:

```text
Producer
   |
   v
Event
   |
   v
Event Broker
   |
   v
Consumer
```

Let's understand each.

### 1. Producer

The service that publishes an event.

Example:

Order Service

publishes:

OrderCreated

### 2. Event

Represents something that happened.

Examples:

- OrderCreated
- PaymentCompleted
- PaymentFailed
- ProductShipped
- UserRegistered

### 3. Event Broker

The infrastructure that transports/stores events.

Examples:

- Apache Kafka
- RabbitMQ
- AWS SNS/SQS
- Google Pub/Sub
- Azure Event Hubs

For large-scale event streaming, Kafka is especially common.

### 4. Consumer

A service that listens to events.

Example:

Payment Service

consumes:

OrderCreated

---

## 7. Event Broker

The broker sits between producers and consumers.

```text
Producer
   |
   v
+----------------+
| Event Broker   |
+----------------+
   |
   +----> Consumer A
   |
   +----> Consumer B
   |
   +----> Consumer C
```

This provides decoupling.

The producer doesn't need to know:

- Who consumes the event?
- How many consumers exist?
- Where are they running?

---

## 8. Synchronous vs Event-Driven Communication

### Synchronous

```text
Order Service
     |
     | HTTP
     v
Payment Service
     |
     | response
     v
Order Service
```

Order Service waits.

```text
Order Service ----> Payment Service
       WAIT
       WAIT
       WAIT
       <---- Response
```

### Event-driven

```text
Order Service
     |
     | OrderCreated
     v
Kafka
     |
     +----> Payment Service
     |
     +----> Inventory Service
```

Order Service doesn't have to wait for every consumer.

---

## 9. Why Use Event-Driven Architecture?

### 1. Loose Coupling

Services don't directly depend on each other.

```text
Order Service
      |
      v
    Kafka
   /  |  \
  v   v   v
 A    B    C
```

Order Service doesn't need direct knowledge of A, B, and C.

### 2. Scalability

Suppose 1 million orders generate events.

Consumers can scale independently.

```text
                 Kafka
                   |
          OrderCreated events
                   |
       +-----------+-----------+
       |           |           |
       v           v           v
 Payment-1     Payment-2    Payment-3
```

We can add more consumers when traffic increases.

### 3. Asynchronous Processing

Some operations don't need an immediate response.

For example:

```text
Order Created
      |
      v
Send Email
Generate Analytics
Update Recommendation
```

These can happen asynchronously.

### 4. Multiple Consumers

One event can be useful to many services.

```text
             OrderCreated
                  |
                Kafka
          /       |       \
         /        |        \
        v         v         v
   Payment    Inventory   Analytics
```

### 5. Resilience

If Notification Service temporarily goes down:

```text
Order Service
     |
     v
   Kafka
     |
     X
Notification Service
```

The event can remain available in Kafka according to its retention configuration, allowing the consumer to process it later.

---

## 10. Event-Driven Architecture Does NOT Mean "No REST"

This is an important interview point.

A real system can use both:

```text
              REST
Client ----------------> Order Service

                         |
                         | Event
                         v
                       Kafka
                    /     |     \
                   v      v      v
              Payment  Inventory Notification
```

Use synchronous communication when an immediate response is required.

Use asynchronous events when work can happen independently.

---

## 11. Event vs Command

This is a very common interview question.

### Command

A command tells another component what to do.

- CreateOrder
- ProcessPayment
- ReserveInventory
- SendEmail

It is usually imperative.

"Do this."

### Event

An event tells the system what happened.

- OrderCreated
- PaymentProcessed
- InventoryReserved
- EmailSent

It is a fact.

"This happened."

Think:

Command:
"Process Payment"

       ↓

Payment Service

       ↓

Event:
"PaymentProcessed"

---

## 12. Event Chaining

Events can trigger other events.

For example:

```text
OrderCreated
     |
     v
Payment Service
     |
     v
PaymentCompleted
     |
     v
Inventory Service
     |
     v
InventoryReserved
     |
     v
Shipping Service
     |
     v
ShipmentCreated
```

This creates an event-driven workflow.

---

## 13. Event-Driven Architecture + Kafka

A common architecture looks like:

```text
                 +----------------+
                 | Order Service  |
                 +-------+--------+
                         |
                         | publish
                         v
                  +-------------+
                  |    Kafka    |
                  +------+------+
                         |
              +----------+----------+
              |          |          |
              v          v          v
          Payment    Inventory   Notification
          Consumer    Consumer     Consumer
```

Kafka provides:

- durable event storage
- partitioning
- consumer groups
- ordering within partitions
- replay capability
- horizontal scalability

---

## 14. Very Important Problem: Lost Events

Consider:

```text
Order Service

1. Save Order to DB       SUCCESS
2. Publish OrderCreated   FAILURE
```

Now:

```text
Database
   |
   +---- Order exists

Kafka
   |
   +---- OrderCreated doesn't exist
```

Payment Service never knows that the order was created.

This is a dual-write consistency problem.

A common solution is the Transactional Outbox Pattern.

```text
Order Service
     |
     +------------------+
     |                  |
     v                  v
 Order DB          Outbox Table
     |                  |
     |                  |
     +------------------+
              |
              v
        Outbox Publisher
              |
              v
            Kafka
```

We'll cover this deeply when studying reliable event-driven systems.

---

## 15. Another Problem: Duplicate Events

Suppose Kafka delivers:

OrderCreated

twice.

Consumer receives:

```text
OrderCreated
OrderCreated
```

If the consumer charges the customer twice, that's a serious problem.

Therefore consumers should generally be idempotent.

For example:

Payment Service

```text
if payment already processed for orderId:
       ignore
else:
       process payment
```

This is another critical distributed-systems concept.

---

## 16. Event Ordering

Suppose we have:

```text
OrderCreated
PaymentCompleted
OrderShipped
```

We normally want:

```text
OrderCreated
      ↓
PaymentCompleted
      ↓
OrderShipped
```

But distributed systems can have ordering challenges.

Kafka provides ordering within a partition, not globally across all partitions.

For example:

```text
Partition 0

OrderCreated
PaymentCompleted
OrderShipped
```

can preserve order for events with the same key if they are routed to the same partition.

A common key is:

orderId

---

## 17. Event Replay

One powerful capability of event-based systems is replay.

Suppose Analytics Service was down.

Kafka may still have:

```text
OrderCreated
OrderCreated
PaymentCompleted
OrderCreated
...
```

The service can consume historical events again.

```text
Kafka
 |
 | historical events
 v
Analytics Service
 |
 v
Rebuild state
```

This is very useful for:

- rebuilding projections
- analytics
- debugging
- recovering state
- new consumers

---

## 18. Eventual Consistency

EDA frequently leads to eventual consistency.

For example:

```text
Order DB
   |
   | OrderCreated
   v
Kafka
   |
   v
Inventory Service
```

There may be a small delay before Inventory Service receives and processes the event.

So temporarily:

```text
Order DB       = Order CREATED
Inventory DB   = old state
```

After processing:

```text
Order DB       = Order CREATED
Inventory DB   = Inventory RESERVED
```

The system eventually becomes consistent.

---

## 19. EDA and Microservices

EDA works particularly well with microservices.

Instead of:

```text
Order Service
     |
     +---- REST ---> Payment
     |
     +---- REST ---> Inventory
     |
     +---- REST ---> Notification
```

we can have:

```text
Order Service
     |
     v
   Kafka
     |
     +----> Payment
     |
     +----> Inventory
     |
     +----> Notification
     |
     +----> Analytics
```

This reduces direct service-to-service dependencies.

---

## 20. Event-Driven Architecture vs Pub/Sub

They are related but not exactly the same.

### Pub/Sub

Focuses on:

Publisher sends messages and subscribers receive them.

```text
Publisher
    |
    v
Topic
 / | \
v  v  v
A  B  C
```

### Event-Driven Architecture

Is a broader architectural style where system behavior is driven by events.

It can involve:

- Events
- Message Brokers
- Event Processing
- Event Stores
- Consumers
- Event-driven workflows

So:

Pub/Sub is a communication pattern.

EDA is an architectural style.

---

## 21. Event-Driven Architecture vs Event Sourcing

Don't confuse these.

### Event-Driven Architecture

Events are used to communicate between components.

```text
Order Service
     |
     v
OrderCreated
     |
     v
Payment Service
```

### Event Sourcing

Events are used as the source of truth for state.

Instead of storing:

```text
Account Balance = ₹10,000
```

we store events:

```text
AccountCreated
MoneyDeposited ₹20,000
MoneyWithdrawn ₹5,000
MoneyWithdrawn ₹5,000
```

Then reconstruct:

```text
₹20,000 - ₹5,000 - ₹5,000
= ₹10,000
```

You can have EDA without Event Sourcing.

---

## 22. Complete Real-World Example

Let's put everything together.

User buys a product:

```text
                         Client
                           |
                           v
                    +-------------+
                    |    Order    |
                    |   Service   |
                    +------+------+
                           |
                           | Save Order
                           v
                       Order DB
                           |
                           | OrderCreated
                           v
                    +-------------+
                    |    Kafka    |
                    +------+------+
                           |
          +----------------+----------------+
          |                |                |
          v                v                v
     Payment           Inventory       Notification
     Service            Service           Service
          |                |                |
          v                v                v
 PaymentCompleted     StockReserved      Email Sent
          |                |
          +-------+--------+
                  |
                  v
               Kafka
                  |
                  v
            Shipping Service
                  |
                  v
            ShipmentCreated
```

This is a typical event-driven microservices architecture.

---

## 23. When Should You Use EDA?

EDA is a good fit when:

- services need to be loosely coupled
- workloads are asynchronous
- there are many consumers of the same event
- high scalability is required
- temporary downstream failures should be tolerated
- event history/replay is valuable
- real-time processing is required

Examples:

- E-commerce
- Banking
- Payment processing
- Order management
- Fraud detection
- IoT
- Log processing
- Notifications
- Analytics
- Recommendation systems

---

## 24. When Should You NOT Use EDA?

Don't introduce Kafka/events just because they're popular.

For a simple operation:

```text
Client
  |
  v
User Service
  |
  v
Database
```

adding:

```text
Kafka
Consumers
Event schemas
Retries
Dead-letter queues
Idempotency
Monitoring
```

may create unnecessary complexity.

Use synchronous REST when a simple request-response interaction is sufficient.

---

## 25. The Key Mental Model

Remember this:

### Traditional architecture

```text
Service A
   |
   | "Do this"
   v
Service B
```

Direct dependency.

### Event-driven architecture

```text
Service A
   |
   | "This happened"
   v
 Event Broker
   |
   +----> Service B
   +----> Service C
   +----> Service D
```

Indirect communication and loose coupling.

---

## 26. Interview-Level Summary

If an interviewer asks:

What is Event-Driven Architecture?

A strong answer would be:

Event-Driven Architecture is an architectural style where services communicate by producing and consuming events representing state changes or facts that have occurred. Producers publish events to an event broker such as Kafka, and consumers independently process those events. This provides loose coupling, asynchronous communication, scalability, and the ability to support multiple consumers and event replay. However, it also introduces challenges such as eventual consistency, duplicate processing, ordering, schema evolution, retries, and reliable event publishing.

### What you should learn next

For your System Design progression, I'd recommend going in this order:

```text
Event-Driven Architecture
        ↓
Message Queue vs Event Streaming
        ↓
Kafka Fundamentals
        ↓
Topics & Partitions
        ↓
Consumer Groups
        ↓
Offset Management
        ↓
Delivery Semantics
   ├── At-most-once
   ├── At-least-once
   └── Exactly-once
        ↓
Idempotency
        ↓
Retry & Dead Letter Queue
        ↓
Transactional Outbox
        ↓
Saga Pattern
   ├── Choreography
   └── Orchestration
        ↓
Event Sourcing
        ↓
CQRS
```

This sequence will connect directly with the Distributed Transactions, Saga, Transactional Outbox, Replication, CAP, and Eventual Consistency topics you've been studying.
