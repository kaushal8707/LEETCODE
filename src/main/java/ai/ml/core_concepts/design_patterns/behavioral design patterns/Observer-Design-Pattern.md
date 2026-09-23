# Observer Design Pattern

The Observer Design Pattern is a Behavioral Design Pattern.

Observer Pattern defines a one-to-many relationship between objects so that when one object changes its state, all dependent objects are automatically notified.

In simple words:

One object changes → notify all interested objects automatically.

A very common real-world example is:

YouTube Channel → Subscribers

When a YouTube channel uploads a new video:

```
YouTube Channel
      |
      | notify
      ↓
+-----+-----+-----+
|           |     |
Subscriber  Subscriber  Subscriber
```

The channel doesn't need to manually call each subscriber's business logic. It simply notifies all registered observers.

## 1. Real-Time Example — YouTube Notification

Imagine you subscribe to a YouTube channel.

The channel has:

```
Subscribers:
    User A
    User B
    User C
```

When a new video is uploaded:

```
New Video
   ↓
YouTube Channel
   ↓
Notify subscribers
   ↓
+--------+--------+--------+
|        |        |        |
User A  User B   User C
```

This is exactly the idea behind the Observer Pattern.

## 2. The Problem Without Observer ❌

Suppose we write:

```java
class YouTubeChannel {

    public void uploadVideo() {

        System.out.println(
                "New video uploaded"
        );

        UserA.notifyUser();
        UserB.notifyUser();
        UserC.notifyUser();
    }
}
```

This creates strong coupling.

The channel needs to know about:

- UserA
- UserB
- UserC

What happens if we have:

100,000 subscribers?

We don't want the channel class to contain all of them as hard-coded dependencies.

## 3. Observer Pattern Solution ✅

Create an Observer interface:

```java
interface Observer {

    void update(String message);
}
```

Every subscriber implements this interface.

```java
class Subscriber implements Observer {

    private String name;

    public Subscriber(String name) {
        this.name = name;
    }

    @Override
    public void update(String message) {

        System.out.println(
                name + " received: " + message
        );
    }
}
```

Now create the Subject:

```java
class YouTubeChannel {

    private List<Observer> subscribers =
            new ArrayList<>();

    public void subscribe(
            Observer observer) {

        subscribers.add(observer);
    }

    public void unsubscribe(
            Observer observer) {

        subscribers.remove(observer);
    }

    public void uploadVideo(
            String videoTitle) {

        System.out.println(
                "New video: " + videoTitle
        );

        notifySubscribers(videoTitle);
    }

    private void notifySubscribers(
            String videoTitle) {

        for (Observer observer : subscribers) {

            observer.update(
                    "New video uploaded: "
                            + videoTitle
            );
        }
    }
}
```

## 4. Complete Java Example

```java
import java.util.ArrayList;
import java.util.List;

interface Observer {

    void update(String message);
}
```

### Subscriber:

```java
class Subscriber implements Observer {

    private String name;

    public Subscriber(String name) {
        this.name = name;
    }

    @Override
    public void update(String message) {

        System.out.println(
                name + " received notification: "
                        + message
        );
    }
}
```

### YouTube Channel:

```java
class YouTubeChannel {

    private List<Observer> subscribers =
            new ArrayList<>();

    public void subscribe(
            Observer observer) {

        subscribers.add(observer);
    }

    public void unsubscribe(
            Observer observer) {

        subscribers.remove(observer);
    }

    public void uploadVideo(
            String videoTitle) {

        System.out.println(
                "\nNew video uploaded: "
                        + videoTitle
        );

        notifySubscribers(videoTitle);
    }

    private void notifySubscribers(
            String videoTitle) {

        for (Observer observer : subscribers) {

            observer.update(
                    "Watch: " + videoTitle
            );
        }
    }
}
```

### Main:

```java
public class Main {

    public static void main(String[] args) {

        YouTubeChannel channel =
                new YouTubeChannel();

        Observer user1 =
                new Subscriber("Rahul");

        Observer user2 =
                new Subscriber("Amit");

        Observer user3 =
                new Subscriber("Priya");

        channel.subscribe(user1);
        channel.subscribe(user2);
        channel.subscribe(user3);

        channel.uploadVideo(
                "Java Design Patterns"
        );
    }
}
```

### Output:

```
New video uploaded: Java Design Patterns

Rahul received notification:
Watch: Java Design Patterns

Amit received notification:
Watch: Java Design Patterns

Priya received notification:
Watch: Java Design Patterns
```

## 5. What are the Roles?

Observer Pattern generally has two major concepts:

- Subject
- Observer

### Subject

The object whose state changes.

Example:

`YouTubeChannel`

It maintains a list of observers.

```java
List<Observer> subscribers;
```

### Observer

The object interested in changes.

Example:

`Subscriber`

It receives notifications.

```java
void update(String message);
```

## 6. Architecture

```
                   SUBJECT
               YouTubeChannel
                     |
                     |
             notifyObservers()
                     |
          +----------+----------+
          |          |          |
          ↓          ↓          ↓
      Observer   Observer   Observer
          |          |          |
          ↓          ↓          ↓
       User A     User B     User C
```

The important relationship is:

```
One Subject
     ↓
Many Observers
```

That's why Observer is called a:

One-to-Many relationship

## 7. Real-Time Example — Stock Market

This is one of the best examples.

Suppose:

Apple Stock = $200

Many applications are interested in its price:

```
Stock Exchange
       |
       ↓
   Stock Price
       |
   +---+---+---+
   |   |   |   |
   ↓   ↓   ↓   ↓
Trading App
Portfolio App
Mobile App
Notification Service
```

When the price changes:

$200 → $205

all observers are notified.

### Stock Observer

```java
interface Observer {

    void update(double price);
}
```

### Trading application:

```java
class TradingApp implements Observer {

    @Override
    public void update(double price) {

        System.out.println(
                "Trading App: Stock price = "
                        + price
        );
    }
}
```

### Notification service:

```java
class NotificationService
        implements Observer {

    @Override
    public void update(double price) {

        System.out.println(
                "Notification: Stock price = "
                        + price
        );
    }
}
```

### Stock:

```java
class Stock {

    private List<Observer> observers =
            new ArrayList<>();

    private double price;

    public void subscribe(
            Observer observer) {

        observers.add(observer);
    }

    public void setPrice(double price) {

        this.price = price;

        notifyObservers();
    }

    private void notifyObservers() {

        for (Observer observer : observers) {

            observer.update(price);
        }
    }
}
```

Now:

```java
Stock stock = new Stock();

stock.subscribe(new TradingApp());

stock.subscribe(new NotificationService());

stock.setPrice(205);
```

Output:

```
Trading App: Stock price = 205.0

Notification: Stock price = 205.0
```

The stock doesn't know what the observers actually do.

It only knows:

`Observer`

This gives us loose coupling.

## 8. Real-Time Example — E-commerce Product Price

Suppose you're watching a product:

```
iPhone
₹80,000
```

You select:

"Notify me when price drops."

Now multiple systems may observe the product:

```
Product
   |
   +── Price Alert Service
   |
   +── Email Service
   |
   +── Mobile Notification
```

When:

₹80,000 → ₹75,000

the product price changes and observers are notified.

```
Product
   |
   ↓
Price Changed
   |
   +--------+--------+
   |        |        |
   ↓        ↓        ↓
Email     SMS       App
```

This is a classic Observer use case.

## 9. Real-Time Example — Weather Application

Another common example is a weather monitoring system.

Suppose a weather station measures:

- Temperature
- Humidity
- Pressure

Different applications are interested in the data:

```
Weather Station
      |
      ↓
 Temperature changed
      |
 +----+----+----+
 |    |    |    |
 ↓    ↓    ↓    ↓
Mobile Display
Web Dashboard
Weather Alert
Analytics
```

