# Builder Design Pattern

The Builder Design Pattern is a Creational Design Pattern used to create complex objects step-by-step.

## Simple definition

Builder Pattern separates the construction of a complex object from its representation, allowing the same construction process to create different representations.

In simple Java terms:

Instead of creating an object using a constructor with many parameters, we build the object step-by-step using a Builder.

---

## 1. What problem does Builder solve?

Imagine you have an Employee class:

```java
class Employee {


    private String name;
    private String email;
    private String phone;
    private String department;
    private String designation;
    private String city;
    private String country;
    private int age;
}
```

You could create it using a constructor:

```java
Employee employee = new Employee(
        "Kaushal",
        "kaushal@gmail.com",
        "9876543210",
        "IT",
        "Software Engineer",
        "Mumbai",
        "India",
        30
);
```

### What's wrong with this?

Look at:

```java
new Employee(
    "Kaushal",
    "kaushal@gmail.com",
    "9876543210",
    "IT",
    "Software Engineer",
    "Mumbai",
    "India",
    30
);
```

It's difficult to understand:

- Which parameter is email?
- Which one is phone?
- Which fields are optional?
- What happens if there are 15–20 fields?

This is called the **telescoping constructor problem**.

---

## 2. Without Builder

Suppose we have:

```java
class Employee {


    private String name;
    private String email;
    private String phone;
    private String department;
    private String designation;
    private String city;


    public Employee(
            String name,
            String email,
            String phone,
            String department,
            String designation,
            String city) {


        this.name = name;
        this.email = email;
        this.phone = phone;
        this.department = department;
        this.designation = designation;
        this.city = city;
    }
}
```

Now imagine another employee where phone and city aren't available.

You might end up creating multiple constructors:

```java
Employee(String name)
Employee(String name, String email)
Employee(String name, String email, String phone)
Employee(String name, String email, String phone, String department)
...
```

This becomes ugly very quickly.

---

## 3. Builder Pattern solution

Instead of:

```java
new Employee(...many parameters...)
```

we can write:

```java
Employee employee = new Employee.Builder()
        .name("Kaushal")
        .email("kaushal@gmail.com")
        .phone("9876543210")
        .department("IT")
        .designation("Software Engineer")
        .city("Mumbai")
        .build();
```

Now it's immediately clear what every value represents.

---

## 4. Step-by-step implementation

Let's create an Employee.

### Step 1: Employee class

```java
class Employee {


    private String name;
    private String email;
    private String phone;
    private String department;
    private String designation;
    private String city;


    private Employee(Builder builder) {


        this.name = builder.name;
        this.email = builder.email;
        this.phone = builder.phone;
        this.department = builder.department;
        this.designation = builder.designation;
        this.city = builder.city;
    }
}
```

Notice the constructor:

```java
private Employee(Builder builder)
```

The outside world cannot directly create an Employee using the constructor.

---

## 5. Create the Builder class

Inside Employee, we create a static Builder.

```java
static class Builder {


    private String name;
    private String email;
    private String phone;
    private String department;
    private String designation;
    private String city;
}
```

So we have:

```
Employee
   |
   +--- Builder
          |
          +--- name
          +--- email
          +--- phone
          +--- department
          +--- designation
          +--- city
```

---

## 6. Add Builder methods

Now we create methods for each property.

```java
public Builder name(String name) {


    this.name = name;
    return this;
}
```

Similarly:

```java
public Builder email(String email) {


    this.email = email;
    return this;
}
```

And:

```java
public Builder phone(String phone) {


    this.phone = phone;
    return this;
}
```

The important part is:

```java
return this;
```

Why?

Because it allows method chaining.

For example:

```java
builder
    .name("Kaushal")
    .email("kaushal@gmail.com")
    .phone("9876543210");
```

---

## 7. Add the build() method

Finally:

```java
public Employee build() {


    return new Employee(this);
}
```

This is the method that actually creates the final object.

---

## 8. Complete Builder example

