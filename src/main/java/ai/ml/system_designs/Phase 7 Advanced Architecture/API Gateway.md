# API Gateway

An API Gateway is a single entry point between clients and a set of backend microservices.

Instead of the client communicating directly with every microservice, the client communicates with the API Gateway, and the gateway routes the request to the appropriate service.

---

## 1. Without API Gateway

Imagine an e-commerce system:

```
                    Client
                      │
        ┌─────────────┼──────────────┐
        │             │              │
        ▼             ▼              ▼
   User Service  Order Service  Payment Service
        │             │              │
        ▼             ▼              ▼
     User DB       Order DB       Payment DB
```

The client needs to know:

- User Service URL
- Order Service URL
- Payment Service URL
- Inventory Service URL
- Notification Service URL

For example:

```
GET https://user-service/users/123

GET https://order-service/orders/456

GET https://payment-service/payments/789
```

This creates several problems.

### Problems

- Client knows too much about internal architecture
- Many service URLs must be maintained
- Authentication logic may be duplicated
- Rate limiting may be duplicated
- CORS/security configuration becomes harder
- Backend services are exposed directly
- Mobile/web clients may need different APIs

---

## 2. With API Gateway

Instead:

```
                         Client
                           │
                           ▼
                    ┌─────────────┐
                    │ API Gateway │
                    └──────┬──────┘
                           │
              ┌────────────┼─────────────┐
              │            │             │
              ▼            ▼             ▼
        User Service  Order Service  Payment Service
              │            │             │
              ▼            ▼             ▼
           User DB      Order DB      Payment DB
```

The client only knows:

```
api.mycompany.com
```

The gateway decides where the request should go.

---

## 3. Simple Example

Client sends:

```
GET /orders/123
```

to:

```
https://api.mycompany.com/orders/123
```

The gateway sees:

```
/orders/**
```

and routes it to:

```
Order Service
```

So:

```
Client
  │
  │ GET /orders/123
  ▼
API Gateway
  │
  │ route
  ▼
Order Service
  │
  ▼
Order DB
```

The client doesn't need to know where Order Service is actually running.

---

## 4. API Gateway is more than routing

A common beginner mistake is:

> "API Gateway is just a router."

Routing is one responsibility, but a gateway can provide many cross-cutting concerns.

Typical responsibilities:

```
                    API Gateway
                         │
       ┌─────────────────┼──────────────────┐
       │                 │                  │
       ▼                 ▼                  ▼
    Routing        Authentication       Rate Limiting
       │                 │                  │
       ▼                 ▼                  ▼
   Load Balance       Security          Protection
```

Common capabilities include:

- Routing
- Authentication
- Authorization
- Rate limiting
- Load balancing
- TLS termination
- Request/response transformation
- Request aggregation
- Caching
- Logging
- Metrics
- Distributed tracing
- API versioning
- CORS handling
- Security filtering

---

## 5. Routing

This is the most basic responsibility.

Suppose:

```
API Gateway
     │
     ├── /users/**      → User Service
     │
     ├── /orders/**     → Order Service
     │
     ├── /payments/**   → Payment Service
     │
     └── /products/**   → Product Service
```

Request:

```
GET /products/100
```

Gateway:

```
GET /products/100
       │
       ▼
API Gateway
       │
       ▼
Product Service
```

---

## 6. Authentication

The gateway can verify whether the request is authenticated.

For example, client sends:

```
Authorization: Bearer eyJhbGci...
```

Gateway validates the token.

```
Client
  │
  │ JWT
  ▼
API Gateway
  │
  ├── Invalid → 401
  │
  └── Valid
       │
       ▼
   Backend Service
```

This prevents every service from implementing the same basic authentication mechanism independently.

---

## 7. Authorization

Authentication answers:

> Who are you?

Authorization answers:

> What are you allowed to do?

Example:

```
User
 │
 │ DELETE /orders/123
 ▼
API Gateway
 │
 │ Role = CUSTOMER
 │
 ▼
403 Forbidden
```

