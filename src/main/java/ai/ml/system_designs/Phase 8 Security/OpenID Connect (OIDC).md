# OpenID Connect (OIDC)

**OpenID Connect (OIDC)** is an authentication protocol built on top of OAuth 2.0.

The simplest way to remember it:

> OAuth 2.0 → "What can this application access?"
> OIDC → "Who is the user?"

OAuth 2.0 by itself is mainly about **authorization**. OIDC adds a standardized way for an application to **authenticate the user** and receive identity information.

---

## 1. Why do we need OpenID Connect?

Suppose you have an application:

```
                    ┌──────────────────┐
                    │   Your App       │
                    │   shopping.com   │
                    └────────┬─────────┘
                             │
                       Login with Google
                             │
                             ▼
                    ┌──────────────────┐
                    │ Google / IdP     │
                    │                  │
                    │ Authentication   │
                    └──────────────────┘
```

Your application wants to know:

```
Who is this user?
-----------------
User ID: 12345
Email: user@example.com
Name: John
```

OAuth 2.0 alone doesn't standardize this identity information.

OIDC solves this.

It provides an **ID Token** containing claims about the authenticated user.

---

## 2. OAuth 2.0 vs OIDC

This is one of the most important interview concepts.

| OAuth 2.0 | OpenID Connect |
|---|---|
| Authorization | Authentication + Authorization |
| What can you access? | Who is the user? |
| Uses Access Token | Uses Access Token + ID Token |
| API access | User identity |
| OAuth framework | Identity layer on OAuth 2.0 |

Think:

```
OAuth 2.0
     │
     │ Authorization
     ▼
Access Token
     │
     ▼
"Can I access this API?"
```

Whereas:

```
OpenID Connect
     │
     ├──────────────► Access Token
     │                 │
     │                 ▼
     │              API access
     │
     └──────────────► ID Token
                       │
                       ▼
                  User identity
```

---

## 3. Main components

OIDC has several important actors.

```
┌──────────────────────┐
│       User           │
│   Resource Owner     │
└──────────┬───────────┘
           │
           ▼
┌──────────────────────┐
│      Client App      │
│   Web / Mobile App   │
└──────────┬───────────┘
           │
           │ OIDC
           ▼
┌──────────────────────┐
│ Authorization Server │
│ / Identity Provider  │
│                      │
│ Google / Azure AD /  │
│ Keycloak / Okta etc. │
└──────────┬───────────┘
           │
           │ Access Token
           ▼
┌──────────────────────┐
│    Resource Server   │
│       Your API       │
└──────────────────────┘
```

### Client

Your application.

Examples:

- Web application
- Mobile application
- SPA
- Backend application

### Identity Provider

The system responsible for authenticating the user.

Examples:

- Google
- Microsoft Entra ID
- Keycloak
- Okta
- Auth0

### Resource Server

Your backend APIs.

For example:

```
GET /orders
GET /profile
POST /payments
```

---

## 4. The most important concept: ID Token

OIDC introduces the **ID Token**.

Usually it is a JWT.

Example:

```
eyJhbGciOiJSUzI1NiIs...
.
eyJzdWIiOiIxMjM0NSIs...
.
signature
```

Decoded conceptually:

```json
{
  "iss": "https://identity.example.com",
  "sub": "12345",
  "aud": "shopping-app",
  "email": "john@example.com",
  "name": "John",
  "iat": 1768000000,
  "exp": 1768003600
}
```

Important claims include:

### iss

Issuer.

```
https://identity.example.com
```

Tells the application:

> Who issued this token?

### sub

Subject.

```
12345
```

This is the unique identifier of the user within that issuer/client context.

Applications should generally use `sub` as the stable user identifier rather than relying on email.

### aud

Audience.

```
shopping-app
```

Tells you:

> Who is this ID token intended for?

The application must verify that the token was issued for itself.

### exp

Expiration time.

```
1768003600
```

Prevents an ID token from being valid forever.

### iat

Issued-at time.

```
1768000000
```

---

## 5. How OIDC works internally

The most common modern flow is:

> **OIDC Authorization Code Flow + PKCE**

Let's go step by step.

### Step 1: User opens your application

