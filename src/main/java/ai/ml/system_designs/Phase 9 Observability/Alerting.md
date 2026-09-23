# Alerting — What is it, Why and How to Use It

## 1. What is Alerting?

**Alerting** is the process of automatically detecting important problems in a system and notifying the responsible team so they can take action.

In simple words:

> **Alerting tells the engineering team: "Something important is wrong with the system. You need to check it."**

### Real-time example: Payment API

Imagine your Payment API is running in production.

Normally:

- Availability: 99.95%
- Error rate: 0.05%
- p95 latency: 300 ms

Suddenly, the database becomes slow.

Now:

- Error rate increases to 10%.
- p95 latency increases to 5 seconds.
- Payment requests start failing.

The monitoring system detects the problem.

The alerting system sends a notification to the on-call engineer.

```
Payment API
     |
     v
Metrics Collection
     |
     v
Monitoring System
     |
     v
Alert Rule Evaluation
     |
     v
Alert Triggered
     |
     v
PagerDuty / Slack / Email
     |
     v
On-call Engineer
```

The engineer investigates and fixes the issue.

---

## 2. Why do we need Alerting?

Without alerting, engineers may discover problems only when customers complain.

### Without Alerting

Customer:

> "My payment is failing."

Support Team:

> "Let me check with engineering."

Engineering:

> "We didn't know there was a problem."

### With Alerting

```
Database Failure
       |
       v
Error Rate Increases
       |
       v
Alert Triggered
       |
       v
Engineer Notified
       |
       v
Issue Investigated
       |
       v
Service Recovered
```

### Main reasons to use alerting

**1. Detect problems quickly**

Find production failures without waiting for customer complaints.

**2. Reduce downtime**

Notify the right engineer so the issue can be investigated quickly.

**3. Protect SLOs**

Detect when reliability is at risk of missing the target.

**4. Reduce manual monitoring**

Engineers don't need to continuously watch dashboards.

---

## 3. Alerting vs Monitoring vs Metrics

These concepts are connected but different.

| Concept | Meaning | Example |
|---|---|---|
| Metrics | Numeric measurements | Error rate = 5% |
| Monitoring | Collecting and analyzing system health | Dashboard shows error rate |
| Alerting | Notifying when important conditions occur | Alert when error rate exceeds threshold |

### Easy way to remember

```
Metrics = Data
    ↓
Monitoring = Observe and analyze
    ↓
Alerting = Notify when action is needed
```

### Example

Payment API:

- **Metrics:** 10,000 requests, 500 errors.
- **Monitoring:** Error rate is 5%.
- **Alerting:** Error rate is above 2% for 5 minutes → notify on-call engineer.

---

## 4. How Alerting works internally

Let's understand the complete flow in a production system.

### Application

Payment API

- Produces metrics, logs, and traces

### 2. Metrics Collection

- Prometheus / OpenTelemetry

### 3. Monitoring / Alert Evaluation

- Evaluate alert rules against current measurements

### Alert Condition

- Error rate > 5% for 5 minutes
- Condition met

### Notification

- Alert Manager
- Routes and sends the alert

### 6. On-call Engineer

- Investigates, mitigates, and resolves the issue

### Step-by-step

1. The application generates metrics.
2. A monitoring system collects the metrics.
3. Alert rules evaluate the metrics.
4. If a condition is met, an alert becomes active.
5. The notification system sends the alert to the right team.
6. The engineer investigates and takes action.
7. The alert resolves when the condition clears, or is handled according to the alerting system's rules.

---

## 5. Types of Alerting

### A. Availability Alert

Used when the service is unavailable or its availability is at risk.

Example:

> Payment API availability is below 99.9%.

Possible alert:

```
ALERT: Payment API Availability
Condition: Availability < 99.9%
Duration: 5 minutes
Severity: Critical
Action: Notify on-call engineer
```

### B. Error Rate Alert

Used when too many requests fail.

Example:

```
ALERT: High Error Rate
Condition: Error rate > 5%
Duration: 5 minutes
Severity: Critical
```

Possible causes:

- Database failure.
- Application bug.
- Downstream service failure.
- Invalid deployment.
- Traffic spike.

