# What is HLD and How to Create It?

**HLD (High-Level Design)** is the architectural design of a software system. It describes the major components, services, databases, communication mechanisms, scaling strategy, and how everything works together.

A simple way to remember:

> **HLD = What components do we need, why do we need them, and how do they communicate?**

While **LLD** goes into classes and methods, **HLD** stays at the system/component level.

---

## 1. HLD vs LLD

Think about building a food-delivery application.

### HLD

You decide:

```
Client
   |
API Gateway
   |
   +------ Order Service
   |
   +------ Payment Service
   |
   +------ Restaurant Service
   |
   +------ Delivery Service
   |
   +------ Notification Service
```

You also decide:

```
Order Service → Order DB
Payment Service → Payment DB
Order Service → Kafka
Kafka → Notification Service
Kafka → Delivery Service
```

### LLD

Inside Order Service, you decide:

```
OrderController
       |
OrderService
       |
OrderRepository
       |
OrderValidator
       |
Order
OrderItem
```

### HLD vs LLD — Side-by-Side

| HLD | LLD |
|---|---|
| System architecture | Class/object design |
| Services | Classes |
| Databases | Objects |
| API boundaries | Methods |
| Kafka/queues | Object interactions |
| Load balancers | Interfaces |
| Scaling | Design patterns |
| Deployment | Implementation details |

---

## 2. What Does HLD Contain?

A typical HLD should answer:

1. What are the requirements?
2. What are the major components?
3. How do components communicate?
4. What database should we use?
5. How will the system scale?
6. How will we handle failures?
7. How will we maintain availability?
8. Where will caching be used?
9. Where will asynchronous communication be used?
10. How will the system be secured?
11. How will we monitor it?
12. What are the major bottlenecks?

---

## 3. Step 1 — Understand Requirements

Before drawing architecture, understand the problem.

Suppose the interviewer says:

> Design an e-commerce order system.

First clarify:

### Functional requirements

```
Customer can:
    - Browse products
    - Add product to cart
    - Place order
    - Make payment
    - Track order
    - Cancel order
```

### Non-functional requirements

- High availability
- Low latency
- Scalable
- Fault tolerant
- Secure
- Consistent order/payment data

This distinction is extremely important.

---

## 4. Step 2 — Estimate Scale

At senior level, don't immediately draw boxes.

First estimate the scale.

Suppose:

```
10 million users
1 million daily active users
100,000 orders/day
10,000 orders/hour during peak
```

Then estimate:

```
Orders/sec ≈ 100,000 / 86,400
           ≈ 1.16 orders/sec
```

If peak traffic is 10× average:

```
Peak ≈ 12 orders/sec
```

Now you can reason about:

- server capacity
- database capacity
- caching
- load balancing
- Kafka partitions
- replication
- storage

The exact numbers aren't the point.

The point is:

> **Use scale estimates to justify architectural decisions.**

---

## 5. Step 3 — Identify Major Components

Now identify the major responsibilities.

For e-commerce:

```
Client
   |
API Gateway
   |
   +------------------+
   |                  |
   v                  v
User Service      Product Service
                      |
                      v
                  Product DB

   |
   v
Order Service
   |
   +------ Order DB
   |
   +------ Kafka
             |
       +-----+-------+---------+
       |             |         |
       v             v         v
 Payment         Inventory   Notification
 Service         Service     Service
```

This is the HLD architecture.

---

## 6. Step 4 — Decide Monolith or Microservices

Don't automatically choose microservices.

### For a small application

```
Client
  |
  v
Application
  |
  +---- Users
  +---- Products
  +---- Orders
  +---- Payments
  |
  v
Database
```

A monolith can be appropriate.

### For a large system

```
Client
   |
API Gateway
   |
   +---- User Service
   +---- Product Service
   +---- Order Service
   +---- Payment Service
   +---- Inventory Service
```

You should explain **why** the boundary exists.

For example:

> Payment has different scaling, security, reliability, and ownership requirements, so it can be separated from Order Service.

---

## 7. Step 5 — Add Load Balancer

Suppose Order Service receives:

```
10,000 requests/sec
```

One server may not be sufficient.

Use horizontal scaling:

```
                  Load Balancer
                  /     |      \
                 /      |       \
                v       v        v
          Order-1   Order-2   Order-3
```

The load balancer distributes traffic.

This gives:

- scalability
- better availability
- fault tolerance

If:

```
Order-2 → DOWN
```

traffic can be sent to:

```
Order-1
Order-3
```

---

## 8. Step 6 — Add API Gateway

Instead of clients directly calling every service:

```
Client
  |
  +---- User Service
  +---- Order Service
  +---- Payment Service
  +---- Product Service
```

use:

```
Client
   |
   v
API Gateway
   |
   +---- User Service
   +---- Order Service
   +---- Payment Service
   +---- Product Service
```

API Gateway can handle:

- Authentication
- Authorization
- Rate limiting
- Routing
- Request validation
- TLS termination
- Logging
- Correlation ID

---

## 9. Step 7 — Database Design

Now ask:

> What data do we have?

For an order system:

- User
- Product
- Order
- OrderItem
- Payment
- Inventory

You might choose:

```
User Service
     |
User DB

Product Service
     |
Product DB

Order Service
     |
Order DB

Payment Service
     |
Payment DB
```

This is common in microservice architectures:

> **Each service owns its data.**

You then consider:

- SQL
- NoSQL
- Replication
- Partitioning
- Sharding
- Indexes
- Read replicas
- Transactions
- Consistency

---

## 10. Step 8 — Add Cache

Suppose Product Service receives:

```
50,000 product reads/sec
```

But product information doesn't change frequently.

Instead of hitting the database every time:

```
Client
  |
  v
Product Service
  |
  v
Redis
  |
  +---- HIT → return
  |
  +---- MISS
          |
          v
       Database
```

Typical cache-aside flow:

```
GET product

       |
       v
    Redis
       |
    +--+--+
    |     |
   HIT   MISS
    |     |
    v     v
 Return   DB
            |
            v
          Redis
            |
            v
          Return
```

---

## 11. Step 9 — Decide Synchronous vs Asynchronous Communication

Suppose customer places an order.

Some operations need an immediate response:

```
Order Service
     |
     v
Inventory Service
```

But sending email doesn't necessarily need to block the customer:

```
Order Service
     |
     v
Kafka
     |
     v
Notification Service
```

So:

### Synchronous

```
Order Service
     |
     | HTTP/gRPC
     v
Inventory Service
```

Used when the caller needs an immediate result.

### Asynchronous

```
Order Service
     |
     | Event
     v
Kafka
     |
     +---- Notification
     +---- Analytics
     +---- Delivery
```

Used when processing can happen asynchronously.

---

## 12. Step 10 — Add Kafka/Event Streaming

Imagine:

```
Order Created
```

Multiple systems need this information:

```
Order Service
      |
      v
   Kafka
   / |  \
  /  |   \
 v   v    v
Email Inventory Analytics
```

Without Kafka:

```
Order Service
   |
   +---- Notification
   +---- Inventory
   +---- Analytics
```

The Order Service becomes tightly coupled to everything.

With Kafka:

```
Order Service → Kafka → Consumers
```

This provides:

- asynchronous processing
- decoupling
- buffering
- replay capability
- independent consumer scaling

---

## 13. Step 11 — Design for Failure

A good HLD must answer:

> What happens when something fails?

Example:

```
Order Service
      |
      v
Payment Service
      |
      X
   Timeout
```

Possible mechanisms:

- Timeout
- Retry
- Exponential Backoff
- Circuit Breaker
- Idempotency
- Fallback
- Dead Letter Queue

For example:

```
Payment request
     |
     v
Timeout
     |
     v
Retry
     |
     v
Retry
     |
     v
Circuit Breaker
```

But retries must be used carefully, especially for payment operations.

---

## 14. Step 12 — High Availability

Suppose you have:

```
Order Service
     |
   Server
```

If the server fails:

```
Order Service → DOWN
```

Instead:

```
             Load Balancer
             /     |     \
            v      v      v
          Node1   Node2   Node3
```

Similarly, databases can use replication:

```
          Primary DB
          /       \
         v         v
    Replica-1   Replica-2
```

If the primary fails, a failover mechanism can promote another suitable node.

