# Circuit Breaker

A **Circuit Breaker** is a resilience pattern used in distributed systems and microservices to prevent a failing downstream service from bringing down the entire system.

The basic idea is:

> If a dependency is repeatedly failing, stop calling it temporarily and fail fast.

This protects your application from cascading failures, excessive latency, thread exhaustion, and resource exhaustion.

---

## 1. Real-world example

Suppose you have:

```
                    ┌─────────────────┐
                    │  Order Service  │
                    └────────┬────────┘
                             │
                             ▼
                    ┌─────────────────┐
                    │ Payment Service │
                    └─────────────────┘
```

Normally:

```
User
 │
 ▼
Order Service
 │
 │ HTTP request
 ▼
Payment Service
 │
 ▼
Payment successful
 │
 ▼
Order confirmed
```

Now suppose Payment Service becomes unhealthy.

Every Order Service request still tries:

```
Order → Payment
Order → Payment
Order → Payment
Order → Payment
Order → Payment
...
```

If each request takes 10 seconds to timeout, your Order Service can quickly accumulate hundreds or thousands of waiting requests.

Eventually:

```
Payment Service fails
        ↓
Order Service waits
        ↓
Threads/connections consumed
        ↓
Latency increases
        ↓
Requests start timing out
        ↓
Order Service becomes unhealthy
        ↓
Other services calling Order Service also suffer
        ↓
Cascading failure
```

A circuit breaker prevents this.

---

## 2. Circuit Breaker states

A circuit breaker typically has three states:

```
              failures exceed threshold
        ┌──────────────────────────────┐
        │                              ▼
   ┌──────────┐                   ┌──────────┐
   │  CLOSED  │ ────────────────► │   OPEN   │
   └──────────┘                   └──────────┘
        ▲                              │
        │                              │ wait
        │                              ▼
        │                       ┌─────────────┐
        └────────────────────── │ HALF-OPEN   │
             success            └─────────────┘
                                      │
                                      │ failure
                                      ▼
                                   OPEN
```

Let's understand each.

---

## 3. CLOSED

**CLOSED** = everything is normal.

Requests are allowed to reach the downstream service.

```
Order Service
     │
     ▼
Circuit Breaker
     │
     ▼
Payment Service
```

For example:

```
Request 1 → Payment → SUCCESS
Request 2 → Payment → SUCCESS
Request 3 → Payment → SUCCESS
Request 4 → Payment → SUCCESS
```

The circuit monitors failures.

For example:

```
Failure threshold = 50%
Minimum calls     = 10
```

If enough requests fail, the circuit opens.

---

## 4. OPEN

**OPEN** = stop calling the downstream service.

Suppose Payment Service is failing:

```
Request
   │
   ▼
Circuit Breaker
   │
   │ OPEN
   X
Payment Service
```

The request is rejected without making the downstream call.

This is called:

> **Fail Fast**

Instead of:

```
Request
  ↓
Payment Service
  ↓
wait 10 seconds
  ↓
timeout
```

you get:

```
Request
  ↓
Circuit Breaker
  ↓
FAIL FAST
```

This is extremely important for protecting resources.

---

## 5. HALF-OPEN

The circuit shouldn't remain OPEN forever.

After some configured time:

```
OPEN
 │
 │ wait 30 seconds
 ▼
HALF-OPEN
```

The circuit allows a small number of test requests.

For example:

```
Test Request 1 → Payment → SUCCESS
Test Request 2 → Payment → SUCCESS
Test Request 3 → Payment → SUCCESS
```

If the downstream service has recovered:

```
HALF-OPEN
     │
     │ successful test calls
     ▼
  CLOSED
```

If it is still failing:

```
HALF-OPEN
     │
     │ failure
     ▼
   OPEN
```

---

## 6. Complete lifecycle

Imagine:

```
Payment Service healthy
        ↓
      CLOSED
        ↓
Payment starts failing
        ↓
Failure threshold reached
        ↓
       OPEN
        ↓
Requests fail fast
        ↓
Wait for recovery period
        ↓
    HALF-OPEN
        ↓
   Test request
     ↙       ↘
 SUCCESS     FAILURE
    ↓           ↓
 CLOSED        OPEN
```

---

## 7. Circuit Breaker vs Retry

This is a very important interview question.

### Retry

Retry says:

> "The request failed. Let's try again."

