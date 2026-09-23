# Session-Based Authentication — What It Is and How It Works

**Session-based authentication** is an authentication mechanism where the server maintains the user's login state in a session, and the client sends a session ID with every subsequent request.

The core idea is:

> The server remembers who you are, and the client carries only a session ID.

---

## 1. Simple Example

Suppose you log in to an e-commerce website.

```
Username: kaushal
Password: ********
```

The server verifies your credentials.

Instead of sending your username/password with every request, the server creates a session:

```
Session ID = abc123xyz
User        = Kaushal
```

The browser receives the session ID, usually in a cookie:

```
Set-Cookie: SESSION_ID=abc123xyz
```

For the next request:

```
GET /profile
Cookie: SESSION_ID=abc123xyz
```

The server looks up:

```
abc123xyz
      ↓
Session Store
      ↓
User = Kaushal
```

Therefore, the server knows who is making the request.

---

## 2. How Session-Based Authentication Works

The complete flow is:

```
              ① Login
Client ----------------------> Server
  |                              |
  | username + password          |
  |                              v
  |                         Verify credentials
  |                              |
  |                              v
  |                         Create Session
  |                              |
  |       ② Session ID           |
  | <----------------------------|
  |                              |
  |                              |
  | ③ Request + Session ID       |
  | ---------------------------> |
  |                              |
  |                         Find Session
  |                              |
  |                              v
  |                         Identify User
  |                              |
  |                              v
  |                         Process Request
  |                              |
  | ④ Response                   |
  | <----------------------------|
```

---

## 3. Step 1 — User Logs In

Client sends:

```
POST /login
Content-Type: application/json
{
  "username": "kaushal",
  "password": "MyPassword123"
}
```

The server verifies the credentials.

The password should be stored as a secure hash, not plain text.

```
Database

username = kaushal
password_hash = $2a$10$....
```

---

## 4. Step 2 — Server Creates a Session

After successful authentication, the server generates a unique session ID.

For example:

```
SESSION_ID = 8f72a9c1....
```

The server stores something like:

```
Session Store

8f72a9c1...  →  User ID: 12345
```

It may also store:

- Session ID
- User ID
- Created At
- Last Accessed
- Expiration
- IP information (if appropriate)
- Other session metadata

---

## 5. Step 3 — Server Sends Session ID to Browser

The server typically sends the session ID using a cookie:

```
HTTP/1.1 200 OK

Set-Cookie: SESSION_ID=8f72a9c1...; HttpOnly; Secure; SameSite=Lax
```

The browser stores the cookie.

Important:

> The cookie usually contains only the session identifier, not the entire user session.

For example:

```
Browser Cookie

SESSION_ID=8f72a9c1...
```

while the server maintains:

```
Server Session Store

8f72a9c1... → {
    userId: 12345,
    role: CUSTOMER
}
```

---

## 6. Step 4 — Subsequent Requests

Now the user requests:

```
GET /profile
```

The browser automatically sends:

```
Cookie: SESSION_ID=8f72a9c1...
```

The server receives the session ID.

It checks:

```
SESSION_ID
     |
     v
Session Store
     |
     v
User ID = 12345
     |
     v
Load User
     |
     v
Return Profile
```

The user doesn't have to log in again.

---

## 7. Where Is the Session Stored?

This is one of the most important system-design concepts.

### Small application

The session may be stored directly in the application server's memory:

```
┌───────────────┐
│ Application   │
│ Server        │
│               │
│ Session Store │
└───────────────┘
```

For example:

```
Session A → User 123
Session B → User 456
```

This works reasonably well for a single server.

But it creates a problem when we scale horizontally.

---

## 8. Problem With Multiple Servers

Suppose we have:

```
                 Load Balancer
                  /          \
                 /            \
                v              v
          Server A          Server B
          Session A         Session B
```

User logs in:

```
User
 |
 v
Load Balancer
 |
 v
Server A
 |
 | creates session
 v
Session A
```

Now the next request goes to Server B:

```
User
 |
 v
Load Balancer
 |
 v
Server B
 |
 | "Where is this session?"
 v
❌ Session not found
```

The user might appear to be logged out.

This is a major issue with in-memory sessions in horizontally scaled systems.

---

## 9. Solution — Shared Session Store

A common solution is to store sessions in a distributed/shared store such as **Redis**.

Architecture:

```
                     Load Balancer
                    /             \
                   v               v
            Application A    Application B
                   \               /
                    \             /
                     v           v
                     ┌───────────┐
                     │   Redis   │
                     │           │
                     │ Sessions  │
                     └───────────┘
```

Now:

```
User → Server A
          |
          | Create session
          v
        Redis
```

Later:

```
User → Server B
          |
          | SESSION_ID
          v
        Redis
          |
          v
      User Session
```

Both servers can access the same session.

---

## 10. Session-Based Authentication in a Distributed System

A typical production architecture might look like:

```
                         Internet
                            |
                            v
                     ┌─────────────┐
                     │Load Balancer│
                     └──────┬──────┘
                            |
              +-------------+-------------+
              |                           |
              v                           v
       ┌──────────────┐          ┌──────────────┐
       │ App Server 1 │          │ App Server 2 │
       └──────┬───────┘          └──────┬───────┘
              |                         |
              +------------+------------+
                           |
                           v
                    ┌─────────────┐
                    │    Redis    │
                    │   Sessions  │
                    └─────────────┘
```

The client has:

```
SESSION_ID=abc123
```

Redis has:

```
abc123 → {
    userId: 1001,
    role: CUSTOMER,
    expiresAt: ...
}
```

---

## 11. Session Lifecycle

A session typically has a lifecycle:

```
Login
  |
  v
Create Session
  |
  v
Active Session
  |
  +---- Request ----> Update last activity
  |
  +---- Timeout ----> Expire
  |
  +---- Logout -----> Destroy
```

For example:

```
Created:
10:00 AM

Last activity:
10:20 AM

Expiration:
11:20 AM
```

The exact expiration strategy depends on the application.

---

## 12. Logout

When the user clicks Logout:

```
POST /logout
Cookie: SESSION_ID=abc123
```

The server deletes or invalidates the session:

```
Redis

abc123 → User 1001
```

becomes:

```
abc123 → ❌ Deleted
```

The browser's cookie can also be cleared.

Next request:

```
GET /profile
Cookie: SESSION_ID=abc123
```

Server checks:

```
abc123
   ↓
Redis
   ↓
Not found
   ↓
401 Unauthorized
```

---

## 13. Session Cookie Security

Session IDs are sensitive.

If an attacker obtains:

```
SESSION_ID=abc123
```

they may be able to impersonate the user.

Therefore, session cookies should generally use security attributes such as:

```
HttpOnly
```

### HttpOnly

Prevents JavaScript from directly reading the cookie in normal browser contexts.

Helps reduce the impact of certain XSS attacks.

### Secure

```
Secure
```

Cookie is sent only over HTTPS.

### SameSite

For example:

```
SameSite=Lax
```

or:

```
SameSite=Strict
```

helps reduce certain cross-site request forgery scenarios.

Conceptually:

```
SESSION_ID=abc123

HttpOnly
Secure
SameSite=Lax
```

---

## 14. Session Fixation

Another important security issue is **session fixation**.

Imagine:

```
Before Login:
SESSION_ID=ABC
```

User logs in.

If the server continues using the same session:

```
After Login:
SESSION_ID=ABC
```

an attacker who somehow knows `ABC` may potentially abuse it.

A safer approach is:

```
Before Login:
SESSION_ID=ABC

Login successful

After Login:
SESSION_ID=XYZ
```

The server **regenerates** the session ID after authentication.

---

## 15. Session Timeout

Sessions should normally expire.

There are commonly two concepts:

### Idle timeout

Session expires after no activity for a period.

```
Last activity
     |
     | 30 minutes
     v
Session expires
```

### Absolute timeout

Session expires after a maximum lifetime regardless of activity.

```
Login
  |
  | 8 hours
  v
Session expires
```

Production systems may use both.

---

## 16. Session-Based vs Token-Based Authentication

This is an important system design interview question.

| Feature | Session-Based | Token-Based |
|---|---|---|
| State | Server maintains session | Token carries claims |
| Client stores | Session ID | Access token |
| Server lookup | Usually required | Often no session lookup |
| Scaling | Shared session store often needed | Easier horizontally |
| Revocation | Easy — delete session | More complicated |
| Typical web apps | Very common | Common |
| Microservices | Possible | Very common |
| Cookie usage | Common | Optional |
| Server-side state | Yes | Often less |

---

## 17. Session-Based Authentication vs JWT

### Session

```
Client
   |
   | SESSION_ID
   v
Server
   |
   v
Redis
   |
   v
User Session
```

The session ID means:

> "Look up my state on the server."

### JWT

```
Client
   |
   | JWT
   v
Server
   |
   | Validate signature
   v
User Identity / Claims
```

The JWT means:

> "Here is a signed representation of my identity/claims."

---

## 18. Why Use Session-Based Authentication?

Session-based authentication is particularly useful for traditional web applications.

Advantages:

### 1. Easy revocation

Delete the session:

```
Redis:
SESSION_ID → deleted
```

User is immediately logged out.

### 2. Sensitive information stays server-side

The browser only needs:

```
SESSION_ID
```

The actual session data remains on the server.

### 3. Easy session management

The server can control:

- expiration
- logout
- concurrent sessions
- session invalidation

---

## 19. Disadvantages

The biggest issue is **server-side state**.

Suppose:

```
10 Application Servers
```

All of them need access to sessions.

Therefore you may need:

```
Application Servers
       |
       v
Distributed Session Store
       |
       v
Redis Cluster
```

This introduces another distributed system dependency.

You also need to consider:

- Redis availability
- Session expiration
- Failover
- Session replication
- Network latency
- Session-store capacity

---

## 20. Sticky Sessions

Another approach is **sticky sessions**.

The load balancer always routes the same user to the same server.

```
User A
  |
  v
Load Balancer
  |
  +----> Server A
          Session A
```

Future requests from User A continue going to Server A.

```
User A → Load Balancer → Server A
User A → Load Balancer → Server A
User A → Load Balancer → Server A
```

This avoids a shared session store in some architectures.

But sticky sessions have disadvantages.

For example:

```
Server A
  |
  | Session data
  v
Server crashes ❌
```

The user's session may disappear.

It can also create uneven load distribution.

Therefore, for highly available distributed systems, a shared/distributed session store is often preferable when using server-side sessions.

---

## 21. Real-World Example

Consider an online banking website.

```
                    Browser
                       |
                       | HTTPS
                       v
                 Load Balancer
                  /          \
                 v            v
             Server A      Server B
                 \            /
                  \          /
                   v        v
                     Redis
                   Sessions
```

**Login:**

```
POST /login
```

Server A verifies credentials:

```
username/password
       ↓
     User DB
       ↓
    Valid ✅
```

Creates:

```
SESSION_ID = X123
```

Stores:

```
Redis

X123 → {
    userId: 9876,
    role: CUSTOMER,
    expiresAt: ...
}
```

Browser receives:

```
Set-Cookie: SESSION_ID=X123
```

Later:

```
GET /accounts
Cookie: SESSION_ID=X123
```

The load balancer sends it to Server B.

Server B:

```
X123
 ↓
Redis
 ↓
User 9876
 ↓
Check authorization
 ↓
Return accounts
```

It works even though the request reached a different server.

---

## 22. Complete Flow

The complete session-based authentication architecture can be visualized as:

```
                  ┌─────────────┐
                  │    Client   │
                  └──────┬──────┘
                         |
                         | 1. username/password
                         v
                  ┌─────────────┐
                  │Auth Service │
                  └──────┬──────┘
                         |
                         | 2. Verify password
                         v
                  ┌─────────────┐
                  │   User DB   │
                  └──────┬──────┘
                         |
                         | 3. Valid
                         v
                  ┌─────────────┐
                  │Create Session│
                  └──────┬──────┘
                         |
                         v
                  ┌─────────────┐
                  │    Redis    │
                  │   Session   │
                  └──────┬──────┘
                         |
                         | 4. SESSION_ID
                         v
                  ┌─────────────┐
                  │    Client   │
                  └──────┬──────┘
                         |
                         | 5. Cookie
                         v
                  ┌─────────────┐
                  │Load Balancer│
                  └──────┬──────┘
                         |
                  +------+------+
                  |             |
                  v             v
              Server A       Server B
                  |             |
                  +------+------+
                         |
                         v
                       Redis
                         |
                         v
                   User Session
                         |
                         v
                    Authorization
                         |
                         v
                      Resource
```

---

## 23. Interview Answer

If the interviewer asks:

> "What is session-based authentication and how does it work?"

You can answer:

> Session-based authentication is an authentication mechanism where the server maintains the authenticated user's state in a server-side session. After successful login, the server generates a unique session ID and sends it to the client, typically using a secure HTTP cookie. For every subsequent request, the browser automatically sends the session ID. The server uses that ID to retrieve the user's session from its session store, such as memory or Redis, identifies the user, and then performs authorization before accessing the requested resource. In a horizontally scaled system, sessions are commonly stored in a shared distributed store such as Redis so that any application server can process the request.

### Remember this flow:

```
Login
  ↓
Verify credentials
  ↓
Create server-side session
  ↓
Send SESSION_ID cookie
  ↓
Client sends SESSION_ID on every request
  ↓
Server looks up session
  ↓
Identify user
  ↓
Authorization
  ↓
Access resource
```

> **The key difference from JWT:** with session-based authentication, the session state lives on the server and the client typically holds only a session ID.

