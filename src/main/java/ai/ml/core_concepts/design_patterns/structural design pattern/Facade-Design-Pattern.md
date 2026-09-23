# Facade Design Pattern

The Facade Design Pattern is a Structural Design Pattern.

Facade provides a simple, unified interface to a complex subsystem.

In simple words:

Hide complicated internal operations behind one simple method.

A very simple way to remember it:

```
Many complex classes
       ↓
     Facade
       ↓
One simple interface
```

## 1. Real-Time Example — Online Shopping Checkout

Imagine you are buying a product from an e-commerce application.

From the customer's perspective:

Click "Place Order"

But internally, many things happen:

```
Place Order
    |
    +── Validate Customer
    |
    +── Check Inventory
    |
    +── Process Payment
    |
    +── Create Order
    |
    +── Generate Invoice
    |
    +── Send Notification
    |
    +── Arrange Shipping
```

The customer doesn't need to interact with all these services individually.

Instead:

```
Customer
    |
    ↓
OrderFacade
    |
    +── CustomerService
    +── InventoryService
    +── PaymentService
    +── OrderService
    +── InvoiceService
    +── NotificationService
    +── ShippingService
```

The Facade hides all this complexity.

## 2. Without Facade ❌

Suppose we have:

```java
class CustomerService {

    public boolean validateCustomer() {
        System.out.println("Customer validated");
        return true;
    }
}
```

```java
class InventoryService {

    public boolean checkInventory() {
        System.out.println("Inventory checked");
        return true;
    }
}
```

```java
class PaymentService {

    public boolean processPayment() {
        System.out.println("Payment processed");
        return true;
    }
}
```

```java
class OrderService {

    public void createOrder() {
        System.out.println("Order created");
    }
}
```

Now the client has to understand everything:

```java
CustomerService customer =
        new CustomerService();

InventoryService inventory =
        new InventoryService();

PaymentService payment =
        new PaymentService();

OrderService order =
        new OrderService();

if (customer.validateCustomer()
        && inventory.checkInventory()
        && payment.processPayment()) {

    order.createOrder();
}
```

This works, but the client is now tightly coupled to many subsystem classes.

### Facade Design Pattern

The Facade Design Pattern is a Structural Design Pattern.

Facade provides a simple, unified interface to a complex subsystem.

In simple words:

Facade hides complex implementation and gives the client one simple entry point.

### Easy example

When you order something online, you simply click:

```
        "Place Order"
              |
              ↓
         Order Facade
              |
    +---------+---------+---------+
    ↓         ↓         ↓         ↓
Inventory   Payment    Order    Notification
 Service    Service   Service     Service
```

You don't personally call all these services. The Facade coordinates them for you.

## 1. Real-Time Example — E-Commerce Checkout

Suppose an e-commerce application has these services:

- CustomerService
- InventoryService
- PaymentService
- OrderService
- NotificationService
- ShippingService

To place an order, we need to:

1. Validate customer
2. Check product availability
3. Process payment
4. Create order
5. Arrange shipping
6. Send notification

Without Facade, the client might need to know all these classes.

With Facade:

```java
orderFacade.placeOrder();
```

That's it.

## 2. Subsystem Classes

### CustomerService

```java
class CustomerService {

    public boolean validateCustomer() {

        System.out.println(
                "Customer validated"
        );

        return true;
    }
}
```

### InventoryService

```java
class InventoryService {

    public boolean checkInventory() {

        System.out.println(
                "Inventory checked"
        );

        return true;
    }
}
```

### PaymentService

```java
class PaymentService {

    public boolean processPayment() {

        System.out.println(
                "Payment processed"
        );

        return true;
    }
}
```

### OrderService

```java
class OrderService {

    public void createOrder() {

        System.out.println(
                "Order created"
        );
    }
}
```

### ShippingService

```java
class ShippingService {

    public void arrangeShipping() {

        System.out.println(
                "Shipping arranged"
        );
    }
}
```

### NotificationService

```java
class NotificationService {

    public void sendNotification() {

        System.out.println(
                "Order confirmation sent"
        );
    }
}
```

These are our subsystem classes.

## 3. Create the Facade

Now create:

```java
class OrderFacade {

    private CustomerService customerService;
    private InventoryService inventoryService;
    private PaymentService paymentService;
    private OrderService orderService;
    private ShippingService shippingService;
    private NotificationService notificationService;

    public OrderFacade() {

        customerService =
                new CustomerService();

        inventoryService =
                new InventoryService();

        paymentService =
                new PaymentService();

        orderService =
                new OrderService();

        shippingService =
                new ShippingService();

        notificationService =
                new NotificationService();
    }

    public void placeOrder() {

        if (!customerService.validateCustomer()) {
            return;
        }

        if (!inventoryService.checkInventory()) {
            return;
        }

        if (!paymentService.processPayment()) {
            return;
        }

        orderService.createOrder();

        shippingService.arrangeShipping();

        notificationService.sendNotification();
    }
}
```

Now the complexity is hidden inside:

`OrderFacade`

## 4. Client Code

The client doesn't need to know about:

- CustomerService
- InventoryService
- PaymentService
- OrderService
- ShippingService
- NotificationService

It simply does:

```java
public class Main {

    public static void main(String[] args) {

        OrderFacade orderFacade =
                new OrderFacade();

        orderFacade.placeOrder();
    }
}
```

Output:

```
Customer validated
Inventory checked
Payment processed
Order created
Shipping arranged
Order confirmation sent
```

This is the Facade Pattern.

## 5. Understand the Architecture

```
                    CLIENT
                      |
                      |
              placeOrder()
                      |
                      ↓
               +-------------+
               | OrderFacade |
               +-------------+
                      |
        +-------------+-------------+
        |             |             |
        ↓             ↓             ↓
 CustomerService  InventoryService  PaymentService
        |
        +-------------+-------------+
                      |
                      ↓
                 OrderService
                      |
                      ↓
                ShippingService
                      |
                      ↓
              NotificationService
```

The client communicates primarily with:

`OrderFacade`

instead of directly communicating with every subsystem.

## 6. Why Do We Need Facade?

Imagine there are 10 complex services.

Without Facade:

```
Client
  |
  +── Service 1
  +── Service 2
  +── Service 3
  +── Service 4
  +── Service 5
  +── Service 6
  +── Service 7
  +── Service 8
  +── Service 9
  +── Service 10
```

The client needs to understand all of them.

With Facade:

```
Client
   |
   ↓
Facade
   |
   +── Service 1
   +── Service 2
   +── Service 3
   +── ...
   +── Service 10
```

The client only needs to understand:

`Facade`

## 7. Real-Time Example — Hotel Booking

Imagine booking a hotel room.

From the customer's perspective:

Book Hotel

But internally:

```
Hotel Booking
     |
     +── Check Room Availability
     |
     +── Validate Customer
     |
     +── Process Payment
     |
     +── Reserve Room
     |
     +── Generate Invoice
     |
     +── Send Confirmation
```

You can create:

```java
class HotelBookingFacade {

    private RoomService roomService;
    private PaymentService paymentService;
    private CustomerService customerService;
    private NotificationService notificationService;

    public void bookRoom() {

        customerService.validateCustomer();

        roomService.checkAvailability();

        paymentService.processPayment();

        roomService.reserveRoom();

        notificationService.sendNotification();
    }
}
```

The client simply calls:

```java
hotelBookingFacade.bookRoom();
```

Instead of:

```java
customerService.validateCustomer();

roomService.checkAvailability();

paymentService.processPayment();

roomService.reserveRoom();

notificationService.sendNotification();
```

## 8. Real-Time Example — Home Theater

This is another classic Facade example.

Imagine a home theater system containing:

- Amplifier
- Projector
- DVD Player
- Lights
- Screen
- Sound System

To watch a movie, you might have to do:

1. Turn on projector
2. Lower screen
3. Turn on amplifier
4. Set amplifier input
5. Turn on DVD player
6. Start DVD
7. Dim lights

Without Facade:

```java
projector.on();
screen.down();
amplifier.on();
amplifier.setInput("DVD");
dvdPlayer.on();
dvdPlayer.play();
lights.dim();
```

That's complicated.

Create a Facade:

```java
class HomeTheaterFacade {

    private Projector projector;
    private Screen screen;
    private Amplifier amplifier;
    private DVDPlayer dvdPlayer;
    private Lights lights;

    public HomeTheaterFacade(
            Projector projector,
            Screen screen,
            Amplifier amplifier,
            DVDPlayer dvdPlayer,
            Lights lights) {

        this.projector = projector;
        this.screen = screen;
        this.amplifier = amplifier;
        this.dvdPlayer = dvdPlayer;
        this.lights = lights;
    }

    public void watchMovie() {

        projector.on();

        screen.down();

        amplifier.on();

        amplifier.setInput("DVD");

        dvdPlayer.on();

        dvdPlayer.play();

        lights.dim();
    }

    public void endMovie() {

        dvdPlayer.stop();

        projector.off();

        amplifier.off();

        screen.up();

        lights.on();
    }
}
```

Now the client simply does:

```java
homeTheater.watchMovie();
```

and:

```java
homeTheater.endMovie();
```

## 9. Real-Time Example — Banking Application

Consider a bank application.

A customer wants to transfer money.

Internally, many operations may happen:

```
Money Transfer
      |
      +── Validate Account
      |
      +── Check Balance
      |
      +── Fraud Check
      |
      +── Debit Account
      |
      +── Credit Account
      |
      +── Record Transaction
      |
      +── Send Notification
```

Instead of exposing all these operations to the client:

```java
accountService.validate();

balanceService.check();

fraudService.check();

debitService.debit();

creditService.credit();

transactionService.record();

notificationService.send();
```

we can provide:

```java
bankingFacade.transferMoney(
        fromAccount,
        toAccount,
        amount
);
```

The Facade coordinates everything.

## 10. Real-Time Example — Spring Boot Application

Facade is also commonly useful in backend applications.

Imagine a REST controller:

```java
@RestController
class OrderController {

    private OrderService orderService;
    private PaymentService paymentService;
    private InventoryService inventoryService;
    private NotificationService notificationService;

    @PostMapping("/orders")
    public void createOrder() {

        inventoryService.check();

        paymentService.pay();

        orderService.create();

        notificationService.send();
    }
}
```

The controller now knows too much about business workflow.

Instead:

```java
@RestController
class OrderController {

    private OrderFacade orderFacade;

    @PostMapping("/orders")
    public void createOrder() {

        orderFacade.placeOrder();
    }
}
```

And:

```java
class OrderFacade {

    public void placeOrder() {

        inventoryService.check();

        paymentService.pay();

        orderService.create();

        notificationService.send();
    }
}
```

Now:

```
Controller
    |
    ↓
OrderFacade
    |
    +── InventoryService
    +── PaymentService
    +── OrderService
    +── NotificationService
```

This keeps the controller simpler.

## 11. Facade Does NOT Hide the Subsystems Completely

This is an important point.

Facade provides a simple interface, but it doesn't necessarily prevent clients from accessing subsystem classes.

For example:

```java
OrderFacade facade =
        new OrderFacade();

facade.placeOrder();
```

is the preferred/simple way.

But the underlying services may still exist:

```java
PaymentService paymentService =
        new PaymentService();
```

So:

Facade simplifies access; it doesn't necessarily make the subsystem inaccessible.

## 12. Facade vs Adapter

These two patterns are commonly confused.

### Facade

Facade simplifies a complex subsystem.

```
Client
  |
  ↓
Facade
  |
  +── A
  +── B
  +── C
```

Question:

How can I make this complex system easier to use?

### Adapter

Adapter makes incompatible interfaces work together.

```
Client
  |
  ↓
Adapter
  |
  ↓
Existing Class
```

Question:

How can I make these two incompatible interfaces work together?

### Easy difference:

```
Facade  → Simplifies

Adapter → Converts
```

## 13. Facade vs Proxy

Another common interview question.

### Facade

Provides a simpler interface to a subsystem.

```
Client
 ↓
Facade
 ↓
Complex System
```

### Proxy

Acts as a substitute or representative for another object.

```
Client
 ↓
Proxy
 ↓
Real Object
```

Proxy can be used for:

- Access control
- Lazy loading
- Caching
- Remote access

So:

```
Facade → Simplification

Proxy → Controlled/substitute access
```

## 14. Facade vs Command

Since you've studied Command:

### Command

Turns a request into an object.

```
Remote
 ↓
Command
 ↓
TV
```

### Facade

Simplifies interaction with multiple components.

```
Client
 ↓
Facade
 ↓
Service A
Service B
Service C
```

Easy way:

```
Command → "Perform this action"

Facade  → "Make this complex system easy to use"
```

## 15. Facade vs Strategy

### Strategy

Allows you to switch between algorithms.

```
PaymentService
      |
      ↓
PaymentStrategy
   /    |    \
 UPI  Card  PayPal
```

### Facade

Hides a group of subsystem operations.

```
OrderFacade
   |
   +── Payment
   +── Inventory
   +── Shipping
```

Easy difference:

```
Strategy → Different ways of doing something

Facade   → Simple way of accessing many things
```

## 16. Advantages of Facade

### 1. Simplifies client code

Instead of:

```java
serviceA.method();
serviceB.method();
serviceC.method();
serviceD.method();
```

you have:

```java
facade.execute();
```

### 2. Reduces coupling

The client doesn't need to know every subsystem.

```
Client → Facade
```

instead of:

```
Client → 10 different services
```

### 3. Hides complexity

The business workflow is encapsulated inside the Facade.

### 4. Easier to use

Developers only need to learn the Facade's public API for common workflows.

### 5. Centralizes workflow

For example:

`placeOrder()`

can coordinate:

- Inventory
- Payment
- Order
- Shipping
- Notification

## 17. Disadvantages

Facade shouldn't become a God Class.

Bad design:

```
HugeFacade
   |
   +── 100 operations
   +── 200 business rules
   +── Database logic
   +── Payment logic
   +── Notification logic
```

The Facade should primarily coordinate the subsystem.

It shouldn't contain all the actual business logic.

A good Facade looks more like:

```
Facade
  |
  ↓
Coordinate services
  |
  +── Service A
  +── Service B
  +── Service C
```

## 18. Facade and SOLID Principles

Facade can help with Single Responsibility at the client level.

For example, instead of a controller coordinating:

- Payment
- Inventory
- Shipping
- Notification

the controller delegates the workflow:

```
Controller
    ↓
Facade
```

The controller now has a simpler responsibility:

Handle the request and delegate the use case.

## 19. Complete Architecture

Remember this diagram:

```
                    CLIENT
                       |
                       |
                simple method
                       |
                       ↓
                +-------------+
                |   FACADE    |
                +-------------+
                       |
          +------------+------------+
          |            |            |
          ↓            ↓            ↓
      Subsystem A  Subsystem B  Subsystem C
          |            |            |
          ↓            ↓            ↓
      Complex       Complex      Complex
      Operations    Operations   Operations
```

The client sees:

`Facade`

The Facade handles:

Complex subsystem interaction

## 20. Interview Answer

If the interviewer asks:

**What is Facade Design Pattern?**

You can say:

Facade is a structural design pattern that provides a simple and unified interface to a set of complex subsystem classes. It hides the complexity of the subsystem from the client and reduces the client's dependency on multiple classes.

**Real-time example:**

In an e-commerce application, placing an order may require customer validation, inventory checking, payment processing, order creation, shipping, and notification. Instead of the client interacting with each service individually, an OrderFacade can expose a simple placeOrder() method and internally coordinate all these services.

## 21. Easy Way to Remember

Think about a hotel receptionist.

You tell the receptionist:

"I want to check in."

You don't personally go to:

- Room Service
- Payment Department
- Booking System
- Housekeeping
- Security

The receptionist coordinates the required systems.

```
                  YOU
                   |
                   ↓
             RECEPTIONIST
               (Facade)
                   |
       +-----------+-----------+
       |           |           |
       ↓           ↓           ↓
   Booking      Payment    Room Service
    System       System       System
```

**One-line memory trick:**

> Facade Pattern = One simple interface hiding many complex operations.

Remember:

```
Facade
  ↓
Simplifies
  ↓
Complex subsystem
```

**Facade = "Don't make the client deal with all the complexity; give it one easy entry point."**
