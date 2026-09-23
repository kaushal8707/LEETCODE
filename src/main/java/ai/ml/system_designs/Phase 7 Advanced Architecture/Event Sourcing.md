# Event Sourcing

Event Sourcing is an architectural pattern where we store every change to an application's state as an immutable event instead of storing only the latest state.

Rather than saving just the current balance of a bank account, we save every transaction that changed the balance. The current balance can be reconstructed by replaying those events.

This is an important topic in System Design, Microservices, and Event-Driven Architecture, especially for someone with 10 years of experience preparing for system design interviews.

---

## 1. What is Event Sourcing?

In a traditional application, we store the current state of an entity in a database.

### Traditional approach

Suppose a bank account starts with ₹1,000.

**Bank Account — Current state stored in database**

| Field | Value |
|---|---|
| Account ID | ACC-101 |
| Balance | ₹700 |

The database stores the latest balance. The history of how it became ₹700 may be stored separately, or not at all.

If the account had:

- Deposited ₹1,000
- Deposited ₹500
- Withdrawn ₹800

The final balance is ₹700.

The traditional database might contain only:

```
account_id | balance
-----------|--------
ACC-101    | 700
```

**Problem:** We know the current balance, but not necessarily the complete history of changes.

---

## 2. Event Sourcing approach

With Event Sourcing, we store every business change as an event.

Instead of:

```
Account Balance = ₹700
```

We store:

```
MoneyDeposited(₹1,000)
MoneyDeposited(₹500)
MoneyWithdrawn(₹800)
```

The current balance is calculated from these events.

### Event Store — Immutable history of business changes (Append-only)

| Event | Change | Resulting Balance |
|---|---|---|
| MoneyDeposited | +₹1,000 | Balance becomes ₹1,000 |
| MoneyDeposited | +₹500 | Balance becomes ₹1,500 |
| MoneyWithdrawn | −₹800 | Balance becomes ₹700 |

**Reconstructed balance:** ₹700

### Key idea

The event history is the source of truth. The current state is derived by applying the events.

---

## 3. How Event Sourcing works internally

Let's understand the complete flow using a real-time banking example.

1. **User requests withdrawal** — Withdraw ₹800 from account ACC-101
2. **Account Service** — Validates the command and business rules.
3. **Generate domain event** — `MoneyWithdrawn(₹800)`
4. **Append event to Event Store** — Event is persisted as an immutable record.
5. **Update current state** — Apply the event to calculate the new balance.

### Step-by-step explanation

#### Step 1: Client sends a command

A command represents an intention to perform an action.

```json
{
  "command": "WithdrawMoney",
  "accountId": "ACC-101",
  "amount": 800
}
```

The command says:

> Please withdraw ₹800 from this account.

A command is not an event. It is a request to perform an operation.

#### Step 2: Domain validates the command

The Account Service checks:

- Does the account exist?
- Is the account active?
- Is the withdrawal amount valid?
- Is there enough balance?
- Does the request meet business rules?

For example:

```
Current Balance = ₹1,500
Withdrawal      = ₹800

₹1,500 - ₹800 = ₹700
```

If the withdrawal is valid, the service generates an event.

#### Step 3: Create a domain event

```json
{
  "eventId": "evt-003",
  "eventType": "MoneyWithdrawn",
  "aggregateId": "ACC-101",
  "amount": 800,
  "occurredAt": "2026-09-14T10:00:00Z"
}
```

This event represents a fact:

> ₹800 was withdrawn from account ACC-101.

Notice the difference:

| Command | Event |
|---|---|
| WithdrawMoney | MoneyWithdrawn |
| DepositMoney | MoneyDeposited |
| CreateOrder | OrderCreated |
| CancelOrder | OrderCancelled |

**Command = intention. Event = fact.**

#### Step 4: Persist the event

The event is appended to the Event Store.

Example:

```
Event Store
───────────────────────────────────────────────
Sequence | Aggregate | Event Type       | Amount
───────────────────────────────────────────────
1        | ACC-101   | AccountCreated   | -
2        | ACC-101   | MoneyDeposited   | 1000
3        | ACC-101   | MoneyDeposited   | 500
4        | ACC-101   | MoneyWithdrawn   | 800
───────────────────────────────────────────────
```

Events are not normally updated or deleted as part of ordinary business processing.

