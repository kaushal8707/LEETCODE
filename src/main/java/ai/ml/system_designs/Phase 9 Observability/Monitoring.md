# Monitoring — What Is It, Why and How to Use It?

## 1. What is Monitoring?

**Monitoring** is the process of continuously observing a system to understand its health, performance, availability, and behavior.

In simple words:

> **Monitoring tells us what is happening inside our system and whether the system is healthy.**

For example, for a Payment API, monitoring can tell us:

- Is the service up?
- How many requests are coming?
- How many requests are failing?
- How fast are requests?
- Is CPU too high?
- Is memory increasing?
- Is the database slow?
- Is Kafka consumer lag increasing?

---

## 2. Why do we need Monitoring?

Imagine you have a production system with 100 microservices.

Without monitoring:

```
Production
    |
    |--- Order Service
    |--- Payment Service
    |--- User Service
    |--- Notification Service
    |--- Kafka
    |--- Redis
    |--- Database
```

Something fails.

You don't know:

- Which service failed?
- When did it fail?
- Why did it fail?
- How many users are affected?
- Is the database responsible?
- Is Kafka responsible?

Monitoring gives you visibility.

### With monitoring

```
Production System
       |
       v
   Monitoring
       |
       +---- CPU
       +---- Memory
       +---- Requests
       +---- Errors
       +---- Latency
       +---- Database
       +---- Kafka
       +---- Redis
       |
       v
    Dashboard
```

Now engineers can understand what is happening.

---

## 3. Main goals of Monitoring

### 1. Detect problems

Example:

```
Error Rate
   0.1%
     ↓
   0.2%
     ↓
   2%
     ↓
   10%  ← Problem
```

Monitoring shows that the system is degrading.

### 2. Understand performance

Example:

```
p50 = 100 ms
p95 = 300 ms
p99 = 800 ms
```

You can see whether API latency is increasing.

### 3. Understand resource utilization

Monitor:

- CPU
- Memory
- Disk
- Network
- Connection pools
- Thread pools

### 4. Troubleshoot production problems

Monitoring helps answer:

> "What changed when the problem started?"

### 5. Capacity planning

Suppose:

```
Current traffic = 5,000 requests/sec
```

Monitoring shows traffic increasing:

```
5K → 6K → 7K → 8K → 10K
```

The team can plan additional capacity.

---

## 4. Monitoring vs Metrics vs Alerting

These concepts are related but different.

| Concept | Meaning |
|---|---|
| Metrics | Numeric measurements |
| Monitoring | Observing and analyzing system health using those measurements |
| Alerting | Notifying engineers when an important condition requires action |

### Easy way to remember

```
Metrics
   ↓
Monitoring
   ↓
Alerting
   ↓
Engineer Action
```

Example:

```
Metric:
Error rate = 8%

        ↓

Monitoring:
Error rate has increased significantly

        ↓

Alert:
Error rate > 5% for 5 minutes

        ↓

Engineer:
Investigates the problem
```

---

## 5. Monitoring vs Logging vs Tracing

This is especially important for microservices.

| Technology | Answers |
|---|---|
| Metrics | What is happening? |
| Logs | What happened? |
| Traces | Where did this request go? |
| Monitoring | Is the system healthy and how is it behaving? |
| Alerting | Does someone need to take action? |

### Example

Suppose a customer says:

> "My payment is very slow."

#### Monitoring

You discover:

```
Payment API p95 latency
300 ms → 4 seconds
```

#### Logs

You find:

```
Database query took 3.5 seconds
```

#### Distributed Trace

You see:

```
API Gateway
    ↓ 20ms
Payment Service
    ↓ 50ms
Fraud Service
    ↓ 100ms
Payment Database
    ↓ 3.5 sec  ← bottleneck
```

Together, these tools help identify the problem.

---

## 6. What should we monitor?

A common starting point for services is the **four golden signals**:

### 1. Latency

How long does a request take?

Example:

```
p50 = 100 ms
p95 = 300 ms
p99 = 700 ms
```

### 2. Traffic

How much demand is the system receiving?

Example:

```
Requests/sec = 10,000
```

For Kafka:

```
Messages/sec = 50,000
```

### 3. Errors

How many requests are failing?

Example:

```
Total requests = 100,000
Errors = 500

Error rate = 0.5%
```

### 4. Saturation

How close are system resources to their limits?

Examples:

```
CPU = 85%
Memory = 90%
Disk = 80%
DB connections = 95%
Kafka consumer lag = increasing
```

### Easy memory trick

```
Golden Signals

Latency
Traffic
Errors
Saturation
```

---

## 7. How Monitoring works internally

Let's take a Spring Boot Payment Service.

