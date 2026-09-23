# Abstract Factory Design Pattern

The Abstract Factory Design Pattern is a creational pattern used when we need to create families of related objects without specifying their concrete classes.

The easiest way to remember it:

> Factory creates one type of object. Abstract Factory creates a family of related objects.

---

## 1. Real-time example: Payment System

Suppose you're building an e-commerce application.

Your application supports two payment providers:

- Razorpay
- Stripe

Each payment provider has multiple related components:

```
Razorpay
 ├── Payment
 └── Refund


Stripe
 ├── Payment
 └── Refund
```

We want to make sure that when we select Razorpay, we get both:

- RazorpayPayment
- RazorpayRefund

And when we select Stripe, we get:

- StripePayment
- StripeRefund

This is a perfect situation for Abstract Factory.

---

## 2. First create product interfaces

We have two types of products:

**Payment**

```java
interface Payment {


    void pay(double amount);
}
```

**Refund**

```java
interface Refund {


    void refund(double amount);
}
```

These interfaces define what every payment provider must support.

---

## 3. Create Razorpay products

```java
class RazorpayPayment implements Payment {


    @Override
    public void pay(double amount) {
        System.out.println(
            "Payment of ₹" + amount + " made using Razorpay"
        );
    }
}
```

And:

```java
class RazorpayRefund implements Refund {


    @Override
    public void refund(double amount) {
        System.out.println(
            "Refund of ₹" + amount + " processed using Razorpay"
        );
    }
}
```

So Razorpay gives us a family:

```
Razorpay
   |
   +-- RazorpayPayment
   |
   +-- RazorpayRefund
```

---

## 4. Create Stripe products

```java
class StripePayment implements Payment {


    @Override
    public void pay(double amount) {
        System.out.println(
            "Payment of ₹" + amount + " made using Stripe"
        );
    }
}
```

And:

```java
class StripeRefund implements Refund {


    @Override
    public void refund(double amount) {
        System.out.println(
            "Refund of ₹" + amount + " processed using Stripe"
        );
    }
}
```

Now Stripe gives us another family:

```
Stripe
   |
   +-- StripePayment
   |
   +-- StripeRefund
```

---

## 5. Create the Abstract Factory

Now we create an interface that knows how to create the family of products.

```java
interface PaymentFactory {


    Payment createPayment();


    Refund createRefund();
}
```

Notice something important.

The factory doesn't say:

```java
RazorpayPayment createPayment();
```

It says:

```java
Payment createPayment();
```

This is because the client should depend on the interface, not the concrete implementation.

---

## 6. Razorpay Factory

```java
class RazorpayFactory implements PaymentFactory {


    @Override
    public Payment createPayment() {
        return new RazorpayPayment();
    }


    @Override
    public Refund createRefund() {
        return new RazorpayRefund();
    }
}
```

This factory creates the complete Razorpay family.

```
RazorpayFactory
      |
      +----> RazorpayPayment
      |
      +----> RazorpayRefund
```

---

## 7. Stripe Factory

```java
class StripeFactory implements PaymentFactory {


    @Override
    public Payment createPayment() {
        return new StripePayment();
    }


    @Override
    public Refund createRefund() {
        return new StripeRefund();
    }
}
```

Now:

```
StripeFactory
      |
      +----> StripePayment
      |
      +----> StripeRefund
```

---

## 8. Client code

Now comes the most important part.

Our application doesn't need to know:

```java
new RazorpayPayment();
new RazorpayRefund();
```

or:

```java
new StripePayment();
new StripeRefund();
```

It only knows about:

`PaymentFactory`

Example:

```java
public class Main {


    public static void main(String[] args) {


        PaymentFactory factory = new RazorpayFactory();


        Payment payment = factory.createPayment();
        Refund refund = factory.createRefund();


        payment.pay(5000);
        refund.refund(1000);
    }
}
```

Output:

```
Payment of ₹5000.0 made using Razorpay
Refund of ₹1000.0 processed using Razorpay
```

---

## 9. What if we want Stripe?

Just change:

```java
PaymentFactory factory = new RazorpayFactory();
```

to:

```java
PaymentFactory factory = new StripeFactory();
```

Everything else remains the same.

```java
Payment payment = factory.createPayment();
Refund refund = factory.createRefund();


payment.pay(5000);
refund.refund(1000);
```

Output:

```
Payment of ₹5000.0 made using Stripe
Refund of ₹1000.0 processed using Stripe
```

This is the major benefit.

The client doesn't care which concrete implementation it is using.

---

## 10. Complete code

Putting everything together:

```java
interface Payment {
    @Override
    public void pay(double amount) {
        System.out.println(
            "Payment of ₹" + amount + " made using Stripe"
        );
    }
}


class StripeRefund implements Refund {


    @Override
    public void refund(double amount) {
        System.out.println(
            "Refund of ₹" + amount + " processed using Stripe"
        );
    }
}


// Abstract Factory


interface PaymentFactory {


    Payment createPayment();


    Refund createRefund();
}


// Razorpay Factory


class RazorpayFactory implements PaymentFactory {


    @Override
    public Payment createPayment() {
        return new RazorpayPayment();
    }


    @Override
    public Refund createRefund() {
        return new RazorpayRefund();
    }
}


// Stripe Factory


class StripeFactory implements PaymentFactory {


    @Override
    public Payment createPayment() {
        return new StripePayment();
    }


    @Override
    public Refund createRefund() {
        return new StripeRefund();
    }
}


// Client


public class Main {


    public static void main(String[] args) {


        PaymentFactory factory =
                new RazorpayFactory();


        Payment payment =
                factory.createPayment();


        Refund refund =
                factory.createRefund();


        payment.pay(5000);
        refund.refund(1000);
    }
}
```

---

## 11. Understanding the structure

The entire design looks like this:

```
                     PaymentFactory
                           |
              +------------+------------+
              |                         |
              v                         v
       createPayment()            createRefund()
              |                         |
              v                         v
          Payment                    Refund
              ^                         ^
              |                         |
       +------+-------+          +------+-------+
       |              |          |              |
       |              |          |              |
RazorpayPayment  StripePayment  RazorpayRefund  StripeRefund
```

Factories:

```
RazorpayFactory
       |
       +---- RazorpayPayment
       |
       +---- RazorpayRefund




StripeFactory
       |
       +---- StripePayment
       |
       +---- StripeRefund
```

The important concept is that each factory creates a consistent family.

---

## 12. Why not simply use Factory?

This is a very common interview question.

Suppose we use a normal Factory:

```java
Payment payment = PaymentFactory.create("RAZORPAY");
```

That's fine when we only need one product.

But now we have:

- Payment
- Refund
- Invoice
- Subscription

And each provider has its own implementation:

```
                Razorpay          Stripe
                   |                |
Payment       RazorpayPayment   StripePayment
Refund        RazorpayRefund    StripeRefund
Invoice       RazorpayInvoice   StripeInvoice
Subscription  RazorpaySub       StripeSub
```

Now we need to create a family of related objects.

That's where Abstract Factory becomes useful.

---

## 13. Factory vs Abstract Factory

### Factory

Think:

I need ONE product.

```
PaymentFactory
      |
      +-- UPI Payment
      +-- Card Payment
      +-- PayPal Payment
```

### Abstract Factory

Think:

I need a FAMILY of related products.

```
                  PaymentProviderFactory
                          |
             +------------+------------+
             |                         |
        Razorpay                   Stripe
             |                         |
       +-----+-----+             +-----+-----+
       |           |             |           |
    Payment      Refund        Payment      Refund
```

---

## 14. Another very common example: UI

A classic Abstract Factory example is a UI framework.

Suppose your application supports:

- Windows
- Mac

Each operating system needs:

- Button
- Checkbox
- Textbox

So:

```
WindowsFactory
   |
   +-- WindowsButton
   +-- WindowsCheckbox
   +-- WindowsTextbox
```

and:

```
MacFactory
   |
   +-- MacButton
   +-- MacCheckbox
   +-- MacTextbox
```

The application can simply say:

```java
GUIFactory factory = new WindowsFactory();


Button button = factory.createButton();
Checkbox checkbox = factory.createCheckbox();
```

It doesn't care about the concrete classes.

---

## 15. When should you use Abstract Factory?

Use Abstract Factory when:

**1. You have multiple related products**

For example:

- Payment
- Refund
- Invoice

**2. You have multiple families of those products**

For example:

- Razorpay
- Stripe
- PayPal

**3. You want to prevent mixing incompatible implementations**

You don't want:

```
RazorpayPayment
+
StripeRefund
```

when your business rules require them to belong to the same provider.

Instead:

```
RazorpayFactory
   |
   +-- RazorpayPayment
   +-- RazorpayRefund
```

**4. You want the client to depend on interfaces**

The client works with:

- Payment
- Refund
- PaymentFactory

rather than:

- RazorpayPayment
- RazorpayRefund

---

## 16. Interview definition

If an interviewer asks:

> What is Abstract Factory Design Pattern?

A good answer is:

> Abstract Factory is a creational design pattern that provides an interface for creating families of related or dependent objects without specifying their concrete classes.

Then give the example:

> For example, in a payment system, RazorpayFactory can create RazorpayPayment and RazorpayRefund, while StripeFactory can create StripePayment and StripeRefund. The client works with interfaces and doesn't depend on concrete implementations.

### One-line memory trick

> Factory → creates an object.
>
> Abstract Factory → creates a family of related objects.
