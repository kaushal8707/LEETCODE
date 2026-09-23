# LSP — Square and Rectangle Example

The Square–Rectangle example is one of the most famous examples used to explain the Liskov Substitution Principle (LSP).

The key idea is:

> Mathematically, a square is a rectangle. But in object-oriented design, making Square extends Rectangle can violate LSP.

Let's understand why.

---

## 1. Normal Rectangle

A rectangle has:

```
Width  = 10
Height = 20
```

So:

```
Area = 10 × 20
     = 200
```

Java class:

```java
class Rectangle {


    protected int width;
    protected int height;


    public void setWidth(int width) {
        this.width = width;
    }


    public void setHeight(int height) {
        this.height = height;
    }


    public int getArea() {
        return width * height;
    }
}
```

We can use it:

```java
Rectangle rectangle = new Rectangle();


rectangle.setWidth(10);
rectangle.setHeight(20);


System.out.println(rectangle.getArea());
```

Output:

```
200
```

Everything is fine.

---

## 2. Now Create Square

Mathematically:

> Square IS-A Rectangle

because a square is a special type of rectangle.

So someone might write:

```java
class Square extends Rectangle {


    @Override
    public void setWidth(int width) {
        this.width = width;
        this.height = width;
    }


    @Override
    public void setHeight(int height) {
        this.width = height;
        this.height = height;
    }
}
```

The reason is simple:

A square must always have equal width and height.

---

## 3. The Problem

Now look at this code:

```java
Rectangle rectangle = new Square();


rectangle.setWidth(10);
rectangle.setHeight(20);


System.out.println(
        rectangle.getArea()
);
```

What would we normally expect?

Because the variable is a Rectangle:

```
Width  = 10
Height = 20


Area = 10 × 20
     = 200
```

But because the actual object is a Square:

First:

```java
rectangle.setWidth(10);
```

Square becomes:

```
width  = 10
height = 10
```

Then:

```java
rectangle.setHeight(20);
```

Square must keep both sides equal:

```
width  = 20
height = 20
```

Therefore:

```
Area = 20 × 20
     = 400
```

Output:

```
400
```

But the code using Rectangle expected:

```
200
```

💥 The behavior changed when we substituted Rectangle with Square.

That's the LSP violation.

---

## 4. Complete Example

```java
class Rectangle {


    protected int width;
    protected int height;


    public void setWidth(int width) {
        this.width = width;
    }


    public void setHeight(int height) {
        this.height = height;
    }


    public int getArea() {
        return width * height;
    }
}
```

Square:

```java
class Square extends Rectangle {


    @Override
    public void setWidth(int width) {
        this.width = width;
        this.height = width;
    }


    @Override
    public void setHeight(int height) {
        this.width = height;
        this.height = height;
    }
}
```

Main:

```java
public class Main {


    public static void main(String[] args) {


        Rectangle rectangle =
                new Square();


        rectangle.setWidth(10);
        rectangle.setHeight(20);


        System.out.println(
                "Area = " + rectangle.getArea()
        );
    }
}
```

Output:

```
Area = 400
```

But the client code expected:

```
Area = 200
```

---

## 5. Where exactly is the LSP violation?

Look at:

```java
Rectangle rectangle = new Square();
```

This is the substitution.

We are saying:

```
I expect a Rectangle
        ↓
Give me a Square
```

LSP asks:

> Can the Square behave correctly wherever a Rectangle is expected?

In this design:

No.

Because Rectangle allows:

```
width  ≠ height
```

while Square requires:

```
width = height
```

Therefore, the assumptions made by the Rectangle abstraction are not preserved by Square.

---

## 6. Why inheritance is the problem here

The problem isn't that:

"A square isn't mathematically a rectangle."

Mathematically, it absolutely is.

The problem is that our software abstraction says:

```
Rectangle
```

allows independent modification:

```java
setWidth()
setHeight()
```

But:

```
Square
```

cannot honor that behavior independently.

For a square:

```java
setWidth(10)
```

also changes height.

And:

```java
setHeight(20)
```

also changes width.

So the contract of the parent doesn't fit the child.

---

## 7. Another way to see the problem

