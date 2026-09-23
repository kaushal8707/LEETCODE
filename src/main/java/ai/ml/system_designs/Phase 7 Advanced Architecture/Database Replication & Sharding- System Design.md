# Database Replication & Sharding — System Design

# What will happen when all of a sudden, instead of 1K data, 1 million requests start coming?

Let's say a client sends a request. The request is first intercepted by the **API Gateway**.

The API Gateway handles:

- Authentication
- Authorization
- Rate limiting
- Routing the request to the corresponding microservice based on the URL

But in between our microservices and the API Gateway, we have something called a **Load Balancer**. So the request goes to the load balancer, which distributes the traffic across multiple instances of our microservices.

Now you might have a question — **why do we have multiple instances?**
Because if traffic increases, instead of putting all the load on a single instance, we can scale our service **horizontally** and distribute traffic across multiple instances. That is the reason we horizontally scale our microservices.

Once a request reaches the microservice, it typically needs some data. So what does it do? It first checks in the **cache** — is the data present in the cache? If data is present in the cache, it returns quickly. Otherwise, the application goes to the **DB**, fetches the data, and stores the updated value in the cache for future requests.

So far, everything looks good, right? But hold on — look carefully at one thing.

We scaled our application layer to handle more traffic. **But what about our database?** Imagine our application is now handling millions of requests. That means millions of requests could eventually reach our database as well. So the question is — **how do we scale the database when the traffic keeps increasing?** And that's not the only problem.

What if our database currently has 1 TB of data but over time it grows to 10 TB, 50 TB, or even 100 TB? Can a single database server keep handling all the traffic and data?

That's exactly where **database replication** and **database sharding** come into the picture.

So here we'll understand:

- How **replication** helps us scale database traffic and improve availability.
- How **sharding** helps us distribute a huge data set across multiple databases.
- How real-world systems can **combine both** of them.

---

## Starting point: One application, one database

Suppose we have a very simple application — one application and one database. Initially every request goes to this single database.

Let's say initially we are getting **1,000 requests per second**. Our database may handle it easily. But eventually traffic goes from 1,000 to **1 lakh (100,000) requests per second**. Now the database becomes a bottleneck, isn't it?

So what can we do?

One easy option is to create **multiple copies of our database** — something like Database 1, Database 2, and Database 3. Now our application will point to all three databases (A, B, and C). All the databases A, B, and C will contain the same data. Whenever there is a data change, those changes will be replicated to the other databases as well.

This concept is called **database replication**.

But if we have multiple copies of the database, then who should handle the **write** operation, and which database should handle the **read** operation? To solve this problem, we have different replication architectures. Let's understand them one by one.

---

# Replication Technique — Leader-Follower / Leader-Leader Replication

## Leader-Follower Replication

Let's start with one of the most common replication architectures — **Leader-Follower replication**.

This is also called **Primary-Replica** or **Leader-Replica**. The idea is very simple: we'll have **one leader** and **one or more followers**.

```
                Application
                    |
       +------------+------------+
       ↓                         ↓
   Write Replica            Read Replica
   (Leader)               (Followers / Replicas)
```

The basic rule is:

- **Writes** always go to the **Leader**.
- **Reads** go to the **Read Replicas** (Followers).

For example, suppose a user creates a new account in your application. What kind of operation is that? A **write** operation. So the request goes to the leader first, the leader writes to the DB, and then those changes are replicated to the follower databases.

Now suppose another user wants to read that account information — that will be a **read** operation. Instead of sending the request to the leader, the request is fetched from the read replica. Because we have dedicated replicas for read operations, reads always fetch from replicas and writes always go to the leader database.

### Real-time scenario

Let's say our application receives **1 lakh (100,000) requests**. Out of these:

- **10,000** are write operations
- **90,000** are read operations

Since we have Leader-Follower replication:

- All 10K writes go to the leader.
- All 90K reads are distributed across Replica 1 and Replica 2 — maybe 45K each, or some other split.

So instead of a single database handling all 1 lakh requests, we can distribute the read workload across multiple replicas. That is one of the biggest benefits of replication — it helps us scale our **read traffic horizontally**. If you have 3 or 4 replicas, the traffic per replica reduces further and is distributed accordingly.

Now you might be thinking — when the leader writes the data, when do those writes reflect in the replicas? That's really an interesting concept.

