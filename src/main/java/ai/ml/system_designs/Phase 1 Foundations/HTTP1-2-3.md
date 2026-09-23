# HTTP/1.1 vs HTTP/2 vs HTTP/3

Since you're learning System Design from scratch, HTTP/1.1 → HTTP/2 → HTTP/3 is an important evolution to understand. The key is not just memorizing features, but understanding **what problem each version solved**.

The evolution is roughly:

```
HTTP/1.1
   ↓
Problem: Too many requests + inefficient connections
   ↓
HTTP/2
   ↓
Problem: TCP-level Head-of-Line Blocking
   ↓
HTTP/3
   ↓
QUIC over UDP
```

The biggest architectural difference is:

```
HTTP/1.1
    ↓
   TCP
    ↓
   IP


HTTP/2
    ↓
   TLS
    ↓
   TCP
    ↓
   IP


HTTP/3
    ↓
   QUIC
    ↓
   UDP
    ↓
   IP
```

---

## 1. HTTP/1.1

Let's start with the traditional model.

Suppose a browser needs:

```
index.html
style.css
app.js
logo.png
product.jpg
```

The browser sends HTTP requests.

Conceptually:

```
Client                         Server
  |                              |
  | GET /index.html ------------>|
  |<------------- response ------|
  |                              |
  | GET /style.css ------------->|
  |<------------- response ------|
  |                              |
  | GET /app.js ---------------->|
  |<------------- response ------|
```

HTTP/1.1 supports persistent connections, so the TCP connection can be reused.

But HTTP/1.1 has limitations.

---

## 2. HTTP/1.1 — Text-Based Protocol

HTTP/1.1 messages look like:

```
GET /products HTTP/1.1
Host: example.com
Accept: application/json
```

Response:

```
HTTP/1.1 200 OK
Content-Type: application/json

[
  {
    "id": 101,
    "name": "Laptop"
  }
]
```

It's relatively easy for humans to understand.

---

## 3. HTTP/1.1 Problem — One Request at a Time per Connection

A simplified model is:

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

Browsers could open multiple TCP connections to work around this.

For example:

```
              Server
             /  |  \
            /   |   \
       TCP 1  TCP 2  TCP 3
         ↑      ↑      ↑
       Req A  Req B  Req C
```

But this has costs:

```
More TCP connections
       +
More TLS handshakes
       +
More resources
       +
More congestion
```

So the web needed something better.

---

## 4. HTTP/2

HTTP/2 was designed to improve HTTP performance while generally keeping HTTP semantics familiar.

The major idea:

> Multiple HTTP requests/responses can be multiplexed over a single connection.

Instead of:

```
TCP Connection 1 → Request A
TCP Connection 2 → Request B
TCP Connection 3 → Request C
```

HTTP/2 can do:

```
             ONE TCP CONNECTION
                     |
       ┌─────────────┼─────────────┐
       ↓             ↓             ↓
    Stream 1      Stream 3      Stream 5
    Request A     Request B     Request C
       ↓             ↓             ↓
    Response      Response      Response
```

This is one of the most important differences between HTTP/1.1 and HTTP/2.

---

## 5. What is a Stream?

An HTTP/2 connection can contain multiple independent logical streams.

For example:

```
HTTP/2 Connection
       |
       ├── Stream 1 → GET /index.html
       |
       ├── Stream 3 → GET /style.css
       |
       ├── Stream 5 → GET /app.js
       |
       └── Stream 7 → GET /logo.png
```

All of them can share one TCP connection.

---

## 6. HTTP/2 Uses Binary Framing

HTTP/1.1 primarily uses textual message syntax.

HTTP/2 uses binary frames.

Conceptually:

**HTTP/1.1**

```
GET /products HTTP/1.1
Host: example.com
```

**HTTP/2:**

```
┌──────────────┐
│ Binary Frame │
├──────────────┤
│ Header       │
├──────────────┤
│ Payload      │
└──────────────┘
```

This makes parsing and framing more efficient and unambiguous for machines.

---

## 7. HTTP/2 Multiplexing

This is the killer feature.

Imagine:

```
Request A = HTML
Request B = CSS
Request C = JavaScript
```

HTTP/1.1 might require multiple connections or careful sequencing.

HTTP/2:

One TCP connection

```
A1 → B1 → C1 → A2 → C2 → B2 → A3 ...
```

Frames from different streams can be interleaved.

```
TCP
│
├── Stream A
│   ├── Frame A1
│   ├── Frame A2
│   └── Frame A3
│
├── Stream B
│   ├── Frame B1
│   └── Frame B2
│
└── Stream C
    ├── Frame C1
    └── Frame C2
```

