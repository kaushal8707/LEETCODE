# Cache Invalidation

Cache invalidation means removing or updating cached data when the original data changes, so that users don't receive stale or incorrect information.

The famous system-design saying is:

> **"There are only two hard things in Computer Science: cache invalidation and naming things."**

---

## 1. Why Do We Need Cache Invalidation?

Suppose we have:

```
Database:
Product 123 → ₹1,000
```

And cache contains:

```
Redis:
product:123 → ₹1,000
```

Now someone changes the product price:

```
Database:
Product 123 → ₹900
```

But Redis still has:

```
Cache:
product:123 → ₹1,000
```

Now:

```
User
  |
  v
Cache
  |
  v
₹1,000   ❌
```

The correct value is:

```
Database
  |
  v
₹900
```

This is **stale cache data**.

Cache invalidation solves this problem.

---

## 2. Basic Cache Invalidation Flow

A common approach is:

```
                Application
                /          \
               v            v
          Database        Cache
               |             |
               |             |
               └── UPDATE ───┘
                     ↓
                Invalidate
```

For example:

1. Update Database
2. Delete cache entry

```
DB:
product:123 = ₹900

Cache:
product:123 = DELETE
```

Next read:

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
 ₹900
    |
    v
 Cache
```

So the cache gets the latest value.

---

## 3. Three Common Invalidation Strategies

There are three major approaches you should know:

- Delete / Invalidate
- Update the cache
- TTL-based expiration

Let's understand each.

---

## 4. Strategy 1 — Delete the Cache

This is one of the most common approaches with Cache-Aside.

Suppose:

```
Cache:
user:123 → name="John"
```

User changes their name:

```
John → David
```

Application:

1. UPDATE database
2. DELETE `user:123` from cache

Then:

```
Database = David
Cache    = missing
```

Next read:

```
Cache → MISS
   ↓
Database → David
   ↓
Cache → David
```

This is called **cache invalidation**.

### Why delete instead of update?

Because deleting the cache is often simpler and safer than trying to keep two independently updated copies synchronized.

---

## 5. Strategy 2 — Update the Cache

Instead of deleting:

```
Database = ₹900
Cache    = ₹1,000
```

you can update both:

```
Database = ₹900
Cache    = ₹900
```

Flow:

```
Application
    |
    +----> Database UPDATE
    |
    +----> Cache UPDATE
```

This avoids the next cache miss.

But it creates a consistency problem:

```
DB update → SUCCESS
Cache update → FAILURE
```

Now:

```
Database = ₹900
Cache    = ₹1,000  ❌
```

So updating the cache requires careful failure handling.

---

## 6. Strategy 3 — TTL

**TTL = Time To Live**

You give every cache entry an expiration time.

For example:

```
product:123 → ₹1,000
TTL = 5 minutes
```

After 5 minutes:

```
product:123 → EXPIRED
```

Next request:

```
Cache → MISS
   ↓
Database
   ↓
Latest value
```

TTL is extremely useful because it provides automatic expiration.

---

## 7. TTL Does NOT Mean Perfect Invalidation

Suppose:

```
TTL = 1 hour
```

At:

```
10:00 → Cache ₹1,000
```

Database changes:

```
10:05 → ₹900
```

But cache remains:

```
₹1,000
```

until:

```
11:00
```

So users could see stale data for up to almost an hour.

Therefore:

> **TTL is an expiration mechanism, not necessarily immediate cache invalidation.**

---

## 8. Cache Invalidation with Cache-Aside

This is extremely important because you just learned Cache-Aside.

A common pattern is:

```
                READ
                 |
                 v
              Cache
             /     \
          HIT       MISS
           |          |
           v          v
        Return       DB
                       |
                       v
                     Cache
                       |
                       v
                    Return
```

For writes:

```
                WRITE
                  |
                  v
              Database
                  |
                  v
            Delete Cache
```

So:

```
READ:
Cache → DB on MISS

WRITE:
DB → Invalidate Cache
```

---

## 9. The Dangerous Race Condition

Here's where cache invalidation becomes interesting in system design.

Suppose:

```
Cache = ₹1,000
Database = ₹1,000
```

Two requests happen simultaneously.

### Request A — Update

```
UPDATE DB → ₹900
```

### Request B — Read

At almost the same time:

```
Read Cache → ₹1,000
```

Depending on the exact ordering, you can temporarily serve stale data.

Even worse, consider:

```
A: Update DB → ₹900
B: Cache MISS
B: Read DB → ₹900
A: Delete Cache
B: Write ₹900 to Cache
```

This is okay.

But different ordering can produce stale values.

For example:

```
A: Read old DB value ₹1,000
B: Update DB → ₹900
B: Delete Cache
A: Write ₹1,000 into Cache
```

Now:

```
Database = ₹900
Cache    = ₹1,000 ❌
```

This is one of the reasons cache invalidation is hard.

---

## 10. Cache Invalidation Ordering

A very common recommendation for Cache-Aside is:

```
UPDATE DATABASE
       ↓
DELETE CACHE
```

rather than:

```
DELETE CACHE
       ↓
UPDATE DATABASE
```

### Why?

Consider:

```
DELETE CACHE
      ↓
Another request reads
      ↓
Cache MISS
      ↓
