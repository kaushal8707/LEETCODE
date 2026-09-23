# Distributed System

Since you're learning System Design from scratch, this is a very important concept because almost every large-scale system—banking, e-commerce, Netflix-like streaming, payment systems, etc.—uses distributed-system concepts.

---

## 1. What is a Distributed System?

A distributed system is a system where multiple independent computers/processes work together over a network to provide a single application or service.

In simple words:

> Instead of doing everything on one machine, we distribute the work across multiple machines that communicate with each other over a network.

For example:

```
                  Client
                    |
                    ↓
              Load Balancer
                    |
          ┌─────────┼─────────┐
          ↓         ↓         ↓
       Server 1  Server 2  Server 3
          |         |         |
          └─────────┼─────────┘
                    ↓
                 Database
```

From the user's perspective, this may look like one application.

Internally, however, multiple machines are working together.

---

## 2. Why Do We Need Distributed Systems?

Imagine your application initially runs on one server:

```
                Client
                  |
                  ↓
             Application
                  |
                  ↓
               Database
```

Suppose that server can handle:

```
10,000 requests/sec
```

But your application grows and now receives:

```
100,000 requests/sec
```

One server isn't enough.

So we add more servers:

```
                    Client
                      |
                      ↓
                Load Balancer
                      |
          ┌───────────┼───────────┐
          ↓           ↓           ↓
       Server 1    Server 2    Server 3
          ↓           ↓           ↓
          └───────────┼───────────┘
                      ↓
                   Database
```

Now the workload is distributed.

That's one of the simplest forms of a distributed system.

---

## 3. Distributed System vs Single Server

### Single System

```
Client
  |
  ↓
┌──────────────┐
│   Server     │
│              │
│ Application  │
└──────┬───────┘
       ↓
    Database
```

Everything depends heavily on one server.

### Distributed System

```
                    Client
                      |
                      ↓
                Load Balancer
                      |
            ┌─────────┼─────────┐
            ↓         ↓         ↓
        Server 1  Server 2  Server 3
            |         |         |
            └─────────┼─────────┘
                      ↓
                   Database
```

Multiple machines participate in processing requests.

---

## 4. Real-Time Example — Netflix

Think about a Netflix-like application.

It has many responsibilities:

- User
- Movie Catalog
- Search
- Recommendations
- Video Streaming
- Payment
- Subscription
- Notifications

You don't necessarily want one machine to do everything.

You could have:

```
                 Client
                   |
                   ↓
              API Gateway
                   |
       ┌───────────┼────────────┐
       ↓           ↓            ↓
   User Service  Search      Catalog
                   Service      Service
       ↓           ↓            ↓
       └───────────┼────────────┘
                   |
            Recommendation
                Service
                   |
                   ↓
             Streaming System
```

These components may run on different machines.

They communicate over a network.

That's a distributed system.

---

## 5. Real-Time Example — E-Commerce

Let's use an Amazon-like system.

You have:

- User
- Product
- Order
- Payment
- Inventory
- Notification

A distributed architecture could look like:

```
                         Client
                           |
                           ↓
                      API Gateway
                           |
       ┌───────────────────┼──────────────────┐
       ↓                   ↓                  ↓
  User Service       Product Service      Order Service
                                               |
                                  ┌────────────┼────────────┐
                                  ↓            ↓            ↓
                            Inventory      Payment     Notification
                             Service        Service       Service
```

Each service can potentially run on separate machines.

For example:

```
User Service
   ↓
Server 1
Server 2

Product Service
   ↓
Server 3
Server 4
Server 5

Payment Service
   ↓
Server 6
Server 7
```

The entire system is distributed across machines.

---

## 6. Distributed System Does NOT Necessarily Mean Microservices

This distinction is very important.

You might think:

> Distributed system = Microservices

❌ Not exactly.

Microservices are one architectural approach for building distributed systems.

For example:

```
Distributed System
       |
       ├── Microservices
       |
       ├── Distributed Database
       |
       ├── Distributed Cache
       |
       ├── Distributed Queue
       |
       └── Distributed Storage
```

Even a monolithic application can be deployed across multiple machines:

```
             Load Balancer
                   |
       ┌───────────┼───────────┐
       ↓           ↓           ↓
   Monolith 1  Monolith 2  Monolith 3
```

This is still a distributed deployment.

The application itself is monolithic, but multiple instances communicate through the network.

---

## 7. Key Characteristic: Network Communication

This is one of the biggest differences from a simple monolithic application.

In a monolith:

```
paymentService.processPayment();
```

The method call happens inside the same application/process.

In a distributed system:

```
Order Service
      |
      | HTTP / gRPC
      ↓
Payment Service
```

The request travels through a network.

For example:

```
POST /payments
```

The Payment Service processes it and sends a response.

This introduces new problems.

---

## 8. Network Failure

This is one of the biggest challenges in distributed systems.

Suppose:

```
Order Service
      |
      | HTTP
      ↓
Payment Service
```

What happens if the network fails?

