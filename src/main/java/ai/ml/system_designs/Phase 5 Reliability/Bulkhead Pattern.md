# Bulkhead Pattern

The **Bulkhead Pattern** is a resilience pattern used in distributed systems to isolate resources between different parts of an application, so that failure or overload in one area does not bring down the entire system.

The name comes from ships: a ship is divided into watertight compartments. If one compartment floods, the others can remain operational.

> **Bulkhead** = isolate resources so one failing dependency cannot consume everything.

---

## 1. The problem Bulkhead solves

Suppose an Order Service calls three downstream services:

```
                    Order Service
                         │
          ┌──────────────┼──────────────┐
          │              │              │
          ▼              ▼              ▼
       Payment       Inventory       Shipping
```

Assume your application has 100 threads.

Without bulkhead isolation, Payment Service becomes extremely slow:

```
Payment requests
      ↓
100 threads occupied
      ↓
No threads available
      ↓
Inventory requests wait
      ↓
Shipping requests wait
      ↓
Order Service becomes unhealthy
```

Even though:

```
Inventory = HEALTHY
Shipping  = HEALTHY
```

they become unavailable because Payment consumed the shared resources.

This is a classic **cascading failure**.

---

## 2. Without Bulkhead

Imagine:

```
Order Service
     │
     ▼
Shared Thread Pool
     │
     ├──── Payment
     ├──── Inventory
     └──── Shipping
```

Suppose:

```
Total threads = 100
```

Payment becomes slow.

```
Payment → 100 threads
Inventory → 0
Shipping → 0
```

The result:

```
Payment failure
      ↓
Thread exhaustion
      ↓
Order Service degradation
      ↓
Inventory unavailable
      ↓
Shipping unavailable
      ↓
Cascading failure
```

---

## 3. With Bulkhead

Now isolate resources:

```
                    Order Service
                         │
          ┌──────────────┼──────────────┐
          │              │              │
          ▼              ▼              ▼
     Payment Pool   Inventory Pool  Shipping Pool
       40 threads      30 threads      30 threads
```

Now Payment becomes slow:

```
Payment
   ↓
40 threads consumed
```

But:

```
Inventory
   ↓
30 threads still available

Shipping
   ↓
30 threads still available
```

So the failure is contained.

That's the fundamental purpose of Bulkhead.

---

## 4. Simple analogy

Imagine a restaurant with separate kitchens:

```
Restaurant
│
├── Pizza Kitchen
├── Indian Kitchen
└── Chinese Kitchen
```

Suppose the Pizza Kitchen gets overloaded.

If every kitchen shares exactly the same workers:

```
Pizza traffic
    ↓
All workers busy
    ↓
Indian food delayed
    ↓
Chinese food delayed
```

Instead:

```
Pizza Kitchen   → 5 workers
Indian Kitchen  → 5 workers
Chinese Kitchen → 5 workers
```

Pizza can become overloaded without taking down the other kitchens.

That's Bulkhead.

---

## 5. Types of Bulkhead

There are two common approaches.

### A. Thread Pool Bulkhead

Each dependency gets a separate thread pool.

```
Payment Service
     ↓
Payment Thread Pool
     ↓
20 threads
```

```
Inventory Service
     ↓
Inventory Thread Pool
     ↓
20 threads
```

```
Shipping Service
     ↓
Shipping Thread Pool
     ↓
10 threads
```

Example:

```
Total = 50 threads

Payment   = 20
Inventory = 20
Shipping  = 10
```

If Payment becomes slow:

```
Payment → 20 busy
Inventory → 20 available
Shipping → 10 available
```

---

## 6. Semaphore Bulkhead

A semaphore-based bulkhead limits the number of concurrent calls, without necessarily creating a dedicated thread pool.

For example:

```
Payment max concurrent calls = 20
```

Suppose 20 Payment requests are currently executing:

```
Payment
 ├── Request 1
 ├── Request 2
 ├── ...
 └── Request 20
```

Request 21 arrives:

```
Request 21
    ↓
Bulkhead
    ↓
LIMIT REACHED
    ↓
Reject / fail fast
```

This prevents unlimited concurrency.

---

## 7. Thread Pool vs Semaphore

| Feature | Thread Pool Bulkhead | Semaphore Bulkhead |
|---|---|---|
| Isolation | Stronger | Concurrency-based |
| Dedicated threads | Yes | No |
| Controls concurrency | Yes | Yes |
| Resource overhead | Higher | Lower |
| Useful for blocking calls | Very useful | Less useful |
| Useful for lightweight calls | Sometimes | Very useful |

A simple way to remember:

```
Thread Pool Bulkhead
    → Separate execution resources

Semaphore Bulkhead
    → Limit number of concurrent executions
```

