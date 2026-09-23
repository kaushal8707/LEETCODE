# Kafka Log Structure

Kafka's **log structure** is one of the most important concepts for understanding how Kafka achieves high throughput, durability, sequential I/O, retention, replay, offsets, and fast recovery.

The key idea is:

> **Each Kafka partition is an append-only log stored on disk.**

So when you learned:

```
Topic
  ↓
Partitions
```

you can now go one level deeper:

```
Topic
  ↓
Partition
  ↓
Append-only Log
  ↓
Log Segments
  ↓
Records
```

---

## 1. Topic → Partition → Log

Suppose we have:

```
Topic: orders
```

with 3 partitions:

```
orders
 ├── partition-0
 ├── partition-1
 └── partition-2
```

Each partition is an independent ordered log:

```
Partition 0:

offset 0
offset 1
offset 2
offset 3
offset 4
...
```

Similarly:

```
Partition 1:

offset 0
offset 1
offset 2
offset 3
...
```

And:

```
Partition 2:

offset 0
offset 1
offset 2
...
```

Offsets are scoped to a partition.

So:

```
P0 → offset 100
P1 → offset 100
```

are two different records.

---

## 2. Kafka is fundamentally an append-only log

Traditional database thinking might look like:

```
INSERT
UPDATE
DELETE
```

Kafka's partition log is primarily:

```
append
append
append
append
append
```

For example:

```
P0

+--------------------------------------------------+
| R0 | R1 | R2 | R3 | R4 | R5 | R6 | R7 | ...     |
+--------------------------------------------------+
  0    1    2    3    4    5    6    7
```

New records are appended at the end:

```
R8
 ↓
+------------------------------------------------------+
| R0 | R1 | R2 | R3 | R4 | R5 | R6 | R7 | R8         |
+------------------------------------------------------+
```

Kafka doesn't normally modify old records in place.

---

## 3. Why append-only?

Append-only storage provides several advantages:

```
Sequential writes
      ↓
High disk throughput
      ↓
Efficient storage
      ↓
High Kafka throughput
```

Instead of constantly modifying random locations on disk, Kafka primarily appends new data.

This works particularly well with:

- sequential disk I/O
- OS page cache
- batching
- zero-copy techniques
- sequential reads

---

## 4. Physical structure on disk

Suppose Kafka stores:

```
Topic = orders
Partition = 0
```

The broker may have a directory structure conceptually like:

```
/kafka-data/
    orders-0/
        00000000000000000000.log
        00000000000000000000.index
        00000000000000000000.timeindex

        00000000000000123456.log
        00000000000000123456.index
        00000000000000123456.timeindex

        ...
```

The exact directory/file naming and implementation details can vary by Kafka version/configuration, but the important concept is:

```
Partition
   |
   +-- Log segments
        |
        +-- .log
        +-- .index
        +-- .timeindex
```

---

## 5. What is a Log Segment?

A Kafka partition is **not one giant file forever**.

Kafka divides the partition log into **segments**.

Conceptually:

```
Partition 0

+-------------------+
| Segment 0         |
| offsets 0 - 9999  |
+-------------------+

+-------------------+
| Segment 1         |
| 10000 - 19999     |
+-------------------+

+-------------------+
| Segment 2         |
| 20000 - 29999     |
+-------------------+
```

New records are written to the **active segment**.

Older segments become inactive.

---

## 6. Active segment

At any point, one segment is generally the **active segment** for appending new records.

For example:

```
Partition 0

Segment 0  CLOSED
Segment 1  CLOSED
Segment 2  CLOSED
Segment 3  ACTIVE
```

New records:

```
R100
R101
R102
```

are appended to Segment 3:

```
Segment 3

R100
R101
R102
```

When the segment reaches a configured roll condition, Kafka creates a new segment.

```
Segment 3 → CLOSED

Segment 4 → ACTIVE
```

---

## 7. Why use segments?

Segments make Kafka's retention and deletion operations much easier.

Suppose you have:

```
30 days of data
```

and retention says:

```
7 days
```

Kafka doesn't need to inspect and delete every individual record.

It can remove old segments.

Conceptually:

```
Oldest
  ↓
Segment 0 ❌ delete
Segment 1 ❌ delete
Segment 2 ❌ delete
Segment 3 ✅ keep
Segment 4 ✅ keep
Segment 5 ✅ active
```

This makes retention much more efficient.

---

## 8. Log segment files

A typical segment has three important file types:

- `.log`
- `.index`
- `.timeindex`

For example:

```
00000000000000100000.log
00000000000000100000.index
00000000000000100000.timeindex
```

Let's understand each.

---

## 9. `.log` file

The `.log` file contains the actual Kafka record data.

Conceptually:

```
00000000000000100000.log

+------------------------------------------------+
| Record | Record | Record | Record | Record    |
+------------------------------------------------+
```

