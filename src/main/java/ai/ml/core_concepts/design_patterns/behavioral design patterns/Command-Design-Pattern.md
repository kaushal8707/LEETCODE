# Command Design Pattern

The Command Design Pattern is a Behavioral Design Pattern.

Command Pattern encapsulates a request as an object, allowing you to parameterize, queue, log, undo, or execute requests later.

In simple words:

Convert an action/request into an object.

## 1. Real-Time Example — Remote Control

Think about a remote control.

You have buttons:

```
Remote Control
     |
     +── ON
     +── OFF
     +── Volume Up
     +── Volume Down
```

The remote shouldn't need to know how a TV turns on.

It simply says:

"Execute the ON command"

The actual TV knows how to turn itself on.

This gives us:

```
Remote Control
      |
      ↓
   Command
      |
      ↓
      TV
```

## 2. Without Command Pattern ❌

Suppose we directly write:

```java
class RemoteControl {

    private TV tv;

    public RemoteControl(TV tv) {
        this.tv = tv;
    }

    public void pressOnButton() {
        tv.turnOn();
    }

    public void pressOffButton() {
        tv.turnOff();
    }

    public void pressVolumeUpButton() {
        tv.volumeUp();
    }
}
```

This works.

But imagine the remote needs to control:

- TV
- Light
- AC
- Music System
- Door
- Fan

The remote becomes tightly coupled to all these devices.

```
RemoteControl
   |
   +── TV
   +── Light
   +── AC
   +── Fan
   +── Music System
```

This becomes difficult to maintain.

## 3. Command Pattern Solution ✅

We introduce a Command interface.

```java
interface Command {

    void execute();
}
```

Now every action becomes a command.

For example:

- Turn TV ON
- Turn TV OFF
- Turn Light ON
- Turn Light OFF

Each action becomes its own object.

## 4. Receiver

The Receiver is the object that actually performs the operation.

For example:

```java
class TV {

    public void turnOn() {
        System.out.println("TV is ON");
    }

    public void turnOff() {
        System.out.println("TV is OFF");
    }
}
```

The TV knows how to perform:

- `turnOn()`
- `turnOff()`

The command doesn't need to implement the actual TV logic.

## 5. Concrete Command

Create a command for turning the TV on:

```java
class TVOnCommand implements Command {

    private TV tv;

    public TVOnCommand(TV tv) {
        this.tv = tv;
    }

    @Override
    public void execute() {
        tv.turnOn();
    }
}
```

And another command for turning the TV off:

```java
class TVOffCommand implements Command {

    private TV tv;

    public TVOffCommand(TV tv) {
        this.tv = tv;
    }

    @Override
    public void execute() {
        tv.turnOff();
    }
}
```

Now:

```
TVOnCommand
      |
      ↓
     TV
      |
      ↓
  turnOn()
```

## 6. Invoker

The object that triggers the command is called the Invoker.

Here:

`RemoteControl`

is our Invoker.

```java
class RemoteControl {

    private Command command;

    public void setCommand(Command command) {
        this.command = command;
    }

    public void pressButton() {
        command.execute();
    }
}
```

Notice something important:

RemoteControl doesn't know about:

- TV
- Light
- AC

It only knows:

- Command

## 7. Complete Example

### Command

```java
interface Command {

    void execute();
}
```

### Receiver

```java
class TV {

    public void turnOn() {
        System.out.println("TV is ON");
    }

    public void turnOff() {
        System.out.println("TV is OFF");
    }
}
```

### Concrete Command

```java
class TVOnCommand implements Command {

    private TV tv;

    public TVOnCommand(TV tv) {
        this.tv = tv;
    }

    @Override
    public void execute() {
        tv.turnOn();
    }
}
```

```java
class TVOffCommand implements Command {

    private TV tv;

    public TVOffCommand(TV tv) {
        this.tv = tv;
    }

    @Override
    public void execute() {
        tv.turnOff();
    }
}
```

### Invoker

```java
class RemoteControl {

    private Command command;

    public void setCommand(Command command) {
        this.command = command;
    }

    public void pressButton() {
        command.execute();
    }
}
```

### Main

```java
public class Main {

    public static void main(String[] args) {

        TV tv = new TV();

        Command tvOn =
                new TVOnCommand(tv);

        Command tvOff =
                new TVOffCommand(tv);

        RemoteControl remote =
                new RemoteControl();

        remote.setCommand(tvOn);
        remote.pressButton();

        remote.setCommand(tvOff);
        remote.pressButton();
    }
}
```

Output:

```
TV is ON
TV is OFF
```

## 8. Understand the Roles

There are usually four important participants.

```
                  Command
                     ↑
              +------+------+
              |             |
        TVOnCommand    TVOffCommand
              |             |
              +------+------+
                     |
                     ↓
                    TV
                 (Receiver)

RemoteControl
   (Invoker)
       |
       ↓
   Command
```

