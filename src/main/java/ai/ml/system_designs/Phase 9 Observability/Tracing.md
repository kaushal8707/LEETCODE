# Tracing — What Is It, Why and How to Use It?

## 1. What is Tracing?

**Tracing** is a technique used to track the execution of a request as it moves through a system.

In simple words:

> **Tracing tells us where a request went, how long it spent in each component, and where the problem occurred.**

This is especially important in microservices and distributed systems.

---

## 2. Why do we need Tracing?

Consider a simple monolith:

```
Client
  |
  v
Application
  |
  v
Database
```

If the request takes 5 seconds, debugging is relatively straightforward.

But in microservices:

```
Client
  |
  v
API Gateway
  |
  v
Order Service
  |
  +----> User Service
  |
  +----> Payment Service
  |          |
  |          +----> Bank API
  |
  +----> Inventory Service
             |
             +----> Database
```

Suppose the customer says:

> "My order is taking 8 seconds."

Which service is responsible?

- API Gateway?
- Order Service?
- Payment Service?
- Bank API?
- Inventory Service?
- Database?

Tracing helps you answer this question.

---

## 3. Real-time example

Suppose a customer calls:

```
POST /orders
```

The request travels through:

```
Client
  |
  v
API Gateway
  |
  v
Order Service
  |
  +----> User Service
  |
  +----> Payment Service
  |          |
  |          v
  |       Bank API
  |
  +----> Inventory Service
```

Total request time:

```
5000 ms
```

Tracing might show:

```
POST /orders
|
+-- API Gateway          50 ms
|
+-- Order Service       100 ms
|
+-- User Service         80 ms
|
+-- Payment Service    4000 ms  <-- bottleneck
|      |
|      +-- Bank API     3800 ms
|
+-- Inventory Service   150 ms
```

Now the problem is much easier to identify.

> **Payment Service → Bank API is consuming most of the request time.**

---

## 4. How tracing works

The basic concept is:

```
Trace
  |
  +--- Span
  |
  +--- Span
  |
  +--- Span
```

### Trace

A **trace** represents the complete journey of one request through the distributed system.

Example:

```
Trace ID:
abc123
```

Everything related to that request belongs to the same trace.

---

## 5. What is a Span?

A **span** represents one unit of work within a trace.

For example:

```
Trace: POST /orders

Span 1: API Gateway
Span 2: Order Service
Span 3: Payment Service
Span 4: Bank API
Span 5: Inventory Service
```

Each span can contain information such as:

- Service name
- Operation name
- Start time
- Duration
- Status
- Attributes
- Events
- Parent span
- Trace ID
- Span ID

---

## 6. Trace vs Span

This is an important interview question.

### Trace

Represents the entire request journey.

```
Client → Gateway → Order → Payment → Bank
```

### Span

Represents one operation within that journey.

```
Payment Service → Bank API
```

### Easy way to remember

```
Trace = complete journey
Span = one step in the journey
```

---

## 7. How tracing works internally

Let's look at a distributed system.

```
Client
   |
   v
API Gateway
   |
   v
Order Service
   |
   +----------> Payment Service
   |                 |
   |                 v
   |              Bank API
   |
   +----------> Inventory Service
```

When the request enters the system:

### Step 1 — Create Trace ID

The tracing system creates a unique identifier:

```
Trace ID = 7f83a91c
```

This identifies the complete request.

### Step 2 — Create first Span

API Gateway creates:

```
Span ID = span-001
Trace ID = 7f83a91c
Span ID  = span-001
Service  = API Gateway
```

### Step 3 — Propagate tracing context

When API Gateway calls Order Service, it sends tracing information with the request.

Conceptually:

```
Trace ID = 7f83a91c
Parent Span = span-001
```

Order Service creates another span:

```
Span ID = span-002
Parent = span-001
```

### Step 4 — Continue propagation

Order Service calls Payment Service:

```
Trace ID = 7f83a91c
Parent Span = span-002
```

Payment Service creates:

```
Span ID = span-003
Parent = span-002
```

The same process continues across the system.

---

## 8. Trace tree

Eventually, the tracing backend can reconstruct something like:

```
Trace: 7f83a91c
│
├── API Gateway
│     └── 50 ms
│
└── Order Service
      ├── User Service
      │     └── 80 ms
      │
      ├── Payment Service
      │     └── 4000 ms
      │           └── Bank API
      │                 └── 3800 ms
      │
      └── Inventory Service
            └── 150 ms
```

This gives engineers a complete view of the request.

---

## 9. Trace Context Propagation

This is one of the most important concepts in distributed tracing.

When Service A calls Service B, the tracing context needs to travel with the request.

```
Service A
   |
   | Trace Context
   v
Service B
```

A commonly used standard is **W3C Trace Context**.

It uses HTTP headers such as:

- `traceparent`
- `tracestate`

Conceptually:

```
traceparent:
00-<trace-id>-<parent-id>-<flags>
```

You normally don't manually construct these headers in a modern instrumented application; tracing libraries and frameworks can handle propagation.

---

## 10. Example of tracing in microservices

Imagine an e-commerce system:

```
                    Client
                      |
                      v
                API Gateway
                      |
                      v
                 Order Service
                 /     |      \
                /      |       \
               v       v        v
           User      Payment   Inventory
                      |
                      v
                   Bank API
```

A trace could look like:

```
Trace ID: 12345

API Gateway
   20ms
     |
     v
Order Service
   100ms
     |
     +---- User Service
     |       50ms
     |
     +---- Payment Service
     |       3000ms
     |          |
     |          +---- Bank API
     |                 2900ms
     |
     +---- Inventory Service
             100ms
```

The engineer immediately sees:

```
Payment → Bank API
        ↓
    2900 ms
```

That's the bottleneck.

---

## 11. Tracing in asynchronous systems

Tracing becomes more interesting with Kafka.

Suppose:

```
Order Service
     |
     v
   Kafka
     |
     v
Payment Consumer
     |
     v
Payment Service
```

The request doesn't directly call the Payment Service.

Instead:

```
Order Service
     |
     | Produce Event
     v
   Kafka
     |
     | Consume Event
     v
Payment Consumer
```

Tracing can propagate context through the message so the relationship between producer and consumer work can be represented.

This helps answer:

> "Which original request caused this Kafka message to be processed?"

For example:

```
Trace ID: ABC123

POST /orders
     |
     v
Order Service
     |
     v
Kafka Produce
     |
     v
Kafka Topic
     |
     v
Payment Consumer
     |
     v
Payment Service
```

This is particularly useful when debugging event-driven architectures.

---

## 12. Tracing vs Logging vs Metrics

This is extremely important for interviews.

| Tool | Main question |
|---|---|
| Metrics | What is happening? |
| Logs | What happened? |
| Tracing | Where did this request go? |

### Example

Customer reports:

> "Checkout is slow."

#### Metrics

You see:

```
Checkout p95 latency = 5 seconds
```

#### Tracing

You discover:

```
Checkout
  |
  +-- Cart Service       100ms
  +-- User Service        50ms
  +-- Payment Service   4.5sec  <-- problem
```

#### Logs

You investigate Payment Service:

```
Bank API timeout
```

### Together:

```
Metrics
   ↓
Problem detected

Tracing
   ↓
Problem location identified

Logs
   ↓
Detailed cause investigated
```

---

## 13. Tracing and Monitoring

Monitoring tells you:

> "Something is wrong."

Tracing helps tell you:

> "Where in the request path is it wrong?"

Example:

```
Monitoring
    |
    v
Payment API latency increased
    |
    v
Tracing
    |
    v
Payment Service
    |
    v
Bank API
    |
    v
Slow response
```

---

## 14. Tracing and Correlation ID

You will encounter **Correlation ID** frequently in distributed systems.

A correlation ID is an identifier used to associate related operations or logs.

Example:

```
Correlation ID = ABC123
```

You might see it in logs across multiple services:

```
Order Service
correlationId=ABC123

Payment Service
correlationId=ABC123

Inventory Service
correlationId=ABC123
```

Modern distributed tracing usually provides a Trace ID for this purpose, while a separate business/request correlation ID may still be useful depending on the system.

### Important distinction

```
Trace ID
    ↓
Identifies a distributed trace

Span ID
    ↓
Identifies one operation within that trace

Correlation ID
    ↓
Application-level identifier used to correlate related operations
```

They can be related, but they are not necessarily the same thing.

---

## 15. How to implement tracing in Spring Boot

A modern Spring Boot application commonly uses **OpenTelemetry** instrumentation and an OpenTelemetry-compatible backend.

A conceptual architecture is:

```
Spring Boot
     |
     v
OpenTelemetry
     |
     v
OTel Collector
     |
     +---------> Trace Backend
     |
     +---------> Metrics Backend
     |
     +---------> Log Backend
```

The exact components can vary.

### Example

```
Client
  |
  v
Order Service
  |
  v
Payment Service
  |
  v
Database
```

Instrumentation automatically creates spans around relevant operations.

You might see:

```
Trace ID: 8f92ab

POST /orders
  10ms
    |
    +-- Order Service
    |      50ms
    |
    +-- Payment Service
           500ms
             |
             +-- DB query
                    450ms
```

---

## 16. What information does a Span contain?

A span can contain:

### Identity

- Trace ID
- Span ID
- Parent Span ID

### Timing

- Start time
- End time
- Duration

### Operation

- HTTP GET
- POST /orders
- DB query
- Kafka produce
- Kafka consume

### Attributes

For example:

```
service.name = payment-service
http.request.method = POST
http.response.status_code = 200
```

### Status

```
OK
ERROR
UNSET
```

### Events

