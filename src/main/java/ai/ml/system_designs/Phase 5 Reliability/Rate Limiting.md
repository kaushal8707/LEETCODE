# Rate Limiting

**Rate Limiting** is a system-design technique used to control how many requests a client, user, API, service, or IP address can make within a given period of time.

The primary goal is:

> Protect the system from excessive traffic and ensure fair resource usage.

For example:

```
User A → maximum 100 requests/minute
User B → maximum 100 requests/minute
```

If User A sends 150 requests:

```
Requests 1–100   → ALLOWED
Requests 101–150 → REJECTED / DELAYED
```

---

## 1. Why do we need Rate Limiting?

Imagine you have:

```
                    Internet
                       │
                       ▼
                 API Gateway
                       │
                       ▼
                  Order Service
                       │
                       ▼
                    Database
```

Your application can handle:

```
10,000 requests/second
```

Suddenly one client sends:

```
100,000 requests/second
```

**Without rate limiting:**

```
100,000 requests/sec
        ↓
API servers overloaded
        ↓
CPU increases
        ↓
Database connections exhausted
        ↓
Latency increases
        ↓
Timeouts
        ↓
Service failure
```

**With rate limiting:**

```
100,000 requests/sec
        ↓
Rate Limiter
        ↓
10,000 allowed
90,000 rejected/throttled
        ↓
System remains healthy
```

---

## 2. Real-world example

Consider a login API:

```
POST /login
```

You don't want an attacker to send:

```
1,000,000 login requests
```

trying different passwords.

You could configure:

```
5 login attempts / minute / IP
```

Then:

```
Attempt 1 → ALLOWED
Attempt 2 → ALLOWED
Attempt 3 → ALLOWED
Attempt 4 → ALLOWED
Attempt 5 → ALLOWED
Attempt 6 → REJECTED
```

Typically the client receives:

```
HTTP 429 Too Many Requests
```

---

## 3. Rate Limiting vs Throttling

These terms are related but often used slightly differently.

### Rate Limiting

Defines the **maximum allowed request rate**.

```
100 requests/minute
```

### Throttling

Controls or **slows** traffic when the system reaches a limit.

For example:

```
Normal traffic → process immediately
Excess traffic → delay/queue/reject
```

In system-design discussions, you'll often hear them used interchangeably.

---

## 4. Where should Rate Limiting happen?

A common architecture is:

```
                    Clients
                       │
                       ▼
                Load Balancer
                       │
                       ▼
                  API Gateway
                       │
                 Rate Limiter
                       │
          ┌────────────┼────────────┐
          ▼            ▼            ▼
       Service A    Service B    Service C
```

Rate limiting is commonly implemented at:

- API Gateway
- Reverse Proxy
- Load Balancer
- Application layer
- Service-to-service communication layer

For public APIs, putting a coarse-grained limiter at the gateway is often useful because unwanted traffic can be rejected before reaching application services.

---

## 5. What exactly are we limiting?

You can rate-limit based on different identities.

### Per IP

```
IP = 10.20.30.40

100 requests/minute
```

Useful for:

- public APIs
- anonymous traffic
- basic abuse protection

But IP-based limiting has problems because many users may share one public IP.

### Per user

```
user123 → 100 requests/minute
user456 → 100 requests/minute
```

Better when users are authenticated.

### Per API key

```
API-Key-A → 10,000 requests/hour
API-Key-B → 100,000 requests/hour
```

Common for developer APIs.

### Per endpoint

Different APIs may have different costs.

```
GET /products
→ 1,000 requests/minute

POST /payment
→ 20 requests/minute

GET /search
→ 100 requests/minute
```

This is often more realistic than applying one global limit.

---

## 6. Fixed Window Algorithm

One of the simplest algorithms.

Suppose:

```
Limit = 100 requests/minute
```

The system creates fixed time windows:

```
10:00:00 ───── 10:01:00
10:01:00 ───── 10:02:00
10:02:00 ───── 10:03:00
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
counter > 100
```

reject the request.

### Problem with Fixed Window

Suppose the limit is:

```
100 requests/minute
```

Client sends:

```
10:00:59
→ 100 requests

10:01:01
→ another 100 requests
```

The client effectively sends:

```
200 requests in ~2 seconds
```

even though the configured limit is only:

```
100/minute
```

This is called the **boundary problem**.

---

## 7. Sliding Window

Sliding Window provides a smoother limit.

Instead of fixed calendar windows:

```
10:00 ───────── 10:01
```

we look at the previous N seconds from the current request.

