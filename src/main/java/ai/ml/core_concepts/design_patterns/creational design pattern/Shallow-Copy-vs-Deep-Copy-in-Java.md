# Shallow Copy vs Deep Copy in Java

This is an important concept, especially when learning the Prototype Design Pattern.

The easiest way to remember it is:

> Shallow Copy → Copies the object, but shares nested/reference objects.
> Deep Copy → Copies the object and also creates independent copies of nested/reference objects.

---

## 1. Simple real-time example

Imagine an employee has an address:

```
Employee
 ├── name
 └── Address
      ├── city
      └── pincode
```

Suppose we copy the employee.

### Shallow Copy

```
Original Employee ───┐
                     ├──> Same Address
Copied Employee  ────┘
```

Both employees share the same Address object.

### Deep Copy

```
Original Employee ───> Address 1


Copied Employee  ────> Address 2
```

The copied employee gets its own Address object.

---

## 2. First understand primitive vs reference types

Consider:

```java
class Employee {


    String name;
    Address address;
}
```

There are two kinds of data here.

**name**

```java
String name;
```

The field contains a reference to a String object, but Strings are immutable, so this example won't demonstrate the mutation issue well.

**address**

```java
Address address;
```

This is a reference to another mutable object.

That's where shallow vs deep copying becomes important.

---

## 3. Real-time Employee example

Let's create:

```java
class Address {


    String city;


    Address(String city) {
        this.city = city;
    }
}
```

And:

```java
class Employee {


    String name;
    Address address;


    Employee(String name, Address address) {
        this.name = name;
        this.address = address;
    }
}
```

Create an employee:

```java
Address address =
        new Address("Mumbai");


Employee employee1 =
        new Employee("Kaushal", address);
```

Our objects look like:

```
employee1
   |
   +---- name = "Kaushal"
   |
   +---- address --------+
                         |
                         v
                    Address
                    city = Mumbai
```

---

## 4. Shallow Copy

Let's create a shallow copy.

```java
Employee employee2 =
        new Employee(
                employee1.name,
                employee1.address
        );
```

Now:

```
employee1
   |
   +---- address --------+
                         |
                         v
                    Address
                    Mumbai
                         ^
                         |
   +---- address --------+
   |
employee2
```

Both employees point to the same Address object.

---

## 5. The problem

Suppose:

```java
employee2.address.city = "Pune";
```

What happens?

Let's print:

```java
System.out.println(
        employee1.address.city
);


System.out.println(
        employee2.address.city
);
```

Output:

```
Pune
Pune
```

😮 Why did employee1 change?

Because:

```
employee1.address
       |
       +--------+
                |
                v
             Address
             Pune
                ^
                |
       +--------+
       |
employee2.address
```

There is only one Address object.

---

## 6. Deep Copy

With deep copy, we create a completely new Address.

```java
Employee employee2 =
        new Employee(
                employee1.name,
                new Address(employee1.address.city)
        );
```

Now:

```
employee1
   |
   +---- address ----> Address 1
                        city = Mumbai




employee2
   |
   +---- address ----> Address 2
                        city = Mumbai
```

There are two different Address objects.

---

## 7. Now change employee2

```java
employee2.address.city = "Pune";
```

Print:

```java
System.out.println(
        employee1.address.city
);


System.out.println(
        employee2.address.city
);
```

Output:

```
Mumbai
Pune
```

That's because they have independent Address objects.

---

## 8. Complete Shallow Copy Example

```java
class Address {


    String city;


    Address(String city) {
        this.city = city;
    }
}




class Employee {


    String name;
    Address address;


    Employee(String name, Address address) {
        this.name = name;
        this.address = address;
    }
}




public class Main {


    public static void main(String[] args) {


        Address address =
                new Address("Mumbai");


        Employee employee1 =
                new Employee(
                        "Kaushal",
                        address
                );


        // Shallow copy
        Employee employee2 =
                new Employee(
                        employee1.name,
                        employee1.address
                );


        employee2.address.city = "Pune";


        System.out.println(
                "Employee 1 city = "
                        + employee1.address.city
        );


        System.out.println(
                "Employee 2 city = "
                        + employee2.address.city
        );
    }
}
```

Output:

```
Employee 1 city = Pune
Employee 2 city = Pune
```

---

## 9. Complete Deep Copy Example

```java
class Address {


    String city;


    Address(String city) {
        this.city = city;
    }
}




class Employee {


    String name;
    Address address;


    Employee(String name, Address address) {
        this.name = name;
        this.address = address;
    }


    // Deep copy constructor
    Employee(Employee employee) {


        this.name = employee.name;


        this.address =
                new Address(
                        employee.address.city
                );
    }
}




public class Main {


    public static void main(String[] args) {


        Address address =
                new Address("Mumbai");


        Employee employee1 =
                new Employee(
                        "Kaushal",
                        address
                );


        // Deep copy
        Employee employee2 =
                new Employee(employee1);


        employee2.address.city = "Pune";


        System.out.println(
                "Employee 1 city = "
                        + employee1.address.city
        );


        System.out.println(
                "Employee 2 city = "
                        + employee2.address.city
        );
    }
}
```

