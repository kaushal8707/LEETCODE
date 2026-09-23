# Reverse Proxy

A Reverse Proxy is a server that sits between clients and backend servers and receives client requests on behalf of those backend servers.

Since you're learning System Design from scratch, it's important to understand this clearly because Reverse Proxy, Load Balancer, API Gateway, DNS, and Application Servers are often used together.

---

## 1. What is a Reverse Proxy?

Normally, a client could directly communicate with your application server:

```
Client
   |
   v
Application Server
```

With a reverse proxy:

```
Client
   |
   v
Reverse Proxy
   |
   v
Application Server
```

The client doesn't directly communicate with the backend server.

The reverse proxy receives the request and forwards it to the appropriate backend.

### Simple definition

> A Reverse Proxy is a server that accepts requests from clients and forwards those requests to backend servers on the clients' behalf.

---

## 2. Why is it called "Reverse" Proxy?

Let's first understand a normal/forward proxy.

### Forward Proxy

The proxy represents the client.

```
Client
   |
   v
Forward Proxy
   |
   v
Internet
   |
   v
Server
```

The server may not know the actual client directly.

Example:

```
Employee
   |
   v
Company Proxy
   |
   v
Internet
```

The proxy acts on behalf of the employee/client.

### Reverse Proxy

The proxy represents the server/backend.

```
Client
   |
   v
Reverse Proxy
   |
   v
Backend Server
```

The client doesn't need to know which backend server actually processes the request.

That's why:

```
Forward Proxy → represents CLIENT

Reverse Proxy → represents SERVER
```

---

## 3. Real-World Example

Imagine you have a Spring Boot application:

```
                  Internet
                     |
                     v
                  Client
                     |
                     v
                Reverse Proxy
                     |
                     v
              Spring Boot App
```

The user accesses:

```
https://example.com
```

The client doesn't necessarily know that internally the application is running at:

```
10.0.1.25:8080
```

The reverse proxy handles the internal routing.

---

## 4. Reverse Proxy with Multiple Servers

Now combine this with Horizontal Scaling.

Suppose you have:

```
              Client
                |
                v
          Reverse Proxy
           /     |     \
          v      v      v
       App 1   App 2   App 3
```

The reverse proxy can forward requests to different backend instances.

This is where the distinction with a Load Balancer becomes interesting.

A reverse proxy can perform load balancing, but the concepts are not identical.

---

## 5. Reverse Proxy vs Load Balancer

### Reverse Proxy

The primary concept is:

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

- Request routing
- TLS termination
- Caching
- Compression
- Security filtering
- Header manipulation
- Authentication integration
- Load balancing

### Load Balancer

The primary purpose is:

```
                 Load Balancer
                /      |      \
               v       v       v
             App1    App2    App3
```

Its main job is distributing traffic across backend instances.

**Important:**

> A reverse proxy can act as a load balancer, but not every reverse proxy is necessarily being used as a load balancer.

---

## 6. Example: NGINX

A common reverse proxy is NGINX.

Conceptually:

```
Client
   |
   | HTTPS
   v
NGINX
   |
   +------> Spring Boot :8080
```

NGINX receives:

```
GET /orders/123
```

and forwards it internally:

```
GET /orders/123
Host: internal-app
```

The backend processes the request and sends the response back through NGINX.

**Flow:**

```
Client
  |
  | Request
  v
NGINX
  |
  | Forward
  v
Spring Boot
  |
  | Response
  v
NGINX
  |
  | Response
  v
Client
```

---

## 7. Reverse Proxy Hides Backend Servers

Suppose internally you have:

```
App 1 → 10.0.1.10:8080
App 2 → 10.0.1.11:8080
App 3 → 10.0.1.12:8080
```

The client only sees:

```
https://api.example.com
```

Architecture:

```
                       Internet
                          |
                          v
                   api.example.com
                          |
                          v
                    Reverse Proxy
                          |
              +-----------+-----------+
              |           |           |
              v           v           v
          10.0.1.10  10.0.1.11  10.0.1.12
             App1       App2       App3
```

