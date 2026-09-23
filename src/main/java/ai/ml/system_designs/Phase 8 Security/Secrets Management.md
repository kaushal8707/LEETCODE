# Secrets Management

**Secrets Management** is the practice of securely storing, accessing, rotating, and controlling sensitive information such as:

- Database passwords
- API keys
- JWT signing keys
- TLS private keys
- OAuth client secrets
- Cloud credentials
- Encryption keys
- Third-party service credentials

The main goal is:

> Keep secrets out of source code and control exactly who/what can access them.

---

## 1. Why do we need Secrets Management?

A common but dangerous approach is:

```java
String dbPassword = "MyPassword123";
String apiKey = "abc123xyz";
```

Or putting secrets in:

```
application.properties
application.yml
.env
Git repository
Docker image
```

For example:

```yaml
database:
  username: admin
  password: MySecretPassword
```

If this gets committed to Git:

```
Developer
    |
    v
Git Repository
    |
    +---- application.yml
             |
             +---- DB password ❌
```

Anyone with repository access may potentially obtain the credential.

Even deleting the file later doesn't necessarily remove it from Git history.

---

## 2. What is a Secret?

A secret is sensitive information that should only be available to authorized users, applications, or infrastructure.

Examples:

```
DB_PASSWORD
DB_USERNAME
STRIPE_API_KEY
JWT_PRIVATE_KEY
OAUTH_CLIENT_SECRET
TLS_PRIVATE_KEY
AWS_ACCESS_KEY
```

Think of it as:

```
Secret
   ↓
Something that must remain confidential
```

---

## 3. What is a Secrets Manager?

A **Secrets Manager** is a centralized secure system that stores and manages secrets.

Examples include:

- HashiCorp Vault
- AWS Secrets Manager
- Azure Key Vault
- Google Secret Manager
- Kubernetes Secrets, often combined with an external secret manager for stronger protection

Conceptually:

```
                    Secrets Manager
                  +------------------+
                  |                  |
                  | DB Password      |
                  | API Key          |
                  | OAuth Secret     |
                  | Private Key      |
                  |                  |
                  +------------------+
                           ^
                           |
                      Secure Access
                           |
                           |
                      Application
```

Instead of:

```
Application → hardcoded password ❌
```

we have:

```
Application → Secrets Manager → Secret
```

---

## 4. How Secrets Management Works

A typical architecture looks like this:

```
                 Developer
                     |
                     | deploy
                     v
              CI/CD Pipeline
                     |
                     v
                Application
                     |
                     | authenticate
                     v
             Secrets Manager
                     |
                     | authorized?
                     v
                  Secret
```

Let's walk through the process.

### Step 1: Store the secret

An administrator creates a secret:

```
DB_PASSWORD = "SuperSecretPassword"
```

It is stored in a secure secrets manager.

```
Secrets Manager

secret/data/payment-service
        |
        +-- DB_USERNAME
        +-- DB_PASSWORD
        +-- API_KEY
```

The application code does not contain the actual password.

---

## 5. Step 2: Application authenticates

Before an application can retrieve a secret, it must prove its identity.

For example:

```
Application
     |
     | "Who am I?"
     v
Identity Provider
     |
     v
Application Identity
```

Modern systems often use:

- IAM roles
- Kubernetes service accounts
- Workload identity
- Cloud instance identity
- Short-lived credentials
- mTLS certificates

The important principle is:

> Don't give the application a permanent master password just so it can retrieve another password.

---

## 6. Step 3: Authorization

After authentication, the secrets manager checks permissions.

For example:

```
Payment Service
       |
       v
Secrets Manager
       |
       v
Policy
       |
       +---- payment/db/password → ALLOW
       |
       +---- payroll/db/password → DENY
```

So:

```
Payment Service → payment secrets ✅
Payment Service → payroll secrets ❌
```

This follows **least privilege**.

---

## 7. Step 4: Secret Retrieval

The application requests:

```
GET secret/payment-service/database
```

The secrets manager verifies:

```
Who is requesting?
        ↓
Is identity valid?
        ↓
Does identity have permission?
        ↓
Return secret
```

