# OAuth 2.0 — What It Is and How It Works

**OAuth 2.0** is an authorization framework that allows one application to access a user's resources on another system without the user giving their password to that application.

The simplest definition:

> OAuth 2.0 = A standardized way to give an application limited access to a resource on behalf of a user.

For example, suppose your application wants to access a user's Google Calendar.

Instead of:

```
Your App
   |
   | Give me your Google username/password
   v
Google
```

OAuth 2.0 allows:

```
Your App
   |
   | I need calendar:read permission
   v
Google
   |
   | User approves
   v
Access Token
   |
   v
Your App
   |
   | Access Token
   v
Google Calendar API
```

The application never needs the user's Google password.

---

## 1. OAuth 2.0 Is About Authorization

This distinction is extremely important.

```
Authentication
    ↓
Who are you?

Authorization
    ↓
What are you allowed to access?
```

OAuth 2.0 primarily addresses:

```
Authorization
```

For user authentication/identity on top of OAuth 2.0, the standard commonly used is **OpenID Connect (OIDC)**.

So:

```
OAuth 2.0
    ↓
Authorization

OpenID Connect
    ↓
Authentication / Identity
    ↓
Built on OAuth 2.0
```

---

## 2. Real-World Example

Imagine you build:

```
MyApplication
```

and want to let users export their Google Drive files.

**Without OAuth:**

```
MyApplication
      |
      | Google username/password
      v
    Google
```

This is terrible because your application would have access to the user's password.

**With OAuth:**

```
MyApplication
      |
      | "I need drive.readonly"
      v
Google Authorization Server
      |
      | User approves
      v
Access Token
      |
      v
MyApplication
      |
      | Access Token
      v
Google Drive API
```

Now your application only has the permission it needs.

---

## 3. The Four Main OAuth Components

OAuth 2.0 defines several important roles.

### 1. Resource Owner

Usually the user.

```
User
```

The resource owner owns the data.

Example:

```
Google User
    ↓
Owns Google Drive files
```

### 2. Client

The application requesting access.

```
Your Web Application
Mobile App
Backend Application
```

Example:

```
MyApplication
```

### 3. Authorization Server

The server responsible for:

- authenticating the user
- obtaining consent
- issuing authorization codes
- issuing access tokens
- issuing refresh tokens

Example:

```
Google Authorization Server
```

### 4. Resource Server

The API containing the protected resource.

For example:

```
Google Drive API
```

So:

```
Resource Owner
       |
       | owns
       v
Resource
       ^
       |
Resource Server
       ^
       |
Access Token
       ^
       |
Client
       ^
       |
Authorization Server
```

---

## 4. Complete OAuth 2.0 Flow

The most important OAuth flow to understand today is **Authorization Code Flow**, especially with **PKCE** for public clients.

Simplified:

```
User
 |
 | 1. Login / Connect Google
 v
Client Application
 |
 | 2. Redirect
 v
Authorization Server
 |
 | 3. Authenticate + Consent
 v
User
 |
 | 4. Approve
 v
Authorization Server
 |
 | 5. Authorization Code
 v
Client
 |
 | 6. Exchange code
 v
Authorization Server
 |
 | 7. Access Token
 v
Client
 |
 | 8. Access Token
 v
Resource Server
 |
 | 9. Protected Resource
 v
Client
```

Let's break it down.

---

## 5. Step 1 — Client Wants Access

Suppose your application needs:

```
Google Drive → Read files
```

The application redirects the user to the authorization server.

Conceptually:

```
GET /authorize
```

with parameters such as:

```
client_id
redirect_uri
response_type=code
scope=drive.readonly
state=...
```

The user is sent to the authorization server.

---

## 6. Step 2 — User Authenticates

The authorization server asks the user to log in.

```
Google Login

Email: ********
Password: ********
```

Important:

> Your application never sees the Google password.

The password is entered directly into Google's authentication system.

```
Your Application
       |
       | Redirect
       v
Google
       |
       | User enters password
       v
Google Authentication
```

---

## 7. Step 3 — User Gives Consent

After authentication, the authorization server asks:

```
MyApplication wants:

✓ Read your Google Drive files

Allow?
```

The user chooses:

```
ALLOW
```

This is the **consent** step.

---

## 8. Step 4 — Authorization Code

After the user approves, the authorization server redirects the browser back to the application:

```
https://myapp.com/callback?code=ABC123
```

The important part is:

```
code=ABC123
```

This is called an **authorization code**.