When temperature changes:

30°C → 35°C

all registered observers are notified.

## 10. Real-Time Example — Order Status

This is especially useful in backend applications.

Consider an e-commerce order:

```
Order #1001
```

Its status changes:

```
CREATED
   ↓
CONFIRMED
   ↓
PACKED
   ↓
SHIPPED
   ↓
DELIVERED
```

Multiple systems are interested:

```
                Order
                  |
            Status Changed
                  |
        +---------+---------+
        |         |         |
        ↓         ↓         ↓
Notification  Inventory  Analytics
Service       Service    Service
```

When the order changes to:

SHIPPED

the observers can react:

```
NotificationService
→ Send "Your order has shipped"

AnalyticsService
→ Record shipping event

CustomerApp
→ Update order status
```

The Order object doesn't need to know the internal implementation of these services.

## 11. Observer Pattern with Java

The modern Java approach is usually to create your own interface:

```java
interface Observer {

    void update(String event);
}
```

And a subject:

```java
interface Subject {

    void subscribe(Observer observer);

    void unsubscribe(Observer observer);

    void notifyObservers();
}
```

Then:

```java
class Order implements Subject {

    private List<Observer> observers =
            new ArrayList<>();

    private String status;

    @Override
    public void subscribe(
            Observer observer) {

        observers.add(observer);
    }

    @Override
    public void unsubscribe(
            Observer observer) {

        observers.remove(observer);
    }

    public void setStatus(String status) {

        this.status = status;

        notifyObservers();
    }

    @Override
    public void notifyObservers() {

        for (Observer observer : observers) {

            observer.update(status);
        }
    }
}
```

## 12. Push vs Pull Model

Observer Pattern can work in two common ways.

### Push Model

Subject sends the updated data directly.

```java
observer.update(price);
```

The observer receives:

Price = 205

This is called push.

```
Subject
   |
   | price = 205
   ↓
Observer
```

### Pull Model

Subject tells observers that something changed:

```java
observer.update();
```

Then the observer asks the subject for the latest data.

```
Subject
   |
   | "Something changed"
   ↓
Observer
   |
   | getPrice()
   ↓
Subject
```

For example:

```java
observer.update();
```

Then:

```java
double price =
        stock.getPrice();
```

This is called pull.

## 13. Observer Pattern and Loose Coupling

This is one of the biggest benefits.

Without Observer:

```
OrderService
   |
   +── EmailService
   +── SMSService
   +── AnalyticsService
   +── InventoryService
```

The order service knows about everything.

With Observer:

```
              Observer
                 ↑
                 |
              Order
                 |
       +---------+---------+
       |         |         |
      Email     SMS    Analytics
```

Order only knows:

`Observer`

It doesn't care whether the observer is:

- Email
- SMS
- Kafka
- Database
- Mobile notification

## 14. Observer Pattern and OCP

Observer also works nicely with the Open/Closed Principle.

Suppose today we have:

```
Order
  ↓
EmailNotification
```

Tomorrow we add:

`SMSNotification`

We create:

```java
class SMSNotification
        implements Observer {
    
    @Override
    public void update(String event) {
        // Send SMS
    }
}
```

We don't need to modify the existing Order logic.

We simply register:

```java
order.subscribe(
        new SMSNotification()
);
```

So the system is open for adding new observers without modifying the subject's core logic.

## 15. Observer vs Command

Since you were just learning Command Pattern, this distinction is important.

### Command Pattern

The focus is:

Encapsulate a request/action as an object.

```
Remote
  ↓
Command
  ↓
TV
```

Example:

- TurnOnCommand
- TurnOffCommand

### Observer Pattern

The focus is:

Notify interested objects when something changes.

```
Subject
  ↓
+---+---+---+
↓   ↓   ↓
A   B   C
```

Example:

```
Stock
  ↓
Trading App
Notification App
Portfolio App
```

### Easy difference:

```
Command  → "Do this."

Observer → "Something happened."
```

