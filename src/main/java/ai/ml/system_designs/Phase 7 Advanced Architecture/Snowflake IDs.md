# Snowflake IDs

A Snowflake ID is a distributed, unique ID-generation technique designed for large-scale distributed systems.

It allows multiple servers to generate unique IDs independently, without calling a central database every time an ID is needed.

A simplified Snowflake ID looks like:

```
+----------------------+-------------+------------+
|      Timestamp       |  Worker ID  |  Sequence  |
+----------------------+-------------+------------+
```

The exact bit allocation can vary by implementation.

---

## 1. Why do we need Snowflake IDs?

Imagine an e-commerce application running on multiple servers:

```
                         Load Balancer
                              |
               +--------------+--------------+
               |              |              |
               ↓              ↓              ↓
           Server-1       Server-2       Server-3
           Worker-1       Worker-2       Worker-3
               |              |              |
               +--------------+--------------+
                              |
                         Order Service
```

All three servers can receive requests simultaneously.

Suppose:

```
Server-1 → Create Order
Server-2 → Create Order
Server-3 → Create Order
```

Every order needs a unique ID.

We want:

```
Server-1 → 182736451001
Server-2 → 182736451002
Server-3 → 182736451003
```

and never:

```
Server-1 → 182736451001
Server-2 → 182736451001   ❌
```

Snowflake IDs solve this problem.

---

## 2. Why not use database auto-increment?

A traditional database can generate:

```
1001
1002
1003
1004
```

using:

`AUTO_INCREMENT`

But imagine:

```
             Application
           /      |      \
          ↓       ↓       ↓
        DB-1    DB-2    DB-3
```

Each database could generate:

```
DB-1 → 1001
DB-2 → 1001
DB-3 → 1001
```

Now we have duplicate IDs.

We could use a central ID database:

```
Server-1 ──┐
Server-2 ──┤
Server-3 ──┤
Server-4 ──┤
            ↓
       Central ID DB
```

But now the central service can become:

- a bottleneck
- a single dependency
- a latency source
- a scalability problem

Snowflake avoids needing this centralized call for every ID.

---

## 3. Main idea behind Snowflake

Snowflake combines three important pieces of information:

```
Timestamp
    +
Worker ID
    +
Sequence Number
    =
Unique ID
```

For example:

```
Timestamp = 5000
Worker ID = 7
Sequence  = 25
```

These are encoded into one integer.

Conceptually:

```
+----------------------+-------------+------------+
|      Timestamp       |  Worker ID  |  Sequence  |
+----------------------+-------------+------------+
```

---

## 4. How Snowflake IDs work

Let's understand each part.

### A. Timestamp

The timestamp represents when the ID was generated.

For example:

```
Timestamp
   ↓
5000
```

Usually Snowflake implementations don't store the full Unix timestamp directly.

Instead, they store:

```
currentTime - customEpoch
```

For example:

```
Current Time = 2026-09-19 10:30:00

Custom Epoch = 2020-01-01

Timestamp Difference = X
```

This difference is stored in the ID.

This allows the timestamp portion to fit into fewer bits.

---

## 5. Worker ID

The Worker ID identifies the machine/process generating the ID.

For example:

```
Server-1 → Worker ID = 1
Server-2 → Worker ID = 2
Server-3 → Worker ID = 3
```

So:

**Server-1**
```
Timestamp = 5000
Worker    = 1
```

**Server-2**
```
Timestamp = 5000
Worker    = 2
```

Even though the timestamp is identical, the Worker IDs are different.

Therefore their generated IDs are different.

---

## 6. Sequence Number

Now consider Server-1 receiving many requests in the same millisecond.

```
Server-1
   |
   +---- Request 1
   +---- Request 2
   +---- Request 3
   +---- Request 4
   +---- Request 5
```

All requests may have:

```
Timestamp = 5000
Worker ID = 1
```

So we use the sequence:

```
Timestamp  Worker  Sequence

5000       1       0
5000       1       1
5000       1       2
5000       1       3
5000       1       4
```

Now each ID is unique.

---

## 7. Classic Snowflake 64-bit structure

