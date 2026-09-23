# Authorization — What It Is and How It Works

**Authorization** is the process of determining what an authenticated user or service is allowed to access or perform.

In simple terms:

> Authorization = "What are you allowed to do?"

Authentication and authorization work together:

```
Authentication → Who are you?
Authorization  → What can you do?
```

---

## 1. Authentication vs Authorization

Suppose you log in to an e-commerce application.

```
Username: kaushal
Password: ********
```

The system first verifies:

```
"Is this really Kaushal?"
        ↓
Authentication
        ↓
YES
```

Then it asks:

```
"What is Kaushal allowed to do?"
        ↓
Authorization
```

For example:

```
Customer
 ├── View products       ✅
 ├── Create order        ✅
 ├── Cancel own order    ✅
 ├── Delete user         ❌
 └── View admin panel    ❌
```

---

## 2. Basic Authorization Flow

A typical request looks like this:

```
                    User
                     |
                     | Login
                     v
              Authentication
                     |
                     | Identity verified
                     v
                Access Token
                     |
                     | Request + token
                     v
                API Gateway
                     |
                     | Validate token
                     v
             Authorization
                     |
              +------+------+
              |             |
            Allowed       Denied
              |             |
              v             v
          Resource       HTTP 403
```

---

## 3. Step 1 — User Authenticates

The user logs in:

```json
POST /login
{
  "username": "kaushal",
  "password": "password"
}
```

The authentication service verifies the credentials.

If successful, it might issue an access token:

```
Access Token
```

For example, the token may contain claims such as:

```json
{
  "sub": "12345",
  "role": "CUSTOMER",
  "scope": "orders:read orders:create"
}
```

Now the application knows:

```
User ID = 12345
Role    = CUSTOMER
```

---

## 4. Step 2 — User Requests a Resource

The user wants to create an order:

```
POST /api/orders
Authorization: Bearer <access-token>
```

The server first validates the token.

Then authorization begins.

The server asks:

```
Who is this?
        ↓
User 12345

What role?
        ↓
CUSTOMER

What permission?
        ↓
orders:create

Is this operation allowed?
        ↓
YES
```

Then the request reaches the Order Service.

---

## 5. What Happens When Authorization Fails?

Suppose a customer tries:

```
DELETE /api/users/456
```

The authorization system checks:

```
User role = CUSTOMER

Required permission =
users:delete
```

Result:

```
CUSTOMER
   |
   | users:delete ?
   |
   └── ❌ NO
```

The server returns:

```
403 Forbidden
```

This is different from authentication failure.

```
401 Unauthorized
→ Authentication failed / credentials not accepted

403 Forbidden
→ User is authenticated but doesn't have permission
```

---

## 6. Role-Based Access Control — RBAC

One of the most common authorization models is **RBAC (Role-Based Access Control)**.

Instead of assigning permissions individually to every user, we assign users to roles.

```
User
 ↓
Role
 ↓
Permissions
```

Example:

```
Kaushal
   ↓
ADMIN
   ↓
+ users:read
+ users:create
+ users:update
+ users:delete
+ orders:read
+ orders:update
```

Another user:

```
Rahul
   ↓
CUSTOMER
   ↓
+ products:read
+ orders:read
+ orders:create
```

---

## 7. RBAC Example

Imagine an application with three roles:

```
ADMIN
 ├── CREATE_USER
 ├── DELETE_USER
 ├── UPDATE_USER
 └── VIEW_USER

MANAGER
 ├── VIEW_USER
 └── UPDATE_USER

CUSTOMER
 ├── VIEW_PROFILE
 └── CREATE_ORDER
```

Authorization table:

| Role | Permission |
|---|---|
| ADMIN | CREATE_USER |
| ADMIN | DELETE_USER |
| ADMIN | UPDATE_USER |
| ADMIN | VIEW_USER |
| MANAGER | VIEW_USER |
| MANAGER | UPDATE_USER |
| CUSTOMER | VIEW_PROFILE |
| CUSTOMER | CREATE_ORDER |

