# TLS (Transport Layer Security)

**TLS** is a cryptographic protocol that secures communication between two systems over a network.

In simple terms:

> TLS provides encryption, authentication, and integrity for data traveling between a client and server.

For example, when you access:

```
https://example.com
```

HTTPS is essentially:

```
HTTP + TLS
```

So:

```
HTTP
  +
TLS
  ↓
HTTPS
```

---

## 1. Why do we need TLS?

Imagine you're using an API:

```
POST /login

username=kaushal
password=secret123
```

**Without encryption:**

```
Client
   │
   │ username=kaushal
   │ password=secret123
   ▼
Internet
   │
   ▼
Server
```

An attacker monitoring the network could potentially see the data.

**With TLS:**

```
Client
   │
   │ 🔒 encrypted data
   ▼
Internet
   │
   │ 🔒 encrypted data
   ▼
Server
```

The attacker may see packets, but shouldn't be able to read or modify the protected application data.

---

## 2. What security does TLS provide?

TLS primarily provides three important properties.

### 1. Confidentiality

Data is encrypted.

```
Hello
  ↓
Encryption
  ↓
x8F$kP92@...
```

An attacker shouldn't be able to understand the original message.

### 2. Integrity

TLS detects whether protected data has been modified in transit.

```
Client
  │
  │ "Transfer $100"
  ▼
Attacker
  │
  │ tries to modify
  ▼
Server
```

The modification should be detected and the connection/request rejected.

### 3. Authentication

TLS allows the client to verify that it is communicating with the intended server.

For example:

```
Client
   │
   │ "Are you really example.com?"
   ▼
Server
   │
   │ Certificate
   ▼
Client
```

The client validates the server's certificate.

---

## 3. TLS vs SSL

You may hear:

- SSL
- TLS

Historically, SSL came first.

```
SSL 2.0
SSL 3.0
   ↓
TLS 1.0
TLS 1.1
TLS 1.2
TLS 1.3
```

Modern systems should use TLS, not obsolete SSL versions.

Today, **TLS 1.2** and especially **TLS 1.3** are the important versions to know.

---

## 4. Where does TLS fit?

Consider:

```
Application
    │
    │ HTTP
    ▼
   TLS
    │
    ▼
   TCP
    │
    ▼
   IP
    │
    ▼
 Network
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

TLS sits between the application protocol and the transport layer in the traditional TCP model.

---

## 5. How TLS works

The most important concept is the:

> **TLS Handshake**

Before sending normal application data, the client and server establish a secure connection.

Simplified:

```
Client                                  Server
  │                                       │
  │──── ClientHello ────────────────────►│
  │                                       │
  │◄─── ServerHello + Certificate ──────│
  │                                       │
  │     Key Exchange                     │
  │◄────────────────────────────────────►│
  │                                       │
  │════ Encrypted Communication ═════════│
```

Let's understand each step.

---

## 6. Step 1 — ClientHello

The client starts the TLS handshake.

For example:

```
Client
   │
   │ ClientHello
   ▼
Server
```

The ClientHello can contain information such as:

- TLS versions supported
- Supported cipher suites
- Random value
- Key-share information
- Extensions
- SNI
- ALPN

For example:

```
Supported TLS:
    TLS 1.3
    TLS 1.2

Supported cipher suites:
    TLS_AES_256_GCM_SHA384
    TLS_CHACHA20_POLY1305_SHA256
```

The client is essentially saying:

> "Here are the TLS capabilities I support."

---

## 7. SNI — Server Name Indication

Suppose one server hosts:

```
example.com
api.example.com
payments.example.com
```

The server needs to know which hostname the client wants.

The client can include:

```
SNI = api.example.com
```

This allows the server/load balancer to select the appropriate certificate.

```
Client
  │
  │ SNI: api.example.com
  ▼
Load Balancer
  │
  ├── example.com certificate
  ├── api.example.com certificate
  └── payments.example.com certificate
```

---

## 8. Step 2 — ServerHello

The server responds.

```
Client                         Server
  │                              │
  │──── ClientHello ────────────►│
  │                              │
  │◄──── ServerHello ───────────│
```

The server chooses compatible cryptographic parameters.

For TLS 1.3, the server also sends its key-share information and certificate during the handshake.

---

## 9. Step 3 — Server Certificate

The server sends its digital certificate.

Example conceptually:

```
Certificate
--------------------------------
Domain: example.com
Issuer: DigiCert
Public Key: ...
Validity: ...
Signature: ...
```

The certificate binds:

```
Domain
   +