Then:

```
Secrets Manager
       |
       | DB_PASSWORD
       v
Payment Service
```

The application can now connect to the database.

---

## 8. Complete Flow

A simplified production flow:

```
                   ┌─────────────────────┐
                   │   Secrets Manager   │
                   │                     │
                   │ DB Password         │
                   │ API Keys            │
                   │ OAuth Secrets       │
                   │ Private Keys        │
                   └──────────┬──────────┘
                              ^
                              |
                       Authenticate
                              |
                              |
┌─────────────┐       ┌───────┴───────┐
│ Application │──────>│ Identity/IAM  │
└──────┬──────┘       └───────────────┘
       |
       | Retrieve secret
       v
    Database
```

---

## 9. Where Should Secrets Be Stored?

A common architecture is:

```
                Source Code
                    |
                    | ❌ No secrets
                    v
              application.yml
                    |
                    | references
                    v
             Secrets Manager
                    |
                    v
                Application
```

For example, instead of:

```yaml
db:
  password: MyPassword123
```

you might have:

```yaml
db:
  password: ${DB_PASSWORD}
```

The actual value is supplied securely at runtime.

---

## 10. Environment Variables

Environment variables are better than hardcoding secrets into source code:

```
DB_PASSWORD=secret
```

Application:

```java
String password = System.getenv("DB_PASSWORD");
```

But there is an important point:

> Environment variables are not automatically a complete secrets-management solution.

They can potentially appear in:

- Process environments
- Debug output
- Container configuration
- CI/CD logs
- Crash dumps
- Deployment configuration

A stronger architecture is:

```
Application
     |
     v
Secrets Manager
     |
     v
Secret
```

rather than simply:

```
Application
     |
     v
Environment Variable
```

---

## 11. Secret Rotation

One of the most important features of secrets management is **rotation**.

Suppose:

```
DB_PASSWORD = Password123
```

After some time, we want to change it:

```
DB_PASSWORD = NewPassword456
```

Without secrets management:

```
Change password
      ↓
Find every application
      ↓
Update configuration
      ↓
Redeploy everything
```

This can be difficult.

With a secrets manager:

```
        Secrets Manager
              |
              | old
              v
       Password123
              |
           rotate
              |
              v
       NewPassword456
```

Applications can retrieve the updated secret.

---

## 12. Automatic Rotation

Some systems can automatically rotate credentials.

Example:

```
                  Scheduler
                     |
                     v
              Rotate Secret
                     |
                     v
             Secrets Manager
                     |
                     v
              New DB Password
                     |
                     v
                 Database
```

For example:

```
Every 30 days
      ↓
Generate new credential
      ↓
Update database
      ↓
Update secret
      ↓
Applications retrieve new value
```

This reduces the lifetime of compromised credentials.

---

## 13. Secret Versioning

Secrets managers often support versions.

```
DB_PASSWORD

v1 → Password123
v2 → Password456
v3 → Password789
```

You can have:

```
CURRENT → v3
```

This is useful for:

- Rotation
- Rollbacks
- Safe deployments
- Migration

---

## 14. Secret vs Encryption Key

These are related but not exactly the same.

### Secret

Examples:

```
DB_PASSWORD
API_KEY
OAuth_CLIENT_SECRET
```

### Encryption key

Used specifically for cryptographic operations:

```
AES Key
RSA Private Key
Data Encryption Key
```

**Key-management systems** such as KMS/HSM are often used for cryptographic keys.

A production architecture may therefore look like:

```
             Application
                  |
        +---------+---------+
        |                   |
        v                   v
 Secrets Manager          KMS
        |                   |
        v                   v
 Passwords/API keys      Encryption keys
```

---

## 15. Envelope Encryption

This is an important senior-level concept.

Suppose we have a large amount of data:

```
Customer Data
```

Instead of using a master encryption key directly for everything:

```
Master Key
    |
    v
Encrypt everything
```

we use:

```
             KMS Master Key
                    |
                    v
             Encrypt Data Key
                    |
                    v
               Data Key
                    |
                    v
             Encrypt Data
```

Conceptually:

```
KMS
 |
 +-- Master Key
       |
       +---- encrypts Data Key
                    |
                    v
               Data Key
                    |
                    +---- encrypts actual data
```

This is called **envelope encryption**.

It provides better scalability and key-management separation.

---

## 16. Secrets Management in Kubernetes

Consider a Kubernetes environment:

```
             Kubernetes Cluster
                    |
        +-----------+-----------+
        |                       |
        v                       v
   Pod: Order Service      Pod: Payment Service
        |                       |
        +-----------+-----------+
                    |
                    v
             Secrets Manager
```

The pod authenticates using its workload identity/service account.

Then:

```
Order Service
      |
      | authenticate
      v
Secrets Manager
      |
      | authorize
      v
Order DB credentials
```

The pod doesn't need a global secret containing access to every database.

---

## 17. Secrets Management in Microservices

Imagine:

```
                  API Gateway
                      |
          +-----------+-----------+
          |           |           |
          v           v           v
       Order       Payment      User
       Service     Service      Service
          |           |           |
          |           |           |
          +-----------+-----------+
                      |
                      v
               Secrets Manager
```

Permissions can be isolated:

```
Order Service
   ↓
order-db-password

Payment Service
   ↓
payment-db-password
payment-provider-api-key

User Service
   ↓
user-db-password
```

This is much safer than:

```
All services
     |
     v
ONE giant secret
     |
     v
Everything ❌
```

---

## 18. Secret Leakage

One of the biggest security problems is accidentally logging secrets.

**Bad:**

```java
logger.info("Calling API with token: {}", token);
```

This can result in:

```
Application Logs
       |
       v
Authorization: Bearer eyJhbGci...
```

Now anyone with log access may obtain the token.

**Instead:**

```
Authorization: Bearer ******
```

Secrets should generally never appear in logs, error messages, metrics, traces, screenshots, or source control.

---

## 19. CI/CD and Secrets

Consider:

```
Developer
    |
    v
Git
    |
    v
CI/CD
    |
    v
Deployment
    |
    v
Production
```

The CI/CD pipeline may need credentials.

**Bad:**

```yaml
password: my-production-password
```

inside the pipeline configuration.

**Better:**

```
CI/CD
  |
  | authenticate using workload identity
  v
Secrets Manager
  |
  v
Retrieve required secret
```

This avoids putting long-lived credentials directly into the repository.

---

## 20. Secrets Management Security Model

A strong system follows:

```
              Secret
                |
       +--------+--------+
       |        |        |
       v        v        v
 Encryption   Access   Rotation
              Control
       |        |        |
       v        v        v
    At Rest  Least     Automatic
             Privilege
```

And:

```
Authentication
      ↓
Authorization
      ↓
Secret Retrieval
      ↓
Secret Usage
      ↓
Audit
      ↓
Rotation
```

---

## 21. What Happens If a Secret Is Compromised?

Suppose an API key leaks:

```
API_KEY = ABC123
```

A good secrets-management system allows you to:

```
Detect
  ↓
Revoke
  ↓
Rotate
  ↓
Issue new secret
  ↓
Update applications
```

For example:

```
Old API Key
    ↓
Compromised
    ↓
Revoke ❌
    ↓
Generate new key
    ↓
Update Secrets Manager
    ↓
Applications use new key
```

This is much better than having a secret that lives unchanged for years.

---

## 22. Secrets Management Best Practices

### 1. Never hardcode secrets

```java
password = "secret123"; ❌
```

### 2. Never commit secrets to Git

```
application.yml
.env
private.key
credentials.json
```

should be handled carefully and excluded from source control where appropriate.

### 3. Use least privilege

Don't give:

```
Order Service → access to ALL secrets ❌
```

Give:

```
Order Service → order-related secrets ✅
```

### 4. Rotate secrets

```
Old Secret
   ↓
Rotation
   ↓
New Secret
```

### 5. Use short-lived credentials where possible

Prefer:

```
Temporary credential
      ↓
expires
```

over:

```
Permanent credential
      ↓
never expires
```

### 6. Audit access

Record:

- Who accessed the secret?
- When?
- Which secret?
- From which workload?
- Was access allowed/denied?

### 7. Don't log secrets

Never log:

- Passwords
- API keys
- JWTs
- Private keys
- OAuth secrets

### 8. Encrypt secrets at rest

The secrets database itself should be protected using strong encryption and key management.

---

## 23. Secrets Management vs Configuration

This distinction is important.

### Configuration

Usually non-sensitive:

```yaml
server:
  port: 8080

cache:
  host: redis.internal

feature:
  payment-v2: true
```

### Secret

Sensitive:

```yaml
database:
  password: ********
```

So:

```
Configuration → application behavior
Secrets       → sensitive credentials/keys
```

They can be managed together operationally, but security requirements differ.

---

## 24. Secrets Management vs KMS

Another important interview distinction:

| Secrets Manager | KMS |
|---|---|
| Stores/retrieves secrets | Manages cryptographic keys |
| DB passwords | Encryption keys |
| API keys | Key generation |
| OAuth client secrets | Encrypt/decrypt operations |
| Credentials | Key rotation/policies |
| Application secrets | Often backed by HSM |

In practice they can work together:

```
Secrets Manager
       |
       v
Encrypted Secret
       |
       v
KMS
       |
       v
Encryption Key
```

---

## 25. Real-World Payment Example

Suppose you have:

```
Payment Service
```

It needs:

- DB password
- Stripe/API provider key
- JWT signing information
- TLS certificate/private key

**Don't do:**

```
Payment Service
      |
      +-- DB password in code ❌
      +-- API key in code ❌
      +-- Private key in Git ❌
```

**Instead:**

```
                  Secrets Manager
                 /       |       \
                /        |        \
               v         v         v
          DB Password  API Key  TLS Secret
               |
               v
         Payment Service
```

The service authenticates using its workload identity:

```
Payment Service
      |
      | "I am payment-service"
      v
Identity Provider
      |
      | identity/token
      v
Secrets Manager
      |
      | Policy check
      v
Payment secrets
```

---

## 26. Complete Production Architecture

A senior-level architecture could look like:

```
                    Internet
                       |
                       v
                    WAF
                       |
                       v
                Load Balancer
                       |
                       v
                  API Gateway
                       |
                       v
              +------------------+
              | Payment Service  |
              +--------+---------+
                       |
              Authenticate workload
                       |
                       v
              +------------------+
              | Secrets Manager  |
              +--------+---------+
                       |
              +--------+---------+
              |                  |
              v                  v
        DB Credentials       API Key
              |
              v
          Database
```

With:

```
TLS
 ↓
Authentication
 ↓
Authorization
 ↓
Secrets Manager
 ↓
Least Privilege
 ↓
Encryption at Rest
 ↓
Audit Logging
 ↓
Rotation
```

---

## 27. Interview Answer

If the interviewer asks:

> "What is Secrets Management and how does it work?"

A strong answer is:

> Secrets management is the practice of securely storing, accessing, rotating, and auditing sensitive credentials such as database passwords, API keys, OAuth secrets, certificates, and private keys. Instead of hardcoding secrets in source code or configuration files, applications authenticate to a centralized secrets manager using a workload identity or IAM mechanism. The secrets manager authenticates the workload, evaluates its authorization policy, and returns only the secrets that workload is allowed to access. Secrets are encrypted at rest and in transit, access is audited, and credentials should be rotated regularly. In a microservices architecture, each service should have least-privilege access to only its required secrets. Short-lived credentials and automated rotation further reduce the impact of credential compromise.

### Mental model

```
                SECRETS MANAGEMENT
                       |
        +--------------+--------------+
        |              |              |
        v              v              v
     Store          Access          Rotate
        |              |              |
        v              v              v
   Encrypted       IAM/RBAC       Automatic
    secrets       Least privilege  rotation
        |              |              |
        +--------------+--------------+
                       |
                       v
                    Audit
```

The key system-design principle is:

> Never make your application responsible for securely distributing its own long-lived secrets. Give the workload an identity, authorize that identity to retrieve only what it needs, and rotate credentials automatically.

