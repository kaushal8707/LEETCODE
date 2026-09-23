# Asynchronous Communication in System Design

Asynchronous communication means:

> The sender sends a message/request and does not wait for the receiver to finish processing it.

> The sender can continue doing its own work while the receiver processes the message later.

The simplest picture is:

```
Producer
   |
   | Message
   ↓
Queue / Message Broker
   |
   ↓
Consumer
   |
   | Process later
   ↓
Database / External System
```

The important thing is:

> Producer and Consumer don't have to be available and processing at exactly the same time.

---

## 1. Real-Time Example — Sending an Email After Order Creation

Suppose you're buying something from an e-commerce application.

You click:

```
Place Order
```

The system needs to:

- Create the order
- Send confirmation email
- Send SMS
- Update analytics
- Generate invoice

Do we want the customer to wait for all of these?

Probably not.

Instead:

```
                    Client
                      |
                      ↓
                 Order Service
                      |
                      | Create Order
                      ↓
                   Order DB
                      |
                      ↓
                Order Created
                      |
                      | Publish event
                      ↓
                Message Broker
                      |
          ┌───────────┼────────────┐
          ↓           ↓            ↓
     Email Service SMS Service  Invoice Service
          |           |            |
          ↓           ↓            ↓
        Email         SMS        Invoice
```

The Order Service can respond to the customer:

```
Order placed successfully
```

without waiting for the email/SMS/invoice processing to finish.

That's asynchronous communication.

---

## 2. Why Do We Need a Message Broker?

A message broker acts as a buffer between the producer and consumer.

Common examples include:

- Kafka
- RabbitMQ
- Amazon SQS
- Google Pub/Sub

Conceptually:

```
Producer
   |
   ↓
┌──────────────────────┐
│   Message Broker     │
│                      │
│ OrderCreated         │
│ OrderCreated         │
│ OrderCreated         │
└──────────┬───────────┘
           |
           ↓
        Consumer
```

The producer puts a message into the broker.

The consumer processes it when it is ready.

---

## 3. Real-Time Example — Your Earlier 100 Requests/sec Problem

You previously asked about this scenario:

> Client sends 100 requests/sec, but server can process only 10 requests/sec.

Suppose we have:

```
Client
   |
   | 100 requests/sec
   ↓
Server
   |
   | Can process 10/sec
```

If we directly communicate synchronously:

```
Client → Server
Client → Server
Client → Server
...
```

the server can become overloaded.

Instead, we can introduce a queue:

```
Client
   |
   | 100 requests/sec
   ↓
┌─────────────────┐
│     Queue       │
│                 │
│ 100 messages/s  │
└────────┬────────┘
         |
         | 10 messages/sec
         ↓
      Server
```

Now:

```
Producer = 100/sec
Consumer = 10/sec
```

The queue absorbs the temporary difference.

---

## 4. What Happens to the Remaining 90 Requests?

Suppose:

```
Incoming = 100/sec
Processing = 10/sec
```

After 1 second:

```
Queue ≈ 90 messages
```

After 10 seconds:

```
Queue ≈ 900 messages
```

So a queue doesn't magically solve an overload problem.

It buffers the workload.

If traffic stays at:

```
100/sec
```

while processing remains:

```
10/sec
```

the queue will eventually become full.

Therefore, you need to increase consumer capacity:

```
                 Queue
                   |
       ┌───────────┼───────────┐
       ↓           ↓           ↓
   Consumer 1  Consumer 2  Consumer 3
      10/s        10/s        10/s
```

Now:

```
Total processing = 30 requests/sec
```

You can add more consumers as required.

---

## 5. Very Important: Queue vs Backpressure

This connects directly to your previous question.

Suppose:

```
Producer = 100/sec
Consumer = 10/sec
```

A queue gives you:

```
100/sec
   ↓
Queue
   ↓
10/sec
```

But if the queue keeps growing:

```
100/sec incoming
10/sec processing

Queue:
90
180
270
360
...
```

eventually you'll have a problem.

So the system needs backpressure or some form of admission/load control.

Possible approaches include:

```
Producer
   |
   ↓
Rate Limiter
   |
   ↓
Queue
   |
   ↓
Consumers
```

or:

```
Queue Full
    ↓
Reject / Throttle / Slow Producer
```

So:

> Asynchronous communication can help absorb bursts, but it does not eliminate the need for capacity planning and backpressure.

---

## 6. Another Real-Time Example — Notification System

Imagine a banking application.

Customer transfers money:

```
Transfer ₹10,000
```

The core operation might be:

```
Transfer Service
      |
      ↓
Bank Database
      |
      ↓
Transfer Successful
```

Now we need to notify the customer:

- Email
- SMS
- Push Notification

Instead of:

```
Transfer
   ↓
Send Email
   ↓
Send SMS
   ↓
Send Push
   ↓
Return response
```

which makes the user wait,

we can do:

```
                  Transfer Service
                        |
                        ↓
                 Transfer Completed
                        |
                        ↓
                  Message Broker
                 /       |       \
                ↓        ↓        ↓
             Email      SMS      Push
            Service    Service   Service
```

The transfer can complete without waiting for all notifications.

---

## 7. Another Example — Video/Image Processing

Suppose a user uploads a video.

```
Client
   |
   | Upload video
   ↓
Upload Service
```

Video processing might take:

```
5 seconds
30 seconds
2 minutes
```

We don't necessarily want:

```
Client
   |
   ↓
Upload Service
   |
   ↓
Video Processing
   |
   | 2 minutes
   ↓
Response
```

Instead:

```
Client
   |
   ↓
Upload Service
   |
   ↓
Store Video
   |
   ↓
Queue
   |
   ↓
Video Processing Worker
```

The API can quickly return:

```json
{
  "videoId": "VID-123",
  "status": "PROCESSING"
}
```

The client can later check:

```
GET /videos/VID-123
```

or receive a notification when processing finishes.

---

## 8. Asynchronous Communication Using Kafka

A common architecture:

```
Order Service
      |
      | OrderCreated
      ↓
     Kafka
      |
      ├────────→ Notification Service
      |
      ├────────→ Analytics Service
      |
      ├────────→ Invoice Service
      |
      └────────→ Recommendation Service
```

The Order Service doesn't need to know exactly when each consumer finishes.

This creates **loose coupling**.

---

## 9. Synchronous vs Asynchronous

This is extremely important for System Design interviews.

### Synchronous

```
Service A
   |
   | Request
   ↓
Service B
   |
   | Response
   ↓
Service A
```

A waits for B.

### Asynchronous

```
Service A
   |
   | Message
   ↓
Message Broker
   |
   ↓
Service B
```

A doesn't wait for B's processing to complete.

---

## 10. Real-Time Comparison

Imagine an e-commerce order.

### Synchronous

```
Order Service
     |
     ↓
Payment Service
     |
     ↓
Inventory Service
     |
     ↓
Notification Service
     |
     ↓
Response to customer
```

The customer waits for the entire chain.

Potential problem:

```
Notification Service is slow
           ↓
Order response is slow
```

### Asynchronous

```
Order Service
     |
     ↓
Create Order
     |
     ↓
Message Broker
     |
     ├──→ Payment
     ├──→ Notification
     ├──→ Analytics
     └──→ Invoice
```

The customer can receive the initial response quickly.

---

## 11. When Should We Use Asynchronous Communication?

Use asynchronous communication when the caller doesn't need the result immediately.

### Good Use Cases

#### 1. Notifications

```
Order Created
     ↓
Send Email
Send SMS
Send Push
```

Definitely a good candidate.

#### 2. Analytics

```
User clicked button
     ↓
Event
     ↓
Analytics
```

The user doesn't need to wait for analytics processing.

#### 3. Video/Image Processing

```
Upload
   ↓
Queue
   ↓
Process
```

Good candidate.

#### 4. Report Generation

```
Generate 500-page report
       ↓
Queue
       ↓
Worker
```

Don't keep the HTTP request open for minutes.

#### 5. Background Jobs

Examples:

- Generate invoice
- Resize image
- Send email
- Process file
- Update search index
- Generate recommendations

#### 6. Event-Driven Microservices

For example:

```
OrderCreated
      ↓
Kafka
      |
      ├── Payment
      ├── Inventory
      ├── Notification
      └── Analytics
```

---

## 12. When Should We NOT Use Asynchronous Communication?

If the caller needs the answer immediately, synchronous is usually better.

For example:

### Login

```
Login
 ↓
Validate credentials
 ↓
Token
```

The client needs to know whether login succeeded.

### Get Account Balance

```
Client
  ↓
Get Balance
  ↓
₹50,000
```

The user wants the answer now.

### Validate OTP

```
Client
  ↓
OTP
  ↓
Auth Service
  ↓
VALID / INVALID
```

The caller needs the result immediately.

---

## 13. Advantages of Asynchronous Communication

### 1. Loose Coupling

```
Order Service
      |
      ↓
    Kafka
      |
      ↓
Notification Service
```

Order Service doesn't need to wait for Notification Service.

### 2. Better Resilience

If Notification Service is temporarily down:

```
Order Service
      ↓
    Queue
      ↓
Notification Service ❌
```

The message can remain in the broker until the consumer recovers, depending on the messaging system/configuration.

### 3. Handles Traffic Spikes

Suppose:

```
Normal traffic = 100/sec
Peak traffic   = 10,000/sec
```

A queue can absorb some of the burst:

```
10,000/sec
    ↓
┌─────────────┐
│    Queue    │
└──────┬──────┘
       ↓
Consumers process at sustainable rate
```

### 4. Independent Scaling

You can increase consumers:

```
Queue
 |
 ├── Worker 1
 ├── Worker 2
 ├── Worker 3
 ├── Worker 4
 └── Worker 5
```

---

## 14. Disadvantages

Asynchronous systems also introduce complexity.

### Eventual Consistency

You may temporarily have:

```
Order = CREATED
Notification = NOT_SENT
```

Later:

```
Order = CREATED
Notification = SENT
```

### Duplicate Messages

A message may sometimes be delivered more than once.

Therefore consumers should often be idempotent.

```
OrderCreated
OrderCreated
```

Consumer should not accidentally create two invoices.

### Ordering

Suppose:

```
OrderCreated
OrderCancelled
```

If events are processed in the wrong order:

```
OrderCancelled
     ↓
OrderCreated
```

you could get an incorrect state.

Messaging systems therefore require careful ordering design.

### Debugging

Instead of:

```
A → B → C
```

you may have:

```
A
 ↓
Kafka
 ↓
B
 ↓
Kafka
 ↓
C
```

Tracing the complete workflow becomes more complicated.

---

## 15. Asynchronous Communication + Retry + Idempotency

This connects directly with what we discussed earlier.

Suppose:

```
Order Service
      |
      ↓
Kafka
      |
      ↓
Payment Service
```

Payment Service receives:

```
PaymentRequested
```

It processes payment successfully:

```
₹1,000 charged
```

But crashes before acknowledging the message:

```
Payment Service
      |
      | Processed
      ↓
₹1,000 charged ✅
      |
      X
   Crash
```

Kafka may deliver the message again:

```
PaymentRequested
      ↓
Payment Service
```

Without idempotency:

```
₹1,000
+
₹1,000
=
₹2,000 ❌
```

With idempotency:

```
PaymentRequestId = PAY-123

First:
PAY-123 → ₹1,000 charged

Second:
PAY-123 → Already processed
```

Therefore:

```
Asynchronous
      +
Retry
      +
Idempotency
      =
Reliable processing
```

---

## 16. Asynchronous Communication Doesn't Mean "No Response"

This is a common misunderstanding.

Asynchronous doesn't necessarily mean the client gets no response.

For a long-running operation, you can return:

```
202 Accepted
```

For example:

```
POST /reports
```

Response:

```json
{
  "jobId": "JOB-123",
  "status": "PROCESSING"
}
```

Then:

```
GET /reports/JOB-123
```

returns:

```json
{
  "jobId": "JOB-123",
  "status": "COMPLETED",
  "downloadUrl": "..."
}
```

So the API request itself is quick, while the actual work happens asynchronously.

---

## 17. Synchronous vs Asynchronous — Final Comparison

| | Synchronous | Asynchronous |
|---|---|---|
| Caller waits? | ✅ Yes | ❌ Usually no |
| Immediate response? | ✅ | Not necessarily |
| Communication | REST/gRPC | Kafka/Queue/Event |
| Coupling | Higher | Lower |
| Latency | Usually predictable/immediate | Processing may happen later |
| Traffic spikes | Less tolerant | Better with buffering |
| Failure isolation | Lower | Better |
| Complexity | Lower | Higher |
| Consistency | Often immediate | Often eventual |
| Good for | Queries, validation, login | Events, background jobs |
| Retry handling | Important | Very important |
| Idempotency | Important for side effects | Very important |

---

## 18. The Best Mental Model

Ask one question:

> "Does the caller need the result right now?"

### YES → Synchronous

```
Client
  |
  ↓
Service
  |
  ↓
Response
```

Examples:

- Login
- Get balance
- Get product
- Validate OTP
- Check availability

### NO → Asynchronous

```
Producer
   |
   ↓
Queue / Kafka
   |
   ↓
Consumer
```

Examples:

- Send email
- Send SMS
- Analytics
- Generate report
- Video processing
- Notifications
- Background jobs

### And for Your 100 req/sec → 10 req/sec Example:

```
             100 req/sec
                  |
                  ↓
              Producer
                  |
                  ↓
          ┌───────────────┐
          │ Queue / Kafka │
          └───────┬───────┘
                  |
          ┌───────┼────────┐
          ↓       ↓        ↓
       Worker   Worker   Worker
        10/s     10/s     10/s

             = 30 req/sec
```

If incoming traffic remains higher than total consumer capacity, the queue keeps growing. So asynchronous communication should be combined with consumer scaling, rate limiting, backpressure, retry policies, dead-letter queues, and monitoring.

---

> **Synchronous** = "I need your answer before I continue."
>
> **Asynchronous** = "I've given you the work; you can process it independently, and I'll deal with the result later."