It is generally:

> Short-lived and single-use.

It is **not** the access token.

This is a very important distinction.

```
Authorization Code
        ↓
Exchange
        ↓
Access Token
```

---

## 9. Step 5 — Client Exchanges the Code

The application sends the authorization code to the token endpoint.

Conceptually:

```
POST /token
```

with:

```
grant_type=authorization_code
code=ABC123
redirect_uri=...
client_id=...
```

For **confidential clients**, client authentication is also involved.

For **public clients** such as mobile apps and browser-based apps, **PKCE** is used to protect the authorization code exchange.

---

## 10. Step 6 — Authorization Server Issues Access Token

The authorization server verifies the request.

If everything is valid:

```json
{
  "access_token": "eyJ...",
  "token_type": "Bearer",
  "expires_in": 900,
  "scope": "drive.readonly"
}
```

The client now has:

```
Access Token
```

---

## 11. Step 7 — Client Calls API

The client calls the resource server:

```
GET /drive/files
Authorization: Bearer eyJ...
```

The resource server validates the access token.

```
Access Token
     |
     v
Validate
     |
     +---- Invalid → 401
     |
     +---- Valid
             |
             v
       Check scope
             |
             v
       drive.readonly?
             |
             +---- YES → Allow
             |
             +---- NO → 403
```

---

## 12. The Complete Flow

Here's the complete picture:

```
                 ┌──────────────────┐
                 │       User       │
                 └────────┬─────────┘
                          |
                          | 1. "Connect Google"
                          v
                 ┌──────────────────┐
                 │ Client App       │
                 └────────┬─────────┘
                          |
                          | 2. Redirect
                          v
                 ┌──────────────────┐
                 │ Authorization    │
                 │ Server           │
                 └────────┬─────────┘
                          |
                          | 3. Login
                          v
                       User
                          |
                          | 4. Consent
                          v
                 Authorization Server
                          |
                          | 5. Authorization Code
                          v
                    Client App
                          |
                          | 6. Exchange Code
                          v
                 Authorization Server
                          |
                          | 7. Access Token
                          v
                    Client App
                          |
                          | 8. Bearer Token
                          v
                 ┌──────────────────┐
                 │ Resource Server  │
                 │ / API            │
                 └────────┬─────────┘
                          |
                          | 9. Protected Resource
                          v
                       Client
```

---

## 13. What Is the Access Token?

The **access token** is the credential that the client uses to access protected APIs.

Example:

```
GET /api/orders
Authorization: Bearer ACCESS_TOKEN
```

The API checks:

- Is token valid?
- Does token have required scope?
- Has token expired?
- Was token issued by trusted authorization server?

Then it allows or denies the request.

---

## 14. What Is a Scope?

A **scope** represents what access the client is requesting.

For example:

```
orders:read
orders:create
orders:update
orders:delete
```

A client might request:

```
scope=orders:read orders:create
```

The user approves.

The resulting access token might allow:

```
orders:read      ✅
orders:create    ✅
orders:delete    ❌
```

So OAuth gives us **limited access**.

This is called **least privilege**.

---

## 15. Access Token vs Refresh Token

OAuth commonly uses two different tokens.

```
             Authorization Server
                      |
            +---------+---------+
            |                   |
            v                   v
      Access Token        Refresh Token
       short-lived        longer-lived
            |
            v
       Resource API
```

### Access Token

Used to call APIs.

```
Authorization: Bearer <access_token>
```

Usually short-lived.

For example:

```
15 minutes
```

### Refresh Token

Used to obtain a new access token.

```
Client
  |
  | Refresh Token
  v
Authorization Server
  |
  | New Access Token
  v
Client
```

The refresh token is generally **not** sent to the resource API.

---

## 16. Why Do We Need Refresh Tokens?

Imagine:

```
Access Token = valid for 15 minutes
```

After 15 minutes:

```
Access Token
     ↓
Expired
```

Instead of asking the user to log in again:

```
Refresh Token
      ↓
Authorization Server
      ↓
New Access Token
```

The user can continue using the application.

---

## 17. Authorization Code Flow + PKCE

For modern applications, you should understand **PKCE (Proof Key for Code Exchange)**.

The flow becomes:

```
Client
  |
  | Generate code_verifier
  |
  | Create code_challenge
  |
  v
Authorization Server
```

The client sends the `code_challenge` during authorization.

After authentication:

```
Authorization Server
       |
       | Authorization Code
       v
Client
       |
       | code + code_verifier
       v
Authorization Server
```

