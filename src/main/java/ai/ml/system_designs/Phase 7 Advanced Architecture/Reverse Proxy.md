# Reverse Proxy

A Reverse Proxy is a server that sits between clients and backend servers and forwards client requests to the appropriate backend server.

The client talks to the reverse proxy, not directly to the backend.

```
Client
   │
   │ HTTP/HTTPS
   ▼
┌─────────────────┐
│  Reverse Proxy  │
└────────┬────────┘
         │
    ┌────┼────┐
    ▼    ▼    ▼
 Server1 Server2 Server3
```

The key idea is:

> A reverse proxy represents the backend servers to the client.

---

## 1. Why is it called "Reverse" Proxy?

First understand a forward proxy.

### Forward Proxy

The proxy represents the client.

```
Client
   │
   ▼
Forward Proxy
   │
   ▼
Internet
```

The destination server doesn't necessarily know the real client directly.

Example:

```
Employee
   │
   ▼
Company Proxy
   │
   ▼
Internet
```

The company proxy acts on behalf of the employee.

### Reverse Proxy

The proxy represents the servers.

```
Client
   │
   ▼
Reverse Proxy
   │
   ▼
Backend Servers
```

The client doesn't need to know which backend server actually handled the request.

---

## 2. Simple Real-World Analogy

Imagine a restaurant.

```
Customer
    │
    ▼
Receptionist
    │
    ├──► Waiter 1
    ├──► Waiter 2
    └──► Waiter 3
```

The customer doesn't need to know which waiter is available.

The receptionist decides where to send the request.

A reverse proxy works similarly:

```
Client
   │
   ▼
Reverse Proxy
   │
   ├──► Backend 1
   ├──► Backend 2
   └──► Backend 3
```

---

## 3. Basic Example

Suppose you have three application servers:

```
10.0.0.10:8080
10.0.0.11:8080
10.0.0.12:8080
```

Instead of exposing them directly, you expose:

```
api.mycompany.com
```

Architecture:

```
                    Internet
                       │
                       ▼
              api.mycompany.com
                       │
                       ▼
                Reverse Proxy
                       │
             ┌─────────┼─────────┐
             ▼         ▼         ▼
          Server 1  Server 2  Server 3
```

The client only sees:

```
api.mycompany.com
```

It doesn't need to know:

```
10.0.0.10
10.0.0.11
10.0.0.12
```

---

## 4. What does a Reverse Proxy do?

A reverse proxy can perform several functions:

```
Reverse Proxy
     │
     ├── Routing
     ├── Load Balancing
     ├── TLS Termination
     ├── Caching
     ├── Compression
     ├── Security
     ├── Rate Limiting
     ├── Request Filtering
     └── Connection Management
```

The exact capabilities depend on the software/configuration.

---

## 5. Reverse Proxy for Load Balancing

One of the most common uses.

Suppose:

```
             Reverse Proxy
                  │
       ┌──────────┼──────────┐
       ▼          ▼          ▼
   Server 1    Server 2    Server 3
```

Requests can be distributed:

```
Request 1 → Server 1
Request 2 → Server 2
Request 3 → Server 3
Request 4 → Server 1
```

Possible algorithms include:

- Round Robin
- Weighted Round Robin
- Least Connections
- IP Hash
- Random

---

## 6. Reverse Proxy for High Availability

Suppose:

```
Server 1 ✓
Server 2 ✓
Server 3 ✗
```

The reverse proxy can stop sending traffic to Server 3.

```
             Reverse Proxy
                  │
          ┌───────┴───────┐
          ▼               ▼
       Server 1         Server 2
        HEALTHY          HEALTHY
```

This requires appropriate health checks.

---

## 7. TLS Termination

The client sends HTTPS:

```
Client
  │
  │ HTTPS
  ▼
Reverse Proxy
  │
  │ HTTP or HTTPS
  ▼
Backend
```

The reverse proxy can terminate TLS.

For example:

```
Client
   │
   │ HTTPS
   ▼
Nginx
   │
   │ HTTP
   ▼
Application
```

Or you can keep HTTPS/TLS between the proxy and backend too.

---

## 8. Security

A reverse proxy can act as a protective layer.

```
Internet
    │
    ▼
Reverse Proxy
    │
    ├── Block malicious requests
    ├── Rate limit
    ├── IP filtering
    └── Forward valid requests
            │
            ▼
       Application
```

This means backend servers don't necessarily need to be directly exposed to the public internet.

---

## 9. Caching

Suppose users frequently request:

```
GET /products/100
```

The reverse proxy can cache the response.

```
Client
   │
   ▼
Reverse Proxy
   │
   ├── Cache HIT
   │      ↓
   │   Response
   │
   └── Cache MISS
          ↓
       Backend
```

### Cache HIT

```
Client
   ↓
Reverse Proxy
   ↓
Cache
   ↓
Response
```

No backend request is necessary.

### Cache MISS

```
Client
   ↓
Reverse Proxy
   ↓
Backend
   ↓
Response
   ↓
Cache
```

