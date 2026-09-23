# LRU — Least Recently Used Cache

**LRU (Least Recently Used)** is a cache eviction strategy that removes the item that has not been accessed for the longest time when the cache is full.

The basic idea is:

> **Keep recently used data, remove data that hasn't been used recently.**

LRU is one of the most common cache eviction strategies and is especially useful when applications have **temporal locality**.

---

## 1. Simple Example

Suppose our cache can hold only 3 items:

```
Cache capacity = 3
```

We insert:

```
A
B
C
```

Cache:

```
[A, B, C]
```

Now the cache is full.

We access:

```
A
```

So A becomes recently used:

```
[B, C, A]
```

Then we access:

```
B
```

Now:

```
[C, A, B]
```

Now we insert:

```
D
```

Cache is full, so we need to evict something.

**Which one?**

```
C
```

Because C was accessed least recently.

Final cache:

```
[A, B, D]
```

---

## 2. How LRU Thinks

You can visualize an LRU cache as:

```
MOST RECENT                         LEAST RECENT
     |                                    |
     v                                    v

   [ A ] → [ B ] → [ C ] → [ D ]
    MRU                         LRU
```

Where:

- **MRU** = Most Recently Used
- **LRU** = Least Recently Used

When an item is accessed, move it to the front:

**Before:**

```
[A] [B] [C] [D]
 ^             ^
MRU           LRU
```

**Access C:**

```
[C] [A] [B] [D]
 ^             ^
MRU           LRU
```

**Now access D:**

```
[D] [C] [A] [B]
 ^             ^
MRU           LRU
```

If we need to remove something:

```
Evict B
```

---

## 3. Real-World Example

Imagine an e-commerce website.

Users frequently view products:

```
iPhone
MacBook
AirPods
Samsung TV
Shoes
```

Suppose the cache can store only 3 products.

Recent access:

```
iPhone
MacBook
AirPods
```

Cache:

```
[iPhone, MacBook, AirPods]
```

User accesses:

```
iPhone
```

Now:

```
[MacBook, AirPods, iPhone]
```

Then user accesses:

```
MacBook
```

Now:

```
[AirPods, iPhone, MacBook]
```

New product:

```
Samsung TV
```

Cache is full.

LRU removes:

```
AirPods
```

because it was used least recently.

Result:

```
[iPhone, MacBook, Samsung TV]
```

---

## 4. Why LRU Works

LRU relies on **temporal locality**.

Temporal locality means:

> **If data was accessed recently, there is a good chance it will be accessed again soon.**

For example:

```
User opens product
       ↓
Product details
       ↓
Reviews
       ↓
Add to cart
       ↓
Back to product
```

The product data is accessed repeatedly within a short period.

LRU tries to keep such recently used data in memory.

---

## 5. LRU Cache Operations

A typical LRU cache needs two operations:

### get(key)

Retrieve an item.

```
get(A)
```

If A exists:

```
Return A
Move A → MRU position
```

### put(key, value)

Insert/update an item.

If cache has space:

```
Insert item
```

If cache is full:

```
Remove LRU item
Insert new item
```

Ideally both operations should be:

```
get() → O(1)
put() → O(1)
```

This is a very common interview question.

---

## 6. How Do We Implement LRU in O(1)?

The classic solution uses:

> **HashMap + Doubly Linked List**

This is extremely important for interviews.

### HashMap

The HashMap provides:

```
key → Node
```

So we can find an item in:

```
O(1)
```

Example:

```
HashMap

A → Node(A)
B → Node(B)
C → Node(C)
```

### Doubly Linked List

The linked list maintains usage order:

```
HEAD                              TAIL
 ↓                                  ↓
[A] ⇄ [B] ⇄ [C] ⇄ [D]
 ↑                                  ↑
MRU                                LRU
```

**Why doubly linked?**

Because we need to remove a node from the middle in:

```
O(1)
```

With a doubly linked list, we have:

```
previous ← Node → next
```

So we can unlink it directly.

---

## 7. Internal Architecture

```
                  HashMap
              ┌──────────────┐
              │ A → Node A   │
              │ B → Node B   │
              │ C → Node C   │
              └──────┬───────┘
                     │
                     │ references
                     ↓

HEAD                                      TAIL
 ↓                                          ↓
[A] ⇄ [B] ⇄ [C] ⇄ [D]
 ↑                                          ↑
MRU                                        LRU
```

