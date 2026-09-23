# Client–Server Architecture

Since you're starting System Design from scratch, let's build the foundation properly.

The first concept is **Client–Server Architecture** because almost every modern system—banking, e-commerce, Netflix, Uber, social media, etc.—is built around some form of client-server communication.

---

## 1. What is Client–Server Architecture?

Client–Server Architecture is an architecture where:

- **Client** → requests a service or resource.
- **Server** → receives the request, processes it, and sends a response.

The simplest picture is:

```
┌──────────────┐                 ┌──────────────┐
│    Client    │  ── Request ──► │    Server    │
│              │                 │              │
│ Browser      │ ◄── Response ── │ Application  │
│ Mobile App   │                 │ Logic        │
│ Desktop App  │                 │ Database     │
└──────────────┘                 └──────────────┘
```

### Simple example

When you open an e-commerce application:

```
Client
  │
  │ GET /products
  ▼
Server
  │
  │ Query database
  ▼
Database
  │
  │ Products
  ▼
Server
  │
  │ HTTP Response
  ▼
Client
```

The client doesn't directly access the database.

---

## 2. Who is the Client?

A client is a component that initiates a request.

**Examples:**

- Web browser
  - Chrome
  - Firefox
  - Safari
  - Edge
- Mobile application
  - Android App
  - iOS App
- Desktop application
  - Desktop Banking App
  - Desktop Email Client
- Another backend service

This is very important in microservices.

```
Order Service
      │
      │ HTTP/gRPC request
      ▼
Payment Service
```

Here:

Order Service is the **client**, and Payment Service is the **server**.

So a client doesn't necessarily mean a browser.

---

## 3. Who is the Server?

A server is a system that receives requests and provides a service.

For example:

```
Client
   │
   ▼
Web Server
   │
   ▼
Application Server
   │
   ▼
Database
```

A server might:

- Authenticate users
- Process orders
- Calculate prices
- Retrieve products
- Store data
- Process payments
- Return search results

---

## 4. Real-World Example — Amazon-like Application

Imagine you open an e-commerce application.

The architecture could look like:

```
                    CLIENT
                      │
              ┌───────┴────────┐
              │                │
           Browser          Mobile App
              │                │
              └───────┬────────┘
                      │
                  HTTP/HTTPS
                      │
                      ▼
                Load Balancer
                      │
             ┌────────┼────────┐
             ▼        ▼        ▼
          Server 1 Server 2 Server 3
             │        │        │
             └────────┼────────┘
                      │
                      ▼
                 Application
                   Logic
                      │
             ┌────────┼─────────┐
             ▼        ▼         ▼
          Database   Cache    Message Queue
```

This is still client-server architecture.

The architecture simply becomes more sophisticated as the system grows.

---

## 5. How Does a Request Work?

Suppose you want to see your orders.

You open:

```
https://example.com/orders
```

The flow is roughly:

```
1. User clicks "My Orders"
              │
              ▼
2. Browser creates request
              │
              ▼
3. DNS resolves domain → IP
              │
              ▼
4. Request reaches server
              │
              ▼
5. Server authenticates user
              │
              ▼
6. Server queries database
              │
              ▼
7. Database returns orders
              │
              ▼
8. Server creates response
              │
              ▼
9. Response goes to browser
              │
              ▼
10. Browser displays orders
```

We'll study DNS, IP, HTTP/HTTPS, TCP, load balancers, etc. individually later.

For now, remember the basic flow:

```
REQUEST

Client ──────────────────────► Server


RESPONSE

Client ◄────────────────────── Server
```

---

## 6. Client and Server Have Different Responsibilities

A major advantage is **separation of responsibilities**.

For example:

### Client

Responsible for:

- UI
- User interaction
- Displaying data
- Sending requests
- Receiving responses

### Server

Responsible for:

- Business logic
- Authentication
- Authorization
- Data processing
- Database access
- Validation

### Example:

User clicks:

> **"Buy Product"**

The client might send:

```
POST /orders
```

with:

```json
{
  "productId": 123,
  "quantity": 2
}
```

