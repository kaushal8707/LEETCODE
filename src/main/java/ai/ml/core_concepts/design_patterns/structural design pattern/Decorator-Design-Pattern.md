# Decorator Design Pattern

The Decorator Design Pattern is a Structural Design Pattern.

Decorator allows you to add new behavior or responsibilities to an existing object dynamically, without modifying its original class.

## Simple definition

Think of adding toppings to a pizza 🍕.

Start with:

Pizza

Add:

+ Cheese

Then:

+ Mushroom

Then:

+ Olives

You don't create a completely new pizza class for every combination.

```
Pizza
  ↓
Cheese Decorator
  ↓
Mushroom Decorator
  ↓
Olive Decorator
```

That's the basic idea of Decorator.

## 1. Real-Time Example — Coffee Shop ☕

This is one of the best examples for understanding Decorator.

Suppose your coffee application has:

Simple Coffee

You can add:

- Milk
- Sugar
- Chocolate
- Whipped Cream

You don't want to create classes like:

```
CoffeeWithMilk
CoffeeWithSugar
CoffeeWithMilkAndSugar
CoffeeWithMilkSugarChocolate
CoffeeWithMilkSugarChocolateCream
...
```

The number of combinations can become huge.

Instead, use Decorators.

```
Simple Coffee
     ↓
   Milk
     ↓
   Sugar
     ↓
Chocolate
```

Each decorator adds something to the existing object.

## 2. Basic Structure

The Decorator pattern looks like:

```
                  Component
                     ↑
              +------+------+
              |             |
        ConcreteComponent  Decorator
                            |
                            ↓
                     ConcreteDecorator
```

The important idea is:

```
Client
  ↓
Decorator
  ↓
Existing Object
```

## 3. Java Example — Coffee

### Step 1: Component Interface

```java
interface Coffee {

    String getDescription();

    double getCost();
}
```

This is the common interface.

Both the actual coffee and decorators will implement it.

## 4. Concrete Component

Create the basic coffee.

```java
class SimpleCoffee implements Coffee {

    @Override
    public String getDescription() {

        return "Simple Coffee";
    }

    @Override
    public double getCost() {

        return 100;
    }
}
```

Our basic coffee costs:

₹100

## 5. Create Abstract Decorator

```java
abstract class CoffeeDecorator
        implements Coffee {

    protected Coffee coffee;

    public CoffeeDecorator(Coffee coffee) {

        this.coffee = coffee;
    }

    @Override
    public String getDescription() {

        return coffee.getDescription();
    }

    @Override
    public double getCost() {

        return coffee.getCost();
    }
}
```

The important part is:

```java
protected Coffee coffee;
```

The decorator contains another Coffee object.

This is called composition.

## 6. Milk Decorator

```java
class MilkDecorator
        extends CoffeeDecorator {

    public MilkDecorator(Coffee coffee) {

        super(coffee);
    }

    @Override
    public String getDescription() {

        return coffee.getDescription()
                + ", Milk";
    }

    @Override
    public double getCost() {

        return coffee.getCost() + 30;
    }
}
```

Milk costs:

₹30

## 7. Sugar Decorator

```java
class SugarDecorator
        extends CoffeeDecorator {

    public SugarDecorator(Coffee coffee) {

        super(coffee);
    }

    @Override
    public String getDescription() {

        return coffee.getDescription()
                + ", Sugar";
    }

    @Override
    public double getCost() {

        return coffee.getCost() + 10;
    }
}
```

Sugar costs:

₹10

## 8. Chocolate Decorator

```java
class ChocolateDecorator
        extends CoffeeDecorator {

    public ChocolateDecorator(Coffee coffee) {

        super(coffee);
    }

    @Override
    public String getDescription() {

        return coffee.getDescription()
                + ", Chocolate";
    }

    @Override
    public double getCost() {

        return coffee.getCost() + 40;
    }
}
```

Chocolate costs:

₹40

## 9. Client Code

Now we can dynamically add features.

