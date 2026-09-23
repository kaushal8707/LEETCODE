# CDN — Content Delivery Network

A CDN (Content Delivery Network) is a geographically distributed network of servers that caches and delivers content closer to users.

In system design, CDN is mainly used to improve:

- ⚡ Latency
- 🚀 Response time
- 📈 Scalability
- 🛡️ Availability
- 💰 Origin server cost
- 🔥 Traffic/load on backend servers

---

## 1. The Problem Without a CDN

Suppose your application is hosted in Mumbai:

```
                    Internet
                       |
                       v
                 +-----------+
                 |  Users    |
                 +-----------+
                       |
                       v
                +--------------+
                | Load Balancer|
                +--------------+
                       |
                       v
                +--------------+
                | App Servers  |
                +--------------+
                       |
                       v
                +--------------+
                |   Database   |
                +--------------+
```

Now imagine a user is in New York.

The request has to travel:

```
New York User
     |
     |  Long network distance
     v
  Mumbai
     |
     v
Application Server
```

This increases network latency.

And imagine 1 million users requesting the same image:

```
1,000,000 users
       |
       v
Application Servers
       |
       v
Storage / Database
```

Your backend has to repeatedly serve the same content.

For example:

```
GET /images/product-123.jpg
```

The image might be identical for every user.

That's where a CDN helps.

---

## 2. What Does a CDN Do?

Instead of forcing every user to contact your origin server, a CDN places servers called Edge Servers / Edge Locations around the world.

```
                     CDN
        +-----------------------------+
        |                             |
        |  New York Edge              |
        |       |                     |
        |  London Edge                |
        |       |                     |
        |  Singapore Edge             |
        |       |                     |
        |  Mumbai Edge                |
        |                             |
        +-----------------------------+
                     |
                     v
              Origin Server
```

Users are generally routed to a suitable nearby CDN edge location.

For example:

```
User in India
      |
      v
Mumbai CDN Edge
      |
      v
Origin Server (only if needed)
```

While:

```
User in USA
      |
      v
US CDN Edge
      |
      v
Origin Server (only if needed)
```

---

## 3. Real-Life Example

Consider an e-commerce application.

You have:

```
https://example.com/images/iphone.jpg
```

The image is 10 MB.

Without CDN:

```
User 1 ────────> Origin ────────> 10 MB
User 2 ────────> Origin ────────> 10 MB
User 3 ────────> Origin ────────> 10 MB
...
User 1,000,000 -> Origin
```

The origin server has to repeatedly deliver the same file.

With CDN:

```
                    CDN Edge
                       |
User 1 ───────────────>|
User 2 ───────────────>|
User 3 ───────────────>|
                       |
                       v
                 Cached Image
```

The origin might only need to provide the image to the CDN once.

---

## 4. CDN Request Flow

Let's understand the most important concept: Cache Hit and Cache Miss.

Suppose the user requests:

```
GET /images/product.jpg
```

### Step 1 — User sends request

```
User
 |
 | GET /images/product.jpg
 v
CDN
```

The CDN checks whether it already has the requested object.

---

## 5. Cache Hit

Suppose the CDN already has the image.

```
User
 |
 | Request
 v
CDN Edge
 |
 | Image exists in cache
 v
Return Image
```

This is called a:

**Cache Hit**

Example:

```
User
 |
 v
Mumbai CDN
 |
 | Cache HIT
 |
 v
Image
```

The origin server is not contacted.

This is extremely fast.

---

## 6. Cache Miss

Suppose the image isn't present at the CDN edge.

```
User
 |
 v
CDN Edge
 |
 | Cache MISS
 v
Origin Server
 |
 v
Image
```

The CDN gets the image from the origin.

Then it usually stores the response in its cache:

```
                    +----------------+
                    | Origin Server  |
                    +----------------+
                            |
                            | Image
                            v
                    +----------------+
                    | CDN Edge Cache |
                    +----------------+
                            |
                            v
                           User
```

The next user can get the image directly from the CDN.

```
User 2
  |
  v
CDN Edge
  |
  | Cache HIT
  v
Image
```

---

## 7. CDN Architecture

A simplified architecture looks like this:

```
                         Users
                           |
             +-------------+-------------+
             |             |             |
             v             v             v
        CDN Edge       CDN Edge      CDN Edge
        Mumbai         London         New York
             |             |             |
             +-------------+-------------+
                           |
                           v
                    Load Balancer
                           |
                           v
                    Application
                       Servers
                           |
                           v
                     Origin Storage
```

The important distinction is:

