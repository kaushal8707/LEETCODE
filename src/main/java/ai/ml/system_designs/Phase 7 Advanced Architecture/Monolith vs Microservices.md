# Monolith vs Microservices

For system design interviews, this is a fundamental comparison. The important thing is not simply saying "monolith is simple and microservices are scalable"—you should understand why the trade-offs exist.

---

## 1. What is a Monolith?

A monolithic application is an application where most or all business functionality is packaged and deployed as one unit.

Example: E-commerce application.

```
                    Client
                      │
                      ▼
              ┌─────────────────┐
              │   E-Commerce    │
              │    Monolith     │
              │                 │
              │  User           │
              │  Product        │
              │  Order          │
              │  Payment        │
              │  Inventory      │
              │  Notification   │
              └────────┬────────┘
                       │
                       ▼
                    Database
```

Inside the application:

```
E-Commerce Application
│
├── User Module
├── Product Module
├── Order Module
├── Payment Module
├── Inventory Module
└── Notification Module
```

Even though these are separate modules, they are typically deployed together.

---

## 2. What are Microservices?

In microservices, the application is split into independently deployable services, generally around business capabilities.

```
                       Client
                         │
                         ▼
                  ┌─────────────┐
                  │ API Gateway │
                  └──────┬──────┘
                         │
       ┌─────────────────┼──────────────────┐
       │                 │                  │
       ▼                 ▼                  ▼
 ┌──────────┐      ┌──────────┐      ┌───────────┐
 │  Order   │      │ Payment  │      │ Inventory │
 │ Service  │      │ Service  │      │  Service  │
 └────┬─────┘      └────┬─────┘      └─────┬─────┘
      │                 │                  │
      ▼                 ▼                  ▼
  Order DB          Payment DB        Inventory DB
```

Each service can potentially be:

- developed independently
- deployed independently
- scaled independently
- monitored independently
- owned by a different team

---

## 3. The simplest difference

### Monolith

```
              ONE APPLICATION
                    │
       ┌────────────┼────────────┐
       │            │            │
     Order       Payment      Inventory
       │            │            │
       └────────────┼────────────┘
                    │
                 Database
```

### Microservices

```
              MULTIPLE SERVICES

 Order Service ─────► Order DB

 Payment Service ───► Payment DB

 Inventory Service ─► Inventory DB
```

The fundamental difference is deployment and ownership boundaries.

---

## 4. Deployment

This is one of the biggest differences.

### Monolith

Suppose you change Payment functionality:

```
Change Payment
      ↓
Build entire application
      ↓
Test entire application
      ↓
Deploy entire application
```

Even if only Payment changed, the whole application is normally deployed.

### Microservices

```
Change Payment
      ↓
Build Payment Service
      ↓
Test Payment Service
      ↓
Deploy Payment Service
```

Other services don't necessarily need to be redeployed.

---

## 5. Scaling

Suppose your application has:

```
Order       → 100 requests/sec
Payment     → 100 requests/sec
Search      → 10,000 requests/sec
```

### Monolith

You might have:

```
             Monolith
          /      |      \
         /       |       \
       Node     Node     Node
```

You scale the entire application even though Search is the real bottleneck.

### Microservices

You can scale Search independently:

```
Order Service
     │
     └── 3 instances

Payment Service
     │
     └── 3 instances

Search Service
     │
     └── 20 instances
```

This is one of the major benefits of microservices.

---

## 6. Database

### Monolith

A common design is:

```
              Application
                   │
        ┌──────────┴──────────┐
        │                     │
    Order Module         Payment Module
        │                     │
        └──────────┬──────────┘
                   ▼
              Shared DB
```

All modules may access the same database.

### Microservices

A common principle is:

```
Order Service ─────► Order DB

Payment Service ───► Payment DB

Inventory Service ─► Inventory DB
```

This gives each service data ownership.

---

## 7. Communication

### Monolith

Modules can communicate through normal method calls:

```
orderService.createOrder();
```

This is an in-process call.

