# Dependency Inversion Principle (DIP)

The Dependency Inversion Principle is the D in SOLID.

> High-level modules should not depend on low-level modules. Both should depend on abstractions.

And:

> Abstractions should not depend on details. Details should depend on abstractions.

In simple words:

> Don't make your important business logic directly dependent on concrete implementation classes. Depend on interfaces/abstractions instead.

---

## 1. What is a Dependency?

Suppose we have:

```java
class OrderService {


    private MySQLDatabase database =
            new MySQLDatabase();


}
```

OrderService depends on:

```
MySQLDatabase
```

So:

```
OrderService
     |
     ↓
MySQLDatabase
```

If tomorrow we change:

```
MySQL → PostgreSQL
```

we have to modify OrderService.

That's tight coupling.

---

## 2. Real-Time Example — E-commerce Order

Imagine an e-commerce application.

When an order is created:

```
Customer
   ↓
OrderService
   ↓
Save Order
   ↓
Database
```

A beginner might write:

```java
class OrderService {


    private MySQLDatabase database =
            new MySQLDatabase();


    public void createOrder() {


        // Create order


        database.save();
    }
}
```

And:

```java
class MySQLDatabase {


    public void save() {


        System.out.println(
                "Saving order in MySQL"
        );
    }
}
```

This works.

But there is a problem.

---

## 3. ❌ Problem — Tight Coupling

The high-level class:

```
OrderService
```

directly depends on:

```
MySQLDatabase
```

Diagram:

```
       OrderService
            |
            ↓
      MySQLDatabase
```

Suppose the company changes the database:

```
MySQL
  ↓
PostgreSQL
```

We need to modify:

```java
class OrderService {


    private PostgreSQLDatabase database =
            new PostgreSQLDatabase();
}
```

That's undesirable.

---

## 4. Apply Dependency Inversion

Create an abstraction:

```java
interface OrderRepository {


    void save();
}
```

Now MySQL implements it:

```java
class MySQLOrderRepository
        implements OrderRepository {


    @Override
    public void save() {


        System.out.println(
                "Saving order in MySQL"
        );
    }
}
```

PostgreSQL can implement the same interface:

```java
class PostgreSQLOrderRepository
        implements OrderRepository {


    @Override
    public void save() {


        System.out.println(
                "Saving order in PostgreSQL"
        );
    }
}
```

Now OrderService depends on the abstraction:

```java
class OrderService {


    private OrderRepository repository;


    public OrderService(
            OrderRepository repository) {


        this.repository = repository;
    }


    public void createOrder() {


        System.out.println(
                "Creating order"
        );


        repository.save();
    }
}
```

Now the dependency looks like:

```
                  OrderRepository
                 /               \
                /                 \
               ↓                   ↓
 MySQLOrderRepository     PostgreSQLOrderRepository
                ↑
                |
          OrderService
```

More precisely:

```
          OrderService
               |
               ↓
       OrderRepository
          ↑          ↑
          |          |
       MySQL      PostgreSQL
```

The important point is:

> OrderService does not know which database implementation it is using.

---

## 5. Complete Example

### Interface

```java
interface OrderRepository {


    void save();
}
```

### MySQL

```java
class MySQLOrderRepository
        implements OrderRepository {


    @Override
    public void save() {


        System.out.println(
                "Order saved in MySQL"
        );
    }
}
```

### PostgreSQL

```java
class PostgreSQLOrderRepository
        implements OrderRepository {


    @Override
    public void save() {


        System.out.println(
                "Order saved in PostgreSQL"
        );
    }
}
```

### Service

```java
class OrderService {


    private final OrderRepository repository;


    public OrderService(
            OrderRepository repository) {


        this.repository = repository;
    }


    public void createOrder() {


        System.out.println(
                "Creating order..."
        );


        repository.save();
    }
}
```

### Main

```java
public class Main {


    public static void main(String[] args) {


        OrderRepository repository =
                new MySQLOrderRepository();


        OrderService service =
                new OrderService(repository);


        service.createOrder();
    }
}
```

Output:

```
Creating order...
Order saved in MySQL
```

Now change to PostgreSQL:

```java
OrderRepository repository =
        new PostgreSQLOrderRepository();


OrderService service =
        new OrderService(repository);
```

Output:

```
Creating order...
Order saved in PostgreSQL
```

OrderService itself did not change.

That's Dependency Inversion.

---

## 6. Why is this called "Dependency Inversion"?

Normally, we might have:

```
High-Level Module
       ↓
Low-Level Module
```

For example:

```
OrderService
       ↓
MySQLDatabase
```

With DIP:

```
High-Level Module
       ↓
   Abstraction
       ↑
Low-Level Module
```

So:

```
       OrderService
            |
            ↓
     OrderRepository
            ↑
            |
     MySQLRepository
```

Both sides depend on the abstraction.

---

## 7. Real-Time Example — Payment Gateway