#### Step 5: Rebuild the current state

The Account Aggregate replays the events:

```
Initial Balance = ₹0

AccountCreated
    Balance = ₹0

MoneyDeposited(₹1,000)
    Balance = ₹1,000

MoneyDeposited(₹500)
    Balance = ₹1,500

MoneyWithdrawn(₹800)
    Balance = ₹700
```

Final state:

```
Account Balance = ₹700
```

---

## 4. Event Sourcing architecture

### Architecture overview

- **Client** — REST API / UI
- **Command Handler** — Receives commands
- **Domain Aggregate** — Business rules + current in-memory state
- **Event Store** — Append-only event history
- **Event Publisher** — Publishes committed events to consumers
- **Read Model** — Current balance / queries
- **Other Services** — Notifications, audit, analytics

### Important distinction

The Event Store is the source of truth for the aggregate's history.

A read database, such as a SQL table containing the current balance, is often a projection or read model derived from that history.

---

## 5. Event Sourcing vs traditional CRUD

| Feature | Traditional CRUD | Event Sourcing |
|---|---|---|
| Source of truth | Current database state | Event history |
| Data storage | Latest state | Immutable events |
| Updates | Modify existing rows | Append new events |
| History | May need audit tables | Naturally preserved |
| Current state | Read directly | Rebuild or read projection |
| Debugging | Inspect current values | Replay history |
| Complexity | Usually simpler | More complex |

### Traditional CRUD example

```sql
UPDATE account
SET balance = 700
WHERE account_id = 'ACC-101';
```

The old balance may be overwritten.

### Event Sourcing example

```sql
INSERT INTO events (
    aggregate_id,
    event_type,
    amount
)
VALUES (
    'ACC-101',
    'MoneyWithdrawn',
    800
);
```

The withdrawal is recorded as a new fact.

---

## 6. Real-time example: E-commerce Order

Consider an order in an e-commerce application.

An order goes through several stages:

```
OrderCreated
      ↓
PaymentCompleted
      ↓
OrderConfirmed
      ↓
OrderShipped
      ↓
OrderDelivered
```

With Event Sourcing, we store every transition.

```json
[
  {
    "eventType": "OrderCreated",
    "orderId": "ORD-101"
  },
  {
    "eventType": "PaymentCompleted",
    "orderId": "ORD-101"
  },
  {
    "eventType": "OrderConfirmed",
    "orderId": "ORD-101"
  },
  {
    "eventType": "OrderShipped",
    "orderId": "ORD-101"
  },
  {
    "eventType": "OrderDelivered",
    "orderId": "ORD-101"
  }
]
```

The current order status is:

```
Order Status = DELIVERED
```

### What if the customer asks:

> When was my order shipped?

You can replay or query the event history and find:

```
OrderShipped
Timestamp: 2026-09-14 09:30
```

### What if a bug occurred?

Suppose the order was marked delivered incorrectly.

You can inspect the event history:

```
OrderCreated
PaymentCompleted
OrderConfirmed
OrderShipped
OrderDelivered
```

This makes troubleshooting and auditing much easier than looking only at:

```
order_status = DELIVERED
```

---

## 7. Event Sourcing with CQRS

Event Sourcing and CQRS are different patterns, but they are frequently used together.

### What is CQRS?

**CQRS = Command Query Responsibility Segregation.**

It separates:

- **Command side:** Handles writes and business operations.
- **Query side:** Handles reads and optimized queries.

### CQRS + Event Sourcing

- **Command side** — Withdraw money → Validate → Save event
- **Query side** — Read balance → Query read model
- Events flow from the write side to projections
- **Event Store** — Source of truth
- **Projection / Read Model** — Build query-optimized data from events

### Example

A banking application can use:

- Event Store: all deposits and withdrawals.
- Account Balance Projection: current balance for fast reads.
- Transaction History Projection: list of transactions.
- Analytics Projection: spending patterns.

One event can update multiple read models.

> **Important:** Event Sourcing does not require CQRS, and CQRS does not require Event Sourcing. They are independent patterns.

---

## 8. Event Store vs Message Broker

This is a very common interview question, especially when discussing Kafka.

