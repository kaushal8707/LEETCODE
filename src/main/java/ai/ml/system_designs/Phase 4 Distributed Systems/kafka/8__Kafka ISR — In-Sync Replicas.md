# Kafka ISR — In-Sync Replicas

**ISR** stands for **In-Sync Replicas**.

It is one of the most important Kafka concepts because it connects:

> **Replication → Fault Tolerance → Data Durability → `acks=all` → `min.insync.replicas`**

ISR is the set of replicas of a partition that are considered sufficiently caught up with the partition leader.

---

## 1. Start with Replication

Suppose we have:

```
Topic: order-events
Partition: P0
Replication Factor = 3
```

Kafka stores P0 on three brokers:

```
Broker 1 → P0 Leader
Broker 2 → P0 Follower
Broker 3 → P0 Follower
```

Initially, all three replicas are healthy and caught up:

```
P0

Broker 1 → Leader   ✓
Broker 2 → Follower ✓
Broker 3 → Follower ✓
```

Therefore:

```
ISR = {Broker 1, Broker 2, Broker 3}
```

---

## 2. Why does Kafka need ISR?

Imagine the leader has processed:

```
Offset 100
Offset 101
Offset 102
Offset 103
```

and the followers have replicated those records:

```
Leader B1:
100 101 102 103

Follower B2:
100 101 102 103

Follower B3:
100 101 102 103
```

All replicas are caught up.

So:

```
ISR = B1, B2, B3
```

Now suppose B3 becomes slow:

```
Leader B1:
100 101 102 103 104 105 106

Follower B2:
100 101 102 103 104 105 106

Follower B3:
100 101 102
```

B3 is significantly behind.

Kafka can remove B3 from the ISR:

```
ISR = {B1, B2}
```

Now:

```
B1 → Leader
B2 → In-Sync Follower
B3 → Out of Sync
```

---

## 3. Simple definition

Think of ISR as:

```
Leader
  |
  +-- Replica A ✓
  |
  +-- Replica B ✓
  |
  +-- Replica C ✗
```

Then:

```
ISR = Leader + Replica A + Replica B
```

Replica C is not currently part of ISR.

---

## 4. What does "In-Sync" actually mean?

It does **not** simply mean:

> "The follower has some copy of the data."

It means the replica is considered sufficiently caught up according to Kafka's replication/liveness rules.

A follower can temporarily fall behind.

If it falls too far behind or is unavailable long enough, Kafka can remove it from the ISR.

---

## 5. ISR changes dynamically

ISR is not permanent.

Suppose:

Initial:

```
ISR = {B1, B2, B3}
```

B3 becomes unavailable:

```
B3 → ❌
```

Kafka updates:

```
ISR = {B1, B2}
```

Later B3 recovers and catches up:

```
B3:
100
101
102
103
104
105
106
```

Once it satisfies Kafka's criteria for being caught up/in sync again:

```
ISR = {B1, B2, B3}
```

So:

```
In Sync
   ↓
Falls behind
   ↓
Removed from ISR
   ↓
Catches up
   ↓
Rejoins ISR
```

---

## 6. ISR and Leader Election

This is where ISR becomes extremely important.

Suppose:

```
P0

B1 → Leader
B2 → Follower
B3 → Follower

ISR = {B1, B2, B3}
```

B1 crashes:

```
B1 → ❌
```

Kafka can elect an eligible in-sync replica, for example:

```
B2 → New Leader
B3 → Follower
```

So:

Before:

```
B1 → Leader
B2 → Follower
B3 → Follower
```

After:

```
B1 → ❌
B2 → New Leader
B3 → Follower
```

The reason B2 is a good candidate is that it was in the ISR and therefore was sufficiently caught up.

---

## 7. What if a replica is NOT in ISR?

Consider:

```
B1 → Leader
B2 → ISR
B3 → Out of ISR
```

Then B1 crashes.

Kafka normally prefers an eligible ISR replica:

```
B2 → New Leader
```

rather than:

```
B3 → New Leader
```

because B3 may be missing recent records.

For example:

```
Leader B1:
100 101 102 103 104

B2:
100 101 102 103 104

B3:
100 101
```

If B3 became leader, offsets 102–104 could be unavailable from that replica.

That's why ISR is central to Kafka's durability model.

---

## 8. ISR + `acks=all`

