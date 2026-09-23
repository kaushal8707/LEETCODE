# Idempotency in a Payment System

Idempotency is one of the most important concepts in payment-system design.

The problem it solves is:

> What happens if the same payment request is sent multiple times?

We want:

> One payment request → One financial effect, even if the request is retried multiple times.

---

## 1. Real-Time Payment Problem

Suppose you are buying a product for ₹1,000.

Your client sends:

```
Client
  |
  | Pay ₹1,000
  ↓
Payment Service
```

Payment Service successfully charges the customer:

```
Customer Account
      ↓
    -₹1,000
```

But then something goes wrong.

Maybe the network fails:

```
Client
  |
  | Pay ₹1,000
  ↓
Payment Service
  |
  | ₹1,000 deducted ✅
  X
  |
Network failure
```

The client never receives the response.

From the client's perspective:

> "I don't know whether payment succeeded."

So the client retries:

```
Client
  |
  | Pay ₹1,000 AGAIN
  ↓
Payment Service
```

Without idempotency:

```
First request  → -₹1,000
Second request → -₹1,000

Total = -₹2,000 ❌
```

This is obviously unacceptable.

---

## 2. Idempotency Solves This

The client sends an Idempotency Key with the payment request.

For example:

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
```

Payment Service processes it:

```
PAY-12345
₹1,000
SUCCESS
```

It stores the result.

Now suppose the client retries:

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
Has PAY-12345 already been processed?
```

Answer:

```
YES
```

So it does not charge the customer again.

Instead, it returns the original result:

```
Payment = SUCCESS
Transaction ID = TXN-98765
```

Therefore:

```
First request   → ₹1,000 charged
Retry request   → ₹0 additional charge

Total           → ₹1,000 ✅
```

---

## 3. Simple Architecture

```
                    Client
                      |
                      |
             Idempotency-Key:
                PAY-12345
                      |
                      ↓
              Payment Service
                      |
                      ↓
             Idempotency Store
                      |
                ┌─────┴─────┐
                ↓           ↓
             EXISTS       NOT EXISTS
                |             |
                ↓             ↓
         Return previous    Process
             result         payment
                              |
                              ↓
                         Save result
```

---

## 4. What Is an Idempotency Key?

An idempotency key is a unique identifier for one logical operation.

For example:

```
PAY-12345
```

or a UUID:

```
550e8400-e29b-41d4-a716-446655440000
```

The client generates it before making the payment request.

Example HTTP request:

```
POST /payments
Idempotency-Key: 550e8400-e29b-41d4-a716-446655440000
Content-Type: application/json
{
  "orderId": "ORD-1001",
  "amount": 1000,
  "currency": "INR"
}
```

---

## 5. What Does the Payment Service Store?

The payment service can maintain an idempotency record.

For example:

```
idempotency_key | status    | transaction_id | response
----------------------------------------------------------------
PAY-12345       | SUCCESS   | TXN-98765      | Payment successful
```

When another request comes with:

```
PAY-12345
```

the service looks it up.

```
If it doesn't exist:
PAY-12345 → Process payment

If it exists:
PAY-12345 → Return existing result
```

---

## 6. Java/Spring Boot Example

A simplified API might look like:

```java
@PostMapping("/payments")
public PaymentResponse makePayment(
        @RequestHeader("Idempotency-Key") String idempotencyKey,
        @RequestBody PaymentRequest request) {

    Payment existingPayment =
            paymentRepository.findByIdempotencyKey(idempotencyKey);

    if (existingPayment != null) {
        return existingPayment.getResponse();
    }

    Payment payment = processPayment(request);

    payment.setIdempotencyKey(idempotencyKey);

    paymentRepository.save(payment);

    return payment.getResponse();
}
```

Conceptually:

```
Request
   |
   ↓
Check Idempotency Key
   |
   ├── Exists → Return previous response
   |
   └── Doesn't exist
            ↓
       Process Payment
            ↓
       Save Result
            ↓
       Return Response
```

---

## 7. But There Is a BIG Problem With the Above Code

Consider two requests arriving at exactly the same time:

```
Request 1 → PAY-12345
Request 2 → PAY-12345
```

Both might execute:

```
Request 1 → Check DB → Not Found
Request 2 → Check DB → Not Found
```

Then:

```
Request 1 → Charge ₹1,000
Request 2 → Charge ₹1,000
```

Oops! ❌

So simply checking:

```
findByIdempotencyKey()
```

is not enough.

We need **concurrency protection**.

---

## 8. Use a Unique Constraint

One common solution is to put a unique constraint on the idempotency key.

For example:

```sql
CREATE TABLE payments (
    id BIGINT PRIMARY KEY,
    idempotency_key VARCHAR(100) UNIQUE,
    order_id VARCHAR(100),
    amount DECIMAL(10,2),
    status VARCHAR(30),
    transaction_id VARCHAR(100)
);
```

Now:

```
PAY-12345
```

can exist only once.

But there is another important issue: when exactly do we record the idempotency key relative to the external payment provider?

That's where real payment systems become more interesting.

---

## 9. Payment Provider Example

Suppose your architecture is:

```
Client
  |
  ↓
Your Payment Service
  |
  ↓
Payment Gateway
  |
  ↓
Bank
```

Suppose your service sends:

```
₹1,000
```

to the payment gateway.

The gateway successfully charges the customer:

```
₹1,000 deducted ✅
```

But your service times out:

```
Payment Gateway
      |
      | SUCCESS
      X
      |
   Timeout
      |
      ↓
Your Payment Service
```

Your service doesn't know whether the payment succeeded.

It retries.

If the gateway itself supports idempotency, send the same idempotency key to it:

```
Your Service
      |
      | PAY-12345
      ↓
Payment Gateway
```

Retry:

```
Your Service
      |
      | PAY-12345
      ↓
Payment Gateway
```

Gateway recognizes:

```
PAY-12345 → Already processed
```

and returns the original transaction result.

This is much safer.

---

## 10. Idempotency at Multiple Levels

In a real payment system, you may have:

```
Client
  ↓
Payment Service
  ↓
Payment Gateway
  ↓
Bank
```

Idempotency may need to be considered at each boundary.

```
Client
   |
   | Idempotency Key
   ↓
Payment Service
   |
   | Same logical operation ID
   ↓
Payment Gateway
   |
   ↓
Bank
```

The important thing is that the same logical payment operation can be correlated across retries and components.

---

## 11. What If the Same Key Has Different Data?

Suppose the first request is:

```
Idempotency-Key: PAY-12345

Amount = ₹1,000
```

Then someone sends:

```
Idempotency-Key: PAY-12345

Amount = ₹5,000
```

This should not be treated as the same valid request.

The service should detect that:

```
PAY-12345

Original:
Amount = ₹1,000

New:
Amount = ₹5,000
```

and reject it.

For example:

```
400 Bad Request

Idempotency key already used with different request parameters.
```

Therefore, you generally store enough information to validate that a retry represents the same logical operation.

---

## 12. Idempotency Is Not the Same as "Duplicate Request Detection"

There's a subtle distinction.

### Duplicate Detection

You might simply say:

> "I've seen this request before."

### Idempotency

You say:

> "Regardless of how many times this operation is requested, the business effect should occur only once."

For payments:

```
10 retries
    ↓
1 financial charge
```

That's the goal.

---

## 13. What About Failed Payments?

Suppose:

```
PAY-12345
```

was attempted and payment failed.

The client retries using the same key.

What should happen?

Usually, the service should return the stored result according to the API's idempotency contract rather than blindly treating the retry as a new payment.

For example:

First attempt:

```
PAY-12345
    ↓
FAILED
```

Retry:

```
PAY-12345
    ↓
Return FAILED
```

If the business wants a new payment attempt, it should generally create a new logical operation / new idempotency key, depending on the payment API's semantics.

---

## 14. Idempotency and Retry

These two concepts go together.

In distributed systems, we often use:

```
Timeout
   ↓
Retry
   ↓
Retry
   ↓
Retry
```

But retries can create duplicates.

Therefore:

```
Retry
 +
Idempotency
 =
Safe retry
```

For example:

```
Request #1
PAY-12345
   ↓
Timeout

Request #2
PAY-12345
   ↓
Already processed
   ↓
Return original result
```

---

## 15. Idempotency + Database Transaction

You also need to carefully handle the race between recording the payment and performing the external side effect.

A naive sequence like:

```
1. Check idempotency key
2. Charge external gateway
3. Save idempotency record
```

can have a problem:

```
Request 1
   ↓
Check → Not Found
   ↓
Charge gateway → SUCCESS
   ↓
Application crashes ❌
   ↓
Record never saved
```

Now a retry might see:

```
No record
```

and charge again.

So robust payment systems need careful coordination with the payment provider, durable transaction state, unique constraints, and reconciliation/status-check mechanisms.

This is one reason payment systems are a classic distributed-systems problem.

---

## 16. A Better High-Level Design

A simplified robust flow could look like:

```
                    Client
                      |
                      | Idempotency-Key
                      ↓
                Payment Service
                      |
                ┌─────┴─────┐
                ↓           ↓
          Idempotency    Payment
             Store       Record
                            |
                            ↓
                     Payment Gateway
                            |
                            ↓
                          Bank
```

Flow:

```
1. Client generates idempotency key
             ↓
2. Payment Service receives request
             ↓
3. Check existing operation
             ↓
4. If completed → return previous result
             ↓
5. Otherwise create/lock operation state
             ↓
6. Call payment provider using a stable operation key
             ↓
7. Store final status
             ↓
8. Return result
```

For uncertain outcomes:

```
Payment status = UNKNOWN
       ↓
Don't blindly charge again
       ↓
Query provider / reconcile
       ↓
Determine final state
```

---

## 17. Real-Life Scenario

Imagine you click:

```
"Pay ₹10,000"
```

Your mobile network is poor.

```
You
 ↓
Pay button
 ↓
Payment request
 ↓
Payment Service
 ↓
Bank
 ↓
₹10,000 deducted
```

But your phone receives no response.

You click Pay again.

Without idempotency:

```
₹10,000
+
₹10,000
=
₹20,000 ❌
```

With idempotency:

```
First request:
PAY-ABC123 → ₹10,000 → SUCCESS

Second request:
PAY-ABC123 → Already processed

Result:
Only ₹10,000 deducted ✅
```

---

## 18. Interview Answer

If an interviewer asks:

> "What is idempotency in a payment system?"

You can say:

> Idempotency ensures that multiple requests representing the same logical payment operation produce only one financial effect. This is especially important because distributed systems can experience timeouts, network failures, and retries. We assign a unique idempotency key to each payment operation. The payment service stores the key and the resulting transaction state. If the same key is received again, it returns the existing result instead of processing the payment again. We also need concurrency control, such as a unique database constraint, and should propagate a stable operation identifier to the payment provider when supported.

---

## Remember This Diagram

```
             Same Payment Request
                  PAY-123
                      |
          ┌───────────┴───────────┐
          ↓                       ↓
      Request 1               Request 2
          |                       |
          └───────────┬───────────┘
                      ↓
              Payment Service
                      |
                PAY-123 exists?
                   /       \
                 YES        NO
                  |          |
                  ↓          ↓
           Return result   Process
           Don't charge    payment
           again              |
                              ↓
                         Save result
```

One key sentence to remember:

> 🔑 Idempotency = Same logical payment + multiple retries = One financial effect.
