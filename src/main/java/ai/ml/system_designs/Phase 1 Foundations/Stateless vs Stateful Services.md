# Stateless vs Stateful Services

This is a very important concept in system design, especially when you start talking about horizontal scaling, load balancers, microservices, caching, and high availability.

The simplest way to remember it:

- **Stateless service** → does not remember previous requests.
- **Stateful service** → remembers information from previous requests.

---

## 1. What is "State"?

State means **information that needs to be remembered between requests**.

For example, imagine you log into an e-commerce application.

You send:

```
POST /login

username = kaushal
password = ****
```

The server authenticates you.

Now you send:

```
GET /profile
```

The server needs to know:

> "Who is making this request?"

That information — the fact that you are authenticated — is **state**.

Other examples of state:

- User logged in
- Shopping cart
- Current game position
- Payment transaction status
- Order status
- Uploaded file progress
- WebSocket connection
- User session

---

## 2. Stateful Service

A stateful service stores information about the client/session and uses it in subsequent requests.

For example:

```
Client
   |
   | Login
   ↓
┌──────────────┐
│   Server     │
│              │
│ sessionId →  │
│ user=Kaushal │
└──────────────┘
```

The server remembers:

```
sessionId = ABC123
user = Kaushal
loggedIn = true
```

Later:

```
Client
   |
   | sessionId=ABC123
   ↓
Same Server
```

The server looks up its stored session and knows who the user is.

That's **stateful behavior**.

---

## 3. Stateless Service

A stateless service doesn't rely on information stored in the server's local memory from previous requests.

**Each request contains everything necessary to process it.**

For example:

**Request 1**

```
GET /profile

Authorization: Bearer <token>
```

Then:

**Request 2**

```
GET /orders

Authorization: Bearer <token>
```

The server doesn't need to remember:

> "Was this user here before?"

It can validate the token and process the request.

---

## 4. Simple Analogy

Think about a restaurant waiter.

### Stateful waiter

The waiter remembers:

> "Table 5 ordered butter chicken and naan."

Later you say:

> "Bring me another one."

The waiter knows what "one" means because they remember your previous order.

That's **stateful**.

### Stateless waiter

You say:

> "Bring me another butter chicken."

The waiter doesn't need to remember anything.

Every request contains the necessary information.

That's **stateless**.

---

## 5. Why Stateless Services Matter for Horizontal Scaling

This is where it connects directly to the previous topic.

Suppose we have:

```
                 Load Balancer
                /      |      \
               ↓       ↓       ↓
             App1    App2    App3
```

A user makes:

```
Request 1 → App1
```

Then:

```
Request 2 → App3
```

Then:

```
Request 3 → App2
```

If the application is **stateless**, this is perfectly fine.

Each server can independently process the request.

```
Request
   ↓
Any Server
   ↓
Response
```

---

## 6. Stateful Problem

Now imagine App1 stores the user's session in its local memory.

```
                 Load Balancer
                /      |      \
               ↓       ↓       ↓
             App1    App2    App3
              |
              ↓
        Session in RAM
        user = Kaushal
```

Request 1:

```
User → App1
```

App1 knows:

```
user = Kaushal
```

But request 2 goes to App3:

```
User → App3
```

App3 says:

> "I don't know this user."

Because the session exists only inside App1's memory.

**This is one of the major problems with stateful application servers.**

---

## 7. Sticky Sessions

One solution is **sticky sessions**.

The load balancer tries to keep a user connected to the same server.

```
                 Load Balancer
                /      |      \
               ↓       ↓       ↓
             App1    App2    App3
              ↑
              |
           User A
```

So:

```
User A → App1
User A → App1
User A → App1
```

And:

```
User B → App2
User B → App2
```

This can make stateful applications work with multiple servers.

But there are drawbacks.

---

## 8. Problem with Sticky Sessions

Suppose:

```
User A → App1
```

and App1 crashes.

```
App1 ❌
```

The user's session may be lost.

Also, traffic can become unbalanced.

Imagine:

```
App1 → 10,000 users
App2 → 1,000 users
App3 → 500 users
```

The load balancer can't freely distribute requests.

**This defeats some of the benefits of horizontal scaling.**

---

## 9. Better Approach: Externalize State

A common architecture is:

```
                Load Balancer
               /      |      \
              ↓       ↓       ↓
            App1    App2    App3
              \       |      /
               \      |     /
                    Redis
                      |
                  Database
```

The application servers themselves remain **stateless**.

State is stored **externally**.

For example:

```
App1 ─┐
App2 ─┼──→ Redis
App3 ─┘
```

Now:

```
Request 1 → App1
              ↓
            Redis

Request 2 → App3
              ↓
            Redis
```

App3 can retrieve the same state.

---

## 10. Example: Shopping Cart

Consider an e-commerce application.

You add:

- iPhone
- Laptop
- Headphones

Your cart is **state**.

### Bad architecture

```
             Load Balancer
                  |
            ┌─────┼─────┐
            ↓     ↓     ↓
           App1  App2  App3
            ↑
            |
        Cart in RAM
```

If your first request goes to App1:

```
Add iPhone → App1
```

App1 stores:

```
Cart = [iPhone]
```

Next request:

```
Add Laptop → App3
```

App3 doesn't know about the iPhone.

**Problem.**

---

## 11. Better Architecture

Store the cart in a shared datastore:

```
                 Load Balancer
                /      |      \
               ↓       ↓       ↓
             App1    App2    App3
                \      |      /
                 \     |     /
                    Redis
```

Now:

```
Add iPhone
   ↓
App1
   ↓
Redis
   ↓
Cart = [iPhone]
```

Next request:

```
Add Laptop
   ↓
App3
   ↓
Redis
   ↓
Cart = [iPhone, Laptop]
```

**It doesn't matter which application server receives the request.**

---

## 12. Stateless Doesn't Mean "No State Exists"

This is a very important distinction.

A stateless service does **not** mean the entire system has no state.

It means:

> **The service does not depend on locally stored state from previous requests.**

For example:

```
                 ┌───────────────┐
                 │ Stateless API │
                 └───────┬───────┘
                         |
               ┌─────────┼─────────┐
               ↓         ↓         ↓
             Redis     Database   Kafka
```

The application service is stateless.

But the overall system absolutely has state.

The state is stored in **dedicated systems**.

---

## 13. Authentication Example

This is one of the most common interview examples.

### Stateful authentication

```
Login
  ↓
Server
  ↓
Create session
  ↓
Store session in server memory
```

Example:

```
sessionId = ABC123

Server memory:
ABC123 → user=Kaushal
```

Every subsequent request uses:

```
sessionId=ABC123
```

The server retrieves the session.

### Stateless authentication

Instead, the server gives the client a token.

For example:

```
Login
  ↓
Authentication Service
  ↓
JWT
  ↓
Client
```

The client sends:

```
Authorization: Bearer <JWT>
```

to subsequent requests.

The server validates the token.

It doesn't need to maintain a local session.

---

## 14. Stateful vs Stateless Authentication

| | Stateful | Stateless |
|---|---|---|
| Session stored | Server-side | Usually encoded in token |
| Server remembers client | Yes | No |
| Horizontal scaling | More difficult | Easier |
| Sticky sessions | May be used | Usually unnecessary |
| Server memory | Session storage | Minimal session storage |
| Failure handling | More complicated | Easier |
| Common example | Server-side session | JWT |

However, JWT isn't automatically better. Large tokens, revocation, expiration, and security considerations need to be handled carefully.

---

## 15. Stateful Services Are Sometimes Necessary

Don't conclude:

> "Stateful services are bad."

That's incorrect.

Some systems naturally need state.

### Examples:

#### Databases

```
Database
   ↓
Stores persistent state
```

Obviously stateful.

#### Redis

```
Redis
   ↓
Stores cached/session/state data
```

Stateful.

#### Kafka

Kafka stores messages and their offsets.

```
Producer
   ↓
Kafka
   ↓
Consumer
```

Kafka is stateful infrastructure.

#### WebSocket Server

Suppose a user establishes:

```
Client
   │
   │ WebSocket connection
   ↓
Server
```

The server maintains an active connection.

That connection itself represents state.

---

## 16. Stateful vs Stateless Architecture

### Stateful application architecture

```
             Load Balancer
            /      |      \
           ↓       ↓       ↓
         App1    App2    App3
          ↑
          |
     Local Session
```

