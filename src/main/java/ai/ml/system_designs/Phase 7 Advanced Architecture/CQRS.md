# CQRS — Command Query Responsibility Segregation

CQRS is an architectural pattern where we separate operations that change data (Commands) from operations that read data (Queries).

The simplest definition is:

> Commands change state. Queries return state.

Instead of having one model/database structure responsible for both reads and writes, CQRS allows us to optimize the write side and read side independently.

---

## 1. Traditional Approach — Without CQRS

Suppose we have an e-commerce application.

```
                    Client
                       │
                       ▼
                Order Service
                       │
                       ▼
                  Order Model
                       │
                       ▼
                  Order DB
```

Both operations use the same model/database:

```
Create Order  ──────►
Update Order  ──────►     Order DB
Get Order     ◄──────
Search Orders ◄──────
```

This is perfectly fine for many applications.

But imagine our requirements become:

- millions of order reads
- complex reporting
- different read views
- high write volume
- different scaling requirements for reads and writes

A single model can become a bottleneck.

---

## 2. CQRS Approach

CQRS separates the system into:

```
                 Client
                    │
             ┌──────┴──────┐
             │             │
          Command         Query
             │             │
             ▼             ▼
       Command Side     Query Side
             │             │
             ▼             ▼
        Write Model     Read Model
             │             │
             ▼             ▼
        Write DB       Read DB
```

So:

```
Command → Write side
Query   → Read side
```

That's the core idea.

---

## 3. What is a Command?

A Command asks the system to perform an operation that changes state.

Examples:

- CreateOrder
- UpdateOrder
- CancelOrder
- ProcessPayment
- ReserveInventory
- ChangeAddress

Commands usually have imperative names:

```
CreateOrder
CancelOrder
UpdateCustomer
```

rather than:

```
OrderCreated
OrderCancelled
```

because those are events.

---

## 4. What is a Query?

A Query asks for data without changing system state.

Examples:

- GetOrder
- GetCustomer
- SearchProducts
- GetOrderHistory
- GetAccountBalance
- GetDashboard

The important rule is:

> A query should not modify business state.

---

## 5. Command vs Query vs Event

This is extremely important.

| Concept | Meaning | Example |
|---|---|---|
| Command | Do something | CreateOrder |
| Query | Give me something | GetOrder |
| Event | Something happened | OrderCreated |

Think:

```
Command
   │
   ▼
"Please create this order."
   │
   ▼
System changes state
   │
   ▼
Event
   │
   ▼
"OrderCreated happened."
```

---

## 6. Simple CQRS Example

Suppose:

```
POST /orders
```

This is a command.

```
CreateOrder
     │
     ▼
Command Handler
     │
     ▼
Validate
     │
     ▼
Order Aggregate
     │
     ▼
Write DB
```

Then:

```
GET /orders/1001
```

is a query.

```
GetOrder
    │
    ▼
Query Handler
    │
    ▼
Read DB
    │
    ▼
Order Response
```

---

## 7. Why Separate Read and Write?

Because read workloads and write workloads are often very different.

Imagine Amazon-like e-commerce:

```
10,000 writes/sec
```

but:

```
1,000,000 reads/sec
```

If both use the same database:

```
                 ┌─────────────┐
Writes ─────────►│             │
                 │   Database  │
Reads  ─────────►│             │
                 └─────────────┘
```

Read traffic can affect write performance.

With CQRS:

```
             ┌──────────────┐
Writes ────► │  Write DB    │
             └──────────────┘


             ┌──────────────┐
Reads  ────► │  Read DB     │
             └──────────────┘
```

Now they can scale independently.

---

## 8. Real-Time Example — E-Commerce

Imagine Amazon-like order management.

The write model might need:

- Order
- Customer
- Payment
- Inventory
- OrderStatus

But the UI needs:

- Order ID
- Customer Name
- Product Name
- Quantity
- Payment Status
- Shipping Status
- Total Amount

For every request, joining many tables can become expensive.

Instead, create a read-optimized model:

```
OrderReadModel

orderId
customerName
productName
quantity
paymentStatus
shippingStatus
totalAmount
```

Now:

```
GET /orders/1001
```

can be extremely fast.

---

## 9. CQRS Architecture

A more realistic architecture:

```
                         Client
                           │
                 ┌─────────┴─────────┐
                 │                   │
              Command              Query
                 │                   │
                 ▼                   ▼
         ┌──────────────┐    ┌──────────────┐
         │Command       │    │Query         │
         │Handler       │    │Handler       │
         └──────┬───────┘    └──────┬───────┘
                │                   │
                ▼                   ▼
         ┌──────────────┐    ┌──────────────┐
         │Write Model   │    │Read Model    │
         └──────┬───────┘    └──────┬───────┘
                │                   │
                ▼                   ▼
          Write Database       Read Database
```