There is usually no network involved.

### Microservices

Services communicate over a network:

```
Order Service
      │
      │ HTTP / gRPC
      ▼
Payment Service
```

or asynchronously:

```
Order Service
      │
      ▼
    Kafka
      │
      ▼
Payment Service
```

This introduces network-related problems:

- latency
- timeouts
- connection failures
- retries
- duplicate messages
- partial failures

---

## 8. Failure Handling

This is where microservices become significantly more complicated.

### Monolith

```
Order Module
     │
     ▼
Payment Module
     │
     ▼
Database
```

Everything is within the same process in a typical monolith.

### Microservices

```
Order
  │
  ▼
Payment
  │
  ▼
External Payment Gateway
```

Now there are multiple failure points.

For example:

```
Payment Service
       ↓
Payment Gateway
       ↓
Timeout
```

If you don't handle the timeout correctly:

```
Payment slow
      ↓
Order waits
      ↓
Threads/connections consumed
      ↓
Order becomes slow
      ↓
More requests wait
      ↓
System becomes overloaded
```

This is a cascading failure.

Microservices therefore often need:

- Timeout
- Retry
- Exponential Backoff
- Circuit Breaker
- Bulkhead
- Rate Limiting
- Fallback

---

## 9. Transactions

### Monolith

Suppose:

```
Create Order
Update Inventory
Record Payment
```

If everything uses one database, you can potentially use a single database transaction:

```
BEGIN TRANSACTION

Create Order
Update Inventory
Record Payment

COMMIT
```

If something fails:

```
ROLLBACK
```

This is relatively straightforward.

### Microservices

Now you might have:

```
Order DB
Payment DB
Inventory DB
```

You cannot simply use one normal local transaction across all of them.

You may need:

- Saga
- Transactional Outbox
- Compensating Transactions
- Eventual Consistency

This is one of the biggest complexities introduced by microservices.

---

## 10. Technology Choice

### Monolith

Usually one application stack:

```
Java
Spring Boot
PostgreSQL
```

### Microservices

Potentially:

```
Order       → Java
Payment     → Java
Search      → Go
Notification → Python
```

This provides flexibility.

However:

> Technology independence is a benefit, not a reason to create microservices.

Using 10 different programming languages can create enormous operational complexity.

---

## 11. Team Structure

### Monolith

A team might own the entire application:

```
             Team
              │
       ┌──────┼──────┐
       ▼      ▼      ▼
     Order  Payment Inventory
```

As the organization grows, teams can interfere with each other.

### Microservices

You can organize ownership:

```
Order Team
     │
     └── Order Service

Payment Team
     │
     └── Payment Service

Inventory Team
     │
     └── Inventory Service
```

This enables independent development.

---

## 12. Development Complexity

### Monolith

Initially:

```
Simple
  ↓
One codebase
  ↓
One deployment
  ↓
One application
```

Generally easier to:

- develop
- debug
- test locally
- deploy
- understand

### Microservices

You might have:

```
20 Services
   ↓
20 deployments
   ↓
20 logs
   ↓
20 configurations
   ↓
Multiple databases
   ↓
Kafka
   ↓
API Gateway
   ↓
Service discovery
   ↓
Distributed tracing
```

Operational complexity increases significantly.

---

## 13. Debugging

### Monolith

```
Request
   ↓
Application
   ↓
Database
```

You can usually inspect one application log.

### Microservices

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

You need distributed tracing.

For example:

```
Trace ID = ABC123

Gateway       ABC123
Order         ABC123
Payment       ABC123
Inventory     ABC123
Notification  ABC123
```

Without good observability, debugging can become difficult.

---

## 14. Availability

Microservices can provide better fault isolation when designed correctly.

Suppose:

```
Order Service       → UP
Payment Service     → UP
Inventory Service   → UP
Notification        → DOWN
```

You may still be able to process the order and retry the notification later.

But this is not automatic.

A poorly designed microservices system can actually be less reliable than a monolith because it has many network dependencies.