```
User
  │
  │ Open application
  ▼
Your Application
```

The application says:

```
Please login
```

User clicks:

```
Login with Google
```

---

## 6. Step 2: Application redirects to Identity Provider

Your application redirects the browser to the IdP.

Conceptually:

```
https://identity.example.com/authorize
    ?client_id=shopping-app
    &response_type=code
    &redirect_uri=https://shopping.com/callback
    &scope=openid profile email
    &state=xyz
    &code_challenge=abc
```

Notice:

```
scope=openid profile email
```

The most important one is:

```
openid
```

### openid scope

This tells the authorization server:

> This is an OpenID Connect authentication request.

Without the `openid` scope, you're generally doing OAuth rather than OIDC.

---

## 7. Step 3: User authenticates

The browser is now on the Identity Provider.

```
                 Identity Provider
                ┌───────────────────┐
                │                   │
User ──────────►│ Username          │
                │ Password          │
                │ MFA               │
                │                   │
                └───────────────────┘
```

The user may enter:

- username
- password
- OTP
- biometric
- security key

The important thing is:

> Your application does NOT see the user's password.

The IdP handles authentication.

---

## 8. Step 4: User gives consent

Depending on the request and provider:

```
Shopping App wants:

✓ Your profile
✓ Your email
✓ Basic identity information
```

User approves.

---

## 9. Step 5: Authorization Server returns authorization code

The IdP redirects the browser back:

```
https://shopping.com/callback?code=AUTH_CODE&state=xyz
```

Your application receives:

```
AUTH_CODE
```

Important:

> The authorization code is NOT the access token.

It is temporary and typically single-use.

---

## 10. Step 6: Application exchanges code for tokens

Your backend sends the code to the token endpoint.

```
Application
     │
     │ POST /token
     │
     │ code
     │ code_verifier
     ▼
Identity Provider
```

The IdP validates things such as:

- Is code valid?
- Is code unused?
- Is code expired?
- Is client correct?
- Is redirect URI correct?
- Does PKCE verifier match?

If everything is valid, the IdP returns tokens.

For example:

```json
{
  "access_token": "eyJ...",
  "id_token": "eyJ...",
  "refresh_token": "..."
}
```

---

## 11. Access Token vs ID Token

This is extremely important.

### ID Token

Used by the client application to understand:

> Who authenticated?

Example:

```json
{
  "sub": "12345",
  "email": "john@example.com",
  "name": "John"
}
```

### Access Token

Used to access an API/resource server.

```
Authorization: Bearer eyJ...
```

For example:

```
GET /orders
Authorization: Bearer ACCESS_TOKEN
```

The API validates the access token.

### Simple mental model

```
ID Token
   │
   ▼
"Who are you?"

Access Token
   │
   ▼
"What API are you allowed to access?"
```

> Do not use the ID token as an API access token merely because both may be JWTs.

---

## 12. Complete OIDC flow

```
                 ┌───────────────┐
                 │     User      │
                 └───────┬───────┘
                         │
                         │ Login
                         ▼
                 ┌───────────────┐
                 │   Client App  │
                 └───────┬───────┘
                         │
                         │ Authorization Request
                         │ scope=openid
                         ▼
              ┌──────────────────────┐
              │ Identity Provider    │
              │                      │
              │ Authentication       │
              │ MFA                  │
              │ Consent              │
              └──────────┬───────────┘
                         │
                         │ Authorization Code
                         ▼
                 ┌───────────────┐
                 │   Client App  │
                 └───────┬───────┘
                         │
                         │ Code + PKCE verifier
                         ▼
              ┌──────────────────────┐
              │ Identity Provider    │
              └──────────┬───────────┘
                         │
                         │ ID Token
                         │ Access Token
                         │ Refresh Token
                         ▼
                 ┌───────────────┐
                 │   Client App  │
                 └───────┬───────┘
                         │
                         │ Access Token
                         ▼
                 ┌───────────────┐
                 │ Resource/API  │
                 └───────────────┘
```

---

## 13. What happens when the API receives the Access Token?

Suppose:

```
GET /orders
Authorization: Bearer eyJ...
```

The API needs to validate the token.

If JWT:

```
              Access Token
                   │
                   ▼
        ┌─────────────────────┐
        │ Validate signature  │
        └──────────┬──────────┘
                   ▼
              Check exp
                   │
                   ▼
              Check iss
                   │
                   ▼
              Check aud
                   │
                   ▼
             Check scope
                   │
                   ▼
             Allow / Deny
```

For asymmetric signing such as RS256:

```
Identity Provider

Private Key
    │
    ▼
Sign Token
    │
    ▼
Access Token
```

API:

```
Access Token
    │
    ▼
Public Key
    │
    ▼
Verify Signature
```

The API doesn't need the private signing key.

---

## 14. Where does the public key come from?

OIDC providers normally expose a **JWKS endpoint**.

Conceptually:

```
Identity Provider
       │
       │ JWKS
       ▼
Public Keys
       │
       ▼
API Gateway / Services
```

For example:

```
kid = key-2026-01
alg = RS256
```

The `kid` in the JWT header tells the verifier which public key to use.

This also supports **key rotation**.

---

## 15. What is the UserInfo endpoint?

OIDC also defines a **UserInfo** endpoint.

The client can call:

```
GET /userinfo
Authorization: Bearer ACCESS_TOKEN
```

The Identity Provider can return claims such as:

```json
{
  "sub": "12345",
  "name": "John",
  "email": "john@example.com"
}
```

So you can think of:

```
ID Token
   │
   └── Identity information received during login

UserInfo endpoint
   │
   └── Identity information retrieved using Access Token
```

---

## 16. OIDC Discovery

OIDC also provides a **discovery** mechanism.

Instead of manually configuring every endpoint, the application can discover the provider's configuration.

Conceptually:

```
/.well-known/openid-configuration
```

It can provide information such as:

```
authorization_endpoint
token_endpoint
userinfo_endpoint
jwks_uri
issuer
```

So your application can learn:

- Where is login?
- Where do I exchange codes?
- Where is UserInfo?
- Where are public keys?
- Who is the issuer?

This is very useful in real systems.

---

## 17. Example: Login with Google

Imagine:

```
                User
                  │
                  │ Login
                  ▼
             Shopping App
                  │
                  │ Redirect
                  ▼
              Google
                  │
                  │ User Login
                  │
                  │ Authorization Code
                  ▼
             Shopping App
                  │
                  │ Code exchange
                  ▼
              Google
                  │
                  │ ID Token
                  │ Access Token
                  ▼
             Shopping App
```

The application can now know:

```
User authenticated successfully.

sub = 12345
email = user@example.com
name = John
```

The application creates or finds its own local user record:

```
users
--------------------------------
id       5001
provider google
subject  12345
email    user@example.com
```

This is a common production architecture.

---

## 18. OIDC and Single Sign-On

OIDC is frequently used for **SSO**.

Imagine a company has:

```
              Identity Provider
                     │
          ┌──────────┼──────────┐
          │          │          │
          ▼          ▼          ▼
       Payroll     Jira       GitHub
        App         App        App
```

User logs into the IdP once.

Then:

```
User
 │
 │ Login
 ▼
Identity Provider
 │
 ├────────► Application A
 │
 ├────────► Application B
 │
 └────────► Application C
```

The applications trust the same identity provider.

That's the foundation of many enterprise SSO systems.

---

## 19. OIDC vs SAML

You will frequently see these together in enterprise architecture.

| OIDC | SAML |
|---|---|
| Modern | Older but still heavily used |
| OAuth 2.0 based | XML based |
| JSON/JWT | XML assertions |
| Web/mobile/API friendly | Enterprise web SSO |
| Easier for modern applications | Common in legacy enterprise systems |

For new applications, OIDC is often preferred when the ecosystem supports it.

---

## 20. OIDC vs JWT

Another common interview question.

They are **not alternatives**.

```
OIDC
 │
 │ Protocol
 ▼
Defines authentication
 │
 ▼
ID Token
 │
 ▼
Often JWT
```

JWT is a **token format**.

OIDC is an **authentication protocol**.

Therefore:

```
OIDC ≠ JWT
```

Instead:

```
OIDC
  └── commonly uses JWT for ID Token
```

---

## 21. OIDC vs OAuth 2.0 vs JWT

