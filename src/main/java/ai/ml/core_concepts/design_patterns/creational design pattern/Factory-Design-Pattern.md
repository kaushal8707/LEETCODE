# Factory Design Pattern

The Factory Design Pattern is a Creational Design Pattern used to create objects without exposing the object-creation logic to the client.

## Simple definition

Factory Pattern provides a common way to create objects while hiding which concrete class is actually instantiated.

The client asks the factory for an object:

```java
Payment payment = PaymentFactory.createPayment("UPI");
```

instead of directly doing:

```java
Payment payment = new UPIPayment();
```

---

## 1. Real-time example — Payment System

Imagine an e-commerce application.

Customers can pay using:

- UPI
- Credit Card
- PayPal

We have:

```
                Payment
                   |
        +----------+----------+
        |          |          |
        v          v          v
      UPI        Card       PayPal
```

The application should not have new scattered everywhere:

```java
new UPIPayment();
new CreditCardPayment();
new PayPalPayment();
```

Instead, we create a PaymentFactory.

---

## 2. Step 1 — Create the common interface

```java
interface Payment {


    void pay(double amount);
}
```

This is our common abstraction.

Every payment implementation must provide:

- pay()

---

## 3. Step 2 — Create concrete classes

### UPI Payment

```java
class UPIPayment implements Payment {


    @Override
    public void pay(double amount) {
        System.out.println(
                "Payment of ₹" + amount + " made using UPI"
        );
    }
}
```

### Credit Card Payment

```java
class CreditCardPayment implements Payment {


    @Override
    public void pay(double amount) {
        System.out.println(
                "Payment of ₹" + amount + " made using Credit Card"
        );
    }
}
```

### PayPal Payment

```java
class PayPalPayment implements Payment {


    @Override
    public void pay(double amount) {
        System.out.println(
                "Payment of ₹" + amount + " made using PayPal"
        );
    }
}
```

Now we have:

```
Payment
   |
   +---- UPIPayment
   |
   +---- CreditCardPayment
   |
   +---- PayPalPayment
```

---

## 4. Step 3 — Create the Factory

Now comes the important part.

```java
class PaymentFactory {


    public static Payment createPayment(String type) {


        if (type.equalsIgnoreCase("UPI")) {
            return new UPIPayment();
        }


        if (type.equalsIgnoreCase("CARD")) {
            return new CreditCardPayment();
        }


        if (type.equalsIgnoreCase("PAYPAL")) {
            return new PayPalPayment();
        }


        throw new IllegalArgumentException(
                "Invalid payment type: " + type
        );
    }
}
```

The Factory is responsible for deciding:

> Which concrete Payment object should be created?

---

## 5. Step 4 — Client code

Now our client doesn't need to know about:

```java
new UPIPayment();
new CreditCardPayment();
new PayPalPayment();
```

Instead:

```java
public class Main {


    public static void main(String[] args) {


        Payment payment =
                PaymentFactory.createPayment("UPI");


        payment.pay(5000);
    }
}
```

Output:

```
Payment of ₹5000.0 made using UPI
```

---

## 6. What happens internally?

When we write:

```java
Payment payment =
        PaymentFactory.createPayment("UPI");
```

the following happens:

```
                 Client
                   |
                   v
          PaymentFactory
                   |
             type = "UPI"
                   |
                   v
            UPIPayment
                   |
                   v
              Payment
```

The client only knows:

- Payment

It doesn't need to know:

- UPIPayment

---

## 7. What if we want Credit Card?

Simply change:

```java
Payment payment =
        PaymentFactory.createPayment("CARD");
```

The Factory returns:

```java
new CreditCardPayment();
```

Then:

```java
payment.pay(5000);
```

Output:

```
Payment of ₹5000.0 made using Credit Card
```

The client code remains almost identical.

---

## 8. Complete Example

