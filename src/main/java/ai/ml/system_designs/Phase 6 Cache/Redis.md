# Redis

**Redis** is an in-memory data store commonly used as a distributed cache, but it can also be used as a database, message broker, queue, counter store, session store, and coordination mechanism.

For system design, the most important mental model is:

> **Redis keeps frequently accessed data in memory so applications can read/write it much faster than going to a disk-based database.**

Since you're learning caching and have already covered **LRU → Cache Stampede → Cache Penetration → Cache Avalanche → Distributed Cache**, Redis is the natural next step.

---

## 1. What is Redis?

Redis originally stood for **REmote DIctionary Server**.

Conceptually:

```
                 Application
                      |
                      | Network
                      ↓
                    Redis
                      |
                 In-memory data
                      |
                      ↓
                  Database
```

Instead of:

```
Request
   ↓
Database
   ↓
Disk / storage
   ↓
Response
```

we can do:

```
Request
   ↓
Redis
   ↓
RAM
   ↓
Response
```

Because memory access is extremely fast, Redis can handle very high request rates.

---

## 2. Why Do We Need Redis?

Imagine:

```
1,000,000 requests/sec
```

and every request queries your database:

```
1,000,000 requests
        ↓
     Database
        ↓
     Overload
```

Instead:

```
1,000,000 requests
        ↓
      Redis
        ↓
   950,000 HIT
        ↓
    Response

50,000 MISS
        ↓
      DB
```

Redis dramatically reduces the number of database requests.

---

## 3. Redis Architecture

A simple architecture:

```
                    Clients
                       |
                       ↓
                  Load Balancer
                       |
             ┌─────────┼─────────┐
             ↓         ↓         ↓
          App-1      App-2      App-3
             \         |         /
              \        |        /
               └───────┼───────┘
                       ↓
                     Redis
                       |
                     MISS
                       ↓
                   Database
```

Redis is usually deployed **separately** from application servers.

**Why?**

Because:

```
Application memory
       ≠
Shared cache memory
```

Multiple application instances can share Redis.

---

## 4. Redis Is Primarily In-Memory

This is one of the most important things to understand.

Redis stores active data primarily in:

```
RAM
```

Conceptually:

```
Redis
┌──────────────────────────┐
│ RAM                      │
│                          │
│ user:101 → John          │
│ user:102 → Alice         │
│ product:101 → ₹1000      │
│ session:abc → ...        │
└──────────────────────────┘
```

RAM is much faster than disk-based database storage.

But Redis can also persist data to disk using mechanisms such as:

- RDB
- AOF

So:

> **In-memory does not necessarily mean "no persistence."**

---

## 5. Redis Data Structures

One of Redis's biggest strengths is that it isn't just a simple:

```
key → string
```

store.

Redis provides several useful data structures.

The important ones are:

- String
- Hash
- List
- Set
- Sorted Set
- Stream
- Bitmap
- HyperLogLog

Let's understand the most important ones.

---

## 6. Redis String

The simplest data type.

```
key → value
```

Example:

```
user:101:name → "John"
```

Commands:

```
SET user:101:name "John"

GET user:101:name
```

Result:

```
John
```

Strings are also useful for **counters**.

```
INCR page:views
```

If:

```
page:views = 100
```

then:

```
INCR
```

makes it:

```
101
```

This atomic counter capability is very useful.

---

## 7. Redis Hash

A **Hash** is useful for representing an object.

Instead of:

```
user:101:name → John
user:101:age → 30
user:101:city → Mumbai
```

you can have:

```
user:101
    |
    ├── name → John
    ├── age  → 30
    └── city → Mumbai
```

Example:

```
HSET user:101 name "John" age 30 city "Mumbai"
```

Read:

```
HGET user:101 name
```

or:

```
HGETALL user:101
```

This is useful for:

- user profiles
- product metadata
- configuration
- session information

---

## 8. Redis List

A **List** is an ordered collection.

```
[A] [B] [C] [D]
```

Useful for:

- queues
- task lists
- recent activity
- simple work queues

Example:

```
LPUSH jobs "job-1"
LPUSH jobs "job-2"
```

Conceptually:

```
jobs
 ↓
[job-2] [job-1]
```

---

## 9. Redis Set

