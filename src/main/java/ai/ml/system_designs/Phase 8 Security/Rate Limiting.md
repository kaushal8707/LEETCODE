# Rate Limiting

**Rate Limiting** is a technique used to control how many requests a user, client, IP, service, or API can make within a specific period of time.

The main purpose is:

> Protect a system from excessive traffic, abuse, accidental overload, and denial-of-service-like traffic while ensuring fair resource usage.

For example:

```
User
  |
  | 100 requests/minute
  v
Rate Limiter
  |
  +---- Requests <= 100 → ALLOW
  |
  +---- Requests > 100  → REJECT (429)
```

The HTTP status code commonly used when the limit is exceeded is:

```
429 Too Many Requests
```

---

## 1. Why Do We Need Rate Limiting?

Imagine your API normally handles:

```
10,000 requests/second
```

Suddenly one client sends:

```
1,000,000 requests/second
```

**Without rate limiting:**

```
                    1,000,000 req/sec
                           |
                           v
                    +-------------+
                    | API Gateway |
                    +------+------+
                           |
                           v
                    +-------------+
                    | Application |
                    +------+------+
                           |
                           v
                       Database
                           |
                           X
                     Overloaded
```

Possible consequences:

- CPU exhaustion
- Memory exhaustion
- Database overload
- Connection pool exhaustion
- Increased latency
- Cascading failures
- Service outage

**With rate limiting:**

```
                    1,000,000 req/sec
                           |
                           v
                    +-------------+
                    | Rate Limiter|
                    +------+------+
                           |
                     Only allowed
                       traffic
                           |
                           v
                    +-------------+
                    | Application |
                    +-------------+
```

The excessive traffic gets rejected or delayed before reaching expensive downstream resources.

---

## 2. Simple Example

Suppose we define:

```
100 requests / minute / user
```

User Alice sends requests:

```
Request 1   → ALLOW
Request 2   → ALLOW
Request 3   → ALLOW
...
Request 100 → ALLOW
Request 101 → REJECT
```

Response:

```
HTTP/1.1 429 Too Many Requests
```

The limiter may also return information such as:

```
Retry-After: 30
```

meaning the client should retry after approximately 30 seconds.

---

## 3. Where Does Rate Limiting Happen?

In a microservices architecture, rate limiting is commonly implemented at the API Gateway, edge proxy, WAF/CDN layer, or sometimes inside individual services.

Example:

```
                Internet
                   |
                   v
              +---------+
              |   WAF   |
              +----+----+
                   |
                   v
           +---------------+
           | API Gateway   |
           |               |
           | Rate Limiter  |
           +-------+-------+
                   |
          +--------+--------+
          |        |        |
          v        v        v
       Order    Payment    User
       Service  Service    Service
```

This is useful because abusive traffic can be stopped before it reaches the application.

---

## 4. How Rate Limiting Works

At a high level:

```
Request
   |
   v
Identify client
   |
   v
Check current usage
   |
   v
Compare with limit
   |
   +--------+
   |        |
   v        v
Allowed   Exceeded
   |        |
   v        v
Forward   429
request
```

For example:

```
Limit = 100 requests/minute

Current count = 72

72 < 100
   ↓
ALLOW
```

But:

```
Current count = 100

100 >= 100
   ↓
REJECT
```

---

## 5. What Can We Rate Limit By?

This is very important in system design.

You can limit based on:

### IP address

```
10.20.30.40
   ↓
100 requests/minute
```

Useful for anonymous APIs.

But NAT/proxies can cause many users to share an IP.

### User ID

```
userId = 12345
   ↓
1000 requests/minute
```

Useful after authentication.

### API Key

```
apiKey = abc123
   ↓
10,000 requests/hour
```

Common for public/developer APIs.

### Client/application

```
Mobile App → 10,000 req/min
Web App    → 20,000 req/min
Partner A  → 50,000 req/min
```

### Endpoint

Different APIs can have different limits:

```
GET /products
→ 1000 req/min

POST /orders
→ 100 req/min

POST /login
→ 10 req/min
```

The login endpoint should often have a much stricter limit because authentication endpoints are attractive targets for brute-force attacks.

---

## 6. Rate Limiting Algorithms

The major algorithms you should know for system design interviews are:

1. Fixed Window
2. Sliding Window
3. Sliding Window Counter
4. Token Bucket
5. Leaky Bucket

The two especially important ones are **Token Bucket** and **Sliding Window**.

---

## 7. Fixed Window

The simplest implementation.

Suppose:

```
Limit = 100 requests/minute
```

We divide time into windows:

```
10:00:00 ───────── 10:00:59
10:01:00 ───────── 10:01:59
10:02:00 ───────── 10:02:59
```