| Event Store | Message Broker |
|---|---|
| Stores the authoritative event history | Transports or distributes messages |
| Used to rebuild aggregate state | Used to communicate between services |
| Events are retained according to event-store policy | Messages retained according to broker policy |
| Supports aggregate versioning and concurrency | Supports consumer groups and delivery |
| Example: EventStoreDB, a custom event database | Example: Kafka, RabbitMQ |

### Can Kafka be used as an Event Store?

Yes, Kafka can serve as an event log for some event-sourced systems, but using Kafka alone does not automatically give you all the features of a dedicated event store.

For a bank account, you may use:

```
Account Service
      ↓
Kafka / Event Store
      ↓
Balance Projection
      ↓
Read API
```

Kafka's log retention and replay capabilities can support event-driven reconstruction. However, the system must still handle aggregate identity, ordering, optimistic concurrency, event schema evolution, and durable projections correctly.

---

## 9. Java example: Event Sourcing

Let's build a simple bank account using Java.

The example shows the core idea: events are stored, and the account state is rebuilt by replaying them.

### Step 1: Define events

```java
public sealed interface AccountEvent
        permits AccountCreated,
                MoneyDeposited,
                MoneyWithdrawn {
}
```

```java
public record AccountCreated() implements AccountEvent {
}
```

```java
public record MoneyDeposited(
        BigDecimal amount
) implements AccountEvent {
}
```

```java
public record MoneyWithdrawn(
        BigDecimal amount
) implements AccountEvent {
}
```

### Step 2: Create the Account Aggregate

```java
public class BankAccount {

    private BigDecimal balance = BigDecimal.ZERO;

    public void apply(AccountEvent event) {

        if (event instanceof AccountCreated) {
            balance = BigDecimal.ZERO;

        } else if (event instanceof MoneyDeposited deposited) {
            balance = balance.add(deposited.amount());

        } else if (event instanceof MoneyWithdrawn withdrawn) {
            balance = balance.subtract(withdrawn.amount());
        }
    }

    public BigDecimal getBalance() {
        return balance;
    }
}
```

The `apply()` method is responsible for changing the current state based on an event.

### Step 3: Replay events

```java
List<AccountEvent> events = List.of(
        new AccountCreated(),
        new MoneyDeposited(new BigDecimal("1000")),
        new MoneyDeposited(new BigDecimal("500")),
        new MoneyWithdrawn(new BigDecimal("800"))
);

BankAccount account = new BankAccount();

for (AccountEvent event : events) {
    account.apply(event);
}

System.out.println(account.getBalance());
```

Output:

```
700
```

### What is happening?

```
AccountCreated        → ₹0
MoneyDeposited(1000)  → ₹1,000
MoneyDeposited(500)   → ₹1,500
MoneyWithdrawn(800)   → ₹700
```

This is the fundamental mechanism behind event replay.

> **Production note:** The example demonstrates event application only. A real banking system needs proper validation, atomic event persistence, concurrency control, idempotency, and monetary rules.

---

## 10. Important concept: Aggregate

An **Aggregate** is a consistency boundary that owns business rules and processes commands.

For example:

```
BankAccount Aggregate
    ├── accountId
    ├── balance
    ├── status
    └── apply(event)
```

It processes commands:

```
DepositMoney
WithdrawMoney
CloseAccount
```

It generates events:

```
MoneyDeposited
MoneyWithdrawn
AccountClosed
```

### Why Aggregate matters

Suppose two requests try to withdraw ₹800 from an account with ₹1,000.

Without proper concurrency control:

```
Request A reads balance = ₹1,000
Request B reads balance = ₹1,000

A withdraws ₹800
B withdraws ₹800

Final balance = -₹600
```

This is incorrect.

Event-sourced systems commonly use **optimistic concurrency control** with an aggregate version.

```
Account ACC-101
Expected Version = 5
```

The event store accepts the new event only if the current version is still 5.

If another request has already appended an event, the version changes and the second write fails with a concurrency conflict.

This prevents multiple writers from silently overwriting each other's aggregate history.

---

## 11. Event Sourcing benefits

- **Complete audit history** — Every business change is recorded. Useful for banking, payments, compliance, and order tracking.
- **Time travel / replay** — Reconstruct the state at a previous point in time by replaying events up to a chosen version.
- **Multiple read models** — Build different projections from the same event history.
- **Debugging** — Investigate what happened and in which sequence.

