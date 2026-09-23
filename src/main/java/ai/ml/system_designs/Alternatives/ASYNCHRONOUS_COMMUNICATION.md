# What is Synchronous and Asynchronous Communication?

---

## Asynchronous Communication (Non-Blocking Calls)

![img.png](img.png)

![img_1.png](img_1.png)

Suppose there is a person who want to eat a cake, but to bake a cake it might take 3 hours so person said ok i will come after 3 hours.
person will not wait he is free so he will do some other tasks. so here process is not synchronous. for 3 hours what person will do there.
Earlier we saw things were happening in a sequential manner untill you won't get a cash you can't eat burger. here things are not
happening in a sequential manner.

![img_2.png](img_2.png)

Earlier we saw we have a function in:
1. **1st step** — we fetch data from db
2. **2nd step** — we do some data manipulation
3. **3rd step** — we prepare response and send back.

All steps are happening in a sequential manner so all are depending on each other. till db data not fetch we won't do manipulation
without manipulation we can send response back.

Now suppose before sending a RESPONSE we want to send a notification to user. so 3rd step notification and 4th step sending response:
- **so, now 3rd statement we can make it asynchronous.**
- **1st, 2nd and 4th statement will be synchronous.**

Because 3rd statement Sending a Notification may take Time and there is no dependency on IT and we do not want
to happen Immediately. so, NOTIFICATION statement we can keep as an ASYNCHRONOUS.

---

## Example

![img_3.png](img_3.png)

### Amazon Cart (Synchronous)

The Application waits for this response before allowing the user to add the product into the cart.

we have added few Items in cart so this will perform synchronous because In REAL time we have to check either that item is available or not.

### Payment (Synchronous)

Application waits till the confirmation of payment has been successfully received from bank.

while doing payment, we need high consistency so synchronous.

### Notification (Asynchronous)

It can take some time for the notification to arrive, but in the mean time a user can do anything
on the application without waiting for the notification.

after placing order need to notify user. why simply i should block that user there is no use to wait because
there is no dependency on it. It's not like after Notification result come then we are going to use that notification result.

---

## Where Is It Necessary?

### 1. Computation Takes a lot of Time

when computations takes more time then we make the process as asynchronous as we saw bakery example.

suppose you have open a web pages like flipkart.com or google.com it open immediately but processing is going in background
because that is asynchronous page get loaded immediately but process still under processing which is asynchronous.

> If we not make this as an asynchronous then manner will be sequential until and unless our all backgroud task not done
> till then we will wait and our web page was getting loading an loading an loading...we will wait for a loag time for a
> web page to open, in this case user experience will go wrong.

### 2. Scalability of Application

If You want to make your system more scalable then also you have to use asynchronous communication.

for an example there is a function with 4 line...1st line fetch data from db, 2nd manupulation on code, 3rd sending a notification
and 4th sending a response back. so for all user all 4 line of code will run...but the 3rd line will take more time to respond
and since there is no dependencies on it so we can make it as an asynchronous call.

so, what will happen for me all 4 line will run where 3rd line will be running in a background...for the 2nd person also all 4 line
will get run where 3rd line will be running in background. so what is happening more and more people are able to sending a REQUEST
if it was synchronous then processing time will get increased.

### 3. Avoid Cascading Failure

suppose I am a client and sending a REQUEST to a person 100 request per second but that person are able to send only 10 response per
second so in this case that person will be overloaded with a requests so what we can do we will make this communication as a asynchronous.

---

## Deep Dive: Handling Overload with Asynchronous Communication

![img_4.png](img_4.png)

Asynchronous communication is one solution to prevent the downstream person/service from being overwhelmed, but asynchronous communication alone does not solve the overload problem.

Let's understand it from the beginning.

### The Scenario

Suppose:
- Client can send **100 requests/sec**
- Server/person can process only **10 requests/sec**

```
Client
|
| 100 requests/sec
↓
Server
|
| Can process only 10 requests/sec
↓
Response
```

The server receives requests faster than it can process them.

If we simply keep sending:

```
100 req/sec → Server → 10 req/sec processed
```

then 90 requests/sec are accumulating somewhere.

Eventually you can get:
- High memory usage
- Large request queue
- Increased latency
- Timeouts
- Rejected requests
- Server crash

---

### Solution 1: Asynchronous Communication

Yes, we can introduce a message queue.

For example:

```
Client
|
| 100 requests/sec
↓
Message Queue
|
| 10 messages/sec
↓
Consumer / Server
|
↓
Processing
```

