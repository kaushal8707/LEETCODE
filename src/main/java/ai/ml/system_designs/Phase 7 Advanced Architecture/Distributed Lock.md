# Distributed Lock

A **Distributed Lock** is a mechanism that ensures only one instance of a distributed application can execute a particular critical operation at a time, even when multiple application servers are running.

It is mainly used when you have:

```
          Load Balancer
               |
       +-------+-------+
       |       |       |
    App-1    App-2    App-3
       |       |       |
       +-------+-------+
               |
       Distributed Lock
               |
        Shared Resource
```

For example, suppose 3 instances receive the same payment request at almost the same time.

**Without a distributed lock:**

```
App-1 ──> Process Payment
App-2 ──> Process Payment
App-3 ──> Process Payment
```

The payment could potentially be processed multiple times.

**With a distributed lock:**

```
App-1 ──> Acquire Lock ──> Process Payment
App-2 ──> Cannot acquire lock ──> Wait/Retry
App-3 ──> Cannot acquire lock ──> Wait/Retry
```

Only one instance enters the critical section.

---

## 1. Why Do We Need a Distributed Lock?

Consider an order system.

Suppose:

```
Order ID = ORD-1001
```

The order should be processed only once.

But because you have multiple application instances:

```
                  Load Balancer
                       |
          +------------+------------+
          |            |            |
        App-1        App-2        App-3
          |            |            |
          +------------+------------+
                       |
                    Database
```

Two requests can arrive simultaneously:

```
10:00:01 → App-1 → Process ORD-1001
10:00:01 → App-2 → Process ORD-1001
```

Both check:

```sql
SELECT status
FROM orders
WHERE order_id = 'ORD-1001';
```

Both may see:

```
status = NEW
```

Then both process the order.

This is a **race condition**.

A distributed lock can coordinate the application instances.

---

## 2. What Does "Distributed" Mean?

A normal Java lock:

```java
synchronized
```

or:

```java
ReentrantLock
```

works only inside one JVM.

For example:

```
JVM 1
 ├── Thread-1
 ├── Thread-2
 └── synchronized lock
```

But in a distributed system:

```
JVM 1              JVM 2              JVM 3
  |                  |                  |
  |                  |                  |
 App-1              App-2              App-3
```

Each JVM has its own memory.

Therefore:

```java
synchronized
```

cannot coordinate App-1 and App-2.

We need a shared coordination mechanism.

For example:

```
App-1 ──┐
App-2 ──┼──> Redis / Database / ZooKeeper
App-3 ──┘
```

That shared system maintains the distributed lock.

---

## 3. Basic Distributed Lock Flow

Suppose we use Redis.

```
              Redis
          +-------------+
          | Lock:       |
          | order:1001  |
          | owner=App-1 |
          +-------------+
             ↑       ↑
             |       |
           App-1    App-2
```

App-1 tries to acquire:

```
lock:order:1001
```

Redis says:

```
SUCCESS
```

App-1 owns the lock.

App-2 tries:

```
lock:order:1001
```

Redis says:

```
FAILED
```

because App-1 already owns it.

---

## 4. How Redis Distributed Lock Works

A simplified Redis implementation uses:

```
SET lock-key unique-value NX EX 30
```

Meaning:

**SET** — Create a key.

**NX** — Create it only if it doesn't already exist.

**EX 30** — Automatically expire it after 30 seconds.

Conceptually:

```
SET lock:order:1001
    owner=abc123
    NX
    EX 30
```

If the key doesn't exist:

```
Redis → OK
```

If it already exists:

```
Redis → nil
```

---

## 5. Why NX Is Important

Imagine:

```
App-1 → Acquire lock
App-2 → Acquire lock
```

If Redis simply performed:

```
SET lock:order:1001 App-1
```

then App-2 could overwrite it:

```
SET lock:order:1001 App-2
```

Now App-2 owns the lock even though App-1 may still be processing.

That's dangerous.

`NX` makes acquisition atomic:

```
SET key value NX
```

Only the first application succeeds.

```
App-1 ──> SET lock NX ──> SUCCESS

App-2 ──> SET lock NX ──> FAILURE

App-3 ──> SET lock NX ──> FAILURE
```

---

## 6. Why Do We Need a Unique Lock Value?

This is extremely important.

Suppose:

```
App-1
```

gets the lock:

```
lock:order:1001 = UUID-111
TTL = 30 seconds
```

App-1 starts processing.

But App-1 becomes slow.

After 30 seconds:

```
Lock expires
```

Then:

```
App-2 → acquires lock
```

Now:

```
App-1 → still running
App-2 → owns lock
```

If App-1 blindly executes:

```
DEL lock:order:1001
```

it could delete App-2's lock.

That's a serious bug.

Therefore, the lock contains an **owner token**:

```
lock:order:1001 = UUID-111
```

App-1 can release the lock only if the current value is still:

```
UUID-111
```

Conceptually:

```
if Redis.get(lockKey) == myToken:
    Redis.delete(lockKey)
```

But this check-and-delete must itself be atomic.

With Redis, this is commonly implemented with a **Lua script**.

---

## 7. Complete Redis Lock Lifecycle

The lifecycle looks like this:

```
             Acquire
                |
                ↓
        +---------------+
        | Lock available|
        +---------------+
                |
                ↓
             SUCCESS
                |
                ↓
        Critical Section
                |
                ↓
             Release
                |
                ↓
          Lock deleted
```

If acquisition fails:

```
Acquire
   |
   +----> FAIL
           |
        Wait/Retry
           |
           ↓
        Acquire
```

---

## 8. Real-Time Example: Prevent Duplicate Payment

Imagine:

```
Payment ID = PAY-1001
```

Two requests arrive:

```
App-1 ──> PAY-1001
App-2 ──> PAY-1001
```

Both attempt:

```
lock:payment:PAY-1001
```

### App-1

```
SET lock:payment:PAY-1001 UUID-A NX EX 30
```

Result:

```
SUCCESS
```

### App-2

```
SET lock:payment:PAY-1001 UUID-B NX EX 30
```

Result:

```
FAIL
```

Now:

```
App-1
  |
  ├── Lock acquired
  |
  ├── Check payment
  |
  ├── Process payment
  |
  └── Release lock
```

App-2 can retry after a small delay.

---

## 9. Lock Acquisition Should Have a Timeout

You generally shouldn't wait forever.

For example:

```
Try acquiring lock
       |
       ↓
   Success?
    /    \
  Yes     No
   |       |
Process   Retry
           |
       100 ms
           |
       Try again
```

You might configure:

```
Lock wait timeout = 5 seconds
```

If the lock isn't acquired within 5 seconds:

```
Return failure
```

This prevents requests from waiting indefinitely.

---

## 10. Lock TTL Is Extremely Important

Consider:

```
App-1 acquires lock
```

Then App-1 crashes:

```
App-1 💥
```

Without expiration:

```
lock remains forever
```

Nobody can acquire it.

This is called a **stale lock**.

Therefore:

```
Lock
  |
  +── TTL = 30 seconds
```

After 30 seconds:

```
Redis automatically deletes lock
```

Another instance can acquire it.

---

## 11. But TTL Creates Another Problem

Suppose:

```
TTL = 30 seconds
```

but processing takes:

```
60 seconds
```

Timeline:

```
0 sec
App-1 acquires lock
    |
    |
30 sec
Lock expires
    |
    ↓
App-2 acquires lock
    |
    |
60 sec
App-1 finishes
```

Now:

```
App-1 → processing
App-2 → processing
```

The distributed lock no longer provides mutual exclusion for the entire operation.

This is one of the most important problems to understand.

---

## 12. Lock Renewal / Watchdog

One solution is to periodically extend the TTL while the owner is still alive.

