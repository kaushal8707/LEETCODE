# SLO (Service Level Objective)

## 1. What is SLO?

**SLO** stands for **Service Level Objective**.

An SLO is a measurable target that defines the expected reliability or performance of a service over a specific time period.

In simple words:

> **SLO tells the engineering team: "How reliable or fast should our service be?"**

### Real-time example: Payment API

Imagine your company has a payment service.

The engineering team defines:

> "Our Payment API should be available at least 99.9% of the time every month."

This is an SLO.

It is an internal engineering target used to operate and improve the service.

---

## 2. Why do we need SLO?

Without an SLO, the engineering team may not know what level of reliability is expected.

### Without SLO

Developer:

> "The API is working."

Business:

> "But sometimes the API is slow or unavailable."

There is no measurable target.

### With SLO

Engineering team:

> "Our availability target is 99.9% per month."

Now the team can:

- Measure actual availability.
- Detect reliability problems.
- Set monitoring and alerting thresholds.
- Decide whether to prioritize reliability or new features.
- Track whether the service is meeting its target.

### Main reasons to use SLO

**1. Define a reliability target**

Establish how reliable your service should be.

**2. Measure service performance**

Compare actual results against the target.

**3. Configure meaningful alerts**

Alert when the service is at risk of missing its target.

**4. Guide engineering decisions**

Use reliability goals to prioritize improvements and manage release risk.

---

## 3. SLI vs SLO vs SLA

These three concepts are very important for system design interviews.

| Concept | Full form | Meaning | Example |
|---|---|---|---|
| SLI | Service Level Indicator | What we measure | Actual availability = 99.95% |
| SLO | Service Level Objective | What we target | Availability target = 99.9% |
| SLA | Service Level Agreement | What we promise the customer | Contract guarantees 99.9% |

### Easy way to remember

```
SLI = Measurement
      ↓
SLO = Internal Target
      ↓
SLA = External Agreement
```

### Example

Your Payment API:

- **SLI:** Actual availability is 99.95%.
- **SLO:** Engineering target is 99.9%.
- **SLA:** Customer contract promises 99.9%.

> **Important:** SLO and SLA can be different. An internal SLO may be stricter than the contractual SLA.

---

## 4. How SLO works internally

Let's understand how an SLO is used in a real production system.

### Define target

Payment API

- Availability SLO = 99.9% per month

### Production

Payment Service

- Receives customer requests

### Collect SLI

- Successful requests
- Measured

- Failed requests
- Measured

### Evaluate SLO

- Compare actual vs target
- Is availability meeting 99.9%?

### 5. Engineering action

Alert, investigate, improve reliability, and manage releases.

### Step-by-step

1. Define an SLO for the service.
2. Collect the required measurements (SLIs).
3. Calculate the actual service performance.
4. Compare actual performance with the SLO.
5. Track error budget and configure alerts.
6. Improve the system if reliability is at risk.

---

## 5. Types of SLO

### A. Availability SLO

Defines how often the service should be available.

Example:

> "Payment API availability should be at least 99.9% per month."

### B. Latency SLO

Defines how quickly requests should complete.

Example:

> "At least 95% of API requests should complete within 500 ms."

This is a percentile latency SLO.

### C. Error rate SLO

Defines the acceptable proportion of failed requests.

Example:

> "Less than 0.1% of valid API requests should return server errors."

### D. Data freshness SLO

Defines how up-to-date data should be.

Example:

> "95% of order data should be reflected in the reporting system within 5 minutes."

### E. Queue processing SLO

Defines how quickly messages should be processed.

Example:

> "99% of Kafka messages should be processed within 30 seconds of arrival."

These are illustrative SLOs. Actual targets should be chosen based on business needs and measured system behavior.

---

## 6. How to calculate SLO

Let's use an availability SLO.

### Requirement

> Payment API availability SLO = 99.9% per month.

Assume a 30-day month.

Total minutes:

```
30 × 24 × 60 = 43200
```

Allowed unreliability:

```
100% − 99.9% = 0.1%
```

Error budget in minutes:

```
43200 × 0.001 = 43.2
```

### Result

- SLO = 99.9%
- Allowed downtime = 43 minutes 12 seconds under this measurement model.

If the service is unavailable for 20 minutes:

```
Availability = (43200 − 20) / 43200 × 100
             = 99.954%
```

The service meets the 99.9% availability SLO, assuming the downtime is counted according to the defined SLO rules.

---

## 7. What is an error budget?

An **error budget** is the amount of unreliability allowed by an SLO during its measurement period.

### Formula

```
Error Budget = 100% − SLO
```

For an SLO of 99.9%:

```
100% − 99.9% = 0.1%
```

For a 30-day month, this corresponds to **43 minutes 12 seconds** of downtime under the same availability model.