- **CDN Edge** → serves cached content
- **Origin** → source of the original content

---

## 8. What Content Does a CDN Cache?

CDNs are especially useful for static content.

Examples:

- Images
- CSS
- JavaScript
- Videos
- Fonts
- PDFs
- HTML
- Static files
- Software downloads

For example:

```
/logo.png
/styles.css
/app.js
/font.woff2
/video.mp4
```

These are excellent CDN candidates because many users request the same content.

---

## 9. Can a CDN Cache API Responses?

Yes.

A CDN can cache certain API responses, but you have to be much more careful.

For example:

```
GET /products/123
```

might be cacheable.

But:

```
GET /account/balance
```

usually should not be publicly cached because it is user-specific and sensitive.

Similarly:

```
POST /payment
```

is generally not something you cache like a static asset.

A simplified rule:

| Content | CDN Cache? |
|---|---|
| Images | ✅ Excellent |
| CSS | ✅ Excellent |
| JS | ✅ Excellent |
| Videos | ✅ Excellent |
| Fonts | ✅ Excellent |
| Public product data | ✅ Often |
| User profile | ⚠️ Carefully |
| Account balance | ❌ Usually |
| Payment request | ❌ |
| Password/authentication response | ❌ |

---

## 10. CDN + Cache-Control

Your application can tell the CDN how long content should be cached.

For example:

```
Cache-Control: public, max-age=3600
```

This means the response can be cached for:

```
3600 seconds = 1 hour
```

Another example:

```
Cache-Control: public, max-age=86400
```

That's:

```
24 hours
```

So:

```
Origin
  |
  | Cache-Control: max-age=86400
  v
CDN
  |
  | Store for 24 hours
  v
Users
```

---

## 11. TTL

TTL = Time To Live

TTL determines how long an object remains cached.

Example:

```
product.jpg
TTL = 1 hour
```

Timeline:

```
10:00 AM → Cached
11:00 AM → Cache expires
11:01 AM → CDN fetches from origin
```

After expiration, the CDN may need to contact the origin again.

---

## 12. CDN Cache Invalidation

Imagine you deploy a new logo:

```
logo.png
```

But the CDN still has the old logo.

You can invalidate/remove the cached object.

```
Application
     |
     | Invalidate
     v
CDN
     |
     X Old logo removed
```

Then the next request causes a cache miss:

```
User
 |
 v
CDN
 |
 | MISS
 v
Origin
 |
 | New logo
 v
CDN
 |
 v
User
```

Many systems avoid frequent invalidation by using versioned filenames:

```
app.v1.js
```

then:

```
app.v2.js
```

This is called **cache busting**.

---

## 13. CDN and DNS

CDNs commonly work closely with DNS.

Suppose the user requests:

```
www.example.com
```

DNS/CDN routing can direct the user toward an appropriate CDN edge.

Conceptually:

```
                 www.example.com
                        |
                        v
                       DNS
                        |
             +----------+----------+
             |                     |
             v                     v
        Mumbai Edge           New York Edge
             |                     |
             v                     v
           User                   User
```

The exact routing mechanism varies by CDN provider, but the goal is generally:

Send the user to an appropriate edge location.

---

## 14. CDN vs Application Cache

These are different layers.

### Application cache

Example:

```
Application Server
       |
       v
     Redis
       |
       v
   Database
```

Redis might cache:

```
product:123
```

### CDN cache

```
User
 |
 v
CDN
 |
 v
Application
```

CDN caches content closer to the user.

So:

```
                    User
                      |
                      v
                    CDN
                 (Edge Cache)
                      |
                      v
              Load Balancer
                      |
                      v
                 App Server
                      |
                      v
                   Redis
                      |
                      v
                  Database
```

There can be multiple caching layers.

---

## 15. CDN vs Load Balancer

This is a very common system-design interview question.

### Load Balancer

Its primary responsibility is:

**Distribute requests across backend servers.**

```
             Load Balancer
              /    |    \
             /     |     \
           App1   App2   App3
```

### CDN

Its primary responsibility is:

**Deliver cached content closer to users.**

```
          CDN Edge
             |
             v
          Cached Data
```

They solve different problems.

| CDN | Load Balancer |
|---|---|
| Caches content | Distributes traffic |
| Geographically distributed | Usually sits in front of backend servers |
| Reduces latency | Prevents one server from being overloaded |
| Reduces origin traffic | Improves backend scalability |
| Best for static/cacheable content | Handles dynamic requests too |

And they are often used together.

---

## 16. CDN in a Real System

Consider Netflix-like video streaming.

