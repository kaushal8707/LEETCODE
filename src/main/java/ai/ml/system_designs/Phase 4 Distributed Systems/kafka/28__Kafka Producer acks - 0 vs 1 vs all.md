# Kafka Producer acks: 0 vs 1 vs all

The Kafka producer's `acks` setting controls how much acknowledgment the producer requires from the broker before considering a message successfully written.

This is directly related to the **durability vs latency vs availability** trade-off.

```
Producer
   |
   |  acks = ?
   v
Kafka Leader
   |
   +---- Follower 1
   |
   +---- Follower 2
```

The three important values are:

- `acks=0`
- `acks=1`
- `acks=all`

---

## 1. `acks=0`

### Meaning

The producer does **not** wait for an acknowledgment from the broker.

```
Producer
   |
   | send
   v
Kafka Broker
```

Producer essentially says:

> "I've sent the request. I don't need Kafka to acknowledge it."

### Flow

```
Producer
   |
   | Record
   v
Broker
   |
   X
Producer doesn't wait
```

The producer considers the send successful without waiting for broker acknowledgment.

### Failure scenario

Suppose:

```
Producer
   |
   | OrderCreated
   v
Network
   |
   X
Kafka
```

The network fails.

The producer may not know whether Kafka received the record.

Therefore:

```
acks=0
    ↓
Very low latency
    ↓
High risk of message loss
```

### Advantages

- Lowest producer-side acknowledgment latency
- Maximum throughput potential
- Minimal waiting

### Disadvantages

- No broker acknowledgment
- Producer cannot reliably detect many broker-side failures for the request
- Potential message loss
- Poor choice for critical business events

### Suitable for

Potentially:

- metrics
- telemetry
- non-critical logging
- disposable events

Not ideal for:

- payments
- orders
- financial transactions
- inventory changes.

---

## 2. `acks=1`

This is the **leader acknowledgment** mode.

```
Producer
   |
   | Record
   v
Leader
   |
   | append
   v
Leader's local log
   |
   | ACK
   v
Producer
```

The producer waits for the partition leader to acknowledge the write.

### Important point

`acks=1` does **not** mean:

> "All replicas have received the message."

It means approximately:

> The leader has successfully appended the record and acknowledged it.

Followers may still be replicating asynchronously.

---

## 3. `acks=1` failure scenario

Suppose:

```
RF = 3

       P0
        |
   +----+----+
   |         |
  B1        B2        B3
Leader    Follower  Follower
```

Producer sends:

```
PaymentCompleted
```

B1 appends it:

```
B1 → PaymentCompleted
```

B1 sends ACK:

```
B1
 |
 ACK
 |
Producer
```

But before B2/B3 replicate:

```
B1 ❌
```

Potentially:

```
B2 → does not have the latest record
B3 → does not have the latest record
```

Now a new leader is elected from eligible replicas.

Depending on replication state and leader-election rules, the record may not be present on the new leader.

So:

```
acks=1
    ↓
Lower latency
    ↓
Better than acks=0
    ↓
But weaker durability than acks=all
```

---

## 4. `acks=all`

`acks=all` means the producer requests the strongest acknowledgment mode: the leader waits for the required in-sync replica acknowledgment condition.

```
Producer
   |
   | Record
   v
Leader
   |
   +----------+
   |          |
   v          v
Follower 1  Follower 2
   |          |
   +----------+
        |
   required ISR
        |
        v
       ACK
        |
        v
    Producer
```

This provides the strongest producer acknowledgment guarantee among these three settings.

But there's an important detail:

> `acks=all` does not necessarily mean every configured replica must be alive and acknowledge.

It is tied to the **ISR** and Kafka's acknowledgment rules.

---

## 5. `acks=all` + ISR

Suppose:

```
RF = 3
```

and:

```
ISR = B1, B2, B3
```

Then:

```
Producer
   |
   v
B1 Leader
   |
   +---- B2
   |
   +---- B3
```

With `acks=all`, the leader waits according to the ISR acknowledgment requirements before acknowledging the producer.

Now suppose B3 falls behind:

```
ISR = B1, B2
```

B3 is no longer in the ISR.

The configured RF is still:

```
RF = 3
```

but:

```
ISR = 2
```

