# Single Responsibility Principle (SRP)

The Single Responsibility Principle is the S in the SOLID principles.

> A class should have one responsibility and therefore one reason to change.

A common simplified version is:

> One class should do one job.

But the more accurate definition is "one reason to change."

---

## 1. What does "responsibility" mean?

A responsibility is something a class is responsible for doing.

For example, imagine an e-commerce application has an Order class.

An order may involve:

```
Order
 ├── Calculate total
 ├── Save order to database
 ├── Send email
 ├── Generate invoice
 └── Process payment
```

If one class does all of these, it has too many responsibilities.

```java
class Order {


    public void calculateTotal() {
    }


    public void saveToDatabase() {
    }


    public void sendEmail() {
    }


    public void generateInvoice() {
    }


    public void processPayment() {
    }
}
```

This violates SRP.

---

## 2. Why does this violate SRP?

Ask:

> How many reasons can this class change?

Order could change because:

**Reason 1 — Business calculation changes**

```java
calculateTotal()
```

For example, discount rules change.

**Reason 2 — Database changes**

```java
saveToDatabase()
```

For example:

```
MySQL → PostgreSQL
```

**Reason 3 — Email requirements change**

```java
sendEmail()
```

For example:

```
SMTP → SendGrid
```

**Reason 4 — Invoice format changes**

```java
generateInvoice()
```

For example:

```
PDF → HTML
```

**Reason 5 — Payment changes**

```java
processPayment()
```

For example:

```
UPI → Stripe
```

So one class has many reasons to change.

---

## 3. Real-time example — E-commerce Order

Imagine Amazon/Flipkart-like software.

An order goes through:

```
Customer
   |
   v
Create Order
   |
   +---- Calculate Price
   |
   +---- Save Order
   |
   +---- Process Payment
   |
   +---- Generate Invoice
   |
   +---- Send Notification
```

A bad design puts everything into one class.

---

## 4. ❌ Bad Design

```java
class Order {


    public double calculateTotal() {


        // Calculate order total
        return 5000;
    }


    public void saveOrder() {


        // Save to database
        System.out.println("Order saved");
    }


    public void processPayment() {


        // Payment logic
        System.out.println("Payment processed");
    }


    public void generateInvoice() {


        // Invoice generation
        System.out.println("Invoice generated");
    }


    public void sendEmail() {


        // Email logic
        System.out.println("Email sent");
    }
}
```

This class is doing too much.

```
Order
 ├── Business logic
 ├── Database
 ├── Payment
 ├── Invoice
 └── Email
```

❌ This violates SRP.

---

## 5. How do we fix it?

Separate responsibilities into different classes.

```
Order
   |
   +── OrderCalculator
   |
   +── OrderRepository
   |
   +── PaymentService
   |
   +── InvoiceService
   |
   +── EmailService
```

Each class has a focused responsibility.

---

## 6. Order class

The Order class should represent the order itself.

```java
class Order {


    private int orderId;
    private double amount;


    public Order(int orderId, double amount) {
        this.orderId = orderId;
        this.amount = amount;
    }


    public double getAmount() {
        return amount;
    }
}
```

Its responsibility is related to order data/domain behavior, not email, database, and payment infrastructure.

---

## 7. Order Calculator

Its responsibility:

> Calculate the order price.

```java
class OrderCalculator {


    public double calculateTotal(Order order) {


        double discount = 500;


        return order.getAmount() - discount;
    }
}
```

Now:

```
OrderCalculator
       |
       └── Calculate order total
```

One responsibility.

---

## 8. Order Repository

Its responsibility:

> Save and retrieve orders.

```java
class OrderRepository {


    public void save(Order order) {


        System.out.println(
                "Order saved to database"
        );
    }
}
```

Now:

```
OrderRepository
       |
       └── Database operations
```

One responsibility.

---

## 9. Payment Service

Its responsibility:

> Process payments.

```java
class PaymentService {


    public void processPayment(double amount) {


        System.out.println(
                "Payment processed: ₹" + amount
        );
    }
}
```

