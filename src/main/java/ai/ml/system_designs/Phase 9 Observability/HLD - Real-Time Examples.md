# HLD Creation with Real-Time Examples

Since you have ~10 years of experience, it's useful to learn HLD as an **interview + production architecture process**, rather than just learning how to draw boxes.

> **HLD = Requirements → Scale → Components → Data → Communication → Scaling → Reliability → Security → Observability**

Let's create HLDs using real-world systems.

---

## 1. First Understand the HLD Process

Whenever someone says:

> "Design an Uber-like system."

Don't immediately start drawing:

```
Client → API Gateway → Services → DB
```

Instead, follow this sequence:

```
1. Requirements
       ↓
2. Scale estimation
       ↓
3. Core APIs
       ↓
4. High-level components
       ↓
5. Data storage
       ↓
6. Communication
       ↓
7. Caching
       ↓
8. Scaling
       ↓
9. Reliability
       ↓
10. Security
       ↓
11. Observability
       ↓
12. Bottlenecks / trade-offs
```

This is the fundamental HLD creation process.

---

## 2. Real-Time Example #1 — URL Shortener

Let's start with a relatively simple system.

Think about:

```
bit.ly/abc123
```

which redirects to:

```
https://example.com/very/long/url/...
```

### Requirements

**Functional**

1. Create short URL
2. Redirect short URL
3. Track number of clicks

**Non-functional**

- High availability
- Low latency
- Scalable
- Durable

---

## 3. Estimate Traffic

Suppose:

```
100 million URLs
10 million new URLs/day
1 billion redirects/day
```

Average redirect requests:

```
1,000,000,000 / 86,400
≈ 11,574 requests/sec
```

Peak might be several times higher.

This immediately tells us:

> **Redirect traffic is much higher than URL creation traffic.**

Therefore the architecture should optimize heavily for read/redirect operations.

---

## 4. Identify Components

We can start with:

```
                  Client
                    |
                    v
              Load Balancer
                    |
                    v
               API Gateway
                    |
             +------+------+
             |             |
             v             v
       URL Service    Redirect Service
             |             |
             v             v
          Database       Cache
```

---

## 5. Create URL Flow

Client:

```
POST /urls
```

Request:

```json
{
  "longUrl": "https://example.com/very/long/url"
}
```

Flow:

```
Client
   |
   | POST /urls
   v
API Gateway
   |
   v
URL Service
   |
   +---- Generate ID
   |
   +---- Store mapping
   |
   v
Database
```

Database:

```
short_code    long_url
-----------   ----------------------------
abc123        https://example.com/...
xyz789        https://google.com/...
```

Response:

```
https://short.com/abc123
```

---

## 6. Redirect Flow

User accesses:

```
GET /abc123
```

Architecture:

```
Client
   |
   v
Load Balancer
   |
   v
Redirect Service
   |
   v
Redis
   |
   +---- HIT → return URL
   |
   +---- MISS
          |
          v
       Database
```

Because redirects are extremely frequent, Redis can significantly reduce database reads.

This is a typical example of:

> **Cache-aside architecture**

---

## 7. Complete URL Shortener HLD

```
                         Client
                           |
                           v
                    Load Balancer
                           |
                           v
                     API Gateway
                           |
                 +---------+---------+
                 |                   |
                 v                   v
           URL Service        Redirect Service
                 |                   |
                 |                   v
                 |                 Redis
                 |                   |
                 v                   v
              Database <--------- Cache Miss
                 |
                 v
            Replicas
```

For analytics:

```
Redirect Service
      |
      v
    Kafka
      |
      +---- Analytics Service
      |
      +---- Click Counter
      |
      +---- Reporting
```

Now we have asynchronous processing.

---

## 8. Why Kafka?

Suppose every redirect needs analytics.

**Bad approach:**

```
Redirect
   |
   +---- Save analytics
   |
   +---- Update counter
   |
   +---- Return response
```

The redirect becomes slower.

**Instead:**

```
Redirect Service
      |
      | publish Clicked event
      v
    Kafka
      |
      +---- Analytics
      +---- Counter
      +---- Reporting
```

The user gets the redirect without waiting for all downstream processing.

---

## 9. Real-Time Example #2 — E-Commerce System

Now let's design something closer to Amazon/Flipkart-style architecture.

### Requirements

Customer should be able to:

- Browse products
- Search products
- Add to cart
- Place order
- Pay
- Track order
- Cancel order

---

