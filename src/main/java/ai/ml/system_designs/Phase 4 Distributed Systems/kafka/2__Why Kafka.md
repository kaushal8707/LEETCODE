# Why Kafka?

The simplest answer is:

> We use Kafka when we need to reliably move a large volume of events between distributed systems, without tightly coupling the systems, while supporting scalability, fault tolerance, and replay.

For system design and interviews, don't memorize "Kafka is fast." Understand what problems Kafka solves.

---

## Problem: Tight coupling between services

Suppose we have an e-commerce system:

```
                Order Service
               /      |       \
              /       |        \
             v        v         v
      Payment     Inventory   Notification
```

When an order is created, Order Service directly calls:

- Payment Service
- Inventory Service
- Notification Service
- Fraud Service
- Analytics Service

This creates tight coupling.

If you add another service:

```
Recommendation Service
```

you need to modify Order Service again.

### With Kafka

```
Order Service
      |
      | OrderCreated
      v
    Kafka
      |
      +----------------+----------------+
      |                |                |
      v                v                v
   Payment        Inventory       Notification
```

Order Service only knows:

> "I need to publish OrderCreated."

It doesn't need to know who consumes it.

**Benefit:** Loose coupling.

---

## Problem: Synchronous communication is blocking

Without Kafka:

```
Order Service
     |
     | HTTP
     v
Payment Service
     |
     | HTTP
     v
Inventory Service
     |
     | HTTP
     v
Notification Service
```

Suppose Notification Service takes 5 seconds.

The overall request can become slow.

### With Kafka

```
Client
   |
   v
Order Service
   |
   | publish event
   v
 Kafka
   |
   +----> Payment
   |
   +----> Inventory
   |
   +----> Notification
```

The Order Service can finish its own transaction without synchronously waiting for every downstream service.

This is useful for asynchronous processing.

---

## Problem: One event needs multiple consumers

Suppose an order is created.

You need:

```
OrderCreated
     |
     +----> Payment
     +----> Inventory
     +----> Email
     +----> Fraud Detection
     +----> Analytics
```

With point-to-point communication, this becomes complicated.

Kafka naturally supports multiple consumer groups:

```
               order-events
                    |
      +-------------+-------------+
      |             |             |
      v             v             v
 Payment Group  Inventory Group  Analytics Group
```

Each consumer group maintains its own position in the Kafka topic.

So the same event can be consumed independently by many applications.

---

## Problem: High volume

Imagine:

```
10,000 orders/sec
```

A single application instance may not be able to process everything.

Kafka uses partitions to distribute work:

```
order-events

Partition 0
Partition 1
Partition 2
Partition 3
Partition 4
Partition 5
```

Consumers can process partitions in parallel:

```
P0 ---> Consumer 1
P1 ---> Consumer 2
P2 ---> Consumer 3
P3 ---> Consumer 4
P4 ---> Consumer 5
P5 ---> Consumer 6
```

This provides horizontal scalability.

---

## Problem: Consumer is temporarily down

This is one of Kafka's major advantages.

Suppose:

```
Order Service
     |
     v
   Kafka
     |
     v
Payment Service
     X
   DOWN
```

The event can remain in Kafka according to the topic's retention policy.

When Payment Service comes back:

```
Kafka
   |
   | old events
   v
Payment Service
```

It can continue consuming from its previous offset.

So Kafka acts as a durable buffer between producers and consumers.

---

## Problem: Need to replay events

This is a very important reason to choose Kafka.

Suppose your consumer processed:

```
Offset 100
Offset 101
Offset 102
...
Offset 10000
```

Later you discover a bug in your application.

You fix the bug and want to process old events again.

Kafka allows consumers to move their position backward and replay historical events, provided those records are still retained.

Before:

```
0  1  2  3 ... 10000
                 ↑
              current
```

After resetting:

```
0  1  2  3 ... 10000
↑
start again
```

This is much harder with a traditional queue where messages are commonly removed after successful consumption.

---

## Problem: Need fault tolerance

Kafka supports replication.

Suppose:

```
Partition 0

Broker 1 ---> Leader
Broker 2 ---> Replica
Broker 3 ---> Replica
```

If Broker 1 fails:

```
Broker 1
   X
 DOWN
```

Kafka can elect another replica as leader.

```
Broker 2 ---> New Leader
Broker 3 ---> Replica
```

So Kafka provides high availability through replication.

---

## Problem: Producer and consumer operate at different speeds

Imagine:

- Producer: 10,000 events/sec
- Consumer: 2,000 events/sec

If the producer directly calls the consumer, the consumer becomes a bottleneck.

Kafka acts as a buffer:

```
Producer
   |
   | 10,000/sec
   v
+----------------+
|     Kafka      |
|                |
|    backlog     |
+----------------+
   |
   | 2,000/sec
   v
Consumer
```

The consumer can gradually catch up.