For each window:

```
counter = 0
```

Every request:

```
counter++
```

If:

```
counter <= 100
```

allow.

Otherwise:

```
429
```

### Example

```
10:00:00 → request 1
10:00:01 → request 2
...
10:00:59 → request 100
```

Request 101:

```
→ REJECT
```

At:

```
10:01:00
```

the counter resets.

### Problem with Fixed Window

There is a **boundary problem**.

Suppose limit is:

```
100 requests/minute
```

A client sends:

```
10:00:59 → 100 requests
10:01:00 → 100 requests
```

That's:

```
200 requests
```

within approximately:

```
2 seconds
```

So fixed windows can allow bursts at boundaries.

---

## 8. Sliding Window

Instead of fixed time boundaries, we look at the last N seconds/minutes.

Example:

```
Limit = 100 requests / rolling 60 seconds
```

At any point:

```
Current Time
     |
     v
<---- Last 60 seconds ---->
```

If there are already 100 requests in that rolling window:

```
New request
    |
    v
REJECT
```

Otherwise:

```
ALLOW
```

This handles boundary problems better.

---

## 9. Sliding Window Using Request Timestamps

Conceptually, store timestamps:

```
[10:00:01,
 10:00:03,
 10:00:07,
 10:00:10,
 ...]
```

For a new request at:

```
10:00:30
```

remove timestamps older than:

```
10:00:30 - 60 sec
```

Then count remaining requests.

If:

```
count < 100
```

allow.

Otherwise:

```
429
```

The downside is that storing every request timestamp can consume more memory.

---

## 10. Token Bucket

**Token Bucket** is one of the most important algorithms for system design.

Imagine a bucket containing tokens.

```
          Token Bucket
       +---------------+
       | ● ● ● ● ●     |
       | ● ● ● ●       |
       +---------------+
              |
              v
           Request
```

Each request consumes one token.

If a token exists:

```
Request → consume token → ALLOW
```

If no token exists:

```
Request → no token → REJECT
```

---

## 11. Token Refill

Tokens are added at a fixed rate.

Suppose:

```
Bucket capacity = 10 tokens
Refill rate = 2 tokens/sec
```

Initially:

```
10 tokens
```

Request:

```
Request 1 → token consumed
Request 2 → token consumed
```

Remaining:

```
8 tokens
```

After one second:

```
8 + 2 = 10
```

But the bucket cannot exceed its capacity:

```
max = 10
```

---

## 12. Token Bucket Allows Bursts

This is a major advantage.

Suppose:

```
Bucket capacity = 100
Refill rate = 10 tokens/sec
```

If the application hasn't received requests for a while, the bucket may contain:

```
100 tokens
```

The client can immediately send:

```
100 requests
```

and they can all be allowed.

After that:

```
10 requests/sec
```

can continue to be allowed as tokens refill.

So:

```
Token Bucket
     |
     +---- Controls average rate
     |
     +---- Allows controlled bursts
```

---

## 13. Leaky Bucket

The **Leaky Bucket** model behaves more like a queue.

Imagine:

```
Requests
   |
   v
+----------------+
|     Queue      |
| req req req    |
+-------+--------+
        |
        | fixed rate
        v
      Server
```

Requests enter the bucket and leave at a controlled rate.

For example:

```
10 requests/sec
```

Even if 100 requests arrive at once, the system processes them at the configured rate, assuming the queue has capacity.

If the queue is full:

```
New request
    |
    v
REJECT
```

---

## 14. Token Bucket vs Leaky Bucket

| Feature | Token Bucket | Leaky Bucket |
|---|---|---|
| Burst traffic | Allows controlled bursts | Smooths traffic |
| Processing | Request consumes token | Requests wait in queue |
| Main idea | Limit consumption rate | Fixed output rate |
| Common use | API rate limiting | Traffic shaping |

For API rate limiting, **Token Bucket** is often a very practical choice.

---

## 15. Distributed Rate Limiting

This is where system design becomes more interesting.

Suppose you have:

```
             Load Balancer
                  |
        +---------+---------+
        |         |         |
        v         v         v
     API-1     API-2     API-3
```

Suppose the limit is:

```
100 requests/minute/user
```

If each application server maintains its own counter:

```
API-1 → 100
API-2 → 100
API-3 → 100
```

The user could potentially send:

```
300 requests/minute
```

instead of 100.

That's a problem.

---

## 16. Centralized Rate Limiter

A common solution is a shared distributed store such as **Redis**.

```
                  API Gateway
                       |
             +---------+---------+
             |         |         |
             v         v         v
           API-1     API-2     API-3
             |         |         |
             +---------+---------+
                       |
                       v
                     Redis
                       |
                       v
                Rate Limit State
```

