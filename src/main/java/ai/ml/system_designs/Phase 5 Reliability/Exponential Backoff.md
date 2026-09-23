# Exponential Backoff

**Exponential Backoff** is a retry strategy where the waiting time between retry attempts increases exponentially after each failure.

**Simple definition:**

> Exponential Backoff = "If the service keeps failing, wait longer before trying again."

It is one of the most important techniques for preventing **retry storms** in distributed systems.

---

## 1. Why Do We Need Exponential Backoff?

Imagine:

```
Order Service
     |
     v
Payment Service
     |
     X
Temporary failure
```

**Without backoff:**

```
Attempt 1 → Failure
Attempt 2 → Immediately
Attempt 3 → Immediately
Attempt 4 → Immediately
```

If thousands of clients do this:

```
1000 clients
    |
    v
Payment Service
    |
    X Failure
    |
    +---- Retry
    +---- Retry
    +---- Retry
    +---- Retry
```

The service gets hammered with even more requests.

> This is a **retry storm**.

---

## 2. Exponential Backoff Solution

**Instead of retrying immediately:**

```
Attempt 1 → Failure
              ↓
           wait 100ms

Attempt 2 → Failure
              ↓
           wait 200ms

Attempt 3 → Failure
              ↓
           wait 400ms

Attempt 4 → Failure
              ↓
           wait 800ms
```

The delay increases exponentially.

**Typical pattern:**

```
100 ms
200 ms
400 ms
800 ms
1600 ms
3200 ms
```

Usually we also define a **maximum delay**.

For example:

```
maxDelay = 5 seconds
```

So eventually:

```
100ms
200ms
400ms
800ms
1600ms
3200ms
5000ms
5000ms
...
```

---

## 3. Formula

A common formula is:

```
delay = initialDelay × 2^n
```

where:

- `initialDelay` = starting delay
- `n` = retry number

**For example:**

```
initialDelay = 100ms
```

Then:

```
Retry 1 → 100 × 2⁰ = 100ms
Retry 2 → 100 × 2¹ = 200ms
Retry 3 → 100 × 2² = 400ms
Retry 4 → 100 × 2³ = 800ms
Retry 5 → 100 × 2⁴ = 1600ms
```

In production, use a maximum:

```
delay = min(initialDelay × 2^n, maxDelay)
```

---

## 4. Real-World Example

Suppose your application calls an Inventory Service:

```
Order Service
     |
     v
Inventory Service
```

The Inventory Service temporarily returns `503`.

**You configure:**

```
Initial delay = 100ms
Maximum delay = 2 seconds
Max retries = 5
```

**The flow becomes:**

```
Attempt 1
   |
   X 503
   |
 100ms
   |
   v
Attempt 2
   |
   X 503
   |
 200ms
   |
   v
Attempt 3
   |
   X 503
   |
 400ms
   |
   v
Attempt 4
   |
   X 503
   |
 800ms
   |
   v
Attempt 5
   |
   v
Success
```

> The service gets time to recover instead of being continuously hammered.

---

## 5. Why Not Use Fixed Delay?

Another strategy is:

```
Retry 1 → wait 1 sec
Retry 2 → wait 1 sec
Retry 3 → wait 1 sec
Retry 4 → wait 1 sec
```

This is **fixed backoff**.

It can work, but exponential backoff adapts better when the failure persists.

**Compare:**

**Fixed:**

```
Failure
 ↓
1 sec
 ↓
Retry
 ↓
1 sec
 ↓
Retry
 ↓
1 sec
 ↓
Retry
```

**versus:**

**Exponential:**

```
Failure
 ↓
100ms
 ↓
Retry
 ↓
200ms
 ↓
Retry
 ↓
400ms
 ↓
Retry
 ↓
800ms
```

> Exponential backoff gradually reduces pressure on the failing service.

---

## 6. Exponential Backoff + Jitter

This is even more important.

Suppose 10,000 clients experience a failure at exactly the same time.

**Without jitter:**

```
10,000 clients
      |
      v
Failure
      |
      v
wait 1 second
      |
      v
10,000 retries simultaneously
```

> 💥 Another traffic spike.

**With jitter:**

```
Client A → 820ms
Client B → 1.13s
Client C → 940ms
Client D → 1.27s
Client E → 1.05s
```

The retries are distributed over time.

**Without jitter:**

```
Requests
  |
  |             ███████████
  |             ███████████
  +--------------------------> Time
                 1 sec
```

**With jitter:**

```
Requests
  |
  |       ███ █████ ██ ███
  |    ██ ███ ███ ███ ███
  +--------------------------> Time
```

**Therefore:**

> Exponential backoff controls the retry frequency; jitter prevents clients from retrying at the same time.

---

## 7. Types of Jitter