This provides abstraction between the public interface and the internal infrastructure.

---

## 8. Reverse Proxy and TLS Termination

This is one of the most common uses.

Suppose the client communicates using HTTPS:

```
Client
   |
   | HTTPS
   v
Reverse Proxy
   |
   | HTTP or HTTPS
   v
Application
```

The reverse proxy can terminate the TLS connection.

Conceptually:

```
Client
   |
   | HTTPS
   v
+----------------+
| Reverse Proxy  |
| TLS Termination|
+----------------+
        |
        | HTTP
        v
+----------------+
| Spring Boot    |
+----------------+
```

The reverse proxy handles:

- TLS Handshake
- Certificate
- Encryption/Decryption

while the application focuses on business logic.

In some environments, traffic from the proxy to the application is also encrypted:

```
Client
  |
 HTTPS
  v
Reverse Proxy
  |
 HTTPS
  v
Application
```

---

## 9. Why Terminate TLS at the Reverse Proxy?

Imagine 100 application instances:

```
Client
  |
 HTTPS
  v
Reverse Proxy
  |
  +---- App1
  +---- App2
  +---- App3
  ...
  +---- App100
```

Instead of managing public TLS certificates and TLS configuration independently across every application instance, you can centralize much of that edge configuration at the reverse-proxy layer.

This can simplify:

- Certificate management
- TLS configuration
- Security policies
- Routing

---

## 10. Reverse Proxy for Routing

A reverse proxy can route requests based on URL paths.

Suppose you have:

```
/api/orders
/api/payments
/api/users
```

You can route:

```
/api/orders/*
       ↓
Order Service

/api/payments/*
       ↓
Payment Service

/api/users/*
       ↓
User Service
```

Architecture:

```
                         Client
                           |
                           v
                    Reverse Proxy
                     /     |     \
                    /      |      \
                   v       v       v
             Order      Payment    User
             Service    Service    Service
```

This is particularly useful in microservice architectures.

---

## 11. Reverse Proxy for Different Domains

You can also route based on hostname.

For example:

```
api.example.com
     ↓
API servers

admin.example.com
     ↓
Admin application

static.example.com
     ↓
Static content
```

Architecture:

```
                         Internet
                            |
                            v
                     Reverse Proxy
                     /      |      \
                    /       |       \
                   v        v        v
              API App   Admin App  Static
```

---

## 12. Reverse Proxy for Caching

A reverse proxy can cache frequently requested responses.

**Without caching:**

```
Client
  |
  v
Reverse Proxy
  |
  v
Application
  |
  v
Database
```

**With caching:**

```
Client
  |
  v
Reverse Proxy
  |
  +---- Cache HIT → Response
  |
  +---- Cache MISS
           |
           v
       Application
           |
           v
        Database
```

For static or cacheable content, this can reduce backend traffic.

---

## 13. Reverse Proxy for Compression

Suppose the application returns a large JSON response:

```
Application
     |
     | 10 MB
     v
Reverse Proxy
     |
     | Compress
     v
Client
     |
     | 2 MB
```

The reverse proxy can compress responses when appropriate.

This reduces:

- Network bandwidth
- Response size
- Transfer time

---

## 14. Reverse Proxy for Security

A reverse proxy can act as an additional security layer.

For example:

```
Internet
   |
   v
Reverse Proxy
   |
   +-- Rate limiting
   +-- Request filtering
   +-- IP filtering
   +-- TLS
   +-- Security headers
   |
   v
Application
```

This means the application servers don't have to directly expose themselves to the public internet.

---

## 15. Reverse Proxy + Load Balancer

Now let's combine the concepts.

```
                         Users
                           |
                           v
                    Reverse Proxy
                           |
                           v
                    Load Balancing
                    /     |      \
                   v      v       v
                 App1   App2    App3
```

In many systems, the same component can perform both roles.

For example:

> **NGINX** can be configured as:
>
> Reverse Proxy + Load Balancer

---

## 16. Reverse Proxy vs API Gateway

These are also commonly confused.

### Reverse Proxy

General-purpose traffic intermediary:

```
Client
   |
   v
Reverse Proxy
   |
   v
Backend
```

Typical responsibilities:

- Routing
- TLS termination
- Caching
- Compression
- Load balancing
- Connection handling

### API Gateway

Usually provides API-specific capabilities:

```
Client
   |
   v
API Gateway
   |
   +----> User Service
   +----> Order Service
   +----> Payment Service
```

It may provide:

- Authentication
- Authorization
- Rate limiting
- Request transformation
- API composition
- Routing
- Observability

An API Gateway can itself be implemented using or built on top of reverse-proxy functionality.

---

## 17. Reverse Proxy vs Forward Proxy

This is an excellent interview question.

| Feature | Forward Proxy | Reverse Proxy |
|---|---|---|
| Represents | Client | Server |
| Client knows about proxy | Usually yes | Client may not know backend |
| Controls | Outbound traffic | Inbound traffic |
| Common in | Corporate networks | Web architectures |
| Direction | Client → Proxy → Internet | Client → Proxy → Backend |

Remember:

```
FORWARD PROXY

Client → Proxy → Internet
   ↑
Proxy represents client
```

```
REVERSE PROXY

Client → Proxy → Server
           ↑
    Proxy represents server
```

---

## 18. Reverse Proxy + Horizontal Scaling

Let's connect this with the previous topics.

You learned:

**Horizontal Scaling**

```
1 Server
   ↓
3 Servers
```

Then:

**Load Balancer**

```
              Load Balancer
             /      |      \
            v       v       v
          App1    App2    App3
```

Now:

**Reverse Proxy**

```
                   Client
                     |
                     v
               Reverse Proxy
                     |
                     v
                Load Balancer
                /     |     \
               v      v      v
             App1    App2    App3
```

But in practice, a single component can often combine the reverse-proxy and load-balancing roles:

```
                   Client
                     |
                     v
            Reverse Proxy /
             Load Balancer
               /    |    \
              v     v     v
            App1  App2   App3
```

---

## 19. Complete Production Architecture

Now combine almost everything you've learned so far:

```
                         Users
                           |
                           v
                          DNS
                           |
                           v
                 Reverse Proxy /
                  Load Balancer
                           |
              +------------+------------+
              |            |            |
              v            v            v
            App1         App2         App3
              |            |            |
           Pool 10      Pool 10      Pool 10
              \            |            /
               \           |           /
                +----------+----------+
                           |
                           v
                         Redis
                           |
                           v
                       Database
```

Possible responsibilities:

```
DNS
 ↓
Find service endpoint

Reverse Proxy
 ↓
TLS termination
Routing
Security
Caching
Compression

Load Balancer
 ↓
Distribute traffic

Application Servers
 ↓
Business logic

Connection Pool
 ↓
Reuse DB connections

Redis
 ↓
Cache frequently accessed data

Database
 ↓
Persistent data
```

---

## 20. What Happens When a Request Arrives?

Let's trace one request.

User requests:

```
GET https://api.example.com/orders/123
```

### Step 1 — DNS

DNS resolves:

```
api.example.com
       ↓
Reverse Proxy / Load Balancer
```

### Step 2 — TLS

HTTPS connection is established with the edge/reverse-proxy layer.

```
Client
  |
 HTTPS
  v
Reverse Proxy
```

### Step 3 — Routing

Reverse proxy sees:

```
/orders/123
```

and determines where to send it.

### Step 4 — Load Balancing

Suppose it selects:

```
App2
```

### Step 5 — Application

Spring Boot processes:

```
GET /orders/123
```

### Step 6 — Connection Pool

Application needs the database:

```
App2
 |
 v
Connection Pool
 |
 v
Borrow Connection
```

### Step 7 — Database

```sql
SELECT ...
```

### Step 8 — Response

```
Database
   ↓
App2
   ↓
Reverse Proxy
   ↓
Client
```

**Complete:**

```
Client
  |
  | HTTPS
  v
DNS
  |
  v
Reverse Proxy / Load Balancer
  |
  v
App2
  |
  v
Connection Pool
  |
  v
Database
  |
  v
Response
  |
  v
Client
```

