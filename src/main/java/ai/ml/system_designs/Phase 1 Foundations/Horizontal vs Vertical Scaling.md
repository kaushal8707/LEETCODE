# Horizontal vs Vertical Scaling

In system design, scaling means increasing a system's capacity so it can handle more:

- Users
- Requests
- Data
- Traffic
- CPU work
- Memory usage

There are two fundamental approaches:

- **Vertical Scaling** = Make one machine bigger
- **Horizontal Scaling** = Add more machines

---

## 1. Vertical Scaling

Vertical scaling is also called **scaling up**.

You increase the resources of an existing server.

For example:

**Before**

```
┌──────────────────────┐
│      Server          │
│                      │
│  CPU: 4 cores        │
│  RAM: 16 GB          │
│  Disk: 500 GB        │
└──────────────────────┘
```

Upgrade it:

**After**

```
┌──────────────────────────────┐
│          Server              │
│                              │
│  CPU: 32 cores               │
│  RAM: 128 GB                 │
│  Disk: 2 TB                  │
└──────────────────────────────┘
```

You haven't added another server.

You made the existing server **more powerful**.

---

## 2. Real-World Example

Imagine you have an e-commerce application:

```
Users
  |
  ↓
┌───────────────┐
│ Application   │
│ Server        │
│ 4 CPU         │
│ 16 GB RAM     │
└───────────────┘
```

Initially it handles:

```
5,000 requests/sec
```

Traffic increases to:

```
10,000 requests/sec
```

You might upgrade:

```
4 CPU  → 16 CPU
16 GB  → 64 GB RAM
```

Now the same server might handle the increased load.

That's **vertical scaling**.

---

## 3. Advantages of Vertical Scaling

### Simpler architecture

You still have one server:

```
Client
  |
  ↓
Server
```

You don't necessarily need:

- Load balancer
- Service discovery
- Distributed coordination
- Data partitioning

### Easier application design

Suppose you have:

```
Application
     |
     ↓
Database
```

You can often continue using the same architecture.

### Useful for databases

Vertical scaling is particularly common for databases.

For example:

```
Database Server

8 CPU
32 GB RAM
       ↓
32 CPU
128 GB RAM
```

Many databases can benefit significantly from having more CPU and memory.

---

## 4. Disadvantages of Vertical Scaling

The biggest problem:

> **There is a physical/resource limit.**

You can't infinitely increase the size of a machine.

For example:

```
8 CPU
   ↓
16 CPU
   ↓
32 CPU
   ↓
64 CPU
   ↓
128 CPU
   ↓
?
```

Eventually you reach the largest available machine.

### Single Point of Failure

This is another major issue.

Suppose:

```
             Users
               |
               ↓
        ┌─────────────┐
        │   Server    │
        └─────────────┘
```

If that server crashes:

```
Users
  |
  X
  |
Server ❌
```

Your entire application can become unavailable.

---

## 5. Horizontal Scaling

Horizontal scaling is also called **scaling out**.

Instead of making one server bigger, you **add more servers**.

**Before:**

```
             Users
               |
               ↓
        ┌─────────────┐
        │   Server    │
        └─────────────┘
```

**After:**

```
             Users
               |
               ↓
        ┌─────────────┐
        │Load Balancer│
        └──────┬──────┘
          ┌────┼────┐
          ↓    ↓    ↓
       Server Server Server
         1      2      3
```

Instead of one powerful machine, you have **multiple machines**.

---

## 6. Why Load Balancer?

If you have three servers:

```
Server 1
Server 2
Server 3
```

How does the request know where to go?

Usually:

```
Client
   |
   ↓
Load Balancer
   |
   ├──→ Server 1
   ├──→ Server 2
   └──→ Server 3
```

The load balancer distributes traffic across the servers.

For example:

```
Request 1 → Server 1
Request 2 → Server 2
Request 3 → Server 3
Request 4 → Server 1
Request 5 → Server 2
```

---

## 7. Horizontal Scaling Example

Suppose one server can handle:

```
5,000 requests/sec
```

Your traffic is:

```
20,000 requests/sec
```

**With vertical scaling:**

```
1 server
   ↓
make it more powerful
   ↓
20,000 RPS
```

**With horizontal scaling:**

```
Server 1 → 5,000 RPS
Server 2 → 5,000 RPS
Server 3 → 5,000 RPS
Server 4 → 5,000 RPS

Total = 20,000 RPS
```

---

## 8. Horizontal Scaling and High Availability

This is one of the biggest benefits.

Suppose:

```
                 Load Balancer
                /      |      \
               ↓       ↓       ↓
             S1       S2       S3
```

Server 2 crashes:

```
                 Load Balancer
                /             \
               ↓               ↓
             S1               S3

             S2 ❌
```

The application can continue serving traffic through S1 and S3.

This gives you **fault tolerance** and **high availability**.

---

## 9. Horizontal Scaling + Stateless Services

Horizontal scaling works particularly well with **stateless application servers**.

For example:

```
                Load Balancer
                /     |     \
               ↓      ↓      ↓
             App1   App2   App3
```

Each application server should ideally be able to handle **any request**.

Don't store important user state only in:

```
App1's memory
```

because the next request might go to:

```
App2
```

Instead, use shared infrastructure such as:

```
             App Servers
            /     |     \
           ↓      ↓      ↓
        Redis   Database  Object Storage
```

This makes adding/removing application servers much easier.

---

## 10. Horizontal Scaling Is Not Just "Add Servers"

This is a very important system-design point.

Suppose:

```
              Load Balancer
                    |
        ┌───────────┼───────────┐
        ↓           ↓           ↓
       App1        App2        App3
        |           |           |
        └───────────┼───────────┘
                    ↓
                Database
```