There are several approaches.

### Full Jitter

Conceptually:

```
delay = random(0, exponentialDelay)
```

**Example:**

```
Exponential delay = 800ms

Actual delay:
Client A → 423ms
Client B → 712ms
Client C → 91ms
Client D → 634ms
```

### Equal Jitter

Conceptually:

```
delay = exponentialDelay / 2
        + random(0, exponentialDelay / 2)
```

> This ensures some minimum waiting time while still spreading requests.

### Decorrelated Jitter

The next delay depends on the previous delay plus randomness, producing a less synchronized retry pattern.

For most practical system-design discussions, remember:

> **Exponential backoff + jitter** is the standard mental model.

---

## 8. Exponential Backoff + Maximum Delay

You don't want this:

```
100ms
200ms
400ms
800ms
1600ms
3200ms
6400ms
12800ms
25600ms
...
```

Eventually, retries may take minutes or hours.

**Instead:**

```
maxDelay = 5 seconds
```

Then:

```
100ms
200ms
400ms
800ms
1600ms
3200ms
5000ms
5000ms
```

**Formula:**

```
delay = min(initialDelay × 2^n, maxDelay)
```

---

## 9. Maximum Retry Attempts

Backoff should almost always be combined with a **maximum retry count**.

**Example:**

```
maxRetries = 3
```

**Flow:**

```
Attempt 1
   |
   X
   |
Backoff
   |
Attempt 2
   |
   X
   |
Backoff
   |
Attempt 3
   |
   X
   |
STOP
```

**Don't do:**

```java
while(true) {
    retry();
}
```

because the dependency may be unavailable for hours.

---

## 10. Overall Deadline

There's another subtle problem.

Suppose:

```
Timeout per attempt = 2 seconds
Retries = 5
```

You could potentially spend:

```
2 × 5 = 10 seconds
```

plus backoff delays.

But perhaps the user-facing API has only:

```
5-second deadline
```

So you need an **overall deadline**.

**Example:**

```
Overall deadline = 5 seconds
```

Then:

```
Attempt 1 → timeout
     ↓
Backoff
     ↓
Attempt 2 → timeout
     ↓
Backoff
     ↓
Attempt 3
     ↓
Deadline exceeded
     ↓
STOP
```

> This prevents retries from violating the API's latency budget.

---

## 11. Exponential Backoff + Timeout

These work together:

```
              Request
                 |
                 v
              Timeout
                 |
             Failure?
                 |
                 v
        Exponential Backoff
                 |
               Jitter
                 |
                 v
              Retry
```

**Example:**

```
Timeout = 500ms
Initial backoff = 100ms
Max backoff = 2sec
Max attempts = 3
```

**Flow:**

```
Attempt 1
   |
 500ms timeout
   |
   v
Backoff ~100ms
   |
   v
Attempt 2
   |
 500ms timeout
   |
   v
Backoff ~200ms
   |
   v
Attempt 3
```

---

## 12. Exponential Backoff + Circuit Breaker

These patterns complement each other.

Suppose:

```
Payment Service ❌
```

**Initially:**

```
Request
 ↓
Timeout
 ↓
Backoff
 ↓
Retry
```

**If failures continue:**

```
Failure
Failure
Failure
Failure
Failure
```

**Circuit breaker opens:**

```
CLOSED
   |
   | repeated failures
   v
OPEN
```

Now:

```
Request
   |
   v
Circuit OPEN
   |
   X
Payment Service
```

The system stops wasting resources.

**So:**

> Backoff slows retries; circuit breaker eventually stops retries.

---

## 13. Exponential Backoff + Retry Budget

Suppose your service receives:

```
1000 requests/sec
```

You don't want retries to consume unlimited capacity.

You might define:

```
Retry budget = 5%
```

Meaning approximately:

```
1000 normal requests/sec
+
50 retry requests/sec
```

> This provides protection against retry storms.

---

## 14. Exponential Backoff in Kafka

This connects directly to what you've been learning.

Suppose a Kafka consumer processes:

```
OrderCreated
```

and calls Payment Service:

```
Kafka Consumer
      |
      v
Payment Service
      |
      X
503
```

You don't necessarily want:

```
retry immediately
retry immediately
retry immediately
```

**Instead:**

```
OrderCreated
     |
     v
Payment
     |
     X
    503
     |
     v
Wait
     |
     v
Retry
     |
     X
    503
     |
     v
Longer wait
     |
     v
Retry
```

For longer delays, Kafka systems commonly use **retry topics / delayed retry mechanisms**:

```
orders
   |
   v
consumer
   |
   X
failure
   |
   v
retry-1m
   |
   v
retry-5m
   |
   v
retry-30m
   |
   v
consumer
   |
   X
   |
   v
DLQ
```

