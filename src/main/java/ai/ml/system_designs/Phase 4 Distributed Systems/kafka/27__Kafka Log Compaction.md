# Kafka Log Compaction

**Log compaction** is a Kafka feature that allows Kafka to retain the **latest record for each key**, instead of retaining every historical value forever.

The easiest way to remember it:

> **Retention** = "keep data for X time."
> **Compaction** = "keep the latest state for each key."

This is especially useful when a Kafka topic represents **current state** rather than an **event history**.

---

## 1. Why do we need Log Compaction?

Consider a topic called:

```
customer-profile
```

Suppose customer 101 changes their address several times:

```
offset 0 → key=101, value=Delhi
offset 1 → key=102, value=Mumbai
offset 2 → key=101, value=Pune
offset 3 → key=103, value=Bangalore
offset 4 → key=101, value=Hyderabad
```

Without compaction:

```
101 → Delhi
102 → Mumbai
101 → Pune
103 → Bangalore
101 → Hyderabad
```

All records remain until normal retention removes them.

But perhaps we only care about:

```
Current customer state
```

Then the useful information is:

```
101 → Hyderabad
102 → Mumbai
103 → Bangalore
```

Kafka log compaction helps achieve this.

---

## 2. How compaction works

Imagine:

Before compaction:

```
+--------+-----------------------+
| Offset | Key       Value       |
+--------+-----------------------+
| 0      | 101       Delhi       |
| 1      | 102       Mumbai      |
| 2      | 101       Pune        |
| 3      | 103       Bangalore   |
| 4      | 101       Hyderabad   |
+--------+-----------------------+
```

After compaction, conceptually:

```
+--------+-----------------------+
| Offset | Key       Value       |
+--------+-----------------------+
| 1      | 102       Mumbai      |
| 3      | 103       Bangalore   |
| 4      | 101       Hyderabad   |
+--------+-----------------------+
```

The important point is:

> Kafka keeps the latest value for each key, not necessarily the latest offset numbering without gaps.

So you should never assume offsets become contiguous after compaction.

---

## 3. Enable log compaction

You configure it using:

```
cleanup.policy=compact
```

For example:

```
customer-profile
       |
       ↓
cleanup.policy=compact
```

Kafka's background **log cleaner** performs the compaction asynchronously.

It does not happen immediately when a new record arrives.

---

## 4. Very important: compaction is asynchronous

Suppose you produce:

```
101 → Delhi
101 → Pune
101 → Hyderabad
```

Immediately after producing:

```
101 → Delhi
101 → Pune
101 → Hyderabad
```

You may still see all three records.

Later, the log cleaner compacts the relevant segments.

Eventually:

```
101 → Hyderabad
```

remains as the latest state.

So:

```
Produce
  ↓
Records remain
  ↓
Log cleaner runs
  ↓
Compaction
  ↓
Old superseded records removed
```

---

## 5. Kafka does NOT compact the active segment immediately

This is an important internal detail.

Kafka generally compacts inactive/eligible log segments, while the **active segment** is still being appended to.

Conceptually:

```
Partition

+---------+---------+---------+---------+
| Segment | Segment | Segment | Active  |
|   0     |   1     |   2     | Segment |
+---------+---------+---------+---------+
     ↓         ↓         ↓
  eligible for cleaning
```

The active segment is still receiving new records:

```
Producer
   ↓
Active segment
   ↓
append
append
append
```

Once segments roll, older segments can become candidates for cleaning.

---

## 6. What is the Log Cleaner?

Kafka has a background component called the **log cleaner**.

Its responsibility is to identify records that are obsolete and rewrite/clean log segments.

Conceptually:

```
                Kafka Broker
                    |
              Log Cleaner
                    |
        +-----------+-----------+
        |                       |
   Identify segments       Clean records
        |                       |
        +-----------+-----------+
                    ↓
              Replace/merge
                    ↓
             Cleaner log
```

The log cleaner uses a key-based mechanism to determine which records are obsolete.

---

## 7. How does Kafka know which record is latest?