Now:

```
PaymentService
       |
       └── Payment processing
```

One responsibility.

---

## 10. Invoice Service

Its responsibility:

> Generate invoices.

```java
class InvoiceService {


    public void generateInvoice(Order order) {


        System.out.println(
                "Invoice generated"
        );
    }
}
```

---

## 11. Email Service

Its responsibility:

> Send emails.

```java
class EmailService {


    public void sendEmail(String email) {


        System.out.println(
                "Email sent to " + email
        );
    }
}
```

---

## 12. Now the design looks like this

```
                    Order
                      |
          +-----------+-----------+
          |           |           |
          v           v           v
  OrderCalculator  Repository  PaymentService
          |
          |
    Calculate total




                    Order
                      |
              +-------+-------+
              |               |
              v               v
       InvoiceService    EmailService
```

Each class has a focused responsibility.

---

## 13. Complete example

```java
class Order {


    private int orderId;
    private double amount;


    public Order(int orderId, double amount) {
        this.orderId = orderId;
        this.amount = amount;
    }


    public double getAmount() {
        return amount;
    }
}




class OrderCalculator {


    public double calculateTotal(Order order) {


        double discount = 500;


        return order.getAmount() - discount;
    }
}




class OrderRepository {


    public void save(Order order) {


        System.out.println(
                "Order saved to database"
        );
    }
}




class PaymentService {


    public void processPayment(double amount) {


        System.out.println(
                "Payment processed: ₹" + amount
        );
    }
}




class InvoiceService {


    public void generateInvoice(Order order) {


        System.out.println(
                "Invoice generated"
        );
    }
}




class EmailService {


    public void sendEmail(String email) {


        System.out.println(
                "Email sent to " + email
        );
    }
}
```

Now the application can use them:

```java
public class Main {


    public static void main(String[] args) {


        Order order =
                new Order(101, 5000);


        OrderCalculator calculator =
                new OrderCalculator();


        OrderRepository repository =
                new OrderRepository();


        PaymentService paymentService =
                new PaymentService();


        InvoiceService invoiceService =
                new InvoiceService();


        EmailService emailService =
                new EmailService();


        double total =
                calculator.calculateTotal(order);


        paymentService.processPayment(total);


        repository.save(order);


        invoiceService.generateInvoice(order);


        emailService.sendEmail(
                "customer@gmail.com"
        );
    }
}
```

Now every class has a clear purpose.

---

## 14. Another real-time example — Employee Management

Suppose we have:

```java
class Employee {


    public void calculateSalary() {
    }


    public void saveEmployee() {
    }


    public void generateReport() {
    }


    public void sendEmail() {
    }
}
```

❌ This violates SRP.

Why?

Because there are multiple reasons to change:

```
Salary calculation changes
        ↓
Employee changes


Database changes
        ↓
Employee changes


Report format changes
        ↓
Employee changes


Email provider changes
        ↓
Employee changes
```

Instead:

```
Employee
   |
   +── SalaryCalculator
   |
   +── EmployeeRepository
   |
   +── EmployeeReport
   |
   +── EmailService
```

Now:

```java
class SalaryCalculator {


    public double calculate(Employee employee) {
        // salary calculation
        return 50000;
    }
}
```

```java
class EmployeeRepository {


    public void save(Employee employee) {
        // database operation
    }
}
```

```java
class EmployeeReport {


    public void generate(Employee employee) {
        // report generation
    }
}
```

```java
class EmailService {


    public void send(String email) {
        // email operation
    }
}
```

Much cleaner.

---

## 15. Real-time example — Banking Application

Consider a banking system.

❌ Bad design:

```java
class BankAccount {


    public void deposit() {
    }


    public void withdraw() {
    }


    public void saveToDatabase() {
    }


    public void sendSMS() {
    }


    public void generateStatement() {
    }
}
```

This class has too many responsibilities.

A better design:

```
BankAccount
     |
     +── AccountService
     |
     +── AccountRepository
     |
     +── NotificationService
     |
     +── StatementService
```

