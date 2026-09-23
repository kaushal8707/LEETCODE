# Distributed ID Generation

Distributed ID Generation is a technique used in distributed systems to generate unique identifiers across multiple servers/services without depending on one centralized ID generator.

For example, suppose you have 100 application servers:

```
                    Load Balancer
                         |
          +--------------+--------------+
          |              |              |
       Server-1       Server-2       Server-3
          |              |              |
          +--------------+--------------+
                         |
                  Order / Payment
```

All servers may create records at the same time.

We need to guarantee:

```
Server-1 → Order ID = 100001
Server-2 → Order ID = 100002
Server-3 → Order ID = 100003
```

without generating duplicate IDs.

---

## 1. Why do we need Distributed ID Generation?

In a simple application, we can use:

`AUTO_INCREMENT`

For example:

```
1
2
3
4
5
```

This works well when there is a single database.

But distributed systems look more like:

```
                 Application
              /       |       \
             /        |        \
          DB-1       DB-2      DB-3
```

Now suppose each database generates:

```
DB-1 → 1001
DB-2 → 1001
DB-3 → 1001
```

We have a collision.

```
1001
 ↑
 ├── DB-1
 ├── DB-2
 └── DB-3
```

Duplicate ID ❌

Distributed ID generation solves this problem.

---

## 2. What problems are we trying to solve?

A good distributed ID generator should provide:

### Unique

Two servers should not generate the same ID.

```
Server-1 → 10001
Server-2 → 10002
Server-3 → 10003
```

### High performance

It should generate IDs very quickly.

```
10,000 IDs/sec
100,000 IDs/sec
1,000,000 IDs/sec
```

depending on the implementation.

### Distributed

Each server can generate IDs independently.

```
Server-1 ──→ generate ID
Server-2 ──→ generate ID
Server-3 ──→ generate ID
```

They don't need to call a central server for every ID.

### Highly available

If Server-2 is down:

```
Server-1 → still generates IDs
Server-3 → still generates IDs
```

The entire ID-generation mechanism doesn't have to stop.

### Scalable

Adding more application servers should not create collisions.

---

## 3. Why not use MAX(id) + 1?

A beginner might try:

```sql
SELECT MAX(order_id) + 1
FROM orders;
```

Suppose:

Current MAX = 100

Two requests arrive simultaneously:

- Server-1 → reads 100 → generates 101
- Server-2 → reads 100 → generates 101

Result:

```
Server-1 → 101
Server-2 → 101
```

Collision ❌

So this approach is not suitable for distributed ID generation.

---

## 4. Why not always use a database sequence?

A database sequence is much better:

```
Sequence
   |
   +--> 1001
   +--> 1002
   +--> 1003
```

But if every server has to communicate with one centralized database just to obtain an ID:

```
Server-1 ──┐
Server-2 ──┤
Server-3 ──┤
Server-4 ──┤
Server-5 ──┤
            ↓
       Central DB
```

The database can become:

- bottleneck
- availability dependency
- network dependency
- scalability limitation

For very large distributed systems, we often want IDs to be generated locally.

---

## 5. Common Distributed ID Generation approaches

There are several approaches.

```
Distributed ID Generation
│
├── UUID
├── Database Sequence
├── Hi/Lo / Range Allocation
├── Redis-based ID
├── Timestamp-based ID
└── Snowflake ID
```

One of the most important approaches to understand for system design interviews is:

**Snowflake-style ID generation**

---

## 6. Snowflake ID

A Snowflake-style ID generates a unique numeric ID by combining multiple pieces of information.

Conceptually:

```
+----------------+-------------+------------+
|   Timestamp    |  Worker ID  |  Sequence  |
+----------------+-------------+------------+
```

For example:

```
Timestamp = 123456789
Worker ID = 5
Sequence  = 10
```

These values are combined into one large integer.

---

## 7. How Snowflake works

Suppose we have three servers:

```
Server-1 → Worker ID = 1
Server-2 → Worker ID = 2
Server-3 → Worker ID = 3
```

All three receive requests at the same time.

**Server-1**
```
Timestamp = 5000
Worker ID = 1
Sequence = 0
```

**Server-2**
```
Timestamp = 5000
Worker ID = 2
Sequence = 0
```

**Server-3**
```
Timestamp = 5000
Worker ID = 3
Sequence = 0
```