The server performs:

```
Validate request
      ↓
Check authentication
      ↓
Check product
      ↓
Check inventory
      ↓
Calculate price
      ↓
Create order
      ↓
Save to database
      ↓
Return response
```

The client shouldn't be responsible for these business operations.

---

## 7. Thin Client vs Thick Client

This is an important architectural concept.

### Thin Client

Most of the business logic lives on the server.

```
Client
  │
  │ Request
  ▼
Server
  │
  ├── Business Logic
  ├── Validation
  ├── Database
  └── Processing
```

The client primarily handles presentation and user interaction.

Examples:

- Web applications
- Browser-based applications

### Thick Client

The client performs more processing.

```
Client
 ├── UI
 ├── Business Logic
 ├── Validation
 └── Local Storage

        │
        ▼

     Server
        │
        ▼
     Database
```

Examples can include:

- Desktop applications
- Some mobile applications

Modern applications often use a combination.

---

## 8. Two-Tier Architecture

The simplest architecture is two-tier architecture.

```
┌──────────────┐
│    Client    │
└──────┬───────┘
       │
       │ Request
       ▼
┌──────────────┐
│    Server    │
│              │
│ Business     │
│ Logic        │
│              │
│ Database     │
└──────────────┘
```

The server contains both:

- Application Logic
- Database

This works for small applications.

But as systems become larger, we usually separate responsibilities.

---

## 9. Three-Tier Architecture

A common architecture is:

```
┌──────────────┐
│ Presentation │
│    Layer     │
└──────┬───────┘
       │
       ▼
┌──────────────┐
│ Application  │
│    Layer     │
└──────┬───────┘
       │
       ▼
┌──────────────┐
│ Data Layer   │
│  Database    │
└──────────────┘
```

For example:

```
Browser
   │
   ▼
Spring Boot Application
   │
   ▼
PostgreSQL
```

This should look very familiar if you've worked with Spring Boot.

---

## 10. Client–Server in Microservices

Now let's connect this to your existing microservices knowledge.

Suppose we have:

```
             Client
                │
                ▼
          API Gateway
                │
       ┌────────┼─────────┐
       ▼        ▼         ▼
     Order    Payment   User
    Service   Service   Service
       │        │         │
       ▼        ▼         ▼
    Order DB Payment DB User DB
```

Here, there are multiple client-server relationships.

For example:

**Client → API Gateway**

The client is the client.

The API Gateway is the server.

Then:

**API Gateway → Order Service**

The API Gateway becomes the client.

The Order Service becomes the server.

And:

**Order Service → Payment Service**

Order Service becomes the client.

Payment Service becomes the server.

### Important insight

> **Client and server are roles, not permanent identities.**

A component can be a client in one interaction and a server in another.

---

## 11. Synchronous Client–Server Communication

The client sends a request and **waits** for the response.

```
Client
  │
  │ Request
  ▼
Server
  │
  │ Processing
  │
  │
  ▼
Response
  │
  ▼
Client
```

Example:

```
Client → GET /users/123
          │
          ▼
       Server
          │
          ▼
Client ← User information
```

This is **synchronous communication**.

HTTP REST APIs commonly work this way.

---

## 12. Asynchronous Communication

The client doesn't necessarily wait for the final processing to finish.

For example:

```
Order Service
      │
      │ OrderCreated
      ▼
    Kafka
      │
      ├────────► Payment Service
      │
      ├────────► Inventory Service
      │
      └────────► Notification Service
```

The Order Service publishes an event and continues.

This is common in **event-driven microservices**.

You were recently studying:

- Event-driven architecture
- Kafka
- Saga
- Transactional Outbox
- Eventual consistency

All of these build on the basic client/server and communication concepts.

---

## 13. Why Do We Need Client–Server Architecture?

### 1. Separation of concerns

Client handles presentation.

Server handles business logic.

### 2. Centralized data

Instead of every client having its own database:

```
Client A ─┐
Client B ─┼──► Server ───► Database
Client C ─┘
```

The server controls access to the data.

