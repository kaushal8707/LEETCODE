# LLD — What Is It and How to Create It?

## 1. What is LLD?

**LLD (Low-Level Design)** is the detailed design of a software system at the class, object, method, interface, and interaction level.

If HLD answers:

> "What components do we need and how do they communicate?"

LLD answers:

> "How exactly will those components/classes be implemented?"

For someone with 10 years of experience, LLD is especially important for designing code that is **maintainable, extensible, testable**, and follows good object-oriented principles.

---

## 2. HLD vs LLD

Suppose you're designing an **E-commerce Order System**.

### HLD

You might design:

```
                Client
                  |
                  v
             API Gateway
                  |
                  v
             Order Service
             /     |      \
            v      v       v
        Payment  Inventory Notification
          |          |
          v          v
       Payment DB  Inventory DB
```

HLD focuses on:

- Services
- Databases
- Kafka
- API Gateway
- Load Balancer
- Communication
- Scalability
- Availability

### LLD

Now you go inside **Order Service**.

```
OrderController
       |
       v
OrderService
       |
       +---- OrderRepository
       |
       +---- PaymentService
       |
       +---- InventoryService
       |
       +---- OrderValidator
```

Then you define:

- Classes
- Interfaces
- Methods
- Fields
- Relationships
- Design patterns
- Exception handling
- Validation
- Object interactions

---

## 3. Simple Definition

> **LLD is the detailed blueprint of how a software component will be implemented using classes, interfaces, objects, methods, relationships, and design patterns.**

---

## 4. What Does an LLD Contain?

A good LLD usually contains:

1. Requirements
2. Use cases
3. Actors
4. Classes
5. Interfaces
6. Attributes
7. Methods
8. Relationships
9. Class diagram
10. Sequence diagrams
11. Design patterns
12. Exception handling
13. Validation
14. Concurrency considerations
15. Extensibility
16. Persistence considerations
17. Unit-testability

You don't always need every item for every problem.

---

## 5. How to Create LLD — Step by Step

Use this process:

```
Requirements
     ↓
Identify Use Cases
     ↓
Identify Actors
     ↓
Identify Core Entities
     ↓
Identify Responsibilities
     ↓
Create Classes
     ↓
Create Interfaces
     ↓
Define Relationships
     ↓
Define Methods
     ↓
Apply SOLID
     ↓
Choose Design Patterns
     ↓
Create Class Diagram
     ↓
Create Sequence Diagram
     ↓
Handle Exceptions
     ↓
Consider Concurrency
     ↓
Review Extensibility
```

Let's understand each step.

---

## 6. Step 1 — Understand Requirements

Before creating classes, understand what the system must do.

**Example: Parking Lot**

Requirements:

1. Parking lot has multiple floors.
2. Each floor has parking spots.
3. Different vehicle types exist.
4. Vehicle enters parking lot.
5. System assigns a suitable spot.
6. Vehicle exits.
7. Payment is calculated.
8. Spot becomes available again.

Don't immediately start writing classes.

First understand the behavior.

---

## 7. Step 2 — Identify Use Cases

Convert requirements into actions.

For Parking Lot:

- Park Vehicle
- Remove Vehicle
- Find Available Spot
- Calculate Parking Fee
- Make Payment
- Display Available Spots

These become the system's use cases.

---

## 8. Step 3 — Identify Core Entities

Look for **nouns** in the requirements.

For Parking Lot:

- ParkingLot
- Floor
- ParkingSpot
- Vehicle
- Ticket
- Payment

This is a useful starting technique:

> **Nouns often suggest entities/classes, while verbs often suggest behaviors/methods.**

But don't blindly create a class for every noun. Each class should have a meaningful responsibility.

---

## 9. Step 4 — Define Responsibilities

Ask:

> Who should be responsible for what?

For example:

```
ParkingLot
    → manages floors

Floor
    → manages parking spots

ParkingSpot
    → knows whether it is occupied

Vehicle
    → contains vehicle information

Ticket
    → represents parking session

Payment
    → represents payment information
```

