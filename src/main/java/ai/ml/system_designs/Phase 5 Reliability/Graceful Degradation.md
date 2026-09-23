# Graceful Degradation

Graceful Degradation is a resilience pattern where a system continues providing useful functionality even when some components, dependencies, or features are unavailable or overloaded.

The key idea is:

> When the full system cannot work, don't make everything fail. Reduce functionality and keep the critical path alive.

---

## 1. The basic problem

Consider an e-commerce application:

```
                    E-Commerce
                        │
          ┌─────────────┼─────────────┐
          ▼             ▼             ▼
       Product       Payment       Recommendation
        Service       Service         Service
```

Suppose **Recommendation Service** goes down.

A bad design does this:

```
Recommendation DOWN
       ↓
Product API fails
       ↓
Homepage fails
       ↓
Entire application unavailable
```

That's unnecessary.

Recommendations are not required to display products.

A better design:

```
Recommendation DOWN
       ↓
Show products normally
       ↓
Hide "Recommended for you"
       ↓
User can still shop
```

That's **Graceful Degradation**.

---

## 2. Full functionality vs degraded functionality

Think of your application as:

```
             FULL EXPERIENCE
                   │
        ┌──────────┼──────────┐
        ▼          ▼          ▼
     Critical   Important   Optional
     Feature    Feature     Feature
```

**During normal operation:**

Everything works.

**During partial failure:**

- Critical features → **KEEP**
- Important features → **DEGRADED**
- Optional features → **DISABLE**

So instead of:

```
100% → 0%
```

you aim for:

```
100% → 70% → 50%
```

depending on the failure.

---

## 3. Real-world example: Amazon-style product page

Imagine a product page:

```
┌───────────────────────────────┐
│ Product                       │
│                               │
│ Price: ₹999                   │
│ Stock: Available              │
│                               │
│ [Add to Cart]                 │
│                               │
│ ★ Reviews                     │
│                               │
│ Recommended Products          │
└───────────────────────────────┘
```

Suppose the recommendation service fails.

Instead of:

```
HTTP 500
Product page unavailable
```

return:

```
┌───────────────────────────────┐
│ Product                       │
│                               │
│ Price: ₹999                   │
│ Stock: Available              │
│                               │
│ [Add to Cart]                 │
│                               │
│ ★ Reviews                     │
│                               │
│ Recommended Products          │
│ Temporarily unavailable       │
└───────────────────────────────┘
```

The customer can still purchase.

---

## 4. Critical vs non-critical dependencies

This is one of the most important concepts.

Suppose:

```
Order Service
     │
     ├── Database       ← Critical
     ├── Payment        ← Critical
     ├── Inventory      ← Critical
     ├── Email          ← Non-critical
     └── Recommendation ← Optional
```

If **Email Service** fails:

```
Order
 ↓
Save order
 ↓
Payment
 ↓
Inventory
 ↓
SUCCESS
 ↓
Email → FAILED
```

You don't necessarily want:

```
Email failure
    ↓
Order failure
```

Instead:

```
Order → SUCCESS

Email → retry asynchronously
```

This is graceful degradation.

---

## 5. Common degradation strategies

There are several ways to degrade a system.

### Strategy 1: Cached data

Suppose your recommendation service is unavailable.

Instead of:

```
Recommendation Service
       ↓
      DOWN
```

use:

```
Recommendation Service
       X
       │
       ▼
     Cache
       │
       ▼
Previous recommendations
```

The user sees slightly stale data.

That's often much better than seeing nothing.

---

## 6. Example: Product catalog

Suppose:

```
Database
   ↓
Product Service
```

Database becomes temporarily unavailable.

You might have:

```
Redis Cache
```

containing:

```
Product 101 → ₹999
Product 102 → ₹1,299
Product 103 → ₹799
```

Instead of:

```
DB DOWN
 ↓
HTTP 500
```

you can return:

```
DB DOWN
 ↓
Cache
 ↓
Stale-but-useful data
```

This is a common graceful-degradation strategy.

---

## 7. Strategy 2: Disable optional features

Suppose your homepage has:

- Products
- Search
- Cart
- Recommendations
- Personalization
- Ads
- Reviews

During overload:

```
Keep:
✓ Products
✓ Search
✓ Cart

Disable:
✗ Recommendations
✗ Personalization
✗ Ads
```

The application remains usable.

---

## 8. Strategy 3: Return simplified responses

Suppose a search API normally returns:

```json
{
  "products": [...],
  "recommendations": [...],
  "personalizedOffers": [...],
  "reviews": [...],
  "similarProducts": [...]
}
```

During degradation:

```json
{
  "products": [...]
}
```

You remove expensive optional computation.

This reduces:

- CPU
- database load
- network traffic
- downstream calls
- latency

---

## 9. Strategy 4: Fallback

Suppose:

```
Order Service
      │
      ▼
Shipping Service
      │
      X
    DOWN
```

Instead of failing immediately:

```
Shipping unavailable
      ↓
Fallback
      ↓
"Shipping estimate temporarily unavailable"
```

Or:

```
Shipping Service unavailable
      ↓
Use cached shipping estimate
```

Fallback is one mechanism for implementing graceful degradation.

---

## 10. Strategy 5: Queue the work

Some operations don't need to happen synchronously.

For example:

```
Order
  │
  ├── Save Order → synchronous
  │
  └── Send Email → asynchronous
                       │
                       ▼
                     Kafka
                       │
                       ▼
                 Email Consumer
```

If Email Service is down:

```
Order
 ↓
SUCCESS

Email
 ↓
Kafka
 ↓
WAIT
```

When Email Service recovers:

```
Kafka
 ↓
Email Consumer
 ↓
Send email
```

This is a very powerful degradation strategy.

---

## 11. Strategy 6: Stale data

Sometimes stale data is better than no data.

Example:

```
Exchange rate service
        ↓
       DOWN
```

You have a cached exchange rate from 2 minutes ago.

Instead of:

```
Unable to display price
```

you might show:

```
Estimated price based on recently cached exchange rate
```

But this depends heavily on business requirements.

- For **financial transactions**, stale data may be unacceptable.
- For **recommendations**, it may be perfectly fine.

---

## 12. Strategy 7: Reduce quality

Sometimes you can reduce computational quality.

For example, an image-processing service might normally generate:

```
High-quality image
```

During overload:

```
High-quality processing
        ↓
Disabled

Lower-quality processing
        ↓
Enabled
```

Similarly:

```
Search:
Normal → sophisticated ranking
Degraded → basic keyword matching
```

This is graceful degradation through reduced computation.

---

## 13. Strategy 8: Priority-based degradation

Not all requests are equally important.

Suppose:

```
                    Traffic
                       │
          ┌────────────┼────────────┐
          ▼            ▼            ▼
       Critical      Normal      Background
          │            │            │
          ▼            ▼            ▼
        KEEP         LIMIT         DROP
```

For example:

```
Payment       → KEEP
Order         → KEEP
Inventory     → KEEP
Analytics     → DELAY
Recommendations → DISABLE
```

This ensures the most important business functionality survives an overload.

---

## 14. Graceful Degradation + Circuit Breaker

These patterns work extremely well together.

Suppose:

```
Order Service
      │
      ▼
Recommendation Service
```

Recommendation starts failing.

**Circuit breaker:**

```
Failures
   ↓
Circuit OPEN
```

**Then graceful degradation:**

```
Circuit OPEN
    ↓
Don't call Recommendation
    ↓
Return product page without recommendations
```

Architecture:

```
Request
   │
   ▼
Circuit Breaker
   │
   ├── CLOSED → Recommendation
   │
   └── OPEN → Fallback
                    │
                    ▼
              Basic response
```

So:

> Circuit Breaker decides **when** to stop calling. Graceful Degradation decides **what the user gets instead**.

---

## 15. Graceful Degradation + Cache

Another common combination:

```
Request
   │
   ▼
Service
   │
   ▼
Downstream
   │
   X
   │
   ▼
Cache
   │
   ▼
Stale response
```

For example:

```
Live recommendation
       X
       ↓
Cached recommendation
       ↓
Return to user
```

This is particularly useful for read-heavy systems.

---

## 16. Graceful Degradation + Backpressure

Suppose:

```
Incoming = 100,000 requests/sec
Capacity = 20,000 requests/sec
```

**Backpressure says:**

> "We cannot process all of this."

**Graceful degradation says:**

> "Let's preserve the important functionality."

For example:

```
100,000 incoming
       │
       ▼
Backpressure
       │
       ├── Critical → Process
       │
       ├── Normal → Limit
       │
       └── Optional → Reject
```

This is a very powerful combination.

---

## 17. Graceful Degradation + Rate Limiting

Suppose your API has:

```
Normal capacity = 10,000 req/sec
```

Traffic suddenly becomes:

```
50,000 req/sec
```

Rate limiter:

```
10,000 → ALLOW
40,000 → REJECT/THROTTLE
```

But you can make the system smarter:

```
Critical API
→ 5,000 req/sec

Normal API
→ 4,000 req/sec

Optional API
→ 1,000 req/sec
```

During overload:

```
Optional API → disabled
```

Critical operations continue.

---

## 18. Graceful Degradation + Bulkhead

Suppose:

```
Order Service
│
├── Payment Pool → 50
├── Inventory Pool → 30
└── Recommendation Pool → 20
```

Recommendation becomes slow.

Bulkhead prevents it from consuming:

- Payment resources
- Inventory resources

Then graceful degradation can disable recommendations:

```
Recommendation
      ↓
Pool exhausted
      ↓
Fallback
      ↓
No recommendations
```

Meanwhile:

```
Payment → works
Inventory → works
Order → works
```

---

## 19. Graceful Degradation + Health Checks

Health checks tell you:

```
Service unhealthy
```

But they don't necessarily tell you:

> "What should the user experience?"

That's where graceful degradation comes in.

For example:

```
Recommendation Service
        ↓
Health = UNHEALTHY
        ↓
Traffic stopped / Circuit opened
        ↓
Application uses fallback
        ↓
Product page still works
```

---

## 20. Graceful Degradation vs Failover

These are related but different.

### Failover

Move work to another instance/service.

```
Primary DB
    X
    ↓
Replica DB
    ↓
Continue
```

### Graceful Degradation

Continue with reduced functionality.

```
Recommendation Service
    X
    ↓
Show product page without recommendations
```

So:

- **Failover** → Find another way to provide the same functionality
- **Graceful Degradation** → Provide less functionality while keeping the system useful

---

## 21. Graceful Degradation vs High Availability

**High Availability** means:

> The system is operational and accessible for a high percentage of time.

**Graceful Degradation** means:

> When something fails, preserve as much useful functionality as possible.

Example:

```
Payment Service DOWN
```

Without graceful degradation:

```
Entire e-commerce site → DOWN
```

With graceful degradation:

```
Browse products → WORKS
Search → WORKS
Cart → WORKS
Payment → TEMPORARILY UNAVAILABLE
```

That's a much better failure mode.

---

## 22. Fail-open vs Fail-closed

Graceful degradation often requires choosing between these behaviors.

### Fail-closed

When dependency fails:

```
Dependency unavailable
        ↓
Reject operation
```

Example:

```
Payment authorization unavailable
        ↓
Don't confirm payment
```

This is appropriate when correctness/security is critical.

### Fail-open

When optional dependency fails:

```
Dependency unavailable
        ↓
Continue without it
```

Example:

```
Recommendation unavailable
        ↓
Show product page anyway
```

The choice depends on the business operation.

---

## 23. Very important: Don't degrade critical correctness

Consider payment:

```
Payment Service unavailable
```

You **must not** do:

```
Payment unavailable
      ↓
Assume payment succeeded
      ↓
Confirm order
```

That is **not** graceful degradation.

That's potentially a correctness failure.

Instead:

```
Payment unavailable
      ↓
Order = PAYMENT_PENDING
      ↓
Retry / asynchronous processing
```

or:

```
Payment unavailable
      ↓
Tell user to try again
```

Graceful degradation must preserve business invariants.

---

## 24. Example: Food delivery system

Imagine:

```
                    Food App
                       │
       ┌───────────────┼────────────────┐
       ▼               ▼                ▼
   Restaurant       Payment          Maps
    Service          Service         Service
       │               │                │
       ▼               ▼                ▼
    Critical        Critical         Useful
```

Suppose Maps Service fails.

**Bad:**

```
Maps DOWN
 ↓
Order cannot be placed
```

**Better:**

```
Maps DOWN
 ↓
Restaurant browsing → works
Payment → works
Order → works
Delivery ETA → temporarily unavailable
```

The system has degraded functionality but remains useful.

---

## 25. Example: Netflix-style system

Imagine:

```
                    Streaming
                       │
       ┌───────────────┼───────────────┐
       ▼               ▼               ▼
   Video Stream   Recommendations   Reviews
```

If Recommendations fails:

```
Video Streaming → KEEP
Recommendations → DISABLE
Reviews → KEEP
```

The user can still watch content.