### C. Latency Alert

Used when the API becomes slow.

Example:

```
ALERT: High API Latency
Condition: p95 latency > 1 second
Duration: 10 minutes
Severity: Warning
```

Possible causes:

- Slow database queries.
- CPU saturation.
- Network latency.
- Lock contention.
- Downstream dependency slowness.

### D. CPU / Memory Alert

Used to detect resource exhaustion.

Example:

```
ALERT: High CPU Usage
Condition: CPU > 85%
Duration: 10 minutes
Severity: Warning
```

Memory example:

```
ALERT: High Memory Usage
Condition: Memory > 90%
Duration: 5 minutes
Severity: Critical
```

> **Important:** High CPU alone does not always mean a user-facing problem. Resource alerts are useful when they indicate a real risk or require operational action.

### E. Kafka Consumer Lag Alert

Since you are learning Kafka, this is a real production example.

Suppose your Kafka consumer is processing order events.

```
Order Service
     |
     v
    Kafka
     |
     v
Order Consumer
     |
     v
Order Database
```

Alert:

```
ALERT: Kafka Consumer Lag
Condition: Consumer lag > 10,000 messages
Duration: 5 minutes
Severity: Warning
```

Possible causes:

- Consumer is slow.
- Consumer is down.
- Database is slow.
- Traffic has increased.
- Consumer rebalance is occurring.

#### Better Kafka alerting

Lag alone may not indicate a customer-impacting issue. Combine it with processing delay, message age, or a user-facing freshness objective when appropriate.

---

## 6. How to use Alerting in Spring Boot

Let's take a Spring Boot Payment API.

### Architecture

```
Spring Boot Application
          |
          v
       Micrometer
          |
          v
      Prometheus
          |
          v
  Prometheus Alert Rules
          |
          v
    Alertmanager
          |
          v
   Slack / PagerDuty
```

### Step 1: Collect application metrics

Example metrics:

```
http_server_requests_seconds_count
http_server_requests_seconds_sum
payment_requests_total
payment_failures_total
```

These are example metric names; actual names depend on your instrumentation and metric conventions.

### Step 2: Define alert rule

Example Prometheus alert rule:

```yaml
groups:
  - name: payment-api-alerts
    rules:
      - alert: HighPaymentErrorRate
        expr: |
          (
            sum(rate(payment_failures_total[5m]))
            /
            sum(rate(payment_requests_total[5m]))
          ) > 0.05
        for: 5m
        labels:
          severity: critical
        annotations:
          summary: "High payment error rate"
          description: "Payment error rate is above 5% for 5 minutes."
```

### What does this mean?

```
rate(payment_failures_total[5m])
```

Measures the average failure rate per second over the last 5 minutes.

```
rate(payment_requests_total[5m])
```

Measures the average request rate per second over the last 5 minutes.

The ratio gives the approximate error proportion.

If it is greater than `0.05` (5%) for 5 minutes, the alert fires.

> **Production note:** Make sure the denominator is nonzero, and filter metrics to the intended service, route, and request class. For a real deployment, define which errors count as failures.

---

## 7. Alerting with SLI and SLO

This is a very important system design concept.

Suppose:

- **SLI** = Actual availability.
- **SLO** = 99.9% availability.

### Alerting process

```
Actual Availability
        |
        v
      SLI
        |
        v
Compare with SLO
        |
        v
SLO at risk?
        |
        v
      Alert
```

### Example

| Metric | Value |
|---|---|
| Actual availability (SLI) | 99.95% |
| SLO | 99.9% |
| Status | Meeting target |

If availability falls below the target, the team may need to investigate.

However, an alert does not have to wait until the SLO is already breached. Teams can alert when the SLO is at risk, using burn-rate alerts or other predictive conditions.

---

## 8. What is a Burn Rate Alert?

**Burn rate** tells us how quickly a service is consuming its error budget.

Example:

- SLO = 99.9%.
- Error budget = 0.1%.
- Error rate = 1%.

The service is consuming error budget faster than its allowed average.

### Simple explanation

