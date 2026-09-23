# Authentication — What It Is and How It Works

**Authentication** is the process of verifying who a user or system actually is.

In simple terms:

> Authentication = "Who are you?"

For example, when you log in to Gmail, your username/password are used to prove that you are the owner of that account.

---

## 1. Authentication vs Authorization

These two concepts are often confused.

| Concept | Question | Example |
|---|---|---|
| Authentication | Who are you? | Are you Kaushal? |
| Authorization | What are you allowed to do? | Can Kaushal delete this account? |

Typical flow:

```
User
  |
  | 1. Login
  v
Authentication
  |
  | "Who are you?"
  v
Identity verified
  |
  v
Authorization
  |
  | "What can you access?"
  v
Resource
```

---

## 2. How Authentication Works

A very simple username/password authentication flow looks like this:

```
             Login Request
User ------------------------------> Server
                                      |
                                      | Check credentials
                                      v
                                  User Database
                                      |
                                      | Password matches?
                                      v
                              Authentication Success
                                      |
                                      | Generate session/token
                                      v
User <----------------------------- Server
             Token / Session
```

Let's go step by step.

---

## 3. Step 1 — User Registers

Suppose a user creates an account:

```
Username: kaushal
Password: MyPassword123
```

The application should **never** store the plain-text password.

Instead, it generates a password hash.

```
MyPassword123
       |
       v
   Password Hash
       |
       v
$2a$10$....
```

Database:

```
users

+----+----------+----------------+
| id | username | password_hash  |
+----+----------+----------------+
| 1  | kaushal  | $2a$10$......  |
+----+----------+----------------+
```

The important point is:

```
Plain Password ❌
Password Hash   ✅
```

---

## 4. Step 2 — User Logs In

The user sends:

```
POST /login
```

with:

```json
{
  "username": "kaushal",
  "password": "MyPassword123"
}
```

The request should travel over **HTTPS**, not plain HTTP.

```
Client
   |
   | HTTPS
   v
Server
```

HTTPS protects credentials while they are traveling across the network.

---

## 5. Step 3 — Server Finds the User

The server searches the database:

```sql
SELECT *
FROM users
WHERE username = 'kaushal';
```

It gets:

```
username       = kaushal
password_hash  = $2a$10$....
```

---

## 6. Step 4 — Password Verification

The server does **not** simply compare:

```
"MyPassword123" == "$2a$10$..."
```

Instead, the password hashing algorithm verifies the supplied password against the stored hash.

Conceptually:

```
User Password
     |
     v
Password verification
     |
     v
Stored password hash
     |
     v
Match?
```

If it matches:

```
Authentication SUCCESS
```

Otherwise:

```
Authentication FAILED
```

---

## 7. What Happens After Successful Authentication?

The server needs some way to remember that the user has authenticated.

There are two major approaches:

### Session-based authentication

```
User
 |
 | username/password
 v
Server
 |
 | create session
 v
Session Store
 |
 | sessionId
 v
User
```

The browser receives something like:

```
SESSION_ID=abc123
```

Usually this is stored in a secure cookie.

For subsequent requests:

```
GET /profile

Cookie: SESSION_ID=abc123
```

The server checks:

```
SESSION_ID
     |
     v
Session Store
     |
     v
User = Kaushal
```

Therefore the server knows who is making the request.

---

## 8. Token-Based Authentication

Modern APIs frequently use **token-based authentication**.

For example:

```
User
 |
 | username + password
 v
Authentication Server
 |
 | Generate access token
 v
Client
```

The client receives:

```
Access Token
```

For example, a JWT might conceptually look like:

```
eyJhbGciOiJIUzI1NiJ9.
eyJzdWIiOiIxMjMifQ.
signature
```

The client then sends:

```
GET /api/orders
Authorization: Bearer <access-token>
```

The server validates the token.

```
Request
   |
   v
Extract token
   |
   v
Validate token
   |
   +---- Invalid ----> 401 Unauthorized
   |
   +---- Valid
          |
          v
       Identify user
          |
          v
       Authorization
          |
          v
       Access resource
```

---

## 9. JWT Authentication

A common token format is **JWT — JSON Web Token**.

A JWT has three parts:

```
HEADER.PAYLOAD.SIGNATURE
```

For example:

```
xxxxx.yyyyy.zzzzz
```

### Header

Contains information such as the signing algorithm:

```json
{
  "alg": "RS256",
  "typ": "JWT"
}
```

### Payload

Contains claims:

```json
{
  "sub": "12345",
  "username": "kaushal",
  "role": "USER",
  "exp": 1790000000
}
```

### Signature

The signature allows the server to detect whether the token has been modified.

Conceptually:

```
Header + Payload
       |
       v
Signing Algorithm + Secret/Private Key
       |
       v
Signature
```

The server verifies that signature.

---

## 10. Authentication with OAuth 2.0 / OpenID Connect

In large systems, applications often don't want to implement login themselves.

Instead, they use an **Identity Provider (IdP)** such as:

- Google
- Microsoft Entra ID
- Okta
- Auth0
- Keycloak

A simplified flow:

```
             1. Login
User ----------------------> Application
                              |
                              | Redirect
                              v
                         Identity Provider
                              |
                              | Login
                              v
                         Verify User
                              |
                              | Authorization Code
                              v
                         Application
                              |
                              | Exchange code
                              v
                         Identity Provider
                              |
                              | Access Token
                              v
                         Application
                              |
                              v
                            User
```

This is especially common in enterprise systems.

---

## 11. Multi-Factor Authentication

Authentication doesn't have to rely only on passwords.

There are several authentication factors.

### Something you know

- Password
- PIN
- Security answer

### Something you have

- Mobile phone
- Security key
- Authenticator app

### Something you are

- Fingerprint
- Face
- Iris

**MFA** combines multiple factors.

For example:

```
Username + Password
        +
OTP
        |
        v
Authentication successful
```

So even if someone steals your password, they may still be unable to log in.

---

## 12. Complete Real-World Authentication Flow

Consider an e-commerce application.

```
                    ┌──────────────┐
                    │    User      │
                    └──────┬───────┘
                           │
                           │ HTTPS
                           │ username/password
                           v
                    ┌──────────────┐
                    │ API Gateway  │
                    └──────┬───────┘
                           │
                           v
                    ┌──────────────┐
                    │ Auth Service  │
                    └──────┬───────┘
                           │
                           │ Verify password
                           v
                    ┌──────────────┐
                    │ User DB      │
                    └──────┬───────┘
                           │
                           │ Valid
                           v
                    ┌──────────────┐
                    │ Token /      │
                    │ Session      │
                    └──────┬───────┘
                           │
                           v
                    ┌──────────────┐
                    │    Client    │
                    └──────┬───────┘
                           │
                           │ Authorization: Bearer token
                           v
                    ┌──────────────┐
                    │ API Gateway  │
                    └──────┬───────┘
                           │
                           │ Validate token
                           v
                    ┌──────────────┐
                    │ Order Service│
                    └──────────────┘
```

---

## 13. Authentication in Microservices

This becomes particularly important in microservices.

Suppose we have:

```
                    API Gateway
                         |
        +----------------+----------------+
        |                |                |
        v                v                v
   User Service     Order Service    Payment Service
```

Instead of every service implementing username/password authentication, an Identity Provider / Authentication Service can authenticate the user.

```
User
 |
 | Login
 v
Identity Provider
 |
 | Access Token
 v
Client
 |
 | Bearer Token
 v
API Gateway
 |
 | Validate token
 v
Microservices
```

The token can contain identity information such as:

```
userId = 123
role   = CUSTOMER
scope  = orders:read
```

Services can then perform authorization based on those claims.

---

## 14. Authentication vs Authorization in a Request

Suppose:

```
GET /api/orders/123
Authorization: Bearer eyJ...
```

The system might perform:

### Authentication

```
Is this token valid?
        |
        v
Who is the user?
        |
        v
User ID = 12345
```

### Authorization

```
Can user 12345 access order 123?
        |
        v
YES
```

Then:

```
HTTP 200 OK
```

If the token is invalid:

```
HTTP 401 Unauthorized
```

If the user is authenticated but doesn't have permission:

```
HTTP 403 Forbidden
```

A useful rule:

```
401 = "I don't know/accept who you are."

403 = "I know who you are, but you aren't allowed."
```

---

## 15. Important Authentication Concepts

For system design interviews, understand these well:

```
Authentication
│
├── Username / Password
│
├── Password Hashing
│
├── Session-based Authentication
│
├── Token-based Authentication
│
├── JWT
│
├── OAuth 2.0
│
├── OpenID Connect
│
├── Identity Provider
│
├── Access Token
│
├── Refresh Token
│
├── MFA
│
├── SSO
│
├── Cookies
│
├── HTTPS / TLS
│
└── Authentication vs Authorization
```

---

## 16. Authentication vs SSO vs OAuth vs JWT

These are related but not the same thing.

| Term | What it is |
|---|---|
| Authentication | Verifying identity |
| Authorization | Checking permissions |
| Session | Server-side login state |
| JWT | A token format |
| OAuth 2.0 | Authorization framework |
| OpenID Connect | Authentication layer built on OAuth 2.0 |
| SSO | Login once and access multiple applications |
| MFA | Multiple authentication factors |
| Identity Provider | System responsible for managing identities/login |

---

## 17. Interview Answer

If an interviewer asks:

> "What is authentication and how does it work?"

A good answer is:

> Authentication is the process of verifying the identity of a user or system. In a typical web application, the client sends credentials such as username and password over HTTPS. The authentication service retrieves the user's stored password hash and securely verifies the supplied password. If authentication succeeds, the server creates a session or issues an access token such as a JWT. The client sends the session cookie or access token with subsequent requests. The server validates it, identifies the user, and then performs authorization to determine whether that user has permission to access the requested resource.

---

## The key mental model

```
Authentication
      ↓
"Who are you?"
      ↓
Verify identity
      ↓
Session / Access Token
      ↓
Every subsequent request
      ↓
Validate identity
      ↓
Authorization
      ↓
"What are you allowed to do?"
      ↓
Access Resource
```

---

## Next Steps

> For system design, the next concepts to learn after this are **Authorization, OAuth 2.0, OpenID Connect, JWT, Access Token vs Refresh Token, Session-based vs Token-based Authentication, and SSO**.

