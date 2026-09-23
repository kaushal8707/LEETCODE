# Prototype Design Pattern

The Prototype Design Pattern is a Creational Design Pattern used when we want to create a new object by copying an existing object, called the prototype.

## Simple definition

Prototype Pattern creates new objects by cloning an existing object instead of creating the object from scratch.

The easiest way to remember it:

Normal approach:

```
new Object()
     ↓
Create everything from scratch
```

Prototype:

```
Existing Object
      |
     clone()
      |
      +------> New Object
      |
      +------> New Object
```

---

## 1. Real-time example — Report Generation

Imagine an application that generates reports.

A report contains:

```
Report
 ├── Company Logo
 ├── Header
 ├── Footer
 ├── Formatting
 ├── Page Settings
 └── Default Sections
```

Suppose creating a report is expensive because we need to load:

- Company information
- Logo
- Formatting
- Default sections
- Page configuration

If we need 1,000 similar reports, creating everything from scratch every time is inefficient.

Instead:

```
                 Report Template
                       |
             +---------+---------+
             |         |         |
           clone     clone     clone
             |         |         |
             v         v         v
          Report 1  Report 2  Report 3
```

We create one prototype report and clone it.

---

## 2. Basic Java Example

Let's create a Report.

```java
class Report implements Cloneable {


    private String title;
    private String content;


    public Report(String title, String content) {
        this.title = title;
        this.content = content;
    }


    @Override
    public Report clone() {


        try {
            return (Report) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }


    public void print() {
        System.out.println("Title   : " + title);
        System.out.println("Content : " + content);
    }
}
```

Now create the original object:

```java
Report original =
        new Report(
                "Monthly Sales Report",
                "Sales data for August"
        );
```

Clone it:

```java
Report report1 = original.clone();
Report report2 = original.clone();
```

Now:

```
original
   |
   +----> Report


report1
   |
   +----> Copy of Report


report2
   |
   +----> Copy of Report
```

---

## 3. Complete Example

```java
class Report implements Cloneable {


    private String title;
    private String content;


    public Report(String title, String content) {
        this.title = title;
        this.content = content;
    }


    @Override
    public Report clone() {


        try {
            return (Report) super.clone();


        } catch (CloneNotSupportedException e) {


            throw new RuntimeException(e);
        }
    }


    public void setTitle(String title) {
        this.title = title;
    }


    public void setContent(String content) {
        this.content = content;
    }


    public void print() {


        System.out.println("Title   : " + title);
        System.out.println("Content : " + content);
    }
}




public class Main {


    public static void main(String[] args) {


        // Original object
        Report original =
                new Report(
                        "Monthly Sales Report",
                        "Sales data for August"
                );


        // Clone the original
        Report report1 = original.clone();
        Report report2 = original.clone();


        report1.setTitle("September Sales Report");


        original.print();


        System.out.println();


        report1.print();


        System.out.println();


        report2.print();
    }
}
```

Output:

```
Title   : Monthly Sales Report
Content : Sales data for August


Title   : September Sales Report
Content : Sales data for August


Title   : Monthly Sales Report
Content : Sales data for August
```

Notice that changing report1 did not change original.

---

## 4. How does clone() work?

This is the important part:

```java
@Override
public Report clone() {


    try {
        return (Report) super.clone();
    } catch (CloneNotSupportedException e) {
        throw new RuntimeException(e);
    }
}
```

The class implements:

```java
Cloneable
```

which indicates that cloning is supported.

Then:

```java
super.clone()
```

creates a copy of the current object.

---

## 5. Why use Prototype?

Suppose creating an object is expensive.

For example:

```java
class Report {


    public Report() {


        // Load company information
        // Load logo
        // Load templates
        // Load formatting
        // Load default configuration


        System.out.println(
                "Expensive initialization..."
        );
    }
}
```

If we create:

```java
new Report();
new Report();
new Report();
```

the expensive initialization happens every time.

With Prototype:

```
Create expensive object once
           |
           v
      Prototype
       /   |   \
      /    |    \
   clone clone clone
```

This can avoid repeating expensive setup.

---

## 6. Real-time example — Game Characters