Records contain information such as:

- key
- value
- timestamp
- headers
- record metadata

Kafka stores records in batches internally, so physically the format is more complex than a simple row-per-record file.

---

## 10. `.index` file

Searching a huge log directly would be expensive.

Imagine:

```
10 billion records
```

and consumer asks:

```
Give me offset 8,500,000,000
```

Kafka needs to locate the corresponding position efficiently.

The `.index` file provides an index from:

```
Logical offset
       ↓
Physical file position
```

Conceptually:

```
Offset       File Position

1000    →       500
1100    →       920
1200    →      1410
1300    →      1920
```

Kafka can use this index to jump near the desired location and then scan forward.

---

## 11. Important: Kafka index is sparse

This is a common interview question.

Kafka does **not** necessarily create an index entry for every single record.

It uses a **sparse index**.

For example:

```
Offset → Position

1000 → 500
1100 → 920
1200 → 1410
```

If Kafka needs:

```
offset = 1150
```

it can find:

```
1000 → position 500
1100 → position 920
1200 → position 1410
```

and start around the indexed position for 1100, then scan forward to 1150.

So:

```
Sparse Index
     ↓
Find approximate position
     ↓
Sequential scan
     ↓
Target record
```

This is much smaller than maintaining a complete index for every record.

---

## 12. `.timeindex`

Kafka also maintains a time-based index.

It helps map:

```
Timestamp
    ↓
Approximate log position/offset
```

For example:

```
Timestamp              Offset

10:00:00        →       1000
10:01:00        →       1400
10:02:00        →       1800
```

This is useful for operations involving timestamps, such as finding records around a particular time.

---

## 13. Complete log structure

Now combine everything:

```
Kafka Broker
    |
    +-- Topic: orders
          |
          +-- Partition 0
          |     |
          |     +-- Segment 0
          |     |     +-- .log
          |     |     +-- .index
          |     |     +-- .timeindex
          |     |
          |     +-- Segment 1
          |     |     +-- .log
          |     |     +-- .index
          |     |     +-- .timeindex
          |     |
          |     +-- Segment 2
          |           +-- .log
          |           +-- .index
          |           +-- .timeindex
          |
          +-- Partition 1
          |     |
          |     +-- Segments...
          |
          +-- Partition 2
                |
                +-- Segments...
```

This is the core Kafka storage architecture.

---

## 14. Offset vs physical position

This distinction is extremely important.

An **offset** is a logical identifier.

A **file position** is a physical byte location.

For example:

```
Kafka Record

Logical:
offset = 10500

Physical:
segment = 00000000000000100000.log
position = 8,742,391 bytes
```

Kafka's index helps connect:

```
Offset
  ↓
Physical file position
```

---

## 15. How a consumer reads a record

Suppose consumer says:

```
Give me offset 10500 from partition 0
```

Conceptually Kafka does:

```
Consumer
   |
   | offset 10500
   v
Partition 0
   |
   v
Find appropriate segment
   |
   v
Use offset index
   |
   v
Approximate file position
   |
   v
Read from .log
   |
   v
Find exact record
   |
   v
Return to consumer
```

The important point is:

> Kafka does not need to scan the entire partition from offset 0.

---

## 16. Consumer doesn't delete messages

This is one of the biggest differences between Kafka and traditional message queues.

Suppose:

```
P0

0
1
2
3
4
5
```

Consumer reads:

```
0
1
2
```

The records remain in Kafka.

Kafka stores:

```
0
1
2
3
4
5
```

The consumer group stores its position:

```
next offset ≈ 3
```

Conceptually:

```
Kafka Log
+---+---+---+---+---+---+
| 0 | 1 | 2 | 3 | 4 | 5 |
+---+---+---+---+---+---+

Consumer Group
             ^
             |
          position
```

This is why another consumer group can independently replay the same data.

---

## 17. Offset is not a database pointer

An offset is specific to:

```
Topic + Partition
```

For example:

```
orders-P0 → offset 100
orders-P1 → offset 100
```

These aren't the same record.

So always think:

```
(topic, partition, offset)
```

identifies a Kafka record position.

---

## 18. Multiple consumer groups

Suppose:

```
orders
   |
   +-- P0
```

Consumer Group A:

```
Group A → offset 100
```

Consumer Group B:

```
Group B → offset 50
```

Kafka doesn't need to create another copy of the data.

Both groups can independently read from the same partition log.

```
                  Kafka Log
                     |
             +-------+-------+
             |               |
          Group A         Group B
          offset 100       offset 50
```

This is one of Kafka's major architectural advantages.

---

## 19. Retention

Kafka retains records according to configured retention policies.

Common policies include:

- `retention.ms`
- `retention.bytes`

For example:

```
retention.ms = 7 days
```

means Kafka can retain data based on the configured time-based retention policy.

