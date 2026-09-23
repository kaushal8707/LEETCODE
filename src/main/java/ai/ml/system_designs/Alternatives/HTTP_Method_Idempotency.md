# HTTP Method Idempotency

Yes. This is a very common System Design / REST API interview question.

First, one important point:

> Idempotency is about the effect of repeating the same request multiple times.

> If making the same request 1 time or 10 times results in the same final server state, the operation is idempotent.

---

## HTTP Methods and Idempotency

| HTTP Method | Idempotent? | Why? |
|---|---|---|
| GET | ✅ Yes | Reading data doesn't change the resource |
| PUT | ✅ Yes | Sets/replaces the resource with the same representation |
| DELETE | ✅ Yes | After the first deletion, subsequent deletes leave it deleted |
| POST | ❌ Usually no | Repeating it can create multiple resources |
| PATCH | ❌ Usually no | Depends on what the PATCH operation does |
| CREATE | ⚠️ Not an HTTP method | Depends on how the API implements it |

Let's understand each one.

---

## 1. GET — Idempotent ✅

Example:

```
GET /users/101
```

You send it once:

```
GET → User 101
```

You send it 10 times:

```
GET → User 101
GET → User 101
GET → User 101
...
```

The server state doesn't change.

```
Database:

Before GET → User 101 exists
After GET  → User 101 exists
```

Therefore:

> GET is idempotent.

Note: GET is also intended to be **safe** (no state-changing side effect).

---

## 2. PUT — Idempotent ✅

Suppose:

```
PUT /users/101
{
  "name": "Kaushal",
  "city": "Mumbai"
}
```

First request:

```
User 101
name = Kaushal
city = Mumbai
```

Send exactly the same request again:

```
PUT
name = Kaushal
city = Mumbai
```

Final state is still:

```
name = Kaushal
city = Mumbai
```

Send it 10 times:

```
PUT
PUT
PUT
...
```

Final state remains the same.

Therefore:

> PUT is idempotent.

### Important Distinction

PUT generally means:

> "Make this resource look like this."

So repeating the same PUT produces the same final state.

---

## 3. DELETE — Idempotent ✅

Suppose:

```
DELETE /users/101
```

First request:

```
User 101 → Deleted
```

Second request:

```
User 101 → Already deleted
```

Third:

```
Already deleted
```

Final state:

```
User 101 → Does not exist
```

Therefore:

> DELETE is idempotent.

The HTTP response may differ between the first and subsequent requests—for example, 204 first and 404 later—but idempotency is about the resulting server state, not identical responses.

---

## 4. POST — Usually NOT Idempotent ❌

This is the important one for payments and orders.

Suppose:

```
POST /orders
{
  "productId": 101,
  "quantity": 1
}
```

First request:

```
POST
   ↓
Order #1001 created
```

Retry:

```
POST
   ↓
Order #1002 created
```

Again:

```
POST
   ↓
Order #1003 created
```

So:

```
1 request  → 1 order
3 requests → 3 orders
```

Therefore:

> POST is generally non-idempotent.

---

## 5. Payment Example

Imagine:

```
POST /payments
{
  "amount": 1000
}
```

First request:

```
POST
 ↓
₹1,000 charged
```

Retry:

```
POST
 ↓
₹1,000 charged again
```

Now:

```
₹2,000 charged ❌
```

That's why payment APIs commonly use:

```
Idempotency-Key: PAY-12345
```

Then:

```
POST + PAY-12345
        ↓
₹1,000 charged

Retry
        ↓
POST + PAY-12345
        ↓
Already processed
        ↓
Return previous result
```

So although POST is not inherently idempotent, you can design a particular POST API to behave idempotently using an idempotency key.

---

## 6. What About CREATE?

CREATE is not an HTTP method.

You may see something like:

```
CREATE USER
CREATE ORDER
CREATE PAYMENT
```

Usually this is implemented using:

```
POST /users
POST /orders
POST /payments
```

For example:

```
POST /users
```

typically means:

```
Create a new user
```

Repeating it may create:

```
User 1
User 2
User 3
```

So a typical CREATE operation is:

> ❌ Not idempotent

But you can design creation to be idempotent.

---

## 7. CREATE Can Be Made Idempotent

Suppose:

```
POST /orders
Idempotency-Key: ORDER-123
```

First request:

```
ORDER-123
    ↓
Create Order #1001
```

Retry:

```
ORDER-123
    ↓
Order already created
    ↓
Return Order #1001
```

So:

```
POST + Idempotency Key
        ↓
Can be made idempotent
```

---

## 8. PATCH — Depends

PATCH is interesting.

Suppose:

```
PATCH /users/101
{
  "city": "Mumbai"
}
```

If the operation means:

```
Set city to Mumbai
```

then repeating it:

```
PATCH
PATCH
PATCH
```

could be idempotent.

Final state:

```
city = Mumbai
```

But consider:

```json
{
  "incrementBalance": 100
}
```

First:

```
Balance = ₹1,000
       ↓
+ ₹100
       ↓
₹1,100
```

Retry:

```
+ ₹100
       ↓
₹1,200
```

That's not idempotent.

So:

> PATCH can be idempotent or non-idempotent depending on the operation.

---

## 9. Easy Way to Remember

```
GET      → ✅ Idempotent
PUT      → ✅ Idempotent
DELETE   → ✅ Idempotent

POST     → ❌ Usually NOT idempotent
PATCH    → ⚠️ Depends

CREATE   → ⚠️ Not an HTTP method
             Usually POST
             Usually non-idempotent
```

---

## 10. One Important Interview Trap

Don't say:

> "PUT is always idempotent because HTTP says so."

The more precise statement is:

> HTTP defines PUT and DELETE as idempotent methods, meaning the intended effect of multiple identical requests is the same as one request. However, an implementation can still have unintended external side effects, so API design matters.

For example, suppose:

```
PUT /user/101
```

also sends an email every time it is called.

The resource state can remain the same, but the email side effect occurs multiple times.

So when discussing idempotency in System Design, distinguish:

> Resource state vs external side effects.

---

## 11. Final Picture

```
                    HTTP METHODS
                         |
       ┌─────────────────┼──────────────────┐
       ↓                 ↓                  ↓
    GET/PUT            DELETE             POST
       ↓                 ↓                  ↓
      ✅                  ✅                 ❌
   Idempotent         Idempotent       Usually not
                                           |
                                           ↓
                                  Idempotency Key
                                           |
                                           ↓
                                  Can make operation
                                    safely retryable
```

---

## The Key Interview Sentence

> GET, PUT, and DELETE are idempotent by HTTP semantics. POST is generally non-idempotent, while PATCH depends on the operation. "Create" isn't an HTTP method; when implemented with POST, it is usually non-idempotent unless we explicitly design it with something like an idempotency key.