---

## 21. What If App2 Goes Down?

Suppose:

```
Reverse Proxy / LB
       /    |    \
      v     X     v
    App1   App2   App3
            ❌
```

Health checks detect:

```
App2 = unhealthy
```

Traffic goes to:

```
App1 or App3
```

This connects:

- Reverse Proxy
- Load Balancer
- Health Checks
- Horizontal Scaling

to provide a more resilient architecture.

---

## 22. Reverse Proxy Can Protect Backend Servers

**Without reverse proxy:**

```
Internet
   |
   +------------------+
   |                  |
   v                  v
App1                App2
```

Backend servers are directly exposed.

**With reverse proxy:**

```
Internet
   |
   v
Reverse Proxy
   |
   +---- App1
   |
   +---- App2
```

You can keep backend instances on private/internal networks in many architectures.

Conceptually:

```
Public Network
      |
      v
Reverse Proxy
      |
      v
Private Network
      |
   +--+--+
   |     |
  App1  App2
```

This provides an additional layer of isolation.

---

## 23. Common Technologies

Examples of technologies commonly used for reverse-proxy/load-balancing roles include:

- NGINX
- HAProxy
- Apache HTTP Server
- Envoy
- Cloud-managed load balancers
- Kubernetes ingress/gateway components

The exact choice depends on the architecture and infrastructure.

---

## 24. Interview Questions

### Q1. What is a Reverse Proxy?

> A reverse proxy sits in front of backend servers, receives client requests, and forwards them to the appropriate backend on the client's behalf.

### Q2. Why use a Reverse Proxy?

Common reasons:

> TLS termination, routing, caching, compression, security controls, load balancing, and hiding backend infrastructure.

### Q3. Reverse Proxy vs Forward Proxy?

> A forward proxy represents the client; a reverse proxy represents the server/backend.

### Q4. Can a Reverse Proxy perform load balancing?

> Yes. Many reverse proxies can distribute traffic across multiple backend servers.

### Q5. Does Reverse Proxy equal Load Balancer?

> No. They overlap, but their primary concepts differ. A reverse proxy is an intermediary for backend services; a load balancer's primary purpose is distributing traffic among backend instances.

### Q6. Why use TLS termination at a Reverse Proxy?

> It centralizes much of the TLS handling and certificate configuration at the edge, allowing backend applications to focus on application logic.

---

## 25. The Mental Model

Keep this picture in your head:

```
                         INTERNET
                            |
                            v
                          Client
                            |
                            v
                     +-------------+
                     |   Reverse   |
                     |    Proxy    |
                     +-------------+
                       /    |    \
                      /     |     \
                     v      v      v
                  App 1   App 2   App 3
                     \      |      /
                      \     |     /
                       v    v    v
                     Connection
                        Pools
                          |
                          v
                       Database
```

And remember the responsibilities:

```
Reverse Proxy
     ↓
"Who should receive this request?"

Load Balancer
     ↓
"Which healthy instance should receive it?"

Application
     ↓
"What business logic should I execute?"

Connection Pool
     ↓
"Which reusable DB connection can I give the application?"

Database
     ↓
"Where is the persistent data?"
```

---

## ⭐ Most Important Takeaway

You now have a strong foundation for the request path:

```
Client
  ↓
DNS
  ↓
Reverse Proxy / Load Balancer
  ↓
Multiple Application Instances
  ↓
Connection Pool
  ↓
Database
```

And each concept solves a different problem:

| Component | Main Problem It Solves |
|---|---|
| DNS | Where is the service? |
| Reverse Proxy | How should incoming traffic be handled/routed? |
| Load Balancer | Which backend instance should receive the request? |
| Horizontal Scaling | How do we handle more traffic? |
| Connection Pool | How do we efficiently reuse DB connections? |
| Cache | How do we avoid unnecessary DB calls? |
| Database | How do we persist data? |

The next concept that fits very naturally after Reverse Proxy is **API Gateway**, followed by **Caching** (Redis, Cache-Aside, TTL, Eviction, Cache Invalidation).
