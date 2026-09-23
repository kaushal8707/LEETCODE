# Caching

Caching is one of the most important System Design concepts.

Since you've just learned about Database Bottleneck, Connection Pooling, Load Balancer, Reverse Proxy, and Horizontal Scaling, caching is the natural next step.

The fundamental idea is:

> **Don't repeatedly fetch data from a slow/expensive source if you can temporarily keep a copy somewhere faster.**

---

## 1. What is Caching?

A cache is a temporary storage layer that keeps frequently accessed data so that future requests can retrieve it faster.

**Without cache:**

```
Client
  |
  v
Application
  |
  v
Database
  |
  v
Data
```

**With cache:**

```
Client
  |
  v
Application
  |
  v
Cache
  |
  +---- HIT  → Return data
  |
  +---- MISS → Database
```

The cache is usually much faster than going all the way to the database.

---

## 2. Real-Time Example

Imagine an e-commerce application.

A user requests:

```
GET /products/123
```

The product information is:

```
Product ID: 123
Name: iPhone
Price: ₹80,000
Category: Mobile
```

Thousands of users may request the same product.

**Without caching**

```
User 1 → Application → Database
User 2 → Application → Database
User 3 → Application → Database
User 4 → Application → Database
...
User 10000 → Application → Database
```

The database gets hammered.

**With caching**

```
User 1 → Application → Cache → MISS → Database
                              |
                              v
                            Store

User 2 → Application → Cache → HIT
User 3 → Application → Cache → HIT
User 4 → Application → Cache → HIT
...
User 10000 → Application → Cache → HIT
```

Now most requests don't reach the database.

---

## 3. Why Do We Need Caching?

Caching primarily helps with:

### 1. Lower latency

```
Database → relatively slower
Cache    → usually much faster
```

### 2. Reduce database load

```
Without Cache:
100,000 requests → Database

With Cache:
100,000 requests
      ↓
90,000 → Cache
10,000 → Database
```

### 3. Increase throughput

The system can serve more requests without increasing database workload proportionally.

### 4. Reduce infrastructure cost

Fewer database operations can reduce the resources needed at the database tier.

---

## 4. Cache Hit and Cache Miss

These two terms are extremely important.

### Cache Hit

The requested data exists in the cache.

```
Request
   |
   v
Cache
   |
   | FOUND
   v
Return Data
```

This is a:

> **Cache Hit**

### Cache Miss

The requested data doesn't exist in the cache.

```
Request
   |
   v
Cache
   |
   | NOT FOUND
   v
Database
   |
   v
Data
   |
   v
Cache
```

This is a:

> **Cache Miss**

---

## 5. Cache Hit Ratio

A very important metric is:

> **Cache Hit Ratio**

**Formula:**

```
Cache Hit Ratio =
Cache Hits / Total Requests
```

For example:

```
Total requests = 100,000
Cache hits     = 90,000
```

Then:

```
90,000 / 100,000 = 90%
```

So:

```
Cache Hit Ratio = 90%
```

A higher hit ratio generally means the cache is successfully serving more requests, although the "right" target depends heavily on the workload.

---

## 6. Cache Miss Ratio

Similarly:

```
Cache Miss Ratio =
Cache Misses / Total Requests
```

If:

```
Total = 100,000
Misses = 10,000
```

Then:

```
10%
```

And:

```
Hit Ratio + Miss Ratio = 100%
```

---

## 7. Where Can a Cache Exist?

Caching can happen at different layers.

A typical architecture might look like:

```
Client
  |
  v
Browser Cache
  |
  v
CDN
  |
  v
Reverse Proxy
  |
  v
Application Cache
  |
  v
Redis
  |
  v
Database
```

Not every system needs every layer.

---

## 8. Browser Cache

The browser can cache resources such as:

- Images
- CSS
- JavaScript
- Fonts
- Some HTTP responses

For example:

```
User
 |
 v
Browser
 |
 +---- Cache HIT → Use local copy
 |
 +---- MISS → Server
```

This prevents unnecessary network requests.

---

## 9. CDN Cache

A CDN (Content Delivery Network) can cache content closer to users geographically.

Imagine your server is in Mumbai and a user is in London.

**Without CDN:**

```
London User
    |
    | Long network path
    v
Mumbai Server
```

**With CDN:**

```
London User
    |
    v
London CDN Edge
    |
    | Cache HIT
    v
Response
```

**If the content isn't cached:**