```java
public class Main {

    public static void main(String[] args) {

        Coffee coffee =
                new SimpleCoffee();

        System.out.println(
                coffee.getDescription()
        );

        System.out.println(
                coffee.getCost()
        );

        coffee =
                new MilkDecorator(coffee);

        coffee =
                new SugarDecorator(coffee);

        coffee =
                new ChocolateDecorator(coffee);

        System.out.println(
                coffee.getDescription()
        );

        System.out.println(
                coffee.getCost()
        );
    }
}
```

Output:

```
Simple Coffee
100.0

Simple Coffee, Milk, Sugar, Chocolate
180.0
```

Calculation:

```
Coffee       = ₹100
Milk         = ₹30
Sugar        = ₹10
Chocolate    = ₹40
------------------
Total        = ₹180
```

## 10. What Actually Happens?

This code:

```java
coffee =
        new ChocolateDecorator(
                new SugarDecorator(
                        new MilkDecorator(
                                new SimpleCoffee()
                        )
                )
        );
```

creates a chain.

```
ChocolateDecorator
        ↓
SugarDecorator
        ↓
MilkDecorator
        ↓
SimpleCoffee
```

When we call:

```java
coffee.getCost();
```

the call travels through the chain.

```
Chocolate
   ↓
Sugar
   ↓
Milk
   ↓
Simple Coffee
```

Then the cost is built back up:

```
Simple Coffee = 100

Milk = 100 + 30 = 130

Sugar = 130 + 10 = 140

Chocolate = 140 + 40 = 180
```

## 11. The Most Important Concept

Decorator uses composition rather than creating a huge inheritance hierarchy.

Without Decorator:

```
Coffee
 ├── CoffeeWithMilk
 ├── CoffeeWithSugar
 ├── CoffeeWithChocolate
 ├── CoffeeWithMilkSugar
 ├── CoffeeWithMilkChocolate
 ├── CoffeeWithSugarChocolate
 └── CoffeeWithMilkSugarChocolate
```

This can become difficult to maintain.

With Decorator:

```
Coffee
 ↓
Milk Decorator
 ↓
Sugar Decorator
 ↓
Chocolate Decorator
```

Much more flexible.

## 12. Real-Time Example — Java I/O

This is a very important real-world Java example.

Java I/O uses the Decorator idea extensively.

For example:

```java
InputStream input =
        new BufferedInputStream(
                new FileInputStream(
                        "data.txt"
                )
        );
```

Here:

```
FileInputStream
      ↓
BufferedInputStream
```

FileInputStream provides basic file reading.

BufferedInputStream adds buffering behavior.

Conceptually:

```
Client
  ↓
BufferedInputStream
  ↓
FileInputStream
  ↓
File
```

Another example:

```java
InputStream input =
        new DataInputStream(
                new BufferedInputStream(
                        new FileInputStream(
                                "data.txt"
                        )
                )
        );
```

Now:

```
FileInputStream
        ↓
BufferedInputStream
        ↓
DataInputStream
```

Each layer adds additional functionality.

This is the Decorator pattern in action.

## 13. Real-Time Example — Food Ordering App 🍔

Imagine an online food ordering system.

Start with:

Burger

Then users can add:

- Cheese
- Extra Patty
- Bacon
- Sauce
- Jalapeno

Instead of creating:

```
BurgerWithCheese
BurgerWithPatty
BurgerWithCheeseAndPatty
BurgerWithCheesePattyBacon
...
```

use decorators.

```
Burger
  ↓
Cheese
  ↓
Extra Patty
  ↓
Bacon
```

Each decorator adds:

- Description
- Price

For example:

```
Burger = ₹150
Cheese = ₹30
Patty = ₹80
Bacon = ₹50

Total = ₹310
```

## 14. Real-Time Example — Notifications 🔔

Suppose you have a basic notification service:

Notification

You want to add:

- Email
- SMS
- Push Notification
- WhatsApp
- Logging
- Encryption

Instead of creating classes for every combination:

```
EmailNotification
SMSNotification
EmailAndSMSNotification
EmailSMSPushNotification
...
```

you can decorate the notification service.

```
Basic Notification
        ↓
Email Decorator
        ↓
SMS Decorator
        ↓
Push Decorator
```