---

# How can Leader and Follower data sync up?
## Synchronous (More Consistency) / Asynchronous (High Availability) Technique

There are two common approaches — **synchronous replication** and **asynchronous replication**.

### Synchronous replication

When a request comes to the leader:

1. Leader writes it.
2. Leader **waits** for the replicas to acknowledge.
3. Replica 1 writes and acknowledges: "I am done with the changes."
4. Leader then waits for Replica 2 to acknowledge: "Yes, I did the changes."
5. Once everything is completed, the operation is marked as **succeeded**.

This is perfect for **strong consistency**.

**But there is a trade-off.** The leader now has to wait for the replicas to update the value. So this can introduce **higher latency**, and if a replica (say Replica 1) is slow or unavailable, the write can also become slow or fail.

Synchronous is always recommended if you want to achieve **strong consistency**, but keep in mind it can increase latency.

### Asynchronous replication

In asynchronous replication, **the leader does not wait** for the replicas. Once the request comes:

1. Leader writes it first.
2. Leader immediately responds to the application: "Hey, the replication happens in the background. You don't need to worry for it. I wrote the updated value but replicas will be updated behind the scenes."
3. Replication happens **in the background**.

This provides **lower latency** and **better availability**, but there can be a small delay between the leader and replicas.

For example, the leader writes the value. Then asynchronously it publishes the update to the replicas. But what if Replica 1 is slow, or there's a network gap? Then you'll find **data inconsistency** between the leader and replica. This concept is called **replication lag** — remember this term.

For example, the leader may have value **100**, while the replica balance is still **80**. So for a short period the replica may contain older data — that's fine.

### Key trade-off

| Approach | Consistency | Latency | Replication Lag |
|---|---|---|---|
| Synchronous | Stronger consistency | More latency | No |
| Asynchronous | Eventual consistency | Lower latency | Possible |

So it depends on your requirement — you can choose one of them.

---

## But what if the Leader itself fails?

Now let me ask you one important question — **what happens if our leader itself fails?**

We understand the Leader-Follower architecture where the leader database takes care of writes and the read replica / followers take care of reads. We also understand how the data is updated in the replica using synchronous and asynchronous approaches.

But the major problem is — **what if the leader itself fails?** How will the data be updated in the replicas? Think about it.

This is one of the most important problems with Leader-Follower replication.

Let's assume the leader crashes. Now what happens to the new writes? They cannot go to the failed leader, because the leader is already crashed.

So we need a mechanism to **promote one of the replicas as a new leader**. For example, we have a leader which got crashed — then we need to find a way to promote one replica as the new leader. Let's say Replica 1 will act as the new leader. Now this guy will be the new leader going forward.

So the important idea is:

> If the main leader fails, another replica can become the new leader. This process is called **failover**.

Again, remember this term — **assigning a new leader is called failover**. In real-world systems, failover can be handled by database-native mechanisms, or external coordination and service discovery systems. So now we have a system that can continue operating even though the original leader has crashed or failed.

But we still have one limitation with Leader-Follower. If you see this Leader-Follower architecture / replication pattern, we have a **single leader**. But what if we want **multiple databases to accept write operations**? That brings us to another replication architecture — **Leader-Leader replication**.

---

# Leader-Leader Technique

The second replication architecture is called **Leader-Leader replication**. In Leader-Leader replication architecture, we can have **multiple leaders**.

Both databases can accept the write operation. For example:

- User A's request can go to **Leader 1**.
- User B's request can go to **Leader 2**.

In parallel, requests can go to any of the leaders. Let's say User A wrote something to DB1 — then immediately, the changes will reflect in Database B (Leader 2). This is called **Leader-Leader replication architecture**.

### Advantage

We don't have a single write leader. If one leader goes down, another leader is already available who can continue accepting writes.

### Major challenge — Write Conflict

If the same piece of data is updated **concurrently** through different leaders, we can have a **write conflict**.

**Example:**

- User account balance = **1,000**
- Two requests arrive at around the same time:
  - Request 1 → **Leader 1** → withdraw **100 rupees**
  - Request 2 → **Leader 2** → withdraw **200 rupees**

Now the original balance is 1,000. Each leader may temporarily calculate from the same old value (1,000):

- Leader 1: 1000 − 100 = **900**
- Leader 2: 1000 − 200 = **800**