A **Set** contains unique values.

```
users
 ↓
{101, 102, 103, 104}
```

Adding the same value twice doesn't create duplicates.

Useful for:

- unique users
- tags
- memberships
- relationships

Example:

```
SADD online-users 101
SADD online-users 102
```

Check membership:

```
SISMEMBER online-users 101
```

---

## 10. Redis Sorted Set

A **Sorted Set** stores values with scores.

Example:

```
Leaderboard

Alice → 1000
Bob   → 900
John  → 800
```

Conceptually:

```
1000 → Alice
 900 → Bob
 800 → John
```

This is extremely useful for:

- leaderboards
- ranking
- priority queues
- time-based data
- top-N queries

For example:

```
ZADD leaderboard 1000 Alice
ZADD leaderboard 900 Bob
ZADD leaderboard 800 John
```

Then retrieve highest-ranked users.

---

## 11. Redis Streams

**Redis Streams** are useful for event/stream processing.

Conceptually:

```
Producer
   |
   ↓
Redis Stream
   |
   ├── Event 1
   ├── Event 2
   ├── Event 3
   └── Event 4
   |
   ↓
Consumers
```

They can be useful for:

- event processing
- activity feeds
- event pipelines
- consumer groups

However, for large-scale durable event streaming, **Kafka** is often a better fit depending on the requirements.

---

## 12. Redis TTL

TTL is extremely important for caching.

Example:

```
SET user:101 "John" EX 300
```

This means:

```
user:101
   ↓
John
   ↓
TTL = 300 seconds
```

After 300 seconds:

```
Entry expires
```

You can check:

```
TTL user:101
```

This is how Redis commonly implements temporary cached data.

---

## 13. Redis + Cache-Aside

This is probably the most common Redis usage in system design.

```
                Application
                     |
                     ↓
                   Redis
                  /     \
               HIT       MISS
                |          |
                ↓          ↓
             Return      Database
                            |
                            ↓
                         Redis
                            |
                            ↓
                         Return
```

Example:

```
GET /users/101
```

Application:

```
GET user:101
```

Redis:

```
MISS
```

Application:

```
SELECT * FROM users WHERE id = 101
```

Database returns:

```
John
```

Application:

```
SET user:101 "John" EX 300
```

Future requests:

```
GET user:101
     ↓
Redis HIT
     ↓
John
```

---

## 14. Redis and Cache Invalidation

Suppose:

```
Database:
user:101 → John

Redis:
user:101 → John
```

Now user changes name:

```
Database
John → James
```

If Redis isn't updated:

```
Redis → John
```

Users may get stale data.

**Typical Cache-Aside approach:**

```
Update DB
   ↓
Delete Redis key
```

Example:

```sql
UPDATE users
SET name = 'James'
WHERE id = 101;
```

```
DEL user:101
```

Next request:

```
Redis MISS
   ↓
DB
   ↓
James
   ↓
Redis
```

---

## 15. Redis Eviction

Redis has limited memory.

Suppose:

```
Redis memory = 10 GB
```

and eventually:

```
Memory usage → 10 GB
```

Redis needs an eviction policy.

Common policies include:

- LRU
- LFU
- Random
- TTL-based
- No eviction

For example:

```
allkeys-lru
```

means Redis can evict the least recently used keys.

This connects directly to the **LRU** concept you just learned.

---

## 16. Redis LRU

Suppose:

```
Cache capacity = 3
```

Entries:

```
A
B
C
```

Access:

```
A
B
```

Order:

```
B → A → C
```

Now insert:

```
D
```

Redis needs space.

LRU removes:

```
C
```

Result:

```
D → B → A
```

So:

```
Redis
  ↓
Memory full
  ↓
Eviction policy
  ↓
LRU
  ↓
Evict old key
```

---

## 17. Redis Persistence

Redis can persist data to disk.

Two important mechanisms are:

### RDB

**RDB = Redis Database snapshot**

Redis periodically creates snapshots:

```
Redis RAM
    ↓
Snapshot
    ↓
RDB file
    ↓
Disk
```

**Advantages:**

- compact
- good for backups
- relatively efficient

**Potential downside:**

> If Redis crashes between snapshots, changes since the last snapshot can be lost.

---

## 18. AOF

