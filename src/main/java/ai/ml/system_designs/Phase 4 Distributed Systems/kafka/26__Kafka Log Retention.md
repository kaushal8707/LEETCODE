# Kafka Log Retention

**Log retention** is Kafka's mechanism for deciding how long records remain available in a topic before Kafka removes them.

The most important concept is:

> Kafka does not delete a message when a consumer reads it. **Kafka deletes old log segments according to retention policies.**

This is fundamentally different from traditional message queues.

---

## 1. Why does Kafka need retention?

Imagine an `orders` topic receiving:

```
100 GB/day
```

If Kafka kept everything forever:

```
Day 1 → 100 GB
Day 2 → 200 GB
Day 3 → 300 GB
...
Day 365 → 36.5 TB
```

Storage would continuously grow.

Retention allows Kafka to say:

```
Keep data for 7 days
```

After that, old data becomes eligible for deletion.

---

## 2. Basic retention flow

Suppose:

```
retention = 7 days
```

Kafka log:

```
Oldest                                      Newest
  |                                            |
  v                                            v

+--------+--------+--------+--------+--------+
| Seg 0  | Seg 1  | Seg 2  | Seg 3  | Seg 4  |
+--------+--------+--------+--------+--------+
    ❌       ❌       ❌       ✅       ACTIVE
```

Kafka periodically checks the log.

Old segments that satisfy the retention policy become eligible for deletion.

```
Segment 0 → delete
Segment 1 → delete
Segment 2 → delete
```

The newer segments remain.

---

## 3. Retention is segment-based

This connects directly with the previous topic.

You learned:

```
Partition
   ↓
Log
   ↓
Segments
```

Retention works primarily by removing **log segments**, rather than individually deleting every record.

For example:

```
Partition 0

Segment 0
offset 0 - 9999

Segment 1
offset 10000 - 19999

Segment 2
offset 20000 - 29999

Segment 3
offset 30000 - 39999
```

If Segment 0 becomes old enough:

```
Segment 0 → DELETE
```

This is much more efficient than:

```
record 0 → delete
record 1 → delete
record 2 → delete
...
```

---

## 4. Main retention policies

There are two major ways Kafka controls retention:

### 1. Time-based retention

```
retention.ms
```

### 2. Size-based retention

```
retention.bytes
```

You can also use both.

---

## 5. Time-based retention

Example:

```
retention.ms=604800000
```

That's:

```
7 days
```

Conceptually:

```
Record age
    |
    +---- < 7 days → keep
    |
    +---- ≥ retention threshold → eligible for deletion
```

But remember:

> Retention deletion is not necessarily an exact second-by-second guarantee.

Kafka operates with log segments and cleanup intervals, so data may remain somewhat longer than the configured threshold before deletion.

---

## 6. Size-based retention

Suppose:

```
retention.bytes=10737418240
```

which is approximately:

```
10 GB
```

Kafka limits retained log size for the configured scope.

Conceptually:

```
10 GB limit

+------+-----+-----+-----+-----+
| Seg0 | Seg1| Seg2| Seg3| Seg4|
+------+-----+-----+-----+-----+
                       ↑
                   New data
```

When enough data accumulates, older segments become eligible for deletion.

---

## 7. Important: `retention.bytes` is per partition

This is a common interview question.

Suppose:

```
Topic = orders
Partitions = 10
retention.bytes = 100 GB
```

The configured retention size applies to **each partition's log** rather than being one single 100 GB pool shared across the entire topic.

So conceptually, the aggregate topic capacity could be around:

```
100 GB × 10 partitions
=
~1 TB
```

assuming the partitions reach that limit and other factors don't intervene.

### Interview answer

> `retention.bytes` is applied on a per-partition basis, so the total potential topic storage scales with the number of partitions.

---

## 8. Time + size retention together

Suppose:

```
retention.ms=7 days
retention.bytes=100GB
```

Now Kafka has two constraints.

Conceptually:

```
                  Retention
                     |
             +-------+-------+
             |               |
          Time limit      Size limit
             |               |
          7 days           100 GB
             |               |
             +-------+-------+
                     |
                     v
              Older segments
                 removed
```

This means data can become eligible for deletion when the applicable retention condition is reached.

A useful way to remember it:

> **Retention can be bounded by time, size, or both.**

---

## 9. What happens when a consumer reads a message?

This is extremely important.

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

Consumer offset:

```
3
```

But Kafka still has:

```
0
1
2
3
4
5
```

So:

```
Consumer reads
      ≠
Kafka deletes
```

Kafka may delete those records much later according to retention.

---

## 10. Why is this powerful?

Because consumers can **replay** old data.

Suppose:

```
orders
```

was processed by:

```
Order Service
```

Then you create:

```
Analytics Service
```

You can potentially start the new consumer group from an earlier available offset.

```
Kafka
 |
 +------------------------------+
 | historical event log         |
 +------------------------------+
          |              |
          v              v
     Order Service   Analytics
```

This is one of Kafka's biggest advantages.

---

## 11. Example: bug recovery

Suppose your consumer processed:

```
offset 1000 → 2000
```

and you discover a bug in the application.

If the data is still retained:

```
offset 1000
    ↓
replay
    ↓
process again with fixed code
```

You can rebuild downstream state.

This is possible because Kafka treats the log as a durable event history rather than deleting messages immediately after consumption.

---

## 12. Retention and consumer lag

Suppose:

```
Producer
   ↓
Kafka
   ↓
Consumer
```

Producer is producing faster than consumer.

Consumer lag grows:

```
Producer
offset = 10,000,000

Consumer
offset = 5,000,000
```

Now imagine retention removes old records before the consumer catches up.

Then the consumer may no longer be able to read the missing historical records from Kafka.

Conceptually:

```
Kafka retention
      ↓
Old records deleted
      ↓
Consumer hasn't processed them
      ↓
Consumer may fall behind the available log
```

This is a critical production consideration.

---

## 13. Log start offset

Every partition effectively has a moving available history.

Think:

```
             Available log
                 |
                 v

        +----------------------+
        |                      |
        |      Kafka Log       |
        |                      |
        +----------------------+
        ^                      ^
        |                      |
   Log Start Offset       Log End Offset
```

Retention moves the log start forward.

For example:

```
Before retention:

0 1 2 3 4 5 6 7 8 9 10
^
log start


After retention:

5 6 7 8 9 10
^
new log start
```

Offsets 0–4 no longer exist in the partition.

---

## 14. What happens if a consumer asks for deleted data?

Suppose:

```
Consumer committed offset = 100
```

But retention deleted everything before:

```
offset 500
```

The consumer wants:

```
offset 100
```

but Kafka no longer has it.

Kafka has to deal with the consumer being behind the current log start. Depending on the consumer's `auto.offset.reset` configuration, it can reset to an available offset, such as the earliest or latest position, rather than reading data that no longer exists.

This is why retention planning is important.

---

## 15. `auto.offset.reset`

Common settings include:

```
auto.offset.reset=earliest
```

or:

```
auto.offset.reset=latest
```

### `earliest`

If the requested offset is unavailable, start from the earliest available offset.

### `latest`

If the requested offset is unavailable, start from the latest offset.

Important:

> `auto.offset.reset` is **not** a general mechanism for overriding normal committed offsets. It matters when there is no valid committed offset or the requested offset is unavailable.

---

## 16. Retention and segments

Suppose:

```
retention.ms = 7 days
```

and your segments are:

```
Segment 0 → 10 days old
Segment 1 → 8 days old
Segment 2 → 6 days old
Segment 3 → 2 days old
Segment 4 → active
```

Kafka may eventually remove:

```
Segment 0 ❌
Segment 1 ❌
```

while keeping:

```
Segment 2 ✅
Segment 3 ✅
Segment 4 ✅
```

The exact deletion timing depends on segment rolling and log cleanup behavior.

---

## 17. `segment.ms` and `segment.bytes`

These settings influence when Kafka rolls to a new segment.

Conceptually:

```
segment.bytes
      OR
segment.ms
      ↓
Segment roll
      ↓
New active segment
```

Why does this matter for retention?

Because retention operates on segments.

If segments are extremely large:

```
Segment 0
    ↓
Huge amount of data
```

retention cannot simply remove individual records from the middle of that segment under normal delete retention.

Therefore:

> Segment configuration indirectly affects how precisely and efficiently retention can remove old data.

---

## 18. Retention doesn't mean exact deletion time

Suppose:

```
retention.ms = 7 days
```

It is incorrect to think:

```
At exactly 7 days + 0 seconds
→ record disappears
```

Instead:

```
Record becomes old enough
       ↓
Segment becomes eligible
       ↓
Kafka log cleanup runs
       ↓
Segment is deleted
```

So retention should be understood as a **policy/eligibility mechanism**, not an exact TTL for each individual record.

---

## 19. Retention vs TTL

They sound similar but aren't exactly the same.

### Traditional cache TTL

```
Key
 ↓
TTL expires
 ↓
Key removed
```

### Kafka retention

```
Record
 ↓
belongs to segment
 ↓
segment becomes eligible
 ↓
cleanup
 ↓
segment removed
```

Kafka retention is therefore more naturally understood at the log/segment level.

---

## 20. Retention and replication

Suppose:

```
RF = 3
```

```
        Leader
       /      \
      F1       F2
```

Each replica has its own copy of the partition log.

When old segments are removed, the replicas also maintain the corresponding retained log state.

Conceptually:

```
Leader
  |
  +-- old segment → removed
  |
Follower 1
  |
  +-- corresponding old segment → removed
  |
Follower 2
  |
  +-- corresponding old segment → removed
```

Retention is therefore not simply:

```
delete from leader only
```

and forget the replicas.

---

## 21. Retention and disk capacity

Suppose:

```
100 MB/sec
```

of data enters Kafka.

That's approximately:

```
100 × 60 × 60 × 24
≈ 8.64 TB/day
```

before accounting for compression and replication.

If you retain:

```
7 days
```

you could need roughly:

```
8.64 TB × 7
≈ 60.5 TB
```

of logical data before considering compression and other storage factors.

If:

```
Replication Factor = 3
```

the physical storage requirement can be roughly:

```
60.5 TB × 3
≈ 181.5 TB
```

Again, this is a simplified capacity estimate; actual disk requirements also include compression ratio, segment/index overhead, replication distribution, broker headroom, and operational safety margins.

This is how retention becomes a system-design **capacity planning** problem.

---

## 22. Compression + retention

You just learned compression.

These two work together:

```
Producer
   |
Compression
   |
Kafka
   |
Log Segments
   |
Retention
```

Suppose uncompressed data is:

```
10 TB/day
```

and compression reduces it to:

```
3 TB/day
```

Then retention storage requirements can be dramatically lower.

So:

```
Compression
    ↓
Less physical storage
    ↓
Longer retention possible
```

for the same disk budget.

---

## 23. Retention + log compaction

This is another critical distinction.

You learned:

```
cleanup.policy=delete
```

and:

```
cleanup.policy=compact
```

These solve different problems.

### Delete retention

```
Old data
   ↓
Delete
```

### Compaction

```
Same key
   ↓
Keep latest relevant value
```

---

## 24. Example: delete retention

Suppose:

```
orderId=101 → Created
orderId=101 → Paid
orderId=101 → Shipped
```

With normal delete retention:

```
All records
   ↓
remain until retention says they are old enough
   ↓
old segments deleted
```

---

## 25. Example: compaction

For a compacted topic:

```
customer-101 → John
customer-101 → John Smith
customer-101 → John Smith, Mumbai
```

Kafka can eventually clean up superseded records and retain the latest state for that key, subject to compaction semantics.

Conceptually:

```
Before:

101 → John
101 → John Smith
101 → John Smith, Mumbai


After compaction:

101 → John Smith, Mumbai
```

---

## 26. Delete + compact

Kafka can combine them:

```
cleanup.policy=compact,delete
```

Then:

```
Compaction
    +
Time/size retention
```

can both apply.

This is useful when you want:

```
latest state
+
bounded history
```

---

## 27. Example: customer state topic

Imagine:

```
customer-state
```

Events:

```
C101 → status=ACTIVE
C101 → status=SUSPENDED
C101 → status=ACTIVE
```

Compaction helps keep the latest state.

But if you also have a time-based retention policy, older log data can eventually be removed according to the applicable cleanup policy.

---

## 28. Retention and replay design

Suppose you need:

```
Replay up to 30 days
```

Then you need enough retention:

```
retention ≥ required replay window
```

For example:

```
Business requirement:
Replay previous 30 days

Kafka:
retention.ms ≥ 30 days
```

But you should also account for:

- Consumer lag
- Operational delays
- Incident recovery
- Reprocessing window
- Capacity

A production design might intentionally retain longer than the minimum business requirement.

---

## 29. Real-world example

Suppose you're designing an order system.

Requirements:

```
Order events
→ retain 14 days
→ allow replay
→ high throughput
```

You could conceptually configure:

```
cleanup.policy=delete
retention.ms=1209600000
```

which is:

```
14 days
```

Architecture:

```
Order Service
      |
      v
Kafka orders
      |
      +-- P0 → segments
      +-- P1 → segments
      +-- P2 → segments
      +-- P3 → segments
      |
      v
Consumers
```

Every partition maintains its own log segments.

After the retention window:

```
Old segments
      ↓
Eligible
      ↓
Deleted
```

---

## 30. Retention and consumer groups

This is a common interview scenario.

Suppose:

```
Group A
→ consuming normally

Group B
→ stopped for 10 days
```

Kafka doesn't care that Group B hasn't consumed the records.

Retention continues.

```
Kafka
 |
 +-- records 0-1000
 +-- records 1001-2000
 +-- records 2001-3000
```

After retention:

```
0-1000 → deleted
```