Imagine a game.

You have a character:

```
Warrior
 ├── Health
 ├── Weapons
 ├── Armor
 ├── Skills
 └── Configuration
```

Creating a fully configured warrior may be expensive.

You can create one:

```java
Warrior original = new Warrior();
```

Then:

```java
Warrior warrior1 = original.clone();
Warrior warrior2 = original.clone();
Warrior warrior3 = original.clone();
```

Then customize each clone:

```java
warrior1.setName("John");
warrior2.setName("Alex");
warrior3.setName("David");
```

All three start with the same base configuration.

---

## 7. Real-time example — Document Templates

This is another excellent example.

Suppose your company has:

- Employee Offer Letter

The template contains:

- Company Logo
- Company Address
- Terms & Conditions
- Footer
- Formatting
- Legal Information

Instead of constructing the entire document every time:

```
Offer Letter Template
         |
         +---- clone → Employee 1
         |
         +---- clone → Employee 2
         |
         +---- clone → Employee 3
```

Then customize:

- Employee Name
- Salary
- Joining Date
- Designation

This is a natural use case for Prototype.

---

## 8. Real-time example — Product Templates

Suppose an e-commerce application has product templates:

```
Laptop Template
   |
   +-- Brand
   +-- Processor
   +-- RAM
   +-- Storage
   +-- Display
   +-- Default configuration
```

You can create one prototype:

```java
Product laptopTemplate = ...;
```

Then:

```java
Product laptop1 = laptopTemplate.clone();
Product laptop2 = laptopTemplate.clone();
Product laptop3 = laptopTemplate.clone();
```

Customize each product:

```java
laptop1.setRam("16 GB");
laptop2.setRam("32 GB");
laptop3.setRam("64 GB");
```

This is useful when many objects share a common base configuration.

---

## 9. Prototype Registry

In larger applications, you may maintain a collection of prototypes.

For example:

```
Prototype Registry
       |
       +--- BasicLaptop
       |
       +--- GamingLaptop
       |
       +--- BusinessLaptop
```

The client asks the registry for a clone.

Example:

```java
class PrototypeRegistry {


    private Map<String, Product> products =
            new HashMap<>();


    public void register(
            String key,
            Product product) {


        products.put(key, product);
    }


    public Product get(String key) {


        return products.get(key).clone();
    }
}
```

Usage:

```java
registry.register(
        "GAMING_LAPTOP",
        gamingLaptop
);


Product laptop =
        registry.get("GAMING_LAPTOP");
```

This is a more advanced and practical form of Prototype.

---

## 10. Shallow Copy vs Deep Copy

This is very important when discussing Prototype in Java.

Suppose:

```java
class Employee {


    String name;
    Address address;
}
```

If we clone the Employee:

```
Original Employee
       |
       +---- Address Object
       
Cloned Employee
       |
       +---- Address Object
```

With a shallow copy, both employees may refer to the same Address object.

```
Employee 1 ────┐
               |
               v
           Address
               ^
               |
Employee 2 ────┘
```

Changing the Address through one object could affect the other.

---

## 11. Deep Copy

With deep copying:

```
Employee 1
    |
    +---- Address 1




Employee 2
    |
    +---- Address 2
```

Now each cloned object has its own nested objects.

For example:

```java
class Address {


    String city;


    public Address(String city) {
        this.city = city;
    }
}
```

And:

```java
class Employee implements Cloneable {


    String name;
    Address address;


    public Employee clone() {


        try {


            Employee copy =
                    (Employee) super.clone();


            copy.address =
                    new Address(this.address.city);


            return copy;


        } catch (CloneNotSupportedException e) {


            throw new RuntimeException(e);
        }
    }
}
```

Now the Address is also copied.

---

## 12. Shallow vs Deep Copy

Remember:

### Shallow Copy

```
Object A
   |
   +----> Nested Object


Object B
   |
   +----> Same Nested Object
```

### Deep Copy

```
Object A
   |
   +----> Nested Object A




Object B
   |
   +----> Nested Object B
```

So:

> Shallow copy copies the object structure but shares referenced nested objects.

> Deep copy creates independent copies of the nested objects as well.

