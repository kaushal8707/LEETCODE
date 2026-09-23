# Proxy Design Pattern

The Proxy Design Pattern is a Structural Design Pattern.

Proxy provides a substitute or placeholder for another object and controls access to the real object.

In simple words:

Proxy stands between the client and the real object.

```
Client
   |
   ↓
 Proxy
   |
   ↓
Real Object
```

The client thinks it is communicating with the real object, but the Proxy can perform additional work before or after forwarding the request.

## 1. Real-Time Example — Bank ATM 🏦

Consider an ATM.

You want to withdraw money:

```
Customer
   |
   ↓
  ATM
   |
   ↓
Bank Server
```

The ATM acts as an intermediary.

Before allowing the operation, it can:

1. Verify card
2. Ask for PIN
3. Check account
4. Check balance
5. Forward withdrawal request
6. Return result

The customer doesn't directly communicate with the bank's internal server.

Conceptually:

```
Customer
   |
   ↓
   ATM
  Proxy
   |
   ↓
Bank Server
Real Object
```

The ATM controls access to the actual banking system.

## 2. Real-Time Example — Security Proxy

Suppose a company has an internal server:

```
Employee
   |
   ↓
Security Proxy
   |
   ↓
Internal Server
```

The Proxy can check:

- Is the user authenticated?
- Is the user authorized?
- Is this operation allowed?

If allowed:

```
Employee
   ↓
Proxy
   ↓
Server
```

If not:

```
Employee
   ↓
Proxy
   ↓
Access Denied
```

This is a common use of the Proxy Pattern:

Access control.

## 3. Java Example — Security Proxy

Let's start with an interface.

```java
interface BankAccount {

    void withdraw(double amount);
}
```

This is the interface expected by the client.

### Real Object

```java
class RealBankAccount
        implements BankAccount {

    @Override
    public void withdraw(double amount) {

        System.out.println(
                "₹" + amount +
                " withdrawn from bank account"
        );
    }
}
```

This is the Real Subject.

## 4. Create the Proxy

```java
class BankAccountProxy
        implements BankAccount {

    private RealBankAccount realBankAccount;
    private boolean authenticated;

    public BankAccountProxy(
            boolean authenticated) {

        this.authenticated = authenticated;
    }

    @Override
    public void withdraw(double amount) {

        if (!authenticated) {

            System.out.println(
                    "Access Denied"
            );

            return;
        }

        if (realBankAccount == null) {

            realBankAccount =
                    new RealBankAccount();
        }

        realBankAccount.withdraw(amount);
    }
}
```

Notice what the Proxy does:

```
Client
  ↓
BankAccountProxy
  |
  +── Authentication
  |
  +── Create Real Object if required
  |
  ↓
RealBankAccount
```

## 5. Client

```java
public class Main {

    public static void main(String[] args) {

        BankAccount account =
                new BankAccountProxy(true);

        account.withdraw(5000);
    }
}
```

Output:

```
₹5000.0 withdrawn from bank account
```

If authentication fails:

```java
BankAccount account =
        new BankAccountProxy(false);

account.withdraw(5000);
```

Output:

```
Access Denied
```

The client doesn't directly access:

`RealBankAccount`

The Proxy controls access.

## 6. Proxy Pattern Structure

The general architecture is:

```
                    Client
                       |
                       ↓
                 +-----------+
                 |   Proxy   |
                 +-----------+
                       |
               controls access
                       |
                       ↓
                +------------+
                | RealObject |
                +------------+
```

Both Proxy and Real Object implement the same interface:

```
             Subject
             /     \
            /       \
        Proxy      RealObject
```

Therefore the client can use either one.

## 7. Important Components

There are usually four important components.

### 1. Subject

Common interface.

```java
interface BankAccount {

    void withdraw(double amount);
}
```

### 2. Real Subject

The actual object that performs the operation.

```java
class RealBankAccount
        implements BankAccount {

    public void withdraw(double amount) {
        // Actual operation
    }
}
```

### 3. Proxy

Controls access to the Real Subject.

```java
class BankAccountProxy
        implements BankAccount {

    private RealBankAccount realBankAccount;
}
```

### 4. Client

Uses the Subject interface.

```java
BankAccount account =
        new BankAccountProxy(true);
```

The client doesn't need to know whether it's using the Proxy or the Real Object.