## 16. Observer vs Strategy

### Strategy

Chooses one behavior/algorithm:

```
PaymentService
      |
      ↓
PaymentStrategy
      |
 +----+----+
 |    |    |
UPI Card PayPal
```

### Observer

Notifies multiple interested objects:

```
Stock
 |
 +----+----+
 |    |    |
 ↓    ↓    ↓
App  SMS  Email
```

So:

```
Strategy → Choose behavior

Observer → Broadcast changes
```

## 17. Observer vs Pub/Sub

They are conceptually similar but often implemented differently.

### Observer

Usually direct object-to-object relationship:

```
Subject
   |
   ↓
Observer
```

The subject typically maintains the observer list.

### Pub/Sub

Usually uses a broker/message infrastructure:

```
Publisher
    |
    ↓
 Message Broker
    |
 +--+--+--+
 ↓  ↓  ↓
A   B   C
```

Examples of messaging systems include:

- Kafka
- RabbitMQ
- AWS SNS

So:

```
Observer → Usually in-process object notification

Pub/Sub → Usually message-based communication
```

## 18. When Should You Use Observer?

Use Observer when:

- ✅ **One object changes and many objects need to know** — Stock price, Order status, Weather data
- ✅ **You want loose coupling** — Subject doesn't know concrete observers.
- ✅ **Observers can be added/removed dynamically**

```java
subscribe(observer);

unsubscribe(observer);
```

- ✅ **You want event-driven behavior** — OrderCreated, PaymentCompleted, OrderShipped

## 19. Potential Problems

Observer Pattern is powerful, but it isn't free.

### Problem 1 — Too many observers

One event can trigger many operations.

```
Order Updated
   ↓
20 observers
   ↓
20 operations
```

This can become expensive.

### Problem 2 — Unexpected chains

An observer can trigger another event:

```
Order
 ↓
Notification
 ↓
Email
 ↓
Another Event
 ↓
Another Observer
```

This can become difficult to debug.

### Problem 3 — Memory leaks

If observers are registered but never removed, they may remain referenced longer than necessary.

That's why lifecycle management matters:

```java
subscribe();

unsubscribe();
```

## 20. Complete Diagram

Remember this diagram:

```
                     SUBJECT
                  Stock / Order
                       |
                       |
                 State changes
                       |
                       ↓
                notifyObservers()
                       |
          +------------+------------+
          |            |            |
          ↓            ↓            ↓
      Observer A    Observer B   Observer C
          |            |            |
          ↓            ↓            ↓
        Email        SMS        Analytics
```

The relationship is:

```
          ONE
           |
           ↓
        SUBJECT
           |
           |
          MANY
           |
     +-----+-----+-----+
     ↓     ↓     ↓     ↓
    Obs   Obs   Obs   Obs
```

## 21. Interview Answer

If the interviewer asks:

**What is Observer Design Pattern?**

You can say:

Observer is a behavioral design pattern that establishes a one-to-many relationship between a subject and multiple observers. When the subject's state changes, it automatically notifies all registered observers. This helps achieve loose coupling between the object producing an event and the objects consuming it.

**Real-time example:**

In an e-commerce application, an order can act as the subject. When its status changes from CONFIRMED to SHIPPED, different observers such as notification service, analytics service, and customer application can be notified. The order doesn't need to know the implementation details of these services.

## 22. Easy Way to Remember

Think about YouTube subscriptions:

```
          YouTube Channel
                |
          New Video Uploaded
                |
        notifySubscribers()
                |
       +--------+--------+
       |        |        |
       ↓        ↓        ↓
     User A   User B   User C
       |        |        |
       ↓        ↓        ↓
   Notification Notification Notification
```

**One-line memory trick:**

> Observer Pattern = When one object changes, automatically notify all interested objects.

And remember:

```
Subject   → Something changes

Observer  → Interested in the change

Notify    → Tell all observers

One → Many
```

**Observer = "Something changed; let everyone interested know."**
