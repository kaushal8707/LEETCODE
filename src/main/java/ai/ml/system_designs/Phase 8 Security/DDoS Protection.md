# DDoS Protection

**DDoS** stands for **Distributed Denial of Service**.

A DDoS attack happens when a large number of compromised devices simultaneously send traffic or requests toward a target system, with the goal of exhausting its resources and making the service unavailable to legitimate users.

**DDoS protection** is a combination of network infrastructure, traffic filtering, rate limiting, WAF rules, and application-level defenses used to detect and absorb/block malicious traffic before it overwhelms the application.

---

## 1. What does a DDoS attack look like?

Imagine your API normally receives:

```
                    Internet
                       |
                       v
                 10,000 req/sec
                       |
                       v
                  API Gateway
                       |
                       v
                   Services
```

Suddenly attackers control thousands of machines:

```
Attacker 1 ──────┐
Attacker 2 ──────┤
Attacker 3 ──────┤
Attacker 4 ──────┤
     ...         ├──────> Your System
Attacker 10,000 ─┘
```

They generate:

```
1,000,000 requests/sec
```

Your infrastructure may become:

```
CPU       → 100%
Memory    → exhausted
Network   → saturated
DB        → overloaded
Connections → exhausted
```

Eventually:

```
Legitimate User
       |
       v
    API
       |
       X
   Unavailable
```

---

## 2. Why is it called "Distributed"?

Because the traffic comes from many different sources.

A traditional DoS attack might be:

```
Attacker
   |
   v
Server
```

A DDoS attack is:

```
Bot 1 ──────┐
Bot 2 ──────┤
Bot 3 ──────┤
Bot 4 ──────┤
Bot 5 ──────┤
...         ├────> Target
Bot 100000 ─┘
```

The attacker may use a **botnet** — a collection of compromised devices controlled by the attacker.

---

## 3. Three Major Types of DDoS Attacks

You should know these for system-design interviews.

```
                    DDoS
                      |
        +-------------+-------------+
        |             |             |
        v             v             v
   Volumetric     Protocol      Application
      Attack        Attack          Attack
```

---

## 4. Volumetric Attacks

The goal is to consume the target's network bandwidth.

Example:

```
Attack traffic
     |
     | 500 Gbps
     v
Internet connection
     |
     X
Bandwidth exhausted
```

Even if your application is perfectly optimized, it cannot receive legitimate traffic if the network pipe is saturated.

Examples include:

- UDP floods
- ICMP floods
- Amplification attacks

The key problem:

> Too much traffic reaches the network infrastructure.

---

## 5. Protocol Attacks

These exploit weaknesses or resource limits in network protocols.

A common example is a **SYN flood**.

Normally TCP connection establishment is:

```
Client                    Server
  |                         |
  |------ SYN ------------>|
  |                         |
  |<----- SYN-ACK ----------|
  |                         |
  |------ ACK ------------>|
  |                         |
  |     Connection         |
```

In a SYN flood:

```
Attacker
   |
   | SYN
   v
Server

Attacker
   |
   | SYN
   v
Server

Attacker
   |
   | SYN
   v
Server
```

The server receives many connection attempts and allocates resources waiting for completion.

Eventually:

```
Connection table
      ↓
    FULL
      ↓
Legitimate connections
      ↓
     FAIL
```

---

## 6. Application-Layer DDoS

This is particularly interesting for backend engineers.

Instead of sending huge amounts of raw network traffic, attackers send valid-looking HTTP requests.

For example:

```
GET /search?q=some-expensive-query
```

Repeated millions of times:

```
Bot 1 → GET /search
Bot 2 → GET /search
Bot 3 → GET /search
...
Bot 100000 → GET /search
```

The traffic may look legitimate.

But each request might trigger:

```
HTTP Request
     |
     v
Authentication
     |
     v
Application logic
     |
     v
Complex query
     |
     v
Database
```

The database eventually becomes overloaded.

