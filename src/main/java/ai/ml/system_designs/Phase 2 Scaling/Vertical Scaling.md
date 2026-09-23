# Vertical Scaling

Since you're learning System Design from scratch, let's understand Vertical Scaling from the basics and then connect it to real-world systems.

---

## 1. What is Vertical Scaling?

Vertical Scaling = **Scale Up / Scale Down**

It means increasing or decreasing the resources of an existing server.

For example, suppose your application is running on:

```
Server
------
CPU    : 4 cores
RAM    : 8 GB
Storage: 100 GB
```

Your application starts getting more traffic and the server becomes overloaded.

Instead of adding another server, you upgrade the existing server:

```
Server
------
CPU    : 16 cores
RAM    : 64 GB
Storage: 1 TB
```

That's **Vertical Scaling**.

### Simple definition

> Vertical scaling means increasing the capacity of a single machine.

---

## 2. Real-World Example

Imagine you have an e-commerce application:

```
             Users
               |
               v
       +----------------+
       | Application    |
       | Server         |
       |                |
       | 4 CPU          |
       | 8 GB RAM       |
       +----------------+
               |
               v
           Database
```

Initially, 4 CPU and 8 GB RAM are enough.

But your application becomes popular.

You now have:

```
10,000 requests/sec
```

The server starts experiencing:

- High CPU
- High memory usage
- Increased latency
- Request timeouts
- Application crashes

One solution is to upgrade the server:

```
             Users
               |
               v
       +----------------+
       | Application    |
       | Server         |
       |                |
       | 32 CPU         |
       | 128 GB RAM     |
       +----------------+
               |
               v
           Database
```

This is vertical scaling.

---

## 3. Vertical Scaling vs Horizontal Scaling

This is one of the most important concepts in System Design.

### Vertical Scaling

Make one machine bigger.

```
Before:

+---------+
| Server  |
| 4 CPU   |
| 8 GB    |
+---------+

        ↓ SCALE UP

+----------------+
|     Server     |
|    32 CPU      |
|    128 GB      |
+----------------+
```

### Horizontal Scaling

Add more machines.

```
Before:

+---------+
| Server  |
+---------+

        ↓ SCALE OUT

+---------+   +---------+   +---------+
| Server 1|   | Server 2|   | Server 3|
+---------+   +---------+   +---------+
```

So remember:

```
Vertical = Bigger machine
Horizontal = More machines
```

---

## 4. What Can We Increase?

When vertically scaling a server, we can increase resources such as:

### CPU

```
4 CPU → 8 CPU → 16 CPU → 32 CPU
```

Useful when your application is CPU-bound.

For example:

- Image Processing
- Video Encoding
- Complex Calculations
- Data Processing

### RAM

```
8 GB → 16 GB → 32 GB → 64 GB → 128 GB
```

Useful when your application needs more memory.

For example:

- Large in-memory cache
- Large JVM heap
- Large datasets

For a Java/Spring Boot application, you might increase:

```
-Xmx2g
```

to:

```
-Xmx8g
```

provided the machine has enough RAM.

### Storage

You can also increase:

```
100 GB → 500 GB → 1 TB → 5 TB
```

Useful when the server is running out of disk space.

### Network Capacity

You may also increase network bandwidth:

```
1 Gbps → 10 Gbps
```

This can help when network throughput is the bottleneck.

---

## 5. Vertical Scaling of a Database

Vertical scaling is especially common with databases.

Suppose you have:

```
                Application
                     |
                     v
              +-------------+
              |   MySQL     |
              |             |
              | 4 CPU       |
              | 16 GB RAM   |
              +-------------+
```

Database traffic increases.

You can upgrade it:

```
              +-------------+
              |   MySQL     |
              |             |
              | 32 CPU      |
              | 128 GB RAM  |
              +-------------+
```

This can significantly improve database performance.

This is one reason why managed database services offer different instance sizes.

---

## 6. Advantages of Vertical Scaling

### 1. Simple

You don't necessarily need to modify your application architecture.

For example:

```
Application
     |
     v
One Server
```

You simply increase the server capacity.

### 2. Easier to manage

Compare:

**Vertical:**

```
        Application
             |
             v
        Big Server
```

with:

**Horizontal:**

```
             Load Balancer
             /     |      \
            /      |       \
       Server1 Server2 Server3
          |        |        |
         DB       DB       DB
```

Horizontal architecture introduces additional complexity.

### 3. No need for load balancing

If you have only one application server:

```
Client
  |
  v
Server
```

there's no need to distribute requests across multiple application servers.

### 4. Useful for databases

Databases can sometimes be harder to horizontally scale than stateless application servers.

Therefore, vertical scaling can be a very useful strategy for databases.

---

## 7. Disadvantages of Vertical Scaling

This is where System Design interviews become important.

### 7.1 Hardware Limit

There is always a maximum machine size.

You cannot infinitely increase:

- CPU
- RAM
- Storage
- Network

For example:

```
4 CPU
  ↓
8 CPU
  ↓
16 CPU
  ↓
32 CPU
  ↓
64 CPU
  ↓
???
```

Eventually, you hit the maximum available configuration.

---

## 8. Single Point of Failure

This is one of the biggest problems.

Suppose you have:

```
              Users
                |
                v
          +-----------+
          |  Server   |
          |  64 CPU   |
          |  256 GB   |
          +-----------+
```

If that server goes down:

```
              Users
                |
                X
          Server DOWN
```

Your entire application may become unavailable.

This is called a:

> **Single Point of Failure (SPOF)**

---

## 9. Downtime During Scaling

Depending on the infrastructure and workload, upgrading a machine may require:

```
Stop
  ↓
Upgrade
  ↓
Restart
```

For example:

```
Application
    |
    v
Server
    |
    X
  Restart
    |
    v
Server
```

Modern cloud infrastructure can often reduce or avoid downtime for many scaling operations, but vertical scaling is still fundamentally constrained by the capacity and operational characteristics of one machine.

---

## 10. Cost

Bigger machines become increasingly expensive.

For example, conceptually:

```
4 CPU / 8 GB     → $$
8 CPU / 16 GB    → $$$
16 CPU / 32 GB   → $$$$
32 CPU / 64 GB   → $$$$$$$
```

The price doesn't always increase linearly with resources.

At some point:

> **Adding more machines can be more cost-effective than buying one extremely large machine.**

---

## 11. Vertical Scaling in Java/Spring Boot

Imagine you have:

```
Spring Boot Application

CPU: 90%
RAM: 85%
```

You could increase:

```
4 CPU → 8 CPU
8 GB  → 16 GB
```

and configure the JVM appropriately:

```
-Xms4g
-Xmx8g
```

But there's an important point:

> Giving the JVM more memory doesn't automatically solve every performance problem.

The bottleneck might actually be:

```
Database
   ↓
Network
   ↓
External API
   ↓
Thread pool
   ↓
Connection pool
   ↓
CPU
```

So before scaling, identify the actual bottleneck.

---

## 12. Vertical Scaling + Horizontal Scaling

Real-world systems often use both.

For example:

```
                    Users
                      |
                      v
                Load Balancer
                 /    |    \
                /     |     \
               v      v      v
           Server1 Server2 Server3
             8CPU    8CPU    8CPU
              |       |       |
              +-------+-------+
                      |
                      v
                 Database
                  32 CPU
                  128 GB
```

Here:

**Application servers**

Use horizontal scaling:

```
3 servers → 10 servers
```

**Individual servers**

Can also be vertically scaled:

```
8 CPU → 16 CPU
```

**Database**

May initially use vertical scaling:

```
16 CPU → 32 CPU → 64 CPU
```

and later introduce other techniques such as:

- Read Replicas
- Partitioning / Sharding
- Caching

---

## 13. When Should You Use Vertical Scaling?

Vertical scaling is particularly useful when:

- Your application is small or medium-sized
- You want a simple architecture
- The workload doesn't justify multiple servers
- The application is difficult to distribute
- You're scaling a database
- You need more CPU/RAM on a particular machine
- You want a quick capacity increase

---

## 14. Interview Example

**Interviewer:**

> Your application is receiving 10× more traffic. How would you scale it?

Don't immediately say:

> "I'll increase the CPU."

Instead, think:

```
             Increased Traffic
                    |
                    v
             Identify Bottleneck
                    |
       +------------+------------+
       |            |            |
      CPU          RAM        Database
       |            |            |
       v            v            v
    Scale Up     Scale Up    Scale Up
```

If one server isn't enough:

```
                Load Balancer
               /      |      \
              v       v       v
           Server1 Server2 Server3
```

Now you're using horizontal scaling.

---

## 15. Key Difference to Remember

| Feature | Vertical Scaling | Horizontal Scaling |
|---|---|---|
| Other name | Scale Up | Scale Out |
| Approach | Bigger machine | More machines |
| What to increase | CPU/RAM | Add servers |
| Complexity | Simpler | More complex |
| Hardware limit | Yes | Much larger theoretical capacity |
| SPOF risk | Can create SPOF | Better fault tolerance |
| Database scaling | Often useful for DBs | Very common for stateless services |
| Scalability | Limited | Highly scalable |

---

## 16. The Most Important Mental Model

Think about a restaurant.

### Vertical Scaling

You have one restaurant.

You make it bigger:

```
Small Restaurant
      ↓
Bigger Restaurant
      ↓
Huge Restaurant
```

That's vertical scaling.

### Horizontal Scaling

You open more restaurants:

```
        Restaurant 1
             |
Restaurant 2 - Restaurant 3
             |
        Restaurant 4
```

That's horizontal scaling.

### In System Design:

```
VERTICAL
"Make the server stronger."

HORIZONTAL
"Add more servers."
```

And the key trade-off is:

> **Vertical scaling is simpler but has a ceiling. Horizontal scaling is more complex but gives you much greater scalability and availability.**