**AOF = Append Only File**

Redis records write operations.

Conceptually:

```
SET user:101 John
SET user:102 Alice
DEL user:101
```

are written to the append-only log.

After restart, Redis can replay operations to reconstruct state.

Compared with RDB:

```
RDB → snapshot-based
AOF → operation/log-based
```

AOF can provide stronger durability characteristics, depending on its configuration.

---

## 19. RDB vs AOF

|  | RDB | AOF |
|---|---|---|
| Model | Snapshot | Write log |
| File size | Usually smaller | Usually larger |
| Recovery | Fast | Can be slower |
| Data loss window | Snapshot interval | Depends on fsync policy |
| Good for | Backups/snapshots | More durability |
| Performance overhead | Generally lower | Generally higher |

You can also use both.

---

## 20. Redis Replication

For high availability, Redis can have replicas.

```
             Redis Primary
                  |
          ┌───────┴───────┐
          ↓               ↓
      Replica-1        Replica-2
```

Writes generally go to the primary:

```
Application
     ↓
Primary
     ↓
Replicas
```

If the primary fails, a replica can potentially be promoted depending on the deployment architecture.

---

## 21. Redis Sentinel

**Redis Sentinel** provides monitoring and automatic failover for Redis deployments.

Conceptually:

```
              Sentinel
             /   |   \
            ↓    ↓    ↓
        Redis Primary
          /        \
         ↓          ↓
     Replica-1   Replica-2
```

Sentinel can:

- monitor Redis nodes
- detect failures
- coordinate failover
- promote a replica

This helps improve availability.

---

## 22. Redis Cluster

When a single Redis node isn't enough, **Redis Cluster** can distribute data across multiple nodes.

```
                    Redis Cluster

             ┌─────────┼─────────┐
             ↓         ↓         ↓
          Node-1     Node-2     Node-3
          slots      slots      slots
             |         |          |
          keys       keys       keys
```

For example:

```
user:101 → Node-1
user:102 → Node-2
user:103 → Node-3
```

This provides **horizontal scaling**.

---

## 23. Redis Cluster vs Sentinel

Important interview distinction.

### Sentinel

Primarily focuses on:

- High availability
- Monitoring
- Failover

### Cluster

Primarily focuses on:

- Horizontal scaling
- Data partitioning
- High availability

Simplified:

```
Sentinel
   ↓
"Keep Redis available"

Cluster
   ↓
"Distribute Redis data and scale it"
```

---

## 24. Redis Sharding

Suppose you have:

```
1 TB of cache data
```

but one Redis server has insufficient memory.

You can shard:

```
                 Redis
                   |
          ┌────────┼────────┐
          ↓        ↓        ↓
       Shard-1  Shard-2  Shard-3
```

Keys are distributed based on hashing/slots.

Conceptually:

```
hash(key)
    ↓
slot
    ↓
Redis node
```

This allows you to scale memory and throughput horizontally.

---

## 25. Redis Atomic Operations

Redis provides many **atomic** operations.

For example:

```
INCR counter
```

is atomic.

Suppose:

```
counter = 100
```

Two requests execute:

```
INCR counter
INCR counter
```

Result:

```
102
```

This makes Redis useful for:

- counters
- rate limiting
- quotas
- inventory-like counters in some designs
- distributed coordination

---

## 26. Redis and Rate Limiting

Redis is commonly used to implement rate limiting.

Suppose:

```
User = 101
Limit = 100 requests/minute
```

Conceptually:

```
user:101:requests → 57
```

Each request:

```
INCR user:101:requests
```

When:

```
count > 100
```

reject:

```
HTTP 429
```

You can combine this with TTL:

```
user:101:requests
TTL = 60 seconds
```

Redis is very well suited to this because of its fast atomic operations and expiration support.

---

## 27. Redis Distributed Lock

Redis can also be used for coordination/locking.

Conceptually:

```
SET lock:product:123 <unique-token> NX EX 10
```

Meaning roughly:

- **NX** → only create if key doesn't exist
- **EX** → expiration

If successful:

```
Application A
     ↓
Acquires lock
     ↓
Refreshes cache
```

Other applications:

```
Application B
     ↓
Lock exists
     ↓
Wait / retry / use stale data
```

