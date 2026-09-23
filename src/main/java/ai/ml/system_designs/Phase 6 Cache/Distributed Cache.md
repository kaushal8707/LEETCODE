# Distributed Cache

A **distributed cache** is a cache that is shared across multiple application instances and usually runs on separate cache servers/nodes.

The key idea is:

> **Instead of each application server maintaining its own cache, multiple application servers use a common distributed cache.**

Common technologies include **Redis** and **Memcached**.

---

## 1. Why Do We Need a Distributed Cache?

Consider a system with three application instances:

```
                    Load Balancer
                   /      |      \
                  /       |       \
                 ↓        ↓        ↓
              App-1     App-2    App-3
```

Suppose each application has its own local cache:

```
App-1 → Local Cache
App-2 → Local Cache
App-3 → Local Cache
```

Now:

```
App-1 cache:
user:101 → John

App-2 cache:
user:101 → ?

App-3 cache:
user:101 → ?
```

The same data may need to be stored three times.

Worse, the caches can become **inconsistent**.

---

## 2. Local Cache vs Distributed Cache

### Local/In-Memory Cache

```
             Load Balancer
            /      |      \
           ↓       ↓       ↓
        App-1    App-2    App-3
          |        |        |
       Cache     Cache     Cache
```

Each application owns its cache.

Examples:

- Java `ConcurrentHashMap`
- Caffeine
- Guava Cache

### Distributed Cache

```
             Load Balancer
            /      |      \
           ↓       ↓       ↓
        App-1    App-2    App-3
           \       |       /
            \      |      /
             ↓     ↓     ↓
             Distributed
                Cache
                  |
               Redis
```

All application instances can access the same cache.

---

## 3. Real-World Example

Suppose an e-commerce application has:

```
GET /products/123
```

Product data is:

```
Product 123
Name: iPhone
Price: ₹100000
Stock: 25
```

**Without caching:**

```
Request
   ↓
Application
   ↓
Database
   ↓
Product
```

If 100,000 users request the same product:

```
100,000 requests
        ↓
    Database
```

That's expensive.

**With a distributed cache:**

```
100,000 requests
        ↓
      Redis
        ↓
     Cache HIT
        ↓
    Response
```

The database might receive very few requests.

---

## 4. Why "Distributed"?

Because the cache itself can be distributed across multiple nodes.

For example:

```
                 Application
                      |
                      v
               Redis Cluster
             /       |       \
            v        v        v
         Node-1    Node-2    Node-3
```

Data can be distributed:

```
Node-1 → user:101
Node-2 → user:102
Node-3 → user:103
```

This allows the cache to handle:

- more memory
- more throughput
- more connections
- node failures

---

## 5. Basic Architecture

A typical architecture looks like:

```
                         Clients
                            |
                            v
                       Load Balancer
                            |
              ┌─────────────┼─────────────┐
              ↓             ↓             ↓
           App-1          App-2         App-3
              \             |             /
               \            |            /
                └───────────┼───────────┘
                            ↓
                    Distributed Cache
                            |
                    ┌───────┼───────┐
                    ↓       ↓       ↓
                 Cache-1 Cache-2 Cache-3
                            |
                            v
                        Database
```

---

## 6. Cache-Aside with Distributed Cache

The most common pattern is **Cache-Aside**.

Flow:

```
Request
   ↓
Application
   ↓
Distributed Cache
   |
   ├── HIT ──→ Return data
   |
   └── MISS
        ↓
      Database
        ↓
   Put into Cache
        ↓
     Return
```

Example:

```
GET user:101
      ↓
    Redis
      ↓
    MISS
      ↓
   Database
      ↓
 John
      ↓
Redis: user:101 → John
      ↓
 Response
```

Next request:

```
GET user:101
      ↓
    Redis
      ↓
    HIT
      ↓
    John
```

---

## 7. Why Distributed Cache Improves Scalability

Suppose you have:

```
10 application servers
```

and each receives:

```
10,000 requests/sec
```

Total:

```
100,000 requests/sec
```

If all requests go to the database:

```
100,000
   ↓
Database
```

The DB can become a bottleneck.

With caching:

```
100,000 requests
       ↓
Distributed Cache
       ↓
98,000 HIT
2,000 MISS
       ↓
Database
```

Now the DB handles only approximately:

```
2,000 requests/sec
```

instead of:

```
100,000 requests/sec
```

The exact numbers depend on the workload, but the architectural principle is important.

---

## 8. Distributed Cache vs Local Cache