```java
interface Payment {


    void pay(double amount);
}




class UPIPayment implements Payment {


    @Override
    public void pay(double amount) {
        System.out.println(
                "Payment of ₹" + amount + " made using UPI"
        );
    }
}




class CreditCardPayment implements Payment {


    @Override
    public void pay(double amount) {
        System.out.println(
                "Payment of ₹" + amount + " made using Credit Card"
        );
    }
}




class PayPalPayment implements Payment {


    @Override
    public void pay(double amount) {
        System.out.println(
                "Payment of ₹" + amount + " made using PayPal"
        );
    }
}




class PaymentFactory {


    public static Payment createPayment(String type) {


        if (type.equalsIgnoreCase("UPI")) {
            return new UPIPayment();
        }


        if (type.equalsIgnoreCase("CARD")) {
            return new CreditCardPayment();
        }


        if (type.equalsIgnoreCase("PAYPAL")) {
            return new PayPalPayment();
        }


        throw new IllegalArgumentException(
                "Invalid payment type"
        );
    }
}




public class Main {


    public static void main(String[] args) {


        Payment payment =
                PaymentFactory.createPayment("UPI");


        payment.pay(5000);
    }
}
```

---

## 9. Why do we need Factory?

Consider code without Factory:

```java
class OrderService {


    public void makePayment(String type) {


        Payment payment;


        if (type.equals("UPI")) {
            payment = new UPIPayment();
        }
        else if (type.equals("CARD")) {
            payment = new CreditCardPayment();
        }
        else if (type.equals("PAYPAL")) {
            payment = new PayPalPayment();
        }


        // payment processing
    }
}
```

Now OrderService knows about:

- UPIPayment
- CreditCardPayment
- PayPalPayment

This creates tight coupling.

With Factory:

```java
class OrderService {


    public void makePayment(String type) {


        Payment payment =
                PaymentFactory.createPayment(type);


        payment.pay(5000);
    }
}
```

Now:

```
OrderService
      |
      v
PaymentFactory
      |
      +---- UPIPayment
      +---- CreditCardPayment
      +---- PayPalPayment
```

The creation responsibility has been moved to the Factory.

---

## 10. Another real-time example — Notification System

This is another example you'll commonly see in real applications.

Suppose your application sends:

- Email
- SMS
- Push Notification

We create:

```java
interface Notification {


    void send(String message);
}
```

Email:

```java
class EmailNotification implements Notification {


    public void send(String message) {
        System.out.println(
                "Sending Email: " + message
        );
    }
}
```

SMS:

```java
class SMSNotification implements Notification {


    public void send(String message) {
        System.out.println(
                "Sending SMS: " + message
        );
    }
}
```

Push:

```java
class PushNotification implements Notification {


    public void send(String message) {
        System.out.println(
                "Sending Push Notification: " + message
        );
    }
}
```

Factory:

```java
class NotificationFactory {


    public static Notification createNotification(
            String type) {


        if (type.equalsIgnoreCase("EMAIL")) {
            return new EmailNotification();
        }


        if (type.equalsIgnoreCase("SMS")) {
            return new SMSNotification();
        }


        if (type.equalsIgnoreCase("PUSH")) {
            return new PushNotification();
        }


        throw new IllegalArgumentException(
                "Invalid notification type"
        );
    }
}
```

Client:

```java
Notification notification =
        NotificationFactory.createNotification("EMAIL");


notification.send("Your order has been shipped.");
```

Output:

```
Sending Email: Your order has been shipped.
```

This is a very realistic use case.

---

## 11. Factory Pattern structure

The basic structure is:

```
                    Client
                      |
                      v
                  Factory
                      |
              createProduct()
                      |
        +-------------+-------------+
        |             |             |
        v             v             v
    ProductA       ProductB      ProductC
        |             |             |
        +-------------+-------------+
                      |
                      v
                  Interface
```

For our payment example:

```
                     Client
                       |
                       v
                PaymentFactory
                       |
                createPayment()
                       |
          +------------+------------+
          |            |            |
          v            v            v
        UPI           Card        PayPal
          |            |            |
          +------------+------------+
                       |
                    Payment
```

---

## 12. Factory vs normal object creation

### Without Factory

```java
Payment payment = new UPIPayment();
```

The client knows the concrete class.

### With Factory

```java
Payment payment =
        PaymentFactory.createPayment("UPI");
```

The client only knows the abstraction:

- Payment

That's the main benefit.

---

## 13. Factory vs Abstract Factory

This is very important for interviews.

### Factory

Usually deals with creating one type of product.

```
PaymentFactory
      |
      +---- UPI
      +---- Card
      +---- PayPal
```

It answers:

> Which Payment object should I create?

### Abstract Factory

Creates a family of related products.

For example:

```
                 PaymentProviderFactory
                         |
             +-----------+-----------+
             |                       |
         Razorpay                  Stripe
             |                       |
       +-----+-----+           +-----+-----+
       |           |           |           |
    Payment      Refund      Payment      Refund
```

It answers:

> Which family of related objects should I create?

---

## 14. Factory vs Builder

Another common interview question.

### Factory

Used when:

The type of object needs to be selected.

```java
Payment payment =
        PaymentFactory.createPayment("UPI");
```

### Builder

Used when:

A complex object needs to be constructed step-by-step.

```java
Order order = new Order.Builder()
        .customer(customer)
        .address(address)
        .coupon("SAVE10")
        .paymentMethod("UPI")
        .build();
```

Remember:

```
Factory → WHICH object?
Builder  → HOW to construct the object?
```

---

## 15. Advantages of Factory Pattern

**1. Loose coupling**

Client depends on:

- Payment

rather than:

- UPIPayment

**2. Centralized object creation**

All creation logic is in:

- PaymentFactory

**3. Easier maintenance**

If object creation changes, you primarily modify the Factory.

**4. Easier to extend**

You can add:

- ApplePay
- GooglePay
- AmazonPay

without changing the client code.

---

## 16. One limitation

Look at this Factory:

```java
if (type.equals("UPI")) {
    return new UPIPayment();
}


if (type.equals("CARD")) {
    return new CreditCardPayment();
}


if (type.equals("PAYPAL")) {
    return new PayPalPayment();
}
```

As the number of products increases, the Factory can become very large.

For example:

- UPI
- CARD
- PAYPAL
- APPLE_PAY
- GOOGLE_PAY
- AMAZON_PAY
- BANK_TRANSFER
- CRYPTO
- ...

In larger systems, you can improve this using approaches such as:

- Factory Method
- Registry/Map-based factories
- Dependency Injection
- Strategy + Factory
- Spring's dependency injection/container

---

## 17. When should you use Factory?

Use Factory when:

**✅ There are multiple implementations of an interface.**

```
Payment
   ├── UPI
   ├── Card
   └── PayPal
```

**✅ The exact implementation depends on runtime input/configuration.**

```java
createPayment("UPI")
```

**✅ You want to hide object creation.**

**✅ You want the client to depend on an interface rather than concrete classes.**

---

## 18. Interview answer

If the interviewer asks:

> What is Factory Design Pattern?

You can answer:

> Factory is a creational design pattern that provides a centralized mechanism for creating objects without exposing the object-creation logic to the client. The client works with an abstraction, while the Factory decides which concrete implementation should be instantiated.

Then give the payment example:

> For example, in an e-commerce application, PaymentFactory can create UPIPayment, CreditCardPayment, or PayPalPayment based on the payment type. The client only works with the Payment interface and doesn't directly instantiate the concrete classes.

The easiest way to remember it:

```
new UPIPayment()
       ↓
Client knows the implementation ❌




PaymentFactory.createPayment("UPI")
       ↓
Factory knows the implementation ✅
       ↓
Client only knows Payment
```

> Factory = "You tell me WHAT you need; I'll decide WHICH concrete object to create."
