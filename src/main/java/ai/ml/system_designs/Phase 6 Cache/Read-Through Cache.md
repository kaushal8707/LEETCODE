# Read-Through Cache

Read-Through caching is a caching pattern where the **cache itself is responsible for loading data from the database when there is a cache miss**.

The key idea is:

> **The application talks only to the cache. The cache talks to the database on a miss.**

This is the biggest difference from Cache-Aside.

---

## 1. Basic Architecture

```
                 Client
                    |
                    v
             ┌─────────────┐
             │ Application │
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

Notice:

- Application does **NOT** directly query the database.
- The cache layer handles the database lookup.

---

## 2. Cache Hit

Suppose the application requests:

```
GET user:123
```

The flow is:

```
Client
  |
  v
Application
  |
  v
Cache
  |
  | HIT
  v
User 123
  |
  v
Application
  |
  v
Client
```

The database is **not** touched.

---

## 3. Cache Miss

Now suppose:

```
user:123
```

doesn't exist in the cache.

With Read-Through:

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
  |
  v
Database
  |
  | User 123
  v
Cache
  |
  v
Application
  |
  v
Client
```

The important part is:

> **The application doesn't know how to load the data from the database.**
>
> **The cache layer does it.**

---

## 4. Example

Imagine:

```
GET /products/100
```

Application:

```java
Product product = cache.get("product:100");

return product;
```

That's essentially all the application needs to do.

Internally, the cache behaves approximately like:

```java
get("product:100") {

    product = cacheStorage.get("product:100");

    if (product != null) {
        return product;
    }

    product = database.findProduct(100);

    cacheStorage.put("product:100", product);

    return product;
}
```

So the cache becomes responsible for:

```
Cache lookup
     ↓
MISS?
     ↓
Database lookup
     ↓
Populate cache
     ↓
Return data
```

---

## 5. Cache-Aside vs Read-Through

This is one of the most important distinctions.

### Cache-Aside

```
                 Application
                 /          \
                v            v
             Cache        Database
```

The application manages both.

```
App → Cache
       ↓ MISS
App → Database
       ↓
App → Cache
```

### Read-Through

```
              Application
                   |
                   v
                Cache
                   |
                   v
              Database
```

The cache manages the database read.

```
App → Cache
       ↓ MISS
     Cache → Database
       ↓
     Cache
       ↓
      App
```

### One-line difference

> **Cache-Aside:** Application handles cache misses.
>
> **Read-Through:** Cache handles cache misses.

---

## 6. Side-by-Side Example

Suppose we want user 123.

### Cache-Aside

```
Application
     |
     v
   Cache
     |
     | MISS
     v
Application
     |
     v
 Database
     |
     v
Application
     |
     v
   Cache
```

The application explicitly performs all operations.

### Read-Through

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
   Cache
     |
     v
Application
```

The cache abstraction performs the database lookup.

---

## 7. Why Use Read-Through?

The biggest benefit is **simplifying application code**.

**Without Read-Through:**

```java
Product product = cache.get(key);

if (product == null) {
    product = database.getProduct(id);

    cache.put(key, product);
}

return product;
```

Every service may need similar logic.

**With Read-Through:**

```java
return cache.get(key);
```

The cache abstraction handles:

```
HIT
 ↓
Return


MISS
 ↓
Load from DB
 ↓
Store in cache
 ↓
Return
```

---

## 8. Good Use Cases

Read-Through works particularly well when:

### Read-heavy systems

```
1,000,000 reads
10,000 writes
```

### Frequently accessed data

For example:

- Product information
- User profiles
- Configuration
- Reference data
- Frequently viewed content

### Expensive database reads

For example:

```
Application
   ↓
Cache
   ↓
Complex DB query
   ↓
Cache result
```

Subsequent requests avoid the expensive query.

---

## 9. What About Writes?

Read-Through primarily describes the **read path**.

For writes, you typically combine it with another strategy.

For example:

```
Application
    |
    v
Database
    |
    v
Invalidate Cache
```

Or combine it with Write-Through:

```
Application
     |
     v
   Cache
     |
     v
 Database
```

This gives you:

> **Read-Through + Write-Through**

which can provide a very clean caching abstraction.

---

## 10. Advantages

### 1. Simple application code

Application doesn't need to know:

- Where is the database?
- How do I query it?
- How do I populate the cache?

It just asks:

```
cache.get(key)
```

### 2. Centralized cache logic

The cache layer can centrally implement:

- loading
- TTL
- serialization
- eviction
- retries
- metrics

### 3. Prevents duplicated caching logic

Without a centralized mechanism, multiple services may implement:

```
if cache miss:
    query DB
    populate cache
```

slightly differently.

---

## 11. Disadvantages

### 1. More infrastructure complexity

The cache needs to know how to retrieve data from the underlying data store.

### 2. Cache becomes tightly coupled to the data source

For example:

```
Redis
  ↓
Database
```

The caching layer needs knowledge of how the database is accessed.

### 3. Cache miss still causes database latency

A miss still requires:

```
Cache
  ↓
Database
  ↓
Cache
  ↓
Application
```

So the first request is slower than a cache hit.

### 4. Cache failure needs careful handling

If the application only talks to the cache:

```
Application
     |
     X
   Cache
```

what happens if the cache is unavailable?

You need a fallback strategy or the cache may become a critical dependency.

---

## 12. Read-Through vs Cache-Aside vs Write-Through

Remember this table:

| Pattern | Who reads DB on cache miss? | Who populates cache? |
|---|---|---|
| Cache-Aside | Application | Application |
| Read-Through | Cache | Cache |
| Write-Through | N/A | Cache during write |
| Write-Behind | N/A | Cache first, DB later |

The most important distinction:

**Cache-Aside:**

```
Application
   ├── Cache
   └── Database
```

versus:

**Read-Through:**

```
Application
      |
      v
    Cache
      |
      v
  Database
```

---

## 13. Real-World Analogy

Imagine a library.

### Cache-Aside

You ask the librarian:

> *"Do you have this book?"*

The librarian checks the shelf.

If it's not there, **you personally** go to the storage room, find the book, and put a copy on the shelf.

**You** manage the process.

### Read-Through

You ask the librarian:

> *"Give me this book."*

The librarian checks the shelf.

If it's not there, **the librarian** goes to the storage room, gets it, puts it on the shelf, and gives it to you.

**The librarian** manages the process.

That's essentially the difference.

---

## 14. Interview Answer

If an interviewer asks:

> *"What is Read-Through caching?"*

A strong answer is:

> "Read-Through is a caching pattern where the application reads data only through the cache. On a cache hit, the cache returns the data immediately. On a cache miss, the cache layer itself retrieves the data from the underlying database, stores it in the cache, and returns it to the application. This simplifies application code and centralizes cache-loading logic. Compared with Cache-Aside, where the application handles the cache miss and database lookup, Read-Through makes the cache responsible for that process."

---

## Remember this diagram

```
             CACHE-ASIDE

Application
   |       \
   v        v
 Cache     Database
   ^
   |
Application handles MISS
```

```
             READ-THROUGH

Application
     |
     v
   Cache
     |
     v
 Database

Cache handles MISS
```

---

> **Next concept:** Write-Through caching — this is the natural counterpart to Read-Through and is especially important for understanding how cache + database consistency works.

