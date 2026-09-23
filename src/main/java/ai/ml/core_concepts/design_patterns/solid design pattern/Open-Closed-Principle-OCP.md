# Open/Closed Principle (OCP)

The Open/Closed Principle is the O in SOLID.

> Software entities such as classes, modules, and functions should be open for extension but closed for modification.

In simple words:

> You should be able to add new functionality without changing existing, tested code.

---

## 1. What does "Open for Extension" mean?

It means you should be able to add new behavior.

For example, suppose your application supports:

- UPI
- Credit Card
- PayPal

Tomorrow the business asks:

"Add Apple Pay."

Your design should allow you to add Apple Pay without modifying the existing UPI, Credit Card, and PayPal code.

That's:

```
Open for extension
        ↓
Add new functionality
```

---

## 2. What does "Closed for Modification" mean?

It means:

Once existing code is tested and working, we should avoid modifying it every time a new requirement comes.

For example, suppose you have:

```java
class PaymentService {


    public void pay(String type) {


        if (type.equals("UPI")) {
            // UPI payment
        }
        else if (type.equals("CARD")) {
            // Card payment
        }
        else if (type.equals("PAYPAL")) {
            // PayPal payment
        }
    }
}
```

Now business asks for Apple Pay.

You have to modify:

```java
else if (type.equals("APPLE_PAY")) {
    // Apple Pay
}
```

Then another request:

- Google Pay
- Amazon Pay
- Bank Transfer
- Crypto

You keep modifying the same class.

❌ This violates OCP.

---

## 3. Real-time example — Payment System

Let's take an e-commerce application.

Initially:

```
Payment
 ├── UPI
 ├── Credit Card
 └── PayPal
```

Later:

```
Payment
 ├── UPI
 ├── Credit Card
 ├── PayPal
 ├── Apple Pay       ← New
 └── Google Pay      ← New
```

We want to add new payment methods without changing existing payment logic.

---

## 4. ❌ Bad Design — Violates OCP

```java
class PaymentService {


    public void pay(String paymentType,
                    double amount) {


        if (paymentType.equals("UPI")) {


            System.out.println(
                    "Paying ₹" + amount + " using UPI"
            );


        } else if (paymentType.equals("CARD")) {


            System.out.println(
                    "Paying ₹" + amount + " using Card"
            );


        } else if (paymentType.equals("PAYPAL")) {


            System.out.println(
                    "Paying ₹" + amount + " using PayPal"
            );
        }
    }
}
```

Usage:

```java
PaymentService service =
        new PaymentService();


service.pay("UPI", 5000);
```

Works fine.

But now we need Apple Pay.

We must modify:

```
PaymentService
```

```java
else if (paymentType.equals("APPLE_PAY")) {
    // Apple Pay
}
```

❌ Existing class has to change for every new payment type.

---

## 5. Why is this dangerous?

Imagine PaymentService has been tested extensively.

```
PaymentService
      |
      +── UPI
      +── Card
      +── PayPal
      +── Refund
      +── Validation
      +── Logging
      +── Security
```

You add Apple Pay and modify the class.

Now:

```
New change
    ↓
Existing code modified
    ↓
Existing functionality might break
    ↓
More regression testing
```

This is exactly what OCP tries to minimize.

---

## 6. ✅ Good Design — Following OCP

Create an abstraction:

```java
interface Payment {


    void pay(double amount);
}
```

Now each payment type implements the interface.

### UPI

```java
class UPIPayment implements Payment {


    @Override
    public void pay(double amount) {


        System.out.println(
                "Paying ₹" + amount + " using UPI"
        );
    }
}
```

### Credit Card

```java
class CreditCardPayment implements Payment {


    @Override
    public void pay(double amount) {


        System.out.println(
                "Paying ₹" + amount + " using Credit Card"
        );
    }
}
```

### PayPal

```java
class PayPalPayment implements Payment {


    @Override
    public void pay(double amount) {


        System.out.println(
                "Paying ₹" + amount + " using PayPal"
        );
    }
}
```

Now our service:

```java
class PaymentService {


    public void pay(
            Payment payment,
            double amount) {


        payment.pay(amount);
    }
}
```

Notice something important:

PaymentService doesn't know about:

- UPI
- Card
- PayPal

It only knows:

- Payment

---

## 7. Adding Apple Pay

