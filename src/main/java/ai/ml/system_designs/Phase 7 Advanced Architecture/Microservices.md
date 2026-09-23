# Microservices

Microservices architecture is an architectural style where a large application is broken into small, independently deployable services, with each service responsible for a specific business capability.

For someone with 10 years of experience preparing for system design, the important part is not just knowing the definition—you should understand why microservices exist, how services communicate, data ownership, failures, deployment, scaling, and distributed-system problems.

---

## 1. Monolith vs Microservices

### Monolithic architecture

Everything is inside one application:

```
                ┌─────────────────────────┐
                │      E-Commerce App     │
                │                         │
User ──────────►│ Order                    │
                │ Payment                  │
                │ Inventory                │
                │ User                    │
                │ Notification            │
                └───────────┬─────────────┘
                            │
                            ▼
                       Database
```

For example:

```
E-Commerce
 ├── User
 ├── Order
 ├── Payment
 ├── Inventory
 └── Notification
```

Usually, the whole application is deployed together.

---

## 2. Microservices architecture

We split the application according to business capabilities.

```
                         ┌───────────────┐
                         │ API Gateway   │
                         └───────┬───────┘
                                 │
          ┌──────────────────────┼─────────────────────┐
          │          │           │          │           │
          ▼          ▼           ▼          ▼           ▼
     ┌────────┐ ┌─────────┐ ┌─────────┐ ┌────────┐ ┌────────────┐
     │ User   │ │ Order   │ │ Payment │ │Inventory│ │Notification│
     │Service │ │ Service │ │ Service │ │Service │ │  Service   │
     └───┬────┘ └────┬────┘ └────┬────┘ └────┬───┘ └─────┬──────┘
         │            │           │           │             │
         ▼            ▼           ▼           ▼             ▼
       User DB     Order DB    Payment DB  Inventory DB Notification DB
```

Each service can potentially:

- be developed independently
- be deployed independently
- scale independently
- own its data
- fail independently
- use different technology if necessary

---

## 3. What is a Microservice?

A microservice should ideally represent a business capability, not simply a technical component.

For example:

**Order Service**

owns order-related operations:

```
Create Order
Get Order
Cancel Order
Update Order
```

**Payment Service** owns:

```
Authorize Payment
Capture Payment
Refund Payment
```

**Inventory Service** owns:

```
Reserve Stock
Release Stock
Check Availability
```

The important concept is:

> A service should have clear ownership and responsibility.

---

## 4. Real-time example — E-Commerce

Suppose you place an order.

```
Customer
   │
   ▼
API Gateway
   │
   ▼
Order Service
   │
   ├──────────► Order DB
   │
   ▼
Kafka
   │
   ├────────► Payment Service
   │
   ├────────► Inventory Service
   │
   └────────► Notification Service
```

A possible flow:

### Step 1 — Create order

```
POST /orders
```

Order Service:

```
Create Order
     ↓
Save Order
     ↓
Publish OrderCreated
```

### Step 2 — Payment

Payment Service consumes:

```
OrderCreated
```

and processes payment.

```
OrderCreated
     ↓
Payment Service
     ↓
Payment Gateway
     ↓
Payment successful
```

### Step 3 — Inventory

Inventory Service consumes the same event:

```
OrderCreated
     ↓
Inventory Service
     ↓
Reserve Product
```

### Step 4 — Notification

Notification Service consumes:

```
OrderCreated
```

and sends:

- Email
- SMS
- Push notification

This is where event-driven architecture + Kafka becomes very useful.

---

## 5. Why do we need Microservices?

The major reasons are:

### 1. Independent scaling

Suppose:

```
Order Service     → 100 requests/sec
Payment Service   → 50 requests/sec
Search Service    → 10,000 requests/sec
```

In a monolith, you may need to scale the entire application.

With microservices:

```
Search Service
     ↓
10 instances

Order Service
     ↓
3 instances

Payment Service
     ↓
2 instances
```

You scale only what needs scaling.

### 2. Independent deployment

Suppose you modify Notification Service.

In a monolith:

```
Change Notification
        ↓
Build entire application
        ↓
Test entire application
        ↓
Deploy entire application
```

Microservices:

```
Change Notification Service
        ↓
Build Notification Service
        ↓
Test
        ↓
Deploy Notification Service
```

Other services don't necessarily need redeployment.

### 3. Fault isolation

Suppose Notification Service is down.

```
Order Service       → UP
Payment Service     → UP
Inventory Service   → UP
Notification        → DOWN
```

Orders can potentially continue processing while notifications are retried asynchronously.

This is much better than allowing a failure in one module to bring down the entire application.

### 4. Team independence

Different teams can own different services:

```
Team A → Order Service

Team B → Payment Service

Team C → Inventory Service

Team D → Notification Service
```

This becomes especially valuable for large organizations.

### 5. Technology independence

One service might use:

```
Java + Spring Boot
```

Another:

```
Go
```

Another:

```
Python
```

Although this flexibility is useful, using different technologies just because you can is usually a bad reason.

---

## 6. Microservices are NOT simply "many APIs"

This is an important interview point.

Bad decomposition:

```
UserController Service
OrderController Service
PaymentController Service
```

Better decomposition:

```
User Service
Order Service
Payment Service
Inventory Service
```

The boundary should generally be based on business/domain boundaries.

This is closely related to Domain-Driven Design (DDD) and bounded contexts.

---

## 7. How do Microservices communicate?

There are two major approaches.

### Synchronous communication

Usually:

- REST
- gRPC

Example:

```
Order Service
     │
     │ HTTP
     ▼
Payment Service
```

Order Service waits for the response.

```
Order → Payment
         ↓
       response
         ↓
Order continues
```

#### Advantages

- Simple
- Immediate response
- Easy to understand

#### Problems

- Tight runtime coupling
- Network latency
- Cascading failures
- Dependency on another service's availability

---

## 8. Asynchronous communication

Common technologies:

- Kafka
- RabbitMQ
- AWS SQS

Example:

```
Order Service
      │
      ▼
    Kafka
      │
      ├────────► Payment Service
      │
      ├────────► Inventory Service
      │
      └────────► Notification Service
```

The producer doesn't have to wait for every consumer.

This provides:

- loose coupling
- better scalability
- buffering
- asynchronous processing
- resilience

But introduces distributed-system complexity such as:

- duplicate messages
- ordering
- retries
- consumer lag
- eventual consistency
- idempotency
- dead-letter queues/topics

---

## 9. Database per Service

One of the most important microservices principles is:

> A service should own its data.

Example:

```
Order Service
     │
     ▼
 Order DB

Payment Service
     │
     ▼
Payment DB

Inventory Service
     │
     ▼
Inventory DB
```

Avoid:

```
              ┌──────────────┐
Order ───────►│              │
Payment ─────►│ Shared DB    │
Inventory ───►│              │
              └──────────────┘
```

Why?

Because a shared database creates strong coupling.

For example:

```
Payment Service
      │
      ▼
payment_table
```

If Order Service directly modifies the same table:

```
Order Service ──► payment_table
```

then Payment Service no longer truly owns its data.

---

## 10. But how do services get another service's data?

Suppose Order Service needs payment status.

Instead of accessing Payment DB directly:

```
Order Service ──X──► Payment DB
```

Use:

### Option 1 — API

```
Order Service
      │
      ▼
Payment Service
      │
      ▼
Payment DB
```

### Option 2 — Events

```
Payment Service
      │
      ▼
PaymentCompleted
      │
      ▼
Kafka
      │
      ▼
Order Service
```

Order Service maintains the information it needs.

---

## 11. API Gateway

Clients usually shouldn't communicate directly with dozens of services.

Instead:

```
Mobile App
     │
     ▼
API Gateway
     │
     ├────► User Service
     ├────► Order Service
     ├────► Payment Service
     └────► Product Service
```

API Gateway can handle:

- authentication
- authorization
- routing
- rate limiting
- request aggregation
- SSL/TLS termination
- logging
- monitoring

---

## 12. Service Discovery

In a dynamic environment, service instances can change.

For example:

```
Order Service

10.0.0.10
10.0.0.11
10.0.0.12
```

Payment Service shouldn't necessarily hard-code these IP addresses.

Service discovery provides a mechanism to locate instances.

```
Order Service
      │
      ▼
Service Discovery
      │
      ▼
Payment Service instances
```

Examples include Kubernetes service discovery and systems such as Consul.

---

## 13. Load Balancing

Suppose Order Service has three instances:

```
             Load Balancer
                  │
        ┌─────────┼─────────┐
        ▼         ▼         ▼
     Order-1   Order-2   Order-3
```

Requests are distributed across instances.

This provides:

- scalability
- availability
- better resource utilization

---

## 14. Failure is inevitable

This is where microservices become a distributed systems problem.

Suppose:

```
Order Service
     │
     ▼
Payment Service
     │
     ▼
Payment Gateway
```

Payment Gateway becomes slow.

Without protection:

```
Payment slow
     ↓
Payment Service slow
     ↓
Order Service waits
     ↓
Threads exhausted
     ↓
Order Service fails
```

This can become a cascading failure.

Therefore microservices commonly use:

- Timeout
- Retry
- Exponential Backoff
- Circuit Breaker
- Bulkhead
- Rate Limiting
- Backpressure
- Fallback

These are important topics for system design interviews.

---

## 15. Distributed Transactions

This is one of the biggest challenges.

Suppose:

```
Order DB
Payment DB
Inventory DB
```

You want:

```
Create Order
     ↓
Payment
     ↓
Reserve Inventory
```

These are separate databases.

You can't simply assume one traditional database transaction covers everything.

Common approaches include:

- Saga Pattern
- Transactional Outbox
- Event-driven architecture
- Idempotency
- Compensating transactions

For example:

```
OrderCreated
     ↓
Payment
     ↓
PaymentFailed
     ↓
Cancel Order
```

The cancellation is a compensating action.

---

## 16. Observability

In a monolith:

```
Request
  ↓
Application
  ↓
Database
```

Debugging can be relatively straightforward.

In microservices:

```
Client
  ↓
Gateway
  ↓
Order
  ↓
Payment
  ↓
Kafka
  ↓
Inventory
  ↓
Notification
```

You need:

### Logs

```
Order Service logs
Payment Service logs
Inventory Service logs
```

### Metrics

- CPU
- Memory
- Latency
- Throughput
- Error rate
- Consumer lag

### Distributed tracing

A request can carry a trace ID:

```
Trace ID: ABC123

Gateway
   ↓
Order
   ↓
Payment
   ↓
Inventory
```

This lets you trace one request across multiple services.

---

## 17. Security

Each service shouldn't blindly trust every request.

Common concerns:

- Authentication
- Authorization
- TLS
- OAuth2 / OIDC
- JWT
- Service-to-service authentication
- Secrets management
- mTLS

Typical architecture:

```
Client
   ↓
API Gateway
   ↓
Authentication
   ↓
Microservices
```

---

## 18. Deployment

Microservices are commonly deployed using containers.

```
             Kubernetes
                 │
     ┌───────────┼────────────┐
     ▼           ▼            ▼
 Order Pod    Payment Pod   Inventory Pod
```

Kubernetes can provide:

- service discovery
- scaling
- rolling deployments
- health checks
- self-healing
- load balancing

---

## 19. Microservices vs Monolith

| Feature | Monolith | Microservices |
|---|---|---|
| Deployment | Whole application | Individual services |
| Scaling | Usually whole app | Per service |
| Database | Often shared | Usually service-owned |
| Communication | In-process | Network |
| Failure isolation | Lower | Higher |
| Complexity | Lower initially | Higher |
| Deployment independence | Low | High |
| Technology flexibility | Lower | Higher |
| Distributed transactions | Easier | Difficult |
| Debugging | Easier | More difficult |
| Operational overhead | Lower | Higher |

---

## 20. The biggest misconception

Microservices are not automatically better.

You should not do:

```
Small application
     ↓
50 microservices
     ↓
Kubernetes
     ↓
Kafka
     ↓
Distributed tracing
     ↓
Huge operational complexity
```

when a simple monolith would solve the problem.

A better approach is:

```
Start simple
     ↓
Understand domain boundaries
     ↓
Identify scaling/failure/team boundaries
     ↓
Extract services where justified
```

---

## 21. Microservices architecture — interview view

For system design, think about microservices in these layers:

```
                    CLIENT
                       │
                       ▼
                 API GATEWAY
                       │
        ┌──────────────┼───────────────┐
        ▼              ▼               ▼
   Order Service  Payment Service  User Service
        │              │               │
        ▼              ▼               ▼
    Order DB       Payment DB        User DB
        │
        ▼
      Kafka
        │
   ┌────┼─────────┐
   ▼    ▼         ▼
Payment Inventory Notification
```

Then ask:

### Communication

- REST or gRPC?
- Synchronous or asynchronous?
- Kafka?

### Data

- Who owns the data?
- Database per service?
- Consistency requirements?

### Reliability

- Timeout?
- Retry?
- Circuit breaker?
- Bulkhead?
- Idempotency?

### Scalability

- Which service is the bottleneck?
- Horizontal scaling?
- Load balancing?
- Caching?

### Transactions

- Saga?
- Outbox?
- Eventual consistency?

### Operations

- Logging?
- Metrics?
- Distributed tracing?
- Health checks?

### Deployment

- Docker?
- Kubernetes?
- Rolling deployment?
- Canary deployment?

---

## 22. Microservices learning roadmap

Since you're building system design from scratch, I'd learn Microservices in this order:

```
1. What are Microservices?
        ↓
2. Monolith vs Microservices
        ↓
3. Service Decomposition
        ↓
4. Bounded Context / DDD
        ↓
5. Synchronous Communication
        ↓
6. Asynchronous Communication
        ↓
7. API Gateway
        ↓
8. Service Discovery
        ↓
9. Database per Service
        ↓
10. Distributed Transactions
        ↓
11. Saga Pattern
        ↓
12. Transactional Outbox
        ↓
13. Event-Driven Microservices
        ↓
14. Resilience Patterns
        ↓
15. Idempotency
        ↓
16. Distributed Caching
        ↓
17. Observability
        ↓
18. Security
        ↓
19. Docker
        ↓
20. Kubernetes
        ↓
21. Deployment Strategies
        ↓
22. Microservices System Design
```

The key idea to remember:

> Microservices trade local simplicity for independent scalability, deployment, ownership, and failure isolation—but introduce distributed-systems complexity.

