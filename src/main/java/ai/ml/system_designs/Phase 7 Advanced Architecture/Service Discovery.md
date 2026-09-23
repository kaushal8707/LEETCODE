# Service Discovery

Service Discovery is a mechanism that allows one microservice to find the network location of another microservice dynamically, without hard-coding its IP address or hostname.

It is a fundamental concept in microservices and distributed systems.

---

## 1. Why do we need Service Discovery?

Consider:

```
Order Service
      │
      │ HTTP
      ▼
Payment Service
```

In a simple environment, you might configure:

```
http://10.10.20.15:8080
```

But in a microservices environment, Payment Service may have multiple instances:

```
Payment Service

10.10.20.15:8080
10.10.20.16:8080
10.10.20.17:8080
```

And instances can:

- start
- stop
- restart
- move to another machine
- scale up/down
- receive a new IP address

So this is a bad design:

```
Order Service
      │
      ▼
10.10.20.15:8080
```

What happens if that instance dies?

The Order Service still points to:

```
10.10.20.15
```

---

## 2. Service Discovery solves this problem

Instead of Order Service knowing the actual IP addresses:

```
Order Service
      │
      ▼
Service Discovery
      │
      ▼
Payment Service instances
```

Service Discovery maintains information such as:

```
Payment Service
 ├── 10.10.20.15:8080
 ├── 10.10.20.16:8080
 └── 10.10.20.17:8080
```

Order Service asks:

> "Where can I find Payment Service?"

Service Discovery responds with available instances.

---

## 3. Real-world analogy

Think about a company's employee directory.

You don't memorize:

```
John → Desk 14
```

because John may move desks.

Instead:

```
You
 │
 ▼
Employee Directory
 │
 ▼
John → Current location
```

Service Discovery works similarly:

```
Order Service
      │
      ▼
Service Registry
      │
      ▼
Payment Service
      │
      ├── Instance 1
      ├── Instance 2
      └── Instance 3
```

---

## 4. Service Registry

The Service Registry is the central place that maintains information about available service instances.

For example:

```
Service Registry

Order Service
    10.0.1.10:8080
    10.0.1.11:8080

Payment Service
    10.0.2.10:8080
    10.0.2.11:8080
    10.0.2.12:8080

Inventory Service
    10.0.3.10:8080
    10.0.3.11:8080
```

The registry may maintain:

- Service Name
- IP Address
- Port
- Health Status
- Metadata
- Version
- Zone/Region

---

## 5. How Service Discovery works

There are generally three important steps.

### Step 1 — Service starts

Payment Service starts:

```
Payment Service
10.0.2.15:8080
```

It registers itself:

```
Payment Service
      │
      │ REGISTER
      ▼
Service Registry
```

Registry stores:

```
Payment Service → 10.0.2.15:8080
```

### Step 2 — Another service wants it

Order Service needs Payment Service.

```
Order Service
      │
      │ "Where is Payment Service?"
      ▼
Service Registry
```

Registry returns:

```
10.0.2.15:8080
10.0.2.16:8080
10.0.2.17:8080
```

### Step 3 — Order Service sends request

```
Order Service
      │
      ▼
Payment Service
10.0.2.16:8080
```

The important thing is that Order Service didn't need to know the IP beforehand.

---

## 6. Registration

There are two common models.

### Self-registration

The service registers itself.

```
Payment Service
      │
      │ register()
      ▼
Service Registry
```

For example:

```
Payment Service starts
        ↓
Register IP + port
        ↓
Send heartbeat
        ↓
Remain registered
```

If the service shuts down, it can deregister itself.

---

## 7. Client-side Service Discovery

In client-side discovery, the client asks the registry for service instances.

```
                    Service Registry
                           │
                           │
                     Payment instances
                           │
                           ▼
Order Service ─────────► Payment Service
```

Flow:

```
1. Order → Registry
2. Registry → [Payment-1, Payment-2, Payment-3]
3. Order selects an instance
4. Order → Payment instance
```

The client is responsible for choosing the instance.

It may use:

- Round Robin
- Random
- Least Connections
- Weighted selection
- Zone-aware routing

---

## 8. Server-side Service Discovery

Here, the client doesn't directly query the registry.

Instead:

```
Client
  │
  ▼
Load Balancer
  │
  ▼
Service Discovery
  │
  ├── Payment-1
  ├── Payment-2
  └── Payment-3
```

The load balancer/service infrastructure determines which instance receives the request.

This simplifies the client.

---

## 9. Client-side vs Server-side