### 3. Security

You don't want:

```
Mobile App
    │
    └──────► Direct Database Access ❌
```

Instead:

```
Mobile App
    │
    ▼
API
    │
    ▼
Database
```

The server can enforce:

- Authentication
- Authorization
- Validation
- Rate limiting
- Auditing

### 4. Scalability

You can add more servers:

```
                Load Balancer
                     │
        ┌────────────┼────────────┐
        ▼            ▼            ▼
     Server 1     Server 2     Server 3
```

This is **horizontal scaling**.

We'll study this in detail later.

---

## 14. What Happens If the Server Goes Down?

This introduces an important System Design concept:

### Single Point of Failure

Suppose:

```
Client
  │
  ▼
Server
  X
DOWN
```

The entire application becomes unavailable.

That's a **Single Point of Failure (SPOF)**.

To solve this:

```
                  Load Balancer
                       │
             ┌─────────┼─────────┐
             ▼         ▼         ▼
          Server 1  Server 2  Server 3
             ✓         ✓         ✓
```

If Server 1 fails:

```
                  Load Balancer
                       │
                  ❌ Server 1
                       │
             ┌─────────┴─────────┐
             ▼                   ▼
          Server 2            Server 3
```

Traffic can continue going to healthy servers.

This leads us to:

**Load Balancer → High Availability → Fault Tolerance**

---

## 15. Client–Server vs Peer-to-Peer

### Client–Server

```
Client A ──┐
Client B ──┼──► Server
Client C ──┘
```

Central server provides services.

### Peer-to-Peer

```
Peer A ◄────► Peer B
  ▲             ▲
  │             │
  ▼             ▼
Peer C ◄────► Peer D
```

Peers communicate directly.

Examples of P2P concepts include file-sharing networks and some decentralized systems.

Most web applications primarily use **client-server architecture**.

---

## 16. Interview Perspective

At 10 years of experience, don't stop at:

> "Client sends request and server sends response."

An interviewer may ask:

### Q1. What happens when there are millions of clients?

You should start thinking:

```
Clients
   ↓
Load Balancer
   ↓
Multiple Application Servers
   ↓
Cache
   ↓
Database
```

### Q2. What if one server fails?

```
Load Balancer
      ↓
Health Check
      ↓
Route traffic to healthy servers
```

### Q3. What if the database becomes a bottleneck?

Think:

- Caching
- Read Replicas
- Partitioning
- Sharding

### Q4. What if services need asynchronous communication?

Think:

- Kafka / Message Queue

### Q5. What if a distributed operation partially fails?

Think:

- Saga
- Retry
- Idempotency
- Transactional Outbox

This is how you should gradually move from **basic architecture → distributed system design**.

---

## 17. Complete Picture

Eventually, your mental model should become:

```
                         CLIENTS
              ┌────────────┼────────────┐
              │            │            │
           Browser      Mobile       Other API
              │            │            │
              └────────────┼────────────┘
                           │
                      DNS / HTTPS
                           │
                           ▼
                    ┌─────────────┐
                    │Load Balancer│
                    └──────┬──────┘
                           │
              ┌────────────┼────────────┐
              ▼            ▼            ▼
          App Server   App Server   App Server
              │            │            │
              └────────────┼────────────┘
                           │
                    ┌──────┴──────┐
                    │             │
                    ▼             ▼
                  Cache       Message Queue
                    │             │
                    ▼             ▼
                Database      Other Services
                    │
             ┌──────┴──────┐
             ▼             ▼
         Read Replica   Read Replica
```

Don't worry about understanding every component yet.

We'll build this architecture one block at a time.

---

## ⭐ Key Takeaways

Remember these 7 points:

1. **Client initiates a request.**
2. **Server provides a service/resource.**
3. **Client can be a browser, mobile app, or another backend service.**
4. **Client and server are roles, not fixed identities.**
5. **Communication can be synchronous or asynchronous.**
6. **Servers can be scaled horizontally to handle more traffic.**
7. **Client-server architecture is the foundation for modern distributed systems and microservices.**
