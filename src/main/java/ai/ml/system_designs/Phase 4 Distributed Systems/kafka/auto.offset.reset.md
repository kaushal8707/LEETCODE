# auto.offset.reset in Kafka

`auto.offset.reset` is a Kafka consumer configuration that determines:

> What should the consumer do when it does not have a valid committed offset for a partition?

This is one of the most important Kafka consumer concepts.

---

## 1. Why do we need auto.offset.reset?

Suppose Kafka has:

```
Partition 0

Offset:
100
101
102
103
104
105
106
107
```

A consumer wants to consume this partition.

Normally Kafka asks:

> "Where did this consumer group stop last time?"

Kafka checks the group's committed offset.

For example:

```
Consumer Group: payment-service

Committed Offset = 104
```

Kafka starts from:

```
104 → 105 → 106 → 107
```

So far, no problem.

But what if there is no valid committed offset?

That's where:

```
auto.offset.reset
```

comes into play.

---

## 2. The three important values

Modern Kafka has three commonly used values:

```
auto.offset.reset=earliest
auto.offset.reset=latest
auto.offset.reset=none
```

There is also a newer option, `by_duration:<duration>`, in supported Kafka versions, which resets to the earliest offset whose timestamp is within the specified duration.

For interviews, the three classic values are the most important.

---

## 3. earliest

```
auto.offset.reset=earliest
```

Means:

> Start consuming from the earliest available offset when there is no valid committed offset.

Example:

```
Partition 0

100
101
102
103
104
105
```

No committed offset exists.

With:

```
auto.offset.reset=earliest
```

consumer starts at:

```
100
 ↓
101
 ↓
102
 ↓
103
...
```

### Real-world use case

Suppose you create a new consumer group:

```
Group = analytics-service
```

You want it to process all currently retained events.

Use:

```
auto.offset.reset=earliest
```

Common for:

- Analytics
- Data migration
- Backfilling
- Reprocessing historical events
- New services that need existing data

---

## 4. latest

```
auto.offset.reset=latest
```

Means:

> Start consuming from the latest offset when there is no valid committed offset.

Suppose:

```
Partition:

100
101
102
103
104
105
```

The end offset is approximately:

```
106
```

A new consumer group starts.

With:

```
auto.offset.reset=latest
```

the consumer waits for new messages.

If producer sends:

```
106
107
108
```

consumer processes:

```
106
107
108
```

It does not go back and process:

```
100-105
```

### Real-world use case

Imagine:

```
Order Notification Service
```

You deploy it today.

You don't want it to send notifications for orders created six months ago.

You want:

```
New Order
    ↓
Kafka
    ↓
Notification Service
```

So `latest` can be appropriate.

---

## 5. none

```
auto.offset.reset=none
```

Means:

> If there is no valid committed offset, throw an exception instead of automatically choosing an offset.

Conceptually:

```
No valid offset
      ↓
auto.offset.reset=none
      ↓
Exception
```

This is useful when silently choosing `earliest` or `latest` would be dangerous.

Your application can then explicitly decide what to do.

---

## 6. The most important misconception

A lot of developers think:

> "auto.offset.reset=earliest means the consumer always starts from the beginning."

That's incorrect.

It means:

> When Kafka cannot find a valid committed offset, start from the earliest available offset.

For example:

```
Consumer Group
      ↓
Committed offset = 500
      ↓
Start from 500
```

Even if:

```
auto.offset.reset=earliest
```

Kafka doesn't go back to offset 0.

The committed offset wins.

---

## 7. Example

Kafka topic:

```
orders
```

Partition 0:

```
Offset
------
100
101
102
103
104
105
```

Consumer group:

```
order-service
```

### Scenario A — committed offset exists

```
Committed offset = 103
```

Configuration:

```
auto.offset.reset=earliest
```

Result:

```
103 → 104 → 105
```

Not:

```
100 → 101 → 102 → ...
```

---

## 8. Scenario B — no committed offset

Suppose:

```
Committed offset = NONE
```

And:

```
auto.offset.reset=earliest
```

Result:

```
100 → 101 → 102 → 103 → 104 → 105
```

---

## 9. Scenario C — no committed offset + latest

```
Committed offset = NONE

auto.offset.reset=latest
```

Current end:

```
105
```

Consumer starts waiting for new records.

Producer sends:

```
106
```

Then:

```
Consumer
   ↓
106
```

---

## 10. What does "no valid committed offset" actually mean?

There are several situations.

### Case 1 — New consumer group

```
group.id = new-group
```

No committed offset exists.

Therefore:

```
auto.offset.reset
```

is used.

### Case 2 — Offset was deleted

Kafka doesn't retain committed offsets forever under all circumstances.

If the group's committed offset is no longer available, reset behavior can be triggered.

### Case 3 — Consumer's committed offset is outside the retained log