### Why do we need error budget?

Imagine the engineering team wants to deploy a major change.

The team has two goals:

- Deliver new features.
- Maintain service reliability.

Error budget helps balance those goals.

### Example

> SLO = 99.9%

| Situation | Possible engineering action |
|---|---|
| Error budget is mostly available | Proceed with normal release process |
| Error budget is being consumed quickly | Investigate reliability issues |
| Error budget is exhausted | Consider prioritizing reliability work and reducing risky changes |

Error-budget policies are defined by the organization. An exhausted budget does not automatically mean every deployment must stop.

---

## 8. How to use SLO in system design

This is where SLO becomes important for a senior software engineer.

### Scenario: Payment Service

Business requirement:

> "Payment API should be available 99.99% of the time."

Engineering defines:

> "Payment API availability SLO = 99.99% per month."

Now the architecture must support the target.

### Architecture decisions

| SLO requirement | Possible design decision |
|---|---|
| High availability | Multiple application instances |
| Avoid single point of failure | Redundant load balancers and dependencies |
| Database reliability | Replication and failover |
| Fast API response | Caching, indexing, efficient queries |
| Handle traffic spikes | Horizontal scaling and rate limiting |
| Recover from failures | Backups and disaster recovery |
| Detect problems | Monitoring, metrics, and alerting |

> **Important:** A 99.99% SLO does not automatically require one specific architecture. The design depends on the workload, dependencies, cost, and failure model.

---

## 9. SLO in microservices

Suppose you have three services:

```
                 API Gateway
                      |
          +-----------+-----------+
          |           |           |
          v           v           v
      Order API   Payment API   Notification API
```

Each service can have its own SLO.

| Service | Example availability SLO |
|---|---|
| Order Service | 99.9% |
| Payment Service | 99.99% |
| Notification Service | 99.5% |

These are illustrative values.

### Why different SLOs?

Because services have different business requirements.

For example:

- **Payment Service** may need stricter availability because payment failures directly affect transactions.
- **Notification Service** may tolerate some delay if notifications can be retried asynchronously.

### Important interview question

> **Does the overall system SLO equal the lowest individual service SLO?**

Not necessarily.

If services are called synchronously, their dependencies can affect the end-to-end availability. If some work is asynchronous or has fallbacks, the overall user-facing SLO may be different.

For example:

```
Order API
    |
    v
Payment API
    |
    v
Payment Database
```

A failure in the payment database may affect the payment API and, in turn, the order flow.

Good system design considers dependency availability, timeouts, retries, circuit breakers, and graceful degradation.

---

## 10. How to define a good SLO

A good SLO should be:

### 1. Measurable

**Bad:**

> "The API should be fast."

**Good:**

> "95% of requests should complete within 500 ms."

### 2. Time-bound

**Bad:**

> "The service should be available."

**Good:**

> "The service should have 99.9% availability over a rolling 30-day period."

### 3. Business-relevant

The SLO should reflect what matters to users and the business.

### 4. Realistic

The engineering team should be able to work toward the target with an appropriate level of investment.

### 5. Clearly defined

Specify:

- What counts as a successful request.
- Which requests are included.
- Measurement period.
- Exclusions.
- How the SLO is calculated.

---

## 11. SLO interview questions

### Q1. What is SLO?

> SLO is a measurable internal target for the reliability or performance of a service.

### Q2. Difference between SLI and SLO?

- **SLI:** Actual measurement.
- **SLO:** Target for that measurement.

### Q3. Difference between SLO and SLA?

- **SLO:** Internal engineering objective.
- **SLA:** External contractual commitment.

### Q4. What is error budget?

> The amount of unreliability allowed by an SLO during its measurement period.

### Q5. If SLO is 99.9%, what is the error budget?

> 0.1% of the measurement window.
>
> For a 30-day month, that is approximately 43 minutes 12 seconds under a simple availability model.

### Q6. How does SLO influence system design?

> It influences redundancy, failover, scaling, monitoring, disaster recovery, and reliability engineering.

### Q7. Can SLO be stricter than SLA?

> Yes. For example, internal SLO = 99.95%, external SLA = 99.9%.

### Q8. What happens when the error budget is exhausted?

> The team may prioritize reliability work and reduce risky changes according to its error-budget policy.

---

## Final summary

> **SLO = Service Level Objective.**
>
> **It defines the target reliability or performance of a service.**

| Term | Meaning |
|---|---|
| SLI | What we measure |
| SLO | What we target |
| SLA | What we promise |
| Error Budget | How much unreliability we allow |

### Remember this for interviews

> **SLI measures the service, SLO defines the internal target, and SLA defines the contractual commitment. Error budget tells us how much unreliability is permitted by the SLO.**

