# Multiplexing in HTTP/2 and HTTP/3

Multiplexing is one of the most important concepts to understand before going deeper into HTTP/2 and HTTP/3.

The simplest definition is:

> **Multiplexing means sending multiple independent requests and responses concurrently over a single connection.**

Let's build it from scratch.

---

## 1. The Problem Before Multiplexing

Imagine a browser needs these resources:

```
1. index.html
2. style.css
3. app.js
4. logo.png
5. products.json
```

With a simplified HTTP/1.1 model, you could have:

```
Request 1
   ↓
Response 1
   ↓
Request 2
   ↓
Response 2
   ↓
Request 3
   ↓
Response 3
```

So the requests are handled sequentially on that connection.

The browser could open multiple TCP connections to increase parallelism:

```
Connection 1 → index.html
Connection 2 → style.css
Connection 3 → app.js
Connection 4 → logo.png
```

But every connection consumes resources and may require TCP/TLS setup.

---

## 2. Multiplexing Solves This

HTTP/2 says:

> "Why create multiple connections? Let's put multiple logical streams inside one connection."

So instead of:

```
TCP 1 → Request A
TCP 2 → Request B
TCP 3 → Request C
```

we can have:

```
              ONE TCP CONNECTION
                     |
       ┌─────────────┼─────────────┐
       ↓             ↓             ↓
    Stream 1      Stream 3      Stream 5
       ↓             ↓             ↓
   Request A      Request B      Request C
```

That's multiplexing.

---

## 3. Real-World Analogy

Imagine one highway.

### Without Multiplexing

You have separate highways:

```
Highway 1 → Car A
Highway 2 → Car B
Highway 3 → Car C
```

Each highway requires infrastructure.

### With Multiplexing

You have:

```
             ONE HIGHWAY
                 |
       ┌─────────┼─────────┐
       ↓         ↓         ↓
     Car A     Car B     Car C
```

Multiple cars share the same highway.

Similarly:

```
ONE TCP CONNECTION
       |
 ┌─────┼─────┐
 ↓     ↓     ↓
 S1    S3    S5
 ↓     ↓     ↓
Req A Req B Req C
```

---

## 4. What is a Stream?

A stream is a logical, independent sequence of messages/data within an HTTP/2 or HTTP/3 connection.

For example:

```
HTTP/2 Connection
│
├── Stream 1
│     └── GET /index.html
│
├── Stream 3
│     └── GET /style.css
│
├── Stream 5
│     └── GET /app.js
│
└── Stream 7
      └── GET /logo.png
```

All of these share the same underlying connection.

---

## 5. Requests Can Be Interleaved

This is where multiplexing becomes powerful.

Suppose:

```
Request A = Large image
Request B = Small CSS
Request C = Small JSON
```

Instead of waiting for A to finish:

```
A A A A A A A
B
C
```

HTTP/2 can interleave frames:

```
A1
B1
C1
A2
B2
A3
C2
A4
...
```

Conceptually:

```
             ONE CONNECTION

A1 ─┐
B1 ─┤
C1 ─┤
A2 ─┤
B2 ─┤
A3 ─┤
C2 ─┤
A4 ─┘
```

This is multiplexing.

---

## 6. HTTP/2 Frames

HTTP/2 doesn't simply send complete requests as one giant block.

It breaks communication into **binary frames**.

Conceptually:

```
┌──────────────┐
│ Frame Header │
├──────────────┤
│ Stream ID    │
├──────────────┤
│ Frame Type   │
├──────────────┤
│ Payload      │
└──────────────┘
```

The important part here is:

**Stream ID**

For example:

```
Frame 1 → Stream 1
Frame 2 → Stream 3
Frame 3 → Stream 1
Frame 4 → Stream 5
Frame 5 → Stream 3
```

The receiver knows which stream each frame belongs to.

---

## 7. Example

Suppose the browser requests:

```
GET /products
GET /profile
GET /orders
```

HTTP/2 might conceptually produce:

```
Stream 1 → /products
Stream 3 → /profile
Stream 5 → /orders
```

Frames:

```
┌─────────────┐
│ Stream 1    │ → products part 1
├─────────────┤
│ Stream 3    │ → profile part 1
├─────────────┤
│ Stream 5    │ → orders part 1
├─────────────┤
│ Stream 1    │ → products part 2
├─────────────┤
│ Stream 5    │ → orders part 2
├─────────────┤
│ Stream 3    │ → profile part 2
└─────────────┘
```

All traveling through:

```
ONE TCP CONNECTION
```

---

## 8. Why is This Better?

Imagine:

```
/products → 10 MB
/profile  → 10 KB
/orders   → 10 KB
```

Without efficient multiplexing, the large request can interfere with smaller requests.

With HTTP/2:

```
Products
  ↓
Stream 1 ──────────────┐
                       │
Profile                │
  ↓                    │
Stream 3 ──────────────┤ → ONE CONNECTION
                       │
Orders                 │
  ↓                    │
Stream 5 ──────────────┘
```

Small responses can make progress without requiring separate TCP connections.

---

## 9. Multiplexing ≠ Multiple TCP Connections

This distinction is very important.

### HTTP/1.1 Approach

```
TCP Connection 1
    ↓
Request A

TCP Connection 2
    ↓
Request B

TCP Connection 3
    ↓
Request C
```

### HTTP/2

```
ONE TCP CONNECTION
        |
   ┌────┼────┐
   ↓    ↓    ↓
Stream Stream Stream
  A      B      C
```

So:

> **Multiplexing means multiple logical streams share one physical connection.**

---

## 10. But There is a Catch with HTTP/2

This is one of the most important concepts in understanding HTTP/3.

HTTP/2:

```
HTTP/2
   ↓
TCP
```

TCP itself is:

**One ordered byte stream.**

Suppose:

```
Stream A → Packet 1
Stream B → Packet 2
Stream C → Packet 3
```

And:

```
Packet 2 → LOST
```

TCP says:

> "I need to maintain ordered delivery of the byte stream."

So even though Stream A and Stream C may not logically depend on Stream B, TCP's ordered delivery can cause subsequent bytes to wait for the missing data.

Conceptually:

```
HTTP/2

Stream A ──┐
Stream B ──┼──→ TCP → Packet lost
Stream C ──┘
                    ↓
             Retransmission
                    ↓
             Delivery delayed
```

This is **TCP-level Head-of-Line Blocking**.

---

## 11. HTTP/3 Improves This

HTTP/3 uses:

```
HTTP/3
   ↓
QUIC
   ↓
UDP
```

QUIC also supports multiple streams:

```
QUIC Connection
│
├── Stream 1
├── Stream 2
├── Stream 3
└── Stream 4
```

But unlike TCP, QUIC doesn't make all application streams one single ordered byte stream.

So:

```
Stream 1 → Packet lost ❌
Stream 2 → Packet received ✅
Stream 3 → Packet received ✅
```

Stream 2 and Stream 3 can continue making progress while Stream 1 waits for retransmission.

---

## 12. HTTP/2 vs HTTP/3 Multiplexing

This is the important comparison:

### HTTP/2

```
              HTTP/2
                 |
          ONE TCP CONNECTION
                 |
       ┌─────────┼─────────┐
       ↓         ↓         ↓
    Stream 1  Stream 3  Stream 5
       \         |         /
        \        |        /
             TCP
              ↓
       One ordered byte stream
```

Problem:

```
Packet loss
    ↓
TCP retransmission
    ↓
Potential blocking across streams
```

### HTTP/3

```
              HTTP/3
                 |
               QUIC
                 |
       ┌─────────┼─────────┐
       ↓         ↓         ↓
    Stream 1  Stream 3  Stream 5
       ↓         ↓         ↓
   independent QUIC streams
                 |
                UDP
```

Loss on one stream doesn't inherently block unrelated streams.

---

## 13. Another Real-World Example

Imagine you're using an e-commerce application.

Your browser requests:

- Product information
- User profile
- Recommendations
- Reviews

With HTTP/2:

```
             HTTP/2
                |
          One connection
                |
      ┌─────────┼─────────┐
      ↓         ↓         ↓
 Product     Profile    Reviews
 Stream      Stream     Stream
```

With HTTP/3:

```
             HTTP/3
                |
               QUIC
                |
      ┌─────────┼─────────┐
      ↓         ↓         ↓
 Product     Profile    Reviews
 Stream      Stream     Stream
```

The application-level concept of multiplexing is similar.

The underlying transport behavior is what changes significantly.

---

## 14. Multiplexing vs Parallel Connections

This is a common interview question.

### Multiple Connections

```
Client
 |
 ├── TCP 1 → Request A
 ├── TCP 2 → Request B
 └── TCP 3 → Request C
```

Problems:

- More connection state
- More TCP handshakes
- More TLS setup if each connection is separately secured
- More resource usage
- More congestion-control state

### Multiplexing

```
Client
 |
 └── ONE connection
       ├── Stream A
       ├── Stream B
       └── Stream C
```

Benefits:

- Connection reuse
- Less connection overhead
- Better utilization
- Concurrent request/response progress
- Fewer connections to manage

---

## 15. Multiplexing and TLS

Since you've just learned HTTPS/TLS, connect these concepts.

HTTP/2 commonly looks like:

```
HTTP/2
  ↓
TLS
  ↓
TCP
```

You establish:

```
ONE TCP connection
        ↓
ONE TLS session
        ↓
MANY HTTP/2 streams
```

So instead of doing:

```
Request A
 ↓
TLS connection

Request B
 ↓
another TLS connection

Request C
 ↓
another TLS connection
```

you can have:

```
             ONE TLS connection
                    |
        ┌───────────┼───────────┐
        ↓           ↓           ↓
     Stream 1    Stream 3    Stream 5
```

This is a major performance benefit.

---

## 16. Multiplexing Doesn't Mean Requests are Completely Independent

This is subtle.

At the HTTP layer, streams are logically independent.

But the underlying transport still matters.

For HTTP/2:

```
HTTP/2 streams
      ↓
One TCP connection
      ↓
TCP ordering
```

Therefore TCP can still introduce cross-stream blocking.

For HTTP/3:

```
HTTP/3 streams
      ↓
QUIC streams
      ↓
UDP
```

QUIC provides independent stream delivery behavior.

---

## 17. Multiplexing vs Pipelining

Don't confuse these.

### HTTP/1.1 Pipelining

Conceptually:

```
Request A →
Request B →
Request C →

Response A ←
Response B ←
Response C ←
```

Requests can be sent without waiting for each response, but responses are still ordered.

### HTTP/2 Multiplexing

```
A1
B1
C1
A2
C2
B2
A3
```

Frames from different streams can be interleaved.

So HTTP/2 provides much more flexible concurrent communication.

---

## 18. Multiplexing and Microservices

This becomes useful in system design.

Suppose an API Gateway calls:

```
             API Gateway
                  |
             HTTP/2 connection
                  |
       ┌──────────┼──────────┐
       ↓          ↓          ↓
   User Service Order      Payment
                Service     Service
```

A protocol supporting multiplexed streams can carry multiple logical operations over a shared connection.

This can be especially useful for high-volume service-to-service communication.

Technologies such as **gRPC** commonly use HTTP/2's multiplexing.

---

## 19. gRPC Connection

This is worth knowing for your system-design preparation.

gRPC commonly uses:

```
gRPC
  ↓
HTTP/2
  ↓
TLS
  ↓
TCP
```

Multiple RPC calls can use streams over a shared HTTP/2 connection.

For example:

```
             ONE CONNECTION
                    |
        ┌───────────┼───────────┐
        ↓           ↓           ↓
     RPC #1      RPC #2      RPC #3
```

This is one reason HTTP/2 is important in modern backend architectures.

---

## 20. The Easiest Analogy

Think about a train.

### HTTP/1.1

```
Train A → Request A
Train B → Request B
Train C → Request C
```

Multiple trains.

### HTTP/2

```
             ONE TRAIN
                |
      ┌─────────┼─────────┐
      ↓         ↓         ↓
    Stream A  Stream B  Stream C
```

One train carrying multiple logical streams.

### HTTP/3

Similar logical streams, but the underlying transport is QUIC:

```
             QUIC
       ┌───────┼───────┐
       ↓       ↓       ↓
    Stream A Stream B Stream C
```

A delay/loss affecting one stream doesn't inherently stop unrelated streams in the same way TCP does.

---

## 21. Interview Answer

If an interviewer asks:

> **"What is multiplexing in HTTP/2?"**

You can say:

> "Multiplexing allows multiple independent HTTP request and response streams to share a single TCP connection. HTTP/2 breaks communication into binary frames, each associated with a stream ID, so frames from different streams can be interleaved. This reduces the need for multiple TCP connections and improves resource utilization and concurrency. However, because HTTP/2 still runs over TCP's single ordered byte stream, packet loss can cause transport-level head-of-line blocking."

And if they ask:

> **"How does HTTP/3 improve this?"**

Answer:

> "HTTP/3 uses QUIC over UDP. QUIC provides independent multiplexed streams at the transport layer, so loss affecting one stream doesn't inherently block unrelated streams. QUIC also integrates TLS 1.3 and supports features such as connection migration."

---

## 22. Final Mental Model

Remember this:

```
                 HTTP/1.1
                    |
             Multiple connections
                    |
          ┌─────────┼─────────┐
          ↓         ↓         ↓
        TCP 1     TCP 2     TCP 3


                 HTTP/2
                    |
             ONE TCP CONNECTION
                    |
          ┌─────────┼─────────┐
          ↓         ↓         ↓
       Stream 1  Stream 3  Stream 5
          \         |         /
                 TCP
                  ↓
          HOL possible


                 HTTP/3
                    |
                  QUIC
                    |
          ┌─────────┼─────────┐
          ↓         ↓         ↓
       Stream 1  Stream 3  Stream 5
                    |
                   UDP
                    ↓
              No TCP HOL
```

**The one sentence to remember:**

> Multiplexing = multiple logical streams sharing one physical connection; HTTP/2 introduced this at the HTTP layer over TCP, while HTTP/3 uses QUIC to provide multiplexed streams without TCP's single ordered byte-stream limitation.