When old data qualifies for deletion:

```
Segment 0 → old → delete
Segment 1 → old → delete
Segment 2 → keep
Segment 3 → keep
```

Again, segments make this practical.

---

## 20. Log compaction

Kafka has another important retention mechanism:

> **Log compaction**

This is different from normal time/size retention.

Suppose the topic uses:

```
cleanup.policy=compact
```

and you have:

```
key = customer-101

customer-101 → name=John
customer-101 → name=John Smith
customer-101 → name=John Smith, city=Mumbai
```

For a compacted topic, Kafka can eventually retain the latest value for that key while removing older superseded records, subject to compaction semantics.

Conceptually:

```
Before:

customer-101 → John
customer-101 → John Smith
customer-101 → John Smith, Mumbai


After compaction:

customer-101 → John Smith, Mumbai
```

This is useful for:

- current state
- configuration
- customer profiles
- account state
- caches
- changelog topics

---

## 21. Delete/tombstone in compaction

Suppose:

```
customer-101 → null
```

This is called a **tombstone** in a compacted topic.

It indicates deletion of that key's value.

Conceptually:

```
customer-101 → Customer data
customer-101 → null
```

Eventually compaction can remove the older record, and after the tombstone's relevant retention period, the tombstone itself can be removed.

---

## 22. Time retention vs compaction

Very important:

### Delete retention

```
cleanup.policy=delete
```

Old records are removed based on retention conditions.

### Compaction

```
cleanup.policy=compact
```

Older records for the same key can be cleaned up while retaining the latest state.

You can also combine them:

```
cleanup.policy=compact,delete
```

Then both policies apply.

---

## 23. Log segment rolling

A segment can become inactive based on conditions such as:

- `segment.bytes`
- `segment.ms`

Conceptually:

```
Active Segment
      |
      | reaches roll condition
      v
Closed Segment
      |
      v
New Active Segment
```

Example:

```
Segment 0 → 0–1 GB
Segment 1 → 1–2 GB
Segment 2 → 2–3 GB
Segment 3 → ACTIVE
```

The exact defaults depend on Kafka configuration/version.

---

## 24. Why Kafka doesn't use one giant file

Imagine:

```
Partition = 10 TB
```

as one file.

Operations such as:

- Retention
- Deletion
- Compaction
- Recovery
- Indexing

would become much harder.

Segments allow Kafka to manage the log in manageable chunks.

```
10 TB

↓ split

Segment
Segment
Segment
Segment
...
```

---

## 25. Kafka log and replication

Remember your previous topic:

```
Leader / Follower
```

Suppose:

```
P0

Leader
  |
  +-- Segment 0
  +-- Segment 1
  +-- Segment 2
```

Followers replicate the partition's log.

Conceptually:

```
              Leader P0
             /         \
            /           \
           v             v
      Follower P0     Follower P0
```

Each replica maintains its own local copy of the partition log.

So:

```
Leader log
    ↓
Follower log
    ↓
Follower log
```

Replication does not mean all replicas share one physical file.

Each broker stores its own replica.

---

## 26. Log structure and ISR

Suppose:

```
RF = 3
```

```
        Leader
        /    \
      F1      F2
```

If F2 falls behind:

```
Leader → F1       ✅ ISR
       → F2       ❌ out of ISR
```

The underlying partition log on F2 may lag behind.

Once F2 catches up sufficiently:

```
F2 → ISR
```

This connects log structure directly to your previous ISR topic.

---

## 27. Log structure and durability

Suppose:

```
acks=all
RF=3
min.insync.replicas=2
```

A record is accepted only when Kafka can satisfy the configured ISR/minimum requirements.

Conceptually:

```
Producer
   |
   v
Leader
   |
   +---- F1
   |
   +---- F2
```

The replicated logs provide durability.

So:

```
Log
 +
Replication
 +
ISR
 +
acks
```

work together to determine how safely data is stored.

---

## 28. Why Kafka is fast

Kafka's log structure contributes significantly to performance:

```
Producer
   |
   v
Batch
   |
   v
Sequential append
   |
   v
Log segment
   |
   v
OS page cache
   |
   v
Sequential reads
```

Combined with:

- batching
- compression
- sequential I/O
- page cache
- efficient network transfer
- zero-copy techniques

Kafka can process very high volumes.

---

## 29. Page cache

Kafka relies heavily on the operating system's **page cache**.

Conceptually:

```
Application
    |
    v
Kafka
    |
    v
OS Page Cache
    |
    v
Disk
```

When consumers read recently written data, the data may already be in memory through the OS page cache.

Therefore Kafka doesn't need to explicitly maintain a huge Java heap cache for all messages.

---

## 30. Zero-copy

Kafka can take advantage of OS-level **zero-copy** mechanisms for transferring data from storage/cache to the network.

