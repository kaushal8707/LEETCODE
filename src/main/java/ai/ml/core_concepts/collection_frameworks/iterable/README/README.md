

Iterable having one method iterator()

iterator() method returns Iterator

Iterator uses to traverse over the collection one-by-one.

using forEach() loop we can do but with this Iterator also we can do.

=> If any class implements Iterable then there foreach() functionality also will come



How Works
----------

        List<Integer> list = new ArrayList<>();

        -> ArrayList Implements List

![img.png](img.png)

        -> List extends Collection Interface

![img_1.png](img_1.png)

        -> Collection Interface extends Iterable Interface

![img_2.png](img_2.png)

        -> Iterable Interface is having a method called iterator()

![img_3.png](img_3.png)

        -> If you see in ArrayList class having one overriden method iterator() bcz ArrayList Implements List

![img_4.png](img_4.png)


![img_5.png](img_5.png)

Now Java Compiler convert this above code into a below code format

![img_6.png](img_6.png)

=> In our Hierarchy at top Iterable is there that's the reason in all implemented class we can use iterator method.




Iterator remove() method
========================

![img_7.png](img_7.png)


=> If you say we can use CopyOnWriteArrayList here

![img_8.png](img_8.png)

=> *** It will get removed but after this for loop. bcz what copyOnWriteArrayList will do it will iterate foreach on stable copy of list and once the
   modification done on list it will create a new copy of modified list. then after completing for each loop it will replace old list with copied new list.
   => purani list = new list

=> Iterator provide the remove functionality while Iterating
   ----------------------------------------------------------
 
 let's run in a DEBUG mode:-             ->first iterator points to -1 index

![img_9.png](img_9.png)

![img_10.png](img_10.png)

we are having 5 numbers in list before remove

![img_11.png](img_11.png)

while first remove method called it removed from list. 

=> while Iterating it is removing and modifying the list also

=> Here In a collection loop is also running and on same collection removal also running...

=> ![img_12.png](img_12.png)

using list iterator() we can iterate in both direction

some other methods in list iterator()--------

![img_13.png](img_13.png)



   