| Feature | Local Cache | Distributed Cache |
|---|---|---|
| Location | Application memory | Separate cache infrastructure |
| Shared between instances | ❌ No | ✅ Yes |
| Network call | ❌ Usually no | ✅ Yes |
| Latency | Very low | Low, but network-dependent |
| Memory | Limited per app | Can scale across nodes |
| Consistency | Hard across instances | Easier to centralize |
| Failure impact | Usually isolated | Potentially broader |
| Scalability | Limited | Higher |
| Example | Caffeine | Redis |

---

## 9. The Biggest Advantage

The biggest advantage is **shared state**.

Imagine:

```
              Load Balancer
             /      |      \
            ↓       ↓       ↓
          App-1   App-2   App-3
             \      |      /
              \     |     /
               ↓    ↓    ↓
                  Redis
```

If App-1 stores:

```
user:123 → John
```

App-2 can immediately access:

```
user:123 → John
```

and App-3 can also access it.

You don't need to replicate the cache manually between application servers.

---

## 10. But Distributed Cache Has a Cost

The cache is no longer inside the application process.

Therefore:

```
Application
     ↓
Network
     ↓
Redis
```

There is **network latency**.

**Local cache:**

```
Application → Memory
```

**Distributed cache:**

```
Application → Network → Redis
```

So typically:

```
Local cache
   ↓
Fastest

Distributed cache
   ↓
Slightly slower

Database
   ↓
Usually much slower
```

This leads to an important architecture:

```
L1 Cache → L2 Cache → Database
```

---

## 11. Multi-Level Cache

A sophisticated system may use:

```
                  Application
                       |
                       v
                  L1 Cache
               Local Memory
                       |
                    MISS
                       ↓
                  L2 Cache
                    Redis
                       |
                    MISS
                       ↓
                   Database
```

For example:

```
Request
  ↓
Caffeine
  ↓
HIT → Return
  |
 MISS
  ↓
Redis
  ↓
HIT → Return
  |
 MISS
  ↓
Database
```

This gives you:

```
L1 → extremely low latency
L2 → shared cache
DB → source of truth
```

---

## 12. How Does Distributed Cache Know Which Node Has the Data?

When the cache has multiple nodes:

```
Redis Cluster

Node-1
Node-2
Node-3
```

the system needs to determine where a key belongs.

A common technique is **consistent hashing** or a hash-slot-based distribution mechanism.

Conceptually:

```
hash(key)
   ↓
partition / slot
   ↓
cache node
```

Example:

```
user:101
   ↓
hash()
   ↓
slot 500
   ↓
Node-2
```

Another key:

```
user:102
   ↓
hash()
   ↓
slot 900
   ↓
Node-3
```

This allows the cache to distribute data across nodes.

---

## 13. Consistent Hashing

Suppose you have:

```
Node A
Node B
Node C
```

Keys are mapped based on their hash:

```
              Hash Ring

          A
       /     \
     key1   key2

   C           B

     key3   key4
```

If Node B fails, ideally you don't want every key to move.

Only a **portion** of the keys need reassignment.

This is one of the reasons consistent hashing is useful in distributed caching.

---

## 14. Replication

Distributed cache can also replicate data.

```
            Primary
              Node
               |
          ┌────┴────┐
          ↓         ↓
       Replica-1  Replica-2
```

If the primary fails:

```
Primary
   ↓
FAIL
   ↓
Replica
   ↓
Continue serving
```

This improves availability.

---

## 15. Distributed Cache Failure

This is extremely important from a system-design perspective.

Never assume:

> *"Redis is always available."*

Suppose:

```
Application
     ↓
Redis
     ↓
DOWN
```

If every request requires Redis:

```
Application
     ↓
Redis failure
     ↓
Application failure
```

That's dangerous.

A better architecture has a **fallback**:

```
Application
     ↓
Redis
     |
    FAIL
     ↓
Local Cache / DB / fallback
```

Depending on the data and business requirements.

---

## 16. Cache Failure Can Cause a Cache Avalanche

Suppose:

```
100 application servers
       ↓
Redis
       ↓
FAIL
```

Now:

```
100 servers
     ↓
All requests
     ↓
Database
```

The database may suddenly receive massive traffic.

This is effectively a **cache avalanche** caused by cache infrastructure failure.

Therefore distributed cache must be designed with:

- replication
- failover
- clustering
- timeouts
- circuit breakers
- rate limiting
- bulkheads
- fallback strategies

---

## 17. Distributed Cache and Consistency

This is one of the hardest parts.

Suppose:

```
Database:
Price = ₹1000

Cache:
Price = ₹900
```

Now DB is updated:

```
Database:
Price = ₹1100
```

but cache still contains:

```
₹900
```

Users can receive stale data.

**Typical solution:**

```
Update DB
   ↓
Invalidate Cache
```

or:

```
Update DB
   ↓
Update Cache
```

or event-driven:

```
DB update
   ↓
Event
   ↓
Kafka
   ↓
Cache invalidation consumer
   ↓
Redis
```

---

## 18. Cache Invalidation in Distributed Systems

Imagine three application instances:

```
App-1
App-2
App-3
```

and Redis:

```
Redis
```

If App-1 updates:

```
user:101
```

the shared distributed cache allows all applications to see the same cached state.

But if you have local L1 caches:

```
App-1 → Caffeine
App-2 → Caffeine
App-3 → Caffeine
```

you now have to invalidate all three local caches.

A common pattern is:

```
DB update
   ↓
Event
   ↓
Kafka
   ↓
All application instances
   ↓
Invalidate L1
```

This is one reason multi-level caching is more complicated than simply using Redis.

---

## 19. Hot Key Problem

Distributed caching introduces another important problem:

> **Hot keys.**

Suppose:

```
product:iphone
```

is requested:

```
500,000 times/sec
```

All those requests may target the same cache key.

Even though Redis is distributed:

```
500,000 requests
       ↓
product:iphone
       ↓
One cache shard
```

One node can become overloaded.

This is called a **hot key problem**.

Possible solutions include:

```
Local L1 cache
+
Key replication
+
Request coalescing
+
Read replicas
+
Key splitting where appropriate
```

---

## 20. Distributed Cache and Cache Stampede

Suppose:

```
product:123
```

is a hot key.

It expires:

```
Cache
  ↓
MISS
```

10,000 application instances may try to reload it.

You can use:

```
Distributed Lock
       ↓
One instance refreshes
       ↓
Redis
       ↓
Other instances use cached value
```

This is why **distributed locking + distributed cache** often appear together in system design.

---

## 21. Distributed Cache and TTL

Every cache entry can have a TTL:

```
user:101
value = John
TTL = 300 seconds
```

After expiration:

```
TTL → 0
 ↓
Expired
 ↓
Cache MISS
```

For distributed systems, avoid synchronized expiration:

```
TTL = 300 seconds
```

for millions of entries created at exactly the same time.

Use:

```
TTL + random jitter
```

to reduce **cache avalanche** risk.

---

## 22. What Should You Put in a Distributed Cache?

### Good candidates:

```
✓ Frequently read data
✓ Expensive DB queries
✓ Product information
✓ User profiles
✓ Session data
✓ Configuration
✓ Reference data
✓ Computed results
✓ Frequently accessed metadata
```

### Be careful with:

```
✗ Highly sensitive data
✗ Data requiring strict immediate consistency
✗ Huge objects
✗ Data that is rarely reused
✗ Data whose cache cost exceeds DB cost
```

Most importantly:

> **The cache should usually not be your only source of truth.**

For important persistent business data:

```
Database = source of truth
Cache = performance optimization
```

---

## 23. Distributed Cache vs Database

Don't think of Redis as simply a faster database.

Their roles are different:

```
Database
   ↓
Durable source of truth

Cache
   ↓
Fast temporary copy
```

Typical architecture:

```
                Application
                     |
                     v
             Distributed Cache
                /        \
              HIT        MISS
               |           |
               |           v
               |        Database
               |           |
               |       Populate cache
               |           |
               └───────────┘
```

---

## 24. Important Design Decisions

When designing a distributed cache, think about:

### 1. Cache strategy

- Cache-Aside
- Read-Through
- Write-Through
- Write-Back

### 2. Eviction

- LRU
- LFU
- TTL

### 3. Consistency

- Strong-ish
- Eventual
- Stale acceptable

### 4. Availability

- Replication
- Failover
- Multi-zone

### 5. Scaling

- Sharding
- Partitioning
- Cluster

### 6. Failure protection

- Timeout
- Circuit Breaker
- Bulkhead
- Rate Limiting
- Fallback

### 7. Stampede protection

- Distributed Lock
- Request Coalescing
- TTL Jitter
- Background Refresh

---

## 25. Distributed Cache in a Complete System

A senior-level architecture might look like:

```
                         Clients
                            |
                            v
                       Load Balancer
                            |
             ┌──────────────┼──────────────┐
             ↓              ↓              ↓
          App-1           App-2          App-3
             |              |              |
             └──────────────┼──────────────┘
                            |
                         L1 Cache
                       Local Memory
                            |
                          MISS
                            ↓
                    Distributed Cache
                       Redis Cluster
                    /       |       \
                   ↓        ↓        ↓
                Node-1   Node-2   Node-3
                   |        |        |
                   └────────┼────────┘
                            |
                          MISS
                            ↓
                         Database
```

