# Monolith vs Distributed

Monolithic system = application is built/deployed as one unit.
Distributed system = work is spread across multiple independent machines/processes that communicate over a network.

There is one important nuance: monolithic and distributed are not strict opposites. A monolithic application can have multiple instances running on different servers, making the deployment distributed.

---

## 1. Basic Difference

### Monolithic

```
                    Client
                      |
                      ↓
              ┌───────────────┐
              │   Monolith    │
              │               │
              │ User          │
              │ Product       │
              │ Order         │
              │ Payment       │
              │ Inventory     │
              └───────┬───────┘
                      |
                      ↓
                   Database
```

All major functionality is inside one application.

### Distributed

```
                       Client
                         |
                         ↓
                    API Gateway
                         |
          ┌──────────────┼──────────────┐
          ↓              ↓              ↓
     User Service   Order Service   Payment Service
          |              |              |
          ↓              ↓              ↓
       Database       Database       Database
```

Different components run independently and communicate over a network.

---

## 2. Side-by-Side Comparison

| Aspect | Monolithic | Distributed |
|---|---|---|
| Application | One application | Multiple components/processes |
| Deployment | Usually one deployment unit | Components can be independently deployed |
| Communication | Mostly in-process calls | Network calls |
| Scaling | Often scale entire application | Can scale individual components |
| Failure | Failure can affect entire instance | Partial failures are common |
| Database | Often shared DB | May have multiple DBs |
| Development | Simpler initially | More complex |
| Deployment | Simpler | More complicated |
| Debugging | Easier | Distributed tracing/logging needed |
| Network dependency | Low inside application | High |
| Latency | Lower for internal calls | Network latency exists |
| Transactions | Relatively easy | Distributed transactions are harder |
| Infrastructure | Less | More |
| Best suited | Small/medium systems | Large/complex systems |

---

## 3. Example: E-Commerce

Suppose we build an Amazon-like application.

We have:

- User
- Product
- Order
- Payment
- Inventory
- Notification

### Monolithic

All are inside one application:

```
┌──────────────────────────────────┐
│       E-Commerce Application     │
│                                  │
│ User                             │
│ Product                          │
│ Order                            │
│ Payment                          │
│ Inventory                        │
│ Notification                     │
└────────────────┬─────────────────┘
                 ↓
              Database
```

If you deploy the application:

```
ecommerce.jar
```

everything goes together.

### Distributed

We split the system:

```
                 E-Commerce System

       ┌──────────┼──────────┬──────────┐
       ↓          ↓          ↓          ↓
     User      Product      Order     Payment
    Service    Service     Service    Service
                              |
                              ↓
                         Inventory
                          Service
```

These services can run on separate machines.

For example:

```
User Service     → Server 1
Product Service  → Server 2
Order Service    → Server 3
Payment Service  → Server 4
Inventory        → Server 5
```

They communicate over HTTP/gRPC/messaging.

---

## 4. Scaling Difference

Suppose Product Search gets huge traffic:

```
Product Search = 10,000 requests/sec

Order          = 500 requests/sec
Payment        = 200 requests/sec
```

### Monolithic

You generally replicate the whole application:

```
             Load Balancer
                   |
        ┌──────────┼──────────┐
        ↓          ↓          ↓
     Monolith   Monolith   Monolith
       #1         #2         #3
```

Each server contains:

```
User
Product
Order
Payment
Inventory
```

You're scaling everything.

### Distributed

You can scale only Product Service:

```
Product Service
      |
 ┌────┼────┬────┐
 ↓    ↓    ↓    ↓
 P1   P2   P3   P4
```

while:

```
Payment Service
      |
      ↓
     P1
```

may be enough.

This is one of the biggest benefits of distributed architecture.

---

## 5. Communication Difference

This is very important.

### Monolithic

Suppose Order needs Payment.

```
paymentService.processPayment();
```

This is typically an in-process method call.

```
Order
  |
  ↓
Payment
```

No network is necessarily involved.

### Distributed

Order Service needs Payment Service:

```
Order Service
      |
      | HTTP / gRPC
      ↓
Payment Service
```

Now the network is involved.

Therefore, you have to worry about:

- Network latency
- Network failure
- Timeout
- Retry
- Circuit breaker
- Serialization
- Authentication

---

## 6. Failure Difference

### Monolithic

Suppose Payment has a serious memory problem:

```
┌──────────────────────────┐
│ Monolith                 │
│                          │
│ User        ✅           │
│ Product     ✅           │
│ Order       ✅           │
│ Payment     ❌           │
│ Inventory   ✅           │
└──────────────────────────┘
```

If the entire application instance crashes:

```
User        ❌
Product     ❌
Order       ❌
Payment     ❌
Inventory   ❌
```

### Distributed

Suppose Payment Service goes down:

```
User Service       ✅
Product Service    ✅
Order Service      ✅
Payment Service    ❌
Inventory Service  ✅
```

The other services can potentially continue working.

But remember: this requires proper fault isolation. A distributed system doesn't automatically provide fault tolerance.

---

## 7. Database Difference

### Monolithic

Often:

```
                 Monolith
                    |
                    ↓
               One Database
```

For example:

```
users
products
orders
payments
inventory
```

This makes transactions relatively straightforward.

### Distributed

You might have:

```
User Service
     ↓
 User DB

Order Service
     ↓
 Order DB

Payment Service
     ↓
 Payment DB

Inventory Service
     ↓
 Inventory DB
```

Now things become more complicated.

Suppose an order requires:

1. Create Order
2. Charge Payment
3. Reduce Inventory

What if:

```
Order       ✅
Payment     ✅
Inventory   ❌
```

You now have to handle a distributed transaction / consistency problem.

---

## 8. Development Complexity

### Monolithic

A developer may start:

```
Spring Boot
     +
MySQL
```

That's relatively simple.

### Distributed

You may have:

```
API Gateway
     |
     ├── User Service
     ├── Product Service
     ├── Order Service
     ├── Payment Service
     └── Inventory Service

Kafka
Redis
Multiple Databases
Service Discovery
Monitoring
Distributed Tracing
```

Much more infrastructure.

So:

> Distributed architecture gives you scalability and independence, but introduces significant complexity.

---

## 9. Real-Time Analogy

Imagine a restaurant.

### Monolithic Restaurant

One kitchen prepares everything:

```
             ONE KITCHEN
        ┌──────────────────┐
        │ Pizza            │
        │ Burger           │
        │ Pasta            │
        │ Dessert          │
        └──────────────────┘
```

Simple to manage.

### Distributed Restaurant

Separate specialized kitchens:

```
Pizza Kitchen
      ↓
Pizza

Burger Kitchen
      ↓
Burger

Dessert Kitchen
      ↓
Dessert
```

Now each kitchen can independently scale.

But coordination becomes harder.

---

## 10. One Very Important Interview Point

Don't say:

> "Monolithic means one server, distributed means multiple servers."

That's not always correct.

For example:

```
             Load Balancer
                  |
        ┌─────────┼─────────┐
        ↓         ↓         ↓
    Monolith  Monolith  Monolith
     Server 1  Server 2  Server 3
```

This is still a monolithic application, because each instance contains the entire application.

But it is also distributed across multiple servers.

So the better distinction is:

### Monolithic

One application/deployment unit.

### Distributed

Multiple independent processes/machines cooperating through a network.

---

## 11. The Most Important Differences

If you remember only these 6 points, you're good for now:

```
MONOLITHIC
─────────────────────────────
One application
One deployment unit
In-process communication
Usually shared database
Simpler
Scale whole application


DISTRIBUTED
─────────────────────────────
Multiple independent components
Potentially independent deployments
Network communication
Potentially multiple databases
More complex
Can scale components independently
```

And the architectural evolution often looks like:

```
Simple Monolith
      ↓
Modular Monolith
      ↓
Distributed System
      ↓
Microservices
```

**Important:** A distributed system is the broader concept. Microservices are one way of designing a distributed system.
