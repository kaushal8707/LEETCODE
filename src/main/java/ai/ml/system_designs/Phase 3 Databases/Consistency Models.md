# Consistency Models

Consistency Model defines how and when a read operation sees the result of a write operation in a distributed system.

When you have multiple servers or database replicas, the same data may exist in multiple places:

```
                    ┌─────────────┐
                    │   Client    │
                    └──────┬──────┘
                           │
                    ┌──────▼──────┐
                    │Load Balancer│
                    └──────┬──────┘
                           │
              ┌────────────┴────────────┐
              ▼                         ▼
        ┌──────────┐              ┌──────────┐
        │ DB Node A│              │ DB Node B│
        │ Balance  │              │ Balance  │
        │  ₹10,000 │              │  ₹10,000 │
        └──────────┘              └──────────┘
```

The question is:

**If we update Node A, when should a read from Node B see that update?**

This is where consistency models become important.

The two fundamental models to understand first are:

- **Strong Consistency**
- **Eventual Consistency**

---

## 1. Strong Consistency

With strong consistency, once a write is successfully completed, every subsequent read sees the latest value.

In simple terms:

    Write first → immediately read → get the latest value.

### Example

Suppose we have:

    User Wallet Balance = ₹10,000

Two database replicas:

    DB A → ₹10,000
    DB B → ₹10,000

User withdraws ₹2,000.

The system updates:

    DB A → ₹8,000
    DB B → ₹8,000

Now if another request reads the balance:

```
Read Balance
     │
     ▼
   ₹8,000
```

It should **not** return:

    ₹10,000

after the successful write has been acknowledged.

### How Strong Consistency Works

In a distributed database, achieving strong consistency often requires coordination between replicas.

For example:

```
                 Write ₹8,000
                      │
                      ▼
                ┌──────────┐
                │ Leader   │
                │   DB     │
                └────┬─────┘
                     │
              Replicate data
                ┌────┴────┐
                ▼         ▼
             Replica A  Replica B
                │         │
                ▼         ▼
              ₹8,000     ₹8,000
```

The system may wait for the required replicas to acknowledge the write before confirming success.

This provides stronger correctness guarantees but can introduce:

- Higher latency
- More network communication
- Reduced availability during failures
- More coordination between nodes

### Real-Time Example: Banking

Suppose your bank account contains:

    ₹50,000

You withdraw:

    ₹10,000

The new balance should be:

    ₹40,000

Imagine the write succeeds, but another server still returns:

    ₹50,000

That could cause serious problems.

For financial transactions, inventory reservations, and other correctness-sensitive operations, strong consistency can be very important.

### Strong Consistency Timeline

```
Time ───────────────────────────────>

Write ₹8,000
     │
     ▼
┌──────────────┐
│ Write Success│
└──────┬───────┘
       │
       ▼
    Read
       │
       ▼
   ₹8,000
```

The read sees the latest committed value.

---

## 2. Eventual Consistency

With eventual consistency, replicas are allowed to temporarily have different values.

The guarantee is:

**If no new updates occur, all replicas will eventually converge to the same value.**

### Example

Initially:

    DB A → ₹10,000
    DB B → ₹10,000

A write occurs:

    DB A → ₹8,000

But replication to DB B takes some time.

For a short period:

    DB A → ₹8,000
    DB B → ₹10,000

A read from DB A returns:

    ₹8,000

A read from DB B could temporarily return:

    ₹10,000

Later, replication completes:

    DB A → ₹8,000
    DB B → ₹8,000

The replicas have eventually become consistent.

### Eventual Consistency Timeline

```
Time ───────────────────────────────────────>

        WRITE
          │
          ▼
DB A → ₹8,000
DB B → ₹10,000
          │
          │ Replication
          ▼
DB A → ₹8,000
DB B → ₹8,000
```

During the replication period:

    DB A ≠ DB B

After synchronization:

    DB A = DB B

### Real-Time Example: Social Media Likes

Imagine a post has:

    Likes = 100

There are multiple servers:

    Server A
    Server B
    Server C

Someone clicks Like.

Server A immediately updates:

    Likes = 101

But replication may take a short amount of time.

For a brief period:

    Server A → 101
    Server B → 100
    Server C → 100

A user hitting Server B might see:

    100 likes

while another user hitting Server A sees:

    101 likes

After replication:

    Server A → 101
    Server B → 101
    Server C → 101

For a social-media feed, this temporary difference is often acceptable.

### Why Use Eventual Consistency?

The major advantage is **availability** and **scalability**.

Instead of waiting for every replica to synchronize:

```
          WRITE
            │
            ▼
         Server A
            │
            ├──────► Replica B
            │
            └──────► Replica C
```

the system can acknowledge the request quickly and synchronize replicas asynchronously.

This can provide:

- Lower write latency
- Higher availability
- Better scalability
- Better tolerance of network problems