Conceptually:

```
Disk / Page Cache
       |
       | efficient transfer
       v
Network
       |
       v
Consumer
```

The goal is to reduce unnecessary copying between application buffers.

This contributes to Kafka's high-throughput design.

---

## 31. Log structure + compression

Connect this with the topic you just learned.

Kafka:

```
Records
   ↓
Batch
   ↓
Compression
   ↓
Log Segment
```

So a segment can contain compressed record batches.

Conceptually:

```
Segment
+--------------------------------+
| Compressed Batch               |
|   Records 0-99                 |
+--------------------------------+
| Compressed Batch               |
|   Records 100-199              |
+--------------------------------+
| Compressed Batch               |
|   Records 200-299              |
+--------------------------------+
```

---

## 32. Log structure + parallelism

Now connect it with partition strategy:

```
Topic
 |
 +-- P0 → Log → Segments
 |
 +-- P1 → Log → Segments
 |
 +-- P2 → Log → Segments
 |
 +-- P3 → Log → Segments
```

Consumers:

```
P0 → C0
P1 → C1
P2 → C2
P3 → C3
```

Therefore:

```
Partition
   ↓
Independent log
   ↓
Independent consumption
   ↓
Parallelism
```

---

## 33. Log structure + replay

This is another major Kafka feature.

Suppose a consumer processed:

```
offset 0 → 1 → 2 → 3 → 4
```

and later you discover a bug.

You can reset the consumer group to an earlier offset:

```
offset 2
```

and process:

```
2 → 3 → 4 → 5 → ...
```

The original records still exist because:

```
Consumer reading
    ≠
Message deletion
```

until retention/compaction removes them.

This is why Kafka is excellent for:

- event replay
- recovery
- rebuilding projections
- reprocessing
- analytics

---

## 34. Complete Kafka internal picture

At this point, combine everything you've learned:

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
                         Partitioner
                            |
             +--------------+--------------+
             |              |              |
             v              v              v
          Partition 0    Partition 1    Partition 2
             |              |              |
             v              v              v
          Log             Log             Log
             |              |              |
        +----+----+     +----+----+     +----+----+
        |         |     |         |     |         |
     Segment   Segment Segment  Segment Segment Segment
        |         |        |         |        |        |
       .log      .log     .log      .log     .log     .log
       .index    .index   .index    .index   .index   .index
       .time     .time    .time     .time    .time    .time
        |
        v
   Replication
        |
   Leader → Followers
        |
       ISR
        |
        v
     Consumers
        |
   Consumer Group
        |
      Offset
```

---

## 35. Most important interview questions

### Q1. What is Kafka's log?

> A Kafka partition is an ordered, append-only log of records. Records are appended sequentially and identified by offsets within that partition.

### Q2. What is a log segment?

> A partition log is divided into multiple segment files. One segment is active for writes while older segments are closed. Segmentation enables efficient retention, deletion, recovery, and compaction.

### Q3. What are `.log`, `.index`, and `.timeindex` files?

> `.log` stores the actual record data, `.index` maps offsets to approximate physical file positions, and `.timeindex` helps locate records based on timestamps.

### Q4. Why does Kafka use a sparse index?

> A full index for every record would consume significant storage. A sparse index finds an approximate location, after which Kafka performs a sequential scan to locate the exact record.

### Q5. Does Kafka delete a record after a consumer reads it?

**No.** Consumer consumption only advances the consumer group's offset. Records remain available until retention or compaction removes them.

### Q6. Why does Kafka use segments?

> Segments allow Kafka to efficiently roll active logs, delete old data, perform retention, and manage compaction without operating on one enormous file.

### Q7. What is the difference between offset and file position?

> An offset is a logical record position within a partition. A file position is a physical byte location in a segment. Kafka's index maps between the two approximately.

---

## 36. The most important mental model

Remember this hierarchy:

```
                    Kafka
                      |
                    Topic
                      |
                  Partitions
                      |
                  Append-only
                     Logs
                      |
                 Log Segments
                      |
          +-----------+-----------+
          |           |           |
         .log       .index    .timeindex
          |
      Record Batches
          |
       Records
          |
        Offset
```

And connect it with your previous topics:

```
Partition
    ↓
Ordering
    ↓
Parallelism
    ↓
Log
    ↓
Segments
    ↓
Offsets
    ↓
Replay
```

---

## One sentence to remember:

> **A Kafka partition is an append-only ordered log, physically divided into segments, where `.log` files contain record data and index files help locate records efficiently; retention and compaction operate largely at the segment/log level, while consumers track logical offsets rather than deleting messages.**

---

For your next level of Kafka internals, the natural progression is **Log Segment → `.log` file → Record Batch → Offset Index → Time Index → Page Cache → Zero Copy → Log Retention → Log Compaction → Log Recovery**.

