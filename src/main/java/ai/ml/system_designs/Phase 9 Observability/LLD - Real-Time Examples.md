# LLD Creation with Real-Time Examples

Absolutely. The best way to understand LLD creation is to take a real-world problem and go from **requirements → classes → interfaces → relationships → design patterns → sequence → code structure**.

---

## 1. Real-world example: Parking Lot

Suppose we need to design the software for a parking lot like the ones used in malls or airports.

The system should:

- Support multiple floors
- Support cars, bikes, trucks
- Assign available parking spots
- Generate a parking ticket
- Calculate parking fees
- Process payment
- Free the parking spot when a vehicle leaves

We will create the LLD step by step.

---

## 2. Step 1 — Understand the Requirements

First, don't create classes.

Write down the requirements.

### Parking Lot System

1. Parking lot has multiple floors.
2. Each floor has multiple parking spots.
3. Different vehicle types can enter.
4. Vehicle should get a suitable parking spot.
5. Entry creates a ticket.
6. Exit calculates parking fee.
7. Customer makes payment.
8. Parking spot becomes available after exit.

This is the problem statement.

---

## 3. Step 2 — Identify Actors

Ask:

> Who interacts with the system?

For parking lot:

- Customer
- Parking Attendant
- Payment System

Potentially:

- Admin

---

## 4. Step 3 — Identify Use Cases

Convert requirements into actions.

```
                Parking Lot
                     |
       +-------------+-------------+
       |             |             |
       v             v             v
   Park Vehicle   Remove Vehicle  Payment
       |
       v
 Find Spot
       |
       v
 Create Ticket
```

Main use cases:

- Park Vehicle
- Find Parking Spot
- Create Ticket
- Calculate Fee
- Make Payment
- Remove Vehicle

---

## 5. Step 4 — Identify Core Entities

Now look at important nouns.

From:

> Parking lot has floors, floors have spots, vehicles need tickets, and payment is required.

We get:

- ParkingLot
- ParkingFloor
- ParkingSpot
- Vehicle
- Ticket
- Payment

Then ask whether each deserves a separate responsibility.

---

## 6. Step 5 — Define Responsibilities

This is one of the most important LLD steps.

### ParkingLot

Responsible for:

- Managing floors
- Finding suitable spots
- Parking vehicle
- Removing vehicle

### ParkingFloor

Responsible for:

- Managing its parking spots
- Finding available spot

### ParkingSpot

Responsible for:

- Knowing whether it is available
- Holding a vehicle

### Vehicle

Responsible for:

- Vehicle number
- Vehicle type

### Ticket

Responsible for:

- Ticket ID
- Entry time
- Exit time
- Vehicle
- Parking spot

### Payment

Responsible for:

- Payment amount
- Payment method
- Payment status

---

## 7. Step 6 — Identify What Can Change

This is where experienced engineers differentiate a simple design from an extensible design.

Ask:

> What requirements are likely to change?

For example:

**Vehicle types:**

- Car
- Bike
- Truck
- Electric Vehicle

**Parking strategies might change:**

- Nearest spot
- First available
- Cheapest spot
- EV charging spot
- Reserved spot

**Pricing might change:**

- Hourly
- Daily
- Weekend
- Peak hours

**Payment methods might change:**

- Cash
- Card
- UPI
- Wallet

These are candidates for abstractions/strategies.

---

## 8. Step 7 — Create the Vehicle Abstraction

Instead of hardcoding everything around cars:

```java
class Car {
}
```

we can define:

```java
public abstract class Vehicle {

    private String number;
    private VehicleType type;

    public Vehicle(String number, VehicleType type) {
        this.number = number;
        this.type = type;
    }

    public String getNumber() {
        return number;
    }

    public VehicleType getType() {
        return type;
    }
}
```

Then:

```java
public class Car extends Vehicle {

    public Car(String number) {
        super(number, VehicleType.CAR);
    }
}
```