All instances use the same counter/token state.

For example:

```
rate:user:12345
```

Redis stores the user's current rate-limit state.

---

## 17. Why Redis?

Redis is commonly used because it provides:

- Very fast reads/writes
- Atomic operations
- TTL/expiration
- Shared state between application instances
- Lua scripts for atomic multi-step operations
- High throughput

Conceptually:

```
Request
   |
   v
Redis
   |
   +---- check tokens/counter
   |
   +---- update state atomically
   |
   v
ALLOW / REJECT
```

---

## 18. Race Condition

Suppose two requests arrive simultaneously:

```
Request A ──┐
            ├──> Redis
Request B ──┘
```

Both might read:

```
count = 99
```

Both think:

```
99 < 100 → ALLOW
```

Then both increment:

```
100
```

Potentially allowing more requests than intended.

Therefore distributed rate limiting needs **atomic operations**.

For example:

```
CHECK + UPDATE
```

should happen atomically.

Redis Lua scripts or suitable atomic Redis operations can be used.

---

## 19. Rate Limiting Architecture

A typical production architecture:

```
                    Internet
                       |
                       v
                  +---------+
                  |   WAF   |
                  +----+----+
                       |
                       v
               +---------------+
               | API Gateway   |
               +-------+-------+
                       |
                       v
               +---------------+
               | Rate Limiter  |
               +-------+-------+
                       |
                 +-----+-----+
                 |           |
                 v           v
              Redis      Metrics
                 |
                 v
           Rate-limit state
                       |
                       v
              Application Services
```

The gateway can reject excessive traffic before forwarding it.

---

## 20. Multi-Level Rate Limiting

Real production systems often use multiple limits.

For example:

**IP limit:**

```
1000 req/min
```

AND:

**User limit:**

```
500 req/min
```

AND:

**API limit:**

```
100 req/min
```

AND:

**Global system limit:**

```
100,000 req/sec
```

So a request may go through:

```
Request
   |
   v
IP Limit
   |
   v
User Limit
   |
   v
Endpoint Limit
   |
   v
Global Limit
   |
   v
Application
```

This provides **defense in depth**.

---

## 21. Different APIs Need Different Limits

Consider an e-commerce system:

```
GET /products
→ 1000 requests/minute

GET /orders
→ 100 requests/minute

POST /orders
→ 30 requests/minute

POST /login
→ 10 requests/minute
```

Why?

Because these operations have different costs and security risks.

For example:

```
POST /login
```

can be abused for:

- Brute force
- Credential stuffing
- Password guessing

So it should have a stricter limit.

---

## 22. Rate Limiting vs Throttling

These terms are sometimes used interchangeably, but there is a useful distinction.

### Rate limiting

Controls how many requests are allowed.

```
100 req/min
```

Excess:

```
429
```

### Throttling

Can mean slowing or controlling traffic rather than immediately rejecting everything above a limit.

For example:

```
Requests
   |
   v
Queue
   |
   | controlled rate
   v
Service
```

---

## 23. Rate Limiting vs Backpressure

These are related but different.

### Rate Limiting

Usually controls incoming request rate.

```
Client
  |
  v
Rate Limiter
  |
  +---- 429
```

### Backpressure

Controls how a system behaves when downstream processing cannot keep up.

```
Producer
   |
   v
Queue
   |
   v
Consumer
   |
   X
Too slow
```

Backpressure tells upstream components:

> "Slow down; I can't process this fast."

Rate limiting says:

> "You are only allowed to send this many requests."

---

## 24. Rate Limiting and DDoS

Rate limiting can help with abusive traffic, but it is **not** a complete DDoS solution.

For example:

```
Internet
   |
   v
CDN / DDoS Protection
   |
   v
WAF
   |
   v
Rate Limiter
   |
   v
API Gateway
   |
   v
Services
```

Large volumetric attacks may need infrastructure-level DDoS protection before traffic even reaches your API gateway.

---

## 25. Rate Limit Headers

An API can communicate limits to clients.

For example:

```
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 35
X-RateLimit-Reset: 1690000000
```

Or standardized headers may be used depending on the API design.

When exceeded:

```
HTTP/1.1 429 Too Many Requests
Retry-After: 30
```

The client can then implement controlled retry behavior.

---

## 26. Important: Don't Retry 429 Aggressively

Suppose:

```
Client → API
        |
        v
       429
```

If the client immediately retries:

```
retry → 429
retry → 429
retry → 429
retry → 429
```

it makes the problem worse.

Instead:

```
429
 ↓
Retry-After
 ↓
Wait
 ↓
Exponential backoff + jitter
 ↓
Retry
```