## 10. High-Level Architecture

```
                           Client
                         /        \
                      Web         Mobile
                         \        /
                          \      /
                         API Gateway
                              |
        +---------------------+----------------------+
        |          |           |         |            |
        v          v           v         v            v
   User Service Product    Cart      Order       Search
                 Service   Service   Service      Service
                    |         |        |
                    v         v        v
                  DB       Redis    Order DB

                              Order Service
                                   |
                                   v
                                 Kafka
                         /         |         \
                        /          |          \
                       v           v           v
                  Payment      Inventory   Notification
                   Service      Service       Service
                     |            |              |
                     v            v              v
                 Payment DB   Inventory DB    Email/SMS
```

---

## 11. Product Read Flow

Product pages are usually read much more frequently than products are modified.

So:

```
Client
  |
  v
API Gateway
  |
  v
Product Service
  |
  v
Redis
  |
  +---- HIT → Product
  |
  +---- MISS
          |
          v
      Product DB
          |
          v
        Redis
```

This reduces database load.

---

## 12. Search Architecture

Don't necessarily use the relational database for every complex product search.

Instead:

```
Product DB
    |
    | ProductUpdated event
    v
  Kafka
    |
    v
Search Index
```

For example:

```
Product Service
      |
      v
    Kafka
      |
      v
 Elasticsearch/OpenSearch
```

Then:

```
User
 |
 | "iPhone 17 256GB"
 v
Search Service
 |
 v
Search Index
```

This allows optimized search capabilities.

---

## 13. Order Creation

Customer clicks:

```
Place Order
```

Flow:

```
Client
   |
   v
API Gateway
   |
   v
Order Service
   |
   +---- Validate Cart
   |
   +---- Check/Reserve Inventory
   |
   +---- Create Order
   |
   +---- Initiate Payment
   |
   v
Order DB
```

After successful order creation:

```
Order Service
      |
      v
OrderCreated Event
      |
      v
Kafka
   /     |       \
  /      |        \
 v       v         v
Payment Inventory Notification
```

---

## 14. Important Distributed-System Problem

Suppose:

```
Order Service
      |
      +---- Save order to DB      SUCCESS
      |
      +---- Publish Kafka event   FAILURE
```

Now:

```
Database → Order exists
Kafka → No event
```

This creates inconsistency.

A common HLD solution is:

> **Transactional Outbox Pattern**

Architecture:

```
             Order Service
                  |
                  v
          Database Transaction
             /          \
            /            \
           v              v
      Order Table     Outbox Table
                          |
                          v
                    Outbox Publisher
                          |
                          v
                        Kafka
```

This is an important senior-level HLD discussion.

---

## 15. Real-Time Example #3 — Food Delivery

Consider an application similar to Swiggy/Zomato.

### Requirements

- Customer searches restaurants
- Customer places order
- Restaurant accepts order
- Payment processed
- Delivery partner assigned
- Order tracked
- Customer receives notification

### Architecture

```
Client
  |
  v
API Gateway
  |
  +---- Restaurant Service
  |
  +---- Menu Service
  |
  +---- Order Service
  |
  +---- Payment Service
  |
  +---- Delivery Service
  |
  +---- Tracking Service
  |
  +---- Notification Service
```

---

## 16. Real-Time Location Tracking

This is where HLD becomes interesting.

Suppose delivery partners continuously send locations:

```
Driver → GPS coordinates → Tracking Service
```

Potentially:

```
100,000 drivers
5 location updates/sec
```

That's:

```
500,000 location updates/sec
```

You probably don't want every update synchronously written into a traditional relational database.

Possible architecture:

```
Driver App
    |
    v
Location Gateway
    |
    v
Kafka
    |
    v
Tracking Service
    |
    v
Redis / Geospatial Store
    |
    v
Customer App
```

Redis can be used for frequently changing location state.

Historical location data can be stored separately if needed.

---

## 17. Real-Time Example #4 — Banking / Payment System

Payment systems require very different design priorities.

### Requirements

- Create payment
- Authorize payment
- Capture payment
- Refund payment
- Prevent duplicate payment
- Track payment state

### Architecture

```
Client
  |
  v
API Gateway
  |
  v
Payment Service
  |
  +---- Payment DB
  |
  +---- Idempotency Store
  |
  +---- Payment Gateway
  |
  +---- Kafka
```

---

## 18. Idempotency

Imagine the customer clicks:

```
Pay
```

Request reaches server.

Payment succeeds.