For a streaming platform, video playback is the critical path; recommendations are secondary.

---

## 26. Designing graceful degradation

When designing a system, explicitly classify dependencies:

```
Dependency
     │
     ├── Critical
     │
     ├── Important
     │
     └── Optional
```

Then define the failure behavior.

For example:

| Dependency | Importance | Failure behavior |
|---|---|---|
| Database | Critical | Fail operation |
| Payment | Critical | Payment pending/retry |
| Inventory | Critical | Don't confirm order |
| Recommendations | Optional | Hide recommendations |
| Email | Non-critical | Queue for retry |
| Analytics | Non-critical | Drop/delay events |
| Search ranking | Important | Use basic ranking |

This is exactly the type of thinking expected in senior system-design interviews.

---

## 27. A complete resilience architecture

Putting together everything you've learned:

```
                         CLIENT
                           │
                           ▼
                          WAF
                           │
                           ▼
                    RATE LIMITER
                           │
                           ▼
                     LOAD BALANCER
                           │
                      HEALTH CHECK
                           │
                           ▼
                      APPLICATION
                           │
                    ┌──────┴──────┐
                    │             │
                BULKHEAD      BACKPRESSURE
                    │             │
                    └──────┬──────┘
                           │
                         TIMEOUT
                           │
                     CIRCUIT BREAKER
                           │
                    ┌──────┴──────┐
                    │             │
                 SUCCESS       FAILURE
                    │             │
                    ▼             ▼
                Response      FALLBACK
                                  │
                     ┌────────────┼────────────┐
                     ▼            ▼            ▼
                   Cache        Queue       Reduced
                  Response      Later       Functionality
```

This is the bigger resilience picture.

---

## 28. A powerful example

Suppose your Order Service calls:

- Payment
- Inventory
- Recommendation
- Email

**Normal flow:**

```
Order
 │
 ├── Payment ─────────→ SUCCESS
 │
 ├── Inventory ───────→ SUCCESS
 │
 ├── Recommendation ─→ SUCCESS
 │
 └── Email ───────────→ SUCCESS
```

Now suppose:

```
Recommendation → DOWN
Email → SLOW
```

**A resilient design:**

```
Payment
  ↓
SUCCESS

Inventory
  ↓
SUCCESS

Recommendation
  ↓
Circuit OPEN
  ↓
Fallback
  ↓
Skip recommendation

Email
  ↓
Timeout
  ↓
Kafka
  ↓
Send asynchronously later
```

**Result:**

```
Order → SUCCESS
```

The customer doesn't need to know that two non-critical dependencies had problems.

That's graceful degradation in action.

---

## 29. Interview answer

If the interviewer asks:

> "What is graceful degradation?"

A strong answer is:

> Graceful degradation is a resilience strategy where a system continues providing its critical functionality even when optional components or dependencies fail or become overloaded. Instead of allowing a partial failure to cause total failure, the system uses mechanisms such as caching, fallbacks, asynchronous processing, feature disabling, simplified responses, and load shedding to provide reduced but useful functionality.

Then give an example:

> For an e-commerce application, if the recommendation service is unavailable, I would keep product browsing, cart, and checkout functional while disabling recommendations. For email, I would decouple it through a message queue so order processing doesn't depend on email delivery.

---

## 30. The mental model

You now have a very useful set of resilience patterns:

```
                    RESILIENCE
                        │
 ┌──────────────────────┼──────────────────────┐
 │                      │                      │
 ▼                      ▼                      ▼
Rate Limiting        Backpressure           Bulkhead
 │                      │                      │
 │                      │                      └─ Limit resources
 │                      └─ Control work flow
 └─ Control traffic

                        │
                        ▼
                     Timeout
                        │
                        └─ Don't wait forever
                        │
                        ▼
                  Retry + Backoff
                        │
                        └─ Recover transient failures
                        │
                        ▼
                 Circuit Breaker
                        │
                        └─ Stop unhealthy dependency
                        │
                        ▼
                  Graceful Degradation
                        │
                        └─ Keep critical functionality
```

The simplest way to remember all of them:

- **Rate Limiting** controls how much enters.
- **Backpressure** controls how work flows.
- **Bulkhead** controls how much resource one workload can consume.
- **Timeout** controls how long we wait.
- **Retry** handles transient failures.
- **Circuit Breaker** stops calls to unhealthy dependencies.
- **Graceful Degradation** keeps the important functionality working when something fails.

