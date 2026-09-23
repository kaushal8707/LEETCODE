# Hashing

**Hashing** is a technique that converts input data of any size into a fixed-size value, called a **hash**, using a hash function.

```
Input Data
    |
    v
Hash Function
    |
    v
Fixed-size Hash
```

For example:

```
"hello"
   |
   v
Hash Function
   |
   v
2cf24dba5fb0a30e...
```

The important idea is:

> Same input → same hash, but different inputs should ideally produce different hashes.

---

## 1. Why do we need Hashing?

Hashing is heavily used in system design and software engineering for:

- Fast lookup
- HashMap / HashSet
- Database indexing
- Password storage
- Distributed systems
- Distributed caching
- Consistent hashing
- Data integrity
- Deduplication
- Partitioning/sharding
- Load distribution

For example, a HashMap doesn't search every element one by one.

Instead:

```
Key
 |
 v
Hash Function
 |
 v
Bucket
 |
 v
Value
```

This allows average **O(1) lookup**.

---

## 2. How Hashing Works

Suppose we have:

```
Key = "user123"
```

A hash function processes the key:

```
hash("user123")
       |
       v
   28473651
```

If we have 10 buckets:

```
bucket = hash(key) % 10
```

So:

```
28473651 % 10
      |
      v
     1
```

Therefore:

```
"user123" → hash → 28473651 → bucket 1
```

The data can be stored in bucket 1.

---

## 3. Hash Function

A hash function is a function:

```
H(input) → fixed-size output
```

For example, conceptually:

```
H("Alice") → 1234
H("Bob")   → 5678
H("John")  → 9012
```

Real hash functions are much more sophisticated.

Examples include:

- MD5
- SHA-1
- SHA-256
- SHA-512
- MurmurHash
- xxHash

But they are designed for different purposes.

---

## 4. Important Properties of Hashing

A good hash function generally has these properties.

### 1. Deterministic

Same input should always produce the same output.

```
hash("hello")
      ↓
ABC123

hash("hello")
      ↓
ABC123
```

### 2. Fixed-size output

Input can be:

```
"a"
```

or:

```
"this is a very long piece of data..."
```

But the hash size remains fixed for a particular algorithm.

For SHA-256:

```
Input → any size
Output → 256 bits
```

### 3. Fast to calculate

Hashing should generally be efficient, especially for data structures and distributed systems.

### 4. Avalanche effect

A tiny change in input should produce a substantially different hash.

For example:

```
hello
```

and:

```
Hello
```

should have completely different cryptographic hashes.

---

## 5. Hash Collision

One important problem is a **collision**.

A collision occurs when two different inputs produce the same hash.

```
Input A
   |
   v
Hash Function
   |
   v
12345


Input B
   |
   v
Hash Function
   |
   v
12345
```

Therefore:

```
A ≠ B
```

but

```
hash(A) = hash(B)
```

This is called a **hash collision**.

Collisions cannot be completely eliminated when mapping an enormous input space into a finite output space.

Good hash functions make collisions sufficiently unlikely or handle them appropriately.

---

## 6. How HashMap Uses Hashing

This is one of the most important practical applications.

Suppose:

```java
Map<String, String> users = new HashMap<>();

users.put("Alice", "India");
users.put("Bob", "USA");
users.put("John", "UK");
```

Conceptually:

```
"Alice"
   |
   v
hashCode()
   |
   v
bucket index
   |
   v
Bucket 3
   |
   +---- Alice → India
```

When we execute:

```java
users.get("Alice");
```

Java approximately does:

```
"Alice"
   |
   v
hash
   |
   v
bucket index
   |
   v
find entry
   |
   v
"India"
```

Therefore, instead of searching all entries:

```
Alice
Bob
John
David
Mike
...
```

it jumps directly toward the appropriate bucket.

Average complexity:

```
put()    → O(1)
get()    → O(1)
remove() → O(1)
```

Worst-case behavior can be worse because of collisions, although modern Java HashMap has mechanisms to improve heavily-collided buckets.

---

## 7. Hashing vs Encryption

This is extremely important.

### Encryption

Encryption is designed to be **reversible** with the appropriate key.

```
Plaintext
   |
   v
Encryption + Key
   |
   v
Ciphertext
   |
   v
Decryption + Key
   |
   v
Plaintext
```

Example:

```
"hello"
   ↓
Encrypted data
   ↓
"hello"
```

### Hashing

Hashing is generally designed as a **one-way** transformation.

```
"hello"
   |
   v
Hash
   |
   X
Cannot normally reverse hash back to "hello"
```

So:

```
Encryption → reversible with key
Hashing    → one-way
```

---

## 8. Hashing vs Encoding

Encoding is different too.

For example:

```
"hello"
   |
   v
Base64
   |
   v
"aGVsbG8="
```

Base64 is **not** encryption and **not** hashing.

Anyone can decode it:

```
"aGVsbG8="
   |
   v
"hello"
```

So:

```
Encoding   → representation
Encryption → confidentiality
Hashing    → fixed-size fingerprint / one-way transformation
```

---

## 9. Hashing for Passwords

Hashing is commonly used for passwords, but normal fast hashes such as SHA-256 should **not** be used directly for password storage.

Instead, password-specific algorithms are used, such as:

- Argon2id
- bcrypt
- scrypt

Example:

User enters:

```
MyPassword123
      |
      v
Password hashing algorithm
      |
      v
Password hash
      |
      v
Database
```

During login:

```
User enters password
        |
        v
Password hashing
        |
        v
Compare with stored password hash
        |
        v
Match?
```

The application should not need to store the original password.

---

## 10. Hashing in Distributed Systems

Hashing becomes especially important in distributed systems.

Suppose we have 4 servers:

```
Server 1
Server 2
Server 3
Server 4
```

