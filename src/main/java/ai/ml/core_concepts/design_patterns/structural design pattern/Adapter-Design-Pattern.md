# Adapter Design Pattern

The Adapter Design Pattern is a Structural Design Pattern.

Adapter allows two incompatible interfaces to work together.

In simple words:

Adapter converts the interface of an existing class into an interface that the client expects.

### Easy memory trick

```
Client expects A
      ↓
   Adapter
      ↓
Existing class provides B
```

The Adapter acts like a translator between two incompatible interfaces.

## 1. Real-Time Example — Mobile Charger 🔌

Imagine you have:

Wall Socket → 240V AC

But your mobile requires:

Mobile → 5V DC

You cannot directly connect the mobile to the wall socket.

You need a charger/adapter.

```
Wall Socket
    |
    | 240V AC
    ↓
+-------------+
|   Adapter   |
|   Charger   |
+-------------+
    |
    | 5V DC
    ↓
   Mobile
```

The charger converts one interface/format into another that the mobile understands.

That's the basic idea of the Adapter Pattern.

## 2. Real-Time Software Example — Payment Gateway

Suppose your application expects every payment provider to have:

`pay(amount)`

So you define:

```java
interface PaymentProcessor {

    void pay(double amount);
}
```

Your application expects:

```
PaymentProcessor
       |
       ↓
    pay()
```

But suppose a third-party payment library provides:

```java
class Razorpay {

    public void makePayment(double amount) {
        System.out.println(
                "Payment using Razorpay"
        );
    }
}
```

The method is:

`makePayment()`

not:

`pay()`

The interfaces are incompatible.

## 3. The Problem ❌

Your application expects:

```java
PaymentProcessor processor;
processor.pay(1000);
```

But the third-party class provides:

```java
Razorpay razorpay =
        new Razorpay();

razorpay.makePayment(1000);
```

The client expects:

`pay()`

while Razorpay provides:

`makePayment()`

We don't want to modify the third-party library.

So we introduce an Adapter.

## 4. Adapter Solution ✅

```
Application
     |
     ↓
PaymentProcessor
     |
     ↓
RazorpayAdapter
     |
     ↓
Razorpay
```

The Adapter translates:

```
pay()
  ↓
makePayment()
```

## 5. Step-by-Step Java Example

### Step 1 — Target Interface

This is the interface our application expects.

```java
interface PaymentProcessor {

    void pay(double amount);
}
```

This is called the Target.

### Step 2 — Existing/Third-Party Class

Suppose this class comes from an external library.

```java
class Razorpay {

    public void makePayment(double amount) {

        System.out.println(
                "Payment of ₹" + amount
                        + " made using Razorpay"
        );
    }
}
```

We cannot change this class.

Its interface is:

`makePayment()`

## 6. Step 3 — Create Adapter

```java
class RazorpayAdapter
        implements PaymentProcessor {

    private Razorpay razorpay;

    public RazorpayAdapter(
            Razorpay razorpay) {

        this.razorpay = razorpay;
    }

    @Override
    public void pay(double amount) {

        razorpay.makePayment(amount);
    }
}
```

The Adapter implements what our application expects:

`pay()`

and internally calls:

`makePayment()`

## 7. Client

Now the client doesn't need to know about the incompatible method.

```java
public class Main {

    public static void main(String[] args) {

        Razorpay razorpay =
                new Razorpay();

        PaymentProcessor processor =
                new RazorpayAdapter(razorpay);

        processor.pay(1000);
    }
}
```

Output:

```
Payment of ₹1000.0 made using Razorpay
```

## 8. Complete Architecture

```
                  CLIENT
                    |
                    ↓
          PaymentProcessor
             <<Target>>
                    |
                    ↓
          RazorpayAdapter
             <<Adapter>>
                    |
                    ↓
              Razorpay
        <<Adaptee/Existing>>
```

The Adapter performs:

```
Application's interface
        ↓
       Adapter
        ↓
Third-party interface
```

## 9. Important Terms

There are usually four important components.

### 1. Client

The class that wants to use the functionality.

`Application`

### 2. Target

The interface expected by the client.

```java
interface PaymentProcessor {

    void pay(double amount);
}
```

### 3. Adaptee

The existing class with an incompatible interface.

```java
class Razorpay {

    void makePayment(double amount) {
    }
}
```

### 4. Adapter

The class that converts the Adaptee's interface into the Target interface.

```java
class RazorpayAdapter
        implements PaymentProcessor {

    public void pay(double amount) {

        razorpay.makePayment(amount);
    }
}
```

## 10. Real-Time Example — Old Legacy System

This is extremely common in enterprise applications.

Suppose your new application expects:

```java
interface EmployeeService {

    Employee getEmployee(int id);
}
```

But your company's old legacy system has:

```java
class LegacyEmployeeSystem {

    public String findEmployee(int employeeId) {

        return "Employee details";
    }
}
```