```java
class Employee {


    private String name;
    private String email;
    private String phone;
    private String department;
    private String designation;
    private String city;


    private Employee(Builder builder) {


        this.name = builder.name;
        this.email = builder.email;
        this.phone = builder.phone;
        this.department = builder.department;
        this.designation = builder.designation;
        this.city = builder.city;
    }


    public static class Builder {


        private String name;
        private String email;
        private String phone;
        private String department;
        private String designation;
        private String city;


        public Builder name(String name) {
            this.name = name;
            return this;
        }


        public Builder email(String email) {
            this.email = email;
            return this;
        }


        public Builder phone(String phone) {
            this.phone = phone;
            return this;
        }


        public Builder department(String department) {
            this.department = department;
            return this;
        }


        public Builder designation(String designation) {
            this.designation = designation;
            return this;
        }


        public Builder city(String city) {
            this.city = city;
            return this;
        }


        public Employee build() {
            return new Employee(this);
        }
    }
}
```

Now the client can create the object:

```java
public class Main {


    public static void main(String[] args) {


        Employee employee = new Employee.Builder()
                .name("Kaushal")
                .email("kaushal@gmail.com")
                .phone("9876543210")
                .department("IT")
                .designation("Software Engineer")
                .city("Mumbai")
                .build();
    }
}
```

---

## 9. What exactly happens internally?

This code:

```java
Employee employee = new Employee.Builder()
        .name("Kaushal")
        .email("kaushal@gmail.com")
        .phone("9876543210")
        .department("IT")
        .designation("Software Engineer")
        .city("Mumbai")
        .build();
```

happens step-by-step.

**Step 1**

Create Builder:

```java
new Employee.Builder()
```

**Step 2**

Set name:

```java
.name("Kaushal")
```

Internally:

```java
this.name = "Kaushal";
```

**Step 3**

Set email:

```java
.email("kaushal@gmail.com")
```

Internally:

```java
this.email = "kaushal@gmail.com";
```

**Step 4**

Continue setting properties:

```java
.phone(...)
.department(...)
.designation(...)
.city(...)
```

**Step 5**

Call:

```java
.build()
```

Internally:

```java
return new Employee(this);
```

The Builder passes all the collected values to the private Employee constructor.

---

## 10. Real-time example: E-commerce Order

This is where Builder becomes very useful.

Imagine an e-commerce system.

An Order could contain:

```
Order
 ├── orderId
 ├── customer
 ├── products
 ├── shippingAddress
 ├── billingAddress
 ├── coupon
 ├── discount
 ├── paymentMethod
 └── deliveryType
```

Some fields are mandatory, while others are optional.

For example:

```java
Order order = new Order.Builder()
        .orderId("ORD12345")
        .customer(customer)
        .products(products)
        .shippingAddress("Mumbai")
        .paymentMethod("UPI")
        .coupon("WELCOME10")
        .discount(500)
        .deliveryType("EXPRESS")
        .build();
```

This is much easier to read than:

```java
new Order(
    "ORD12345",
    customer,
    products,
    "Mumbai",
    null,
    "WELCOME10",
    500,
    "UPI",
    "EXPRESS"
);
```

---

## 11. Builder with validation

One of the biggest advantages is that we can put validation inside build().

For example:

```java
public Employee build() {


    if (name == null || name.isBlank()) {
        throw new IllegalArgumentException(
                "Name is mandatory"
        );
    }


    if (email == null || email.isBlank()) {
        throw new IllegalArgumentException(
                "Email is mandatory"
        );
    }


    return new Employee(this);
}
```

Now:

```java
Employee employee = new Employee.Builder()
        .name("Kaushal")
        .build();
```

will fail because email is mandatory.

This keeps object creation rules in one place.

---

## 12. Builder and immutability

Builder is commonly used to create immutable objects.

For example:

```java
class Employee {


    private final String name;
    private final String email;
    private final String phone;


    private Employee(Builder builder) {


        this.name = builder.name;
        this.email = builder.email;
        this.phone = builder.phone;
    }
}
```

Notice:

```java
private final String name;
```

After the Employee object is created, its values cannot be changed.