Suppose we have this method:

```java
public static void testRectangle(
        Rectangle rectangle) {


    rectangle.setWidth(10);
    rectangle.setHeight(20);


    if (rectangle.getArea() != 200) {
        throw new RuntimeException(
                "Unexpected result"
        );
    }
}
```

Now:

```java
testRectangle(new Rectangle());
```

Works:

```
Area = 200
```

But:

```java
testRectangle(new Square());
```

Fails:

```
Area = 400
```

This is a perfect demonstration of LSP.

The method expects any Rectangle to behave according to the rectangle contract.

Square breaks that expectation.

---

## 8. How can we fix it?

One solution is to not make Square inherit from a mutable Rectangle.

Instead, create a common abstraction.

For example:

```java
interface Shape {


    int getArea();
}
```

Rectangle:

```java
class Rectangle implements Shape {


    private int width;
    private int height;


    public Rectangle(int width, int height) {
        this.width = width;
        this.height = height;
    }


    @Override
    public int getArea() {
        return width * height;
    }
}
```

Square:

```java
class Square implements Shape {


    private int side;


    public Square(int side) {
        this.side = side;
    }


    @Override
    public int getArea() {
        return side * side;
    }
}
```

Now:

```
             Shape
             /   \
            /     \
     Rectangle    Square
```

Both are shapes.

But we don't force Square to behave like a mutable Rectangle.

---

## 9. Complete Better Design

```java
interface Shape {


    int getArea();
}
```

```java
class Rectangle implements Shape {


    private int width;
    private int height;


    public Rectangle(int width, int height) {
        this.width = width;
        this.height = height;
    }


    @Override
    public int getArea() {
        return width * height;
    }
}
```

```java
class Square implements Shape {


    private int side;


    public Square(int side) {
        this.side = side;
    }


    @Override
    public int getArea() {
        return side * side;
    }
}
```

Usage:

```java
public class Main {


    public static void main(String[] args) {


        Shape rectangle =
                new Rectangle(10, 20);


        Shape square =
                new Square(10);


        System.out.println(
                "Rectangle Area = "
                        + rectangle.getArea()
        );


        System.out.println(
                "Square Area = "
                        + square.getArea()
        );
    }
}
```

Output:

```
Rectangle Area = 200
Square Area = 100
```

Now there's no violation.

---

## 10. The important lesson

The lesson isn't:

> "Never use inheritance."

Instead:

> Don't use inheritance just because two objects have a real-world relationship.

You should ask:

> "Does the child satisfy the behavioral contract of the parent?"

For our example:

```
Mathematical relationship:


Square IS-A Rectangle
        ↓
True




Software behavioral relationship:


Square can substitute Rectangle
        ↓
False, with this mutable API
```

That's the important distinction.

---

## 11. Interview explanation

If the interviewer asks:

> "Explain LSP using the Square and Rectangle example."

You can say:

> A rectangle allows its width and height to be changed independently. If Square extends Rectangle, it must keep width and height equal. Therefore, overriding setWidth() and setHeight() changes the expected behavior of Rectangle. For example, if a method expects a Rectangle, sets width to 10 and height to 20, it expects an area of 200. Passing a Square results in an area of 400 because changing one dimension changes the other. Therefore, Square cannot safely substitute Rectangle in that design, violating LSP.

---

## 12. Easy diagram to remember

### ❌ LSP Violation

```
             Rectangle
             /       \
            /         \
           ↓           ↓
       Normal        Square
       Rectangle
           |
           |
   setWidth(10)
   setHeight(20)
           |
           ↓
     Expected Area = 200


Square:
   setWidth(10)  → 10 × 10
   setHeight(20) → 20 × 20
           |
           ↓
     Actual Area = 400


          💥 LSP violation
```

### ✅ Better Design

```
               Shape
              /     \
             /       \
            ↓         ↓
      Rectangle     Square
         |             |
      width ×       side ×
      height          side
```

Both satisfy the Shape contract without pretending that their dimension-setting behavior is interchangeable.

### The one-line memory trick:

> LSP = If I replace the parent object with the child object, the existing code should continue to work correctly
