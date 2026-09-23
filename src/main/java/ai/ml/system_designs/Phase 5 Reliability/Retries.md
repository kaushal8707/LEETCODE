# Retries in System Design

A **retry** means attempting an operation again after the first attempt fails or times out.

**Simple definition:**

> Retry = "The operation failed temporarily, so try again."

Retries are extremely useful in distributed systems because many failures are transient.

For example:

```
Order Service
     |
     | Request
     v
Payment Service
     |
     X
Temporary network failure
```

**Instead of immediately failing:**

```
Request → Failure → Return error
```

we can do:

```
Request
   |
   X
Failure
   |
   v
Retry
   |
   v
Payment Service
   |
   v
Success
```

---

## 1. Why Do We Need Retries?

Distributed systems frequently experience temporary failures:

- Network packet loss
- Connection reset
- Temporary service overload
- Database connection failure
- Leader election
- Kafka broker transition
- Load balancer issue
- Temporary DNS/network problem
- Cloud infrastructure transient failure

**Example:**

```
Client
  |
  v
Order Service
  |
  v
Payment Service
  |
  X
Temporary network failure
```

The Payment Service itself may be perfectly healthy.

A retry can recover:

```
Attempt 1 → Network failure
Attempt 2 → Success
```

---

## 2. Retry Should NOT Be Used for Every Failure

This is one of the most important concepts.

Suppose:

```
POST /payment
```

returns:

```
400 Bad Request
```

> Retrying usually doesn't help.

**Why?**

Because the request itself is invalid.

Similarly:

```
401 Unauthorized
403 Forbidden
404 Not Found
```

usually shouldn't be blindly retried.

**But transient failures may be retryable:**

- Timeout
- Connection reset
- Temporary network failure
- 503 Service Unavailable
- Some 429 Too Many Requests responses

> The exact retry policy depends on the API and error semantics.

---

## 3. Basic Retry

Suppose:

```
Retry count = 3
```

Then:

```
Attempt 1
   |
   X Failure
   |
Attempt 2
   |
   X Failure
   |
Attempt 3
   |
   v
Success
```

If all attempts fail:

```
Attempt 1 → Failure
Attempt 2 → Failure
Attempt 3 → Failure
              |
              v
          Return error
```

> Never retry forever.

---

## 4. Why Infinite Retries Are Dangerous

Imagine:

```
Order Service
      |
      v
Payment Service ❌
```

**If Order Service continuously retries:**

```
Retry
Retry
Retry
Retry
Retry
Retry
...
```

you can create:

> **Retry Storm**

The dependency is already unhealthy, and retries generate even more traffic.

This can turn:

```
Small failure
```

into:

```
Large outage
```

---

## 5. Retry Storm

Suppose:

```
10,000 requests
```

Each request retries 3 times.

**Potentially:**

```
10,000 original requests
+
30,000 retry attempts
=
40,000 requests
```

The downstream service is already overloaded.

Now it receives 4× the traffic.

```
             10,000 requests
                    |
                    v
              Service B
                    |
                 overload
                    |
          +---------+---------+
          |         |         |
        retry     retry     retry
          |         |         |
          +---------+---------+
                    |
                    v
             More overload
```

> This is why retry design matters.

---

## 6. Exponential Backoff

Instead of retrying immediately, wait between attempts.

For example:

```
Attempt 1 → failure
       ↓
     wait 100ms

Attempt 2 → failure
       ↓
     wait 200ms

Attempt 3 → failure
       ↓
     wait 400ms

Attempt 4 → failure
```

This is called:

> **Exponential Backoff**

A common conceptual formula is:

```
delay = initialDelay × 2^attempt
```

For example:

```
Initial delay = 100ms

Retry 1 → 100ms
Retry 2 → 200ms
Retry 3 → 400ms
Retry 4 → 800ms
```

Usually we also impose a maximum:

```
maxDelay = 5 seconds
```

so the delay doesn't grow indefinitely.

---

## 7. Jitter

Exponential backoff alone has another problem.

Imagine 1,000 clients experience the same failure:

```
10:00:00 → failure
```

They all calculate:

```
retry after 1 second
```

So at:

```
10:00:01
```

all 1,000 clients retry simultaneously.

That's another traffic spike.

> This is called a **thundering herd** problem.

### Jitter adds randomness

**Instead of:**

```
Client A → 1 sec
Client B → 1 sec
Client C → 1 sec
```