## 8. Real-Time Example — Image Loading 🖼️

This is one of the most common examples.

Suppose an application displays a very large image:

`large-image.jpg`

Loading it immediately might be expensive.

Instead of loading the actual image immediately:

```
Application
     |
     ↓
RealImage
     |
     ↓
Load huge image
```

we use:

```
Application
     |
     ↓
ImageProxy
     |
     ↓
RealImage
```

The Proxy can delay loading until the image is actually needed.

This is called:

Virtual Proxy / Lazy Loading Proxy

## 9. Java Image Proxy Example

### Interface

```java
interface Image {

    void display();
}
```

### Real Image

```java
class RealImage
        implements Image {

    private String fileName;

    public RealImage(String fileName) {

        this.fileName = fileName;

        loadImage();
    }

    private void loadImage() {

        System.out.println(
                "Loading " + fileName
        );
    }

    @Override
    public void display() {

        System.out.println(
                "Displaying " + fileName
        );
    }
}
```

Creating this object immediately loads the image.

## 10. Image Proxy

```java
class ImageProxy
        implements Image {

    private String fileName;
    private RealImage realImage;

    public ImageProxy(String fileName) {

        this.fileName = fileName;
    }

    @Override
    public void display() {

        if (realImage == null) {

            realImage =
                    new RealImage(fileName);
        }

        realImage.display();
    }
}
```

Notice:

```java
private RealImage realImage;
```

Initially:

`realImage = null`

The actual image is not loaded yet.

## 11. Client

```java
public class Main {

    public static void main(String[] args) {

        Image image =
                new ImageProxy(
                        "large-image.jpg"
                );

        System.out.println(
                "Image proxy created"
        );

        image.display();
    }
}
```

Output:

```
Image proxy created
Loading large-image.jpg
Displaying large-image.jpg
```

The important point is:

```
ImageProxy created
        ↓
RealImage NOT loaded
        ↓
display()
        ↓
RealImage created
        ↓
Image loaded
```

This is lazy loading.

## 12. Real-Time Example — Caching Proxy

Suppose an application repeatedly requests:

`getUser(100)`

Calling the actual database every time can be expensive.

Without Proxy:

```
Client
  ↓
Database
  ↓
getUser(100)

Client
  ↓
Database
  ↓
getUser(100)

Client
  ↓
Database
  ↓
getUser(100)
```

With a caching Proxy:

```
Client
   |
   ↓
Cache Proxy
   |
   +── Is data in cache?
   |       |
   |      YES
   |       ↓
   |    Return data
   |
   +── NO
         ↓
      Database
```

## 13. Java Caching Proxy Example

```java
interface UserService {

    String getUser(int id);
}
```

Real service:

```java
class RealUserService
        implements UserService {

    @Override
    public String getUser(int id) {

        System.out.println(
                "Fetching user from database..."
        );

        return "User-" + id;
    }
}
```

Proxy:

```java
import java.util.HashMap;
import java.util.Map;

class UserServiceProxy
        implements UserService {

    private RealUserService realService =
            new RealUserService();

    private Map<Integer, String> cache =
            new HashMap<>();

    @Override
    public String getUser(int id) {

        if (cache.containsKey(id)) {

            System.out.println(
                    "Returning user from cache..."
            );

            return cache.get(id);
        }

        String user =
                realService.getUser(id);

        cache.put(id, user);

        return user;
    }
}
```

Client:

```java
public class Main {

    public static void main(String[] args) {

        UserService userService =
                new UserServiceProxy();

        System.out.println(
                userService.getUser(101)
        );

        System.out.println(
                userService.getUser(101)
        );
    }
}
```

Output:

```
Fetching user from database...
User-101

Returning user from cache...
User-101
```

The second call doesn't hit the database.

This is a:

Caching Proxy

## 14. Real-Time Example — Remote Proxy

Another important type is a Remote Proxy.

Suppose your application needs to communicate with another server:

```
Application
    |
    ↓
Remote Proxy
    |
    ↓
Remote Server
```

The client calls:

```java
service.getCustomer();
```

The Proxy handles:

- Request serialization
- Network communication
- Response deserialization
- Error handling

The client doesn't need to manage the network communication directly.

This concept appears in distributed systems and remote-service technologies.

