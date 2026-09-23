# Apache Kafka — From Fundamentals to Expert Level

## What is Apache Kafka?

Apache Kafka is a distributed event-streaming platform used to send, store, process, and consume large amounts of data/events in real time.

In simple words:

Kafka is a highly scalable, distributed system that allows applications to communicate asynchronously by publishing and consuming events.

For example, imagine an e-commerce application:

```
Customer places an order
        |
        v
   Order Service
        |
        |  OrderCreated event
        v
      Kafka
        |
        +------------------> Payment Service
        |
        +------------------> Inventory Service
        |
        +------------------> Notification Service
        |
        +------------------> Analytics Service
```

The Order Service doesn't need to directly call every other service.

It publishes an event to Kafka:

```
OrderCreated
{
   "orderId": "ORD-123",
   "customerId": "CUST-456",
   "amount": 2500
}
```

Different services can independently consume that event.

---

## 1. Why was Kafka created?

Traditional applications often communicate synchronously:

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

This creates problems.

### Problem 1 — Tight coupling

Order Service needs to know about Payment, Inventory, Notification, etc.

### Problem 2 — Failure propagation

If Notification Service is down:

```
Order Service
     |
     v
Notification Service
     X
   DOWN
```

The order flow may fail or become slow.

### Problem 3 — Scalability

Suppose:

10,000 orders/sec

The Order Service might need to make thousands of synchronous calls.

### Problem 4 — Data loss

If a downstream service is temporarily unavailable, the event can potentially be lost unless you implement additional persistence/retry mechanisms.

Kafka addresses these problems by introducing a durable event log between producers and consumers.

---

## 2. Kafka's basic architecture

The most important concepts are:

```
Producer
   |
   v
Kafka Cluster
   |
   +--> Topic
   |      |
   |      +--> Partition 0
   |      +--> Partition 1
   |      +--> Partition 2
   |
   v
Consumer
```

You need to understand these components:

- Producer
- Consumer
- Broker
- Kafka Cluster
- Topic
- Partition
- Offset
- Consumer Group
- Replication
- Leader/Follower

These are the foundation of Kafka.

---

## 3. Producer

A producer is an application that sends events/messages to Kafka.

For example:

```
Order Service
     |
     | OrderCreated
     v
    Kafka
```

The Order Service is the Kafka Producer.

Example:

```java
kafkaTemplate.send(
    "order-events",
    orderId,
    orderCreatedEvent
);
```

The producer sends the event to a Kafka topic.

---

## 4. Topic

A topic is a logical category/name where Kafka stores events.

For example:

- order-events
- payment-events
- user-events
- notification-events

Think of a topic like a named stream:

```
Topic: order-events

OrderCreated
OrderCreated
OrderCancelled
OrderCreated
OrderShipped
```

A producer writes events to a topic.

Consumers read events from a topic.

---

## 5. Partition — VERY IMPORTANT

A Kafka topic is divided into partitions.

For example:

```
Topic: order-events

Partition 0
-------------------------
Order 101
Order 104
Order 107
Order 110


Partition 1
-------------------------
Order 102
Order 105
Order 108
Order 111


Partition 2
-------------------------
Order 103
Order 106
Order 109
Order 112
```

Why?

Because partitions allow Kafka to scale horizontally.

Instead of having one machine handle everything:

```
              Topic
                |
       -------------------
       |        |        |
       P0       P1       P2
```

Different Kafka brokers can host different partitions.

---

## 6. Broker

A Kafka broker is a Kafka server.

For example:

```
Kafka Cluster

Broker 1
Broker 2
Broker 3
Broker 4
```

Each broker stores partitions.

Example:

```
Broker 1
   |
   +-- order-events-0
   +-- payment-events-1

Broker 2
   |
   +-- order-events-1
   +-- payment-events-0

Broker 3
   |
   +-- order-events-2
   +-- user-events-0
```

A collection of Kafka brokers is called a Kafka cluster.

---

## 7. Kafka Cluster

A Kafka cluster is multiple Kafka brokers working together.

For example:

```
                Kafka Cluster
                     |
        +------------+------------+
        |            |            |
     Broker 1     Broker 2     Broker 3
        |            |            |
       P0           P1           P2
```

Having multiple brokers provides:

- scalability
- fault tolerance
- high availability
- parallel processing

---

## 8. Offset

Every message inside a partition has an offset.

For example:

```
Partition 0

Offset       Event

  0       OrderCreated
  1       OrderCreated
  2       OrderCancelled
  3       OrderCreated
  4       OrderShipped
```

Offset 3 identifies a particular position in that partition.

Kafka consumers use offsets to know:

"Which message have I already processed?"

This is one of the most important concepts in Kafka.

---

## 9. Consumer

A consumer reads events from Kafka.

For example:

```
Kafka
  |
  | OrderCreated
  v
Payment Service
```

Payment Service is the Kafka Consumer.

It receives:

```json
{
   "orderId": "ORD-123",
   "amount": 2500
}
```

and processes the event.

---

## 10. Consumer Group

This is another very important Kafka concept.

Suppose:

```
Topic: order-events

Partition 0
Partition 1
Partition 2
Partition 3
```

And you have:

```
Consumer Group: payment-service

Consumer 1
Consumer 2
Consumer 3
```

Kafka distributes partitions among consumers:

```
Partition 0 ---> Consumer 1
Partition 1 ---> Consumer 2
Partition 2 ---> Consumer 3
Partition 3 ---> Consumer 1
```

This allows parallel processing.

### Important rule

Within a consumer group:

One partition is assigned to only one consumer at a time.

So if you have:

- 4 partitions
- 3 consumers

you can have:

```
Consumer 1 -> P0
Consumer 2 -> P1
Consumer 3 -> P2 + P3
```

But if:

- 4 partitions
- 6 consumers

then:

```
Consumer 1 -> P0
Consumer 2 -> P1
Consumer 3 -> P2
Consumer 4 -> P3
Consumer 5 -> idle
Consumer 6 -> idle
```

Therefore:

Maximum parallelism within a consumer group is generally bounded by the number of partitions.

---

## 11. Kafka can have multiple consumer groups

This is extremely powerful.

Suppose:

```
                    Kafka
                      |
                order-events
                      |
          +-----------+-----------+
          |           |           |
          v           v           v
     Payment CG   Inventory CG  Notification CG
```

Each consumer group can independently consume the same event.

For example:

```
OrderCreated
     |
     +----> Payment Service
     |
     +----> Inventory Service
     |
     +----> Notification Service
```

The event isn't removed when Payment Service reads it.

Inventory Service can still read it.

Notification Service can also read it.

This is fundamentally different from many traditional message queues.

---

## 12. Kafka doesn't normally delete a message after consumption

This is a common misconception.

Suppose Kafka has:

```
Topic: order-events

P0

Offset 0 -> OrderCreated
Offset 1 -> OrderCreated
Offset 2 -> OrderCancelled
Offset 3 -> OrderCreated
```

Consumer reads offset 0.

Kafka doesn't say:

```
DELETE offset 0
```

Instead, Kafka keeps the record according to its retention configuration.

For example:

```
Retention = 7 days
```

After the retention period, Kafka can delete old records.

This allows consumers to potentially replay historical events.

---

## 13. Kafka vs traditional message queue

A traditional queue often behaves conceptually like:

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
message removed
```

Kafka behaves more like:

```
Producer
   |
   v
Kafka Log
   |
   +---- Consumer Group A
   |
   +---- Consumer Group B
   |
   +---- Consumer Group C
```

Each consumer group maintains its own position.

That's one of Kafka's biggest strengths.

---

## 14. How Kafka achieves high throughput

Suppose you have:

1 million events/sec

A single server may not be enough.

Kafka partitions the data:

```
                  Topic
                    |
       +------------+------------+
       |            |            |
      P0           P1           P2
       |            |            |
   Broker 1      Broker 2      Broker 3
```

Now processing/storage can happen in parallel.

This is the fundamental scalability model:

```
Partitioning
     +
Parallel processing
     +
Multiple brokers
     =
High throughput
```

---

## 15. Replication

Kafka also provides fault tolerance through replication.

Suppose:

```
Partition 0

Leader
  |
  +---- Replica 1
  |
  +---- Replica 2
```

Or across brokers:

```
Broker 1       Broker 2       Broker 3

 P0 Leader     P0 Replica     P0 Replica
```

If Broker 1 fails:

```
Broker 1
   X
 DOWN
```

Kafka can elect another replica as leader.

```
Broker 2
   |
   +-- P0 becomes Leader
```

Therefore Kafka can continue operating despite broker failures.

---

## 16. Kafka message flow

Let's put everything together.

Suppose a customer places an order.

### Step 1 — Application creates order

```
Customer
   |
   v
Order Service
```

### Step 2 — Producer publishes event

```
Order Service
      |
      | OrderCreated
      v
Kafka Producer
```

### Step 3 — Kafka determines partition

```
                order-events
                     |
        +------------+------------+
        |            |            |
       P0           P1           P2
```

Kafka chooses a partition based on things such as the record key.

For example:

```
key = customerId
```

can help ensure events for the same customer go to the same partition.

### Step 4 — Kafka stores the event

```
P1

Offset 100 -> OrderCreated
```

### Step 5 — Consumer polls Kafka

```
Payment Service
       |
       | poll()
       v
Kafka
```

### Step 6 — Consumer processes event

```
OrderCreated
      |
      v
Payment Service
      |
      v