```java
public class Bike extends Vehicle {

    public Bike(String number) {
        super(number, VehicleType.BIKE);
    }
}
```

```java
public class Truck extends Vehicle {

    public Truck(String number) {
        super(number, VehicleType.TRUCK);
    }
}
```

Now:

```
             Vehicle
                |
       +--------+--------+
       |        |        |
      Car     Bike     Truck
```

---

## 9. Step 8 — Parking Spot

Now define:

```java
public class ParkingSpot {

    private String id;
    private SpotType type;
    private Vehicle vehicle;

    public boolean isAvailable() {
        return vehicle == null;
    }

    public void park(Vehicle vehicle) {
        if (!isAvailable()) {
            throw new IllegalStateException(
                "Parking spot already occupied"
            );
        }

        this.vehicle = vehicle;
    }

    public void removeVehicle() {
        this.vehicle = null;
    }
}
```

The important principle:

> **ParkingSpot owns the state of whether it is occupied.**

We don't want random classes modifying its internal state directly.

---

## 10. Step 9 — Parking Floor

A floor contains multiple spots.

```java
public class ParkingFloor {

    private String floorId;

    private List<ParkingSpot> spots;

    public ParkingSpot findAvailableSpot(
            Vehicle vehicle) {

        for (ParkingSpot spot : spots) {

            if (spot.isAvailable()
                    && isCompatible(spot, vehicle)) {
                return spot;
            }
        }

        return null;
    }

    private boolean isCompatible(
            ParkingSpot spot,
            Vehicle vehicle) {

        return true;
    }
}
```

Conceptually:

```
ParkingFloor
      |
      +---- Spot 1
      +---- Spot 2
      +---- Spot 3
      +---- Spot 4
```

---

## 11. Step 10 — Parking Strategy

Now suppose the business says:

> "Today use the first available spot."

Tomorrow they say:

> "Use the nearest available spot."

We don't want to rewrite `ParkingLot`.

So create:

```java
public interface ParkingStrategy {

    ParkingSpot findSpot(
        List<ParkingFloor> floors,
        Vehicle vehicle
    );
}
```

Implementation:

```java
public class FirstAvailableSpotStrategy
        implements ParkingStrategy {

    @Override
    public ParkingSpot findSpot(
            List<ParkingFloor> floors,
            Vehicle vehicle) {

        for (ParkingFloor floor : floors) {

            ParkingSpot spot =
                floor.findAvailableSpot(vehicle);

            if (spot != null) {
                return spot;
            }
        }

        return null;
    }
}
```

Now we can add:

```
ParkingStrategy
       |
       +---- FirstAvailableStrategy
       |
       +---- NearestSpotStrategy
       |
       +---- CheapestSpotStrategy
       |
       +---- EVSpotStrategy
```

This is the **Strategy Pattern**.

---

## 12. Step 11 — Pricing Strategy

The same idea applies to pricing.

```java
public interface PricingStrategy {

    double calculateFee(Ticket ticket);
}
```

Implementation:

```java
public class HourlyPricingStrategy
        implements PricingStrategy {

    @Override
    public double calculateFee(Ticket ticket) {

        long hours = calculateHours(ticket);

        return hours * 50;
    }

    private long calculateHours(Ticket ticket) {
        // calculate duration
        return 2;
    }
}
```

Later:

```
PricingStrategy
       |
       +---- HourlyPricing
       |
       +---- DailyPricing
       |
       +---- WeekendPricing
       |
       +---- PeakHourPricing
```

Again, **Strategy Pattern**.

---

## 13. Step 12 — Payment Abstraction

Payment providers can change.

Create:

```java
public interface PaymentGateway {

    PaymentResult pay(double amount);

    PaymentResult refund(String transactionId);
}
```

Implementations:

```
PaymentGateway
      |
      +---- CardPaymentGateway
      |
      +---- UpiPaymentGateway
      |
      +---- WalletPaymentGateway
```

The high-level service doesn't care which implementation is being used.