We could calculate:

```
server = hash(userId) % 4
```

For example:

```
user123
   |
   v
hash(user123)
   |
   v
28473651
   |
   v
28473651 % 4
   |
   v
Server 3
```

Therefore:

```
user123 → Server 3
user456 → Server 1
user789 → Server 4
```

This allows requests to be distributed across servers.

---

## 11. Hashing in Distributed Cache

Suppose Redis/cache has multiple nodes:

```
        Cache Cluster
       /      |      \
      v       v       v
 Redis-1   Redis-2   Redis-3
```

We can hash the cache key:

```
"user:123"
     |
     v
   hash()
     |
     v
 choose Redis node
```

For example:

```
user:123 → Redis-2
user:456 → Redis-1
user:789 → Redis-3
```

The same key will normally map to the same node.

---

## 12. Problem with Simple Hashing

Suppose:

```
hash(key) % 3
```

We have:

```
Node 1
Node 2
Node 3
```

Now suppose we add Node 4:

```
hash(key) % 4
```

A huge number of keys may move.

Example:

**Before:**

```
user123 → Node 2
user456 → Node 1
user789 → Node 3
```

**After adding a node:**

```
user123 → Node 4
user456 → Node 3
user789 → Node 1
```

This can cause:

- Massive cache misses
- Data movement
- Network traffic
- Load spikes

This is one of the reasons **Consistent Hashing** is used in distributed systems.

---

## 13. Hashing vs Consistent Hashing

### Normal hashing

```
hash(key) % N
```

Changing `N` can move many keys.

### Consistent hashing

```
                 Node A
                   |
          -------------------
        /                     \
    Node D                   Node B
        \                     /
          -------------------
                   |
                 Node C
```

Both keys and nodes are mapped onto a logical hash ring.

When a node is added or removed, generally only a portion of the keys need to move.

This is extremely useful for:

- Distributed caches
- Distributed databases
- Load distribution
- Service routing

---

## 14. Hashing for Sharding

Suppose you have:

```
1 billion users
```

and 4 database shards:

```
Shard 1
Shard 2
Shard 3
Shard 4
```

You can use:

```
shard = hash(userId) % 4
```

Example:

```
userId = 10001
       |
       v
hash(10001)
       |
       v
% 4
       |
       v
Shard 2
```

So the user's data consistently goes to the same shard.

---

## 15. Hashing for Data Integrity

Hashing can also detect whether data changed.

Suppose a file has:

```
file.txt
```

Calculate:

```
SHA-256(file)
        |
        v
ABC123...
```

Later, calculate the hash again.

```
SHA-256(file)
        |
        v
ABC123...
```

Same hash:

```
Probably unchanged
```

If someone modifies the file:

```
SHA-256(file)
        |
        v
XYZ789...
```

The hashes differ.

Therefore hashing can act like a **digital fingerprint**.

---

## 16. Important Hashing Categories

It's useful to distinguish three broad categories.

### General-purpose hash

Used for data structures and fast lookup.

Examples:

- MurmurHash
- xxHash

### Cryptographic hash

Designed for security properties such as collision resistance and preimage resistance.

Examples:

- SHA-256
- SHA-512
- SHA-3

### Password hashing

Designed to be deliberately expensive to slow down password guessing.

Examples:

- Argon2id
- bcrypt
- scrypt

---

## 17. Real-World Example

Consider an e-commerce system.

```
                Client
                   |
                   v
             Load Balancer
                   |
                   v
             API Gateway
                   |
                   v
            Order Service
                   |
          hash(customerId)
                   |
        +----------+----------+
        |          |          |
        v          v          v
      DB-1       DB-2       DB-3
```

Suppose:

```
customerId = 10025
```

The system calculates:

```
hash(10025) % 3
```

Result:

```
DB-2
```

Therefore:

```
Customer 10025
      ↓
    hash
      ↓
    DB-2
```

Every request for that customer can be routed consistently to the same shard.

---

## 18. Hashing in System Design

For senior-level system design, remember these major uses:

```
                 HASHING
                    |
       +------------+-------------+
       |            |             |
       v            v             v
   HashMap       Caching       Sharding
       |            |             |
       v            v             v
   Fast Lookup  Redis Nodes   DB Shards

       |
       +------------------+
       |                  |
       v                  v
 Data Integrity      Consistent Hashing
       |                  |
       v                  v
 SHA-256             Distributed Systems
```

---

## 19. Interview Answer

If an interviewer asks:

> "What is hashing and how does it work?"

A strong answer would be:

> Hashing is a technique that maps input data of arbitrary size to a fixed-size hash value using a deterministic hash function. The same input produces the same hash, while a good hash function distributes different inputs uniformly and minimizes collisions. Hashing is widely used in HashMap/HashSet for fast lookup, database sharding, distributed caching, data integrity, and consistent hashing. In distributed systems, hashing can map keys to servers or shards. Simple `hash(key) % N` can cause massive key movement when the number of nodes changes, so consistent hashing is often used to minimize redistribution. For security-sensitive use cases, cryptographic and password-specific hash functions such as SHA-256 and Argon2id serve different purposes.

### Mental model

```
Hashing
   ↓
Input
   ↓
Hash Function
   ↓
Fixed-size Hash
   ↓
Use the hash to:
   ├── Find data quickly
   ├── Select cache node
   ├── Select database shard
   ├── Verify data integrity
   └── Build distributed systems
```

**Most important distinction:**

```
Hashing      → one-way fingerprint / mapping
Encryption   → reversible confidentiality
Encoding     → data representation
```

And in distributed system design:

```
Hashing
   ↓
Consistent Hashing
   ↓
Distributed Cache / Sharding / Load Distribution
```

This is the key connection to understand before going deeper into **Consistent Hashing**.

