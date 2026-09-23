# Kafka Compression

**Kafka compression** means compressing records before they are sent to Kafka, so that Kafka needs to transfer and store less data.

The basic flow is:

```
Producer
   |
   | Compress
   v
Compressed Record Batch
   |
   v
Kafka Broker
   |
   | Store compressed data
   v
Kafka Log
   |
   v
Consumer
   |
   | Decompress
   v
Original Records
```

The important idea is:

> **Compression reduces network bandwidth and disk usage, but costs CPU.**

---

## 1. Why does Kafka need compression?

Imagine your producer sends:

```
OrderCreated
OrderCreated
OrderCreated
OrderCreated
...
```

Suppose the producer generates:

```
100 MB/sec
```

Without compression:

```
Producer
   |
   | 100 MB/sec
   v
Kafka
   |
   | Replication
   v
Followers
```

With compression, suppose the data compresses to 25 MB/sec:

```
Producer
   |
   | 25 MB/sec
   v
Kafka
```

You potentially save:

```
75 MB/sec network traffic
```

and Kafka can store substantially less data on disk.

---

## 2. Kafka compression algorithms

Kafka commonly supports:

| Compression | CPU | Compression Ratio | Speed |
|---|---|---|---|
| `none` | Very low | None | Fastest |
| `gzip` | High | Good | Slower |
| `snappy` | Low | Moderate | Fast |
| `lz4` | Low/Moderate | Good | Very fast |
| `zstd` | Moderate/High | Excellent | Good |

The commonly considered choices are:

- LZ4
- ZSTD
- Snappy
- GZIP

For modern systems, **LZ4** and **ZSTD** are particularly common choices, depending on the CPU/network/storage trade-off.

---

## 3. Producer configuration

Compression is primarily configured on the producer:

```
compression.type=lz4
```

or:

```
compression.type=zstd
```

or:

```
compression.type=snappy
```

or:

```
compression.type=gzip
```

Without compression:

```
compression.type=none
```

---

## 4. Where does compression happen?

This is an important interview question.

The producer generally compresses **record batches**, not each individual message independently.

Think:

```
Producer
   |
   +-- Record A
   +-- Record B
   +-- Record C
   +-- Record D
   |
   v
Record Batch
   |
   | Compress
   v
Compressed Batch
   |
   v
Kafka
```

This is important because compression works much better when multiple similar records are grouped together.

---

## 5. Why batch compression is efficient

Suppose you have:

```
Record A:
OrderCreated customerId=100

Record B:
OrderCreated customerId=101

Record C:
OrderCreated customerId=102
```

There is a lot of repeated structure:

- `OrderCreated`
- `customerId`
- JSON field names
- timestamps
- etc.

Compressing the entire batch allows the compression algorithm to exploit this repetition.

Conceptually:

```
Individual compression:

A → compress
B → compress
C → compress

Less efficient


Batch compression:

A + B + C
     ↓
  Compress
     ↓
Better compression
```

---

## 6. Kafka Record Batch

Kafka producers don't normally send every record as an individual network request.

Instead:

```
Application
   |
   +-- Record 1
   +-- Record 2
   +-- Record 3
   +-- Record 4
   |
   v
Producer Batch
   |
   v
Compress
   |
   v
ProduceRequest
   |
   v
Broker
```

This is one reason Kafka can achieve high throughput.

---

## 7. Compression and `batch.size`

Two producer settings are closely related:

```
batch.size=...
linger.ms=...
```

### `batch.size`

Controls the maximum size of a producer batch for a partition.

### `linger.ms`

Controls how long the producer may wait for additional records so that the batch can become larger.

Conceptually:

```
Records arrive
     |
     v
Producer Batch
     |
     | wait briefly
     | or batch becomes full
     v
Compress
     |
     v
Send to Kafka
```

Larger/more populated batches often improve compression efficiency.

---

## 8. Compression and `linger.ms`

Suppose records arrive:

```
t=0ms → A
t=1ms → B
t=2ms → C
t=3ms → D
```

If:

```
linger.ms=0
```

the producer may send batches sooner.

With a small positive linger:

```
linger.ms=5
```

the producer can potentially collect more records:

```
A + B + C + D
       ↓
   One batch
       ↓
   Compress
```

This can improve:

- batching
- compression ratio
- network efficiency
- throughput

But it can increase latency.

---

## 9. Compression trade-off

Compression is not free.

You get:

```
Compression
    |
    +---- Lower network usage
    |
    +---- Lower disk usage
    |
    +---- Potentially higher throughput
    |
    +---- More CPU usage
```

So the trade-off is:

```
Network / Disk
      ↕
    CPU
      ↕
Latency
```

---

## 10. Compression and replication

This is particularly important in Kafka.

Suppose:

```
Replication Factor = 3
```

You have:

```
Leader
 /    \
v      v
F1     F2
```

If the producer sends compressed batches, Kafka can replicate the compressed representation efficiently.

Conceptually:

```
Producer
   |
   | compressed batch
   v
Leader
   |
   +---- compressed data ----> Follower 1
   |
   +---- compressed data ----> Follower 2
```