The new application expects:

`getEmployee()`

but the legacy system provides:

`findEmployee()`

Instead of rewriting the old system, create:

```java
class EmployeeAdapter
        implements EmployeeService {

    private LegacyEmployeeSystem legacySystem;

    public EmployeeAdapter(
            LegacyEmployeeSystem legacySystem) {

        this.legacySystem = legacySystem;
    }

    @Override
    public Employee getEmployee(int id) {

        String data =
                legacySystem.findEmployee(id);

        return convertToEmployee(data);
    }

    private Employee convertToEmployee(
            String data) {

        // conversion logic
        return new Employee(data);
    }
}
```

Architecture:

```
New Application
       |
       ↓
EmployeeService
       |
       ↓
EmployeeAdapter
       |
       ↓
LegacyEmployeeSystem
```

This allows a new system to work with an old system without changing the old system.

## 11. Real-Time Example — Multiple Payment Providers

Suppose your application supports:

- Razorpay
- PayPal
- Stripe

But every provider has a different API.

```
Razorpay
    → makePayment()

PayPal
    → sendPayment()

Stripe
    → charge()
```

Your application wants one standard interface:

```java
interface PaymentProcessor {

    void pay(double amount);
}
```

Now create adapters.

```
                    PaymentProcessor
                           |
              +------------+------------+
              |            |            |
              ↓            ↓            ↓
       RazorpayAdapter  PayPalAdapter  StripeAdapter
              |            |            |
              ↓            ↓            ↓
          Razorpay       PayPal        Stripe
```

Your application can now use:

```java
PaymentProcessor processor;

processor.pay(1000);
```

without knowing which payment provider is underneath.

## 12. Real-Time Example — Database Libraries

Suppose your application expects:

```java
interface Database {

    void save(String data);
}
```

But an old database library has:

```java
class OldDatabase {

    public void insertRecord(String data) {

        System.out.println(
                "Record inserted"
        );
    }
}
```

Adapter:

```java
class DatabaseAdapter
        implements Database {

    private OldDatabase oldDatabase;

    public DatabaseAdapter(
            OldDatabase oldDatabase) {

        this.oldDatabase = oldDatabase;
    }

    @Override
    public void save(String data) {

        oldDatabase.insertRecord(data);
    }
}
```

Now:

```java
Database database =
        new DatabaseAdapter(
                new OldDatabase()
        );

database.save("Employee");
```

The new application doesn't care that the underlying database uses:

`insertRecord()`

## 13. Real-Time Example — XML to JSON

Suppose your new application expects JSON:

```json
{
  "name": "Kaushal",
  "age": 25
}
```

But an old service returns XML:

```xml
<Employee>
    <name>Kaushal</name>
    <age>25</age>
</Employee>
```

You could create an adapter:

```
New Application
       |
       ↓
 EmployeeData
       |
       ↓
 XML Adapter
       |
       ↓
 Legacy XML Service
```

The adapter converts:

XML → Application's expected format

This is another practical example of adapting an existing interface/data representation.

## 14. Object Adapter vs Class Adapter

There are two common forms.

### Object Adapter

Uses composition.

```java
class Adapter implements Target {

    private Adaptee adaptee;

    public Adapter(Adaptee adaptee) {
        this.adaptee = adaptee;
    }
}
```

Relationship:

```
Adapter
   |
   | has-a
   ↓
Adaptee
```

This is generally the more flexible approach in Java.

### Class Adapter

Uses inheritance.

Conceptually:

```java
class Adapter
        extends Adaptee
        implements Target {

    // adaptation
}
```

Relationship:

```
Adapter
   |
   +── extends Adaptee
   |
   +── implements Target
```

Java's lack of multiple class inheritance can make this approach less convenient.

## 15. Adapter and Composition

The Object Adapter commonly uses:

Composition

Example:

```java
class RazorpayAdapter
        implements PaymentProcessor {

    private Razorpay razorpay;
}
```

The Adapter has a Razorpay object.

```
RazorpayAdapter
      |
      | has-a
      ↓
   Razorpay
```

This is generally preferable because we don't modify or inherit from the third-party class.

## 16. Adapter vs Facade

This is a very important interview question.

### Adapter

Adapter solves:

Incompatible interfaces

```
Client
  |
  ↓
Target
  |
  ↓
Adapter
  |
  ↓
Incompatible class
```

Example:

```
pay()
 ↓
Adapter
 ↓
makePayment()
```

### Facade

Facade solves:

Complexity

```
Client
  |
  ↓
Facade
  |
  +── Service A
  +── Service B
  +── Service C
```

### Easy memory:

```
Adapter → Converts

Facade  → Simplifies
```

## 17. Adapter vs Decorator

Another common interview question.

### Adapter

Changes the interface.