This distinction is critical.

---

## 6. `acks=all` + `min.insync.replicas`

This is where Kafka durability configuration becomes much more powerful.

Suppose:

```
RF = 3
min.insync.replicas = 2
acks = all
```

Initially:

```
ISR = 3
```

Write succeeds.

Now one replica fails:

```
ISR = 2
```

Write can still succeed.

Now two replicas fail:

```
ISR = 1
```

We have:

```
ISR < min.insync.replicas
```

Therefore the broker should reject an `acks=all` write rather than accepting it with insufficient in-sync replicas.

This is an important production pattern.

---

## 7. Why `acks=all` alone isn't enough

This is a common interview trap.

Suppose:

```
RF = 3
acks = all
```

but:

```
ISR = 1
```

If you want to guarantee that at least two replicas are in sync before accepting writes, you need:

```
min.insync.replicas = 2
```

So a common durable configuration is:

```
acks=all
min.insync.replicas=2
```

with:

```
replication.factor=3
```

Conceptually:

```
                 Kafka
                   |
        +----------+----------+
        |          |          |
       B1         B2         B3
     Leader     Follower    Follower
        \          |          /
         \---------+---------/
                  ISR
                   |
          min ISR = 2
                   |
             Write allowed
```

---

## 8. Side-by-Side Comparison

| Configuration | Producer waits for | Durability | Latency | Throughput potential |
|---|---|---|---|---|
| `acks=0` | Nothing | Lowest | Lowest | Highest |
| `acks=1` | Leader | Medium | Low | High |
| `acks=all` | Required ISR acknowledgment | Highest | Higher | Lower than weaker modes |

The exact latency/throughput impact depends on network, workload, batching, replication, broker load, and configuration.

---

## 9. Simple Real-World Example

Imagine a payment system.

```
Payment Service
      |
      | PaymentCompleted
      v
    Kafka
```

### `acks=0`

```
Payment Service
      |
      | send
      v
    Kafka

No ACK required
```

If the network/broker fails:

```
PaymentCompleted
       ↓
Potentially LOST
```

Very dangerous for a payment event.

### `acks=1`

```
Payment Service
      |
      v
Kafka Leader
      |
      | ACK
      v
Payment Service
```

The leader has the record, but followers may not yet have it.

Better, but still weaker than `acks=all`.

### `acks=all`

```
Payment Service
      |
      v
Kafka Leader
      |
   +--+--+
   |     |
   v     v
 F1     F2
   \     /
    \   /
     ACK
      |
      v
Payment Service
```

The producer gets the acknowledgment only after the required ISR condition is satisfied.

For critical events, this is generally the preferred direction.

---

## 10. `acks` and Producer Retries

This is another important connection.

Suppose:

```
acks=1
```

Producer sends:

```
OrderCreated
```

Leader successfully writes:

```
Leader → OrderCreated
```

But ACK is lost:

```
Leader
   |
   X
   |
Producer
```

Producer thinks:

> "Maybe the write failed."

It retries.

Without producer idempotence:

```
Leader:
OrderCreated
OrderCreated
```

Potential duplicate.

This is why:

```
acks
+
retries
+
idempotence
```

must be considered together.

---

## 11. `acks` + Idempotent Producer

A strong production configuration commonly includes:

```
acks=all
enable.idempotence=true
```

Idempotence protects against duplicate appends caused by producer retries.

Conceptually:

```
Producer
   |
   | seq=10
   v
Kafka
   |
   X ACK lost
   |
Producer retries
   |
   | seq=10
   v
Kafka
   |
   | recognizes duplicate
   X
```

So:

```
acks=all
       ↓
Stronger durability

idempotence=true
       ↓
Duplicate-safe retries
```

They solve different problems.

---

## 12. `acks=0` vs `acks=1` vs `acks=all`

Think about the amount of waiting:

```
acks=0

Producer ───────────────> Kafka
       doesn't wait


acks=1

Producer ─────> Leader
                  |
                 ACK
                  |
                  v
              Producer


acks=all

Producer ─────> Leader
                  |
             +----+----+
             |         |
             v         v
            F1        F2
             |         |
             +----+----+
                  |
              required
             ISR condition
                  |
                 ACK
                  |
                  v
              Producer
```

