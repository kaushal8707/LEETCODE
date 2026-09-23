# SLA (Service Level Agreement)

## 1. What is SLA?

**SLA** stands for **Service Level Agreement**.

An SLA is a formal agreement between a service provider and a customer that defines the expected service quality, measurable commitments, and what happens if those commitments are not met.

In simple words:

> **SLA tells the customer: "What level of service are we promising, how will we measure it, and what happens if we fail?"**

### Real-time example: Payment API

Imagine your company provides a payment API to an e-commerce company.

The customer expects the payment service to be reliable.

Your company agrees to:

- Availability: 99.9% per month.
- Critical incident response: Within 30 minutes.
- API latency: 95% of requests under 500 ms, if included in the agreement.
- Service credits if the agreed availability commitment is missed, according to the contract.

These commitments are documented in the SLA.

---

## 2. Why do we need SLA?

Without an SLA, the customer and service provider may have different expectations.

### Without SLA

Customer:

> "I expect the payment service to be available all the time."

Provider:

> "We provide the payment service, but we never promised 99.9% availability."

There is no clear measurable agreement.

### With SLA

Customer:

> "The contract says 99.9% monthly availability."

Provider:

> "We must measure availability and meet the agreed target."

Now both sides understand the commitment.

### Main reasons to use SLA

**1. Define service expectations**

Clearly specify what the provider promises to deliver.

**2. Improve accountability**

Measure the provider against agreed commitments.

**3. Measure service quality**

Define measurable availability, latency, and support targets.

**4. Handle service failures**

Define service credits, escalation, or other remedies when applicable.

---

## 3. SLA vs SLI vs SLO

These three concepts are important for system design and SRE interviews.

| Concept | Meaning | Example |
|---|---|---|
| SLI | Service Level Indicator — what you measure | Actual availability = 99.95% |
| SLO | Service Level Objective — internal target | Target availability = 99.9% |
| SLA | Service Level Agreement — formal customer commitment | Contract promises 99.9% |

### Easy way to remember

```
SLI = What we measure
      ↓
SLO = What we target internally
      ↓
SLA = What we promise the customer
```

Example:

- **SLI:** Payment API actual availability is 99.95%.
- **SLO:** Engineering targets 99.95% availability.
- **SLA:** Customer contract guarantees 99.9% availability.

> **Important:** SLO and SLA do not have to be identical. An organization may set a stricter internal SLO than its contractual SLA.

---

## 4. How SLA works internally

Let's understand it with a payment service.

### Customer

- E-commerce application
- Sends payment requests

### Payment API

- Service provider

### Monitoring and measurement

- Availability
- 99.95%

### Response time

- Measured

### SLA Evaluation

Compare actual results with contractual commitments.

### Monthly SLA Report

Met → Report compliance · Missed → Apply agreed remedies

### Step-by-step

1. **Define the SLA:** Agree on availability, latency, support commitments, exclusions, and remedies.
2. **Deploy the service:** Run the payment API in production.
3. **Collect measurements:** Monitor uptime, errors, latency, and incidents.
4. **Calculate compliance:** Compare actual performance with the agreed SLA.
5. **Report results:** Share the results with the customer.
6. **Take action:** If a commitment was missed, follow the agreed remediation process.

---

## 5. What is SLA availability?

Availability is one of the most common SLA commitments.

```
Availability = (Total Time − Downtime) / Total Time × 100
```

For example, if a service is available for 99.9% of a month, it has 99.9% availability for that measurement period.

### Availability and allowed downtime

Assuming a 30-day month and that all downtime counts against the SLA:

| Availability | Approx. allowed downtime |
|---|---|
| 99% | 7 hours 12 minutes |
| 99.9% | 43 minutes 12 seconds |
| 99.95% | 21 minutes 36 seconds |
| 99.99% | 4 minutes 19 seconds |

These are illustrative downtime budgets, not universal contractual rules. The actual SLA defines its measurement window, exclusions, and calculation method.

### Example calculation

Suppose your payment API has:

- Monthly measurement window: 30 days.
- Total time: 43,200 minutes.
- Downtime: 20 minutes.

```
Availability = (43200 − 20) / 43200 × 100
             = 99.954%
```

If the SLA promises 99.9% availability, the service meets that availability target under these assumptions.

---

## 6. How to use SLA in system design

As a system design engineer, you use SLA requirements to make architecture decisions.

### Scenario: Payment service

