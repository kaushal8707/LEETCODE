# Cache Eviction

Cache eviction is the process of removing data from the cache when the cache needs space or when a configured eviction policy decides that an entry should be removed.

The key idea is:

> **TTL answers "when should this entry expire?"**
>
> **Eviction answers "which entry should we remove when the cache needs space?"**

This distinction is very important.

---

## 1. Why Do We Need Cache Eviction?

Imagine Redis has:

```
Cache capacity = 10 GB
```

Over time:

```
Cache
 ├── user:101
 ├── user:102
 ├── product:1
 ├── product:2
 ├── product:3
 ├── ...
 └── product:999999
```

Eventually:

```
Used memory = 10 GB
Available memory = 0
```

Now a new item needs to be stored:

```
SET product:1000000
```

But there is no room.

The cache needs to decide:

> *"Which existing entry should I remove?"*

That's **cache eviction**.

---

## 2. Basic Flow

```
                Application
                     |
                     v
                  Cache
                     |
                     v
              Memory becomes full
                     |
                     v
              Eviction Policy
                     |
            ┌────────┼────────┐
            v        v        v
           LRU      LFU      FIFO
            |
            v
       Remove entry
            |
            v
       Store new entry
```

---

## 3. Eviction vs TTL

These are often confused.

### TTL

An entry has a lifetime:

```
product:123
TTL = 10 minutes
```

After 10 minutes:

```
EXPIRED
```

### Eviction

Cache memory becomes full:

```
Cache = 10 GB / 10 GB
```

The cache removes an entry according to its eviction policy.

That entry could have:

```
TTL remaining = 8 minutes
```

It doesn't matter—the cache may evict it early because memory is needed.

So:

```
TTL
 ↓
Time-based expiration

Eviction
 ↓
Memory/capacity-based removal
```

---

## 4. Most Important Eviction Policies

For system design interviews, know these well:

- LRU
- LFU
- FIFO
- Random
- TTL-based
- No eviction / reject writes

The most important ones are **LRU** and **LFU**.

---

## 5. LRU — Least Recently Used

LRU means:

> **Remove the item that hasn't been accessed for the longest time.**

Suppose the cache contains:

```
A
B
C
D
```

Access pattern:

```
A → B → C → A → D
```

Recent usage becomes approximately:

```
A = recently used
D = recently used
C
B = least recently used
```

If the cache is full and needs space:

```
Evict B
```

Because B hasn't been accessed for the longest time.

### LRU Example

Suppose cache capacity is only 3 items.

Initially:

```
[A, B, C]
```

Access:

```
A
```

Now:

```
[B, C, A]
```

Access:

```
B
```

Now:

```
[C, A, B]
```

New item:

```
D
```

Cache is full, so:

```
Evict C
```

Result:

```
[A, B, D]
```

Because C was least recently used.

---

## 6. Why LRU Works Well

Many applications have **temporal locality**.

That means:

> *"If something was accessed recently, there's a good chance it will be accessed again soon."*

For example:

```
User opens Amazon
       ↓
Views laptop
       ↓
Views laptop details
       ↓
Adds laptop to cart
```

Recently accessed product data is more likely to be accessed again.

Therefore LRU is often a good default.

---

## 7. LFU — Least Frequently Used

LFU means:

> **Remove the item that has been accessed the fewest times.**

Suppose:

```
A → accessed 100 times
B → accessed 50 times
C → accessed 2 times
D → accessed 1 time
```

If we need to evict one:

```
Evict D
```

because D has the lowest access frequency.

---

## 8. LRU vs LFU

Consider:

```
A → accessed 1,000 times yesterday
B → accessed 10 times recently
```

If A hasn't been accessed recently:

### LRU

May remove:

```
A
```

because it hasn't been used recently.

### LFU

May keep:

```
A
```

because it has a high historical frequency.

This is the key difference:

> **LRU → "When was it last used?"**
>
> **LFU → "How often is it used?"**

---

## 9. When Is LFU Better?

LFU can work well when you have **hot data**.

Example:

```
Product A → 1,000,000 requests
Product B → 500,000 requests
Product C → 10 requests
```

You want popular products to stay cached.

LFU recognizes:

```
A = HOT 🔥
B = HOT 🔥
C = COLD
```

So C becomes a stronger eviction candidate.

---

## 10. FIFO — First In, First Out

FIFO means:

> **Remove the item that entered the cache first.**

Example:

```
A → inserted first
B
C
D → inserted last
```

If the cache is full:

```
Evict A
```

It doesn't care how frequently or recently A was accessed.

```
FIFO:
Oldest item → Evict
```

It's simple, but often less effective than LRU/LFU for workloads with locality.

---

## 11. Random Eviction

Another simple approach:

```
[A, B, C, D]
```

Cache needs space.

It randomly chooses:

```
C
```

and removes it.

**Advantages:**

- Very simple
- Low overhead

**Disadvantage:**

> *Could remove a very popular item.*

---

## 12. No Eviction

Some systems can be configured to reject new writes when memory is full:

```
Cache full
   |
   v
New write
   |
   v
REJECT
```

This can be appropriate when you don't want existing entries automatically removed.

But then the application needs to handle the failure.

---

## 13. TTL-Based Expiration

TTL can also participate in memory management.

Example:

```
A → TTL 10 sec
B → TTL 100 sec
C → TTL 1 hour
```

A expires first.

But remember:

> **Expiration and eviction are conceptually different.**