This means compression can significantly reduce the amount of data moved through the Kafka cluster.

---

## 11. Compression reduces network traffic

Imagine:

```
1 GB/sec
```

of uncompressed application data.

If compression achieves:

```
4:1
```

compression ratio:

```
1 GB
 ↓
250 MB
```

So the producer-to-broker network traffic could be dramatically reduced.

And with:

```
RF = 3
```

replication also moves less data over the network compared with sending the full uncompressed representation.

Actual savings depend heavily on the data.

---

## 12. Compression reduces disk usage

Suppose Kafka receives:

```
100 GB/day
```

and the compressed representation is:

```
30 GB/day
```

Kafka's log storage can be significantly smaller.

This is especially valuable for:

- High-volume topics
- Event streams
- Log aggregation
- Telemetry
- Analytics events

However, don't assume a fixed compression ratio. JSON, Avro, Protobuf, random data, encrypted payloads, and already-compressed data behave very differently.

---

## 13. Compression and Consumer CPU

The consumer eventually needs the original records.

So:

```
Producer
   |
   | Compress
   v
Kafka
   |
   | Compressed batch
   v
Consumer
   |
   | Decompress
   v
Application
```

Therefore CPU work exists on both sides:

```
Producer CPU
    ↓
Compression

Consumer CPU
    ↓
Decompression
```

In many workloads, decompression is relatively inexpensive compared with compression, but the exact cost depends on the algorithm and workload.

---

## 14. Compression does NOT compress the business object

Suppose your application has:

```java
Order order = ...
```

Kafka doesn't magically compress your Java object.

The application first serializes it:

```
Java Object
    ↓
Serializer
    ↓
Bytes
    ↓
Kafka Record
    ↓
Batch
    ↓
Compression
```

So the pipeline is conceptually:

```
Object
  ↓
Serialization
  ↓
Bytes
  ↓
Batching
  ↓
Compression
  ↓
Network
```

---

## 15. Serialization vs Compression

Don't confuse these.

### Serialization

Converts an object into bytes.

```
Java Object
     ↓
JSON / Avro / Protobuf
     ↓
Bytes
```

### Compression

Reduces the size of those bytes.

```
Bytes
 ↓
gzip / lz4 / zstd
 ↓
Smaller bytes
```

Therefore:

```
Serialization ≠ Compression
```

---

## 16. JSON vs Avro/Protobuf + Compression

This is an interesting architecture consideration.

Suppose you use JSON:

```json
{
  "orderId": "ORD123",
  "customerId": "C100",
  "status": "CREATED"
}
```

JSON contains lots of repeated textual field names.

Compression can reduce that overhead substantially.

With binary formats such as:

- Avro
- Protobuf

the serialized data may already be smaller.

Then compression can reduce it further depending on the data.

Conceptually:

```
JSON
 ↓
Compression
 ↓
Small

Avro/Protobuf
 ↓
Compression
 ↓
Potentially even smaller
```

The right choice should be benchmarked for your workload.

---

## 17. Compression and Kafka Throughput

Suppose your bottleneck is network:

```
Producer
   |
   | 1 Gbps network
   X
Network bottleneck
```

Compression can help:

```
100 MB
 ↓
30 MB
 ↓
Network
```

Now more logical application data can fit through the same network bandwidth.

Therefore:

```
Compression
    ↓
Less bytes transferred
    ↓
Higher logical throughput
```

But if you're already CPU-bound:

```
CPU = 100%
```

adding heavy compression could make performance worse.

---

## 18. Choosing the compression algorithm

A practical mental model:

### LZ4

Good when you want:

```
High speed
+
Low CPU overhead
+
Good compression
```

Common choice for high-throughput workloads.

### ZSTD

Good when you want:

```
Excellent compression
+
Good performance
```

Often attractive when storage/network savings are especially important.

### Snappy

Historically popular for:

```
Fast compression/decompression
```

with moderate compression.

### GZIP

Can provide good compression but usually uses more CPU and can be slower.

Useful when compression ratio is more important than maximum throughput/CPU efficiency.

---

## 19. Compression in a high-volume system

Imagine:

```
             10 Producers
                  |
                  v
          +---------------+
          | Kafka Cluster  |
          | 10 Brokers     |
          +---------------+
                  |
                  v
             100 Consumers
```

Suppose producers generate:

```
500 MB/sec
```

Without compression:

```
500 MB/sec network
```

With compression reducing it to:

```
150 MB/sec
```

you've dramatically reduced network pressure.

For a replicated cluster:

```
Producer
   |
   v
Leader
  / \
 v   v
F1   F2
```

there are also replication network/storage implications.

---

## 20. Compression and Producer Batching

This relationship is very important:

```
batch.size
     +
linger.ms
     ↓
Larger batches
     ↓
Better compression opportunity
     ↓
Less network traffic
```

But:

```
linger.ms ↑
     ↓
Latency ↑
```

So tuning Kafka often becomes a balancing act:

```
                Throughput
                    ↑
                    |
             Larger batches
                    |
Compression ←-------+------→ Latency
                    |
               Smaller batches
```

