# Connection Pooling

Connection Pooling is an important System Design and backend concept, especially when you're working with Spring Boot + databases.

It answers a simple question:

> Do we create a new database connection for every request, or can we reuse existing connections?

The answer is: **reuse them**.

---

## 1. What is a Database Connection?

Suppose your Spring Boot application needs data from MySQL/PostgreSQL.

The application needs to establish a connection:

```
Spring Boot
     |
     |  Open Connection
     v
   Database
```

Conceptually, establishing a database connection involves work such as:

```
Application
     |
     | TCP connection
     v
 Database
     |
     | Authentication
     |
     | Session setup
     v
Connection Established
```

Creating this connection is not free.

There is network overhead, authentication/session setup, database resource usage, etc.

---

## 2. Without Connection Pooling

Imagine your application receives 5 requests:

```
Request 1
Request 2
Request 3
Request 4
Request 5
```

Without pooling, you might have:

```
Request 1 → Create DB Connection → Query → Close
Request 2 → Create DB Connection → Query → Close
Request 3 → Create DB Connection → Query → Close
Request 4 → Create DB Connection → Query → Close
Request 5 → Create DB Connection → Query → Close
```

Diagram:

```
Request
   |
   v
Create Connection
   |
   v
Execute Query
   |
   v
Close Connection
```

Then repeat for every request.

This is inefficient when traffic is high.

---

## 3. What is Connection Pooling?

Instead of creating a new connection every time, we create a pool of reusable database connections.

```
                  Spring Boot
                      |
                      v
              +----------------+
              | Connection Pool|
              +----------------+
                |  |  |  |  |
                v  v  v  v  v
               C1 C2 C3 C4 C5
                \  |  |  |  /
                 \ |  |  | /
                   Database
```

The application borrows a connection from the pool.

After finishing the query, it returns the connection to the pool.

**Important:**

> `close()` usually means return the connection to the pool, not physically destroy the underlying database connection.

---

## 4. Simple Example

Suppose the pool contains 5 connections:

```
Connection Pool

C1 → Available
C2 → Available
C3 → Available
C4 → Available
C5 → Available
```

**Request 1 arrives:**

```
Request 1 → C1
```

Now:

```
C1 → In Use
C2 → Available
C3 → Available
C4 → Available
C5 → Available
```

**Request 2:**

```
Request 2 → C2
```

Now:

```
C1 → In Use
C2 → In Use
C3 → Available
C4 → Available
C5 → Available
```

**After Request 1 finishes:**

```
C1 → Available
```

Another request can reuse it.

---

## 5. The Lifecycle

A typical request looks like:

```
HTTP Request
     |
     v
Spring Boot
     |
     | getConnection()
     v
Connection Pool
     |
     | Borrow C1
     v
Database
     |
     | Execute SQL
     v
Application
     |
     | close()
     v
Connection Pool
     |
     | C1 becomes available
     v
Next Request
```

So:

```
Borrow → Use → Return
```

not:

```
Create → Use → Destroy
```

---

## 6. Real-World Analogy

Think about taxis.

**Without pooling:**

```
Customer
   |
   v
Create/Get new taxi
   |
   v
Travel
   |
   v
Destroy taxi
```

That's obviously wasteful.

**With a pool:**

```
                 Taxi Pool
              /   /   |   \
             🚕  🚕   🚕   🚕
                    |
                    v
                 Customer
                    |
                  Travel
                    |
                    v
              Taxi returned
                 to pool
```

The same taxi can serve another customer.

Database connections work similarly.

---

## 7. Connection Pool in Spring Boot

Spring Boot applications commonly use HikariCP as the JDBC connection pool.

Conceptually:

```
Spring Boot
     |
     v
HikariCP
     |
 +---+---+---+---+
 |   |   |   |   |
 C1  C2  C3  C4  C5
     |
     v
 Database
```

You typically configure things such as:

```properties
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5
```

Meaning conceptually:

```
Maximum connections = 10
Minimum idle        = 5
```

Exact behavior depends on the pool configuration and version.

---

## 8. Why Do We Need a Pool Size?

This is extremely important.

Suppose:

```
Connection Pool = 5
```

And you have:

```
100 concurrent requests
```

Only 5 requests can hold database connections simultaneously.

```
100 Requests
     |
     v
Connection Pool
+---+---+---+---+
| C1| C2| C3| C4| C5|
+---+---+---+---+
  ↑   ↑   ↑   ↑   ↑
  |   |   |   |   |
 Request 1 ... Request 5

Requests 6-100
       |
       v
     WAIT
```

As connections become available:

```
C1 finishes
   ↓
Request 6 gets C1
```

---

## 9. What Happens If the Pool Is Too Small?

Suppose:

```
Pool size = 5
```

but your application has:

```
100 concurrent database requests
```

Many requests have to wait.

Eventually you may see:

```
Connection Timeout
```

For example, conceptually:

```
Request
   |
   v
Connection Pool
   |
   +--> No connection available
   |
   v
Wait
   |
   v
Timeout
```

This can increase:

```
Latency
   ↓
Request Timeout
   ↓
Errors
```

---

