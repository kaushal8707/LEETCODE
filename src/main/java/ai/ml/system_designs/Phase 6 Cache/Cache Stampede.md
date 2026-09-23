# Cache Stampede

Cache Stampede is a situation where a popular cache entry expires or disappears, and many requests simultaneously try to reload the same data from the database.

It is also called:

- **Thundering Herd**
- **Cache Miss Storm**

The dangerous part is that the cache is supposed to *protect* the database, but during a stampede it can actually **overload** the database.

---

## 1. Simple Example

Suppose we have:

```
Product: iPhone 17
Cache TTL: 10 minutes
Traffic: 10,000 requests/sec
```

Normally:

```
10,000 requests/sec
        ↓
      Redis
        ↓
   Cache HIT
        ↓
    Response
```

The database receives very few requests.

Now the cache entry expires:

```
iPhone:17
    ↓
TTL expires
    ↓
Cache MISS
```

At exactly that moment, thousands of users request the product:

```
Request 1 ──┐
Request 2 ──┤
Request 3 ──┤
Request 4 ──┤
Request 5 ──┤
   ...      ├──→ Redis → MISS
Request 9999┤
Request 10000┘
```

All of them see:

```
CACHE MISS
```

And all of them go to the database:

```
             Redis
               |
             MISS
               |
       ┌───────┼────────┐
       ↓       ↓        ↓
      DB      DB       DB
       ↑       ↑        ↑
    thousands of queries
```

The database suddenly gets:

```
10,000 requests/sec
```

This can cause:

- DB CPU ↑
- DB connections ↑
- DB latency ↑
- DB timeouts ↑
- Application errors ↑

Potentially causing a **cascading failure**.

---

## 2. Why Does Cache Stampede Happen?

The most common cause is **simultaneous expiration**.

Suppose:

```
Cache TTL = 5 minutes
```

At:

```
10:00:00 → cache populated
```

At:

```
10:05:00 → cache expires
```

Now thousands of requests arrive:

```
10:05:00
    ↓
Cache expires
    ↓
1000 requests
    ↓
1000 cache misses
    ↓
1000 DB queries
```

This is the classic cache stampede.

---

## 3. Real-World Example

Imagine an e-commerce website.

There is a very popular product:

```
product:123
```

Normally:

```
                    ┌── HIT ──→ Response
                    │
Request → Redis ────┤
                    │
                    └── MISS → DB
```

Suppose 50,000 users are viewing the product.

The cache entry expires.

Now:

```
50,000 requests
       ↓
    Redis
       ↓
     MISS
       ↓
50,000 DB queries
```

The database may not be able to handle that load.

This is why cache stampede is a **reliability problem**, not merely a caching problem.

---

## 4. Cache Stampede Flow

### Normal situation

```
             Request
                |
                v
             Cache
                |
             HIT ✓
                |
                v
             Response
```

Database is protected.

### During stampede

```
             Request
                |
                v
             Cache
                |
             MISS ✗
                |
       ┌────────┼─────────┐
       ↓        ↓         ↓
      DB       DB        DB
       ↑        ↑         ↑
       └────────┼─────────┘
          Thousands
          of requests
```

The protection disappears.

---

## 5. Solution #1 — Locking

One of the most common solutions is to use a **distributed lock**.

Instead of allowing every request to query the database:

```
1000 requests
     ↓
1000 DB queries
```

we allow only one request to reload the cache.

```
1000 requests
      ↓
    Cache MISS
      ↓
  Distributed Lock
      ↓
   One request
      ↓
      DB
      ↓
 Populate Cache
      ↓
Other requests read Cache
```

For example:

```
Request 1 → acquires lock → DB → Redis
Request 2 → waits
Request 3 → waits
Request 4 → waits
...
```

After Request 1 populates Redis:

```
Request 2 → Redis HIT
Request 3 → Redis HIT
Request 4 → Redis HIT
```

### Important

The lock should have a timeout/lease.

Otherwise:

```
Request 1 acquires lock
       ↓
Application crashes
       ↓
Lock remains forever
       ↓
Nobody can refresh cache
```

So use:

> **Distributed Lock + TTL**

---

## 6. Solution #2 — Double-Check After Acquiring Lock

This is an important optimization.

Suppose:

```
Request A → MISS
Request B → MISS
Request C → MISS
```

Request A gets the lock.

It loads from DB:

```
DB → Redis
```

But Request B was already waiting.

When B gets the lock, it must check the cache again.