---

## 21. Does the broker compress the data?

The producer is normally responsible for compression when configured with:

```
compression.type=lz4
```

Kafka stores records in batches, and Kafka brokers can also be configured with a compression policy for topics.

The important practical point is:

> Producer-side compression is generally preferred because it reduces network traffic before the data reaches the broker.

If producer and broker/topic compression settings differ, Kafka may recompress data as required by the configured behavior.

---

## 22. Compression and Kafka storage

Kafka stores data in append-only logs:

```
Partition 0

Segment 1
+------------------+
| compressed batch |
+------------------+

Segment 2
+------------------+
| compressed batch |
+------------------+

Segment 3
+------------------+
| compressed batch |
+------------------+
```

Kafka periodically rolls log segments.

Compression works at the record-batch level, while Kafka still maintains its normal log/segment structure.

---

## 23. Compression and offsets

Compression does **not** change Kafka's logical offsets.

Suppose:

```
Offset 100 → A
Offset 101 → B
Offset 102 → C
```

After compression:

```
Compressed batch
   |
   +-- A → offset 100
   +-- B → offset 101
   +-- C → offset 102
```

The records still have their logical offsets.

So:

```
Compression
    ≠
Offset change
```

---

## 24. Compression and message ordering

Compression doesn't change Kafka's ordering guarantee.

If a partition contains:

```
A
B
C
D
```

then compression doesn't change:

```
A → B → C → D
```

The batch is compressed as a unit while Kafka maintains the records' logical order.

---

## 25. Compression and delivery semantics

Compression is independent of:

- At-most-once
- At-least-once
- Exactly-once

You can have:

```
Compression
+
At-least-once
```

or:

```
Compression
+
Exactly-once
```

or:

```
Compression
+
Idempotent producer
```

They solve different problems.

```
Compression
    ↓
Efficiency


Idempotence
    ↓
Duplicate-safe retries


Transactions
    ↓
Atomic processing


Replication / ISR
    ↓
Durability
```

---

## 26. Important interview question

### Q: Where does Kafka compression happen?

A good answer:

> Kafka compression is generally performed by the producer on record batches before they are sent to the broker. Compressing batches rather than individual records improves compression efficiency and reduces network traffic and storage requirements. Consumers decompress the batches when reading them.

---

## 27. Interview question

### Q: Why is compression especially useful in Kafka?

Answer:

> Kafka moves and stores very large volumes of data and replicates partitions across brokers. Compression reduces the number of bytes transmitted and stored, which can reduce network bandwidth, disk usage, and replication overhead. The trade-off is additional CPU consumption and potentially increased latency.

---

## 28. Interview question

### Q: Does compression affect Kafka offsets?

**No.**

Offsets remain logical record positions.

```
100 → A
101 → B
102 → C
```

Compression only changes the physical representation.

---

## 29. Interview question

### Q: Which compression should I choose?

There is no universal answer.

A good senior-level answer is:

> I would benchmark LZ4 and ZSTD first for a high-throughput workload. LZ4 is attractive when CPU and latency are important, while ZSTD can provide better compression when network and storage savings are more important. The final choice depends on message size, data compressibility, CPU capacity, network bandwidth, latency requirements, and storage cost.

---

## 30. Compression + Kafka architecture

Now connect this with everything you've learned:

```
                         Producer
                            |
                            v
                       Serialization
                            |
                            v
                         Batching
                            |
                            v
                       Compression
                            |
                            v
                         Kafka
                            |
                       +----+----+
                       |         |
                       v         v
                    Leader    Followers
                       |         |
                       +----+----+
                            |
                           ISR
                            |
                            v
                         Storage
                            |
                            v
                         Consumer
                            |
                       Decompression
                            |
                            v
                       Deserialization
                            |
                            v
                        Application
```

This gives you the complete flow:

```
Object
  ↓
Serialize
  ↓
Batch
  ↓
Compress
  ↓
Send
  ↓
Leader
  ↓
Replicate
  ↓
ISR
  ↓
Consumer
  ↓
Decompress
  ↓
Deserialize
  ↓
Application
```

---

## 31. What you should remember for interviews

### Compression

```
compression.type=lz4
```

means:

```
Records
  ↓
Batch
  ↓
Compress
  ↓
Kafka
```

### Benefits

```
↓ Network bandwidth
↓ Disk usage
↓ Replication traffic
↑ Effective throughput
```

### Costs

```
↑ CPU
Potential latency impact
```

### Important relationship

```
batch.size
     +
linger.ms
     ↓
larger batches
     ↓
better compression
```

### Key distinction

```
Serialization
    ↓
Object → Bytes

Compression
    ↓
Bytes → Smaller Bytes
```

### And don't confuse:

```
Compression → Performance / efficiency

Idempotence → Duplicate-safe retries

Transactions → Atomic Kafka processing

ISR → Replication / durability
```

---

> **Senior-level takeaway:** In a high-throughput Kafka system, compression should be considered together with batching, `linger.ms`, network bandwidth, CPU capacity, replication factor, storage, and latency rather than as an isolated producer setting.

