# REST APIs — From Scratch

Since you're learning System Design from scratch, REST APIs are the natural next step after:

```
Client → IP → Port → Socket → TCP → HTTP/HTTPS
```

REST APIs sit mainly at the application layer, using HTTP as the communication protocol.

The most important mental model is:

> **REST is an architectural style for designing APIs around resources, while HTTP provides the actual network communication.**

---

## 1. What is an API?

API means:

**Application Programming Interface**

An API allows one software system to communicate with another.

For example:

```
Mobile App
    |
    | API Request
    ↓
Backend Server
    |
    ↓
Database
```

The mobile application doesn't directly access the database.

Instead:

```
Mobile App
    ↓
REST API
    ↓
Backend
    ↓
Database
```

---

## 2. Real-World Example

Imagine an e-commerce application.

You want to retrieve a product.

The mobile app sends:

```
GET /products/101
```

The server might respond:

```json
{
  "id": 101,
  "name": "iPhone",
  "price": 79999
}
```

That's a REST API interaction.

---

## 3. REST API Architecture

A typical architecture looks like:

```
                Client
          ┌───────┴───────┐
          ↓               ↓
       Browser          Mobile
          \               /
           \             /
            ↓           ↓
              REST API
                 |
                 ↓
          Application Server
                 |
        ┌────────┴────────┐
        ↓                 ↓
     Database           Cache
```

The client communicates with the backend using HTTP.

---

## 4. REST

REST stands for:

**Representational State Transfer**

It was described by Roy Fielding in his doctoral dissertation.

REST is **not** a protocol.

That's important.

```
HTTP = Protocol
REST = Architectural style
```

So when someone says:

> "REST is a protocol"

that's incorrect.

---

## 5. REST is Resource-Oriented

This is probably the most important REST concept.

Think about **resources**.

For an e-commerce system:

- Users
- Products
- Orders
- Payments
- Reviews

Each resource can have a URI.

For example:

```
/users
/products
/orders
/payments
/reviews
```

Specific resource:

```
/users/101
/products/5001
/orders/9001
```

---

## 6. REST Uses HTTP Methods

REST APIs commonly map HTTP methods to operations.

| HTTP Method | Meaning |
|---|---|
| GET | Retrieve |
| POST | Create |
| PUT | Replace/update |
| PATCH | Partial update |
| DELETE | Delete |

For example:

```
GET    /products
GET    /products/101

POST   /products

PUT    /products/101

PATCH  /products/101

DELETE /products/101
```

---

## 7. GET

GET retrieves a resource.

Example:

```
GET /products/101
```

Response:

```json
{
  "id": 101,
  "name": "Laptop",
  "price": 75000
}
```

Conceptually:

```
Client
   |
   | GET /products/101
   ↓
Server
   |
   ↓
Find product
   |
   ↓
Response
```

GET should generally be **safe** and **idempotent**.

We'll discuss idempotency shortly.

---

## 8. POST

POST is commonly used to create a new resource or trigger an operation where the server assigns the resource identifier.

Example:

```
POST /products
Content-Type: application/json
```

Body:

```json
{
  "name": "Laptop",
  "price": 75000
}
```

Server:

```json
{
  "id": 101,
  "name": "Laptop",
  "price": 75000
}
```

Conceptually:

```
Client
   |
   | POST /products
   ↓
Server
   |
   ↓
Create product
   |
   ↓
Database
```

---

## 9. PUT

PUT is generally used to replace the representation of a resource at a known URI.

```
PUT /products/101
```

Body:

```json
{
  "name": "MacBook",
  "price": 100000
}
```

Think:

```
Existing Product
       ↓
Replace with
       ↓
New Representation
```

PUT is generally **idempotent**.

---

## 10. PATCH

PATCH is commonly used for partial modification.

Suppose:

```json
{
  "id": 101,
  "name": "Laptop",
  "price": 75000,
  "description": "Business laptop"
}
```

You only want to change the price.

```
PATCH /products/101
```

```json
{
  "price": 70000
}
```

Only the specified field is changed.

---

## 11. DELETE

DELETE removes a resource.

```
DELETE /products/101
```

Conceptually:

```
Client
   |
   | DELETE /products/101
   ↓
Server
   |
   ↓
Delete product 101
```

DELETE is generally considered idempotent: repeating the operation should result in the resource being absent, although the exact response may differ.

---

## 12. REST URL Design

**Good REST API:**

```
GET /users/101
GET /users/101/orders
GET /orders/5001
```

**Less REST-oriented:**

```
GET /getUser?id=101
GET /getUserOrders?id=101
POST /createOrder
POST /deleteOrder
```

