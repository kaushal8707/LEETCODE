# Cache-Aside Pattern

Cache-Aside is the most commonly used caching pattern in system design.

It is also called **Lazy Loading**.

The key idea is:

> **The application is responsible for reading from the cache and loading data into the cache when there is a cache miss.**

---

## 1. Basic Architecture

```
             Client
                |
                v
         ┌─────────────┐
         │ API Server  │
         └──────┬──────┘
                |
                v
         ┌─────────────┐
         │    Cache    │
         │   Redis     │
         └──────┬──────┘
                |
             MISS
                |
                v
         ┌─────────────┐
         │  Database   │
         └─────────────┘
```

The application controls the entire process.

---

## 2. Read Flow

Suppose we need:

```
GET /users/123
```

### Step 1 — Check Cache

Application asks Redis:

```
GET user:123
```

There are two possibilities.

### Cache HIT

```
Application
     |
     v
   Redis
     |
     | HIT
     v
 User 123
```

The application immediately returns the cached data.

```
Client
  |
  v
API
  |
  v
Redis
  |
  v
Response
```

Database is **not** called.

---

## 3. Cache MISS

Suppose Redis doesn't contain `user:123`.

```
Application
     |
     v
   Redis
     |
     | MISS
     v
   Database
```

The application then queries the database:

```sql
SELECT * FROM users WHERE id = 123;
```

Database returns:

```json
{
  "id": 123,
  "name": "John",
  "email": "john@example.com"
}
```

The application then puts the result into Redis:

```
SET user:123 {...}
```

Finally, it returns the response to the client.

**Complete flow:**

```
Client
  |
  v
Application
  |
  v
Cache
  |
  | MISS
  v
Database
  |
  | data
  v
Application
  |
  +----> Cache
  |
  v
Client
```

---

## 4. Why Is It Called "Cache-Aside"?

Because the application sits **beside** the cache and explicitly manages it.

The application decides:

```
Read cache
    ↓
If missing → read DB
    ↓
Put result in cache
```

The cache itself doesn't automatically know how to retrieve the data from the database.

---

## 5. Pseudocode

The classic Cache-Aside read logic is:

```
getUser(id):

    user = cache.get(id)

    if user != null:
        return user          // Cache HIT

    user = database.get(id)  // Cache MISS

    cache.put(id, user)

    return user
```

That's the entire core concept.

---

## 6. Real Example with Redis

Suppose we have:

```
GET /products/100
```

Application:

```java
Product product = redis.get("product:100");

if (product == null) {

    product = database.findProductById(100);

    redis.set("product:100", product, TTL);

}

return product;
```

**First request:**

```
Redis → MISS
DB    → Product
Redis → Store Product
Client → Product
```

**Second request:**

```
Redis → HIT
Client → Product
```

So repeated database queries are avoided.

---

## 7. Write Flow

This is where things become interesting.

Suppose:

```
Product price = ₹1,000
```

Cache:

```
product:100 → ₹1,000
```

Database:

```
product:100 → ₹1,000
```

Now the price changes to:

```
₹900
```

With Cache-Aside, a common write strategy is:

```
Application
    |
    +----> Database UPDATE
    |
    +----> Cache DELETE
```

For example:

```sql
UPDATE product
SET price = 900
WHERE id = 100;

DELETE product:100 FROM Redis;
```

Then the next read:

```
Cache → MISS
   ↓
Database → ₹900
   ↓
Cache → Store ₹900
```

This keeps the cache refreshed lazily.

---

## 8. Why Delete Instead of Update?

A common Cache-Aside approach is:

```
Write DB
   ↓
Invalidate cache
```

rather than:

```
Write DB
   ↓
Update cache
```

**Why?**

Because updating both independently can introduce additional consistency problems.

For example:

```
DB update → SUCCESS
Cache update → FAILURE
```

Now:

```
DB    = ₹900
Cache = ₹1,000
```

If instead we delete the cache after updating the DB:

```
DB update → SUCCESS
Cache delete → SUCCESS
```

the next read repopulates the cache from the database.

---

## 9. Complete Cache-Aside Pattern

