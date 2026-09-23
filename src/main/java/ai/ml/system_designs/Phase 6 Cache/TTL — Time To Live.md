# TTL — Time To Live

**TTL (Time To Live)** is the amount of time a cached item is allowed to remain valid in the cache before it expires automatically.

The simple idea is:

> **Store data in cache → keep it for a defined duration → automatically expire it.**

---

## 1. Simple Example

Suppose we cache a product:

```
Key:   product:123
Value: ₹1,000
TTL:   5 minutes
```

At 10:00:

```
Redis
product:123 → ₹1,000
             TTL = 5 min
```

At 10:05:

```
product:123 → EXPIRED
```

The cache removes/invalidates the entry according to its expiration mechanism.

Next request:

```
Application
     |
     v
   Cache
     |
   MISS
     |
     v
 Database
     |
     v
 Latest value
```

---

## 2. Why Do We Need TTL?

The biggest reason is:

> **Prevent stale data from living in the cache forever.**

Suppose:

```
Database = ₹900
Cache    = ₹1,000
```

Without TTL, the cache could theoretically continue returning:

```
₹1,000 ❌
```

indefinitely if nothing explicitly invalidates it.

With TTL:

```
Cache
 ₹1,000
   |
   | TTL expires
   v
 MISS
   |
   v
Database
 ₹900
```

The cache eventually refreshes.

---

## 3. TTL in Cache-Aside

TTL works very naturally with Cache-Aside.

### First request

```
Application
    |
    v
 Cache
    |
    | MISS
    v
Database
    |
    v
Data
    |
    v
Cache + TTL
    |
    v
Application
```

For example:

```
SET product:123 ₹900 EX 300
```

Meaning:

```
product:123
    ↓
₹900
    ↓
expires after 300 seconds
```

---

## 4. Subsequent Requests

Before TTL expires:

```
Request
   |
   v
Cache
   |
   | HIT
   v
₹900
```

No database query is required.

---

## 5. After TTL Expires

```
Request
   |
   v
Cache
   |
   | MISS / expired
   v
Database
   |
   v
Latest data
   |
   v
Cache
   |
   v
Response
```

The cache is repopulated.

---

## 6. TTL Does NOT Mean Data Is Always Fresh

This is an important interview point.

Suppose:

```
TTL = 1 hour
```

At 10:00:

```
Cache = ₹1,000
```

At 10:05:

```
Database = ₹900
```

But cache still contains:

```
Cache = ₹1,000
```

until its TTL expires.

Therefore:

> **TTL allows stale data for the duration of the TTL.**

For data that requires immediate freshness, TTL alone isn't enough.

You may need:

```
Database update
      ↓
Explicit cache invalidation
```

---

## 7. TTL vs Explicit Invalidation

### TTL

```
Data changes
     |
     | cache doesn't know immediately
     |
     v
Wait for TTL
     |
     v
Cache expires
```

### Explicit Invalidation

```
Data changes
     |
     v
Delete cache
     |
     v
Immediate invalidation
```

So:

| Approach | Freshness | Complexity |
|---|---|---|
| TTL | Eventually | Low |
| Explicit invalidation | Faster/immediate | Higher |
| TTL + invalidation | Stronger protection | Moderate |

A common production strategy is:

```
Explicit invalidation
        +
       TTL
        ↓
Defense in depth
```

---

## 8. How Do We Choose TTL?

There is no universal TTL.

It depends on how frequently the data changes and how much stale data is acceptable.

### Frequently changing data

- Stock price
- Inventory
- Availability

Could require:

```
Seconds
```

or even no cache, depending on correctness requirements.

### Moderately changing data

- Product information
- User profile

Could use:

```
Minutes
```

### Rarely changing data

- Country list
- Currency metadata
- Application configuration

Could use:

```
Hours
Days
```

The principle is:

> **The more frequently data changes and the less stale data you can tolerate, the shorter the TTL should generally be.**

---

## 9. TTL Trade-off

There is a fundamental trade-off.

### Short TTL

```
TTL = 10 seconds
```

**Advantages:**

- Fresher data
- Less stale data

**Disadvantages:**

- More cache misses
- More database requests
- Lower cache hit ratio

### Long TTL

```
TTL = 24 hours
```

**Advantages:**

- Higher cache hit ratio
- Lower database load

**Disadvantages:**

- Data can remain stale for longer

So:

```
Short TTL
   ↓
Freshness ↑
DB load ↑

Long TTL
   ↓
Freshness ↓
DB load ↓
```

---

## 10. TTL and Cache Stampede

Here's an important system-design problem.

Suppose:

```
product:123
TTL = 10 minutes
```

And it is extremely popular.

At exactly 10:00:

```
Cache entry expires
```

Suddenly:

```
10,000 requests
       |
       v
Cache MISS
       |
       v
10,000 DB queries
```

The database can get overloaded.

This is called:

> **Cache Stampede** or **Thundering Herd**

---

## 11. TTL Jitter

One solution is to avoid having many entries expire at exactly the same time.

Instead of:

```
All entries:
TTL = 10 minutes
```

use:

```
TTL = 10 min + random(0–60 sec)
```

For example:

```
Product A → 10m 12s
Product B → 10m 45s
Product C → 10m 03s
Product D → 10m 51s
```

Now expiration is spread out.

```
                    Requests
                       |
             ┌─────────┴─────────┐
             v                   v
        Cache HIT             Cache MISS
                                  |
                                  v
                             DB requests
                                  |
                           spread over time
```

This reduces the probability of a massive simultaneous database load.

---

## 12. TTL and Cache Avalanche

Suppose you load 1 million products at the same time:

```
1 million keys
     |
     v
TTL = 1 hour
```

They may all expire around the same time:

```
1 hour later
     ↓
1 million keys expire
     ↓
Huge number of DB requests
```

That's a **cache avalanche**.

TTL jitter helps:

```
Product A → 60m 10s
Product B → 61m 03s
Product C → 60m 47s
Product D → 62m 11s
```

Expiration is distributed.

---

## 13. TTL and Negative Caching

TTL is also useful for caching "not found" results.

Suppose someone repeatedly requests:

```
user:999999
```

The user doesn't exist.

**Without negative caching:**

```
Cache MISS
   ↓
DB
   ↓
NOT FOUND
```

Every request repeats the DB query.

**Instead:**

```
user:999999 → NOT_FOUND
TTL = 30 seconds
```

Now:

```
Request
  ↓
Cache
  ↓
NOT_FOUND
```

No database query.

This is called **negative caching**.

Usually the TTL should be relatively short because the missing record could be created later.

---

## 14. TTL and Sliding Expiration

There are two common concepts.

### Fixed TTL

Suppose:

```
TTL = 10 minutes
```

Entry expires 10 minutes after it is created.

Even if it's accessed repeatedly:

```
10:00 → Created
10:05 → Read
10:09 → Read
10:10 → Expire
```

### Sliding TTL

Every access extends the expiration:

```
10:00 → Created
10:05 → Read → TTL extended
10:09 → Read → TTL extended
10:14 → Read → TTL extended
```

So the entry remains while it is actively being accessed.

This is useful for some workloads but can cause rarely changing yet continuously accessed data to remain cached indefinitely unless there is an absolute expiration limit.

---

## 15. TTL and Eviction Are Different

This is another common interview question.

### TTL expiration

```
Entry
  |
  | Time expires
  v
Removed
```

### Eviction

```
Cache memory full
       |
       v
Choose an entry
       |
       v
Remove it
```

For example:

```
Cache capacity = 10 GB

Cache becomes full
      ↓
LRU/LFU policy
      ↓
Evict entries
```

So:

> **TTL is time-based expiration.**
>
> **Eviction is usually capacity/policy-based removal.**

An item can be evicted before its TTL expires.

---

## 16. TTL in Redis

A conceptual Redis example:

```
SET user:123 "John" EX 300
```

means:

```
user:123
   |
   +-- Value: John
   |
   +-- TTL: 300 seconds
```

You can inspect remaining TTL conceptually with:

```
TTL user:123
```

For example:

```
TTL = 245 seconds
```

---

## 17. Real-World Example

Imagine an e-commerce application.

```
GET /products/123
```

Product data:

```json
{
  "id": 123,
  "name": "Laptop",
  "price": 75000
}
```

Cache:

```
product:123
TTL = 10 minutes
```

Flow:

```
                 Request
                    |
                    v
                  Redis
                 /     \
              HIT       MISS
               |          |
               v          v
            Return      Database
                           |
                           v
                         Redis
                      TTL = 10 min
                           |
                           v
                        Return
```

When the product price changes:

```
Database
   |
   | UPDATE
   v
Invalidate product:123
```

Then the next request loads the latest value.

This gives us:

```
Explicit invalidation
        +
       TTL
        +
   Cache-Aside
```

which is a very common practical design.

---

## 18. Interview Answer

If asked:

> *"What is TTL in caching?"*

A strong answer is:

> "TTL, or Time To Live, defines how long a cache entry remains valid before it expires automatically. TTL helps prevent stale data from remaining in the cache indefinitely and provides a safety mechanism even if explicit invalidation fails. However, a shorter TTL increases cache misses and database load, while a longer TTL improves the hit ratio but allows stale data to live longer. In high-scale systems, TTL jitter is often used to prevent many entries from expiring simultaneously and causing cache stampede or avalanche."

---

## Mental Model

```
                 CACHE ENTRY
                      |
                      v
              ┌───────────────┐
              │   Value       │
              │   ₹900        │
              │               │
              │   TTL: 5 min  │
              └───────┬───────┘
                      |
                 Time passes
                      |
                      v
                  EXPIRED
                      |
                      v
                   MISS
                      |
                      v
                  Database
                      |
                      v
                Fresh value
                      |
                      v
                  New TTL
```

---

## Remember these 5 points

1. **TTL = Time To Live**
2. **TTL automatically expires cache entries**
3. **Short TTL → fresher data, more DB load**
4. **Long TTL → better hit ratio, potentially staler data**
5. **TTL + jitter helps prevent cache stampede/avalanche**

---

> **The next important topic is Cache Eviction Policies** — LRU, LFU, FIFO, and how Redis decides what to remove when memory is full.

