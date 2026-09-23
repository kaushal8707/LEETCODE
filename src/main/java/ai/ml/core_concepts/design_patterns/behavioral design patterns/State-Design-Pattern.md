# State Design Pattern

The State Design Pattern is a Behavioral Design Pattern.

State Pattern allows an object to change its behavior when its internal state changes.

In simple words:

The same object behaves differently depending on its current state.

## 1. Real-Time Example — Vending Machine 🥤

A vending machine is an excellent example.

A vending machine can be in different states:

```
No Money
   ↓
Money Inserted
   ↓
Product Selected
   ↓
Product Dispensed
```

What the machine does depends on its current state.

For example:

### If there is no money

```
insertMoney() → Accept money
selectProduct() → "Please insert money first"
dispense() → "Please insert money first"
```

### If money is inserted

```
insertMoney() → "Money already inserted"
selectProduct() → Select product
dispense() → "Please select product"
```

### If product is selected

```
dispense() → Dispense product
```

The same vending machine behaves differently based on its state.

That's exactly what the State Pattern solves.

## 2. Without State Pattern ❌

A common approach is:

```java
class VendingMachine {

    String state;

    public void insertMoney() {

        if (state.equals("NO_MONEY")) {
            // ...
        }
        else if (state.equals("MONEY_INSERTED")) {
            // ...
        }
        else if (state.equals("PRODUCT_SELECTED")) {
            // ...
        }
    }
}
```

Then every method starts getting:

```
if
else if
else if
else
```

As states increase, the class becomes difficult to maintain.

```
VendingMachine
      |
      +-- if NO_MONEY
      |
      +-- if MONEY_INSERTED
      |
      +-- if PRODUCT_SELECTED
      |
      +-- if OUT_OF_STOCK
      |
      +-- if ...
```

The State Pattern moves state-specific behavior into separate classes.

## 3. With State Pattern ✅

Instead of putting everything inside VendingMachine:

```
VendingMachine
      |
      ↓
Current State
      |
      +---- NoMoneyState
      +---- MoneyInsertedState
      +---- ProductSelectedState
      +---- OutOfStockState
```

Each state knows what to do.

## 4. Basic Structure

```
                 Context
                    |
                    ↓
             Current State
                    |
          +---------+---------+
          |         |         |
          ↓         ↓         ↓
       State A   State B   State C
```

The Context is the main object.

The State objects define behavior for each state.

## 5. Java Example — Vending Machine

### Step 1: State Interface

```java
interface VendingMachineState {

    void insertMoney();

    void selectProduct();

    void dispense();
}
```

Every state must implement these operations.

## 6. Context — Vending Machine

```java
class VendingMachine {

    private VendingMachineState state;

    public VendingMachine() {

        state = new NoMoneyState();
    }

    public void setState(
            VendingMachineState state) {

        this.state = state;
    }

    public void insertMoney() {

        state.insertMoney();
    }

    public void selectProduct() {

        state.selectProduct();
    }

    public void dispense() {

        state.dispense();
    }
}
```

The VendingMachine doesn't need to know all the state-specific logic.

It delegates to:

```java
state.insertMoney();
```

## 7. No Money State

```java
class NoMoneyState
        implements VendingMachineState {

    private VendingMachine machine;

    public NoMoneyState(
            VendingMachine machine) {

        this.machine = machine;
    }

    @Override
    public void insertMoney() {

        System.out.println(
                "Money inserted"
        );

        machine.setState(
                new MoneyInsertedState(machine)
        );
    }

    @Override
    public void selectProduct() {

        System.out.println(
                "Please insert money first"
        );
    }

    @Override
    public void dispense() {

        System.out.println(
                "Please insert money first"
        );
    }
}
```

## 8. Money Inserted State

```java
class MoneyInsertedState
        implements VendingMachineState {

    private VendingMachine machine;

    public MoneyInsertedState(
            VendingMachine machine) {

        this.machine = machine;
    }

    @Override
    public void insertMoney() {

        System.out.println(
                "Money already inserted"
        );
    }

    @Override
    public void selectProduct() {

        System.out.println(
                "Product selected"
        );

        machine.setState(
                new ProductSelectedState(machine)
        );
    }

    @Override
    public void dispense() {

        System.out.println(
                "Please select a product"
        );
    }
}
```