This is a very common real-world example.

Imagine your e-commerce application initially uses:

- Razorpay

Your code might look like:

```java
class PaymentService {


    private RazorpayPayment payment =
            new RazorpayPayment();


    public void processPayment(
            double amount) {


        payment.pay(amount);
    }
}
```

Problem:

```
PaymentService
       ↓
RazorpayPayment
```

Now the business says:

"We want to support Stripe."

You have to modify PaymentService.

That's tight coupling.

---

## 8. Apply DIP to Payment

Create:

```java
interface PaymentGateway {


    void pay(double amount);
}
```

Razorpay:

```java
class RazorpayGateway
        implements PaymentGateway {


    @Override
    public void pay(double amount) {


        System.out.println(
                "Payment using Razorpay: ₹"
                        + amount
        );
    }
}
```

Stripe:

```java
class StripeGateway
        implements PaymentGateway {


    @Override
    public void pay(double amount) {


        System.out.println(
                "Payment using Stripe: ₹"
                        + amount
        );
    }
}
```

Payment service:

```java
class PaymentService {


    private final PaymentGateway gateway;


    public PaymentService(
            PaymentGateway gateway) {


        this.gateway = gateway;
    }


    public void processPayment(
            double amount) {


        gateway.pay(amount);
    }
}
```

Now:

```java
PaymentGateway gateway =
        new RazorpayGateway();


PaymentService service =
        new PaymentService(gateway);


service.processPayment(5000);
```

Later:

```java
PaymentGateway gateway =
        new StripeGateway();


PaymentService service =
        new PaymentService(gateway);


service.processPayment(5000);
```

No change to PaymentService.

---

## 9. This is also Dependency Injection

You may have noticed:

```java
public PaymentService(
        PaymentGateway gateway) {


    this.gateway = gateway;
}
```

We are injecting the dependency from outside.

This is called:

> Dependency Injection (DI)

DIP and DI are related, but they are not the same thing.

### DIP

A design principle:

> Depend on abstractions, not concrete implementations.

### DI

A technique for providing dependencies:

```java
new PaymentService(
        new StripeGateway()
);
```

So:

```
DIP
 ↓
Design principle


DI
 ↓
Technique used to implement/manage dependencies
```

---

## 10. Spring Boot Example

This is where DIP becomes very practical.

Suppose:

```java
public interface PaymentGateway {


    void pay(double amount);
}
```

Implementation:

```java
@Service
public class StripePaymentGateway
        implements PaymentGateway {


    @Override
    public void pay(double amount) {


        System.out.println(
                "Payment processed using Stripe"
        );
    }
}
```

Service:

```java
@Service
public class PaymentService {


    private final PaymentGateway paymentGateway;


    public PaymentService(
            PaymentGateway paymentGateway) {


        this.paymentGateway =
                paymentGateway;
    }


    public void processPayment(
            double amount) {


        paymentGateway.pay(amount);
    }
}
```

Spring injects the implementation:

```
PaymentService
      |
      ↓
PaymentGateway
      ↑
      |
StripePaymentGateway
```

The service depends on the interface, not directly on Stripe.

---

## 11. Real-Time Example — Notification

Suppose an application sends notifications.

Initially:

```java
class NotificationService {


    private EmailService emailService =
            new EmailService();


    public void notifyUser(String message) {


        emailService.send(message);
    }
}
```

Problem:

```
NotificationService
       ↓
EmailService
```

What if we need:

- Email
- SMS
- WhatsApp
- Push Notification

We don't want to keep changing NotificationService.

---

## 12. Better Design

Create:

```java
interface NotificationSender {


    void send(String message);
}
```

Email:

```java
class EmailNotificationSender
        implements NotificationSender {


    public void send(String message) {


        System.out.println(
                "Sending Email: " + message
        );
    }
}
```

SMS:

```java
class SMSNotificationSender
        implements NotificationSender {


    public void send(String message) {


        System.out.println(
                "Sending SMS: " + message
        );
    }
}
```

WhatsApp:

```java
class WhatsAppNotificationSender
        implements NotificationSender {


    public void send(String message) {


        System.out.println(
                "Sending WhatsApp: " + message
        );
    }
}
```

Service:

```java
class NotificationService {


    private final NotificationSender sender;


    public NotificationService(
            NotificationSender sender) {


        this.sender = sender;
    }


    public void notifyUser(String message) {


        sender.send(message);
    }
}
```

Now:

```
                NotificationSender
                /        |        \
               /         |         \
            Email       SMS      WhatsApp
               ↑
               |
     NotificationService
```

---

## 13. Why is this useful?

Suppose tomorrow you switch:

```
Email provider
    ↓
SendGrid
```

You don't need to change:

```
NotificationService
```

You can create:

```java
class SendGridNotificationSender
        implements NotificationSender {


    public void send(String message) {
        // Send through SendGrid
    }
}
```

