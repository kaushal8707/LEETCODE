# Bridge Design Pattern

The Bridge Design Pattern is a Structural Design Pattern.

Bridge separates an abstraction from its implementation so that both can evolve independently.

In simple words:

Bridge = Separate two dimensions of change and connect them using composition.

This pattern is especially useful when you have two things that can vary independently.

## 1. Real-Time Example — Remote Control and Devices 📺

This is one of the easiest examples.

Suppose we have different devices:

- TV
- Radio
- Projector

And different remotes:

- Basic Remote
- Advanced Remote
- Smart Remote

Without Bridge, you might end up creating:

```
BasicTVRemote
BasicRadioRemote
BasicProjectorRemote

AdvancedTVRemote
AdvancedRadioRemote
AdvancedProjectorRemote

SmartTVRemote
SmartRadioRemote
SmartProjectorRemote
```

That's a combinatorial explosion.

Instead, separate:

**Abstraction** → Remote

**Implementation** → Device

Then connect them:

```
             Remote
                |
                | bridge
                ↓
              Device
             /      \
           TV      Radio
```

Now:

```
BasicRemote → TV
BasicRemote → Radio
AdvancedRemote → TV
AdvancedRemote → Projector
```

can all work without creating separate classes for every combination.

## 2. Why Is It Called "Bridge"?

Because the abstraction and implementation are connected through a bridge.

```
     Abstraction
          |
          |
       BRIDGE
          |
          |
    Implementation
```

In Java, this bridge is usually created using composition.

For example:

```java
class Remote {

    protected Device device;
}
```

Here:

`Remote → Device`

is the bridge.

## 3. The Problem Without Bridge ❌

Imagine:

Remote

can vary:

- BasicRemote
- AdvancedRemote
- SmartRemote

And:

Device

can vary:

- TV
- Radio
- Projector

If you use inheritance for every combination:

```
                    Remote
                      |
          +-----------+-----------+
          |           |           |
       Basic       Advanced      Smart
          |           |           |
       +--+--+     +--+--+     +--+--+
       |     |     |     |     |     |
      TV   Radio   TV  Radio   TV  Radio
```

Add another device:

Speaker

and another remote:

VoiceRemote

The number of classes keeps growing.

## 4. With Bridge ✅

Separate the two hierarchies:

```
Remote Hierarchy              Device Hierarchy

     Remote                        Device
       |                         /   |   \
   +---+---+                    TV Radio Projector
   |       |
 Basic   Advanced
```

Then connect them:

```
BasicRemote ─────→ TV
BasicRemote ─────→ Radio

AdvancedRemote ──→ TV
AdvancedRemote ──→ Projector
```

Much more flexible.

## 5. Java Example — Remote and Device

### Step 1: Implementation Interface

First define the device abstraction.

```java
interface Device {

    void turnOn();

    void turnOff();

    void setVolume(int volume);
}
```

This is the Implementation side of the Bridge.

## 6. Concrete Implementations

### TV

```java
class TV implements Device {

    @Override
    public void turnOn() {

        System.out.println(
                "TV turned ON"
        );
    }

    @Override
    public void turnOff() {

        System.out.println(
                "TV turned OFF"
        );
    }

    @Override
    public void setVolume(int volume) {

        System.out.println(
                "TV volume: " + volume
        );
    }
}
```

### Radio

```java
class Radio implements Device {

    @Override
    public void turnOn() {

        System.out.println(
                "Radio turned ON"
        );
    }

    @Override
    public void turnOff() {

        System.out.println(
                "Radio turned OFF"
        );
    }

    @Override
    public void setVolume(int volume) {

        System.out.println(
                "Radio volume: " + volume
        );
    }
}
```

## 7. Abstraction — Remote

Now create the abstraction.

```java
abstract class Remote {

    protected Device device;

    public Remote(Device device) {

        this.device = device;
    }

    public abstract void turnOn();

    public abstract void turnOff();

    public abstract void setVolume(int volume);
}
```