Because they both evaluate from the old value which is 1,000. But what will be the **real** value? Withdrawing 100 and 200, it should be:

```
1000 - 100 - 200 = 700
```

But Leader 1 updates it as **900** and Leader 2 updates it as **800**, since there is a concurrent request. So in such scenarios you will find a **write conflict**.

Again, these are edge cases, but you need to remember them. So Leader-Leader replication requires a **conflict resolution strategy**. Because of this complexity, Leader-Leader is generally harder to operate than Leader-Follower. And **most of the industry follows the Leader-Follower** replication architecture compared to Leader-Leader replication architecture.

So with replication we understand how we can achieve **high availability** and **read scaling**.

---

# Database Sharding

But now let's move to our second major concept — **database sharding**.

So what happened? Replication creates a copy of the same data. That is what you understand, right? Each database contains the same data. Both will be updated. But what if our actual **data set** becomes too large for a single database? For example, today it is 10 TB, tomorrow it increases to 50 TB, or even 100 TB in future.

Even if we create replicas, each replica still needs to store the entire data set. So **replication does not solve the problem of data set size**. It copies the same data across multiple DBs. So let's say this DB will need 100 TB, this DB also needs 100 TB, and they both will contain the same data. So if data set size grows, replication will not help you.

This is where **database sharding** comes into the picture — splitting a large data set into smaller databases called **Shards**. So if you have a single database, you need to split it into multiple databases. Basically **database horizontal scaling** — that concept is called database sharding. Each small database is called a **Shard**.

For example, instead of keeping everything in one database, we can split the data something like this:

```
                Application
                    |
       +------------+------------+
       ↓            ↓            ↓
    Shard 1      Shard 2      Shard 3
```

Now here in replication all three databases contain the same data. But in database sharding, we horizontally scale our database into multiple database instances. Each will contain different data. For example:

- **Shard 1** contains user IDs 1 to 1 million
- **Shard 2** contains user IDs 1 million to 2 million
- **Shard 3** contains user IDs 2 million to 3 million

Now with this approach, the total data set is distributed across multiple database servers (1, 2, 3). And this gives us something replication could not give us — **horizontal scaling of the database / data set**.

## Shard Key

But now the question here is — if we split our data across multiple databases, **how do we know which shard contains a particular record?** How can I know that 1 million to 2 million records are present in Shard 2?

Well, that's where we need something called a **Shard Key** — a very important concept in database sharding.

> A **Shard Key** is the value we use to determine which shard should store a particular record.

For example, let's say **User ID** is my shard key. I'll take user ID as a shard key. Then I will use the user ID to decide where the data belongs.

---

# Sharding — Data Storing and Fetching Techniques
## Hash-Based / Range-Based

### Hash-Based Sharding

For example, one simple approach — you can consider `User ID % Total Shard Count`. This is a kind of hashing we do to evaluate where this particular user will be stored (Shard 1, 2, or 3).

**Example:**

- User ID = 1.1, Total shard count = 3 → output = **2** → This user goes to **Shard 2**
- User ID = 2.5 → `2.5 % 3 = 1` → This user goes to **Shard 2** (per the example)

Based on the shard key, I decide in which shard a particular record is present. You can use different algorithms — this is the simplest algorithm, just used for demo. This approach is called **hash-based sharding**.

### Range-Based Sharding

There is also an alternative approach called **range-based sharding**. Now let's understand what range-based sharding is.

In range-based sharding, instead of calculating a hash, we divide the data into **ranges** — something like what we already understood:

- **Shard 1** → User IDs 0 to 1 million
- **Shard 2** → User IDs 1 million to 2 million
- **Shard 3** → User IDs 2 million to 3 million

Now suppose we want to find a user between ID **1 million and 1.5 million** — we immediately know that range belongs to **Shard 2**, correct? So range-based sharding can make **range queries easier**.

### Potential problem — Hotspots

So range-based sharding can make range queries easier. But there is a potential problem called **hotspots**.

Suppose new users are continuously arriving with incrementing IDs:

- Shard 1: 1 to 1 million
- Shard 2: 1 million to 2 million
- Shard 3: 2 million to 3 million