```
Order Service
      |
      | X
      ↓
Payment Service
```

The Order Service doesn't know immediately whether:

- Payment Service is down
- Network is down
- Request is still processing
- Payment succeeded but response was lost

This creates complexity that doesn't exist in the same way inside a single process.

---

## 9. Partial Failure

This is a fundamental distributed-system concept.

Imagine:

```
User Service       ✅
Product Service    ✅
Order Service      ✅
Payment Service    ❌
Notification       ✅
```

Only one component failed.

The entire system is partially working.

This is called **partial failure**.

In a distributed system, you have to design for this.

For example:

```
Order
  |
  ↓
Payment Service
  |
  X
  ↓
Timeout
```

What should Order Service do?

Possible strategies:

- Retry
- Timeout
- Circuit breaker
- Return a pending status
- Put the request into a queue
- Compensating transaction

---

## 10. Real-Time Payment Example

Suppose you pay ₹1,000.

```
Client
  |
  ↓
Order Service
  |
  ↓
Payment Service
```

Payment Service processes:

```
₹1,000 deducted
```

But then the network connection fails before the response reaches Order Service.

```
Order Service
      |
      | Payment Request
      ↓
Payment Service
      |
      | ₹1,000 deducted
      X
      |
Network failure
```

Order Service doesn't receive:

```
SUCCESS
```

It might think:

```
Payment failed
```

But actually:

```
Payment succeeded!
```

This is one of the reasons distributed systems are difficult.

You need mechanisms such as:

- Idempotency
- Unique transaction IDs
- Retry-safe APIs
- Transaction status queries
- Event-driven processing

---

## 11. Distributed Database

The database itself can also be distributed.

Instead of:

```
Application
     |
     ↓
   DB 1
```

you might have:

```
              Application
                   |
           ┌───────┼───────┐
           ↓       ↓       ↓
         DB 1    DB 2    DB 3
```

Data may be distributed across multiple machines.

This helps with:

- Scalability
- Availability
- Fault tolerance
- Geographic distribution

But it also introduces problems like:

- Replication
- Consistency
- Network partitions
- Conflict resolution

---

## 12. Distributed Cache

Suppose you have Redis-like caching.

Instead of one cache:

```
Application
    |
    ↓
 Redis
```

you might have:

```
              Application
                   |
          ┌────────┼────────┐
          ↓        ↓        ↓
       Cache 1  Cache 2  Cache 3
```

The cache is distributed across machines.

Again:

> Multiple independent machines cooperate to provide one logical capability.

---

## 13. Distributed Queue

Suppose your application receives:

```
100,000 requests/sec
```

Instead of processing everything immediately:

```
Producer
   |
   ↓
Message Queue
   |
   ↓
Consumers
```

The queue itself might be distributed:

```
             Message Queue
       ┌─────────┬─────────┐
       ↓         ↓         ↓
   Broker 1   Broker 2   Broker 3
```

Kafka is a common example of a distributed messaging platform.

---

## 14. Distributed System and Scalability

One major reason for distributing a system is horizontal scaling.

Suppose one server can process:

```
10,000 requests/sec
```

and you need:

```
100,000 requests/sec
```

Instead of buying one extremely powerful server, you can use multiple servers:

```
10 servers × 10,000 requests/sec
=
100,000 requests/sec
```

Architecture:

```
                    Load Balancer
                         |
       ┌─────────┬───────┼───────┬─────────┐
       ↓         ↓       ↓       ↓         ↓
      S1        S2      S3      S4        S5
```

This is **horizontal scaling**.

---

## 15. Distributed System and Availability

Suppose you have one server:

```
Server
  ↓
❌
```

Your entire application goes down.

But with multiple servers:

```
             Load Balancer
                   |
          ┌────────┼────────┐
          ↓        ↓        ↓
         S1       S2       S3
          ❌       ✅       ✅
```

S1 failed.

But S2 and S3 can continue serving traffic.

This provides **fault tolerance** and **high availability**.

---

## 16. Geographic Distribution

Large systems may also distribute servers geographically.

For example:

```
                 Users
                   |
              Global DNS
                   |
       ┌───────────┼───────────┐
       ↓           ↓           ↓
    Mumbai      Singapore    London
    Region       Region      Region
```

A user in India can potentially be served from a nearby region.

Benefits include:

- Lower latency
- Higher availability
- Disaster recovery
- Better global performance

---

## 17. CAP Theorem

Once you understand distributed systems, you'll encounter the CAP theorem.

CAP stands for:

**C — Consistency**

Every node sees the same data.

**A — Availability**

Every request receives a response.

**P — Partition Tolerance**

The system continues operating despite network communication failures between nodes.

The important distributed-system problem is that network partitions can happen, and during a partition you have to make trade-offs between consistency and availability.

For example:

```
        Node A
          |
          X
      Network
      Partition
          X
          |
        Node B
```

Now Node A and Node B cannot communicate.

What should the system do?

```
Option 1:
Reject some requests → favor consistency

Option 2:
Continue accepting requests → favor availability
```