**AccountService**

- Deposit
- Withdraw
- Transfer

**AccountRepository**

- Save
- Find
- Update

**NotificationService**

- SMS
- Email
- Push notification

**StatementService**

- Generate bank statement

Each class has a focused purpose.

---

## 16. "One class should do one thing" — Is that always true?

This is an important point.

SRP does not literally mean:

> "A class should contain only one method."

For example:

```java
class EmailService {


    sendEmail();
    validateEmail();
    formatEmail();
}
```

This can still be perfectly fine if these operations belong to the same responsibility.

The real question is:

> Do these methods belong to the same reason for change?

If yes, keeping them together may be appropriate.

---

## 17. The "one reason to change" test

When reviewing a class, ask:

> "Who might ask me to modify this class?"

For example:

```java
class OrderService {


    calculatePrice();
    saveOrder();
    sendEmail();
}
```

Possible people/teams:

```
Business team
     ↓
calculatePrice()


Database team
     ↓
saveOrder()


Notification team
     ↓
sendEmail()
```

That's a warning sign.

There are multiple independent reasons to modify the class.

---

## 18. Benefits of SRP

**✅ Easier maintenance**

If payment logic changes:

```
PaymentService
```

is the place to look.

You don't need to search through a giant Order class.

**✅ Easier testing**

You can test:

```
PaymentService
```

independently.

**✅ Less coupling**

Classes don't need to know unrelated responsibilities.

**✅ Better readability**

A class name tells you what it does.

```
PaymentService
OrderRepository
InvoiceService
EmailService
```

**✅ Easier changes**

Changing invoice generation shouldn't require modifying order persistence.

---

## 19. SRP and Spring Boot

You'll see SRP frequently in Spring applications.

A typical structure might look like:

```
Controller
    ↓
Service
    ↓
Repository
```

For example:

```java
@RestController
class OrderController {


    private final OrderService orderService;


    // Handle HTTP requests
}
```

```java
@Service
class OrderService {


    private final OrderRepository orderRepository;


    // Business logic
}
```

```java
@Repository
class OrderRepository {


    // Database operations
}
```

The responsibilities are separated:

```
Controller
   ↓
HTTP/API responsibility


Service
   ↓
Business responsibility


Repository
   ↓
Database responsibility
```

This separation is closely related to SRP.

---

## 20. SRP vs Separation of Concerns

They are related but not exactly the same.

### Separation of Concerns

A broader architectural idea:

> Keep different concerns separate.

For example:

- UI
- Business Logic
- Database
- Security
- Logging

### SRP

A class-level principle:

> A class should have one responsibility / one reason to change.

So:

```
Separation of Concerns → broader concept


SRP → focused principle for responsibility boundaries
```

---

## 21. Interview answer

If an interviewer asks:

> "What is Single Responsibility Principle?"

You can say:

> The Single Responsibility Principle states that a class should have one responsibility and one reason to change. It means a class should focus on a cohesive piece of functionality rather than handling unrelated responsibilities.

Real-time example:

> In an e-commerce application, an Order class shouldn't calculate prices, save data to the database, process payments, generate invoices, and send emails. These responsibilities should be separated into classes such as OrderCalculator, OrderRepository, PaymentService, InvoiceService, and EmailService.

---

## 22. Easy way to remember SRP

Think of a restaurant.

❌ One person:

```
Chef
 ├── Cook food
 ├── Take orders
 ├── Collect payment
 ├── Clean tables
 └── Manage inventory
```

Too many responsibilities.

✅ Better:

```
Waiter
   ↓
Take orders


Chef
   ↓
Cook food


Cashier
   ↓
Handle payment


Cleaner
   ↓
Clean tables


Manager
   ↓
Manage restaurant
```

Each role has a focused responsibility.

### In programming:

```
One class
    ↓
One responsibility
    ↓
One primary reason to change
```

> SRP = Keep a class focused. Don't create a "God class" that does everything.