Public Key
```

The certificate is signed by a trusted **Certificate Authority (CA)**, directly or through an intermediate CA.

---

## 10. What is a Certificate Authority?

A **Certificate Authority** is a trusted entity that issues/signs certificates.

Examples include organizations such as:

- DigiCert
- Let's Encrypt
- GlobalSign
- Sectigo

The browser has a **trust store** containing trusted root CA certificates.

Conceptually:

```
              Root CA
                 │
                 │ signs
                 ▼
          Intermediate CA
                 │
                 │ signs
                 ▼
        example.com Certificate
                 │
                 ▼
              Browser
```

The browser verifies the certificate chain.

---

## 11. How does the browser trust the certificate?

Suppose the server sends:

```
example.com certificate
```

The browser checks things such as:

- Is certificate expired?
- Is hostname correct?
- Is the certificate signature valid?
- Is the issuing CA trusted?
- Is the chain valid?
- Is the certificate allowed for this purpose?

If valid:

```
Certificate ✅
```

If not:

```
Certificate ❌
```

The browser may display a security warning.

---

## 12. Public Key and Private Key

The server has a key pair:

- Public Key
- Private Key

The public key can be distributed through the certificate.

The private key must remain secret.

```
Server
 ├── Public Key → certificate → clients
 │
 └── Private Key → NEVER share
```

The private key is used for cryptographic operations associated with proving possession of the server identity.

---

## 13. Does TLS encrypt everything using the server's public key?

This is a common misconception.

> No.

Modern TLS does **not** use the server's public/private key pair to encrypt every HTTP request.

Instead, TLS typically uses asymmetric cryptography for authentication/key establishment and symmetric cryptography for the actual application data.

Why?

Symmetric encryption is much faster.

```
Asymmetric crypto
     ↓
Authentication / key establishment

Symmetric crypto
     ↓
Actual application data
```

---

## 14. Key Exchange

The client and server need to establish shared secret key material.

Modern TLS commonly uses ephemeral Diffie-Hellman, such as **ECDHE**.

Conceptually:

```
Client                              Server
  │                                   │
  │ Client key share                  │
  │──────────────────────────────────►│
  │                                   │
  │ Server key share                  │
  │◄──────────────────────────────────│
  │                                   │
  │                                   │
  └──── derive shared secret ─────────┘
```

Both sides independently derive the same secret.

An observer watching the network should not be able to derive that secret from the exchanged public information.

---

## 15. Diffie-Hellman mental model

Very simplified:

```
Client                         Server

Private value A                Private value B
     │                              │
     ▼                              ▼
Public value A'                  Public value B'
     │                              │
     └────────── exchange ──────────┘
                 │
                 ▼
          Shared Secret
```

The important point is:

```
Client → derives secret
Server → derives same secret
Attacker → cannot derive secret
```

Modern TLS uses carefully designed elliptic-curve variants rather than this simplified arithmetic model.

---

## 16. Session Keys

From the handshake/key schedule, TLS derives symmetric keys.

Conceptually:

```
Shared secret
      │
      ▼
TLS Key Schedule
      │
      ├── Client → Server key
      │
      └── Server → Client key
```

Now application data can be encrypted efficiently.

---

## 17. Encrypted Application Data

After the handshake:

```
Client
   │
   │ HTTP Request
   │
   ▼
TLS Encryption
   │
   ▼
Encrypted TLS Record
   │
   ▼
Internet
   │
   ▼
Server
   │
   ▼
TLS Decryption
   │
   ▼
HTTP Request
```

For example, the application may send:

```
GET /accounts/123
Authorization: Bearer abc...
```

But an attacker observing the network sees encrypted TLS records rather than the plaintext HTTP contents.

---

## 18. Encryption + Integrity

Modern TLS uses **authenticated encryption**, such as:

- AES-GCM
- ChaCha20-Poly1305

These provide confidentiality and integrity/authentication of the protected records.

Conceptually:

```
Plaintext
   │
   ▼
Encryption + Authentication
   │
   ▼
Ciphertext + Authentication Tag
```

If an attacker modifies the protected ciphertext:

```
Ciphertext
    │
    ▼
Integrity verification
    │
    ▼
FAIL ❌
```

The modified data is not accepted as valid TLS application data.

---

## 19. TLS 1.2 vs TLS 1.3

For interviews, know the high-level difference.

### TLS 1.2

Handshake is more complex and can require additional round trips depending on the negotiation.

```
Client
  │
  │ ClientHello
  ▼
Server
  │
  │ ServerHello + Certificate
  │ ...
  ▼
Client
  │
  │ Key exchange / Finished
  ▼
Server
  │
  ▼
Encrypted data
```

### TLS 1.3

TLS 1.3 simplified the handshake and generally reduces latency.

Conceptually:

```
Client                         Server
  │                              │
  │ ClientHello + KeyShare       │
  │─────────────────────────────►│
  │                              │
  │ ServerHello + KeyShare       │
  │ Certificate + Finished       │
  │◄─────────────────────────────│
  │                              │
  │════ Encrypted Data ══════════│