---

## 14. Step 13 — Ticket

```java
public class Ticket {

    private String ticketId;
    private Vehicle vehicle;
    private ParkingSpot parkingSpot;
    private LocalDateTime entryTime;
    private LocalDateTime exitTime;

    public Ticket(
            String ticketId,
            Vehicle vehicle,
            ParkingSpot parkingSpot) {

        this.ticketId = ticketId;
        this.vehicle = vehicle;
        this.parkingSpot = parkingSpot;
        this.entryTime = LocalDateTime.now();
    }

    public void close() {
        this.exitTime = LocalDateTime.now();
    }
}
```

---

## 15. Step 14 — ParkingLot

Now bring everything together.

```java
public class ParkingLot {

    private List<ParkingFloor> floors;

    private ParkingStrategy parkingStrategy;

    private PricingStrategy pricingStrategy;

    public ParkingLot(
            List<ParkingFloor> floors,
            ParkingStrategy parkingStrategy,
            PricingStrategy pricingStrategy) {

        this.floors = floors;
        this.parkingStrategy = parkingStrategy;
        this.pricingStrategy = pricingStrategy;
    }

    public Ticket parkVehicle(Vehicle vehicle) {

        ParkingSpot spot =
                parkingStrategy.findSpot(
                    floors,
                    vehicle
                );

        if (spot == null) {
            throw new ParkingLotFullException();
        }

        spot.park(vehicle);

        return createTicket(vehicle, spot);
    }

    private Ticket createTicket(
            Vehicle vehicle,
            ParkingSpot spot) {

        return new Ticket(
                UUID.randomUUID().toString(),
                vehicle,
                spot
        );
    }
}
```

Notice the important thing:

> **ParkingLot doesn't know how the parking strategy works.**

It simply says:

```
"I need a spot."
       ↓
ParkingStrategy
       ↓
"Here is the spot."
```

That's abstraction.

---

## 16. Step 15 — Class Diagram

Now our LLD looks approximately like this:

```
                       +------------------+
                       |    ParkingLot    |
                       +------------------+
                       | floors           |
                       | parkingStrategy  |
                       | pricingStrategy  |
                       +------------------+
                       | parkVehicle()    |
                       | removeVehicle()  |
                       +--------+---------+
                                |
                                |
                                v
                       +------------------+
                       |  ParkingFloor    |
                       +------------------+
                       | spots            |
                       +------------------+
                                |
                                |
                                v
                       +------------------+
                       |  ParkingSpot     |
                       +------------------+
                       | id               |
                       | type             |
                       | vehicle          |
                       +------------------+

                       +------------------+
                       |     Vehicle      |
                       +------------------+
                       | number           |
                       | type             |
                       +--------+---------+
                                |
                    +-----------+-----------+
                    |           |           |
                   Car         Bike       Truck


ParkingLot
    |
    +---- ParkingStrategy
    |          |
    |          +---- FirstAvailable
    |          +---- NearestSpot
    |
    +---- PricingStrategy
               |
               +---- HourlyPricing
               +---- DailyPricing


PaymentGateway
       |
       +---- CardPayment
       +---- UpiPayment
       +---- WalletPayment
```

---

## 17. Step 16 — Sequence Diagram

Now ask:

> What happens when a car enters?

```
Customer
   |
   | park(car)
   v
ParkingLot
   |
   | findSpot(car)
   v
ParkingStrategy
   |
   | return spot
   v
ParkingLot
   |
   | spot.park(car)
   v
ParkingSpot
   |
   | create ticket
   v
Ticket
   |
   | return ticket
   v
Customer
```

More explicitly:

```
Customer
   |
   | 1. parkVehicle(car)
   v
ParkingLot
   |
   | 2. parkingStrategy.findSpot()
   v
ParkingStrategy
   |
   | 3. return spot
   v
ParkingLot
   |
   | 4. spot.park(car)
   v
ParkingSpot
   |
   | 5. createTicket()
   v
Ticket
   |
   | 6. ticket
   v
Customer
```

