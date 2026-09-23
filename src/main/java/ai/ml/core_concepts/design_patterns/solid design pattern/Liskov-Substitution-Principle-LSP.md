# Liskov Substitution Principle (LSP)

The Liskov Substitution Principle is the L in SOLID.

> Objects of a superclass should be replaceable with objects of its subclasses without breaking the correctness of the program.

In simpler words:

> If B is a subtype of A, you should be able to use B wherever A is expected, and the program should still work correctly.

---

## 1. Simple Example

Suppose we have:

```java
class Bird {


    public void fly() {
        System.out.println("Bird is flying");
    }
}
```

And:

```java
class Sparrow extends Bird {


    @Override
    public void fly() {
        System.out.println("Sparrow is flying");
    }
}
```

This is fine.

We can do:

```java
Bird bird = new Sparrow();


bird.fly();
```

The program works correctly.

```
Bird
  ↑
  |
Sparrow
```

Sparrow can substitute Bird.

✅ This follows LSP.

---

## 2. The Famous Example — Bird and Penguin

Now suppose we create:

```java
class Bird {


    public void fly() {
        System.out.println("Bird is flying");
    }
}
```

Then:

```java
class Sparrow extends Bird {


    @Override
    public void fly() {
        System.out.println("Sparrow is flying");
    }
}
```

So far, everything is good.

But then we add:

```java
class Penguin extends Bird {


    @Override
    public void fly() {


        throw new UnsupportedOperationException(
                "Penguins cannot fly"
        );
    }
}
```

Now consider:

```java
public void makeBirdFly(Bird bird) {


    bird.fly();
}
```

We call:

```java
makeBirdFly(new Sparrow());
```

Works:

```
Sparrow is flying
```

But:

```java
makeBirdFly(new Penguin());
```

throws:

```
UnsupportedOperationException
```

💥 The program breaks.

This violates LSP.

---

## 3. Why does it violate LSP?

The parent class says:

```
Bird
  |
  +-- fly()
```

So any code using Bird reasonably expects:

```java
bird.fly();
```

to work.

But when we substitute:

```
Penguin
```

the behavior breaks.

```java
Bird bird = new Penguin();


bird.fly();  // 💥 Exception
```

Therefore:

> Penguin cannot properly substitute Bird in this design.

---

## 4. Correct Design

Instead of putting fly() directly into Bird, separate the capabilities.

```java
interface Bird {
}
```

Flying birds:

```java
interface FlyingBird extends Bird {


    void fly();
}
```

Now:

```java
class Sparrow implements FlyingBird {


    @Override
    public void fly() {
        System.out.println(
                "Sparrow is flying"
        );
    }
}
```

Penguin:

```java
class Penguin implements Bird {


    public void swim() {


        System.out.println(
                "Penguin is swimming"
        );
    }
}
```

Now:

```
             Bird
            /    \
           /      \
      Sparrow    Penguin
         |
    FlyingBird
```

There is no false promise that every Bird can fly.

---

## 5. Real-time Example — Payment System

Let's use a practical software example.

Suppose we have:

```java
class Payment {


    public void pay(double amount) {
        System.out.println(
                "Payment processed"
        );
    }


    public void refund(double amount) {
        System.out.println(
                "Payment refunded"
        );
    }
}
```

Now we have:

```java
class CreditCardPayment extends Payment {
}
```

and:

```java
class CashPayment extends Payment {


    @Override
    public void refund(double amount) {


        throw new UnsupportedOperationException(
                "Cash cannot be refunded"
        );
    }
}
```

Now:

```java
void processRefund(Payment payment) {


    payment.refund(500);
}
```

We can pass:

```java
processRefund(
        new CreditCardPayment()
);
```

Works.

But:

```java
processRefund(
        new CashPayment()
);
```

💥 Exception.

This means CashPayment cannot safely substitute Payment for code that expects refund behavior.

---

## 6. Better Payment Design

Separate payment capabilities.

```java
interface Payment {


    void pay(double amount);
}
```

Refundable payments:

```java
interface RefundablePayment
        extends Payment {


    void refund(double amount);
}
```

Credit Card:

```java
class CreditCardPayment
        implements RefundablePayment {


    @Override
    public void pay(double amount) {


        System.out.println(
                "Paid using credit card"
        );
    }


    @Override
    public void refund(double amount) {


        System.out.println(
                "Refunded to credit card"
        );
    }
}
```

