# Distributed Tracing — What Is It, Why and How to Use It?

## 1. What is Distributed Tracing?

**Distributed Tracing** is an observability technique used to track a single request as it travels through multiple services, APIs, databases, queues, and other components in a distributed system.

In simple words:

> **Distributed tracing shows the complete journey of a request across your microservices and tells you where the request spent its time or where it failed.**

This becomes extremely useful in microservices, event-driven systems, and distributed architectures.

---

## 2. Why do we need Distributed Tracing?

Consider an e-commerce application:

```
                         Client
                           |
                           v
                     API Gateway
                           |
                           v
                     Order Service
                      /    |     \
                     /     |      \
                    v      v       v
               User Service Payment  Inventory
                              Service   Service
                                |
                                v
                             Bank API
```

Customer sends:

```
POST /orders
```

The request takes 5 seconds.

Without distributed tracing, you may see:

```
Order Service = 5 seconds
```

But you don't know **why**.

Is it:

- User Service?
- Payment Service?
- Inventory Service?
- Bank API?
- Database?
- Network?
- Retry?

Distributed tracing can show:

```
POST /orders                         5000 ms
│
├── API Gateway                        20 ms
│
├── Order Service                     100 ms
│
├── User Service                       50 ms
│
├── Payment Service                  4000 ms
│     │
│     └── Bank API                   3800 ms   <-- bottleneck
│
└── Inventory Service                 100 ms
```

Now you know where most of the time was spent.

---

## 3. Real-time example

Imagine Amazon-like checkout.

```
Customer
   |
   v
Checkout API
   |
   v
Order Service
   |
   +------> Cart Service
   |
   +------> Payment Service
   |             |
   |             v
   |          Bank API
   |
   +------> Inventory Service
                  |
                  v
               Database
```

The customer sees:

> "Checkout took 6 seconds."

Distributed tracing might show:

```
Trace ID: ABC123

Checkout API                 6000 ms
│
├── Order Service             100 ms
│
├── Cart Service               80 ms
│
├── Payment Service          5700 ms
│     │
│     └── Bank API           5500 ms
│
└── Inventory Service         100 ms
```

You immediately know:

> **Payment → Bank API is responsible for most of the latency.**

---

## 4. Trace, Span, and Distributed Trace

These terms are important.

### Trace

A **trace** represents the complete journey of one request.

```
Client
  ↓
Gateway
  ↓
Order
  ↓
Payment
  ↓
Bank
```

All of this can belong to one trace.

### Span

A **span** represents one unit of work within that trace.

For example:

```
Trace: ABC123

Span 1 → API Gateway
Span 2 → Order Service
Span 3 → Payment Service
Span 4 → Bank API
```

Each span contains information such as:

- Start time
- Duration
- Service
- Operation
- Status
- Attributes
- Parent relationship

### Easy way to remember

```
Trace = Complete journey
Span = One step in the journey
```

---

## 5. How Distributed Tracing works

The most important concept is **Trace Context Propagation**.

Consider:

```
Client
   |
   v
API Gateway
   |
   v
Order Service
   |
   v
Payment Service
   |
   v
Bank API
```

### Step 1 — Request enters the system

A trace is created.

```
Trace ID = ABC123
```

Gateway creates a span:

```
Span ID = S1
Trace ID = ABC123
Span ID  = S1
Service  = API Gateway
```

### Step 2 — Gateway calls Order Service

The tracing context is propagated to Order Service.

Conceptually:

```
Trace ID = ABC123
Parent Span = S1
```

Order Service creates:

```
Span ID = S2
Parent Span = S1
```

### Step 3 — Order calls Payment

```
Trace ID = ABC123
Parent Span = S2
```

Payment creates:

```
Span ID = S3
Parent Span = S2
```

### Step 4 — Payment calls Bank API

```
Trace ID = ABC123
Parent Span = S3
```