## 9. Product Selected State

```java
class ProductSelectedState
        implements VendingMachineState {

    private VendingMachine machine;

    public ProductSelectedState(
            VendingMachine machine) {

        this.machine = machine;
    }

    @Override
    public void insertMoney() {

        System.out.println(
                "Please wait, product is being dispensed"
        );
    }

    @Override
    public void selectProduct() {

        System.out.println(
                "Product already selected"
        );
    }

    @Override
    public void dispense() {

        System.out.println(
                "Product dispensed"
        );

        machine.setState(
                new NoMoneyState(machine)
        );
    }
}
```

## 10. Client Code

```java
public class Main {

    public static void main(String[] args) {

        VendingMachine machine =
                new VendingMachine();

        machine.selectProduct();

        machine.insertMoney();

        machine.selectProduct();

        machine.dispense();
    }
}
```

Output:

```
Please insert money first
Money inserted
Product selected
Product dispensed
```

The behavior changes as the machine moves through different states.

## 11. Understanding the State Transition

The important part is the transition:

```
NoMoneyState
      |
      | insertMoney()
      ↓
MoneyInsertedState
      |
      | selectProduct()
      ↓
ProductSelectedState
      |
      | dispense()
      ↓
NoMoneyState
```

Think of it as:

```
         insertMoney
              ↓
       +---------------+
       | No Money      |
       +---------------+
              |
              ↓
       +---------------+
       | Money Inserted|
       +---------------+
              |
              ↓
       +---------------+
       | Product       |
       | Selected      |
       +---------------+
              |
              ↓
       +---------------+
       | Product       |
       | Dispensed     |
       +---------------+
```

## 12. Real-Time Example — Online Order System 📦

This is extremely common in real-world applications.

An order can have states:

```
Order Placed
     ↓
Payment Pending
     ↓
Payment Successful
     ↓
Preparing
     ↓
Shipped
     ↓
Delivered
```

The same operation may behave differently depending on the order state.

For example:

`cancelOrder()`

### Order Placed

```
cancelOrder()
→ Cancellation allowed
```

### Shipped

```
cancelOrder()
→ Cancellation not allowed
```

### Delivered

```
cancelOrder()
→ Cannot cancel delivered order
```

So:

```
Order
  ↓
Current State
  |
  +-- PlacedState
  +-- PaidState
  +-- ShippedState
  +-- DeliveredState
```

## 13. Order State Example

```java
interface OrderState {

    void pay();

    void ship();

    void cancel();

    void deliver();
}
```

Context:

```java
class Order {

    private OrderState state;

    public Order() {

        state =
                new OrderPlacedState(this);
    }

    public void setState(OrderState state) {

        this.state = state;
    }

    public void pay() {

        state.pay();
    }

    public void ship() {

        state.ship();
    }

    public void cancel() {

        state.cancel();
    }

    public void deliver() {

        state.deliver();
    }
}
```

Then:

- OrderPlacedState
- PaidState
- ShippedState
- DeliveredState
- CancelledState

can each implement their own behavior.

## 14. Real-Time Example — Traffic Signal 🚦

A traffic signal can have:

- RED
- YELLOW
- GREEN

Its behavior changes based on its state.

```
RED
 ↓
GREEN
 ↓
YELLOW
 ↓
RED
```

For example:

```
RED
Vehicles → Stop

GREEN
Vehicles → Go

YELLOW
Vehicles → Prepare to stop
```

Using State Pattern:

```
TrafficLight
      |
      ↓
Current State
      |
      +-- RedState
      +-- GreenState
      +-- YellowState
```

## 15. Real-Time Example — Media Player 🎵

A music player can have:

- Stopped
- Playing
- Paused

The behavior of:

- `play()`
- `pause()`
- `stop()`

depends on the current state.

### Stopped

```
play() → Start playing
pause() → Invalid operation
```

### Playing

