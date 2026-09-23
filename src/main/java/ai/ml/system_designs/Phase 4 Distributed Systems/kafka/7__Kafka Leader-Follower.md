# Kafka Leader / Follower

**Leader/Follower** is Kafka's replication mechanism for providing high availability and fault tolerance.

The core idea is:

> Each partition has one **Leader** replica that handles client requests, while one or more **Follower** replicas maintain copies of that partition's data.

---

## 1. Start with a Kafka partition

Suppose we have:

```
Topic: order-events

Partition 0
```

Kafka doesn't necessarily store Partition 0 on only one broker.

With replication factor = 3:

```
Broker 1
   |
   +---- Partition 0 (Leader)

Broker 2
   |
   +---- Partition 0 (Follower)

Broker 3
   |
   +---- Partition 0 (Follower)
```

So we have:

```
                 Partition 0
                     |
          +----------+----------+
          |          |          |
          v          v          v
       Leader     Follower   Follower
       Broker 1   Broker 2   Broker 3
```

---

## 2. Why do we need Leader and Followers?

Imagine Kafka stores an order event only on Broker 1:

```
Broker 1
   |
   +---- P0
```

If Broker 1 crashes:

```
Broker 1
   X
 DOWN
```

Partition 0 becomes unavailable.

Potentially, data could also be lost if it wasn't replicated elsewhere.

Kafka solves this using replication:

```
Broker 1 → Leader
Broker 2 → Follower
Broker 3 → Follower
```

If Broker 1 fails:

```
Broker 1
   X
 DOWN

Broker 2
   |
   +---- becomes Leader
```

Kafka can continue serving the partition from the new leader, assuming the necessary replica is available and the cluster remains healthy.

---

## 3. Leader handles client requests

For a partition, the leader replica is the primary replica for client operations.

Conceptually:

```
Producer
   |
   v
Leader
   |
   +----> Follower
   |
   +----> Follower
```

Similarly, consumers normally fetch partition data from the leader under the traditional leader-based replication model.

So:

```
Producer
    |
    v
Partition Leader
    |
    +----> Follower
    |
    +----> Follower
```

The followers replicate the leader's log.

---

## 4. Example of producing a message

Suppose:

```
P0 Leader = Broker 1
```

Producer sends:

```
OrderCreated
```

The conceptual flow is:

```
Producer
   |
   | OrderCreated
   v
Broker 1
Leader P0
   |
   +------------> Broker 2
   |               Follower P0
   |
   +------------> Broker 3
                   Follower P0
```

The followers replicate the record from the leader.

---

## 5. Each partition has its own leader

This is very important.

Suppose:

```
Topic: order-events

P0
P1
P2
```

You might have:

```
Broker 1 → P0 Leader
Broker 2 → P1 Leader
Broker 3 → P2 Leader
```

So **leadership is per partition**, not one global leader for the entire topic.

For example:

```
Broker 1
   |
   +-- P0 Leader
   +-- P1 Follower
   +-- P2 Follower


Broker 2
   |
   +-- P0 Follower
   +-- P1 Leader
   +-- P2 Follower


Broker 3
   |
   +-- P0 Follower
   +-- P1 Follower
   +-- P2 Leader
```

This allows leadership and workload to be distributed across brokers.

---

## 6. Replication Factor

The number of copies of a partition is called the **replication factor**.

For example:

```
Replication Factor = 3
```

means:

```
1 Leader
2 Followers
```

Total:

```
3 replicas
```

Example:

```
P0

Broker 1 → Leader
Broker 2 → Follower
Broker 3 → Follower
```

If:

```
Replication Factor = 2
```

then:

```
1 Leader
1 Follower
```

---

## 7. What happens when the Leader fails?

This is one of the most important Kafka interview questions.

Initially:

```
P0

Broker 1 → Leader
Broker 2 → Follower
Broker 3 → Follower
```

Broker 1 fails:

```
Broker 1
   X
 DOWN
```

Kafka needs a new leader.

For example:

```
Broker 2 → New Leader
Broker 3 → Follower
```

Conceptually:

Before:

```
       P0
        |
   +----+----+
   |    |    |
  B1   B2   B3
  L    F    F
```

After B1 failure:

```
       P0
        |
     +--+--+
     |     |
    B2    B3
    L     F
```

The new leader can then handle requests for P0.

---

## 8. Who decides the new Leader?

Kafka has a control-plane mechanism responsible for managing partition leadership and cluster metadata.

Modern Kafka uses **KRaft** rather than ZooKeeper for this role.

Conceptually:

```
Kafka Cluster
      |
      v
Controller / KRaft metadata quorum
      |
      v
Detects broker failure
      |
      v
Elects new partition leader
```

Don't confuse:

> Partition Leader

with:

> Kafka Controller / KRaft

They are different concepts.

