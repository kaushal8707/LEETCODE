# Interface Segregation Principle (ISP)

The Interface Segregation Principle is the I in SOLID.

> Clients should not be forced to depend on methods they do not use.

In simple words:

> Don't create one large interface containing many unrelated methods. Instead, split it into smaller, specific interfaces.

---

## 1. Simple Example

Suppose we create an interface for a machine:

```java
interface Machine {


    void print();


    void scan();


    void fax();


    void staple();
}
```

Now imagine we have a simple printer.

A simple printer only needs:

- print()

But because it implements Machine, it is forced to implement:

- print()
- scan()
- fax()
- staple()

That's the problem.

---

## 2. ❌ Bad Design — ISP Violation

```java
interface Machine {


    void print();


    void scan();


    void fax();
}
```

Now:

```java
class SimplePrinter implements Machine {


    @Override
    public void print() {
        System.out.println("Printing...");
    }


    @Override
    public void scan() {
        // Not supported
        throw new UnsupportedOperationException();
    }


    @Override
    public void fax() {
        // Not supported
        throw new UnsupportedOperationException();
    }
}
```

This is a bad design.

Why?

Because SimplePrinter doesn't need:

- scan()
- fax()

But it is forced to implement them.

This violates ISP.

---

## 3. Why is this a problem?

Imagine your interface has:

```
Machine
 ├── print()
 ├── scan()
 ├── fax()
 ├── copy()
 ├── staple()
 └── email()
```

Now you create:

```
SimplePrinter
```

It only supports:

- print()

But Java forces it to provide implementations for:

- scan()
- fax()
- copy()
- staple()
- email()

You may end up with:

```java
throw new UnsupportedOperationException();
```

everywhere.

That's a strong indication that the interface is too large.

---

## 4. Real-Time Example — Office Printer

Think about printers in a real office.

You may have:

**Simple Printer**

```
SimplePrinter
    |
    └── Print
```

**Scanner**

```
Scanner
    |
    └── Scan
```

**Fax Machine**

```
FaxMachine
    |
    └── Fax
```

**Multi-Function Printer**

```
MultiFunctionPrinter
    |
    +── Print
    +── Scan
    +── Fax
```

It would be wrong to create one giant interface:

```java
interface Machine {


    void print();


    void scan();


    void fax();
}
```

Instead, segregate it.

---

## 5. ✅ ISP Solution

Create small interfaces.

### Printer

```java
interface Printer {


    void print();
}
```

### Scanner

```java
interface Scanner {


    void scan();
}
```

### Fax

```java
interface Fax {


    void fax();
}
```

Now a simple printer:

```java
class SimplePrinter implements Printer {


    @Override
    public void print() {


        System.out.println(
                "Printing document..."
        );
    }
}
```

It only depends on what it needs.

---

## 6. Multi-Function Printer

A multi-function printer supports everything.

```java
class MultiFunctionPrinter
        implements Printer, Scanner, Fax {


    @Override
    public void print() {


        System.out.println(
                "Printing document..."
        );
    }


    @Override
    public void scan() {


        System.out.println(
                "Scanning document..."
        );
    }


    @Override
    public void fax() {


        System.out.println(
                "Sending fax..."
        );
    }
}
```

This is perfectly fine.

Because the MultiFunctionPrinter genuinely supports all three capabilities.

---

## 7. Visual Comparison

### ❌ Without ISP

```
              Machine
                 |
       +---------+---------+
       |         |         |
     print      scan      fax
       |
       ↓
 SimplePrinter


Problem:
SimplePrinter is forced
to implement scan() and fax().
```

### ✅ With ISP

```
              Printer
                 ↑
                 |
          SimplePrinter




              Scanner
                 ↑
                 |
          ScannerDevice




                Fax
                 ↑
                 |
            FaxMachine




        MultiFunctionPrinter
          /       |       \
         ↓        ↓        ↓
      Printer   Scanner    Fax
```

Each interface represents a specific capability.

---

## 8. Real-Time Example — Payment System

Let's take another practical example.

Suppose an application has:

```java
interface PaymentService {


    void pay();


    void refund();


    void generateInvoice();


    void recurringPayment();


    void chargeback();
}
```

Now consider Cash Payment.

Cash may support:

- pay()

But perhaps your design doesn't support:

- recurringPayment()
- chargeback()

You might end up with:

```java
class CashPayment implements PaymentService {


    public void pay() {
        System.out.println("Cash payment");
    }


    public void refund() {
        System.out.println("Cash refund");
    }


    public void generateInvoice() {
        System.out.println("Invoice generated");
    }


    public void recurringPayment() {
        throw new UnsupportedOperationException();
    }


    public void chargeback() {
        throw new UnsupportedOperationException();
    }
}
```

That's a design smell.

---

## 9. Better Payment Design

Separate the capabilities.

```java
interface Payment {


    void pay();
}
```

```java
interface Refundable {


    void refund();
}
```

```java
interface RecurringPayment {


    void processRecurringPayment();
}
```

```java
interface Chargeback {


    void processChargeback();
}
```

Now:

### Cash

```java
class CashPayment
        implements Payment, Refundable {


    public void pay() {


        System.out.println(
                "Cash payment"
        );
    }


    public void refund() {


        System.out.println(
                "Cash refund"
        );
    }
}
```

### Credit Card

```java
class CreditCardPayment
        implements Payment,
                   Refundable,
                   RecurringPayment,
                   Chargeback {


    public void pay() {
        System.out.println(
                "Credit card payment"
        );
    }


    public void refund() {
        System.out.println(
                "Credit card refund"
        );
    }


    public void processRecurringPayment() {
        System.out.println(
                "Recurring card payment"
        );
    }


    public void processChargeback() {
        System.out.println(
                "Chargeback processed"
        );
    }
}
```