> This is particularly useful when the dependency may be unavailable for minutes rather than milliseconds.

---

## 15. Exponential Backoff for Database Failures

Suppose:

```
Application
     |
     v
Database
     |
     X
Temporary connection failure
```

You might retry connection establishment:

```
Attempt 1
 ↓
failure
 ↓
100ms

Attempt 2
 ↓
failure
 ↓
200ms

Attempt 3
 ↓
failure
 ↓
400ms

Attempt 4
 ↓
success
```

But be careful with **database writes**.

A timeout doesn't necessarily mean the database didn't execute the operation.

**For example:**

```
Application
    |
    | INSERT
    v
Database
    |
    | INSERT succeeds
    X
Response lost
```

Application sees:

```
TIMEOUT
```

Blindly retrying could create duplicate data.

**So for writes:**

> Backoff controls **when** to retry, but idempotency controls **whether** retrying is safe.

---

## 16. Retry Decision Flow

A mature distributed system might use this decision process:

```
                Request
                   |
                   v
                Failure
                   |
                   v
             Is it timeout?
              /          \
            YES           NO
             |             |
             v             v
       Is retryable?    Retryable?
          /    \          /    \
        YES    NO       YES    NO
         |      |        |      |
         v      v        v      v
      Retry   Fail     Retry   Fail
         |
         v
  Exponential Backoff
         |
         v
       Jitter
         |
         v
  Deadline exceeded?
       /       \
     YES        NO
      |          |
      v          v
    Stop       Retry
```

---

## 17. Production Configuration Example

A reasonable conceptual configuration might be:

```
Max attempts        = 3
Initial backoff     = 100 ms
Backoff multiplier   = 2
Maximum backoff      = 2 seconds
Jitter               = enabled
Overall deadline     = 5 seconds
Retryable errors     = timeout, connection reset, 502, 503
Circuit breaker      = enabled
Retry budget         = limited
```

**Then:**

```
Attempt 1
    ↓
Failure
    ↓
100ms + jitter
    ↓
Attempt 2
    ↓
Failure
    ↓
200ms + jitter
    ↓
Attempt 3
    ↓
Success / Failure
```

---

## 18. When Should You NOT Use Exponential Backoff?

Don't blindly apply retries.

**Avoid retries when:**

### Permanent failure

```
400 Bad Request
```

> Retrying doesn't fix invalid input.

### Authentication failure

```
401 Unauthorized
```

> Unless credentials/token are refreshed, retrying the same request won't help.

### Authorization failure

```
403 Forbidden
```

### Resource doesn't exist

```
404 Not Found
```

### Business validation failure

- Insufficient balance
- Invalid account
- Invalid product

> Retries don't solve these.

---

## 19. The Complete Pattern

For senior-level system design, remember this combination:

```
                    Request
                       |
                       v
                    Timeout
                       |
                       v
                Retryable error?
                  /          \
                NO            YES
                |              |
                v              v
              Fail       Retry budget?
                              |
                              v
                           Backoff
                              |
                              v
                            Jitter
                              |
                              v
                            Retry
                              |
                              v
                       Still failing?
                              |
                              v
                       Circuit Breaker
                              |
                    +---------+---------+
                    |                   |
                 Fallback              Stop
                    |
                    v
                  Queue
                    |
                    v
             Async processing
```

**For writes:**

```
Retry
  +
Exponential Backoff
  +
Jitter
  +
Timeout
  +
Deadline
  +
Idempotency
```

---

## 20. Interview Answer

If the interviewer asks:

> "What is exponential backoff and why do we use it?"

**A strong senior-level answer:**

> Exponential backoff is a retry strategy where the delay between consecutive retry attempts increases exponentially, typically using a formula such as `min(initialDelay × 2^n, maxDelay)`. It prevents clients from continuously hammering a temporarily unhealthy dependency and helps reduce retry storms. In production, I would combine exponential backoff with jitter to prevent synchronized retries, a maximum retry count, an overall request deadline, and a retry budget. For critical writes, I would also ensure idempotency because retrying after a timeout can otherwise duplicate the operation. A circuit breaker can be added to stop retries altogether when the dependency remains unhealthy.

---

## The 6 Concepts You Should Remember

```
TIMEOUT
   ↓
Stop waiting

RETRY
   ↓
Try again

EXPONENTIAL BACKOFF
   ↓
Wait progressively longer

JITTER
   ↓
Randomize retry timing

CIRCUIT BREAKER
   ↓
Stop calling an unhealthy service

IDEMPOTENCY
   ↓
Make retries safe
```

### One-line mental model

> "Timeout detects the problem, retry tries again, exponential backoff slows the retries, jitter spreads them out, circuit breaker eventually stops them, and idempotency makes the retry safe."

