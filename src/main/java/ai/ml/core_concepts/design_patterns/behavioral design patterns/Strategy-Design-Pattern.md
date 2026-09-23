# Strategy Design Pattern

The Strategy Design Pattern is a behavioral design pattern.

> Strategy Pattern defines a family of algorithms/behaviors, encapsulates each one separately, and makes them interchangeable at runtime.

In simple words:

> Instead of putting many if-else or switch conditions in one class, create separate classes for each behavior and choose the required behavior at runtime.

---

## 1. Real-Time Example — Payment System

Suppose an e-commerce application supports multiple payment methods:

```
              Payment
                 |
      +----------+----------+
      |          |          |
     UPI        Card       PayPal
```

A customer can choose:

- Pay using UPI
- Pay using Credit Card
- Pay using PayPal

A naive implementation might be:

```java
class PaymentService {


    public void pay(String type, double amount) {


        if (type.equals("UPI")) {


            System.out.println(
                    "Payment using UPI"
            );


        } else if (type.equals("CARD")) {


            System.out.println(
                    "Payment using Credit Card"
            );


        } else if (type.equals("PAYPAL")) {


            System.out.println(
                    "Payment using PayPal"
            );
        }
    }
}
```

This works initially.

But imagine we add:

- Apple Pay
- Google Pay
- Amazon Pay
- Bank Transfer
- Crypto

The PaymentService becomes full of conditions.

```
PaymentService
 ├── UPI
 ├── Card
 ├── PayPal
 ├── Apple Pay
 ├── Google Pay
 ├── Amazon Pay
 ├── Bank Transfer
 └── ...
```

This is difficult to maintain.

Strategy Pattern solves this problem.

---

## 2. Identify the changing behavior

Ask:

> What is changing?

The payment algorithm.

- UPI payment
- Card payment
- PayPal payment

These are different strategies for performing payment.

So we create an interface:

```java
interface PaymentStrategy {


    void pay(double amount);
}
```

---

## 3. Create Individual Strategies

### UPI Strategy

```java
class UPIPaymentStrategy
        implements PaymentStrategy {


    @Override
    public void pay(double amount) {


        System.out.println(
                "Paid ₹" + amount + " using UPI"
        );
    }
}
```

### Credit Card Strategy

```java
class CreditCardPaymentStrategy
        implements PaymentStrategy {


    @Override
    public void pay(double amount) {


        System.out.println(
                "Paid ₹" + amount +
                " using Credit Card"
        );
    }
}
```

### PayPal Strategy

```java
class PayPalPaymentStrategy
        implements PaymentStrategy {


    @Override
    public void pay(double amount) {


        System.out.println(
                "Paid ₹" + amount +
                " using PayPal"
        );
    }
}
```

Now each payment algorithm is isolated.

---

## 4. Create the Context

The class that uses the strategy is called the Context.

Here:

```
PaymentService
```

is our Context.

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

Notice:

PaymentService doesn't know whether it is using:

- UPI
- Card
- PayPal

It only knows:

- PaymentStrategy

---

## 5. Complete Example

### Strategy interface

```java
interface PaymentStrategy {


    void pay(double amount);
}
```

### UPI

```java
class UPIPaymentStrategy
        implements PaymentStrategy {


    @Override
    public void pay(double amount) {


        System.out.println(
                "Paid ₹" + amount +
                " using UPI"
        );
    }
}
```

### Card

```java
class CreditCardPaymentStrategy
        implements PaymentStrategy {


    @Override
    public void pay(double amount) {


        System.out.println(
                "Paid ₹" + amount +
                " using Credit Card"
        );
    }
}
```

### PayPal

```java
class PayPalPaymentStrategy
        implements PaymentStrategy {


    @Override
    public void pay(double amount) {


        System.out.println(
                "Paid ₹" + amount +
                " using PayPal"
        );
    }
}
```

### Context

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

### Main

```java
public class Main {


    public static void main(String[] args) {


        PaymentStrategy upi =
                new UPIPaymentStrategy();


        PaymentService paymentService =
                new PaymentService(upi);


        paymentService.pay(5000);
    }
}
```

Output:

```
Paid ₹5000.0 using UPI
```

---

## 6. Changing Strategy at Runtime

This is one of the most important features of the Strategy Pattern.

Suppose the customer first chooses UPI:

```java
PaymentStrategy strategy =
        new UPIPaymentStrategy();


PaymentService service =
        new PaymentService(strategy);


service.pay(5000);
```

Output:

```
Paid ₹5000.0 using UPI
```

Now the customer chooses Credit Card.

We can change the strategy:

```java
strategy =
        new CreditCardPaymentStrategy();


service =
        new PaymentService(strategy);


service.pay(5000);
```

Output:

```
Paid ₹5000.0 using Credit Card
```

The payment service doesn't change.

Only the strategy changes.

---

## 7. Better Context — Allow Strategy Switching

We can also provide a setter:

```java
class PaymentService {


    private PaymentStrategy strategy;


    public PaymentService(
            PaymentStrategy strategy) {


        this.strategy = strategy;
    }


    public void setStrategy(
            PaymentStrategy strategy) {


        this.strategy = strategy;
    }


    public void pay(double amount) {


        strategy.pay(amount);
    }
}
```

