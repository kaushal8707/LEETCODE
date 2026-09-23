# Consensus in Distributed Systems

**Consensus** is the process by which multiple distributed nodes agree on one consistent value or decision, even when some nodes fail or messages are delayed.

A simple definition:

> Consensus = multiple machines agreeing on the same decision despite failures.

This is one of the most important concepts in distributed systems.

---

## 1. Why Do We Need Consensus?

Imagine you have 5 servers:

```
        Distributed System

   Node-1    Node-2    Node-3
      \        |        /
       \       |       /
        +-------------+
        |   Shared    |
        |   Decision  |
        +-------------+
       /       |       \
   Node-4    Node-5
```

Suppose the system needs to decide:

> Who should be the leader?

All nodes need to agree:

```
Node-1 → Node-3 is leader
Node-2 → Node-3 is leader
Node-3 → Node-3 is leader
Node-4 → Node-3 is leader
Node-5 → Node-3 is leader
```

Now the cluster has a consistent view.

But distributed systems have failures:

```
Node-3 💥
Network delay
Node-4 unreachable
Node-5 restarted
```

Consensus algorithms allow the surviving nodes to continue making consistent decisions.

---

## 2. Real-World Example: Bank Account

Suppose you have replicated data:

```
Account balance = ₹10,000
```

Across three nodes:

```
Node-A → ₹10,000
Node-B → ₹10,000
Node-C → ₹10,000
```

A withdrawal request arrives:

```
Withdraw ₹2,000
```

The system needs to agree:

> Should this withdrawal be accepted?

If Node-A says:

```
₹8,000
```

while Node-B says:

```
₹10,000
```

and Node-C says:

```
₹8,000
```

the system needs a consistent decision about the operation and its ordering.

Consensus protocols help replicated state machines agree on the sequence of committed operations.

---

## 3. Consensus Is NOT Just "Majority Vote"

This is an important distinction.

You might think:

```
Node-1 → A
Node-2 → A
Node-3 → B
Node-4 → A
Node-5 → B

A wins because 3 voted for A
```

That's an oversimplification.

A real consensus protocol needs to handle:

- Node failures
- Network partitions
- Message delays
- Duplicate messages
- Retries
- Out-of-order messages
- Nodes recovering
- Concurrent proposals
- Leader failures
- Ensuring committed decisions aren't contradicted

That's why algorithms such as **Raft** and **Paxos** are sophisticated.

---

## 4. Three Important Consensus Properties

Consensus is commonly described using properties such as:

### Agreement

Correct nodes should not decide conflicting values.

```
Node-1 → X
Node-2 → X
Node-3 → X
```

not:

```
Node-1 → X
Node-2 → Y
```

for the same consensus decision.

### Validity

The decided value should come from an acceptable proposal/value according to the protocol.

In simplified terms:

```
Client proposes X
        ↓
Consensus
        ↓
X
```

The exact validity definition depends on the consensus algorithm.

### Termination

If the system remains sufficiently healthy and communication eventually works, participating correct nodes should eventually reach a decision.

```
Proposal
   ↓
Communication
   ↓
Agreement
   ↓
Decision
```

---

## 5. Consensus vs Leader Election

You just learned Leader Election, so this distinction is very important.

### Leader Election

Answers:

> Who is the leader?

Example:

```
Node-1 → Follower
Node-2 → Leader ⭐
Node-3 → Follower
```

### Consensus

Answers:

> What value/state should the cluster agree has been committed?

For example:

```
"Apply transaction #101"
```

Consensus may use leader election as part of its implementation.

For example, Raft contains:

```
Raft
 |
 +── Leader Election
 |
 +── Log Replication
 |
 +── Commitment
 |
 +── Safety
```

So:

> Leader election is a component of Raft consensus, not the same thing as consensus.

---

## 6. How Consensus Works — High Level

Let's use **Raft** because it is easier to understand than Paxos.

Suppose we have 5 nodes:

```
Node-1
Node-2
Node-3
Node-4
Node-5
```

Initially:

```
Node-1 → Follower
Node-2 → Follower
Node-3 → Follower
Node-4 → Follower
Node-5 → Follower
```

An election happens.

Suppose Node-3 becomes leader:

```
Node-1 → Follower
Node-2 → Follower
Node-3 → Leader ⭐
Node-4 → Follower
Node-5 → Follower
```

Now clients send requests to the leader.

---

## 7. Client Request

Suppose client sends:

```
SET balance = 8000
```

to Node-3.

```
Client
   |
   ↓
Node-3 ⭐ Leader
```

The leader doesn't immediately declare the operation committed.

It first records the operation in its replicated log.

Node-3 log:

```
1. SET balance = 8000
```

Then it replicates the entry to followers.

```
             Leader
             Node-3
                |
        +-------+-------+
        |       |       |
        ↓       ↓       ↓
      Node-1  Node-2  Node-4
```

---

## 8. Log Replication

The leader sends the log entry:

```
SET balance = 8000
```

to followers.

Now:

```
Node-1 → [SET balance = 8000]
Node-2 → [SET balance = 8000]
Node-3 → [SET balance = 8000]
Node-4 → [SET balance = 8000]
Node-5 → []
```

Suppose 4 nodes have successfully stored it.

That's a majority:

```
5 nodes
majority = 3
```

Since the leader itself has the entry plus enough followers have replicated it, the entry can become committed according to Raft's rules.

---

## 9. Why Majority?

For 5 nodes:

```
Majority = 3
```

For 3 nodes:

```
Majority = 2
```

For 7 nodes:

```
Majority = 4
```

Formula:

```
majority = floor(N / 2) + 1
```

Examples:

| Nodes | Majority |
|---|---|
| 3 | 2 |
| 5 | 3 |
| 7 | 4 |
| 9 | 5 |

The important property is:

> Any two majorities overlap in at least one node.

For example, with 5 nodes:

```
Majority A = Node-1, Node-2, Node-3

Majority B = Node-3, Node-4, Node-5
```

They overlap at:

```
Node-3
```

This intersection property is fundamental to quorum-based consensus.

---

## 10. Why Not Require All Nodes?

Suppose you have:

```
5 nodes
```

and require all 5 to acknowledge every operation.

If one node fails:

```
Node-1 → OK
Node-2 → OK
Node-3 → OK
Node-4 → OK
Node-5 → 💥
```

the entire system can't make progress.

With a majority:

```
Node-1 → OK
Node-2 → OK
Node-3 → OK
Node-4 → OK
Node-5 → 💥
```

You still have:

```
4 healthy nodes
```

and can continue.

This gives consensus systems a balance between:

> Safety + availability under supported failure assumptions.

---

## 11. What If the Leader Dies?

This is where leader election and consensus connect.

Suppose:

```
Node-3 → Leader ⭐
```

and it crashes:

```
Node-3 💥
```

The remaining nodes detect that they aren't receiving the leader's heartbeats.

An election begins:

```
Node-1 ──┐
Node-2 ──┼──> Election
Node-4 ──┤
Node-5 ──┘
```

Suppose Node-4 wins:

```
Node-1 → Follower
Node-2 → Follower
Node-4 → Leader ⭐
Node-5 → Follower
```

The new leader continues replication.

---

## 12. What If the Network Is Partitioned?

This is one of the most important distributed-systems scenarios.

Suppose:

```
        Network Partition

 Node-1   Node-2   |   Node-3   Node-4   Node-5
                   X
                   X
```

Left side:

```
2 nodes
```

Right side:

```
3 nodes
```

The right side has a majority.

Therefore it can continue making progress.

```
Node-3
Node-4
Node-5

     ↓
 Majority = 3

Can continue consensus
```

The minority side cannot safely commit new decisions because it cannot form a majority.

This prevents the minority partition from independently committing conflicting state.

---

## 13. Why Consensus Helps Prevent Split-Brain

Without consensus:

```
Partition

A B | C D E

A → "I am leader"
C → "I am leader"
```

Potentially:

```
Leader A
   +
Leader C
```

Two sides might make conflicting decisions.

With a majority-based consensus protocol:

```
A B → minority
C D E → majority
```

Only the majority can normally commit new entries.

This greatly reduces split-brain problems.

---

## 14. Terms in Raft

To understand Raft, learn these concepts:

### Term

A logical election period.

```
Term 1
Term 2
Term 3
Term 4
```

If a new leader is elected:

```
Term 4
```

The term number helps nodes recognize stale leaders/messages.

### Log

Each node maintains a sequence of operations:

Node-1:

```
Index   Command
-------------------------
1       SET A=10
2       SET B=20
3       SET C=30
4       SET D=40
```

The leader replicates these entries to followers.

### Commit Index

The commit index indicates the highest log entry known to be committed.

Log:

```
1 → committed
2 → committed
3 → committed
4 → not committed
```

### State Machine

After an entry is committed, nodes apply it to their state machine.

```
Replicated Log
      ↓
Committed Entry
      ↓
State Machine
      ↓
Application State
```

This is called a **replicated state machine**.

---

## 15. The Complete Raft Flow

Here's the most useful mental model:

```
                  Client
                    |
                    ↓
              Leader ⭐
                    |
              Append Entry
                    |
        +-----------+-----------+
        |           |           |
        ↓           ↓           ↓
     Follower    Follower    Follower
        |           |           |
        +-----------+-----------+
                    |
                Majority
                    |
                    ↓
                 COMMIT
                    |
                    ↓
              Apply to State
                    |
                    ↓
               Client ACK
```

---

## 16. Example With 5 Nodes

Suppose:

```
N1
N2
N3 ⭐ Leader
N4
N5
```

Client:

```
SET user:101 = Kaushal
```

### Step 1

```
Client → N3

N3 log:
SET user:101 = Kaushal
```

### Step 2

N3 replicates:

```
N3 → N1
N3 → N2
N3 → N4
N3 → N5
```

### Step 3

Suppose N1 and N2 acknowledge:

```
N1 ✓
N2 ✓
N3 ✓
N4 ✗
N5 ✗
```

That's 3 nodes:

```
N1 + N2 + N3 = 3
```

