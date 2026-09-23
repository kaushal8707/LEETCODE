# Chain of Responsibility Design Pattern

The Chain of Responsibility (CoR) is a Behavioral Design Pattern.

It passes a request through a chain of handlers until one handler handles the request or the chain is exhausted.

In simple words:

```
Request → Handler 1 → Handler 2 → Handler 3 → ...
```

Each handler decides:

```
Can I handle this request?
If yes → handle it.
If no → pass it to the next handler.
```

## 1. Real-Time Example — Customer Support 🎧

Imagine you contact an online shopping company's support team.

Your complaint goes through:

```
Customer
   ↓
Level 1 Support
   ↓
Level 2 Support
   ↓
Technical Support
   ↓
Manager
```

Suppose you have a simple password problem:

```
Customer
   ↓
Level 1 Support
   ↓
Solved
```

For a technical issue:

```
Customer
   ↓
Level 1 Support
   ↓
Level 2 Support
   ↓
Technical Support
   ↓
Solved
```

For a serious financial issue:

```
Customer
   ↓
Level 1
   ↓
Level 2
   ↓
Manager
   ↓
Solved
```

This is exactly the Chain of Responsibility Pattern.

## 2. Why Do We Need It?

Suppose we write everything in one class:

```java
if (issue == PASSWORD) {

    // Level 1

} else if (issue == TECHNICAL) {

    // Level 2

} else if (issue == BILLING) {

    // Billing

} else if (issue == SERIOUS) {

    // Manager
}
```

As the application grows:

```
if
else if
else if
else if
else if
...
```

becomes difficult to maintain.

Instead, we create separate handlers:

- PasswordHandler
- TechnicalHandler
- BillingHandler
- ManagerHandler

and connect them:

```
PasswordHandler
       ↓
TechnicalHandler
       ↓
BillingHandler
       ↓
ManagerHandler
```

## 3. Basic Structure

The pattern generally looks like this:

```
                  Client
                    |
                    ↓
              +-----------+
              | Handler 1 |
              +-----------+
                    |
                  next
                    ↓
              +-----------+
              | Handler 2 |
              +-----------+
                    |
                  next
                    ↓
              +-----------+
              | Handler 3 |
              +-----------+
```

Each handler contains a reference to the next handler.

## 4. Main Components

There are usually three important parts.

### 1. Handler

Defines how requests are handled.

```java
interface Handler {

    void setNext(Handler handler);

    void handle(Request request);
}
```

### 2. Concrete Handler

Actually processes specific requests.

Examples:

- Level1Support
- Level2Support
- Manager

### 3. Client

Creates the chain and sends the request.

```
Client
 ↓
Handler 1
 ↓
Handler 2
 ↓
Handler 3
```

## 5. Java Example — Customer Support

Let's build a complete example.

### Step 1 — Request

First create a request.

```java
class SupportRequest {

    private String issue;
    private int priority;

    public SupportRequest(
            String issue,
            int priority) {

        this.issue = issue;
        this.priority = priority;
    }

    public String getIssue() {
        return issue;
    }

    public int getPriority() {
        return priority;
    }
}
```

Here:

```
priority = 1 → Level 1
priority = 2 → Level 2
priority = 3 → Manager
```

## 6. Handler Interface

```java
interface SupportHandler {

    void setNext(SupportHandler next);

    void handle(SupportRequest request);
}
```

Every handler must implement:

- `setNext()`
- `handle()`

## 7. Level 1 Support

```java
class Level1Support
        implements SupportHandler {

    private SupportHandler next;

    @Override
    public void setNext(SupportHandler next) {

        this.next = next;
    }

    @Override
    public void handle(
            SupportRequest request) {

        if (request.getPriority() == 1) {

            System.out.println(
                    "Level 1 Support handled: "
                    + request.getIssue()
            );

        } else {

            if (next != null) {
                next.handle(request);
            }
        }
    }
}
```

If Level 1 cannot handle the request:

```java
next.handle(request);
```

The request moves forward.

## 8. Level 2 Support

