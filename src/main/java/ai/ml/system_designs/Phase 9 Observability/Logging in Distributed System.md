# How Logging Works in Distributed Systems

In a distributed system, a single user request can travel through multiple services, servers, databases, Kafka topics, and external APIs.

Logging allows us to record what happened at each step and later reconstruct the complete workflow.

> **Simple definition:** Logging in a distributed system is the process of generating, propagating, collecting, centralizing, and searching logs from multiple services so engineers can understand and troubleshoot a distributed request.

---

## 1. Example Distributed System

Consider an e-commerce system:

```
                         Client
                           |
                           v
                     API Gateway
                           |
                           v
                    Order Service
                     /          \
                    /            \
                   v              v
             Inventory       Payment Service
               Service              |
                                    v
                                Bank API
                                    |
                                    v
                                Database
```

A customer sends:

```
POST /orders
```

The request might travel through:

```
Client
  ↓
API Gateway
  ↓
Order Service
  ↓
Payment Service
  ↓
Bank API
```

Every component can generate logs.

---

## 2. The Main Problem

Imagine Payment Service fails.

You might have:

```
Order Service → Server A
Payment Service → Server B
Bank API → External system
```

And their logs are stored separately.

Without correlation:

```
Order Service logs:
Order created

Payment Service logs:
Payment failed

Bank logs:
Timeout
```

The engineer has difficulty knowing whether these events belong to the same customer request.

This is where **Correlation ID** and **Trace ID** become important.

---

## 3. Complete Logging Flow

A typical architecture looks like this:

```
                    Client
                       |
                       v
                 API Gateway
                       |
                Generate/Accept
                Correlation ID
                       |
                       v
                 Order Service
                       |
                       v
               Payment Service
                       |
                       v
                   Bank API


       Each component generates logs
                    |
                    v
              Log Collector
                    |
                    v
             Central Log Store
                    |
                    v
             Search / Dashboard
```

For example:

```
Spring Boot
    ↓
SLF4J / Logback
    ↓
JSON logs
    ↓
Fluent Bit / Filebeat
    ↓
Elasticsearch / OpenSearch
    ↓
Kibana / OpenSearch Dashboards
```

---

## 4. Step 1 — Request Enters the System

The client sends:

```
POST /orders
```

The API Gateway receives it.

The gateway either:

- accepts a valid incoming correlation ID, according to the system's policy, or
- generates a new one.

For example:

```
Correlation ID = CORR-1001
```

The gateway logs:

```json
{
  "timestamp": "2026-09-16T10:00:00Z",
  "level": "INFO",
  "service": "api-gateway",
  "message": "Request received",
  "method": "POST",
  "path": "/orders",
  "correlationId": "CORR-1001"
}
```

---

## 5. Step 2 — Propagate the Correlation ID

The gateway calls Order Service:

```
POST /orders
X-Correlation-ID: CORR-1001
```

Order Service receives:

```
CORR-1001
```

and includes it in its logs:

```json
{
  "service": "order-service",
  "message": "Creating order",
  "orderId": "ORD-5001",
  "correlationId": "CORR-1001"
}
```

Now both services are connected.

---

## 6. Step 3 — Downstream Service Continues the ID

Order Service calls Payment Service:

```
POST /payments
X-Correlation-ID: CORR-1001
```

Payment Service logs:

```json
{
  "service": "payment-service",
  "message": "Payment processing started",
  "orderId": "ORD-5001",
  "correlationId": "CORR-1001"
}
```

Then Payment Service calls the bank:

```
POST /bank/pay
X-Correlation-ID: CORR-1001
```

and logs:

```json
{
  "service": "payment-service",
  "message": "Calling bank API",
  "correlationId": "CORR-1001"
}
```

---

## 7. Step 4 — An Error Happens

Suppose the bank API doesn't respond within 3 seconds.

Payment Service logs:

```json
{
  "timestamp": "2026-09-16T10:00:03Z",
  "level": "ERROR",
  "service": "payment-service",
  "message": "Bank API timeout",
  "orderId": "ORD-5001",
  "correlationId": "CORR-1001",
  "timeoutMs": 3000
}
```

Payment Service returns an error to Order Service.

Order Service logs:

```json
{
  "level": "ERROR",
  "service": "order-service",
  "message": "Payment failed",
  "orderId": "ORD-5001",
  "correlationId": "CORR-1001"
}
```

---

## 8. Step 5 — Logs Are Collected

The application usually doesn't directly send every log to Elasticsearch.

A common architecture is:

```
Application
     |
     v
stdout / log file
     |
     v
Log Agent
     |
     v
Log Pipeline
     |
     v
Central Storage
```

For example:

```
Spring Boot
    ↓
Console
    ↓
Fluent Bit
    ↓
Elasticsearch
    ↓
Kibana
```

The log agent can run alongside applications or on the host/container environment.

---

## 9. Why Centralized Logging?

Imagine you have:

- 100 microservices
- 500 application instances

Logs could be distributed across hundreds of machines.

You don't want engineers doing:

```
SSH Server 1
Search logs
SSH Server 2
Search logs
SSH Server 3
Search logs
...
```

Instead:

```
                  Centralized Logging
                         |
       +-----------------+----------------+
       |                 |                |
       v                 v                v
 Order Service     Payment Service    Inventory
       |                 |                |
       +-----------------+----------------+
                         |
                         v
                    Log Search
```

An engineer can search:

```
CORR-1001
```

and retrieve all related logs.

---

## 10. Structured Logging

In distributed systems, **structured logging** is very useful.

Instead of:

```
Payment failed for order ORD-5001
```

use:

```json
{
  "timestamp": "2026-09-16T10:00:03Z",
  "level": "ERROR",
  "service": "payment-service",
  "event": "PAYMENT_FAILED",
  "orderId": "ORD-5001",
  "correlationId": "CORR-1001",
  "errorCode": "BANK_TIMEOUT"
}
```

Now your logging system can easily query:

```
service = payment-service
```

or:

```
errorCode = BANK_TIMEOUT
```

or:

```
correlationId = CORR-1001
```

---

## 11. Correlation ID vs Trace ID

This is an important distributed-system concept.

You may have:

```
Correlation ID = CORR-1001
Trace ID       = abc123
Span ID        = span789
```

They have different purposes.

### Correlation ID

Answers:

> "Which logs/events belong to this logical operation?"

### Trace ID

Answers:

> "Which distributed trace does this operation belong to?"

### Span ID

Answers:

> "Which specific operation/service call is this?"

Example:

```
Trace ID: ABC123
|
+-- Gateway Span
|
+-- Order Service Span
|     |
|     +-- Database Span
|
+-- Payment Service Span
      |
      +-- Bank API Span
```

Correlation ID can be present across all related logs:

```
CORR-1001
```

---

## 12. Logging + Distributed Tracing

The most powerful setup is to connect logs and traces.

For example:

```
Metrics
   |
   | Error rate increased
   v
Tracing
   |
   | Payment → Bank API = 3 sec
   v
Logs
   |
   | Bank API timeout
   v
Root-cause investigation
```

A log might contain:

```json
{
  "service": "payment-service",
  "level": "ERROR",
  "message": "Bank API timeout",
  "correlationId": "CORR-1001",
  "traceId": "ABC123",
  "spanId": "XYZ789"
}
```

Now an engineer can jump from the trace to the exact logs for the problematic operation.

---

## 13. Logging Across Kafka

Logging becomes slightly different when communication is asynchronous.

Consider:

```
Order Service
      |
      v
    Kafka
      |
      v
Payment Consumer
```

Order Service publishes:

```
Topic: order-created

Headers:
    correlationId = CORR-1001
```

Payment Consumer receives the message and extracts:

```
CORR-1001
```

Then:

```
Order Service
    |
    | CORR-1001
    v
  Kafka
    |
    | CORR-1001
    v
Payment Consumer
```

Payment Consumer logs:

```json
{
  "service": "payment-consumer",
  "event": "MESSAGE_RECEIVED",
  "topic": "order-created",
  "partition": 3,
  "offset": 12540,
  "correlationId": "CORR-1001"
}
```

If processing fails:

```json
{
  "level": "ERROR",
  "event": "MESSAGE_PROCESSING_FAILED",
  "topic": "order-created",
  "partition": 3,
  "offset": 12540,
  "correlationId": "CORR-1001"
}
```

This is extremely useful for Kafka troubleshooting.

---

## 14. Thread Context / MDC

In Java applications, a common approach is to put the correlation/trace information into the logging context.

Conceptually:

```
HTTP Request
     |
     v
Filter
     |
     v
Extract Correlation ID
     |
     v
MDC
     |
     v
Business Logic
     |
     v
Logs automatically include ID
```

For example:

```java
MDC.put("correlationId", correlationId);
```

Then:

```java
log.info("Order created");
```

can produce:

```
correlationId=CORR-1001
Order created
```

You don't necessarily need to manually add the ID to every log message.

> **Important:** clear thread-local logging context after the request, especially because application servers reuse threads.

---

## 15. What Happens Inside a Logging Pipeline?

Let's look deeper.

```
Application
    |
    | 1. Create log event
    v
Logging Framework
    |
    | 2. Format
    v
JSON Log
    |
    | 3. Write
    v
stdout/file
    |
    | 4. Collect
    v
Log Agent
    |
    | 5. Buffer / transform
    v
Log Pipeline
    |
    | 6. Send
    v
Log Storage
    |
    | 7. Index
    v
Search Engine
    |
    v
Dashboard
```

---

## 16. Log Collection and Buffering

A log collector may temporarily buffer logs.

Why?

Suppose:

```
Application
    ↓
10,000 logs/sec
```

but the central logging system can currently accept:

```
7,000 logs/sec
```

A buffer can temporarily hold logs.

```
Application
    ↓
Log Agent
    ↓
Buffer
    ↓
Central Logging
```

This helps absorb short-term bursts.

For longer outages, you need appropriate durable buffering and backpressure/drop policies depending on how critical the logs are.