---

## 18. Exit Flow

When the customer leaves:

```
Customer
   |
   | exit(ticket)
   v
ParkingLot
   |
   | calculateFee(ticket)
   v
PricingStrategy
   |
   | fee = ₹100
   v
ParkingLot
   |
   | payment.pay(100)
   v
PaymentGateway
   |
   | SUCCESS
   v
ParkingLot
   |
   | spot.removeVehicle()
   v
ParkingSpot
```

This sequence is important because it validates whether your class responsibilities make sense.

---

## 19. Real-Time Example: Food Delivery

Let's apply the same LLD process to another real system.

Imagine something like a food-delivery application.

### Requirements

- Customer places order
- Restaurant accepts order
- Payment is processed
- Delivery partner is assigned
- Order is delivered
- Customer can cancel

### Identify entities

- Customer
- Restaurant
- Order
- OrderItem
- Payment
- DeliveryPartner
- Address

### Identify services

- OrderService
- PaymentService
- DeliveryService
- RestaurantService

### Identify changing behavior

**Payment:**

- Card
- UPI
- Wallet

**Delivery assignment:**

- NearestPartner
- LeastBusyPartner
- PremiumPartner

**Pricing:**

- DistanceBased
- SurgePricing
- CouponPricing

Now Strategy Pattern becomes useful.

---

## 20. Real-Time Example: Payment System

### Requirements

- Customer can pay
- Support UPI
- Support Card
- Support Wallet
- Payment can fail
- Payment can be retried
- Payment can be refunded
- Multiple payment providers

### Possible LLD

```
                    PaymentService
                          |
             +------------+------------+
             |                         |
             v                         v
       PaymentMethod              PaymentGateway
          /   |   \                  /       \
         /    |    \                /         \
      Card   UPI  Wallet        ProviderA   ProviderB
```

This is much better than:

```java
if (type == CARD) {
   ...
} else if (type == UPI) {
   ...
} else if (type == WALLET) {
   ...
}
```

because adding another payment method doesn't require continuously growing one giant method.

---

## 21. Real-Time Example: Notification System

### Requirements

- Send notification
- Support Email
- Support SMS
- Support Push

### LLD

```
              NotificationService
                       |
                       v
              NotificationSender
                 /      |       \
                /       |        \
             Email      SMS      Push
```

Interface:

```java
public interface NotificationSender {

    void send(
        String recipient,
        String message
    );
}
```

Implementations:

```java
class EmailNotificationSender
        implements NotificationSender {
}

class SmsNotificationSender
        implements NotificationSender {
}

class PushNotificationSender
        implements NotificationSender {
}
```

Now `NotificationService` depends on the abstraction.

---

## 22. Real-Time Example: E-Commerce Order

Suppose:

```
Customer
   |
   v
OrderController
   |
   v
OrderService
   |
   +---- OrderValidator
   |
   +---- InventoryService
   |
   +---- PaymentService
   |
   +---- OrderRepository
   |
   +---- NotificationService
```

The LLD classes could be:

- OrderController
- OrderService
- OrderValidator
- OrderRepository
- InventoryService
- PaymentService
- NotificationService
- Order
- OrderItem
- Payment

### Sequence

```
Client
  |
  v
OrderController
  |
  v
OrderService
  |
  +---- Validate Order
  |
  +---- Reserve Inventory
  |
  +---- Process Payment
  |
  +---- Save Order
  |
  +---- Send Notification
  |
  v
Response
```

At HLD level you might see:

- Order Service
- Payment Service
- Inventory Service
- Kafka
- Database

At LLD level you see:

- OrderController
- OrderService
- OrderRepository
- PaymentClient
- InventoryClient
- OrderValidator
- Order
- OrderItem

---

## 23. The Most Important LLD Question

Whenever you create a class, ask:

> **"What responsibility does this class own?"**

For example:

**Bad:**

