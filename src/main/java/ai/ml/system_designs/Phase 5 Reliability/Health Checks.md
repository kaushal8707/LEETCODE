# Health Checks

A **Health Check** is a mechanism used to determine whether a service or application instance is healthy enough to receive traffic or perform its responsibilities.

In distributed systems, health checks are essential for:

- Load balancers
- Kubernetes
- Service discovery
- Auto-scaling
- Failover
- Container orchestration
- Deployment systems
- Monitoring
- Traffic routing

The simplest definition is:

> **Health Check** = "Is this service currently capable of doing its job?"

---

## 1. Why do we need Health Checks?

Imagine you have three instances:

```
                 Load Balancer
                /      |      \
               ▼       ▼       ▼
             App-1   App-2   App-3
               │       │       │
            HEALTHY  DOWN    HEALTHY
```

Without health checks, the load balancer may continue sending requests to App-2:

```
Client
  │
  ▼
Load Balancer
  │
  ├── App-1 → SUCCESS
  ├── App-2 → FAILURE
  └── App-3 → SUCCESS
```

With health checks:

```
Load Balancer
     │
     ├── App-1 → HEALTHY → send traffic
     ├── App-2 → UNHEALTHY → remove
     └── App-3 → HEALTHY → send traffic
```

Now traffic flows only to healthy instances.

---

## 2. Basic Health Check Architecture

```
                    Load Balancer
                         │
             ┌───────────┼───────────┐
             │           │           │
             ▼           ▼           ▼
           App-1       App-2       App-3
             │           │           │
             ▼           ▼           ▼
          /health     /health     /health
```

The load balancer periodically asks:

```
GET /health
```

The application responds:

```
200 OK
```

or:

```
503 Service Unavailable
```

The load balancer uses this information to decide whether the instance should receive traffic.

---

## 3. Liveness vs Readiness

This is one of the most important health-check concepts, especially in Kubernetes and production systems.

There are generally two different questions:

### Liveness

> "Is the application alive?"

### Readiness

> "Is the application ready to receive traffic?"

These are **not** the same thing.

---

## 4. Liveness Check

Liveness answers:

> Is the process/application fundamentally alive?

Example:

```
GET /health/live
```

Response:

```
200 OK
```

means:

```
Application is alive
```

If liveness fails repeatedly, an orchestrator such as Kubernetes may **restart** the application.

---

## 5. Readiness Check

Readiness answers:

> Is this instance ready to serve requests?

Example:

```
GET /health/ready
```

Suppose the application starts:

```
Application starts
       ↓
JVM starts
       ↓
Spring Boot starts
       ↓
Application connects to dependencies
       ↓
Cache initialized
       ↓
Kafka consumer initialized
       ↓
READY
```

During startup:

```
Liveness  → SUCCESS
Readiness → FAILURE
```

This is perfectly valid.

The application is alive but not yet ready to receive production traffic.

---

## 6. Why separate Liveness and Readiness?

Consider:

```
Application
    │
    ├── Process alive
    │
    └── Database connection unavailable
```

The process itself is alive.

Therefore:

```
Liveness → PASS
```

But if your API cannot function without the database:

```
Readiness → FAIL
```

The load balancer should stop sending traffic to that instance.

```
                 Load Balancer
                       │
             ┌─────────┼─────────┐
             ▼         ▼         ▼
           App-1     App-2     App-3
          READY      NOT       READY
                     READY
             │         X         │
             ▼                   ▼
          traffic               traffic
```

---

## 7. Startup Check

Some systems also distinguish a **startup check**.

The question is:

> "Has the application finished starting?"

This is particularly useful for applications that take a long time to initialize.

Example:

```
Application
   │
   ▼
Startup Probe
   │
   ▼
Application initialized?
```

If startup takes 60 seconds, you don't want the system to immediately assume the application is dead simply because it hasn't become ready yet.

---

## 8. Three important checks

Think of them as:

```
                 Application
                     │
        ┌────────────┼────────────┐
        ▼            ▼            ▼
     Startup       Liveness     Readiness
        │            │            │
   "Started?"    "Alive?"     "Ready?"
```

- **Startup** → Has initialization completed?
- **Liveness** → Is the process functioning?
- **Readiness** → Should I send traffic here?

---

## 9. Health Check types

Health checks can be implemented in different ways.

### A. HTTP Health Check

```
GET /health
```

Response:

```json
{
  "status": "UP"
}
```

Very common for HTTP services.

