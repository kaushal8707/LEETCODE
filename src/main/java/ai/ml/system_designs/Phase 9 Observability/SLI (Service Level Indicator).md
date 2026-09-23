# SLI (Service Level Indicator)

## 1. What is SLI?

**SLI** stands for **Service Level Indicator**.

An SLI is a measurable indicator that tells us how well a service is performing from the user's perspective.

In simple words:

> **SLI tells us what is actually happening in our system.**

For example, if you have a Payment API, you can measure:

- How many requests succeed?
- How many requests fail?
- How long does each request take?
- How many messages are processed successfully?
- How fresh is the data?

These measurements are SLIs.

### Real-time example: Payment API

Suppose your Payment API receives 100,000 requests in one hour.

- Successful requests: 99,950
- Failed requests: 50

The availability-style SLI is:

```
SLI = Successful Requests / Total Valid Requests × 100
    = 99950 / 100000 × 100
    = 99.95%
```

**SLI = 99.95%**

This is the actual measured service performance.

---

## 2. Why do we need SLI?

Without SLI, engineering teams may rely on assumptions instead of real measurements.

### Without SLI

Developer:

> "Our API seems to be working fine."

But how do we know?

- Is the API actually available?
- Are requests slow?
- Are users getting errors?
- Is Kafka processing messages on time?

We need measurable data.

### With SLI

Engineering team:

> "Our Payment API availability is 99.95%, and p95 latency is 420 ms."

Now the team can understand actual performance.

### Main reasons to use SLI

**1. Measure actual performance**

Know how your service is behaving in production.

**2. Detect problems**

Identify high error rates, slow APIs, and unavailable services.

**3. Compare with SLO**

Check whether actual performance meets the engineering target.

**4. Improve system reliability**

Use real measurements to guide optimization and reliability work.

---

## 3. SLI vs SLO vs SLA

This is one of the most important concepts for system design interviews.

| Concept | Full form | Meaning | Example |
|---|---|---|---|
| SLI | Service Level Indicator | Actual measurement | Availability = 99.95% |
| SLO | Service Level Objective | Internal target | Availability target = 99.9% |
| SLA | Service Level Agreement | Customer commitment | Contract promises 99.9% |

### Easy way to remember

```
SLI = What is happening?
SLO = What do we want?
SLA = What did we promise?
```

### Example

Payment API:

- **SLI:** Actual availability is 99.95%.
- **SLO:** Engineering target is 99.9%.
- **SLA:** Customer contract guarantees 99.9%.

The SLI is the measurement. The SLO is the target against which it is evaluated.

---

## 4. How SLI works internally

Let's understand with a real production API.

### Architecture

#### User

- E-commerce Application
- Sends payment requests

#### Payment API

- Processes requests

#### Payment Database

- Stores payment information

#### Monitoring and Metrics

- Requests
- 100,000

- Successful
- 99,950

#### SLI Calculation

- Actual availability = 99.95%

### Step-by-step

1. User sends a request.
2. API processes the request.
3. Monitoring records request outcome and latency.
4. Metrics system aggregates the measurements.
5. SLI calculation produces the actual service performance.
6. Engineering compares the SLI with the SLO.

---

## 5. Types of SLI

### A. Availability SLI

Measures the proportion of valid requests that succeed, or the proportion of time a service is available, depending on the defined measurement model.

#### Example

100,000 requests:

- Successful = 99,950
- Failed = 50

```
Availability SLI = 99950 / 100000 × 100
                 = 99.95%
```

### B. Latency SLI

Measures how long a request takes.

Example:

Your API processes 10,000 requests.

- p50 latency = 100 ms
- p95 latency = 300 ms
- p99 latency = 800 ms

#### What does p95 mean?

- 95% of measured requests completed in 300 ms or less.
- The remaining 5% took longer than 300 ms.

For user-facing APIs, latency SLIs are often more useful when expressed as a percentile or as the proportion of requests meeting a latency threshold.

Example latency SLI:

```
Latency SLI = Requests completed within 500 ms / Total valid requests × 100
```

### C. Error Rate SLI

Measures the proportion of requests that fail.

Example:

- Total requests = 100,000
- Failed requests = 100

```
Error Rate = 100 / 100000 × 100
           = 0.1%
```

A lower error rate is generally desirable.

You can also define a success-rate SLI:

```
Success Rate = 100% − Error Rate
```

### D. Throughput SLI

Measures how much work a system processes over time.

Example:

> Kafka messages processed = 50,000 per second.

```
Throughput SLI = 50,000 messages/sec
```

This is useful for Kafka consumers, data processing systems, and APIs.

### E. Freshness SLI

Measures how up-to-date data is.

Example:

> "Order data should be available in the reporting system within 5 minutes."