The two structures have different responsibilities:

```
HashMap
   ↓
Fast lookup

Doubly Linked List
   ↓
Fast ordering + removal
```

Together:

- O(1) lookup
- O(1) insertion
- O(1) deletion
- O(1) move-to-front

---

## 8. Example Step-by-Step

Capacity:

```
3
```

### Step 1

```
put(A)
[A]
```

### Step 2

```
put(B)
[B] → [A]
```

B is most recently used.

### Step 3

```
put(C)
[C] → [B] → [A]
```

A is LRU.

### Step 4

```
get(A)
```

Move A to the front:

```
[A] → [C] → [B]
```

Now B is LRU.

### Step 5

```
put(D)
```

Cache is full.

Evict B:

```
[D] → [A] → [C]
```

---

## 9. Why Not Just Use a Queue?

A common interview question is:

> *"Why can't we simply use a queue?"*

Because when an existing item is accessed, we need to move it to the MRU position.

Suppose:

```
[A] → [B] → [C]
```

Access A:

```
[B] → [C] → [A]
```

With a normal array/list, finding and moving A may take:

```
O(n)
```

With a doubly linked list + HashMap:

```
Find A       → O(1)
Remove A     → O(1)
Move A front → O(1)
```

Therefore:

```
Total → O(1)
```

---

## 10. LRU Algorithm

Conceptually:

```
get(key):

    if key doesn't exist:
        return MISS

    node = map[key]

    remove(node)

    addToFront(node)

    return node.value
```

For put:

```
put(key, value):

    if key already exists:
        update value
        move node to front
        return

    if cache is full:
        remove tail
        remove tail.key from map

    create new node
    add node to front
    map[key] = node
```

---

## 11. Java Implementation

A clean interview implementation looks like this:

```java
import java.util.HashMap;
import java.util.Map;

class LRUCache {

    private static class Node {
        int key;
        int value;
        Node prev;
        Node next;

        Node(int key, int value) {
            this.key = key;
            this.value = value;
        }
    }

    private final int capacity;
    private final Map<Integer, Node> map;

    private final Node head;
    private final Node tail;

    public LRUCache(int capacity) {
        this.capacity = capacity;
        this.map = new HashMap<>();

        head = new Node(0, 0);
        tail = new Node(0, 0);

        head.next = tail;
        tail.prev = head;
    }

    public int get(int key) {

        if (!map.containsKey(key)) {
            return -1;
        }

        Node node = map.get(key);

        remove(node);
        addToFront(node);

        return node.value;
    }

    public void put(int key, int value) {

        if (map.containsKey(key)) {

            Node node = map.get(key);

            node.value = value;

            remove(node);
            addToFront(node);

            return;
        }

        if (map.size() == capacity) {

            Node lru = tail.prev;

            remove(lru);
            map.remove(lru.key);
        }

        Node newNode = new Node(key, value);

        map.put(key, newNode);

        addToFront(newNode);
    }

    private void remove(Node node) {

        node.prev.next = node.next;
        node.next.prev = node.prev;
    }

    private void addToFront(Node node) {

        node.next = head.next;
        node.prev = head;

        head.next.prev = node;
        head.next = node;
    }
}
```

---

## 12. Why Dummy Head and Tail?

The implementation uses:

```
HEAD
TAIL
```

as dummy/sentinel nodes.

Instead of:

```
head → A → B → C → tail
```

we can always perform:

```
head.next
tail.prev
```

without constantly checking:

- Is this the first node?
- Is this the last node?
- Is the list empty?

It significantly simplifies the implementation.

---

## 13. Complexity

For the HashMap + Doubly Linked List implementation:

| Operation | Complexity |
|---|---|
| `get()` | O(1) average |
| `put()` | O(1) average |
| Delete | O(1) |
| Move to MRU | O(1) |
| Space | O(capacity) |

This is the classic answer expected in coding interviews.

---

## 14. LRU vs LFU

This is another common system-design interview question.