You horizontally scaled the application layer.

But now the database might become the bottleneck:

```
App1 ─┐
App2 ─┼──→ Database 🔥
App3 ─┘
```

So scaling one component doesn't automatically scale the entire system.

**You need to identify the bottleneck.**

---

## 11. Database Scaling

Database scaling is more complicated.

### Vertical:

```
Database
   ↓
More CPU
More RAM
Faster disk
```

### Horizontal approaches can include:

#### Read replicas

```
             Application
                  |
             Primary DB
             /       \
            ↓         ↓
       Read Replica  Read Replica
```

Writes:

```
Application → Primary
```

Reads:

```
Application → Replica 1
Application → Replica 2
```

#### Sharding

Split data across multiple databases.

```
                    Application
                         |
              ┌──────────┼──────────┐
              ↓          ↓          ↓
            DB-1       DB-2       DB-3
          Users A-H   Users I-P   Users Q-Z
```

This is a form of horizontal scaling at the data layer.

---

## 12. Vertical vs Horizontal

| Feature | Vertical Scaling | Horizontal Scaling |
|---|---|---|
| Also called | Scale Up | Scale Out |
| Approach | Bigger machine | More machines |
| Architecture | Simpler | More complex |
| Maximum capacity | Limited by machine | Potentially much higher |
| Fault tolerance | Usually lower | Higher |
| High availability | Harder | Easier |
| Load balancer | Usually unnecessary | Usually required |
| Cost | Can become expensive | Often more flexible |
| Distributed systems | Less necessary | Usually required |
| Stateless design | Less important | Very important |
| Database | Common approach | More complex |
| Operational complexity | Lower | Higher |

---

## 13. A Very Important Example

Imagine an application that initially looks like:

```
              Users
                |
                ↓
          ┌──────────┐
          │  Server  │
          └────┬─────┘
               |
               ↓
          ┌──────────┐
          │ Database │
          └──────────┘
```

### Stage 1 — Vertical Scaling

Traffic increases.

Server:

```
4 CPU / 16 GB
       ↓
16 CPU / 64 GB
```

Database:

```
8 CPU / 32 GB
       ↓
32 CPU / 128 GB
```

Simple.

### Stage 2 — Horizontal Application Scaling

Traffic increases again.

```
                   Users
                     |
                     ↓
               Load Balancer
              /      |      \
             ↓       ↓       ↓
           App1    App2    App3
              \      |      /
               \     |     /
                    ↓
                Database
```

Now we have a scalable application layer.

### Stage 3 — Database Scaling

Database becomes the bottleneck.

We might introduce:

```
                   Users
                     |
                Load Balancer
                     |
             ┌───────┼───────┐
             ↓       ↓       ↓
            App1    App2    App3
             \       |      /
              \      |     /
                   DB
```

Then potentially:

```
                   DB
              /     |     \
             ↓      ↓      ↓
         Replica  Replica Replica
```

or eventually sharding.

**This is how real systems evolve as traffic grows.**

---

## 14. When Should You Use Which?

### Use Vertical Scaling when:

- System is relatively small
- Architecture needs to remain simple
- Database benefits from more memory/CPU
- You don't yet need massive scale
- Distributed complexity isn't justified

Example:

```
Small application
      ↓
One powerful server
```

### Use Horizontal Scaling when:

- Traffic is large
- You need high availability
- You need fault tolerance
- Traffic changes dynamically
- You need to scale beyond one machine's limits

Typical large-scale architecture:

```
                    Users
                      |
                      ↓
                Load Balancer
                      |
          ┌───────────┼───────────┐
          ↓           ↓           ↓
        App 1       App 2       App 3
          |           |           |
          └───────────┼───────────┘
                      ↓
                    Cache
                      |
                      ↓
                  Database
```

---

## 15. Auto Scaling

Horizontal scaling becomes especially powerful when combined with **auto scaling**.

For example:

**Normal traffic**

```
             LB
              |
        ┌─────┼─────┐
        ↓     ↓     ↓
       App1  App2  App3
```

**Traffic suddenly increases:**

```
              🔥 Traffic 🔥
                   |
                   ↓
                  LB
                   |
       ┌───────────┼────────────┐
       ↓           ↓            ↓
      App1        App2         App3
                                  +
                                  ↓
                                App4
                                  +
                                  ↓
                                App5
```

**When traffic drops:**

```
App1
App2
App3
App4 ❌
App5 ❌
```

Unused servers can be removed.

This is called **elasticity**.

---

## 16. Key Interview Concept

Don't say:

> "Horizontal scaling is always better."

That's incorrect.

The correct answer is:

> **The appropriate scaling strategy depends on the component, workload, cost, availability requirements, and architectural complexity.**

For example:

```
Application servers → Horizontal scaling
Database            → Often vertical + replicas/sharding
Cache               → Horizontal clustering
Kafka               → Horizontal brokers
Storage             → Distributed/horizontal
```

---

## 17. One-Line Mental Model

Remember this:

```
VERTICAL
────────

        BIGGER
          ↑
       ┌───────┐
       │SERVER │
       └───────┘


HORIZONTAL
──────────

       MORE
        ↓
   ┌───────┐ ┌───────┐ ┌───────┐
   │SERVER │ │SERVER │ │SERVER │
   └───────┘ └───────┘ └───────┘
```

And for system design interviews:

> **Vertical scaling** increases the capacity of a single node; **horizontal scaling** increases system capacity by adding nodes. Horizontal scaling generally provides better fault tolerance and elasticity, but introduces distributed-system complexity.