### B. TCP Health Check

Instead of calling an HTTP endpoint, the infrastructure checks whether a TCP connection can be established.

```
Load Balancer
      │
      │ TCP connection
      ▼
Application:8080
```

Useful when:

- HTTP isn't available
- You only need basic connectivity validation

But TCP success doesn't mean the application is actually capable of serving requests.

### C. Command/Process Check

The system executes a command:

```
health-check.sh
```

or checks whether a process is running.

This can be useful in some environments, but it may provide less meaningful information than an application-level health check.

---

## 10. Shallow vs Deep Health Checks

This is a very important production distinction.

### Shallow health check

Checks only whether the application process is running.

```
GET /health/live

Application process → OK
```

It might return:

```json
{
  "status": "UP"
}
```

### Deep health check

Checks important dependencies.

For example:

```
Application
   │
   ├── Database
   ├── Redis
   ├── Kafka
   └── External API
```

A deep check might verify:

```
Application → OK
Database    → OK
Redis       → OK
Kafka       → OK
```

But you should be careful about putting every dependency into readiness.

---

## 11. Why deep health checks can be dangerous

Suppose your health endpoint checks:

- Database
- Redis
- Kafka
- Payment Service
- Inventory Service
- Shipping Service

And the load balancer calls:

```
GET /health
```

every few seconds on every instance.

Now every health check generates downstream traffic.

With:

```
100 application instances
```

you could create a large number of dependency checks.

Worse, suppose the Payment Service is down.

Every application instance reports:

```
UNHEALTHY
```

The load balancer removes all instances.

You may accidentally turn:

```
Payment Service failure
```

into:

```
Entire application unavailable
```

This is why **liveness should generally be shallow**, and **readiness should include only dependencies that are genuinely required to serve traffic**.

---

## 12. Health Check and Load Balancer

This is a classic architecture:

```
                        Clients
                           │
                           ▼
                    Load Balancer
                           │
                    Health Checks
                           │
             ┌─────────────┼─────────────┐
             ▼             ▼             ▼
           App-1         App-2         App-3
           READY         DOWN          READY
             │                           │
             └───────────┬───────────────┘
                         │
                      Traffic
```

The load balancer periodically evaluates instance health.

If:

```
App-2 → unhealthy
```

it stops routing traffic there.

---

## 13. Health Check and Kubernetes

This becomes particularly important with Kubernetes.

Conceptually:

```
                    Kubernetes
                        │
              ┌─────────┼─────────┐
              ▼         ▼         ▼
            Pod-1     Pod-2     Pod-3
             │          │         │
             ▼          ▼         ▼
          Probes      Probes    Probes
```

Kubernetes commonly uses:

- Startup Probe
- Liveness Probe
- Readiness Probe

---

## 14. Kubernetes Liveness

Suppose your application gets stuck:

```
Application
   ↓
Deadlock / unrecoverable state
```

The process may still exist:

```
Process = RUNNING
```

but it cannot actually function.

A liveness probe can detect the problem.

If it repeatedly fails:

```
Liveness failure
       ↓
Kubernetes restarts container
```

---

## 15. Kubernetes Readiness

Suppose:

```
Pod = running
```

but:

```
Database connection unavailable
```

The application may not be able to serve requests.

Readiness fails:

```
Readiness = FAIL
       ↓
Pod removed from service endpoints
       ↓
No new traffic
```

Importantly:

> Readiness failure does **not** necessarily mean the container should be restarted.

This distinction is critical.

---

## 16. Startup Probe

Suppose an application requires:

```
90 seconds
```

to initialize.

Without a startup probe, aggressive liveness settings might do:

```
Application starting
       ↓
Liveness fails
       ↓
Restart
       ↓
Application starts again
       ↓
Liveness fails
       ↓
Restart
```

This creates a restart loop.

Startup probing provides a separate initialization phase:

```
STARTING
   │
   ▼
Startup Probe
   │
   ▼
Started
   │
   ├── Liveness
   └── Readiness
```

---

## 17. Health Check States

A health check isn't necessarily just:

```
UP / DOWN
```

You can think in terms of:

```
UNKNOWN
   ↓
STARTING
   ↓
READY
   ↓
DEGRADED
   ↓
UNHEALTHY
```

For example:

```
Database = healthy
Redis    = healthy
Kafka    = slow
```

The service might be:

```
DEGRADED
```

rather than completely dead.

This can be useful for monitoring even if your load balancer only needs a binary ready/not-ready decision.

