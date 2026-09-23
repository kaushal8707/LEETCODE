# Kafka At-Most-Once Delivery

**At-most-once delivery** means:

> A message is delivered **zero or one time**, but never more than once.

In simple words:

```
Message
   ↓
0 times OR 1 time
   ↓
NEVER duplicated
```

The trade-off is that **messages can be lost**.

---

## 1. The three Kafka delivery semantics

This is very important for interviews:

| Semantics | Duplicate possible? | Message loss possible? |
|---|---|---|
| At-most-once | ❌ No | ✅ Yes |
| At-least-once | ✅ Yes | ❌ Normally no |
| Exactly-once | ❌ No | ❌ Within the supported transactional scope |

Think of them as:

```
At-most-once
    ↓
No duplicates
    ↓
But messages may be lost


At-least-once
    ↓
No message loss
    ↓
But duplicates may occur


Exactly-once
    ↓
No duplicates
    +
No loss
```

---

## 2. How At-Most-Once works

The key idea is:

> **Commit the consumer offset BEFORE processing the message.**

Flow:

```
Kafka
  |
  | Message A
  v
Consumer
  |
  | Commit offset
  v
Kafka
  |
  | Offset committed
  |
  v
Process Message A
```

Now suppose the consumer crashes during processing:

```
Kafka
  |
  | Message A
  v
Consumer
  |
  | Commit offset ✓
  |
  X CRASH
  |
  | Message A was never processed
```

When the consumer restarts:

```
Kafka
  |
  | Start from next offset
  v
Consumer
```

Message A is not processed again.

Therefore:

```
Message A
    ↓
0 times
```

This satisfies at-most-once.

---

## 3. Real-world example

Imagine an email notification service.

Kafka contains:

```
Offset 100 → Send welcome email
Offset 101 → Send password reset email
Offset 102 → Send promotion email
```

Consumer receives offset 100.

It first commits:

```
Commit offset 101
```

Then:

```
Process offset 100
     ↓
Send welcome email
```

But suppose the application crashes after committing and before sending:

```
Commit ✓
   ↓
CRASH
   ↓
Email ❌
```

After restart:

```
Consumer starts from offset 101
```

Offset 100 won't be processed again.

So the email is lost.

That's the fundamental trade-off.

---

## 4. Why commit BEFORE processing?

Because the goal is to prevent duplicates.

Consider:

```
Message
   ↓
Commit offset
   ↓
Process
```

If processing succeeds:

```
Commit ✓
Process ✓
```

Good.

If processing fails:

```
Commit ✓
Process ❌
```

Message is lost.

If the consumer crashes:

```
Commit ✓
   ↓
CRASH
   ↓
Message won't be processed again
```

So:

```
At-most-once
=
Commit first
+
Process later
```

---

## 5. Compare with At-Least-Once

This is one of the most important interview comparisons.

### At-most-once

```
Consume
   ↓
Commit offset
   ↓
Process
```

### At-least-once

```
Consume
   ↓
Process
   ↓
Commit offset
```

The order is reversed.

### At-most-once failure

```
Consume
   ↓
Commit ✓
   ↓
CRASH
   ↓
Process ❌
```

Result:

```
Message LOST
```

### At-least-once failure

```
Consume
   ↓
Process ✓
   ↓
CRASH
   ↓
Commit ❌
```

After restart:

```
Consume same message again
        ↓
Process again
```

Result:

```
DUPLICATE
```

---

## 6. Side-by-side

```
          AT-MOST-ONCE
          
Kafka
  |
  v
Consume
  |
  v
Commit Offset
  |
  v
Process
  |
  X
Crash?
  |
  v
Message may be LOST
```

Versus:

```
          AT-LEAST-ONCE

Kafka
  |
  v
Consume
  |
  v
Process
  |
  X
Crash?
  |
  v
Offset NOT committed
  |
  v
Process again
  |
  v
DUPLICATE
```

---

## 7. Kafka configuration

A typical Kafka consumer configuration for at-most-once behavior would use manual offset management and commit before processing.

