# JWT — What It Is and How It Works

**JWT (JSON Web Token)** is a compact, URL-safe token format used to securely transmit claims between parties.

In authentication systems, JWT is commonly used as an **access token** that allows a client to prove its identity to an API after logging in.

The simplest mental model is:

> JWT = a digitally signed token containing claims about a user or client.

For example:

```
User Login
    ↓
Authentication Server
    ↓
JWT Access Token
    ↓
Client
    ↓
Send JWT with API requests
    ↓
API validates JWT
    ↓
Allow / Deny
```

---

## 1. JWT vs Authentication

JWT itself does not perform the login.

Usually the flow is:

```
Authentication
      ↓
Verify username/password
      ↓
Issue JWT
      ↓
JWT used for subsequent requests
      ↓
Authorization
```

So:

```
Authentication → Who are you?
JWT            → Evidence/credential representing the authenticated identity
Authorization  → What are you allowed to do?
```

---

## 2. What Does a JWT Look Like?

A JWT looks like this:

```
xxxxx.yyyyy.zzzzz
```

It contains three parts:

```
HEADER.PAYLOAD.SIGNATURE
```

For example:

```
eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.
eyJzdWIiOiIxMjM0NSIsInJvbGUiOiJVU0VSIiwiZXhwIjoxNz...
.signature
```

The three components are:

```
┌──────────────┐
│    Header    │
├──────────────┤
│   Payload    │
├──────────────┤
│  Signature   │
└──────────────┘
```

---

## 3. JWT Header

The header contains metadata about the token.

Example:

```json
{
  "alg": "RS256",
  "typ": "JWT"
}
```

`alg` tells us which cryptographic algorithm is used.

For example:

```
HS256
RS256
ES256
```

`typ` indicates that the token is a JWT.

---

## 4. JWT Payload

The payload contains **claims**.

Example:

```json
{
  "sub": "12345",
  "name": "Kaushal",
  "role": "CUSTOMER",
  "scope": "orders:read orders:create",
  "iat": 1790000000,
  "exp": 1790003600
}
```

Some common claims:

| Claim | Meaning |
|---|---|
| sub | Subject / user identifier |
| iss | Issuer |
| aud | Audience |
| iat | Issued-at time |
| exp | Expiration time |
| nbf | Not valid before |
| jti | Token identifier |

Custom claims can also be included.

For example:

```json
{
  "department": "FINANCE",
  "role": "MANAGER"
}
```

---

## 5. Important — JWT Payload Is Not Encrypted

This is one of the most important JWT interview points.

> A normal signed JWT is **encoded**, not **encrypted**.

Anyone who possesses the token can generally decode the header and payload.

For example:

```
JWT
 ↓
Decode
 ↓
Header + Payload
```

Therefore, **do not put sensitive secrets in the JWT payload**.

**Bad example:**

```json
{
  "password": "MyPassword123",
  "creditCard": "...."
}
```

**Good example:**

```json
{
  "sub": "12345",
  "role": "CUSTOMER",
  "scope": "orders:read"
}
```

The signature protects against unauthorized modification; it doesn't make the payload confidential.

---

## 6. JWT Signature

The signature is the most important security component.

Conceptually:

```
Header
   +
Payload
   |
   v
Signing Algorithm
   +
Secret / Private Key
   |
   v
Signature
```

For an HMAC algorithm such as **HS256**:

```
HMAC(
    base64url(header) + "." + base64url(payload),
    secret
)
```

For an asymmetric algorithm such as **RS256**:

```
Header + Payload
       |
       v
Private Key
       |
       v
Signature
```

The server can verify the signature using the corresponding public key.

---

## 7. How JWT Authentication Works

Let's go through a complete login example.

### Step 1 — User Logs In

Client sends:

```json
POST /login
{
  "username": "kaushal",
  "password": "MyPassword123"
}
```

The Authentication Server verifies the credentials.

```
Username + Password
        ↓
    User Database
        ↓
      Valid?
        ↓
       YES
```

---

## 8. Step 2 — Server Creates JWT

The authentication server creates claims:

```json
{
  "sub": "12345",
  "role": "CUSTOMER",
  "scope": "orders:read orders:create",
  "exp": 1790003600
}
```

Then it signs the token.

```
Header + Payload
       ↓
Signing algorithm
       ↓
Private/Secret Key
       ↓
Signature
       ↓
JWT
```

The client receives:

```
Access Token = eyJhbGciOi...
```

---

## 9. Step 3 — Client Sends JWT

The client now wants to access orders.

```
GET /api/orders
Authorization: Bearer eyJhbGciOi...
```

The standard HTTP pattern is:

```
Authorization: Bearer <JWT>
```

---

## 10. Step 4 — API Validates JWT

The API receives the token.

It performs several checks.

```
                  JWT
                   |
                   v
          ┌─────────────────┐
          │ Validate Token  │
          └────────┬────────┘
                   |
       +-----------+-----------+
       |           |           |
       v           v           v
   Signature     Expiry      Issuer
     valid?       valid?      valid?
       |           |           |
       +-----------+-----------+
                   |
                   v
               Valid JWT
```

The API may check:

### 1. Signature

Was the token modified?

```
Signature valid? ✅
```

### 2. Expiration

```
exp > current time?
```

If expired:

```
❌ Token expired
```

### 3. Issuer

```
iss = trusted-auth-server?
```

### 4. Audience

```
aud = this API?
```

### 5. Other claims

Depending on the application:

- scope
- role
- tenant
- permissions

---

## 11. Step 5 — Authorization

After validating the JWT, the API identifies the user:

```
sub = 12345
```

Then authorization happens.

Suppose the endpoint requires:

```
orders:read
```

JWT contains:

```
scope = orders:read orders:create
```

Therefore:

```
Authentication ✅
JWT validation  ✅
Authorization  ✅
```

Request succeeds:

```
200 OK
```

---

## 12. What If the JWT Is Invalid?

Suppose someone changes:

```json
{
  "role": "CUSTOMER"
}
```

to:

```json
{
  "role": "ADMIN"
}
```

The payload has changed.

Therefore the signature no longer matches.

**Original:**

```
Header + Payload + Private Key
              ↓
          Signature A
```

**Modified:**

```
Header + Modified Payload
              ↓
          Signature B
```

The server sees:

```
Signature A ≠ Signature B
```

Therefore:

```
❌ Invalid JWT
```

The attacker cannot simply modify the payload and become an admin if the signing key is properly protected.

---

## 13. JWT Architecture

A typical production architecture looks like this:

```
                         ┌──────────────────┐
                         │ Identity Provider│
                         │   / Auth Server  │
                         └────────┬─────────┘
                                  |
                            Login / Token
                                  |
                                  v
                              Client
                                  |
                                  | JWT
                                  v
                           API Gateway
                                  |
                    ┌─────────────┴─────────────┐
                    |                           |
                    v                           v
             Order Service              Payment Service
                    |                           |
                    | Validate JWT              |
                    v                           v
              Authorization              Authorization
```

- The Authentication Server owns the signing key.
- APIs verify the token.

---

## 14. Symmetric vs Asymmetric JWT Signing

This is especially important for microservices.

### HS256 — Symmetric

Same secret is used for signing and verification.

```
Auth Server
     |
     | Secret Key
     v
   Sign JWT
     |
     v
   JWT
     |
     v
Service
     |
     | Same Secret Key
     v
 Verify JWT
```

**Problem:**

Every service that verifies tokens needs the secret.

```
Secret
  ↓
Service A
Service B
Service C
Service D
```

If one service leaks the secret, an attacker may be able to create valid tokens.

---

## 15. RS256 — Asymmetric

With asymmetric cryptography:

```
Private Key → Sign
Public Key  → Verify
```

Architecture:

```
                 Auth Server
                     |
               Private Key
                     |
                     v
                 Sign JWT
                     |
                     v
                    JWT
                     |
       +-------------+-------------+
       |             |             |
       v             v             v
   Service A     Service B     Service C
       |             |             |
       +-------------+-------------+
                     |
                Public Key
                     |
                     v
                  Verify
```

This is very useful in distributed systems.

- The authentication server keeps the private key secret.
- Services only need the public key.

---

## 16. Public Key Distribution

In a real system, services shouldn't necessarily have a public key hardcoded forever.

An Identity Provider can expose public keys through a **JWKS endpoint**.

Conceptually:

```
Identity Provider
       |
       | JWKS
       v
Public Keys
       |
       v
API / Resource Servers
```

Services can retrieve and cache the public keys.

If keys are rotated:

```
Old Key
   ↓
New Key
```

services can obtain the new public key.

---

## 17. JWT Expiration

JWTs commonly have an expiration claim:

```json
{
  "sub": "12345",
  "exp": 1790003600
}
```

The API checks:

```
Current Time < exp
```

If:

```
Current Time > exp
```

then:

```
❌ Token expired
```

**Short-lived** access tokens are generally preferred.

For example:

```
Access Token
    |
    | 15 minutes
    v
Expires
```

---

## 18. Access Token + Refresh Token

A common authentication architecture uses two tokens:

```
Login
  |
  +----------------+
  |                |
  v                v
Access Token    Refresh Token
(short-lived)  (longer-lived)
```

### Access Token

Used for APIs:

```
Authorization: Bearer <access-token>
```

Example lifetime:

```
15 minutes
```

### Refresh Token

Used to obtain a new access token:

```
Refresh Token
      |
      v
Auth Server
      |
      v
New Access Token
```

This allows access tokens to remain short-lived.

---

## 19. JWT Is Stateless

This is one of the biggest advantages of JWT.

**With session authentication:**

```
Client
  |
  | Session ID
  v
Server
  |
  v
Redis
  |
  v
Session
```

The server needs to look up the session.

**With a self-contained signed JWT:**

```
Client
  |
  | JWT
  v
Server
  |
  | Verify signature
  v
Identity / Claims
```

There may be no server-side session lookup for ordinary validation.

This makes horizontal scaling easier:

```
             Load Balancer
             /     |     \
            v      v      v
        Server1 Server2 Server3
            |      |      |
            +------+------+
                   |
              Public Key
```

Each server can independently validate the token.

---

## 20. But JWT Is Not Always Stateless

This is an important advanced point.

People often say:

> "JWT means completely stateless authentication."

Not necessarily.

You may introduce server-side state for:

- Token revocation
- Refresh tokens
- Logout
- Blacklisting
- Session management
- Key rotation
- Risk detection

For example:

```
JWT
 |
 | jti = abc123
 v
Revocation Store
 |
 +---- revoked → reject
```

So JWT can reduce server-side session state, but it doesn't guarantee that the entire authentication system is stateless.

---

## 21. JWT Logout Problem

Consider:

```
JWT expires in 1 hour
```

User clicks Logout after 5 minutes.

**With session authentication:**

```
Delete Session
      ↓
Immediately invalid
```

**With a self-contained JWT:**

```
JWT
 |
 | valid until expiration
 v
Still valid
```

This creates a challenge.

Possible solutions include:

### Short-lived access tokens

```
Access token → 5–15 minutes
```

### Refresh-token revocation

Revoke the refresh token so no new access tokens can be issued.

### Token blacklist / denylist

Maintain revoked token identifiers.

### Token introspection

Ask the authorization server whether the token is still valid.

Each solution introduces different trade-offs.

---

## 22. JWT Security Best Practices

### 1. Always use HTTPS

Never send access tokens over unencrypted HTTP.

```
HTTPS ✅
HTTP  ❌
```

### 2. Keep access tokens short-lived

For example:

```
5–15 minutes
```

depending on the application's risk profile.

### 3. Protect signing keys

The private signing key should be tightly protected.

```
Private Key
    |
    +---- Authentication Server only
```

### 4. Validate all important claims

Don't just verify the signature.

Validate:

- signature
- exp
- iss
- aud
- nbf (when applicable)

### 5. Don't put secrets in payload

Remember:

```
JWT Payload ≠ encrypted storage
```

### 6. Don't blindly trust roles from untrusted tokens

Only trust claims after the token has been properly validated and comes from a trusted issuer.

---

## 23. JWT vs Session-Based Authentication

This is a very common interview question.

| Feature | Session | JWT |
|---|---|---|
| Client stores | Session ID | JWT |
| Server state | Session required | Often not required |
| Server lookup | Usually yes | Usually no |
| Scaling | Shared session store often needed | Easier |
| Revocation | Easy | More difficult |
| Logout | Easy | Requires strategy |
| Token contains claims | No, usually | Yes |
| Microservices | Possible | Very common |
| Network size | Small cookie/session ID | JWT can be larger |
| Best use | Traditional web apps | APIs / distributed systems |

---

## 24. JWT vs OAuth 2.0

Another important interview question:

> JWT and OAuth are not competitors.

They are different things.

```
OAuth 2.0
    ↓
Authorization framework
```

JWT:

```
JWT
 ↓
Token format
```

OAuth 2.0 can use JWT-formatted access tokens, but an OAuth access token does not have to be a JWT.

