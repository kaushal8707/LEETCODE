# Encryption — What It Is and How It Works

**Encryption** is the process of converting readable data (**plaintext**) into an unreadable form (**ciphertext**) using a cryptographic algorithm and a key.

Encryption protects data so that someone who intercepts or obtains it cannot understand it without the required key.

Simple example:

```
Plaintext
   │
   │ Encryption + Key
   ▼
Ciphertext
   │
   │ Decryption + Key
   ▼
Plaintext
```

For example:

```
"Hello Kaushal"
       │
       │ 🔐 Encrypt
       ▼
"8fA$2kP@91..."
       │
       │ 🔓 Decrypt
       ▼
"Hello Kaushal"
```

---

## 1. Why do we need encryption?

Imagine you're sending:

```
Account Number: 123456789
Password: MySecret
Amount: ₹10,000
```

**Without encryption:**

```
Client
   │
   │ Account=123456789
   │ Password=MySecret
   ▼
Internet
   │
   ▼
Server
```

If an attacker can observe the communication, the information may be exposed.

**With encryption:**

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

The attacker may capture the ciphertext, but should not be able to recover the plaintext without the required key.

---

## 2. Basic encryption model

There are four important concepts:

```
Plaintext
    +
Encryption Algorithm
    +
Key
    ↓
Ciphertext
```

Then:

```
Ciphertext
    +
Decryption Algorithm
    +
Key
    ↓
Plaintext
```

Mathematically:

```
C = E(K, P)
```

Where:

```
P = Plaintext
K = Key
E = Encryption function
C = Ciphertext
```

Decryption:

```
P = D(K, C)
```

---

## 3. Encryption is NOT encoding

This is very important.

### Encoding

Encoding changes representation.

Example:

```
Hello
  ↓ Base64
SGVsbG8=
```

Base64 is not encryption.

Anyone can decode it:

```
SGVsbG8=
    ↓
Hello
```

### Encryption

Encryption requires a key.

```
Hello
  ↓
Encryption + Key
  ↓
Ciphertext
```

Without the key, recovering the plaintext should be computationally infeasible when a secure algorithm is properly used.

Remember:

```
Encoding     → Representation
Encryption   → Confidentiality
Hashing      → One-way transformation
```

---

## 4. Symmetric Encryption

The first major type is:

> **Symmetric-key encryption**

The same secret key is used for encryption and decryption.

```
             Secret Key
                 │
                 ▼
Plaintext ──► Encryption
                 │
                 ▼
             Ciphertext
                 │
                 │
                 ▼
             Decryption
                 │
                 │
             Secret Key
                 │
                 ▼
             Plaintext
```

Example:

```
Key = K123

"Hello"
   │
   │ Encrypt with K123
   ▼
"X8$92..."
   │
   │ Decrypt with K123
   ▼
"Hello"
```

---

## 5. Examples of symmetric algorithms

Common modern algorithms include:

- AES
- ChaCha20

AES is extremely common.

Examples:

```
AES-128
AES-192
AES-256
```

The number refers to the key size in bits.

For example:

```
AES-256
```

uses a 256-bit key.

---

## 6. Why symmetric encryption is fast

Symmetric cryptography is computationally efficient.

Therefore it is suitable for encrypting large amounts of data:

- Files
- Database data
- Network traffic
- Backups
- Messages

For example:

```
10 GB file
     │
     ▼
AES encryption
     │
     ▼
Encrypted 10 GB file
```

You wouldn't normally use RSA directly to encrypt a huge 10 GB file.

---

## 7. The biggest problem with symmetric encryption

The problem is:

> How do both parties securely obtain the secret key?

Suppose:

```
Client
   │
   │ Secret Key = ABC123
   ▼
Internet
   │
   ▼
Server
```

If you send the secret key over an insecure network:

```
Client ───── Key ─────► Server
             │
             ▼
          Attacker
```