Group B cannot magically recover those records from Kafka.

This is why:

> **Kafka retention is independent of consumer progress.**

---

## 31. Retention is not based on consumer offsets

This distinction is critical:

```
Kafka retention
    ≠
Consumer offset
```

Kafka doesn't normally ask:

> "Has every consumer processed this record?"

before deleting it.

Instead, retention policies determine when old log data is eligible for removal.

Therefore, a slow consumer can fall behind the available history.

---

## 32. Retention and consumer lag monitoring

In production, monitor:

```
Consumer Lag
+
Log Start Offset
+
Consumer Offset
```

Conceptually:

```
Log Start
    |
    v
    5000
             Consumer
                |
                v
              6000
                              Log End
                                 |
                                 v
                                9000
```

Consumer is behind the end but still has data available.

Problem:

```
Log Start = 7000
Consumer = 6000
```

Now the consumer has fallen behind the retained history.

That's a serious operational situation.

---

## 33. Retention and disk alerts

You should also monitor:

- Disk utilization
- Broker storage
- Partition size
- Retention deletion rate
- Consumer lag
- Segment count

For example:

```
Disk usage > 80%
       ↓
Investigate
       ↓
Traffic increase?
Retention too long?
Compression poor?
Unexpected topic growth?
Consumer lag?
```

---

## 34. Senior system-design example

Suppose you're designing a payment-event platform.

Requirements:

```
Traffic:
2 TB/day

Replay requirement:
30 days

Replication:
RF = 3
```

Logical retention storage:

```
2 TB × 30
=
60 TB
```

With RF=3:

```
60 TB × 3
=
180 TB
```

before compression and overhead.

Suppose compression gives a 2:1 reduction:

```
60 TB / 2
=
30 TB
```

Then replicated storage is roughly:

```
30 TB × 3
=
90 TB
```

This shows why you must consider:

```
Traffic
  +
Retention
  +
Compression
  +
Replication
  +
Partition count
  +
Broker capacity
```

when designing Kafka.

---

## 35. Interview questions

### Q1. What is Kafka log retention?

> Log retention is Kafka's mechanism for controlling how long or how much partition log data is retained before old log segments become eligible for deletion.

### Q2. Does Kafka delete a message after consumption?

**No.** Kafka tracks consumer offsets independently. Records remain until retention or compaction removes them.

### Q3. What are the main retention settings?

```
retention.ms
retention.bytes
```

### Q4. Is `retention.bytes` per topic?

> It is applied per partition, so total topic storage can scale with the number of partitions.

### Q5. Does retention delete individual records?

> Normal delete retention primarily works by deleting eligible log segments, not by individually deleting every record.

### Q6. What happens if a consumer falls behind retention?

> Records it has not consumed may be deleted. If its requested offset is no longer available, the consumer cannot retrieve those records from Kafka and offset-reset behavior may apply.

### Q7. Does consumer lag affect retention?

> Consumer lag doesn't normally prevent retention deletion. Kafka retention operates independently of consumer progress.

### Q8. What is the difference between retention and compaction?

> Delete retention removes old log segments based on time/size policies, while compaction removes superseded records for the same key while preserving the latest state according to compaction semantics.

---

## 36. Complete Kafka storage picture

You now have:

```
                         Kafka
                           |
                         Topic
                           |
                     +-----+-----+
                     |           |
                  Partition    Partition
                     |           |
                     v           v
                   Log         Log
                     |           |
                  Segments     Segments
                     |
              +------+------+
              |             |
            .log         indexes
              |
         Record Batches
              |
           Records
              |
           Offsets
              |
              v
        Retention / Compaction
              |
       +------+------+
       |             |
      Delete       Compact
       |             |
   Old segments   Old values
       |             |
       +------+------+
              |
              v
           Storage
```

---

## 37. The key mental model

Remember these five statements:

1. Kafka stores records in partition logs.
2. Partition logs are divided into segments.
3. Consumers advance offsets; they don't delete messages.
4. Retention removes old log segments based on configured policies.
5. Compaction is different: it cleans up older records for the same key.

And the most important relationship:

```
Producer
   ↓
Batch
   ↓
Compression
   ↓
Partition
   ↓
Append-only Log
   ↓
Log Segments
   ↓
Retention
   ↓
Delete old segments
```

---

## Senior-level takeaway

> **Kafka retention is independent of consumer consumption. Kafka stores records in partitioned append-only logs, divides those logs into segments, and periodically removes segments that satisfy time/size retention policies. This design enables high-throughput sequential writes, replay within the retention window, and efficient cleanup. When designing retention, you must consider traffic volume, compression ratio, replication factor, partition count, consumer lag, replay requirements, and broker disk capacity.**