Output:

```
Employee 1 city = Mumbai
Employee 2 city = Pune
```

---

## 10. The most important difference

Consider:

```java
Employee employee2 =
        new Employee(
                employee1.name,
                employee1.address
        );
```

This:

```java
employee1.address
```

is reused.

Therefore:

```java
employee1.address == employee2.address
```

is:

```
true
```

With deep copy:

```java
Employee employee2 =
        new Employee(
                employee1.name,
                new Address(employee1.address.city)
        );
```

Now:

```java
employee1.address == employee2.address
```

is:

```
false
```

---

## 11. Visual difference

### Shallow Copy

```
             Original
                |
                |
                v
             Employee
                |
                |
             Address
                ^
                |
                |
              Copy
             Employee
```

Both objects share:

```
Address
```

### Deep Copy

```
             Original
                |
                v
             Employee
                |
                v
             Address 1




              Copy
             Employee
                |
                v
             Address 2
```

Nothing mutable is shared.

---

## 12. Real-time example — Shopping Cart

Suppose an e-commerce cart contains:

```
ShoppingCart
 ├── customer
 └── products
       ├── Laptop
       ├── Mouse
       └── Keyboard
```

Imagine you want to create a cart for a customer based on a template cart.

### Shallow copy

```
Cart 1 ────────┐
               |
               v
          Product List
               ^
               |
Cart 2 ────────┘
```

Both carts share the same list.

If you do:

```java
cart2.products.add(newProduct);
```

the product may also appear in cart1.

That's dangerous.

### Deep copy

```
Cart 1 ───> Product List 1


Cart 2 ───> Product List 2
```

Now modifying one cart doesn't modify the other.

---

## 13. Real-time example — Game Character

Suppose a game character has:

```
Character
 ├── name
 ├── health
 ├── Weapon
 └── Inventory
```

You want to clone a character.

### Shallow copy

```
Character 1 ──┐
              ├──> Weapon
Character 2 ──┘
```

Both characters share the same weapon object.

If one character upgrades the weapon:

```java
Weapon.damage = 100
```

the other character may see the same weapon change.

### Deep copy

```
Character 1 ──> Weapon 1


Character 2 ──> Weapon 2
```

Each character can modify their own weapon independently.

---

## 14. Shallow Copy vs Deep Copy

| Feature | Shallow Copy | Deep Copy |
|---|---|---|
| Creates new outer object | ✅ Yes | ✅ Yes |
| Copies primitive values | ✅ Yes | ✅ Yes |
| Copies nested objects | ❌ Usually references are shared | ✅ Creates independent copies |
| Nested object shared? | ✅ Yes | ❌ No |
| Memory usage | Lower | Higher |
| Performance | Usually faster | Usually slower |
| Independence | Partial | High |
| Implementation | Easier | More complex |

---

## 15. How this relates to Prototype Pattern

This is especially important because you just learned the Prototype Design Pattern.

Prototype often uses cloning:

```java
Product copy = original.clone();
```

But you need to decide:

> Should the clone share nested objects or should it get copies of them?

### Shallow Prototype

```
Prototype
    |
    +---- Address
              ↑
              |
            Clone
```

### Deep Prototype

```
Prototype
    |
    +---- Address 1


Clone
    |
    +---- Address 2
```

So when implementing Prototype, understanding shallow and deep copying is essential.

---

## 16. One important Java point

Java's:

```java
super.clone()
```

performs a field-by-field copy.

For example:

```java
@Override
public Employee clone() {


    try {
        return (Employee) super.clone();


    } catch (CloneNotSupportedException e) {
        throw new RuntimeException(e);
    }
}
```

If Employee contains:

```java
Address address;
```

then super.clone() copies the reference to Address.

Therefore, by itself, it generally gives you a shallow copy.

To make it deep, you need to explicitly copy the nested object:

```java
@Override
public Employee clone() {


    try {


        Employee copy =
                (Employee) super.clone();


        copy.address =
                new Address(
                        this.address.city
                );


        return copy;


    } catch (CloneNotSupportedException e) {


        throw new RuntimeException(e);
    }
}
```

---

## 17. Easy interview answer

If the interviewer asks:

> "What is shallow copy?"

Say:

> Shallow copy creates a new outer object, but references to nested mutable objects are shared between the original and the copy.

> "What is deep copy?"

Say:

> Deep copy creates a new outer object and also creates independent copies of its nested objects, so changes to one object don't affect the other.

---

## 18. The easiest way to remember

Think about a house 🏠.

### Shallow Copy

You build a second house but both houses use the same garage:

```
House 1 ──┐
          ├──> Same Garage
House 2 ──┘
```

Change the garage → both houses are affected.

### Deep Copy

You build a second house with its own garage:

```
House 1 ──> Garage 1


House 2 ──> Garage 2
```

Change Garage 2 → House 1 is unaffected.

### In one line:

```
Shallow Copy → New object + shared nested objects


Deep Copy    → New object + new nested objects
```

> Shallow = Share
> Deep = Independent