The attacker gets the key.

Now the attacker can decrypt everything.

This is called the **key distribution problem**.

---

## 8. Asymmetric Encryption

Asymmetric cryptography uses **two related keys**:

- Public Key
- Private Key

The public key can be shared.

The private key must remain secret.

```
                Key Pair
                   │
           ┌───────┴────────┐
           ▼                ▼
       Public Key       Private Key
```

Example:

```
Server
 ├── Public Key
 └── Private Key
```

Public key:

```
Can be shared
```

Private key:

```
Must be protected
```

---

## 9. How asymmetric encryption works

Suppose Bob has:

```
Public Key
Private Key
```

Alice wants to send Bob a secret.

She obtains Bob's public key:

```
Alice
   │
   │ Bob's Public Key
   ▼
Encrypt message
   │
   ▼
Ciphertext
   │
   ▼
Bob
   │
   │ Bob's Private Key
   ▼
Decrypt
   │
   ▼
Original message
```

Conceptually:

**Encrypt:**

```
Plaintext
   +
Bob's Public Key
   ↓
Ciphertext
```

**Decrypt:**

```
Ciphertext
   +
Bob's Private Key
   ↓
Plaintext
```

---

## 10. Examples of asymmetric cryptography

Common algorithms include:

- RSA
- ECC

Modern systems also use elliptic-curve cryptography extensively.

However, there is an important distinction:

> Asymmetric cryptography is usually not used to encrypt large application data directly.

It is more expensive than symmetric cryptography.

---

## 11. Hybrid Encryption

Real systems commonly combine symmetric and asymmetric cryptography.

This is one of the most important concepts for system design.

Suppose:

```
Client
   │
   │ wants secure communication
   ▼
Server
```

We can do:

1. Generate symmetric session key
2. Securely establish/exchange key material using asymmetric cryptography
3. Use symmetric key to encrypt actual data

Conceptually:

```
             Asymmetric Cryptography
                       │
                       ▼
               Establish Session Key
                       │
                       ▼
                Symmetric Key
                       │
                       ▼
               Encrypt Data
                       │
                       ▼
               Large Data
```

This gives us:

```
Asymmetric
    ↓
Secure key establishment

Symmetric
    ↓
Fast data encryption
```

This is essentially the idea behind modern secure protocols such as **TLS**.

---

## 12. Encryption in TLS

This connects directly to your previous question about TLS.

When you connect to:

```
https://example.com
```

TLS establishes cryptographic keys and then uses symmetric authenticated encryption to protect application traffic.

Conceptually:

```
Client
   │
   │ TLS Handshake
   ▼
Server
   │
   │ Key establishment
   ▼
Session Keys
   │
   ▼
Symmetric Encryption
   │
   ▼
Encrypted HTTP Data
```

So:

```
HTTPS
   ↓
TLS
   ↓
Key establishment
   ↓
Session keys
   ↓
Symmetric encryption
   ↓
Encrypted application traffic
```

---

## 13. What is a Session Key?

A **session key** is a symmetric key used for a particular secure communication session.

For example:

```
Connection 1 → Session Key A
Connection 2 → Session Key B
Connection 3 → Session Key C
```

The idea is that you don't need to use one permanent encryption key for all communication.

This improves security and performance.

---

## 14. Encryption at Rest vs Encryption in Transit

This is very important in system design.

### Encryption in transit

Protects data while moving across networks.

```
Client
   │
   │ 🔐
   ▼
Internet
   │
   │ 🔐
   ▼
Server
```

Usually:

```
TLS / HTTPS
```

### Encryption at rest

Protects stored data.

```
Application
     │
     ▼
Database
     │
     ▼
Encrypted Storage
```

Examples:

- Database encryption
- Disk encryption
- Encrypted backups
- Encrypted object storage

---

## 15. Example: Banking system

Suppose a banking application has:

```
Account Number
Customer Name
Balance
Transaction History
```