Even though the timestamp is the same:

```
Server-1 → 5000 + 1 + 0
Server-2 → 5000 + 2 + 0
Server-3 → 5000 + 3 + 0
```

the IDs are different because the Worker ID is different.

---

## 8. Why do we need Sequence?

Suppose Server-1 receives 5 requests within the same millisecond.

```
Server-1
   |
   +-- Request 1
   +-- Request 2
   +-- Request 3
   +-- Request 4
   +-- Request 5
```

All requests may have:

```
Timestamp = 5000
Worker ID = 1
```

So we use a sequence:

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

## 9. How the complete ID is generated

Conceptually:

```
               ID Generator
                    |
        +-----------+-----------+
        |           |           |
        ↓           ↓           ↓
   Timestamp    Worker ID    Sequence
        |           |           |
        +-----------+-----------+
                    |
                    ↓
             Unique ID
```

A typical Snowflake-style implementation uses bit manipulation:

```
ID =
    (timestamp << timestampBits)
    |
    (workerId << workerBits)
    |
    sequence
```

For example, a common 64-bit layout is conceptually:

```
+------+----------------------+----------+------------+
| Sign | Timestamp            | Worker   | Sequence   |
+------+----------------------+----------+------------+
  1          41 bits             10 bits     12 bits
```

The exact layout can vary.

---

## 10. Real-world example: E-commerce

Suppose you have:

```
                Load Balancer
                     |
        +------------+------------+
        |            |            |
    Order-1       Order-2      Order-3
    Worker 1      Worker 2      Worker 3
```

Customer A creates an order.

Order Service on Server-1 generates:

```
orderId = 182736451234
```

Customer B creates another order.

Server-2 generates:

```
orderId = 182736451235
```

Customer C:

```
orderId = 182736451236
```

These IDs can then be stored in the database:

```
orders
------------------------------------------------
order_id        customer_id       amount
------------------------------------------------
182736451234    C101              500
182736451235    C102              900
182736451236    C103              250
```

The important point is that the application servers can generate these IDs without first asking a central database for the next ID.

---

## 11. How to use it in a microservices architecture

Consider:

```
                    API Gateway
                         |
        +----------------+----------------+
        |                |                |
        ↓                ↓                ↓
   Order Service   Payment Service   Inventory
        |                |                |
        ↓                ↓                ↓
     Order DB        Payment DB      Inventory DB
```

Order Service creates:

```
orderId = 123456789
```

Then publishes:

```
OrderCreated {
    orderId: 123456789
}
```

Payment Service creates its own ID:

```
paymentId = 987654321
```

Inventory Service creates:

```
reservationId = 456789123
```

So:

```
Order ID
   ↓
123456789
   |
   +---- Kafka
          |
          +---- Payment Service
          |       |
          |       └── paymentId
          |
          +---- Inventory Service
                  |
                  └── reservationId
```

Each service can independently generate IDs.

---

## 12. Java example

A simplified Snowflake-style implementation could look like:

```java
public class IdGenerator {

    private final long workerId;

    private long sequence = 0;
    private long lastTimestamp = -1;

    public IdGenerator(long workerId) {
        this.workerId = workerId;
    }

    public synchronized long nextId() {

        long timestamp = System.currentTimeMillis();

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
IdGenerator generator = new IdGenerator(1);

long id1 = generator.nextId();
long id2 = generator.nextId();
long id3 = generator.nextId();

System.out.println(id1);
System.out.println(id2);
System.out.println(id3);
```

You might get IDs conceptually like:

```
123456789001
123456789002
123456789003
```

The actual numbers depend on the timestamp and bit allocation.

> **Note:** This is a simplified educational implementation. A production implementation must also handle clock rollback, sequence overflow, worker-ID assignment, and concurrency carefully.

---

## 13. Worker ID is very important

Suppose:

```
Server-1 → Worker ID = 1
Server-2 → Worker ID = 2
```

Good.

But if accidentally:

```
Server-1 → Worker ID = 1
Server-2 → Worker ID = 1
```

both servers could produce:

```
Timestamp = 5000
Worker = 1
Sequence = 0
```

Potential collision:

```
Server-1 → ID X
Server-2 → ID X
```

❌ Duplicate

Therefore, production systems need a reliable way to assign unique worker IDs.

Possible approaches:

- Kubernetes Pod/StatefulSet identity
- Database allocation
- ZooKeeper
- etcd
- Configuration service
- Cloud instance identity

---

## 14. Important problem: Clock rollback

Snowflake relies on timestamps.

Suppose:

```
Current time = 10:00:10
```

Then the server clock moves backward:

```
10:00:10
     ↓
10:00:09
```

Now the generator may produce IDs that are out of order or potentially collide depending on implementation.

Production systems therefore need logic for clock rollback.

For example:

```
if currentTimestamp < lastTimestamp:

    handleClockRollback()
```

Possible strategies include:

- wait until clock catches up
- use a logical timestamp
- fail generation temporarily
- use a clock sequence

---

## 15. UUID vs Snowflake

### UUID

Example:

```
550e8400-e29b-41d4-a716-446655440000
```

**Advantages:**

- easy to generate
- decentralized
- extremely low collision probability
- no worker-ID management

**Disadvantages:**

- larger
- less convenient for some database indexes
- ordering depends on UUID version

### Snowflake-style ID

Example:

```
182736451234567890
```

**Advantages:**

- compact 64-bit representation
- high generation performance
- roughly time-ordered
- contains worker information
- good for distributed systems

**Disadvantages:**

- more complex
- requires worker-ID management
- depends on clock behavior

---

## 16. Distributed ID vs Database Auto Increment

| Feature | Auto Increment | Distributed ID |
|---|---|---|
| Single DB | Excellent | Excellent |
| Multiple servers | Limited | Designed for it |
| Multiple DBs | Difficult | Easy |
| Central dependency | Yes | Can avoid it |
| High scale | Can become bottleneck | Better suited |
| Globally unique | Usually DB-scoped | Designed for global uniqueness |
| Complexity | Low | Higher |

---

## 17. Where should you use Distributed IDs?

Typical examples:

- Order ID
- Payment ID
- Transaction ID
- User ID
- Event ID
- Message ID
- Invoice ID
- Shipment ID
- Reservation ID
- Distributed database primary keys

For example:

```
Order Service
      |
      ↓
orderId = 183746281937
      |
      +---- Database
      |
      +---- Kafka
      |
      +---- Payment Service
      |
      +---- Inventory Service
```

---

## 18. Distributed ID vs Correlation ID

These are often confused.

### Distributed ID

Identifies an entity.

```
orderId = 123456
```

Meaning:

Which order is this?

### Correlation ID

Identifies a request flow.

```
correlationId = abc-123
```

Example:

```
HTTP Request
     |
     | correlationId = abc-123
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

The correlation ID helps you trace the request across services.

---

## 19. Complete architecture

A typical production design could look like:

```
                         Client
                           |
                           ↓
                    API Gateway
                           |
                           ↓
                  +----------------+
                  | Order Service  |
                  +----------------+
                           |
                           ↓
                    ID Generator
                           |
              +------------+------------+
              |                         |
         Timestamp                  Sequence
              |                         |
              +------------+------------+
                           |
                           ↓
                       Order ID
                           |
              +------------+------------+
              |                         |
              ↓                         ↓
          Order DB                   Kafka
                                         |
                           +-------------+-------------+
                           |                           |
                           ↓                           ↓
                    Payment Service             Inventory
                           |                           |
                           ↓                           ↓
                     Payment ID              Reservation ID
```

---

## 20. Interview answer

If an interviewer asks:

> What is Distributed ID Generation and why do we need it?

You can answer:

> Distributed ID Generation is a mechanism for generating globally unique identifiers across multiple application instances or services without relying on a single centralized ID generator. It is needed in distributed systems because multiple servers, databases, or microservices can create records concurrently. A centralized database sequence can become a bottleneck or availability dependency. Approaches such as Snowflake generate IDs locally by combining a timestamp, worker/node ID, and sequence number, providing high throughput, uniqueness, and scalability.

---

## The most important concept to remember

```
                 Distributed ID
                       |
          +------------+------------+
          |            |            |
          ↓            ↓            ↓
      Timestamp     Worker ID    Sequence
          |            |            |
          +------------+------------+
                       |
                       ↓
                  Unique ID
```

- **Timestamp** → when it was generated
- **Worker ID** → which server generated it
- **Sequence** → distinguishes multiple IDs generated by that server at the same time

