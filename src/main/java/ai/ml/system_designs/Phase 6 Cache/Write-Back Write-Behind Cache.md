# Write-Back / Write-Behind Cache

Write-back caching—also called **write-behind caching**—is a caching strategy where the application writes to the cache first, gets a fast response, and the cache updates the database asynchronously later.

The key idea is:

> **Write to cache now, write to database later.**

This is different from Write-Through, where the database is updated synchronously before the write succeeds.

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
                | ASYNC
                v
         ┌─────────────┐
         │  Database   │
         └─────────────┘
```

The important flow is:


```
Application
     |
     v
   Cache
     |
     +----> Return SUCCESS immediately
     |
     | later
     v
 Database
```

---

## 2. Write-Through vs Write-Back

This is one of the most important interview distinctions.

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
Success
```

The application **waits** for the database.

### Write-Back

```
Application
     |
     v
   Cache
     |
     v
Success immediately

     ...

     Cache
       |
       v
    Database
```

The application **doesn't wait** for the database.

---

## 3. Real Example

Suppose a user updates their shopping cart.

```
Cart = 10 items
```

User adds another item.

### Write-Through

```
User
 ↓
Application
 ↓
Cache
 ↓
Database
 ↓
Success
```

Suppose this takes 50 ms.

The user waits for the database write.

### Write-Back

```
User
 ↓
Application
 ↓
Cache
 ↓
Success
```

Maybe the cache operation takes only a few milliseconds.

Then:

```
Cache
  |
  | asynchronous
  v
Database
```

The user doesn't wait for the database.

---

## 4. Why Use Write-Back?

The primary reason is:

> **Very fast writes.**

Imagine:

```
10,000 writes/sec
```

The database might struggle with this workload.

With Write-Back:

```
10,000 writes
       ↓
     Cache
       ↓
Fast response
       ↓
Batch / async processing
       ↓
Database
```

The cache absorbs the write traffic.

---

## 5. Batching Is a Major Benefit

Suppose these operations happen:

```
User adds item A
User changes quantity
User adds item B
User removes item C
User changes quantity
```

Instead of immediately performing five database writes:

```
DB write
DB write
DB write
DB write
DB write
```

the cache can consolidate changes:

```
Cache
  |
  | collect changes
  v
Final state
  |
  v
Database
```

For example:

```
5 cache updates
        ↓
1 database update
```

This can significantly reduce database write load.

---

## 6. Example: Counters

Consider a page-view counter:

```
Page views = 1,000,000
```

Suppose 100,000 users generate increments.

Doing:

```sql
UPDATE page
SET views = views + 1
```

100,000 times may be unnecessarily expensive.

Instead:

```
Requests
   |
   v
Cache Counter
   |
   | +1
   | +1
   | +1
   | ...
   |
   v
Flush periodically
   |
   v
Database
```

For example:

```
Cache:
views = 1,100,000

Database:
views = 1,000,000
```

After the flush:

```
Database:
views = 1,100,000
```

This is a classic use case where temporary divergence between cache and database is acceptable.

---

## 7. The Big Advantage

Consider a database write latency of:

```
50 ms
```

and cache write latency of:

```
2 ms
```

**Write-through:**

```
Write
 ↓
Cache
 ↓
DB
 ↓
Response

≈ 50 ms+
```

**Write-back:**

```
Write
 ↓
Cache
 ↓
Response

≈ 2 ms
```

Database update happens afterward.

So:

> **Write-back optimizes for write latency and throughput.**

---

## 8. But There Is a BIG Problem ⚠️

**What happens if the cache crashes before the database is updated?**

Example:

```
Application
     |
     v
   Cache
     |
     | SUCCESS
     v
Application
```

The application tells the user:

> *"Update successful"*

But:

```
Database
    |
    X
Not updated yet
```

Then Redis crashes.

The pending data may be **lost**.

Now:

```
Cache:
₹900

Database:
₹1,000
```

The application previously told the user the update succeeded.

**This is the biggest risk of Write-Back caching.**

---

## 9. Durability Becomes Critical

Because data may exist temporarily only in the cache, you need to think carefully about:

```
Cache durability
      ↓
Persistence
      ↓
Replication
      ↓
Failure recovery
      ↓
Retry
      ↓
Database synchronization
```

For Redis-based systems, for example, persistence and replication can reduce risk, but they don't automatically turn the cache/database combination into one atomic transaction.

---

## 10. Another Problem: Ordering

Suppose the user performs:

```
1. Quantity = 2
2. Quantity = 5
3. Quantity = 3
```

You need the database eventually to end up with:

```
Quantity = 3
```

But if asynchronous operations execute out of order:

```
2 → 5
3 → 3
1 → 2
```

the database could incorrectly end up with:

```
Quantity = 2
```

Therefore, Write-Back systems often need mechanisms for:

- ordering
- version numbers
- sequence numbers
- timestamps
- idempotency
- retries

---

## 11. Retry Problem

Imagine:

```
Cache
  |
  | async write
  v
Database
  |
  X
FAILURE
```

Now what?

You need:

```
Retry
  ↓
Retry
  ↓
Retry
```

But blindly retrying can create duplicate effects.

For example:

```
Increment balance
```

If the first request actually succeeded but the response was lost:

```
DB update → SUCCESS
Response → LOST
```

Retrying an increment could result in:

```
Increment twice
```

Therefore **idempotency** becomes very important.

---

## 12. Write-Back Often Uses a Queue

A common architecture is:

```
                  Application
                       |
                       v
                    Cache
                       |
                       v
                    Queue
                       |
                       v
                  Consumers
                       |
                       v
                   Database
```

For example:

```
Redis
  |
  v
Kafka
  |
  v
Database Writer
  |
  v
Database
```

The queue provides a durable mechanism for pending writes, depending on how the system is designed.

This is especially useful when you need:

- retries
- buffering
- batching
- ordering
- asynchronous processing

---

## 13. Write-Back + Queue

A more production-oriented design might look like:

```
                       ┌──────────────┐
                       │ Application  │
                       └──────┬───────┘
                              |
                              v
                       ┌──────────────┐
                       │    Cache     │
                       └──────┬───────┘
                              |
                         Async Write
                              |
                              v
                       ┌──────────────┐
                       │    Queue     │
                       └──────┬───────┘
                              |
                              v
                       ┌──────────────┐
                       │   Consumer   │
                       └──────┬───────┘
                              |
                              v
                       ┌──────────────┐
                       │   Database   │
                       └──────────────┘
```

Now if the database is temporarily unavailable:

```
Queue
  |
  | pending messages
  v
Consumer retries later
```

The system can absorb temporary database outages.

---

## 14. When Should You Use Write-Back?

Use it when:

### Very high write volume

```
100K+ writes/sec
```

### Very low write latency is important

```
Cache → milliseconds
DB    → tens/hundreds of milliseconds
```

### Temporary inconsistency is acceptable

For example:

- Analytics
- Counters
- Metrics
- View counts
- Some user activity
- Shopping-cart state

### Writes can be batched

```
100 updates
   ↓
1 DB operation
```

---

## 15. When Should You NOT Use It?

Avoid or be extremely careful when every successful write must immediately be durable.

**Examples:**

- Bank balance
- Payment transaction
- Money transfer
- Critical financial ledger
- Order payment confirmation

For these systems, saying:

> *"Cache says successful"*

while:

> *"Database hasn't recorded it yet"*

can be dangerous.

---

## 16. Cache-Aside → Read-Through → Write-Through → Write-Back

Now you can connect the four patterns:

```
                    CACHING PATTERNS

Cache-Aside
    |
    | Application manages cache
    v

Read-Through
    |
    | Cache handles DB reads
    v

Write-Through
    |
    | Cache handles synchronous DB writes
    v

Write-Back
    |
    | Cache handles asynchronous DB writes
    v
```

### Mental model

```
┌──────────────────────────────────────────────┐
│ Cache-Aside                                 │
│                                              │
│ App → Cache                                  │
│ App → DB                                     │
└──────────────────────────────────────────────┘

┌──────────────────────────────────────────────┐
│ Read-Through                                │
│                                              │
│ App → Cache → DB                             │
│          ↑                                   │
│       Cache loads DB                         │
└──────────────────────────────────────────────┘

┌──────────────────────────────────────────────┐
│ Write-Through                               │
│                                              │
│ App → Cache → DB                             │
│          ↑                                   │
│      Synchronous                             │
└──────────────────────────────────────────────┘

┌──────────────────────────────────────────────┐
│ Write-Back                                  │
│                                              │
│ App → Cache → SUCCESS                        │
│              ↓                               │
│          DB later                            │
└──────────────────────────────────────────────┘
```

---

## 17. Interview Answer

If asked:

> *"What is Write-Back caching?"*

A strong 10-year-experience-level answer would be:

> "Write-Back, or Write-Behind, is an asynchronous caching strategy where writes are first persisted in the cache and the application can receive a response without waiting for the database. The cache or an asynchronous mechanism subsequently flushes those changes to the database, often in batches. This provides very low write latency and reduces database write load, but introduces risks around data loss, eventual consistency, ordering, retries, and durability. Therefore, it is best suited for workloads where temporary inconsistency is acceptable and should be carefully designed for failure recovery."

---

## The key comparison to remember

| Pattern | Write path | DB updated | Main benefit | Main risk |
|---|---|---|---|---|
| Cache-Aside | App → DB + Cache | Immediately | Simple | Stale cache |
| Write-Through | App → Cache → DB | Synchronously | Better consistency | Write latency |
| Write-Back | App → Cache → DB | Asynchronously | Very fast writes | Data loss / inconsistency |
| Read-Through | App → Cache → DB on miss | N/A | Simple reads | Cache dependency |

---

### The most important sentence:

> **Write-through prioritizes consistency; write-back prioritizes write performance.**

