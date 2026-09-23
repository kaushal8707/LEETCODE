# Cache Avalanche

**Cache Avalanche** occurs when a large number of cache entries become unavailable at roughly the same time, causing 
a huge number of requests to bypass the cache and hit the backend/database simultaneously.

The key idea is:

> **Cache avalanche = many cache keys disappear/expire together → massive cache misses → sudden backend load.**

This is different from cache stampede, where typically **one popular key** becomes unavailable and many requests simultaneously try to rebuild it.

---

## 1. Simple Example

Imagine Redis contains:

```
product:101 → ₹100
product:102 → ₹200
product:103 → ₹300
...
product:1,000,000
```

Suppose all these entries were loaded at:

```
10:00 AM
```

and all have:

```
TTL = 1 hour
```

At:

```
11:00 AM
```

many of them expire together.

Now:

```
1 million cache entries
        ↓
   expire together
        ↓
Huge number of cache misses
        ↓
Database
        ↓
Massive traffic spike
```

The database can become overloaded.

---

## 2. Normal Situation

Normally:

```
                 Requests
                    |
                    v
                  Redis
                    |
              ┌─────┴─────┐
              |           |
             HIT         MISS
              |           |
              v           v
           Response       DB
```

Most requests are handled by Redis.

For example:

```
100,000 requests/sec
       ↓
Redis
       ↓
99,000 HIT
1,000 MISS
       ↓
DB
```

Database handles only:

```
1,000 requests/sec
```

---

## 3. During Cache Avalanche

Now imagine many cache entries expire together:

```
                 100,000 requests/sec
                          |
                          v
                        Redis
                          |
                       MISS MISS
                    MISS MISS MISS
                  MISS MISS MISS MISS
                          |
                          v
                         DB
                          |
                80,000 requests/sec
```

The database suddenly receives an enormous amount of traffic.

Possible consequences:

```
DB CPU              ↑↑↑
DB connections      ↑↑↑
DB latency          ↑↑↑
Connection pool     exhausted
Timeouts            ↑↑↑
Errors              ↑↑↑
Application health  ↓↓↓
```

And this can become a **cascading failure**.

---

## 4. Why Does Cache Avalanche Happen?

There are several common causes.

### Cause 1 — Same TTL

Suppose you load 1 million entries:

```
TTL = 30 minutes
```

at approximately the same time.

Then:

```
10:00 → cache populated
10:30 → huge number of entries expire
```

Result:

```
Many MISS → DB
```

### Cause 2 — Cache Restart

Suppose Redis crashes or restarts:

```
Redis
  ↓
Restart
  ↓
Cache empty
```

Now:

```
100,000 requests
       ↓
Redis
       ↓
100% MISS
       ↓
Database
```

Even though the TTLs weren't synchronized, the cache has suddenly lost its contents.

### Cause 3 — Cache Cluster Failure

In a distributed cache:

```
          Application
          /    |    \
         v     v     v
      Redis Redis Redis
```

If a significant portion of the cache cluster becomes unavailable:

```
Redis cluster
      ↓
Failure
      ↓
Cache unavailable
      ↓
Applications → DB
```

The database becomes the fallback for a huge amount of traffic.

---

## 5. Cache Avalanche vs Cache Stampede

This is very important for interviews.

### Cache Stampede

Usually:

```
ONE HOT KEY
     ↓
expires
     ↓
many requests
     ↓
DB
```

Example:

```
product:iphone
```

expires and 10,000 requests try to reload it.

### Cache Avalanche

Usually:

```
MANY KEYS
    ↓
expire/fail together
    ↓
huge number of cache misses
    ↓
DB
```

Example:

```
product:1
product:2
product:3
...
product:1,000,000
```

expire together.

### Comparison

|  | Cache Stampede | Cache Avalanche |
|---|---|---|
| Scope | Usually one/few hot keys | Many cache entries |
| Main trigger | Hot key expiration | Mass expiration/cache failure |
| Result | Many requests reload same data | Huge number of requests bypass cache |
| Typical solution | Lock/request coalescing | TTL jitter/warming/HA |
| Example | Popular product expires | Entire cache expires |

A useful mental model:

```
Stampede:

        ONE KEY
           ↓
    10,000 requests
           ↓
           DB


Avalanche:

     MILLIONS OF KEYS
           ↓
    MILLIONS OF MISSes
           ↓
           DB
```

---

## 6. Solution #1 — TTL Jitter

This is one of the most important solutions.

Don't use:

```
TTL = exactly 60 minutes
```

for every entry.

Instead:

```
TTL = 60 minutes + random(0–10 minutes)
```

For example:

```
product:101 → 60m 23s
product:102 → 64m 51s
product:103 → 61m 08s
product:104 → 68m 44s
product:105 → 63m 19s
```

Now expiration is distributed:

```
11:00 → some keys
11:01 → some keys
11:03 → some keys
11:05 → some keys
11:08 → some keys
11:10 → some keys
```

Instead of:

```
11:00
 ↓
ALL KEYS EXPIRE
```

This spreads the database load over time.

---

## 7. Solution #2 — Cache Warming

Before a predictable traffic spike, populate important data into the cache.

Example:

```
Black Friday
```

Before traffic starts:

```
Popular Products
       ↓
Cache Warming
       ↓
Redis
```

Then:

```
Millions of users
       ↓
Redis HIT
       ↓
Response
```

instead of:

```
Millions of users
       ↓
Redis MISS
       ↓
Database
```

Cache warming is particularly useful for:

- product catalogs
- configuration
- popular articles
- trending content
- reference data
- frequently accessed metadata

---

## 8. Solution #3 — Background Refresh

Don't wait for everything to expire.

Suppose:

```
TTL = 10 minutes
```

At 8 minutes:

```
Cache entry
    ↓
Almost expired
    ↓
Background refresh
    ↓
DB
    ↓
Redis
```

Users continue getting cached data.

```
User
 ↓
Redis → existing value
```

while:

```
Background worker
        ↓
       DB
        ↓
      Redis
```

refreshes the value.

---

## 9. Solution #4 — Stale-While-Revalidate

For data where slightly stale values are acceptable:

```
Request
   ↓
Expired/Stale cache
   ↓
Return stale value
   +
Background refresh
```

For example:

```
Product information
```

might tolerate a few seconds of staleness.

Flow:

```
                  Redis
                    |
              stale value
                    |
          ┌─────────┴─────────┐
          ↓                   ↓
    Return old value     Background
                          refresh
                             ↓
                             DB
                             ↓
                           Redis
```

This prevents a large number of requests from waiting for the database.

---

## 10. Solution #5 — Multi-Level Cache

Use multiple cache layers.

For example:

```
             Application
                  |
                  v
           Local Memory Cache
                  |
                MISS
                  |
                  v
               Redis
                  |
                MISS
                  |
                  v
              Database
```

If Redis temporarily fails:

```
Application
     ↓
Local cache
     ↓
HIT
```

Some traffic can still be served without hitting Redis or the database.

For example:

```
L1 → Caffeine/local memory
L2 → Redis
L3 → Database
```

This is called a **multi-level cache**.

---

## 11. Solution #6 — High Availability for Redis

If the avalanche is caused by cache infrastructure failure, you need to make the cache itself highly available.

Conceptually:

```
              Application
                   |
                   v
            Redis Cluster
           /      |      \
          v       v       v
       Node 1  Node 2  Node 3
          |       |       |
       Replica Replica Replica
```

Depending on the technology and architecture, you can use:

- replication
- automatic failover
- clustering
- multiple cache nodes
- multi-zone deployment

The goal is:

```
One cache node fails
       ↓
Other nodes continue serving
```

rather than:

```
Cache fails
   ↓
Everything → DB
```

---

## 12. Solution #7 — Rate Limiting

Even if the cache fails, don't allow unlimited traffic to reach the database.

```
             Requests
                 |
                 v
           Rate Limiter
                 |
          controlled traffic
                 |
                 v
               DB
```

For example:

```
Cache failure
     ↓
100,000 requests/sec
     ↓
Rate limiter
     ↓
DB receives only safe load
```

The excess requests might:

- wait
- receive cached/stale data
- receive fallback data
- receive 429
- receive a graceful degradation response

depending on the business requirements.

---

## 13. Solution #8 — Graceful Degradation

During a cache failure, not every request needs to fail.

Suppose:

```
Recommendation Service
```

is unavailable.

Instead of:

```
Checkout
  ↓
Recommendation Service
  ↓
FAIL
  ↓
Checkout FAIL
```

you might do:

```
Checkout
   ↓
Recommendation Service
   ↓
FAIL
   ↓
Use default recommendations
   ↓
Checkout continues
```

This is **graceful degradation**.

It prevents a cache problem from becoming a complete application outage.

---

## 14. Solution #9 — Protect the Database

You should assume:

> **Cache can fail.**

Therefore, the database should have protection mechanisms.

For example:

```
Cache
  ↓
Cache MISS
  ↓
Bulkhead / Rate Limiter
  ↓
Connection Pool
  ↓
Database
```

You can control:

- maximum DB connections
- request concurrency
- query rate
- timeout
- queue size
- priority

This prevents the database from being completely overwhelmed.

---

## 15. Combining the Solutions

A production architecture could look like:

```
                       Clients
                          |
                          v
                    Load Balancer
                          |
                          v
                    Application
                          |
                    ┌─────┴─────┐
                    ↓           ↓
                 L1 Cache     Redis
                    |           |
                    |         MISS
                    |           |
                    └─────┬─────┘
                          ↓
                    Request Control
                  ┌───────┼────────┐
                  ↓       ↓        ↓
              Rate Limit  Lock   Timeout
                  |       |        |
                  └───────┼────────┘
                          ↓
                       Database
```

And Redis entries use:

```
TTL
 +
TTL Jitter
 +
Background Refresh
```

while Redis itself uses:

```
Replication
+
Failover
+
Cluster
```

---

## 16. A Real-World Example

Imagine a food-delivery application.

Cache contains:

```
restaurant:101
restaurant:102
restaurant:103
...
restaurant:1,000,000
```

All restaurants were cached during a deployment:

```
10:00 AM
```

and the application used:

```
TTL = 30 minutes
```

At:

```
10:30 AM
```

many entries expire.

At the same time:

```
Lunch traffic starts
```

Now:

```
                    500K requests
                         |
                         v
                       Redis
                         |
                  Massive MISS
                         |
                         v
                    Restaurant DB
                         |
                    overloaded
                         |
              ┌──────────┼──────────┐
              ↓          ↓          ↓
           timeout     timeout    timeout
```

This is a classic **cache avalanche**.

**Better design:**

```
TTL = 30 min + random(0–5 min)
```

plus:

```
Cache warming
+
Background refresh
+
Redis HA
+
DB rate limiting
```

Now expiration is distributed:

```
10:30 → some entries
10:31 → some entries
10:32 → some entries
10:33 → some entries
...
10:35 → some entries
```

The DB sees a manageable stream instead of a sudden spike.

---

## 17. Cache Avalanche + Other Cache Problems

You should know all three together:

```
                CACHE PROBLEMS
                      |
        ┌─────────────┼─────────────┐
        ↓             ↓             ↓
   Penetration     Stampede      Avalanche
        |             |             |
        ↓             ↓             ↓
Data doesn't      One hot key   Many keys
exist             expires       expire/fail
        |             |             |
        ↓             ↓             ↓
Repeated DB      Many requests   Massive
queries          reload same     cache misses
                 key             |
                                  ↓
                                  DB
```

### Solutions

```
CACHE PENETRATION
→ Negative caching
→ Bloom filter
→ Input validation
→ Rate limiting


CACHE STAMPEDE
→ Distributed lock
→ Request coalescing
→ Background refresh
→ Stale-while-revalidate


CACHE AVALANCHE
→ TTL jitter
→ Cache warming
→ Background refresh
→ Multi-level cache
→ Cache HA/failover
→ Rate limiting
→ Graceful degradation
```

---

## 18. Important Interview Question

### Interviewer:

> *"Your Redis cache contains millions of keys, and they all expire at approximately the same time. What will happen?"*

### Strong answer:

> "This can cause a cache avalanche. A large number of requests will experience cache misses simultaneously and fall through to the database, potentially overwhelming it. I would avoid synchronized expiration using TTL jitter, use cache warming and background refresh for important data, make the cache highly available, and protect the database using rate limiting, bulkheads and connection limits."

---

## 19. Cache Avalanche vs Stampede — Senior-Level Nuance

There is a useful way to remember the difference:

### Stampede is about concurrency

```
One key
   ↓
Many requests
   ↓
At the same time
```

### Avalanche is about scale

```
Many keys
   ↓
Many cache misses
   ↓
Huge backend load
```

And cache avalanche can be caused by more than expiration:

```
Mass expiration
      OR
Cache restart
      OR
Cache cluster failure
      OR
Network failure
```

---

## 20. Final Mental Model

Remember:

```
                 CACHE AVALANCHE

          MANY CACHE ENTRIES
                  |
          ┌───────┴────────┐
          ↓                ↓
     TTL expires       Cache failure
          |                |
          └───────┬────────┘
                  ↓
            CACHE MISS STORM
                  ↓
              Database
                  ↓
             DB overload
                  ↓
          Cascading failures
```

The most important prevention pattern is:

```
              Cache
                |
         Don't expire all
          entries together
                |
                ↓
           TTL + Jitter
                +
         Background Refresh
                +
          Cache Warming
                +
             HA Cache
                +
          DB Protection
```

---

### One-line interview answer

> **"Cache avalanche occurs when a large number of cache entries expire or become unavailable at roughly the same time, causing massive cache misses and a sudden surge of traffic to the backend. We mitigate it with TTL jitter, cache warming, background refresh, stale-while-revalidate, highly available caches, and backend protection."**