| | Client-side | Server-side |
|---|---|---|
| Registry queried by | Client | Infrastructure/load balancer |
| Client chooses instance | Yes | No |
| Client complexity | Higher | Lower |
| Infrastructure complexity | Lower | Higher |
| Example concept | Client library | Load balancer/proxy |

---

## 10. Health Checks

This is extremely important.

Suppose Registry contains:

```
Payment Service

10.0.2.10 ✓
10.0.2.11 ✓
10.0.2.12 ✗
```

If 10.0.2.12 has crashed, requests shouldn't continue going there.

The discovery system can use health checks:

```
Registry
   │
   ├── Payment-1 → HEALTHY
   ├── Payment-2 → HEALTHY
   └── Payment-3 → UNHEALTHY
```

Then:

```
Payment-3
   ↓
Removed / marked unavailable
```

---

## 11. Heartbeat

A service can periodically tell the registry:

> "I'm still alive."

For example:

```
Payment Service
      │
      │ heartbeat
      ▼
Registry
```

Every few seconds:

```
10:00:00 → heartbeat
10:00:05 → heartbeat
10:00:10 → heartbeat
```

If heartbeats stop:

```
No heartbeat
     ↓
Registry waits for timeout
     ↓
Instance considered unhealthy
     ↓
Instance removed
```

This prevents dead instances from receiving traffic.

---

## 12. Service Discovery with Load Balancing

Service Discovery and Load Balancing often work together.

Suppose:

```
Payment Service

P1
P2
P3
```

Order Service discovers them:

```
Registry
   │
   ├── P1
   ├── P2
   └── P3
```

Then requests can be distributed:

```
Request 1 → P1
Request 2 → P2
Request 3 → P3
Request 4 → P1
Request 5 → P2
```

For example, Round Robin:

```
P1 → P2 → P3 → P1 → P2 → P3
```

---

## 13. DNS-based Service Discovery

Service discovery doesn't always require a dedicated registry API.

DNS itself can be used.

For example:

```
payment-service.mycompany.internal
```

Order Service asks DNS:

```
payment-service.mycompany.internal
              ↓
             DNS
              ↓
     10.0.2.10
     10.0.2.11
     10.0.2.12
```

This is very common in containerized environments.

---

## 14. Kubernetes Service Discovery

This is especially important if you're learning modern microservices.

In Kubernetes, you generally don't want services calling individual Pod IPs.

Instead:

```
Order Pod
    │
    │
    ▼
payment-service
    │
    ▼
┌───────────────┐
│ Kubernetes    │
│ Service       │
└───────┬───────┘
        │
    ┌───┼────┐
    ▼   ▼    ▼
  Pod1 Pod2 Pod3
```

The Pods can come and go.

Their IPs can change.

But the Kubernetes Service provides a stable endpoint.

For example:

```
http://payment-service:8080
```

The caller doesn't need to know:

```
Pod1 = 10.244.1.15
Pod2 = 10.244.2.18
Pod3 = 10.244.3.21
```

This is one reason Kubernetes makes service discovery much easier.

---

## 15. Service Discovery vs Load Balancer

These are related but different.

### Service Discovery

Answers:

> "Where are the available instances?"

```
Payment Service
   ↓
P1
P2
P3
```

### Load Balancer

Answers:

> "Which instance should receive this request?"

```
Request
   ↓
Load Balancer
   ├── P1
   ├── P2
   └── P3
```

Often they work together:

```
              Service Registry
                     │
              Available instances
                     │
                     ▼
               Load Balancer
                     │
          ┌──────────┼──────────┐
          ▼          ▼          ▼
         P1         P2         P3
```

---

## 16. What happens when a service scales?

Suppose initially:

```
Payment Service

P1
P2
```

Traffic increases:

```
Traffic ↑
   ↓
Scale Payment Service
   ↓
P1
P2
P3
P4
P5
```

New instances register automatically:

```
Registry

Payment
 ├── P1
 ├── P2
 ├── P3 ← new
 ├── P4 ← new
 └── P5 ← new
```

Now clients can discover the new instances.

This is one of the major advantages of dynamic service discovery.

---

## 17. What happens when an instance crashes?

Suppose:

```
Payment

P1 ✓
P2 ✓
P3 ✗
```

Health check detects:

```
P3 → unhealthy
```

Registry becomes:

```
Payment

P1 ✓
P2 ✓
```

Traffic is routed only to healthy instances.

```
Order
  │
  ▼
P1 / P2
```

---

## 18. Service Discovery + Kafka

Service discovery is also relevant to event-driven microservices, but the mechanisms are different.

For HTTP:

```
Order
  │
  ▼
Service Discovery
  │
  ▼
Payment
```

For Kafka:

```
Order Service
     │
     ▼
Kafka Cluster
     │
     ▼
Payment Consumer
```

The application doesn't normally discover an individual consumer instance through a service registry. Kafka manages broker discovery and consumer-group coordination.

So don't confuse:

```
Service Discovery
```

with:

```
Kafka Consumer Group
```

They solve different problems.

---

## 19. Common Service Discovery Technologies

Examples include:

- Kubernetes Services / DNS
- Consul
- Eureka
- etcd
- cloud-provider service discovery mechanisms

The specific technology matters less for an interview than understanding the underlying mechanism.

---

## 20. Service Discovery failure

Now consider an important interview question:

> What happens if the Service Registry itself goes down?

Suppose:

```
Order Service
      │
      ▼
Service Registry ❌
```

If every request requires contacting the registry, the system could become unavailable.

Therefore systems commonly use techniques such as:

### Caching discovered endpoints

```
Order Service
      │
      ▼
Local cache
      │
      ▼
Payment P1/P2/P3
```

If registry temporarily fails, the client can continue using recently discovered endpoints.

### Multiple registry instances

```
          Registry Cluster
        ┌──────┬──────┬──────┐
        ▼      ▼      ▼
       R1     R2     R3
```

Avoid making the registry a single point of failure.

---

## 21. Service Discovery in a complete architecture

Putting everything together:

```
                         Client
                           │
                           ▼
                     API Gateway
                           │
                           ▼
                    Order Service
                           │
                           │
                           ▼
                  Service Discovery
                           │
                  ┌────────┼────────┐
                  ▼        ▼        ▼
             Payment-1 Payment-2 Payment-3
                  │        │        │
                  └────────┼────────┘
                           ▼
                      Payment DB
```

With health checking:

```
                 Service Registry
                       │
          ┌────────────┼────────────┐
          ▼            ▼            ▼
      Payment-1    Payment-2    Payment-3
       HEALTHY      HEALTHY     UNHEALTHY
          │            │
          └──────┬─────┘
                 ▼
           Traffic only
           to healthy nodes
```

---

## 22. Interview scenario

### Question:

> "Order Service needs to call Payment Service. Payment has 10 instances. How does Order know which instance to call?"

Good answer:

> Order Service should not hard-code the IP address of a Payment instance. A service-discovery mechanism maintains the available Payment Service instances. Order can either query the registry and select an instance itself, or send the request through infrastructure such as a load balancer/service proxy that performs the routing. Health checks ensure unhealthy instances aren't selected.

Then explain:

```
Order
  │
  ▼
Service Discovery
  │
  ├── P1 ✓
  ├── P2 ✓
  ├── P3 ✓
  └── P4 ✗
  │
  ▼
Load Balancer
  │
  ▼
P1 / P2 / P3
```

---

## 23. Service Discovery vs DNS vs Load Balancer

This distinction is very important:

```
DNS
 │
 └── "What address corresponds to this name?"

Service Discovery
 │
 └── "Which instances of this service are currently available?"

Load Balancer
 │
 └── "Which available instance should receive this request?"
```

In a modern platform, these responsibilities can overlap.

For example, Kubernetes combines service naming, discovery, and routing through its Service abstraction and networking layer.

---

## 24. Key problems Service Discovery solves

### Without Service Discovery

```
Hard-coded IP
     ↓
Instance changes
     ↓
Configuration becomes stale
     ↓
Requests fail
```

### With Service Discovery

```
Service starts
     ↓
Register
     ↓
Health checks
     ↓
Discover
     ↓
Route
     ↓
Scale dynamically
```

---

## 25. One picture to remember

```
                         ┌──────────────────┐
                         │ Service Registry │
                         │                  │
                         │ Payment:         │
                         │ P1 ✓             │
                         │ P2 ✓             │
                         │ P3 ✗             │
                         └────────┬─────────┘
                                  ▲
                                  │
                             Register /
                             Heartbeat
                                  │
                                  │
                         ┌────────┴────────┐
                         │ Payment Service │
                         │ P1 P2 P3        │
                         └─────────────────┘


Order Service
      │
      │ "Find Payment"
      ▼
Service Discovery
      │
      ▼
Healthy instances
      │
      ▼
Load Balancing
      │
   ┌──┼──┐
   ▼  ▼  ▼
  P1  P2  P3
```

---

## Interview definition

> Service Discovery is the mechanism by which microservices dynamically locate healthy instances of other services without relying on hard-coded network addresses.