|  | LRU | LFU |
|---|---|---|
| Full form | Least Recently Used | Least Frequently Used |
| Decision | Last access time | Access frequency |
| Evicts | Least recently used | Least frequently used |
| Reacts to recent traffic | Excellent | Less direct |
| Tracks frequency | No | Yes |
| Complexity | Usually simpler | More complex |
| Good for | Temporal locality | Persistent hot items |

**Mental model:**

```
LRU
"What haven't I used recently?"

LFU
"What do I use the least?"
```

---

## 15. LRU + TTL

In production, you often combine LRU with TTL.

For example:

```
Cache
 ├── product:101 → TTL 10 min
 ├── product:102 → TTL 5 min
 ├── product:103 → TTL 20 min
 └── product:104 → TTL 30 min
```

Two independent things can happen.

### TTL expires

```
product:101
     ↓
TTL expires
     ↓
Entry becomes expired
```

### Cache becomes full

```
Memory full
    ↓
LRU policy
    ↓
Evict least recently used entry
```

So:

```
TTL     → lifetime
LRU     → capacity management
```

---

## 16. LRU in Distributed Systems

In a distributed application:

```
              Load Balancer
               /    |    \
              /     |     \
             v      v      v
          App-1   App-2   App-3
             |      |       |
             v      v       v
          Local   Local   Local
          Cache   Cache   Cache
```

Each application instance could have its own LRU cache.

But this creates a problem:

```
App-1 cache ≠ App-2 cache ≠ App-3 cache
```

You may instead use a distributed cache:

```
             App Servers
           /      |      \
          /       |       \
         v        v        v
              Redis
                |
             LRU/LFU
```

Now cache state can be shared.

---

## 17. Important Problem: Cache Thrashing

Imagine the cache capacity is:

```
3
```

But your application repeatedly accesses:

```
A B C D A B C D A B C D
```

Cache:

```
A B C
↓
D comes → A evicted
A comes → B evicted
B comes → C evicted
C comes → D evicted
```

Almost every request becomes a miss.

This is called **cache thrashing**.

You might see:

```
Cache hit ratio ↓↓↓
Database requests ↑↑↑
Latency ↑↑↑
```

Increasing cache capacity or changing the caching strategy may help.

---

## 18. LRU and Hot Keys

LRU is generally good when recently accessed data tends to remain useful.

But consider:

```
Product A → accessed every second
Product B → accessed every second
Product C → accessed every second
```

These are **hot keys**.

LRU keeps them because they are constantly accessed.

That's good.

But if the workload suddenly changes:

```
Old hot keys
      ↓
New hot keys
```

LRU adapts relatively quickly because it considers recent access.

---

## 19. Interview Questions

### Q1. What is LRU?

> LRU is a cache eviction policy that removes the item that has not been accessed for the longest time.

### Q2. How do you implement LRU in O(1)?

> Use a HashMap for O(1) key lookup and a doubly linked list to maintain access order and perform O(1) insertion, deletion, and movement.

### Q3. Why doubly linked list?

> Because we need to remove and reposition an arbitrary node in O(1).

### Q4. Why HashMap?

> To locate the linked-list node directly using its key in O(1) average time.

### Q5. What happens on cache hit?

```
Find node
   ↓
Return value
   ↓
Move node to MRU
```

### Q6. What happens on cache miss?

```
Cache MISS
   ↓
Fetch from DB
   ↓
Insert into cache
   ↓
If full → evict LRU
```

### Q7. LRU vs LFU?

> LRU considers **recency**, while LFU considers **frequency**.

---

## 20. The Most Important Mental Model

Remember this picture:

```
                  LRU CACHE

       MOST RECENT                LEAST RECENT
            |                          |
            v                          v

          HEAD                       TAIL
            |                          |
            v                          v
          [D] ⇄ [A] ⇄ [C] ⇄ [B]
                               ↑
                              EVICT
```

When an item is accessed:

```
Any node
   ↓
Move to HEAD
```

When cache is full:

```
TAIL
 ↓
Remove
```

And the data structures are:

```
HashMap
   +
Doubly Linked List
   =
O(1) LRU Cache
```

---

### One-line interview answer:

> **"LRU is an eviction policy that removes the least recently accessed item. The standard O(1) implementation combines a HashMap for direct lookup with a doubly linked list for maintaining recency order."**