```
OrderService
    |
    +---- Validate order
    +---- Calculate tax
    +---- Process payment
    +---- Send email
    +---- Save database
    +---- Generate invoice
    +---- Generate report
```

This becomes a **God class**.

**Better:**

```
OrderService
   |
   +---- OrderValidator
   +---- PricingService
   +---- PaymentService
   +---- NotificationService
   +---- InvoiceService
   +---- OrderRepository
```

Each class has a clearer responsibility.

---

## 24. How SOLID Fits Into LLD

LLD isn't just about drawing classes.

You should continuously evaluate the design against **SOLID**.

```
LLD
 |
 +-- SRP → One clear responsibility
 |
 +-- OCP → Easy to extend
 |
 +-- LSP → Correct substitution
 |
 +-- ISP → Focused interfaces
 |
 +-- DIP → Depend on abstractions
```

Example:

```
PaymentService
      |
      v
PaymentGateway
      |
      +---- StripeGateway
      +---- OtherGateway
```

This demonstrates **DIP**.

And:

```
PaymentStrategy
      |
      +---- Card
      +---- UPI
      +---- Wallet
```

can demonstrate **OCP + Strategy Pattern**.

---

## 25. LLD Creation Checklist

When you get an LLD problem in an interview, follow this exact sequence:

```
                    LLD Problem
                         |
                         v
                1. Requirements
                         |
                         v
                  2. Use Cases
                         |
                         v
                    3. Actors
                         |
                         v
                  4. Entities
                         |
                         v
                5. Responsibilities
                         |
                         v
                     6. Classes
                         |
                         v
                   7. Interfaces
                         |
                         v
                  8. Relationships
                         |
                         v
                     9. Methods
                         |
                         v
                    10. SOLID
                         |
                         v
                 11. Design Patterns
                         |
                         v
                  12. Class Diagram
                         |
                         v
                13. Sequence Diagram
                         |
                         v
              14. Exception Handling
                         |
                         v
                  15. Concurrency
                         |
                         v
                   16. Extensibility
                         |
                         v
                    17. Testability
```

---

## 26. HLD → LLD → Code

This is the overall software-design hierarchy:

```
                    REQUIREMENTS
                         |
                         v
                        HLD
                         |
             +-----------+-----------+
             |                       |
             v                       v
        Services                  Database
             |
             v
             LLD
             |
       +-----+-----+
       |     |     |
       v     v     v
    Classes Interfaces Patterns
       |
       v
     Methods
       |
       v
      CODE
       |
       v
     TESTS
```

### Example

**HLD:**

- Order Service
- Payment Service
- Inventory Service
- Kafka
- Order DB

**LLD:**

- OrderController
- OrderService
- OrderRepository
- PaymentClient
- InventoryClient
- OrderValidator
- Order
- OrderItem

**Code:**

```java
orderService.createOrder(request);
```

---

## ⭐ The Golden Rule for LLD

Don't start with:

> "Which design pattern should I use?"

Start with:

```
What does the system need to do?
        ↓
Who is responsible?
        ↓
What changes frequently?
        ↓
What should be abstract?
        ↓
How do objects collaborate?
        ↓
Which pattern, if any, makes that collaboration cleaner?
```

That approach produces much better LLD than trying to force Factory, Strategy, Singleton, Observer, etc. into every problem.

---

## For your preparation, a strong progression is:

```
1. Parking Lot
       ↓
2. Vending Machine
       ↓
3. Elevator
       ↓
4. ATM
       ↓
5. Library Management
       ↓
6. Tic-Tac-Toe / Chess
       ↓
7. Splitwise
       ↓
8. Car Rental
       ↓
9. Food Delivery
       ↓
10. Payment System
       ↓
11. Notification System
       ↓
12. E-Commerce Order System
```

The goal is to solve each one using the same **Requirements → Entities → Responsibilities → Classes → Interfaces → Relationships → Patterns → Sequence → Edge Cases → Concurrency** process.