**we get:**

```
Client A → 0.82 sec
Client B → 1.17 sec
Client C → 0.94 sec
Client D → 1.31 sec
```

Now requests are spread out.

**Without jitter:**

```
|████████████████████| 1000 requests
                    time
```

**With jitter:**

```
|███|██|████|███|█|██|  spread over time
```

**Therefore:**

> Exponential backoff controls how quickly retries happen; jitter prevents synchronized retries.

---

## 8. Retry Policy

A production retry policy usually contains:

```
Retry Policy
    |
    +-- Maximum attempts
    |
    +-- Retryable errors
    |
    +-- Initial delay
    |
    +-- Backoff strategy
    |
    +-- Maximum delay
    |
    +-- Jitter
    |
    +-- Overall deadline
```

**For example:**

```
Max attempts       = 3
Initial delay      = 100 ms
Backoff             = exponential
Max delay           = 2 sec
Jitter              = enabled
Overall deadline    = 5 sec
Retryable errors    = timeout, connection reset, 503
```

---

## 9. Retry Budget

This is a more advanced concept.

Suppose your service normally handles:

```
1000 requests/sec
```

You don't want retries to consume unlimited capacity.

You can define a **retry budget**:

```
Normal traffic = 1000 req/sec

Retry budget = 5%
```

**Therefore:**

```
Maximum retry traffic ≈ 50 req/sec
```

> This prevents retries from overwhelming the system.

---

## 10. Retry + Timeout

Retries and timeouts are closely related.

**Example:**

```
Timeout = 1 second
Retries = 3
```

**Potentially:**

```
Attempt 1
   |
   | 1 sec
   X
   |
Attempt 2
   |
   | 1 sec
   X
   |
Attempt 3
   |
   | 1 sec
   X
```

You might accidentally turn a 1-second timeout into a 3+ second request latency.

> Therefore, you need an **overall deadline**.

For example:

```
Overall deadline = 3 seconds

Attempt 1 → 1 sec
Backoff   → 100 ms
Attempt 2 → remaining budget
Backoff   → ...
Attempt 3 → remaining budget
```

The retry mechanism must stop when the overall deadline expires.

---

## 11. Retry + Timeout + Backoff

A production flow often looks like:

```
                 Request
                    |
                    v
              Attempt #1
                    |
              +-----+-----+
              |           |
           Success      Timeout
              |           |
              v           v
           Return      Backoff
                          |
                          v
                     Attempt #2
                          |
                    +-----+-----+
                    |           |
                 Success      Failure
                    |           |
                    v           v
                 Return      Backoff
                                |
                                v
                           Attempt #3
                                |
                         +------+------+
                         |             |
                      Success       Failure
                         |             |
                         v             v
                      Return        Error
```

---

## 12. Retry + Circuit Breaker

Retries alone are not enough.

Suppose Payment Service is completely down:

```
Payment Service ❌
```

Every request does:

```
Attempt 1 → timeout
Retry
Attempt 2 → timeout
Retry
Attempt 3 → timeout
```

This wastes resources.

**A circuit breaker helps:**

```
             Payment
                |
                X
             Failure
                |
                v
        Circuit Breaker
                |
          failures exceed
             threshold
                |
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
Don't call Payment
```

The request fails fast or uses a fallback.

**So:**

> Retries handle transient failures; circuit breakers prevent repeatedly calling an unhealthy dependency.

---

## 13. Retry + Idempotency

This is extremely important for distributed systems.

Consider:

```
POST /payment
```

Client sends:

```
Pay ₹10,000
```

Payment Service processes successfully:

```
₹10,000 deducted ✅
```

But the response gets lost:

```
Payment Service
      |
      | SUCCESS
      X
    Network
      |
      v
Client
```

Client sees:

```
TIMEOUT
```

Client retries.

**Without idempotency:**

```
Attempt 1 → ₹10,000 deducted
Attempt 2 → ₹10,000 deducted again
```

> 💥 Customer gets charged twice.

---

## 14. Idempotency Key

Client sends:

```
POST /payment

Idempotency-Key: ABC123
```

**First request:**

```
ABC123
   |
   v
Process payment
   |
   v
SUCCESS
```

**Retry:**

```
ABC123
   |
   v
Already processed?
   |
  YES
   |
   v
Return previous result
```

**So:**

```
Retry
  +
Idempotency
```

is extremely important for operations such as:

- Payments
- Orders
- Booking
- Money transfers
- Inventory reservations

---

## 15. Read vs Write Retries

Retries are generally safer for **idempotent reads**.

**Example:**

```
GET /orders/123
```

Retrying usually doesn't create another order.

**But:**

```
POST /orders
```

may create a new order each time.

**Therefore:**

```
GET
 → usually safer to retry

PUT
 → often designed to be idempotent

DELETE
 → generally idempotent by API semantics

POST
 → potentially non-idempotent
```

> But don't blindly rely on HTTP method alone. The actual API behavior matters.

---

## 16. Retry a Payment?

Interviewers love this question.

**Question:**

> "Would you retry a payment request after a timeout?"

**Correct answer:**

> Not blindly.

Because after a timeout, the outcome may be unknown.

```
Client
  |
  v
Payment
  |
  v
Bank
  |
  v
Payment succeeds
  |
  X
Response lost
  |
  v
Client timeout
```

The client doesn't know whether payment succeeded.

**Better approach:**

```
Payment Request
      |
      v
Idempotency Key
      |
      v
Payment Service
      |
      v
Bank
```

**On timeout:**

```
Check payment status
        OR
Retry with same idempotency key
```

> This avoids double charging.

---

## 17. Retryable vs Non-Retryable Errors

A useful classification:

| Error | Retry? | Reason |
|---|---|---|
| Connection reset | Usually yes | Potentially transient |
| Network timeout | Usually yes | Potentially transient |
| 500 | Maybe | Server failure may be temporary |
| 502 | Usually yes | Gateway/upstream issue |
| 503 | Usually yes | Service unavailable |
| 429 | Yes, with server guidance | Rate limited |
| 400 | Usually no | Invalid request |
| 401 | Usually no | Authentication issue |
| 403 | Usually no | Authorization issue |
| 404 | Usually no | Resource may not exist |
| Validation error | No | Retrying won't fix input |

> The exact policy depends on the service contract.

---

## 18. Respect Retry-After

Suppose a server returns:

```
HTTP 429
Retry-After: 5
```

The server is effectively saying:

> "Please wait 5 seconds before trying again."

A good client should respect that guidance rather than aggressively retrying.

**Example:**

```
Request
  |
  v
429 Too Many Requests
  |
  v
Retry-After: 5 sec
  |
  v
wait
  |
  v
Retry
```

---

## 19. Kafka Producer Retries

Since you've been studying Kafka, retries are particularly important here.

Consider:

```
Producer
    |
    v
Kafka Broker
```

Producer sends:

```
ProduceRequest
```

but the broker response is lost.

```
Producer
    |
    | message
    v
Broker
    |
    | stored successfully
    X
response lost
```

Producer thinks:

```
Failure
```

and retries.

**Potentially:**

```
Producer
    |
    +---- Attempt 1 → Broker
    |
    +---- Retry → Broker
```

> This is where **idempotent producer** support becomes important.

With idempotence, Kafka can use producer identity and sequence numbers to prevent duplicate records caused by retries.

**Conceptually:**

```
Producer
  |
  | Producer ID + sequence
  v
Broker
  |
  v
Deduplicate duplicate retry
```

---

## 20. Kafka Retry Flow

Conceptually:

```
Producer
   |
   v
Broker
   |
   X
Temporary failure
   |
   v
Retry
   |
   v
Broker
   |
   v
Success
```

Kafka producer configuration includes concepts such as:

- `retries`
- `delivery timeout`
- `request timeout`
- `acks`
- `enable.idempotence`

> These need to be considered together rather than independently.

---

## 21. Retry Topics in Kafka

Retries can also be implemented at the consumer/application level.

Suppose:

```
orders-topic
     |
     v
Consumer
     |
     X
Payment Service unavailable
```

**Instead of immediately losing/failing the message:**

```
orders-topic
     |
     v
Consumer
     |
     v
retry-topic
     |
     v
Consumer
```

**Potentially:**

```
orders-topic
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
orders processing
```

**If it continues failing:**

```
retry
  |
  v
DLQ
```

> This is useful when the dependency may remain unavailable for minutes rather than milliseconds.

---

## 22. Synchronous Retry vs Asynchronous Retry

### Synchronous retry

```
Request
   |
   v
Service
   |
   X
Retry
   |
   v
Service
   |
   v
Response
```

**Good when:**

- Failure is likely transient
- Operation is short
- User is waiting
- Retry can complete within the deadline