---

## 15. Step 13 — Monitoring and Observability

A production HLD should include:

- Logs
- Metrics
- Traces
- Alerts

Example:

```
Services
   |
   +---- Logs
   |
   +---- Metrics
   |
   +---- Traces
          |
          v
    Observability
      Platform
```

Typical metrics:

- Request rate
- Error rate
- p95 latency
- CPU
- Memory
- Database connections
- Kafka consumer lag
- Redis hit ratio

---

## 16. Step 14 — Security

Security should be part of HLD, not an afterthought.

Typical architecture:

```
Client
  |
 HTTPS/TLS
  |
API Gateway
  |
Authentication
  |
Authorization
  |
Services
```

You may need:

- OAuth 2.0
- OpenID Connect
- JWT
- TLS
- Secrets Management
- Rate Limiting
- DDoS Protection
- Encryption

---

## 17. Complete E-Commerce HLD

Putting everything together:

```
                         +----------------+
                         |    Clients     |
                         | Web / Mobile   |
                         +-------+--------+
                                 |
                                 | HTTPS
                                 v
                         +----------------+
                         | API Gateway    |
                         +-------+--------+
                                 |
                    +------------+------------+
                    |            |            |
                    v            v            v
              User Service  Product Service  Order Service
                    |            |            |
                    v            v            v
                 User DB     Product DB     Order DB
                                 |
                                 |
                              +--+--+
                              |Redis|
                              +-----+

                              Order Service
                                   |
                                   v
                                 Kafka
                           /       |       \
                          /        |        \
                         v         v         v
                    Payment    Inventory  Notification
                    Service     Service     Service
                       |           |           |
                       v           v           v
                   Payment DB   Inventory DB  Email/SMS
```

Then add infrastructure around it:

```
                     Load Balancer
                           |
                     API Gateway
                           |
              +------------+------------+
              |            |            |
           Services      Kafka         Redis
              |            |             |
             DBs      Consumers       Cache
              |
        Observability
     Logs / Metrics / Traces
```

---

## 18. How to Create HLD in an Interview

For a system-design interview, I recommend this order:

### Phase 1 — Requirements

1. Functional requirements
2. Non-functional requirements
3. Constraints

### Phase 2 — Scale

4. Users
5. Requests/sec
6. Storage
7. Read/write ratio
8. Peak traffic

### Phase 3 — Architecture

9. Client
10. Load Balancer
11. API Gateway
12. Services
13. Databases
14. Cache
15. Message Queue/Kafka

### Phase 4 — Deep Dive

16. Database choice
17. Partitioning/sharding
18. Replication
19. Consistency
20. Caching
21. API design
22. Kafka/event flow

### Phase 5 — Reliability

23. Timeouts
24. Retries
25. Circuit breaker
26. Idempotency
27. Failover
28. Disaster recovery

### Phase 6 — Production

29. Security
30. Monitoring
31. Logging
32. Metrics
33. Distributed tracing
34. Alerting

---

## 19. The HLD Mental Model

When you see any system-design problem, think:

```
                 HLD
                  |
      +-----------+-----------+
      |           |           |
      v           v           v
   Traffic      Data       Behavior
      |           |           |
      v           v           v
 Load Balancer  Database    Services
 API Gateway    Cache       Kafka
 Scaling        Sharding    APIs
                Replication Async
      |           |           |
      +-----------+-----------+
                  |
                  v
             Reliability
                  |
       +----------+----------+
       |          |          |
     Retry     Failover   Circuit
                         Breaker
                  |
                  v
             Observability
                  |
       +----------+----------+
       |          |          |
     Logs      Metrics     Traces
```

---

## ⭐ Interview-ready definition

> **HLD, or High-Level Design, is the architectural design of a software system that defines its major components, services, databases, APIs, communication mechanisms, data flow, scaling strategy, reliability, security, and observability. To create an HLD, first clarify functional and non-functional requirements, estimate scale, identify major components, choose communication and storage technologies, design data flow, and then address scalability, availability, fault tolerance, security, and monitoring.**

The key distinction to remember is:

- **HLD** → Services and how they communicate
- **LLD** → Classes and how objects collaborate
- **Code** → Actual implementation