### 1. Command

Defines the operation.

```java
interface Command {
    void execute();
}
```

### 2. Concrete Command

Implements a particular operation.

- TVOnCommand
- TVOffCommand

### 3. Receiver

Actually performs the work.

- TV
- Light
- AC

### 4. Invoker

Triggers the command.

- RemoteControl

## 9. Another Real-Time Example — Food Ordering System

This pattern is very useful in an online food delivery system.

Imagine a restaurant system receives:

- Place Order
- Cancel Order
- Prepare Order
- Deliver Order

Instead of directly calling methods everywhere, each request can become a command.

```
Customer
    |
    ↓
PlaceOrderCommand
    |
    ↓
OrderService
```

### Command Interface

```java
interface OrderCommand {

    void execute();
}
```

### Receiver:

```java
class OrderService {

    public void placeOrder() {
        System.out.println(
                "Order placed"
        );
    }

    public void cancelOrder() {
        System.out.println(
                "Order cancelled"
        );
    }
}
```

### Place order command:

```java
class PlaceOrderCommand
        implements OrderCommand {

    private OrderService orderService;

    public PlaceOrderCommand(
            OrderService orderService) {

        this.orderService = orderService;
    }

    @Override
    public void execute() {

        orderService.placeOrder();
    }
}
```

### Cancel order command:

```java
class CancelOrderCommand
        implements OrderCommand {

    private OrderService orderService;

    public CancelOrderCommand(
            OrderService orderService) {

        this.orderService = orderService;
    }

    @Override
    public void execute() {

        orderService.cancelOrder();
    }
}
```

Now the system can treat:

- PlaceOrderCommand
- CancelOrderCommand

as objects.

## 10. Why make a request an object?

This is the main purpose of Command Pattern.

Once a request becomes an object:

```
"Turn TV ON"
       ↓
TVOnCommand object
```

we can do things with that object.

For example:

- **Execute it** — `command.execute();`
- **Store it** — `List<Command>`
- **Queue it** — Command Queue
- **Log it** — Command → Log
- **Undo it** — Command → `undo()`

This is why Command is much more powerful than simply calling a method.

## 11. Real-Time Example — Undo Operation

One of the most famous uses of Command Pattern is Undo.

Consider a text editor.

Actions might be:

- Type text
- Delete text
- Copy
- Paste
- Format

If every action is represented by a command:

- TypeCommand
- DeleteCommand
- PasteCommand

we can store executed commands:

```
Command History

1. TypeCommand
2. TypeCommand
3. DeleteCommand
4. PasteCommand
```

When the user presses:

`CTRL + Z`

we can undo the most recent command.

## 12. Command with Undo

Change the interface:

```java
interface Command {

    void execute();

    void undo();
}
```

### Receiver:

```java
class Light {

    public void turnOn() {
        System.out.println(
                "Light is ON"
        );
    }

    public void turnOff() {
        System.out.println(
                "Light is OFF"
        );
    }
}
```

### Command:

```java
class LightOnCommand
        implements Command {

    private Light light;

    public LightOnCommand(Light light) {
        this.light = light;
    }

    @Override
    public void execute() {

        light.turnOn();
    }

    @Override
    public void undo() {

        light.turnOff();
    }
}
```

Now:

`command.execute();`

does:

Light ON

And:

`command.undo();`

does:

Light OFF

That's the power of encapsulating the action as an object.

## 13. Real-Time Example — Job Queue

Command Pattern is also useful in systems where requests need to be processed later.

Imagine an application receives:

- Send Email
- Generate Report
- Process Payment
- Resize Image
- Generate Invoice

Instead of immediately processing everything:

```
Request
   ↓
Command Object
   ↓
Queue
   ↓
Worker
   ↓
Execute
```

For example:

```java
Queue<Command> queue =
        new LinkedList<>();
```

Add commands:

```java
queue.add(
        new SendEmailCommand()
);

queue.add(
        new GenerateReportCommand()
);

queue.add(
        new ProcessPaymentCommand()
);
```

A worker can process them:

```java
while (!queue.isEmpty()) {

    Command command = queue.poll();

    command.execute();
}
```

This is a common conceptual use of Command in asynchronous/job-processing systems.

## 14. Real-Time Example — Banking

Consider a banking application.

Operations could be:

- Deposit
- Withdraw
- Transfer
- Pay Bill

Instead of directly calling:

```java
account.withdraw(5000);
```

you could have:

`WithdrawCommand`

containing:

- Account
- Amount
- Transaction ID

Then:

```
WithdrawCommand
       |
       ↓
BankAccount
       |
       ↓
withdraw()
```

Because the withdrawal is represented as an object, it can potentially be:

- Executed
- Queued
- Logged
- Audited
- Retried
- Undone/compensated

This is particularly useful for transaction processing and audit-oriented systems.

## 15. Command Pattern in a REST Application

Suppose you have:

```
POST /orders
DELETE /orders/{id}
```

Conceptually, the operations could be represented as:

- CreateOrderCommand
- CancelOrderCommand

Then:

```
REST Controller
      |
      ↓
Command
      |
      ↓
Handler/Receiver
      |
      ↓
Business Logic
```

This can be useful when commands need validation, auditing, queuing, retries, or asynchronous processing.

## 16. Command vs Strategy

Since you just learned Strategy Pattern, this distinction is very important.

Both use interfaces and composition, but their intent is different.

### Strategy

Choose an algorithm/behavior.

Example:

```
Payment
   ↓
UPI Strategy
Card Strategy
PayPal Strategy
```

Question:

How should this operation be performed?

### Command

Represent a request/action as an object.

Example:

```
Remote
   ↓
TurnOnCommand
TurnOffCommand
```

Question:

What action/request should be performed?

So:

- **Strategy → HOW?**
- **Command  → WHAT?**

## 17. Command vs Factory

### Factory

Factory is about:

Creating objects.

```
PaymentFactory
      ↓
UPIPaymentStrategy
```

### Command

Command is about:

Representing an action as an object.

```
TurnOnCommand
      ↓
execute()
```

So:

- **Factory  → Object creation**
- **Command  → Request encapsulation**

## 18. Command vs Observer

These are also different.

### Command

Usually represents:

Sender → Request → Receiver

Example:

```
Remote → TurnOnCommand → TV
```

### Observer

Represents:

Subject → Notification → Observers

Example:

```
Stock Price
    |
    +── Investor
    +── Trading App
    +── Notification Service
```

So:

- **Command  → Perform an action**
- **Observer → Notify interested objects**

## 19. When Should You Use Command Pattern?

Use Command when you need:

- ✅ **Undo/Redo** — Ctrl + Z, Ctrl + Y
- ✅ **Queue requests** — Command Queue
- ✅ **Schedule operations** — Execute later
- ✅ **Log operations** — Command → Audit Log
- ✅ **Retry failed operations** — Command → Failed → Retry
- ✅ **Decouple sender from receiver**

```
Remote
  ↓
Command
  ↓
TV
```

The remote doesn't need to know how the TV works.

## 20. Command Pattern Structure

Remember this structure:

```
                 COMMAND
                    |
             +------+------+
             |             |
          Concrete       Concrete
          Command A       Command B
             |               |
             +-------+-------+
                     |
                     ↓
                 RECEIVER
                     |
                     ↓
               Actual action


                  INVOKER
                     |
                     ↓
                  COMMAND
```

For the remote example:

```
RemoteControl
   (Invoker)
       |
       ↓
    Command
       |
       +------ TVOnCommand
       |
       +------ TVOffCommand
                 |
                 ↓
                 TV
              (Receiver)
```

## 21. Simple Java Template

You can remember this basic structure for interviews:

### Command

```java
// Command
interface Command {

    void execute();
}
```

### Receiver

```java
// Receiver
class Receiver {

    public void action() {

        System.out.println(
                "Performing action"
        );
    }
}
```

### Concrete Command

```java
// Concrete Command
class ConcreteCommand
        implements Command {

    private Receiver receiver;

    public ConcreteCommand(
            Receiver receiver) {

        this.receiver = receiver;
    }

    @Override
    public void execute() {

        receiver.action();
    }
}
```

### Invoker

```java
// Invoker
class Invoker {

    private Command command;

    public void setCommand(
            Command command) {

        this.command = command;
    }

    public void executeCommand() {

        command.execute();
    }
}
```

### Usage:

```java
Receiver receiver =
        new Receiver();

Command command =
        new ConcreteCommand(receiver);

Invoker invoker =
        new Invoker();

invoker.setCommand(command);

invoker.executeCommand();
```

## 22. Interview Answer

If an interviewer asks:

**What is Command Design Pattern?**

You can say:

Command is a behavioral design pattern that encapsulates a request or action as an object. It separates the object that initiates a request from the object that performs the request. This allows commands to be stored, queued, logged, scheduled, retried, and potentially undone.

**Real-time example:**

A remote control can be designed using the Command Pattern. The remote is the Invoker, Command is the interface, TVOnCommand and TVOffCommand are concrete commands, and the TV is the Receiver. The remote doesn't directly know how the TV operates; it simply executes a command.

## 23. Easy Way to Remember

Think of a food delivery order:

```
Customer
   |
   | "Place Order"
   ↓
PlaceOrderCommand
   |
   ↓
OrderService
   |
   ↓
Order placed
```

The request:

"Place Order"

becomes:

`PlaceOrderCommand`

That's the whole idea.

**One-line memory trick:**

> Command Pattern = Turn a request/action into an object.

And remember the four roles:

```
Command    → What action?
Concrete   → Specific action
Receiver   → Who performs it?
Invoker    → Who triggers it?
```

**Command = encapsulated action/request.**