Cash:

```java
class CashPayment
        implements Payment {


    @Override
    public void pay(double amount) {


        System.out.println(
                "Paid using cash"
        );
    }
}
```

Now the design accurately represents the capabilities.

```
                 Payment
                /       \
               /         \
      CreditCard          Cash
          |
   RefundablePayment
```

---

## 7. Real-time Example — Notification

Imagine:

```java
class Notification {


    public void send() {
        System.out.println("Sending notification");
    }


    public void attachFile() {
        System.out.println("Attaching file");
    }
}
```

Now:

```java
class EmailNotification
        extends Notification {
}
```

Fine.

But:

```java
class SMSNotification
        extends Notification {


    @Override
    public void attachFile() {


        throw new UnsupportedOperationException(
                "SMS does not support attachments"
        );
    }
}
```

Problem:

```java
void sendNotification(Notification notification) {


    notification.attachFile();
}
```

This works for Email:

```
Email → attachment supported
```

but breaks for SMS:

```
SMS → exception
```

❌ LSP violation.

---

## 8. Better Notification Design

Separate the capabilities:

```java
interface Notification {


    void send();
}
```

Then:

```java
interface AttachmentSupported
        extends Notification {


    void attachFile();
}
```

Email:

```java
class EmailNotification
        implements AttachmentSupported {


    public void send() {
        System.out.println("Email sent");
    }


    public void attachFile() {
        System.out.println("File attached");
    }
}
```

SMS:

```java
class SMSNotification
        implements Notification {


    public void send() {
        System.out.println("SMS sent");
    }
}
```

Now the design doesn't force SMS to support something it cannot do.

---

## 9. Real-time Example — Vehicle

Here's another common example.

Suppose:

```java
class Vehicle {


    public void startEngine() {
        System.out.println("Engine started");
    }
}
```

Then:

```java
class Car extends Vehicle {
}
```

Works.

But what about an electric vehicle?

If your design assumes:

```java
class Vehicle {


    public void startEngine() {
        // Start petrol/diesel engine
    }
}
```

and:

```java
class ElectricVehicle extends Vehicle {


    @Override
    public void startEngine() {


        throw new UnsupportedOperationException(
                "Electric vehicle has no engine"
        );
    }
}
```

Now:

```java
void startVehicle(Vehicle vehicle) {


    vehicle.startEngine();
}
```

Calling:

```java
startVehicle(new ElectricVehicle());
```

breaks.

The abstraction is wrong.

---

## 10. Better Vehicle Design

Instead of assuming every vehicle has an engine:

```java
interface Vehicle {


    void start();
}
```

Petrol car:

```java
class PetrolCar implements Vehicle {


    public void start() {


        System.out.println(
                "Starting petrol engine"
        );
    }
}
```

Electric car:

```java
class ElectricCar implements Vehicle {


    public void start() {


        System.out.println(
                "Starting electric motor"
        );
    }
}
```

Now:

```java
Vehicle vehicle =
        new ElectricCar();


vehicle.start();
```

Works correctly.

Both are valid substitutes for Vehicle.

---

## 11. Real-time Example — File Storage

Imagine an application supports:

- Local Storage
- AWS S3
- Google Cloud Storage
- Azure Blob Storage

You might define:

```java
interface Storage {


    void upload(String file);


    void download(String file);


    void delete(String file);
}
```

But suppose one storage implementation doesn't support deletion:

```java
class ReadOnlyStorage
        implements Storage {


    public void upload(String file) {
    }


    public void download(String file) {
    }


    public void delete(String file) {


        throw new UnsupportedOperationException();
    }
}
```

Now any code expecting:

```java
Storage storage
```

might call:

```java
storage.delete("file.txt");
```

and break.

❌ LSP violation.

The interface promises something that the implementation cannot honor.

---

## 12. What LSP is really about

LSP is not simply about inheritance.

The deeper idea is about behavioral compatibility.

If:

```java
Parent parent = new Child();
```

then the Child should respect the expectations established by Parent.

For example:

```java
class Parent {


    public int getValue() {
        return 10;
    }
}
```

If code expects:

```java
Parent p = new Child();


int value = p.getValue();
```

