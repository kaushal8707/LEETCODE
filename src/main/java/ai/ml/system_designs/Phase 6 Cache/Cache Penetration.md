# Cache Penetration

**Cache Penetration** happens when requests repeatedly ask for data that **does not exist**, causing every request to miss the cache and reach the database.

The key idea is:

> **Cache penetration = requests for non-existent data bypass the cache and repeatedly hit the database.**

This is different from cache stampede, where many requests try to reload the same **valid** but expired data.

---

## 1. Simple Example

Suppose your application has:

```
GET /users/123
```

User 123 exists.

```
Request
   ↓
Redis
   ↓
HIT
   ↓
User 123
```

Good.

Now someone requests:

```
GET /users/999999999
```

but that user doesn't exist.

Flow:

```
Request
   ↓
Redis
   ↓
MISS
   ↓
Database
   ↓
NOT FOUND
```

If the same request happens repeatedly:

```
GET /users/999999999
        ↓
     Redis MISS
        ↓
        DB
        ↓
    NOT FOUND

GET /users/999999999
        ↓
     Redis MISS
        ↓
        DB
        ↓
    NOT FOUND

GET /users/999999999
        ↓
     Redis MISS
        ↓
        DB
        ↓
    NOT FOUND
```

The database keeps receiving useless queries.

---

## 2. Why Is This Dangerous?

Imagine:

**Normal traffic:**

```
10,000 requests/sec
       ↓
     Redis
       ↓
   99% HIT
       ↓
      DB
  ~100 queries/sec
```

Now an attacker sends random IDs:

```
/user/abc123
/user/xyz789
/user/999888
/user/777555
/user/111222
...
```

These users don't exist.

Every request becomes:

```
Cache MISS
     ↓
Database
     ↓
NOT FOUND
```

Now:

```
10,000 requests/sec
        ↓
     Redis MISS
        ↓
10,000 DB queries/sec
```

The database can become overloaded.

This can cause:

- DB CPU ↑
- DB connections ↑
- DB latency ↑
- DB connection pool exhaustion
- timeouts ↑
- errors ↑

---

## 3. Cache Penetration vs Cache Stampede

This distinction is extremely important in interviews.

### Cache Penetration

The requested data **doesn't exist**.

```
Request
   ↓
Cache MISS
   ↓
DB
   ↓
NOT FOUND
```

Repeated requests keep doing this.

### Cache Stampede

The data **does exist**, but its cache entry expires.

```
Popular data
     ↓
Cache expires
     ↓
Many requests
     ↓
Cache MISS
     ↓
DB
```

### Comparison

|  | Cache Penetration | Cache Stampede |
|---|---|---|
| Data exists? | ❌ No | ✅ Yes |
| Main cause | Invalid/non-existent keys | Expiration/missing hot key |
| Problem | Repeated DB lookups | Concurrent DB reloads |
| Typical solution | Negative caching, Bloom filter | Locking, request coalescing |
| Example | `/user/999999` | Popular product cache expires |

---

## 4. Solution #1 — Negative Caching

One of the simplest solutions is to cache the fact that the data **doesn't exist**.

Suppose:

```
user:999999
```

doesn't exist.

**First request:**

```
Request
   ↓
Redis MISS
   ↓
DB
   ↓
NOT FOUND
   ↓
Redis
   ↓
"user does not exist"
```

For example:

```
user:999999 → NULL
TTL = 60 seconds
```

Now another request arrives:

```
Request
   ↓
Redis
   ↓
HIT → NULL
   ↓
Return 404
```

The database isn't queried.

---

## 5. Why Should Negative Cache Have a TTL?

You generally shouldn't cache "not found" forever.

Imagine:

```
user:999999 → NOT FOUND
```

Then five minutes later the user gets created:

```
DB:
user:999999 EXISTS
```

But if the negative cache remains forever:

```
Redis:
user:999999 → NOT FOUND
```