Suppose:

```
offset 10 → user=101 → ACTIVE
offset 20 → user=101 → SUSPENDED
offset 30 → user=101 → ACTIVE
```

Kafka knows that:

```
offset 30
```

is the latest record for:

```
key = 101
```

Therefore:

```
offset 10 → obsolete
offset 20 → obsolete
offset 30 → keep
```

Conceptually:

```
101
 |
 +-- offset 10 ❌
 +-- offset 20 ❌
 +-- offset 30 ✅
```

---

## 8. Compaction happens per partition

This is extremely important.

Kafka ordering and storage are partition-based.

Suppose:

```
customer-profile
```

has:

```
Partition 0
Partition 1
Partition 2
```

Compaction happens **independently within each partition**.

Therefore, Kafka can determine the latest value for a key based on records in that partition.

This is another reason why **key-based partitioning** is important for compacted topics.

---

## 9. Why the key is extremely important

Suppose:

```
key = customerId
```

Then:

```
customer 101
      ↓
same partition
      ↓
multiple updates
      ↓
compaction
      ↓
latest customer state
```

Example:

```
101 → Delhi
101 → Mumbai
101 → Pune
101 → Hyderabad
```

Kafka can compact these records because they have the same key.

---

## 10. What if you use different keys?

Consider:

```
101-A → Delhi
101-B → Mumbai
101-C → Pune
```

Kafka sees:

```
key A
key B
key C
```

These are different keys.

Therefore Kafka does not know that they represent the same customer.

Compaction is based on the **Kafka record key**, not your business interpretation.

This leads to a very important design rule:

> For a compacted topic, the record key should represent the entity whose latest state you want to retain.

---

## 11. Real-world example: Customer Profile

Imagine:

```
customer-profile
```

Events:

```
101 → {
    name: "John",
    city: "Delhi"
}
```

Later:

```
101 → {
    name: "John",
    city: "Mumbai"
}
```

Later:

```
101 → {
    name: "John",
    city: "Pune"
}
```

Kafka may eventually compact this to:

```
101 → {
    name: "John",
    city: "Pune"
}
```

This makes the topic behave somewhat like a distributed changelog/state store.

---

## 12. Real-world example: Product Inventory

Consider:

```
inventory-state
```

Events:

```
product-1001 → quantity=50
product-1001 → quantity=45
product-1001 → quantity=30
product-1001 → quantity=25
```

With compaction:

```
product-1001 → quantity=25
```

The topic represents:

> "What is the current inventory state?"

rather than:

> "Tell me every inventory change that ever happened."

---

## 13. Real-world example: Configuration

Suppose:

```
application-config
```

contains:

```
payment-service.timeout → 5000
payment-service.timeout → 7000
payment-service.timeout → 10000
```

After compaction:

```
payment-service.timeout → 10000
```

This is an excellent use case for log compaction.

---

## 14. Real-world example: Kafka Streams

Kafka Streams frequently uses compacted topics for **changelog/state recovery**.

Conceptually:

```
Application State
       |
       ↓
Changelog Topic
       |
       ↓
Compacted Kafka Log
```

If the application crashes:

```
Application
    ↓
crash
    ↓
restart
    ↓
restore state from Kafka
```

Because the topic retains the latest state for each key, Kafka can be used as a durable source for rebuilding state.

---

## 15. Tombstones

This is one of the most important concepts in log compaction.

Suppose:

```
101 → John
```

Now you want to delete customer 101.

You produce:

```
key = 101
value = null
```

This is called a **tombstone**.

```
101 → null
```

Conceptually:

Before:

```
101 → John
```

After tombstone:

```
101 → null
```

The tombstone tells the compaction process:

> "The latest state for key 101 is deletion."

Eventually Kafka can remove the old value.

---

## 16. Tombstone flow

```
101 → John
       ↓
101 → Mumbai
       ↓
101 → null
       ↓
    Tombstone
       ↓
    Compaction
       ↓
Old values removed
       ↓
Key eventually disappears
```

But the tombstone itself is not necessarily removed immediately.

