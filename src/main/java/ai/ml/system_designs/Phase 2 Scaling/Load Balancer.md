# Load Balancer

A Load Balancer is one of the most important components in System Design because it connects directly with Horizontal Scaling.

You already saw:

```
Horizontal Scaling
        ↓
Add multiple servers
```

But once we have multiple servers, the next question is:

> **Who decides which server should handle each request?**

That's the job of a **Load Balancer**.

---

## 1. What is a Load Balancer?

A Load Balancer is a component that receives incoming requests and distributes them across multiple backend servers.

```
                    Users
                  /   |   \
                 /    |    \
                v     v     v
             Requests
                  |
                  v
          +----------------+
          | Load Balancer  |
          +----------------+
             /     |      \
            /      |       \
           v       v        v
       Server 1 Server 2 Server 3
```

### Simple definition

> A Load Balancer distributes incoming traffic across multiple servers to improve scalability, availability, and reliability.

---

## 2. Why Do We Need a Load Balancer?

Suppose your application has three servers:

```
+---------+   +---------+   +---------+
| Server 1|   | Server 2|   | Server 3|
+---------+   +---------+   +---------+
```

A client sends:

```
GET /users/123
```

Which server should process it?

Without a load balancer, the client would need to know the individual server addresses.

That's inconvenient and creates operational problems.

Instead:

```
Client
  |
  v
Load Balancer
  |
  +----> Server 1
  |
  +----> Server 2
  |
  +----> Server 3
```

The client only knows the Load Balancer's address.

---

## 3. Real-World Example

Imagine an e-commerce application.

You have:

```
100,000 users
```

and three application servers:

```
App Server 1
App Server 2
App Server 3
```

Architecture:

```
                         Users
                           |
                           v
                    Load Balancer
                    /      |      \
                   /       |       \
                  v        v        v
               App 1     App 2     App 3
```

Requests might be distributed like:

```
Request 1 → App 1
Request 2 → App 2
Request 3 → App 3
Request 4 → App 1
Request 5 → App 2
Request 6 → App 3
```

This prevents one server from receiving all the traffic.

---

## 4. Load Balancer Does More Than Distribution

A good load balancer typically performs several functions:

```
                 Load Balancer
                       |
        +--------------+--------------+
        |              |              |
   Distribution    Health Check    Routing
        |              |              |
   Which server?   Is it alive?   Where to send?
```

It can also provide:

- TLS termination
- Connection management
- Traffic routing
- Health checks
- Failover
- Rate limiting in some architectures
- Security integration

---

## 5. How Does It Know Which Server to Choose?

This is where **Load Balancing Algorithms** come in.

There are several common algorithms.

---

## 6. Round Robin

The simplest algorithm.

Suppose:

```
Server 1
Server 2
Server 3
```

Requests are distributed sequentially:

```
Request 1 → Server 1
Request 2 → Server 2
Request 3 → Server 3
Request 4 → Server 1
Request 5 → Server 2
Request 6 → Server 3
```

Diagram:

```
             Load Balancer
                  |
      +-----------+-----------+
      |           |           |
      v           v           v
     S1          S2          S3
      ↑           ↑           ↑
      |           |           |
      +-----------+-----------+
        Repeat sequence
```

**Advantage**

Very simple.

**Problem**

It assumes servers can handle approximately similar workloads.

Suppose:

```
Server 1 → 16 CPU
Server 2 → 8 CPU
Server 3 → 4 CPU
```

Sending the same number of requests to all three may not be optimal.

---

## 7. Weighted Round Robin

We can assign different weights.

For example:

```
Server 1 → Weight 5
Server 2 → Weight 3
Server 3 → Weight 2
```

Then roughly:

```
S1 → 50%
S2 → 30%
S3 → 20%
```

Useful when servers have different capacities.

---

## 8. Least Connections

The load balancer sends the request to the server currently handling the fewest active connections.

Example:

```
Server 1 → 20 connections
Server 2 → 5 connections
Server 3 → 12 connections
```

Next request:

```
             Load Balancer
                   |
                   v
             Server 2
          (fewest connections)
```

This can be useful when requests have significantly different processing times.

---

## 9. IP Hash

The load balancer calculates a hash based on the client's IP address.

Conceptually:

```
hash(clientIP) % numberOfServers
```

For example:

```
Client A → Server 1
Client B → Server 3
Client C → Server 2
```

The same client IP tends to go to the same server as long as the backend set remains compatible with the hashing strategy.

This can help with session affinity, but it has limitations.

---

## 10. Health Checks

This is extremely important.

Suppose we have:

```
                 Load Balancer
                 /      |      \
                v       v       v
              S1       S2       S3
              ✅       ❌       ✅
```

Server 2 has crashed.

The load balancer performs health checks.

For example:

```
GET /health
```

Server 2 doesn't respond.

The load balancer marks it as unhealthy:

```
S1 → Healthy
S2 → Unhealthy
S3 → Healthy
```