---

## 8. Bulkhead + Circuit Breaker

These patterns solve different problems.

### Circuit Breaker

Detects an unhealthy dependency and stops calling it.

```
Payment failing
      ↓
Circuit Breaker
      ↓
OPEN
      ↓
Stop calls
```

### Bulkhead

Prevents a dependency from consuming all resources.

```
Payment slow
      ↓
Payment resource pool
      ↓
Maximum 20 concurrent calls
```

Together:

```
Request
   │
   ▼
Bulkhead
   │
   ▼
Circuit Breaker
   │
   ▼
Timeout
   │
   ▼
Payment Service
```

You can think:

> **Bulkhead** limits how much damage one dependency can cause.
>
> **Circuit Breaker** stops calling the dependency when it is clearly unhealthy.

---

## 9. Bulkhead + Timeout

Consider:

```
Payment Service
```

becomes slow.

**Without timeout:**

```
Request
   ↓
Payment
   ↓
waiting...
   ↓
waiting...
   ↓
waiting...
```

Eventually all Payment resources can become occupied.

**With timeout:**

```
Request
   ↓
Payment
   ↓
2 sec timeout
   ↓
FAIL
```

Combined with Bulkhead:

```
Payment
   │
   ▼
Bulkhead
max 20 concurrent
   │
   ▼
Timeout
2 seconds
   │
   ▼
Payment Service
```

Now you have two protections:

```
Bulkhead → limits concurrency
Timeout  → limits waiting time
```

---

## 10. Real-world example

Imagine an e-commerce application:

```
                     E-Commerce
                         │
          ┌──────────────┼──────────────┐
          │              │              │
          ▼              ▼              ▼
       Payment        Inventory       Shipping
```

Suppose:

```
Payment → 500 ms
Inventory → 100 ms
Shipping → 200 ms
```

Everything is healthy.

Then Payment suddenly takes:

```
20 seconds
```

Traffic:

```
1,000 requests/sec
```

**Without protection:**

```
Payment
   ↓
Huge number of waiting requests
   ↓
Threads/connections exhausted
   ↓
Entire application suffers
```

**With Bulkhead:**

```
Payment Pool
max = 50
```

Only 50 Payment operations can occupy the allocated resource.

Additional requests can:

```
Reject
   OR
Queue within a bounded limit
   OR
Fallback
```

while Inventory and Shipping continue operating.

---

## 11. Important: Bulkhead doesn't mean unlimited queues

This is a common mistake.

You might think:

```
Payment Pool = 20

Extra requests
      ↓
Queue them forever
```

That's dangerous.

A huge queue can cause:

```
Memory consumption
      ↓
High latency
      ↓
Timeouts
      ↓
More retries
      ↓
More load
      ↓
System collapse
```

Instead, use **bounded concurrency** and **bounded queues**.

For example:

```
Payment
 ├── 20 active requests
 ├── 50 queued requests
 └── remaining requests → rejected/fallback
```

---

## 12. Bulkhead and Backpressure

Bulkhead is closely related to backpressure.

Suppose your system can process:

```
100 requests/sec
```

but receives:

```
1,000 requests/sec
```

You need to prevent unlimited work from entering the system.

```
Incoming traffic
       │
       ▼
   Bulkhead
       │
       ├── accepted
       │
       └── rejected
```

This is much healthier than accepting everything and eventually crashing.

---

## 13. Bulkhead at different levels

Bulkheads aren't limited to downstream services.

You can isolate:

### By dependency

```
Payment → Pool A
Inventory → Pool B
Shipping → Pool C
```

### By tenant

```
Tenant A → Resource Pool A
Tenant B → Resource Pool B
Tenant C → Resource Pool C
```

This prevents one large customer from consuming all resources.

### By operation

```
Read APIs  → Pool A
Write APIs → Pool B
Reports    → Pool C
```

For example, an expensive reporting API shouldn't consume resources needed for normal customer operations.

### By priority

```
Critical requests → 70%
Normal requests   → 20%
Background jobs   → 10%
```

This allows critical functionality to survive overload.

---

## 14. Bulkhead vs Rate Limiter

These are also different.

### Rate Limiter

Controls:

> How many requests are allowed per unit of time?

Example:

```
100 requests/second
```

### Bulkhead

Controls:

> How many operations can execute concurrently?

Example:

```
20 concurrent requests
```

So:

```
Rate Limiter
    ↓
Controls arrival rate

Bulkhead
    ↓
Controls concurrent work
```

They can work together.

---

## 15. Bulkhead vs Load Balancer

Another common interview question.

### Load Balancer

Distributes traffic:

```
             Load Balancer
             /     |     \
            /      |      \
         App-1   App-2   App-3
```

