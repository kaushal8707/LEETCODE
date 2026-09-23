# 🌳 Binary Search Tree (BST)

A Binary Search Tree (BST) is a special type of Binary Tree where every node follows a specific ordering rule:

All values in the left subtree are smaller than the node, and all values in the right subtree are greater than the node.

For example:

```
             50
           /    \
         30      70
        /  \    /  \
      20   40  60   80
```

For node 50:

```
Left side  → values < 50
Right side → values > 50
```

For node 30:

```
Left side  → values < 30
Right side → values > 30
```

## 1. BST Properties

A Binary Search Tree generally follows these rules:

### Rule 1 — At most two children

Every node can have:

- 0 children
- 1 child
- 2 children

### Rule 2 — Left subtree contains smaller values

```
        50
       /
      30
```

30 < 50 ✅

### Rule 3 — Right subtree contains greater values

```
        50
          \
           70
```

70 > 50 ✅

### Rule 4 — The same rule applies recursively

Consider:

```
             50
           /    \
         30      70
        /  \    /  \
      20   40  60   80
```

The entire subtree rooted at 30 must also satisfy the BST rule.

```
        30
       /  \
     20    40
```

And the subtree rooted at 70:

```
        70
       /  \
      60   80
```

## 2. Binary Tree vs Binary Search Tree

This is a very important interview question.

### Binary Tree

A Binary Tree only says:

A node can have at most two children.

There is no ordering requirement.

```
        50
       /  \
      70   20
```

This is a valid Binary Tree.

But it is not a BST because:

70 > 50

yet 70 is on the left.

### Binary Search Tree

A BST additionally requires:

Left < Root < Right

Example:

```
        50
       /  \
      30   70
```

This is a valid BST.

## 3. BST Node in Java

We can create a Node class:

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

Each node contains:

- data
- left
- right

For example:

```
        50
       /  \
      30   70
```

Internally:

```
Node
 ├── data = 50
 ├── left → Node(30)
 └── right → Node(70)
```

## 4. Creating a BST

Suppose we want to insert:

50, 30, 70, 20, 40, 60, 80

Start with:

```
50
```

Insert 30:

30 < 50

```
        50
       /
      30
```

Insert 70:

70 > 50

```
        50
       /  \
      30   70
```

Insert 20:

20 < 50
20 < 30

```
        50
       /  \
      30   70
     /
    20
```

Insert 40:

40 < 50
40 > 30

```
        50
       /  \
      30   70
     / \
    20 40
```

Eventually:

```
             50
           /    \
         30      70
        /  \    /  \
      20   40  60   80
```

## 5. Searching in a BST

One of the biggest advantages of a BST is efficient searching.

Suppose we want to find:

60

in:

```
             50
           /    \
         30      70
        /  \    /  \
      20   40  60   80
```

### Step 1

Start at 50.

60 > 50

So we don't need to search the left subtree.

Move right:

```
             50
               \
                70
```

### Step 2

Now:

60 < 70

Move left:

```
             70
            /
           60
```

### Step 3

Found 60.

So instead of checking every node, we eliminated large portions of the tree at every step.

## 6. Search — Recursive Java Implementation

```java
public static Node search(Node root, int key) {

    if (root == null || root.data == key) {
        return root;
    }

    if (key < root.data) {
        return search(root.left, key);
    }

    return search(root.right, key);
}
```

Usage:

```java
Node result = search(root, 60);

if (result != null) {
    System.out.println("Element found");
} else {
    System.out.println("Element not found");
}
```

## 7. How Search Works

This part is extremely important:

```java
if (key < root.data) {
    return search(root.left, key);
}
```

If the key is smaller than the current node:

Search LEFT

Otherwise:

```java
return search(root.right, key);
```

we search the right subtree.

The basic logic is:

```
             Current
                |
       ┌────────┴────────┐
       ↓                 ↓
   key < current     key > current
       ↓                 ↓
     LEFT              RIGHT
```

## 8. Search — Iterative Approach

We can also search without recursion.

```java
public static Node search(Node root, int key) {

    while (root != null) {

        if (root.data == key) {
            return root;
        }

        if (key < root.data) {
            root = root.left;
        } else {
            root = root.right;
        }
    }

    return null;
}
```

This approach doesn't use the recursive call stack.

## 9. Insertion in BST

Suppose we have:

```
             50
           /    \
         30      70
        /  \
      20   40
```

We want to insert:

35

Compare:

35 < 50

Go left.

35 > 30

Go right.

35 < 40

Go left.

Therefore:

```
             50
           /    \
         30      70
        /  \
      20   40
           /
          35
```

