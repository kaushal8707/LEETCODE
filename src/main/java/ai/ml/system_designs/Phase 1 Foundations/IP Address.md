# IP Address

Now that you understand Client–Server Architecture, the next building block is the **IP Address**.

The simplest way to think about it is:

> An IP address is a numerical address used to identify a device/interface on a network and help route packets to it.

Think of it like a **postal address** for network communication.

---

## 1. Why Do We Need an IP Address?

Suppose you have:

```
Client
  │
  │ "I want to talk to the server"
  ▼
Server
```

But how does the client know where the server is?

Every networked device needs an address that networking equipment can use to deliver traffic.

For example:

```
Client IP  →  192.168.1.10

Server IP  →  192.168.1.20
```

The client can send packets toward:

```
192.168.1.20
```

---

## 2. Real-World Analogy

Think about sending a parcel.

```
Person
  │
  │ Package
  ▼
Courier Network
  │
  ▼
House Address
```

The courier needs an address to know where to deliver the package.

Similarly:

```
Application
    │
    │ Data
    ▼
Network
    │
    ▼
Destination IP Address
```

The network uses the destination address to route the data.

---

## 3. What Does an IP Address Look Like?

There are two major versions:

### IPv4

Example:

```
192.168.1.10
```

IPv4 contains **32 bits**.

It's represented as four numbers:

```
192 . 168 . 1 . 10
```

Each number is one byte:

```
192       168       1        10
 │         │        │         │
8 bits    8 bits   8 bits    8 bits
```

Total = 32 bits

Therefore:

```
8 + 8 + 8 + 8 = 32 bits
```

---

## 4. IPv4 Range

Each section can contain values from:

```
0 → 255
```

So theoretically an IPv4 address looks like:

```
0.0.0.0
```

through:

```
255.255.255.255
```

That gives:

```
2^32
```

possible IPv4 addresses.

That's approximately:

```
4.3 billion
```

addresses.

---

## 5. Why Did We Need IPv6?

The internet grew enormously.

- Phones: **1 billion+**
- Servers: **Millions**
- IoT devices: **Billions**

IPv4's ~4.3 billion addresses became insufficient.

So **IPv6** was introduced.

IPv6 uses:

```
128 bits
```

Example:

```
2001:0db8:85a3:0000:0000:8a2e:0370:7334
```

The address space is enormous:

```
2^128
```

possible addresses.

You don't need to memorize the exact number.

Just remember:

```
IPv4 → 32-bit
IPv6 → 128-bit
```

---

## 6. Public IP vs Private IP

This is very important for System Design.

There are two concepts you'll frequently encounter:

- Private IP
- Public IP

### Private IP

A private IP is used inside a private network.

For example, your home network might look like:

```
              Router
                │
       ┌────────┼────────┐
       ▼        ▼        ▼
     Laptop   Phone    TV
   192.168.1.10
   192.168.1.11
   192.168.1.12
```

These addresses are not directly routable across the public internet.

Common private IPv4 ranges include:

```
10.0.0.0/8

172.16.0.0/12

192.168.0.0/16
```

For example:

```
192.168.1.10
```

is a private IP.

---

## 7. Public IP

A public IP is an address used for communication over the public internet.

For example:

```
Internet
    │
    ▼
Public IP
    │
    ▼
Router / Firewall / Load Balancer
    │
    ▼
Private Network
```

A company's infrastructure might look like:

```
Internet
    │
    │ Public IP
    ▼
Load Balancer
    │
    │ Private IP
    ▼
Application Server
```

This architecture is extremely common.

---

## 8. Very Important: Your Application Server Doesn't Have to Have a Public IP

Imagine:

```
                 Internet
                    │
                    ▼
              Public IP
                    │
                    ▼
             Load Balancer
                    │
              Private Network
                    │
         ┌──────────┼──────────┐
         ▼          ▼          ▼
      Server 1   Server 2   Server 3
      Private    Private    Private
       IP         IP         IP
```

Only the Load Balancer might need to be reachable from the internet.

The application servers can remain private.

This provides:

- Better security
- Controlled access
- Easier scaling
- Reduced attack surface

---

## 9. IP Address Does Not Mean "Application"

This is an important distinction.

An IP address identifies a network interface/address, not a particular application.

Suppose:

Server IP:

```
192.168.1.100
```

You might have:

```
192.168.1.100:8080 → Order Service
192.168.1.100:8081 → Payment Service
192.168.1.100:9090 → Another Service
```

How do we distinguish them?

That's where **ports** come in.

**IP Address + Port**

identifies a network endpoint for a service.

We'll cover ports shortly.

---

## 10. IP Address + Port

Think:

```
IP Address = Building address
Port       = Apartment/door
```

For example:

```
192.168.1.100:8080
```

means:

```
IP   = 192.168.1.100
Port = 8080
```

Another service can be:

```
192.168.1.100:8081
```

Same machine:

```
192.168.1.100
```

Different port:

```
8080 → Order Service
8081 → Payment Service
```

---

## 11. How Does This Fit Into Client–Server?

Previously we had:

```
Client
   │
   │ Request
   ▼
Server
```

Now add IP addresses:

```
Client
IP: 192.168.1.10
   │
   │ Request
   │
   │ Destination: 192.168.1.20
   ▼
Server
IP: 192.168.1.20
```

And with a port:

```
Client
192.168.1.10
     │
     │
     ▼
192.168.1.20:8080
     │
     ▼
Order Service
```

---

## 12. What Happens on the Internet?