the application continues returning 404.

Therefore:

```
Negative cache
       ↓
Short TTL
```

For example:

```
60 seconds
```

or another value appropriate for the application's consistency requirements.

---

## 6. Solution #2 — Bloom Filter

For very large systems, a **Bloom filter** can be used to determine whether a key definitely does not exist.

Conceptually:

```
Request
   ↓
Bloom Filter
   ↓
┌───────────────┐
│               │
Definitely      Maybe
not present     present
│               │
↓               ↓
Reject          Redis
request           ↓
              Database
```

The important property:

> **A Bloom filter can tell you that an item is definitely not present, but a positive result means it may be present.**

---

## 7. Example of Bloom Filter

Suppose your database contains:

```
Users:

100
200
300
400
500
```

Bloom filter knows approximately:

```
100 → possibly present
200 → possibly present
300 → possibly present
400 → possibly present
500 → possibly present
```

Request:

```
GET /users/999999
```

Bloom filter:

```
999999
   ↓
Definitely not present
```

So we can immediately reject:

```
404 Not Found
```

without querying Redis or the database.

---

## 8. Bloom Filter False Positives

Bloom filters have an important property.

They can produce:

> **False positive**

Example:

```
Bloom Filter says:
"Maybe user 999 exists"
```

But actually:

```
Database:
User 999 doesn't exist
```

That's okay.

We simply continue:

```
Bloom Filter
     ↓
Maybe exists
     ↓
Redis
     ↓
DB
```

But a Bloom filter should **not** incorrectly say:

```
"Definitely doesn't exist"
```

for an item that actually exists, assuming the filter is correctly maintained.

**Mental model:**

```
Bloom Filter:

NO  → Definitely NO
YES → Maybe YES
```

---

## 9. Solution #3 — Input Validation

Don't allow obviously invalid requests to reach the database.

For example:

```
GET /users/abcxyz
```

when user IDs must be numeric.

Validate:

```
abcxyz
   ↓
Invalid format
   ↓
400 Bad Request
```

instead of:

```
abcxyz
   ↓
Redis
   ↓
MISS
   ↓
DB
```

This protects your infrastructure from garbage requests.

---

## 10. Solution #4 — Rate Limiting

Attackers can generate huge numbers of random IDs.

For example:

```
/user/100001
/user/100002
/user/100003
...
```

Rate limiting can control this:

```
Client
  ↓
Rate Limiter
  ↓
100 requests/sec
  ↓
Application
```

If the client sends:

```
100,000 requests/sec
```

the rate limiter blocks or throttles the excess traffic.

This is especially useful when cache penetration is caused by malicious traffic.

---

## 11. Solution #5 — Authorization

Sometimes attackers shouldn't even be able to determine whether a resource exists.

For example:

```
GET /orders/123456
```

If the caller doesn't own that order, don't query the database unnecessarily.

Instead:

```
Authentication
      ↓
Authorization
      ↓
Allowed?
  /       \
No        Yes
↓          ↓
Reject    Cache/DB
```

This can reduce unnecessary database traffic and prevent information leakage.

---

## 12. Complete Production Architecture

A robust system might look like:

```
                  Client
                    |
                    v
              Rate Limiter
                    |
                    v
              Input Validation
                    |
                    v
              Bloom Filter
                    |
              ┌─────┴─────┐
              |           |
          Definitely      Maybe
           doesn't        exists
            exist           |
              |             v
              |           Redis
              |             |
              |        ┌────┴────┐
              |        |         |
              |       HIT       MISS
              |        |         |
              |        |         v
              |        |        DB
              |        |         |
              |        |      NOT FOUND
              |        |         |
              |        |         v
              |        |    Negative Cache
              |        |         |
              └────────┴─────────┘
                       |
                       v
                    Response
```

---

## 13. Recommended Strategy

For a normal application:

```
Input validation
       ↓
Cache
       ↓
DB
       ↓
Negative caching
```