Now:

```java
public class Main {


    public static void main(String[] args) {


        PaymentService service =
                new PaymentService(
                        new UPIPaymentStrategy()
                );


        service.pay(5000);


        service.setStrategy(
                new CreditCardPaymentStrategy()
        );


        service.pay(5000);


        service.setStrategy(
                new PayPalPaymentStrategy()
        );


        service.pay(5000);
    }
}
```

Output:

```
Paid ₹5000.0 using UPI
Paid ₹5000.0 using Credit Card
Paid ₹5000.0 using PayPal
```

That's the essence of Strategy Pattern.

---

## 8. Understanding the Structure

The Strategy Pattern has three important parts.

### 1. Strategy

Interface defining the common behavior.

```java
interface PaymentStrategy {


    void pay(double amount);
}
```

### 2. Concrete Strategies

Different implementations.

- UPIPaymentStrategy
- CreditCardPaymentStrategy
- PayPalPaymentStrategy

### 3. Context

Uses the strategy.

- PaymentService

Architecture:

```
                 PaymentStrategy
                       ↑
          +------------+------------+
          |            |            |
          |            |            |
         UPI          Card        PayPal
          ↑            ↑            ↑
          +------------+------------+
                       |
                       |
                PaymentService
                  (Context)
```

---

## 9. Another Real-Time Example — Google Maps / Route Selection

Think about a navigation application.

You may want different routes:

- Fastest Route
- Shortest Route
- Avoid Toll
- Avoid Highway
- Public Transport

Without Strategy Pattern:

```java
class NavigationService {


    public void calculateRoute(
            String type) {


        if (type.equals("FASTEST")) {
            // fastest route
        }
        else if (type.equals("SHORTEST")) {
            // shortest route
        }
        else if (type.equals("NO_TOLL")) {
            // avoid toll
        }
    }
}
```

Again, lots of if-else.

---

## 10. Apply Strategy Pattern

Create:

```java
interface RouteStrategy {


    void calculateRoute(
            String source,
            String destination
    );
}
```

Fastest route:

```java
class FastestRouteStrategy
        implements RouteStrategy {


    @Override
    public void calculateRoute(
            String source,
            String destination) {


        System.out.println(
                "Calculating fastest route"
        );
    }
}
```

Shortest route:

```java
class ShortestRouteStrategy
        implements RouteStrategy {


    @Override
    public void calculateRoute(
            String source,
            String destination) {


        System.out.println(
                "Calculating shortest route"
        );
    }
}
```

Avoid toll:

```java
class NoTollRouteStrategy
        implements RouteStrategy {


    @Override
    public void calculateRoute(
            String source,
            String destination) {


        System.out.println(
                "Calculating route without toll"
        );
    }
}
```

Context:

```java
class NavigationService {


    private RouteStrategy strategy;


    public NavigationService(
            RouteStrategy strategy) {


        this.strategy = strategy;
    }


    public void navigate(
            String source,
            String destination) {


        strategy.calculateRoute(
                source,
                destination
        );
    }
}
```

Usage:

```java
NavigationService navigation =
        new NavigationService(
                new FastestRouteStrategy()
        );


navigation.navigate(
        "Mumbai",
        "Pune"
);
```

Tomorrow the user selects:

> Avoid Toll

We simply use:

```java
navigation = new NavigationService(
        new NoTollRouteStrategy()
);
```

No changes to the navigation algorithm's client code.

---

## 11. Real-Time Example — Discount Calculation

An e-commerce application may have different discount algorithms:

- Regular Customer
- Premium Customer
- Festival Sale
- Black Friday
- Employee Discount

Create:

```java
interface DiscountStrategy {


    double calculateDiscount(
            double amount
    );
}
```

Regular:

```java
class RegularDiscountStrategy
        implements DiscountStrategy {


    public double calculateDiscount(
            double amount) {


        return amount * 0.05;
    }
}
```

Premium:

```java
class PremiumDiscountStrategy
        implements DiscountStrategy {


    public double calculateDiscount(
            double amount) {


        return amount * 0.10;
    }
}
```

Festival:

```java
class FestivalDiscountStrategy
        implements DiscountStrategy {


    public double calculateDiscount(
            double amount) {


        return amount * 0.20;
    }
}
```

Context:

```java
class DiscountCalculator {


    private DiscountStrategy strategy;


    public DiscountCalculator(
            DiscountStrategy strategy) {


        this.strategy = strategy;
    }


    public double calculate(
            double amount) {


        return strategy.calculateDiscount(amount);
    }
}
```

Usage:

```java
DiscountCalculator calculator =
        new DiscountCalculator(
                new PremiumDiscountStrategy()
        );


double discount =
        calculator.calculate(10000);


System.out.println(discount);
```

Output:

```
1000.0
```

---

## 12. Strategy vs if-else

This is one of the most important reasons to use Strategy Pattern.

### ❌ Without Strategy