Kafka needs to retain the tombstone long enough for consumers to observe the deletion.

---

## 17. Why can't Kafka immediately remove tombstones?

Imagine:

```
101 → John
101 → null
```

If Kafka immediately deleted both:

```
101 → John ❌
101 → null ❌
```

a consumer that starts/rebuilds from the compacted log could incorrectly reconstruct:

```
101 → John
```

Therefore Kafka retains tombstones for a period so consumers have an opportunity to observe the deletion.

The relevant configuration includes:

```
delete.retention.ms
```

---

## 18. Compaction does NOT mean "only one record exists"

This is a common interview trap.

People often say:

> "Kafka compaction guarantees only one record per key."

That's an oversimplification.

The correct answer is:

> Kafka compaction eventually removes obsolete records for the same key, but compaction is asynchronous and Kafka does not guarantee that at every moment there is exactly one record per key.

There can temporarily be:

```
101 → Delhi
101 → Mumbai
101 → Pune
```

even after compaction has been enabled.

---

## 19. Compaction does NOT preserve only the latest event history

Suppose:

```
101 → CREATED
101 → PAID
101 → SHIPPED
101 → DELIVERED
```

Compaction is generally **not appropriate** if you need:

```
CREATED
PAID
SHIPPED
DELIVERED
```

as an event history.

Because the latest value may eventually be:

```
101 → DELIVERED
```

The earlier states are candidates for removal.

So:

### Event history

Use:

```
cleanup.policy=delete
```

### Current state

Use:

```
cleanup.policy=compact
```

---

## 20. Retention vs Compaction

This is one of the most important Kafka interview comparisons.

| Feature | Retention/Delete | Compaction |
|---|---|---|
| Main purpose | Keep data for time/size window | Keep latest value per key |
| Based on | Age/size | Key |
| Old records | Deleted when retention applies | Superseded values eventually removed |
| Useful for event history | ✅ | Usually ❌ |
| Useful for current state | Sometimes | ✅ |
| Requires key | No | Yes, for records to participate in key-based compaction |
| Replay all historical events | Possible within retention | Not guaranteed after compaction |
| Tombstones | Not a special compaction mechanism | Important |
| Cleanup | Segment deletion | Log cleaner |

---

## 21. `delete` vs `compact`

Normal topic:

```
cleanup.policy=delete
```

Compacted topic:

```
cleanup.policy=compact
```

You can also combine them:

```
cleanup.policy=compact,delete
```

This is very useful.

It means the topic can use:

```
Compaction
+
Time/size based deletion
```

---

## 22. `compact,delete`

Suppose:

```
cleanup.policy=compact,delete
retention.ms=604800000
```

Conceptually:

```
Same key
   ↓
Older superseded values
   ↓
Compaction

AND

Old segments
   ↓
Retention
   ↓
Deletion
```

This is useful when you want:

> latest state, but only for a bounded period.

---

## 23. Compaction and offsets

This is a very important internal concept.

Suppose:

```
offset 0 → A
offset 1 → B
offset 2 → A
offset 3 → C
offset 4 → A
```

After compaction:

```
offset 1 → B
offset 3 → C
offset 4 → A
```

Notice:

```
0
1
2
3
4
```

became:

```
1
3
4
```

**Kafka does not renumber offsets.**

So:

```
offset ≠ array index
```

Offsets remain immutable logical positions.

---

## 24. Why offsets cannot be renumbered

Suppose a consumer previously committed:

```
offset = 4
```

If Kafka renumbered records after compaction, consumer positions would become meaningless.

Kafka therefore maintains the logical offset model.

Think:

```
Offset
0 → deleted
1 → exists
2 → deleted
3 → exists
4 → exists
```

There can be gaps.

---

## 25. Compaction and consumers

Suppose Consumer A has already processed:

```
offset 0
offset 1
offset 2
offset 3
```

Then compaction happens.

It doesn't change the consumer's committed offset semantics.

Kafka is not saying:

> "Consumer A already read this, so delete it."

Instead:

```
Kafka
  |
  +-- compaction decides which records are obsolete
  |
Consumer
  |
  +-- maintains its own offset
```

Again:

> **Compaction is independent of consumer progress.**

---

## 26. Important: Compaction preserves ordering

Kafka still maintains offset ordering within a partition.

For example:

```
offset 10 → A
offset 20 → B
offset 30 → A
```

After compaction:

```
offset 20 → B
offset 30 → A
```

The remaining records still have their original offsets/order relationship.

Compaction doesn't reorder the records arbitrarily.

---

## 27. How Kafka Log Cleaner works conceptually

At a high level:

```
Partition
   |
   +-------------------+
   |                   |
Older segments      Active segment
   |
   ↓
Log Cleaner
   |
   ↓
Identify duplicate/superseded keys
   |
   ↓
Keep latest relevant value
   |
   ↓
Rewrite cleaned segment
   |
   ↓
Swap cleaned segment
```

A key structure used internally by the cleaner is a **deduplication map/index** that tracks keys encountered and helps determine which records are obsolete.

You don't need to memorize the exact implementation details for most interviews, but you should understand the principle:

```
Key
 ↓
find latest occurrence
 ↓
older occurrence becomes obsolete
```

---

## 28. Compaction is not free

Compaction consumes:

- CPU
- Disk I/O
- Memory

because Kafka has to:

```
Read segments
    ↓
Analyze keys
    ↓
Identify obsolete records
    ↓
Rewrite/clean data
```

Therefore, excessive compaction can create broker resource pressure.

---

## 29. Compaction and disk usage

A common misconception is:

> "If I enable compaction, my disk usage immediately becomes tiny."

**No.**

Suppose:

```
101 → Delhi
101 → Mumbai
101 → Pune
101 → Hyderabad
```

Immediately after producing:

```
4 records
```

During compaction:

```
Old records may still exist
```

Eventually:

```
101 → Hyderabad
```

So compaction is a background cleanup process, not an immediate deduplication mechanism.

---

## 30. Compaction and partitioning

Suppose:

```
customerId = 101
```

Producer uses:

```
key = customerId
```

Then all updates for customer 101 are normally routed to the same partition for a fixed partition count and partitioning configuration.

Example:

```
customer 101
      |
      v
Partition 3
      |
      +-- 101 → Delhi
      +-- 101 → Mumbai
      +-- 101 → Pune
```

Now compaction can identify the older values for key 101.

This is why:

> **Key selection is critical for compacted topics.**

---

## 31. Compaction + Kafka Streams

One of the strongest practical applications is **state restoration**.

Imagine a Kafka Streams application maintains:

```
customerId → account balance
```

State:

```
101 → ₹10,000
102 → ₹20,000
103 → ₹15,000
```

The application maintains a changelog topic.

```
State Store
    |
    ↓
Changelog Topic
    |
    ↓
Compaction
```

If the application crashes:

```
Application crashes
       ↓
Restart
       ↓
Read changelog
       ↓
Restore latest state
       ↓
Continue processing
```

This can make state recovery much faster than replaying an enormous history of every old state update.

---

## 32. Compacted topic as a database-like state snapshot

This is a useful mental model:

Normal Kafka topic:

```
Event history
-------------------------------->
Created → Paid → Shipped → Delivered
```

Compacted topic:

```
Current state
-------------------------------->
Order-101 → DELIVERED
Order-102 → PAID
Order-103 → SHIPPED
```

So:

```
Normal topic
    ≈ event history

Compacted topic
    ≈ latest state per key
```

This is a simplification, but a very useful system-design mental model.

---

## 33. When should you use compaction?

Good use cases:

### Customer state

```
customerId → latest customer profile
```

### Product state

```
productId → latest product information
```

### Inventory state

```
productId → current inventory
```

### Configuration

```
configKey → current configuration
```

### Feature flags

```
feature → current configuration
```

### Changelog topics

```
entityId → latest state
```

### Cache rebuilding

```
Kafka
 ↓
Latest state
 ↓
Rebuild cache
```