---

## 8. HTTP/2 Header Compression

HTTP requests often repeat headers.

For example:

```
Host: example.com
User-Agent: Chrome
Cookie: session=abc123
Accept: application/json
```

Then another request:

```
Host: example.com
User-Agent: Chrome
Cookie: session=abc123
Accept: application/json
```

Sending the same information repeatedly wastes bandwidth.

HTTP/2 introduced **HPACK** header compression.

Conceptually:

**First request:**

```
Host: example.com
User-Agent: Chrome
Cookie: abc123

        ↓

Compression table
```

**Second request:**

```
"Use existing header entries"
```

So repeated headers can be represented more efficiently.

---

## 9. HTTP/2 Server Push

HTTP/2 also defined server push, where a server could proactively send resources the server believed the client would need.

For example:

```
Client:
GET /index.html

Server:
Here's index.html

And I think you'll need:
style.css
app.js
```

Conceptually:

```
Client
   |
   | GET index.html
   ↓
Server
   |
   ├── index.html
   ├── style.css
   └── app.js
```

However, server push turned out to be difficult to use effectively and was removed from the HTTP/3 specification. Modern web applications generally rely on other mechanisms such as preload and caching strategies.

So for interviews:

> Know that HTTP/2 introduced server push, but don't present it as a major modern best practice.

---

## 10. HTTP/2's Major Remaining Problem — TCP

This is where HTTP/3 becomes interesting.

HTTP/2 multiplexes streams:

```
             HTTP/2
                |
        ┌───────┼───────┐
        ↓       ↓       ↓
     Stream A Stream B Stream C
        \       |       /
         \      |      /
              TCP
```

But TCP guarantees:

**Ordered delivery of one byte stream.**

Suppose TCP packets are:

```
Packet 1 → received
Packet 2 → received
Packet 3 → LOST
Packet 4 → received
Packet 5 → received
```

TCP cannot deliver the bytes represented by packet 4/5 to HTTP/2 until the missing packet 3 is recovered, because TCP must preserve byte ordering.

---

## 11. Head-of-Line Blocking

This is called:

**Head-of-Line (HOL) blocking**

Imagine:

```
HTTP/2

Stream A ──┐
           │
Stream B ──┼── TCP
           │
Stream C ──┘
```

Suppose data for Stream A is lost at the TCP layer:

```
Stream A → Packet LOST
Stream B → Packet received
Stream C → Packet received
```

Even though B and C's TCP packets arrived, TCP's ordered byte stream means the application may have to wait for the missing bytes.

```
Stream A
   ↓
   ❌ lost
   ↓
TCP waits for retransmission
   ↓
Stream B/C data delivery delayed
```

This is one of the reasons HTTP/3 was developed.

---

## 12. Enter HTTP/3

HTTP/3 changes the underlying transport.

Instead of:

```
HTTP/2
   ↓
TLS
   ↓
TCP
```

HTTP/3 uses:

```
HTTP/3
   ↓
QUIC
   ↓
UDP
```

This is the fundamental architectural change.

---

## 13. What is QUIC?

QUIC is a modern transport protocol originally developed at Google and standardized by the IETF.

It provides many transport features we associate with TCP:

- Reliable delivery
- Congestion control
- Flow control
- Stream multiplexing

while using UDP underneath.

And importantly:

```
QUIC
 +
TLS 1.3 integration
```

provides encrypted transport.

So don't think:

> "HTTP/3 uses unreliable UDP."

Instead:

> HTTP/3 uses QUIC, and QUIC uses UDP as its underlying packet transport. QUIC itself provides reliability, congestion control, multiplexed streams, and security.

---

## 14. HTTP/3 Architecture

```
┌─────────────────────┐
│       HTTP/3        │
├─────────────────────┤
│        QUIC         │
│                     │
│ Reliability         │
│ Flow control        │
│ Congestion control  │
│ Multiplexing        │
│ TLS 1.3             │
├─────────────────────┤
│        UDP          │
├─────────────────────┤
│         IP          │
└─────────────────────┘
```

Compare that with HTTP/2:

```
┌─────────────────────┐
│       HTTP/2        │
├─────────────────────┤
│        TLS          │
├─────────────────────┤
│        TCP          │
├─────────────────────┤
│         IP          │
└─────────────────────┘
```

---

## 15. HTTP/3 Solves the TCP HOL Problem

Suppose:

```
Stream A → Packet lost
Stream B → Packet received
Stream C → Packet received
```

**With HTTP/2 + TCP:**

```
TCP
 ↓
Packet A lost
 ↓
TCP waits for A
 ↓
B/C can be delayed
```