This is often called an **L7 (Layer 7) DDoS attack**.

---

## 7. DDoS vs Rate Limiting

These are related but different.

### Rate limiting

Controls how much traffic a client is allowed to generate.

```
User
 |
 v
Rate Limiter
 |
 +----> 100 req/min → ALLOW
 |
 +----> request 101 → 429
```

### DDoS protection

Defends the entire infrastructure against large-scale malicious traffic.

```
Internet
   |
   v
DDoS Protection
   |
   v
WAF
   |
   v
Rate Limiter
   |
   v
API Gateway
   |
   v
Application
```

Rate limiting is one component of DDoS defense, not a complete DDoS solution.

---

## 8. Why Rate Limiting Alone Isn't Enough

Suppose your API gateway can handle:

```
100,000 requests/sec
```

Attackers send:

```
10,000,000 requests/sec
```

Your rate limiter may reject most requests.

But the traffic still has to reach your infrastructure:

```
Internet
   |
   | 10M req/sec
   v
Your network
   |
   X
Network saturated
```

Therefore, large DDoS attacks need protection **before** traffic reaches your application infrastructure.

---

## 9. DDoS Protection Architecture

A typical architecture looks like:

```
                        Internet
                           |
                           v
                 +-------------------+
                 | DDoS Protection   |
                 |                   |
                 | Traffic filtering |
                 +---------+---------+
                           |
                           v
                     +-----------+
                     |    CDN    |
                     +-----+-----+
                           |
                           v
                     +-----------+
                     |    WAF    |
                     +-----+-----+
                           |
                           v
                  +----------------+
                  | Load Balancer |
                  +-------+--------+
                          |
                          v
                  +---------------+
                  | API Gateway   |
                  | Rate Limiting |
                  +-------+-------+
                          |
              +-----------+-----------+
              |           |           |
              v           v           v
           Service     Service     Service
              |           |           |
              +-----------+-----------+
                          |
                          v
                       Database
```

The key idea is:

> Push protection as far toward the edge as possible.

---

## 10. How DDoS Protection Works

A DDoS protection system generally performs several stages.

```
Incoming Traffic
       |
       v
Traffic Analysis
       |
       v
Identify Attack Pattern
       |
       v
Filter / Challenge / Rate Limit
       |
       v
Clean Traffic
       |
       v
Your Infrastructure
```

---

## 11. Step 1 — Traffic Monitoring

The protection system observes traffic patterns.

For example:

**Normal:**

```
100K req/sec
Distributed across countries
Normal endpoint distribution
Normal request behavior
```

Suddenly:

**Attack:**

```
10M req/sec
Huge spike
Same URL
Same request pattern
Abnormal source distribution
```

This can indicate an attack.

---

## 12. Step 2 — Traffic Classification

The system attempts to distinguish:

```
                    Traffic
                       |
             +---------+---------+
             |                   |
             v                   v
        Legitimate            Suspicious
             |                   |
             v                   v
          ALLOW              Analyze further
```

Signals can include:

- Source IP reputation
- Traffic volume
- Request frequency
- Geographic patterns
- Protocol behavior
- Connection behavior
- HTTP headers
- TLS/client fingerprints
- Request patterns
- WAF rules
- Behavioral anomalies

---

## 13. Step 3 — Filtering

Malicious traffic can be dropped:

```
                    Traffic
                       |
                       v
                  DDoS Layer
                 /          \
                /            \
           Malicious       Legitimate
              |                |
              v                v
             DROP            ALLOW
```

The important part is that this happens **before** expensive application processing.

---

## 14. Step 4 — Rate Limiting

For suspicious clients:

```
Client
  |
  v
Rate Limiter
  |
  +---- Within limit → ALLOW
  |
  +---- Exceeds limit → DROP/429
```

Different limits may exist:

- IP
- User
- API Key
- Endpoint
- Country/region
- Tenant

---