But response is lost.

Client retries.

**Without idempotency:**

```
Request 1 → ₹100 charged
Request 2 → ₹100 charged
```

Customer gets charged twice.

**With an idempotency key:**

```
Idempotency-Key: abc-123
```

Server stores:

```
abc-123 → Payment SUCCESS → transactionId=TX100
```

Retry:

```
abc-123
   |
   v
Already processed
   |
   v
Return previous result
```

This is a critical HLD concept for payment systems.

---

## 19. Real-Time Example #5 — Ride Booking

Consider Uber-like ride booking.

### Requirements

- Customer requests ride
- Find nearby drivers
- Driver accepts
- Trip starts
- Trip ends
- Payment
- Tracking

### Architecture

```
                   Client
                     |
                     v
                API Gateway
                     |
        +------------+-------------+
        |            |             |
        v            v             v
   Ride Service  Driver Service  Payment
        |            |
        |            v
        |       Location Service
        |            |
        v            v
      Redis       Geo Index
        |
        v
      Kafka
```

---

## 20. Finding Nearby Drivers

A naive implementation:

```sql
SELECT *
FROM drivers
WHERE latitude BETWEEN ...
AND longitude BETWEEN ...
```

This doesn't scale well for high-frequency geospatial queries.

Instead, you might use:

```
Driver GPS
    |
    v
Location Service
    |
    v
Geo-enabled store
    |
    v
Nearby drivers
```

The exact technology depends on requirements, scale, consistency, and geographic model.

---

## 21. Real-Time Example #6 — Notification System

Suppose your company has:

- Order Service
- Payment Service
- Delivery Service
- User Service

All need notifications.

**Instead of:**

```
Order Service → Email Service
Payment Service → Email Service
Delivery Service → Email Service
```

you can create:

```
                    Kafka
                      |
          +-----------+-----------+
          |           |           |
          v           v           v
       Email        SMS         Push
      Worker       Worker       Worker
```

Events:

```
OrderCreated
PaymentSuccessful
OrderShipped
OrderDelivered
```

This provides decoupling and allows each notification channel to scale independently.

---

## 22. How to Decide Database

One of the most important HLD decisions is:

> **Why this database?**

Don't say:

> "Let's use MongoDB because it's scalable."

Explain the access pattern.

### SQL

Good when you need:

- Transactions
- Relationships
- Strong consistency
- Complex queries
- Structured data

Examples:

- Orders
- Payments
- Bank transactions
- Inventory

### NoSQL

Can be appropriate for:

- Very high scale
- Flexible schema
- Specific access patterns
- Large distributed datasets

### Redis

Useful for:

- Caching
- Sessions
- Counters
- Rate limiting
- Distributed coordination
- Short-lived state

### Kafka

Not a database replacement.

It's primarily used for:

- Event streaming
- Asynchronous communication
- Buffering
- Decoupling
- Replay

---

## 23. How to Decide Cache

Ask:

- Is the data read frequently?
- Does it change relatively infrequently?
- Can the application tolerate cache-related staleness?
- Is database load significant?

If yes:

```
Service
  |
  v
Redis
  |
  v
Database
```

Typical examples:

- Product details
- User profile
- Configuration
- Popular restaurants
- Frequently accessed metadata

---

## 24. How to Decide Kafka

Ask:

> Does the caller need the result immediately?

**If yes:**

```
Service A
   |
 HTTP/gRPC
   v
Service B
```

**If no:**

```
Service A
   |
   v
 Kafka
   |
   v
Service B
```

Kafka is particularly useful when:

- Multiple consumers
- High event volume
- Need buffering
- Need replay
- Loose coupling
- Asynchronous processing

---

## 25. HLD Scaling

Suppose:

```
1 server → 1,000 requests/sec
```

Traffic becomes:

```
10,000 requests/sec
```

Horizontal scaling:

```
                 Load Balancer
                /      |      \
               v       v       v
            Server1 Server2 Server3
```

Then:

```
                Load Balancer
                      |
        +-------------+-------------+
        |             |             |
        v             v             v
      Node1         Node2         Node3
        |             |             |
        +-------------+-------------+
                      |
                    Redis
                      |
                    DB
```

For the database, you might need:

- Read replicas
- Partitioning
- Sharding
- Caching
- Connection pooling

---

## 26. HLD Reliability

For every important component, ask:

> What happens if this component fails?

Example:

```
Order Service
     |
     X
Payment Service DOWN
```