Conceptually:

```
enable.auto.commit=false
```

Then:

```java
consumer.poll();

consumer.commitSync();  // commit first

processRecords();       // process after commit
```

The exact implementation depends on the consumer framework and how records are batched.

The important concept is:

```
OFFSET COMMIT
      ↓
PROCESS MESSAGE
```

not the specific API call.

---

## 8. What happens if Kafka crashes?

Suppose:

```
Kafka
  |
  | Message offset 100
  v
Consumer
```

Consumer commits offset 101 before processing.

If Kafka later crashes:

```
Offset 101 already committed
```

After recovery, the consumer resumes from the committed position.

Therefore:

```
Offset 100
   ↓
won't be processed again
```

Again, this avoids duplicates but means a failure between commit and processing can lose the message.

---

## 9. When would we use At-Most-Once?

At-most-once can make sense when duplicates are more harmful than losing an occasional message.

Examples:

### Metrics

```
Application
    ↓
Kafka
    ↓
Metrics Consumer
```

If one metric is lost occasionally, that may be acceptable.

### Non-critical telemetry

```
Device
  ↓
Kafka
  ↓
Telemetry
```

Missing one telemetry event may be preferable to duplicate processing.

### High-volume logging

Some logging/analytics pipelines may tolerate occasional loss to avoid duplicate processing overhead.

---

## 10. When should you NOT use it?

Avoid at-most-once for critical business events such as:

- PaymentCompleted
- OrderCreated
- MoneyTransferred
- InvoiceGenerated
- BankTransaction
- StockUpdated

Imagine:

```
PaymentCompleted
       ↓
Commit offset
       ↓
Consumer crashes
       ↓
Payment event never processed
```

You could end up with:

```
Payment = SUCCESS

But downstream:
PaymentCompleted event = LOST
```

That can create serious consistency problems.

---

## 11. At-most-once and Producer Idempotence

Don't confuse these concepts.

### Producer idempotence

Protects the **producer → Kafka** side:

```
Producer
   |
   | retry
   v
Kafka
   ↓
Prevent duplicate append
```

### At-most-once

Controls the **Kafka → Consumer** processing side:

```
Kafka
   |
   v
Consumer
   |
   | commit first
   v
Process
```

So:

```
Producer Idempotence
        ↓
Producer → Kafka reliability


At-Most-Once
        ↓
Kafka → Consumer processing semantics
```

They solve different problems.

---

## 12. At-most-once does NOT mean zero duplicates everywhere

At-most-once refers to the delivery/processing semantics of the consumer flow.

It doesn't magically guarantee that your entire distributed system can never generate duplicate business effects.

For example:

```
Kafka
  ↓
Consumer
  ↓
External API
```

Your external API may have its own retry mechanisms or duplicate requests from other parts of the system.

So always define **where** the delivery guarantee applies.

---

## 13. Interview scenario

### Question:

> A Kafka consumer processes a message and then crashes before committing the offset. What delivery semantic does this represent?

If the consumer uses:

```
Process
   ↓
Commit
```

and crashes before commit:

```
Process ✓
Commit ❌
```

The message will be consumed again.

That's:

> **At-least-once**, and duplicate processing is possible.

### Question:

> How do you achieve at-most-once?

Answer:

> Commit the offset before processing the message. If the consumer crashes after committing but before processing, Kafka will not redeliver that message. Therefore duplicates are avoided, but message loss is possible.

---

## 14. The easiest way to remember

Memorize this:

### AT-MOST-ONCE

```
Commit
  ↓
Process

Risk:
Message LOST
```

### AT-LEAST-ONCE

```
Process
  ↓
Commit

Risk:
Message DUPLICATED
```

### EXACTLY-ONCE

```
Process + Offset
      ↓
Atomic Transaction
      ↓
Commit

Goal:
No loss + No duplicate
```

---

And the most important interview statement:

> **At-most-once prioritizes avoiding duplicates over avoiding message loss. The consumer commits the offset before processing, so a crash after the commit can cause the message to be skipped.**

