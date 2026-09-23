# API Security

**API Security** is the set of techniques used to protect APIs from:

- Unauthorized access
- Data theft
- Data manipulation
- Abuse and excessive traffic
- Injection attacks
- Token theft
- Replay attacks
- Denial of service
- Privilege escalation

In simple terms:

> API Security ensures that the right client can perform the right operation on the right resource, while protecting the API and its data from attacks.

For a senior/system-design interview, don't think of API security as just JWT authentication. It is a complete security layer covering authentication, authorization, transport security, input validation, rate limiting, secrets, logging, and threat protection.

---

## 1. Why do we need API Security?

Imagine an e-commerce API:

```
GET    /users/123
GET    /orders/456
POST   /orders
POST   /payments
DELETE /users/123
```

Without security, an attacker could potentially do:

```
GET /users/123
```

and access another user's information.

Or:

```
DELETE /users/123
```

and delete someone else's account.

Or send:

```
POST /orders
```

10,000 times per second.

So API security needs to answer several questions:

```
Who are you?
       ↓
Are you allowed?
       ↓
Can you access THIS resource?
       ↓
Is the request valid?
       ↓
Are you sending too many requests?
       ↓
Is the request suspicious?
```

---

## 2. API Security has multiple layers

A production API typically has several security layers:

```
                    API Security
                         │
        ┌────────────────┼─────────────────┐
        │                │                 │
        ▼                ▼                 ▼
 Authentication     Authorization      Transport
        │                │                 │
        ▼                ▼                 ▼
 JWT/OIDC          RBAC/ABAC         HTTPS/TLS
 API Keys          Permissions
 mTLS
        │
        ├──────────────────────────────────┐
        │                                  │
        ▼                                  ▼
 Input Validation                     Rate Limiting
        │                                  │
        ▼                                  ▼
 Injection Protection                 Abuse Protection
        │
        ├──────────────────────────────────┐
        │                                  │
        ▼                                  ▼
 Secrets Management                   Monitoring
        │                                  │
        ▼                                  ▼
 Key Rotation                        Audit Logs
```

---

## 3. How API Security works

A typical request looks like:

```
Client
   │
   │ HTTPS Request
   │ Authorization: Bearer <token>
   ▼
Load Balancer
   │
   ▼
API Gateway
   │
   ├── TLS
   ├── Authentication
   ├── Rate Limiting
   ├── WAF
   ├── Token Validation
   └── Request Filtering
   │
   ▼
Microservice
   │
   ├── Authorization
   ├── Input Validation
   ├── Business Rules
   └── Resource Ownership
   │
   ▼
Database
```

Security should happen at multiple layers, not only at the gateway.

---

## 4. Layer 1 — HTTPS / TLS

The first protection is encryption in transit.

Instead of:

```
HTTP
```

use:

```
HTTPS
```

Example:

```
POST https://api.example.com/orders
```

TLS protects data while traveling:

```
Client
   │
   │ encrypted
   ▼
Internet
   │
   │ encrypted
   ▼
API
```

Without TLS, an attacker on the network could potentially intercept:

- Authorization header
- Cookies
- Passwords
- Personal information
- Payment information

So:

> Never send authentication credentials or tokens over plain HTTP.

---

## 5. Layer 2 — Authentication

Authentication answers:

> Who are you?

Common approaches:

- JWT
- OAuth 2.0 / OIDC
- Session cookies
- API Keys
- mTLS
- Basic Authentication

For modern user-facing applications:

```
OIDC / OAuth 2.0
       +
Access Token
```

is common.

Example:

```
GET /orders
Authorization: Bearer eyJhbGciOi...
```

The API validates the access token.

---

## 6. JWT validation

Suppose the access token is a JWT:

```
HEADER.PAYLOAD.SIGNATURE
```

The API validates:

1. Signature
2. Expiration
3. Issuer
4. Audience
5. Not-before time if applicable
6. Required claims
7. Scopes/permissions

Conceptually:

```
              JWT
               │
               ▼
       Verify Signature
               │
               ▼
         Check exp
               │
               ▼
         Check issuer
               │
               ▼
         Check audience
               │
               ▼
        Check scopes
               │
               ▼
        Authentication
             passed
```

If validation fails:

```
401 Unauthorized
```

---

## 7. 401 vs 403

This is an important interview question.

### 401 Unauthorized

Means:

> The request is not successfully authenticated.

Examples:

- Missing token
- Invalid token
- Expired token
- Invalid signature

Example:

```
GET /orders
```

No token.

Response:

```
401 Unauthorized
```

### 403 Forbidden

Means:

> The user is authenticated, but doesn't have permission.

Example:

```
User = normal-user
Endpoint = DELETE /users/123
```

Token is valid.

But user doesn't have the required permission.

Response:

```
403 Forbidden
```

Remember:

```
401 → Who are you? Authentication failed.
403 → I know who you are, but you're not allowed.
```

---

## 8. Layer 3 — Authorization

Authentication is not enough.

Suppose:

```
User A
```

has a valid token.

They request:

```
GET /users/200
```

But user 200 belongs to another person.

The API must check:

> Does User A have permission to access User 200?

This is **authorization**.

Common models:

### RBAC

Role-Based Access Control.

```
ADMIN
MANAGER
USER
```

Example:

```
ADMIN → DELETE users
USER  → READ own profile
```

### Permission-based

```
orders:read
orders:create
orders:update
orders:delete
```

### ABAC

Attribute-Based Access Control.

Example:

```
user.department == order.department
```

### Resource ownership

```
order.userId == authenticatedUser.id
```

This is extremely important for APIs.

---

## 9. IDOR / Broken Object-Level Authorization

One of the most important API security problems is **Broken Object Level Authorization (BOLA)**, historically often called **IDOR**.

Suppose:

```
GET /orders/1001
```

User owns order 1001.

Attacker changes it to:

```
GET /orders/1002
```

If the API returns another user's order, there's a security vulnerability.

**Bad:**

```java
Order order = orderRepository.findById(orderId);
return order;
```

**Better:**

```java
Order order =
    orderRepository.findByIdAndUserId(orderId, authenticatedUserId);
```

Conceptually:

```
Request
   │
   ▼
Authenticate user
   │
   ▼
Extract userId from trusted identity
   │
   ▼
Load resource
   │
   ▼
Check ownership / permission
   │
   ├── allowed ──► Return data
   │
   └── denied ───► 403
```

> Never trust a user ID supplied by the client when determining identity.

---

## 10. Layer 4 — Input Validation

Never blindly trust API input.

Suppose:

```
POST /users
```

Request:

```json
{
  "name": "John",
  "age": 30,
  "email": "john@example.com"
}
```

Validate:

- name → length
- age → valid range
- email → valid format
- required fields
- maximum payload size
- allowed characters
- enum values

Why?

Because attackers can send malicious input.

---

## 11. SQL Injection

Suppose you build SQL like:

```java
String sql =
    "SELECT * FROM users WHERE name = '" + name + "'";
```

Attacker sends malicious input.

This can potentially alter the SQL query.

Instead use:

- Prepared Statements
- Parameterized Queries
- ORM parameter binding

Conceptually:

```
User Input
    │
    ▼
Validation
    │
    ▼
Parameterized Query
    │
    ▼
Database
```

> Never construct SQL by concatenating untrusted input.

---

## 12. Other injection attacks

API security also protects against:

- SQL Injection
- NoSQL Injection
- Command Injection
- LDAP Injection
- XSS
- XML External Entity attacks
- Template Injection

The exact defenses depend on the technology.

---

## 13. Layer 5 — Rate Limiting

Suppose an attacker sends:

```
1,000,000 requests/second
```

Your API could become unavailable.

Rate limiting controls how many requests a client can make.

Example:

```
100 requests/minute/user
```

Architecture:

```
Client
   │
   ▼
API Gateway
   │
   ▼
Rate Limiter
   │
   ├── Under limit ──► API
   │
   └── Over limit ───► 429
```

Response:

```
429 Too Many Requests
```

---

## 14. How rate limiting works internally

A common distributed implementation uses Redis.

For example:

```
User = 123
Limit = 100 requests/minute
```

Redis might maintain:

```
rate:user:123 = 73
```

Each request increments the counter.

```
Request
   │
   ▼
Redis INCR
   │
   ▼
73 → 74
   │
   ▼
74 <= 100
   │
   ▼
Allow
```

Once:

```
101 > 100
```

the request is rejected.

Common algorithms:

- Fixed Window
- Sliding Window
- Token Bucket
- Leaky Bucket

---

## 15. Layer 6 — API Gateway

In microservices, API Gateway is commonly a security enforcement point.

