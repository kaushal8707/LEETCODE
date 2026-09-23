# HTTP vs HTTPS

Since you've just learned TCP/UDP and TLS Handshake, this is a good point to connect everything together.

The simplest definition is:

> **HTTPS = HTTP + TLS**

HTTP and HTTPS both define how a client and server communicate at the application level. The major difference is that HTTPS protects HTTP traffic using TLS.

---

## 1. HTTP

**HTTP = HyperText Transfer Protocol**

Suppose your browser requests:

```
GET /users/101 HTTP/1.1
Host: example.com
```

With HTTP, the data is sent without TLS encryption.

Conceptually:

```
Browser
   |
   | HTTP Request
   |
   ↓
Internet
   |
   ↓
Server
```

The network traffic can potentially be observed or modified by an attacker who is in a position to intercept it.

---

## 2. HTTPS

**HTTPS = HTTP Secure**

HTTPS adds TLS:

```
Browser
   |
   | HTTP
   ↓
  TLS
   ↓
  TCP
   ↓
  IP
   ↓
Internet
```

So the HTTP request:

```
GET /users/101 HTTP/1.1
Host: example.com
```

is protected by TLS before it travels across the network.

An observer sees encrypted TLS records rather than the HTTP contents.

---

## 3. Main Difference

| Feature | HTTP | HTTPS |
|---|---|---|
| Security | ❌ No TLS protection | ✅ TLS protected |
| Encryption | ❌ No | ✅ Yes |
| Data confidentiality | ❌ No | ✅ Yes |
| Data integrity | ❌ No cryptographic protection | ✅ Yes |
| Server authentication | ❌ No | ✅ Via TLS certificate |
| Default port | 80 | 443 |
| TLS handshake | ❌ No | ✅ Yes |
| Performance | Slightly less overhead | Small TLS overhead, usually negligible |
| Modern websites | Rarely appropriate | Standard choice |

---

## 4. Port Difference

By convention:

```
HTTP
 ↓
Port 80
```

and:

```
HTTPS
 ↓
Port 443
```

For example:

```
http://example.com
```

usually means:

```
example.com:80
```

while:

```
https://example.com
```

usually means:

```
example.com:443
```

These are conventions; servers can technically use other ports.

---

## 5. How HTTP Works

Suppose you visit:

```
http://example.com
```

The simplified flow is:

```
Browser
   |
   | TCP connection
   ↓
Server
   |
   | HTTP Request
   ↓
Server
   |
   | HTTP Response
   ↓
Browser
```

For example:

```
GET /products HTTP/1.1
Host: example.com
```

The response might be:

```
HTTP/1.1 200 OK

[
  {
    "id": 101,
    "name": "Laptop"
  }
]
```

There is no TLS layer.

---

## 6. How HTTPS Works

Now:

```
https://example.com
```

The flow becomes:

```
Browser
   |
   | TCP Handshake
   ↓
Server
   |
   | TLS Handshake
   ↓
Server + Client establish
secure session keys
   |
   ↓
Encrypted HTTP
   |
   ↓
Server
```

The HTTP request is still conceptually:

```
GET /products HTTP/1.1
```

But it is protected by TLS while traveling across the network.

---

## 7. HTTPS Doesn't Replace HTTP

This is a very important concept.

HTTPS doesn't mean there is a completely different application protocol replacing HTTP.

Instead:

```
HTTP
 +
TLS
 =
HTTPS
```

Think of TLS as a **secure wrapper** around HTTP.

Without HTTPS:

```
HTTP
 ↓
TCP
 ↓
IP
```

With HTTPS:

```
HTTP
 ↓
TLS
 ↓
TCP
 ↓
IP
```

---

## 8. Why HTTP is Insecure

Imagine you're logging into a website.

You send:

```
POST /login

username=kaushal
password=secret123
```

With plain HTTP, an attacker positioned to observe the traffic could potentially see the contents.

```
Browser
   |
   | username=kaushal
   | password=secret123
   |
   ↓
Attacker
   |
   ↓
Server
```

That's obviously dangerous.