The business logic stays the same.

---

## 14. DIP improves testing

This is one of the biggest practical benefits.

Suppose your service directly depends on a real payment gateway:

```java
class PaymentService {


    private StripeGateway gateway =
            new StripeGateway();
}
```

Testing becomes difficult because your test may actually call Stripe.

Instead:

```java
interface PaymentGateway {


    void pay(double amount);
}
```

For production:

```
PaymentService
      ↓
StripeGateway
```

For testing:

```
PaymentService
      ↓
MockPaymentGateway
```

Example:

```java
class MockPaymentGateway
        implements PaymentGateway {


    public void pay(double amount) {


        System.out.println(
                "Mock payment successful"
        );
    }
}
```

Test:

```java
PaymentGateway gateway =
        new MockPaymentGateway();


PaymentService service =
        new PaymentService(gateway);


service.processPayment(5000);
```

No real payment is made.

That's a major advantage of DIP.

---

## 15. DIP vs OCP

These two principles are closely related.

### OCP

Open for extension, closed for modification.

Example:

- Add Stripe
- Add PayPal
- Add Razorpay

without changing PaymentService.

### DIP

PaymentService should depend on an abstraction.

```
PaymentService
      ↓
PaymentGateway
      ↑
      |
Stripe / Razorpay / PayPal
```

So:

> DIP helps us achieve OCP.

---

## 16. DIP vs Dependency Injection

This is a common interview question.

| DIP | Dependency Injection |
|---|---|
| SOLID principle | Design/implementation technique |
| Says depend on abstractions | Provides dependencies from outside |
| Focuses on coupling | Focuses on supplying dependencies |
| "What should I depend on?" | "How do I receive it?" |

Example:

### DIP

```java
private PaymentGateway gateway;
```

Rather than:

```java
private StripeGateway gateway;
```

### Dependency Injection

```java
public PaymentService(
        PaymentGateway gateway) {


    this.gateway = gateway;
}
```

---

## 17. A very important distinction

DIP does not mean:

> "Every class must have an interface."

That's overengineering.

For example:

```java
class TaxCalculator {


    public double calculate(double amount) {
        return amount * 0.18;
    }
}
```

There may be no need to create:

```
ITaxCalculator
TaxCalculator
```

just for the sake of DIP.

Use abstractions where there is a meaningful dependency boundary, especially when:

- implementations can vary,
- external systems are involved,
- business logic shouldn't depend on infrastructure,
- testing benefits from substituting implementations.

---

## 18. High-Level vs Low-Level Modules

This is important for interviews.

**High-level module**

Contains business logic.

Example:

- OrderService
- PaymentService
- CheckoutService

**Low-level module**

Contains implementation/infrastructure details.

Example:

- MySQL
- Stripe
- SMTP
- Kafka
- AWS S3

Without DIP:

```
High-Level
    ↓
Low-Level
```

With DIP:

```
          Abstraction
          ↑         ↑
          |         |
   High-Level    Low-Level
```

The business logic doesn't need to know the infrastructure details.

---

## 19. Real-world architecture

A typical Spring Boot application might look like:

```
              Controller
                  |
                  ↓
             OrderService
                  |
                  ↓
          OrderRepository
                  ↑
                  |
        JpaOrderRepository
                  |
                  ↓
               MySQL
```

The important dependency boundary is:

```
OrderService
     ↓
OrderRepository
     ↑
     |
JPA implementation
     ↓
   MySQL
```

The high-level business logic depends on an abstraction rather than directly on the database technology.

---

## 20. All SOLID principles together

You've now covered the complete SOLID set:

| Principle | Meaning | Easy memory |
|---|---|---|
| S — SRP | One responsibility | One job |
| O — OCP | Open for extension, closed for modification | Extend |
| L — LSP | Subtypes should be substitutable | Replace safely |
| I — ISP | Don't force unused methods | Small interfaces |
| D — DIP | Depend on abstractions | Depend on interfaces |

A useful way to remember them:

```
S → Keep classes focused
O → Extend instead of modifying
L → Child must safely replace parent
I → Keep interfaces small
D → Depend on abstractions
```

---

## 21. Interview Answer

If asked:

> What is Dependency Inversion Principle?

A strong answer is:

> The Dependency Inversion Principle states that high-level modules should not depend directly on low-level modules. Both should depend on abstractions. It also states that abstractions should not depend on implementation details; rather, implementation details should depend on abstractions.

Real-time example:

> In an e-commerce application, PaymentService should not directly depend on StripeGateway or RazorpayGateway. Instead, we create a PaymentGateway interface. Stripe, Razorpay, and other gateways implement that interface, and PaymentService depends only on PaymentGateway. This allows us to switch payment providers without changing the business logic and also makes unit testing easier.

### One-line memory trick:

> DIP = High-level business logic should depend on abstractions, not concrete implementation details.
