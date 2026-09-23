

![img.png](img.png)

        // Set is a collection that can not contain duplicate elements.

        // Because Set is Based on HashMap Principles so
              // Time Complexity ->   find()  ->  O(1)
              // Time Complexity ->   insert() -> O(1)

        // Faster Operations

        // Map ->  Implementations (HashMap, LinkedHashMap, TreeMap, EnumMap)
        // Set ->  Implementations (HashSet, LinkedHashSet, TreeSet, EnumSet)


discussion
------------

while we do 

  Map<Integer, String> map = new HashMap<>();
  Set<Integer> integers = map.keySet();
  

so, Actually the keys in a HashMap is a actually a Set. because keys in HashMap are unique.

Internal Implementation similar to HashMap where values will be a dummy values so these all are a keys only.

=> All Methods of Set is similar to a List Because Set also extends Collection Interface like List.

=> ![img_1.png](img_1.png)

    so un-ordered or not maintain Insertion-order.
    
    elements will be in sorted manner.

=> But If you want to maintain Order use [LinkedHashSet] internally it use a LinkedList to maintain an Order.   

  ![img_2.png](img_2.png)

=> If you want elements in a sorted manner use TreeSet

![img_3.png](img_3.png)

=> TreeSet implements NavigableSet so if we give reference of NavigableSet we can have some additional methods.

=> Set contains all methods like a List bcz list and set both extends Collection Interface. Map not comes under collection framework.

![img_4.png](img_4.png)



Thread - Safety ( Not Recommended - Collections.synchronizedSet(set))
=====================================================================

![img_5.png](img_5.png)

=> Using Collections.synchronizedSet(set);  -> we can make out set synchronized and thread safe.

=> If you pass any implementation like HashSet, LinkedHashSet, TreeSet, EnumSet in a synchronizedSet()
   whet it will do it will call external Synchronization which mean this normal set will be wrapped inside 
   Synchronization block.

=> All operations are a blocking operations which means If any thread is doing any operation it not allow to 
another thread to do anu operations so there will be no performance.

=> just assume if we are using a TreeSet then there Red Black Tree will come so again performance will be too
   slow so i will recommend don't use this synchronizedSet(set) method.



Thread - Safety ( Recommended - ConcurrentSkipListSet )
========================================================

=> This ConcurrentSkipListSet implements NavigableSet.

=> In a ConcurrentSkipListSet we have a built-in Thread Safety so i recommend use this instead of  Collections.synchronizedSet(set);


   another example
   ---------------

![img_6.png](img_6.png)

=> we are using synchronizedSet() here, only for a loop again we need to use synchronized() method. 
   All methods of our Tree Set wrapped with a Synchronized Block by using this synchronizedSet() method
   while we are doing loop for own again we need to use synchronized() block/method because one by one
   loop is iterating in that situation may be a chance another thread can access. so It's not recommended.
   so we should use ConcurrentSkipListSet().

=> ConcurrentSkipListSet() is our Thread Safe Set. 




Unmodifiable - Set
=================

=> How we were having unModifiableMap() here also we are having UnmodifiableSet. 

![img_7.png](img_7.png)








   

   