A commonly referenced Snowflake design uses 64 bits:

```
+-----+-----------------------+------------+------------+
|  1  |        41 bits        |  10 bits   |  12 bits   |
+-----+-----------------------+------------+------------+
|Sign | Timestamp             | Worker ID  | Sequence   |
+-----+-----------------------+------------+------------+
```

Meaning:

```
1 bit
 ↓
Sign

41 bits
 ↓
Timestamp

10 bits
 ↓
Worker / machine identifier

12 bits
 ↓
Sequence number
```

This gives:

```
41 + 10 + 12 + 1 = 64 bits
```

The exact layout is configurable; don't assume every Snowflake implementation uses these exact allocations.

---

## 8. What does 10-bit Worker ID mean?

If we allocate:

```
10 bits
```

for the worker ID, we can represent:

```
2^10 = 1024
```

different worker IDs.

For example:

```
Worker 0
Worker 1
Worker 2
...
Worker 1023
```

So theoretically we can have up to 1024 unique worker identifiers under that allocation.

---

## 9. What does 12-bit Sequence mean?

If we allocate:

```
12 bits
```

for the sequence:

```
2^12 = 4096
```

possible sequence values.

So one worker can generate up to roughly:

```
4096 IDs / millisecond
```

before it needs to move to the next millisecond, assuming that allocation and implementation.

That is approximately:

```
4.096 million IDs/second
```

per worker under the idealized model.

---

## 10. Example

Suppose:

```
Timestamp = 1000
Worker ID = 5
Sequence = 10
```

Conceptually:

```
+----------------+----------+----------+
| Timestamp 1000 | Worker 5 | Seq 10   |
+----------------+----------+----------+
```

The generator encodes these values using bit shifting:

```java
long id =
        (timestamp << 22)
        | (workerId << 12)
        | sequence;
```

Why 22?

Because:

```
Worker bits   = 10
Sequence bits = 12

10 + 12 = 22
```

So:

```
timestamp << 22
```

moves the timestamp into its position.

And:

```
workerId << 12
```

moves the worker ID into its position.

Then:

`|`

combines the fields.

---

## 11. How to implement Snowflake in Java

A simplified implementation:

```java
public class SnowflakeIdGenerator {

    private final long workerId;

    private long sequence = 0;
    private long lastTimestamp = -1;

    public SnowflakeIdGenerator(long workerId) {
        this.workerId = workerId;
    }

    public synchronized long nextId() {

        long timestamp = System.currentTimeMillis();

        if (timestamp < lastTimestamp) {
            throw new IllegalStateException(
                "Clock moved backwards"
            );
        }

        if (timestamp == lastTimestamp) {
            sequence++;
        } else {
            sequence = 0;
        }

        lastTimestamp = timestamp;

        return (timestamp << 22)
                | (workerId << 12)
                | sequence;
    }
}
```

Usage:

```java
SnowflakeIdGenerator generator =
        new SnowflakeIdGenerator(1);

long id1 = generator.nextId();
long id2 = generator.nextId();
long id3 = generator.nextId();

System.out.println(id1);
System.out.println(id2);
System.out.println(id3);
```

You would get unique numeric IDs.

> This is an educational implementation, not production-ready. A production implementation must carefully handle bit ranges, sequence overflow, clock rollback, worker-ID allocation, concurrency, and the timestamp epoch.

---

## 12. What happens when multiple requests arrive?

Suppose:

```
Worker ID = 10
Timestamp = 1000
```

Requests:

```
Request 1 → Sequence 0
Request 2 → Sequence 1
Request 3 → Sequence 2
Request 4 → Sequence 3
```

The generated IDs are conceptually:

```
Timestamp Worker Sequence
1000      10     0
1000      10     1
1000      10     2
1000      10     3
```

Because the sequence changes, the IDs are different.

---

## 13. What happens when another server generates an ID?

**Server-1:**

```
Timestamp = 1000
Worker = 10
Sequence = 0
```

**Server-2:**

```
Timestamp = 1000
Worker = 11
Sequence = 0
```

So:

```
Server-1 → 1000 + 10 + 0
Server-2 → 1000 + 11 + 0
```