### Bulkhead

Isolates resources:

```
App
 ├── Payment Pool
 ├── Inventory Pool
 └── Shipping Pool
```

- **Load balancing** distributes traffic.
- **Bulkheading** isolates resources/failure domains.

---

## 16. Spring Boot / Resilience4j concept

In Java/Spring Boot systems, **Resilience4j** is commonly used to implement resilience patterns such as Bulkhead and Circuit Breaker.

Conceptually:

```java
@Bulkhead(
    name = "paymentService",
    type = Bulkhead.Type.SEMAPHORE
)
public PaymentResponse processPayment() {
    return paymentClient.pay();
}
```

The idea is:

```
Payment concurrency limit = N
```

If the limit is reached, the call can be rejected rather than allowing unlimited concurrent execution.

For a thread-pool style isolation, the configuration is conceptually:

```
Payment Thread Pool
    core/max capacity
    queue capacity
```

The exact configuration depends on whether you use a semaphore or thread-pool bulkhead.

---

## 17. Production architecture

A mature microservice might use several resilience mechanisms together:

```
                         Request
                            │
                            ▼
                       Rate Limiter
                            │
                            ▼
                         Bulkhead
                            │
                            ▼
                       Circuit Breaker
                            │
                            ▼
                          Retry
                            │
                            ▼
                         Timeout
                            │
                            ▼
                    Downstream Service
```

But the ordering and whether each pattern is needed depends on the workload.

For example, you don't want retries blindly combined with a large concurrency pool because you can multiply downstream traffic during an outage.

---

## 18. Bulkhead failure scenario

Suppose:

```
Payment bulkhead = 20
```

and all 20 slots are occupied.

Request #21 arrives:

```
Request #21
     ↓
Payment Bulkhead
     ↓
20/20 occupied
     ↓
REJECT
```

The application could return:

```
HTTP 503 Service Unavailable
```

or:

```
Payment Pending
```

or place the work onto a durable queue if the business operation supports asynchronous processing.

The important thing is:

> Don't allow request #21 to consume unlimited resources just because the dependency is overloaded.

---

## 19. What Bulkhead prevents

Bulkhead helps prevent:

- Thread pool exhaustion
- Connection pool exhaustion
- Memory exhaustion from excessive queued work
- One slow dependency affecting unrelated operations
- Cascading failures
- Noisy-neighbor problems
- Unbounded concurrency

---

## 20. What Bulkhead does NOT solve

Bulkhead does **not**:

- Fix the downstream service
- Detect failures by itself
- Automatically retry failed requests
- Guarantee high availability
- Replace a circuit breaker
- Replace timeouts
- Replace rate limiting

It is one component of a broader resilience strategy.

---

## 21. Circuit Breaker vs Bulkhead — interview favorite

| | Circuit Breaker | Bulkhead |
|---|---|---|
| Main goal | Stop calls to unhealthy dependency | Isolate resources |
| Trigger | Failure/slow-call threshold | Resource/concurrency limit |
| Primary protection | Dependency failure | Resource exhaustion |
| Typical action | Open circuit | Reject/limit work |
| Prevents cascading failure | Yes | Yes |
| Handles slow dependency | Yes, indirectly | Yes, by limiting concurrency |
| Replaces timeout? | No | No |
| Replaces retry? | No | No |

### Easy way to remember

**Circuit Breaker:**

> "Don't call the sick service."

**Bulkhead:**

> "Don't let the sick service consume all our resources."

---

## 22. Interview scenario

**Interviewer:**

> Payment Service is down. How would you prevent it from affecting Inventory and Shipping?

**A strong answer:**

> I would isolate Payment, Inventory, and Shipping using separate bulkheads so that Payment cannot exhaust the shared thread or connection resources. I would also use a timeout for Payment calls and a circuit breaker to stop calls after repeated failures. If the payment bulkhead becomes saturated, requests should fail fast or move to a controlled asynchronous workflow rather than creating an unbounded queue.

---

## 23. The key mental model

Remember these five patterns together:

```
                RESILIENCE
                    │
       ┌────────────┼─────────────┐
       │            │             │
    Timeout       Retry       Bulkhead
       │            │             │
 "Don't wait"  "Try again"   "Isolate"
                                  │
                                  ▼
                         Circuit Breaker
                                  │
                            "Stop calling"
```

And then:

```
Rate Limiter → Control incoming rate
Timeout      → Limit waiting time
Retry        → Recover from transient failures
Backoff      → Slow down retries
Jitter       → Avoid synchronized retries
Bulkhead     → Isolate resources
Circuit Breaker → Stop calls to unhealthy dependencies
Fallback     → Degrade gracefully
```