This is where good LLD starts.

---

## 10. Step 5 — Create Classes

Now define classes.

```java
class Vehicle {
    private String number;
    private VehicleType type;
}
```

```java
class ParkingSpot {
    private String id;
    private Vehicle vehicle;

    public boolean isAvailable() {
        return vehicle == null;
    }

    public void park(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public void removeVehicle() {
        this.vehicle = null;
    }
}
```

---

## 11. Step 6 — Use Interfaces

Suppose parking fees can be calculated differently.

You could define:

```java
interface PricingStrategy {

    double calculateFee(Ticket ticket);
}
```

Then:

```java
class HourlyPricingStrategy
        implements PricingStrategy {

    @Override
    public double calculateFee(Ticket ticket) {
        // calculate hourly fee
        return 100;
    }
}
```

Later:

```
PricingStrategy
      |
      +---- HourlyPricingStrategy
      |
      +---- DailyPricingStrategy
      |
      +---- WeekendPricingStrategy
```

This improves extensibility.

---

## 12. Step 7 — Define Relationships

Classes don't exist independently.

You need to determine relationships.

Common relationships:

- Association
- Aggregation
- Composition
- Inheritance
- Dependency
- Realization

For example:

```
ParkingLot
    |
    | contains
    v
ParkingFloor
    |
    | contains
    v
ParkingSpot
```

Conceptually:

```
ParkingLot
    ◆── ParkingFloor
            ◆── ParkingSpot
```

The diamond indicates **composition**.

---

## 13. Step 8 — Define Methods

Now determine what each class can do.

Example:

```
ParkingLot
------------------------
parkVehicle()
removeVehicle()
findAvailableSpot()

ParkingSpot
------------------------
isAvailable()
park()
removeVehicle()

Payment
------------------------
pay()
refund()
```

The important question is:

> **Which class should own this behavior?**

Avoid putting all logic into one giant class.

---

## 14. Step 9 — Apply SOLID Principles

This is extremely important in LLD interviews.

### SRP

**Single Responsibility Principle**

A class should have one primary responsibility.

**Bad:**

```
ParkingLot
 ├── parking
 ├── payment
 ├── notification
 ├── report generation
 └── database access
```

**Better:**

```
ParkingService
PaymentService
NotificationService
ReportService
ParkingRepository
```

### OCP

**Open/Closed Principle**

Design for extension without constantly modifying existing code.

Example:

```
PricingStrategy
    |
    +── HourlyPricing
    +── DailyPricing
    +── WeekendPricing
```

### LSP

Subclasses should be usable where their parent abstraction is expected.

### ISP

Prefer smaller focused interfaces.

Instead of:

```java
interface VehicleOperations {
    park();
    drive();
    fly();
    charge();
}
```

split responsibilities appropriately.

### DIP

High-level classes should depend on abstractions.

Instead of:

```java
class PaymentService {

    private StripePayment payment;
}
```

prefer:

```java
class PaymentService {

    private PaymentGateway paymentGateway;
}
```

where:

```
PaymentGateway
      |
      +---- StripePaymentGateway
      |
      +---- RazorpayPaymentGateway
```

---

## 15. Step 10 — Choose Design Patterns

Don't start with design patterns.

First understand the problem.

Then ask:

> Is there a recurring design problem that a pattern solves cleanly?

Common LLD patterns:

```
Creational
    Factory
    Abstract Factory
    Builder
    Singleton
    Prototype

Structural
    Adapter
    Decorator
    Facade
    Proxy
    Composite
    Bridge

Behavioral
    Strategy
    Observer
    Command
    State
    Chain of Responsibility
    Template Method
```

For example, if payment methods can change:

```
PaymentService
      |
      v
PaymentStrategy
      |
      +---- CardPayment
      +---- UpiPayment
      +---- WalletPayment
```

This is a good use case for **Strategy Pattern**.

---

## 16. Step 11 — Create Class Diagram