```
Request B
   ↓
Acquire lock
   ↓
Check Redis again
   ↓
HIT
   ↓
Return
```

Otherwise B might unnecessarily query the database again.

The pattern is:

```
Cache MISS
    ↓
Acquire lock
    ↓
Double-check cache
    ↓
Still MISS?
    ↓
Query DB
    ↓
Populate cache
    ↓
Release lock
```

This is often called **double-checked locking** in cache loading.

---

## 7. Solution #3 — TTL Jitter

This is one of the most useful techniques.

Instead of giving every cache entry exactly:

```
TTL = 10 minutes
```

use:

```
TTL = 10 min + random value
```

For example:

```
Product A → 10m 23s
Product B → 10m 51s
Product C → 10m 08s
Product D → 10m 44s
```

Now entries don't expire simultaneously.

**Without jitter:**

```
10:00 → A expires
10:00 → B expires
10:00 → C expires
10:00 → D expires
       ↓
   huge load
```

**With jitter:**

```
10:08 → C expires
10:23 → A expires
10:44 → D expires
10:51 → B expires
```

Load is spread over time.

---

## 8. Solution #4 — Background Refresh

Instead of waiting until the cache expires, refresh it before expiration.

For example:

```
TTL = 10 minutes
```

At 9 minutes:

```
Cache
  ↓
Almost expired
  ↓
Background refresh
  ↓
DB
  ↓
Update cache
```

Users continue receiving the existing value.

```
User → Cache → Old but acceptable value
              +
        Background refresh
```

This is particularly useful for:

- configuration
- product catalogs
- reference data
- popular content
- exchange rates
- feature configuration

---

## 9. Solution #5 — Stale-While-Revalidate

This is an excellent strategy for highly popular data.

Suppose:

```
Cache value = ₹1000
TTL = expired
```

Instead of immediately forcing the request to wait for DB:

```
Request
  ↓
Expired cache
  ↓
DB
  ↓
Wait
```

we can temporarily return the stale value:

```
Request
  ↓
Stale cache
  ↓
Return ₹1000
```

while refreshing asynchronously:

```
              ┌──→ Return stale value
Request → Cache
              └──→ Background refresh → DB
```

Then:

```
DB → Redis
```

with the latest value.

This trades a small amount of staleness for much better availability and latency.

---

## 10. Solution #6 — Request Coalescing

Another technique is to make concurrent requests for the same key share **one** in-flight database request.

Suppose:

```
1000 requests
     ↓
product:123
```

Instead of:

```
1000 DB calls
```

the application creates:

```
One in-flight request
       ↓
      DB
       ↓
   Result
   / | \
  /  |  \
1000 waiting requests
```

So:

```
1000 requests
      ↓
1 DB request
      ↓
1000 responses
```

This is also called:

> **Single-flight / request collapsing / request coalescing**

---

## 11. Solution #7 — Rate Limiting

You can also protect the database from a sudden cache miss storm.

For example:

```
Cache MISS
    ↓
Rate limiter
    ↓
Only 100 DB requests/sec
    ↓
DB
```

The remaining requests may:

- wait
- receive stale data
- receive fallback data
- receive an error

depending on the business requirement.

This prevents the database from being overwhelmed.

---

## 12. Solution #8 — Cache Warming

For predictable high-traffic keys, proactively populate the cache.

For example, before a major sale:

```
Black Friday
     ↓
Identify popular products
     ↓
Load products into Redis
     ↓
Traffic arrives
     ↓
Cache HIT
```

Instead of:

```
Traffic arrives
     ↓
Cache MISS
     ↓
DB suddenly overloaded
```

you have:

```
Traffic arrives
     ↓
Cache HIT
```

---

## 13. Multiple Solutions Together

Production systems often combine several techniques.

For example:

```
                    Request
                       |
                       v
                    Redis
                       |
                 ┌─────┴─────┐
                 |           |
                HIT         MISS
                 |           |
              Return      Lock
                             |
                       Double-check
                             |
                       ┌─────┴─────┐
                       |           |
                      HIT         MISS
                       |           |
                    Return        DB
                                   |
                                   v
                                Redis
                                   |
                                   v
                                Return
```

And the cache may use:

```
TTL + Jitter
+
Distributed Lock
+
Background Refresh
+
Request Coalescing
+
Rate Limiting
```

---

## 14. Cache Stampede vs Cache Penetration vs Cache Avalanche

These three are frequently confused in interviews.