```
                    Internet
                       │
                       ▼
                ┌─────────────┐
                │ API Gateway │
                └──────┬──────┘
                       │
          ┌────────────┼────────────┐
          ▼            ▼            ▼
       User API     Order API    Payment API
```

Gateway can handle:

- TLS termination
- Authentication
- JWT validation
- Rate limiting
- IP filtering
- Request size limits
- WAF integration
- API key validation
- Logging
- Routing

But **don't put all authorization logic into the gateway**.

For example:

```
Gateway:
    Is JWT valid?

Order Service:
    Does this user own order #123?
```

Business authorization should generally remain close to the resource.

---

## 16. Layer 7 — API Keys

API keys are commonly used for:

- Server-to-server APIs
- Public APIs
- Developer APIs
- Internal integrations

Example:

```
GET /weather
X-API-Key: abc123
```

But API keys should not be treated as equivalent to a full user identity system.

Protect them:

- Don't commit to Git
- Don't put secrets in source code
- Rotate periodically
- Restrict permissions
- Monitor usage
- Revoke compromised keys

Use a proper **secrets manager** where appropriate.

---

## 17. Layer 8 — OAuth 2.0

For delegated API access:

```
Client
   │
   ▼
Authorization Server
   │
   ▼
Access Token
   │
   ▼
API
```

For example:

```
orders:read
orders:create
```

The access token can carry **scopes**.

API checks:

```
Required:
orders:read

Token:
orders:read orders:create
```

Access allowed.

---

## 18. Layer 9 — mTLS

For high-trust service-to-service communication, **mutual TLS (mTLS)** can be used.

**Normal TLS:**

```
Client ───────► Server
       Server certificate
```

**mTLS:**

```
Client ◄──────► Server
  │               │
Client cert    Server cert
```

Both sides authenticate each other.

This is useful in environments such as:

- Microservices
- Banking systems
- Service meshes
- Highly sensitive internal APIs

---

## 19. Layer 10 — Secrets Management

Never hardcode:

```java
String password = "MyPassword123";
```

or:

```
JWT_SECRET=abc123
```

inside source code.

Instead:

```
Application
    │
    ▼
Secrets Manager
    │
    ├── DB password
    ├── API keys
    ├── Private keys
    └── OAuth credentials
```

Examples include cloud/provider secrets-management systems and Vault-style solutions.

Also implement:

- Secret rotation
- Key rotation
- Access control
- Audit logging

---

## 20. Layer 11 — Replay Attack Protection

Suppose an attacker steals a valid request:

```
POST /payment
Authorization: Bearer TOKEN
```

They replay it.

For sensitive operations, systems can use mechanisms such as:

- Idempotency keys
- Short-lived tokens
- Nonce
- Request timestamps
- Replay detection
- TLS

For example:

```
POST /payments
Idempotency-Key: 7f83-abc-123
```

If the same payment request is accidentally or maliciously submitted again, the server can recognize the key.

---

## 21. Layer 12 — CORS

For browser-based applications, **CORS** controls which origins are allowed to make browser requests to an API.

Example:

```
https://myapp.com
```

may be allowed.

But:

```
https://evil.com
```

may not be allowed.

Conceptually:

```
Browser
   │
   │ Origin: https://myapp.com
   ▼
API
   │
   ▼
CORS Policy
   │
   ├── Allowed
   └── Rejected by browser policy
```

Important:

> CORS is primarily a browser security mechanism. It is not a replacement for authentication or authorization.

A non-browser attacker can call your API directly, so the API still needs proper security.

---

## 22. Layer 13 — CSRF

CSRF is especially relevant when authentication relies on browser cookies.

Example:

```
User logged into bank.com
          │
          ▼
Browser has session cookie
          │
          ▼
Malicious website tricks browser
into sending request to bank.com
```

Defenses include:

- SameSite cookies
- CSRF tokens
- Origin/Referer validation
- Proper cookie configuration

With bearer tokens sent explicitly in an Authorization header, the traditional cookie-based CSRF threat is different, but token storage and XSS still need careful consideration.

---

## 23. Layer 14 — Security Headers

APIs and web applications can use appropriate HTTP security headers.

Examples:

```
Strict-Transport-Security
Content-Security-Policy
X-Content-Type-Options
```

Exact headers depend on whether the endpoint serves APIs, browser content, or both.

---

## 24. Layer 15 — Logging and Monitoring

Security doesn't end after the request is processed.

You need to detect suspicious behavior.

For example:

```
User 123
   │
   ├── 10 failed logins
   ├── 500 API requests
   ├── accesses many user IDs
   └── requests unusual endpoints
```

Monitoring can detect:

- Brute-force attacks
- Credential abuse
- Token abuse
- BOLA attempts
- DDoS patterns
- Unusual traffic
- Privilege escalation

Important:

> Never log sensitive secrets or raw authentication tokens.

---

## 25. WAF

A **Web Application Firewall (WAF)** sits in front of APIs/web applications and filters suspicious HTTP traffic.

Architecture:

```
Internet
   │
   ▼
WAF
   │
   ├── Block malicious patterns
   ├── IP reputation
   ├── Request filtering
   └── Attack detection
   │
   ▼
Load Balancer
   │
   ▼
API Gateway
   │
   ▼
Services
```

WAF can help mitigate common web attack patterns, but it does not replace application-level authorization.

---

## 26. API Security in a Microservices Architecture

A production architecture might look like:

```
                         Internet
                            │
                            ▼
                    ┌───────────────┐
                    │      CDN      │
                    └───────┬───────┘
                            │
                            ▼
                    ┌───────────────┐
                    │      WAF      │
                    └───────┬───────┘
                            │
                            ▼
                    ┌───────────────┐
                    │ Load Balancer │
                    └───────┬───────┘
                            │
                            ▼
                    ┌───────────────┐
                    │ API Gateway   │
                    │               │
                    │ Auth          │
                    │ Rate Limit    │
                    │ Validation    │
                    └───────┬───────┘
                            │
             ┌──────────────┼──────────────┐
             │              │              │
             ▼              ▼              ▼
        User Service   Order Service  Payment Service
             │              │              │
             │              │              │
             └──────────────┼──────────────┘
                            │
                            ▼
                       Databases
```

Security responsibilities can be distributed:

| Layer | Responsibility |
|---|---|
| CDN | Edge protection/caching |
| WAF | HTTP attack filtering |
| Load Balancer | Traffic distribution/TLS |
| API Gateway | Authentication, rate limits, routing |
| Service | Authorization + business rules |
| Database | Access controls |
| Secrets Manager | Secrets/keys |
| Monitoring | Detection/auditing |

---

## 27. Example: Secure Order API

Suppose:

```
GET /orders/123
Authorization: Bearer <access-token>
```

The request travels:

```
Client
  │
  │ HTTPS
  ▼
WAF
  │
  ▼
API Gateway
  │
  ├── Rate limit
  ├── Validate JWT
  └── Validate request
  │
  ▼
Order Service
  │
  ├── Check permission
  ├── Check order ownership
  └── Business validation
  │
  ▼
Database
```

Let's say the token identifies:

```
userId = 500
```

The order is:

```
orderId = 123
userId = 500
```

Access:

```
500 == 500
```

Allowed.

But if:

```
orderId = 456
userId = 700
```

then:

```
500 != 700
```

Return:

```
403 Forbidden
```

---

## 28. Defense in Depth

A very important system-design principle is:

> Never depend on a single security mechanism.

**Bad:**

```
Internet
   │
   ▼
API
   │
   ▼
Database
```

**Better:**

```
Internet
   │
   ▼
HTTPS
   │
   ▼
WAF
   │
   ▼
API Gateway
   │
   ├── Authentication
   ├── Rate limiting
   ├── Request validation
   │
   ▼
Service
   │
   ├── Authorization
   ├── Ownership check
   ├── Business validation
   │
   ▼
Database
   │
   └── DB access control
```

If one layer fails, other layers still provide protection.

---

## 29. Common API Security vulnerabilities

You should know these for senior interviews:

1. Broken Object Level Authorization
2. Broken Authentication
3. Broken Function Level Authorization
4. Unrestricted Resource Consumption
5. Injection
6. Security Misconfiguration
7. Improper Inventory Management
8. Unsafe Consumption of APIs
9. Server-Side Request Forgery
10. Sensitive Data Exposure

These are closely aligned with the major concerns in the **OWASP API Security Top 10**.

---

## 30. Common mistakes

### ❌ Trusting client-supplied user ID

```
GET /users/123
```

and assuming the caller owns user 123.

### ❌ Only checking authentication

```
Valid JWT → allow everything
```

Wrong.

You also need authorization.

### ❌ Using ID token as API access token

OIDC:

```
ID Token → client identity information
Access Token → API authorization
```