For the Parking Lot example:

```
                    +------------------+
                    |    ParkingLot    |
                    +------------------+
                    | floors           |
                    +------------------+
                    | parkVehicle()    |
                    | removeVehicle()  |
                    +--------+---------+
                             |
                             | 1..*
                             v
                    +------------------+
                    |  ParkingFloor    |
                    +------------------+
                    | spots            |
                    +------------------+
                             |
                             | 1..*
                             v
                    +------------------+
                    |  ParkingSpot     |
                    +------------------+
                    | id               |
                    | vehicle          |
                    +------------------+
                    | isAvailable()    |
                    | park()           |
                    | removeVehicle()  |
                    +------------------+

                    +------------------+
                    |     Vehicle      |
                    +------------------+
                    | number           |
                    | type             |
                    +------------------+

                    +------------------+
                    |      Ticket      |
                    +------------------+
                    | ticketId         |
                    | entryTime        |
                    | exitTime         |
                    +------------------+
```

---

## 17. Step 12 — Create Sequence Diagram

Class diagrams tell you **what exists**.

Sequence diagrams tell you **how objects interact**.

For parking a vehicle:

```
Customer
   |
   | park(vehicle)
   v
ParkingLot
   |
   | findAvailableSpot()
   v
ParkingFloor
   |
   | findSpot()
   v
ParkingSpot
   |
   | park(vehicle)
   v
ParkingLot
   |
   | createTicket()
   v
Ticket
```

This helps identify responsibilities and method calls.

---

## 18. Step 13 — Handle Exceptions

Your LLD should define failure scenarios.

For example:

```
No parking spot
     ↓
ParkingLotFullException
```

Payment failure:

```
Payment
   ↓
PaymentGateway
   ↓
Failure
   ↓
PaymentFailedException
```

Define meaningful exceptions rather than using generic exceptions everywhere.

---

## 19. Step 14 — Consider Concurrency

This is often missed in LLD interviews.

Imagine two cars arrive simultaneously:

```
Car A ──────┐
            ├──→ Same ParkingSpot
Car B ──────┘
```

Without synchronization:

```
Thread A → sees spot available
Thread B → sees spot available
Thread A → parks
Thread B → parks
```

You have an inconsistent state.

You need an appropriate concurrency mechanism, such as:

- `synchronized`
- `Lock`
- Atomic operations
- Database transaction
- Distributed lock

depending on where the shared state lives.

---

## 20. Step 15 — Think About Extensibility

Ask:

> What is likely to change?

Parking lot example:

```
Today:
Car
Bike

Tomorrow:
Truck
Electric Vehicle
Disabled Parking
```

Don't hardcode everything around only `Car`.

Use abstractions:

```java
enum VehicleType {
    BIKE,
    CAR,
    TRUCK,
    ELECTRIC
}
```

and appropriate strategies/spot types where behavior differs.

---

## 21. Step 16 — Think About Testability

Good LLD should be easy to test.

For example:

```
PaymentService
      |
      v
PaymentGateway interface
      |
      +---- RealPaymentGateway
      |
      +---- MockPaymentGateway
```

Unit tests can inject:

```
MockPaymentGateway
```

instead of calling a real payment provider.

This is one of the practical benefits of **dependency inversion**.

---

## 22. A Practical LLD Creation Template

When solving any LLD problem, use:

```
1. Requirements
       ↓
2. Use Cases
       ↓
3. Actors
       ↓
4. Core Entities
       ↓
5. Responsibilities
       ↓
6. Classes
       ↓
7. Interfaces
       ↓
8. Relationships
       ↓
9. Methods
       ↓
10. SOLID
       ↓
11. Design Patterns
       ↓
12. Class Diagram
       ↓
13. Sequence Diagram
       ↓
14. Exception Handling
       ↓
15. Concurrency
       ↓
16. Extensibility
       ↓
17. Testability
```

---

## 23. HLD → LLD Relationship

This is very important given your system-design learning path.