```
London User
    |
    v
London CDN
    |
    | MISS
    v
Origin Server
```

CDNs are particularly useful for:

- Images
- Videos
- CSS
- JavaScript
- Static files
- Cacheable API responses

---

## 10. Application-Level Cache

Your application can maintain a cache.

For example:

```
Spring Boot
    |
    v
Application Cache
    |
    v
Database
```

You might cache:

- Configuration
- Frequently accessed objects
- Reference data
- Expensive computation results

However, if you have multiple application instances, an in-memory cache on one instance isn't automatically shared with the others.

---

## 11. Distributed Cache

For multiple application servers, a distributed cache is often useful.

A common example is Redis.

**Architecture:**

```
                    Load Balancer
                   /     |      \
                  v      v       v
                App1   App2    App3
                  \      |      /
                   \     |     /
                      Redis
                        |
                        v
                     Database
```

All application instances can access the same cache.

This is one reason distributed caches are common in horizontally scaled systems.

---

## 12. Why Redis?

Redis is an in-memory data store commonly used as a cache.

Conceptually:

```
Application
     |
     v
   Redis
     |
     v
 Database
```

Redis can store data such as:

```
key                 value
--------------------------------
product:123         Product JSON
user:456            User JSON
config:payment      Configuration
```

For example:

```
product:123
    ↓
{
   "id": 123,
   "name": "iPhone",
   "price": 80000
}
```

The exact data model and serialization depend on the application.

---

## 13. Cache-Aside Pattern

One of the most important caching patterns is Cache-Aside.

Also called:

> **Lazy Loading**

The application controls the cache.

**Flow:**

```
Request
   |
   v
Application
   |
   v
Check Cache
   |
   +-------- HIT --------> Return
   |
   +-------- MISS
              |
              v
           Database
              |
              v
          Put in Cache
              |
              v
            Return
```

---

## 14. Cache-Aside Example

Suppose:

```
GET /users/123
```

Application checks:

```
Redis:
user:123
```

**First request**

```
Redis
  |
  | MISS
  v
Database
  |
  v
User data
  |
  v
Redis
  |
  v
Application
```

**Second request**

```
Application
     |
     v
Redis
     |
     | HIT
     v
User data
```

Database isn't contacted.

---

## 15. Java/Spring Boot Conceptual Example

Conceptually:

```java
public User getUser(Long id) {

    User user = redis.get("user:" + id);

    if (user != null) {
        return user; // Cache hit
    }

    user = userRepository.findById(id);

    redis.set("user:" + id, user);

    return user;
}
```

The actual implementation would depend on your Redis client and serialization setup.

The logic is:

```
Check Cache
   |
   +-- Found → Return
   |
   +-- Not Found
          ↓
       Database
          ↓
       Store Cache
          ↓
       Return
```

---

## 16. TTL — Time To Live

Caching data forever is usually dangerous.

That's why cache entries often have a TTL.

**TTL means:**

> How long the cached entry is considered valid before it expires.

**Example:**

```
product:123
TTL = 10 minutes
```

**Timeline:**

```
12:00 → Cache entry created
12:05 → Still valid
12:09 → Still valid
12:10 → Expires
```

**Next request:**

```
Cache
  |
  | MISS / expired
  v
Database
```

---

## 17. Why TTL Is Important

Suppose product price changes:

```
Database:
₹80,000 → ₹75,000
```

But cache still contains:

```
₹80,000
```

If there is no expiration or invalidation strategy, users may see stale data.

TTL limits how long stale data can remain in the cache.

But TTL alone doesn't guarantee immediate consistency.

---

## 18. Cache Invalidation

One of the famous engineering problems is:

> *"There are only two hard things in Computer Science: cache invalidation and naming things..."*

When database data changes, the cache may contain an old copy.

**Example:**

```
Database:
Price = ₹80,000

Cache:
Price = ₹80,000
```

Now database changes:

```
Database:
Price = ₹75,000
```

Cache still contains:

```
₹80,000
```

You need a strategy.

---

## 19. Cache Invalidation Strategy

One approach:

```
Update Database
      |
      v
Delete Cache Entry
```

**Example:**

```sql
UPDATE product
SET price = 75000
WHERE id = 123;
```

```
DELETE product:123 FROM CACHE;
```

**Next read:**

```
Cache
  |
  | MISS
  v
Database
  |
  v
₹75,000
  |
  v
Cache
```

---

## 20. Write-Through Cache

