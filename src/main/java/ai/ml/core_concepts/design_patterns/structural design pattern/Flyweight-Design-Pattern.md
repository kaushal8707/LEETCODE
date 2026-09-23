# Flyweight Design Pattern

The Flyweight Design Pattern is a Structural Design Pattern.

Flyweight reduces memory usage by sharing common objects instead of creating a new object every time.

In simple words:

If thousands of objects contain the same data, don't create thousands of copies. Create one shared object and reuse it.

### Easy memory trick

```
Without Flyweight:
Object A → Same data
Object B → Same data
Object C → Same data
Object D → Same data

With Flyweight:
              Shared Object
             /      |      \
            A       B       C
```

## 1. Real-Time Example — Text Editor

This is one of the best examples of the Flyweight Pattern.

Imagine Microsoft Word or a text editor containing:

```
Hello World
Hello Java
Hello Design Pattern
```

Suppose the document contains 1 million characters.

If every character object stores:

- Character
- Font
- Font Size
- Font Style
- Color

then memory usage can become huge.

For example:

```
'A' + Arial + 12 + Black
'A' + Arial + 12 + Black
'A' + Arial + 12 + Black
'A' + Arial + 12 + Black
```

We're unnecessarily storing the same formatting repeatedly.

Instead, we can share the common formatting.

```
                Character Formatting
                       |
             +---------+---------+
             |         |         |
             ↓         ↓         ↓
            'A'       'B'       'C'
```

This is the idea behind Flyweight.

## 2. Intrinsic vs Extrinsic State

This is the most important concept in Flyweight.

Flyweight separates object data into:

### Intrinsic State

Data that is:

Common and shared

Example:

```
Font = Arial
FontSize = 12
Color = Black
```

This can be shared.

### Extrinsic State

Data that is:

Unique to each usage

Example:

```
Character position
x
y
```

This should be supplied from outside.

## 3. Text Editor Example

Suppose we have:

```
A A A A A A A A A A
```

Each A may have:

```
Character = A
Font = Arial
Size = 12
Color = Black
```

Instead of:

```
A → Arial, 12, Black
A → Arial, 12, Black
A → Arial, 12, Black
A → Arial, 12, Black
```

we create one shared object:

```
       Character A
    Arial / 12 / Black
          ↑
     +----+----+----+
     |    |    |    |
     A    A    A    A
```

The position of each character is external:

```
A → position 10
A → position 25
A → position 50
```

## 4. Java Example

Let's create a CharacterStyle.

```java
class CharacterStyle {

    private String font;
    private int size;
    private String color;

    public CharacterStyle(
            String font,
            int size,
            String color) {

        this.font = font;
        this.size = size;
        this.color = color;
    }

    public void display(
            char character,
            int position) {

        System.out.println(
                "Character: " + character
                        + ", Position: " + position
                        + ", Font: " + font
                        + ", Size: " + size
                        + ", Color: " + color
        );
    }
}
```

Here:

- font
- size
- color

are intrinsic state.

They can be shared.

## 5. Flyweight Factory

We don't want to create the same style repeatedly.

So we create a Factory.

```java
import java.util.HashMap;
import java.util.Map;

class CharacterStyleFactory {

    private static Map<String, CharacterStyle>
            styles = new HashMap<>();

    public static CharacterStyle getStyle(
            String font,
            int size,
            String color) {

        String key =
                font + "-" + size + "-" + color;

        if (!styles.containsKey(key)) {

            styles.put(
                    key,
                    new CharacterStyle(
                            font,
                            size,
                            color
                    )
            );
        }

        return styles.get(key);
    }
}
```

The factory checks:

```
Does this style already exist?
        |
   +----+----+
   |         |
  YES       NO
   |         |
Reuse      Create
```

## 6. Using the Flyweight

```java
public class Main {

    public static void main(String[] args) {

        CharacterStyle style1 =
                CharacterStyleFactory.getStyle(
                        "Arial",
                        12,
                        "Black"
                );

        CharacterStyle style2 =
                CharacterStyleFactory.getStyle(
                        "Arial",
                        12,
                        "Black"
                );

        System.out.println(
                style1 == style2
        );
    }
}
```

Output:

```
true
```

Why?

Because both variables point to the same shared object.

```
style1 ─────┐
            ↓
       CharacterStyle
            ↑
style2 ─────┘
```

Instead of:

```
style1 → CharacterStyle Object 1

style2 → CharacterStyle Object 2
```