---

## 13. Performance Trade-off

Think of the spectrum:

```
                  Reliability
                      ↑
                      |
acks=0  ──────────────┼────────────── acks=all
                      |
                      |
Latency               ↓
```

More accurately:

```
acks=0
  ↓
Less waiting
  ↓
Lower latency
  ↓
Less durability assurance


acks=1
  ↓
Wait for leader
  ↓
Moderate durability


acks=all
  ↓
Wait for required ISR acknowledgment condition
  ↓
Strongest durability among these modes
```

---

## 14. Important Interview Question

### Q: Does `acks=all` mean all three replicas must acknowledge?

**Answer: Not necessarily.**

If:

```
RF = 3
ISR = 2
```

then `acks=all` works with the current ISR according to Kafka's acknowledgment rules.

If you require:

> At least 2 replicas in sync

configure:

```
min.insync.replicas=2
```

So:

```
RF = 3
ISR = 2
min.insync.replicas = 2
acks = all
```

can accept writes.

But:

```
RF = 3
ISR = 1
min.insync.replicas = 2
acks = all
```

should reject writes.

---

## 15. Important Interview Question

### Q: Which is safer, `acks=1` or `acks=all`?

**`acks=all`**, assuming the replication/ISR configuration is healthy.

```
acks=1
   ↓
Leader acknowledgment

acks=all
   ↓
Required ISR acknowledgment condition
```

`acks=all` provides stronger durability at the cost of potentially higher latency and reduced availability when insufficient ISR replicas are available.

---

## 16. Important Interview Question

### Q: Does `acks=all` guarantee exactly-once?

**No.**

This is a very important distinction.

```
acks=all
    ↓
Producer durability/acknowledgment

idempotence
    ↓
Duplicate-safe producer retries

transactions
    ↓
Atomic Kafka operations

consumer idempotency
    ↓
Duplicate-safe business processing
```

`acks=all` by itself does not provide exactly-once processing.

---

## 17. Important Interview Question

### Q: What happens if all followers are down?

Suppose:

```
RF = 3

B1 = Leader
B2 = ❌
B3 = ❌
```

Potentially:

```
ISR = B1
```

If:

```
acks=all
min.insync.replicas=2
```

then:

```
ISR = 1
min ISR = 2

1 < 2
```

Producer writes should fail.

This is intentional.

Kafka is effectively saying:

> "I don't have enough healthy replicas to satisfy your durability requirement, so I won't accept the write."

That's a classic **durability vs availability** trade-off.

---

## 18. The Production Configuration to Remember

For a critical business topic, a commonly used pattern is:

```
replication.factor=3
min.insync.replicas=2
acks=all
enable.idempotence=true
```

Conceptually:

```
              Producer
                  |
          idempotent producer
                  |
               acks=all
                  |
                  v
             Kafka Leader
              /       \
             /         \
           F1           F2
            \           /
             \         /
                ISR
                 |
           min ISR = 2
                 |
             Durable write
```

This is not a universal configuration — you still need to consider workload, availability requirements, broker topology, and failure domains.

---

## 19. `acks` vs Replication Factor

These are often confused.

### Replication Factor

Answers:

> How many copies should Kafka maintain?

```
RF = 3

B1
B2
B3
```

### `acks`

Answers:

> How much acknowledgment does the producer require before considering the send successful?

```
acks=0
acks=1
acks=all
```

### `min.insync.replicas`

Answers:

> How many replicas must be in sync for an `acks=all` write to be accepted?

Together:

```
Replication Factor
        ↓
Number of copies

ISR
        ↓
Currently in-sync copies

min.insync.replicas
        ↓
Minimum acceptable ISR

acks
        ↓
Producer acknowledgment requirement
```

---

## 20. One-Line Memory Trick

Remember:

```
acks=0
→ "Don't wait."

acks=1
→ "Leader, tell me."

acks=all
→ "Wait for the required ISR acknowledgment condition."
```

And for a senior interview:

> **`acks` controls producer acknowledgment semantics. `acks=0` provides no broker acknowledgment, `acks=1` waits for the partition leader, and `acks=all` waits for the leader's required in-sync replica acknowledgment condition. For critical data, `acks=all` is commonly combined with `replication.factor=3`, `min.insync.replicas=2`, and idempotent production to provide stronger durability and retry safety.**

