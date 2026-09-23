# Bandwidth in System Design

Bandwidth is the maximum amount of data that can be transmitted over a network connection in a given amount of time.

Think of it as the width of a highway:

- **Wider highway** → more cars can travel at the same time.
- **Higher bandwidth** → more data can be transferred per second.

---

## 1. Simple Definition

**Bandwidth = Maximum data-carrying capacity of a network connection**

It is usually measured in:

| Unit   | Meaning             |
|--------|---------------------|
| bps    | bits per second     |
| Kbps   | kilobits/second     |
| Mbps   | megabits/second     |
| Gbps   | gigabits/second     |

For example:

```
Network bandwidth = 1 Gbps
```

means the network can theoretically carry up to:

```
1,000,000,000 bits/second
```

---

## 2. Bandwidth vs Data Size

Suppose you have a:

```
100 MB file
```

and your network bandwidth is:

```
100 Mbps
```

Be careful:

```
1 Byte = 8 bits
```

Therefore:

```
100 MB = 800 Mb
```

Approximate transfer time:

```
800 Mb / 100 Mbps
= 8 seconds
```

So, theoretically, it takes around **8 seconds**.

In real systems, it will usually take longer because of **protocol overhead, congestion, latency, encryption, retransmissions**, etc.

---

## 3. Bandwidth vs Latency

This is very important in system design.

### Bandwidth

Answers:

> How much data can I transfer per second?

### Latency

Answers:

> How long does it take for a request/data packet to travel from A to B?

For example:

```
Client
   |
   |  Request
   ↓
Server
```

Suppose:

```
Latency   = 50 ms
Bandwidth = 1 Gbps
```

The network can transfer a large amount of data per second, but there is still a **50 ms delay** before communication completes.

---

## 4. Highway Analogy

Imagine two roads.

```
Road A
Width = 1 lane

Road B
Width = 10 lanes
```

Both roads have:

```
Distance = 100 km
```

- The **distance** is analogous to **latency**.
- The **number of lanes** is analogous to **bandwidth**.

So:

- **Bandwidth** → how much can travel simultaneously
- **Latency** → how long the journey takes

This distinction becomes extremely important when designing distributed systems.

---

## 5. Bandwidth vs Throughput

These two terms are often confused.

### Bandwidth

The **maximum capacity**.

```
Bandwidth = 1 Gbps
```

### Throughput

The **actual amount of data** successfully transferred.

For example:

```
Bandwidth  = 1 Gbps
Throughput = 700 Mbps
```

Your connection has a capacity of 1 Gbps, but you're actually transferring 700 Mbps.

**Why might throughput be lower?**

- Network congestion
- Packet loss
- TCP overhead
- Server limitations
- Client limitations
- Network hardware
- Protocol overhead
- Retransmissions

So:

> **Bandwidth ≠ Throughput**

---

## 6. Bandwidth in a Web Application

Consider:

```
                Internet
                   |
                   |
              Load Balancer
               /        \
              /          \
        Server 1       Server 2
```

Suppose each server sends:

```
1 MB response
```

and you have:

```
1,000 requests/sec
```

Then outgoing data is approximately:

```
1 MB × 1,000
= 1,000 MB/sec
= 1 GB/sec
```

In bits:

```
1 GB/sec × 8
= 8 Gbps
```

So your infrastructure may need approximately:

```
8 Gbps
```

of network capacity just for that traffic direction, before accounting for overhead.

This is why bandwidth becomes an important **scalability consideration**.

---

## 7. Example: Image Service

Suppose you're designing an image-sharing application.

Each image:

```
5 MB
```

Traffic:

```
10,000 images/sec
```

Required bandwidth:

```
5 MB × 10,000
= 50,000 MB/sec
= 50 GB/sec
```

Convert to bits:

```
50 × 8
= 400 Gbps
```

That's enormous.

Instead of forcing your application servers to send all those images, you might introduce a **CDN**.

```
                 ┌──────────────┐
                 │ Application  │
                 │   Servers    │
                 └──────┬───────┘
                        │
                        │ Images
                        ↓
                 ┌──────────────┐
                 │     CDN      │
                 └──────┬───────┘
                        │
             ┌──────────┼──────────┐
             ↓          ↓          ↓
           User       User       User
```

The CDN caches frequently requested images.

Now:

```
User → CDN
```

instead of:

```
User → Application Server
```

This significantly reduces bandwidth requirements on your application infrastructure.

---

## 8. Bandwidth and Caching

Caching is one of the most important techniques for reducing bandwidth consumption.

**Without cache:**

```
1 million requests
       ↓
Application Server
       ↓
Database
```

**With cache:**

```
1 million requests
       ↓
     Cache
    /     \
   ↓       ↓
90% HIT   10% MISS
           ↓
        Database
```

If the cache hit ratio is high, you reduce:

- Database traffic
- Application-server traffic
- Network traffic
- Bandwidth consumption
- Latency

---

## 9. Bandwidth and APIs

Suppose an API returns:

```json
{
  "id": 123,
  "name": "Kaushal",
  "email": "kaushal@example.com",
  "address": "...",
  "orders": [...],
  "transactions": [...],
  "recommendations": [...]
}
```

Imagine the response is:

```
2 MB
```

At:

```
10,000 requests/sec
```

you need:

```
2 MB × 10,000
= 20 GB/sec
```

or:

```
160 Gbps
```

That's a lot of network traffic.

This is why system designers care about:

- Smaller API responses
- Pagination
- Compression
- Caching
- CDN
- Efficient serialization
- Avoiding unnecessary fields