## 15. Step 5 — Challenge Suspicious Clients

Some systems don't immediately block traffic.

Instead they challenge suspicious clients.

For example:

```
Client
  |
  v
Challenge
  |
  +---- Pass → Allow
  |
  +---- Fail → Block
```

Challenges can include mechanisms designed to distinguish normal users/browsers from automated traffic.

This is particularly useful for HTTP-layer attacks.

---

## 16. CDN and DDoS Protection

A **CDN** can help absorb large amounts of traffic because requests are distributed across many edge locations.

Without CDN:

```
Users
  |
  v
Your Data Center
```

With CDN:

```
                Internet
                   |
        +----------+----------+
        |          |          |
        v          v          v
      Edge       Edge       Edge
       US         EU         Asia
        |          |          |
        +----------+----------+
                   |
                   v
              Origin Server
```

Attack traffic is handled closer to the source/edge rather than all hitting your origin.

---

## 17. Anycast and DDoS Protection

Large DDoS-protection networks often use **Anycast**.

Conceptually:

```
              Same IP
                 |
      +----------+----------+
      |          |          |
      v          v          v
    Edge       Edge       Edge
   Mumbai     London      New York
```

Traffic is routed toward a suitable/nearby edge location.

Instead of:

```
10 million requests
        |
        v
ONE server
```

traffic can be distributed:

```
10M requests
     |
     +----> Edge 1
     +----> Edge 2
     +----> Edge 3
     +----> Edge 4
     +----> ...
```

This makes it much harder for one origin location to be overwhelmed.

---

## 18. WAF's Role

A **Web Application Firewall (WAF)** operates primarily at the HTTP/application layer.

It can detect patterns associated with attacks such as:

- SQL Injection
- XSS
- Malicious HTTP requests
- Known attack signatures
- Suspicious request patterns

Architecture:

```
Internet
   |
   v
DDoS Protection
   |
   v
WAF
   |
   v
Load Balancer
   |
   v
API Gateway
```

But:

> WAF is not the same as DDoS protection.

They complement each other.

---

## 19. DDoS Protection vs WAF vs Rate Limiting

| Technology | Main Purpose |
|---|---|
| DDoS Protection | Absorb/filter massive malicious traffic |
| CDN | Distribute/cache traffic at edge |
| WAF | Block malicious HTTP/application patterns |
| Rate Limiter | Control request frequency |
| API Gateway | API routing, auth, policies, rate limits |

Think:

```
DDoS Protection
       ↓
   "Too much traffic?"

WAF
       ↓
   "Is this request malicious?"

Rate Limiter
       ↓
   "Is this client sending too many requests?"

API Gateway
       ↓
   "Where should this request go?"
```

---

## 20. L3/L4 vs L7 Protection

This is an important interview concept.

### Layer 3 — Network

Examples:

```
IP traffic
```

Protection focuses on:

- IP addresses
- Traffic volume
- Packets
- Bandwidth

### Layer 4 — Transport

Examples:

```
TCP
UDP
```

Protection focuses on:

- Connections
- SYN floods
- UDP floods
- Connection rates

### Layer 7 — Application

Examples:

```
HTTP
HTTPS
```

Protection focuses on:

- URLs
- HTTP methods
- Headers
- Request patterns
- Application behavior

Diagram:

```
             DDoS
               |
     +---------+---------+
     |         |         |
     v         v         v
    L3        L4        L7
    IP       TCP/UDP    HTTP
     |         |         |
     v         v         v
 Network    Transport  Application
```

---

## 21. Application-Level DDoS Example

Suppose your API has:

```
GET /search
```

and the request causes:

```
Search request
     |
     v
Complex DB query
     |
     v
Multiple joins
     |
     v
500 ms CPU/database work
```

An attacker sends:

```
10,000 req/sec
```

Even though 10,000 req/sec isn't necessarily huge from a network perspective, the backend may perform:

```
10,000 × expensive query
```