# Kafka Producer acks: 0 vs 1 vs all

The Kafka producer's `acks` setting controls how much acknowledgment the producer requires from the broker before considering a message successfully written.

This is directly related to the **durability vs latency vs availability** trade-off.

```
Producer
   |
   |  acks = ?
   v
Kafka Leader
   |
   +---- Follower 1
   |
   +---- Follower 2
```

The three important values are:

- `acks=0`
- `acks=1`
- `acks=all`

---

## 1. `acks=0`

### Meaning

The producer does **not** wait for an acknowledgment from the broker.

```
Producer
   |
   | send
   v
Kafka Broker
```

Producer essentially says:

> "I've sent the request. I don't need Kafka to acknowledge it."

### Flow

```
Producer
   |
   | Record
   v
Broker
   |
   X
Producer doesn't wait
```

The producer considers the send successful without waiting for broker acknowledgment.

### Failure scenario

Suppose:

```
Producer
   |
   | OrderCreated
   v
Network
   |
   X
Kafka
```

The network fails.

The producer may not know whether Kafka received the record.

Therefore:

```
acks=0
    ↓
Very low latency
    ↓
High risk of message loss
```

### Advantages

- Lowest producer-side acknowledgment latency
- Maximum throughput potential
- Minimal waiting

### Disadvantages

- No broker acknowledgment
- Producer cannot reliably detect many broker-side failures for the request
- Potential message loss
- Poor choice for critical business events

### Suitable for

Potentially:

- metrics
- telemetry
- non-critical logging
- disposable events

Not ideal for:

- payments
- orders
- financial transactions
- inventory changes.

---

## 2. `acks=1`

This is the **leader acknowledgment** mode.

```
Producer
   |
   | Record
   v
Leader
   |
   | append
   v
Leader's local log
   |
   | ACK
   v
Producer
```

The producer waits for the partition leader to acknowledge the write.

### Important point

`acks=1` does **not** mean:

> "All replicas have received the message."

It means approximately:

> The leader has successfully appended the record and acknowledged it.

Followers may still be replicating asynchronously.

---

## 3. `acks=1` failure scenario

Suppose:

```
RF = 3

       P0
        |
   +----+----+
   |         |
  B1        B2        B3
Leader    Follower  Follower
```

Producer sends:

```
PaymentCompleted
```

B1 appends it:

```
B1 → PaymentCompleted
```

B1 sends ACK:

```
B1
 |
 ACK
 |
Producer
```

But before B2/B3 replicate:

```
B1 ❌
```

Potentially:

```
B2 → does not have the latest record
B3 → does not have the latest record
```

Now a new leader is elected from eligible replicas.

Depending on replication state and leader-election rules, the record may not be present on the new leader.

So:

```
acks=1
    ↓
Lower latency
    ↓
Better than acks=0
    ↓
But weaker durability than acks=all
```

---

## 4. `acks=all`

`acks=all` means the producer requests the strongest acknowledgment mode: the leader waits for the required in-sync replica acknowledgment condition.

```
Producer
   |
   | Record
   v
Leader
   |
   +----------+
   |          |
   v          v
Follower 1  Follower 2
   |          |
   +----------+
        |
   required ISR
        |
        v
       ACK
        |
        v
    Producer
```

This provides the strongest producer acknowledgment guarantee among these three settings.

But there's an important detail:

> `acks=all` does not necessarily mean every configured replica must be alive and acknowledge.

It is tied to the **ISR** and Kafka's acknowledgment rules.

---

## 5. `acks=all` + ISR

Suppose:

```
RF = 3
```

and:

```
ISR = B1, B2, B3
```

Then:

```
Producer
   |
   v
B1 Leader
   |
   +---- B2
   |
   +---- B3
```

With `acks=all`, the leader waits according to the ISR acknowledgment requirements before acknowledging the producer.

Now suppose B3 falls behind:

```
ISR = B1, B2
```

B3 is no longer in the ISR.

The configured RF is still:

```
RF = 3
```

but:

```
ISR = 2
```

This distinction is critical.

---

## 6. `acks=all` + `min.insync.replicas`