Majority achieved.

### Step 4

Leader commits the entry.

```
COMMIT
```

### Step 5

Nodes apply it to their state machines.

```
user:101 = Kaushal
```

Now the cluster agrees on that committed operation.

---

## 17. What Happens If a Follower Is Down?

Suppose:

```
N1 → healthy
N2 → healthy
N3 → Leader
N4 → 💥
N5 → healthy
```

You still have:

```
4/5 nodes
```

The system can continue.

If another node fails:

```
N1 → healthy
N2 → 💥
N3 → Leader
N4 → 💥
N5 → healthy
```

You have:

```
N1 + N3 + N5 = 3
```

Still a majority.

But if:

```
N1 → 💥
N2 → 💥
N3 → Leader
N4 → 💥
N5 → healthy
```

Only 2 nodes remain.

```
2 < 3
```

No majority.

The cluster cannot safely commit new entries.

This is intentional: safety is prioritized over making potentially conflicting decisions.

---

## 18. Consensus and CAP Theorem

Consensus is closely related to CAP.

During a network partition:

```
Network Partition
       |
       ↓
Some nodes cannot communicate
       |
       ↓
Need to avoid conflicting decisions
```

Consensus protocols generally sacrifice availability on the minority side so that the system preserves consistency/safety.

But be careful with the simplistic statement:

> "Raft is CP."

The more precise explanation is:

> A consensus-based replicated system can continue committing operations only when it can communicate with a quorum/majority. During a partition that prevents quorum, it sacrifices progress on that side to preserve safety.

---

## 19. Paxos vs Raft

You will frequently hear these two names.

| Feature | Paxos | Raft |
|---|---|---|
| Purpose | Consensus | Consensus |
| Difficulty | Harder to understand | Designed to be easier to understand |
| Leader | Can use a leader | Strong leader model |
| Log replication | Yes, in Multi-Paxos variants | Core part of Raft |
| Common educational choice | Advanced | Very common |
| Used in real systems | Yes | Yes |

For system-design interviews, Raft is usually easier to explain clearly.

---

## 20. Where Is Consensus Used?

Consensus is used in systems that need multiple nodes to agree on replicated state or coordination decisions.

Examples include:

- etcd
- ZooKeeper
- Consul
- distributed databases
- replicated metadata systems
- cluster coordination systems
- some distributed storage systems

For example, etcd uses Raft for its replicated state.

---

## 21. Consensus vs Distributed Lock vs Leader Election

This is a very important interview comparison.

| Concept | Main Question |
|---|---|
| Distributed Lock | "Who owns this resource?" |
| Leader Election | "Who is the leader?" |
| Consensus | "What decision/state should the cluster agree on?" |

Relationship:

```
              Consensus
                  |
        +---------+---------+
        |                   |
 Leader Election       Log Replication
        |                   |
        +---------+---------+
                  |
          Consistent State
```

A system such as Raft uses consensus to maintain a consistent replicated log, and leader election is one component of that protocol.

---

## 22. The Most Important Concept: Quorum

If you remember only one technical concept from this topic, remember:

> A quorum/majority allows a distributed system to make progress while ensuring two conflicting majorities cannot both exist without intersecting.

For 5 nodes:

```
N = 5

Majority = 3
```

```
       N1
      /  \
    N2    N3
          |
    +-----+-----+
    |     |     |
   N4    N5   Leader
```

Any committed decision must involve enough nodes to establish quorum according to the protocol.

---

## 23. The Biggest Interview Trap

Don't say:

> "Consensus means all servers agree."

That's incomplete.

A better answer is:

> Consensus allows distributed nodes to agree on a value or sequence of operations despite failures, while preserving safety and making progress when the required communication/quorum conditions are available.

And don't say:

> "Consensus means every node must be alive."

Instead:

```
5 nodes
↓
3 nodes = majority
↓
Can continue
```

The system is designed to tolerate failures up to its quorum assumptions.

---

## 24. 30-Second Interview Answer

> Consensus is a mechanism that allows multiple distributed nodes to agree on a consistent value or sequence of operations despite node failures and network delays. A common implementation is **Raft**. In Raft, nodes elect a leader, clients send requests to the leader, and the leader replicates operations to followers. Once the operation is safely replicated to a majority, it becomes committed and is applied to the state machine. If the leader fails, a new election occurs. If a network partition occurs, only the partition with a majority can normally continue committing new operations, preventing conflicting decisions.

### Remember the complete picture:

```
              DISTRIBUTED SYSTEM
                      |
                      ↓
                 Consensus
                      |
          +-----------+-----------+
          |                       |
    Leader Election         Replicated Log
          |                       |
          ↓                       ↓
      Leader ⭐              Majority ACK
                                  |
                                  ↓
                               Commit
                                  |
                                  ↓
                         State Machine
                                  |
                                  ↓
                         Consistent State
```

---

## Next Steps

> **Next important topic after Consensus:** Raft — Leader Election + Log Replication + Commit Index + Terms + Failure Handling, because understanding Raft will make the entire consensus concept much easier in system-design interviews.

