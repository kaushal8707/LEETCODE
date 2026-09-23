# Synchronous Communication in System Design

Synchronous communication means:

> The client sends a request to another service and waits for the response before continuing.

In simple terms:

```
Client
  |
  | Request
  ↓
Server
  |
  | Processing...
  |
  | Response
  ↓
Client
  |
  ↓
Continue
```

The caller is blocked/waiting for the response.

---

## 1. Real-Time Example — Login

Consider a normal login:

```
Mobile App
    |
    | username + password
    ↓
Auth Service
    |
    | validate credentials
    ↓
Database
    |
    | user found
    ↓
Auth Service
    |
    | JWT token
    ↓
Mobile App
```

The mobile application needs the response:

```json
{
  "token": "eyJhbGci...",
  "userId": "123"
}
```

The app can't meaningfully continue to the authenticated part of the application until it knows:

> Was login successful or not?

Therefore, synchronous communication makes sense.

---

## 2. How It Works

Suppose Service A needs information from Service B.

```
Service A
    |
    | HTTP Request
    ↓
Service B
    |
    | Process request
    ↓
Service B
    |
    | HTTP Response
    ↓
Service A
```

For example:

```
Order Service
     |
     | GET /products/101
     ↓
Product Service
     |
     | Product details
     ↓
Order Service
```

Order Service waits for Product Service's response.

---

## 3. Real-Time Example — Food Delivery

Imagine you open a food-delivery application and search for restaurants.

```
Mobile App
    |
    | GET /restaurants?location=Mumbai
    ↓
Restaurant Service
    |
    | Query
    ↓
Database
    |
    ↓
Restaurant Service
    |
    | Response
    ↓
Mobile App
```

The user expects the restaurant list now.

So synchronous communication is appropriate.

---

## 4. Another Example — Checking Account Balance

Suppose you open a banking application:

```
Mobile App
     |
     | "What is my balance?"
     ↓
Account Service
     |
     ↓
Database
     |
     ↓
Account Service
     |
     | ₹50,000
     ↓
Mobile App
```

The user is waiting for the answer.

This is a classic synchronous operation.

---

## 5. Synchronous Communication in Microservices

Suppose we have:

- Order Service
- Payment Service
- Inventory Service

A synchronous order workflow might be:

```
              Order Service
                    |
                    | 1. Reserve inventory
                    ↓
             Inventory Service
                    |
                    | Response
                    ↓
              Order Service
                    |
                    | 2. Process payment
                    ↓
              Payment Service
                    |
                    | Response
                    ↓
              Order Service
                    |
                    ↓
              Order Confirmed
```

The Order Service waits for each response.

---

## 6. Example: E-Commerce Order

Customer clicks:

```
"Place Order"
```

The request goes:

```
Client
   |
   ↓
Order Service
   |
   ├────→ Inventory Service
   |          |
   |          ↓
   |       RESERVED
   |          |
   |←─────────┘
   |
   ├────→ Payment Service
   |          |
   |          ↓
   |       SUCCESS
   |          |
   |←─────────┘
   |
   ↓
Order Confirmed
```

The user gets:

```
Order Confirmed
```

The Order Service needs the responses from Inventory and Payment before it can tell the user the final result.

---

## 7. Common Technologies

Synchronous communication is commonly implemented using:

### REST / HTTP

```
Service A
   |
   | HTTP
   ↓
Service B
```

For example:

```
GET /users/123
```

### gRPC

```
Service A
   |
   | gRPC
   ↓
Service B
```

gRPC is often used for high-performance service-to-service communication.

### GraphQL

A client can synchronously request data through a GraphQL API.

```
Client
   |
   | GraphQL Request
   ↓
GraphQL Server
   |
   ↓
Response
```

---

## 8. When Should We Use Synchronous Communication?

Use synchronous communication when the caller needs the response immediately to continue.

### Good Use Cases

#### 1. Login

```
Login
 ↓
Validate
 ↓
Return token
```

The client needs the result.

#### 2. Fetching Data

```
GET user
GET product
GET account balance
GET order details
```

The caller needs the data.

#### 3. Validation

For example:

```
Order Service
     |
     | Is this product available?
     ↓
Inventory Service
     |
     | YES
     ↓
Continue
```

The answer is required immediately.

#### 4. Payment Authorization

For some workflows:

```
Order
  ↓
Payment Authorization
  ↓
SUCCESS / FAILURE
  ↓
Continue
```

The caller may need an immediate payment decision before confirming the order.

---

## 9. When NOT to Use Synchronous Communication

This is equally important.

Don't use synchronous communication when the caller doesn't need an immediate response.

For example:

```
Order Created
```

After an order is created, you might need to:

- Send email
- Send SMS
- Update analytics
- Generate invoice
- Notify recommendation system