### ❌ No rate limiting

Attackers can abuse your API.

### ❌ Long-lived access tokens

If stolen, they remain useful for a long time.

### ❌ Hardcoded secrets

Secrets can leak through source code, Git history, logs, etc.

### ❌ Logging tokens

Never log:

```
Authorization: Bearer eyJ...
```

### ❌ Relying only on API Gateway authorization

Business-level authorization should also happen in the service.

---

## 31. API Security request lifecycle

A good mental model is:

```
                API Request
                     │
                     ▼
                  HTTPS
                     │
                     ▼
                    WAF
                     │
                     ▼
               Rate Limiting
                     │
                     ▼
             Authentication
                     │
                     ▼
              Token Validation
                     │
                     ▼
              Authorization
                     │
                     ▼
             Input Validation
                     │
                     ▼
            Business Validation
                     │
                     ▼
                  Service
                     │
                     ▼
                Database
                     │
                     ▼
                Audit Logs
```

---

## 32. API Security vs Authentication vs Authorization

Don't confuse these concepts.

```
API Security
     │
     ├── Authentication
     │      └── Who are you?
     │
     ├── Authorization
     │      └── What can you do?
     │
     ├── Encryption
     │      └── Can someone read the traffic?
     │
     ├── Rate Limiting
     │      └── Are you abusing the API?
     │
     ├── Input Validation
     │      └── Is the request safe?
     │
     ├── Secrets Management
     │      └── Are credentials protected?
     │
     ├── Monitoring
     │      └── Can we detect attacks?
     │
     └── WAF / Threat Protection
            └── Can we block malicious traffic?
```

---

## 33. Senior System Design Interview Answer

If the interviewer asks:

> "How would you secure an API?"

A strong answer would be:

> "I would use a defense-in-depth approach. First, all communication would use HTTPS/TLS. For user authentication, I'd typically use OAuth 2.0/OIDC with short-lived access tokens. The API Gateway can validate the token's signature, issuer, audience, expiration, and scopes. Authorization should also happen at the service layer, including resource ownership checks to prevent BOLA/IDOR vulnerabilities.
>
> I'd add rate limiting at the gateway, typically using a distributed mechanism such as Redis for horizontally scaled services. I'd validate and sanitize all inputs, use parameterized database queries to prevent injection, enforce request-size limits, and protect secrets using a secrets manager with key rotation.
>
> For service-to-service communication, depending on the security requirements, I'd consider mTLS or workload identity. A WAF can provide additional protection against common HTTP attacks. Finally, I'd implement security auditing, monitoring, anomaly detection, and alerts while ensuring sensitive tokens and credentials aren't written to logs."

---

## 34. The complete picture

For your system-design learning, keep this architecture in mind:

```
                         ┌─────────────┐
                         │   Client    │
                         └──────┬──────┘
                                │
                              HTTPS
                                │
                                ▼
                         ┌─────────────┐
                         │     WAF     │
                         └──────┬──────┘
                                │
                                ▼
                       ┌─────────────────┐
                       │ Load Balancer   │
                       └────────┬────────┘
                                │
                                ▼
                       ┌─────────────────┐
                       │  API Gateway    │
                       │                 │
                       │ Authentication  │
                       │ Rate Limiting   │
                       │ Token Validation│
                       │ Request Limits  │
                       └────────┬────────┘
                                │
                                ▼
                       ┌─────────────────┐
                       │ Microservice    │
                       │                 │
                       │ Authorization   │
                       │ BOLA Check      │
                       │ Input Validation│
                       │ Business Rules  │
                       └────────┬────────┘
                                │
                                ▼
                       ┌─────────────────┐
                       │    Database     │
                       │ Access Control  │
                       └─────────────────┘

            ┌──────────────────────────────┐
            │ Identity Provider            │
            │ OAuth 2.0 / OpenID Connect   │
            └──────────────┬───────────────┘
                           │
                      Access Token
                           │
                           ▼
                       API Gateway
```

### The key interview takeaway

```
API Security
     =
Authentication
+ Authorization
+ HTTPS/TLS
+ Input Validation
+ Rate Limiting
+ WAF
+ Secrets Management
+ Secure Token Handling
+ Resource Ownership Checks
+ Monitoring & Auditing
+ Defense in Depth
```

And the most important distinction to remember:

```
Authentication  → WHO are you?
Authorization   → WHAT can you do?
API Security    → HOW do we protect the entire API?
```