## 15. Real-Time Example — Virtual Proxy

A Virtual Proxy delays creation of an expensive object.

Example:

- Large Image
- Large Video
- Large PDF
- Expensive Database Connection
- Expensive Object

Instead of:

```
Application
 ↓
Create expensive object immediately
```

we use:

```
Application
 ↓
Proxy
 ↓
Create expensive object only when needed
```

This is called:

Lazy Initialization / Virtual Proxy

## 16. Types of Proxy

There are several common types.

### 1. Virtual Proxy

Used for:

Lazy loading / expensive objects

Example:

Large Image

### 2. Protection Proxy

Used for:

Access control

Example:

```
User
 ↓
Security Proxy
 ↓
Bank Account
```

### 3. Remote Proxy

Used for:

Accessing an object on another machine/server

```
Client
 ↓
Remote Proxy
 ↓
Remote Server
```

### 4. Caching Proxy

Used for:

Avoiding expensive repeated operations

```
Client
 ↓
Cache Proxy
 ↓
Database/API
```

### 5. Logging Proxy

Used for:

Logging method calls

```
Client
 ↓
Logging Proxy
 ↓
Real Service
```

The Proxy can log:

- Method called
- Input parameters
- Execution time
- Result
- Exception

## 17. Real-Time Example — Logging Proxy

Suppose:

```java
interface PaymentService {

    void pay(double amount);
}
```

Real service:

```java
class RealPaymentService
        implements PaymentService {

    @Override
    public void pay(double amount) {

        System.out.println(
                "Processing payment: ₹" + amount
        );
    }
}
```

Proxy:

```java
class PaymentLoggingProxy
        implements PaymentService {

    private RealPaymentService realService =
            new RealPaymentService();

    @Override
    public void pay(double amount) {

        System.out.println(
                "Payment request received"
        );

        realService.pay(amount);

        System.out.println(
                "Payment request completed"
        );
    }
}
```

Client:

```java
PaymentService payment =
        new PaymentLoggingProxy();

payment.pay(1000);
```

Output:

```
Payment request received
Processing payment: ₹1000.0
Payment request completed
```

The Proxy adds logging without modifying the real service.

## 18. Proxy vs Adapter

This is an important interview question.

### Adapter

The interfaces are incompatible.

```
Client
  ↓
Adapter
  ↓
Existing Class
```

Purpose:

Convert interface

Example:

```
pay()
 ↓
Adapter
 ↓
makePayment()
```

### Proxy

The interface is generally the same.

```
Client
  ↓
Proxy
  ↓
Real Object
```

Purpose:

Control access / add behavior around the real object

Examples:

- Caching
- Security
- Lazy loading
- Logging
- Remote access

### Remember:

```
Adapter → Converts

Proxy   → Controls
```

## 19. Proxy vs Facade

You've already studied Facade.

### Facade

Simplifies a complex subsystem:

```
Client
  ↓
Facade
  ↓
Service A
Service B
Service C
```

### Proxy

Controls access to one underlying object/service:

```
Client
  ↓
Proxy
  ↓
Real Object
```

Remember:

```
Facade → Simplifies

Proxy  → Controls access
```

## 20. Proxy vs Decorator

This is another common interview question.

### Decorator

Adds responsibilities/behavior to an object.

```
Object
  ↓
Decorator
  ↓
More behavior
```

Example:

```
Coffee
 ↓
Milk Decorator
 ↓
Sugar Decorator
```

### Proxy

Controls access to the object.

```
Client
 ↓
Proxy
 ↓
Real Object
```

Examples:

- Security
- Caching
- Lazy loading
- Remote access

The distinction can sometimes overlap in implementation, but their intent differs:

```
Decorator → Add behavior

Proxy     → Control access
```

## 21. Proxy vs Flyweight

You just studied Flyweight.

### Flyweight

Shares objects to reduce memory:

```
Object 1 ──┐
Object 2 ──┼──→ Shared Flyweight
Object 3 ──┘
```

### Proxy

Controls access to an object:

```
Client
  ↓
Proxy
  ↓
Real Object
```

Remember:

```
Flyweight → Share

Proxy     → Control
```

## 22. Proxy vs Singleton

### Singleton

Ensures only one instance exists:

```
Class
 ↓
One Object
```

### Proxy

Provides controlled access to another object:

```
Client
 ↓
Proxy
 ↓
Real Object
```

They solve completely different problems.

```
Singleton → One instance

Proxy     → Controlled access
```

## 23. Where Do We See Proxy in Real Applications?

Proxy is extremely common in enterprise software.

Examples include:

- Security proxies
- Caching proxies
- API gateways
- Reverse proxies
- Lazy loading
- Remote service proxies
- Logging proxies
- Transaction proxies

For example:

```
Mobile App
    ↓
API Gateway / Proxy
    ↓
Backend Service
```

The proxy/gateway can handle things such as:

- Authentication
- Authorization
- Rate limiting
- Routing
- Logging
- Caching

## 24. Proxy in Spring

If you're working with Java/Spring, this pattern is especially important.

Spring frequently uses proxies to provide framework features such as:

- `@Transactional`
- `@Cacheable`
- `@Async`
- `@PreAuthorize`

Conceptually:

```
Client
   |
   ↓
Spring Proxy
   |
   +── Transaction
   +── Security
   +── Caching
   |
   ↓
Actual Bean
```

For example, with:

```java
@Transactional
public void transferMoney() {
    // business logic
}
```

conceptually, a proxy can intercept the method call and manage transaction behavior around the actual method.

So when you work with Spring, understanding Proxy is particularly useful.

## 25. Advantages

### 1. Access control

Proxy can check permissions before accessing the real object.

### 2. Lazy loading

Expensive objects can be created only when required.

### 3. Caching

Repeated expensive operations can be avoided.

### 4. Logging

Calls can be logged without modifying the real object.

### 5. Remote access

Proxy can hide network communication.

### 6. Separation of concerns

The real object can focus on business logic while the Proxy handles:

- Security
- Logging
- Caching
- Transactions

## 26. Disadvantages

### 1. Additional complexity

You introduce another class:

`Proxy`

### 2. Additional layer

Every request goes through:

```
Client → Proxy → Real Object
```

### 3. Debugging can be slightly harder

There is another layer between the client and real object.

### 4. Poorly designed proxies can become too complex

A Proxy should not become responsible for unrelated business logic.

## 27. Complete Proxy Architecture

Remember this diagram:

```
                       CLIENT
                          |
                          ↓
                  +---------------+
                  |     PROXY     |
                  +---------------+
                    /     |      \
                   /      |       \
                  ↓       ↓        ↓
              Security  Cache    Logging
                   \      |       /
                    \     |      /
                          ↓
                  +---------------+
                  | REAL OBJECT   |
                  +---------------+
                          |
                          ↓
                   Actual Operation
```

## 28. Interview Answer

If an interviewer asks:

**What is Proxy Design Pattern?**

You can answer:

Proxy is a structural design pattern that provides a substitute or placeholder for another object and controls access to the real object. The Proxy and Real Object usually implement the same interface, allowing the client to interact with the Proxy without knowing whether it is communicating directly with the real object.

**Real-time example:**

A security proxy can stand between a user and a bank account service. Before forwarding a withdrawal request to the real bank service, the proxy can authenticate the user and check authorization. Other common uses include caching proxies, virtual proxies for lazy loading, logging proxies, and remote proxies.

## 29. Easy Way to Remember

Think of a security guard at an office:

```
                Employee
                   |
                   ↓
             Security Guard
                 Proxy
                   |
             "Are you allowed?"
                   |
             +-----+-----+
             |           |
            YES          NO
             |           |
             ↓           ↓
        Office       Access Denied
```

The security guard isn't the actual office.

The guard controls access to the office.

That's exactly the Proxy idea.

**One-line memory trick:**

> Proxy Pattern = A substitute that stands in front of the real object and controls or manages access to it.

And for all the structural patterns you've covered:

| Pattern | Main Purpose |
|---|---|
| Adapter | Convert incompatible interfaces |
| Bridge | Separate abstraction from implementation |
| Composite | Represent part-whole tree structures |
| Decorator | Add behavior dynamically |
| Facade | Simplify a complex subsystem |
| Flyweight | Share objects and reduce memory |
| Proxy | Control access to an object |

### Final shortcut

```
Adapter   → Convert
Bridge    → Separate
Composite → Tree
Decorator → Add
Facade    → Simplify
Flyweight → Share
Proxy     → Control
```