Business requirement:

> "Payment service must have 99.99% monthly availability."

This means the architecture needs to account for high availability.

### Example architecture

```
                    Customer
                       |
                       v
                 Load Balancer
                  /          \
                 v            v
           Payment API 1   Payment API 2
                 |            |
                 +-----+------+
                       |
                       v
                Database Cluster
                  /          \
                 v            v
              Primary      Replica
                       |
                       v
                  Monitoring
                       |
                       v
                   SLA Report
```

### Design decisions driven by SLA

| SLA requirement | Possible architecture decision |
|---|---|
| High availability | Multiple application instances |
| Avoid single point of failure | Redundant load balancers and dependencies |
| Database availability | Database replication and failover |
| Fast response | Caching, indexing, efficient queries |
| Disaster recovery | Backups and disaster recovery plan |
| Support response | On-call and incident escalation |

> **Key point:** SLA is not just a document. It influences the reliability, scalability, monitoring, and operational design of a system.

---

## 7. SLA in microservices

In a microservices architecture, each service may have its own SLA.

Example:

| Service | Illustrative SLA |
|---|---|
| API Gateway | 99.99% availability |
| Order Service | 99.95% availability |
| Payment Service | 99.99% availability |
| Notification Service | 99.9% availability |

These are example values, not recommended contractual targets.

### Why different SLAs?

Some services are more critical to the business.

For example:

- **Payment Service:** An outage can prevent transactions.
- **Notification Service:** A notification delay may not prevent an order from being placed.

The business may therefore define different commitments and operational requirements.

### Important interview point

> **A system's end-to-end availability is not automatically equal to the availability of its most reliable service.**

If a synchronous request depends on multiple services, a failure in any dependency may affect the overall availability unless the system has fallbacks, timeouts, or graceful degradation.

---

## 8. SLA and error budget

This is important for SRE and system design interviews.

Suppose:

- **SLA:** 99.9% availability.
- **SLO:** 99.95% availability.

The internal SLO is stricter than the customer commitment.

### Error budget

Error budget is the amount of unreliability permitted by an SLO.

```
Error Budget = 100% − SLO
```

For an SLO of 99.9%:

```
100% − 99.9% = 0.1%
```

This corresponds to about **43 minutes 12 seconds** in a 30-day month, assuming the same availability measurement model.

### How teams use it

If the service is consuming too much error budget:

- Prioritize reliability work.
- Investigate incidents.
- Reduce risky deployments.
- Improve monitoring and recovery.

If the service is meeting its reliability goals, the team may have more room for planned changes, subject to its engineering policies.

---

## 9. How to create an SLA

When designing or managing a service, follow these steps.

### Identify the customer and service

Example: E-commerce company consuming your Payment API.

### Define measurable commitments

Example: 99.9% monthly availability and critical support response within 30 minutes.

### Define how measurement works

Specify the monitoring source, time window, exclusions, and calculation method.

### Define responsibilities

Clarify what the provider and customer are each responsible for.

### Define breach handling

Specify reporting, escalation, service credits, or other remedies.

### Monitor and review

Track the measurements and review the SLA periodically.

---

## 10. SLA interview questions

### Q1. What is SLA?

> SLA is a formal agreement between a service provider and customer that defines measurable service commitments and remedies.

### Q2. Difference between SLA and SLO?

- **SLA:** External/customer-facing commitment.
- **SLO:** Internal target.

### Q3. What is SLI?

> SLI is the actual measurement of service performance, such as availability or latency.

### Q4. What happens if SLA is breached?

> The provider follows the contract. Possible consequences include service credits, escalation, or other agreed remedies.

### Q5. How does SLA affect system architecture?

> It drives decisions about availability, redundancy, failover, monitoring, capacity, disaster recovery, and performance.

### Q6. Is 99.99% availability always better than 99.9%?

> They represent different availability commitments. A higher availability target generally requires more reliability investment, but whether it is appropriate depends on business requirements and cost.

### Q7. Can a service have SLO 99.99% and SLA 99.9%?

> Yes. An internal SLO can be stricter than the external SLA.

---

## Final summary

> **SLA = Service Level Agreement.**

It defines:

- What service quality the provider promises.
- How the quality is measured.
- What the customer can expect.
- What happens if the provider misses the commitment.

### For system design, remember:

> **SLI measures the service, SLO defines the internal target, and SLA defines the contractual commitment to the customer.**