the child shouldn't unexpectedly:

- throw exception

or violate important assumptions made by the parent contract.

---

## 13. LSP and Method Overriding

Consider:

```java
class Account {


    public void withdraw(double amount) {
        System.out.println("Withdraw");
    }
}
```

Then:

```java
class SavingsAccount extends Account {
}
```

Fine.

But:

```java
class ReadOnlyAccount extends Account {


    @Override
    public void withdraw(double amount) {


        throw new UnsupportedOperationException(
                "Cannot withdraw"
        );
    }
}
```

Now:

```java
void withdrawMoney(Account account) {


    account.withdraw(1000);
}
```

If we pass:

```java
withdrawMoney(
        new ReadOnlyAccount()
);
```

the method unexpectedly breaks.

This is a strong signal that the inheritance relationship is wrong.

---

## 14. How to fix it?

Instead of:

```
Account
   |
   +── SavingsAccount
   |
   +── ReadOnlyAccount
```

Use separate capabilities.

```java
interface Account {
}
```

```java
interface WithdrawableAccount
        extends Account {


    void withdraw(double amount);
}
```

Then:

```java
class SavingsAccount
        implements WithdrawableAccount {


    public void withdraw(double amount) {
        System.out.println(
                "Withdrawal successful"
        );
    }
}
```

Read-only:

```java
class ReadOnlyAccount
        implements Account {
}
```

Now only accounts that support withdrawal implement WithdrawableAccount.

---

## 15. LSP Rules to Remember

A subtype should not unexpectedly:

**❌ Remove behavior**

Parent:

```java
withdraw()
```

Child:

```java
throw UnsupportedOperationException
```

**❌ Change expected behavior**

Parent:

```java
getBalance() → returns balance
```

Child:

```java
getBalance() → returns something unrelated
```

**❌ Strengthen requirements**

If parent accepts:

```
amount > 0
```

child shouldn't suddenly require:

```
amount > 10,000
```

without the abstraction accounting for that contract.

**❌ Weaken guarantees**

If the parent promises:

```
method always returns a valid result
```

the child shouldn't unexpectedly return an invalid result.

---

## 16. Relationship with OCP

LSP and OCP are closely related.

OCP says:

> We should be able to extend behavior without breaking existing code.

LSP says:

> The new subtype should behave correctly wherever the base abstraction is expected.

For example:

```
             Payment
                ↑
       +--------+--------+
       |        |        |
      UPI      Card     PayPal
```

OCP:

```
Add new payment type
        ↓
Create new class
```

LSP:

```
New payment type
        ↓
Can safely be used as Payment
```

So:

```
OCP → Can I add it?


LSP → Can I safely substitute it?
```

---

## 17. Relationship with SRP

You already learned SRP.

**SRP**

Class should have one responsibility.

**OCP**

Extend behavior without modifying stable code.

**LSP**

Subtypes should be safely substitutable.

Together:

```
SRP
 ↓
Keep responsibilities focused


OCP
 ↓
Make extensions easier


LSP
 ↓
Make those extensions behave correctly
```

---

## 18. Interview Answer

If the interviewer asks:

> "What is Liskov Substitution Principle?"

You can answer:

> The Liskov Substitution Principle states that objects of a subclass should be replaceable for objects of its superclass without changing the correctness or expected behavior of the program. A subclass should honor the contract of its parent rather than throwing unsupported-operation exceptions or changing expected behavior.

Real-time example:

> Suppose Payment has both pay() and refund(). If CashPayment extends Payment but throws an exception from refund() because cash doesn't support refunds, then CashPayment cannot safely substitute Payment. A better design is to separate Payment and RefundablePayment capabilities so only payment methods that actually support refunds implement the refund operation.

---

## 19. The easiest way to remember LSP

Think:

> "If I replace the parent with the child, will my program still behave correctly?"

For example:

```java
Payment payment =
        new CreditCardPayment();
```

If the program expects a Payment, CreditCardPayment should work.

But if:

```java
Payment payment =
        new CashPayment();
```

and suddenly:

```
💥 UnsupportedOperationException
```

then your abstraction probably has a problem.

### One-line memory trick:

> LSP = Child should be a proper replacement for Parent.

Or even shorter:

> "If it says IS-A, it should behave like one."