Now:

```
Requests
   |
   v
Load Balancer
   |       \
   v        v
  S1        S3
```

Traffic is no longer sent to S2.

---

## 11. What is a Health Check?

A health check is a request used to determine whether a server is capable of receiving traffic.

For a Spring Boot application, you might expose a health endpoint through Spring Boot Actuator.

Conceptually:

```
GET /health
```

Response:

```json
{
  "status": "UP"
}
```

But there's an important distinction:

### Liveness

Is the application process alive?

### Readiness

Is the application actually ready to receive traffic?

For example, an application may be running but unable to connect to its database.

```
Application Process → UP
Database Connection → DOWN
```

In that case, a readiness check can prevent the load balancer from sending normal traffic to that instance.

---

## 12. Load Balancer + Horizontal Scaling

Now the connection between the two concepts becomes clear.

### Without Load Balancer

```
              Users
             / |  \
            /  |   \
           ?   ?    ?
          S1   S2   S3
```

The client needs to know where to send requests.

### With Load Balancer

```
                Users
                  |
                  v
           +-------------+
           |Load Balancer|
           +-------------+
            /     |     \
           v      v      v
          S1     S2     S3
```

The client only interacts with the load balancer.

---

## 13. Load Balancer + Stateless Services

This is a very important System Design combination.

Suppose:

```
Client
  |
  v
Load Balancer
  |
  +----> Server 1
  |
  +----> Server 2
```

**Request 1:**

```
Login → Server 1
```

**Request 2:**

```
Get Profile → Server 2
```

If Server 1 stored the session only in its local memory:

```
Server 1
   |
   +--- session = ABC123
```

Server 2 doesn't know about it.

This creates problems.

Therefore, horizontally scaled services are often designed to be **stateless**.

Shared state can be stored externally:

```
                 Load Balancer
                  /         \
                 v           v
              Server 1    Server 2
                 \           /
                  \         /
                   v       v
                    Redis
                      |
                      v
                   Database
```

This allows requests to move between instances more freely.

---

## 14. Load Balancer and Sticky Sessions

There is another approach called **Sticky Session** or **Session Affinity**.

Suppose:

```
Client A → Server 1
```

The load balancer tries to keep sending Client A to Server 1:

```
Client A
   |
   +---- Request 1 → Server 1
   |
   +---- Request 2 → Server 1
   |
   +---- Request 3 → Server 1
```

This can make stateful applications easier to operate.

But it has disadvantages.

If Server 1 goes down:

```
Client A
   |
   X
Server 1 DOWN
```

the session may be lost unless the session state is replicated or externally stored.

Therefore, in modern distributed architectures, stateless services are generally preferred over relying heavily on sticky sessions.

---

## 15. Layer 4 vs Layer 7 Load Balancing

This is a common System Design interview question.

There are two major categories:

```
Layer 4 Load Balancer
Layer 7 Load Balancer
```

### Layer 4 — Transport Layer

Works primarily with:

- IP
- TCP
- UDP
- Port

It doesn't need to understand the HTTP request content.

Example:

```
Client
  |
  | TCP connection
  v
L4 Load Balancer
  |
  +----> Server 1:443
  |
  +----> Server 2:443
```

It can be very fast because it operates at the transport level.

---

## 16. Layer 7 — Application Layer

Layer 7 understands application protocols such as HTTP.

It can inspect things like:

- HTTP Method
- URL
- Headers
- Host
- Cookies

For example:

```
GET /orders/123
```

The load balancer can route based on the URL.

```
                    Load Balancer
                          |
             +------------+------------+
             |                         |
             v                         v
       /orders/*                  /payments/*
             |                         |
             v                         v
       Order Service             Payment Service
```

This is very powerful.

---

## 17. Example of Layer 7 Routing

Suppose you have:

```
https://example.com/orders
https://example.com/payments
https://example.com/users
```

A Layer 7 load balancer could route:

```
/orders/*     → Order Service
/payments/*   → Payment Service
/users/*      → User Service
```

Architecture:

```
                         Client
                           |
                           v
                   +---------------+
                   | L7 Load       |
                   | Balancer      |
                   +---------------+
                    /      |      \
                   /       |       \
                  v        v        v
              Order     Payment    User
             Service     Service   Service
```

---

## 18. Reverse Proxy vs Load Balancer

These concepts are closely related and often confused.

### Reverse Proxy

A reverse proxy sits between clients and backend servers.

```
Client
  |
  v
Reverse Proxy
  |
  v
Backend
```

It can provide:

- TLS termination
- Routing
- Caching
- Compression
- Security controls
- Request filtering

### Load Balancer

A load balancer specifically focuses on distributing traffic across multiple backend instances.

```
Client
  |
  v
Load Balancer
  |
  +----> Server 1
  +----> Server 2
  +----> Server 3
```

In practice, many technologies can perform both reverse-proxy and load-balancing functions.

---