```
Error Budget
     |
     v
How quickly is it being consumed?
     |
     v
Burn Rate
     |
     v
Alert if consumption is too fast
```

### Why burn-rate alerting?

If you wait until the SLO is completely breached, it may be too late to protect the target.

Burn-rate alerting can detect rapid reliability degradation earlier.

For example:

- **Short window:** Detect a serious ongoing outage.
- **Long window:** Detect sustained reliability degradation.

This is commonly used in SRE practices.

---

## 9. Alerting best practices

### 1. Alert on actionable problems

**Bad:**

> CPU is 80%.

**Better:**

> API latency is above the target and CPU is saturated.

The second alert gives a clearer reason to investigate.

### 2. Avoid alert fatigue

If engineers receive hundreds of alerts every day, they may ignore important ones.

Use:

- Meaningful thresholds.
- Grouping.
- Deduplication.
- Severity levels.
- Maintenance silences.

### 3. Define severity

| Severity | Meaning | Example |
|---|---|---|
| Critical | Immediate action required | Payment API unavailable |
| Warning | Potential issue | CPU sustained at 85% |
| Info | Informational | Deployment completed |

These are example severity definitions. Your organization can define its own policy.

### 4. Add useful context

An alert should tell the engineer:

- What failed?
- Which service?
- Which environment?
- When did it start?
- What is the current value?
- What is the threshold?
- What runbook should be followed?

### 5. Use a `for` duration

Avoid triggering alerts for a brief, harmless spike.

Example:

```
for: 5m
```

This means the condition must remain true for 5 minutes before the alert fires.

### 6. Alert on user impact

Prioritize alerts that indicate real customer impact or a serious risk to service reliability.

---

## 10. Monitoring vs Alerting vs Logging vs Tracing

Since you are learning observability, this comparison is useful.

| Component | Purpose |
|---|---|
| Logging | Record detailed events |
| Metrics | Numeric measurements |
| Monitoring | Observe and analyze system health |
| Alerting | Notify when action is needed |
| Tracing | Follow a request across services |

### Example: Payment failure

```
Payment Request
      |
      v
    Metrics
      |
      v
Error Rate Increases
      |
      v
   Alert Fires
      |
      v
Engineer Checks Logs
      |
      v
Engineer Checks Traces
      |
      v
Root Cause Found
```

---

## 11. Alerting interview questions

### Q1. What is Alerting?

> Alerting automatically detects important system conditions and notifies responsible engineers.

### Q2. Difference between Monitoring and Alerting?

- **Monitoring:** Observe system health.
- **Alerting:** Notify when a defined condition requires attention.

### Q3. What is an alert rule?

> A condition that determines when an alert should fire.

Example:

```
Error rate > 5% for 5 minutes
```

### Q4. What is alert fatigue?

> When too many alerts are generated, making it difficult for engineers to identify important issues.

### Q5. What is a burn-rate alert?

> An alert based on how quickly a service is consuming its SLO error budget.

### Q6. How do you alert on Kafka consumer lag?

> Monitor consumer lag and, where relevant, message age or processing delay. Alert when the consumer is falling behind enough to threaten the service's freshness or processing objectives.

### Q7. How does Alerting help SLA?

> Alerting helps detect service issues early, enabling faster response and recovery. It supports SLA compliance but does not guarantee it.

### Q8. What is the difference between an alert and an incident?

- **Alert:** A notification that a condition needs attention.
- **Incident:** A service disruption or operational problem requiring response.

Not every alert becomes an incident.

---

## Final summary

> **Alerting = Automatically detect important problems and notify the right team.**

| Concept | Meaning |
|---|---|
| Metrics | What is happening? |
| Monitoring | Observe system health |
| SLI | Actual service performance |
| SLO | Target service performance |
| Alerting | Notify when action is needed |
| Incident | Operational problem requiring response |

### Remember this for interviews

> **Metrics provide the data, monitoring observes the system, alerting notifies engineers about important conditions, and SLOs help define which reliability risks matter.**

For your system design learning path:

> **Logging → Metrics → Monitoring → SLI → SLO → Alerting → Incident Response → Reliability Improvements**

