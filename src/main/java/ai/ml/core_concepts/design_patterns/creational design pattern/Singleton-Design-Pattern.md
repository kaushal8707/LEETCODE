# Singleton Design Pattern

The Singleton Design Pattern is a Creational Design Pattern that ensures:

> Only one instance of a class is created and provides a global access point to that instance.

In simple words:

Normally:

```
new Logger()  → Object 1
new Logger()  → Object 2
new Logger()  → Object 3
```

Singleton:

```
getInstance() → Same Object
getInstance() → Same Object
getInstance() → Same Object
```

---

## 1. Why do we need Singleton?

Suppose you have a Logger class.

```java
Logger logger1 = new Logger();
Logger logger2 = new Logger();
Logger logger3 = new Logger();
```

You now have 3 Logger objects.

But imagine a requirement:

"The application should have only one Logger instance."

Creating multiple logger objects can be unnecessary and may cause problems with shared state/resources.

That's where Singleton comes in.

---

## 2. Basic Singleton implementation

```java
class Logger {


    private static Logger instance;


    private Logger() {
    }


    public static Logger getInstance() {


        if (instance == null) {
            instance = new Logger();
        }


        return instance;
    }


    public void log(String message) {
        System.out.println(message);
    }
}
```

Now use it:

```java
public class Main {


    public static void main(String[] args) {


        Logger logger1 = Logger.getInstance();
        Logger logger2 = Logger.getInstance();


        System.out.println(logger1 == logger2);
    }
}
```

Output:

```
true
```

Both references point to the same object.

---

## 3. How does Singleton work?

There are three important parts.

### Part 1 — Private constructor

```java
private Logger() {
}
```

This prevents other classes from doing:

```java
new Logger();
```

For example, this is not allowed:

```java
Logger logger = new Logger(); // ❌
```

Why?

Because the constructor is private.

### Part 2 — Static instance

```java
private static Logger instance;
```

This variable stores the single instance.

Initially:

```
instance
   |
   ↓
 null
```

### Part 3 — getInstance()

```java
public static Logger getInstance() {


    if (instance == null) {
        instance = new Logger();
    }


    return instance;
}
```

First call:

```java
Logger.getInstance();
```

Since:

```
instance == null
```

Java creates:

```
instance
   |
   ↓
 Logger Object
```

Second call:

```java
Logger.getInstance();
```

Now:

```
instance != null
```

So Java returns the existing object.

---

## 4. Real-time example — Application Configuration

Imagine a large application.

You have configuration values:

- Database URL
- Database username
- Application name
- API URL
- Timeout
- Environment

For example:

```java
class AppConfig {


    private static AppConfig instance;


    private String databaseUrl;
    private String environment;


    private AppConfig() {


        databaseUrl = "jdbc:mysql://localhost:3306/mydb";
        environment = "production";
    }


    public static AppConfig getInstance() {


        if (instance == null) {
            instance = new AppConfig();
        }


        return instance;
    }


    public String getDatabaseUrl() {
        return databaseUrl;
    }


    public String getEnvironment() {
        return environment;
    }
}
```

Now multiple services can access the same configuration:

```java
AppConfig config1 = AppConfig.getInstance();
AppConfig config2 = AppConfig.getInstance();


System.out.println(config1.getDatabaseUrl());
System.out.println(config2.getEnvironment());
```

Both use the same configuration object.

---

## 5. Real-time example — Logger

This is one of the easiest examples to understand.

Imagine:

```
Application
    |
    +--- UserService
    |
    +--- OrderService
    |
    +--- PaymentService
    |
    +--- NotificationService
```

All services need logging.

Instead of creating:

```
UserService     → Logger 1
OrderService    → Logger 2
PaymentService  → Logger 3
Notification    → Logger 4
```

we can have:

```
UserService      \
OrderService      \
PaymentService    ---> Single Logger
Notification      /
```

Implementation:

```java
class Logger {


    private static Logger instance;


    private Logger() {
    }


    public static Logger getInstance() {


        if (instance == null) {
            instance = new Logger();
        }


        return instance;
    }


    public void log(String message) {


        System.out.println(
                "[LOG] " + message
        );
    }
}
```

Usage:

```java
class OrderService {


    public void createOrder() {


        Logger logger = Logger.getInstance();


        logger.log("Order created");
    }
}
```