---

## 18. Failure thresholds

Never immediately remove an instance because of one failed check.

For example:

```
Health check interval = 5 seconds
Failure threshold     = 3
```

If:

```
10:00:00 → FAIL
10:00:05 → FAIL
10:00:10 → FAIL
```

then mark unhealthy.

This avoids reacting to temporary network problems.

Similarly, you may configure a **success threshold** before declaring an instance healthy again.

---

## 19. Health Check Flapping

Suppose an application alternates:

```
HEALTHY
UNHEALTHY
HEALTHY
UNHEALTHY
HEALTHY
```

This is called **flapping**.

It can cause:

```
Traffic added
     ↓
Traffic removed
     ↓
Traffic added
     ↓
Traffic removed
```

This can destabilize the system.

Solutions include:

- Failure thresholds
- Success thresholds
- Grace periods
- Hysteresis
- Appropriate timeouts
- Sensible probe intervals

---

## 20. Health Check timeout

A health check should itself have a timeout.

For example:

```
Health check timeout = 2 seconds
```

If:

```
GET /health
```

takes longer than 2 seconds:

```
Health check → FAIL
```

Otherwise, a health checker can itself hang waiting for a broken service.

---

## 21. Health Checks and graceful shutdown

This is an advanced but important concept.

Suppose you're deploying a new version:

```
App-1 → old version
App-2 → old version
App-3 → old version
```

You want to shut down App-2.

You shouldn't immediately kill it while requests are still running.

Instead:

```
App-2
  ↓
Readiness = FAIL
  ↓
Stop receiving new traffic
  ↓
Existing requests finish
  ↓
Graceful shutdown
  ↓
Process terminates
```

This is often called **connection draining** or **graceful termination**.

---

## 22. Health Checks during deployment

Consider a rolling deployment:

```
Old:
App-1
App-2
App-3
```

Deploy:

```
New App-4
```

The system waits:

```
App-4
   ↓
Startup
   ↓
Readiness PASS
   ↓
Receive traffic
```

Only then might it remove an old instance.

This prevents sending production traffic to an application that has started but isn't actually ready.

---

## 23. Health Check vs Monitoring

These are related but different.

### Health Check

Usually answers:

> Can this instance currently serve traffic?

```
READY / NOT READY
```

### Monitoring

Answers:

> What is happening with the system?

For example:

```
CPU = 80%
Memory = 70%
p99 latency = 450ms
Error rate = 2%
Kafka lag = 50,000
DB connections = 90%
```

- **Health checks** are primarily for automated operational decisions.
- **Monitoring** is for observability and diagnosis.

---

## 24. Health Check vs Metrics

For example:

```
/health
```

might say:

```json
{
  "status": "UP"
}
```

But metrics tell you:

```
request_rate = 10,000/sec
error_rate = 3%
p95_latency = 250ms
p99_latency = 1.2s
```

An application can be:

```
Health = UP
```

while still having terrible performance.

Therefore:

> Healthy does not necessarily mean performant.

---

## 25. Health Check and Circuit Breaker

Since we just covered Circuit Breaker, understand the difference.

### Health Check

Asks:

> "Is this service/instance healthy?"

```
Load Balancer
     │
     ▼
App
     │
     └── Health = READY
```

### Circuit Breaker

Asks:

> "Should I continue calling this dependency?"

```
Order Service
      │
      ▼
Circuit Breaker
      │
      ▼
Payment Service
```

Payment keeps failing:

```
Circuit → OPEN
```

So:

- **Health Check** → Determines instance/service health
- **Circuit Breaker** → Protects caller from failing dependency

---

## 26. Health Check + Circuit Breaker

They can work together:

```
                    Load Balancer
                         │
                    Readiness
                         │
             ┌───────────┼───────────┐
             ▼           ▼           ▼
           App-1       App-2       App-3
             │
             ▼
       Circuit Breaker
             │
             ▼
       Payment Service
```

There are now two different failure decisions:

```
App-2 unhealthy?
    ↓
Load Balancer removes App-2
```

versus:

```
Payment unhealthy?
    ↓
Circuit Breaker protects App-2 from Payment
```

---

## 27. A subtle production problem

Imagine:

```
Order Service
      │
      ├── Database
      ├── Redis
      └── Payment
```

Payment is optional for some operations.

If readiness says:

```
Payment unavailable → NOT READY
```

then the entire Order Service instance may be removed from traffic.

But perhaps:

```
GET /orders
```

works perfectly without Payment.

A better architecture might allow the instance to remain ready while the Payment integration is degraded, using:

```
Circuit Breaker
+
Fallback
+
Graceful degradation
```

This is an important system-design principle:

> Don't make the health definition stricter than the business capability actually requires.

---

## 28. Spring Boot example

With Spring Boot Actuator, a common approach is to expose health endpoints such as:

```
/actuator/health
```

and use separate readiness/liveness health groups in environments that support them.

Conceptually:

```
/actuator/health/liveness
/actuator/health/readiness
```

For example:

```
Liveness:
Application process is functioning

Readiness:
Application is ready to receive production traffic
```

The exact configuration depends on the Spring Boot version and deployment environment.

---

## 29. Designing a good health endpoint

A good health-check endpoint should generally be:

### Fast

```
Response time → milliseconds
```

### Lightweight

Don't perform expensive business operations.

### Deterministic

Avoid random failures.

### Dependency-aware where appropriate

Readiness can check critical dependencies.

### Safe

Don't expose:

- Database passwords
- Connection strings
- Internal credentials
- Sensitive configuration

A health endpoint should not become an information-leak endpoint.

---

## 30. Common mistake

**Bad:**

```
GET /health

1. Call Payment
2. Call Inventory
3. Call Database
4. Run expensive query
5. Call external API
6. Calculate something
7. Return status
```

Every health check now creates substantial load.

**Better:**

```
Liveness:
→ Is application process functioning?

Readiness:
→ Are the critical dependencies required for serving requests available?
```

Keep the checks intentional.

---

## 31. Complete production picture

Now combine the resilience concepts you've been learning:

```
                         CLIENT
                           │
                           ▼
                      API Gateway
                           │
                      Rate Limiter
                           │
                           ▼
                     Load Balancer
                           │
                      Health Check
                           │
             ┌─────────────┼─────────────┐
             ▼             ▼             ▼
           App-1         App-2         App-3
          READY          READY         DOWN
             │             │
             └──────┬──────┘
                    │
                 Bulkhead
                    │
              Circuit Breaker
                    │
                  Timeout
                    │
            Retry + Backoff
                    │
                    ▼
              Downstream
```

And for asynchronous processing:

```
Producer
   │
   ▼
Rate Limiter
   │
   ▼
Kafka
   │
   ▼
Consumer
   │
   ▼
Backpressure
   │
   ▼
Bulkhead
   │
   ▼
Database
```

---

## 32. Interview questions you should know

### Q1. What is a health check?

A mechanism for determining whether an application instance or service is alive and/or ready to serve traffic.

### Q2. Liveness vs readiness?

Liveness determines whether the application should be considered alive and potentially restarted if it becomes unrecoverably unhealthy. Readiness determines whether the instance should receive traffic.

### Q3. Can liveness be healthy while readiness is unhealthy?

Yes.

Example:

```
Application process → alive
Database            → unavailable

Liveness  → PASS
Readiness → FAIL
```

### Q4. What happens when readiness fails?

Typically:

```
Instance removed from traffic
```

rather than immediately restarted.

### Q5. What happens when liveness fails?

Depending on the orchestrator:

```
Container/pod may be restarted
```

### Q6. Why not check every dependency in liveness?

Because a temporary dependency failure could cause unnecessary application restarts and potentially create a cascading failure.

### Q7. What is health-check flapping?

Repeated transitions:

```
UP → DOWN → UP → DOWN
```

causing unstable traffic routing.

### Q8. How do you avoid flapping?

Use:

- Failure threshold
- Success threshold
- Timeout
- Grace period
- Hysteresis

---

## 33. The mental model

Remember these distinctions:

```
                 HEALTH & RESILIENCE

Health Check
     ↓
"Am I alive / ready?"

Readiness
     ↓
"Should I receive traffic?"

Liveness
     ↓
"Should I be restarted?"

Startup
     ↓
"Have I finished initialization?"

Rate Limiter
     ↓
"How much traffic should enter?"

Backpressure
     ↓
"Can the downstream keep up?"

Bulkhead
     ↓
"How much resource can this workload consume?"

Timeout
     ↓
"How long should I wait?"

Circuit Breaker
     ↓
"Should I stop calling this failing dependency?"
```

---

## One-line interview answer

> Health checks provide automated signals about service health; **liveness** determines whether an instance is alive, **readiness** determines whether it should receive traffic, and **startup** determines whether initialization has completed.