## 10. BST Insert — Java

```java
public static Node insert(Node root, int key) {

    if (root == null) {
        return new Node(key);
    }

    if (key < root.data) {
        root.left = insert(root.left, key);
    } else if (key > root.data) {
        root.right = insert(root.right, key);
    }

    return root;
}
```

Notice:

```java
root.left = insert(root.left, key);
```

and

```java
root.right = insert(root.right, key);
```

We assign the returned node back to left or right because a new node may need to be attached there.

## 11. Complete Insert Example

```java
public class BinarySearchTree {

    static class Node {

        int data;
        Node left;
        Node right;

        Node(int data) {
            this.data = data;
        }
    }

    static Node insert(Node root, int key) {

        if (root == null) {
            return new Node(key);
        }

        if (key < root.data) {
            root.left = insert(root.left, key);
        } else if (key > root.data) {
            root.right = insert(root.right, key);
        }

        return root;
    }

    public static void main(String[] args) {

        Node root = null;

        root = insert(root, 50);
        root = insert(root, 30);
        root = insert(root, 70);
        root = insert(root, 20);
        root = insert(root, 40);
        root = insert(root, 60);
        root = insert(root, 80);
    }
}
```

The resulting tree:

```
             50
           /    \
         30      70
        /  \    /  \
      20   40  60   80
```

## 12. Inorder Traversal of BST

One of the most important properties of a BST:

**Inorder traversal of a BST produces elements in sorted order.**

Consider:

```
             50
           /    \
         30      70
        /  \    /  \
      20   40  60   80
```

Inorder:

Left → Root → Right

Result:

```
20 30 40 50 60 70 80
```

Java:

```java
public static void inorder(Node root) {

    if (root == null) {
        return;
    }

    inorder(root.left);

    System.out.print(root.data + " ");

    inorder(root.right);
}
```

This property is frequently asked in interviews.

## 13. Finding Minimum Value

In a BST:

**The minimum value is always the leftmost node.**

Example:

```
             50
           /    \
         30      70
        /  \
      20   40
```

Keep moving left:

```
50
↓
30
↓
20
```

Therefore:

Minimum = 20

### Java

```java
public static Node findMin(Node root) {

    while (root.left != null) {
        root = root.left;
    }

    return root;
}
```

## 14. Finding Maximum Value

Similarly:

**The maximum value is always the rightmost node.**

```
             50
           /    \
         30      70
                /  \
               60   80
                    ↑
                 Maximum
```

Therefore:

Maximum = 80

### Java

```java
public static Node findMax(Node root) {

    while (root.right != null) {
        root = root.right;
    }

    return root;
}
```

## 15. Deletion in BST

Deletion is the most important and slightly tricky BST operation.

There are three cases.

### Case 1: Delete a Leaf Node

Consider:

```
             50
           /    \
         30      70
        /  \
      20   40
```

Delete:

20

20 has no children.

Simply remove it:

```
             50
           /    \
         30      70
           \
            40
```

## 16. Case 2: Delete Node With One Child

Consider:

```
        50
       /
      30
     /
    20
```

Suppose we delete 30.

30 has one child:

20

We connect the parent directly to the child:

```
        50
       /
      20
```

## 17. Case 3: Delete Node With Two Children

This is the most important case.

Consider:

```
             50
           /    \
         30      70
        /  \    /  \
      20   40  60   80
```

Suppose we want to delete:

50

50 has two children.

We cannot simply remove it.

A common approach is to replace it with its inorder successor.

The inorder successor is:

The smallest value in the right subtree.

Right subtree:

```
        70
       /  \
      60   80
```

Smallest value:

60

Replace 50 with 60:

```
             60
           /    \
         30      70
        /  \      \
      20   40      80
```

Now delete the original 60.

## 18. BST Delete — Java

```java
public static Node delete(Node root, int key) {

    if (root == null) {
        return null;
    }

    if (key < root.data) {

        root.left = delete(root.left, key);

    } else if (key > root.data) {

        root.right = delete(root.right, key);

    } else {

        // Case 1 and Case 2
        if (root.left == null) {
            return root.right;
        }

        if (root.right == null) {
            return root.left;
        }

        // Case 3: Two children
        Node successor = findMin(root.right);

        root.data = successor.data;

        root.right = delete(root.right, successor.data);
    }

    return root;
}
```

This single method handles all three cases.

## 19. Why Inorder Successor?

Suppose:

```
             50
           /    \
         30      70
        /  \    /  \
      20   40  60   80
```

If we replace 50 with 60:

```
             60
           /    \
         30      70
        /  \      \
      20   40      80
```