---

## 12. Event Sourcing challenges

Event Sourcing is powerful, but it introduces complexity.

| Challenge | Explanation |
|---|---|
| Event replay cost | Thousands or millions of events may be expensive to replay |
| Event schema evolution | Old events must remain readable after code changes |
| Data correction | You should not casually edit historical events |
| Eventual consistency | Read projections may lag behind the event store |
| Debugging complexity | Distributed event flows are harder to trace |
| Storage growth | Event history grows over time |
| Concurrency | Multiple commands may try to update the same aggregate |

### How do we solve replay performance?

Use **snapshots**.

Example:

```
Events 1 → 1000
        ↓
Snapshot at Version 1000
        ↓
Events 1001 → 1050
```

To rebuild the state:

```
Load Snapshot (Version 1000)
        ↓
Replay Events 1001 → 1050
        ↓
Current State (Version 1050)
```

Instead of replaying all 1,050 events, replay only the last 50.

Snapshots are optimization data, not the source of truth.

---

## 13. Event Sourcing vs Event-Driven Architecture

These are often confused.

### Event-Driven Architecture

A service publishes events to communicate with other services.

```
Order Service
      ↓
OrderCreated
      ↓
Notification Service
      ↓
Email sent
```

The Order Service might store only the current order state in a traditional database.

### Event Sourcing

The service stores events as the authoritative history of its state.

```
OrderCreated
PaymentCompleted
OrderShipped
OrderDelivered
```

The current order state is reconstructed from those events.

| | Event-Driven Architecture | Event Sourcing |
|---|---|---|
| Main purpose | Communication between components | Persist state changes as events |
| Must store all events as source of truth? | No | Yes, for the event-sourced aggregate |
| Requires replay? | Not necessarily | Yes, to rebuild state when needed |
| Can use Kafka? | Yes | Yes, with proper design |

> **Remember:** Publishing an event to Kafka does not mean the service is using Event Sourcing.

---

## 14. When should we use Event Sourcing?

### Good use cases

- Banking transactions and financial ledgers.
- Payment processing.
- Order lifecycle and audit trails.
- Inventory changes.
- Compliance-heavy systems.
- Systems where the exact history of changes is valuable.
- Complex business workflows requiring multiple projections.

### When not to use it

- Simple CRUD applications.
- Basic user profile management.
- Small applications with no audit requirements.
- Systems where current state is all you need.
- Teams without the operational maturity to manage event versioning and replay.

For example, a simple employee profile application:

```
Employee
    id
    name
    email
    department
```

Traditional CRUD is usually sufficient.

---

## 15. Event Sourcing interview questions

**1. What is Event Sourcing?**

A pattern where state changes are stored as immutable events, and the current state is reconstructed by applying those events.

**2. What is the difference between a command and an event?**

A command is an intention to perform an operation. An event is a fact that an operation occurred.

**3. Why are events immutable?**

They represent historical facts. Changing them would alter the historical record and make auditing and replay unreliable.

**4. How do you rebuild state?**

Load the aggregate's events in the correct order and apply each event to an initially empty aggregate, or start from a snapshot and replay subsequent events.

**5. What is a snapshot?**

A saved state at a particular event version used to reduce the number of events that must be replayed.

**6. What is the difference between Event Sourcing and CQRS?**

Event Sourcing stores state changes as events. CQRS separates command processing from query processing. They can be used independently.

**7. Can Event Sourcing use Kafka?**

Yes. Kafka can provide a durable ordered log and replay capability, but the application still needs correct aggregate partitioning, concurrency control, event schemas, and projection handling.

**8. How do you handle two concurrent withdrawals?**

Use aggregate versioning and optimistic concurrency control. The event store accepts an append only if the expected version matches the current version.

**9. What happens if a projection fails?**

The projection can generally be rebuilt from the event history. Consumers should also handle retries and idempotency.

**10. Is Event Sourcing always eventually consistent?**

Not necessarily. The aggregate's own event append can be strongly consistent, while separately updated read models are often eventually consistent.

---

## Final summary

Event Sourcing stores **what happened**, not just what the current state is.

```
Command
   ↓
Validate business rules
   ↓
Generate event
   ↓
Append event to Event Store
   ↓
Update aggregate / projections
   ↓
Read current state
```