```
Old Interface
     ↓
  Adapter
     ↓
New Interface
```

### Decorator

Adds new behavior without changing the core interface.

```
Component
    ↓
Decorator
    ↓
Additional behavior
```

Example:

```
Coffee
  ↓
MilkDecorator
  ↓
Coffee + Milk
```

So:

```
Adapter   → Changes interface

Decorator → Adds behavior
```

## 18. Adapter vs Proxy

### Adapter

Makes incompatible interfaces compatible.

```
Client
 ↓
Adapter
 ↓
Existing Class
```

### Proxy

Controls access to the real object.

```
Client
 ↓
Proxy
 ↓
Real Object
```

Proxy can provide:

- Caching
- Security
- Lazy loading
- Remote access

Easy memory:

```
Adapter → "Make them compatible."

Proxy   → "Stand in front of it."
```

## 19. Adapter vs Bridge

These are also often confused.

### Adapter

Usually used when you already have existing classes and need compatibility.

```
Existing System
      ↓
   Adapter
      ↓
New Client
```

### Bridge

Designed from the beginning to separate abstractions from implementations.

```
Abstraction
     |
     ↓
Implementor
```

Easy memory:

```
Adapter → Compatibility

Bridge  → Separation of abstraction and implementation
```

## 20. When Should You Use Adapter?

Use Adapter when:

- ✅ **You have an existing class you cannot modify** — Third-party library, Legacy system, External API
- ✅ **Interfaces don't match**

```
Client → pay()

Existing → makePayment()
```

- ✅ **You want to integrate multiple external systems**

```
Application
    |
    +── Razorpay Adapter
    +── PayPal Adapter
    +── Stripe Adapter
```

- ✅ **You want to reuse existing functionality** — Instead of rewriting an existing class, adapt it.

## 21. Advantages

### 1. Reuse existing code

You don't need to rewrite the existing class.

### 2. Supports third-party integration

Very useful when working with external libraries/APIs.

### 3. Reduces coupling

The client depends on the Target interface rather than the third-party implementation.

### 4. Follows Open/Closed Principle

You can add a new adapter without modifying the existing third-party class.

For example:

```
PaymentProcessor
       |
 +-----+-----+-----+
 |     |     |     |
 ↓     ↓     ↓     ↓
Razor PayPal Stripe NewProvider
Adapter Adapter Adapter Adapter
```

## 22. Disadvantages

### 1. More classes

Instead of directly using:

`Razorpay`

you now have:

`RazorpayAdapter`

### 2. Can add complexity

For a very simple system, creating multiple adapters may be unnecessary.

### 3. Conversion logic can become complicated

Sometimes the two systems don't just have different method names; their data models may also differ.

For example:

```
System A
firstName
lastName

System B
fullName
```

The adapter must convert:

```
firstName + lastName
        ↓
     fullName
```

## 23. Complete Adapter Diagram

Remember this:

```
                         CLIENT
                           |
                           ↓
                    TARGET INTERFACE
                           |
                           ↓
                       ADAPTER
                    /             \
                   /               \
                  ↓                 ↓
           Converts request    Uses existing
                  |             functionality
                  +-------+---------+
                          |
                          ↓
                       ADAPTEE
                 (Existing class)
```

For payment:

```
                  Application
                       |
                       ↓
              PaymentProcessor
                  pay()
                       |
                       ↓
              RazorpayAdapter
                       |
                       ↓
                  Razorpay
              makePayment()
```

The Adapter translates:

```
pay()
 ↓
makePayment()
```

## 24. Interview Answer

If the interviewer asks:

**What is Adapter Design Pattern?**

You can answer:

Adapter is a structural design pattern that allows incompatible interfaces to work together. It acts as a bridge between the client and an existing class by converting the interface expected by the client into the interface provided by the existing class.

**Real-time example:**

Suppose our application expects every payment provider to expose a pay() method, but a third-party Razorpay library exposes makePayment(). Instead of modifying the third-party library, we create RazorpayAdapter that implements our PaymentProcessor interface and internally calls razorpay.makePayment(). This allows our application to use Razorpay through the interface it already understands.

## 25. Easy Way to Remember

Think about a travel plug adapter.

You have:

```
Your Device
    |
    ↓
Different Plug
```

The socket doesn't accept your plug directly.

So:

```
Your Plug
    ↓
Travel Adapter
    ↓
Local Socket
```

The Adapter makes incompatible things work together.

**One-line memory trick:**

> Adapter Pattern = Convert an existing interface into the interface the client expects.

Remember these four:

```
Client
   ↓
Target
   ↓
Adapter
   ↓
Adaptee
```

And the most important distinction:

```
Adapter → Converts incompatible interfaces
Facade  → Simplifies a complex subsystem
Decorator → Adds behavior
Proxy   → Controls access
```

**Adapter = "The interfaces don't match, so I'll translate between them."**