Bank API creates another span if it is instrumented and participates in the trace:

```
Span ID = S4
Parent Span = S3
```

---

## 6. Trace tree

The tracing backend can reconstruct:

```
Trace ABC123
│
├── API Gateway
│      └── 20 ms
│
└── Order Service
       ├── Cart Service
       │      └── 80 ms
       │
       ├── Payment Service
       │      └── 4000 ms
       │             └── Bank API
       │                    └── 3800 ms
       │
       └── Inventory Service
              └── 100 ms
```

This is the power of distributed tracing.

---

## 7. Trace Context Propagation

This is a very important interview topic.

When Service A calls Service B, Service A needs to pass tracing context to Service B.

```
Service A
   |
   | Trace Context
   v
Service B
```

A widely used standard is **W3C Trace Context**.

HTTP requests can carry headers such as:

- `traceparent`
- `tracestate`

Conceptually:

```
traceparent:
00-<trace-id>-<parent-span-id>-<flags>
```

In modern applications, tracing libraries generally handle this propagation automatically.

---

## 8. Distributed Tracing Architecture

A typical architecture looks like:

```
             Microservices
                  |
       +----------+----------+
       |          |          |
       v          v          v
    Order      Payment    Inventory
       |          |          |
       +----------+----------+
                  |
                  v
            OpenTelemetry
                  |
                  v
            OTel Collector
                  |
                  v
          Trace Backend
                  |
                  v
             Dashboard
```

A common modern approach uses **OpenTelemetry** for instrumentation and telemetry collection.

The backend could be a tracing system such as **Jaeger, Tempo**, or a commercial observability platform.

---

## 9. How to implement Distributed Tracing in Spring Boot

For a Spring Boot microservices architecture, a common approach is:

```
Spring Boot
     |
     v
OpenTelemetry
     |
     v
OTel Collector
     |
     v
Tracing Backend
```

Suppose you have:

```
Order Service
      |
      v
Payment Service
      |
      v
Bank API
```

Instrumentation can produce:

```
Trace ID: ABC123

Order Service
   Span: order.create
   Duration: 100ms

Payment Service
   Span: payment.process
   Duration: 500ms

Bank API
   Span: bank.authorize
   Duration: 450ms
```

You can then visualize the complete request.

---

## 10. Distributed Tracing with Kafka

This is particularly important because you're learning Kafka.

Consider:

```
Order Service
      |
      | Produce OrderCreated
      v
    Kafka
      |
      | Consume
      v
Payment Consumer
      |
      v
Payment Service
```

There isn't a direct HTTP call between Order Service and Payment Service.

Instead:

```
Order Service
     |
     v
 Kafka Producer
     |
     v
 Kafka Topic
     |
     v
 Kafka Consumer
     |
     v
Payment Service
```

Tracing context can be propagated through Kafka message metadata.

Conceptually:

```
Trace ID = ABC123

Order Service
     |
     | Produce
     v
Kafka
     |
     | Consume
     v
Payment Consumer
     |
     v
Payment Service
```

Now you can associate downstream processing with the original trace when the propagation and instrumentation are configured correctly.

### Why is this useful?

Suppose:

```
POST /orders
```

takes 8 seconds from the user's perspective.

Tracing may reveal:

```
POST /orders
      |
      v
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
      |
      v
Bank API
```

You can investigate where the delay occurs across both synchronous and asynchronous boundaries.

---

## 11. Distributed Tracing vs Correlation ID

These concepts are related but different.

### Correlation ID

An application-level identifier used to associate related operations or logs.

Example:

```
correlationId=ABC123
```

You might see:

```
Order Service
correlationId=ABC123

Payment Service
correlationId=ABC123
```

### Trace ID

Identifies a distributed trace.

```
traceId=ABC123
```

The trace contains multiple spans.

```
Trace ABC123
│
├── Gateway
├── Order
├── Payment
└── Bank
```

### Difference