**With HTTP/3 + QUIC:**

```
QUIC

Stream A → Packet lost
Stream B → Packet received → Deliver B
Stream C → Packet received → Deliver C
```

The loss in Stream A doesn't necessarily block delivery of independent streams.

This is a major advantage of QUIC's stream model.

---

## 16. HTTP/3 Also Improves Connection Establishment

Traditional HTTPS:

```
TCP handshake
      ↓
TLS handshake
      ↓
HTTP
```

That's separate transport and security setup.

QUIC integrates TLS 1.3 into the transport establishment.

Conceptually:

```
QUIC connection establishment
          +
       TLS 1.3
          ↓
Secure QUIC connection
          ↓
HTTP/3
```

This can reduce connection setup latency.

---

## 17. HTTP/3 Connection Migration

This is another very useful QUIC feature.

Imagine you're using your phone:

```
Wi-Fi
 ↓
IP address A
```

Then you move outside:

```
Mobile Network
 ↓
IP address B
```

With traditional TCP, changing the client's network address generally means the old TCP connection cannot simply continue as-is.

QUIC uses a **connection ID**, allowing a connection to survive certain network-path changes.

Conceptually:

```
Wi-Fi
  |
  | Connection ID = XYZ
  ↓
QUIC Server


Switch network


Mobile
  |
  | Connection ID = XYZ
  ↓
QUIC Server
```

The connection can potentially continue without starting completely from scratch.

This is especially useful for mobile devices.

---

## 18. HTTP/1.1 vs HTTP/2 vs HTTP/3

Here's the table you should know for interviews:

| Feature | HTTP/1.1 | HTTP/2 | HTTP/3 |
|---|---|---|---|
| Transport | TCP | TCP | QUIC/UDP |
| Encryption | Optional | Usually TLS | TLS 1.3 integrated with QUIC |
| Format | Textual | Binary frames | Binary frames |
| Multiplexing | Limited | ✅ Yes | ✅ Yes |
| Header compression | No standardized mechanism like HTTP/2's HPACK | HPACK | QPACK |
| TCP HOL blocking | N/A | ✅ Yes | ❌ Avoided at transport stream level |
| Connection migration | No | No | ✅ Yes |
| Main goal | Basic web communication | Efficient multiplexed HTTP | Modern low-latency transport |

---

## 19. The Evolution

This is the picture I want you to remember:

```
                 HTTP/1.1
                    |
                    |
             Multiple requests
             / multiple TCP
             connections
                    |
                    ↓
                 HTTP/2
                    |
                    |
              Multiplexing
                    +
             Header compression
                    |
                    ↓
              But TCP remains
                    |
                    ↓
             TCP HOL blocking
                    |
                    ↓
                 HTTP/3
                    |
                    ↓
                  QUIC
                    |
                    ↓
                  UDP
```

---

## 20. Real-World Example

Imagine loading an e-commerce website:

```
https://shop.com
```

The browser needs:

```
index.html
app.js
style.css
logo.png
product1.jpg
product2.jpg
reviews.json
```

### HTTP/1.1

Simplified:

```
TCP connection 1
 ├── HTML
 └── CSS

TCP connection 2
 ├── JS
 └── Logo

TCP connection 3
 ├── Product 1
 └── Product 2
```

Multiple connections can be used to improve concurrency.

### HTTP/2

```
ONE TCP CONNECTION

├── Stream 1 → HTML
├── Stream 3 → CSS
├── Stream 5 → JS
├── Stream 7 → Logo
├── Stream 9 → Product 1
└── Stream 11 → Product 2
```

Much better multiplexing.

But:

```
One TCP connection
        ↓
One ordered byte stream
        ↓
Packet loss
        ↓
Potential HOL blocking
```

### HTTP/3

```
ONE QUIC CONNECTION

├── Stream 1 → HTML
├── Stream 3 → CSS
├── Stream 5 → JS
├── Stream 7 → Logo
├── Stream 9 → Product 1
└── Stream 11 → Product 2
```

But now:

```
HTTP/3
   ↓
QUIC
   ↓
Independent streams
   ↓
UDP
```

Loss on one stream doesn't inherently block unrelated streams in the same way TCP does.

---

## 21. HTTP/2 and HTTP/3 Both Multiplex

This is important.

Don't say:

> "HTTP/3 introduced multiplexing."

That's incorrect.

**HTTP/2 already has multiplexing.**

The major HTTP/3 improvement is that multiplexed streams are built on QUIC, avoiding TCP's single ordered byte-stream limitation.

```
HTTP/2:

Multiplexing
    +
TCP
    ↓
TCP HOL problem


HTTP/3:

Multiplexing
    +
QUIC
    ↓
Independent streams
```