```java
if (type.equals("UPI")) {
    // UPI
}
else if (type.equals("CARD")) {
    // Card
}
else if (type.equals("PAYPAL")) {
    // PayPal
}
else if (type.equals("APPLE_PAY")) {
    // Apple Pay
}
```

Problems:

- Large class
- Difficult to maintain
- More conditions as requirements grow
- Violates OCP more easily
- Difficult to test each algorithm independently

### ✅ With Strategy

```
PaymentStrategy
      ↑
      |
 +----+----+----+
 |    |    |    |
UPI Card PayPal ApplePay
```

Each algorithm is isolated.

---

## 13. Strategy Pattern and OCP

Strategy Pattern is closely related to the Open/Closed Principle.

OCP says:

> Open for extension, closed for modification.

Strategy lets us add:

```
New Strategy
     ↓
New Class
```

without changing the context.

For example:

```
PaymentService
      |
      ↓
PaymentStrategy
      ↑
      |
 +----+----+----+
 |    |    |    |
UPI Card PayPal ApplePay
```

Adding:

```
GooglePayStrategy
```

doesn't require changing:

```
PaymentService
```

Therefore:

> Strategy Pattern is often used to implement OCP.

---

## 14. Strategy Pattern and DIP

It also relates to the Dependency Inversion Principle.

Instead of:

```java
class PaymentService {


    private UPIPaymentStrategy strategy;
}
```

we use:

```java
class PaymentService {


    private PaymentStrategy strategy;
}
```

So the high-level class depends on the abstraction.

```
PaymentService
      ↓
PaymentStrategy
      ↑
      |
UPI / Card / PayPal
```

That's DIP.

---

## 15. Strategy Pattern vs Factory Pattern

Since you've already studied Factory Pattern, this distinction is important.

### Factory

Factory answers:

> "Which object should I create?"

Example:

```java
PaymentStrategy strategy =
        PaymentFactory.create("UPI");
```

Factory creates:

- UPIPaymentStrategy

### Strategy

Strategy answers:

> "Which behavior/algorithm should I use?"

Example:

```java
PaymentService service =
        new PaymentService(
                new UPIPaymentStrategy()
        );
```

So:

```
Factory
   ↓
Creates the object


Strategy
   ↓
Uses interchangeable behavior
```

They can also be used together.

---

## 16. Strategy Pattern vs State Pattern

These are sometimes confused.

### Strategy

The client chooses an algorithm.

```
Payment
  ↓
UPI / Card / PayPal
```

### State

The object's behavior changes because its internal state changes.

For example, an order:

```
Order
  ↓
Created
  ↓
Paid
  ↓
Shipped
  ↓
Delivered
```

So:

```
Strategy → Change algorithm


State → Change behavior based on state
```

---

## 17. When should you use Strategy Pattern?

Use Strategy when:

**✅ You have multiple algorithms**

- Sorting
- Payment
- Discount
- Routing
- Compression
- Tax calculation

**✅ You have lots of if-else / switch**

```java
if (type == A)
else if (type == B)
else if (type == C)
```

**✅ Algorithms change independently**

For example:

- Payment algorithm
- Discount algorithm
- Shipping algorithm

**✅ You want runtime selection**

For example:

```
User chooses:
UPI

then:

UPI Strategy
```

---

## 18. When should you NOT use Strategy?

Don't create a Strategy class for every tiny if.

For example:

```java
if (age >= 18) {
    return true;
}
```

You don't need:

```
AgeGreaterThan18Strategy
```

That would be unnecessary complexity.

Use Strategy when you have meaningfully different behaviors/algorithms that benefit from being isolated.

---

## 19. Interview Answer

If the interviewer asks:

> What is Strategy Design Pattern?

You can say:

> Strategy is a behavioral design pattern that defines a family of algorithms, encapsulates each algorithm in a separate class, and makes them interchangeable. The client or context can choose the required strategy at runtime without changing the context's code.

Real-time example:

> In an e-commerce payment system, instead of having a PaymentService containing if-else statements for UPI, Credit Card, and PayPal, we create a PaymentStrategy interface with separate implementations such as UPIPaymentStrategy, CreditCardPaymentStrategy, and PayPalPaymentStrategy. PaymentService receives a PaymentStrategy and delegates payment processing to it. This allows us to add new payment methods without modifying the existing payment service.

---

## 20. Easy way to remember

Think:

```
             STRATEGY
                 |
       "Which algorithm?"
                 |
       +---------+---------+
       |         |         |
      UPI       CARD     PAYPAL
       |         |         |
       +---------+---------+
                 |
                 ↓
          PaymentService
```

### One-line memory trick:

> Strategy Pattern = Put each algorithm in its own class and switch between them when needed.

And the basic structure to remember is:

```java
interface Strategy {
    void execute();
}


class StrategyA implements Strategy {
    public void execute() {
    }
}


class StrategyB implements Strategy {
    public void execute() {
    }
}


class Context {


    private Strategy strategy;


    public Context(Strategy strategy) {
        this.strategy = strategy;
    }


    public void execute() {
        strategy.execute();
    }
}
```

> Strategy = interchangeable behavior.
