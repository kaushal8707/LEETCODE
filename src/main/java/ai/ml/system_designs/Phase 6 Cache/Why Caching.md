# Why Caching?

Caching means storing frequently accessed data in a faster temporary storage so that future requests can be served without repeatedly accessing the slower original source, such as a database or external API.

The primary reason we use caching is:

> **Reduce latency, reduce load, and improve scalability.**

---

## 1. Without Cache

Imagine an e-commerce application:

```
User
  |
  v
API Server
  |
  v
Database
  |
  v
Product Details
```

Suppose 10,000 users request:

```
GET /products/123
```

Every request goes to the database.

```
10,000 Requests
       |
       v
   API Server
       |
       v
   Database
       |
       v
Product 123
```

The database has to process the same query repeatedly.

This can cause:

- Higher database CPU
- More database connections
- Higher latency
- More network traffic
- Database bottlenecks
- Poor scalability

---

## 2. With Cache

We introduce a cache such as Redis:

```
User
  |
  v
API Server
  |
  v
  Cache
   |
   |-- HIT --> Return data
   |
   |-- MISS
         |
         v
      Database
         |
         v
       Cache
         |
         v
      Response
```

Now the first request might query the database:

```
Request
  |
  v
Redis
  |
  | MISS
  v
Database
  |
  v
Store in Redis
  |
  v
Response
```

The next 9,999 requests can potentially be served from Redis:

```
Request
  |
  v
Redis
  |
  | HIT
  v
Response
```

This is much faster.

---

## 3. Main Reasons for Caching

### ① Reduce Latency

This is one of the biggest reasons.

**Database:**

```
Application → Database → Response
```

might take, for example:

```
20 ms
```

**Cache:**

```
Application → Redis → Response
```

might take:

```
1–2 ms
```

So:

```
Without Cache:  ~20 ms
With Cache:      ~1 ms
```

The exact numbers depend on the architecture, network, database, cache location, etc.

---

## 4. Reduce Database Load

Suppose:

```
100,000 requests/sec
```

and 80% of those requests ask for frequently accessed data.

**Without caching:**

```
100,000 requests
        ↓
100,000 DB queries
```

**With caching:**

```
100,000 requests
        ↓
80,000 Cache HIT
20,000 Cache MISS
        ↓
20,000 DB queries
```

The database load can be dramatically reduced.

This is especially important because databases are often one of the most expensive and difficult components to scale.

---

## 5. Improve Scalability

Suppose your database can comfortably handle:

```
10,000 queries/sec
```

but your application receives:

```
50,000 requests/sec
```

**Without caching:**

```
50,000 requests
       ↓
Database
       ↓
Overloaded
```

**With caching:**

```
50,000 requests
       ↓
     Cache
       |
       +---- 45,000 HIT
       |
       +---- 5,000 MISS
                ↓
             Database
```

Now the database handles only around:

```
5,000 requests/sec
```

instead of:

```
50,000 requests/sec
```

Caching therefore acts as a **load reduction layer**.

---

## 6. Reduce Cost

Consider a large system where database infrastructure is expensive.

If caching reduces database traffic significantly, you may need:

- fewer database replicas
- smaller database instances
- fewer database connections
- less database I/O

Therefore:

```
Caching
   ↓
Less DB traffic
   ↓
Less infrastructure
   ↓
Lower cost
```

---

## 7. Handle Traffic Spikes

Imagine an IPL match ticket booking system.

Normally:

```
1,000 requests/sec
```

When tickets open:

```
100,000 requests/sec
```

A large number of users may request the same information:

- Match details
- Venue details
- Team information
- Ticket categories
- Pricing information

If this data is cached:

```
100,000 requests
        ↓
      Cache
        ↓
Most requests served here
```

The database doesn't have to handle the entire spike.

---

## 8. Reduce Dependency on Slow External Services

Caching isn't limited to databases.

Suppose your application calls:

```
Your Service
     |
     v
Payment Provider
```

or:

```
Your Service
     |
     v
Address API
```

If some response doesn't change frequently, you can cache it.

For example:

```
GET country/IN
```

The country information probably doesn't change every second.

Instead of:

```
Every request
     ↓
External API
```

you can do:

```
First request
     ↓
External API
     ↓
Cache

Subsequent requests
     ↓
Cache
```

This reduces external API calls.

---

## 9. Improve Availability — Sometimes

Caching can also provide limited resilience when the underlying data source temporarily fails.

For example:

```
User
 |
 v
Application
 |
 v
Cache
 |
 | HIT
 v
Response
```

Even if the database is temporarily unavailable, an existing cached value may still be served.