The important part is:

```java
protected Device device;
```

The remote contains a Device.

This composition creates the bridge.

## 8. Basic Remote

```java
class BasicRemote extends Remote {

    public BasicRemote(Device device) {

        super(device);
    }

    @Override
    public void turnOn() {

        device.turnOn();
    }

    @Override
    public void turnOff() {

        device.turnOff();
    }

    @Override
    public void setVolume(int volume) {

        device.setVolume(volume);
    }
}
```

## 9. Advanced Remote

```java
class AdvancedRemote extends Remote {

    public AdvancedRemote(Device device) {

        super(device);
    }

    @Override
    public void turnOn() {

        device.turnOn();
    }

    @Override
    public void turnOff() {

        device.turnOff();
    }

    @Override
    public void setVolume(int volume) {

        device.setVolume(volume);
    }

    public void mute() {

        device.setVolume(0);
    }
}
```

The advanced remote has an additional feature:

`mute()`

## 10. Client Code

Now comes the important part.

We can connect any remote to any device.

```java
public class Main {

    public static void main(String[] args) {

        Device tv = new TV();

        Remote basicRemote =
                new BasicRemote(tv);

        basicRemote.turnOn();
        basicRemote.setVolume(20);
        basicRemote.turnOff();
    }
}
```

Output:

```
TV turned ON
TV volume: 20
TV turned OFF
```

## 11. Connect Advanced Remote to Radio

```java
Device radio = new Radio();

Remote advancedRemote =
        new AdvancedRemote(radio);

advancedRemote.turnOn();
advancedRemote.setVolume(30);

((AdvancedRemote) advancedRemote).mute();

advancedRemote.turnOff();
```

Output:

```
Radio turned ON
Radio volume: 30
Radio volume: 0
Radio turned OFF
```

Notice that we didn't create:

`AdvancedRadioRemote`

That's the benefit of Bridge.

## 12. The Complete Structure

```
                  ABSTRACTION
                      Remote
                        |
                +-------+-------+
                |               |
          BasicRemote     AdvancedRemote
                |               |
                +-------+-------+
                        |
                      Bridge
                        |
                        ↓
                IMPLEMENTATION
                     Device
                        |
              +---------+---------+
              |                   |
              TV                Radio
```

The key relationship is:

```
Remote ───────→ Device
```

This is composition.

## 13. Why Not Just Use Inheritance?

This is the most important question about Bridge.

Suppose you have:

**Remote types**

- Basic
- Advanced
- Smart
- Voice

**Device types**

- TV
- Radio
- Projector
- Speaker

If you combine them using inheritance:

```
BasicTVRemote
BasicRadioRemote
BasicProjectorRemote
BasicSpeakerRemote

AdvancedTVRemote
AdvancedRadioRemote
AdvancedProjectorRemote
AdvancedSpeakerRemote

SmartTVRemote
SmartRadioRemote
...
```

That's:

4 Remotes × 4 Devices = 16 classes

If you later have:

- 10 remotes
- 10 devices

you could end up with:

10 × 10 = 100 combinations

Bridge avoids this.

Instead:

- 10 Remote classes
- \+ 10 Device classes

and connect them dynamically.

## 14. The Core Idea

Bridge separates two independent dimensions.

In our example:

### Dimension 1 — Remote

can change independently.

- Basic
- Advanced
- Smart
- Voice

### Dimension 2 — Device

can change independently.

- TV
- Radio
- Projector
- Speaker

Bridge connects them:

```
Remote
   ↓
Device
```

This is the central concept.

## 15. Real-Time Example — Payment System 💳

Suppose an e-commerce application supports different:

### Payment types

```
Payment
 ├── OnlinePayment
 ├── RecurringPayment
 └── InternationalPayment
```

And different:

### Payment gateways

```
Gateway
 ├── Razorpay
 ├── Stripe
 └── PayPal
```