You measure how long it takes for data to become available.

For example:

- 99% of records are available within 5 minutes.
- 1% take longer.

This can be expressed as a freshness SLI.

---

## 6. How to use SLI in a real project

Let's take a Spring Boot Payment API.

### Requirement

The engineering team wants to measure:

- Availability.
- Latency.
- Error rate.

### Step 1: Instrument the application

Collect metrics from the API.

Example metrics:

```
payment_requests_total
payment_requests_success_total
payment_requests_failure_total
payment_request_duration_seconds
```

These are illustrative metric names.

### Step 2: Export metrics

The application exposes metrics to a monitoring system.

Example:

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
      Grafana
```

### Step 3: Calculate SLI

For availability:

```
Successful Requests / Total Valid Requests
```

For latency:

```
Requests under 500 ms / Total Valid Requests
```

For error rate:

```
Failed Requests / Total Valid Requests
```

### Step 4: Compare with SLO

Example:

| Metric | Actual SLI | SLO |
|---|---|---|
| Availability | 99.95% | 99.9% |
| Latency under 500 ms | 98% | 95% |
| Error rate | 0.05% | Less than 0.1% |

Under these illustrative measurements, all three targets are met.

---

## 7. SLI in Kafka

Since you're learning Kafka from basic to expert level, this is a useful real-world example.

### Scenario

Your Kafka consumer processes order events.

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

### Useful Kafka SLIs

| SLI | What it measures |
|---|---|
| Consumer lag | How far the consumer is behind |
| Processing latency | Time to process a message |
| Error rate | Failed message processing |
| Throughput | Messages processed per second |
| Freshness | How recently messages are processed |

### Example

Consumer receives 100,000 messages.

- Successfully processed = 99,900
- Failed = 100

```
Processing Success SLI = 99900 / 100000 × 100
                       = 99.9%
```

This tells you the actual processing success rate.

> **Important:** Consumer lag is a useful indicator, but lag alone is not necessarily a user-facing SLI. For example, a lag of 1,000 messages may be harmless for one workload and critical for another. Define the measurement based on the service's actual reliability requirement.

---

## 8. SLI and SLO with error budget

Suppose:

- **SLI** = 99.95% availability.
- **SLO** = 99.9% availability.

### What does this mean?

- Actual performance is above the target.
- The service is meeting its availability SLO for the measurement period.

### Error budget

```
Error Budget = 100% − SLO
             = 100% − 99.9%
             = 0.1%
```

The error budget is derived from the SLO, not directly from the SLI.

### Important distinction

```
SLI  = Actual performance
SLO  = Target performance
Error Budget = Allowed unreliability from SLO
```

---

## 9. How to define a good SLI

A good SLI should be:

### 1. Measurable

**Bad:**

> "The API should be fast."

**Good:**

> "Percentage of valid requests completed within 500 ms."

### 2. User-focused

Measure something that matters to users.

For a payment API:

- Successful payment completion.
- Payment response latency.
- Availability.

### 3. Clearly defined

Specify:

- What counts as a valid request.
- What counts as success.
- What counts as failure.
- Measurement period.
- How data is collected.

### 4. Consistent

Use the same calculation rules over time so measurements can be compared.

---

## 10. SLI interview questions

### Q1. What is SLI?

> SLI is a measurable indicator of actual service performance or reliability.

### Q2. Difference between SLI and SLO?

- **SLI:** Actual measurement.
- **SLO:** Target for that measurement.

### Q3. Give real-time examples of SLI.

- Availability.
- Latency.
- Error rate.
- Throughput.
- Data freshness.
- Kafka consumer processing latency.

### Q4. How do you calculate availability SLI?

```
Availability = Successful Requests / Total Valid Requests × 100
```

### Q5. What is p95 latency?

> The latency value at or below which 95% of measured requests completed.

### Q6. How do you use SLI in microservices?

> Collect metrics from each service, calculate actual performance, compare it with SLOs, and use the results for monitoring and reliability improvements.

### Q7. Does every metric qualify as an SLI?

> No. A metric can be useful operationally without being an SLI. An SLI should measure a defined aspect of service reliability or performance relevant to the service's objectives.

---

## Final summary

> **SLI = Service Level Indicator.**
>
> **It measures the actual performance of a service.**

| Term | Meaning |
|---|---|
| SLI | What is actually happening? |
| SLO | What do we want to achieve? |
| SLA | What did we promise the customer? |
| Error Budget | How much unreliability does the SLO allow? |

### Remember this for interviews

> **SLI measures the service, SLO defines the target, and SLA defines the contractual commitment.**

For your system design learning path:

> **Metrics → SLI → SLO → Error Budget → Alerting → Reliability Improvements**