For authentication built on OAuth 2.0, **OpenID Connect (OIDC)** adds the identity layer.

Think:

```
OAuth 2.0
    |
    | Authorization framework
    |
    +---- Can use JWT tokens


OpenID Connect
    |
    | Authentication / identity layer
    |
    +---- Built on OAuth 2.0
```

---

## 25. JWT in Microservices

Suppose we have:

```
                     Client
                        |
                        | JWT
                        v
                  API Gateway
                        |
          +-------------+-------------+
          |             |             |
          v             v             v
       User          Order         Payment
      Service        Service        Service
```

The authentication server signs the JWT.

Each service validates it.

```
Auth Server
     |
     | Private Key
     v
   JWT
     |
     +----------+----------+
     |          |          |
     v          v          v
  User       Order      Payment
 Service     Service     Service
     |          |          |
     +----------+----------+
                |
          Public Key
```

This avoids making every request go back to the authentication server just to validate a signed JWT.

---

## 26. Complete Real-World Flow

Let's put everything together.

```
                         ① Login
Client --------------------------------> Identity Provider
                                           |
                                           | Verify credentials
                                           v
                                        User DB
                                           |
                                           | Valid
                                           v
                                    Create + Sign JWT
                                           |
                                  Private Signing Key
                                           |
                                           v
Client <-------------------------------- JWT
  |
  | ② API Request
  | Authorization: Bearer JWT
  v
API Gateway
  |
  | ③ Validate JWT
  |    - Signature
  |    - exp
  |    - iss
  |    - aud
  v
Order Service
  |
  | ④ Authorization
  |    scope = orders:read?
  v
Order Database
  |
  v
Response
```

---

## 27. Authentication + JWT + Authorization

The complete mental model is:

```
                  LOGIN
                    |
                    v
            Authentication
                    |
              "Who are you?"
                    |
                    v
               JWT Issued
                    |
                    v
               API Request
                    |
                    v
             JWT Validation
                    |
          +---------+---------+
          |                   |
       Invalid              Valid
          |                   |
         401                  v
                      Identify User
                            |
                            v
                       Authorization
                            |
                     "What can you do?"
                            |
                 +----------+----------+
                 |                     |
               Allow                  Deny
                 |                     |
                 v                     v
             Resource                 403
```

---

## 28. Interview Answer

If an interviewer asks:

> "What is JWT and how does it work?"

A strong answer for a senior developer would be:

> JWT, or JSON Web Token, is a compact, URL-safe token format used to securely represent claims between parties. In an authentication system, after the user successfully authenticates, the authentication server creates a JWT containing claims such as user ID, issuer, audience, scopes, and expiration time, and signs it using a secret or private key. The client sends the JWT with subsequent API requests, typically using the `Authorization: Bearer` header. The API validates the signature and important claims such as expiration, issuer, and audience. If the token is valid, the API identifies the user and performs authorization based on roles, scopes, permissions, and resource ownership. JWTs are especially useful in distributed and microservice architectures because services can often validate the token locally without maintaining a centralized session.

---

## 29. What You Should Know for System Design Interviews

For your 10-year experience / system-design preparation, learn JWT in this order:

```
JWT
 │
 ├── Header
 ├── Payload
 ├── Signature
 │
 ├── Encoding vs Encryption
 ├── HS256
 ├── RS256
 ├── Public / Private Keys
 ├── JWKS
 │
 ├── Access Token
 ├── Refresh Token
 ├── Token Expiration
 ├── Token Revocation
 ├── Token Rotation
 │
 ├── OAuth 2.0
 ├── OpenID Connect
 ├── SSO
 │
 ├── JWT in API Gateway
 ├── JWT in Microservices
 ├── Service-to-Service Authentication
 └── JWT Security / Common Attacks
```

---

## One final mental model

```
JWT = Signed Identity/Authorization Claims

             Header
                +
             Payload
                +
             Signature
                |
                v
               JWT
                |
                v
        Authorization Header
                |
                v
          API / Service
                |
         Verify Signature
                |
       Validate exp / iss / aud
                |
                v
          Identify User
                |
                v
          Authorization
                |
          +-----+-----+
          |           |
        ALLOW        DENY
          |           |
          v           v
       Resource      403
```

> **The key point:** JWT is a token format, not an authentication protocol by itself. It is commonly used as a signed access token within authentication/authorization architectures such as OAuth 2.0 and OpenID Connect.