and the database becomes overloaded.

Protection could be:

```
DDoS layer
    ↓
WAF
    ↓
Rate Limit
    ↓
Caching
    ↓
Query limits
    ↓
Application
```

This is why application-aware protection matters.

---

## 22. DDoS Protection and Caching

Caching can dramatically reduce the load caused by repeated requests.

Suppose attackers request:

```
GET /products
```

If the response is cached:

```
Request
   |
   v
CDN Cache
   |
   +---- HIT → return cached response
   |
   +---- MISS → Origin
```

A cache hit prevents the request from reaching your application/database.

This can significantly improve resilience for cacheable content.

---

## 23. DDoS Protection and Autoscaling

Autoscaling can help absorb some traffic:

```
Traffic increases
       |
       v
Autoscaling
       |
       +---- 10 instances
       |
       +---- 50 instances
       |
       +---- 100 instances
```

But autoscaling alone is not DDoS protection.

Why?

Because an attacker can cause:

```
Traffic
  ↓
Autoscaling
  ↓
More servers
  ↓
More CPU/cloud cost
  ↓
Database eventually overloaded
```

This is sometimes called an **economic denial-of-service** concern.

You want to filter malicious traffic **before** blindly scaling your backend.

---

## 24. DDoS Protection and Database Protection

A common mistake is protecting only the API.

Consider:

```
Internet
   |
   v
DDoS Protection
   |
   v
API
   |
   v
Database
```

Even if the API survives, excessive legitimate-looking requests can exhaust:

- DB connections
- DB CPU
- DB IOPS
- DB locks
- Connection pools

Therefore use multiple layers:

```
Edge protection
      ↓
Rate limiting
      ↓
Caching
      ↓
Connection limits
      ↓
Query optimization
      ↓
Database protection
```

---

## 25. What Happens During an Attack?

A mature system might behave like this:

```
Normal traffic
     |
     v
DDoS protection
     |
     v
WAF
     |
     v
Rate limiter
     |
     v
Application
```

Attack starts:

```
Huge traffic spike
       |
       v
DDoS detection
       |
       v
Mitigation enabled
       |
       +---- Malicious → DROP
       |
       +---- Suspicious → CHALLENGE/RATE LIMIT
       |
       +---- Legitimate → ALLOW
                              |
                              v
                         Application
```

The goal is:

> Keep legitimate users working while absorbing or blocking attack traffic.

---

## 26. Failover During DDoS

For critical systems, you may have multiple regions:

```
                    Global Traffic
                          |
                +---------+---------+
                |                   |
                v                   v
            Region A             Region B
                |                   |
             Services            Services
                |                   |
                v                   v
              DB A                DB B
```

If one region is heavily affected:

```
Traffic
   |
   +---- Region A ❌
   |
   +---- Region B ✅
```

Global traffic management can redirect traffic where appropriate.

However, multi-region architecture doesn't automatically solve DDoS; the edge protection layer remains important.

---

## 27. DDoS Protection Best Practices

### 1. Protect at the edge

```
Internet
   ↓
DDoS Protection
   ↓
Origin
```

Don't wait until traffic reaches your application.

### 2. Use CDN

Especially for:

- Static files
- Images
- JavaScript
- CSS
- Cacheable APIs

### 3. Use WAF

For HTTP-layer attacks.

### 4. Use rate limiting

Per:

- IP
- User
- API key
- Tenant
- Endpoint

### 5. Protect expensive endpoints

For example:

```
/search
/report
/export
/login
/payment
```

### 6. Cache aggressively where appropriate

Reduce origin traffic.

### 7. Monitor traffic patterns

Track:

- Requests/sec
- Bandwidth
- Connections
- 4xx/5xx
- Latency
- CPU
- DB connections

### 8. Have an incident-response plan

Know:

- Who gets alerted?
- Who enables stricter rules?
- How do we block traffic?
- How do we protect the origin?
- How do we scale?
- How do we communicate?