Another pattern is Write-Through.

```
Application
    |
    v
Cache
    |
    v
Database
```

The application writes through the cache, and the cache synchronously writes to the database.

Conceptually:

```
Write
  |
  v
Cache
  |
  v
Database
```

This can keep cache and database more closely synchronized, but it adds complexity and write latency.

---

## 21. Write-Behind / Write-Back Cache

Another pattern:

```
Application
     |
     v
Cache
     |
     | Later
     v
Database
```

The cache acknowledges the write and the database is updated asynchronously.

**Advantage:**

> Fast writes

But there is a bigger consistency/durability risk if the cache fails before the database is updated.

This pattern should therefore be used carefully.

---

## 22. Read-Through Cache

With a read-through cache:

```
Application
     |
     v
Cache
     |
     +---- HIT → Return
     |
     +---- MISS
             |
             v
          Database
```

The cache itself is responsible for loading the data on a miss, depending on the caching technology/framework.

This differs from Cache-Aside, where the application explicitly performs the database read and then populates the cache.

---

## 23. Cache Eviction

What happens when the cache becomes full?

You need an eviction policy.

Common policies include:

### LRU

**Least Recently Used**

Remove data that hasn't been accessed recently.

```
A → frequently used
B → frequently used
C → rarely used
D → rarely used

Cache full

Evict C/D first
```

### LFU

**Least Frequently Used**

Remove data that has been accessed the fewest times.

### FIFO

**First In, First Out**

Remove the oldest entries first.

The best policy depends on the access pattern and cache implementation.

---

## 24. Cache Stampede

This is a very important production problem.

Suppose:

> Cache entry expires

At exactly the same time:

> 10,000 requests arrive.

All see:

```
CACHE MISS
```

Then:

```
10,000 requests
       |
       v
    Database
       🔥
```

The database suddenly receives a huge number of requests.

This is called a:

> **Cache Stampede**

or sometimes:

> **Thundering Herd**

---

## 25. How Can Cache Stampede Be Reduced?

Common techniques include:

### Locking

Allow one request to rebuild the cache while others wait or use stale data.

```
Request 1 → DB → Populate Cache
Request 2 → Wait
Request 3 → Wait
Request 4 → Wait
```

### Stale-while-revalidate

Serve slightly stale data while refreshing the cache in the background.

### TTL jitter

Instead of all entries expiring at exactly the same time:

```
TTL = 10 min ± random jitter
```

This spreads expirations.

---

## 26. Cache Penetration

Another problem occurs when clients repeatedly request data that doesn't exist.

For example:

```
GET /users/999999999
```

Database:

```
User doesn't exist
```

If you don't cache that negative result, every request may hit the database.

```
Request
 ↓
Cache MISS
 ↓
Database
 ↓
NOT FOUND

Request
 ↓
Cache MISS
 ↓
Database
 ↓
NOT FOUND

...
```

Possible approaches include caching negative results for a short period and using mechanisms such as Bloom filters where appropriate.

---

## 27. Cache Avalanche

Imagine thousands of cache entries all have:

```
TTL = 1 hour
```

and were populated around the same time.

One hour later:

```
Thousands expire
        ↓
Thousands of requests
        ↓
Database
        🔥
```

This is often called a **cache avalanche**.

TTL jitter and controlled refresh strategies can help spread the load.

---

## 28. Cache Consistency

Caching introduces a fundamental trade-off:

```
More caching
     ↓
Better performance
     ↓
Potentially more stale data
```

For example:

**Product catalog**

A few seconds/minutes of staleness may be acceptable.

```
Cache TTL = 5 minutes
```

**Bank account balance**

You generally don't want a stale cached balance to drive a financial transaction.

```
Database / authoritative source
```

The caching strategy depends heavily on the business requirement.

---

## 29. What Should You Cache?

Good candidates often have:

- High read frequency
- Low modification frequency
- Expensive computation/query
- Data that tolerates some staleness

**Examples:**

- Product catalog
- Country/state lists
- Configuration
- User profiles
- Popular articles
- Permissions/reference data
- Computed results

Poor candidates may include highly dynamic or strongly consistent data where stale values are unacceptable.

---

## 30. What Should You NOT Cache Blindly?

Don't cache everything.

Be careful with:

- Highly sensitive data
- Rapidly changing data
- Data requiring strong consistency
- Huge objects
- Data with very low reuse

Caching has its own costs:

- Memory
- Infrastructure
- Complexity
- Invalidation
- Consistency issues
- Operational overhead

---

## 31. Cache and Database Bottleneck

Now connect this with what you learned previously.

**Without caching:**

```
                    Requests
                       |
                       v
                   Application
                       |
                       v
                   Database 🔥
```

**With caching:**

```
                    Requests
                       |
                       v
                   Application
                       |
                       v
                    Cache
                  /       \
              HIT           MISS
               |              |
               v              v
            Response       Database
                              |
                              v
                           Cache
```

So:

```
Cache
  ↓
Database requests ↓
  ↓
Database load ↓
  ↓
Database bottleneck reduced
```

---

## 32. Connection Pool + Cache

This is another useful connection to your previous topic.

**Without cache:**

```
Request
   ↓
Connection Pool
   ↓
Database
```

Every request that needs data may require a database connection.

**With cache:**

```
Request
   ↓
Cache
   |
   +---- HIT → Response
   |
   +---- MISS
          ↓
     Connection Pool
          ↓
       Database
```

Therefore cache hits can reduce pressure on:

- Database
- Connection Pool
- Network

---

## 33. Cache + Horizontal Scaling

Suppose you have:

```
                  Load Balancer
                 /      |      \
                v       v       v
              App1    App2    App3
```

### Local/in-memory cache

Each server has its own cache:

```
App1 → Cache1
App2 → Cache2
App3 → Cache3
```

The caches aren't automatically shared.

You might get:

```
User → App1 → Cache1 → HIT

Another user → App2 → Cache2 → MISS
                         ↓
                      Database
```

### Distributed cache

With Redis:

```
                  Load Balancer
                 /      |      \
                v       v       v
              App1    App2    App3
                \       |      /
                 \      |     /
                    Redis
                      |
                      v
                   Database
```

Now all application instances can access the same cache.

---

## 34. Complete Architecture

Let's combine all the concepts you've learned:

```
                         Users
                           |
                           v
                          DNS
                           |
                           v
                Reverse Proxy / LB
                           |
              +------------+------------+
              |            |            |
              v            v            v
            App1         App2         App3
              \            |            /
               \           |           /
                +----------+----------+
                           |
                           v
                         Redis
                           |
                      Cache MISS
                           |
                           v
                   Connection Pool
                           |
                           v
                       Database
```

The request path becomes:

```
User
 ↓
DNS
 ↓
Reverse Proxy / Load Balancer
 ↓
Application
 ↓
Redis
 ↓
 ├── HIT  → Return
 │
 └── MISS → Connection Pool
              ↓
           Database
              ↓
           Redis
              ↓
           Return
```

---

## 35. Interview Question

**"Why do we use caching?"**

A strong answer:

> Caching stores frequently accessed data in a faster storage layer so subsequent requests can be served with lower latency while reducing load on the database or other expensive downstream systems.

---

## 36. Interview Question

**"What happens on a cache miss?"**

```
Request
   ↓
Cache
   ↓
MISS
   ↓
Database
   ↓
Get data
   ↓
Populate cache
   ↓
Return response
```

---

## 37. Interview Question

**"What is cache invalidation?"**

> Cache invalidation is the process of removing or updating cached data when the underlying source of truth changes, so that stale data isn't served indefinitely.

---

## 38. Interview Question

**"What is TTL?"**

> TTL (Time To Live) specifies how long a cache entry remains valid before it expires.

---

## 39. Interview Question

**"Why can't we just cache everything?"**

Because:

```
Cache
 ↓
Limited memory
 ↓
Eviction required
```

And caching introduces:

- Stale data
- Invalidation complexity
- Consistency problems
- Memory cost
- Operational complexity

---

## 40. The Mental Model

Remember this architecture:

```
                         REQUEST
                            |
                            v
                    Reverse Proxy / LB
                            |
                            v
                       Application
                            |
                            v
                         CACHE
                       /       \
                      /         \
                  HIT             MISS
                   |                |
                   v                v
               RESPONSE       Connection Pool
                                    |
                                    v
                                DATABASE
                                    |
                                    v
                                  CACHE
                                    |
                                    v
                                RESPONSE
```

And remember these five terms:

```
CACHE HIT
    ↓
Data found in cache

CACHE MISS
    ↓
Data not found → fetch from source

TTL
    ↓
How long the entry remains valid

EVICTION
    ↓
Which entries should be removed

INVALIDATION
    ↓
How stale entries are removed/updated
```