This allows functionality to be added dynamically.

## 15. Real-Time Example — Web Request Processing

Suppose you have:

HTTP Request

You want to add:

- Authentication
- Logging
- Compression
- Encryption
- Caching

Conceptually:

```
Request
  ↓
Logging Decorator
  ↓
Authentication Decorator
  ↓
Compression Decorator
  ↓
Actual Request Handler
```

Each layer adds behavior without changing the underlying handler.

This style is common in middleware/pipeline architectures.

## 16. Real-Time Example — Payment Processing 💳

Suppose we have:

PaymentService

Basic payment:

`processPayment()`

We might add:

- Logging
- Fraud Detection
- Encryption
- Retry
- Metrics

Conceptually:

```
Payment
   ↓
Logging
   ↓
Fraud Detection
   ↓
Retry
   ↓
Actual Payment Service
```

Each layer adds responsibility around the original service.

## 17. Real-Time Example — Text Formatting

Suppose you have:

Text

You want:

- Bold
- Italic
- Underline
- Color
- Font

You can dynamically decorate:

```
Text
 ↓
Bold
 ↓
Italic
 ↓
Underline
```

Instead of creating:

```
BoldText
ItalicText
BoldItalicText
BoldItalicUnderlineText
...
```

## 18. Decorator Pattern Structure

The standard structure is:

```
                 Component
                    ↑
            +-------+-------+
            |               |
            ↓               ↓
   ConcreteComponent     Decorator
                            |
                            ↓
                    ConcreteDecorator
```

More concretely:

```
             Coffee
               ↑
       +-------+-------+
       |               |
SimpleCoffee     CoffeeDecorator
                         ↑
                 +-------+-------+
                 |       |       |
                Milk   Sugar  Chocolate
```

## 19. Important Components

### 1. Component

Defines the common interface.

```java
interface Coffee {

    String getDescription();

    double getCost();
}
```

### 2. Concrete Component

The original object.

```java
class SimpleCoffee
        implements Coffee {
}
```

### 3. Decorator

Contains a reference to the Component.

```java
abstract class CoffeeDecorator
        implements Coffee {

    protected Coffee coffee;
}
```

### 4. Concrete Decorator

Adds specific behavior.

```java
class MilkDecorator
        extends CoffeeDecorator {
}
```

## 20. Why Does Decorator Implement the Same Interface?

This is very important.

Both:

`SimpleCoffee`

and:

`MilkDecorator`

implement:

`Coffee`

Therefore:

```java
Coffee coffee =
        new MilkDecorator(
                new SimpleCoffee()
        );
```

works.

The client doesn't care whether coffee is:

- SimpleCoffee
- MilkDecorator
- ChocolateDecorator

They all behave like:

`Coffee`

## 21. Decorator vs Inheritance

Suppose we want to add:

- Milk
- Sugar
- Chocolate

### Inheritance approach

You might create:

```
Coffee
 ├── MilkCoffee
 ├── SugarCoffee
 ├── ChocolateCoffee
 ├── MilkSugarCoffee
 ├── MilkChocolateCoffee
 ├── SugarChocolateCoffee
 └── MilkSugarChocolateCoffee
```

The number of classes grows quickly.

### Decorator approach

```
Coffee
 ↓
Milk
 ↓
Sugar
 ↓
Chocolate
```

You can combine decorators dynamically.

## 22. Decorator vs Adapter

This is a common interview question.

### Adapter

Makes incompatible interfaces compatible.

```
Client
  ↓
Adapter
  ↓
Existing Class
```

Purpose:

Convert interface.

### Decorator

Adds functionality to an existing object.

```
Client
  ↓
Decorator
  ↓
Existing Object
```

Purpose:

Add behavior.

Remember:

```
Adapter   → Convert
Decorator → Add
```

## 23. Decorator vs Proxy

They look very similar structurally.

Both can look like:

```
Client
  ↓
Wrapper
  ↓
Object
```

But their intent is different.

### Decorator

Adds new behavior.

Examples:

```
Coffee
 ↓
Milk
 ↓
Sugar
```

### Proxy