```
                 Users
                   |
                   v
             Load Balancer
                   |
                   v
          Payment Service
                   |
        +----------+----------+
        |          |          |
        v          v          v
      Redis     Database     Kafka
        |          |          |
        +----------+----------+
                   |
                   v
             Observability
                   |
       +-----------+-----------+
       |           |           |
       v           v           v
    Metrics       Logs       Traces
       |           |           |
       +-----------+-----------+
                   |
                   v
              Monitoring
                   |
                   v
               Dashboard
                   |
                   v
                Alerts
```

---

## 8. Step-by-step: How Monitoring works

### Step 1 — Application generates telemetry

Your application produces:

- Metrics
- Logs
- Traces

For example:

```
Payment API
Requests = 10,000/sec
Errors = 50/sec
p95 latency = 300 ms
```

### Step 2 — Collect telemetry

A monitoring/observability platform collects the data.

A common architecture might use:

```
Spring Boot
    |
    v
Micrometer / OpenTelemetry
    |
    v
Metrics / Logs / Traces backend
```

### Step 3 — Store the data

The monitoring backend stores the telemetry so engineers can query historical and current behavior.

For example:

```
10:00 → Error rate = 0.1%
10:05 → Error rate = 0.2%
10:10 → Error rate = 1%
10:15 → Error rate = 8%
```

Now you can see when the degradation started.

### Step 4 — Visualize

A dashboard displays the information.

Example:

```
Payment Service Dashboard

Requests/sec       10,000
Error Rate            2%
p95 Latency          450ms
CPU                    72%
Memory                 68%
DB Connections         80%
Kafka Lag            1,200
```

### Step 5 — Alert

If an important condition is detected:

```
Error Rate > 5%
for 5 minutes
```

the alerting system can notify the on-call engineer.

---

## 9. Monitoring in Spring Boot

For a Spring Boot application, a common approach is:

```
Spring Boot
    |
    v
Actuator / Micrometer
    |
    v
Prometheus
    |
    v
Grafana
```

### Spring Boot

Application exposes useful operational metrics.

### Micrometer

Provides instrumentation and metric collection APIs.

### Prometheus

Collects and stores time-series metrics.

### Grafana

Provides dashboards and visualization.

This is one commonly used stack; other monitoring platforms can provide similar capabilities.

---

## 10. Monitoring an API

Suppose you have:

```
POST /payments
```

You can monitor:

### Traffic

```
10,000 requests/sec
```

### Error rate

```
0.5%
```

### Latency

```
p50 = 100ms
p95 = 300ms
p99 = 700ms
```

### Availability

```
99.95%
```

### Infrastructure

```
CPU = 70%
Memory = 65%
DB connections = 75%
```

Now you have a good operational picture of the service.

---

## 11. Monitoring Kafka

Since you're learning Kafka, monitoring Kafka is very important.

Consider:

```
Producer
    |
    v
Kafka
    |
    v
Consumer
```

Useful Kafka monitoring metrics include:

### Producer

- Produce rate
- Produce latency
- Error rate
- Request failures

### Broker

- CPU
- Memory
- Disk usage
- Network throughput
- Request latency
- Under-replicated partitions

### Consumer

- Consumer lag
- Records consumed
- Processing latency
- Consumer errors
- Rebalances

### Example

```
Consumer Lag

100
 ↓
500
 ↓
2,000
 ↓
10,000
 ↓
50,000
```

This could indicate that the consumer is processing messages slower than they are arriving.

But lag by itself doesn't necessarily mean a user-facing outage. The important question is whether lag threatens a defined processing/freshness objective.

---

## 12. Monitoring databases

For a database, monitor:

### Performance

- Query latency
- Queries/sec
- Slow queries

### Resources

- CPU
- Memory
- Disk
- IOPS

### Connections

- Active connections
- Connection pool utilization
- Connection wait time

### Reliability

- Replication lag
- Failed queries
- Connection failures
- Failover events

Example:

```
API latency increases
       ↓
Database query latency increases
       ↓
DB CPU = 95%
       ↓
Database becomes bottleneck
```

Monitoring helps establish this relationship.

---

## 13. Monitoring Redis

For Redis, monitor:

- Memory usage
- Cache hit ratio
- Cache miss ratio
- Commands/sec
- Latency
- Connected clients
- Evictions
- Keyspace statistics
- Replication health

Example:

```
Cache Hit Ratio

95%
 ↓
90%
 ↓
70%
 ↓
40%
```

A falling hit ratio could increase database traffic and potentially contribute to higher application latency.

---

## 14. Monitoring and SLI/SLO

This connects directly with what you learned earlier.

```
Monitoring
    |
    +---- Metrics
    |
    +---- SLI
             |
             v
           SLO
             |
             v
      SLO Evaluation
             |
             v
          Alerting
```

Example:

### SLI

Actual availability:

```
99.85%
```

### SLO

Target:

```
99.9%
```

### Monitoring

Detects:

```
Availability is decreasing
```

### Alerting

Notifies the team when the defined condition indicates the SLO is at risk.

---