Now each payment implementation only implements the capabilities it actually supports.

---

## 10. Real-Time Example — Employee System

Suppose we create:

```java
interface Employee {


    void work();


    void eat();


    void manageTeam();


    void writeCode();


    void conductInterview();
}
```

This can become problematic.

A developer might need:

- work()
- writeCode()

A manager might need:

- work()
- manageTeam()
- conductInterview()

An external contractor might only need:

- work()

But everyone is forced to implement everything.

❌ ISP violation.

---

## 11. Better Design

Separate responsibilities/capabilities:

```java
interface Workable {


    void work();
}
```

```java
interface Programmer {


    void writeCode();
}
```

```java
interface Manager {


    void manageTeam();
}
```

```java
interface Interviewer {


    void conductInterview();
}
```

Now:

```java
class Developer
        implements Workable, Programmer {


    public void work() {
        System.out.println("Working");
    }


    public void writeCode() {
        System.out.println("Writing code");
    }
}
```

Manager:

```java
class EngineeringManager
        implements Workable,
                   Manager,
                   Interviewer {


    public void work() {
        System.out.println("Managing engineering work");
    }


    public void manageTeam() {
        System.out.println("Managing team");
    }


    public void conductInterview() {
        System.out.println("Conducting interview");
    }
}
```

The interfaces are small and focused.

---

## 12. ISP vs SRP

Since you've already learned SRP, this distinction is important.

### SRP

A class should have one responsibility.

Example:

```
PaymentService
       ↓
Payment processing
```

### ISP

An interface should not force clients to depend on methods they don't need.

Example:

```
Large Interface
       ↓
Split into
       ↓
Small interfaces
```

So:

```
SRP → Focus the CLASS


ISP → Focus the INTERFACE
```

---

## 13. ISP vs LSP

These two are also closely related.

### LSP

A subtype should be safely substitutable for its parent.

Example:

```
Rectangle
    ↑
Square
```

If Square breaks assumptions made by Rectangle, that's an LSP problem.

### ISP

Don't force a class to implement methods it doesn't need.

Example:

```
Machine
 ├── print()
 ├── scan()
 └── fax()


SimplePrinter
     ↓
Only needs print()
```

So:

```
LSP → Can the child safely replace the parent?


ISP → Is the interface too big for the client?
```

---

## 14. ISP in Spring Boot

You'll commonly see ISP when defining service interfaces.

### ❌ Large interface

```java
interface UserService {


    void createUser();


    void updateUser();


    void deleteUser();


    void generateReport();


    void sendNotification();


    void exportData();
}
```

Not every implementation may need all of these.

A better design might be:

```java
interface UserManagement {


    void createUser();


    void updateUser();


    void deleteUser();
}
```

```java
interface UserReporting {


    void generateReport();
}
```

```java
interface UserNotification {


    void sendNotification();
}
```

```java
interface UserExport {


    void exportData();
}
```

Now consumers depend only on the capability they need.

---

## 15. The key idea behind ISP

Think about a TV remote.

You don't want a remote interface with 100 methods:

```
Remote
 ├── turnOn()
 ├── turnOff()
 ├── changeChannel()
 ├── volumeUp()
 ├── volumeDown()
 ├── Netflix()
 ├── YouTube()
 ├── Bluetooth()
 ├── HDMI()
 ├── ...
 └── 90 more methods
```

If a simple device only supports:

- turnOn()
- turnOff()

it shouldn't be forced to implement everything else.

Instead, think in terms of capabilities:

```
PowerControl
   ├── turnOn()
   └── turnOff()


VolumeControl
   ├── volumeUp()
   └── volumeDown()


ChannelControl
   ├── nextChannel()
   └── previousChannel()


Streaming
   ├── Netflix()
   └── YouTube()
```

Clients can use only what they need.

---

## 16. How to identify an ISP violation

Look for these warning signs:

**🚩 1. Many methods in one interface**

```java
interface Everything {


    void method1();
    void method2();
    void method3();
    // 20 more...
}
```

**🚩 2. UnsupportedOperationException**

```java
@Override
public void scan() {
    throw new UnsupportedOperationException();
}
```

This is a strong signal.

**🚩 3. Empty implementations**

```java
@Override
public void fax() {
    // Do nothing
}
```

Another warning sign.

**🚩 4. Implementations only use a small portion of an interface**

If:

```
Interface → 15 methods
Implementation → actually needs 3
```

consider splitting the interface.

---

## 17. Interview Answer

If an interviewer asks:

> What is Interface Segregation Principle?

You can say:

> The Interface Segregation Principle states that clients should not be forced to depend on methods they do not use. Instead of creating large, general-purpose interfaces, we should create smaller, focused interfaces based on specific capabilities.

Real-time example:

> Consider a printer system with print(), scan(), and fax() methods. A simple printer only supports printing, so forcing it to implement scan() and fax() violates ISP. Instead, we can create separate Printer, Scanner, and Fax interfaces. A simple printer implements only Printer, while a multifunction printer can implement all three.

---

## 18. Easy way to remember ISP

Think:

### ❌ BIG INTERFACE

```
        Machine
       /   |   \
   print scan fax
       |
       ↓
SimplePrinter
```

Don't force the simple printer to implement everything.

Instead:

### ✅ SMALL INTERFACES

```
Printer     Scanner      Fax
   ↑           ↑           ↑
   |           |           |
Simple      Scanner    FaxMachine
Printer      Device
```

### One-line memory trick:

> ISP = Don't force a class to depend on methods it doesn't need.

Or:

```
SRP → One class, one responsibility


OCP → Extend without unnecessary modification


LSP → Child should safely replace parent


ISP → Keep interfaces small and focused
```