## 10. What Happens If the Pool Is Too Large?

You might think:

> "Then I'll just configure 500 connections."

This can actually make things worse.

Suppose:

```
Application Server 1 → 100 connections
Application Server 2 → 100 connections
Application Server 3 → 100 connections
Application Server 4 → 100 connections
```

Total:

```
400 database connections
```

But your database might only be comfortable handling a much smaller number of concurrent connections.

Then:

```
                    Database
                       🔥
          ↑      ↑      ↑      ↑
         100    100    100    100
        App1   App2   App3   App4
```

The database becomes overloaded.

So:

> **Connection pool size must be considered across all application instances, not just one instance.**

---

## 11. Very Important System Design Calculation

Suppose you have:

```
10 application servers
```

Each server has:

```
maximumPoolSize = 20
```

Potential maximum database connections:

```
10 × 20 = 200
```

So your database could receive up to:

```
200 connections
```

This is a critical point when horizontally scaling.

> **Horizontal scaling can multiply database connections.**

```
              Load Balancer
          /    /    |    \    \
         v    v     v     v     v
       App1 App2  App3  ...   App10
        |    |     |           |
       20   20    20          20
        \    |     |           /
         \   |     |          /
              Database
             ~200 max
```

This is why application scaling and database capacity must be designed together.

---

## 12. Connection Pool vs Thread Pool

These are different concepts.

### Thread Pool

Controls application threads.

```
Spring Boot
    |
    v
Thread Pool
 | | | | |
 T T T T T
```

### Connection Pool

Controls database connections.

```
Spring Boot
    |
    v
Connection Pool
 | | | | |
 C C C C C
```

A thread may need a database connection:

```
Request
   |
   v
Application Thread
   |
   v
Get DB Connection
   |
   v
Execute SQL
   |
   v
Return Connection
```

You need to size these resources thoughtfully.

---

## 13. Connection Pool and Transactions

Connection pooling is especially important with database transactions.

For example:

```java
@Transactional
public void createOrder() {

    // Insert order

    // Insert order items

    // Update inventory
}
```

Conceptually:

```
Transaction begins
       |
       v
Borrow DB connection
       |
       v
INSERT Order
       |
       v
INSERT Order Items
       |
       v
UPDATE Inventory
       |
       v
COMMIT
       |
       v
Return connection
```

The connection generally remains associated with the transaction/thread context for the duration required by the transaction.

---

## 14. Connection Pool and Slow Queries

This is another important production problem.

Suppose you have:

```
Pool Size = 10
```

and 10 connections are occupied by slow queries:

```
C1 → Slow Query
C2 → Slow Query
C3 → Slow Query
...
C10 → Slow Query
```

Now a new request arrives:

```
Request 11
     |
     v
Connection Pool
     |
     X
No connection available
     |
     v
WAIT
```

Even if your application has plenty of CPU and memory, requests may be blocked waiting for database connections.

> **This is why slow database queries can cause application-wide latency.**

---

## 15. Connection Pool Exhaustion

A common production problem is:

> **Connection Pool Exhausted**

It can happen because:

### 1. Too many concurrent requests

```
1000 requests
     ↓
Pool = 10
```

### 2. Slow SQL queries

```
Query takes 10 seconds
```

### 3. Database is slow

```
Application
    ↓
Database
    ↓
High CPU / Locking
```

### 4. Connection leak

An application obtains a connection but doesn't properly return it.

Conceptually:

```
Borrow C1
Borrow C2
Borrow C3
...
Never returned
```

Eventually:

```
Pool
C1 ❌
C2 ❌
C3 ❌
...
```

No connections remain.

Modern frameworks and connection pools provide mechanisms to reduce these risks, but application code and configuration still matter.

---

## 16. Connection Pooling and Load Balancer

Now connect this to the topics you've already learned.

Suppose:

```
                         Users
                           |
                           v
                    Load Balancer
                    /     |     \
                   v      v      v
                 App1   App2   App3
                  |       |      |
                 Pool    Pool   Pool
                 10      10     10
                  \       |      /
                   \      |     /
                      Database
```

Each application instance has its own connection pool.

Therefore:

```
App1 → 10
App2 → 10
App3 → 10
```

Potential maximum:

```
30 database connections
```

If you scale to 10 instances:

```
10 instances × 10 connections
= 100 possible connections
```

This is a very common System Design consideration.

---

## 17. Connection Pooling and Horizontal Scaling

This is an important relationship:

```
Horizontal Scaling
       ↓
More Application Instances
       ↓
More Connection Pools
       ↓
More Potential DB Connections
       ↓
Database Can Become Bottleneck
```

For example:

```
             Load Balancer
          /       |       \
         v        v        v
       App1     App2     App3
        |         |        |
      Pool       Pool     Pool
      20          20       20
        \         |        /
         \        |       /
             Database
               🔥
```

> **Adding application servers isn't always free.**

---

## 18. Connection Pooling and Database Replicas

Suppose your system has:

```
                  Application
                       |
                  Connection Pool
                       |
              +--------+--------+
              |                 |
              v                 v
          Primary DB       Read Replica
```