## 15. Monitoring and Alerting are NOT the same

This is a common interview question.

### Monitoring

Answers:

> "What is happening?"

### Alerting

Answers:

> "Does someone need to take action?"

For example:

```
CPU = 85%
```

Monitoring reports this.

But you may not necessarily want an alert just because CPU reaches 85%.

Instead, you might define:

```
CPU > 90%
AND
sustained for 10 minutes
```

or use a user-impact/reliability signal.

This reduces unnecessary alerts.

---

## 16. Monitoring in a microservices architecture

Consider:

```
                  API Gateway
                       |
          +------------+------------+
          |            |            |
          v            v            v
       Order         Payment      User
       Service       Service      Service
          |            |            |
          +------------+------------+
                       |
                     Kafka
                       |
              +--------+--------+
              |                 |
              v                 v
         Notification       Analytics
```

You need visibility across the entire system.

Monitor:

### API Gateway

- Request rate
- Error rate
- Latency

### Order Service

- Requests
- Errors
- Latency
- Database performance

### Payment Service

- Payment success rate
- Payment failures
- Latency

### Kafka

- Consumer lag
- Broker health
- Replication

### Database

- Query latency
- CPU
- Connections
- Replication

### Infrastructure

- CPU
- Memory
- Disk
- Network

---

## 17. What makes good monitoring?

Good monitoring should help answer four questions quickly:

### 1. Is the system healthy?

- Availability
- Error rate
- Latency

### 2. Are users experiencing problems?

- Failed requests
- Slow requests
- Failed transactions

### 3. Where is the problem?

- API
- Service
- Database
- Kafka
- Redis
- Network

### 4. When did the problem start?

Historical graphs help identify:

```
Deployment
    ↓
Latency increase
    ↓
Error increase
```

This can help correlate a production problem with a recent change.

---

## 18. Monitoring best practices

### 1. Monitor user-facing behavior

Don't monitor only infrastructure.

CPU can be normal while users are receiving errors.

Monitor:

- Availability
- Latency
- Errors
- Business-critical operations

### 2. Use dashboards

Create dashboards for:

- Application
- Database
- Kafka
- Infrastructure
- Business-critical flows

### 3. Don't collect every possible metric blindly

Too many metrics can increase storage cost and make troubleshooting harder.

Focus on metrics that answer useful operational questions.

### 4. Monitor dependencies

Your service may be healthy while a dependency is failing.

```
Payment Service
      |
      v
Bank API
      |
      X
   Failure
```

Monitoring should make this visible.

### 5. Connect monitoring to alerting

Monitoring provides visibility.

Alerting ensures important conditions get attention.

### 6. Include business metrics

Technical metrics alone aren't enough.

For payment systems, examples include:

- Payments attempted
- Payments successful
- Payments failed
- Payment processing time

A service can have healthy CPU and memory while payment success rate is falling.

---

## 19. Monitoring interview questions

### Q1. What is monitoring?

> Monitoring is continuously observing and analyzing system health, performance, and behavior using telemetry such as metrics, logs, and traces.

### Q2. Why do we need monitoring?

> To detect problems, troubleshoot failures, understand performance, track reliability, and plan capacity.

### Q3. Monitoring vs alerting?

> Monitoring observes and analyzes system behavior.
>
> Alerting notifies engineers when an important condition requires action.

### Q4. What are the four golden signals?

```
Latency
Traffic
Errors
Saturation
```

### Q5. What should you monitor in a microservices system?

At minimum:

- Request rate
- Error rate
- Latency
- Availability
- CPU
- Memory
- Database health
- Kafka health
- Dependency health

### Q6. How do you monitor Kafka?

Monitor:

- Consumer lag
- Processing latency
- Throughput
- Broker health
- Under-replicated partitions
- Producer/consumer errors
- Rebalances

### Q7. How does monitoring help SLO?

> Monitoring provides the measurements needed to calculate SLIs and determine whether the service is meeting or is at risk of missing its SLO.

---

## Final picture

For your system-design and SRE preparation, remember the relationship:

```
                    APPLICATION
                         |
          +--------------+--------------+
          |              |              |
          v              v              v
       Metrics         Logs          Traces
          |              |              |
          +--------------+--------------+
                         |
                         v
                    MONITORING
                         |
             +-----------+-----------+
             |                       |
             v                       v
          Dashboard               SLI
                                     |
                                     v
                                    SLO
                                     |
                                     v
                                  Alerting
                                     |
                                     v
                              Engineer Action
```

---

## One-line interview answer

> **Monitoring is the continuous observation and analysis of a system's health, performance, and behavior using telemetry such as metrics, logs, and traces, so that engineers can detect problems, troubleshoot them, measure reliability, and make informed operational decisions.**

### Your observability sequence is now:

> **Logging → Metrics → Monitoring → SLI → SLO → SLA → Alerting → Distributed Tracing → Correlation ID**