This is useful for mitigating cache stampede, although distributed locking requires careful correctness considerations.

---

## 28. Redis and Cache Stampede

Suppose:

```
product:123
```

is extremely popular.

TTL expires:

```
Redis
  ↓
MISS
```

Thousands of application servers try to reload it.

Use a lock:

```
              Redis MISS
                   |
                   ↓
            Distributed Lock
                   |
             One request
                   |
                   ↓
                  DB
                   |
                   ↓
                 Redis
                   |
                   ↓
             Other requests
              get cached data
```

This prevents 10,000 requests from simultaneously querying the DB.

---

## 29. Redis and Cache Penetration

Suppose:

```
user:999999
```

doesn't exist.

**Without negative caching:**

```
Request
 ↓
Redis MISS
 ↓
DB
 ↓
NOT FOUND
```

Every request repeats this.

**With negative caching:**

```
user:999999 → NULL
TTL = 60 sec
```

Future requests:

```
Redis HIT
   ↓
NULL
   ↓
404
```

No DB query.

---

## 30. Redis and Cache Avalanche

Suppose millions of keys have:

```
TTL = 1 hour
```

and were inserted at the same time.

They can expire together:

```
Redis
 ↓
Millions of keys expire
 ↓
Millions of cache misses
 ↓
Database overload
```

Use:

```
TTL + Jitter
```

For example:

```
TTL = 1 hour + random(0–10 min)
```

This spreads expiration.

---

## 31. Redis Performance Mental Model

Think of the latency hierarchy roughly as:

```
CPU registers
      ↓
CPU cache
      ↓
RAM
      ↓
Redis over network
      ↓
Database
      ↓
Disk / external systems
```

Redis is fast because its active working set is in memory and its data structures/operations are optimized for low latency.

But remember:

> **Redis is not as fast as accessing local process memory, because application → Redis requires network communication.**

Therefore:

```
L1 Local Cache
     ↓
Redis
     ↓
Database
```

can be a powerful architecture.

---

## 32. Redis Failure Scenario

This is critical in system design.

Suppose:

```
Application
     ↓
Redis
     ↓
FAIL
```

A naive system does:

```
Redis failure
     ↓
Every request → DB
     ↓
DB overload
```

**Better:**

```
                Application
                     |
                 L1 Cache
                     |
                  Redis
                     |
                  Failure
                     |
             ┌───────┴────────┐
             ↓                ↓
        Stale/L1 data      Controlled DB
                              traffic
```

Add:

- Timeout
- Circuit Breaker
- Bulkhead
- Rate Limiting
- Fallback

This connects directly to the fault-tolerance concepts you've already studied.

---

## 33. Redis Is Not Always the Source of Truth

A very important architectural principle:

```
Database
   ↓
Source of Truth

Redis
   ↓
Performance Layer
```

If Redis loses:

```
Redis data
   ↓
Lost
```

the application should ideally be able to reconstruct it from the database, unless Redis is intentionally being used as a durable primary datastore for that particular use case.

---

## 34. When Should You Use Redis?

### Good use cases:

```
✓ Distributed cache
✓ Session store
✓ Rate limiting
✓ Counters
✓ Leaderboards
✓ Short-lived state
✓ Distributed locks/coordination
✓ Pub/sub
✓ Queues
✓ Streams
✓ Frequently accessed data
```

### Be careful when:

```
⚠ Data must never be stale
⚠ Dataset doesn't fit available memory
⚠ Strong relational queries are required
⚠ Complex joins are required
⚠ Redis becomes the only copy accidentally
```

---

## 35. Redis vs Kafka

Since you're learning Kafka too, this distinction is important.

| Redis | Kafka |
|---|---|
| In-memory data store | Distributed event streaming platform |
| Very low latency | High-throughput event streaming |
| Key-value/data structures | Durable ordered logs |
| Cache | Event backbone |
| TTL common | Retention-based |
| Random key lookup | Sequential/partitioned consumption |
| Great for counters/rate limiting | Great for event streams |
| Usually current state | Historical event stream |

Example:

```
Redis:
user:101 → current profile
```

Kafka:

```
UserCreated
UserUpdated
UserAddressChanged
UserDeleted
...
```

A useful mental model:

> **Redis answers "What is the current value?"**
>
> **Kafka answers "What events happened?"**

