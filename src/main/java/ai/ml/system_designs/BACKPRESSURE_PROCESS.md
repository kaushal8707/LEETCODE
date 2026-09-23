# Backpressure Process

Let's use your numbers:

- Client sends = **100 requests/sec**
- Server processes = **10 requests/sec**

Backpressure means the system prevents the faster producer from overwhelming the slower consumer.

---

## 1. Without Backpressure

```
Client
  |
  | 100 req/sec
  ↓
Server
  |
  | Processes only 10/sec
  ↓
Response
```

The server can process only 10, so roughly 90 requests/sec are not being processed immediately.

If requests keep coming at 100/sec, the waiting work keeps increasing:

| Time         | Waiting |
|--------------|---------|
| After 1 sec  | 90      |
| After 2 sec  | 180     |
| After 10 sec | 900     |

Eventually, memory/connection pools/queues can be exhausted.

---

## 2. Backpressure

With backpressure, the system says:

> "I can process only 10/sec, so don't send me 100/sec indefinitely."

There are several ways to implement this.

### Approach A: Rate Limiting

Put a rate limiter in front of the server:

```
              100 req/sec
Client ───────────────────→ Rate Limiter
                              |
                              | 10 req/sec
                              ↓
                           Server
```

The rate limiter allows only 10 requests/sec.

The remaining requests might receive:

**HTTP 429 Too Many Requests**

or the client may retry later.

So:

```
100 incoming
     ↓
Rate Limiter
     ↓
10 accepted
90 rejected/throttled
```

This is one form of backpressure.

---

## 3. Client-Side Backpressure

An even better approach can be for the client itself to slow down.

Initially:

```
Client → 100 req/sec
```

Server responds with signals such as:

```
429 Too Many Requests
Retry-After: 2
```

The client then reduces its request rate:

```
100/sec
 ↓
50/sec
 ↓
20/sec
 ↓
10/sec
```

This is essentially:

**Producer adjusts its production rate based on consumer capacity.**

---

## 4. Queue-Based Backpressure

Now consider asynchronous communication.

```
Client
  |
  | 100/sec
  ↓
Message Queue
  |
  | 10/sec
  ↓
Server
```

The queue absorbs temporary bursts.

But there is a very important point:

**The queue has a finite capacity.**

Suppose the queue can hold 1,000 messages.

Initially:

- Incoming = 100/sec
- Processing = 10/sec
- Queue growth = 90/sec

After approximately 11 seconds:

**Queue ≈ 990 messages**

Now the queue is almost full.

At this point, backpressure kicks in.

The queue can tell the producer:

> "Stop sending. I don't have capacity."

Then the producer may:

- slow down
- retry later
- reject requests
- apply exponential backoff

---

## 5. Kafka Example

This becomes particularly interesting with Kafka.

```
Producer
   |
   | 100 msg/sec
   ↓
 Kafka Topic
   |
   | 10 msg/sec
   ↓
Consumer
```

Suppose the consumer can process only 10 messages/sec.

Kafka retains the messages, so the consumer can process them later.

The important metric here is **consumer lag**.

```
Producer: 100/sec
Consumer: 10/sec

Lag is increasing
        ↓
   90 messages/sec
```

If this continues:

```
        Kafka
          |
          | increasing lag
          ↓
     Consumer
       10/sec
```

You can then add consumers:

```
                  ┌→ Consumer 1 → 10/sec
                  │
Producer → Kafka ─┼→ Consumer 2 → 10/sec
                  │
                  ├→ Consumer 3 → 10/sec
                  │
                  └→ Consumer 4 → 10/sec
```

Now:

**Consumer capacity = 40/sec**

Add more:

```
10 consumers × 10/sec
        =
100/sec
```

Now the system can keep up.

---

## The Important Distinction

Don't think of backpressure as simply:

> "Put a queue in front of the server."

Instead think:

> **Backpressure is a mechanism by which a slower consumer communicates its capacity constraints to a faster producer, causing the producer or intermediary to slow down, buffer, reject, or otherwise control incoming work.**

A queue is often part of the solution, but the queue itself doesn't magically solve an infinite rate mismatch.

---

## Remember This Picture

```
          100 requests/sec
Client ──────────────────────→
                               
                         ┌─────────────┐
                         │   Queue     │
                         └──────┬──────┘
                                │
                                │ 10/sec
                                ↓
                            Server
                            10/sec
                               
                   Queue keeps growing
                           ↓
                    Queue becomes full
                           ↓
                      BACKPRESSURE
                           ↓
              Client must slow/retry/reject
```

And in a real system, you typically combine **backpressure + queue + rate limiting + retries + horizontal scaling** rather than relying on just one mechanism.