Important events that occurred during the operation.

---

## 17. Sampling

One important challenge with tracing is **cost**.

Imagine:

```
1 billion requests/day
```

If you store every trace, the amount of telemetry can become very large.

Therefore, tracing systems often use **sampling**.

### Example

```
1,000,000 requests
        |
        v
    Sampling
        |
        v
100,000 traces stored
```

Possible approaches include:

- Head sampling.
- Tail sampling.
- Different sampling rates for different traffic.
- Keeping errors at a higher rate.

### Why keep error traces?

Suppose:

```
Successful requests = 99.9%
Failed requests = 0.1%
```

Failures are usually more valuable for debugging, so organizations may retain them at a higher rate.

---

## 18. Distributed Tracing

**Distributed tracing** is tracing a request across multiple independently running services or components.

Example:

```
Client
  |
  v
API Gateway
  |
  v
Order Service
  |
  +----> User Service
  |
  +----> Payment Service
  |          |
  |          v
  |       Bank API
  |
  +----> Inventory Service
```

One trace connects the complete request path.

This is particularly useful in:

- Microservices.
- Serverless systems.
- Event-driven architectures.
- Distributed databases.
- External API integrations.

---

## 19. What problems can tracing solve?

### Problem 1 — Slow API

```
API = 5 sec
```

Tracing:

```
Payment Service = 4.5 sec
```

### Problem 2 — Random failures

Tracing:

```
Order
  ↓
Payment
  ↓
Bank API
  ↓
Timeout
```

### Problem 3 — Dependency failure

```
Order Service
      |
      v
Inventory Service
      |
      v
Database
      X
```

Trace shows the dependency chain.

### Problem 4 — Retry amplification

```
Service A
   |
   v
Service B
   |
   +--> Retry
   +--> Retry
   +--> Retry
```

Tracing can reveal repeated downstream calls and their latency contribution.

---

## 20. Tracing best practices

### 1. Propagate trace context

Ensure trace context flows across:

- HTTP
- gRPC
- Kafka
- Messaging

where supported by your instrumentation.

### 2. Don't put sensitive information into spans

Avoid putting:

- Passwords.
- Access tokens.
- Credit-card data.
- Other sensitive information.

into trace attributes.

### 3. Use meaningful span names

Good:

```
POST /orders
payment.authorize
inventory.reserve
```

Avoid excessively high-cardinality or unique span names.

### 4. Combine tracing with metrics and logs

A good observability architecture connects:

```
Metrics
   |
   +---- Trace ID
   |
   +---- Logs
          |
          +---- Trace ID
```

Then an engineer can move from:

```
Metric → Trace → Logs
```

very quickly.

---

## 21. Tracing interview questions

### Q1. What is tracing?

> Tracing tracks a request as it travels through components of a distributed system.

### Q2. What is a Trace?

> A trace represents the complete journey of a request.

### Q3. What is a Span?

> A span represents one operation within a trace.

### Q4. Difference between Trace and Span?

```
Trace = Complete request journey
Span  = Individual operation
```

### Q5. What is Trace ID?

> A unique identifier that associates spans belonging to the same distributed trace.

### Q6. What is Span ID?

> An identifier for an individual span.

### Q7. How does tracing work between microservices?

> Through context propagation, where trace context is carried from one service to the next.

### Q8. How does tracing work with Kafka?

> Tracing context can be propagated through message metadata so producer and consumer operations can be associated.

### Q9. Why is sampling required?

> To control telemetry volume, storage, and cost while retaining useful traces.

### Q10. Tracing vs logging?

```
Tracing → Request journey
Logging → Detailed events
```

### Q11. Tracing vs monitoring?

```
Monitoring → Detect/understand system behavior
Tracing   → Follow individual requests
```

---

## Final picture

For your system-design preparation, remember the complete observability flow:

```
                         SYSTEM
                            |
          +-----------------+-----------------+
          |                 |                 |
          v                 v                 v
       Metrics            Logs             Traces
          |                 |                 |
          v                 v                 v
      Monitoring       Debug Details     Request Journey
          |                 |                 |
          +-----------------+-----------------+
                            |
                            v
                         Alerting
                            |
                            v
                     Engineer Action
```

And within tracing:

```
                    TRACE
                      |
       +--------------+--------------+
       |              |              |
       v              v              v
     Span           Span           Span
       |              |              |
   Gateway         Order          Payment
                                     |
                                     v
                                  Bank API
```

---

## ⭐ Interview-ready definition

> **Tracing is an observability technique that records the journey of a request across distributed components. A trace consists of multiple spans, where each span represents an individual operation. Trace context is propagated between services so engineers can understand request flow, identify bottlenecks, diagnose failures, and measure latency across distributed systems.**

### Your observability learning sequence

> **Logging → Metrics → Monitoring → SLI → SLO → SLA → Alerting → Tracing → Distributed Tracing → Correlation ID**

