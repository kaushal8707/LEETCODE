# TCP / UDP — System Design From Scratch

Since you've understood HTTP/HTTPS and the TLS handshake, the next important layer is TCP and UDP.

The easiest way to understand them is:

> HTTP tells us what data to send. TCP/UDP tells us how that data is transported between machines.

---

## 1. Where TCP and UDP Fit

Think about the network stack:

```
┌─────────────────────────┐
│ Application             │
│ HTTP / HTTPS / DNS      │
├─────────────────────────┤
│ Transport               │
│ TCP / UDP               │  ← We are here
├─────────────────────────┤
│ Network                 │
│ IP                      │
├─────────────────────────┤
│ Data Link               │
│ Ethernet / Wi-Fi        │
└─────────────────────────┘
```

For a normal HTTPS website:

```
Browser
   ↓
HTTP
   ↓
TLS
   ↓
TCP
   ↓
IP
   ↓
Internet
```

For HTTP/3:

```
Browser
   ↓
HTTP/3
   ↓
QUIC
   ↓
UDP
   ↓
IP
```

---

## 2. What is TCP?

**TCP = Transmission Control Protocol**

TCP provides a **reliable, ordered, connection-oriented** byte stream between two endpoints.

Suppose you want to send:

```
HELLO
```

TCP makes sure the receiver gets the bytes in the correct order, assuming the connection eventually succeeds.

```
Sender                         Receiver

H ---------------------------->
E ---------------------------->
L ---------------------------->
L ---------------------------->
O ---------------------------->
```

If packets are lost or arrive out of order, TCP handles retransmission and reordering.

---

## 3. Why Do We Need TCP?

The Internet itself doesn't guarantee that every packet will arrive.

Imagine sending:

```
HELLO
```

It could travel as packets:

```
Packet 1 → H
Packet 2 → E
Packet 3 → L
Packet 4 → L
Packet 5 → O
```

Maybe packet 3 gets lost:

```
H → Receiver
E → Receiver
L → ❌ LOST
L → Receiver
O → Receiver
```

TCP detects the missing data and retransmits it.

```
H
E
L → LOST
L
O

       ↓

TCP retransmits missing L

       ↓

H E L L O
```

This reliability is one of TCP's biggest features.

---

## 4. TCP is Connection-Oriented

Before sending application data, TCP establishes a connection.

This is called the:

**TCP 3-way handshake**

```
Client                         Server
  |                              |
  | -------- SYN --------------> |
  |                              |
  | <----- SYN + ACK ----------- |
  |                              |
  | -------- ACK --------------> |
  |                              |
  |       Connection ready       |
```

After this:

```
Client ←──── TCP connection ────→ Server
```

Then application data can flow.

---

## 5. What is SYN?

**SYN = Synchronize**

The client basically says:

> "I'd like to establish a TCP connection."

For example:

```
Client → Server

SYN
Sequence Number = 1000
```

The sequence number is used by TCP to track bytes and ordering.

---

## 6. SYN + ACK

Server responds:

```
Server → Client

SYN + ACK
```

Meaning:

> "I received your request, and I also want to establish the connection."

The server provides its own sequence number.

Conceptually:

```
Client sequence = 1000

Server sequence = 5000
```

---

## 7. Final ACK

Client responds:

```
Client → Server

ACK
```

Now both sides know:

- Client knows server is reachable
- Server knows client is reachable

**TCP connection is established.**

---

## 8. TCP vs TLS Handshake

This is extremely important given what we just discussed.

They are **different handshakes**.

For HTTPS:

```
1. TCP handshake
       ↓
2. TLS handshake
       ↓
3. HTTP communication
```

For example:

```
Client                         Server
  |                              |
  | -------- SYN --------------> |
  | <----- SYN + ACK ----------- |
  | -------- ACK --------------> |
  |                              |
  |       TCP established        |
  |                              |
  | -------- ClientHello -------> |
  | <------- ServerHello -------- |
  | <------- Certificate -------- |
  | <------- Finished ----------- |
  | -------- Finished ----------> |
  |                              |
  |     TLS established          |
  |                              |
  | ===== Encrypted HTTP ======= |
```

So:

> TCP establishes reliable transport. TLS establishes secure communication.

---

## 9. What is UDP?

**UDP = User Datagram Protocol**

UDP is much simpler than TCP.

It sends independent datagrams **without establishing a connection first**.

Conceptually:

```
Client
  |
  | Packet
  ↓
Internet
  |
  ↓
Server
```

There is no TCP-style 3-way handshake.

---

## 10. UDP Doesn't Guarantee Delivery

Suppose:

```
Client → Packet 1 → Server
Client → Packet 2 → ❌ LOST
Client → Packet 3 → Server
```

UDP doesn't automatically retransmit Packet 2.

The application may never know that Packet 2 was lost unless the application/protocol implements its own mechanism.

---

## 11. UDP Doesn't Guarantee Ordering

Suppose sender sends:

```
1
2
3
4
```

Receiver could receive:

```
1
3
4
2
```

TCP handles ordering for you.

UDP doesn't.

---

## 12. TCP vs UDP — Simplest Comparison

| Feature | TCP | UDP |
|---|---|---|
| Connection | Connection-oriented | Connectionless |
| Handshake | Yes | No |
| Reliable delivery | Yes | No |
| Ordering | Yes | No |
| Retransmission | Yes | No |
| Flow control | Yes | No |
| Congestion control | Yes | No |
| Overhead | Higher | Lower |
| Speed/latency | Generally higher overhead | Generally lower overhead |
| Data model | Byte stream | Datagrams |
| Typical use | Web, APIs, databases | DNS, streaming, gaming, QUIC |

