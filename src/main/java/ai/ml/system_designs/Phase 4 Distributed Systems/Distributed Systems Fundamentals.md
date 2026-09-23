# Distributed Systems Fundamentals

Distributed Systems are systems where multiple independent computers (called nodes) work together over a network to achieve a common goal. To users, they often appear as a single system.

---

## Fundamental Concepts

### 1. Distributed System Definition

A distributed system consists of multiple computers that:

- Communicate through a network.
- Coordinate their actions by exchanging messages.
- Share resources and workloads.
- Continue operating even if some nodes fail.

**Examples:**

- Google Search
- Amazon Web Services (AWS)
- Netflix
- Apache Hadoop
- Kubernetes clusters

---

### 2. Characteristics

- **Concurrency:** Multiple nodes execute tasks simultaneously.
- **No Global Clock:** Each machine has its own clock.
- **Independent Failures:** One node can fail while others continue working.
- **Scalability:** Easily add more machines.
- **Transparency:** Users see one unified system.

---

### 3. Goals

- High availability
- Reliability
- Scalability
- Fault tolerance
- Resource sharing
- Performance improvement

---

### 4. Types of Distributed Systems

#### Client-Server

- Clients request services.
- Server provides services.

#### Peer-to-Peer (P2P)

- Every node acts as both client and server.

#### Cloud Systems

- Services distributed across data centers.

#### Cluster Computing

- Multiple computers work together like one machine.

#### Grid Computing

- Computers from different locations share resources.

---

### 5. Challenges

- Network latency
- Network failures
- Data consistency
- Synchronization
- Security
- Scalability
- Fault detection

---

### 6. CAP Theorem

A distributed system can guarantee only two of the following three properties during a network partition:

- **Consistency (C):** Every node sees the same data.
- **Availability (A):** Every request receives a response.
- **Partition Tolerance (P):** System continues despite network failures.

**Examples:**

- **CP:** HBase, ZooKeeper
- **AP:** Cassandra, DynamoDB
- **CA:** Possible only when there is no network partition.

---

### 7. Replication

Keeping multiple copies of data.

**Advantages:**

- High availability
- Faster reads
- Fault tolerance

**Disadvantages:**

- Synchronization overhead
- Consistency issues

**Types:**

- Synchronous replication
- Asynchronous replication

---

### 8. Consistency Models

- **Strong Consistency:** Latest write is always visible.
- **Eventual Consistency:** All replicas eventually become consistent.
- **Causal Consistency:** Related operations are seen in order.
- **Sequential Consistency:** Operations appear in a single sequence.

---

### 9. Fault Tolerance

The system continues operating despite failures.

**Techniques:**

- Replication
- Checkpointing
- Leader election
- Heartbeats
- Failover mechanisms

---

### 10. Communication Models

- Message passing
- Remote Procedure Call (RPC)
- REST APIs
- gRPC
- Publish–Subscribe (Kafka, RabbitMQ)

---

### 11. Time and Clock Synchronization

Since there is no global clock:

- Physical clocks (NTP)
- Logical clocks
- Lamport clocks
- Vector clocks

These help determine the order of events.

---

### 12. Distributed Algorithms

Common algorithms include:

- Leader election
- Mutual exclusion
- Consensus
- Distributed locking
- Failure detection

---

### 13. Consensus

Consensus means all nodes agree on a single value.

**Popular algorithms:**

- Paxos
- Raft
- Byzantine Fault Tolerance (BFT)

**Applications:**

- Distributed databases
- Blockchains
- Configuration management

---

### 14. Load Balancing

Distributes incoming requests among servers.

**Methods:**

- Round Robin
- Least Connections
- Hash-based
- Weighted Round Robin

**Benefits:**

- Improved performance
- Better resource utilization
- High availability

---

### 15. Distributed Storage

**Examples:**

- HDFS
- Google File System (GFS)
- Amazon S3

**Features:**

- Data replication
- Fault tolerance
- Large-scale storage

---

### 16. Distributed Transactions

Properties are often described by ACID:

- Atomicity
- Consistency
- Isolation
- Durability

**Common protocols:**

- Two-Phase Commit (2PC)
- Three-Phase Commit (3PC)

---

### 17. Microservices

An application is split into small, independent services.

**Benefits:**

- Independent deployment
- Better scalability
- Easier maintenance

**Challenges:**

- Network communication
- Service discovery
- Distributed tracing

---

## Example Architecture

```
                Load Balancer
                     |
       -----------------------------
       |            |             |
   Server A     Server B      Server C
       |            |             |
       -------- Database Cluster -------
              |               |
          Replica 1      Replica 2
```

---

## Common Interview Questions

1. What is a distributed system?
2. Explain the CAP theorem.
3. Difference between replication and sharding.
4. What is consensus?
5. Explain Raft vs Paxos.
6. What are Lamport clocks?
7. What is eventual consistency?
8. Explain 2PC and 3PC.
9. Difference between cluster and grid computing.
10. What is fault tolerance?

---

## Key Takeaways

| Concept | Purpose |
|---|---|
| Scalability | Handle increased workload by adding nodes |
| Replication | Improve availability and fault tolerance |
| Sharding | Partition data across multiple nodes |
| Consensus | Ensure nodes agree on shared state |
| CAP Theorem | Explains trade-offs between consistency, availability, and partition tolerance |
| Load Balancing | Evenly distribute requests |
| Fault Tolerance | Keep the system running despite failures |
| Logical Clocks | Order events without a global clock |

---

These fundamentals form the basis for understanding modern distributed technologies such as Kubernetes, Apache Kafka, Hadoop, Spark, Cassandra, MongoDB, and cloud platforms.
