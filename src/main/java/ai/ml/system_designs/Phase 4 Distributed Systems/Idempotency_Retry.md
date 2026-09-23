# Idempotency + Retry in System Design

These two concepts are closely related in distributed systems.

The simple idea is:

> Retry helps when a request fails or times out. Idempotency makes that retry safe.

---

## 1. Why Do We Need Both?

Suppose:

```
Client
   |
   | Request
   ↓
Server
```

Client sends a request, but the network fails before the response comes back.

```
Client
   |
   | Request
   ↓
Server
   |
   | Processing
   ↓
   SUCCESS
   |
   X
 Network failure
   |
   ↓
Client doesn't receive response
```

The client thinks:

> "Maybe the request failed."

So it retries:

```
Client
   |
   | Retry
   ↓
Server
```

### Problem

The first request may actually have succeeded.

So now the server receives:

```
Request 1 → SUCCESS
Request 2 → SUCCESS
```

For a payment:

```
₹1,000
+
₹1,000
=
₹2,000 ❌
```

This is where idempotency comes in.

---

## 2. Retry Without Idempotency

Imagine:

```
Client
  |
  | Pay ₹1,000
  ↓
Payment Service
  |
  ↓
Payment Gateway
  |
  ↓
₹1,000 charged ✅
```

But response is lost:

```
Payment Gateway
      |
      | SUCCESS
      X
      |
   Network
      |
      ↓
Client
```

Client retries:

```
Client
  |
  | Pay ₹1,000
  ↓
Payment Service
  |
  ↓
Payment Gateway
  |
  ↓
₹1,000 charged again ❌
```

Result:

```
₹1,000 + ₹1,000 = ₹2,000
```

---

## 3. Retry With Idempotency

The client generates an idempotency key:

```
Idempotency-Key: PAY-12345
```

First request:

```
Client
  |
  | PAY-12345
  | ₹1,000
  ↓
Payment Service
  |
  ↓
Payment Gateway
  |
  ↓
₹1,000 charged ✅
```

Response is lost.

Client retries with the same key:

```
Client
  |
  | PAY-12345
  | ₹1,000
  ↓
Payment Service
```

Payment Service checks:

```
PAY-12345 already processed?
```

Answer:

```
YES
```

So it doesn't perform the payment again.

```
Payment Service
      |
      ↓
Return previous result
      |
      ↓
SUCCESS
```

Result:

```
₹1,000 charged only once ✅
```

---

## 4. The Relationship

Think of it this way:

```
                 Distributed System
                        |
             ┌──────────┴──────────┐
             ↓                     ↓
           Retry              Idempotency
             |                     |
       Try operation          Make retry safe
           again
             |                     |
             └──────────┬──────────┘
                        ↓
                  Reliable System
```

Or simply:

```
Retry = "Try again."
Idempotency = "Trying again won't duplicate the business operation."
```

---

## 5. Common Retry Scenario

Suppose:

```
Client → Order Service
```

The client sends:

```
POST /orders
Idempotency-Key: ORDER-123
```

The Order Service processes it:

```
ORDER-123
   ↓
Create Order
   ↓
Order #5001 created
```

But the response is lost.

Client retries:

```
POST /orders
Idempotency-Key: ORDER-123
```

The server finds:

```
ORDER-123 → Order #5001
```

and returns:

```json
{
  "orderId": "5001",
  "status": "CREATED"
}
```

It does not create Order #5002.

---

## 6. How the Server Can Implement It

A simplified database table:

```
┌─────────────────────────────────────────────┐
│ idempotency_records                         │
├───────────────┬──────────┬──────────────────┤
│ key           │ status   │ response         │
├───────────────┼──────────┼──────────────────┤
│ PAY-12345     │ SUCCESS  │ TXN-98765        │
│ ORDER-456     │ SUCCESS  │ ORD-1001         │
└───────────────┴──────────┴──────────────────┘
```

Request comes in:

```
PAY-12345
```

Server:

```
            Request
               |
               ↓
       Check idempotency key
               |
        ┌──────┴──────┐
        ↓             ↓
      EXISTS       NOT EXISTS
        |             |
        ↓             ↓
 Return result     Process
                      |
                      ↓
                 Save result
                      |
                      ↓
                 Return result
```

---

## 7. Why a Unique Constraint Is Important

Consider two identical requests arriving simultaneously:

```
Request A → PAY-12345
Request B → PAY-12345
```

Both check:

```
PAY-12345 exists?
```

Both might see:

```
NO
```

Then both process the payment.

To prevent this, you can enforce:

```
UNIQUE(idempotency_key)
```

Conceptually:

```
Request A ──┐
            ├──→ Database
Request B ──┘

PAY-12345 → only one record allowed
```

But for a real payment flow, you also need to carefully coordinate the idempotency record with the external payment provider and handle uncertain outcomes.

---

## 8. Retry Is Not Always Safe

This is an important System Design interview point.

Some operations are naturally idempotent.

### GET

```
GET /users/123
```

Retrying:

```
GET
GET
GET
```

doesn't normally change the data.

So retry is generally safe.

### PUT

For example:

```
PUT /users/123
```

Set:

```
name = Kaushal
```

Repeating it generally produces the same final state.

```
PUT → name = Kaushal
PUT → name = Kaushal
PUT → name = Kaushal
```

### POST

POST often creates a new resource:

```
POST /orders
```

If you retry:

```
POST
POST
```

you might create:

```
Order #1001
Order #1002
```

So POST operations often need an idempotency key when retries are possible.

---

## 9. Retry + Idempotency in Payment

A typical flow:

```
                    Client
                       |
                       | PAY-123
                       ↓
                Payment Service
                       |
                 Check key
                       |
              ┌────────┴────────┐
              ↓                 ↓
          Already done?       New request
              |                 |
             YES                ↓
              |          Process payment
              |                 |
              |                 ↓
              |           Save status
              |                 |
              └────────┬────────┘
                       ↓
                  Return result
```

If timeout occurs:

```
Request
   ↓
Payment processing
   ↓
TIMEOUT
   ↓
Retry with SAME key
   ↓
Check status
   ↓
Return original result
```

---

## 10. What About Multiple Retries?

Suppose the client retries 5 times:

```
Request 1 → PAY-123
Request 2 → PAY-123
Request 3 → PAY-123
Request 4 → PAY-123
Request 5 → PAY-123
```

The server should ensure:

```
PAY-123
   ↓
One logical operation
   ↓
One payment
```

Not:

```
PAY-123
   ↓
5 payments ❌
```

---

## 11. Retry Strategies

Retry itself needs to be designed carefully.

### Immediate Retry

```
Request
   ↓
Failure
   ↓
Retry immediately
   ↓
Failure
   ↓
Retry immediately
```

This can overload an already struggling server.

### Exponential Backoff

Instead:

```
Attempt 1 → Fail
     ↓
wait 1 sec

Attempt 2 → Fail
     ↓
wait 2 sec

Attempt 3 → Fail
     ↓
wait 4 sec

Attempt 4 → Fail
```

Conceptually:

```
1 sec
   ↓
2 sec
   ↓
4 sec
   ↓
8 sec
```

This gives the server time to recover.

---

## 12. Add Jitter

If 1,000 clients all retry at exactly the same time:

```
1000 clients
     |
     | retry at 10:00:05
     ↓
   Server
     |
     ↓
Overloaded again ❌
```

So we add random jitter to the retry delay:

```
Client 1 → retry after 1.2 sec
Client 2 → retry after 1.7 sec
Client 3 → retry after 1.4 sec
Client 4 → retry after 1.9 sec
```

This spreads the load.

A common strategy is:

> Exponential backoff + jitter

---

## 13. Don't Retry Everything

Another important point:

```
Failure
   |
   ├── Temporary failure → Retry
   |
   └── Permanent failure → Don't retry
```

For example:

### Retry

- HTTP 503 Service Unavailable
- Network timeout
- Connection reset
- Temporary infrastructure failure

### Usually Don't Retry Blindly

- HTTP 400 Bad Request
- Invalid card details
- Invalid request
- Insufficient funds
- Unauthorized request

The exact retry policy depends on the API and failure semantics.

---

## 14. Retry + Timeout

These three concepts work together:

```
             Reliable Distributed Request
                       |
          ┌────────────┼────────────┐
          ↓            ↓            ↓
       Timeout       Retry     Idempotency
          |            |            |
   Don't wait forever  Try again   Safe retry
```

For example:

```
Client
  |
  | Request
  ↓
Server
  |
  | ...taking too long...
  |
Timeout
  |
  ↓
Retry
  |
  ↓
Same Idempotency Key
  |
  ↓
Safe result
```

---

## 15. Important Problem: "Unknown" Result

This is especially important in payment systems.

Suppose:

```
Client
   ↓
Payment Service
   ↓
Payment Gateway
   ↓
Bank
```

Bank successfully charges:

```
₹1,000 deducted ✅
```

But:

```
Payment Gateway
       |
       X
       |
Payment Service
```

The Payment Service doesn't know whether it succeeded.

The state is:

```
UNKNOWN
```

Do not blindly retry the payment as a new operation.

Instead, use the same idempotency key or query/reconcile the payment status, depending on the provider's API.

```
UNKNOWN
   |
   ├── Query payment status
   |
   ├── Use same idempotency key
   |
   └── Reconciliation
```

Eventually:

```
UNKNOWN
   ↓
SUCCESS
```

or:

```
UNKNOWN
   ↓
FAILED
```

---

## 16. End-to-End Example

Let's put everything together.

Customer clicks:

```
PAY ₹1,000
```

Client generates:

```
Idempotency-Key = PAY-ABC123
```

### Attempt 1

```
Client
  |
  | PAY-ABC123
  ↓
Payment Service
  |
  ↓
Payment Gateway
  |
  ↓
₹1,000 charged
  |
  X
Response lost
```

Client doesn't know the result.

### Retry

```
Client
  |
  | PAY-ABC123
  ↓
Payment Service
  |
  ↓
Check idempotency record
```

Suppose the payment service knows:

```
PAY-ABC123
Status = SUCCESS
Transaction = TXN-999
```

So:

```
No second charge
       ↓
Return TXN-999
       ↓
SUCCESS
```

Final result:

```
Customer charged = ₹1,000
Payments created = 1
Retries = 1+
```

---

## 17. Interview-Level Answer

If an interviewer asks:

> "How do you handle retries in a distributed system?"

A good answer is:

> "We use retries for transient failures such as network timeouts or temporary service unavailability. To avoid overwhelming the system, retries should use timeouts, exponential backoff and jitter, and we should limit the number of attempts. For operations that have side effects, such as payments or order creation, we use idempotency keys so that retries don't cause duplicate business operations. We also distinguish transient failures from permanent failures and avoid blindly retrying non-retryable errors."

And if they ask:

> "Why is idempotency required with retry?"

Say:

> "Because a timeout doesn't necessarily mean the operation failed. The server may have successfully processed the request but the response may have been lost. When the client retries, idempotency allows the server to recognize the same logical operation and return the original result instead of performing the side effect again."

---

## The Mental Model

```
             Request
                |
                ↓
             Timeout?
             /      \
           NO        YES
           |          |
           ↓          ↓
         Done       Retry
                        |
                        ↓
                Same Idempotency Key
                        |
                        ↓
               Already processed?
                    /       \
                  YES        NO
                   |          |
                   ↓          ↓
             Return old     Process
                result       request
```

Remember:

- 🔄 **Retry** = reliability mechanism
- 🔑 **Idempotency** = duplicate-prevention mechanism
- ⏱️ **Timeout** = prevents waiting forever
- 📈 **Exponential backoff + jitter** = prevents retry storms