A secure architecture could be:

```
                  Banking App
                       │
                    HTTPS/TLS
                       │
                       ▼
                  API Gateway
                       │
                       ▼
                 Banking Service
                       │
                       ▼
                    Database
                       │
                 Encryption at Rest
                       │
                       ▼
                  Encrypted Disk
```

Now:

```
Network traffic → encrypted
Stored data     → encrypted
```

This is **defense in depth**.

---

## 16. Database Encryption

Suppose the database contains:

```
Card Number
----------------
4111111111111111
```

Encryption at rest might protect the underlying storage.

But there's an important distinction between:

### Full-disk/storage encryption

Protects the physical/storage layer.

```
Database
   ↓
Encrypted disk
```

### Application/field-level encryption

The application encrypts particularly sensitive fields.

```
Card Number
    ↓
Encryption
    ↓
Encrypted value
    ↓
Database
```

Field-level encryption is useful when you need stronger protection for especially sensitive fields.

---

## 17. Encryption vs Hashing

This is one of the most common interview questions.

### Encryption

Designed to be **reversible** with the appropriate key.

```
Plaintext
   ↓
Encryption
   ↓
Ciphertext
   ↓
Decryption
   ↓
Plaintext
```

### Hashing

Designed to be **one-way**.

```
Password
   ↓
Hash Function
   ↓
Hash
```

You don't normally "decrypt" a secure password hash.

---

## 18. Passwords should NOT be encrypted

Suppose a user creates:

```
Password = MySecret123
```

Don't store:

```
EncryptedPassword = ...
```

and keep a decryption key that allows recovery of every password.

Instead use a password hashing algorithm designed for password storage, such as:

- Argon2id
- bcrypt
- scrypt

Conceptually:

```
Password
   │
   ▼
Password Hashing Algorithm
   │
   ▼
Password Hash
   │
   ▼
Database
```

During login:

```
User enters password
       │
       ▼
Hash/verify using password-hashing algorithm
       │
       ▼
Compare/verify stored hash
       │
       ▼
Success / Failure
```

---

## 19. Encryption vs Hashing vs Encoding

Remember this table:

| Property | Encoding | Encryption | Hashing |
|---|---|---|---|
| Purpose | Representation | Confidentiality | Integrity/one-way transformation |
| Reversible | Yes | Yes, with key | No practical reversal |
| Requires key | No | Yes | Usually no secret key |
| Example | Base64 | AES | SHA-256 |
| Password storage | ❌ | ❌ | ✅ specialized password hashing |

---

## 20. What is AES?

**AES** stands for:

> Advanced Encryption Standard

It is a symmetric block cipher.

Common key sizes:

```
AES-128
AES-192
AES-256
```

AES is widely used for encrypting data.

But saying:

```
"AES-256"
```

alone doesn't completely specify how AES is being used.

You also need to consider the **mode of operation**.

---

## 21. AES-GCM

One commonly used construction is:

```
AES-GCM
```

GCM provides **authenticated encryption**.

Conceptually:

```
Plaintext
   │
   ├── Encryption
   │
   └── Authentication
         │
         ▼
Ciphertext + Authentication Tag
```

This provides:

```
Confidentiality
+
Integrity/authentication
```

Modern protocols such as TLS commonly use authenticated encryption modes such as AES-GCM.

---

## 22. What is an IV / Nonce?

Encryption schemes often require a value such as an:

```
IV
```

or:

```
Nonce
```

The exact requirements depend on the algorithm/mode.

For AES-GCM, nonce uniqueness is critical for a given key.

Conceptually:

```
Key
 +
Nonce
 +
Plaintext
 ↓
Ciphertext
```

You generally shouldn't simply reuse the same nonce with the same AES-GCM key.

---

## 23. Why randomization matters

Suppose you encrypt:

```
Hello
```

twice.

A secure encryption scheme should generally avoid producing the same ciphertext every time when used correctly with fresh nonces/IVs.