```
Request
  ↓
Payment
  ↓
FAIL
  ↓
Retry
  ↓
Payment
  ↓
FAIL
  ↓
Retry
```

### Circuit Breaker

Circuit breaker says:

> "This service is consistently failing. Stop calling it for now."

```
Request
  ↓
Circuit Breaker
  ↓
OPEN
  ↓
FAIL FAST
```

They are often used together.

For example:

```
Circuit Breaker
      ↓
    Retry
      ↓
Payment Service
```

But retries must be carefully limited. Aggressive retries can actually make an outage worse.

---

## 8. Circuit Breaker + Timeout

A circuit breaker is usually combined with a **timeout**.

For example:

```
Timeout = 2 seconds
Failure threshold = 50%
Open duration = 30 seconds
```

Flow:

```
Order
  │
  ▼
Circuit Breaker
  │
  ▼
Payment
  │
  ├── responds in 200ms → SUCCESS
  │
  └── doesn't respond
           │
           ▼
       2 sec timeout
           │
           ▼
         FAILURE
```

Without timeout, a circuit breaker may not react quickly enough to slow/hung dependencies.

---

## 9. Circuit Breaker + Retry + Timeout

A production system might look like:

```
                 ┌─────────────────┐
                 │   Order Service │
                 └────────┬────────┘
                          │
                          ▼
                 ┌─────────────────┐
                 │ Circuit Breaker │
                 └────────┬────────┘
                          │
                          ▼
                      Timeout
                          │
                          ▼
                        Retry
                          │
                          ▼
                 ┌─────────────────┐
                 │ Payment Service │
                 └─────────────────┘
```

A more precise conceptual flow is:

```
Request
   │
   ▼
Circuit Breaker
   │
   ├── OPEN → Fail Fast
   │
   └── CLOSED/HALF-OPEN
            │
            ▼
         Timeout
            │
            ▼
          Retry
            │
            ▼
       Downstream
```

---

## 10. What does the Circuit Breaker monitor?

Common signals include:

### Failure rate

Example:

```
100 requests
60 failures

Failure rate = 60%
```

If threshold is:

```
50%
```

the circuit can open.

### Slow-call rate

Failure isn't the only problem.

Suppose:

```
100 requests
90 succeed
but each takes 15 seconds
```

The dependency is technically successful but practically unhealthy.

Circuit breakers can therefore consider:

```
Failure rate
+
Slow-call rate
```

---

## 11. Sliding window

Circuit breakers commonly evaluate recent calls using a **sliding window**.

Example:

Last 10 requests:

```
S S F F F F F S F F
```

Where:

```
S = Success
F = Failure
```

Failures:

```
7 / 10 = 70%
```

If the configured failure threshold is 50%:

```
70% > 50%

       ↓

Circuit OPEN
```

The window can be:

- count-based
- time-based

For example:

```
Last 100 calls
```

or:

```
Failures during last 60 seconds
```

---

## 12. Circuit Breaker does NOT fix the downstream service

This is an important concept.

Circuit breaker does **not** repair:

```
Payment Service
```

It protects:

```
Order Service
```

from the consequences of Payment Service failure.

Think of it as an electrical circuit breaker.

If there is a problem:

```
Electrical fault
      ↓
Circuit breaker trips
      ↓
Power is disconnected
      ↓
System protected
```

Similarly:

```
Service failure
      ↓
Circuit breaker opens
      ↓
Calls stopped
      ↓
Application protected
```

---

## 13. Fallback

When the circuit is OPEN, your application may execute a **fallback**.

For example:

```
Order
  ↓
Circuit Breaker
  ↓
OPEN
  ↓
Fallback
```

Possible fallback:

```
"Payment service temporarily unavailable"
```

Or, depending on business requirements:

```
Return cached data
```

or:

```
Queue request for later processing
```

or:

```
Mark payment as pending
```

But fallback is **not always appropriate**.

For example, you should not blindly create an order as "paid" just because Payment Service is unavailable.

---

## 14. Circuit Breaker vs Bulkhead

These two patterns are often confused.

### Circuit Breaker

Protects against:

> Repeated downstream failures

```
Payment failing
      ↓
Circuit OPEN
      ↓
Stop calls
```

### Bulkhead

Protects against:

> Resource exhaustion caused by one dependency

For example:

```
Order Service
 ├── Payment → 100 threads
 ├── Inventory → 20 threads
 └── Shipping → 20 threads
```