```java
class Level2Support
        implements SupportHandler {

    private SupportHandler next;

    @Override
    public void setNext(SupportHandler next) {

        this.next = next;
    }

    @Override
    public void handle(
            SupportRequest request) {

        if (request.getPriority() == 2) {

            System.out.println(
                    "Level 2 Support handled: "
                    + request.getIssue()
            );

        } else {

            if (next != null) {
                next.handle(request);
            }
        }
    }
}
```

## 9. Manager

```java
class Manager
        implements SupportHandler {

    private SupportHandler next;

    @Override
    public void setNext(SupportHandler next) {

        this.next = next;
    }

    @Override
    public void handle(
            SupportRequest request) {

        if (request.getPriority() == 3) {

            System.out.println(
                    "Manager handled: "
                    + request.getIssue()
            );

        } else {

            if (next != null) {
                next.handle(request);
            }
        }
    }
}
```

## 10. Create the Chain

Now connect the handlers.

```java
public class Main {

    public static void main(String[] args) {

        SupportHandler level1 =
                new Level1Support();

        SupportHandler level2 =
                new Level2Support();

        SupportHandler manager =
                new Manager();

        level1.setNext(level2);
        level2.setNext(manager);

        SupportRequest request =
                new SupportRequest(
                        "Password reset",
                        1
                );

        level1.handle(request);
    }
}
```

Output:

```
Level 1 Support handled: Password reset
```

## 11. What Happens Internally?

The chain is:

```
Level 1
   ↓
Level 2
   ↓
Manager
```

The request enters:

```java
level1.handle(request);
```

Level 1 checks:

Can I handle it?

If yes:

Handle request

If no:

```java
level2.handle(request)
```

Then Level 2 checks.

If Level 2 cannot handle:

```java
manager.handle(request)
```

So:

```
Request
   ↓
Level 1
   |
   | Can't handle
   ↓
Level 2
   |
   | Can't handle
   ↓
Manager
```

## 12. Real-Time Example — Expense Approval 💰

This is one of the best enterprise examples.

Imagine an employee wants to claim an expense.

Different managers can approve different amounts:

```
Employee
   ↓
Team Lead
   ↓
Manager
   ↓
Director
   ↓
VP
```

For example:

```
₹5,000
→ Team Lead

₹50,000
→ Manager

₹5,00,000
→ Director

₹50,00,000
→ VP
```

The employee doesn't need to know who will approve the expense.

The request simply enters the chain.

```
Expense Request
       ↓
Team Lead
       ↓
Manager
       ↓
Director
       ↓
VP
```

## 13. Expense Approval Example

```java
interface Approver {

    void setNext(Approver next);

    void approve(double amount);
}
```

### Team Lead:

```java
class TeamLead implements Approver {

    private Approver next;

    @Override
    public void setNext(Approver next) {
        this.next = next;
    }

    @Override
    public void approve(double amount) {

        if (amount <= 10000) {

            System.out.println(
                    "Team Lead approved ₹" + amount
            );

        } else {

            next.approve(amount);
        }
    }
}
```

### Manager:

```java
class Manager implements Approver {

    private Approver next;

    @Override
    public void setNext(Approver next) {
        this.next = next;
    }

    @Override
    public void approve(double amount) {

        if (amount <= 100000) {

            System.out.println(
                    "Manager approved ₹" + amount
            );

        } else {

            next.approve(amount);
        }
    }
}
```

### Director:

```java
class Director implements Approver {

    private Approver next;

    @Override
    public void setNext(Approver next) {
        this.next = next;
    }

    @Override
    public void approve(double amount) {

        if (amount <= 1000000) {

            System.out.println(
                    "Director approved ₹" + amount
            );

        } else {

            next.approve(amount);
        }
    }
}
```

### Client:

```java
public class Main {

    public static void main(String[] args) {

        Approver teamLead =
                new TeamLead();

        Approver manager =
                new Manager();

        Approver director =
                new Director();

        teamLead.setNext(manager);
        manager.setNext(director);

        teamLead.approve(500000);
    }
}
```

Flow:

```
₹5,00,000
   ↓
Team Lead
   |
   | Can't approve
   ↓
Manager
   |
   | Can't approve
   ↓
Director
   |
   ↓
Approved
```

Output:

```
Director approved ₹500000.0
```

## 14. Real-Time Example — Logging System 📝

Suppose an application receives logs:

- INFO
- WARNING
- ERROR
- CRITICAL

Different handlers can process different levels.

```
Log Request
     ↓
Info Handler
     ↓
Warning Handler
     ↓
Error Handler
     ↓
Critical Handler
```

For example:

```
INFO
→ InfoHandler

WARNING
→ WarningHandler

ERROR
→ ErrorHandler

CRITICAL
→ CriticalHandler
```

This can be used in application logging pipelines.

## 15. Real-Time Example — HTTP Request Processing 🌐

A web request can pass through several handlers:

```
HTTP Request
     ↓
Authentication
     ↓
Authorization
     ↓
Validation
     ↓
Rate Limiting
     ↓
Business Logic
```

For example:

```
Request
   ↓
Authentication Handler
   |
   | Valid
   ↓
Authorization Handler
   |
   | Authorized
   ↓
Validation Handler
   |
   | Valid
   ↓
Business Logic
```

If authentication fails:

```
Request
   ↓
Authentication
   ↓
401 Unauthorized
```

The request never reaches the remaining handlers.

This is a very common real-world application of the pattern.

## 16. Real-Time Example — Servlet Filters

If you're working with Java web applications, this concept is particularly useful.

You might have:

```
Request
  ↓
Authentication Filter
  ↓
Logging Filter
  ↓
CORS Filter
  ↓
Validation Filter
  ↓
Controller
```

Each filter can:

- Process the request.
- Decide whether processing should continue.
- Pass it to the next filter.

Conceptually:

```
Filter 1
   ↓
Filter 2
   ↓
Filter 3
   ↓
Controller
```

This follows the Chain of Responsibility idea.

## 17. Real-Time Example — ATM Cash Dispensing 💵

Another interesting example is an ATM dispensing cash.

Suppose an ATM has:

- ₹2000 notes
- ₹500 notes
- ₹200 notes
- ₹100 notes

The request:

Withdraw ₹4,700

can pass through handlers responsible for different denominations.

Conceptually:

```
₹4700
  ↓
₹2000 Handler
  ↓
₹500 Handler
  ↓
₹200 Handler
  ↓
₹100 Handler
```

Each handler dispenses what it can and passes the remaining amount onward.

For example:

```
₹4700
 ↓
₹2000 → 2 notes = ₹4000
 ↓
Remaining = ₹700
 ↓
₹500 → 1 note
 ↓
Remaining = ₹200
 ↓
₹200 → 1 note
 ↓
Remaining = ₹0
```

This is a classic educational example of Chain of Responsibility.

## 18. Real-Time Example — Exception Handling

Exception handling can also be understood conceptually as a chain.

```
Current Method
      ↓
Caller
      ↓
Caller
      ↓
Higher-level Handler
```

If the current method doesn't handle an exception:

`catch`

the exception can propagate to its caller.

```
Method A
   ↓
Method B
   ↓
Method C
   ↓
Exception
   ↑
Method C doesn't handle
   ↑
Method B doesn't handle
   ↑
Method A handles
```

The request/error travels through a chain until somebody handles it.

## 19. Important Concept — Handler Doesn't Have to Handle

This is the heart of the pattern.

Suppose we have:

```
Handler A
   ↓
Handler B
   ↓
Handler C
```

Request arrives at A.

A can say:

"I can't handle this."

Then:

A → B

B can say:

"I can't handle this either."

Then:

B → C

C handles it.

```
Request
  ↓
 A ──cannot──→ B ──cannot──→ C
                              ↓
                           HANDLED
```

## 20. Two Common Variations

There are two common ways the chain behaves.

### Variation 1 — One handler handles it

```
A → B → C
        ↑
     handled
```

Once a handler handles the request, processing stops.

### Variation 2 — Multiple handlers process it

Sometimes every handler can perform an operation and then pass the request onward.