Controls access to the object.

Examples:

```
Client
 ↓
Security Proxy
 ↓
Real Service
```

Remember:

```
Decorator → Add behavior

Proxy     → Control access
```

## 24. Decorator vs Facade

### Facade

Provides a simple interface to a complex subsystem.

```
Client
 ↓
Facade
 ↓
Service A
Service B
Service C
```

### Decorator

Adds functionality to an individual object.

```
Object
 ↓
Decorator
 ↓
Decorator
```

Remember:

```
Facade    → Simplify

Decorator → Enhance
```

## 25. Decorator vs Flyweight

### Flyweight

Shares common data.

```
Object 1 ──┐
Object 2 ──┼──→ Shared Object
Object 3 ──┘
```

Purpose:

Reduce memory.

### Decorator

Adds functionality.

```
Object
 ↓
Decorator
 ↓
Decorator
```

Purpose:

Add responsibilities dynamically.

Remember:

```
Flyweight → Share

Decorator → Add
```

## 26. Advantages of Decorator

### 1. Adds behavior dynamically

You can decide at runtime:

```java
coffee =
    new MilkDecorator(coffee);
```

### 2. Avoids huge inheritance hierarchies

You don't need a class for every combination.

### 3. Follows Open/Closed Principle

You can add new decorators without modifying the existing component.

### 4. Flexible combinations

You can combine:

- Milk
- Sugar
- Chocolate

in different orders.

## 27. Disadvantages

### 1. Many small classes

A large system can have:

```
MilkDecorator
SugarDecorator
ChocolateDecorator
...
```

### 2. Debugging can become difficult

You might have:

```
Decorator
 → Decorator
   → Decorator
     → Object
```

### 3. Order can matter

For some decorators:

`A(B(object))`

may behave differently from:

`B(A(object))`

### 4. Object configuration can become complicated

If too many decorators are chained together, understanding the final behavior may become difficult.

## 28. Real-Time Spring Example

If you're working with Java/Spring, you'll encounter the Decorator idea in many layered designs.

For example, you might have:

PaymentService

and wrap it with:

- Logging
- Metrics
- Retry
- Caching

Conceptually:

```
Client
  ↓
Logging Layer
  ↓
Metrics Layer
  ↓
Retry Layer
  ↓
PaymentService
```

Each layer can add behavior while keeping the underlying service interface consistent.

## 29. Interview Question

### Q: What is Decorator Design Pattern?

A good interview answer:

Decorator is a structural design pattern that allows behavior or responsibilities to be added to an object dynamically without modifying its original class. It uses composition and typically requires the decorator and the wrapped object to implement the same interface.

**Real-time example:**

A coffee ordering system can have a basic coffee and decorators such as Milk, Sugar, and Chocolate. Each decorator adds its own cost and description. This avoids creating separate classes for every possible combination of coffee and toppings.

## 30. Easy Diagram to Remember

```
                 Coffee
                   ↑
                   |
             Simple Coffee
                   |
             +-----+-----+
             ↓           ↓
          Milk         Sugar
             ↓           ↓
          Coffee       Coffee
             |
             ↓
         Chocolate
```

Or think of it like putting layers around an object:

```
+--------------------------------+
|       Chocolate Decorator      |
|  +--------------------------+  |
|  |      Sugar Decorator     |  |
|  |  +--------------------+  |  |
|  |  |  Milk Decorator   |  |  |
|  |  |  +--------------+ |  |  |
|  |  |  | SimpleCoffee | |  |  |
|  |  |  +--------------+ |  |  |
|  |  +--------------------+  |  |
|  +--------------------------+  |
+--------------------------------+
```

### Final shortcut

Since you're learning the Structural Design Patterns:

| Pattern | Remember |
|---|---|
| Adapter | Convert interfaces |
| Bridge | Separate abstraction/implementation |
| Composite | Tree structure |
| Decorator | Add behavior |
| Facade | Simplify complexity |
| Flyweight | Share objects |
| Proxy | Control access |

**One-line memory trick:**

> 🎁 Decorator = Wrap an object and add extra behavior without changing the original object.