With HTTPS:

```
Browser
   |
   | 🔒 Encrypted
   |
   ↓
Attacker
   |
   | Can't practically read contents
   |
   ↓
Server
```

---

## 9. HTTPS Provides Three Major Security Properties

This is worth remembering for interviews.

### A. Confidentiality

The data is encrypted.

HTTP:

```
password=secret123
```

becomes something like:

TLS encrypted data:

```
8A 91 3F 72 C4 ...
```

An observer can't practically recover the original data without the required keys.

### B. Integrity

TLS detects unauthorized modification.

Suppose you send:

```
Transfer ₹100
```

An attacker tries to change it to:

```
Transfer ₹10,000
```

TLS's authenticated encryption allows the receiver to detect that the protected data was modified.

### C. Authentication

The server presents a certificate.

For example:

```
Certificate
   |
   ├── Domain: example.com
   ├── Public Key
   ├── Issuer
   ├── Validity
   └── Digital Signature
```

The browser validates the certificate and the server's proof of possession of the corresponding private key.

This helps prevent an attacker from simply pretending to be the legitimate website.

---

## 10. HTTPS and the TLS Handshake

This directly connects to your previous question.

When HTTPS starts, the TLS handshake occurs before protected HTTP application data is exchanged.

Simplified:

```
Client                              Server
  |                                   |
  |-------- ClientHello ------------> |
  |                                   |
  | <------- ServerHello ------------ |
  | <------- Certificate ------------ |
  | <------- CertificateVerify ------ |
  | <------- Finished -------------- |
  |                                   |
  | -------- Finished -------------> |
  |                                   |
  |                                   |
  | ===== Encrypted HTTP ==========> |
  |                                   |
```

During this process:

- Client and server negotiate TLS parameters.
- Server provides its certificate.
- Client validates the certificate.
- They perform key agreement.
- Both derive symmetric traffic keys.
- HTTP data can then be securely exchanged.

---

## 11. Important: HTTP Itself Doesn't Encrypt

People sometimes say:

> "HTTPS encrypts HTTP."

More precisely:

```
HTTP
  ↓
produces HTTP messages

TLS
  ↓
protects those messages in transit
```

So the application still works with:

```
GET /orders
POST /payment
PUT /user
DELETE /product
```

TLS protects the transport of that application data.

---

## 12. HTTP vs HTTPS Architecture

### HTTP

```
             Client
                |
                |
              HTTP
                |
                ↓
              TCP
                |
                ↓
               IP
                |
                ↓
             Server
```

### HTTPS

```
             Client
                |
                |
              HTTP
                |
                ↓
              TLS
                |
                ↓
              TCP
                |
                ↓
               IP
                |
                ↓
             Server
```

This is one of the most important diagrams to remember.

---

## 13. HTTPS and TCP

Since you just learned TCP:

Traditional HTTPS commonly uses:

```
HTTP
 ↓
TLS
 ↓
TCP
 ↓
IP
```

There are therefore potentially two important handshakes when establishing a traditional HTTPS connection:

**TCP handshake**

```
SYN
 ↓
SYN-ACK
 ↓
ACK
```

Then:

**TLS handshake**

```
ClientHello
 ↓
ServerHello
 ↓
Certificate
 ↓
Key agreement
 ↓
Finished
```

Then:

**Encrypted HTTP**

So:

> TCP establishes the transport connection; TLS establishes the secure session; HTTP carries the application data.

---

## 14. What Happens When You Type an HTTPS URL?

Suppose:

```
https://example.com/products
```

A simplified sequence is:

```
1. Browser parses URL
          ↓
2. DNS resolves example.com
          ↓
3. Browser gets server IP
          ↓
4. TCP connection established
          ↓
5. TLS handshake
          ↓
6. Certificate validated
          ↓
7. Session keys established
          ↓
8. HTTP request encrypted
          ↓
9. Server decrypts request
          ↓
10. Server processes request
          ↓
11. HTTP response encrypted
          ↓
12. Browser decrypts response
```

---

## 15. HTTPS and Load Balancers