Why?

REST encourages using:

```
Nouns = Resources
HTTP methods = Operations
```

Instead of putting the operation into the URL.

---

## 13. Think This Way

Instead of:

```
/createUser
/updateUser
/deleteUser
```

use:

```
POST   /users
PUT    /users/101
DELETE /users/101
```

The HTTP method tells you what you want to do.

---

## 14. REST API Request Structure

A typical HTTP REST request contains:

- HTTP Method
- URL
- Headers
- Body

Example:

```
POST /orders HTTP/1.1
Host: api.shop.com
Content-Type: application/json
Authorization: Bearer <token>

{
  "productId": 101,
  "quantity": 2
}
```

Let's break it down.

---

## 15. HTTP Method

```
POST
```

Tells the server the intended operation semantics.

---

## 16. URL

```
/orders
```

Identifies the resource collection.

Specific resource:

```
/orders/1001
```

---

## 17. Headers

Example:

```
Content-Type: application/json
Authorization: Bearer <token>
```

Headers provide metadata.

Examples:

- Authorization
- Content-Type
- Accept
- Cache-Control
- User-Agent

---

## 18. Request Body

For POST/PUT/PATCH, the client may send data:

```json
{
  "productId": 101,
  "quantity": 2
}
```

The server reads this body and processes it.

---

## 19. REST API Response

The server might return:

```
HTTP/1.1 201 Created
Content-Type: application/json

{
  "id": 1001,
  "status": "CREATED"
}
```

A response contains:

- Status code
- Headers
- Body

---

## 20. HTTP Status Codes

You should know these very well for system design interviews.

### 2xx — Success

```
200 OK
201 Created
202 Accepted
204 No Content
```

Examples:

```
GET /products/101
       ↓
200 OK
```

```
POST /orders
       ↓
201 Created
```

### 4xx — Client-side Error

```
400 Bad Request
401 Unauthorized
403 Forbidden
404 Not Found
409 Conflict
429 Too Many Requests
```

Examples:

```
GET /products/999999
       ↓
404 Not Found
```

### 5xx — Server-side Error

```
500 Internal Server Error
502 Bad Gateway
503 Service Unavailable
504 Gateway Timeout
```

These generally indicate a server or upstream-service problem.

---

## 21. Statelessness

One of REST's important architectural constraints is:

> **The server should not rely on stored client session state between requests.**

Suppose:

```
Request 1
GET /profile
Authorization: Bearer XYZ
```

Then:

```
Request 2
GET /orders
Authorization: Bearer XYZ
```

Each request contains the information needed to authenticate/authorize it.

Conceptually:

```
Request 1 ─────→ Server
                 |
                 └── Can process independently

Request 2 ─────→ Server
                 |
                 └── Can process independently
```

---

## 22. Why Statelessness Matters in System Design

Imagine:

```
             Load Balancer
                  |
       ┌──────────┼──────────┐
       ↓          ↓          ↓
    Server 1   Server 2   Server 3
```

Request 1:

```
Client → Server 1
```

Request 2:

```
Client → Server 3
```

Request 3:

```
Client → Server 2
```

If the requests are stateless, any server can process them.

This makes **horizontal scaling** much easier.

---

## 23. Stateless vs Stateful

### Stateful

```
Client
  |
  ↓
Server 1
  |
  └── Session stored here
```

Next request:

```
Client
  |
  ↓
Server 2
  |
  └── "I don't have the session"
```

Problem.

You may need sticky sessions or shared session storage.

### Stateless

```
Client
  |
  | Request + authentication/context
  ↓
Load Balancer
  |
  ├── Server 1
  ├── Server 2
  └── Server 3
```

Any server can handle the request.

---

## 24. Idempotency

This is very important for distributed systems.

An operation is **idempotent** if making the same request multiple times has the same intended effect as making it once.

Examples:

```
GET
PUT
DELETE
```

are generally defined as idempotent HTTP methods.

**POST** is generally **not** idempotent.

---

## 25. Why Idempotency Matters

Imagine a payment request:

```
POST /payments
```

The client sends:

```
₹1000 payment
```

But the network times out.

The client doesn't know whether the server processed it.

So it retries:

```
POST /payments
```

Potentially:

```
Payment 1 → ₹1000
Payment 2 → ₹1000
```

Customer gets charged twice.

This is a serious distributed-systems problem.

---

## 26. Idempotency Key

A common solution is an **idempotency key**.

```
POST /payments
Idempotency-Key: abc-123
```

Body:

```json
{
  "amount": 1000,
  "currency": "INR"
}
```

Server stores:

```
abc-123 → Payment result
```