Examples of message queues:
- Kafka
- RabbitMQ
- Amazon SQS
- Azure Service Bus

Now the client doesn't directly wait for the server to process the request.

Instead:

```
Client
|
| 100 req/sec
↓
Queue
|
| 10 req/sec
↓
Consumer
|
↓
Business Logic
```

The queue acts as a buffer.

#### But here's the important point

Suppose:
- Incoming = 100 requests/sec
- Processing = 10 requests/sec

After one second:
- Received = 100
- Processed = 10
- **Remaining = 90**

After 10 seconds:
- Received = 1000
- Processed = 100
- **Remaining = 900**

So the queue is growing.

**Therefore:**

Asynchronous communication doesn't increase the processing capacity. It separates the producer from the consumer and allows us to absorb traffic temporarily.

---

### Solution 2: Increase Consumers

Now we can add multiple consumers.

Instead of:

```
             10 req/sec
Queue ───────────→ Consumer
```

we can have:

```
                    ┌→ Consumer 1 → 10/sec
                    │
Client → Queue ─────┼→ Consumer 2 → 10/sec
                    │
                    ├→ Consumer 3 → 10/sec
                    │
                    └→ Consumer 4 → 10/sec
```

Now processing capacity becomes approximately:

**10 × 4 = 40 requests/sec**

If we add 10 consumers:

**10 × 10 = 100 requests/sec**

Now:
- Incoming rate = 100/sec
- Processing rate = 100/sec

The system can keep up.

**This is called horizontal scaling.**

---

### Solution 3: Backpressure

But what if we cannot keep adding consumers?

Suppose the maximum processing capacity is:
- **10 requests/sec**

but clients can send:
- **100 requests/sec**

Then we need **backpressure**.

The system tells the producer:

> "Slow down. I cannot process everything at this rate."

For example:

```
Client
|
| 100 req/sec
↓
Rate Limiter
|
| 10 req/sec
↓
Server
```

We can use:
- Rate limiting
- Throttling
- Queue limits
- HTTP 429 Too Many Requests
- Retry mechanisms
- Exponential backoff

---

## Very Important System Design Concept

When you see:
- Producer = 100 req/sec
- Consumer = 10 req/sec

your first question should be:

**What happens to the extra 90 requests?**

There are several possibilities:

### Option 1 — Reject Them

```
100 req/sec
↓
Server
↓
10 processed
90 rejected
```

Use when requests can be retried or dropped.

### Option 2 — Queue Them

```
100 req/sec
↓
Queue
↓
10/sec processing
```

Use when requests must not be lost and delayed processing is acceptable.

### Option 3 — Scale Consumers

```
                    ┌→ Consumer 1
                    ├→ Consumer 2
Queue ───────┼→ Consumer 3
                    ├→ Consumer 4
                    └→ Consumer 5
```

Increase processing capacity.

### Option 4 — Apply Backpressure

```
Client
↓
Rate Limiter
↓
Server
```

Tell clients to slow down.

---

## Real-World Example

Imagine a payment system.

Suppose:

```
Mobile Apps → Payment Service
```

During normal traffic: **100 payments/sec**

But the downstream fraud-checking system can process only: **20 payments/sec**

You could design:

```
                 ┌→ Fraud Consumer 1
                 ├→ Fraud Consumer 2
Payments → Kafka ├→ Fraud Consumer 3
                 └→ Fraud Consumer 4
```

The payment request can be accepted quickly and placed into Kafka.

Consumers process the messages asynchronously.

This gives you:

- **Decoupling** — Producer ≠ Consumer
- **Buffering** — Traffic spike → Queue → Consumers process gradually
- **Scalability** — More traffic → More consumers
- **Fault tolerance** — If the consumer goes down:

```
Producer → Queue → [Consumer DOWN]
                    ↓
              Messages remain
                    ↓
            Consumer comes back
                    ↓
           Processing resumes
```

---

## One Key Correction

You said:

> "the person will be overloaded with requests, so we will make this communication asynchronous."

A better System Design way to think about it is:

> When a producer can generate requests faster than a consumer can process them, synchronous communication can create backpressure on the producer and increase latency/timeouts. We can introduce asynchronous communication using a queue to decouple the producer and consumer, buffer temporary traffic spikes, and then use consumers, scaling, rate limiting, and backpressure to control the processing rate.

**This is a very important foundation for System Design, because it leads directly into Message Queues → Kafka → Consumer Groups → Backpressure → Rate Limiting → Load Balancing → Scalability.**