This is commonly called:

> **Stale-while-revalidate** or serving stale data, depending on the strategy.

However, caching should **not** automatically be considered a replacement for database availability.

---

## 10. What Should We Cache?

Typically, cache data that is:

**Frequently read**

- Product details
- User profile
- Configuration
- Exchange rates
- Feature flags
- Popular articles

**Expensive to calculate**

For example:

```
Generate recommendation
        ↓
Expensive computation
        ↓
Cache result
```

**Relatively stable**

For example:

```
Country → Currency
```

doesn't change frequently.

---

## 11. What Should NOT Be Cached Easily?

Be careful with highly dynamic or sensitive data.

For example:

- Current bank balance
- Stock availability
- Payment transaction state
- Highly sensitive user-specific information

Caching these incorrectly can result in stale or incorrect data.

For example:

```
Actual balance = ₹10,000

Cache says = ₹15,000
```

That's obviously dangerous.

---

## 12. Cache Hit vs Cache Miss

Two fundamental concepts you should know.

### Cache Hit

Data exists in cache:

```
Request
   ↓
Cache
   ↓
FOUND
   ↓
Response
```

This is a **cache hit**.

### Cache Miss

Data doesn't exist:

```
Request
   ↓
Cache
   ↓
NOT FOUND
   ↓
Database
   ↓
Cache
   ↓
Response
```

This is a **cache miss**.

---

## 13. Cache Hit Ratio

An important metric is:

```
Cache Hit Ratio = Cache Hits / Total Requests
```

**Example:**

```
Total requests = 100,000

Cache hits = 90,000
Cache misses = 10,000
```

Therefore:

```
Hit Ratio = 90,000 / 100,000
          = 90%
```

A higher hit ratio generally means the cache is handling more traffic.

But a high hit ratio isn't automatically good—you also need to consider correctness, memory usage, eviction, and the cost of misses.

---

## 14. Where Can We Cache?

Caching can exist at multiple layers:

```
                 User
                   |
                   v
             CDN / Browser
                   |
                   v
             Load Balancer
                   |
                   v
             Application
                   |
                   v
               Redis
                   |
                   v
              Database
```

**Examples:**

- **Browser Cache** — Browser → Cached image
- **CDN Cache** — User → CDN → Static content
- **Application Cache** — Application → Local Memory
- **Distributed Cache** — Application → Redis
- **Database Cache** — Databases themselves often use memory/buffer caching internally.

---

## 15. The Most Important Trade-off

Caching introduces a new problem:

> **What happens when the underlying data changes?**

Suppose:

```
Database:
Product price = ₹1,000

Cache:
Product price = ₹1,000
```

Someone changes the database:

```
Database:
Product price = ₹900
```

But cache still says:

```
₹1,000
```

Now we have **stale data**.

Therefore, whenever you design caching, you need to think about:

```
Cache
 ├── What to cache?
 ├── TTL?
 ├── Eviction?
 ├── Cache invalidation?
 ├── Consistency?
 ├── Cache stampede?
 ├── Cache penetration?
 ├── Cache avalanche?
 └── What happens when cache is unavailable?
```

These are very important system-design interview topics.

---

## 16. Simple Real-World Example

Think about a restaurant menu.

**Without caching:**

```
Every customer
     ↓
Ask kitchen
     ↓
"What's today's menu?"
```

That's inefficient.

**Instead:**

```
Menu
 ↓
Keep a copy at the front desk
 ↓
Customers read the copy
```

The front-desk copy is effectively a **cache**.

If the menu changes:

```
Update original menu
       ↓
Update cached copy
```

That's essentially the **cache invalidation** problem.

---

## 17. The Core System Design Picture

Remember this:

```
                 ┌──────────────┐
                 │    Client    │
                 └──────┬───────┘
                        │
                        ▼
                 ┌──────────────┐
                 │ API Servers  │
                 └──────┬───────┘
                        │
                        ▼
                 ┌──────────────┐
                 │    Cache     │
                 │    Redis     │
                 └──────┬───────┘
                    HIT  │  MISS
                         │
                         ▼
                 ┌──────────────┐
                 │   Database   │
                 └──────────────┘
```

The fundamental idea is:

> **Put frequently accessed data closer to the consumer and avoid repeatedly doing expensive work.**

---

## Interview Answer

If an interviewer asks *"Why do we use caching?"*, a strong answer is:

> "We use caching to reduce latency, reduce load on databases and downstream services, improve throughput and scalability, and handle traffic spikes more efficiently. The trade-off is that cached data can become stale, so we need an appropriate TTL, eviction strategy, and cache-invalidation strategy."