In more advanced architectures, read and write traffic can be separated.

For example:

```
Writes
  |
  v
Primary DB

Reads
  |
  v
Read Replicas
```

The application may use different data sources/pools depending on the architecture.

```
Application
    |
    +------ Write Pool ------> Primary
    |
    +------ Read Pool -------> Replica 1
                              Replica 2
```

This becomes useful in high-scale systems.

---

## 19. Connection Pooling vs Creating Connections

### Without Pooling

```
Request 1
   ↓
Create connection
   ↓
Query
   ↓
Destroy

Request 2
   ↓
Create connection
   ↓
Query
   ↓
Destroy
```

### With Pooling

```
              Connection Pool
            /   /   |   \   \
           C1  C2   C3  C4  C5

Request 1 → Borrow C1 → Query → Return
Request 2 → Borrow C2 → Query → Return
Request 3 → Borrow C1 → Query → Return
```

The second approach avoids repeatedly creating and tearing down connections.

---

## 20. Important Configuration Parameters

Different connection pools use different configuration names, but common concepts include:

### Maximum Pool Size

Maximum number of connections the pool can have.

```
maximumPoolSize = 20
```

### Minimum Idle

Number of connections the pool tries to keep available/idle, depending on pool behavior.

```
minimumIdle = 5
```

### Connection Timeout

How long a request waits for a connection before failing.

```
connectionTimeout = 30 seconds
```

Conceptually:

```
Request
  ↓
No connection available
  ↓
Wait 30 sec
  ↓
Still unavailable
  ↓
Timeout
```

### Idle Timeout

How long an idle connection can remain before being removed, subject to pool behavior.

### Max Lifetime

Maximum lifetime of a connection before the pool retires it.

This can help avoid issues with infrastructure/database-side connection limits.

---

## 21. Don't Make Pool Size Arbitrarily Large

A common beginner mistake is:

> "More connections = more performance."

Not necessarily.

Imagine:

```
Pool = 10
```

Database handles it comfortably.

You change:

```
Pool = 500
```

Now:

```
500 connections
       ↓
Database
       ↓
CPU / memory / locks increase
       ↓
Queries slow down
       ↓
Connections remain occupied longer
       ↓
More requests wait
```

You can end up making the system slower.

So:

> **Connection pool sizing is a capacity-planning problem, not a "bigger is better" setting.**

---

## 22. Interview Question

**Interviewer:**

> "You have 20 Spring Boot instances and each has a connection pool of 20. How many database connections could the application establish?"

**Answer:**

```
20 instances × 20 connections
= 400 connections
```

So:

> Potential maximum = 400 database connections, assuming every pool reaches its configured maximum and the database/network limits permit it.

---

## 23. Another Interview Question

> "Why do we use connection pooling?"

**Good answer:**

> Creating database connections repeatedly is expensive. Connection pooling maintains reusable database connections, allowing application requests to borrow and return connections instead of creating a new connection for every request. This reduces connection setup overhead and improves throughput and latency.

---

## 24. Another Important Interview Question

> "What happens if all connections in the pool are busy?"

```
Request
   |
   v
Connection Pool
   |
   X
No available connection
   |
   v
Wait
   |
   +---- Connection becomes available
   |
   +---- Timeout
```

If a connection becomes available before the timeout:

```
Request → gets connection → executes query
```

Otherwise:

```
Connection acquisition timeout
```

This can eventually result in HTTP errors depending on how the application handles the exception.

---

## 25. Complete Architecture

Let's combine everything you've learned:

```
                         Users
                           |
                           v
                          DNS
                           |
                           v
                    Load Balancer
                    /     |     \
                   /      |      \
                  v       v       v
                App1    App2    App3
                 |       |       |
                 v       v       v
              Pool10   Pool10   Pool10
                 |       |       |
                 +-------+-------+
                         |
                         v
                      Database
```

Now imagine traffic increases:

```
              Load Balancer
           /    |    |    |    \
          v     v    v    v     v
        App1  App2 App3 App4  App5
         |     |    |    |     |
        Pool  Pool Pool Pool  Pool
         10    10   10   10    10
          \     \    |    /     /
           +-----+----+---+----+
                       |
                       v
                   Database
```

Potential connections:

```
5 × 10 = 50
```

Scale to 20 instances:

```
20 × 10 = 200
```

Now your database may become the bottleneck.

That's a key System Design insight:

> **Scaling one layer can create a bottleneck in another layer.**

---

## 26. The Mental Model

Remember Connection Pooling as:

```
                  Request
                     |
                     v
             "Give me a DB
               connection"
                     |
                     v
             +---------------+
             | Connection    |
             | Pool           |
             +---------------+
              /  /  |  \  \
             C1 C2  C3 C4  C5
                     |
                     v
                  Database
```

And the lifecycle:

```
        BORROW
           ↓
     Use Connection
           ↓
    Execute Query
           ↓
        COMMIT
           ↓
        RETURN
           ↓
      Pool Reuses It
```

### One-line definition

> **Connection Pooling** is the practice of maintaining a reusable set of database connections so application requests can borrow and return connections instead of repeatedly creating and destroying them.