---

## 9. What is ISR?

**ISR = In-Sync Replicas**

This is one of the most important concepts related to Leader/Follower.

Suppose:

```
P0

Broker 1 → Leader
Broker 2 → Follower
Broker 3 → Follower
```

If all replicas are sufficiently caught up:

```
ISR = {Broker 1, Broker 2, Broker 3}
```

Conceptually:

```
        P0
         |
   +-----+-----+
   |     |     |
   B1    B2    B3
   L     F     F
   ✓     ✓     ✓

   All are in ISR
```

---

## 10. What if a follower falls behind?

Suppose Broker 3 has a problem:

```
Broker 1 → Leader ✓
Broker 2 → Follower ✓
Broker 3 → Follower ❌
```

Broker 3 isn't keeping up with the leader.

Kafka can remove Broker 3 from the ISR:

```
ISR = {Broker 1, Broker 2}
```

So:

```
P0

B1 → Leader     ✓
B2 → Follower   ✓
B3 → Follower   ✗

ISR = B1, B2
```

Once Broker 3 catches up sufficiently, it can become part of the ISR again.

---

## 11. Why is ISR important?

Because Kafka's durability guarantees depend heavily on which replicas are in sync.

Suppose:

```
ISR:

B1 → Leader
B2 → Follower
```

and:

```
B3 → Out of sync
```

If B1 fails, Kafka should prefer an in-sync replica for leadership.

```
B1
 X

B2 → New Leader
```

B2 has the latest replicated data among the eligible in-sync replicas.

---

## 12. `acks=all` and Leader/Follower

Now we connect producer acknowledgements with replication.

Suppose:

```
P0

B1 → Leader
B2 → Follower
B3 → Follower
```

Producer sends:

```
OrderCreated
```

with:

```
acks=all
```

Conceptually, Kafka waits for the required in-sync replicas according to the topic's `min.insync.replicas` configuration before acknowledging the write.

For example:

```
Producer
   |
   v
Leader B1
   |
   +----> B2 ✓
   |
   +----> B3 ✓
   |
   v
ACK
   |
   v
Producer
```

This provides stronger durability than simply acknowledging after the leader alone has accepted the record.

---

## 13. `min.insync.replicas`

Suppose:

```
Replication Factor = 3
```

and:

```
min.insync.replicas = 2
```

Then Kafka requires at least 2 in-sync replicas for certain `acks=all` writes to succeed.

For example:

```
B1 → Leader ✓
B2 → Follower ✓
B3 → Follower ✓

ISR = 3
```

Writes can proceed.

If B3 falls out of ISR:

```
B1 → Leader ✓
B2 → Follower ✓
B3 → Out of ISR
```

Then:

```
ISR = 2
```

Writes can still proceed with `acks=all`.

If another replica falls out:

```
B1 → Leader ✓
B2 → Out of ISR
B3 → Out of ISR
```

Then:

```
ISR = 1
```

With:

```
min.insync.replicas = 2
```

an `acks=all` write cannot satisfy the required ISR count, so Kafka rejects the write rather than accepting a write with insufficient in-sync replicas.

This is a common production durability configuration.

---

## 14. Leader vs Follower

| Feature | Leader | Follower |
|---|---|---|
| Handles partition writes | Yes | No |
| Primary partition read path | Yes* | No* |
| Receives replication | N/A | Yes |
| Maintains copy of log | Yes | Yes |
| Can become leader | Already leader | Yes, if eligible |
| Part of ISR | Yes, normally | Yes, if caught up |

> \* Kafka has evolved to support additional consumer-fetch/read paths in newer versions/configurations, but the traditional and most important interview model is leader-based client access.

---

## 15. Leader/Follower and partition ordering

Suppose:

```
P0 Leader:

Offset 100 → A
Offset 101 → B
Offset 102 → C
```

Followers replicate the same log:

```
Follower 1:

100 → A
101 → B
102 → C
```

```
Follower 2:

100 → A
101 → B
102 → C
```

So the replicas maintain copies of the partition's ordered log.

The important point:

> Replication does not create a different ordering; followers replicate the partition log established by the leader.

---

## 16. Leader election

Suppose:

```
P0

B1 → Leader
B2 → Follower
B3 → Follower
```

B1 crashes.

Kafka's controller/KRaft metadata mechanism selects an eligible replica:

```
B2 → New Leader
```

Now:

```
Producer
   |
   v
B2
New Leader
   |
   +----> B3
```

Clients refresh metadata and send future requests to the new leader.

---

## 17. What if all replicas are not in sync?

This is where Kafka's durability behavior becomes interesting.

Suppose:

```
P0

B1 → Leader
B2 → ISR
B3 → Out of sync
```

Then B1 fails.

Kafka generally prefers an in-sync replica as the new leader.

```
B2 → New Leader
```