For a very large/high-traffic system:

```
Rate limiting
       ↓
Input validation
       ↓
Bloom Filter
       ↓
Distributed Cache
       ↓
Negative Cache
       ↓
Database
```

The exact combination depends on traffic, data-change frequency, and whether false positives/temporary negative caching are acceptable.

---

## 14. Cache Penetration + Negative Cache Example

Suppose:

```
GET /products/999999
```

doesn't exist.

### First request

```
Client
  ↓
Redis MISS
  ↓
DB
  ↓
NOT FOUND
  ↓
Redis:
product:999999 → NULL
TTL = 60 sec
```

### Next 10,000 requests

```
Client
  ↓
Redis HIT
  ↓
NULL
  ↓
404
```

Database receives:

```
0 queries
```

during that negative-cache window.

That's the key benefit.

---

## 15. Important Risk with Negative Caching

Consider:

```
10:00 → Product doesn't exist
10:00 → Negative cache created
```

Then:

```
10:00:30 → Product is created
```

But negative cache still exists:

```
product:999 → NULL
```

Users may still receive:

```
404
```

until the negative cache expires.

Therefore negative caching requires a **reasonable TTL**.

You can also explicitly invalidate the negative cache when the resource is created.

---

## 16. Cache Penetration vs Cache Miss

Not every cache miss is cache penetration.

**Normal cache miss:**

```
Cache MISS
   ↓
DB
   ↓
Data EXISTS
   ↓
Populate Cache
```

That's completely normal.

**Cache penetration:**

```
Cache MISS
   ↓
DB
   ↓
Data DOES NOT EXIST
   ↓
Repeated requests
```

The repeated non-existent lookups are the problem.

---

## 17. Interview Question

### Interviewer:

> *"An attacker sends millions of requests with random user IDs. Redis doesn't contain them, so every request goes to MySQL. How would you protect the database?"*

### Strong answer:

> "This is cache penetration because the requested keys don't exist. I would first validate requests and rate-limit abusive clients. For high-scale systems, I would use a Bloom filter to reject keys that definitely don't exist before reaching the database. I would also use negative caching with a short TTL for repeated nonexistent keys. This prevents repeated invalid requests from continuously hitting the database."

---

## 18. Cache Penetration vs Stampede vs Avalanche

Keep this mental model:

```
CACHE PROBLEMS
       |
       +--------------------+
       |                    |
       v                    v
 Penetration             Stampede
       |                    |
Data doesn't exist      Data exists
       |                    |
Random invalid keys     Popular key expires
       |                    |
       v                    v
Repeated DB queries     Many DB queries
```

And:

```
Avalanche
    ↓
Many cache entries
expire/fail together
    ↓
Huge DB load
```

### Solutions

```
Penetration
   → Negative Cache
   → Bloom Filter
   → Input Validation
   → Rate Limiting

Stampede
   → Distributed Lock
   → Request Coalescing
   → Background Refresh
   → Stale-While-Revalidate
   → TTL Jitter

Avalanche
   → TTL Jitter
   → Randomized expiration
   → Cache warming
   → Multi-level cache
   → Rate limiting
```

---

### One-line interview answer

> **"Cache penetration occurs when requests repeatedly target non-existent data, causing cache misses and unnecessary database queries. We can mitigate it using negative caching, Bloom filters, input validation, and rate limiting."**

---

## Mental model

```
              NON-EXISTENT KEY
                     |
                     v
                  Cache
                     |
                   MISS
                     |
                     v
                    DB
                     |
                 NOT FOUND
                     |
          ┌──────────┴──────────┐
          |                     |
    Negative Cache          No protection
          |                     |
       Short TTL            Repeat DB
          |                     |
          v                     v
   Future request          DB overloaded
       → HIT
       → 404
```

---

> **The next closely related concept is Cache Avalanche**, which is different from both penetration and stampede.