These two dimensions can change independently.

Instead of:

```
OnlineRazorpayPayment
OnlineStripePayment
OnlinePayPalPayment

RecurringRazorpayPayment
RecurringStripePayment
RecurringPayPalPayment
```

use Bridge.

```
Payment
   |
   ↓
PaymentGateway
   |
   +── Razorpay
   +── Stripe
   +── PayPal
```

Now:

```
OnlinePayment → Razorpay
OnlinePayment → Stripe

RecurringPayment → Stripe
RecurringPayment → PayPal
```

No explosion of classes.

## 16. Java Payment Example

Implementation:

```java
interface PaymentGateway {

    void pay(double amount);
}
```

Concrete implementations:

```java
class RazorpayGateway
        implements PaymentGateway {

    @Override
    public void pay(double amount) {

        System.out.println(
                "Paid ₹" + amount +
                " using Razorpay"
        );
    }
}
```

```java
class StripeGateway
        implements PaymentGateway {

    @Override
    public void pay(double amount) {

        System.out.println(
                "Paid ₹" + amount +
                " using Stripe"
        );
    }
}
```

Abstraction:

```java
abstract class Payment {

    protected PaymentGateway gateway;

    public Payment(
            PaymentGateway gateway) {

        this.gateway = gateway;
    }

    public abstract void makePayment(
            double amount);
}
```

Concrete abstraction:

```java
class OnlinePayment
        extends Payment {

    public OnlinePayment(
            PaymentGateway gateway) {

        super(gateway);
    }

    @Override
    public void makePayment(
            double amount) {

        gateway.pay(amount);
    }
}
```

Client:

```java
PaymentGateway gateway =
        new RazorpayGateway();

Payment payment =
        new OnlinePayment(gateway);

payment.makePayment(5000);
```

Output:

```
Paid ₹5000 using Razorpay
```

You can switch the gateway:

```java
PaymentGateway gateway =
        new StripeGateway();

Payment payment =
        new OnlinePayment(gateway);
```

without changing OnlinePayment.

## 17. Real-Time Example — Notification System 🔔

Suppose you have two dimensions.

### Notification type

```
Notification
 ├── Alert
 ├── Reminder
 └── Promotional
```

### Delivery mechanism

```
Sender
 ├── Email
 ├── SMS
 ├── Push
 └── WhatsApp
```

These can vary independently.

Without Bridge:

```
EmailAlert
SMSAlert
PushAlert
EmailReminder
SMSReminder
PushReminder
EmailPromotional
...
```

With Bridge:

```
Notification
      |
      ↓
   Sender
   / | \
Email SMS Push
```

Now:

```
Alert → Email
Alert → SMS

Reminder → Push
Reminder → WhatsApp

Promotion → Email
Promotion → SMS
```

## 18. Real-Time Example — Operating System + Application 🖥️

Consider an application that needs to run on:

- Windows
- Linux
- macOS

And applications:

- Drawing App
- Video App
- Office App

These are two independent dimensions.

```
Application
     |
     ↓
OperatingSystem
     |
  +--+--+--+
  |  |  |
Win Linux Mac
```

Instead of:

```
WindowsDrawingApp
LinuxDrawingApp
MacDrawingApp

WindowsVideoApp
LinuxVideoApp
MacVideoApp
```

you separate the two hierarchies.

## 19. Real-Time Example — Database Abstraction

Imagine an application has different operations:

```
Repository
 ├── UserRepository
 ├── OrderRepository
 └── ProductRepository
```

And different database implementations:

```
Database
 ├── MySQL
 ├── PostgreSQL
 └── MongoDB
```

Conceptually:

```
Repository
     |
     ↓
Database
  /  |  \
MySQL PostgreSQL MongoDB
```

This allows repository abstractions and database implementations to evolve independently.

## 20. Real-Time Example — Graphics System 🎨

Suppose you have shapes:

```
Shape
 ├── Circle
 ├── Square
 └── Triangle
```

And rendering systems:

```
Renderer
 ├── RasterRenderer
 ├── VectorRenderer
 └── OpenGLRenderer
```

Without Bridge:

```
RasterCircle
VectorCircle
OpenGLCircle

RasterSquare
VectorSquare
OpenGLSquare
```

With Bridge:

```
Shape
  |
  ↓
Renderer
  |
  +-- Raster
  +-- Vector
  +-- OpenGL
```

Then:

```
Circle → RasterRenderer
Circle → VectorRenderer

Square → RasterRenderer
Square → OpenGLRenderer
```

This is a classic Bridge example.

## 21. Bridge Pattern Components

There are generally four important components.

### 1. Abstraction

Defines high-level operations.

```java
abstract class Remote {

    protected Device device;
}
```

### 2. Refined Abstraction

Extends the abstraction.

```java
class BasicRemote
        extends Remote {
}
```

```java
class AdvancedRemote
        extends Remote {
}
```

### 3. Implementor

Defines implementation operations.

```java
interface Device {

    void turnOn();

    void turnOff();
}
```

### 4. Concrete Implementor

Provides actual implementation.

```java
class TV implements Device {
}
```

```java
class Radio implements Device {
}
```

## 22. Bridge Architecture

```
        Abstraction
             |
             | has-a
             ↓
        Implementor
             |
       +-----+-----+
       |           |
ConcreteImpl A  ConcreteImpl B
```

More complete:

```
                 Abstraction
                     |
              +------+------+
              |             |
        RefinedAbstraction1  RefinedAbstraction2
              |
              |
              ↓
          Implementor
              |
        +-----+-----+
        |           |
     Impl A       Impl B
```

## 23. Bridge and Composition

The most important Java line in Bridge is often:

```java
protected Device device;
```

This means:

Remote HAS-A Device

rather than:

Remote IS-A Device

So Bridge strongly relies on:

**Composition over inheritance.**

## 24. Bridge vs Adapter

These two patterns are frequently confused.

### Adapter

You already have existing classes with an incompatible interface.

Goal:

Make incompatible interfaces work together.

```
Client
  ↓
Adapter
  ↓
Existing Class
```

Remember:

```
Adapter → Convert
```

### Bridge

You design the system from the beginning so two dimensions can vary independently.

Goal:

Separate abstraction from implementation.

```
Abstraction
     ↓
Implementation
```

Remember:

```
Bridge → Separate
```

```
Adapter → Convert
```

## 25. Bridge vs Strategy

They both use composition.

### Strategy

Usually replaces an algorithm:

```
Payment
  ↓
PaymentStrategy
  |
  +-- UPI
  +-- Card
  +-- PayPal
```

Goal:

Change algorithm.

### Bridge

Separates two independently evolving hierarchies:

```
Remote
  ↓
Device
```

Goal:

Separate abstraction and implementation.

Easy memory:

```
Strategy → Change algorithm

Bridge   → Separate dimensions
```

## 26. Bridge vs Decorator

You have already studied Decorator.

### Decorator

Adds behavior:

```
Coffee
  ↓
Milk Decorator
  ↓
Sugar Decorator
```

```
Decorator → ADD
```

### Bridge

Separates two dimensions:

```
Remote
   ↓
Device
```

```
Bridge → SEPARATE
```

## 27. Bridge vs Facade

### Facade

Simplifies a complicated subsystem:

```
Client
  ↓
Facade
  ↓
Service A
Service B
Service C
```

```
Facade → Simplify
```

### Bridge

Separates abstraction and implementation:

```
Abstraction
     ↓
Implementation
```

```
Bridge → Separate
```

## 28. Bridge vs Adapter — Important Interview Difference

| Bridge | Adapter |
|---|---|
| Usually designed upfront | Often introduced later |
| Separates abstraction and implementation | Converts incompatible interfaces |
| Two independent dimensions | Existing incompatible class |
| Focuses on flexibility | Focuses on compatibility |
| Uses composition | Uses composition/inheritance |