And around this you may add:

- TTL + Jitter
- Distributed Lock
- Rate Limiting
- Circuit Breaker
- Replication
- Failover
- Monitoring

---

## 26. What Happens If Redis Goes Down?

This is a classic interview question.

### Bad answer:

> *"All requests will go to the database."*

That's incomplete.

A senior answer considers **database protection**.

For example:

```
Request
   ↓
L1 Cache
   ↓
Redis
   ↓
Redis unavailable
   ↓
Circuit Breaker
   ↓
Fallback
```

Depending on the data:

```
Fallback options:
    ↓
Serve L1 cached value
OR
Serve stale value
OR
Query DB with strict rate limits
OR
Return degraded response
```

This prevents:

```
Redis failure
     ↓
DB receives all traffic
     ↓
DB failure
     ↓
Application failure
```

---

## 27. Monitoring a Distributed Cache

Important metrics include:

- Cache hit ratio
- Cache miss ratio
- Eviction rate
- Memory utilization
- CPU utilization
- Network throughput
- Cache latency
- Commands/sec
- Connections
- Hot keys
- Replication lag
- Node failures
- Failover events
- Lock contention

Especially:

### Cache Hit Ratio

```
Cache Hit Ratio =
Cache Hits / Total Cache Requests
```

For example:

```
Hits = 95,000
Total = 100,000

Hit ratio = 95%
```

Higher isn't automatically better, but a sudden drop is often a useful signal.

---

## 28. Interview Questions

### Q1. What is a distributed cache?

> A distributed cache is a shared caching layer accessible by multiple application instances, usually deployed across multiple nodes for scalability and availability.

### Q2. Why not use local cache?

> Local cache has extremely low latency, but its data is isolated to one application instance. Distributed cache provides shared state across instances and can scale independently.

### Q3. What happens if distributed cache fails?

> The application should have timeouts and fallback behavior and protect the database using circuit breakers, bulkheads, rate limiting, and possibly L1 cache or stale data.

### Q4. How do you scale a distributed cache?

> Primarily through partitioning/sharding, adding cache nodes, and using replication for availability.

### Q5. What is a hot key?

> A hot key is a cache key receiving disproportionately high traffic, potentially overloading the cache node responsible for that key.

### Q6. How do you handle hot keys?

> L1 caching, replication, request coalescing, and workload-specific key distribution strategies can reduce concentration on one cache node.

### Q7. What if all cache entries expire simultaneously?

> That's a cache avalanche. Use TTL jitter, background refresh, cache warming, and backend protection.

---

## 29. Distributed Cache vs LRU

Don't confuse these concepts.

Distributed cache answers:

> **Where/how is the cache deployed?**

LRU answers:

> **Which item should be removed when the cache is full?**

They can work together:

```
Distributed Cache
       |
       ↓
Redis Cluster
       |
       ↓
Eviction Policy
       |
       ↓
LRU / LFU / etc.
```

Similarly:

```
Distributed Cache
       +
TTL
       +
LRU
       +
Cache-Aside
```

These are different dimensions of caching architecture.

---

## 30. Distributed Cache — Mental Model

Remember this architecture:

```
                 APPLICATION SERVERS
              /        |        \
             ↓         ↓         ↓
           App-1     App-2     App-3
              \        |        /
               \       |       /
                ↓      ↓      ↓
               DISTRIBUTED CACHE
                  Redis Cluster
               /       |       \
              ↓        ↓        ↓
           Node-1    Node-2    Node-3
                         |
                       MISS
                         ↓
                     DATABASE
```

The key benefits are:

```
✓ Shared cache
✓ Lower DB load
✓ Lower latency
✓ Independent scaling
✓ High throughput
✓ Cache availability through replication/failover
```

But you must design for:

```
⚠ Network latency
⚠ Cache failure
⚠ Stale data
⚠ Hot keys
⚠ Cache stampede
⚠ Cache avalanche
⚠ Cache invalidation
⚠ Memory limits
⚠ Operational complexity
```

---

### One-line senior interview answer

> **"A distributed cache is a shared, independently scalable caching layer used by multiple application instances to reduce database load and latency. It typically uses sharding for scalability and replication/failover for availability, while requiring careful handling of consistency, invalidation, hot keys, cache stampede, cache avalanche, and cache failure."**