If the same request arrives again:

```
Idempotency-Key: abc-123
```

the server recognizes it.

```
First request
     ↓
Process payment
     ↓
Store result


Retry
     ↓
Same key
     ↓
Return existing result
```

This is a major system-design concept.

---

## 27. REST API Example — Order System

Imagine an order service.

### Create Order

```
POST /orders
```

```json
{
  "customerId": 101,
  "items": [
    {
      "productId": 500,
      "quantity": 2
    }
  ]
}
```

Response:

```
201 Created
```

```json
{
  "orderId": 9001,
  "status": "CREATED"
}
```

### Get Order

```
GET /orders/9001
```

Response:

```json
{
  "orderId": 9001,
  "status": "CREATED",
  "total": 1500
}
```

### Update Order

```
PATCH /orders/9001
```

```json
{
  "status": "CANCELLED"
}
```

### Delete/Cancel

Depending on domain semantics, cancellation is often better represented as a domain operation such as:

```
POST /orders/9001/cancellation
```

rather than blindly using DELETE.

This is an important real-world point:

> **REST is about resource-oriented design, but not every business action maps cleanly to CRUD.**

---

## 28. REST API in Microservices

Suppose you have:

```
                  API Gateway
                       |
       ┌───────────────┼───────────────┐
       ↓               ↓               ↓
   User Service    Order Service   Payment Service
```

The API Gateway may expose:

```
GET  /users/101
GET  /orders/9001
POST /orders
POST /payments
```

Internally:

```
Client
   |
   | HTTPS
   ↓
API Gateway
   |
   | HTTP/REST
   ↓
Order Service
   |
   ↓
Database
```

---

## 29. REST vs Internal Communication

You might have:

**External:**

```
Mobile App
    ↓
REST API
    ↓
API Gateway
```

But internally:

```
Order Service
      ↓
     gRPC
      ↓
Payment Service
```

or:

```
Order Service
      ↓
Kafka
      ↓
Payment Service
```

So REST doesn't mean every communication in a distributed system must be REST.

**Different communication styles solve different problems.**

---

## 30. REST + JSON

Most modern REST APIs commonly use JSON.

Example:

```json
{
  "id": 101,
  "name": "Laptop",
  "price": 75000
}
```

But REST itself does not require JSON.

You can theoretically use:

- JSON
- XML
- HTML
- text
- binary representations

The representation is negotiated/indicated using HTTP mechanisms such as Content-Type and Accept.

---

## 31. REST API Versioning

As your API evolves:

```
/v1/users
/v2/users
```

For example:

```
GET /api/v1/products/101
```

Later:

```
GET /api/v2/products/101
```

Other versioning strategies include headers or query parameters, but URL versioning is easy to understand and commonly encountered.

---

## 32. Pagination

Imagine:

```
GET /products
```

There are:

```
10 million products
```

You should not return all of them.

Instead:

```
GET /products?page=1&limit=20
```

Response:

```json
{
  "data": [
    ...
  ],
  "page": 1,
  "limit": 20,
  "total": 10000000
}
```

For very large or frequently changing datasets, cursor-based pagination is often preferable:

```
GET /products?limit=20&cursor=abc123
```

---

## 33. Filtering

```
GET /products?category=laptop
```

Sorting:

```
GET /products?sort=price
```

Multiple filters:

```
GET /products?category=laptop&minPrice=50000&maxPrice=100000
```

These are query parameters.

---

## 34. REST and Caching

REST APIs can leverage HTTP caching.

For example:

```
GET /products/101
```

Response:

```
Cache-Control: max-age=3600
ETag: "abc123"
```

Then the client/cache may avoid downloading unchanged data repeatedly.

Architecture:

```
Client
   |
   ↓
CDN / Cache
   |
   ↓
API Server
   |
   ↓
Database
```

Caching can dramatically improve scalability.

---

## 35. REST API and HTTPS

In production, REST APIs are typically exposed over HTTPS:

```
Client
   |
   ↓
HTTPS
   |
   ↓
REST API
```

The stack is:

```
REST API
   ↓
HTTP
   ↓
TLS
   ↓
TCP
   ↓
IP
```

Or with HTTP/3:

```
REST API
   ↓
HTTP/3
   ↓
QUIC
   ↓
UDP
   ↓
IP
```

So now all the topics you've learned connect together.

---

## 36. Complete Request Journey

Suppose your mobile app calls:

```
GET https://api.shop.com/orders/9001
```

The journey is roughly:

```
Mobile App
     |
     ↓
DNS
     |
     ↓
IP address
     |
     ↓
Port 443
     |
     ↓
TCP connection
     |
     ↓
TLS handshake
     |
     ↓
HTTPS
     |
     ↓
HTTP GET
     |
     ↓
Load Balancer
     |
     ↓
API Gateway
     |
     ↓
Order Service
     |
     ↓
Database
```

Response travels back:

```
Database
    ↓
Order Service
    ↓
API Gateway
    ↓
Load Balancer
    ↓
HTTPS
    ↓
Mobile App
```

This is exactly why you're learning these networking concepts in sequence.

---

## 37. REST API vs HTTP

This distinction is crucial.

### HTTP

Defines things such as:

- GET
- POST
- PUT
- DELETE
- Headers
- Status codes
- URLs
- HTTP/1.1
- HTTP/2
- HTTP/3

### REST

Provides architectural principles such as:

- Resources
- Stateless interactions
- Uniform interface
- Cacheability
- Layered system
- Client-server separation

So:

```
REST
  ↓
Architectural style

HTTP
  ↓
Protocol used commonly to implement REST APIs
```

---

## 38. REST API vs SOAP

You may encounter this in interviews.

| REST | SOAP |
|---|---|
| Architectural style | Protocol/specification |
| Commonly uses HTTP | Can use HTTP and other transports |
| Often JSON | Commonly XML |
| Lightweight | More formal/feature-heavy |
| Resource-oriented | Operation/service-oriented |
| Common in modern web APIs | Still used in many enterprise/legacy systems |

Don't say:

> REST is always better than SOAP.

It depends on the requirements and ecosystem.

---

## 39. REST API vs gRPC

This is particularly important for microservices.

| REST | gRPC |
|---|---|
| Commonly HTTP/1.1 or HTTP/2/HTTP/3 | Commonly HTTP/2 |
| Often JSON | Protocol Buffers commonly |
| Human-readable payloads | Compact binary payloads |
| Simple/browser-friendly | Excellent service-to-service communication |
| Resource-oriented style | RPC-oriented |
| Easy public API exposure | Strong contracts/code generation |

A common architecture:

```
Internet
   |
 HTTPS REST
   ↓
API Gateway
   |
   | gRPC
   ↓
Microservices
```

---

## 40. REST API Design Checklist

When designing a REST API, think about:

1. Resources
2. URLs
3. HTTP methods
4. Status codes
5. Request/response structure
6. Authentication
7. Authorization
8. Idempotency
9. Pagination
10. Filtering
11. Sorting
12. Versioning
13. Caching
14. Rate limiting
15. Error handling
16. Observability

For system design interviews, this checklist is extremely useful.

---

## 41. One Complete Example

Suppose we're designing an Order Management API.

### Create Order

```
POST /orders
```

```json
{
  "customerId": 101,
  "items": [
    {
      "productId": 500,
      "quantity": 2
    }
  ]
}
```

Response:

```
201 Created
```

```json
{
  "orderId": 9001,
  "status": "CREATED"
}
```

### Get Order

```
GET /orders/9001
```

### Get Customer's Orders

```
GET /customers/101/orders
```

### Update Order

```
PATCH /orders/9001
```

### Cancel Order

Depending on your domain:

```
POST /orders/9001/cancellation
```

### Error

```
GET /orders/999999
```

Response:

```
404 Not Found
```

```json
{
  "code": "ORDER_NOT_FOUND",
  "message": "Order does not exist"
}
```

---

## 42. The System Design Mental Model

Put everything you've learned together:

```
                         CLIENT
                           |
                           ↓
                          DNS
                           |
                           ↓
                      IP Address
                           |
                           ↓
                         :443
                           |
                           ↓
                    TCP / QUIC
                           |
                           ↓
                         TLS
                           |
                           ↓
                    HTTP/1.1/2/3
                           |
                           ↓
                       REST API
                           |
                           ↓
                    Load Balancer
                           |
                           ↓
                     API Gateway
                           |
              ┌────────────┼────────────┐
              ↓            ↓            ↓
          User Service  Order Service Payment
              |            |            |
              ↓            ↓            ↓
             DB           DB           DB
```

### Remember These 5 Statements:

1. **API** = interface through which software communicates.
2. **REST** = architectural style for designing APIs around resources.
3. **HTTP** = common protocol used to implement REST APIs.
4. **HTTP methods** express operation semantics: GET, POST, PUT, PATCH, DELETE.
5. **Statelessness + idempotency + caching + proper status codes** become especially important when REST APIs are used in distributed systems.

---

The next important concept after REST APIs is **API Gateway**—especially Reverse Proxy vs Load Balancer vs API Gateway, because in a real system a client usually doesn't directly call every microservice.