---

## 28. DDoS Protection in a Real-World E-Commerce System

Imagine:

```
                    Customers
                        |
                        v
                 DDoS Protection
                        |
                        v
                       CDN
                        |
                        v
                       WAF
                        |
                        v
                 Load Balancer
                        |
                        v
                  API Gateway
                        |
                  Rate Limiting
                        |
          +-------------+-------------+
          |             |             |
          v             v             v
       Product        Order        Payment
       Service       Service       Service
          |             |             |
          v             v             v
        Cache          DB           DB
```

Suppose attackers send:

```
5 million requests/sec
```

The system attempts to:

```
5M requests
     |
     v
DDoS protection
     |
     +---- Attack traffic → DROP
     |
     +---- Suspicious → CHALLENGE
     |
     +---- Legitimate → CDN/WAF
                              |
                              v
                         Rate Limiter
                              |
                              v
                         Application
```

The application should see only traffic it can reasonably handle.

---

## 29. DDoS Protection vs Firewall

A traditional network firewall primarily controls whether traffic is allowed based on rules such as:

- Source IP
- Destination IP
- Port
- Protocol

DDoS protection is broader and can include:

- Traffic scrubbing
- Volumetric mitigation
- Behavior analysis
- Rate limiting
- Anycast distribution
- WAF
- Bot detection
- Application-layer mitigation

So:

> Firewall ≠ Complete DDoS Protection

A firewall can be one component of the overall security architecture.

---

## 30. Senior System Design View

When designing a highly available public API, think in **layers**:

```
                     INTERNET
                         |
                         v
              +--------------------+
              | DDoS Protection     |
              | L3/L4 Mitigation    |
              +---------+----------+
                        |
                        v
              +--------------------+
              | CDN / Edge         |
              +---------+----------+
                        |
                        v
              +--------------------+
              | WAF                |
              | L7 Filtering       |
              +---------+----------+
                        |
                        v
              +--------------------+
              | Load Balancer      |
              +---------+----------+
                        |
                        v
              +--------------------+
              | API Gateway        |
              | Auth + Rate Limit  |
              +---------+----------+
                        |
                        v
                  Microservices
                        |
                        v
                      Cache
                        |
                        v
                     Database
```

This gives **defense in depth**.

---

## 31. Interview Answer

If the interviewer asks:

> "What is DDoS protection and how does it work?"

A strong senior-level answer would be:

> DDoS protection is a layered defense mechanism that protects a service from distributed malicious traffic intended to exhaust network, connection, compute, or application resources. DDoS attacks can occur at network, transport, or application layers. A production architecture typically places DDoS mitigation at the edge, often using distributed traffic-scrubbing infrastructure and Anycast, followed by CDN, WAF, load balancing, API-gateway rate limiting, and application-level protections. Traffic is analyzed for volume, protocol behavior, source reputation, and HTTP behavior; malicious traffic is dropped, suspicious traffic may be challenged or rate-limited, and legitimate traffic is forwarded to the origin. Rate limiting and autoscaling help but are not sufficient for large volumetric attacks because the traffic must ideally be filtered before it saturates the network or origin infrastructure.

### The mental model to remember

```
DDoS Protection
      |
      v
"Stop huge malicious traffic
 before it reaches my system"
      |
      +---- L3/L4 → Network/Protocol protection
      |
      +---- CDN    → Absorb/distribute traffic
      |
      +---- WAF    → HTTP attack filtering
      |
      +---- Rate Limiting → Control request rate
      |
      +---- App Protection → Protect expensive operations
      |
      v
   Application
```

### Most important distinction

```
DDoS Protection → "Is someone overwhelming my infrastructure?"
WAF             → "Is this HTTP request malicious?"
Rate Limiting   → "Is this client sending too many requests?"
CDN             → "Can I handle traffic at the edge?"
Autoscaling     → "Can I add capacity when legitimate load increases?"
```