---

## 13. Prototype vs Factory

This is an important interview question.

### Factory

Factory creates an object based on some decision:

```java
Payment payment =
        PaymentFactory.createPayment("UPI");
```

Think:

```
Factory
   ↓
"Which type should I create?"
```

### Prototype

Prototype copies an existing object:

```java
Report report = original.clone();
```

Think:

```
Prototype
   ↓
"I already have a similar object.
I'll copy it."
```

---

## 14. Prototype vs Builder

Another common interview question.

### Builder

Builds an object step-by-step:

```java
Employee employee =
        new Employee.Builder()
                .name("John")
                .email("john@test.com")
                .department("IT")
                .build();
```

Think:

```
Builder
   ↓
Build from scratch
```

### Prototype

Copies an existing object:

```java
Employee employee2 =
        employee1.clone();
```

Think:

```
Prototype
   ↓
Copy existing object
```

---

## 15. Prototype vs Singleton

These are completely different.

### Singleton

```
Number of instances = 1
```

```java
Logger.getInstance();
```

### Prototype

```
Number of instances = many
```

But the instances are created by copying an existing object:

```java
prototype.clone();
prototype.clone();
prototype.clone();
```

So:

```
Singleton → ONE object


Prototype → MANY objects copied from a prototype
```

---

## 16. Advantages of Prototype

**✅ 1. Avoid expensive object creation**

If initialization is expensive, cloning can be useful.

**✅ 2. Faster object creation**

Instead of reconstructing everything, copy an existing configured object.

**✅ 3. Reduces subclassing**

You can create objects from existing prototypes rather than creating many specialized classes.

**✅ 4. Easy customization**

Create a prototype and then modify the clone:

```java
Product product = template.clone();


product.setPrice(50000);
product.setRam("32 GB");
```

**✅ 5. Useful for templates**

Excellent when many objects have similar initial configurations.

---

## 17. Disadvantages

**❌ 1. Cloning can become complicated**

Especially when the object contains many nested objects.

**❌ 2. Deep copying needs careful handling**

You need to decide whether nested objects should be shared or copied.

**❌ 3. Java's Cloneable mechanism is often considered awkward**

Modern Java code often uses:

- Copy constructors
- Factory methods
- Custom copy methods

instead of relying directly on Object.clone().

For example:

```java
Employee(Employee other) {


    this.name = other.name;
    this.email = other.email;
}
```

Then:

```java
Employee employee2 =
        new Employee(employee1);
```

This is often easier to control than Cloneable.

---

## 18. When should you use Prototype?

Use Prototype when:

- ✅ Object creation is expensive.
- ✅ You have many objects with similar configuration.
- ✅ You want to create objects from templates.
- ✅ The object's exact runtime type may not be known to the client.
- ✅ You want to avoid repeating complex initialization.

Typical examples:

- Report templates
- Document templates
- Game characters
- Product configurations
- UI components
- Configuration objects
- Complex graphical objects

---

## 19. Complete mental model

Think about an e-commerce product template:

```
                    Product Template
                           |
                    clone / copy
                           |
          +----------------+----------------+
          |                |                |
          v                v                v
       Product 1        Product 2        Product 3
          |                |                |
       16 GB RAM        32 GB RAM        64 GB RAM
       ₹50,000          ₹60,000          ₹70,000
```

The common configuration comes from the prototype, and each clone can then be customized.

---

## 20. All 5 Creational Patterns — Easy Revision

Now that you've covered the five major creational patterns:

| Pattern | Key question | Easy example |
|---|---|---|
| Singleton | How many objects? | One Logger |
| Factory | Which object? | UPI/Card/PayPal |
| Abstract Factory | Which family? | Razorpay/Stripe components |
| Builder | How to construct? | Complex Order |
| Prototype | Can I copy one? | Report/Product template |

### Remember this sequence:

```
Singleton
    ↓
ONE


Factory
    ↓
WHICH


Abstract Factory
    ↓
FAMILY


Builder
    ↓
BUILD


Prototype
    ↓
COPY
```

> Prototype = "Don't create it from scratch; copy an existing object and customize the copy."