This is extremely important.

Suppose:

```
Consumer committed offset = 100
```

But Kafka retention deleted old records.

Now the earliest available offset is:

```
500
```

So:

```
100  ← no longer exists
```

Kafka can't start from 100.

It needs reset behavior.

With:

```
auto.offset.reset=earliest
```

it starts from:

```
500
```

With:

```
auto.offset.reset=latest
```

it starts from the latest available position.

---

## 11. earliest does NOT mean offset 0

This is another important interview point.

Suppose retention removed:

```
0 - 999
```

Current Kafka log:

```
1000
1001
1002
1003
```

Then:

```
auto.offset.reset=earliest
```

means:

```
1000
```

not:

```
0
```

Because:

> `earliest` means the earliest **available** offset.

---

## 12. Consumer Group Example

Suppose:

```
Topic: orders

Partition 0:
0 1 2 3 4 5 6 7 8 9
```

New consumer group:

```
order-service-v2
```

No committed offset.

### earliest

```
0 → 1 → 2 → 3 → ... → 9
```

### latest

Consumer starts at the end:

```
9
```

and waits for:

```
10 → 11 → 12 ...
```

### none

```
No offset
   ↓
Exception
```

---

## 13. auto.offset.reset vs group.id

These two configurations are closely related.

### group.id

Identifies the consumer group:

```
group.id=payment-service
```

Kafka stores offsets for that group.

### auto.offset.reset

Controls what happens when the group doesn't have a usable offset.

Think:

```
group.id
   ↓
Find committed offset
   ↓
┌─────────────────────┐
│ Valid offset exists?│
└──────────┬──────────┘
           │
      ┌────┴─────┐
     YES         NO
      │           │
      ↓           ↓
Start there   auto.offset.reset
```

This mental model is extremely important.

---

## 14. auto.offset.reset does NOT reset an existing consumer

Suppose:

```
auto.offset.reset=earliest
```

and consumer has:

```
Committed offset = 5000
```

Kafka starts at:

```
5000
```

It doesn't suddenly reset to:

```
0
```

If you actually want to replay messages, you need to change/reset the consumer group's offsets explicitly.

For example, operationally you might reset the group's offsets using Kafka's consumer-group tooling.

---

## 15. What happens during a consumer restart?

Suppose:

```
Consumer
   ↓
Processes offset 100
   ↓
Commits offset 101
```

Consumer crashes.

It restarts.

Kafka sees:

```
Committed offset = 101
```

So it resumes from:

```
101
```

`auto.offset.reset` isn't involved because a valid committed offset exists.

---

## 16. auto.offset.reset and Consumer Lag

Suppose:

```
Latest offset = 1000
Committed offset = 700
```

Consumer lag:

```
1000 - 700 = 300
```

The consumer continues from its committed position.

`auto.offset.reset` doesn't determine normal lag processing.

It mainly matters when the consumer has no valid starting offset.

---

## 17. earliest vs latest

| Setting | Starting point when no valid offset | Typical use |
|---|---|---|
| `earliest` | Earliest available record | Replay/backfill |
| `latest` | End of log | New events only |
| `none` | Throws exception | Explicit error handling |
| `by_duration:...` | Offset based on record timestamp | Time-based replay |

---

## 18. Real Production Example

Imagine an e-commerce system:

```
Order Service
      |
      ↓
    Kafka
      |
      ├── Payment Service
      ├── Inventory Service
      ├── Notification Service
      └── Analytics Service
```

### Analytics

You may want historical events:

```
group.id=analytics
auto.offset.reset=earliest
```

So the analytics service can process retained historical data.

### Notification

You generally care about new events:

```
group.id=notification
auto.offset.reset=latest
```

### Strict processing service

If automatic offset selection would be dangerous:

```
auto.offset.reset=none
```

Then explicitly handle the missing-offset condition.

---

## 19. Interview Question

**Q: What is auto.offset.reset?**

A strong answer:

> `auto.offset.reset` determines where a Kafka consumer should start reading when it does not have a valid committed offset for a partition. `earliest` starts from the earliest available offset, `latest` starts from the end of the log, and `none` throws an exception. It is not used when a valid committed offset already exists.

---

## 20. One diagram to remember

```
                  Consumer starts
                        |
                        ↓
              Does valid offset exist?
                   /           \
                 YES            NO
                  |              |
                  ↓              ↓
          Start from       auto.offset.reset
          committed              |
          offset          ┌──────┼─────────┐
                          ↓      ↓         ↓
                      earliest latest     none
                          |      |          |
                          ↓      ↓          ↓
                       Oldest   Newest    Error
                      available available
```

---

## The golden rule

> `auto.offset.reset` is a fallback, not the normal starting position.

- If a valid committed offset exists → use the committed offset.
- If no valid committed offset exists → apply `auto.offset.reset`.