## 19. Load Balancer as a Single Point of Failure?

Excellent interview question.

You might have:

```
             Users
               |
               v
         Load Balancer
          /     |     \
         v      v      v
        S1     S2     S3
```

What if the load balancer itself crashes?

Then:

```
Users
  |
  X
Load Balancer DOWN
```

The whole application could become inaccessible.

Therefore, production systems generally make the load-balancing layer highly available, for example with redundant instances or a managed load-balancing service.

Conceptually:

```
                 Users
                   |
                   v
             +-----------+
             | LB System |
             |  HA setup |
             +-----------+
              /         \
             v           v
           LB-1        LB-2
             \           /
              \         /
               v       v
              App Servers
```

The exact implementation depends on the infrastructure.

---

## 20. Load Balancer + Database

A common architecture looks like:

```
                         Users
                           |
                           v
                    Load Balancer
                    /     |     \
                   v      v      v
                 App1   App2   App3
                   \      |      /
                    \     |     /
                     v    v    v
                    Database
```

But remember:

> **The load balancer doesn't automatically solve database scaling.**

If the database becomes the bottleneck:

```
App1 ─┐
App2 ─┤
App3 ─┼──> Database 🔥
App4 ─┤
App5 ─┘
```

you need database-specific strategies such as:

- Caching
- Read Replicas
- Partitioning
- Sharding
- Connection Pooling

---

## 21. Example: E-Commerce System

Let's put everything together.

Suppose we are designing Amazon-like architecture.

```
                           Users
                             |
                             v
                           DNS
                             |
                             v
                    +----------------+
                    | Load Balancer  |
                    +----------------+
                     /       |       \
                    /        |        \
                   v         v         v
                App 1      App 2      App 3
                   |         |         |
                   +---------+---------+
                             |
                  +----------+----------+
                  |                     |
                  v                     v
                Redis                Database
                  |                     |
                  |                  +--+--+
                  |                  |     |
                  |                  v     v
                  |               Primary Replica
                  |
                  v
                 Cache
```

Traffic flow:

```
User
 ↓
DNS
 ↓
Load Balancer
 ↓
Healthy Application Instance
 ↓
Cache / Database
 ↓
Response
 ↓
User
```

---

## 22. What Happens When Traffic Increases?

Initially:

```
                 LB
              /      \
            App1     App2
```

Traffic increases.

Auto-scaling adds another instance:

```
                 LB
            /      |      \
          App1    App2    App3
```

Traffic increases again:

```
                 LB
         /     /    \     \
       App1  App2  App3   App4
```

This is:

```
Horizontal Scaling
        +
Load Balancing
        +
Auto Scaling
```

These three concepts frequently work together.

---

## 23. What Happens When a Server Crashes?

**Before:**

```
                LB
             /  |  \
            v   v   v
           S1  S2  S3
           ✅  ✅  ✅
```

**S2 crashes:**

```
                LB
             /  |  \
            v   X   v
           S1  S2  S3
           ✅  ❌  ✅
```

**Health check detects:**

```
S2 = Unhealthy
```

**Traffic becomes:**

```
                LB
               /  \
              v    v
             S1    S3
```

The user may not even notice the server failure.

That's the power of load balancing + health checks + multiple instances.

---

## 24. Important Interview Questions

You should be comfortable answering these:

### Q1. Why do we need a Load Balancer?

> To distribute incoming traffic across multiple backend instances and improve scalability, availability, and fault tolerance.

### Q2. What is Round Robin?

> Requests are distributed sequentially across backend servers.

### Q3. What is Least Connections?

> Send traffic to the server with the fewest active connections.

### Q4. What happens when a server goes down?

> Health checks detect the failure and the load balancer stops routing traffic to that server.

### Q5. L4 vs L7?

> L4 operates primarily using transport-level information such as IP, TCP/UDP and ports; L7 understands application-level information such as HTTP paths, headers and cookies.

### Q6. Why are stateless services preferred?

> Because any healthy instance can handle a request, making horizontal scaling and failover much easier.

### Q7. Can a Load Balancer itself fail?

> Yes. Production systems use highly available/redundant load-balancing infrastructure to avoid making it a single point of failure.

---

## 25. The Big Picture

You have now learned three concepts that fit together:

```
                Increased Traffic
                       |
                       v
              Horizontal Scaling
                       |
                       v
              Multiple Servers
                       |
                       v
                Load Balancer
                       |
          +------------+------------+
          |            |            |
          v            v            v
       Server 1     Server 2     Server 3
          |            |            |
          +------------+------------+
                       |
                       v
                  Shared State
                 /            \
              Redis          Database
```

The mental model to remember is:

> **Horizontal Scaling** adds servers.
> **Load Balancer** distributes traffic among those servers.
> **Health Checks** remove unhealthy servers from traffic.
> **Stateless Services** make those servers interchangeable.
> **Auto Scaling** dynamically adds/removes servers based on demand.