### Asynchronous retry

```
Request
   |
   v
Queue
   |
   v
Worker
   |
   X
Failure
   |
   v
Retry later
```

**Good when:**

- Processing can take a long time
- User doesn't need immediate result
- Dependency may be unavailable for a while
- We need durable retries

---

## 23. Retry vs Timeout

These solve different problems.

| Concept | Purpose |
|---|---|
| Timeout | Stop waiting too long |
| Retry | Try the operation again |
| Backoff | Wait between retries |
| Jitter | Randomize retry timing |
| Circuit breaker | Stop calling unhealthy dependency |
| Bulkhead | Isolate resources |
| Idempotency | Prevent duplicate effects |

**Think:**

```
Timeout
   ↓
Request failed/unknown
   ↓
Should we retry?
   ↓
YES
   ↓
Backoff + Jitter
   ↓
Retry
   ↓
Still failing?
   ↓
Circuit breaker / fallback
```

---

## 24. Common Retry Mistakes

### Mistake 1: Infinite retries

```java
while(true) {
    retry();
}
```

> ❌ Dangerous.

### Mistake 2: Immediate retries

```
retry();
retry();
retry();
```

> ❌ Can create retry storms.

### Mistake 3: No jitter

```
1000 clients
   |
   v
retry after exactly 1 sec
```

> ❌ Thundering herd.

### Mistake 4: Retrying permanent failures

```
400 Bad Request
    ↓
Retry
    ↓
400
    ↓
Retry
```

> ❌ No benefit.

### Mistake 5: Retrying non-idempotent operations

```
POST /payment
    ↓
timeout
    ↓
retry
```

> ❌ Could duplicate the operation.

### Mistake 6: Ignoring overall deadline

```
Attempt 1 → 2 sec
Attempt 2 → 2 sec
Attempt 3 → 2 sec
```

> The user may wait 6+ seconds even though the API should respond within 3 seconds.

---

## 25. Production Retry Strategy

A good general design is:

```
                 Request
                    |
                    v
                 Timeout
                    |
              Is error retryable?
                 /       \
               NO         YES
               |           |
             Fail       Retry budget?
                           |
                       +---+---+
                       |       |
                      NO      YES
                       |       |
                     Fail   Backoff
                               |
                             Jitter
                               |
                               v
                             Retry
                               |
                         +-----+-----+
                         |           |
                      Success     Failure
                         |           |
                         v           v
                      Return    Retry again
                                   |
                              max attempts?
                                   |
                                  YES
                                   |
                                   v
                               Fail/Fallback
```

---

## 26. Senior-Level Interview Answer

If the interviewer asks:

> "How would you design retries in a distributed system?"

**A strong 10-year-experience answer:**

> I would use bounded retries only for transient and explicitly retryable failures. I would combine retries with request timeouts, exponential backoff, jitter, and an overall deadline so that retries don't create retry storms or violate the user-facing latency budget. I would avoid retries for permanent failures such as validation errors. For non-idempotent operations like payments or order creation, I would use idempotency keys or another deduplication mechanism because a timeout doesn't tell us whether the original operation succeeded. I would also consider circuit breakers, retry budgets, and asynchronous retry queues for longer-lived failures.

---

## 27. The Complete Resilience Pattern

At this point, connect the concepts you've learned:

```
                         Client
                            |
                            v
                         Timeout
                            |
                            v
                       Service A
                            |
                     Circuit Breaker
                            |
                         Bulkhead
                            |
                            v
                       Service B
                            |
                         Timeout
                            |
                     Retryable?
                       /       \
                     NO         YES
                     |           |
                   Fail       Backoff
                                 |
                               Jitter
                                 |
                                 v
                               Retry
                                 |
                           Still failing?
                                 |
                                 v
                          Circuit Breaker
                                 |
                           Fallback / Queue
                                 |
                                 v
                              Success
```

**And for critical writes:**

```
                 Retry
                   +
              Idempotency
                   +
               Timeout
                   +
          Exponential Backoff
                   +
                Jitter
                   +
           Circuit Breaker
```

> This combination is one of the core resilience patterns in distributed system design.

---

## Mental model

```
Timeout         = stop waiting.
Retry           = try again.
Backoff         = don't retry immediately.
Jitter          = don't retry at the same time.
Circuit breaker = stop calling a broken dependency.
Idempotency     = make retries safe.
```