The read and write databases may be different databases, but they don't have to be.

That distinction is important.

---

## 10. CQRS Does NOT Require Two Databases

A common misconception is:

> "CQRS means two databases."

No.

CQRS is fundamentally about separating command and query responsibilities.

You can have:

### Level 1 — Logical separation

```
Command Handler
       │
       ▼
     DB
       ▲
       │
Query Handler
```

One database, separate models/handlers.

### Level 2 — Separate read/write models

```
Command
   │
   ▼
Write Model
   │
   ▼
Database

Query
   │
   ▼
Read Model
   │
   ▼
Database
```

### Level 3 — Separate databases

```
Command
   │
   ▼
Write DB

Query
   │
   ▼
Read DB
```

The last approach gives the most flexibility but also introduces more complexity.

---

## 11. CQRS + Event-Driven Architecture

This is where CQRS becomes particularly powerful.

A common architecture is:

```
                         Client
                           │
                           │ Command
                           ▼
                    ┌──────────────┐
                    │ Command Side │
                    └──────┬───────┘
                           │
                           ▼
                       Write DB
                           │
                           │ Event
                           ▼
                    ┌──────────────┐
                    │    Kafka     │
                    └──────┬───────┘
                           │
             ┌─────────────┼─────────────┐
             │             │             │
             ▼             ▼             ▼
        Read Model     Analytics      Search
        Consumer       Consumer       Consumer
             │
             ▼
          Read DB
             ▲
             │
             │ Query
             │
           Client
```

This is a very common CQRS + Event-Driven Architecture combination.

---

## 12. Step-by-Step Example

Suppose user creates:

```
Order #1001
```

### Step 1 — Command

Client sends:

```
POST /orders
```

Conceptually:

```
CreateOrder
```

### Step 2 — Command Handler

```
CreateOrder
      │
      ▼
Command Handler
      │
      ▼
Validate request
      │
      ▼
Business rules
      │
      ▼
Save Order
```

### Step 3 — Event

After successful state change:

```
OrderCreated
```

is published.

```
Write DB
   │
   ▼
OrderCreated
   │
   ▼
Kafka
```

### Step 4 — Read Model Consumer

A consumer receives:

```
OrderCreated
```

and updates the read model:

```
Read DB

OrderReadModel
----------------------------
orderId       = 1001
customerName  = Kaushal
status        = CREATED
totalAmount   = 2500
```

### Step 5 — Query

The UI requests:

```
GET /orders/1001
```

The query side reads:

```
Read DB
   │
   ▼
OrderReadModel
   │
   ▼
Response
```

---

## 13. Why Kafka Fits CQRS

Kafka provides a durable event stream between the write and read sides.

```
Write Side
    │
    │ OrderCreated
    ▼
 Kafka Topic
    │
    ▼
Read Model Consumer
    │
    ▼
Read Database
```

Suppose the read database crashes.

After recovering, the consumer can potentially process retained events and rebuild the read model.

This is one of the major advantages of event-driven CQRS.

---

## 14. Multiple Read Models

This is one of the strongest CQRS concepts.

The same events can create different read models.

```
                    OrderCreated
                         │
                         ▼
                       Kafka
                         │
          ┌──────────────┼──────────────┐
          ▼              ▼              ▼
      Order View     Dashboard View   Search View
          │              │              │
          ▼              ▼              ▼
       Read DB        Read DB         Elasticsearch
```

Each read model is optimized for a different use case.

For example:

Order details

```
OrderReadModel
```

Dashboard

```
OrderDashboardModel
```

Search

```
OrderSearchDocument
```

The write model doesn't need to change just because a new read requirement appears.

---

## 15. Polyglot Persistence

CQRS can allow different databases for different workloads.

For example:

```
                  Events
                    │
          ┌─────────┼──────────┐
          ▼         ▼          ▼
       MySQL      Redis    Elasticsearch
          │         │          │
       Orders    Dashboard    Search
```

You might use:

- Write side → PostgreSQL
- Read side  → Elasticsearch
- Cache      → Redis
- Analytics  → Data warehouse

This is called polyglot persistence.

---

## 16. Eventual Consistency in CQRS

CQRS with asynchronous events usually introduces eventual consistency.

For example:

```
10:00:00.000
Command received

10:00:00.010
Order saved

10:00:00.020
OrderCreated published

10:00:00.030
Read model updated
```

Between:

```
Order saved
```

and:

```
Read model updated
```

the two sides may temporarily disagree.

For example:

```
Write DB:
Order 1001 = CREATED

Read DB:
Order 1001 = NOT FOUND
```