Does the customer need to wait for all of these?

Usually no.

Instead:

```
Order Service
     |
     | OrderCreated
     ↓
 Message Broker
     |
     ├────→ Notification Service
     ├────→ Invoice Service
     ├────→ Analytics Service
     └────→ Recommendation Service
```

Now the Order Service doesn't have to wait for everyone.

That's asynchronous communication.

---

## 10. Synchronous vs Asynchronous

This distinction is very important.

### Synchronous

```
Client
  |
  | Request
  ↓
Server
  |
  | Process
  |
  | Response
  ↓
Client
```

The client waits.

### Asynchronous

```
Client
  |
  | Message
  ↓
Queue / Kafka
  |
  ↓
Consumer
  |
  ↓
Process
```

The producer doesn't need to wait for the consumer's processing to finish.

---

## 11. Real-Life Analogy

### Synchronous

Imagine calling a restaurant:

```
You
 |
 | "Is my table ready?"
 ↓
Restaurant
 |
 | "Yes"
 ↓
You
```

You're waiting for the answer.

### Asynchronous

You leave your phone number:

```
You
 |
 | "Call me when my table is ready."
 ↓
Restaurant
```

You don't wait there for the restaurant to finish processing.

Later:

```
Restaurant
    |
    | Notification
    ↓
You
```

That's similar to asynchronous communication.

---

## 12. Major Problem With Synchronous Communication

Suppose:

```
Service A
   |
   ↓
Service B
   |
   ↓
Service C
   |
   ↓
Service D
```

All communication is synchronous:

```
A → B → C → D
```

If D is slow:

```
A
 ↓
B
 ↓
C
 ↓
D
 ↓
10 seconds
```

Then A, B, and C may all be waiting.

This can create a **cascading failure**.

---

## 13. Timeout Is Essential

You should never let synchronous calls wait forever.

For example:

```
Order Service
      |
      | Request
      ↓
Payment Service
      |
      | No response
      |
      ↓
    2 sec
      |
      ↓
   TIMEOUT
```

Then Order Service can take an appropriate action.

```
Payment Service
       |
       X
       |
    Timeout
       ↓
Order Service
       ↓
Handle failure
```

Typical synchronous design includes:

```
Synchronous Communication
        +
Timeout
        +
Retry (when safe)
        +
Circuit Breaker
```

---

## 14. Connection With Your Earlier Retry/Idempotency Question

This is where everything starts connecting.

Suppose:

```
Order Service
      |
      | HTTP
      ↓
Payment Service
```

Payment Service doesn't respond within 2 seconds.

```
Order Service
      |
      ↓
Timeout
```

Order Service may retry:

```
Retry
  ↓
Payment Service
```

But if this operation creates a side effect, such as charging money, we need:

```
Synchronous
    +
Timeout
    +
Retry
    +
Idempotency
```

Otherwise:

```
First request → Payment SUCCESS
Response lost
     ↓
Retry
     ↓
Second payment ❌
```

With idempotency:

```
First request
PAY-123
   ↓
Payment SUCCESS

Timeout
   ↓
Retry PAY-123
   ↓
Already processed
   ↓
Return previous result
```

---

## 15. Interview Perspective

If an interviewer asks:

> "When would you choose synchronous communication?"

A strong answer is:

> "I would use synchronous communication when the caller needs the response immediately to continue processing. Typical examples are fetching data, authentication, validation, and operations where the immediate success or failure is required. I would use REST or gRPC between services and configure timeouts. For transient failures, retries with exponential backoff and jitter can be used, but retries for side-effecting operations should be protected with idempotency. If the caller doesn't need an immediate response, I would prefer asynchronous communication using a message broker such as Kafka or a queue."

---

## 16. Quick Decision Rule

Ask yourself:

> "Does the caller need the result right now?"

### YES → Synchronous

- Login
- Get Balance
- Get Product
- Check Inventory
- Payment Authorization
- Validate Request

### NO → Consider Asynchronous

- Send Email
- Send SMS
- Generate Report
- Analytics
- Notifications
- Audit Events
- Image Processing
- Background Jobs

---

## The Mental Model

```
                 Do I need the result NOW?
                         |
                    ┌────┴────┐
                   YES       NO
                    |         |
                    ↓         ↓
              SYNCHRONOUS  ASYNCHRONOUS
                    |         |
                 REST/gRPC   Kafka/Queue
                    |         |
                 Wait       Don't wait
                    |         |
              Response     Event/Message
```

And one important system-design principle:

> Synchronous communication is simpler and gives an immediate response, but it creates temporal coupling—the caller depends on the downstream service being available and responsive. Asynchronous communication reduces that dependency but introduces eventual consistency and additional complexity such as queues, retries, duplicate messages, and ordering.
