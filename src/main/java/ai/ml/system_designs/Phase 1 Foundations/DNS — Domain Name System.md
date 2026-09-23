# DNS — Domain Name System

Now that you understand Client–Server Architecture and IP Addresses, DNS is the natural next topic.

The most important idea is:

> DNS translates human-friendly domain names into information such as IP addresses that computers can use to reach the destination.

For example:

```
google.com
    ↓
DNS
    ↓
142.250.x.x
```

Think of DNS as the **phonebook of the Internet**.

---

## 1. Why Do We Need DNS?

Imagine Google had an IP address:

```
142.250.195.14
```

You would have to remember:

```
https://142.250.195.14
```

That's difficult.

Instead, we use:

```
https://google.com
```

DNS helps resolve:

```
google.com
     ↓
IP address
```

So the basic flow is:

```
User
 │
 │ google.com
 ▼
DNS
 │
 │ 142.250.x.x
 ▼
Google Server
```

---

## 2. Real-Life Analogy

Think about your phone.

You don't memorize your friend's phone number.

Instead:

```
Rahul
  ↓
Contacts
  ↓
+91-XXXXXXXXXX
```

DNS works similarly:

```
google.com
     ↓
DNS
     ↓
IP Address
```

So:

| Real World | Internet |
|---|---|
| Person's name | Domain name |
| Phone contacts | DNS |
| Phone number | IP address |
| Calling | Network communication |

---

## 3. Domain Name

A domain name is the human-readable name.

Examples:

```
google.com
amazon.com
netflix.com
example.com
```

A domain can have multiple parts.

Consider:

```
www.example.com
```

Break it down:

```
www       . example . com
│             │        │
│             │        └── TLD
│             └────────── Domain
└──────────────────────── Subdomain
```

---

## 4. DNS Hierarchy

DNS is hierarchical.

At the top:

```
                    Root
                     .
                     │
          ┌──────────┼──────────┐
          ▼          ▼          ▼
         .com       .org       .in
          │
          ▼
      example.com
          │
          ▼
     www.example.com
```

There are several important levels.

### Root

Represented conceptually as:

```
.
```

### TLD — Top-Level Domain

Examples:

```
.com
.org
.net
.in
.uk
```

### Domain

```
example.com
```

### Subdomain

```
www.example.com
api.example.com
mail.example.com
```

---

## 5. What Happens When You Type a URL?

This is one of the most important System Design flows.

Suppose you type:

```
https://www.example.com
```

into your browser.

Very simplified:

```
Browser
   │
   │ "What is the IP of www.example.com?"
   ▼
DNS Resolver
   │
   ▼
DNS Servers
   │
   ▼
IP Address
   │
   ▼
Browser
   │
   ▼
Web Server
```

Let's go deeper.

---

## 6. Step-by-Step DNS Resolution

Suppose the browser needs:

```
www.example.com
```

### Step 1 — Browser Cache

The browser may already know the IP.

```
Browser DNS Cache
       │
       ├── Found → use it
       │
       └── Not found → continue
```

---

## 7. Step 2 — Operating System Cache

The OS may have cached the DNS result.

```
Browser
   ↓
OS DNS Cache
```

If the answer isn't there, the request goes to the configured DNS resolver.

---

## 8. Step 3 — Recursive DNS Resolver

Usually your machine is configured to use a recursive DNS resolver.

It may be provided by:

- Your ISP
- Your company/network
- A public DNS service
- A cloud environment

The resolver's job is essentially:

> "I'll find the answer for you."

Conceptually:

```
Client
   │
   ▼
Recursive Resolver
```

If the resolver already has the answer cached:

```
Client
   │
   ▼
Resolver
   │
   │ Cached IP
   ▼
Client
```

Otherwise, it needs to find the answer.

---

## 9. Step 4 — Root DNS Server

The resolver asks the DNS hierarchy.

First, it can ask a Root DNS server:

```
Resolver
    │
    │ Where is example.com?
    ▼
Root DNS
```

The root doesn't usually provide the final IP.

Instead, it can direct the resolver toward the appropriate TLD servers.

For:

```
example.com
```

the root can point toward:

```
.com TLD servers
```

---

## 10. Step 5 — TLD Server

The resolver then asks the .com TLD infrastructure:

```
Resolver
   │
   ▼
.com TLD
```

The TLD server knows which authoritative DNS servers are responsible for:

```
example.com
```

It returns information directing the resolver toward the authoritative DNS server.

---

## 11. Step 6 — Authoritative DNS Server

Now the resolver asks the authoritative DNS server:

```
Resolver
    │
    ▼
Authoritative DNS
    │
    │ www.example.com?
    ▼
IP address
```

For example:

```
www.example.com
       ↓
93.184.216.34
```

The resolver receives the answer.

---

## 12. Complete DNS Resolution

Putting it all together:

```
                     User
                      │
                      │ www.example.com
                      ▼
               Browser Cache
                      │
                 cache miss
                      ▼
                OS DNS Cache
                      │
                 cache miss
                      ▼
             Recursive Resolver
                      │
                      ▼
                 Root DNS
                      │
                      ▼
                  .com TLD
                      │
                      ▼
             Authoritative DNS
                      │
                      │ IP address
                      ▼
             Recursive Resolver
                      │
                      ▼
                   Browser
                      │
                      │ HTTP/HTTPS
                      ▼
                  Web Server
```

This is the high-level DNS resolution process you should remember.

---

## 13. Why DNS Caching Is Important

Imagine every request required:

```
Client
 ↓
Root
 ↓
TLD
 ↓
Authoritative Server
```

That would be expensive and slow.

So DNS uses **caching**.

For example:

**First request:**

```
Client
 ↓
Resolver
 ↓
Root
 ↓
TLD
 ↓
Authoritative DNS
 ↓
IP
```

Then:

**Second request:**

```
Client
 ↓
Resolver
 ↓
Cached IP
```

Much faster.

---

## 14. TTL — Time To Live

DNS records usually have a **TTL**.

TTL tells DNS caches approximately how long the record may be cached before it should be refreshed.

Example:

```
www.example.com
        ↓
IP = 10.20.30.40
TTL = 300 seconds
```

The resolver can cache the result for the TTL period.

After expiration:

```
Cache expired
     ↓
DNS lookup again
```

---

## 15. Why Is TTL Important in System Design?

Imagine you have:

```
api.mycompany.com
       ↓
10.0.0.10
```

Then you move your service:

```
10.0.0.10
      ↓
10.0.0.20
```

If DNS caches still contain:

```
10.0.0.10
```

some clients may continue using the old address until their cached record expires.

That's why DNS changes are **not necessarily instantaneous**.

---

## 16. DNS Doesn't Always Return One IP

This is extremely important for System Design.

Suppose you have:

```
api.example.com
```

DNS could return multiple IP addresses:

```
api.example.com

      ↓

10.0.0.10
10.0.0.11
10.0.0.12
```

This can help distribute traffic.

Conceptually:

```
                DNS
                 │
       ┌─────────┼─────────┐
       ▼         ▼         ▼
   Server 1   Server 2   Server 3
```

However, don't think of DNS alone as a replacement for a sophisticated load balancer. DNS-based distribution has limitations, especially because of caching and client/resolver behavior.

---

## 17. DNS and Load Balancer

A common production architecture looks like:

```
                  User
                    │
                    ▼
                   DNS
                    │
                    ▼
             Load Balancer
                    │
          ┌─────────┼─────────┐
          ▼         ▼         ▼
       Server 1  Server 2  Server 3
```

DNS might resolve:

```
api.example.com
       ↓
Load Balancer IP
```

Then the Load Balancer distributes traffic:

```
Load Balancer
      │
 ┌────┼────┐
 ▼    ▼    ▼
S1   S2    S3
```

This is a very common architecture.

---

## 18. DNS Record Types

You should know the major DNS record types.

### A Record

Maps a domain to an **IPv4** address.

```
example.com
     ↓
192.0.2.10
```

Conceptually:

```
A → IPv4
```

### AAAA Record

Maps a domain to an **IPv6** address.

```
example.com
     ↓
2001:db8::10
```

Remember:

```
A     → IPv4
AAAA  → IPv6
```

### CNAME

Points one domain name to another domain name.

Example:

```
www.example.com
       ↓
example.com
```

Think:

```
CNAME → another hostname
```

### MX

Specifies mail servers for a domain.

For example:

```
example.com
     ↓
Mail server
```

Used for email delivery.

### TXT

Stores text associated with a domain.

It's commonly used for things such as:

- Domain verification
- Email security policies
- SPF-related records

---

## 19. DNS in Microservices

DNS is not only for public websites.

Inside a cloud or Kubernetes environment, services can communicate using DNS names.

For example:

```
Order Service
      │
      │ payment-service
      ▼
Payment Service
```

Instead of hardcoding:

```
10.20.1.15
```

the Order Service can use a service name:

```
payment-service
```

DNS/service discovery resolves the name to the appropriate destination.

This becomes especially useful when service instances change.

---

## 20. Why Not Hardcode IP Addresses?

Imagine:

```
Order Service
      │
      ▼
10.20.1.15
```

Tomorrow:

```
Payment Server
10.20.1.15
      ↓
10.20.1.25
```

Your Order Service would need configuration changes.

Instead:

```
Order Service
      │
      ▼
payment-service
      │
      ▼
Current destination
```

The infrastructure can change behind the name.

This is one reason service names and DNS are valuable in distributed systems.

---

## 21. DNS Failure

What happens if DNS doesn't work?

Imagine:

```
Browser
   │
   ▼
DNS ❌
   │
   X
```

The browser may not be able to determine where to send the request.

So:

```
DNS failure
     ↓
Cannot resolve hostname
     ↓
Application may become unreachable
```