The authorization server verifies that the verifier matches the original challenge.

Then:

```
Access Token
```

is issued.

PKCE helps protect the authorization code from being stolen and redeemed by an attacker.

---

## 18. Why PKCE Is Important

Imagine an attacker obtains:

```
Authorization Code
```

Without additional protection, the attacker might try to exchange it for an access token.

With PKCE:

```
Authorization Request
       |
       | code_challenge
       v
Authorization Server
       |
       | code
       v
Client
       |
       | code + code_verifier
       v
Authorization Server
```

The attacker has the code but doesn't have the correct:

```
code_verifier
```

Therefore:

```
❌ Token exchange fails
```

---

## 19. What Is state?

OAuth requests commonly include a `state` value.

For example:

```
state=xyz123
```

The client stores the expected state and checks it when the browser returns.

Conceptually:

```
Client
 |
 | state = ABC
 v
Authorization Server
 |
 | callback + state=ABC
 v
Client
 |
 | Compare
 v
ABC == ABC
 |
 v
Continue
```

This helps protect against CSRF-related attacks involving the authorization flow.

---

## 20. Redirect URI

The client tells the authorization server where the authorization response should be sent.

For example:

```
https://myapp.com/oauth/callback
```

The authorization server should only redirect to registered/allowed URIs.

```
Registered redirect URI
        ↓
https://myapp.com/oauth/callback
```

An attacker should not be able to replace it with:

```
https://evil.com/callback
```

This is why redirect URI validation is important.

---

## 21. OAuth 2.0 Grant Types

Historically, OAuth 2.0 described several grant types.

For modern applications, the most important ones are:

### Authorization Code

Used for user-delegated access.

```
User
 ↓
Authorization Server
 ↓
Code
 ↓
Client
 ↓
Access Token
```

With PKCE, this is the standard choice for many public clients and is also useful for confidential clients.

### Client Credentials

Used for machine-to-machine authentication/authorization where there is no end-user delegation.

Example:

```
Order Service
      |
      | client credentials
      v
Authorization Server
      |
      | Access Token
      v
Payment Service
```

There is no user sitting there approving access.

---

## 22. Client Credentials Flow

Suppose:

```
Order Service
```

needs to call:

```
Payment Service
```

The Order Service authenticates itself with the authorization server.

```
Order Service
      |
      | client_id + client_secret
      v
Authorization Server
      |
      | Access Token
      v
Order Service
      |
      | Bearer Token
      v
Payment Service
```

This is very common in microservice architectures.

---

## 23. OAuth 2.0 and JWT

This is another extremely important distinction.

> OAuth 2.0 does not mean JWT.

OAuth 2.0 is:

```
Authorization Framework
```

JWT is:

```
Token Format
```

You can have:

```
OAuth 2.0
   |
   v
Access Token
   |
   v
JWT
```

But an OAuth access token can also be an opaque token:

```
OAuth 2.0
   |
   v
Opaque Access Token
```

So:

```
OAuth ≠ JWT
```

---

## 24. OAuth 2.0 + OpenID Connect

If you want login / identity, OAuth 2.0 alone isn't the complete solution.

**OpenID Connect** adds an identity layer.

```
OAuth 2.0
    ↓
Authorization

OpenID Connect
    ↓
Authentication + Identity
```

OIDC introduces an **ID Token**, which is commonly a JWT.

For example:

```
Access Token
    ↓
Used to call API

ID Token
    ↓
Contains identity information for the client
```

Don't confuse them.

```
Access Token → API authorization
ID Token     → Client authentication/identity information
```

---

## 25. OAuth 2.0 in Microservices

Imagine a large system:

```
                         User
                          |
                          v
                     Web / Mobile
                          |
                          v
                     API Gateway
                          |
          +---------------+---------------+
          |               |               |
          v               v               v
     User Service    Order Service   Payment Service
```

There is an Identity Provider:

```
                  Identity Provider
                         |
                         | Access Token
                         v
                        Client
```

The client sends:

```
Authorization: Bearer <access-token>
```

The gateway/services validate the token and enforce authorization.

Example:

**Token scopes:**

```
orders:read
orders:create
payments:read
```

**Order Service:**

```
orders:create
    ↓
Allowed
```

**Payment Service:**

```
payments:delete
    ↓
Not granted
    ↓
403 Forbidden
```

---

## 26. OAuth 2.0 vs Session Authentication