Or:

```
Admin
 │
 │ DELETE /orders/123
 ▼
API Gateway
 │
 ▼
Order Service
```

Fine-grained business authorization may still need to happen inside the service.

---

## 8. Rate Limiting

Suppose one client sends:

```
100,000 requests/second
```

It could overwhelm your backend.

Gateway can enforce:

```
Client
   │
   ▼
API Gateway
   │
   ├── 100 requests → allowed
   ├── 100 requests → allowed
   └── excess       → rejected/throttled
```

For example:

```
100 requests / minute / user
```

This protects backend services.

---

## 9. Load Balancing

Suppose Order Service has multiple instances:

```
                  API Gateway
                       │
                 Load Balancer
                       │
           ┌───────────┼───────────┐
           ▼           ▼           ▼
       Order-1      Order-2      Order-3
```

Gateway/infrastructure can distribute requests:

```
Request 1 → Order-1
Request 2 → Order-2
Request 3 → Order-3
Request 4 → Order-1
```

---

## 10. TLS Termination

Client communicates securely:

```
Client
  │
  │ HTTPS
  ▼
API Gateway
```

The gateway can terminate TLS:

```
Client
   │
   │ HTTPS
   ▼
API Gateway
   │
   │ internal communication
   ▼
Order Service
```

Depending on your security architecture, communication between gateway and services may also use TLS/mTLS.

---

## 11. Request Aggregation

This is a very useful API Gateway capability.

Suppose a mobile application needs:

- User information
- Order information
- Recommendation information

Without aggregation:

```
Mobile App
   │
   ├──► User Service
   ├──► Order Service
   └──► Recommendation Service
```

Three network calls.

With gateway aggregation:

```
Mobile App
     │
     ▼
API Gateway
     │
     ├──► User Service
     ├──► Order Service
     └──► Recommendation Service
     │
     ▼
Combined Response
     │
     ▼
Mobile App
```

For example:

```json
{
  "user": {
    "name": "John"
  },
  "orders": [
    {
      "id": 101
    }
  ],
  "recommendations": [
    {
      "productId": 500
    }
  ]
}
```

The client makes one request instead of three.

---

## 12. API Gateway vs Reverse Proxy

These are related but not exactly the same.

### Reverse Proxy

Primarily forwards requests:

```
Client
  ↓
Reverse Proxy
  ↓
Backend
```

Typical responsibilities:

- routing
- TLS termination
- load balancing
- connection handling

### API Gateway

Usually operates at a higher application/API level:

```
Client
  ↓
API Gateway
  ├── Authentication
  ├── Authorization
  ├── Rate Limiting
  ├── Routing
  ├── Transformation
  ├── Aggregation
  └── Observability
       ↓
   Microservices
```

A reverse proxy can be part of an API gateway architecture, and the boundary isn't always strict.

---

## 13. API Gateway vs Load Balancer

This is an important interview distinction.

### Load Balancer

Main question:

> Which healthy server instance should receive this request?

```
Client
  │
  ▼
Load Balancer
  │
  ├──► Server 1
  ├──► Server 2
  └──► Server 3
```

### API Gateway

Main question:

> Which API/service should handle this request, and what cross-cutting policies should apply?

```
Client
  │
  ▼
API Gateway
  │
  ├── /users   → User Service
  ├── /orders  → Order Service
  └── /payment → Payment Service
```

They can exist together.

---

## 14. API Gateway + Service Discovery

These two concepts work very well together.

Suppose:

```
Client
  │
  ▼
API Gateway
  │
  ▼
Service Discovery
  │
  ▼
Order Service instances
 ├── Order-1
 ├── Order-2
 └── Order-3
```

Flow:

```
1. Client sends request
        ↓
2. Gateway receives request
        ↓
3. Gateway identifies Order Service
        ↓
4. Gateway discovers healthy instances
        ↓
5. Gateway selects an instance
        ↓
6. Request forwarded
```

In Kubernetes, the platform can provide much of this discovery/routing infrastructure through Services and DNS.

