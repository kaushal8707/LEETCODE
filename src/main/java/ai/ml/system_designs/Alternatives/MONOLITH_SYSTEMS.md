# Monolithic System — Detailed Explanation

A monolithic system is an application where most or all of the application's major business functionality is developed, deployed, and run as one single application unit.

The word **monolithic** comes from **monolith**, meaning one large, unified piece.

Since you're learning System Design from scratch, it's useful to understand monolithic architecture before moving to microservices.

---

## 1. Simple Definition

Imagine we are building an E-commerce application.

It has:

- User Management
- Product Catalog
- Order Management
- Payment
- Inventory
- Notifications

In a monolithic architecture, all of these modules are inside one application:

```
                    E-Commerce Application
                 ┌──────────────────────────┐
                 │                          │
                 │   User Management        │
                 │                          │
                 │   Product Catalog        │
                 │                          │
                 │   Order Management       │
                 │                          │
                 │   Payment                │
                 │                          │
                 │   Inventory              │
                 │                          │
                 │   Notification            │
                 │                          │
                 └──────────────────────────┘
                           │
                           ↓
                       Database
```

You might have different packages/classes/modules internally, but they are ultimately part of one deployable application.

For example, in Spring Boot:

```
ecommerce.jar
```

You deploy this one application.

---

## 2. Real-Time Example

Let's take Amazon-like e-commerce as an example.

Suppose the application has these functionalities:

```
User
 ├── Registration
 ├── Login
 └── Profile

Product
 ├── Search
 ├── Product Details
 └── Categories

Order
 ├── Create Order
 ├── Cancel Order
 └── Order History

Payment
 ├── Pay
 ├── Refund
 └── Payment Status

Inventory
 ├── Check Stock
 └── Update Stock
```

In a monolithic system:

```
                   Client
                     |
                     ↓
             Load Balancer
                     |
                     ↓
          ┌─────────────────────┐
          │   Monolithic App    │
          │                     │
          │ User                │
          │ Product             │
          │ Order               │
          │ Payment             │
          │ Inventory           │
          │ Notification        │
          └──────────┬──────────┘
                     |
                     ↓
                  Database
```

The application could internally have a clean separation:

```
src/main/java

user/
product/
order/
payment/
inventory/
notification/
```

But they are still deployed together.

---

## 3. Monolithic Does NOT Mean "Bad Code"

This is very important.

A common misunderstanding is:

> Monolithic = badly designed application

That's not necessarily true.

You can have a well-structured monolith.

For example:

```
Monolithic Application

├── User Module
├── Product Module
├── Order Module
├── Payment Module
└── Inventory Module
```

Each module can have clear boundaries.

This is often called a **modular monolith**.

So:

```
Monolith
   ≠
Bad Architecture
```

A monolith can actually be a very good choice for many systems.

---

## 4. How a Request Works

Suppose the user places an order.

The client sends:

```
POST /orders
```

The request enters the monolithic application:

```
Client
  |
  | POST /orders
  ↓
Monolithic Application
  |
  ↓
Order Controller
  |
  ↓
Order Service
  |
  ├── Check User
  |
  ├── Check Inventory
  |
  ├── Process Payment
  |
  └── Create Order
  |
  ↓
Database
```

Everything happens inside the same application/process.

The modules communicate through **method calls**, rather than network calls.

For example:

```
orderService.createOrder();
```

may internally call:

```
inventoryService.checkStock();
paymentService.processPayment();
notificationService.sendNotification();
```

These are typically **in-process calls**.

---

## 5. Monolith vs Microservices

This is where System Design becomes interesting.

### Monolith

```
                 Application
        ┌─────────────────────────┐
        │ User                    │
        │ Product                 │
        │ Order                   │
        │ Payment                │
        │ Inventory              │
        └─────────────────────────┘
```

One application.

### Microservices

The same functionality might be separated:

```
                 ┌→ User Service
                 │
Client → Gateway ├→ Product Service
                 │
                 ├→ Order Service
                 │
                 ├→ Payment Service
                 │
                 └→ Inventory Service
```

Now each service is independently deployable.

---

## 6. Deployment Difference

This is one of the biggest differences.

### Monolith

Suppose you change only the Payment module.

You modify:

```
Payment
```

But you generally build and deploy the whole application:

```
User
Product
Order
Payment ← changed
Inventory
Notification

       ↓

Build entire application

       ↓

ecommerce.jar

       ↓

Deploy
```

### Microservices

You could deploy only:

```
Payment Service
```

without deploying:

```
User Service
Product Service
Order Service
Inventory Service
```

This gives microservices greater **deployment independence**.

---

## 7. Scaling Problem

Suppose your application has:

```
User
Product
Order
Payment
Inventory
```

And suddenly the Product Search functionality receives huge traffic.

For example:

```
Product Search = 10,000 requests/sec

Everything else = 100 requests/sec
```

In a traditional monolith, you generally scale the whole application:

```
                 Load Balancer
                      |
          ┌───────────┼───────────┐
          ↓           ↓           ↓
      Monolith     Monolith    Monolith
      Instance 1   Instance 2   Instance 3
```

Even though only Product Search needs additional capacity, you're replicating:

```
User
Product
Order
Payment
Inventory
```

every time.

That's potentially inefficient.

---

## 8. Microservices Scaling

With microservices:

```
Product Service
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

For example:

```
                  Product Service
                  ┌────┬────┬────┐
                  ↓    ↓    ↓    ↓
                 P1   P2   P3   P4
```

while:

```
Payment Service
       ↓
      P1
```

might be enough.

---

## 9. Another Real-Time Example — Banking

Imagine a banking application.

Features:

- Customer
- Accounts
- Money Transfer
- Payments
- Loans
- Notifications
- Reports

A monolithic architecture could look like:

```
                     Banking Application
              ┌────────────────────────────┐
              │                            │
              │ Customer Module             │
              │ Account Module              │
              │ Transfer Module             │
              │ Payment Module              │
              │ Loan Module                 │
              │ Notification Module         │
              │ Reporting Module            │
              │                            │
              └─────────────┬──────────────┘
                            ↓
                         Database
```

Suppose the bank changes the Loan calculation logic.

You might have to build and deploy the complete application.

---

## 10. What Happens If One Module Has a Problem?

This is another important characteristic.

Suppose:

```
Payment Module
```

has a serious memory leak.

Because everything runs in the same application:

```
                 Monolith
       ┌─────────────────────────┐
       │ User                    │
       │ Product                 │
       │ Order                  │
       │ Payment ← problem       │
       │ Inventory              │
       └─────────────────────────┘
```

The problem can potentially affect the entire application.

For example:

```
Payment memory leak
       ↓
Memory consumption increases
       ↓
Application becomes unhealthy
       ↓
Entire instance may crash
       ↓
User/Product/Order APIs also affected
```

This is called **failure coupling**.

---

## 11. Microservices Isolation

With microservices:

```
User Service
Product Service
Order Service
Payment Service ← crash
Inventory Service
```

If Payment Service crashes:

```
Payment Service
      ❌
```

the other services can potentially continue operating.

Of course, this doesn't mean microservices automatically eliminate failures. A dependency failure can still propagate if the system isn't designed properly.

---

## 12. Database in a Monolith

A monolithic application often uses a shared database:

```
               Monolithic Application
        ┌──────────────────────────────┐
        │ User                         │
        │ Product                      │
        │ Order                        │
        │ Payment                      │
        │ Inventory                    │
        └──────────────┬───────────────┘
                       |
                       ↓
                  Single DB
```

For example:

```
users
products
orders
payments
inventory
```

All modules can access the same database.

This makes transactions relatively straightforward.

For example:

```
Create Order
     ↓
Update Inventory
     ↓
Create Payment
```

You can potentially use a single database transaction:

```sql
BEGIN TRANSACTION

Create Order
Update Inventory
Create Payment

COMMIT
```

That's one of the advantages of a monolith.

---

## 13. Advantages of Monolithic Architecture

### 1. Simple to develop

Initially, you have one application:

```
One Repository
One Application
One Deployment
```

Less infrastructure is required.

### 2. Easy local development

A developer can start:

```
Spring Boot Application
        +
Database
```

and have almost everything available.

With many microservices, local development can become:

```
User Service
Product Service
Order Service
Payment Service
Inventory Service
Kafka
Redis
Databases
API Gateway
...
```

Much more infrastructure.

### 3. Simple communication

Inside a monolith:

```
orderService.createOrder();
```

This is an in-process call.

In microservices, you might have:

```
Order Service
      |
      | HTTP/gRPC
      ↓