This is why DNS infrastructure itself needs:

- Redundancy
- Caching
- Multiple servers
- High availability

---

## 22. DNS and CDN

DNS can also be part of a CDN architecture.

For example:

```
                User
                  │
                  ▼
                 DNS
                  │
                  ▼
            CDN / Edge
                  │
          ┌───────┴───────┐
          ▼               ▼
      Cache HIT        Cache MISS
          │               │
          ▼               ▼
       Response       Origin Server
```

DNS can help direct users toward an appropriate edge location or CDN endpoint.

This becomes important when designing systems like:

- Netflix
- YouTube
- Large e-commerce platforms
- Global APIs

---

## 23. DNS vs IP Address

This distinction should be crystal clear.

| IP Address | DNS |
|---|---|
| Network address | Naming/resolution system |
| Used for routing traffic | Helps find destination information |
| Example: 192.0.2.10 | Example: example.com |
| Computer/network oriented | Human-friendly naming |
| Can change | DNS can point to a new destination |

Think:

```
                    DNS
                     │
                     ▼
            example.com
                     │
                     ▼
              IP Address
                     │
                     ▼
                  Server
```

---

## 24. DNS vs Load Balancer

Another common interview question.

### DNS

Answers:

> "Where should this hostname resolve?"

```
api.example.com
       ↓
Load Balancer IP
```

### Load Balancer

Answers:

> "Which backend server should handle this request?"

```
Load Balancer
     │
 ┌───┼───┐
 ▼   ▼   ▼
 S1  S2  S3
```

So:

```
DNS
 ↓
Find endpoint
 ↓
Load Balancer
 ↓
Choose backend
 ↓
Application Server
```

---

## 25. Full Request Flow

Now let's combine what you've learned so far.

You type:

```
https://api.example.com/orders
```

The simplified system looks like:

```
                         Client
                           │
                           │
                    api.example.com
                           │
                           ▼
                          DNS
                           │
                           │ IP
                           ▼
                    Load Balancer
                           │
                 ┌─────────┼─────────┐
                 ▼         ▼         ▼
              Server 1  Server 2  Server 3
                 │         │         │
                 └─────────┼─────────┘
                           │
                           ▼
                        Database
```

And the conceptual sequence is:

```
1. User enters URL
        ↓
2. DNS resolution
        ↓
3. Obtain destination IP
        ↓
4. Establish network connection
        ↓
5. Send HTTP/HTTPS request
        ↓
6. Load Balancer receives request
        ↓
7. Load Balancer selects server
        ↓
8. Application processes request
        ↓
9. Database is accessed if necessary
        ↓
10. Response returns to client
```

This is the foundation of a real production architecture.

---

## 26. Interview Questions You Should Be Able to Answer

### Beginner

**What is DNS?**

DNS is a distributed hierarchical naming system that resolves domain names to network-related information, most commonly IP addresses.

### Intermediate

**What happens when you type google.com into a browser?**

You should be able to explain:

```
Browser cache
     ↓
OS cache
     ↓
Recursive resolver
     ↓
Root
     ↓
TLD
     ↓
Authoritative DNS
     ↓
IP
     ↓
Server
```

### Intermediate

**Why does DNS use caching?**

To:

- Reduce latency
- Reduce DNS traffic
- Reduce load on authoritative infrastructure
- Improve resilience

### Intermediate

**What is TTL?**

TTL controls how long a DNS response can be cached before it needs to be refreshed.

### Senior

**How can DNS help with scalability?**

Possible mechanisms include:

- Multiple IP addresses
- Load balancer endpoints
- CDN routing
- Geographic/latency-aware routing
- Failover mechanisms

### Senior

**What happens if you change a DNS record?**

Existing cached records may continue to be used until their TTL expires, so the change may take time to propagate through caches.

---

## 27. Your System Design Mental Model So Far

You've now built three layers:

```
                    CLIENT
                       │
                       │
                 Domain Name
                       │
                       ▼
                      DNS
                       │
                       │ IP Address
                       ▼
                Load Balancer
                       │
            ┌──────────┼──────────┐
            ▼          ▼          ▼
         Server 1   Server 2   Server 3
```

You should now understand:

```
Client
  ↓
"I know the domain name."

DNS
  ↓
"I find the destination."

IP
  ↓
"Here's the network address."

Load Balancer
  ↓
"I choose a healthy backend."

Server
  ↓
"I process the request."
```

---

## Next Topic: HTTP / HTTPS

The next natural question is:

> Once DNS gives us an IP address, how does the client actually communicate with the server?

That's where **HTTP** comes in.

We'll cover:

```
DNS
 ↓
IP Address
 ↓
TCP Connection
 ↓
HTTP Request
 ↓
Server
 ↓
HTTP Response
```

And then go deeper into HTTP methods, status codes, headers, cookies, sessions, HTTPS/TLS, HTTP/1.1 vs HTTP/2 vs HTTP/3, and keep-alive.