This is especially important in distributed systems.

---

## 27. Real-World Example

Imagine a payment API:

```
POST /payments
```

We configure:

```
10 requests/minute/user
```

The user sends:

```
1 → ALLOW
2 → ALLOW
3 → ALLOW
...
10 → ALLOW
11 → 429
```

Architecture:

```
                    Client
                       |
                       v
                  API Gateway
                       |
                       v
               Rate Limiter
                       |
                       v
                    Redis
                       |
                 +-----+-----+
                 |           |
             allowed       limit
                 |          exceeded
                 v             |
           Payment Service      v
                 |             429
                 v
              Payment
```

This prevents a buggy client from accidentally creating hundreds of payment requests.

For financial operations, rate limiting should be combined with **idempotency**, because rate limiting alone doesn't prevent duplicate requests.

---

## 28. Rate Limiting vs Quotas

Another useful interview distinction.

### Rate limit

Controls requests over a short time period:

```
100 requests/minute
```

### Quota

Controls total consumption over a larger period:

```
1 million API calls/month
```

Example:

```
Rate Limit:
100 req/min

Quota:
1,000,000 req/month
```

A customer can have both.

---

## 29. How to Choose a Rate-Limiting Algorithm

A useful decision table:

| Requirement | Good Choice |
|---|---|
| Very simple implementation | Fixed Window |
| More accurate rolling limit | Sliding Window |
| Controlled bursts | Token Bucket |
| Smooth constant output | Leaky Bucket |
| Distributed API gateway | Token Bucket + Redis |
| Strict rolling request count | Sliding Window |

For most API gateway designs, a good starting point is:

```
Token Bucket
      +
Redis
      +
Atomic operations
```

---

## 30. Senior-Level Design Considerations

In a system-design interview, don't stop at:

> "I'll use Redis for rate limiting."

Discuss these questions:

### What is the key?

- userId?
- API key?
- IP?
- endpoint?
- tenant?

### What is the limit?

- 100 req/min?
- 1000 req/sec?

### Is it global or per-user?

- Global
- Per-user
- Per-IP
- Per-tenant
- Per-endpoint

### Where is it enforced?

- CDN
- WAF
- API Gateway
- Service

### What happens when Redis is unavailable?

This is important.

Possible policies:

```
Fail-open
→ allow request if limiter unavailable

Fail-closed
→ reject request if limiter unavailable
```

For a low-risk read API, fail-open might sometimes be acceptable.

For a sensitive endpoint, fail-closed may be safer.

The decision depends on the endpoint and risk.

---

## 31. Complete System Design

A mature architecture could be:

```
                         Internet
                            |
                            v
                     CDN / DDoS
                            |
                            v
                          WAF
                            |
                            v
                     Load Balancer
                            |
                            v
                      API Gateway
                            |
                   +--------+--------+
                   |                 |
                   v                 v
             Authentication     Rate Limiter
                                     |
                                     v
                                   Redis
                                     |
                              +------+------+
                              |             |
                           ALLOW          REJECT
                              |             |
                              v             v
                         Microservices     429
                              |
                    +---------+---------+
                    |         |         |
                    v         v         v
                  Order    Payment     User
                  Service  Service     Service
```

The key principle is:

> Reject excessive traffic as early as possible, before it consumes expensive application and database resources.

---

## 32. Interview Answer

If the interviewer asks:

> "What is Rate Limiting and how does it work?"

A strong senior-level answer would be:

> Rate limiting controls the number of requests a client, user, IP, API key, tenant, or endpoint can make during a defined period. It protects services from abuse, traffic spikes, accidental overload, and resource exhaustion. In a distributed system, rate limiting is commonly implemented at the API Gateway or edge layer using algorithms such as Token Bucket or Sliding Window, with a shared store such as Redis when multiple gateway instances need consistent state. The limiter identifies the client, checks the current token or request count, atomically updates the state, and either forwards the request or returns HTTP 429. Production systems often use multiple limits such as per-IP, per-user, per-endpoint, and global limits, and expose retry information to clients. The design must also consider Redis failures, atomicity, burst behavior, fairness, and whether to fail-open or fail-closed.

### Mental model

```
                    RATE LIMITING
                         |
              "How much traffic
               can you send?"
                         |
        +----------------+----------------+
        |                |                |
        v                v                v
     Identify         Check limit      Update state
      client              |                |
        |                 v                |
        |             ALLOW / 429 <-------+
        |
        v
   IP / User / API Key /
   Tenant / Endpoint
```

### Remember these 5 things

1. Fixed Window
2. Sliding Window
3. Token Bucket ⭐
4. Leaky Bucket
5. Distributed Rate Limiting + Redis ⭐