Without CDN:

```
                  Users
                    |
                    v
              Origin Servers
                    |
                    v
                Videos
```

Millions of users downloading videos directly from origin servers would be expensive and difficult to scale.

With CDN:

```
                       Users
                  /      |      \
                 /       |       \
                v        v        v
           CDN Edge   CDN Edge   CDN Edge
             India      USA       Europe
                 \       |       /
                  \      |      /
                   v     v     v
                    Origin
```

Popular videos are cached at edge locations.

Users download them from nearby CDN servers.

---

## 17. CDN Reduces Origin Load

Suppose:

```
1,000,000 requests
```

and the CDN has:

```
95% cache hit ratio
```

Then approximately:

```
950,000 requests → CDN cache

50,000 requests → Origin
```

So the origin sees dramatically less traffic.

This is one of the biggest reasons CDNs are important in system design.

---

## 18. CDN and Availability

CDNs can also improve resilience.

Suppose your origin temporarily has problems.

If content is already cached:

```
User
 |
 v
CDN
 |
 | Cached content
 v
User
```

Some content may continue to be served without contacting the origin, depending on the CDN's caching/failover behavior and configuration.

So CDN can contribute to availability, although it should not be considered a complete replacement for backend redundancy.

---

## 19. CDN and Security

Modern CDNs often provide security features such as:

- DDoS protection
- WAF
- Rate limiting
- TLS termination
- Bot protection
- IP filtering

A common architecture:

```
Internet
   |
   v
+--------+
|  CDN   |
|  + WAF |
+--------+
   |
   v
Load Balancer
   |
   v
App Servers
```

This means malicious traffic can potentially be filtered before reaching your infrastructure.

---

## 20. Pull vs Push CDN

Two common approaches are:

### Pull CDN

The CDN fetches content from the origin when needed.

```
User
 |
 v
CDN
 |
 | MISS
 v
Origin
 |
 v
CDN caches content
```

This is very common.

### Push CDN

You explicitly upload content to the CDN.

```
Application
     |
     | Upload
     v
CDN
     |
     v
Edge locations
```

This can be useful for large files, videos, or controlled content distribution.

---

## 21. Important CDN Terms for System Design

You should remember these terms:

### Edge Location

A CDN server/location close to users.

```
User → Edge Location
```

### Origin Server

The original source of the content.

```
CDN → Origin
```

### Cache Hit

Requested content exists in CDN cache.

```
CDN → Content
```

### Cache Miss

Content isn't in CDN cache.

```
CDN → Origin → CDN → User
```

### TTL

How long content stays cached.

### Cache Invalidation

Removing cached content before TTL expires.

### Cache Hit Ratio

Percentage of requests served from cache.

```
Cache Hit Ratio =
Cache Hits / Total Requests
```

For example:

```
Cache Hits = 900
Total Requests = 1000

Hit Ratio = 90%
```

---

## 22. Where CDN Fits in System Design

A typical scalable architecture could look like:

```
                         Users
                           |
                           v
                     +-----------+
                     |    CDN    |
                     +-----------+
                           |
                    Cache Miss / API
                           |
                           v
                    +-----------+
                    |    WAF    |
                    +-----------+
                           |
                           v
                    +-----------+
                    |   Load    |
                    |  Balancer |
                    +-----------+
                       /   |   \
                      /    |    \
                     v     v     v
                   App1  App2  App3
                     \    |    /
                      \   |   /
                       v  v  v
                     +-------+
                     | Redis |
                     +-------+
                         |
                         v
                    +---------+
                    |Database |
                    +---------+
```

This is a very important system-design pattern:

```
CDN → Load Balancer → Application Servers → Cache → Database
```

Each component solves a different scalability problem.

---

## 23. Interview Answer

If an interviewer asks:

> "What is a CDN and why do we use it?"

A strong answer is:

> A CDN, or Content Delivery Network, is a geographically distributed network of edge servers that caches and serves content closer to end users. It reduces network latency, decreases load on origin servers, improves scalability and availability, and can also provide security features such as DDoS protection and WAF. For example, instead of every user downloading an image from an origin server in Mumbai, users in different regions can retrieve the cached image from nearby CDN edge locations.

### Remember this mental model

```
                 CDN
                  |
        "Bring content closer"
                  |
                  v
              User ⚡
```

Whereas:

```
           Load Balancer
                  |
        "Distribute traffic"
                  |
          +-------+-------+
          v       v       v
        App1    App2    App3
```

- **CDN** = bring data closer to users.
- **Load Balancer** = distribute requests across servers.