Reads OLD DB value
      ↓
Populates cache with OLD value
      ↓
Original request updates DB
```

Now:

```
DB    = NEW
Cache = OLD ❌
```

So generally:

> **Update DB first, then invalidate cache.**

But even this isn't a universal guarantee under concurrency; high-scale systems may need versioning, locks, CDC/events, or other coordination mechanisms.

---

## 11. Event-Based Cache Invalidation

Another powerful approach is using events.

Suppose:

```
Order Service
      |
      v
Database
      |
      v
OrderUpdated Event
      |
      v
Kafka
      |
      v
Cache Invalidation Consumer
      |
      v
Redis
```

When data changes:

```
Database updated
      ↓
Publish event
      ↓
Kafka
      ↓
Consumer
      ↓
Invalidate cache
```

For example:

```
UserUpdated
```

consumer receives:

```
UserUpdated(userId=123)
```

and executes:

```
DELETE user:123
```

This is useful in distributed systems where many services may cache the same data.

---

## 12. CDC-Based Invalidation

A more advanced architecture uses **Change Data Capture (CDC)**.

```
Database
    |
    | change
    v
CDC
    |
    v
Kafka
    |
    v
Cache Invalidation
    |
    v
Redis
```

For example:

```
DB:
price 1000 → 900
```

CDC detects the change:

```
ProductUpdated {
    productId: 123,
    price: 900
}
```

Then the cache layer can:

```
DELETE product:123
```

or:

```
UPDATE product:123 → ₹900
```

This can be useful when the database is modified by multiple applications and you don't want every writer to manually handle cache invalidation.

---

## 13. Cache Invalidation vs Cache Eviction

These terms are related but not the same.

### Invalidation

Means:

> *"This cached value is no longer valid."*

Example:

```
Product updated
     ↓
DELETE product:123
```

### Eviction

Means:

> *"Remove an item from cache because of a cache-management policy."*

For example:

```
Redis memory full
     ↓
Evict least recently used item
```

Common eviction policies include:

- **LRU** — Least Recently Used
- **LFU** — Least Frequently Used
- **FIFO** — First In, First Out
- **TTL expiration**

So:

```
Invalidation → correctness
Eviction     → memory management
```

This distinction is very useful in interviews.

---

## 14. Cache Invalidation + TTL

In production, you will often combine both.

For example:

```
Cache:
product:123 → ₹900
TTL = 30 minutes
```

When product changes:

```
UPDATE DB
   ↓
DELETE CACHE immediately
```

And if something goes wrong with invalidation:

```
TTL eventually removes stale data
```

So:

```
Explicit invalidation
        +
       TTL
        ↓
Better protection against stale data
```

TTL is a **safety net**, not a substitute for correct invalidation when freshness matters.

---

## 15. Important Distributed-System Problems

Once you understand cache invalidation, several advanced problems naturally follow.

### Cache Stampede

A popular key expires:

```
Cache expires
     ↓
10,000 requests
     ↓
10,000 DB queries
```

Database gets overloaded.

### Cache Penetration

Requests repeatedly ask for data that doesn't exist:

```
GET user:999999999

Cache → MISS
DB → NOT FOUND
```

Next request:

```
Cache → MISS
DB → NOT FOUND
```

Repeatedly.

A common mitigation is **negative caching**.

### Cache Avalanche

Many cache entries expire around the same time:

```
100,000 keys
     ↓
Expire together
     ↓
Huge DB traffic
```

TTL jitter/randomization can help.

---

## 16. Real-Time Example: Product Price

Let's put everything together.

Initial state:

```
Database:
Product 123 = ₹1,000

Cache:
product:123 = ₹1,000
```

Product price changes:

```
₹1,000 → ₹900
```

Recommended Cache-Aside flow:

```
             UPDATE
                |
                v
          ┌───────────┐
          │ Database  │
          │   ₹900    │
          └─────┬─────┘
                |
                v
          DELETE CACHE
                |
                v
          ┌───────────┐
          │   Redis   │
          │  MISSING  │
          └───────────┘
```

Next request:

```
User
 |
 v
Redis
 |
 | MISS
 v
Database
 |
 | ₹900
 v
Redis
 |
 | store
 v
User
```

Now:

```
Database = ₹900
Cache    = ₹900
```

---

## 17. Interview Answer

If an interviewer asks:

> *"What is cache invalidation?"*

A strong answer:

> "Cache invalidation is the process of removing or updating cached data when the underlying source of truth changes, so stale data isn't served. Common strategies include explicit deletion, updating the cache, and TTL-based expiration. In a Cache-Aside architecture, a common approach is to update the database first and then invalidate the corresponding cache key. At scale, invalidation can also be event-driven using Kafka or CDC. The main challenges are concurrent updates, race conditions, stale reads, cache stampedes, and ensuring invalidation events aren't lost."

---

## The mental model to remember

```
                 DATA CHANGES
                      |
                      v
               ┌─────────────┐
               │  Database   │
               └──────┬──────┘
                      |
             Invalidate / Update
                      |
                      v
               ┌─────────────┐
               │    Cache    │
               └─────────────┘
```

And remember:

> **Cache invalidation is primarily about correctness. Cache eviction is primarily about memory management. TTL is primarily about automatic expiration.**