| Feature | Session | OAuth 2.0 |
|---|---|---|
| Primary purpose | Application login/session | Delegated authorization |
| Server session | Yes | Not necessarily |
| Access token | No | Yes |
| Third-party access | Not designed for it | Yes |
| Scopes | Usually no | Yes |
| Refresh token | No | Common |
| Microservices | Possible | Very common |
| SSO | Not inherently | Often used with OIDC/IdP |
| Standard protocol | Application-specific | Standardized framework |

---

## 27. OAuth 2.0 vs JWT

| OAuth 2.0 | JWT |
|---|---|
| Authorization framework | Token format |
| Defines flows | Defines token structure |
| Defines roles and interactions | Header + Payload + Signature |
| Defines scopes | Can contain scope claims |
| Can use JWT | Can be used outside OAuth |
| Access token can be opaque | JWT is self-contained/signed |

Think:

```
OAuth 2.0
    ↓
"How should authorization happen?"
```

and:

```
JWT
    ↓
"How is this token represented?"
```

---

## 28. Common OAuth 2.0 Security Problems

For senior/system-design interviews, know these.

### Authorization Code Interception

Mitigated with:

```
PKCE
```

### CSRF / Login Flow Attacks

Mitigated using:

```
state
```

### Redirect URI Manipulation

Use:

```
Exact registered redirect URIs
```

### Token Leakage

Use:

- HTTPS
- Short-lived access tokens
- Secure storage
- Limited scopes

### Over-Permission

Don't request:

```
scope = everything
```

Instead:

```
orders:read
```

if that's all the application needs.

---

## 29. Complete OAuth 2.0 Architecture

```
                         ┌─────────────────────┐
                         │        User         │
                         └──────────┬──────────┘
                                    |
                                    v
                         ┌─────────────────────┐
                         │   Client App        │
                         └──────────┬──────────┘
                                    |
                         ① Authorization Request
                                    |
                                    v
                         ┌─────────────────────┐
                         │ Authorization       │
                         │ Server / IdP        │
                         └──────────┬──────────┘
                                    |
                              ② Login
                                    |
                                    v
                                  User
                                    |
                              ③ Consent
                                    |
                                    v
                         Authorization Server
                                    |
                         ④ Authorization Code
                                    |
                                    v
                              Client App
                                    |
                         ⑤ Code + PKCE verifier
                                    |
                                    v
                         Authorization Server
                                    |
                           ⑥ Access Token
                                    |
                                    v
                              Client App
                                    |
                         ⑦ Bearer Access Token
                                    |
                                    v
                         ┌─────────────────────┐
                         │   Resource Server   │
                         │       / API        │
                         └──────────┬──────────┘
                                    |
                              ⑧ Validate
                                    |
                                    v
                             Protected Data
```

---

## 30. Interview Answer

If the interviewer asks:

> "What is OAuth 2.0 and how does it work?"

A strong senior-level answer is:

> OAuth 2.0 is an authorization framework that allows a client application to obtain limited access to protected resources on behalf of a resource owner without requiring the client to know the user's credentials. In the Authorization Code flow, the client redirects the user to an authorization server. The authorization server authenticates the user and obtains consent for requested scopes. It then redirects the user back with a short-lived authorization code. The client exchanges that code, using PKCE where applicable, for an access token. The client sends the access token to the resource server, typically as a Bearer token. The resource server validates the token and its scopes before returning the protected resource. Refresh tokens can be used to obtain new access tokens without requiring the user to log in again.

---

## 31. The Most Important Mental Model

Remember this:

```
                    OAuth 2.0
                        |
                        v
              "Give this application
               limited access"
                        |
                        v
                  User Login
                        |
                        v
                    Consent
                        |
                        v
               Authorization Code
                        |
                        v
                  Access Token
                        |
                        v
                 Protected API
                        |
                        v
                 Validate Token
                        |
                        v
                 Check Scope
                        |
                  +-----+-----+
                  |           |
                ALLOW        DENY
                  |           |
                  v           v
               Resource      403
```

### In one sentence:

> OAuth 2.0 allows a client to obtain limited, delegated access to protected resources through an authorization server, typically using an authorization code and access token rather than sharing the user's password.

---

## Next Steps

> For your system-design path, the next concept that ties Authentication + JWT + OAuth 2.0 together is **OpenID Connect (OIDC)**, followed by SSO, Identity Provider, Access Token vs ID Token vs Refresh Token, and OAuth 2.0 Authorization Code + PKCE flow.