```
Request
  ↓
Logging
  ↓
Authentication
  ↓
Validation
  ↓
Business Logic
```

This is common in:

- Filters
- Middleware
- Pipelines
- Request processing

So Chain of Responsibility doesn't always mean "exactly one handler must handle the request."

## 21. Chain Creation

A chain can be constructed like this:

```java
handler1.setNext(handler2);
handler2.setNext(handler3);
handler3.setNext(handler4);
```

Result:

```
H1
 ↓
H2
 ↓
H3
 ↓
H4
```

Then the client only needs:

```java
handler1.handle(request);
```

The client doesn't need to know every handler's internal logic.

## 22. Better Java Implementation

A common approach is to create a base handler.

```java
abstract class BaseHandler {

    protected BaseHandler next;

    public void setNext(BaseHandler next) {

        this.next = next;
    }

    public abstract void handle(
            String request);
}
```

Then:

```java
class HandlerA extends BaseHandler {

    @Override
    public void handle(String request) {

        if (request.equals("A")) {

            System.out.println(
                    "Handler A handled request"
            );

        } else if (next != null) {

            next.handle(request);
        }
    }
}
```

Another:

```java
class HandlerB extends BaseHandler {

    @Override
    public void handle(String request) {

        if (request.equals("B")) {

            System.out.println(
                    "Handler B handled request"
            );

        } else if (next != null) {

            next.handle(request);
        }
    }
}
```

This reduces repeated next handling code.

## 23. Advantages

### 1. Reduces coupling

The sender doesn't need to know which handler will process the request.

```
Client
  ↓
First Handler
```

That's enough.

### 2. Easy to add handlers

Suppose you want:

`FraudDetectionHandler`

You can add it to the chain.

```
Authentication
     ↓
Authorization
     ↓
Fraud Detection
     ↓
Validation
```

The existing handlers don't necessarily need modification.

### 3. Flexible ordering

You can change:

```
A → B → C
```

to:

```
A → C → B
```

depending on requirements.

### 4. Single Responsibility

Each handler focuses on one responsibility.

For example:

```
AuthenticationHandler
→ Authentication

ValidationHandler
→ Validation

LoggingHandler
→ Logging
```

## 24. Disadvantages

### 1. Request may not be handled

If nobody can handle the request:

```
A → B → C → END
```

you need a strategy for unhandled requests.

### 2. Debugging can be harder

You may need to follow:

```
A → B → C → D → E
```

to understand where the request went.

### 3. Long chains can affect performance

If there are many handlers:

```
H1 → H2 → H3 → ... → H100
```

the request may travel through many objects.

### 4. Chain configuration matters

Incorrect ordering can produce unexpected behavior.

For example:

`Authentication`

should usually happen before:

`Authorization`

## 25. Chain of Responsibility vs State

Since you've just studied State Pattern, this distinction is important.

### State

One object has a current state:

```
Order
 ↓
PaidState
```

Its behavior changes according to its state.

```
State → Behavior
```

### Chain of Responsibility

A request moves between handlers:

```
Request
 ↓
Handler A
 ↓
Handler B
 ↓
Handler C
```

```
Request → Handler
```

Remember:

```
State = "What should I do in my current state?"

Chain = "Who should handle this request?"
```

## 26. Chain of Responsibility vs Strategy

### Strategy

Select one algorithm.

```
Payment
 ↓
Strategy
 ├── UPI
 ├── Card
 └── PayPal
```

```
Strategy → Choose
```

### Chain of Responsibility

Try handlers sequentially.

```
Request
 ↓
Handler A
 ↓
Handler B
 ↓
Handler C
```

```
Chain → Pass
```

Easy memory:

```
Strategy → Choose an algorithm

Chain    → Pass the request
```

## 27. Chain of Responsibility vs Command

### Command

Encapsulates a request.

```
Command
 ↓
execute()
```

### Chain

Passes a request through multiple handlers.

```
Request
 ↓
A → B → C
```

Remember:

```
Command → Encapsulate

Chain   → Forward
```

## 28. Chain of Responsibility vs Decorator