```
Hello + Key + Nonce A
       ↓
Ciphertext A

Hello + Key + Nonce B
       ↓
Ciphertext B
```

Otherwise an attacker could potentially learn patterns from repeated plaintext.

---

## 24. Encryption doesn't solve everything

Encryption protects confidentiality, but you also need:

- Authentication
- Authorization
- Integrity
- Key management
- Access control
- Monitoring

For example:

```
Encrypted Database
       │
       ▼
Attacker has valid application credentials
       │
       ▼
Application gives access
```

Encryption at rest alone doesn't solve that.

This is why security is **defense in depth**.

---

## 25. Key Management

This is often more important than the encryption algorithm itself.

Imagine:

```
AES-256
```

but the key is:

```
password123
```

stored in:

```
GitHub repository
```

That's terrible security.

Instead:

```
Application
     │
     ▼
Key Management System
     │
     ├── Encryption Keys
     ├── Key Rotation
     ├── Access Control
     └── Audit Logs
```

Common approaches include:

- KMS
- HSM
- Secrets Manager
- Vault-style systems

---

## 26. Envelope Encryption

This is an important senior-level concept.

Instead of using a master key to encrypt every piece of data:

```
Master Key
    │
    ▼
Encrypt huge database
```

you can use **envelope encryption**.

```
                 Master Key
                     │
                     ▼
              Encrypt Data Key
                     │
                     ▼
                 Data Key
                     │
                     ▼
                Encrypt Data
                     │
                     ▼
               Encrypted Data
```

More precisely:

```
KMS Master/Key-Encryption Key
             │
             ▼
      Encrypt Data Key
             │
             ▼
       Encrypted Data Key

Data Key
   │
   ▼
Encrypt actual data
   │
   ▼
Ciphertext
```

Advantages:

- Less exposure of master keys
- Easy key rotation
- Better scalability
- Separation of responsibilities

---

## 27. Public Key Encryption vs Digital Signature

Another important distinction.

### Encryption

Goal:

> Keep data secret.

Conceptually:

```
Sender
   │
   │ Encrypt with receiver's public key
   ▼
Ciphertext
   │
   ▼
Receiver
   │
   │ Decrypt with private key
   ▼
Plaintext
```

### Digital Signature

Goal:

> Prove authenticity/integrity.

Conceptually:

```
Sender
   │
   │ Sign with private key
   ▼
Signature
   │
   ▼
Receiver
   │
   │ Verify using public key
   ▼
Valid / Invalid
```

So:

```
Encryption → Confidentiality
Signature  → Authenticity + Integrity
```

---

## 28. Real-world example: API request

Suppose:

```http
POST /payments
Authorization: Bearer eyJ...
Content-Type: application/json

{
    "amount": 1000
}
```

With HTTPS:

```
Client
   │
   │ Plain HTTP data
   ▼
TLS
   │
   │ Encrypt
   ▼
Encrypted TLS Records
   │
   ▼
Internet
   │
   ▼
Server
   │
   │ Decrypt TLS records
   ▼
HTTP request
```

The JWT and request body are protected while traveling through the TLS connection.

---

## 29. Encryption in a distributed system

Consider:

```
             Internet
                 │
                TLS
                 │
                 ▼
           API Gateway
                 │
              mTLS
                 │
                 ▼
          Order Service
                 │
              mTLS
                 │
                 ▼
         Payment Service
                 │
                 ▼
              Database
                 │
          Encryption at Rest
```

You can therefore have encryption at multiple levels:

```
External traffic
       ↓
      TLS

Internal service traffic
       ↓
      mTLS/TLS

Stored data
       ↓
Encryption at rest

Sensitive fields
       ↓
Field-level encryption
```

---

## 30. What happens if someone steals the encrypted data?

Suppose an attacker steals:

```
database_backup.sql
```

and the database is encrypted.