---

## 15. API Gateway + Authentication + Microservices

A common production architecture:

```
                       Internet
                          │
                          ▼
                   ┌─────────────┐
                   │ API Gateway │
                   └──────┬──────┘
                          │
                   Authentication
                          │
              ┌───────────┼───────────┐
              ▼           ▼           ▼
           User         Order       Payment
          Service      Service      Service
              │           │           │
              ▼           ▼           ▼
           User DB      Order DB    Payment DB
```

---

## 16. API Gateway and Microservices Security

A common model is:

```
                Client
                  │
                HTTPS
                  │
                  ▼
            API Gateway
                  │
          ┌───────┴────────┐
          │                │
      JWT Validation    Rate Limit
          │                │
          └───────┬────────┘
                  ▼
            Microservices
```

However, don't assume the gateway alone is the security boundary.

Services should still validate authorization and protect sensitive operations.

---

## 17. API Versioning

Suppose you release:

```
/api/v1/orders
```

Then later:

```
/api/v2/orders
```

Gateway can route:

```
/api/v1/* → Old API implementation

/api/v2/* → New API implementation
```

This helps clients migrate gradually.

---

## 18. Caching

Gateway can sometimes cache responses.

Example:

```
Client
  │
  ▼
API Gateway
  │
  ├── Cache HIT → Return immediately
  │
  └── Cache MISS
          ↓
      Product Service
```

For data that changes infrequently, this can reduce backend load and latency.

But caching at the gateway must be designed carefully around:

- TTL
- invalidation
- authorization
- personalized responses
- stale data

---

## 19. Request Transformation

Suppose an old client sends:

```json
{
  "customerId": 123
}
```

but the new backend expects:

```json
{
  "userId": 123
}
```

A gateway can sometimes transform requests.

```
Client
  │
  │ customerId
  ▼
Gateway
  │
  │ userId
  ▼
Backend
```

However, don't put excessive business logic into the gateway.

---

## 20. What should NOT go into API Gateway?

This is a very important architectural principle.

Avoid putting business logic such as:

```
if order > ₹50,000
   give special discount
```

or:

```
Calculate payment eligibility
Reserve inventory
Determine shipping price
```

inside the gateway.

Why?

Because the gateway should primarily handle cross-cutting concerns and traffic management, not become a giant monolithic business-logic component.

Bad:

```
                API Gateway
                     │
          ┌──────────┼───────────┐
          │          │           │
       Business   Business    Business
        Logic      Logic       Logic
```

Good:

```
                API Gateway
                     │
              Routing/Security
                     │
       ┌─────────────┼─────────────┐
       ▼             ▼             ▼
     Order         Payment       Inventory
     Logic          Logic          Logic
```

---

## 21. API Gateway can become a bottleneck

A gateway sits in front of everything:

```
10,000 clients
      │
      ▼
 API Gateway
      │
      ▼
Microservices
```

If the gateway isn't highly available:

```
API Gateway ❌
      ↓
Entire API unavailable
```

Therefore, don't deploy a single gateway instance.

Use multiple instances:

```
                 Load Balancer
                      │
          ┌───────────┼───────────┐
          ▼           ▼           ▼
      Gateway-1   Gateway-2   Gateway-3
          │           │           │
          └───────────┼───────────┘
                      ▼
                 Microservices
```

---

## 22. API Gateway failure

Suppose Gateway-1 crashes:

```
Gateway-1 ❌
Gateway-2 ✓
Gateway-3 ✓
```

Load balancer sends traffic to:

```
Gateway-2
Gateway-3
```

This gives gateway-level high availability.

---

## 23. API Gateway vs Service Mesh

Another common interview question.

### API Gateway

Primarily handles north-south traffic:

```
Internet
   │
   ▼
API Gateway
   │
   ▼
Microservices
```

### Service Mesh

Primarily handles east-west traffic:

```
Order ─────► Payment
  │             │
  └────► Inventory
```

A service mesh can provide things such as:

- service-to-service security
- traffic management
- retries
- observability
- mTLS

So:

```
                Internet
                    │
                    ▼
              API Gateway
                    │
             ┌──────┼──────┐
             ▼      ▼      ▼
           Order  Payment Inventory
             │      │      │
             └──────┼──────┘
                    │
               Service Mesh
```

The exact architecture varies by platform.

---

## 24. API Gateway vs BFF

BFF = Backend for Frontend

Suppose you have:

- Web
- Mobile
- Admin

Each may need different data.

You can have:

```
                 Clients
             ┌─────┼─────┐
             ▼     ▼     ▼
            Web   Mobile Admin
             │     │      │
             ▼     ▼      ▼
           BFF-Web BFF-Mobile BFF-Admin
             │        │          │
             └────────┼──────────┘
                      ▼
                 Microservices
```

The BFF pattern is useful when different clients need significantly different API shapes.

---

## 25. Complete architecture

Putting the concepts together:

```
                         Clients
                    ┌──────┼──────┐
                    │      │      │
                  Web    Mobile  Admin
                    │      │      │
                    └──────┼──────┘
                           │
                           ▼
                    ┌─────────────┐
                    │ Load        │
                    │ Balancer    │
                    └──────┬──────┘
                           │
                 ┌─────────┼─────────┐
                 ▼         ▼         ▼
              Gateway-1 Gateway-2 Gateway-3
                 │         │         │
                 └─────────┼─────────┘
                           │
          ┌────────────────┼────────────────┐
          │                │                │
          ▼                ▼                ▼
       User Service    Order Service    Payment Service
          │                │                │
          ▼                ▼                ▼
       User DB          Order DB        Payment DB
                           │
                           ▼
                         Kafka
                           │
                ┌──────────┼───────────┐
                ▼          ▼           ▼
            Inventory  Notification  Analytics
```

---

## 26. Interview scenario

### Question:

> "Why do we need an API Gateway in a microservices architecture?"

A strong answer:

> In a microservices architecture, exposing every service directly to clients creates tight coupling and duplicates cross-cutting concerns. An API Gateway provides a single entry point and can handle routing, authentication, authorization, rate limiting, TLS termination, request aggregation, API versioning, and observability. It hides internal service topology and allows backend services to evolve independently.

---

## 27. Important interview questions

You should be able to answer these:

### Beginner

- What is an API Gateway?
- Why do we need an API Gateway?
- API Gateway vs Reverse Proxy?
- API Gateway vs Load Balancer?
- What is request routing?
- What is TLS termination?

### Intermediate

- How does API Gateway work with Service Discovery?
- How does API Gateway perform authentication?
- How does rate limiting work?
- What is request aggregation?
- How does API versioning work?
- How do you make API Gateway highly available?
- What happens if the API Gateway goes down?

### Advanced

- API Gateway vs Service Mesh?
- API Gateway vs BFF?
- Where should authorization happen?
- How do you prevent the gateway from becoming a bottleneck?
- How would you design rate limiting for millions of users?
- How do retries at the gateway affect downstream services?
- How do you prevent cascading failures?
- Should the gateway contain business logic?
- How would you monitor an API Gateway?
- How would you handle WebSockets/streaming?
- How would you design a multi-region API Gateway?

---

## The key picture to remember

```
                     CLIENT
                       │
                       ▼
                ┌─────────────┐
                │ API GATEWAY │
                └──────┬──────┘
                       │
        ┌──────────────┼──────────────┐
        │              │              │
        ▼              ▼              ▼
      USER           ORDER          PAYMENT
    SERVICE         SERVICE         SERVICE
        │              │              │
        ▼              ▼              ▼
      User DB        Order DB       Payment DB
```

Think of the API Gateway as the front door of your microservices system.

Its core responsibilities are:

```
Route → Authenticate → Authorize → Protect → Transform → Observe
```

And remember the architectural boundary:

> API Gateway handles cross-cutting API concerns; business logic belongs inside the appropriate microservice.