These can look structurally similar because both can have chained objects.

### Decorator

Every layer generally adds behavior:

```
Object
 ↓
Decorator A
 ↓
Decorator B
 ↓
Decorator C
```

Purpose:

Add responsibilities.

### Chain of Responsibility

Each handler decides whether to handle/pass the request:

```
Request
 ↓
Handler A
 ↓
Handler B
 ↓
Handler C
```

Purpose:

Find a handler / process through handlers.

Remember:

```
Decorator → Add behavior

Chain     → Find/Pass handling
```

## 29. Real-Time Architecture Example

Consider an online banking API:

```
             HTTP Request
                   |
                   ↓
          Authentication
                   |
                   ↓
           Authorization
                   |
                   ↓
           Input Validation
                   |
                   ↓
          Fraud Detection
                   |
                   ↓
          Rate Limiting
                   |
                   ↓
          Business Service
```

Each handler has one responsibility.

If authentication fails:

```
Request
   ↓
Authentication
   ↓
REJECT
```

If authentication succeeds:

```
Request
   ↓
Authentication ✓
   ↓
Authorization ✓
   ↓
Validation ✓
   ↓
Fraud Detection ✓
   ↓
Business Service
```

This is a very practical way to think about Chain of Responsibility in enterprise applications.

## 30. When Should You Use It?

Use Chain of Responsibility when:

- ✅ **Multiple objects could handle a request**

```
Level 1
Level 2
Manager
```

- ✅ **The sender should not know the exact handler**

```
Client → First Handler
```

- ✅ **You want to dynamically change the chain**

```
A → B → C
```

can become:

```
A → D → B → C
```

- ✅ **You have multiple processing steps**

```
Logging
 ↓
Authentication
 ↓
Validation
 ↓
Processing
```

- ✅ **You have large conditional logic**

Instead of:

```
if (...)
else if (...)
else if (...)
else if (...)
```

use separate handlers.

## 31. When Should You NOT Use It?

Don't use it if:

- There is only one possible handler.
- The chain has only one or two simple conditions.
- The request must always be handled by a specific known object.
- The chain would become unnecessarily complicated.

For a simple:

```java
if (amount < 1000)
```

you don't need Chain of Responsibility.

## 32. Interview Answer

If the interviewer asks:

**What is Chain of Responsibility Design Pattern?**

You can answer:

Chain of Responsibility is a behavioral design pattern in which a request is passed through a chain of handlers. Each handler decides whether it can handle the request; if it cannot, it forwards the request to the next handler. This decouples the sender of a request from the object that ultimately handles it.

**Real-time example:**

In an expense approval system, an expense request can pass through Team Lead, Manager, Director, and VP. Each person has an approval limit. If the Team Lead cannot approve the amount, the request moves to the Manager, then Director, and so on.

## 33. Easy Diagram to Remember

```
                       REQUEST
                          |
                          ↓
                 +----------------+
                 |   Handler 1    |
                 +----------------+
                    |          |
              Handles       Can't Handle
                 |              |
                 ↓              ↓
               DONE       +----------------+
                          |   Handler 2    |
                          +----------------+
                             |         |
                        Handles    Can't Handle
                           |           |
                           ↓           ↓
                         DONE    +----------------+
                                 |   Handler 3    |
                                 +----------------+
                                      |
                                      ↓
                                    DONE
```

**One-line memory trick:**

> 🔗 Chain of Responsibility = Pass the request along the chain until the appropriate handler handles it.

And for the behavioral patterns you've been covering:

| Pattern | Main Idea |
|---|---|
| Strategy | Choose an algorithm |
| Command | Encapsulate a request |
| Observer | Notify subscribers |
| State | Behavior changes with state |
| Chain of Responsibility | Pass request through handlers |
| Template Method | Define algorithm skeleton |
| Mediator | Centralize communication |
| Iterator | Traverse a collection |

### The easiest distinction to remember:

```
Strategy → CHOOSE
Command  → ENCAPSULATE
Observer → NOTIFY
State    → CHANGE BEHAVIOR
Chain    → PASS REQUEST
```