Suppose your browser wants to communicate with:

```
example.com
```

The browser ultimately needs an IP address.

The high-level flow is:

```
Browser
   │
   │ example.com
   ▼
DNS
   │
   │ IP address
   ▼
203.0.113.10
   │
   ▼
Internet
   │
   ▼
Server
```

This introduces our next major topic: **DNS**.

DNS essentially helps translate:

```
Domain Name
     ↓
IP Address
```

For example:

```
www.example.com
        ↓
   IP address
```

We'll go into DNS deeply next.

---

## 13. Does One Server Always Have One IP?

No.

This is another important System Design concept.

A server can have:

- One IP
- or multiple IPs.

And more importantly, one IP can represent a larger service infrastructure.

For example:

```
                203.0.113.10
                     │
                     ▼
               Load Balancer
                     │
          ┌──────────┼──────────┐
          ▼          ▼          ▼
       Server 1   Server 2   Server 3
```

The client doesn't need to know:

```
Server 1 IP
Server 2 IP
Server 3 IP
```

It only needs to communicate with the public endpoint.

This is one of the foundations of scalable architecture.

---

## 14. Static IP vs Dynamic IP

An IP address can also be:

### Static

The address generally remains assigned to the resource.

Example:

```
Server
  ↓
203.0.113.10
```

It remains stable.

Useful when other systems need a stable network endpoint.

### Dynamic

The address can change over time.

For example, a typical home internet connection may receive an IP address dynamically from the ISP.

---

## 15. IP Address in Cloud Architecture

Let's take a typical cloud architecture:

```
                    Internet
                       │
                       ▼
                Public IP / DNS
                       │
                       ▼
                 Load Balancer
                       │
               Private Network
                       │
          ┌────────────┼────────────┐
          ▼            ▼            ▼
       EC2/VM       EC2/VM       EC2/VM
       Private      Private      Private
         IP           IP           IP
          │            │            │
          └────────────┼────────────┘
                       ▼
                    Database
```

Notice something important:

**Public-facing layer**

```
Internet
   ↓
Public endpoint
   ↓
Load Balancer
```

**Internal layer**

```
Load Balancer
   ↓
Private IP
   ↓
Application servers
```

This separation is a common system-design pattern.

---

## 16. NAT — One Important Concept

You will frequently hear about **NAT (Network Address Translation)**.

Suppose your laptop has:

```
Private IP:
192.168.1.10
```

It wants to access the internet.

Your router may have a public IP:

```
Public IP:
203.0.113.20
```

The flow is approximately:

```
Laptop
192.168.1.10
      │
      ▼
   Router
      │
      │ NAT
      ▼
203.0.113.20
      │
      ▼
   Internet
```

NAT allows private-network addresses to communicate externally through a public address.

Don't worry about the packet-level details yet.

Just remember:

> NAT translates between private and public addressing contexts.

---

## 17. IP Address vs DNS

This distinction is critical.

### IP Address

Tells the network where to send traffic.

Example:

```
142.250.x.x
```

### DNS

Maps a human-friendly name to an IP or other destination information.

Example:

```
google.com
     ↓
IP address
```

So:

```
Domain Name
     │
     ▼
    DNS
     │
     ▼
IP Address
     │
     ▼
Server
```

---

## 18. Interview Questions

At your experience level, these are worth understanding:

### Basic

**Q: What is an IP address?**

An IP address is a network-layer address used to identify a network interface/address and route packets to their destination.

**Q: IPv4 vs IPv6?**

```
IPv4 → 32 bits
IPv6 → 128 bits
```

IPv6 provides a vastly larger address space.

**Q: Public vs Private IP?**

```
Private IP → Internal/private network
Public IP  → Publicly routable internet address
```

**Q: Can multiple servers have different private IPs behind one public IP?**

Yes.

For example:

```
             Public IP
                 │
                 ▼
           Load Balancer
          /      |       \
         /       |        \
        ▼        ▼         ▼
    Server 1  Server 2  Server 3
    Private   Private   Private
      IP        IP        IP
```

This is extremely common.

**Q: Can multiple applications run on the same IP?**

Yes.

They can use different ports:

```
10.0.0.10:8080
10.0.0.10:8081
10.0.0.10:9090
```

---

## 19. The Mental Model You Should Remember

Don't try to memorize everything.

Keep this picture in your head:

```
                     INTERNET
                         │
                         │
                    Public IP
                         │
                         ▼
                  Load Balancer
                         │
                  Private Network
                         │
             ┌───────────┼───────────┐
             ▼           ▼           ▼
          Server 1    Server 2    Server 3
          10.0.0.10   10.0.0.11   10.0.0.12
             │           │           │
             └───────────┼───────────┘
                         │
                         ▼
                      Database
```

And:

```
IP Address
    │
    ├── IPv4 → 32 bits
    │
    └── IPv6 → 128 bits

IP
 │
 ├── Public
 │
 └── Private

IP + Port
    ↓
Network endpoint/service
```

---

## 🔗 How the Concepts Connect

You've now learned:

```
Client–Server
      ↓
Client needs to reach Server
      ↓
IP Address
      ↓
But humans don't want to remember IPs
      ↓
DNS
      ↓
Domain → IP
      ↓
HTTP/HTTPS
      ↓
Request/Response
```

So the next topic should be **DNS**. We'll go from "I type amazon.com in my browser" → how DNS finds the server → DNS hierarchy → DNS resolver → caching → TTL → multiple IPs → load balancing → what happens when DNS fails.