This reduces backend load and latency for suitable cacheable content.

---

## 10. Routing

A reverse proxy can route based on URL.

For example:

```
             Reverse Proxy
                  │
       ┌──────────┼───────────┐
       ▼          ▼           ▼
   /users       /orders     /payments
       │          │           │
       ▼          ▼           ▼
     User       Order       Payment
    Service     Service      Service
```

Request:

```
GET /orders/123
```

becomes:

```
Client
  │
  ▼
Reverse Proxy
  │
  │ /orders/*
  ▼
Order Service
```

---

## 11. Reverse Proxy in Microservices

A typical architecture can look like:

```
                         Client
                           │
                           ▼
                    Reverse Proxy
                           │
                    ┌──────┼──────┐
                    ▼      ▼      ▼
                  User   Order   Payment
                Service Service Service
```

The reverse proxy hides the internal network topology.

Without it:

```
Client ──► User Service
Client ──► Order Service
Client ──► Payment Service
Client ──► Inventory Service
```

With it:

```
Client
   │
   ▼
Reverse Proxy
   │
   ├──► User
   ├──► Order
   ├──► Payment
   └──► Inventory
```

---

## 12. Reverse Proxy vs Forward Proxy

This is a very common interview question.

|  | Forward Proxy | Reverse Proxy |
|---|---|---|
| Represents | Client | Server |
| Sits in front of | Clients | Servers |
| Main direction | Client → Internet | Internet → Backend |
| Hides | Client identity/location | Backend topology |
| Common use | Corporate internet access | Load balancing/API hosting |

### Forward proxy

```
Client → Proxy → Internet → Server
```

### Reverse proxy

```
Client → Reverse Proxy → Server
```

The difference is whose side the proxy represents.

---

## 13. Reverse Proxy vs Load Balancer

These are often confused.

### Reverse Proxy

A broader concept:

```
Client
   ↓
Reverse Proxy
   ↓
Backend
```

It can perform:

- routing
- TLS termination
- caching
- security
- compression
- load balancing

### Load Balancer

Its primary job is:

> Distribute traffic among backend instances.

```
Client
   ↓
Load Balancer
   ├──► Server 1
   ├──► Server 2
   └──► Server 3
```

A reverse proxy can also act as a load balancer.

For example:

```
Client
   ↓
Nginx
   ↓
Load balancing
   ├──► App 1
   ├──► App 2
   └──► App 3
```

So these terms overlap in real-world architectures, but they describe different roles.

---

## 14. Reverse Proxy vs API Gateway

This is especially important for your microservices learning.

### Reverse Proxy

Generally focused on:

```
Client
  ↓
Reverse Proxy
  ↓
Backend
```

Typical responsibilities:

- Routing
- Load balancing
- TLS termination
- Caching
- Connection handling

### API Gateway

Typically provides API-specific capabilities:

```
Client
  ↓
API Gateway
  ├── Authentication
  ├── Authorization
  ├── Rate limiting
  ├── Routing
  ├── Request aggregation
  ├── API versioning
  └── Observability
       ↓
   Microservices
```

An API Gateway is often implemented using or built on top of reverse-proxy capabilities.

The boundary isn't absolute.

---

## 15. Nginx as a Reverse Proxy

A very common example is Nginx.

Architecture:

```
                 Internet
                    │
                    ▼
                  Nginx
               Reverse Proxy
                    │
          ┌─────────┼─────────┐
          ▼         ▼         ▼
       App-1      App-2      App-3
```

A simplified configuration conceptually looks like:

```nginx
upstream backend {
    server 10.0.0.10:8080;
    server 10.0.0.11:8080;
    server 10.0.0.12:8080;
}

server {
    listen 443 ssl;

    location / {
        proxy_pass http://backend;
    }
}
```

The client sees:

```
https://api.example.com
```

while Nginx forwards the request internally.

---

## 16. Reverse Proxy and Service Discovery

Now combine the concepts you've learned.

```
                    Client
                      │
                      ▼
               Reverse Proxy
                      │
                      ▼
               Service Discovery
                      │
              ┌───────┼───────┐
              ▼       ▼       ▼
           Order-1 Order-2 Order-3
```

The proxy needs to know where healthy instances are.

Depending on the architecture, discovery may be handled through:

- DNS
- Kubernetes Services
- service registry
- platform-native discovery
- configuration

---

## 17. Reverse Proxy in Kubernetes

A simplified Kubernetes architecture:

```
                         Internet
                            │
                            ▼
                     Ingress / Proxy
                            │
                            ▼
                    Kubernetes Service
                            │
               ┌────────────┼────────────┐
               ▼            ▼            ▼
             Pod 1        Pod 2        Pod 3
```

The external request might be:

```
https://api.example.com/orders/123
```

The proxy/Ingress routes it to the appropriate Kubernetes Service, which routes to available Pods.

---

## 18. Reverse Proxy and CDN

You can also have:

```
Client
  │
  ▼
CDN
  │
  ▼
Reverse Proxy
  │
  ▼
Application
```

The CDN may handle:

- static content
- edge caching
- geographic distribution
- DDoS protection

The reverse proxy can then handle application-level routing and backend traffic.

---

## 19. Does every microservice need a reverse proxy?

No.

You might have:

```
Internet
   ↓
Reverse Proxy / API Gateway
   ↓
Microservices
```

The individual services don't necessarily each need their own public reverse proxy.

However, internal proxies or sidecars can be used in some architectures, especially with service meshes.

---

## 20. Biggest benefit: Hide backend topology

Without reverse proxy:

```
Client
  │
  ├──► 10.0.1.10
  ├──► 10.0.1.11
  ├──► 10.0.2.10
  └──► 10.0.3.10
```

The client knows your infrastructure.

With reverse proxy:

```
Client
   │
   ▼
api.example.com
   │
   ▼
Reverse Proxy
   │
   ├──► Backend 1
   ├──► Backend 2
   ├──► Backend 3
   └──► Backend 4
```

Backend infrastructure can change without changing the client.

---

## 21. What if a backend server crashes?

Suppose:

```
             Reverse Proxy
                  │
          ┌───────┼───────┐
          ▼       ▼       ▼
         A       B       C
        ✓       ✗       ✓
```

Health checking detects:

```
B → unhealthy
```

Traffic becomes:

```
             Reverse Proxy
                  │
             ┌────┴────┐
             ▼         ▼
            A          C
```

This gives better availability.

---

## 22. Important: Reverse Proxy is not a magic reliability solution

A single reverse proxy can itself become a single point of failure:

```
Clients
   │
   ▼
Reverse Proxy ❌
   │
   X
Backend
```

Therefore, production architectures often use multiple proxy instances:

```
                    Load Balancer
                         │
              ┌──────────┼──────────┐
              ▼          ▼          ▼
           Proxy-1    Proxy-2    Proxy-3
              │          │          │
              └──────────┼──────────┘
                         ▼
                    Applications
```

---

## 23. Complete Microservices Architecture

Now connect your previous topics:

```
                           CLIENT
                              │
                              ▼
                       ┌─────────────┐
                       │ CDN / Edge  │
                       └──────┬──────┘
                              │
                              ▼
                       ┌─────────────┐
                       │   Reverse   │
                       │    Proxy    │
                       └──────┬──────┘
                              │
                              ▼
                       ┌─────────────┐
                       │ API Gateway │
                       └──────┬──────┘
                              │
                       Service Discovery
                              │
             ┌────────────────┼─────────────────┐
             ▼                ▼                 ▼
        User Service     Order Service     Payment Service
             │                │                 │
             ▼                ▼                 ▼
          User DB          Order DB          Payment DB
                              │
                              ▼
                            Kafka
                              │
                    ┌─────────┼─────────┐
                    ▼         ▼         ▼
                Inventory Notification Analytics
```

You can see how the concepts fit together:

```
Reverse Proxy
      ↓
API Gateway
      ↓
Service Discovery
      ↓
Microservices
      ↓
Database / Kafka
```

---

## 24. Interview Questions

For a 10-year-experience system design interview, be ready for:

### Basic

- What is a reverse proxy?
- Why do we need a reverse proxy?
- Reverse proxy vs forward proxy?
- What is Nginx?
- How does reverse proxy routing work?

### Intermediate

- Reverse proxy vs load balancer?
- Reverse proxy vs API Gateway?
- How does TLS termination work?
- How does caching work at a reverse proxy?
- How does a reverse proxy detect unhealthy servers?
- How does reverse proxy work with service discovery?

### Advanced

- How do you make the reverse proxy highly available?
- How do you avoid it becoming a bottleneck?
- How would you handle millions of connections?
- How would you configure rate limiting?
- How would you handle WebSockets?
- How would you design multi-region reverse proxies?
- Reverse proxy vs service mesh?
- Where should TLS terminate?
- How do you prevent cascading failures?

---

## The 3 concepts you should clearly distinguish

```
                 ┌─────────────────┐
                 │ Reverse Proxy   │
                 │                 │
                 │ "Where/how do   │
                 │ I forward this?"│
                 └────────┬────────┘
                          │
                          ▼
                 ┌─────────────────┐
                 │ API Gateway     │
                 │                 │
                 │ "Which API and  │
                 │ which policies?"│
                 └────────┬────────┘
                          │
                          ▼
                 ┌─────────────────┐
                 │ Service         │
                 │ Discovery       │
                 │                 │
                 │ "Where are the  │
                 │ service nodes?" │
                 └─────────────────┘
```

---

## One-line definitions

**Reverse Proxy:**

> A server that receives client requests and forwards them to backend servers while hiding the backend infrastructure.

**API Gateway:**

> A specialized entry point for APIs that adds capabilities such as routing, authentication, rate limiting, aggregation, and API management.

**Service Discovery:**

> A mechanism for dynamically locating healthy instances of a service.

---

## Easy way to remember

```
Reverse Proxy    = Front door
API Gateway      = Smart API front door
Service Discovery = Address book
Load Balancer    = Traffic distributor
```