---

## 13. The Biggest Conceptual Difference

### TCP

Think:

> "Make sure the receiver gets the complete stream correctly."

### UDP

Think:

> "Send these packets; I'll handle reliability myself if I need it."

---

## 14. Real-World Example — Downloading a File

Suppose you're downloading:

```
movie.mp4
```

You need:

```
Byte 1
Byte 2
Byte 3
...
Byte 1,000,000
```

If some bytes are missing, the file may become corrupted.

TCP is a natural fit:

```
File
 ↓
TCP
 ↓
Reliable ordered byte stream
 ↓
Receiver
```

TCP handles:

- Lost packets
- Ordering
- Retransmission
- Flow control
- Congestion control

---

## 15. Real-World Example — Online Gaming

Imagine a game:

Player position:

```
X = 100
Y = 200
```

Then a few milliseconds later:

```
X = 105
Y = 203
```

If one position update is lost:

```
Position update #100 → received
Position update #101 → LOST
Position update #102 → received
```

You often don't want to pause the game and retransmit old position #101.

By the time it arrives:

```
Position #101
```

may already be outdated.

So low latency can be more important than perfect delivery.

**UDP can be useful here.**

---

## 16. Real-World Example — Voice/Video Call

Imagine:

```
You: "Hello..."
```

Packets carry audio/video frames.

Suppose one packet is lost.

With TCP:

```
Packet 100 → LOST
       ↓
TCP waits/retransmits
       ↓
Packet 100 arrives late
```

But the conversation may already have moved on.

You may hear:

```
"Hel... [pause] ...lo"
```

For real-time media, it's often preferable to tolerate some loss rather than introduce significant delay.

That's why real-time media commonly uses **UDP-based protocols**.

---

## 17. But UDP Isn't Automatically Faster

This is an important interview point.

People often say:

> "UDP is faster than TCP."

That's an oversimplification.

UDP has:

- No connection establishment
- No automatic retransmission
- No ordering
- Less protocol overhead

So applications can potentially achieve lower latency.

But if your application needs:

- Reliability
- Ordering
- Congestion control
- Retransmission

and you build all of that yourself over UDP, you may end up with a protocol that is as complex as—or more complex than—TCP.

---

## 18. TCP Provides a Byte Stream

Suppose application sends:

```
HELLO
```

TCP doesn't preserve application message boundaries.

If the application sends:

```
HELLO
WORLD
```

TCP sees a byte stream:

```
HELLOWORLD
```

The receiver might read:

```
HEL
LOWO
RLD
```

or:

```
HELLOWORLD
```

or:

```
HELLOW
ORLD
```

depending on how the application reads.

This is extremely important when writing TCP-based applications.

---

## 19. UDP Preserves Datagram Boundaries

With UDP:

```
send("HELLO")
send("WORLD")
```

The receiver gets individual datagrams:

```
Datagram 1 = HELLO
Datagram 2 = WORLD
```

So:

```
TCP → byte stream

UDP → datagrams/messages
```

---

## 20. TCP Reliability Mechanisms

How does TCP actually provide reliability?

Several mechanisms work together.

### Sequence Numbers

TCP assigns sequence numbers to bytes.

Data:

```
HELLO
```

```
H → seq 1000
E → seq 1001
L → seq 1002
L → seq 1003
O → seq 1004
```

The receiver can identify missing/out-of-order data.

---

## 21. Acknowledgements

Receiver sends acknowledgements.

For example:

```
Sender                         Receiver

Data seq 1000 --------------->
Data seq 1001 --------------->

              <--------------- ACK 1002
```

ACK 1002 roughly means:

> "I've received everything up to byte 1001; I expect byte 1002 next."

---

## 22. Retransmission

Suppose:

```
1000 → received
1001 → received
1002 → LOST
1003 → received
1004 → received
```

TCP can detect the missing data and retransmit it.

```
1002 → retransmitted
```

Eventually:

```
1000
1001
1002
1003
1004
```

---

## 23. Flow Control

Imagine:

```
Sender = very fast
Receiver = slow
```

If sender sends too much:

```
Sender
██████████████████████
          ↓
      Receiver
       ███
```

Receiver could become overwhelmed.

TCP uses a **receive window** to tell the sender approximately how much unacknowledged data it can accept.

Conceptually:

Receiver:

> "I can accept another 64 KB."

Sender adjusts its sending accordingly.

This is:

**Flow control**

---

## 24. Congestion Control

Flow control protects the **receiver**.

Congestion control protects the **network**.

Suppose millions of clients send huge amounts of data:

```
Clients
 ↓ ↓ ↓ ↓ ↓ ↓
   Internet
      ↓
   Router
      ↓
   Congestion
```

TCP adapts its sending rate based on network conditions.

This helps avoid overwhelming the network.

Common TCP congestion-control algorithms include:

- CUBIC
- BBR

The exact algorithm depends on the operating system/network configuration.

---

## 25. TCP Connection Termination

TCP also has a graceful connection termination process.

Simplified:

```
Client                         Server

  | -------- FIN ------------> |
  | <--------- ACK ------------|
  |                            |
  | <--------- FIN ------------|
  | -------- ACK ------------> |
```

This is commonly described as a four-step TCP connection close, because each direction is closed independently.