we have:

```
style1 ──┐
         ↓
       Object
         ↑
style2 ──┘
```

## 7. Real-Time Example — Game Development 🎮

This is another excellent example.

Imagine a game containing:

- 10,000 trees
- 5,000 rocks
- 20,000 bullets
- 50,000 soldiers

Suppose every tree stores:

- Tree Type
- Tree Texture
- Tree Color
- Tree Model

Many trees may use exactly the same:

- Tree Model
- Texture
- Color

Why create the same data thousands of times?

Instead:

```
                Tree Type
              /     |     \
             /      |      \
          Tree     Tree    Tree
          #1       #2      #3
```

Each tree can have unique:

- x position
- y position
- rotation
- scale

while sharing:

- texture
- model
- color

## 8. Game Example

Let's create:

```java
class TreeType {

    private String name;
    private String texture;
    private String color;

    public TreeType(
            String name,
            String texture,
            String color) {

        this.name = name;
        this.texture = texture;
        this.color = color;
    }

    public void draw(
            int x,
            int y) {

        System.out.println(
                "Drawing " + name
                        + " at (" + x
                        + ", " + y + ")"
        );
    }
}
```

Here:

- name
- texture
- color

are shared.

## 9. Tree Factory

```java
import java.util.HashMap;
import java.util.Map;

class TreeFactory {

    private static Map<String, TreeType>
            treeTypes = new HashMap<>();

    public static TreeType getTreeType(
            String name,
            String texture,
            String color) {

        String key =
                name + "-" + texture + "-" + color;

        if (!treeTypes.containsKey(key)) {

            treeTypes.put(
                    key,
                    new TreeType(
                            name,
                            texture,
                            color
                    )
            );
        }

        return treeTypes.get(key);
    }
}
```

## 10. Tree Object

The actual tree stores only unique data:

```java
class Tree {

    private int x;
    private int y;

    private TreeType treeType;

    public Tree(
            int x,
            int y,
            TreeType treeType) {

        this.x = x;
        this.y = y;
        this.treeType = treeType;
    }

    public void draw() {

        treeType.draw(x, y);
    }
}
```

Notice:

```
Tree
 ├── x       → unique
 ├── y       → unique
 └── TreeType → shared
```

## 11. Creating Thousands of Trees

```java
public class Game {

    public static void main(String[] args) {

        TreeType oak =
                TreeFactory.getTreeType(
                        "Oak",
                        "oak.png",
                        "Green"
                );

        Tree tree1 =
                new Tree(100, 200, oak);

        Tree tree2 =
                new Tree(500, 300, oak);

        Tree tree3 =
                new Tree(700, 900, oak);

        tree1.draw();
        tree2.draw();
        tree3.draw();
    }
}
```

All three trees share:

- Oak
- oak.png
- Green

but have different:

- x
- y

## 12. Memory Comparison

Suppose you have:

1,000,000 trees

### Without Flyweight

Potentially:

1,000,000 TreeType objects

Each stores:

- name
- texture
- color
- model

Huge duplication.

### With Flyweight

You might have:

5 TreeType objects

for:

- Oak
- Pine
- Maple
- Palm
- Birch

And:

1,000,000 Tree objects

store only:

- x
- y
- reference to TreeType

That's a huge reduction in duplicated state.

## 13. Real-Time Example — Google Maps / Map Applications 🗺️

Imagine a map displaying thousands of similar objects:

- Restaurant
- Hospital
- ATM
- Gas Station
- Hotel

Suppose there are:

100,000 restaurants

Many restaurants may share the same:

- Restaurant icon
- Icon color
- Icon dimensions

Instead of creating a new icon object for every restaurant:

```
Restaurant 1 → Restaurant Icon
Restaurant 2 → Restaurant Icon
Restaurant 3 → Restaurant Icon
...
```

the same icon representation can be shared.

Unique data:

- Latitude
- Longitude
- Restaurant name

can remain external.

Conceptually:

```
                    Restaurant Icon
                         |
           +-------------+-------------+
           |             |             |
           ↓             ↓             ↓
       Restaurant 1  Restaurant 2  Restaurant 3
       (location)    (location)    (location)
```

## 14. Real-Time Example — Social Media Emojis / Icons

Suppose a social media application displays millions of messages.

Messages might contain:

- ❤️
- 😂
- 👍
- 🔥
- 😢

Instead of loading the same image/resource independently every time:

```
Message 1 → ❤️
Message 2 → ❤️
Message 3 → ❤️
Message 4 → ❤️
```

the underlying emoji resource can be shared.

```
               ❤️ Resource
                  |
        +---------+---------+
        |         |         |
      Msg 1     Msg 2     Msg 3
```

The message-specific data remains external.

## 15. Real-Time Example — Browser / Image Cache

Suppose a web application displays the same image thousands of times.

Without sharing:

```
Image 1 → logo.png
Image 2 → logo.png
Image 3 → logo.png
Image 4 → logo.png
```

we could potentially duplicate image/resource data.

With sharing/caching:

```
              logo.png
                 |
       +---------+---------+
       |         |         |
      Page 1    Page 2    Page 3
```

The same resource can be reused.

This is conceptually similar to the Flyweight idea, although actual browser/resource caching involves additional mechanisms.

## 16. Intrinsic vs Extrinsic State — Most Important

Let's take the game example.

### Tree

### Intrinsic State

Doesn't change between instances:

```
Tree Type = Oak
Texture = oak.png
Color = Green
Model = oak-model
```

So:

```
              Shared TreeType
             /       |       \
            /        |        \
        Tree 1     Tree 2    Tree 3
```

### Extrinsic State

Changes for each tree:

```
Tree 1 → x=100, y=200
Tree 2 → x=500, y=300
Tree 3 → x=700, y=900
```

So:

```
Tree 1 → position 100,200
Tree 2 → position 500,300
Tree 3 → position 700,900
```

### Remember:

```
Intrinsic → Shared

Extrinsic → Unique
```

This distinction is critical for Flyweight.

## 17. Why Is Factory Usually Used?

You may have noticed:

- TreeFactory
- CharacterStyleFactory

Why?

Because we want to guarantee:

If an identical Flyweight already exists, reuse it instead of creating another one.

For example:

```java
TreeType oak1 =
        TreeFactory.getTreeType(
                "Oak",
                "oak.png",
                "Green"
        );

TreeType oak2 =
        TreeFactory.getTreeType(
                "Oak",
                "oak.png",
                "Green"
        );
```

Then:

```java
System.out.println(
        oak1 == oak2
);
```

Output:

```
true
```

The factory manages the shared objects.

## 18. Flyweight Structure

The basic architecture looks like:

```
                  Client
                    |
                    ↓
             FlyweightFactory
                    |
              getFlyweight()
                    |
             +------+------+
             |             |
             ↓             ↓
        Flyweight A    Flyweight B
             ↑
             |
      +------+------+------+
      |      |      |      |
      ↓      ↓      ↓      ↓
    Obj 1  Obj 2  Obj 3  Obj 4
```

The objects share Flyweight instances.

## 19. Another Java Example — Bullet Types

Imagine a shooting game.

You have:

1,000,000 bullets

Different bullets:

- Normal
- Fire
- Ice
- Laser

Each bullet has unique:

- x
- y
- velocity

But the bullet type contains:

- texture
- damage
- sound
- color

We can share the type:

```
                BulletType
                   |
       +-----------+-----------+
       |           |           |
       ↓           ↓           ↓
    Bullet 1    Bullet 2    Bullet 3
     x,y         x,y         x,y
```

This can significantly reduce repeated memory when there are many objects.

## 20. Flyweight vs Singleton

These are often confused.

### Singleton

Ensures:

Only one instance of a particular class exists.

```
Class
  ↓
One Object
```

Example:

`ConfigurationManager`

### Flyweight

Allows:

A small number of shared objects to be reused by many clients.

```
Class
  ↓
Several shared objects
  ↓
Many clients
```

Example:

```
TreeType
 ├── Oak
 ├── Pine
 └── Palm
```

So:

```
Singleton → One shared instance

Flyweight → Shared instances to reduce duplication
```

## 21. Flyweight vs Prototype

Since you've studied Prototype Pattern:

### Prototype

Creates a new object by copying an existing object.

```
Existing Object
      ↓
    clone()
      ↓
New Object
```

### Flyweight

Avoids creating duplicate objects by sharing one.

```
Shared Object
   ↑   ↑   ↑
   |   |   |
 Obj Obj Obj
```

So:

```
Prototype → Copy

Flyweight → Share
```

## 22. Flyweight vs Factory

These can also appear together.

### Factory

Responsible for:

Creating/providing objects.

```
Factory
   ↓
Object
```

### Flyweight

Responsible for:

Sharing common objects to reduce memory.