A moment later:

```
Write DB:
Order 1001 = CREATED

Read DB:
Order 1001 = CREATED
```

This is eventual consistency.

---

## 17. CQRS and Transactional Outbox

Remember the problem:

```
Save Order → SUCCESS
Publish Event → FAILURE
```

If CQRS relies on events to update the read model, this is dangerous.

You could have:

```
Write DB
   │
   ├── Order saved ✅
   │
   └── Event missing ❌
```

Then the read model never gets updated.

A common solution:

```
                 Transaction
                     │
            ┌────────┴────────┐
            ▼                 ▼
       Order Table       Outbox Table
            │                 │
            └────────┬────────┘
                     │
                     ▼
                Outbox Relay
                     │
                     ▼
                   Kafka
                     │
                     ▼
                Read Model
```

This connects the concepts you've already studied:

```
CQRS
 +
Event-Driven Architecture
 +
Transactional Outbox
 +
Kafka
 =
Powerful distributed architecture
```

---

## 18. CQRS and Saga

CQRS can also work with Saga.

For example:

```
CreateOrder
     │
     ▼
Order Service
     │
     ▼
OrderCreated
     │
     ▼
Payment Service
     │
     ▼
PaymentCompleted
     │
     ▼
Inventory Service
     │
     ▼
InventoryReserved
```

If inventory fails:

```
InventoryReservationFailed
            │
            ▼
      Compensating Action
            │
            ▼
       Refund Payment
            │
            ▼
        Cancel Order
```

CQRS separates the commands and queries, while Saga coordinates the distributed business transaction.

---

## 19. CQRS + Event Sourcing

These are related but not the same thing.

### CQRS

Separates:

```
Command Model
Query Model
```

### Event Sourcing

Stores state changes as events:

```
OrderCreated
ItemAdded
PaymentCompleted
OrderShipped
```

instead of only storing the latest state.

They can be combined:

```
             Command
                │
                ▼
          Domain Model
                │
                ▼
        Event Store
                │
                ▼
       OrderCreated
       PaymentCompleted
       OrderShipped
                │
                ▼
              Kafka
                │
        ┌───────┼────────┐
        ▼       ▼        ▼
      Read    Search   Analytics
      Model
```

But:

```
CQRS ≠ Event Sourcing
```

and:

```
EDA ≠ Event Sourcing
```

They are independent concepts that can be combined.

---

## 20. CQRS Without Event Sourcing

You can have:

```
Command
   ↓
Write DB
   ↓
Read DB
```

without storing every state change as an event.

For example:

```
Orders table
-------------------
1001 | CREATED
1002 | SHIPPED
1003 | CANCELLED
```

That's CQRS without Event Sourcing.

---

## 21. CQRS With Event Sourcing

Instead:

```
Order 1001

Events:
--------------------------
OrderCreated
ItemAdded
PaymentCompleted
OrderShipped
```

Current state is derived from events.

```
Events
  │
  ▼
Replay
  │
  ▼
Current State
```

This gives powerful audit/history capabilities but adds significant complexity.

---

## 22. CQRS Scaling

Suppose:

```
Writes = 10,000/sec
Reads  = 1,000,000/sec
```

Traditional architecture:

```
             ┌──────────────┐
Writes ─────►│              │
Reads  ─────►│   Database   │
             │              │
             └──────────────┘
```

CQRS:

```
                ┌──────────────┐
10K writes ────►│  Write Side  │
                └──────┬───────┘
                       │
                     Events
                       │
                       ▼
                ┌──────────────┐
                │ Read Models  │
                └──────┬───────┘
                       │
                       ▼
                  1M reads/sec
```

Read infrastructure can be scaled independently.

---

## 23. CQRS and Caching

CQRS can work very well with Redis.

```
Client
  │
  │ Query
  ▼
Query Service
  │
  ├──► Redis
  │
  └──► Read DB
```

Flow:

```
Query
 │
 ▼
Redis
 │
 ├── HIT → Return
 │
 └── MISS
       │
       ▼
    Read DB
       │
       ▼
    Redis
       │
       ▼
    Return
```

This is especially useful for high-read systems.

---

## 24. CQRS vs CRUD

Traditional CRUD:

```
Create
Read
Update
Delete
```

often operates on the same model.

```
                 User
                  │
                  ▼
             User Model
                  │
                  ▼
              Database
```

CQRS:

```
               Client
              /      \
             /        \
        Commands      Queries
           │             │
           ▼             ▼
       Write Model    Read Model
           │             │
           ▼             ▼
       Write DB       Read DB
```

CRUD is simpler.

CQRS is more flexible but more complex.

---

## 25. When Should You Use CQRS?

CQRS is useful when you have:

### 1. Very different read/write workloads

```
Reads >>> Writes
```

or vice versa.

### 2. Complex read requirements

Multiple joins and aggregations make the transactional model inefficient for reads.

### 3. Multiple read views

```
Mobile View
Web View
Admin View
Reporting View
Search View
```

### 4. Independent scaling

Read infrastructure needs to scale separately.

### 5. Event-driven architecture

Events naturally update read models.

### 6. Complex domains

When write-side business rules are significantly different from read-side requirements.

### 7. Audit/history requirements

Especially when combined with Event Sourcing.

---

## 26. When Should You NOT Use CQRS?

This is equally important.

Don't use CQRS just because:

> "We are building microservices."

For a simple application:

```
User
  │
  ▼
Service
  │
  ▼
PostgreSQL
```

CRUD may be enough.

CQRS adds:

- Command handlers
- Query handlers
- Read models
- Synchronization
- Events
- Messaging
- Monitoring
- Eventual consistency
- Retries
- Idempotency

If you don't need those capabilities, CQRS can be unnecessary complexity.

---

## 27. Common CQRS Challenges

### 1. Eventual consistency

Read model may lag behind write model.

### 2. Synchronization

You need reliable propagation from write side to read side.

### 3. Duplicate events

Consumers must be idempotent.

### 4. Event ordering

Some business operations depend on order.

### 5. Schema evolution

Events and read models evolve over time.

### 6. Rebuilding read models

If the read model is corrupted, you need a strategy to rebuild it.

### 7. Operational complexity

More components mean more things to monitor.

---

## 28. A Very Important Interview Question

> "Why would you use CQRS?"

A strong answer:

> "CQRS is useful when the read and write workloads or models have significantly different requirements. It separates commands that change state from queries that retrieve state, allowing independent optimization and scaling. In an event-driven implementation, domain events from the write side can asynchronously update one or more read-optimized models. The trade-offs are increased complexity, eventual consistency, synchronization, idempotency, ordering, and operational overhead."

---

## 29. CQRS vs EDA vs Event Sourcing

This distinction is very important for a senior system-design interview.

| Concept | Main Purpose |
|---|---|
| CQRS | Separate reads and writes |
| EDA | Communicate using events |
| Event Sourcing | Store state changes as events |
| Saga | Manage distributed transactions |
| Transactional Outbox | Reliably publish DB changes as events |
| Kafka | Event streaming/broker technology |

They can be combined:

```
                    CQRS
                     │
        ┌────────────┴────────────┐
        │                         │
    Command Side              Query Side
        │                         ▲
        ▼                         │
    Write DB                     │
        │                         │
        ▼                         │
Transactional Outbox             │
        │                         │
        ▼                         │
      Kafka ──────────────────────┘
        │
        ▼
    Domain Events
```

---

## 30. Complete Real-World Architecture

Putting together the concepts you've been learning:

```
                           Client
                             │
                             ▼
                       API Gateway
                             │
                  ┌──────────┴──────────┐
                  │                     │
             Commands                Queries
                  │                     │
                  ▼                     ▼
           Command Service         Query Service
                  │                     │
                  ▼                     ▼
              Write DB              Read DB
                  │
                  │ Transaction
                  ▼
             Outbox Table
                  │
                  ▼
             Outbox Relay
                  │
                  ▼
                Kafka
                  │
       ┌──────────┼──────────┐
       │          │          │
       ▼          ▼          ▼
   Read Model   Search    Analytics
    Consumer    Consumer    Consumer
       │          │
       ▼          ▼
    Read DB   Elasticsearch
```

And the flow is:

```
Client
  │
  │ CreateOrder
  ▼
Command Side
  │
  ▼
Write DB
  │
  ▼
Outbox
  │
  ▼
Kafka
  │
  ▼
OrderCreated
  │
  ├────────► Read Model
  │
  ├────────► Search
  │
  ├────────► Analytics
  │
  └────────► Notification

Client
  │
  │ GetOrder
  ▼
Query Side
  │
  ▼
Read DB
  │
  ▼
Response
```

---

## 31. The Mental Model

Remember CQRS with this simple picture:

```
             CQRS
              │
       ┌──────┴──────┐
       │             │
    COMMAND        QUERY
       │             │
       ▼             ▼
   Change State    Read State
       │             │
       ▼             ▼
   Write Model    Read Model
       │             │
       ▼             ▼
   Write DB       Read DB
       │
       │ Events
       ▼
     Kafka
       │
       ▼
   Read Models
```

### One-line definitions

**Command:**

> "Please change something."

**Query:**

> "Please give me something."

**Event:**

> "Something already happened."

**CQRS:**

> Separate the model used to change state from the model used to read state.