Now business says:

> Add Apple Pay.

Create a new class:

```java
class ApplePayPayment implements Payment {


    @Override
    public void pay(double amount) {


        System.out.println(
                "Paying ₹" + amount + " using Apple Pay"
        );
    }
}
```

That's it.

We didn't modify:

- UPIPayment
- CreditCardPayment
- PayPalPayment
- PaymentService

We extended the system.

That's OCP.

---

## 8. Complete example

```java
interface Payment {


    void pay(double amount);
}
```

```java
class UPIPayment implements Payment {


    @Override
    public void pay(double amount) {


        System.out.println(
                "Paying ₹" + amount + " using UPI"
        );
    }
}
```

```java
class CreditCardPayment implements Payment {


    @Override
    public void pay(double amount) {


        System.out.println(
                "Paying ₹" + amount + " using Credit Card"
        );
    }
}
```

```java
class PayPalPayment implements Payment {


    @Override
    public void pay(double amount) {


        System.out.println(
                "Paying ₹" + amount + " using PayPal"
        );
    }
}
```

```java
class ApplePayPayment implements Payment {


    @Override
    public void pay(double amount) {


        System.out.println(
                "Paying ₹" + amount + " using Apple Pay"
        );
    }
}
```

Service:

```java
class PaymentService {


    public void pay(
            Payment payment,
            double amount) {


        payment.pay(amount);
    }
}
```

Client:

```java
public class Main {


    public static void main(String[] args) {


        PaymentService service =
                new PaymentService();


        Payment payment =
                new UPIPayment();


        service.pay(payment, 5000);


        payment =
                new ApplePayPayment();


        service.pay(payment, 5000);
    }
}
```

Output:

```
Paying ₹5000.0 using UPI
Paying ₹5000.0 using Apple Pay
```

The important thing is that PaymentService did not change when Apple Pay was introduced.

---

## 9. Visualizing OCP

### ❌ Without OCP

```
                 PaymentService
                       |
          +------------+------------+
          |            |            |
         UPI          Card        PayPal
                       |
                 Add Apple Pay
                       |
                       v
              MODIFY PaymentService
```

Every new payment type requires modifying existing code.

### ✅ With OCP

```
                    Payment
                       ↑
          +------------+------------+
          |            |            |
         UPI          Card        PayPal
                                   
                       +
                       |
                 Apple Pay
                       |
                       v
                NEW CLASS
```

We extend the system rather than modifying existing implementations.

---

## 10. Another real-time example — Discount System

Suppose an e-commerce company has different discount types:

- Regular Customer
- Premium Customer
- VIP Customer

### ❌ Bad design

```java
class DiscountCalculator {


    public double calculate(
            String customerType,
            double amount) {


        if (customerType.equals("REGULAR")) {
            return amount * 0.05;
        }


        if (customerType.equals("PREMIUM")) {
            return amount * 0.10;
        }


        if (customerType.equals("VIP")) {
            return amount * 0.20;
        }


        return 0;
    }
}
```

Now the business says:

> Add Employee Discount.

You modify:

```java
if (customerType.equals("EMPLOYEE")) {
    return amount * 0.30;
}
```

Again, the existing class changes.

❌ OCP violation.

---

## 11. Apply OCP to Discount

Create an abstraction:

```java
interface Discount {


    double calculate(double amount);
}
```

Regular:

```java
class RegularDiscount implements Discount {


    public double calculate(double amount) {


        return amount * 0.05;
    }
}
```

Premium:

```java
class PremiumDiscount implements Discount {


    public double calculate(double amount) {


        return amount * 0.10;
    }
}
```

VIP:

```java
class VIPDiscount implements Discount {


    public double calculate(double amount) {


        return amount * 0.20;
    }
}
```

Now add Employee:

```java
class EmployeeDiscount implements Discount {


    public double calculate(double amount) {


        return amount * 0.30;
    }
}
```

No existing discount class needs modification.

That's OCP.

---

## 12. Real-time example — Notification System

Imagine:

```
Notification
 ├── Email
 ├── SMS
 └── Push
```

You might initially write:

```java
class NotificationService {


    public void send(
            String type,
            String message) {


        if (type.equals("EMAIL")) {
            // Email
        }
        else if (type.equals("SMS")) {
            // SMS
        }
        else if (type.equals("PUSH")) {
            // Push
        }
    }
}
```