This is where Kafka durability configuration becomes much more powerful.

Suppose:

```
RF = 3
min.insync.replicas = 2
acks = all
```

Initially:

```
ISR = 3
```

Write succeeds.

Now one replica fails:

```
ISR = 2
```

Write can still succeed.

Now two replicas fail:

```
ISR = 1
```

We have:

```
ISR < min.insync.replicas
```

Therefore the broker should reject an `acks=all` write rather than accepting it with insufficient in-sync replicas.

This is an important production pattern.

---

## 7. Why `acks=all` alone isn't enough

This is a common interview trap.

Suppose:

```
RF = 3
acks = all
```

but:

```
ISR = 1
```

If you want to guarantee that at least two replicas are in sync before accepting writes, you need:

```
min.insync.replicas = 2
```

So a common durable configuration is:

```
acks=all
min.insync.replicas=2
```

with:

```
replication.factor=3
```

Conceptually:

```
                 Kafka
                   |
        +----------+----------+
        |          |          |
       B1         B2         B3
     Leader     Follower    Follower
        \          |          /
         \---------+---------/
                  ISR
                   |
          min ISR = 2
                   |
             Write allowed
```

---

## 8. Side-by-Side Comparison

| Configuration | Producer waits for | Durability | Latency | Throughput potential |
|---|---|---|---|---|
| `acks=0` | Nothing | Lowest | Lowest | Highest |
| `acks=1` | Leader | Medium | Low | High |
| `acks=all` | Required ISR acknowledgment | Highest | Higher | Lower than weaker modes |

The exact latency/throughput impact depends on network, workload, batching, replication, broker load, and configuration.

---

## 9. Simple Real-World Example

Imagine a payment system.

```
Payment Service
      |
      | PaymentCompleted
      v
    Kafka
```

### `acks=0`

```
Payment Service
      |
      | send
      v
    Kafka

No ACK required
```

If the network/broker fails:

```
PaymentCompleted
       ↓
Potentially LOST
```

Very dangerous for a payment event.

### `acks=1`

```
Payment Service
      |
      v
Kafka Leader
      |
      | ACK
      v
Payment Service
```

The leader has the record, but followers may not yet have it.

Better, but still weaker than `acks=all`.

### `acks=all`

```
Payment Service
      |
      v
Kafka Leader
      |
   +--+--+
   |     |
   v     v
 F1     F2
   \     /
    \   /
     ACK
      |
      v
Payment Service
```

The producer gets the acknowledgment only after the required ISR condition is satisfied.

For critical events, this is generally the preferred direction.

---

## 10. `acks` and Producer Retries

This is another important connection.

Suppose:

```
acks=1
```

Producer sends:

```
OrderCreated
```

Leader successfully writes:

```
Leader → OrderCreated
```

But ACK is lost:

```
Leader
   |
   X
   |
Producer
```

Producer thinks:

> "Maybe the write failed."

It retries.

Without producer idempotence:

```
Leader:
OrderCreated
OrderCreated
```

Potential duplicate.

This is why:

```
acks
+
retries
+
idempotence
```

must be considered together.

---

## 11. `acks` + Idempotent Producer

A strong production configuration commonly includes:

```
acks=all
enable.idempotence=true
```

Idempotence protects against duplicate appends caused by producer retries.

Conceptually:

```
Producer
   |
   | seq=10
   v
Kafka
   |
   X ACK lost
   |
Producer retries
   |
   | seq=10
   v
Kafka
   |
   | recognizes duplicate
   X
```

So:

```
acks=all
       ↓
Stronger durability

idempotence=true
       ↓
Duplicate-safe retries
```

They solve different problems.

---

## 12. `acks=0` vs `acks=1` vs `acks=all`

Think about the amount of waiting:

```
acks=0

Producer ───────────────> Kafka
       doesn't wait


acks=1

Producer ─────> Leader
                  |
                 ACK
                  |
                  v
              Producer


acks=all

Producer ─────> Leader
                  |
             +----+----+
             |         |
             v         v
            F1        F2
             |         |
             +----+----+
                  |
              required
             ISR condition
                  |
                 ACK
                  |
                  v
              Producer
```

---

## 13. Performance Trade-off

Think of the spectrum:

```
                  Reliability
                      ↑
                      |
acks=0  ──────────────┼────────────── acks=all
                      |
                      |
Latency               ↓
```

More accurately:

```
acks=0
  ↓
Less waiting
  ↓
Lower latency
  ↓
Less durability assurance


acks=1
  ↓
Wait for leader
  ↓
Moderate durability


acks=all
  ↓
Wait for required ISR acknowledgment condition
  ↓
Strongest durability among these modes
```

---

## 14. Important Interview Question

### Q: Does `acks=all` mean all three replicas must acknowledge?

**Answer: Not necessarily.**

If:

```
RF = 3
ISR = 2
```

then `acks=all` works with the current ISR according to Kafka's acknowledgment rules.

If you require:

> At least 2 replicas in sync

configure:

```
min.insync.replicas=2
```

So:

```
RF = 3
ISR = 2
min.insync.replicas = 2
acks = all
```

can accept writes.

But:

```
RF = 3
ISR = 1
min.insync.replicas = 2
acks = all
```

should reject writes.

---

## 15. Important Interview Question

### Q: Which is safer, `acks=1` or `acks=all`?

**`acks=all`**, assuming the replication/ISR configuration is healthy.

```
acks=1
   ↓
Leader acknowledgment

acks=all
   ↓
Required ISR acknowledgment condition
```

`acks=all` provides stronger durability at the cost of potentially higher latency and reduced availability when insufficient ISR replicas are available.

---

## 16. Important Interview Question

### Q: Does `acks=all` guarantee exactly-once?

**No.**

This is a very important distinction.

```
acks=all
    ↓
Producer durability/acknowledgment

idempotence
    ↓
Duplicate-safe producer retries

transactions
    ↓
Atomic Kafka operations

consumer idempotency
    ↓
Duplicate-safe business processing
```

`acks=all` by itself does not provide exactly-once processing.

---

## 17. Important Interview Question

### Q: What happens if all followers are down?

Suppose:

```
RF = 3

B1 = Leader
B2 = ❌
B3 = ❌
```

Potentially:

```
ISR = B1
```

If:

```
acks=all
min.insync.replicas=2
```

then:

```
ISR = 1
min ISR = 2

1 < 2
```

Producer writes should fail.

This is intentional.

Kafka is effectively saying:

> "I don't have enough healthy replicas to satisfy your durability requirement, so I won't accept the write."

That's a classic **durability vs availability** trade-off.

---

## 18. The Production Configuration to Remember

For a critical business topic, a commonly used pattern is:

```
replication.factor=3
min.insync.replicas=2
acks=all
enable.idempotence=true
```

Conceptually:

```
              Producer
                  |
          idempotent producer
                  |
               acks=all
                  |
                  v
             Kafka Leader
              /       \
             /         \
           F1           F2
            \           /
             \         /
                ISR
                 |
           min ISR = 2
                 |
             Durable write
```

This is not a universal configuration — you still need to consider workload, availability requirements, broker topology, and failure domains.

---

## 19. `acks` vs Replication Factor

These are often confused.

### Replication Factor

Answers:

> How many copies should Kafka maintain?

```
RF = 3

B1
B2
B3
```

### `acks`

Answers:

> How much acknowledgment does the producer require before considering the send successful?

```
acks=0
acks=1
acks=all
```

### `min.insync.replicas`

Answers:

> How many replicas must be in sync for an `acks=all` write to be accepted?

Together:

```
Replication Factor
        ↓
Number of copies

ISR
        ↓
Currently in-sync copies

min.insync.replicas
        ↓
Minimum acceptable ISR

acks
        ↓
Producer acknowledgment requirement
```

---

## 20. One-Line Memory Trick

Remember:

```
acks=0
→ "Don't wait."

acks=1
→ "Leader, tell me."

acks=all
→ "Wait for the required ISR acknowledgment condition."
```

And for a senior interview:

> **`acks` controls producer acknowledgment semantics. `acks=0` provides no broker acknowledgment, `acks=1` waits for the partition leader, and `acks=all` waits for the leader's required in-sync replica acknowledgment condition. For critical data, `acks=all` is commonly combined with `replication.factor=3`, `min.insync.replicas=2`, and idempotent production to provide stronger durability and retry safety.**