This combination is very common:

> Builder + immutable object

---

## 13. Real-world Java example: Lombok

If you work with Spring Boot, you may see Lombok's:

```java
@Builder
```

For example:

```java
@Builder
public class Employee {


    private String name;
    private String email;
    private String department;
    private String designation;
}
```

Then:

```java
Employee employee = Employee.builder()
        .name("Kaushal")
        .email("kaushal@gmail.com")
        .department("IT")
        .designation("Software Engineer")
        .build();
```

Lombok generates much of the Builder boilerplate for you.

---

## 14. Builder vs Factory

This is an important interview question.

### Factory

Factory answers:

> Which object should I create?

For example:

```java
Payment payment =
        PaymentFactory.createPayment("UPI");
```

The Factory chooses:

- UPI
- CARD
- PAYPAL

### Builder

Builder answers:

> How should I construct this complex object?

For example:

```java
Order order = new Order.Builder()
        .customer(customer)
        .products(products)
        .address(address)
        .coupon(coupon)
        .paymentMethod("UPI")
        .build();
```

So:

```
Factory
   ↓
Choose the type of object


Builder
   ↓
Construct a complex object step-by-step
```

---

## 15. Builder vs Constructor

### Constructor

```java
Employee employee = new Employee(
        "Kaushal",
        "kaushal@gmail.com",
        "9876543210",
        "IT",
        "Software Engineer",
        "Mumbai"
);
```

Problem:

- Hard to read
- Many parameters
- Difficult to handle optional fields
- Can lead to many overloaded constructors

### Builder

```java
Employee employee = new Employee.Builder()
        .name("Kaushal")
        .email("kaushal@gmail.com")
        .phone("9876543210")
        .department("IT")
        .designation("Software Engineer")
        .city("Mumbai")
        .build();
```

Advantages:

- Readable
- Handles optional fields nicely
- Supports validation
- Reduces constructor overloads
- Works well with immutable objects

---

## 16. Important interview question: Why return this?

You will often see:

```java
public Builder name(String name) {
    this.name = name;
    return this;
}
```

Why?

Because `return this` enables method chaining.

Without it:

```java
builder.name("Kaushal");
builder.email("test@gmail.com");
builder.phone("9999999999");
```

With it:

```java
builder
    .name("Kaushal")
    .email("test@gmail.com")
    .phone("9999999999")
    .build();
```

---

## 17. Builder Pattern structure

Remember this structure:

```
                    Client
                      |
                      v
                 Order.Builder
                      |
          +-----------+-----------+
          |           |           |
          v           v           v
       customer    products    address
          |           |           |
          +-----------+-----------+
                      |
                      v
                    build()
                      |
                      v
                    Order
```

The Builder collects the information, and build() creates the final object.

---

## 18. When should you use Builder?

Use Builder when:

**✅ Object has many fields**

For example:

- 10+ properties

**✅ Many fields are optional**

For example:

```
Order
 ├── customer       mandatory
 ├── products       mandatory
 ├── coupon         optional
 ├── discount       optional
 ├── giftMessage    optional
 └── notes          optional
```

**✅ Object construction is complicated**

If there are validation rules or multiple construction steps, Builder is useful.

**✅ You want readable object creation**

Instead of:

```java
new User("John", "john@test.com", null, null, 25);
```

you can write:

```java
new User.Builder()
        .name("John")
        .email("john@test.com")
        .age(25)
        .build();
```

---

## 19. Easy way to remember Creational Patterns

Since you're learning the Creational Design Patterns, remember them like this:

| Pattern | Think |
|---|---|
| Singleton | "I need ONE object." |
| Factory | "I need to CHOOSE which object." |
| Abstract Factory | "I need a FAMILY of related objects." |
| Builder | "I need to BUILD a complex object step-by-step." |
| Prototype | "I need to COPY an existing object." |

### Builder in one sentence

> Builder Pattern is used when an object has many parameters, especially optional ones, and we want to construct it in a readable, step-by-step manner without using a large or confusing constructor.