---

## 15. Complete Comparison

| Area | Monolith | Microservices |
|---|---|---|
| Architecture | Single application | Multiple services |
| Deployment | Usually whole application | Service independently |
| Scaling | Usually whole application | Per service |
| Communication | Method calls | Network/API/events |
| Database | Often shared | Usually service-owned |
| Transactions | Relatively simple | Distributed transactions are difficult |
| Failure handling | Simpler | More complex |
| Debugging | Easier | Distributed tracing needed |
| Development | Simpler initially | More complex |
| Operations | Easier | More infrastructure |
| Team ownership | Often shared | Service/team ownership |
| Technology | Usually standardized | Can vary |
| Deployment speed | Can become slower | Independent deployments |
| Fault isolation | Lower | Potentially higher |
| Infrastructure cost | Lower | Higher |
| Initial development | Faster | Slower |
| Long-term scaling | Can become difficult | Better for large systems |

---

## 16. Real-world example

Imagine Amazon-like e-commerce.

### Monolith

```
                   E-Commerce
                       │
        ┌──────────────┼──────────────┐
        │              │              │
      Order         Payment        Inventory
        │              │              │
        └──────────────┼──────────────┘
                       ▼
                    DB
```

If traffic increases dramatically for product search:

```
Search traffic ↑↑↑
       ↓
Entire application needs scaling
```

### Microservices

```
                       API Gateway
                           │
       ┌───────────┬───────┼────────┬───────────┐
       ▼           ▼       ▼        ▼           ▼
     User        Order   Payment  Inventory    Search
     Service     Service Service  Service     Service
       │           │       │        │           │
       ▼           ▼       ▼        ▼           ▼
     User DB     Order DB Payment  Inventory  Search
                            DB       DB          DB
```

If Search receives huge traffic:

```
Search Service
      │
      ├── Instance 1
      ├── Instance 2
      ├── Instance 3
      ├── ...
      └── Instance 20
```

Other services don't necessarily need to scale.

---

## 17. When should you choose a Monolith?

A monolith is often a good choice when:

- the application is small
- the team is small
- the domain is not well understood
- requirements change frequently
- operational simplicity is important
- you don't have a strong need for independent scaling
- you want to move quickly initially

A modular monolith is particularly useful:

```
             Modular Monolith
                    │
       ┌────────────┼────────────┐
       ▼            ▼            ▼
    Order        Payment      Inventory
    Module       Module        Module
```

Modules are kept cleanly separated even though they are deployed together.

This can make future extraction into microservices easier.

---

## 18. When should you choose Microservices?

Microservices become more attractive when you have:

- large engineering teams
- clearly defined business domains
- different scaling requirements
- independent deployment requirements
- strong service ownership
- need for fault isolation
- large and complex systems
- mature DevOps/platform infrastructure

---

## 19. Important interview question

> "Which one is better: Monolith or Microservices?"

The correct answer is:

> Neither is universally better. It depends on the problem.

A good interview answer:

```
Start with a modular monolith when the system and
domain are relatively simple or still evolving.

Move toward microservices when independent scaling,
deployment, team ownership, fault isolation, or domain
boundaries justify the additional distributed-system
complexity.
```

---

## 20. The most important trade-off

Remember this:

```
             MONOLITH
                 │
       Less operational complexity
                 │
                 ▼
        Easier to develop/debug
                 │
                 │
             TRADE-OFF
                 │
                 ▼
      Harder independent scaling
      Harder independent deployment
      Tighter coupling as it grows


           MICROSERVICES
                 │
                 ▼
       Independent deployment
       Independent scaling
       Service ownership
       Fault isolation
                 │
                 │
             TRADE-OFF
                 │
                 ▼
       Distributed-system complexity
       Network failures
       Data consistency problems
       Observability complexity
       Operational overhead
```

---

## One-line interview summary

> Monolith optimizes for simplicity; microservices optimize for independent scalability, deployment, ownership, and isolation—at the cost of distributed-system complexity.