This is a central concept in distributed systems.

---

## 18. Distributed System vs Monolithic System

Let's compare them.

| Monolithic | Distributed |
|---|---|
| Usually one application | Multiple machines/processes cooperate |
| Often one deployment unit | Components may be independently deployed |
| In-process calls | Network communication |
| Simpler initially | More complex |
| Failure often affects application instance | Partial failures are common |
| Easier transactions | Distributed transactions are harder |
| Scaling can be limited | Easier horizontal scaling |
| Easier debugging | Distributed tracing often required |

But remember:

> A monolith can also be deployed on multiple machines, so "monolith" and "distributed" are not exact opposites.

---

## 19. Monolith vs Distributed Microservices

This distinction is useful:

### Monolithic Application

```
             Monolith
     ┌─────────────────────┐
     │ User                │
     │ Product             │
     │ Order               │
     │ Payment             │
     └─────────────────────┘
```

### Distributed Monolith

This is an interesting anti-pattern.

You might have:

```
User Service
     |
     ↓
Order Service
     |
     ↓
Payment Service
     |
     ↓
Inventory Service
```

but every service is tightly dependent on every other service.

You've distributed the application physically but haven't achieved good independence.

This can be worse than a well-designed monolith.

---

## 20. Real-Time Example — Food Delivery

Consider a Swiggy/Zomato-like system.

You might have:

- Customer
- Restaurant
- Order
- Payment
- Delivery
- Notification
- Location

A distributed architecture could look like:

```
                         Client
                           |
                           ↓
                      API Gateway
                           |
          ┌────────────────┼────────────────┐
          ↓                ↓                ↓
     User Service     Restaurant        Order Service
                       Service               |
                                             |
                              ┌──────────────┼──────────────┐
                              ↓              ↓              ↓
                         Payment        Delivery       Notification
                          Service         Service         Service
                                             |
                                             ↓
                                      Location Service
```

Each service may run on multiple machines.

For example:

```
Order Service
   ├── O1
   ├── O2
   └── O3

Payment Service
   ├── P1
   └── P2

Delivery Service
   ├── D1
   ├── D2
   └── D3
```

All these machines communicate through the network.

That's a distributed system.

---

## 21. What Problems Do Distributed Systems Introduce?

This is where System Design becomes challenging.

Once you distribute your system, you have to think about:

### Network Latency

```
Service A ───── network ─────→ Service B
```

The network isn't free.

### Network Failure

```
Service A ───── X ─────→ Service B
```

### Timeout

What if Service B takes 30 seconds?

```
A → B
    |
    | waiting...
    | waiting...
    | waiting...
```

You need timeouts.

### Retries

Should A retry?

```
A → B ❌
A → B ❌
A → B ✅
```

But retries can accidentally create duplicate operations.

### Duplicate Requests

For payments:

```
Payment ₹1000
      ↓
Request succeeds
      ↓
Response lost
      ↓
Client retries
      ↓
Could payment happen twice?
```

You need **idempotency**.

### Data Consistency

Two services might temporarily have different views of data.

```
Service A → Balance = ₹1,000
Service B → Balance = ₹900
```

You need to decide how and when consistency is achieved.

### Distributed Transactions

Suppose:

```
Order Service
     ↓
Payment Service
     ↓
Inventory Service
```

What happens if:

```
Order      ✅
Payment    ✅
Inventory  ❌
```

You need a strategy for handling this partial failure.

---

## 22. The Core Idea You Should Remember

Don't define a distributed system simply as:

> "A system with multiple servers."

A better definition is:

> A distributed system is a collection of independent computers or processes that communicate over a network and cooperate to provide a unified system or service.

The key words are:

```
Independent machines
        +
Network communication
        +
Coordination
        +
One logical system
```

---

## 23. One Picture to Remember

```
                         DISTRIBUTED SYSTEM

                              Client
                                |
                                ↓
                          Load Balancer
                                |
              ┌─────────────────┼─────────────────┐
              ↓                 ↓                 ↓
         Server / Service  Server / Service  Server / Service
              |                 |                 |
              └─────────────────┼─────────────────┘
                                |
                    ┌───────────┼───────────┐
                    ↓           ↓           ↓
                 Database     Cache       Message Queue
                    |
              ┌─────┼─────┐
              ↓     ↓     ↓
             DB1   DB2   DB3
```

Everything is running on different machines/processes, communicating over a network, but together they provide one system to the user.

---

## Recommended Learning Progression

The progression I recommend for your System Design learning is:

```
Monolithic System
       ↓
Distributed System
       ↓
Client-Server Architecture
       ↓
Load Balancer
       ↓
Caching
       ↓
Database Scaling
       ↓
Message Queue
       ↓
Microservices
       ↓
CAP Theorem
       ↓
Consistency & Replication
       ↓
High Availability
       ↓
Fault Tolerance
```

Once you understand distributed systems, concepts like Kafka, Redis, load balancing, database replication, microservices, CAP theorem, eventual consistency, and fault tolerance become much easier to understand.