And:

```java
class PaymentService {


    public void makePayment() {


        Logger logger = Logger.getInstance();


        logger.log("Payment successful");
    }
}
```

Both services use the same Logger instance.

---

## 6. Real-time example — Cache Manager

Suppose your application has a common cache:

```
                 Application
                     |
          +----------+----------+
          |          |          |
          v          v          v
       UserService OrderService ProductService
          |          |          |
          +----------+----------+
                     |
                     v
                CacheManager
```

You don't want each service to create a separate cache.

Singleton can provide one shared CacheManager.

```java
class CacheManager {


    private static CacheManager instance;


    private Map<String, Object> cache =
            new HashMap<>();


    private CacheManager() {
    }


    public static CacheManager getInstance() {


        if (instance == null) {
            instance = new CacheManager();
        }


        return instance;
    }


    public void put(String key, Object value) {
        cache.put(key, value);
    }


    public Object get(String key) {
        return cache.get(key);
    }
}
```

Usage:

```java
CacheManager cache = CacheManager.getInstance();


cache.put("user:101", "Kaushal");


System.out.println(
        cache.get("user:101")
);
```

Any service accessing CacheManager.getInstance() gets the same instance.

---

## 7. The Singleton object lifecycle

Think of it like this:

### Initially

```
Singleton.instance
        |
        ↓
      null
```

### First call

```java
Singleton.getInstance();
```

Creates:

```
Singleton.instance
        |
        ↓
   +-----------+
   | Singleton |
   +-----------+
```

### Second call

```java
Singleton.getInstance();
```

Returns:

```
Singleton.instance
        |
        ↓
   +-----------+
   | Singleton |
   +-----------+
        ↑
        |
   same object
```

No second object is created.

---

## 8. Problem with the basic Singleton

The implementation above has an important problem in a multithreaded application.

Suppose two threads call:

```java
getInstance()
```

at exactly the same time.

```
Thread 1                 Thread 2
   |                         |
   v                         v
instance == null       instance == null
   |                         |
   v                         v
new Singleton()        new Singleton()
```

Potentially:

```
Object 1
Object 2
```

That violates the Singleton requirement.

So in production-quality Java code, we need to consider thread safety.

---

## 9. Thread-safe Singleton — synchronized method

One simple approach:

```java
class Singleton {


    private static Singleton instance;


    private Singleton() {
    }


    public static synchronized Singleton getInstance() {


        if (instance == null) {
            instance = new Singleton();
        }


        return instance;
    }
}
```

The keyword:

```java
synchronized
```

ensures that only one thread can execute the method at a time.

**Advantage**

Simple and thread-safe.

**Disadvantage**

Every call to getInstance() requires synchronization, which can add unnecessary overhead after the object has already been created.

---

## 10. Better approach — Double-Checked Locking

A commonly discussed thread-safe implementation is:

```java
class Singleton {


    private static volatile Singleton instance;


    private Singleton() {
    }


    public static Singleton getInstance() {


        if (instance == null) {


            synchronized (Singleton.class) {


                if (instance == null) {
                    instance = new Singleton();
                }
            }
        }


        return instance;
    }
}
```

The important pieces are:

```java
private static volatile Singleton instance;
```

and:

```java
if (instance == null) {
    synchronized (Singleton.class) {
        if (instance == null) {
            instance = new Singleton();
        }
    }
}
```

The first check avoids synchronization after the instance already exists.

---

## 11. Eager Singleton

Another simple approach is to create the instance when the class loads.

```java
class Singleton {


    private static final Singleton INSTANCE =
            new Singleton();


    private Singleton() {
    }


    public static Singleton getInstance() {
        return INSTANCE;
    }
}
```

Usage:

```java
Singleton obj1 = Singleton.getInstance();
Singleton obj2 = Singleton.getInstance();


System.out.println(obj1 == obj2);
```

Output:

```
true
```

**Advantage**

Very simple and thread-safe because class initialization is handled by the JVM.

**Disadvantage**

The object is created even if the application never actually needs it.

---

## 12. Best simple approach — Enum Singleton

Java provides a very robust way of implementing Singleton:

```java
enum Logger {


    INSTANCE;


    public void log(String message) {
        System.out.println(
                "[LOG] " + message
        );
    }
}
```

Usage:

```java
Logger.INSTANCE.log("Application started");
```

Why is this useful?

Java's enum mechanism provides strong guarantees around:

- Instance uniqueness
- Serialization
- Thread safety

For many situations, enum Singleton is an excellent choice.

---

## 13. Singleton in Spring Boot

This is especially important if you're learning Java/Spring.

Spring beans are singleton-scoped by default.

For example:

```java
@Service
public class PaymentService {


    public void pay() {
        System.out.println("Payment processing");
    }
}
```

By default, Spring creates one bean instance within the Spring application context:

```
Spring Container
       |
       v
 PaymentService
       |
       +------------------+
       |                  |
       v                  v
OrderService         Controller
       |                  |
       +--------+---------+
                |
                v
        Same PaymentService bean
```

So you generally don't need to manually implement Singleton for ordinary Spring-managed services.

This is an important distinction:

> Singleton pattern is a design pattern; Spring singleton scope is a container-managed lifecycle scope.

---

## 14. Where Singleton can be useful

Common examples include:

**Configuration Manager**

```
Application
     |
     v
ConfigManager
```

One shared configuration object.

**Logger**

```
Services
    |
    v
Logger
```

One shared logging facility.

**Cache Manager**

```
Services
    |
    v
CacheManager
```

One shared cache manager.

**Resource Manager**

For certain application-level shared resources, a single coordinated manager can be useful.

---

## 15. Singleton advantages

**✅ 1. Only one instance**

Prevents unnecessary object creation when one shared instance is appropriate.

**✅ 2. Global access point**

You can access it through:

```java
Singleton.getInstance();
```

**✅ 3. Shared state**

All consumers can work with the same object/state.

**✅ 4. Controlled object creation**

The class itself controls how and when its instance is created.

---

## 16. Singleton disadvantages

Singleton isn't something you should use everywhere.

**❌ 1. Global state**

Because many classes can access the same object, hidden dependencies can develop.

**❌ 2. Testing can become harder**

Tests may accidentally share state between one another.

**❌ 3. Multithreading concerns**

A poorly implemented Singleton can create multiple instances under concurrency.

**❌ 4. Can violate Single Responsibility Principle**

A class may end up responsible for both its business functionality and controlling its own lifecycle.

**❌ 5. Often unnecessary in dependency-injection frameworks**

In Spring, the container can manage singleton-scoped beans for you.

---

## 17. Singleton vs Static Class

This is another common interview question.

### Static class

You typically access:

```java
MathUtils.calculate();
```

You don't create an instance.

### Singleton

You have an actual object:

```java
Logger logger = Logger.getInstance();
```

You can:

- Implement interfaces
- Pass the object around
- Use polymorphism
- Maintain instance state

So:

```
Static class → no object required


Singleton   → exactly one object
```

---

## 18. Singleton vs Factory

Don't confuse these two.

### Singleton

Answers:

> How many instances should exist?

ONE

### Factory

Answers:

> Which implementation should I create?

- UPI
- CARD
- PAYPAL

They can even be used together, although that doesn't mean they always should be.

---

## 19. Interview answer

If an interviewer asks:

> What is Singleton Design Pattern?

A good answer is:

> Singleton is a creational design pattern that ensures a class has only one instance and provides a global access point to that instance. It is useful for resources such as configuration managers, logging facilities, or cache managers where a single coordinated instance is appropriate.

If they ask:

> How do you implement Singleton in Java?

Mention the three fundamental requirements:

1. Private constructor
2. Static instance
3. Static getInstance() method

For example:

```java
class Singleton {


    private static Singleton instance;


    private Singleton() {
    }


    public static Singleton getInstance() {


        if (instance == null) {
            instance = new Singleton();
        }


        return instance;
    }
}
```

And if they ask about multithreading, discuss:

- synchronized
- double-checked locking + volatile
- eager initialization
- enum Singleton

### The easiest way to remember all Creational Patterns

Since you're going through them one by one:

```
Singleton
   ↓
"I need ONE object."


Factory
   ↓
"I need to CHOOSE an object."


Abstract Factory
   ↓
"I need a FAMILY of related objects."


Builder
   ↓
"I need to BUILD a complex object step-by-step."


Prototype
   ↓
"I need to COPY an existing object."
```

> Singleton = ONE is the key word.