Remember this table:

| Concept | Purpose |
|---|---|
| OAuth 2.0 | Authorization framework |
| OIDC | Authentication layer on OAuth 2.0 |
| JWT | Token format |
| Access Token | API authorization |
| ID Token | User authentication/identity |
| Refresh Token | Obtain new access tokens |

A very good interview answer is:

> OAuth 2.0 tells us what an application is allowed to access, OIDC tells us who the authenticated user is, and JWT is one possible format used to represent tokens such as ID tokens and access tokens.

---

## 22. Security considerations

In production, OIDC should be implemented carefully.

### HTTPS

Always use HTTPS.

```
HTTP ❌
HTTPS ✅
```

### PKCE

Use **Authorization Code + PKCE**.

```
code_challenge
       ↓
Authorization Server
       ↓
authorization code
       ↓
code_verifier
       ↓
Token endpoint
```

This prevents an intercepted authorization code from being useful without the verifier.

### State

Use:

```
state
```

to protect the authorization flow against CSRF/login-request attacks.

### Nonce

OIDC also uses:

```
nonce
```

to help protect against replay/substitution attacks involving ID tokens.

### Validate ID Token

The client should validate important claims:

- signature
- iss
- aud
- exp
- iat
- nonce

depending on the flow and implementation.

### Validate Access Token

The API should validate:

- signature
- issuer
- audience
- expiration
- scope

and any additional required claims.

---

## 23. Real-world microservices architecture

Suppose we have:

```
                    ┌──────────────┐
                    │    User      │
                    └──────┬───────┘
                           │
                           ▼
                    ┌──────────────┐
                    │ Web / Mobile │
                    └──────┬───────┘
                           │
                           │ OIDC Login
                           ▼
                    ┌──────────────┐
                    │ Identity     │
                    │ Provider     │
                    └──────┬───────┘
                           │
                     Access Token
                           │
                           ▼
                    ┌──────────────┐
                    │ API Gateway  │
                    └──────┬───────┘
                           │
              ┌────────────┼────────────┐
              │            │            │
              ▼            ▼            ▼
         User Service  Order Service Payment Service
              │            │            │
              └────────────┼────────────┘
                           │
                           ▼
                      Databases
```

The API Gateway might perform:

```
Token signature validation
       ↓
Issuer validation
       ↓
Audience validation
       ↓
Expiration validation
       ↓
Scope validation
```

Then services perform business/resource authorization.

For example:

```
Gateway:
    Is this token valid?

Order Service:
    Can this user access order #123?
```

That distinction is important in distributed systems.

---

## 24. Complete mental model

You can remember OIDC like this:

```
                   OIDC
                    │
                    ▼
             Authentication
                    │
                    ▼
             Identity Provider
                    │
             User authenticates
                    │
                    ▼
          Authorization Code
                    │
                    ▼
              Token Endpoint
                    │
          ┌─────────┴──────────┐
          │                    │
          ▼                    ▼
      ID Token            Access Token
          │                    │
          ▼                    ▼
    "Who is user?"       "Access API"
          │                    │
          │                    ▼
          │              Resource Server
          │
          ▼
      Client App
```

---

## 25. Senior-level interview answer

If an interviewer asks:

> "What is OpenID Connect and how does it work?"

You can answer:

> OpenID Connect is an identity/authentication layer built on top of OAuth 2.0. OAuth 2.0 primarily provides authorization, while OIDC standardizes user authentication and identity information.
>
> The common flow is Authorization Code Flow with PKCE. The client redirects the user to the Identity Provider, where the user authenticates. The IdP redirects the user back with a short-lived authorization code. The client exchanges that code at the token endpoint and receives an ID token and usually an access token. The ID token, commonly a JWT, contains identity claims such as `sub`, `iss`, `aud`, and `exp`. The access token is used to access protected APIs.
>
> The API validates the access token's signature and claims such as issuer, audience, expiration, and scopes. OIDC also provides discovery, JWKS, and UserInfo endpoints, which make provider configuration, key management, and identity retrieval standardized.

---

### One-line memory trick

```
OAuth 2.0 → Authorization
OIDC      → Authentication + Identity
JWT       → Token Format
```