Suppose HLD says:

```
                    API Gateway
                         |
                         v
                   Order Service
                         |
             +-----------+-----------+
             |                       |
             v                       v
        Payment Service         Inventory Service
```

Then LLD goes inside:

```
                 Order Service
                      |
          +-----------+-----------+
          |           |           |
          v           v           v
    Controller    Service     Repository
          |           |
          |           +------→ PaymentClient
          |
          +------→ Validator
```

Then you define actual classes:

- OrderController
- OrderService
- OrderRepository
- PaymentClient
- OrderValidator
- Order
- OrderStatus

And their methods:

- `createOrder()`
- `getOrder()`
- `cancelOrder()`
- `validateOrder()`
- `saveOrder()`
- `processPayment()`

So:

```
HLD
 ↓
System components

LLD
 ↓
Classes + objects + methods + interactions
```

---

## 24. LLD Example — Payment System

Suppose requirements are:

1. Customer can pay.
2. Support Card.
3. Support UPI.
4. Support Wallet.
5. Payment provider can change.
6. Payment can succeed/fail.
7. Need refunds.

**A poor design:**

```java
class PaymentService {

    void pay(String type) {

        if (type.equals("CARD")) {
            // card logic
        } else if (type.equals("UPI")) {
            // UPI logic
        } else if (type.equals("WALLET")) {
            // wallet logic
        }
    }
}
```

As payment types grow, this becomes difficult to maintain.

**Better LLD:**

```
                  PaymentService
                        |
                        v
                 PaymentMethod
                 /     |      \
                /      |       \
             Card     UPI     Wallet
```

And perhaps:

```
              PaymentGateway
                /        \
               /          \
          ProviderA      ProviderB
```

Now payment method and provider can evolve independently.

---

## 25. What Interviewers Look For in LLD

For an experienced engineer, interviewers generally look beyond just drawing classes.

They look for:

- ✓ Requirement understanding
- ✓ Good class responsibilities
- ✓ Encapsulation
- ✓ Abstraction
- ✓ Composition
- ✓ Appropriate inheritance
- ✓ SOLID principles
- ✓ Design patterns where justified
- ✓ Extensibility
- ✓ Testability
- ✓ Error handling
- ✓ Thread safety/concurrency
- ✓ Clean APIs
- ✓ Avoidance of unnecessary complexity

The important thing is **not** to use every design pattern you know.

A good LLD is:

> **Simple enough to understand and flexible enough to handle expected change.**

---

## 26. LLD vs Coding

LLD is not simply:

> "Write Java code."

There is a design phase before implementation:

```
Requirements
      ↓
Design
      ↓
Class Diagram
      ↓
Sequence Diagram
      ↓
Interfaces
      ↓
Implementation
      ↓
Unit Tests
```

Coding is the implementation of the design.

---

## ⭐ Interview-Ready Definition

> **LLD, or Low-Level Design, is the detailed design of a software system at the class and object level. It defines classes, interfaces, attributes, methods, relationships, object interactions, design patterns, exception handling, concurrency considerations, and extensibility. To create an LLD, first understand the requirements and use cases, identify core entities and responsibilities, design classes and interfaces, define relationships and interactions, apply SOLID principles, introduce appropriate design patterns, and validate the design using class and sequence diagrams.**

### Remember this flow:

```
Requirements
      ↓
Use Cases
      ↓
Entities
      ↓
Responsibilities
      ↓
Classes
      ↓
Interfaces
      ↓
Relationships
      ↓
Methods
      ↓
SOLID
      ↓
Design Patterns
      ↓
Class Diagram
      ↓
Sequence Diagram
      ↓
Concurrency + Exceptions
      ↓
Testability + Extensibility
```

For your system-design preparation, the natural next step is to practice LLD problems such as **Parking Lot → Elevator → Library Management → ATM → Vending Machine → Splitwise → Chess → Tic-Tac-Toe → Car Rental → Payment System**, while applying the SOLID principles and design patterns you've already been studying.