No collision.

This is the key property of Snowflake.

---

## 14. Real-world example — Order Service

Consider:

```
                         Load Balancer
                              |
             +----------------+----------------+
             |                |                |
             ↓                ↓                ↓
       Order Server-1   Order Server-2   Order Server-3
       Worker ID = 1    Worker ID = 2    Worker ID = 3
```

Customer 1 creates an order.

**Server-1:**

```
Timestamp = T
Worker = 1
Sequence = 0
```

Generated:

```
Order ID = 182736451234
```

Customer 2 creates another order.

**Server-2:**

```
Timestamp = T
Worker = 2
Sequence = 0
```

Generated:

```
Order ID = 182736451235
```

Customer 3:

```
Server-3
Worker = 3
Sequence = 0
```

Generated:

```
Order ID = 182736451236
```

The servers don't need to coordinate with a central ID server for every request.

---

## 15. Using Snowflake IDs in a database

For example:

```sql
CREATE TABLE orders (
    order_id BIGINT PRIMARY KEY,
    customer_id BIGINT,
    amount DECIMAL(10,2),
    created_at TIMESTAMP
);
```

Application:

```java
long orderId = snowflakeGenerator.nextId();
```

Then:

```sql
INSERT INTO orders
(order_id, customer_id, amount)
VALUES
(182736451234, 1001, 500.00);
```

So the flow becomes:

```
HTTP Request
     |
     ↓
Order Service
     |
     ↓
Snowflake Generator
     |
     ↓
orderId
     |
     ↓
Database
```

---

## 16. Snowflake IDs in microservices

Consider:

```
                     API Gateway
                          |
             +------------+------------+
             |            |            |
             ↓            ↓            ↓
        Order Service  Payment      Inventory
             |          Service       Service
             ↓            ↓            ↓
          Order DB    Payment DB   Inventory DB
```

Each service can have its own ID generator.

For example:

```
Order Service
    ↓
orderId = 100001

Payment Service
    ↓
paymentId = 200001

Inventory Service
    ↓
reservationId = 300001
```

This avoids making every service dependent on a central ID-generation service.

---

## 17. Important: Worker ID allocation

This is one of the most important production concerns.

Suppose:

```
Server-1 → Worker ID = 1
Server-2 → Worker ID = 1
```

That's dangerous.

Both can generate:

```
Timestamp = T
Worker = 1
Sequence = 0
```

Potentially producing the same ID.

Therefore:

```
Worker ID
     ↓
Must be unique
```

Worker IDs can be assigned using mechanisms such as:

- Kubernetes StatefulSet identity
- Configuration
- Database allocation
- ZooKeeper
- etcd
- Service discovery/coordination
- Cloud instance identity

The important requirement is:

> Two simultaneously active generators must not use the same worker ID.

---

## 18. Clock rollback problem

Snowflake depends on time.

Suppose:

```
Last timestamp = 10000
Current timestamp = 9995
```

The system's clock moved backward.

```
10000
  ↓
9995
```

This is called **clock rollback**.

It can cause problems with:

- uniqueness
- ordering
- timestamp calculations

A production implementation should detect it.

For example:

```java
if (timestamp < lastTimestamp) {
    // Handle clock rollback
}
```

Possible strategies include:

- Wait until the clock catches up
- Use a logical clock
- Use a clock sequence
- Fail temporarily

The correct strategy depends on the system's requirements.

---

## 19. Sequence overflow

Suppose the sequence has 12 bits:

```
Maximum = 4095
```

Now the server receives more than 4096 ID requests within the same millisecond.

The generator cannot simply wrap around to:

`0`

because that could create a duplicate.

Instead, it typically waits for the next millisecond:

```
Current timestamp = 1000
Sequence = 4095

          ↓

Wait

          ↓

Timestamp = 1001
Sequence = 0
```

Then generation continues.

---

## 20. Why Snowflake IDs are useful

Snowflake IDs provide several useful properties:

### 1. Distributed

```
Server-1 ──┐
Server-2 ──┤
Server-3 ──┤──→ Generate locally
Server-4 ──┘
```