---

## 36. Redis in a Production System

A mature architecture could look like:

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
                            v
                       L1 Cache
                     Local Memory
                            |
                          MISS
                            ↓
                    Redis Cluster
                 ┌──────────┼──────────┐
                 ↓          ↓          ↓
              Node-1     Node-2     Node-3
                 |          |          |
              Replica     Replica    Replica
                            |
                          MISS
                            ↓
                         Database
```

**Supporting mechanisms:**

```
Redis:
├── TTL
├── LRU/LFU eviction
├── Replication
├── Failover
├── Persistence
├── Sharding
└── Cluster

Application:
├── Cache-Aside
├── Distributed Lock
├── Circuit Breaker
├── Rate Limiting
├── Bulkhead
└── Graceful Degradation
```

---

## 37. Most Important Redis Interview Questions

### 1. Why is Redis fast?

> Redis keeps active data primarily in memory and uses efficient data structures and operations, avoiding many disk I/O operations. Network latency still exists because applications usually access Redis remotely.

### 2. Is Redis a database or cache?

> It can be used as both. In many architectures it is used as a distributed cache, but it also supports persistence and richer data structures.

### 3. Redis vs local cache?

> Local cache is faster because it avoids network communication, but it is isolated to one application instance. Redis provides shared state across instances.

### 4. What happens if Redis goes down?

> The application should fail gracefully using timeouts, circuit breakers, L1 cache/stale data where possible, controlled DB fallback, and database protection mechanisms.

### 5. What is Redis Cluster?

> Redis Cluster distributes data across multiple nodes using hash slots, allowing horizontal scaling and providing cluster-level availability mechanisms.

### 6. Sentinel vs Cluster?

> Sentinel focuses primarily on monitoring and automatic failover, while Cluster provides data partitioning and horizontal scaling in addition to availability features.

### 7. How does Redis handle memory pressure?

> Through configurable eviction policies such as LRU, LFU, random, or TTL-based policies, depending on configuration.

### 8. How do you prevent cache stampede with Redis?

> Use distributed locking/request coalescing, double-check the cache after acquiring the lock, TTL jitter, background refresh, and stale-while-revalidate where acceptable.

### 9. How do you implement rate limiting?

> Redis atomic counters combined with TTL are a common approach; token-bucket or sliding-window algorithms can also be implemented using Redis data structures/scripts.

### 10. Can Redis lose data?

> Yes. Depending on the persistence and replication configuration, crashes or failures can result in data loss. Therefore the durability guarantees must be explicitly designed rather than assumed.

---

## 38. Redis — The Complete Mental Model

Think about Redis in six layers:

```
                 REDIS
                   |
     ┌─────────────┼─────────────┐
     ↓             ↓             ↓
 Data Types      Memory       Persistence
     |             |             |
 String          LRU/LFU       RDB/AOF
 Hash            TTL
 List
 Set
 Sorted Set
 Stream
     |
     ↓
Distribution
     |
 ┌───┴────┐
 ↓        ↓
Cluster  Replication
     |
     ↓
Availability
     |
 Sentinel / Failover
     |
     ↓
Application Patterns
     |
 ┌───┼─────────────┐
 ↓   ↓             ↓
Cache Rate       Lock
     Limit
```

And from the system-design perspective:

```
                  APPLICATION
                       |
                    L1 Cache
                       |
                    Redis
                       |
                 ┌─────┴─────┐
                 ↓           ↓
                HIT         MISS
                 |           |
              Return        DB
                             |
                             ↓
                          Redis
```

With protection:

```
Redis
 ↓
TTL + Jitter
 ↓
LRU/LFU
 ↓
Replication
 ↓
Cluster
 ↓
Failover
 ↓
Circuit Breaker
 ↓
Rate Limiting
```

---

### The one answer to remember for interviews

> **"Redis is a high-performance in-memory data store commonly used as a distributed cache. It supports rich data structures such as strings, hashes, lists, sets, sorted sets and streams. In system design, Redis is typically placed between application services and the database to reduce latency and database load. For production, we need to consider TTL, eviction, cache invalidation, persistence, replication, clustering, failover, hot keys, stampede/avalanche protection, and what happens when Redis itself fails."**