```

TLS 1.3 also removed several older/insecure cryptographic options.

---

## 20. TLS and HTTPS

This is important:

```
HTTP
 +
TLS
 =
HTTPS
```

For example:

```
http://example.com
```

doesn't provide TLS protection.

Whereas:

```
https://example.com
```

uses TLS.

Architecture:

```
Browser
   │
   │ HTTPS
   ▼
┌─────────────┐
│     TLS     │
├─────────────┤
│     HTTP    │
└─────────────┘
       │
       ▼
     Server
```

---

## 21. TLS and API Security

TLS is one layer of API security.

For example:

```
                    API Security
                         │
        ┌────────────────┼─────────────────┐
        │                │                 │
        ▼                ▼                 ▼
       TLS         Authentication    Authorization
        │                │                 │
        ▼                ▼                 ▼
 Encryption          JWT/OIDC          RBAC/Scopes
```

TLS protects the communication channel.

Authentication determines:

> Who are you?

Authorization determines:

> What are you allowed to do?

TLS does not replace authentication or authorization.

---

## 22. TLS Termination

In a production architecture, TLS doesn't necessarily terminate directly at your application.

For example:

```
Internet
   │
   │ HTTPS
   ▼
┌──────────────┐
│Load Balancer │
│ TLS Terminate│
└──────┬───────┘
       │
       │ HTTP or HTTPS
       ▼
┌──────────────┐
│ API Gateway  │
└──────┬───────┘
       │
       ▼
    Services
```

The load balancer can terminate TLS.

However, sending plaintext traffic inside a trusted network isn't automatically safe.

For sensitive environments, you might use:

```
Client
  │ HTTPS
  ▼
Load Balancer
  │ HTTPS
  ▼
API Gateway
  │ HTTPS/mTLS
  ▼
Service
```

This is sometimes called **TLS re-encryption** or **end-to-end encryption** through multiple TLS hops, depending on the architecture.

---

## 23. TLS vs mTLS

**Normal TLS:**

```
Client ───────────────► Server
          Server
        authenticates
```

The client verifies the server.

**With mTLS:**

```
Client ◄──────────────► Server
   │                      │
Client certificate    Server certificate
```

Both sides authenticate each other.

This is especially useful for:

- Microservice → Microservice
- Service mesh
- Banking systems
- Internal highly trusted APIs

---

## 24. TLS in Microservices

Suppose you have:

```
                  API Gateway
                       │
              HTTPS / mTLS
                       │
                       ▼
                Order Service
                       │
                HTTPS / mTLS
                       │
                       ▼
               Payment Service
```

TLS protects each network connection.

mTLS can additionally provide:

```
Service A identity
        +
Service B identity
```

This is particularly useful when you don't want to rely solely on network location as a trust boundary.

---

## 25. Forward Secrecy

Another important interview concept.

Modern TLS configurations using ephemeral key exchange provide **forward secrecy**.

Imagine an attacker records traffic today:

```
Encrypted traffic
Encrypted traffic
Encrypted traffic
```

Suppose the server's long-term private key is compromised later.

With forward secrecy, the attacker should still not be able to use that long-term key alone to decrypt previously recorded sessions.

Conceptually:

```
Past Session
     │
     ▼
Ephemeral Session Key
     │
     ▼
Destroyed after session
```

This is one reason ephemeral Diffie-Hellman is important.

---

## 26. What an attacker can still see

TLS encrypts application data, but it doesn't make the network completely invisible.

Depending on the protocol, configuration, and network observation point, an attacker may still learn metadata such as:

- IP addresses
- Approximate traffic volume
- Timing
- Packet sizes
- Some connection information

TLS primarily protects the contents and integrity of the protected connection.

---

## 27. Complete TLS flow

Here's the complete simplified picture:

```
                         TLS HANDSHAKE

Client                                             Server
  │                                                  │
  │────── ClientHello ──────────────────────────────►│
  │       TLS versions                              │
  │       Cipher suites                             │
  │       Key share                                 │
  │       SNI                                       │
  │                                                  │
  │◄───── ServerHello + Certificate ────────────────│
  │       Selected parameters                       │
  │       Server key share                          │
  │       Certificate                               │
  │                                                  │
  │────── Certificate verification ────────────────►│
  │                                                  │
  │       Key exchange / TLS key schedule           │
  │◄───────────────────────────────────────────────►│
  │                                                  │
  │────── Finished ────────────────────────────────►│
  │◄───── Finished ────────────────────────────────│
  │                                                  │
  │══════════ ENCRYPTED APPLICATION DATA ══════════│
  │                                                  │
  │ GET /orders                                     │
  │ Authorization: Bearer ...                       │
  │                                                  │
  │═════════════════════════════════════════════════│
```

---

## 28. What happens when you type https://google.com?

A simplified view:

```
1. DNS
   │
   ▼