```
pause() → Pause
stop() → Stop
play() → Already playing
```

### Paused

```
play() → Resume
stop() → Stop
```

Architecture:

```
MediaPlayer
      |
      ↓
Current State
      |
      +-- StoppedState
      +-- PlayingState
      +-- PausedState
```

## 16. Real-Time Example — ATM 🏦

An ATM can have:

- Idle
- Card Inserted
- PIN Entered
- Transaction Selected
- Processing

The same operation behaves differently depending on the state.

For example:

`withdraw()`

```
Idle
→ Please insert card

Card Inserted
→ Please enter PIN

PIN Entered
→ Select transaction

Transaction Selected
→ Process withdrawal
```

This is a natural State Pattern use case.

## 17. Real-Time Example — Authentication 🔐

A user session might have:

```
Logged Out
     ↓
Logging In
     ↓
Logged In
     ↓
Session Expired
```

Consider:

`accessResource()`

```
Logged Out
Access denied

Logged In
Access allowed

Session Expired
Please login again
```

Instead of having many conditions:

```java
if (state == LOGGED_OUT) ...
else if (state == LOGGED_IN) ...
else if (state == EXPIRED) ...
```

the behavior can be placed inside state classes.

## 18. State Pattern vs if-else

This is one of the main reasons we use State Pattern.

### Without State Pattern

```java
if (state == "NEW") {

    // behavior

} else if (state == "PAID") {

    // behavior

} else if (state == "SHIPPED") {

    // behavior

} else if (state == "DELIVERED") {

    // behavior
}
```

As states increase:

```
if
else if
else if
else if
else if
...
```

becomes difficult to maintain.

### With State Pattern

```
Order
  ↓
State
  |
  +-- NewState
  +-- PaidState
  +-- ShippedState
  +-- DeliveredState
```

Each class handles its own behavior.

## 19. State Pattern vs Strategy Pattern

This is a very important interview question, especially since you've already studied Strategy.

They look very similar because both use interfaces and composition.

### Strategy

The client/context generally chooses which algorithm to use.

```
Payment
   ↓
PaymentStrategy
   |
   +-- CreditCard
   +-- UPI
   +-- PayPal
```

The goal is:

Choose an algorithm.

### State

The object changes behavior because its state changes.

```
Order
  ↓
Current State
  |
  +-- New
  +-- Paid
  +-- Shipped
  +-- Delivered
```

The goal is:

Change behavior based on state.

### Easy way to remember:

```
Strategy → Which algorithm should I use?

State    → What can I do in my current state?
```

## 20. State vs Command

You have also studied Command.

### Command

Encapsulates a request:

```
Command
  ↓
execute()
```

Example:

- TurnOnCommand
- TurnOffCommand

### State

Changes behavior based on current state:

```
Object
 ↓
Current State
```

Remember:

```
Command → Encapsulate request

State   → Behavior depends on state
```

## 21. State vs Observer

### Observer

One object notifies multiple objects when something changes.

```
Subject
  |
  +---- Observer 1
  +---- Observer 2
  +---- Observer 3
```

### State

An object changes its behavior when its state changes.

```
Context
  ↓
Current State
```

Remember:

```
Observer → Notify others

State    → Change own behavior
```

## 22. Advantages

### 1. Removes large conditional statements

Instead of:

```
if
else if
else if
```

we have separate state classes.

### 2. Follows Single Responsibility Principle

Each state class handles behavior related to one state.

### 3. Easy to add new states

For example:

`OutForDeliveryState`

can be added without putting more conditions into the main class.

### 4. Makes state transitions explicit

You can clearly see:

```
Placed → Paid → Shipped → Delivered
```

## 23. Disadvantages

### 1. More classes

For five states, you may have:

```
5 State classes
+ Context
+ State interface
```

### 2. Can be overkill for simple state machines

If you only have:

```
ON
OFF
```

a simple boolean may be enough.

### 3. State transitions need careful management

You need to make sure invalid transitions aren't allowed.

## 24. When Should You Use State Pattern?

Use it when:

- ✅ **An object has many states**

For example:

```
New
Paid
Processing
Shipped
Delivered
Cancelled
```

- ✅ **Behavior changes significantly based on state**

For example:

```
cancel()
pay()
ship()
```

behave differently depending on the state.

- ✅ **You have large state-based if-else or switch statements**

```
if state == A
else if state == B
else if state == C
...
```

- ✅ **State transitions are important to the business logic**

Examples:

- Order lifecycle
- Payment lifecycle
- User session
- Workflow
- Approval process
- ATM
- Vending machine
- Media player

## 25. When NOT to Use It

Don't use State Pattern for every boolean.

For example:

```java
boolean isActive;
```

If you only have:

- Active
- Inactive

and behavior is simple, a State Pattern may be unnecessary.

Use it when the number/complexity of states makes the conditional approach difficult to maintain.

## 26. Real-Time Order Example — Complete Flow

Imagine Amazon-like order processing:

```
                Order Created
                     |
                     ↓
                NEW STATE
                     |
                  payment
                     ↓
                PAID STATE
                     |
                   ship
                     ↓
              SHIPPED STATE
                     |
                 deliver
                     ↓
             DELIVERED STATE
```

Now consider `cancel()`:

```
NEW       → Can cancel
PAID      → Can cancel/refund
SHIPPED   → Usually cannot cancel
DELIVERED → Cannot cancel
```

Without State Pattern:

```
cancel()
    |
    +-- if NEW
    |
    +-- if PAID
    |
    +-- if SHIPPED
    |
    +-- if DELIVERED
```

With State Pattern:

```
NewState.cancel()
PaidState.cancel()
ShippedState.cancel()
DeliveredState.cancel()
```

Each state decides what should happen.

## 27. State Pattern Architecture

```
                         CLIENT
                            |
                            ↓
                        CONTEXT
                      +---------+
                      |  Order  |
                      +---------+
                           |
                           ↓
                     Current State
                           |
            +--------------+--------------+
            |              |              |
            ↓              ↓              ↓
        NewState        PaidState     ShippedState
            |              |              |
            ↓              ↓              ↓
        behavior        behavior       behavior
```

The Context delegates operations to the current State.

## 28. Key Point — State Objects Can Change the Context

This is an important difference from many other patterns.

For example:

```java
class PaidState implements OrderState {

    private Order order;

    @Override
    public void ship() {

        System.out.println(
                "Order shipped"
        );

        order.setState(
                new ShippedState(order)
        );
    }
}
```

So:

```
Current State
     |
     | operation
     ↓
Changes Context's state
     |
     ↓
New State
```

This creates the state transition.

## 29. Interview Answer

If the interviewer asks:

**What is State Design Pattern?**

You can say:

State is a behavioral design pattern that allows an object to change its behavior when its internal state changes. It encapsulates state-specific behavior into separate classes and allows the context to delegate operations to its current state.

**Real-time example:**

An e-commerce order can move through states such as New, Paid, Shipped, and Delivered. Operations like cancel(), ship(), and refund() behave differently depending on the current order state. State Pattern moves this state-specific behavior into separate classes instead of maintaining a large if-else or switch statement.

## 30. Easy Way to Remember

Think about an online order:

```
NEW
 ↓
PAID
 ↓
SHIPPED
 ↓
DELIVERED
```

The same method:

`cancel()`

behaves differently:

```
NEW       → Cancel ✓
PAID      → Refund/Cancel ✓
SHIPPED   → Cancel ✗
DELIVERED → Cancel ✗
```

That's State Pattern.

**One-line memory trick:**

> 🔄 State Pattern = Same object + different state → different behavior.

And for the behavioral patterns you've been learning:

| Pattern | Remember |
|---|---|
| Strategy | Choose an algorithm |
| Command | Encapsulate a request |
| Observer | Notify subscribers |
| State | Change behavior based on state |
| Template Method | Define algorithm skeleton |
| Chain of Responsibility | Pass request through handlers |
| Mediator | Centralize communication |
| Iterator | Traverse a collection |

### The key difference:

```
Strategy → "Which algorithm should I use?"

State    → "What should I do in my current state?"
```