---

## 34. When should you NOT use compaction?

Avoid using it when you need complete historical events.

For example:

```
PaymentCreated
PaymentAuthorized
PaymentCaptured
PaymentRefunded
```

If your business requirement is:

> "Show me the complete payment history."

Then pure compaction is generally inappropriate.

You may instead want:

```
cleanup.policy=delete
```

with an appropriate retention period.

---

## 35. Event sourcing vs compacted state

This distinction is important for senior interviews.

### Event sourcing

```
AccountCreated
MoneyDeposited
MoneyWithdrawn
MoneyDeposited
```

You reconstruct:

```
Current Account State
```

from the complete event history.

### Compacted state

Kafka retains the latest state per key:

```
account-101 → balance=₹50,000
```

These are different design approaches.

---

## 36. Complete example

Let's say we have:

```
Topic: user-profile
cleanup.policy=compact
```

Producer sends:

```
offset 0:
key=101
value=John, Delhi

offset 1:
key=102
value=Alice, Mumbai

offset 2:
key=101
value=John, Pune

offset 3:
key=103
value=Bob, Bangalore

offset 4:
key=101
value=John, Hyderabad
```

Before compaction:

```
0 → 101 → John, Delhi
1 → 102 → Alice, Mumbai
2 → 101 → John, Pune
3 → 103 → Bob, Bangalore
4 → 101 → John, Hyderabad
```

After compaction, conceptually:

```
1 → 102 → Alice, Mumbai
3 → 103 → Bob, Bangalore
4 → 101 → John, Hyderabad
```

Notice:

```
offsets are NOT renumbered
```

---

## 37. Add a deletion

Now:

```
offset 5:
key=102
value=null
```

This is a tombstone.

Before cleanup:

```
1 → 102 → Alice, Mumbai
3 → 103 → Bob, Bangalore
4 → 101 → John, Hyderabad
5 → 102 → null
```

Eventually:

```
3 → 103 → Bob, Bangalore
4 → 101 → John, Hyderabad
```

The old 102 value is removed, and eventually the tombstone can also be removed according to tombstone retention/cleanup rules.

---

## 38. Log Retention vs Log Compaction — final picture

```
                    Kafka Topic
                         |
              +----------+----------+
              |                     |
          DELETE policy          COMPACT policy
              |                     |
         Time / Size              Key based
              |                     |
        Old segments            Old values
              |                     |
              ↓                     ↓
           DELETE              KEEP LATEST
```

Or:

```
DELETE:

Old
 ↓
Time/size threshold
 ↓
Segment deletion


COMPACT:

Same key
 ↓
Multiple values
 ↓
Keep latest value
 ↓
Remove obsolete values
```

---

## 39. Senior interview answer

If an interviewer asks:

> "Explain Kafka log compaction."

A strong answer would be:

> Kafka log compaction is a background cleanup mechanism for keyed records where Kafka eventually removes obsolete records and retains the latest value for each key. It is useful for topics representing current state, such as customer profiles, configurations, inventory, and Kafka Streams changelogs. Compaction is asynchronous and segment-based, so multiple versions of a key may temporarily coexist. Offsets are never renumbered, and tombstone records with a `null` value are used to represent deletions. Unlike normal delete retention, compaction is based on keys rather than simply record age.

---

## 40. The most important points to remember

1. `cleanup.policy=compact` enables compaction.
2. Compaction works based on record keys.
3. Kafka eventually keeps the latest value for a key.
4. Compaction is asynchronous.
5. Compaction happens on eligible/inactive log segments.
6. Offsets are NOT renumbered.
7. Multiple records for the same key can temporarily exist.
8. Tombstone = key + null value.
9. Tombstones represent deletion.
10. Compaction is excellent for current-state topics.
11. Compaction is usually not appropriate when complete event history is required.
12. `cleanup.policy=compact,delete` can combine compaction with retention.

---

## One-line mental model

> **Log retention answers: "How long should Kafka keep the history?"
> Log compaction answers: "For each key, which state is still relevant?"**