Often we use a Factory to manage Flyweights:

```
Client
  ↓
FlyweightFactory
  ↓
Existing Flyweight → Reuse
```

So:

```
Factory   → Creation/lookup

Flyweight → Sharing
```

## 23. Flyweight vs Facade

### Facade

Focuses on:

Simplifying a complex subsystem

```
Client
  ↓
Facade
  ↓
Many Services
```

### Flyweight

Focuses on:

Reducing memory through sharing

```
Many Objects
      ↓
Shared Flyweight
```

Easy difference:

```
Facade   → Simplify

Flyweight → Save memory
```

## 24. When Should You Use Flyweight?

Use Flyweight when:

- ✅ **You have a very large number of objects**

For example:

```
100,000+
1,000,000+
```

- ✅ **Many objects contain duplicate data**

For example:

```
Same texture
Same font
Same icon
Same model
Same configuration
```

- ✅ **Objects can be divided into shared and unique state**

```
Intrinsic → Shared
Extrinsic → Unique
```

- ✅ **Memory is becoming a concern**

Especially in:

- Games
- Graphics applications
- Text editors
- Maps
- Large UI systems
- Caching systems

## 25. When Should You NOT Use Flyweight?

Don't use it just because sharing sounds good.

If you only have:

10 objects

there may be little benefit.

Also avoid it if objects have mostly unique state:

```
Object 1 → completely different
Object 2 → completely different
Object 3 → completely different
```

There isn't much to share.

## 26. Advantages

### 1. Reduces memory consumption

The biggest benefit.

```
100,000 duplicate objects
        ↓
Few shared objects
```

### 2. Improves performance in memory-heavy applications

Less memory allocation can help large applications.

### 3. Promotes object reuse

Common data is stored once.

## 27. Disadvantages

### 1. Code becomes more complicated

You must separate:

- Intrinsic state
- Extrinsic state

### 2. Runtime overhead

The application may need to look up Flyweights in a factory/map.

### 3. Thread-safety considerations

If Flyweights are shared across many threads, mutable shared state can cause problems.

That's why Flyweight objects are often designed to be immutable.

## 28. Complete Diagram — Game Example

```
                         TreeFactory
                              |
                    getTreeType("Oak")
                              |
                              ↓
                        +-----------+
                        | TreeType   |
                        | Oak        |
                        | oak.png    |
                        | Green      |
                        +-----------+
                         ↑    ↑    ↑
                         |    |    |
                    +----+    |    +----+
                    |         |         |
                    ↓         ↓         ↓
                 Tree 1     Tree 2     Tree 3
                 x=100      x=500      x=700
                 y=200      y=300      y=900
```

The important point:

```
TreeType → shared

Tree     → unique position/reference
```

## 29. Interview Answer

If the interviewer asks:

**What is Flyweight Design Pattern?**

You can answer:

Flyweight is a structural design pattern used to reduce memory consumption by sharing common objects among multiple clients. It separates object state into intrinsic state, which is shared, and extrinsic state, which is supplied externally.

**Real-time example:**

In a game with thousands of trees, many trees may use the same texture, model, color, and tree type. Instead of storing this information in every tree object, we create shared TreeType Flyweight objects and let each tree store only its unique position and reference to the shared type.

## 30. Easy Way to Remember

Think about a game with 1 million trees:

Without Flyweight:

```
Tree 1 → Oak + Texture + Color + Position
Tree 2 → Oak + Texture + Color + Position
Tree 3 → Oak + Texture + Color + Position
...
Tree 1,000,000 → same data again
```

Wasteful.

With Flyweight:

```
                 Shared Oak Type
              /       |       \
             ↓        ↓        ↓
          Tree 1    Tree 2    Tree 3
          x,y       x,y       x,y
```

**One-line memory trick:**

> Flyweight Pattern = Share common data instead of storing duplicate data in every object.

And remember:

```
Intrinsic  → Shared
Extrinsic  → Unique

Factory    → Manages shared objects

Flyweight  → Saves memory
```

### The easiest comparison of the structural patterns you've covered:

| Pattern | Main Purpose |
|---|---|
| Adapter | Make incompatible interfaces work together |
| Facade | Simplify a complex subsystem |
| Flyweight | Reduce memory through object sharing |
| Decorator | Add behavior dynamically |
| Proxy | Control access to an object |
| Bridge | Separate abstraction from implementation |

**Flyweight = "Don't create it again if you can safely share it."**
