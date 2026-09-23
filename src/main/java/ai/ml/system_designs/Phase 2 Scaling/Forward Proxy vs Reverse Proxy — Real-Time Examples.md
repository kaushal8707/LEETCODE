# Forward Proxy vs Reverse Proxy — Real-Time Examples

The most important thing to remember is:

```
Forward Proxy  → protects/represents the CLIENT

Reverse Proxy  → protects/represents the SERVER
```

---

## 1. Real-Time Example of a Forward Proxy

Imagine you work in a company.

You have your laptop:

```
Your Laptop
     |
     v
Company Proxy
     |
     v
Internet
     |
     v
google.com
```

When you open:

```
https://google.com
```

your request might go:

```
Laptop
   |
   | "I want google.com"
   v
Forward Proxy
   |
   | "I'll access it for you"
   v
Google
```

The proxy is acting on behalf of you, the client.

That's a **Forward Proxy**.

---

## 2. Why Would a Company Use a Forward Proxy?

Suppose a company has:

```
1000 employees
```

The company wants to control internet access.

Instead of:

```
Employee 1 ─────────> Internet
Employee 2 ─────────> Internet
Employee 3 ─────────> Internet
...
Employee 1000 ──────> Internet
```

they can use:

```
Employee 1 ──┐
Employee 2 ──┤
Employee 3 ──┤
     ...      ├──> Forward Proxy ──> Internet
Employee 1000 ┘
```

Now the company can apply policies at the proxy:

```
Forward Proxy
     |
     +-- Block malicious websites
     +-- Block certain categories
     +-- Monitor traffic
     +-- Apply access policies
     +-- Cache some content
```

So the proxy represents the employees/clients.

---

## 3. Another Forward Proxy Example — School

Imagine a school.

```
Student Laptop
      |
      v
School Forward Proxy
      |
      v
Internet
```

Student requests:

```
youtube.com
```

The proxy checks:

```
Is this website allowed?
```

**If yes:**

```
Student
   ↓
Proxy
   ↓
YouTube
```

**If no:**

```
Student
   ↓
Proxy
   ↓
❌ Blocked
```

Again:

> The proxy is acting on behalf of the client.

---

## 4. Forward Proxy Example With Corporate Security

Imagine:

```
Employee
   |
   v
Corporate Network
   |
   v
Forward Proxy
   |
   v
Internet
```

Employee tries to access:

```
malicious-site.com
```

The forward proxy can inspect the request against company security policies.

```
Request
   ↓
Forward Proxy
   ↓
Security Policy
   |
   +---- Allowed → Internet
   |
   +---- Blocked → ❌
```

This is a classic forward-proxy use case.

---

## 5. Now Let's Understand Reverse Proxy

Suppose you have an e-commerce website.

Users access:

```
https://www.amazon-like-site.com
```

Behind that public URL, you have multiple backend servers:

```
                 Internet
                    |
                    v
                  Users
                    |
                    v
             Reverse Proxy
               /    |    \
              v     v     v
           Server1 Server2 Server3
```

The user doesn't need to know:

```
Server1 IP
Server2 IP
Server3 IP
```

The reverse proxy hides those backend details.

---

## 6. Real-Time Reverse Proxy Example

Suppose you're running a Spring Boot application.

You have:

```
Spring Boot App 1 → 10.0.0.1:8080
Spring Boot App 2 → 10.0.0.2:8080
Spring Boot App 3 → 10.0.0.3:8080
```

You don't want users accessing these directly.

Instead:

```
                     Internet
                        |
                        v
                      User
                        |
                        v
               api.mycompany.com
                        |
                        v
                Reverse Proxy
                 /      |      \
                v       v       v
              App1    App2    App3
```

The user only sees:

```
api.mycompany.com
```

The reverse proxy decides where the request goes.

---

## 7. Real-Time Example: Netflix / Large Web Applications

Think about a large web platform.

You might have:

```
                        User
                          |
                          v
                   Reverse Proxy /
                    Edge Layer
                          |
            +-------------+-------------+
            |             |             |
            v             v             v
       User Service   Movie Service   Payment
```

The user doesn't directly call:

```
10.20.1.5
10.20.1.6
10.20.1.7
```

Instead:

```
api.example.com
```

The reverse proxy routes the request internally.

---

## 8. Reverse Proxy + Load Balancing

This is where it becomes especially important for System Design.

Suppose:

```
1 million users
```

You have:

```
                 Users
                   |
                   v
            Reverse Proxy
                   |
          Load Balancing
           /     |     \
          v      v      v
       App1    App2    App3
```

**Request 1:**

```
User → Reverse Proxy → App1
```

**Request 2:**

```
User → Reverse Proxy → App2
```

**Request 3:**

```
User → Reverse Proxy → App3
```

The reverse proxy can distribute traffic among backend servers when configured to do so.

---

## 9. Reverse Proxy + HTTPS

Another very common real-world use.

```
Client
   |
   | HTTPS
   v
Reverse Proxy
   |
   | HTTP/HTTPS
   v
Spring Boot
```

The reverse proxy can perform TLS termination.

For example:

```
Client
   |
   | HTTPS
   v
+----------------------+
| Reverse Proxy        |
|                      |
| TLS Certificate      |
| TLS Termination      |
+----------------------+
           |
           | Internal traffic
           v
     Spring Boot App
```

This allows the edge layer to handle much of the TLS configuration.

---

## 10. Reverse Proxy + Multiple Microservices

Suppose you have:

```
Order Service
Payment Service
User Service
Inventory Service
```

You don't necessarily want clients to know every service's internal address.

Instead:

```
                       Client
                         |
                         v
                  Reverse Proxy
                         |
             +-----------+-----------+
             |           |           |
             v           v           v
          /orders    /payments    /users
             |           |           |
             v           v           v
          Order       Payment      User
          Service     Service      Service
```

For example:

```
GET /orders/123
```

goes to:

```
Order Service
```

while:

```
GET /users/456
```

goes to:

```
User Service
```

---

## 11. The Most Important Difference

Let's compare the direction.

### Forward Proxy

```
              FORWARD PROXY

       Client
         |
         v
   Forward Proxy
         |
         v
      Internet
         |
         v
       Server
```

The proxy represents:

> **CLIENT**

### Reverse Proxy

```
              REVERSE PROXY

       Client
         |
         v
   Reverse Proxy
         |
         v
      Backend
```

The proxy represents:

> **SERVER**

---

## 12. Easy Real-Life Analogy

Think about a restaurant.

### Forward Proxy = Personal Assistant

You tell your assistant:

```
"Order pizza for me."
```

```
You
 |
 v
Assistant
 |
 v
Restaurant
```

The assistant is acting on your behalf.

That's like a:

> **Forward Proxy**

### Reverse Proxy = Restaurant Receptionist

You enter a restaurant and tell the receptionist:

```
"I want a table."
```

The receptionist decides:

```
Which table?
Which section?
Which waiter?
```

```
Customer
   |
   v
Receptionist
   |
   +---- Table 1
   +---- Table 2
   +---- Table 3
```

The receptionist is acting as the front door to the restaurant's internal resources.

That's similar to:

> **Reverse Proxy**

---

## 13. Side-by-Side Comparison

| Feature | Forward Proxy | Reverse Proxy |
|---|---|---|
| Represents | Client | Server |
| Main direction | Client → Proxy → Internet | Client → Proxy → Backend |
| Client knows proxy? | Usually yes | Often transparent to client |
| Hides | Client identity/network | Backend servers |
| Common use | Corporate internet access | Web applications |
| Access control | Client-side/outbound | Server-side/inbound |
| Load balancing | Not typically its primary role | Common |
| TLS termination | Less typical | Very common |
| Backend routing | Not its primary role | Common |
| Example | Company proxy | NGINX in front of Spring Boot |

---

## 14. Forward Proxy in System Design

Imagine your company has:

```
                 Employees
              /     |      \
             v      v       v
           Laptop Laptop Laptop
              \      |      /
               \     |     /
                v    v    v
              Forward Proxy
                    |
                    v
                 Internet
```

The goal is:

> **Control outbound traffic**

---

## 15. Reverse Proxy in System Design

Your production application:

```
                     Users
                       |
                       v
                     DNS
                       |
                       v
                Reverse Proxy
                       |
                 Load Balancing
                 /     |      \
                v      v       v
              App1    App2    App3
                |      |       |
                +------+-------+
                       |
                       v
                  Connection Pool
                       |
                       v
                    Database
```

The goal is:

> **Control inbound traffic**

---

## 16. One Scenario Showing Both

This is a very useful real-world architecture.

Suppose a company has employees using an internal application hosted on the internet.

You could have:

```
Employee
   |
   v
Forward Proxy
   |
   v
Internet
   |
   v
Reverse Proxy
   |
   v
Load Balancer
   |
   +---- App1
   +---- App2
   +---- App3
```

Here:

### Forward Proxy

Controls the employee's outbound traffic.

```
Employee → Forward Proxy → Internet
```

### Reverse Proxy

Controls the application's incoming traffic.

```
Internet → Reverse Proxy → Application
```

This is a powerful way to visualize the difference.

---

## 17. Memory Trick 🧠

Remember:

```
FORWARD
========

Client → Proxy → Server

Proxy is FORWARDING
the client's request.

Proxy represents CLIENT.
```

And:

```
REVERSE
========

Client → Proxy → Server

Proxy is sitting in front of
the SERVER.

Proxy represents SERVER.
```

Or simply:

> **Forward Proxy** = "My users go through me."

> **Reverse Proxy** = "My servers are behind me."

---

## 18. Interview Answer

If an interviewer asks:

> **"What is the difference between forward and reverse proxy?"**

A strong 10-year-experience answer would be:

> A forward proxy sits in front of clients and represents them when accessing external services, commonly for corporate internet access, security, and outbound traffic control. A reverse proxy sits in front of backend servers and represents the server side, commonly providing TLS termination, routing, caching, security, and load balancing.

The architecture to remember is:

**Forward Proxy:**

```
CLIENT
  |
  v
PROXY
  |
  v
INTERNET / SERVER
```

**Reverse Proxy:**

```
CLIENT
  |
  v
PROXY
  |
  v
BACKEND SERVERS
```

> **Forward = client side. Reverse = server side.**
