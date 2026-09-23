# Write-Through Cache

Write-Through caching is a caching pattern where the application writes data to the cache, and the cache **synchronously** writes that data to the database.

The key idea is:

> **Every write goes through the cache, and the cache updates the database before confirming success.**

This is the natural counterpart to Read-Through.

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
                 v
          ┌─────────────┐
          │  Database   │
          └─────────────┘
```

The important thing is:

```
Application → Cache → Database
```

The application does **not** directly write to the database.

---

## 2. Write Flow

Suppose we want to update:

```
User 123
Name = "John"
```

The application sends:

```
UPDATE user:123
```

to the cache layer.

The flow is:

```
Application
     |
     v
   Cache
     |
     | write
     v
 Database
     |
     | SUCCESS
     v
   Cache
     |
     v
Application
```

The cache waits for the database write to succeed before confirming the operation.

---

## 3. Example

Suppose:

```
Product 100
Price = ₹1,000
```

We want to change it to:

```
₹900
```

Application:

```java
cache.put("product:100", productWithPrice900)
```

The cache layer does:

1. Update cache
2. Update database
3. Confirm success

Conceptually:

```
Application
     |
     | ₹900
     v
   Cache
     |
     | ₹900
     v
 Database
```

After successful completion:

```
Cache    = ₹900
Database = ₹900
```

---

## 4. Why Is It Called "Write-Through"?

Because every write passes **through** the cache.

```
Write
  |
  v
Cache
  |
  v
Database
```

The cache sits in the write path.

---

## 5. Read + Write-Through Together

A very common architecture is:

> **Read-Through + Write-Through**

```
                 Application
                      |
                      v
                ┌───────────┐
                │   Cache   │
                └─────┬─────┘
                      |
                      v
                ┌───────────┐
                │ Database  │
                └───────────┘
```

### Read

```
Application
    |
    v
 Cache
    |
    | HIT
    v
 Return
```

On miss:

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

### Write

```
Application
    |
    v
 Cache
    |
    v
 Database
```

So the application has a very simple interface:

```
cache.get()
cache.put()
```

The cache layer manages the underlying database.

---

## 6. Write-Through vs Cache-Aside

This is very important for interviews.

### Cache-Aside Write

Application manages both:

```
Application
    |
    +------> Database
    |
    +------> Cache
```

Typical flow:

1. Update Database
2. Invalidate Cache

For example:

```
DB = ₹900
Cache = ₹1,000

UPDATE DB → ₹900
DELETE Cache
```

Next read repopulates the cache.

### Write-Through

Application writes to cache:

```
Application
     |
     v
   Cache
     |
     v
 Database
```

The cache handles the database update.

### Key difference

> **Cache-Aside:** Application manages cache + database.
>
> **Write-Through:** Cache manages the database write.

---

## 7. What Happens if Database Write Fails?

This is an important property.

Suppose:

```
Application
     |
     v
   Cache
     |
     X
 Database
```

Database update fails.

A well-designed write-through implementation should **not** acknowledge the write as successful.

Conceptually:

```
Cache
  |
  v
Database
  |
  X FAILURE
  |
  v
Return failure
```

The exact rollback behavior depends on the caching system.

This is one reason distributed cache/database coordination is not trivial.

---

## 8. Consistency Advantage

One major benefit is that successful writes can keep:

```
Cache
  =
Database
```

in sync more naturally than an approach where the application independently updates both.

For example:

```
Application
    |
    v
 Cache
    |
    v
 Database
```

If the database write succeeds, the cache layer knows the operation completed.

However, write-through does **not** magically provide transactional consistency between Redis and a database. Failures between the two systems still need to be designed for carefully.

---

## 9. Performance Trade-off

Here's an important point.

With write-through, every write has to reach the database synchronously.

So:

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

The write cannot simply finish after updating memory.

Therefore, write-through can have **higher write latency** than writing only to the cache.

But you get stronger persistence behavior.

---

## 10. Write-Through vs Write-Behind

These two are frequently confused.

### Write-Through

```
Application
     |
     v
   Cache
     |
     v
 Database
     |
     v
Response
```

Database is updated **synchronously**.

### Write-Behind

```
Application
     |
     v
   Cache
     |
     v
Response immediately

       ...
       |
       v
   Database
```

Database update happens **asynchronously** later.

So:

> **Write-Through** = synchronous database write
>
> **Write-Behind** = asynchronous database write

---

## 11. Example: Shopping Cart

Suppose a user adds an item:

```
Product = iPhone
Quantity = 2
```

### Write-Through

```
User
 |
 v
Application
 |
 v
Cache
 |
 v
Database
 |
 v
Success
```

After success:

```
Cache:
Cart 123 = iPhone × 2

Database:
Cart 123 = iPhone × 2
```

If the application crashes immediately afterward, the database already contains the change.

---

## 12. Advantages

### ✅ Better cache/database consistency

Successful writes go through the cache and database together from the application's perspective.

### ✅ Simple application logic

Application doesn't need:

```
update DB
invalidate cache
```

It simply writes through the cache abstraction.

### ✅ Cache is automatically populated

After a successful write:

```
Cache = new value
```

So the next read is a cache hit.

### ✅ Good for read-heavy systems with important writes

Particularly useful when you want:

```
Fast reads
+
Controlled synchronous writes
```

---

## 13. Disadvantages

### ❌ Write latency

Every write eventually requires a database operation.

### ❌ Cache failure can affect writes

If the cache is the required entry point:

```
Application
     |
     X
   Cache
```

you may not be able to write.

### ❌ More complex infrastructure

The cache layer needs database integration.

### ❌ Doesn't eliminate distributed consistency problems

You still have two systems:

```
Cache
Database
```

and failures can happen between them.

---

## 14. Cache-Aside vs Read-Through vs Write-Through

Keep this mental model:

| Pattern | Read miss handled by | Write handled by |
|---|---|---|
| Cache-Aside | Application | Application |
| Read-Through | Cache | Usually application/another strategy |
| Write-Through | Cache or application depending on read strategy | Cache → DB synchronously |
| Write-Behind | Cache | Cache → DB asynchronously |

The easiest way to remember:

```
Cache-Aside
----------------
App knows DB
App manages cache


Read-Through
----------------
Cache knows how to READ DB


Write-Through
----------------
Cache knows how to WRITE DB


Write-Behind
----------------
Cache writes DB LATER
```

---

## 15. Interview Answer

If the interviewer asks:

> *"What is Write-Through caching?"*

You can answer:

> "Write-Through is a caching strategy where every write is sent to the cache, and the cache synchronously writes the data to the underlying database before the operation is considered successful. This keeps the cache populated with the latest value and simplifies application logic. The trade-off is additional write latency and the need to handle failures between the cache and database."

---

## The four patterns together

```
                    CACHING PATTERNS

        ┌─────────────────────────────────┐
        │                                 │
        │        Cache-Aside              │
        │        App manages DB           │
        │                                 │
        ├─────────────────────────────────┤
        │                                 │
        │        Read-Through             │
        │        Cache reads DB           │
        │                                 │
        ├─────────────────────────────────┤
        │                                 │
        │        Write-Through            │
        │        Cache writes DB NOW      │
        │                                 │
        ├─────────────────────────────────┤
        │                                 │
        │        Write-Behind             │
        │        Cache writes DB LATER    │
        │                                 │
        └─────────────────────────────────┘
```

---

> **For system design, the next concept is Write-Behind (Write-Back) caching**, where the cache acknowledges the write before the database is updated. That introduces interesting problems around data loss, durability, retries, ordering, and batching.