❌ Adding WhatsApp requires modifying NotificationService.

Instead:

```java
interface Notification {


    void send(String message);
}
```

Implement:

- EmailNotification
- SMSNotification
- PushNotification
- WhatsAppNotification

Each class implements:

```
Notification
```

Now adding WhatsApp means:

```
Create WhatsAppNotification
             ↓
Implement Notification
```

Existing code stays untouched.

---

## 13. OCP and Polymorphism

OCP is often achieved using polymorphism.

Instead of:

```java
if (type == "UPI") {
    ...
}
else if (type == "CARD") {
    ...
}
```

we use:

```java
Payment payment;
payment.pay(amount);
```

Java determines which implementation to execute:

```
Payment
   ↑
   |
   +---- UPIPayment
   |
   +---- CardPayment
   |
   +---- PayPalPayment
```

This allows new implementations to be added without changing the code that uses the abstraction.

---

## 14. OCP does NOT mean "never modify code"

This is an important interview point.

OCP does not mean:

> "Existing code must never be changed."

That's impossible in real software development.

It means:

> Design the parts of the system that are likely to change so that new behavior can be added with minimal modification to stable existing code.

For example, if a completely new business requirement changes the core domain model, you may obviously need to modify existing classes.

The goal is to reduce unnecessary modifications and regression risk.

---

## 15. OCP and Strategy Pattern

You will often see OCP implemented using the Strategy Design Pattern.

For example:

```java
interface PaymentStrategy {


    void pay(double amount);
}
```

Implementations:

- UPIPaymentStrategy
- CardPaymentStrategy
- PayPalPaymentStrategy

Then:

```java
class PaymentService {


    private PaymentStrategy strategy;


    public PaymentService(
            PaymentStrategy strategy) {


        this.strategy = strategy;
    }


    public void pay(double amount) {


        strategy.pay(amount);
    }
}
```

Adding a new payment strategy doesn't require changing PaymentService.

Therefore:

```
Strategy Pattern
       ↓
Often helps achieve
       ↓
Open/Closed Principle
```

---

## 16. OCP and Factory

Factory can also help with object creation:

```java
Payment payment =
        PaymentFactory.createPayment("UPI");
```

But be careful: a Factory containing a huge if/else or switch that must be edited for every new type can itself become an OCP violation.

So OCP is about the overall design, not simply "put the new keyword inside a Factory."

---

## 17. OCP vs SRP

Since you just learned SRP, this distinction is important.

### SRP

One class should have one responsibility / one reason to change.

Example:

```
PaymentService
    ↓
Payment processing
```

### OCP

New behavior should be added by extension rather than repeatedly modifying stable existing code.

Example:

```
Payment
   ↑
   +── UPI
   +── Card
   +── PayPal
   +── Apple Pay ← Add
```

Easy way:

```
SRP → Keep responsibilities separate.


OCP → Make changing behavior easy to extend.
```

---

## 18. Interview answer

If the interviewer asks:

> What is Open/Closed Principle?

A strong answer:

> The Open/Closed Principle states that software entities should be open for extension but closed for modification. In practice, we should design stable parts of the application so that new behavior can be added through new implementations or extensions rather than repeatedly changing existing, tested code.

Real-time example:

> In an e-commerce payment system, instead of having a PaymentService with if-else statements for UPI, Card, PayPal, and Apple Pay, we can create a Payment interface and separate implementations for each payment method. When Apple Pay is introduced, we add ApplePayPayment without modifying the existing payment implementations or payment-processing logic.

---

## 19. Easy way to remember OCP

Think of a phone charger.

Suppose your system supports different chargers through a standard interface:

```
             Charger Interface
                    ↑
        +-----------+-----------+
        |           |           |
       USB-C      Lightning   Micro-USB
```

Tomorrow you add another charger:

```
             Charger Interface
                    ↑
        +-----------+-----------+-----------+
        |           |           |           |
       USB-C      Lightning   Micro-USB    New
```

You extend the system by adding a new implementation.

You don't need to redesign the existing charger implementations.

### Remember:

```
OPEN
  ↓
Open for adding new behavior


CLOSED
  ↓
Avoid modifying stable existing behavior
```

> OCP = "Add new behavior without breaking into the old code every time."