```
Correlation ID
     ↓
Application-level correlation

Trace ID
     ↓
Distributed tracing identity
```

A system may use both.

---

## 12. Distributed Tracing vs Logging vs Metrics

This is another important interview question.

| Technology | Main question |
|---|---|
| Metrics | What is happening? |
| Logs | What happened? |
| Distributed Tracing | Where did this request go and where did it spend time? |

### Example

Customer:

> "Checkout is slow."

#### Metrics

```
Checkout p95 = 5 seconds
```

You know there's a problem.

#### Distributed tracing

```
Checkout
   |
   +-- Cart       100ms
   +-- User        50ms
   +-- Payment   4.5sec
   +-- Inventory 100ms
```

You know where the latency is.

#### Logs

Payment Service:

```
Bank API timeout
```

You get detailed diagnostic information.

### Together

```
Metrics
   ↓
Problem detected

Tracing
   ↓
Problem location identified

Logs
   ↓
Detailed investigation
```

---

## 13. What information does a distributed trace contain?

A trace generally contains:

### Trace ID

```
ABC123
```

Identifies the complete trace.

### Span ID

```
SPAN456
```

Identifies one operation.

### Parent Span ID

Shows the relationship between operations.

### Start time

```
10:00:00.000
```

### Duration

```
450 ms
```

### Service

```
payment-service
```

### Operation

```
POST /payments
```

### Status

```
OK
ERROR
```

### Attributes

Examples:

```
service.name=payment-service
http.request.method=POST
http.response.status_code=200
```

---

## 14. Sampling

Distributed systems can generate enormous amounts of tracing data.

Imagine:

```
1 billion requests/day
```

Storing every trace may be expensive.

Therefore, tracing systems often use **sampling**.

Example:

```
1,000,000 requests
       |
       v
    Sampling
       |
       v
100,000 traces stored
```

Common approaches include:

- Head sampling.
- Tail sampling.
- Different rates for different traffic.
- Higher retention for errors.

### Why keep error traces?

Successful requests may be less useful for debugging than failed requests.

So an organization may choose to retain:

```
Successful requests → lower sampling
Failed requests     → higher sampling
```

---

## 15. What problems can Distributed Tracing solve?

### Problem 1 — Latency

```
API = 5 seconds
```

Trace:

```
Payment = 4.5 seconds
```

You can investigate the slow operation.

### Problem 2 — Dependency failure

```
Order
  |
  v
Payment
  |
  v
Bank API
  |
  X
Timeout
```

The trace shows the dependency chain.

### Problem 3 — Database bottleneck

```
Payment
   |
   v
Database query
   |
   v
4 seconds
```

Trace reveals that the DB operation dominates the request.

### Problem 4 — Retry problems

```
Service A
    |
    v
Service B
    |
    +--> Request
    +--> Retry
    +--> Retry
    +--> Retry
```

Tracing can make repeated downstream calls visible.

---

## 16. Distributed Tracing and SLO

Tracing doesn't replace SLOs, but it helps investigate SLO violations.

Example:

```
SLO
99.9% availability
```

Monitoring detects:

```
SLI = 99.5%
```

The SLO is at risk.

Tracing can then help identify:

```
API Gateway
      |
      v
Order Service
      |
      v
Payment Service
      |
      v
Database
      X
```

So:

```
Monitoring
    ↓
SLO problem detected
    ↓
Distributed Tracing
    ↓
Problem location identified
    ↓
Logs
    ↓
Root cause investigation
```

---

## 17. Best practices

### 1. Propagate trace context everywhere

Especially across:

- HTTP
- gRPC
- Kafka
- Messaging

where your instrumentation supports it.

### 2. Don't put sensitive information in traces

Avoid:

- Passwords.
- Access tokens.
- Credit-card information.
- Sensitive personal information.

### 3. Use meaningful span names

Good:

```
POST /orders
payment.authorize
inventory.reserve
```