The application servers have client-specific state.

### Stateless application architecture

```
             Load Balancer
            /      |      \
           ↓       ↓       ↓
         App1    App2    App3
            \      |      /
             \     |     /
              Redis / DB
```

Application servers don't own the state.

State is **externalized**.

---

## 17. Why Stateless Is Preferred for Microservices

Imagine:

```
Order Service
Payment Service
Inventory Service
Notification Service
```

Each service might have multiple instances:

```
Order Service
 ├── Instance 1
 ├── Instance 2
 └── Instance 3

Payment Service
 ├── Instance 1
 └── Instance 2
```

If services are stateless, instances can be:

- Added
- Removed
- Restarted
- Replaced
- Moved
- Scaled

without losing client session state.

This works extremely well with:

- Containers
- Kubernetes
- Auto-scaling
- Load balancers
- Cloud infrastructure

---

## 18. Stateless and Auto Scaling

Suppose:

**Normal traffic:**

```
App1
App2
```

**Traffic increases:**

```
App1
App2
App3
App4
App5
```

Because the application is stateless, new instances can immediately start handling requests.

```
                  Load Balancer
                       |
       ┌───────────────┼───────────────┐
       ↓               ↓               ↓
     App1            App2            App3
                       +
                      App4
                       +
                      App5
```

This is one of the biggest reasons modern cloud architectures favor **stateless application services**.

---

## 19. Stateless vs Stateful — Comparison

| Feature | Stateless | Stateful |
|---|---|---|
| Remembers previous requests locally | ❌ | ✅ |
| Local session/state | Usually no | Usually yes |
| Horizontal scaling | Easy | More difficult |
| Load balancing | Simple | More complicated |
| Server failure | Easier recovery | State may be lost |
| Auto scaling | Easier | More complicated |
| Sticky sessions | Usually unnecessary | May be required |
| State stored externally | Common | Not necessarily |
| Example | REST API | Database |
| Best for | Application/business logic | Persistent/connection state |

---

## 20. Real-World Architecture

A typical scalable architecture might look like:

```
                         Users
                           |
                           ↓
                    ┌─────────────┐
                    │Load Balancer│
                    └──────┬──────┘
                           |
              ┌────────────┼────────────┐
              ↓            ↓            ↓
           App Server   App Server   App Server
           Stateless    Stateless    Stateless
              |            |            |
              └────────────┼────────────┘
                           |
             ┌─────────────┼─────────────┐
             ↓             ↓             ↓
           Redis        Database       Kafka
          Stateful      Stateful      Stateful
```

This is a very common pattern:

> **Keep compute/application instances stateless and move persistent/shared state into systems designed to store state.**

---

## 21. Interview Question

**Interviewer:**

> "Why do you prefer stateless services?"

**A strong answer:**

> "Stateless services make horizontal scaling and load balancing easier because any instance can handle any request. If an instance fails, another instance can process the request without depending on state stored in the failed instance. Shared or persistent state can be externalized to systems such as a database or distributed cache."

---

## 22. One Important Caveat

Stateless doesn't mean:

> "Every request must be completely independent."

A request can still interact with state:

```
Request
   ↓
Stateless Service
   ↓
Redis / Database
   ↓
State
```

The important point is:

> **State belongs to shared/persistent storage — not to one specific application instance.**

---

## 23. Mental Model

Remember these three diagrams:

### Stateful

```
Request
   ↓
Server
   ↓
LOCAL STATE
```

### Stateless

```
Request
   ↓
ANY SERVER
   ↓
Shared State Store
```

### Scalable System

```
                    Load Balancer
                   /      |      \
                  ↓       ↓       ↓
               Stateless Stateless Stateless
                 App       App       App
                  \         |        /
                   \        |       /
                    ┌──────────────┐
                    │ Shared State │
                    │ Redis / DB   │
                    └──────────────┘
```

### The key relationship with your previous topic:

```
Horizontal Scaling
        ↓
Multiple Application Instances
        ↓
Requests can hit ANY instance
        ↓
Therefore...
        ↓
Stateless Services are much easier
        ↓
Shared state → Redis / DB / other storage
```

So when you see "horizontal scaling", immediately think "stateless application servers". **This connection is fundamental to system design.**