Inventory Service
```

Now you have network-related concerns:

- Network latency
- Timeouts
- Retries
- Circuit breakers
- Service discovery
- Authentication
- Serialization

### 4. Easier transactions

Monolith:

```
Order
 +
Payment
 +
Inventory
```

can potentially participate in one database transaction.

Distributed microservices transactions are considerably more complicated.

### 5. Easier debugging

In a monolith:

```
Request
 ↓
Controller
 ↓
Service
 ↓
Repository
 ↓
Database
```

Everything is usually within one application.

In microservices:

```
Client
 ↓
API Gateway
 ↓
Order Service
 ↓
Inventory Service
 ↓
Payment Service
 ↓
Notification Service
```

Debugging requires distributed tracing and centralized logging.

---

## 14. Disadvantages of Monolithic Architecture

### 1. Large codebase

As the application grows:

```
Small Monolith
      ↓
Medium Monolith
      ↓
Large Monolith
      ↓
Very Large Monolith
```

It can become difficult to understand and maintain.

### 2. Deployment coupling

A small change in one module may require:

```
Build
 ↓
Test
 ↓
Deploy
```

the entire application.

### 3. Scaling coupling

If only one module requires more capacity, you generally scale the whole application.

### 4. Failure coupling

A problem in one part can potentially affect the whole application.

### 5. Technology coupling

Suppose your entire application is Java + Spring Boot.

Introducing another technology for only one business area can be harder.

With microservices:

```
Order Service     → Java
Recommendation    → Python
Payment Service   → Java
Analytics         → Python
```

Each service can potentially use a different technology stack.

---

## 15. Is Monolith Bad?

**No.**

This is one of the most important things to understand.

You should not start every project by thinking:

> "We need microservices."

A better approach is:

> Start with the simplest architecture that satisfies the requirements.

For a small or medium application:

```
Client
   ↓
Monolith
   ↓
Database
```

may be completely sufficient.

As requirements grow:

```
Monolith
   ↓
Modular Monolith
   ↓
Potentially extract services
```

This can be a very sensible evolution.

---

## 16. Real-World Analogy

Imagine a restaurant.

### Monolithic restaurant

One kitchen handles everything:

```
             Restaurant
                  |
        ┌─────────┼─────────┐
        ↓         ↓         ↓
      Pizza     Burger    Dessert
        |         |         |
        └─────────┼─────────┘
                  ↓
              One Kitchen
```

Everything is in one place.

If the kitchen is well organized, this works very well.

But imagine the restaurant becomes extremely large.

Now:

```
Pizza orders       → huge
Burger orders      → moderate
Dessert orders     → huge
```

You might create specialized kitchens.

That's analogous to splitting a monolith into services.

---

## 17. Monolith → Microservices Evolution

This is the mental model I recommend remembering:

```
                 START
                   ↓
             Simple Monolith
                   ↓
          Modular Monolith
                   ↓
       Identify scaling/boundary
              problems
                   ↓
       Extract specific modules
                   ↓
             Microservices
```

Don't start with:

> "Microservices are modern, therefore everything must be a microservice."

Instead ask:

- Do we actually need independent deployment?
- Do we need independent scaling?
- Do we need fault isolation?
- Do teams need independent ownership?
- Do different components have different scalability requirements?

If the answer is mostly no, a monolith can be an excellent architecture.

---

## 18. Interview Definition

If an interviewer asks:

> What is a monolithic architecture?

You can answer:

> A monolithic architecture is a software architecture in which multiple business capabilities such as user management, orders, payments, and inventory are implemented within a single application and deployed as a single unit. The components may be logically separated into modules, but they typically share the same application runtime and often the same database. It provides simplicity in development, deployment, communication, and transactions, but can create challenges around independent scaling, deployment, and fault isolation as the system grows.

---

## The Key Picture to Remember

```
                    CLIENT
                       |
                       ↓
                ┌─────────────┐
                │   MONOLITH  │
                │             │
                │ User        │
                │ Product     │
                │ Order       │
                │ Payment     │
                │ Inventory   │
                │ Notification│
                │             │
                └──────┬──────┘
                       |
                       ↓
                    DATABASE
```

**One application → One deployment unit → Usually one runtime → Often shared database.**

That is the core idea of a monolithic system.