Avoid unnecessarily unique/high-cardinality operation names.

### 4. Connect traces with logs

For example:

```
Log
traceId=ABC123
```

Then engineers can jump from a trace to related logs.

### 5. Connect metrics and traces

For example:

```
High latency metric
       ↓
Find affected service
       ↓
Open representative trace
       ↓
Investigate slow span
```

---

## 18. Important interview questions

### Q1. What is Distributed Tracing?

> Distributed tracing tracks a request across multiple services and components in a distributed system, allowing engineers to understand request flow, latency, and failures.

### Q2. What is a Trace?

> A trace represents the complete journey of a request.

### Q3. What is a Span?

> A span represents one operation within a trace.

### Q4. How does tracing work across microservices?

> Through trace-context propagation, where the tracing context is passed from one service to the next.

### Q5. What is Trace ID?

> A unique identifier used to associate spans belonging to the same distributed trace.

### Q6. What is Span ID?

> An identifier for an individual span.

### Q7. How does Distributed Tracing work with Kafka?

> Trace context can be propagated through Kafka message metadata, allowing producer and consumer operations to be associated when properly instrumented.

### Q8. Why is sampling required?

> To control tracing data volume, storage, and cost while retaining useful traces.

### Q9. Distributed Tracing vs Correlation ID?

> Correlation ID is an application-level identifier used to correlate related operations, while Trace ID belongs to the distributed tracing model and connects spans in a trace.

### Q10. Distributed Tracing vs Monitoring?

> Monitoring tells you that the system or a service is behaving abnormally; distributed tracing helps show where an individual request spent time and where failures occurred.

---

## 19. Complete Observability Architecture

For your system-design preparation, keep this picture in mind:

```
                         USERS
                           |
                           v
                    API Gateway
                           |
             +-------------+-------------+
             |             |             |
             v             v             v
          Order         Payment       Inventory
          Service       Service        Service
             |             |             |
             +-------------+-------------+
                           |
                          Kafka
                           |
                           v
                       Consumers
                           |
                           v
                       Database


        +--------------------------------------+
        |          OBSERVABILITY               |
        +--------------------------------------+
             |          |           |
             v          v           v
          Metrics     Logs       Traces
             |          |           |
             v          v           v
         Monitoring   Logging   Distributed
                                Tracing
             |          |           |
             +----------+-----------+
                        |
                        v
                    Alerting
                        |
                        v
                 Engineer Action
```

---

## 20. The most important concept to remember

Think of a request like a package traveling through multiple cities:

```
Package
  |
  v
Delhi
  |
  v
Mumbai
  |
  v
Bangalore
  |
  v
Chennai
```

A tracking system tells you:

- Where the package went.
- When it reached each location.
- How long it stayed there.
- Where it got delayed.

Distributed tracing does the same thing for a request.

```
Request
  |
  v
API Gateway
  |
  v
Order Service
  |
  v
Payment Service
  |
  v
Bank API
```

It tells you:

- Where the request went.
- How long each service took.
- Which dependency was slow.
- Where an error occurred.
- How services are related.

---

## ⭐ Interview-ready answer

> **Distributed tracing is an observability technique that tracks a single request across multiple services and components in a distributed system. A trace consists of multiple spans, where each span represents an individual operation. Trace context is propagated between services so that the complete request path can be reconstructed. It is used to identify latency bottlenecks, dependency failures, errors, retries, and performance problems in microservices and event-driven architectures.**

### Your observability hierarchy

```
Logging
   ↓
Metrics
   ↓
Monitoring
   ↓
SLI
   ↓
SLO
   ↓
SLA
   ↓
Alerting
   ↓
Tracing
   ↓
Distributed Tracing
   ↓
Correlation ID
```

### The key distinction is:

> **Metrics tell you that there is a problem. Distributed tracing helps you locate the problem within the request path. Logs help you investigate the detailed events around it.**