---

## 10. Compression

Suppose an API response is:

```
1 MB
```

After compression:

```
200 KB
```

That's an **80% reduction** in transferred data.

If you have:

```
10,000 requests/sec
```

**Without compression:**

```
1 MB × 10,000
= 10 GB/sec
```

**With compression:**

```
200 KB × 10,000
= 2 GB/sec
```

**Huge difference.**

Common HTTP compression mechanisms include:

- **gzip**
- **Brotli**

---

## 11. Bandwidth in Microservices

Consider:

```
Order Service
     |
     ↓
Payment Service
     |
     ↓
Inventory Service
     |
     ↓
Notification Service
```

Suppose each request carries:

```
500 KB
```

and there are:

```
5,000 requests/sec
```

Every service-to-service communication consumes network bandwidth.

In a microservices architecture, you therefore need to consider:

```
Service-to-service traffic
        +
Client-to-service traffic
        +
Database traffic
        +
Message broker traffic
```

The total network requirement can become substantial.

---

## 12. Bandwidth in Kafka

Since you've been studying event-driven architecture, this is particularly useful.

Suppose Kafka receives:

```
100,000 messages/sec
```

Each message:

```
10 KB
```

Incoming data:

```
100,000 × 10 KB
= 1,000,000 KB/sec
≈ 1 GB/sec
```

That's roughly:

```
8 Gbps
```

before considering replication and protocol overhead.

If Kafka has:

```
Replication Factor = 3
```

the cluster also needs substantial additional internal network traffic for replication.

So Kafka system design involves thinking about:

- **Producer → Kafka** bandwidth
- **Kafka → Consumer** bandwidth
- **Broker → Broker** replication bandwidth

---

## 13. Bandwidth and Database

A database can also become a bandwidth bottleneck.

Imagine:

```
Application Server
        |
        | 10 MB query result
        ↓
     Database
```

If the application executes:

```
1,000 queries/sec
```

that's:

```
10 MB × 1,000
= 10 GB/sec
```

of data movement.

Instead of transferring huge datasets, you might:

- Select only required columns
- Use pagination
- Filter at the database
- Aggregate at the database
- Cache results
- Use read replicas
- Avoid unnecessary queries

---

## 14. Bandwidth Calculation in System Design Interviews

A very useful formula is:

> **Bandwidth = Requests/sec × Average payload size**

For example:

```
Requests = 10,000/sec
Payload  = 100 KB
```

Then:

```
10,000 × 100 KB
= 1,000,000 KB/sec
≈ 1 GB/sec
```

In bits:

```
1 GB/sec × 8
≈ 8 Gbps
```

So you'd need roughly:

```
8 Gbps
```

of raw data-transfer capacity, plus overhead and headroom.

---

## 15. Peak Traffic Matters

Don't design only for average traffic.

Suppose:

```
Average = 10,000 requests/sec
Peak    = 50,000 requests/sec
```

Payload:

```
100 KB
```

Average bandwidth:

```
10,000 × 100 KB
≈ 1 GB/sec
```

Peak bandwidth:

```
50,000 × 100 KB
≈ 5 GB/sec
```

or approximately:

```
40 Gbps
```

Therefore, **peak bandwidth** is often what matters for capacity planning.

---

## 16. Bandwidth vs Latency vs Throughput

Keep this mental model:

| Concept        | Question                                      |
|----------------|-----------------------------------------------|
| Bandwidth      | How much data can the network carry?          |
| Latency        | How long does communication take?             |
| Throughput     | How much data are we actually transferring?   |
| Payload size   | How much data does each request contain?      |
| QPS/RPS        | How many requests are we handling?            |

And the basic relationship:

```
Required Bandwidth
        =
Requests/sec × Data/request
```

---

## 17. Where Bandwidth Becomes a Bottleneck

In a large-scale system, bandwidth can become the bottleneck at:

```
                  Internet
                     |
                     ↓
               Load Balancer
                     |
              ┌──────┴──────┐
              ↓             ↓
           Server         Server
              |             |
              └──────┬──────┘
                     ↓
                  Cache
                     |
                     ↓
                 Database
```

**Potential bottlenecks:**

- Client ↔ Load Balancer
- Load Balancer ↔ Servers
- Server ↔ Server
- Server ↔ Database
- Server ↔ Kafka
- Kafka ↔ Kafka
- Server ↔ Object Storage

A good system designer identifies **where** the network bandwidth is being consumed.

---

## 18. The Most Important Mental Model

When you're doing system design, think:

```
                REQUESTS
                   ↓
              QPS / RPS
                   ↓
             Payload Size
                   ↓
          ┌─────────────────┐
          │    BANDWIDTH    │
          └─────────────────┘
                   ↓
              NETWORK
                   ↓
             THROUGHPUT
                   ↓
               LATENCY
```

For every major component, ask:

> **How much data is moving through this component per second?**

That question naturally leads you toward bandwidth calculations and helps identify network bottlenecks.

### Interview Shortcut

If an interviewer gives you:

```
QPS           = 20,000
Request size  = 10 KB
Response size = 50 KB
```

Then:

```
Incoming bandwidth
= 20,000 × 10 KB
= 200 MB/sec

Outgoing bandwidth
= 20,000 × 50 KB
= 1 GB/sec

Total application network traffic:
≈ 1.2 GB/sec
≈ 9.6 Gbps
```

This is exactly the kind of **back-of-the-envelope calculation** you'll use repeatedly in system design.