```
                    ┌──────────────┐
                    │    Client    │
                    └──────┬───────┘
                           │
                           ▼
                    ┌──────────────┐
                    │ Application  │
                    └──────┬───────┘
                           │
                    ┌──────▼───────┐
                    │    Cache     │
                    │    Redis     │
                    └──────┬───────┘
                       HIT │ MISS
                           │
                           ▼
                    ┌──────────────┐
                    │   Database   │
                    └──────────────┘
```

### Read

**Cache HIT**

```
   ↓
Return data
```

**Cache MISS**

```
   ↓
Read DB
   ↓
Populate Cache
   ↓
Return data
```

### Write

```
Update DB
   ↓
Invalidate Cache
```

---

## 10. Advantages

### ✅ Simple

The application has complete control.

### ✅ Only frequently accessed data gets cached

Data enters the cache when somebody actually requests it.

### ✅ Cache failure doesn't necessarily mean application failure

If Redis is unavailable, the application can potentially fall back to the database.

```
Cache unavailable
       ↓
Database
       ↓
Response
```

Although this can dramatically increase database load, so production systems often need protection against that scenario.

### ✅ Works very well for read-heavy workloads

For example:

```
100,000 reads
10 writes
```

Cache-Aside is a very natural fit.

---

## 11. Disadvantages

### ❌ Cache Miss causes extra latency

First request:

```
Application
   ↓
Cache
   ↓
Database
   ↓
Cache
   ↓
Application
```

More steps than a cache hit.

### ❌ Stale Data

If invalidation fails:

```
Database = ₹900
Cache    = ₹1,000
```

The application may return stale data.

### ❌ Cache Stampede

Suppose a popular cache entry expires:

```
product:100 expires
```

Suddenly:

```
10,000 requests
      ↓
10,000 Cache MISS
      ↓
10,000 DB queries
```

This can overload the database.

We'll later discuss:

- Request coalescing
- Distributed locks
- Early refresh
- TTL jitter

to handle this.

---

## 12. Cache-Aside vs Write-Through

This distinction is very important in interviews.

### Cache-Aside

Application manages cache:

```
READ:

App → Cache
       ↓ MISS
     Database
       ↓
     Cache


WRITE:

App → Database
       ↓
   Invalidate Cache
```

### Write-Through

Application writes to cache, and the cache synchronously writes to the database:

```
Application
     |
     v
   Cache
     |
     v
 Database
```

So:

> **Cache-Aside** = application manages cache population.
>
> **Write-Through** = cache participates in the write path.

---

## 13. Cache-Aside vs Read-Through

Another common interview question.

### Cache-Aside

Application knows about both:

```
Application
   |
   +---- Cache
   |
   +---- Database
```

Application says:

> *"Cache miss → I'll query DB."*

### Read-Through

Application only talks to the cache:

```
Application
      |
      v
    Cache
      |
      v
  Database
```

The cache layer handles the miss.

So:

```
Cache-Aside:
Application handles MISS

Read-Through:
Cache handles MISS
```

---

## 14. When Should You Use Cache-Aside?

Cache-Aside is a strong choice when:

- Reads greatly outnumber writes
- Data is expensive to retrieve
- Data doesn't need perfect real-time freshness
- You want simple application-level control
- Only a subset of data is frequently accessed

**Typical examples:**

- Product catalog
- User profiles
- Configuration
- Frequently accessed reference data
- Popular content
- Metadata

---

## 15. Interview Answer

If the interviewer asks:

> *"Explain Cache-Aside."*

You can say:

> "Cache-Aside, also called Lazy Loading, is a caching pattern where the application explicitly checks the cache first. On a cache hit, it returns the cached value. On a cache miss, the application queries the database, stores the result in the cache, and returns it. On updates, a common approach is to update the database and invalidate the corresponding cache entry. It is simple and works particularly well for read-heavy workloads, but we need to handle stale data, cache stampede, cache failures, and invalidation carefully."

---

## The flow to remember

**READ:**

```
Cache HIT
   ↓
Return


Cache MISS
   ↓
Database
   ↓
Populate Cache
   ↓
Return
```

**WRITE:**

```
Database
   ↓
Invalidate Cache
```