For example:

```
Limit = 100 requests
Window = previous 60 seconds
```

At:

```
10:01:35
```

we examine:

```
10:00:35 → 10:01:35
```

If there are already 100 requests:

```
Request 101 → REJECT
```

---

## 8. Sliding Window Log

One implementation stores timestamps:

```
10:00:01
10:00:03
10:00:08
10:00:15
...
```

For every request:

1. Remove timestamps older than the window.
2. Count remaining timestamps.
3. If count < limit → allow.
4. Otherwise → reject.

Example:

```
Limit = 5 / minute
```

Current timestamps:

```
10:00:10
10:00:20
10:00:30
10:00:40
10:00:50
```

New request:

```
10:00:55
```

Count = 5.

Therefore:

```
REJECT
```

### Disadvantage

Storing every request timestamp can consume significant memory at high traffic.

---

## 9. Sliding Window Counter

A more efficient approach combines counters from adjacent windows.

For example:

```
Previous window:
80 requests

Current window:
30 requests
```

Suppose we're 25% into the current window.

Approximate previous-window contribution:

```
80 × 75% = 60
```

Estimated requests:

```
60 + 30 = 90
```

If limit is:

```
100
```

the request can be allowed.

This uses much less memory than storing every timestamp.

---

## 10. Token Bucket

**Token Bucket** is one of the most important rate-limiting algorithms.

Imagine a bucket:

```
             ┌──────────────┐
Tokens  ---> │              │
             │    BUCKET    │
             │              │
             └──────────────┘
                    │
                    ▼
                 Request
```

The bucket has a maximum capacity.

Example:

```
Bucket capacity = 100 tokens
Refill rate     = 10 tokens/sec
```

Each request consumes one token:

```
Request
   ↓
Take 1 token
   ↓
Token available?
 ┌───────┴───────┐
 YES             NO
 ↓                ↓
Allow           Reject
```

---

## 11. Token Bucket example

Suppose:

```
Capacity = 5
Refill = 1 token/sec
```

Initially:

```
█████
5 tokens
```

Five requests arrive:

```
Request 1 → token consumed
Request 2 → token consumed
Request 3 → token consumed
Request 4 → token consumed
Request 5 → token consumed
```

Now:

```
Bucket = 0
```

Request 6:

```
No token
   ↓
REJECT
```

After one second:

```
Bucket = 1
```

One request can now pass.

---

## 12. Why Token Bucket is powerful

Token Bucket allows **controlled bursts**.

Suppose:

```
Capacity = 100
Refill = 10 tokens/sec
```

If the bucket has accumulated 100 tokens, the client can immediately send:

```
100 requests
```

Then it is limited to approximately:

```
10 requests/sec
```

This makes Token Bucket useful for APIs where short bursts are acceptable.

---

## 13. Leaky Bucket

Another algorithm is **Leaky Bucket**.

Think of a bucket with a fixed output rate:

```
Incoming requests
       │
       ▼
 ┌─────────────┐
 │    Queue    │
 └──────┬──────┘
        │
        ▼
  Fixed processing rate
```

For example:

```
Input:
100 requests/sec

Output:
10 requests/sec
```

The queue absorbs some burst traffic.

If the queue becomes full:

```
New request
    ↓
Queue FULL
    ↓
Reject
```

---

## 14. Token Bucket vs Leaky Bucket

| Feature | Token Bucket | Leaky Bucket |
|---|---|---|
| Allows bursts | Yes | Limited |
| Controls average rate | Yes | Yes |
| Queue required | Not necessarily | Commonly |
| Output rate | Can burst | Usually smoother |
| Good for APIs | Very common | Useful for smoothing |

Easy way to remember:

```
Token Bucket
→ "You can spend accumulated tokens."

Leaky Bucket
→ "Requests leave at a controlled rate."
```

---

## 15. Distributed Rate Limiting

This is where system design becomes interesting.

Suppose you have:

```
                 Load Balancer
                 /     |     \
                /      |      \
              App1    App2    App3
```

Suppose user has:

```
100 requests/minute
```

If each application maintains its own counter:

```
App1 → 100
App2 → 100
App3 → 100
```

The user could potentially make:

```
300 requests/minute
```

instead of 100.

That's incorrect if you intended a global limit.

---

## 16. Distributed Rate Limiter with Redis

A common solution is a centralized/shared store:

```
                 Load Balancer
                       │
            ┌──────────┼──────────┐
            ▼          ▼          ▼
          App1       App2       App3
            │          │          │
            └──────────┼──────────┘
                       ▼
                     Redis
                       │
                 Rate-limit state
```