The BST property remains valid:

```
Everything left of 60 < 60
Everything right of 60 > 60
```

That's why the inorder successor is useful.

You can also use the inorder predecessor, which is the largest value in the left subtree.

## 20. BST Complexity

For a balanced BST:

| Operation | Average/Balanced |
|---|---|
| Search | O(log N) |
| Insert | O(log N) |
| Delete | O(log N) |
| Find Min | O(log N) |
| Find Max | O(log N) |
| Inorder Traversal | O(N) |

But a BST can become skewed.

Example:

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

This is technically a BST, but it behaves like a linked list.

Height becomes:

O(N)

Therefore:

| Operation | Worst Case |
|---|---|
| Search | O(N) |
| Insert | O(N) |
| Delete | O(N) |

## 21. Balanced vs Skewed BST

### Balanced

```
             50
           /    \
         30      70
        /  \    /  \
      20   40  60   80
```

Height:

O(log N)

Operations:

O(log N)

### Skewed

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

Height:

O(N)

Operations:

O(N)

This is a major reason we have AVL Trees and Red-Black Trees.

## 22. BST Complete Example

```java
public class BinarySearchTree {

    static class Node {

        int data;
        Node left;
        Node right;

        Node(int data) {
            this.data = data;
        }
    }

    static Node insert(Node root, int key) {

        if (root == null) {
            return new Node(key);
        }

        if (key < root.data) {
            root.left = insert(root.left, key);
        } else if (key > root.data) {
            root.right = insert(root.right, key);
        }

        return root;
    }

    static Node search(Node root, int key) {

        if (root == null || root.data == key) {
            return root;
        }

        if (key < root.data) {
            return search(root.left, key);
        }

        return search(root.right, key);
    }

    static Node findMin(Node root) {

        while (root.left != null) {
            root = root.left;
        }

        return root;
    }

    static Node delete(Node root, int key) {

        if (root == null) {
            return null;
        }

        if (key < root.data) {

            root.left = delete(root.left, key);

        } else if (key > root.data) {

            root.right = delete(root.right, key);

        } else {

            // No left child
            if (root.left == null) {
                return root.right;
            }

            // No right child
            if (root.right == null) {
                return root.left;
            }

            // Two children
            Node successor = findMin(root.right);

            root.data = successor.data;

            root.right = delete(root.right, successor.data);
        }

        return root;
    }

    static void inorder(Node root) {

        if (root == null) {
            return;
        }

        inorder(root.left);

        System.out.print(root.data + " ");

        inorder(root.right);
    }

    public static void main(String[] args) {

        Node root = null;

        root = insert(root, 50);
        root = insert(root, 30);
        root = insert(root, 70);
        root = insert(root, 20);
        root = insert(root, 40);
        root = insert(root, 60);
        root = insert(root, 80);

        System.out.println("Inorder:");
        inorder(root);

        System.out.println();

        Node result = search(root, 60);

        if (result != null) {
            System.out.println("60 found");
        } else {
            System.out.println("60 not found");
        }

        root = delete(root, 50);

        System.out.println("After deleting 50:");
        inorder(root);
    }
}
```

Output:

```
Inorder:
20 30 40 50 60 70 80

60 found

After deleting 50:
20 30 40 60 70 80
```

## 23. BST Interview Questions

Once you understand the basics, these are the important problems to practice:

### Beginner

- Search an element in BST
- Insert an element in BST
- Find minimum
- Find maximum
- Find height of BST
- Inorder traversal
- Validate whether a tree is a BST

### Intermediate

- Delete a node from BST
- Find kth smallest element
- Find kth largest element
- Find inorder successor
- Find inorder predecessor
- Lowest Common Ancestor in BST
- Convert sorted array to BST
- Find floor and ceil of a value

### Advanced

- Construct BST from preorder
- Construct BST from postorder
- Recover a corrupted BST
- Serialize and deserialize BST
- Balance an unbalanced BST

## ⭐ Key Things to Remember

```
                 BST
                  |
        ┌─────────┴─────────┐
        ↓                   ↓
   Left subtree         Right subtree
   < Root               > Root
```

The three most important BST concepts are:

```
1. Search
       ↓
   key < root → LEFT
   key > root → RIGHT

2. Insert
       ↓
   Compare and move LEFT/RIGHT
   until NULL is found

3. Delete
       ↓
   0 children → simply remove
   1 child    → replace with child
   2 children → replace with successor/predecessor
```

And the golden BST rule:

> 🌟 Inorder traversal of a valid Binary Search Tree gives the elements in sorted order.

That property is one of the most useful tools for solving BST interview problems.