### 2. Unique

Worker ID + sequence + timestamp help prevent collisions.

### 3. High throughput

IDs can be generated locally without a network call.

### 4. Roughly time ordered

Because the timestamp is part of the ID, newer IDs generally have larger timestamp components, assuming normal clock behavior.

### 5. Compact

A 64-bit integer is smaller than many string-based identifiers.

### 6. Database friendly

A numeric ID can be convenient for primary keys and indexes, though the exact indexing behavior depends on workload and implementation.

---

## 21. Snowflake vs UUID

| Feature | Snowflake | UUID |
|---|---|---|
| Distributed | Yes | Yes |
| Central server required | No | No |
| Numeric | Yes | Usually represented as string/128-bit value |
| Size | Commonly 64-bit | Commonly 128-bit |
| Roughly time ordered | Yes | Depends on UUID version |
| Worker ID | Required in classic design | No |
| Sequence | Yes | No |
| High throughput | Yes | Yes |
| Implementation complexity | Higher | Lower |

Example Snowflake:

```
182736451234567890
```

Example UUID:

```
550e8400-e29b-41d4-a716-446655440000
```

---

## 22. Snowflake vs Auto Increment

```
Auto Increment
      |
      ↓
Central Database
      |
      ↓
Next ID
```

versus:

```
Snowflake
      |
      +---- Timestamp
      +---- Worker ID
      +---- Sequence
              |
              ↓
          Unique ID
```

Auto-increment is simple and often perfectly appropriate for a single database.

Snowflake becomes attractive when IDs need to be generated independently across many application instances, partitions, services, or databases.

---

## 23. Where can we use Snowflake IDs?

Typical examples:

```
E-commerce
    ↓
Order ID

Payment System
    ↓
Transaction ID

Banking
    ↓
Transaction ID

Social Network
    ↓
Post ID
User ID
Message ID

Microservices
    ↓
Entity IDs
Event IDs

Distributed Systems
    ↓
Database primary keys
Event identifiers
Message identifiers
```

---

## 24. Snowflake ID vs Correlation ID

Don't confuse these.

### Snowflake ID

Usually identifies an entity:

```
orderId = 182736451234
```

Meaning:

Which order is this?

### Correlation ID

Identifies a request flow:

```
correlationId = ABC-123
```

For example:

```
Client
  |
  | correlationId = ABC-123
  ↓
Order Service
  |
  ↓
Kafka
  |
  ↓
Payment Service
  |
  ↓
Inventory Service
```

The correlation ID helps trace the request across services.

---

## 25. Complete flow

A typical Snowflake-based system:

```
                    Client
                       |
                       ↓
                 API Gateway
                       |
                       ↓
                Order Service
                       |
                       ↓
              Snowflake Generator
                       |
          +------------+------------+
          |            |            |
          ↓            ↓            ↓
     Timestamp     Worker ID     Sequence
          |            |            |
          +------------+------------+
                       |
                       ↓
                  Order ID
                       |
             +---------+---------+
             |                   |
             ↓                   ↓
          Order DB             Kafka
                                  |
                       +----------+----------+
                       |                     |
                       ↓                     ↓
                Payment Service       Inventory Service
```

---

## 26. Interview answer

If the interviewer asks:

> What is Snowflake ID and why do we use it?

A strong answer is:

> Snowflake is a distributed ID-generation algorithm that generates unique 64-bit-style numeric IDs without requiring a centralized ID-generation service for every request. A typical Snowflake ID combines a timestamp, worker or machine ID, and sequence number. The timestamp provides time-based ordering, the worker ID differentiates different generators, and the sequence differentiates multiple IDs generated by the same worker within the same time unit. It is useful in high-scale distributed systems because IDs can be generated locally with high throughput and low latency.

---

## Remember this

```
              Snowflake ID
                   |
       +-----------+-----------+
       |           |           |
       ↓           ↓           ↓
 Timestamp     Worker ID    Sequence
       |           |           |
       +-----------+-----------+
                   |
                   ↓
              Unique ID
```

- **Timestamp** = When
- **Worker ID** = Who
- **Sequence** = Which request