The trade-off is that users can temporarily see stale data.

---

## Strong vs Eventual Consistency

| Feature | Strong Consistency | Eventual Consistency |
|---|---|---|
| Latest data | Immediately visible | May be delayed |
| Stale reads | Generally not allowed after successful write | Possible |
| Replication | Often synchronous/coordination-based | Often asynchronous |
| Read latency | Can be higher | Usually lower |
| Write latency | Can be higher | Usually lower |
| Availability | Can decrease during failures | Usually higher |
| Scalability | More coordination required | Easier to scale |
| Complexity | Higher coordination | Conflict/convergence handling |
| Suitable for | Banking, inventory | Social feeds, likes, analytics |

---

## CAP Theorem Connection

This connects directly to the CAP Theorem you just studied.

During a network partition:

```
                 Network Partition
                        │
              ┌─────────┴─────────┐
              ▼                   ▼
             CP                  AP
              │                   │
              ▼                   ▼
       Prefer consistency    Prefer availability
```

### CP approach

The system may reject/delay a request rather than return potentially stale or conflicting data.

- Consistency ✅
- Availability ❌
- Partition Tolerance ✅

### AP approach

The system continues serving requests even if replicas temporarily disagree.

- Consistency ❌ temporarily
- Availability ✅
- Partition Tolerance ✅

> **Important:** Eventual consistency is commonly associated with AP systems, but CAP and consistency models are not the same thing. CAP describes behavior during partitions; eventual consistency describes how replicas converge over time.

---

## Strong Consistency vs Eventual Consistency Example

Let's use an e-commerce inventory example.

Suppose:

    iPhone stock = 1

Two users try to buy it at almost the same time.

### Strong consistency

The system coordinates the update:

```
User A → Buy iPhone
           ↓
       Stock = 0
           ↓
User B → Buy iPhone
           ↓
       ❌ Out of stock
```

This protects inventory correctness.

### Eventual consistency

Imagine two replicas temporarily have:

    Replica A → Stock = 1
    Replica B → Stock = 1

User A reaches Replica A:

    Buy → SUCCESS
    Stock → 0

User B reaches Replica B before synchronization:

    Buy → SUCCESS
    Stock → 0

Now the system potentially oversold the product.

This is why not every use case can blindly use eventual consistency.

---

## When Should You Use Strong Consistency?

Use strong consistency when **correctness is more important than availability/latency**.

Examples:

- Bank account balance
- Payment processing
- Inventory reservation
- Seat booking
- Distributed locks
- Financial transactions
- Unique username allocation

For example:

```
Movie Seat A10
     │
     ▼
User A books A10
     │
     ▼
A10 = BOOKED
```

Another user should not be able to read stale data saying:

    A10 = AVAILABLE

and successfully book the same seat.

---

## When Should You Use Eventual Consistency?

Use eventual consistency when **temporary stale data is acceptable**.

Examples:

- Social media likes
- View counts
- Follower counts
- Product recommendations
- Search indexes
- Analytics dashboards
- News feeds
- Caching

For example:

    YouTube Video
    Views = 1,000,000

If one user sees:

    1,000,001

and another temporarily sees:

    1,000,000

it's usually not a serious business problem.

---

## A Very Important System Design Concept

Don't think:

    "Strong consistency is always better."

Instead ask:

    "What consistency level does the business requirement need?"

For example:

| Requirement | Preferred consistency |
|---|---|
| Bank transfer | Strong |
| Payment | Strong |
| Seat booking | Strong |
| Inventory reservation | Strong |
| Instagram-like count | Eventual |
| YouTube view count | Eventual |
| Search results | Eventual |
| Recommendation system | Eventual |

---

## Interview Answer

**If the interviewer asks:**

### "What is Strong Consistency?"

You can say:

> Strong consistency guarantees that after a successful write, subsequent reads return the latest committed value. All clients effectively see the same latest state. It provides correctness but may require coordination between replicas, increasing latency and potentially reducing availability during failures.

### "What is Eventual Consistency?"

You can say:

> Eventual consistency allows replicas to temporarily have different values. If no new updates occur, the replicas will eventually converge to the same value. It improves availability, scalability, and latency but allows temporary stale reads.

---

## The Big Picture

You can visualize the concepts you've learned so far like this:

```
                 DISTRIBUTED SYSTEM
                        │
                        ▼
                  CAP THEOREM
                        │
            ┌───────────┴───────────┐
            │                       │
            ▼                       ▼
           CP                      AP
            │                       │
            │                       │
     Prefer Consistency       Prefer Availability
            │                       │
            ▼                       ▼
      Stronger guarantees     Eventual consistency
```

---

## Remember These Three Lines

**Strong Consistency:** "Everyone sees the latest data."

**Eventual Consistency:** "You may see stale data temporarily, but replicas eventually converge."

**CAP:** "During a network partition, you must choose between consistency and availability."
