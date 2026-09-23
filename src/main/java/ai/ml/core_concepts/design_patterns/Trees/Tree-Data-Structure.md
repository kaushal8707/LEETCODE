# 🌳 Tree Data Structure

A Tree is a non-linear data structure used to represent data in a hierarchical relationship.

Unlike arrays, linked lists, stacks, and queues, which are generally linear, a tree stores data in a structure where one element can be connected to multiple other elements.

### Real-life examples

- 📁 File system → Folder → Subfolder → Files
- 🏢 Organization → CEO → Managers → Employees
- 🌐 HTML DOM → HTML → Body → Div → Elements
- 🏦 Bank → Bank → Branches → Accounts
- 💻 Database indexes
- 🧬 Family hierarchy

## 1. Basic Structure of a Tree

Consider this tree:

```
                10
              /    \
             20     30
            /  \      \
           40   50     60
```

Here:

- 10 is the root
- 20 and 30 are children of 10
- 40 and 50 are children of 20
- 60 is a child of 30

## 2. Important Tree Terminology

### Node

Each element in a tree is called a Node.

```
        10
       /  \
      20   30
```

10, 20, and 30 are nodes.

### Root

The topmost node of a tree is called the Root.

```
        10       ← Root
       /  \
      20   30
```

A tree has exactly one root.

### Parent

A node that has one or more children is called a Parent.

```
        10
       /  \
      20   30
```

10 is the parent of 20 and 30.

### Child

A node directly connected below another node is called its Child.

```
        10
       /  \
      20   30
```

20 and 30 are children of 10.

### Siblings

Nodes having the same parent are called Siblings.

```
        10
       /  \
      20   30
```

20 and 30 are siblings.

### Leaf Node

A node that has no children is called a Leaf Node.

```
        10
       /  \
      20   30
     / \
    40  50
```

Leaf nodes:

40, 50, 30

### Edge

The connection between two nodes is called an Edge.

```
        10
       /
      20
```

The connection between 10 and 20 is an edge.

If a tree has N nodes, it always has:

N - 1 edges

For example:

```
5 nodes
↓
4 edges
```

## 3. Depth

The depth of a node represents how far that node is from the root.

For example:

```
             10        depth = 0
            /  \
           20   30     depth = 1
          /  \
         40   50       depth = 2
```

Therefore:

```
Depth(10) = 0
Depth(20) = 1
Depth(40) = 2
```

## 4. Height

The height of a tree is the number of edges on the longest path from the root to a leaf.

```
             10
            /
           20
          /
         30
        /
       40
```

Longest path:

10 → 20 → 30 → 40

There are 3 edges.

Therefore:

Height = 3

Some implementations define height in terms of nodes rather than edges. Always check the convention being used.

## 5. Types of Trees

There are several important types of trees.

### 1. General Tree

A node can have any number of children.

```
             A
        /    |    \
       B     C     D
            / \
           E   F
```

### 2. Binary Tree

Each node can have at most two children.

```
          10
         /  \
        20   30
       / \
      40  50
```

A node can have:

- 0 children
- 1 child
- 2 children

but never more than 2.

### 3. Binary Search Tree (BST)

A Binary Search Tree follows this rule:

Left subtree < Root < Right subtree

Example:

```
          50
         /  \
       30    70
      / \    / \
    20  40  60  80
```

For node 50:

```
Left  → values smaller than 50
Right → values greater than 50
```

BST is useful for efficient searching.

### 4. Full Binary Tree

Every node has either:

- 0 children, or
- exactly 2 children

Example:

```
          10
         /  \
        20   30
       / \
      40  50
```

### 5. Complete Binary Tree

All levels are completely filled except possibly the last level.

The last level is filled from left to right.

```
          10
        /    \
       20     30
      / \    /
     40 50  60
```

Complete binary trees are commonly used in heaps.

### 6. Perfect Binary Tree

Every internal node has exactly two children, and all leaf nodes are at the same level.

```
             10
           /    \
          20     30
         / \    / \
        40 50  60 70
```

### 7. Balanced Binary Tree

The height of the left and right subtrees remains reasonably balanced.

Example:

```
          50
        /    \
       30     70
      / \     / \
     20 40   60 80
```

Examples include:

- AVL Tree
- Red-Black Tree

## 6. Binary Tree in Java

A simple binary tree node can be represented using a class:

```java
class Node {
    int data;
    Node left;
    Node right;

    Node(int data) {
        this.data = data;
        this.left = null;
        this.right = null;
    }
}
```

We can create a tree:

```java
public class BinaryTree {

    static class Node {
        int data;
        Node left;
        Node right;

        Node(int data) {
            this.data = data;
        }
    }

    public static void main(String[] args) {

        Node root = new Node(10);

        root.left = new Node(20);
        root.right = new Node(30);

        root.left.left = new Node(40);
        root.left.right = new Node(50);
    }
}
```

This creates:

```
             10
            /  \
           20   30
          / \
         40  50
```

## 7. Tree Traversal

Traversal means visiting every node of a tree.

The most important tree traversals are:

- Preorder
- Inorder
- Postorder
- Level Order

Consider:

```
             10
            /  \
           20   30
          / \
         40  50
```

### 7.1 Preorder Traversal

Order:

Root → Left → Right

For our tree:

```
10 → 20 → 40 → 50 → 30
```

### Java

```java
static void preorder(Node root) {

    if (root == null) {
        return;
    }

    System.out.print(root.data + " ");

    preorder(root.left);
    preorder(root.right);
}
```

Output:

```
10 20 40 50 30
```

### Real-world use