The difference between produced and consumed data is commonly observed through **consumer lag**.

---

## Problem: Need independent scaling

Suppose:

```
Order Service     100 instances
```

but:

```
Analytics Service  10 instances
```

You don't necessarily want to scale them together.

Kafka provides a buffer and allows each consumer group to scale independently.

```
              Kafka
                |
    +-----------+-----------+
    |                       |
    v                       v
Payment Group         Analytics Group
10 consumers            3 consumers
```

Each can have its own processing capacity.

---

## Kafka provides ordering within a partition

Suppose events for one customer are:

```
Customer 101

OrderCreated
PaymentCompleted
OrderShipped
OrderDelivered
```

If they are written to the same partition:

```
Partition 2

Offset 50 -> OrderCreated
Offset 51 -> PaymentCompleted
Offset 52 -> OrderShipped
Offset 53 -> OrderDelivered
```

Kafka preserves their order within that partition.

This is useful when event order matters.

> **Important:** Kafka does not provide global ordering across all partitions.

---

## Kafka provides durability

Kafka persists records rather than keeping them only in memory.

Conceptually:

```
Producer
   |
   v
 Kafka
   |
   +--> Memory / page cache
   |
   +--> Disk
```

Combined with replication, Kafka can provide durable event storage.

This is why Kafka is more than simply an in-memory message broker.

---

## Kafka is excellent for event-driven architecture

Consider:

```
Customer
   |
   v
Order Service
   |
   | OrderCreated
   v
 Kafka
   |
   +----> Payment
   |
   +----> Inventory
   |
   +----> Shipping
   |
   +----> Notification
   |
   +----> Fraud
   |
   +----> Analytics
```

This architecture has:

- loose coupling
- asynchronous communication
- independent scaling
- durable events
- replay capability
- fault tolerance

That's why Kafka is heavily used in microservices and event-driven systems.

---

## Kafka vs REST

A common interview question is:

> Why Kafka instead of REST?

### REST

```
Service A
   |
   | HTTP request
   v
Service B
```

Good when you need:

- immediate response
- synchronous communication
- request/response
- querying another service

### Kafka

```
Service A
   |
   | Event
   v
 Kafka
   |
   +----> Service B
   +----> Service C
   +----> Service D
```

Good when you need:

- asynchronous communication
- high throughput
- decoupling
- event broadcasting
- buffering
- replay
- independent consumers

So it's not:

> Kafka is better than REST.

It's:

> REST and Kafka solve different communication problems.

---

## Kafka vs traditional message queue

Another important interview question.

### Traditional queue

```
Producer
   |
   v
 Queue
   |
   v
Consumer
   |
   X
Message removed
```

### Kafka

```
Producer
   |
   v
Kafka Topic
   |
   +---- Consumer Group A
   |
   +---- Consumer Group B
   |
   +---- Consumer Group C
```

Kafka stores records for a configured retention period and consumers track their positions using offsets.

This makes replay and multiple independent consumers much easier.

---

## Real-world example

Imagine a banking/payment system.

A payment happens:

```
Payment Service
      |
      | PaymentCompleted
      v
    Kafka
      |
      +----> Fraud Detection
      |
      +----> Notification
      |
      +----> Ledger
      |
      +----> Analytics
      |
      +----> Reporting
```

The Payment Service doesn't need to synchronously call all five systems.

Each system independently consumes the event.

If Analytics is down:

```
Payment
   |
   v
 Kafka
   |
   +----> Fraud         ✓
   +----> Notification  ✓
   +----> Ledger        ✓
   +----> Analytics     ✗
```

Analytics can recover later and consume the events it missed.

---

## So why Kafka?

For an interview, I'd answer:

> Kafka is used when we need a highly scalable, durable, fault-tolerant mechanism for asynchronous communication and event streaming. It decouples producers from consumers, supports multiple independent consumer groups, scales through partitioning, provides durability through replication, and allows consumers to replay events using offsets.

Then give an example:

> For example, when an Order Service publishes an OrderCreated event, Payment, Inventory, Notification, Fraud, and Analytics services can independently consume that event without the Order Service making synchronous calls to each service.

That's a strong 10-year-experience-level interview answer.

---

## The 7 key reasons to remember

```
                    WHY KAFKA?
                        |
      +-----------------+-----------------+
      |                 |                 |
 Scalability        Durability        Decoupling
      |                 |                 |
  Partitions        Replication         Async
      +-----------------+-----------------+
                        |
                  Replayability
                        |
                 High Throughput
                        |
                 Fault Tolerance
```

In one line:

> **Kafka = high-throughput + distributed + durable + scalable + asynchronous event streaming.**

---

The next concept you should understand is **"How Kafka works internally"** — especially:

```
Producer → Broker → Topic → Partition → Log Segment → Disk → Consumer → Offset → Consumer Group
```

That's where you'll understand why Kafka can achieve such high throughput and how it behaves during failures.