---

## 22. Header Compression Comparison

**HTTP/2:**

```
HPACK
```

**HTTP/3:**

```
QPACK
```

Why a different mechanism?

Because HTTP/3 runs over QUIC, which has different stream and ordering characteristics than TCP.

You don't need to go deeply into HPACK/QPACK initially.

For system design, remember:

```
HTTP/1.1 → no HPACK/QPACK
HTTP/2   → HPACK
HTTP/3   → QPACK
```

---

## 23. HTTP/3 Does NOT Mean "HTTP Without TCP but Everything Else is Same"

That's another common misconception.

HTTP/3 changes the architecture substantially:

**HTTP/2:**

```
HTTP
 ↓
TLS
 ↓
TCP
 ↓
IP
```

**HTTP/3:**

```
HTTP
 ↓
QUIC
 ↓
UDP
 ↓
IP
```

QUIC itself provides:

```
Reliability
+
Congestion control
+
Flow control
+
Multiplexing
+
TLS 1.3 security
+
Connection migration
```

So QUIC is doing a lot of the work that historically involved TCP + TLS.

---

## 24. A Key Interview Question

> **"Why was HTTP/3 introduced when HTTP/2 already had multiplexing?"**

Excellent answer:

> "HTTP/2 solved a major HTTP-level problem by multiplexing multiple streams over a single TCP connection. However, TCP still provides one ordered byte stream. If a TCP packet is lost, subsequent bytes may be blocked until retransmission, causing transport-level head-of-line blocking across HTTP/2 streams. HTTP/3 uses QUIC over UDP, where streams are independently multiplexed, so loss in one stream doesn't inherently block unrelated streams. QUIC also integrates TLS 1.3 and supports features such as connection migration."

That's an interview-quality answer.

---

## 25. Another Important Interview Question

> **"Why UDP for HTTP/3?"**

Don't answer:

> "Because UDP is faster."

Better answer:

> "UDP provides a minimal datagram substrate without TCP's connection and ordered-byte-stream semantics. QUIC builds the transport features required by modern applications—reliability, congestion control, flow control, multiplexed streams, and TLS 1.3 security—on top of UDP. This allows QUIC to avoid TCP's transport-level head-of-line blocking and support features such as connection migration."

---

## 26. Complete Picture

Now connect everything we've covered:

```
                         CLIENT
                            |
                            ↓
                       HTTP Request
                            |
             ┌──────────────┴──────────────┐
             │                             │
          HTTP/1.1                       HTTP/2
             │                             │
             ↓                             ↓
            TLS                           TLS
             ↓                             ↓
            TCP                           TCP
             ↓                             ↓
            IP                            IP


                         HTTP/3
                            |
                            ↓
                          QUIC
                            |
                     ┌──────┴──────┐
                     │             │
                   TLS 1.3      Streams
                     │             │
                     └──────┬──────┘
                            ↓
                           UDP
                            ↓
                            IP
```

---

## 27. What You Should Memorize

For your System Design interview preparation, these are the most important points:

### HTTP/1.1

```
HTTP/1.1
   ↓
TCP
```

- Text-based message syntax
- Persistent connections
- Limited request concurrency per connection
- Browsers commonly used multiple TCP connections

### HTTP/2

```
HTTP/2
   ↓
TLS
   ↓
TCP
```

- Binary framing
- Multiplexing
- Streams
- HPACK header compression
- One connection can carry many requests
- Still suffers from TCP-level HOL blocking

### HTTP/3

```
HTTP/3
   ↓
QUIC
   ↓
UDP
```

- Binary framing
- Multiplexing
- QPACK
- QUIC transport
- Avoids TCP-level HOL blocking between independent streams
- TLS 1.3 integrated into QUIC
- Connection migration

---

## The One Diagram I'd Memorize

```
             HTTP/1.1
                 │
                 ↓
                TCP
                 │
                 ↓
                IP


              HTTP/2
                 │
                 ↓
                TLS
                 │
                 ↓
                TCP
                 │
                 ↓
                IP


              HTTP/3
                 │
                 ↓
               QUIC
          ┌──────┼──────┐
          │      │      │
       TLS 1.3 Streams Reliability
          │      │      │
          └──────┼──────┘
                 ↓
                UDP
                 │
                 ↓
                IP
```

**In one sentence:**

> HTTP/1.1 improved basic web communication, HTTP/2 made HTTP much more efficient through multiplexing over TCP, and HTTP/3 moved that multiplexed HTTP model onto QUIC over UDP to overcome TCP's transport-level limitations and provide a more modern connection model.