Preorder can be useful when you need to process a parent before its children, such as copying/serializing a tree.

## 8. Inorder Traversal

Order:

Left → Root → Right

For our tree:

```
40 → 20 → 50 → 10 → 30
```

### Java

```java
static void inorder(Node root) {

    if (root == null) {
        return;
    }

    inorder(root.left);

    System.out.print(root.data + " ");

    inorder(root.right);
}
```

Output:

```
40 20 50 10 30
```

### Important

For a Binary Search Tree, inorder traversal produces values in sorted order.

Example:

```
          50
         /  \
        30   70
       / \   / \
      20 40 60 80
```

Inorder:

```
20 30 40 50 60 70 80
```

## 9. Postorder Traversal

Order:

Left → Right → Root

For our tree:

```
40 → 50 → 20 → 30 → 10
```

### Java

```java
static void postorder(Node root) {

    if (root == null) {
        return;
    }

    postorder(root.left);
    postorder(root.right);

    System.out.print(root.data + " ");
}
```

Output:

```
40 50 20 30 10
```

### Real-world use

Postorder is useful when children must be processed before their parent.

For example, deleting a tree:

```
Delete children first
        ↓
Delete parent
```

## 10. Level Order Traversal

Level Order visits nodes level by level.

```
             10       ← Level 0
            /  \
           20   30    ← Level 1
          / \
         40  50       ← Level 2
```

Output:

```
10 20 30 40 50
```

It generally uses a Queue.

### Java

```java
static void levelOrder(Node root) {

    if (root == null) {
        return;
    }

    Queue<Node> queue = new LinkedList<>();

    queue.offer(root);

    while (!queue.isEmpty()) {

        Node current = queue.poll();

        System.out.print(current.data + " ");

        if (current.left != null) {
            queue.offer(current.left);
        }

        if (current.right != null) {
            queue.offer(current.right);
        }
    }
}
```

## 11. Traversal Summary

For:

```
             10
            /  \
           20   30
          / \
         40  50
```

| Traversal | Rule | Output |
|---|---|---|
| Preorder | Root → Left → Right | 10 20 40 50 30 |
| Inorder | Left → Root → Right | 40 20 50 10 30 |
| Postorder | Left → Right → Root | 40 50 20 30 10 |
| Level Order | Level by level | 10 20 30 40 50 |

### Easy way to remember

```
PREORDER
PRE = Root comes first

INORDER
IN = Root comes in the middle

POSTORDER
POST = Root comes last
```

## 12. Tree Time Complexity

For a tree with N nodes:

### Traversal

Every node is visited once.

```
Time Complexity = O(N)
```

Examples:

```
Preorder  → O(N)
Inorder   → O(N)
Postorder → O(N)
LevelOrder → O(N)
```

### Space Complexity

For recursive DFS traversal:

O(H)

where H is the height of the tree.

For level-order traversal:

O(W)

where W is the maximum width of the tree.

## 13. Binary Search Tree Complexity

For a balanced BST:

```
Search  → O(log N)
Insert  → O(log N)
Delete  → O(log N)
```

But in the worst case, a BST can become skewed:

```
10
  \
   20
     \
      30
        \
         40
           \
            50
```

Now it behaves almost like a linked list.

Therefore:

```
Worst-case Search → O(N)
Worst-case Insert → O(N)
Worst-case Delete → O(N)
```

This is why self-balancing trees such as AVL and Red-Black Trees are important.

## 14. Tree vs Graph

A tree is actually a special type of graph.

| Tree | Graph |
|---|---|
| Hierarchical structure | General network structure |
| Has a root concept | Doesn't necessarily have a root |
| No cycles | Can contain cycles |
| With N nodes → N-1 edges | Number of edges can vary |
| Usually has parent-child relationship | General connections |
| Example: File system | Example: Social network |

## 15. Where Trees Are Used

Trees are extremely important in software engineering.

### File Systems

```
Computer
│
├── Documents
│   ├── Resume.pdf
│   └── Notes.txt
│
├── Pictures
│   ├── Photo1.jpg
│   └── Photo2.jpg
│
└── Videos
```

### Database Indexing

Databases use tree-based structures such as:

- B-Tree
- B+ Tree

for efficient searching and indexing.

### Compilers

Programming language syntax can be represented using:

Abstract Syntax Tree (AST)

For example:

a + b * c

can be represented hierarchically so that multiplication has higher precedence than addition.

### Web Pages

HTML is naturally hierarchical:

```
HTML
│
├── HEAD
│
└── BODY
    │
    ├── DIV
    │   ├── H1
    │   └── P
    │
    └── FOOTER
```

### Artificial Intelligence

Decision trees can represent decisions:

```
             Is income > 50K?
                /       \
              Yes        No
              /           \
       Approve Loan    Reject Loan
```

## 16. Most Important Tree Concepts for Interviews

If you're preparing for Java interviews / DSA, learn trees in this order:

```
1. Tree Terminology
       ↓
2. Binary Tree
       ↓
3. Tree Traversals
       ↓
4. Recursive Tree Problems
       ↓
5. Binary Search Tree
       ↓
6. BST Search / Insert / Delete
       ↓
7. Height / Depth
       ↓
8. Balanced Trees
       ↓
9. AVL Tree
       ↓
10. Red-Black Tree
       ↓
11. Heap
       ↓
12. Trie
       ↓
13. B-Tree / B+ Tree
```

The most important starting point is to become very comfortable with Binary Tree + recursion + Preorder/Inorder/Postorder/Level Order, because a large number of tree-based DSA problems are built on these concepts