If Payment becomes slow, it shouldn't consume all resources.

Bulkhead isolates resources:

```
Payment Pool     → 20 threads
Inventory Pool   → 20 threads
Shipping Pool    → 20 threads
```

So:

```
Circuit Breaker → stop calling unhealthy dependency

Bulkhead        → isolate resources
```

They complement each other.

---

## 15. Circuit Breaker vs Rate Limiter

Another common interview question.

| Pattern | Purpose |
|---|---|
| Circuit Breaker | Protect against unhealthy downstream service |
| Rate Limiter | Control request rate |
| Retry | Try failed request again |
| Timeout | Stop waiting too long |
| Bulkhead | Isolate resources |
| Load Balancer | Distribute traffic |

A resilient architecture often combines several of them.

---

## 16. Real production example

Imagine:

```
                    API Gateway
                         │
                         ▼
                  Order Service
                         │
              ┌──────────┴──────────┐
              │                     │
              ▼                     ▼
       Circuit Breaker       Circuit Breaker
              │                     │
              ▼                     ▼
      Payment Service       Inventory Service
```

Payment becomes unhealthy.

The Payment circuit opens:

```
Order
 │
 ├── Inventory → SUCCESS
 │
 └── Payment → Circuit OPEN → Fail Fast
```

Importantly, the entire Order Service doesn't necessarily become unavailable.

This is one of the key benefits of resilience patterns:

> One failing dependency should not automatically take down the entire system.

---

## 17. Important configuration parameters

When implementing a circuit breaker, you'll typically configure things such as:

- `failureRateThreshold`
- `slowCallRateThreshold`
- `minimumNumberOfCalls`
- `slidingWindowSize`
- `waitDurationInOpenState`
- `permittedCallsInHalfOpenState`
- `timeoutDuration`

Example:

```
failureRateThreshold = 50%

minimumNumberOfCalls = 20

slidingWindowSize = 50

waitDurationInOpenState = 30 seconds

permittedCallsInHalfOpenState = 5
```

Meaning approximately:

```
Observe calls
     ↓
Need enough calls before deciding
     ↓
Calculate recent failure/slow-call rate
     ↓
If threshold exceeded
     ↓
OPEN
     ↓
Wait 30 sec
     ↓
HALF-OPEN
     ↓
Allow 5 test calls
     ↓
Healthy → CLOSED
Unhealthy → OPEN
```

---

## 18. Critical production concern: retry storm

Consider 1,000 requests.

Payment fails.

Every request retries 3 times:

```
1,000 original requests
        ×
3 retries
        ↓
4,000 calls
```

You just turned:

```
1,000 calls
```

into:

```
4,000 calls
```

This can make the failing service even worse.

Therefore:

```
Retry
+
Exponential Backoff
+
Jitter
+
Circuit Breaker
+
Timeout
```

is usually much safer than unlimited immediate retries.

---

## 19. Interview answer

If an interviewer asks:

> "What is a Circuit Breaker?"

A strong answer is:

> A Circuit Breaker is a resilience pattern used in distributed systems to prevent cascading failures. It monitors calls to a downstream dependency and, when failures or slow calls exceed a configured threshold, transitions from CLOSED to OPEN and stops sending requests to that dependency. Requests then fail fast or use a fallback. After a recovery period, the breaker enters HALF-OPEN and allows a limited number of test requests. If the dependency has recovered, it transitions back to CLOSED; otherwise, it returns to OPEN.

---

## 20. The bigger picture

Since you're learning System Design from fundamentals toward expert level, think of Circuit Breaker as one component of a broader resilience strategy:

```
                    ┌─────────────────┐
                    │     Client      │
                    └────────┬────────┘
                             │
                             ▼
                       Load Balancer
                             │
                             ▼
                       API Service
                             │
                 ┌───────────┴───────────┐
                 │                       │
              Timeout                 Bulkhead
                 │                       │
                 ▼                       ▼
             Retry +                Resource
             Backoff                Isolation
                 │
                 ▼
          Circuit Breaker
                 │
                 ▼
          Downstream Service
```

The key mental model is:

```
Timeout → don't wait forever
Retry → transient failure recovery
Backoff + Jitter → don't overload the dependency
Circuit Breaker → stop calling a persistently unhealthy dependency
Bulkhead → prevent one dependency from consuming all resources
Fallback → provide a controlled degraded response
```