google.com → IP address

2. TCP connection
   │
   ▼
Client ↔ Server

3. TLS handshake
   │
   ├── ClientHello
   ├── ServerHello
   ├── Certificate
   ├── Key exchange
   └── Finished

4. Secure TLS session
   │
   ▼
5. HTTP request
   │
   ▼
GET /
```

So:

```
DNS
 ↓
TCP
 ↓
TLS
 ↓
HTTP
```

For HTTP/3, the transport is different:

```
DNS
 ↓
QUIC
 ↓
TLS 1.3
 ↓
HTTP/3
```

---

## 29. TLS in System Design

When designing a production system:

```
                     Internet
                         │
                       HTTPS
                         │
                         ▼
                     CDN / WAF
                         │
                       HTTPS
                         │
                         ▼
                   Load Balancer
                         │
                       HTTPS
                         │
                         ▼
                    API Gateway
                         │
                    HTTPS / mTLS
                         │
                         ▼
                    Microservice
                         │
                    HTTPS / mTLS
                         │
                         ▼
                     Database
```

You need to think about:

- Certificate management
- Certificate rotation
- TLS versions
- Cipher suites
- Private key protection
- TLS termination
- Internal encryption
- mTLS
- Forward secrecy
- Monitoring

---

## 30. Certificate Rotation

Certificates expire.

For example:

```
Certificate A
     │
     │ expires
     ▼
Certificate B
```

Production systems need automated certificate management.

Typical process:

```
Certificate Manager
       │
       ▼
Issue certificate
       │
       ▼
Deploy certificate
       │
       ▼
Rotate before expiry
       │
       ▼
Old certificate removed
```

This avoids outages caused by expired certificates.

---

## 31. TLS vs Encryption

Don't say:

> "TLS is just encryption."

That's incomplete.

TLS provides:

```
Encryption
+
Integrity
+
Authentication
+
Secure key establishment
```

A better interview statement is:

> TLS is a security protocol that establishes an authenticated and encrypted communication channel between two endpoints.

---

## 32. TLS vs JWT

These solve completely different problems.

```
TLS
 │
 └── Secures the communication channel

JWT
 │
 └── Carries claims/identity/authorization information
```

For example:

```
HTTPS
  │
  │ TLS encrypts this entire request
  ▼
Authorization: Bearer <JWT>
```

TLS protects the JWT while it travels over the network.

JWT itself doesn't replace TLS.

---

## 33. TLS vs OAuth/OIDC

Similarly:

```
TLS
 ↓
Secure communication channel

OAuth 2.0
 ↓
Authorization framework

OIDC
 ↓
Authentication/identity layer

JWT
 ↓
Token format
```

A modern API might use all of them together:

```
              Client
                 │
              HTTPS
                 │
                 ▼
        OAuth 2.0 / OIDC
                 │
                 ▼
          Access Token
                 │
              HTTPS
                 │
                 ▼
               API
```

---

## 34. Interview-ready answer

If an interviewer asks:

> "What is TLS and how does it work?"

A strong senior-level answer is:

> TLS, or Transport Layer Security, is a cryptographic protocol used to establish a secure communication channel between two endpoints. It provides confidentiality, integrity, and authentication. HTTPS is HTTP running over TLS.
>
> During the TLS handshake, the client sends a ClientHello containing supported TLS capabilities and key-share information. The server responds with the selected parameters, its key share, and its certificate. The client validates the certificate against its trusted CA chain and verifies that the certificate matches the requested hostname. Both sides then derive shared session keys, typically using ephemeral Diffie-Hellman key exchange. After the handshake, application data is encrypted and integrity-protected using efficient symmetric authenticated encryption such as AES-GCM or ChaCha20-Poly1305.
>
> Modern TLS, particularly TLS 1.3, reduces handshake latency and removes many legacy cryptographic options. In distributed systems, TLS can terminate at a load balancer or gateway, while mTLS can be used for stronger service-to-service authentication.

---

## 35. The most important mental model

Remember this:

```
                 TLS
                  │
       ┌──────────┼───────────┐
       │          │           │
       ▼          ▼           ▼
 Confidentiality Integrity Authentication
       │          │           │
       ▼          ▼           ▼
   Encrypt       Detect      Verify
   data          changes     server
       │
       └──────────┬──────────┘
                  ▼
          Secure Channel
```

And the full request path:

```
DNS
 ↓
TCP / QUIC
 ↓
TLS Handshake
 ↓
Session Keys
 ↓
Encrypted Communication
 ↓
HTTP / HTTP2 / HTTP3
 ↓
API Authentication
 ↓
API Authorization
 ↓
Business Logic
```

### One-line memory trick

> 🔐 TLS secures the connection; OAuth/OIDC establishes identity/authorization; JWT carries claims; API authorization decides what the caller can actually do.

