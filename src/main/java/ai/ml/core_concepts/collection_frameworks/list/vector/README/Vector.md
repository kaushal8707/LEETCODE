![img.png](img.png)

![img_1.png](img_1.png)

![img_2.png](img_2.png)

    ** In Vector Unlike an Array List we can check the Initial-Capacity or capacity.
   
    ** ArrayList Increase there Internal Array size with 1.5 but Vector will double it's size.
   
    ** Vector Initial Capacity is 10 but we can define capacity so it will grow with that capacity.

![img_3.png](img_3.png)

![img_4.png](img_4.png)

![img_5.png](img_5.png)

![img_6.png](img_6.png)


    ** Vector All methods are Synchronized.

    ** In Single threaded env we should never go for Vector bcz of locking-unlocking it slow performances.

![img_7.png](img_7.png)

![img_8.png](img_8.png)

![img_9.png](img_9.png)


        This is happening bcz Array list is not synchronized means not a thread safe.
        which means at same time both the threads accessed the arraylist object and while
        trying to write instead of 2 only one thread can write. 