![img.png](img.png)

![img_1.png](img_1.png)

![img_2.png](img_2.png)

![img_3.png](img_3.png)

![img_4.png](img_4.png)

![img_5.png](img_5.png)



# The volatile keyword in Java guarantees visibility of changes to a variable across threads, but it does not guarantee atomicity for compound operations

Visibility okay, not atomic

![img_6.png](img_6.png)

![img_7.png](img_7.png)

![img_8.png](img_8.png)



# Solution for Atomicity

To fix this, use synchronized blocks for mutual exclusion or AtomicInteger for lock-free, thread-safe operations. 

![img_9.png](img_9.png)

![img_10.png](img_10.png)

![img_11.png](img_11.png)


# how volatile used in the Singleton Design Pattern to prevent threads from seeing a partially constructed object.

![img_12.png](img_12.png)

![img_13.png](img_13.png)

![img_14.png](img_14.png)