Easy example:

```
Bridge:
Remote → Device

Adapter:
NewCharger → OldCharger
```

## 29. Advantages

### ✅ 1. Avoids class explosion

Instead of:

```
10 abstractions × 10 implementations
= 100 classes
```

you can have:

```
10 abstraction classes
+
10 implementation classes
```

and connect them.

### ✅ 2. Independent development

You can add:

`NewRemote`

without modifying:

- TV
- Radio
- Projector

And add:

`NewDevice`

without modifying:

- BasicRemote
- AdvancedRemote

### ✅ 3. Follows Open/Closed Principle

You can extend either hierarchy without changing the other.

### ✅ 4. Uses composition

The implementation can be changed at runtime:

```java
Remote remote =
        new BasicRemote(new TV());
```

and:

```java
Remote remote =
        new BasicRemote(new Radio());
```

## 30. Disadvantages

### ❌ 1. More classes/interfaces

The design becomes more abstract.

### ❌ 2. Can be over-engineering

If you have only:

- One Remote
- One Device

Bridge is probably unnecessary.

### ❌ 3. Requires understanding of two hierarchies

Developers need to understand:

- Abstraction hierarchy
- \+ Implementation hierarchy

## 31. When Should You Use Bridge?

Use Bridge when:

### 1. You have two independent dimensions of variation

For example:

- Remote × Device
- Payment × Gateway
- Shape × Renderer
- Notification × Delivery Channel

### 2. Both sides are expected to grow

For example:

- More Remote types
- \+ More Device types

### 3. Inheritance is creating too many classes

If you're seeing:

```
A_B
A_C
A_D

B_B
B_C
B_D
```

Bridge might be appropriate.

### 4. You want implementation to be replaceable

```java
new BasicRemote(new TV());
```

can become:

```java
new BasicRemote(new Radio());
```

without changing the remote abstraction.

## 32. When NOT to Use Bridge

Don't use it just because composition exists.

If you have:

```
Payment
  ↓
Stripe
```

and neither side has an independent hierarchy, Bridge may be unnecessary.

The pattern becomes valuable when you genuinely have two axes of change.

## 33. Interview Question

### Q: What is Bridge Design Pattern?

A strong interview answer:

Bridge is a structural design pattern that separates an abstraction from its implementation so that both can vary independently. It uses composition to connect the abstraction with the implementation and helps avoid class explosion when two dimensions of a system can change independently.

**Real-time example:**

A remote control and the devices it controls are two independent dimensions. We can have BasicRemote and AdvancedRemote, while also having TV, Radio, and Projector. Instead of creating a separate class for every combination, the Remote contains a Device reference. This allows any remote to work with any device.

## 34. The Most Important Diagram

Remember this:

```
        ABSTRACTION
             |
       +-----+-----+
       |           |
     Basic      Advanced
       |           |
       +-----+-----+
             |
           BRIDGE
             |
             ↓
       IMPLEMENTATION
             |
       +-----+-----+
       |           |
      TV         Radio
```

In code:

```java
abstract class Remote {

    protected Device device;

    public Remote(Device device) {
        this.device = device;
    }
}
```

That Device reference is the key.

## 35. Easy Memory Trick

For the structural patterns you've been studying:

| Pattern | Think |
|---|---|
| Adapter | 🔄 Convert |
| Bridge | 🌉 Separate |
| Composite | 🌳 Tree |
| Decorator | 🎁 Add |
| Facade | 🚪 Simplify |
| Flyweight | ♻️ Share |
| Proxy | 🛡️ Control |

**One-line definition:**

> 🌉 Bridge = Separate two independently changing dimensions and connect them through composition.

For example:

```
Remote  ←────────→  Device
   ↑                    ↑
Basic/Advanced       TV/Radio
```

Remote and Device can evolve independently.