This is a range-based sharding we are using. Now what happens? Currently we have records up to 3 million. Now let's say we get another user whose ID will be 3 million + 1. Next user 3 million + 2, and so on. **All those new users / all those new writes will always go to Shard 3.**

So this concept is called a **hotspot** in database sharding. So choosing a shard strategy depends heavily on the application's access pattern.

Now let's understand a very important challenge in sharding.

---

# Sharding — Scatter-Gather Query Technique

So what do we understand? Sharding gives us scalability, but it also introduces another complexity.

For example, suppose we have stored all the users starting with **A to M in Shard 1**. We also store users starting with **N to Z in Shard 2** based on the alphabet.

But here I want to execute a different query:

```sql
Find all the users whose age > 30
```

If it is alphabet-wise, we know A to M is in Shard 1 and N to Z is in Shard 2. But we are checking with the **age** — age is **not** our shard key.

So what will the application do? The application needs to query **multiple shards**:

- Shard 1 → it will check
- Shard 2 → it will check
- Shard 3 → it will check

Once it checks all the shards, it finds the merged result for you. This is called a **scatter-gather query**.

So while sharding improves scalability, **cross-shard queries and transactions become more complicated**. For this particular request, my query needs to trigger on all three shards. Earlier we queried a single database — now, since the shard key is different from what the user is searching, we have to query three different databases. So that is the reason it provides scalability, but cross-shard queries and transactions become more complicated in database sharding.

---

## Now notice something interesting

We understand replication and sharding. Each one of them has pros and cons, right?

- On one hand, **replication** gave us **multiple copies** of the database.
- On the other hand, **sharding** gave us **multiple partitions** of the database.

**So why not combine them?** And that's exactly what large-scale systems often do. So if you combine **replication + sharding**, this combination will give you more power in database architecture.

Now let's understand how we can combine replication and sharding. In simple words:

- **Replication** = copy of the data
- **Sharding** = split data set into multiple data sets / databases

So in real-world systems we don't necessarily choose between replication and sharding — we can use **both**. For example, something like this:

---

# Combining Both Replication and Sharding Together

If you check this architecture, we have **replication as well as sharding** combined in this flow.

We have an **application**. We have a **sharding router** who will decide to forward the request to which shard. Then each shard has a **leader** and **followers**, and each followers section has multiple replicas — this is the Leader-Follower replication architecture we have added in this flow.

```
                    Application
                        |
                        ↓
                 Sharding Router
                        |
        +---------------+---------------+
        ↓               ↓               ↓
    Shard 1         Shard 2         Shard 3
   ┌────────┐     ┌────────┐     ┌────────┐
   │ Leader │     │ Leader │     │ Leader │
   └───┬────┘     └───┬────┘     └───┬────┘
       ↓              ↓              ↓
   ┌───────┐      ┌───────┐      ┌───────┐
   │  R1   │      │  R1   │      │  R1   │
   │  R2   │      │  R2   │      │  R2   │
   └───────┘      └───────┘      └───────┘
```

So what happens? The request first reaches the **Shard Router**. Now whether you are using hash-based sharding or range-based sharding, based on that, the request goes to the shard.

- If it is a **write** operation, it goes to the **Leader**, which writes the data and updates the followers R1 and R2.
- If it is a **read** operation, it directly goes to the followers, reading from either Replica 1 or Replica 2.

At the end, all the data is the same across the leader and followers / read replicas.

So in this flow we get both **sharding** and **replication**:

- **Sharding** → horizontal data + write scaling
- **Replication** → availability + read scaling

Technically, this combination is extremely powerful. Now our database architecture has become much more scalable, isn't it?

---

## But we have just introduced another interesting problem

Think about a simple Amazon order. A user clicks **Place the Order**. Behind that single click, we might need to:

- Create the order
- Charge the payment
- Reserve the inventory
- Start shipping
- Send a notification to the user

And these operations may belong to completely different microservices like:

- Order Service
- Payment Service
- Inventory Service
- Shipping Service
- Notification Service

as individual microservices. And even each microservice has its own database. So the databases here are also different.

Now imagine the **payment succeeded but the inventory reservation failed**. So should we roll back the payment? Or should we roll back the reservation? **How do we maintain consistency when one business transaction spans multiple services and databases?**

That's completely a different challenge — and that's exactly what we'll explore in the next video: **Distributed Transactions — Two-Phase Commit vs Saga Pattern.**