All instances consult the same logical rate-limit state.

Example:

```
user123
    ↓
Redis
    ↓
tokens = 37
```

Request:

```
user123 → App2
             ↓
           Redis
             ↓
       token available
             ↓
          ALLOW
```

---

## 17. Why Redis?

A distributed rate limiter needs something that can handle:

- Very high throughput
- Low latency
- Shared state
- Atomic operations
- Expiration/TTL

Redis is commonly used because its in-memory operations are fast and it supports atomic primitives/scripts.

The critical word is:

```
Atomic
```

Suppose two application instances simultaneously see:

```
remaining = 1
```

If both independently read and then decrement:

```
App1 → read 1
App2 → read 1

App1 → decrement
App2 → decrement
```

you can accidentally allow both requests.

You need an **atomic check-and-update** operation.

---

## 18. HTTP response

When a client exceeds its limit, a common response is:

```
HTTP 429 Too Many Requests
```

You can also communicate when the client should retry using appropriate rate-limit headers.

Conceptually:

```
HTTP/1.1 429 Too Many Requests

Retry-After: 10
```

Meaning:

> Try again after approximately 10 seconds.

---

## 19. Rate Limit Headers

Modern APIs may expose information such as:

- Limit
- Remaining
- Reset

For example:

```
RateLimit-Limit: 100
RateLimit-Remaining: 27
RateLimit-Reset: 35
```

This tells the client:

```
Maximum = 100
Remaining = 27
Reset ≈ 35 seconds
```

Exact header conventions depend on the API/framework.

---

## 20. Rate Limiting and Distributed Systems

Consider your architecture:

```
                        Internet
                           │
                           ▼
                     API Gateway
                           │
                    Rate Limiter
                           │
                    ┌──────┴──────┐
                    ▼             ▼
                 Service A     Service B
                    │             │
                    ▼             ▼
                  DB A          DB B
```

Rate limiting protects multiple layers.

You might have:

```
Global:
10,000 req/sec
```

Then:

```
Per customer:
1,000 req/sec
```

Then:

```
Per endpoint:
POST /payment → 100 req/sec
```

Then:

```
Per expensive operation:
Search → 50 req/sec
```

This is called **multi-dimensional rate limiting**.

---

## 21. Rate Limiting vs Bulkhead

Since you just learned Bulkhead, this distinction is important.

### Rate Limiting

Controls:

> How quickly requests enter the system.

```
1,000 requests/sec
        ↓
Rate Limiter
        ↓
Allowed traffic
```

### Bulkhead

Controls:

> How much concurrent work can execute.

```
100 concurrent operations
        ↓
Bulkhead
```

Example:

```
Incoming:
10,000 requests/sec

Rate Limiter:
allows 1,000/sec

Bulkhead:
allows 100 concurrent operations
```

So they solve different problems.

---

## 22. Rate Limiting vs Circuit Breaker

### Rate Limiter

Protects against:

```
Too much traffic
```

### Circuit Breaker

Protects against:

```
Unhealthy dependency
```

Example:

```
Client sends 1 million requests
        ↓
Rate Limiter
        ↓
Reject excessive traffic
```

Versus:

```
Payment Service is failing
        ↓
Circuit Breaker
        ↓
Stop calling Payment
```

---

## 23. Rate Limiting + Retry problem

Be careful with retries.

Suppose:

```
Client → Rate Limiter → 429
```

Client immediately retries:

```
Retry → 429
Retry → 429
Retry → 429
Retry → 429
```

The client itself can create unnecessary load.

Better:

```
429
 ↓
Retry-After
 ↓
Wait
 ↓
Retry
```

For transient failures, use:

```
Exponential Backoff
+
Jitter
```

---

## 24. Protecting expensive APIs

Not all requests cost the same.

Consider:

```
GET /health
GET /products
POST /payment
POST /generate-report
```

A simple:

```
100 requests/minute
```

for everything may not be appropriate.

Instead:

```
/health
→ 1,000/min

/products
→ 500/min

/payment
→ 50/min

/generate-report
→ 5/min
```

This is an important real-world system-design consideration.

---

## 25. Fairness

Suppose you have:

```
10,000 users
```

and one user consumes:

```
90% of capacity
```

A global rate limiter may not provide fairness.

You can use:

```
Per-user limit
+
Per-IP limit
+
Global limit
```

Example:

```
Global       → 100,000 req/sec
Per customer → 1,000 req/sec
Per IP       → 100 req/sec
Per endpoint → endpoint-specific
```

This provides multiple layers of protection.

---

## 26. What happens when the limit is reached?

There are several strategies.

### Reject

```
429 Too Many Requests
```

Simple and common.

### Queue

Put requests into a bounded queue:

```
Request
   ↓
Queue
   ↓
Worker
```

Useful when the operation can tolerate delay.

### Degrade

Return cached or reduced information.

```
Full response unavailable
        ↓
Cached response
```

### Shed low-priority traffic

```
Critical requests → ALLOW
Background jobs   → REJECT
```

This is often valuable during overload.

---

## 27. Rate Limiter failure

A distributed rate limiter itself can become a dependency.

For example:

```
Application
    ↓
Redis Rate Limiter
    ↓
Redis DOWN
```

Now what?

You need a policy.

### Fail-open

If limiter is unavailable:

```
Allow request
```

**Pros:**

Application remains available.

**Cons:**

System can become overloaded.

### Fail-closed

If limiter is unavailable:

```
Reject request
```

**Pros:**

Protects backend.

**Cons:**

Can cause unnecessary outage.

The correct choice depends on the endpoint.

- For a critical internal API, **fail-open** might be preferable.
- For an expensive or security-sensitive API, **fail-closed** may be safer.

---

## 28. A production architecture

A robust architecture could look like:

```
                         Clients
                            │
                            ▼
                       CDN / WAF
                            │
                            ▼
                      API Gateway
                            │
                      Rate Limiter
                            │
                     Load Balancer
                            │
             ┌──────────────┼──────────────┐
             ▼              ▼              ▼
          Service 1      Service 2      Service 3
             │              │              │
             ▼              ▼              ▼
         Bulkhead       Bulkhead       Bulkhead
             │              │              │
             ▼              ▼              ▼
        Circuit Breaker Circuit Breaker Circuit Breaker
             │              │              │
             ▼              ▼              ▼
        Downstream       Downstream      Downstream
```

Now you have different defenses:

```
WAF
→ malicious traffic

Rate Limiter
→ excessive traffic

Load Balancer
→ distribute traffic

Bulkhead
→ isolate resources

Circuit Breaker
→ unhealthy dependencies

Timeout
→ don't wait forever

Retry + Backoff
→ transient failures
```

---

## 29. Interview question: "How would you design a distributed rate limiter?"

A strong 10-year-experience answer would be:

> I would first define the limiting dimension—such as user, API key, IP, endpoint, or tenant—and whether the limit is global or per instance. For a distributed deployment, I would maintain shared rate-limit state in a low-latency store such as Redis. Depending on whether bursts are acceptable, I would use Token Bucket or Sliding Window. The check-and-update operation must be atomic to prevent race conditions between service instances. When the limit is exceeded, the API should return HTTP 429 and ideally provide retry information. I would also define a fail-open or fail-closed strategy for rate-limiter failures and monitor allowed, rejected, and throttled requests.

---

## 30. Most important algorithms to know

For system-design interviews, know these well:

1. Fixed Window
2. Sliding Window Log
3. Sliding Window Counter
4. Token Bucket
5. Leaky Bucket

And understand:

```
                    Rate Limiting
                         │
        ┌────────────────┼────────────────┐
        │                │                │
     Algorithm         Storage        Distribution
        │                │                │
 Token Bucket         Redis          Multiple nodes
 Sliding Window       Local          Atomic operations
 Fixed Window
```

---

## 31. The mental model

Keep these distinctions in your head:

```
Rate Limiter
    ↓
"How fast can requests enter?"

Bulkhead
    ↓
"How much concurrent work can execute?"

Timeout
    ↓
"How long am I willing to wait?"

Retry
    ↓
"Can I try this transient failure again?"

Backoff
    ↓
"How long should I wait before retrying?"

Circuit Breaker
    ↓
"Should I stop calling this unhealthy dependency?"

Load Balancer
    ↓
"Which healthy instance should receive the request?"
```

---

## One-line interview definitions

| Pattern | One-line definition |
|---|---|
| Rate Limiting | Controls the rate at which requests are accepted |
| Bulkhead | Isolates resources so one workload cannot exhaust the system |
| Circuit Breaker | Stops calls to a repeatedly failing dependency |
| Timeout | Prevents waiting indefinitely |
| Retry | Reattempts potentially transient failures |
| Backoff | Spaces out retries |
| Jitter | Randomizes retry timing to avoid synchronized load |