Now we connect ISR with producer acknowledgements.

Suppose:

```
Replication Factor = 3

B1 → Leader
B2 → Follower
B3 → Follower

ISR = {B1, B2, B3}
```

Producer sends:

```
OrderCreated
```

with:

```
acks=all
```

Conceptually:

```
Producer
    |
    v
B1 Leader
    |
    +----> B2 ✓
    |
    +----> B3 ✓
```

Kafka waits for the required in-sync replicas according to the ISR and `min.insync.replicas` configuration before acknowledging the write.

This gives stronger durability than simply acknowledging after the leader receives the record.

---

## 9. ISR + `min.insync.replicas`

This is a very important interview combination.

Suppose:

```
Replication Factor = 3

min.insync.replicas = 2
```

Initially:

```
B1 → Leader ✓
B2 → Follower ✓
B3 → Follower ✓

ISR = 3
```

Everything is healthy.

Now B3 falls behind:

```
B1 → Leader ✓
B2 → Follower ✓
B3 → Out of ISR ✗
```

Now:

```
ISR = 2
```

Since:

```
min.insync.replicas = 2
```

there are still enough in-sync replicas for `acks=all` writes.

---

## 10. What if another replica fails?

Suppose B2 also becomes unavailable:

```
B1 → Leader ✓
B2 → Out of ISR ✗
B3 → Out of ISR ✗
```

Now:

```
ISR = 1
```

But:

```
min.insync.replicas = 2
```

Therefore:

```
ISR < min.insync.replicas
```

An `acks=all` produce request cannot satisfy the configured minimum ISR requirement.

Kafka can reject the write rather than accepting it with insufficient in-sync replicas.

This is an intentional **durability vs availability** trade-off.

---

## 11. Why would Kafka reject writes?

At first this seems strange:

> "Why doesn't Kafka just accept the message?"

Because accepting it could increase the risk of losing the record if the remaining leader fails before another replica catches up.

With:

```
RF = 3
min.insync.replicas = 2
acks = all
```

you are effectively saying:

> "Don't acknowledge my write unless at least two replicas are in sync."

This is a common production durability strategy.

---

## 12. Important difference: Replication Factor vs ISR

These are frequently confused.

### Replication Factor

How many replicas are **configured** for the partition.

Example:

```
RF = 3
```

means:

```
3 replicas exist
```

### ISR

How many of those replicas are **currently sufficiently in sync**.

Example:

```
RF = 3

B1 → Leader
B2 → In Sync
B3 → Out of Sync
```

Then:

```
Replication Factor = 3
ISR size = 2
```

So:

> Replication Factor is the configured number of replicas; ISR is the currently in-sync subset of those replicas.

---

## 13. Important difference: ISR vs Followers

Another common interview question.

A partition can have:

```
B1 → Leader
B2 → Follower + ISR
B3 → Follower + ISR
B4 → Follower but not ISR
```

So:

```
Follower ≠ necessarily ISR
```

A follower can exist but be out of sync.

Think:

```
All replicas
     |
     +----------------+
     |                |
   Leader          Followers
                      |
                +-----+-----+
                |           |
              ISR       Out of ISR
```

---

## 14. ISR and Consumer

ISR has **nothing to do with consumer groups**.

Don't confuse:

> ISR

with:

> Consumer Group

### ISR

Deals with:

- Partition replication
- Leader
- Followers
- Fault tolerance

### Consumer Group

Deals with:

- Partition consumption
- Consumer assignment
- Parallel processing
- Rebalancing

So:

```
                 Kafka Partition
                       |
          +------------+------------+
          |                         |
     Replication                 Consumption
          |                         |
    Leader/Follower            Consumer Group
          |                         |
         ISR                    Consumers
```

---

## 15. Real-world payment example

Suppose:

```
Topic = payment-events
Partition = P0
Replication Factor = 3
```

Cluster:

```
Broker 1 → P0 Leader
Broker 2 → P0 Follower
Broker 3 → P0 Follower
```

ISR:

```
{B1, B2, B3}
```

Payment event:

```
PAYMENT_COMPLETED
```

is written to P0.

All replicas catch up:

```
B1 → PAYMENT_COMPLETED ✓
B2 → PAYMENT_COMPLETED ✓
B3 → PAYMENT_COMPLETED ✓
```

Then B1 crashes.

Because B2 and B3 were in ISR:

```
B2 → New Leader
B3 → Follower
```

The system can continue operating with the new leader.

That's the practical purpose of ISR.

---

## 16. ISR and data loss

Suppose:

```
Leader B1:
100 101 102 103 104

Follower B2:
100 101 102 103 104

Follower B3:
100 101
```

ISR:

```
B1, B2
```

B1 fails.

B2 becomes leader:

```
B2:
100 101 102 103 104
```

The latest data is preserved on B2.

But B3 is behind:

```
B3:
100 101
```

This illustrates why Kafka prefers an in-sync replica for leadership.

---

## 17. What happens when the follower catches up?

Suppose B3 was behind:

```
B3:
100 101
```

Leader has:

```
100 101 102 103 104
```

B3 replicates:

```
100 101 102 103 104
```

Once it meets the required synchronization criteria:

```
B3 → rejoins ISR
```

Now:

```
ISR = {B1, B2, B3}
```

So ISR is dynamic.

---

## 18. The complete flow

This is the mental model you should remember:

```
                   Kafka Partition P0
                          |
             +------------+------------+
             |            |            |
             v            v            v
          Broker 1     Broker 2     Broker 3
           Leader       Follower      Follower
             |             |             |
             |             |             |
             +-------------+-------------+
                           |
                          ISR
                           |
                    In-Sync Replicas
                           |
              +------------+------------+
              |                         |
        Leader failure            Producer durability
              |                         |
              v                         v
        Leader Election            acks=all
              |                         |
              v                    min.insync.replicas
       New Leader
```

---

## 19. Interview questions

### Q1. What is ISR?

> ISR stands for In-Sync Replicas. It is the set of partition replicas that are sufficiently caught up with the leader and eligible for certain replication and leadership decisions.

### Q2. Is the leader part of ISR?

**Yes**, normally the leader is part of the ISR.

### Q3. Can a follower be outside ISR?

**Yes.**

A follower can fall behind or become unavailable and be removed from ISR.

### Q4. What happens when a follower falls behind?

Kafka can remove it from ISR. Once it catches up and satisfies the synchronization criteria, it can rejoin.

### Q5. What happens if the leader fails?

Kafka can elect an eligible in-sync replica as the new leader.

### Q6. What is the difference between RF and ISR?

```
RF = total configured replicas
ISR = currently in-sync replicas
```

Example:

```
RF = 3
ISR = 2
```

### Q7. How does ISR relate to `acks=all`?

> `acks=all` requires the write to satisfy the replication acknowledgement rules involving the current ISR, including `min.insync.replicas` where configured.

### Q8. What happens when ISR size becomes less than `min.insync.replicas`?

> With `acks=all`, Kafka can reject produce requests because the required number of in-sync replicas is not available.

---

## 20. One interview scenario to remember

Suppose the interviewer gives you:

```
Replication Factor = 3
min.insync.replicas = 2
acks = all
```

Initially:

```
B1 → Leader
B2 → Follower
B3 → Follower

ISR = {B1, B2, B3}
```

Then B3 fails:

```
B1 → Leader
B2 → Follower
B3 → ❌

ISR = {B1, B2}
```

**Can producers still write?**

Yes, assuming the other required conditions are healthy, because:

```
ISR size = 2
min.insync.replicas = 2
```

Then B2 also becomes unavailable:

```
B1 → Leader
B2 → ❌
B3 → ❌

ISR = {B1}
```

Now:

```
ISR size = 1
min.insync.replicas = 2
```

With `acks=all`, the write cannot satisfy the minimum ISR requirement.

This protects durability but reduces availability.

---

## 21. The key relationship

Memorize this:

```
Replication Factor
        ↓
Number of replicas
        ↓
Some replicas may fall behind
        ↓
ISR changes
        ↓
ISR affects leader election
        ↓
ISR + min.insync.replicas
        ↓
Durability of acks=all writes
```

---

## ⭐ Interview-ready answer

> ISR, or In-Sync Replicas, is the set of replicas of a Kafka partition that are sufficiently caught up with the leader. The leader is normally part of the ISR, and followers that fall too far behind can be removed from it. ISR is important for fault tolerance because Kafka prefers an eligible ISR replica when electing a new leader. It is also important for producer durability: with `acks=all`, `min.insync.replicas` can require a minimum number of ISR members before a write is acknowledged.