An item can expire naturally because its TTL ended, or it can be evicted early because the cache needs space.

---

## 14. Real Example

Suppose we have:

```
Redis memory = 1 GB
```

Our application stores:

- User profiles
- Product information
- Session data
- Recommendations

Eventually:

```
Memory:
1 GB / 1 GB
```

New request:

```
SET recommendation:user:500
```

Redis needs space.

**With LRU:**

```
Least recently accessed key
          ↓
       Evict
          ↓
Store recommendation:user:500
```

**With LFU:**

```
Least frequently accessed key
          ↓
       Evict
          ↓
Store recommendation:user:500
```

The choice depends on your workload.

---

## 15. Cache Eviction Is Not Cache Invalidation

This is a very important interview distinction.

### Invalidation

The underlying data changed:

```
Database
₹1,000 → ₹900
       ↓
Invalidate cache
```

The goal is:

> **Correctness**

### Eviction

Cache needs space:

```
Cache full
   ↓
Evict old/less useful entry
```

The goal is:

> **Memory management**

So:

```
Invalidation → Data correctness
Eviction     → Capacity management
TTL          → Automatic expiration
```

---

## 16. What Happens After Eviction?

Suppose:

```
product:123
```

gets evicted.

The database still has:

```
product:123 → ₹900
```

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
₹900
     |
     v
Cache
```

So eviction generally **doesn't** mean data is permanently lost if the database remains the source of truth.

It means:

> *The next request has to pay the cost of loading it again.*

---

## 17. Eviction and Cache Hit Ratio

Suppose:

```
1,000,000 requests
```

With a good eviction strategy:

```
900,000 cache hits
100,000 cache misses
```

Hit ratio:

```
90%
```

A poor eviction strategy might remove frequently accessed items:

```
600,000 hits
400,000 misses
```

Hit ratio:

```
60%
```

Therefore, the eviction policy can have a major impact on:

- Cache hit ratio
- Database load
- Latency
- Infrastructure cost

---

## 18. A Common Interview Scenario

Interviewer:

> *"You have a cache with limited memory. Which eviction policy would you choose?"*

Don't immediately say:

> *"LRU."*

Instead answer based on workload.

### If recently accessed data is likely to be accessed again:

```
LRU
```

### If a small set of items is extremely popular:

```
LFU
```

### If simplicity is more important:

```
FIFO
```

Then say:

> *"I would validate the choice using cache hit ratio and workload characteristics."*

That's a stronger system-design answer.

---

## 19. LRU vs LFU Example

Imagine an e-commerce website.

```
iPhone → accessed 1,000,000 times
Laptop → accessed 500,000 times
Old product → accessed 5 times
```

LFU is very good at identifying:

```
iPhone  → HOT
Laptop  → HOT
Old     → COLD
```

But imagine a flash sale:

> *Old product suddenly becomes extremely popular.*

LRU can react quickly to recent activity.

So:

```
LFU → good for long-term popularity
LRU → good for recent popularity
```

In real systems, policies can also be more sophisticated than pure textbook LRU/LFU.

---

## 20. Production Considerations

At scale, don't look only at the eviction algorithm.

Monitor:

- Cache hit ratio
- Cache miss ratio
- Eviction rate
- Memory usage
- Key distribution
- Hot keys
- Latency
- Database load

For example:

```
Hit Ratio       = 95%
Eviction Rate   = 2,000/sec
Memory Usage    = 98%
DB CPU          = 80%
```

This tells you the cache may be under significant memory pressure.

---

## 21. Redis Example

Redis supports configurable eviction policies such as:

- `noeviction`
- `allkeys-lru`
- `allkeys-lfu`
- `allkeys-random`
- `volatile-lru`
- `volatile-lfu`
- `volatile-random`
- `volatile-ttl`

The exact choice depends on whether you want eviction to consider:

- all keys
- only keys with TTL
- recency
- frequency
- randomness
- remaining TTL

For interviews, understanding **why** you choose a policy is more important than memorizing every Redis configuration name.

---

## 22. Interview Answer

If asked:

> *"What is cache eviction?"*

A strong answer is:

> "Cache eviction is the process of removing entries from a cache when the cache reaches its memory or capacity limit. The cache uses an eviction policy such as LRU, LFU, FIFO, or random eviction to decide which entry to remove. LRU removes the least recently accessed item, while LFU removes the least frequently accessed item. The right policy depends on workload characteristics and should be evaluated using metrics such as cache hit ratio, eviction rate, latency, and backend load."

---

## The mental model

```
                   CACHE
                     |
              Memory is full
                     |
                     v
             ┌───────────────┐
             │ Eviction      │
             │ Policy        │
             └───────┬───────┘
                     |
          ┌──────────┼──────────┐
          ↓          ↓          ↓
         LRU        LFU        FIFO
          |          |          |
          v          v          v
      Least       Least       Oldest
      recently    frequently  inserted
      used        used        item
          \          |          /
           \         |         /
            └────────┼────────┘
                     ↓
                  EVICT
                     |
                     v
              Free memory
                     |
                     v
               New entry
```

---

## Remember the difference

| Concept | Question it answers |
|---|---|
| TTL | "How long should this entry live?" |
| Invalidation | "When is this data no longer valid?" |
| Eviction | "Which entry should I remove when space is needed?" |
| LRU | "Which entry was used least recently?" |
| LFU | "Which entry was used least frequently?" |