```
App-1
  |
  ├── Acquire lock
  |      TTL = 30 sec
  |
  ├── Process
  |
  ├── Renew TTL
  |      TTL = 30 sec
  |
  ├── Process
  |
  ├── Renew TTL
  |
  └── Finish
         |
         ↓
      Release
```

This is often called a **watchdog** or **lease renewal** mechanism.

But it introduces additional failure scenarios, so you shouldn't treat a Redis lock as magically guaranteeing correctness in every distributed failure scenario.

---

## 13. Distributed Lock vs Database Transaction

This is an important system-design distinction.

Suppose you're protecting a database update.

Sometimes you don't need a distributed lock at all.

For example:

```sql
UPDATE account
SET balance = balance - 100
WHERE account_id = 101
AND balance >= 100;
```

The database can provide atomicity and concurrency control.

You may use:

```
Database transactions
Row-level locks
Optimistic locking
Unique constraints
Atomic conditional updates
```

instead of introducing Redis.

### Rule of thumb

> If the critical section is fundamentally a database operation, first ask whether the database itself can enforce the invariant.

A distributed lock is most useful when coordinating work across multiple processes/services/resources.

---

## 14. Distributed Lock Implementations

Common approaches include:

### Redis

```
Application
     |
     ↓
   Redis
```

Popular because it's fast and supports atomic operations and expiration.

### Database

You can use:

```sql
SELECT ... FOR UPDATE
```

or an application-level lock table.

Example:

```
distributed_lock
------------------------
resource_id | owner
------------------------
order-1001  | app-1
```

The database provides the concurrency guarantees.

### ZooKeeper

ZooKeeper can implement distributed locks using ephemeral/sequential znodes.

Conceptually:

```
             ZooKeeper
                 |
       +---------+---------+
       |         |         |
     App-1     App-2     App-3
       |
   smallest
   sequence
       |
    Lock owner
```

ZooKeeper is designed specifically for distributed coordination.

### etcd

etcd provides distributed coordination primitives and is commonly used in cloud-native systems.

---

## 15. Distributed Lock Architecture

A typical Redis-based implementation:

```
                     Client
                       |
                       ↓
                 Load Balancer
                       |
             +---------+---------+
             |         |         |
           App-1     App-2     App-3
             |         |         |
             +---------+---------+
                       |
                       ↓
                    Redis
                       |
                +------+------+
                |             |
          Lock Metadata    Other Data
                |
        lock:order:1001
        owner=UUID
        TTL=30 sec
```

---

## 16. Distributed Lock Properties

A good distributed lock should provide several properties.

### Mutual Exclusion

At most one owner should hold the lock at a time.

```
App-1 → LOCK ✅
App-2 → LOCK ❌
App-3 → LOCK ❌
```

### Deadlock Avoidance

A crashed application shouldn't leave the lock forever.

Usually:

```
TTL / lease
```

helps.

### Ownership

The lock should identify its owner.

```
lock = UUID-123
```

### Safe Release

Only the owner should be able to release its lock.

### Availability

The locking system itself must be highly available.

---

## 17. Distributed Lock Failure Scenario

Consider:

```
App-1
  |
  | acquire lock
  ↓
Redis
```

App-1 gets the lock.

Then network failure occurs:

```
App-1 ───X─── Redis
```

App-1 doesn't know whether:

```
1. Lock still exists
2. Lock expired
3. Redis processed the request but response was lost
```

This is where distributed systems become difficult.

A distributed lock is **not** simply a `synchronized` statement shared across machines.

Network partitions, pauses, crashes, clock behavior, lock expiration, retries, and stale owners all matter.

---

## 18. Distributed Lock + Fencing Tokens

For high-value operations, a powerful technique is **fencing tokens**.

Suppose:

```
App-1 → token 41
```

Later its lease expires:

```
App-2 → token 42
```

Now App-1 unexpectedly continues processing.

The downstream resource can reject App-1's old token:

```
App-1 → token 41 → REJECT
App-2 → token 42 → ACCEPT
```

Conceptually:

```
               Lock Service
                    |
        +-----------+-----------+
        |                       |
     App-1                   App-2
     token=41                token=42
        |                       |
        +-----------+-----------+
                    |
                    ↓
             Protected Resource
                    |
          accepts highest/current
             fencing token
```

This protects against stale lock holders.

For critical distributed systems, fencing tokens can be more important than simply having a lock with a TTL.

---

## 19. Distributed Lock vs Leader Election

These concepts are related but different.

### Distributed Lock

> "Who is allowed to perform this particular operation?"

Example:

```
Process payment PAY-1001
```

### Leader Election

> "Which instance is currently the leader responsible for a role?"

Example:

```
App-1 → Leader
App-2 → Follower
App-3 → Follower
```

The leader might process all scheduled jobs.

---

## 20. Distributed Lock vs Idempotency

Another important interview distinction.

A distributed lock tries to prevent concurrent execution:

```
App-1 → processing
App-2 → blocked
```

Idempotency ensures repeated execution produces the same logical result:

```
Request
Request
Request
   ↓
Same final result
```

For payments, idempotency is often essential even if you use a distributed lock, because locks don't eliminate every failure mode.

A robust payment architecture might use:

```
Idempotency Key
       +
Database constraints/transaction
       +
Distributed coordination where necessary
       +
Retry handling
```

---

## 21. Redis Lock vs synchronized

| Feature | synchronized | Distributed Lock |
|---|---|---|
| Scope | Single JVM | Multiple JVMs |
| Shared memory | Yes | No |
| Multiple servers | ❌ | ✅ |
| Automatic expiration | ❌ | Usually ✅ |
| Network failures | Not applicable | Must handle |
| Performance | Very fast | Network dependent |
| Use case | Threads | Distributed services |

---

## 22. Interview Example

### Question:

> You have 10 instances of an Order Service. A scheduled job must run only once every minute. How would you solve it?

**Without a distributed lock:**

```
App-1 → Job
App-2 → Job
App-3 → Job
...
App-10 → Job
```

The job executes 10 times.

**With distributed lock:**

```
App-1 ──┐
App-2 ──┤
App-3 ──┤
   ...  ├──> Distributed Lock
App-10 ─┘
              |
              ↓
           App-4 wins
              |
              ↓
          Execute Job
```

The lock key could be:

```
scheduled-job:order-processing
```

with a short lease/TTL appropriate to the job.

---

## 23. The Most Important Mental Model

Think of a distributed lock as a **lease on a resource**:

```
                Resource
                   |
             "Who owns me?"
                   |
                   ↓
             Lock Service
                   |
          +--------+--------+
          |                 |
        App-1             App-2
          |
       Acquires
          |
          ↓
       OWNER
```

The key concepts are:

```
Acquire
   ↓
Owner identity
   ↓
Lease / TTL
   ↓
Critical section
   ↓
Renew if necessary
   ↓
Safe release
```

And for stronger protection:

```
Lease
  +
Fencing Token
  +
Idempotency
  +
Database constraints/transactions
```

---

## 24. Interview Answer — 30 Seconds

> A distributed lock is a coordination mechanism that ensures only one instance of a distributed application can access a shared resource or execute a critical operation at a time. Unlike `synchronized`, which works only within a single JVM, a distributed lock works across multiple application instances using a shared system such as Redis, ZooKeeper, etcd, or a database. A typical Redis implementation uses an atomic `SET NX` operation with a unique owner token and TTL. The owner performs the critical operation and safely releases the lock. TTL prevents stale locks, while lease renewal and fencing tokens can protect against long-running operations and stale lock holders.

---

## The flow to remember

```
             Multiple App Instances
              /       |       \
             /        |        \
          App-1     App-2     App-3
             \        |        /
              \       |       /
               Distributed Lock
                      |
               Acquire atomically
                      |
              +-------+-------+
              |               |
           Success          Failure
              |               |
              ↓             Retry
        Critical Section
              |
          Renew lease
          if required
              |
           Release
              |
              ↓
          Next owner
```