This becomes very important in system design.

A typical architecture might be:

```
                   Internet
                       |
                       |
                    HTTPS
                       |
                       ↓
                Load Balancer
                       |
              TLS Termination
                       |
          ┌────────────┼────────────┐
          ↓            ↓            ↓
      Server 1      Server 2      Server 3
```

The load balancer can terminate TLS.

Meaning:

```
Client
  |
  | HTTPS
  ↓
Load Balancer
  |
  | HTTP / HTTPS
  ↓
Backend
```

If the LB forwards HTTPS to the backend:

```
Client
  |
 HTTPS
  ↓
Load Balancer
  |
 HTTPS
  ↓
Backend
```

This can provide encryption on both network segments.

---

## 16. HTTP vs HTTPS Performance

Historically people sometimes said:

> "HTTPS is slower."

There is some overhead because TLS requires cryptographic work and connection establishment.

But modern TLS implementations, persistent connections, session resumption, HTTP/2, HTTP/3, and hardware acceleration make the overhead generally small relative to the security benefits.

More importantly, HTTPS enables modern HTTP features and is the normal expectation for public web traffic.

So in system design:

> Don't choose HTTP simply because it is "faster." Use HTTPS for traffic that needs confidentiality, integrity, and authentication.

---

## 17. HTTPS Doesn't Protect Everything

HTTPS protects the communication between the TLS endpoints.

For example:

```
Client
   |
   | 🔒 HTTPS
   ↓
Load Balancer
   |
   | HTTP
   ↓
Backend
```

The first segment is protected.

The second isn't.

If your architecture requires encryption throughout the internal network, you might use:

```
Client
   |
 HTTPS
   ↓
Load Balancer
   |
 HTTPS
   ↓
Service A
   |
 HTTPS
   ↓
Service B
```

This is related to **mTLS (mutual TLS)** and service-to-service security.

---

## 18. HTTPS vs HTTP — Interview Answer

If an interviewer asks:

> "What is the difference between HTTP and HTTPS?"

You can answer:

> "HTTP is an application-layer protocol used for communication between clients and servers. HTTPS is HTTP running over TLS. HTTP by itself doesn't provide encryption, authentication, or cryptographic integrity protection. HTTPS establishes a TLS session, validates the server's certificate, performs key agreement, and then protects HTTP traffic using symmetric authenticated encryption. HTTP traditionally uses port 80, while HTTPS traditionally uses port 443."

That's a strong interview answer.

---

## 19. Connect Everything You've Learned So Far

You have now covered several pieces of the networking stack.

Think of a request like this:

```
                 USER
                   |
                   ↓
              Web Browser
                   |
                   ↓
                 HTTP
           "GET /orders"
                   |
                   ↓
                  TLS
       "Encrypt + Authenticate"
                   |
                   ↓
                 TCP
        "Reliable byte stream"
                   |
                   ↓
                  IP
             "Find destination"
                   |
                   ↓
             Internet
                   |
                   ↓
                Server
```

And for HTTP/3:

```
              HTTP/3
                 ↓
               QUIC
                 ↓
                UDP
                 ↓
                 IP
                 ↓
             Internet
```

---

## 20. The 5 Things to Remember

If you're preparing for system design interviews, remember these:

### ① HTTP

Application protocol

### ② HTTPS

HTTP + TLS

### ③ TLS

- Authentication
- Key establishment
- Encryption
- Integrity

### ④ TCP

- Reliable
- Ordered
- Connection-oriented
- Byte stream

### ⑤ UDP

- Connectionless
- Datagrams
- No built-in reliability/order
- Lower protocol overhead

And the complete traditional HTTPS stack:

```
┌───────────────────┐
│       HTTP        │
├───────────────────┤
│       TLS         │
├───────────────────┤
│       TCP         │
├───────────────────┤
│        IP         │
├───────────────────┤
│ Ethernet / Wi-Fi  │
└───────────────────┘
```

**One-line mental model:**

> HTTP = communication, TLS = security, TCP = reliable transport, IP = addressing/routing.