When a request arrives:

```
User → Role → Permission → Resource
```

---

## 8. Permission-Based Authorization

Instead of checking only roles, applications can check specific permissions.

For example:

```
orders:read
orders:create
orders:update
orders:delete
```

A user could have:

```
CUSTOMER

orders:read
orders:create
```

But not:

```
orders:delete
```

This is more granular than simply checking:

```
if role == "ADMIN"
```

---

## 9. Resource-Based Authorization

Sometimes permission depends on which resource the user is accessing.

For example:

```
GET /users/123/orders/456
```

Suppose:

```
User = 123
Order owner = 123
```

Access:

```
✅ ALLOWED
```

But:

```
User = 123
Order owner = 999
```

Access:

```
❌ DENIED
```

Even though both users might have the same role.

So authorization can ask:

> "Does this user have permission to access this specific resource?"

This is extremely common in real applications.

---

## 10. Attribute-Based Access Control — ABAC

Another model is **ABAC (Attribute-Based Access Control)**.

Instead of relying only on roles, authorization uses attributes.

For example:

```
User:
    department = Finance
    country = India
    clearance = HIGH

Resource:
    department = Finance
    classification = CONFIDENTIAL

Request:
    time = 10:30 AM
```

Policy:

```
Allow access if:

user.department == resource.department
AND
user.clearance >= resource.classification
AND
request.time is within working hours
```

This provides much more dynamic authorization.

---

## 11. Policy-Based Authorization

Large systems often have centralized policies.

For example:

```
Policy:

ALLOW
IF
    role = ADMIN

OR

ALLOW
IF
    role = CUSTOMER
    AND
    action = READ
    AND
    resource.owner = user.id
```

The authorization engine evaluates the policy.

Conceptually:

```
             Request
                |
                v
       ┌─────────────────┐
       │ Authorization   │
       │     Engine      │
       └────────┬────────┘
                |
        Evaluate Policy
                |
          +-----+-----+
          |           |
        ALLOW        DENY
          |           |
          v           v
      Resource       403
```

---

## 12. Authorization in Microservices

This becomes very important in a microservices architecture.

Suppose:

```
                    Client
                       |
                       v
                 API Gateway
                       |
          +------------+------------+
          |            |            |
          v            v            v
     User Service  Order Service  Payment Service
```

The client sends:

```
Authorization: Bearer <token>
```

The API Gateway can validate the token.

Then individual services can perform their own authorization checks.

For example:

```
API Gateway
    |
    | Is token valid?
    v
Order Service
    |
    | Can user create order?
    v
Authorization
    |
    +---- YES ---> Create Order
    |
    +---- NO ----> 403 Forbidden
```

---

## 13. Why Authorization Should Not Exist Only at the API Gateway

A common mistake is:

```
Client
  ↓
API Gateway
  ↓
Order Service
```

and assuming:

> "The Gateway already checked authorization, so the service doesn't need to."

This can be dangerous.

A service may be accessed through:

- API Gateway
- Internal Service
- Message Queue
- Admin Tool
- Batch Job

Therefore, sensitive business authorization should also be enforced close to the resource/service.

For example:

```
API Gateway
     |
     | Basic authorization
     v
Order Service
     |
     | Business authorization
     v
Order Database
```

---

## 14. Authentication + Authorization in a Microservice Request

A complete request can look like:

```
Client
  |
  | Authorization: Bearer JWT
  v
API Gateway
  |
  | 1. Validate JWT
  | 2. Identify user
  v
Order Service
  |
  | 3. Check permission
  | 4. Check resource ownership
  v
Database
```

Example:

```
User ID       = 123
Role          = CUSTOMER
Permission    = orders:read
Order Owner   = 123
```

All checks pass:

```
Authentication ✅
Authorization  ✅
Ownership      ✅
```

Therefore:

```
200 OK
```

---

## 15. Authorization Using JWT

A JWT might contain:

```json
{
  "sub": "12345",
  "roles": ["CUSTOMER"],
  "permissions": [
    "orders:read",
    "orders:create"
  ],
  "exp": 1790000000
}
```

The service extracts:

```
userId
roles
permissions
```

Then:

```
Request:
POST /orders

Required:
orders:create

Token:
orders:create

Result:
✅ ALLOW
```

For:

```
DELETE /orders/100
```

Required:

```
orders:delete
```

Token doesn't contain it:

```
❌ DENY
```

---

## 16. Important Security Point

> Never trust authorization information supplied directly by the client.

For example, don't do this:

```json
{
  "userId": "123",
  "role": "ADMIN"
}
```

and blindly trust:

```
role = ADMIN
```

A malicious client could change:

```
CUSTOMER → ADMIN
```

Authorization information should come from a trusted source, such as:

- A validated JWT
- Server-side session
- Identity Provider
- Authorization service
- Policy engine
- Database

---

## 17. Access Token and Refresh Token

Authorization commonly works with access tokens.

```
Login
  |
  +---- Access Token
  |
  +---- Refresh Token
```

### Access Token

Used to access APIs:

```
Authorization: Bearer <access-token>
```

Usually short-lived.

Example:

```
Expires in: 15 minutes
```

### Refresh Token

Used to obtain a new access token.

```
Refresh Token
      |
      v
Authentication Server
      |
      v
New Access Token
```

This reduces the risk of keeping a powerful access token valid for a long time.

---

## 18. Authorization Models

For system design interviews, understand these:

```
Authorization
│
├── RBAC
│   └── Role-Based Access Control
│
├── ABAC
│   └── Attribute-Based Access Control
│
├── ACL
│   └── Access Control List
│
├── Resource-Based Authorization
│
├── Permission-Based Authorization
│
└── Policy-Based Authorization
```

---

## 19. Real-World Example — Banking Application

Suppose a banking application has:

```
ADMIN
CUSTOMER
SUPPORT_AGENT
```

A customer:

```
View own account       ✅
Transfer money         ✅
View another customer  ❌
Delete account         ❌
```

A support agent:

```
View customer profile  ✅
Update customer info   ✅
Transfer money         ❌
Delete account         ❌
```

An admin:

```
View users             ✅
Create users           ✅
Delete users           ✅
Manage permissions     ✅
```

But authorization should also check **ownership**.

For example:

```
Customer A
    |
    | GET /accounts/B
    v
Authorization
    |
    | Is account B owned by Customer A?
    |
    └── NO → 403 Forbidden
```

So role alone is often not enough.

---

## 20. Authentication + Authorization — Complete Picture

```
                     CLIENT
                        |
                        | username/password
                        v
                ┌───────────────┐
                │ Authentication │
                │    Service     │
                └───────┬───────┘
                        |
                        | Access Token
                        v
                     CLIENT
                        |
                        | Bearer Token
                        v
                  API Gateway
                        |
                        | Validate Token
                        v
                ┌───────────────┐
                │ Authorization │
                │     Check     │
                └───────┬───────┘
                        |
              +---------+---------+
              |                   |
            ALLOW                DENY
              |                   |
              v                   v
        Business Service        403
              |
              v
           Database
```

---

## 21. The Most Important Mental Model

Remember this:

```
                REQUEST
                   |
                   v
          ┌─────────────────┐
          │ Authentication  │
          └────────┬────────┘
                   |
             Who are you?
                   |
                   v
             User Identity
                   |
                   v
          ┌─────────────────┐
          │ Authorization   │
          └────────┬────────┘
                   |
          What can you do?
                   |
                   v
             Permission
                   |
                   v
             Resource
```

### One-line difference

> Authentication verifies identity; authorization verifies permissions.

---

## Next Steps

> For a 10-year-experience system design interview, the natural next step is to understand **OAuth 2.0 + OpenID Connect + JWT + Access Token/Refresh Token**, and then design a centralized **Authorization Service** for microservices.