| Problem | What happens? | Example |
|---|---|---|
| Cache Stampede | Many requests simultaneously reload the same expired/missing key | Popular product expires |
| Cache Penetration | Requests repeatedly ask for data that doesn't exist | `user:999999999` |
| Cache Avalanche | Many cache entries expire/fail around the same time | Thousands of keys expire together |

### Stampede

```
ONE popular key
      ↓
many requests
      ↓
DB
```

### Penetration

```
Non-existent key
      ↓
Cache MISS
      ↓
DB
      ↓
NOT FOUND

Repeated again and again
```

### Avalanche

```
Thousands of keys
      ↓
expire simultaneously
      ↓
Thousands of DB requests
```

---

## 15. Why TTL Jitter Helps Avalanche Too

Consider:

```
TTL = exactly 60 minutes
```

If 1 million entries were loaded at the same time:

```
12:00 → loaded

13:00 → 1 million expire
```

Potential avalanche.

With jitter:

```
TTL = 60m + random(0–5m)
```

Expiration becomes:

```
13:00:03
13:00:17
13:01:02
13:02:31
13:04:45
...
```

The load is distributed.

---

## 16. Cache Stampede in a Microservices Architecture

Imagine:

```
                    Load Balancer
                         |
             ┌───────────┼───────────┐
             ↓           ↓           ↓
          Service 1   Service 2   Service 3
             |           |           |
             └───────────┼───────────┘
                         ↓
                       Redis
                         |
                       MISS
                         |
                         ↓
                    Product DB
```

If a popular cache entry expires, **all** application instances can simultaneously attempt to load it.

That's why a **distributed lock** may be required.

A local JVM lock like:

```
synchronized
```

is not sufficient when you have:

```
Service 1 → JVM 1
Service 2 → JVM 2
Service 3 → JVM 3
```

because each JVM has its own lock.

You need coordination across instances, such as a distributed locking mechanism.

---

## 17. The Most Important Interview Scenario

Interviewer:

> *"Your Redis key expires and 10,000 requests simultaneously hit the application. How do you prevent all 10,000 requests from hitting the database?"*

A strong answer:

> "I would prevent a cache stampede by allowing only one request to rebuild the cache. On a cache miss, the request acquires a distributed lock, then double-checks the cache. If the value is still missing, it loads from the database, populates the cache, and releases the lock. Other requests wait or use stale data depending on the consistency requirement. I would also add TTL jitter and, for very hot keys, use background refresh or request coalescing."

That's a senior-level system design answer.

---

## 18. Important Production Considerations

### Lock timeout

Never allow an infinite lock:

```
Lock TTL = 5 seconds
```

or an appropriate value based on DB/cache latency.

### Lock ownership

Only the process that acquired the lock should release it.

### Retry carefully

Don't let waiting requests aggressively retry:

```
retry → retry → retry → retry
```

because that can create another storm.

Use:

> **exponential backoff + jitter**

### Hot keys

A single extremely popular key can still create significant contention.

Monitor:

- cache hit ratio
- cache miss rate
- DB QPS
- lock contention
- lock wait time
- cache latency
- hot-key frequency

---

## 19. Cache Stampede Mental Model

Remember this:

```
                 CACHE
                   |
              Popular Key
                   |
              TTL EXPIRES
                   |
                   ↓
            ┌──────┴──────┐
            ↓      ↓      ↓
          Req1   Req2   Req3
            ↓      ↓      ↓
            └──────┼──────┘
                   ↓
                  DB
                   ↓
             OVERLOADED
```

**Solution:**

```
                 CACHE
                   |
                 MISS
                   |
             Distributed Lock
                   |
             ┌─────┴─────┐
             ↓           ↓
         One request   Others
             ↓           ↓
             DB        Wait/Use stale
             ↓
        Populate Cache
             ↓
          Release
             ↓
        Everyone gets
          cached data
```

---

### Key takeaway

> **Cache Stampede** = many concurrent requests try to rebuild the same missing/expired cache entry, causing a sudden surge against the database.

The main defenses are:

1. Distributed locking
2. Double-check after lock
3. TTL jitter
4. Request coalescing / single-flight
5. Background refresh
6. Stale-while-revalidate
7. Cache warming
8. Rate limiting
9. Exponential backoff + jitter

---

### Interview shortcut:

> **"For a hot key, don't let every cache miss hit the DB. Elect one request to refresh the value, and let the others wait, share the result, or temporarily serve stale data."**