Possible mechanisms:

- Timeout
- Retry
- Exponential backoff
- Circuit breaker
- Fallback
- Queue
- Idempotency

For asynchronous processing:

```
Kafka
  |
  v
Consumer
  |
  X processing failure
  |
  v
Retry Topic
  |
  v
Consumer
  |
  X
  |
  v
Dead Letter Topic
```

---

## 27. HLD Availability

**Bad:**

```
             Service
                |
              Server
```

**Better:**

```
              Load Balancer
               /         \
              v           v
           Server1      Server2
```

Database:

```
          Primary
          /     \
         v       v
    Replica1   Replica2
```

Now the system doesn't rely on a single machine.

---

## 28. HLD Security

A production HLD should show:

```
Client
   |
 HTTPS/TLS
   |
API Gateway
   |
Authentication
   |
Authorization
   |
Services
```

Security components can include:

- OAuth 2.0
- OIDC
- JWT
- TLS
- Secrets Management
- Encryption
- Rate Limiting
- DDoS Protection

Never put things such as:

- Passwords
- API keys
- Private keys
- Database credentials

directly into source code.

---

## 29. HLD Observability

Production architecture should include:

```
                Services
               /   |    \
              /    |     \
             v     v      v
          Logs  Metrics  Traces
             \    |      /
              \   |     /
               Observability
                  |
        +---------+---------+
        |         |         |
      Search    Dashboard  Alerts
```

You should be able to answer:

```
What is happening?
       ↓
Metrics

What exactly happened?
       ↓
Logs

Where did the request spend time?
       ↓
Distributed Tracing
```

---

## 30. Complete HLD Creation Formula

For any system, use this mental framework:

```
                    REQUIREMENTS
                         |
                         v
                 +---------------+
                 | Scale Estimate|
                 +-------+-------+
                         |
                         v
                 +---------------+
                 | API / Use Case |
                 +-------+-------+
                         |
                         v
                 +---------------+
                 | Major Services|
                 +-------+-------+
                         |
             +-----------+-----------+
             |           |           |
             v           v           v
          Database      Cache       Kafka
             |           |           |
             +-----------+-----------+
                         |
                         v
                    Scalability
                         |
                         v
                    Reliability
                         |
                         v
                      Security
                         |
                         v
                   Observability
                         |
                         v
                    Trade-offs
```

---

## 31. What Makes an HLD Good?

A good HLD isn't the architecture with the most boxes.

> **It is an architecture where every major decision has a reason.**

For example:

**Bad explanation:**

> "We'll use Redis."

**Good explanation:**

> "Product reads are significantly higher than product writes, so we'll use Redis as a cache to reduce database load and improve read latency. We'll use a cache-aside strategy and define an appropriate TTL/invalidation strategy."

**Bad:**

> "We'll use Kafka."

**Good:**

> "Order creation publishes an event to Kafka because notification and analytics don't need to block the order request. Kafka also decouples these consumers and provides buffering during downstream load spikes."

**Bad:**

> "We'll use microservices."

**Good:**

> "We can separate Order, Payment, Inventory, and Notification because they have different responsibilities, scaling characteristics, data ownership, and failure boundaries."

---

## 32. HLD Interview Approach

For a 45-minute system-design interview, a practical flow is:

### 0–5 min
Requirements + assumptions

### 5–10 min
Scale estimation

### 10–20 min
Core architecture

### 20–30 min
Database + cache + Kafka + APIs

### 30–40 min
Scaling + reliability + consistency

### 40–45 min
Security + observability + bottlenecks

And continuously explain:

> "I'm choosing X because..."

That is much more valuable than simply drawing components.

---

## 33. HLD vs LLD — Final Picture

```
                 SYSTEM DESIGN
                      |
             +--------+--------+
             |                 |
             v                 v
            HLD               LLD
             |                 |
       Major Components    Classes
       Services            Interfaces
       Databases           Methods
       APIs                Objects
       Kafka               Patterns
       Cache               Relationships
       Scaling             Sequences
       Reliability
             |
             v
       Architecture
             |
             v
           Code
```

### One-line memory trick

> **HLD tells you how the whole system is structured; LLD tells you how each component is implemented internally.**

For your preparation, the most useful next step is to practice HLD in increasing difficulty:

**URL Shortener → Rate Limiter → Notification System → E-commerce → Food Delivery → Uber → YouTube → Netflix → WhatsApp → Payment System → Distributed File Storage.**