---

## 17. Logging During Failures

Suppose Elasticsearch becomes unavailable.

You don't want the entire application to stop just because logging is unavailable.

Therefore production logging should generally be designed so that:

```
Logging failure
      ≠
Application failure
```

This is one reason **asynchronous logging** and **local buffering** can be useful.

But there is a trade-off:

```
More buffering
     ↓
More memory/disk usage
     ↓
Potential log loss if buffers overflow
```

So logging itself needs capacity planning.

---

## 18. Example: Complete Request

Let's follow one request.

```
Client
  |
  | POST /orders
  | CORR-1001
  v
API Gateway
  |
  | log: request received
  v
Order Service
  |
  | log: order created
  |
  v
Kafka
  |
  | correlationId=CORR-1001
  v
Payment Consumer
  |
  | log: message received
  v
Payment Service
  |
  | log: payment started
  |
  v
Bank API
  |
  X
 timeout
```

Centralized logs:

```
10:00:00 Gateway
CORR-1001 Request received

10:00:00 Order Service
CORR-1001 Order created ORD-5001

10:00:01 Payment Consumer
CORR-1001 Message received

10:00:01 Payment Service
CORR-1001 Payment started

10:00:04 Payment Service
CORR-1001 Bank API timeout
```

Search:

```
correlationId = "CORR-1001"
```

and you can reconstruct the workflow.

---

## 19. Logging Architecture for Production

A typical modern architecture:

```
                     Microservices
                          |
             +------------+------------+
             |            |            |
             v            v            v
          Service A    Service B    Service C
             |            |            |
             +------------+------------+
                          |
                     JSON Logs
                          |
                          v
                    Log Collector
                  /      |       \
                 /       |        \
                v        v         v
            Buffer   Transform   Enrich
                \       |        /
                 \      |       /
                  v     v      v
                 Central Log Platform
                          |
                +---------+---------+
                |                   |
                v                   v
             Search             Dashboard
```

---

## 20. What Should Each Log Contain?

A useful distributed-system log often contains:

- timestamp
- level
- service
- environment
- instance/pod
- message/event
- correlationId
- traceId
- spanId
- request/operation
- business identifier
- error code
- exception

For Kafka:

- topic
- partition
- offset
- consumer group

For HTTP:

- method
- route
- status
- latency

Be careful with:

- passwords
- tokens
- API keys
- credit-card data
- private keys
- other sensitive information

> **These should not be logged.**

---

## 21. Logging and Exception Handling

Logging is especially important when handling distributed failures.

For example:

```
Payment Service
      |
      X
Bank timeout
      |
      v
Retry
      |
      X
Retry failed
      |
      v
Circuit breaker
      |
      v
Fallback
```

Logs should make the sequence visible:

```
WARN Bank API timeout attempt=1
WARN Retrying bank API attempt=2
ERROR Bank API timeout attempt=2
WARN Circuit breaker opened
ERROR Payment failed
```

Combined with:

```
correlationId=CORR-1001
```

you can reconstruct the failure.

---

## 22. Logging vs Metrics vs Tracing

Think of them together:

```
                Observability
                     |
        +------------+------------+
        |            |            |
        v            v            v
     Metrics       Logs        Traces
        |            |            |
        v            v            v
      WHAT?      DETAILS?       WHERE?
```

Example:

```
Metrics:
Payment error rate = 5%

        ↓

Trace:
Payment → Bank API = 4.8 sec

        ↓

Logs:
Bank API timeout after 5 sec
```

Each signal answers a different question.

---

## 23. Important Interview Question

> **"How would you troubleshoot a failed request in a microservices architecture?"**

A strong answer:

```
1. Start with metrics
       ↓
2. Identify abnormal service/endpoint
       ↓
3. Find Trace ID / Correlation ID
       ↓
4. Open distributed trace
       ↓
5. Identify slow/failed component
       ↓
6. Search centralized logs
       ↓
7. Check exception/error details
       ↓
8. Check dependency metrics
       ↓
9. Identify root cause
       ↓
10. Apply remediation
```

---

## ⭐ Interview-Ready Answer

> **Logging in a distributed system works by generating logs at each service or infrastructure component, attaching contextual information such as correlation ID and trace ID, propagating that context across synchronous and asynchronous communication, collecting logs through agents, and sending them to centralized storage for searching and analysis. When a request travels through multiple microservices, the common identifiers allow engineers to correlate logs from different services and reconstruct the complete workflow. Structured logging, centralized log collection, correlation IDs, distributed tracing, and appropriate log levels make distributed-system troubleshooting much easier.**

### The complete mental model:

```
Request
   ↓
Service
   ↓
Generate Log
   ↓
Add Correlation ID / Trace ID
   ↓
Propagate Context
   ↓
Next Service
   ↓
Generate Log
   ↓
Log Collector
   ↓
Centralized Log Storage
   ↓
Search / Dashboard
   ↓
Troubleshoot
```

### In one sentence:

> **Distributed logging turns thousands of independent service-level events into a searchable, correlated history of what happened to a request across the entire system.**