B3 should not normally be preferred if it is not sufficiently caught up.

There are configurations affecting whether an out-of-sync replica can be used in exceptional circumstances, but doing so can risk data loss.

For production systems, the key principle is:

> Prefer an in-sync replica to preserve committed data.

---

## 18. Leader/Follower in a real-world payment system

Imagine:

```
Topic: payment-events

Partition 0
```

Replication factor:

```
3
```

Architecture:

```
Broker 1
   |
   +-- P0 Leader

Broker 2
   |
   +-- P0 Follower

Broker 3
   |
   +-- P0 Follower
```

Payment Service publishes:

```
PaymentCompleted
```

Flow:

```
Payment Service
      |
      v
Broker 1
P0 Leader
      |
      +--------> Broker 2
      |           Follower
      |
      +--------> Broker 3
                  Follower
```

Now Broker 1 crashes:

```
Broker 1
   X
```

Kafka elects Broker 2:

```
Broker 2
   |
   +-- P0 New Leader
```

Payment producers can continue after metadata updates.

This provides high availability.

---

## 19. Leader/Follower vs Consumer

Don't confuse these.

### Leader/Follower

This is about replication of Kafka partitions:

```
Leader
   ↓
Followers
```

### Consumer

This is about reading records from Kafka:

```
Kafka
  ↓
Consumer
```

### Complete picture

```
                   Kafka Cluster
                        |
              +---------+---------+
              |                   |
           Broker 1            Broker 2
              |                   |
           P0 Leader          P0 Follower
              |
              +------------------+
                                 |
                              Consumer
```

---

## 20. Leader/Follower vs Consumer Group

Another distinction:

```
Leader/Follower
       ↓
Replication
       ↓
High availability
```

Whereas:

```
Consumer Group
       ↓
Partition assignment
       ↓
Parallel processing
```

So:

```
Kafka Partition
      |
      +--------------------+
      |                    |
Replication            Consumption
      |                    |
Leader/Follower       Consumer Group
      |                    |
Fault tolerance       Parallelism
```

---

## 21. Interview question: Why not let all replicas accept writes?

A common question is:

> Why does Kafka use one leader per partition instead of allowing all replicas to accept writes?

Because having one leader establishes a clear authoritative ordering for that partition.

Imagine:

```
Producer A → Broker 1
Producer B → Broker 2
Producer C → Broker 3
```

If all independently accepted writes, Kafka would need to resolve:

> Which record came first?

With a single leader:

```
Producer A ──┐
Producer B ──┼──> Leader ──> ordered log
Producer C ──┘
```

The leader establishes the append order.

Followers replicate that log.

This simplifies:

- ordering
- replication
- consistency
- failure handling

---

## 22. The complete internal flow

This is the picture you should remember:

```
                     Producer
                        |
                        | produce
                        v
                +---------------+
                | Partition     |
                | Leader        |
                | Broker 1      |
                +---------------+
                   /          \
                  /            \
             replicate       replicate
                /                \
               v                  v
       +---------------+   +---------------+
       | Follower      |   | Follower      |
       | Broker 2      |   | Broker 3      |
       +---------------+   +---------------+
               \                  /
                \                /
                 +------ ISR ----+
```

If the leader fails:

```
                Broker 1
                  X
                 DOWN
                  |
                  v
          Controller / KRaft
                  |
                  v
             Leader Election
                  |
             +----+----+
             |         |
             v         v
           B2          B3
        New Leader   Follower
```

---

## 23. Interview-ready answer

If the interviewer asks:

> Explain Kafka Leader and Follower.

A strong answer is:

> Kafka replicates each partition across multiple brokers according to the replication factor. One replica acts as the leader and is the primary replica for client operations, while the other replicas act as followers and replicate the leader's partition log. Followers that are sufficiently caught up are part of the ISR, or In-Sync Replicas. If the leader fails, Kafka's controller/KRaft metadata mechanism can elect an eligible in-sync replica as the new leader. This provides fault tolerance and high availability. Producer settings such as `acks=all` and `min.insync.replicas` can be used together to strengthen durability guarantees.

---

## 24. The 5 concepts you should connect

At this point, connect these concepts:

```
                 Kafka Partition
                       |
            +----------+----------+
            |                     |
         Leader                Followers
            |                     |
      Client requests        Replication
            |                     |
            +----------+----------+
                       |
                      ISR
                       |
              Failure detection
                       |
                Leader election
                       |
                  New Leader
```

And remember these definitions:

- **Leader** → primary replica for a partition.
- **Follower** → replica that copies the leader's log.
- **Replication Factor** → total number of replicas.
- **ISR** → replicas sufficiently caught up with the leader.
- **Leader Election** → choosing a new eligible leader after failure.
- **KRaft/Controller** → manages cluster metadata and partition leadership changes.