Process Payment
```

### Step 7 — Consumer commits offset

```
Offset = 100
```

Kafka now knows the consumer group's progress.

---

## 17. Why Kafka is so popular in microservices

Imagine 20 microservices:

- Order
- Payment
- Inventory
- Shipping
- Notification
- Customer
- Fraud
- Analytics
- Recommendation
- ...

Without Kafka:

```
Order
 ├── HTTP → Payment
 ├── HTTP → Inventory
 ├── HTTP → Notification
 ├── HTTP → Fraud
 └── HTTP → Analytics
```

This creates a lot of coupling.

With Kafka:

```
                 Kafka
                   |
            order-events
                   |
       +-----------+-----------+
       |           |           |
    Payment    Inventory   Notification
       |
     Fraud
```

Order Service simply publishes:

```
OrderCreated
```

Other services decide whether they care about it.

This is a major building block for Event-Driven Architecture.

---

## 18. Kafka is not just a message queue

A better mental model is:

Kafka is a distributed, durable, partitioned event log.

It can be used for:

### Event-driven microservices

```
Order → Kafka → Payment
```

### Log aggregation

```
Application servers
      |
      v
    Kafka
      |
      v
Log processing
```

### Data pipelines

```
Database
   |
   v
 Kafka
   |
   +--> Data Warehouse
   |
   +--> Analytics
   |
   +--> Search
```

### Real-time analytics

```
Transactions
     |
     v
   Kafka
     |
     v
Stream Processing
     |
     v
Dashboard
```

---

## 19. Kafka's core architecture — remember this

For interviews, keep this picture in your mind:

```
                         KAFKA CLUSTER
      ------------------------------------------------

       Broker 1       Broker 2       Broker 3
          |               |               |
          |               |               |
       Partition 0     Partition 1     Partition 2
          |               |               |
          |               |               |
          +---------------+---------------+
                          |
                       Topic
                    order-events

Producer
   |
   | publish
   v
 Topic
   |
   +---------------- Consumer Group A
   |                    |
   |                    +-- Consumer 1
   |                    +-- Consumer 2
   |
   +---------------- Consumer Group B
                        |
                        +-- Consumer 1
                        +-- Consumer 2
```

And each partition looks like:

```
Partition 0

+-----+-----+-----+-----+-----+
| 100 | 101 | 102 | 103 | 104 |
+-----+-----+-----+-----+-----+
   ↑
 Offset
```

---

## 20. The Kafka concepts you should learn next

Since you have 10 years of experience and are learning Kafka from the fundamentals toward expert/interview level, I would learn it in this order:

### Level 1 — Fundamentals

- What is Kafka?
- Why Kafka?
- Kafka architecture
- Producer
- Consumer
- Broker
- Cluster
- Topic
- Partition
- Offset

### Level 2 — Core Kafka

- Producer internals
- Consumer internals
- Consumer Groups
- Partition assignment
- Rebalancing
- Message ordering
- Keys
- Replication
- Leader/Follower
- ISR
- High Watermark

### Level 3 — Reliability

- acks=0
- acks=1
- acks=all
- Producer retries
- Idempotent producer
- At-most-once
- At-least-once
- Exactly-once
- Transactions

### Level 4 — Performance

- Batching
- Compression
- linger.ms
- batch.size
- buffer.memory
- Consumer fetch behavior
- Parallelism
- Partition strategy
- Throughput vs latency

### Level 5 — Internal Architecture

- Kafka log structure
- Segment files
- Index files
- Log retention
- Log compaction
- Page cache
- Zero-copy
- Network threads
- I/O threads
- Controller
- KRaft

### Level 6 — Advanced

- Consumer rebalancing
- Cooperative rebalancing
- Static membership
- Exactly-once semantics
- Kafka transactions
- Schema Registry
- Avro
- Protobuf
- Kafka Streams
- Kafka Connect

### Level 7 — Production / System Design

- How to choose partitions
- How to choose partition key
- Kafka cluster sizing
- Replication factor
- Failure scenarios
- Consumer lag
- Monitoring
- Dead-letter topics
- Retry topics
- Poison messages
- Backpressure
- Disaster recovery
- Multi-region Kafka
- Security
- Kafka system-design interview questions

---

## The most important mental model

If you remember only one thing initially, remember this:

```
                 PRODUCER
                     |
                     | event
                     v
              +--------------+
              |    TOPIC      |
              +--------------+
                     |
          +----------+----------+
          |          |          |
         P0         P1         P2
          |          |          |
          +----------+----------+
                     |
                 CONSUMER
                     |
                     v
                  SERVICE
```

- **Topic** = logical stream
- **Partition** = unit of parallelism
- **Offset** = position in a partition
- **Producer** = writes events
- **Consumer** = reads events
- **Consumer Group** = enables parallel consumption
- **Broker** = Kafka server
- **Cluster** = collection of brokers
- **Replication** = fault tolerance

Once these concepts are clear, the next important step is Kafka internals: exactly what happens from `producer.send()` until the message is written to disk and later fetched by a consumer. That is where Kafka starts becoming interesting at a 10-year-experience interview level.