They obtain:

```
Encrypted Data
```

but not necessarily the encryption key.

Security becomes:

```
Attacker
   │
   ├── Encrypted Data ✅
   │
   └── Encryption Key ❌
```

Therefore they should not be able to recover the plaintext.

This illustrates an important principle:

> Encryption is only as strong as the protection of the keys and the cryptographic implementation.

---

## 31. Common encryption mistakes

### ❌ Rolling your own encryption

Don't invent your own cryptographic algorithm.

Use well-established libraries and protocols.

### ❌ Hardcoding keys

Bad:

```java
private static final String KEY = "my-secret-key";
```

Use proper key management.

### ❌ Reusing nonces incorrectly

Especially dangerous with authenticated encryption modes such as AES-GCM.

### ❌ Using weak/obsolete algorithms

Avoid obsolete cryptography.

### ❌ Encrypting passwords instead of hashing them

Use:

- Argon2id
- bcrypt
- scrypt

for password storage.

### ❌ Storing encryption keys next to encrypted data

For example:

```
database/
   encrypted_data
   encryption_key
```

This defeats much of the benefit.

### ❌ Assuming Base64 is encryption

It isn't.

```
Base64 ≠ Encryption
```

---

## 32. Encryption and TLS — relationship

Since you're learning TLS and API Security together, this is the easiest way to connect them:

```
                    Security
                       │
       ┌───────────────┼────────────────┐
       │               │                │
       ▼               ▼                ▼
   Encryption     Authentication   Authorization
       │               │                │
       ▼               ▼                ▼
    Protects       Who are you?     What can you do?
    data
       │
       ▼
      TLS
```

TLS uses cryptography to create a secure communication channel.

Within that channel:

```
HTTP Request
    │
    ▼
TLS Encryption
    │
    ▼
Encrypted Network Traffic
```

---

## 33. Senior System Design Interview Answer

If an interviewer asks:

> "What is encryption and how does it work?"

A strong answer is:

> Encryption is the process of transforming plaintext into ciphertext using a cryptographic algorithm and a key, so that only an authorized party with the appropriate key can recover the original data.
>
> There are two major categories: symmetric and asymmetric cryptography. Symmetric encryption, such as AES, uses the same secret key for encryption and decryption and is very efficient for large amounts of data. Asymmetric cryptography uses a public/private key pair and is generally used for authentication and key establishment rather than bulk data encryption.
>
> Modern systems commonly use a hybrid approach. For example, TLS uses asymmetric cryptography and ephemeral key exchange to establish shared session keys and then uses efficient symmetric authenticated encryption such as AES-GCM or ChaCha20-Poly1305 to protect application traffic.
>
> In system design, I would consider encryption both in transit using TLS/mTLS and at rest using storage/database encryption. For highly sensitive fields, application-level encryption may also be appropriate. Keys should be managed separately using KMS/HSM or a dedicated secrets/key-management system, with proper access control and rotation.

---

## 34. Final mental model

```
                         ENCRYPTION
                             │
             ┌───────────────┴────────────────┐
             │                                │
             ▼                                ▼
        Symmetric                         Asymmetric
             │                                │
             ▼                                ▼
        AES / ChaCha20                    RSA / ECC
             │                                │
             │                         Public + Private
             │                              Keys
             │                                │
             └────────────┬───────────────────┘
                          │
                          ▼
                    Hybrid Systems
                          │
                          ▼
                         TLS
                          │
                          ▼
                  Secure Communication
```

And remember these four distinctions:

```
Encryption
    → Keep data secret

Hashing
    → One-way transformation

Digital Signature
    → Prove authenticity + integrity

TLS
    → Secure the communication channel
```

### One-line interview memory trick

> 🔐 Encryption protects confidentiality, hashing provides one-way transformation, digital signatures provide authenticity/integrity, and TLS uses these cryptographic techniques to establish a secure communication channel.

