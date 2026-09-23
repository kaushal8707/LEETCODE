
![img11.png](img11.png)
![img_11.png](img_11.png)


# Comparable

![img.png](img11.png)

![img_1.png](img_11.png)

![img_2.png](img_2.png)


# Comparator

![img_3.png](img_3.png)

![img_4.png](img_4.png)

![img_5.png](img_5.png)

![img_6.png](img_6.png)


# Important points on different kinds of Maps
# HashMap vs Concurrent HashMap

ConcurrentHashMap implements ConcurrentMap.
ConcurrentMap extends Map.
=> ConcurrentMap is an Interface where we can achieve Concurrency.

![img_7.png](img_7.png)

![img_8.png](img_8.png)

![img_9.png](img_9.png)

![img_10.png](img_10.png)

![img_12.png](img_12.png)

![img_13.png](img_13.png)

![img_14.png](img_14.png)

![img_15.png](img_15.png)

![img_16.png](img_16.png)

![img_17.png](img_17.png)


# ConcurrentSkipListMap

-> want concurrency in HashMap use - ConcurrentHashMap  -  concurrent version of HashMap 

-> want concurrency in a sorted manner use - ConcurrentSkipListMap -> concurrent version of TreeMap

-> In ConcurrentSkipListMap data will be store in a data structure name "SkipList".

-> Like Map store data in Array so here ConcurrentSkipListMap store data into SKipList.

-> Hash Map store data in Arrays, Tree Map store data in a Red Black Self Balancing Binary Search Tree.

-> ConcurrentSkipListMap stored data in a sorted Manner and we achieve concurrency as well.

-> ![img_18.png](img_18.png)

-> ![img_19.png](img_19.png)

-> ![img_20.png](img_20.png)

-> ![img_21.png](img_21.png)

-> ![img_22.png](img_22.png)

-> ![img_23.png](img_23.png)

-> ![img_24.png](img_24.png)

-> ![img_25.png](img_25.png)


# ENUM Map

![img_26.png](img_26.png)

![img_27.png](img_27.png)

![img_28.png](img_28.png)

![img_29.png](img_29.png)

![img_30.png](img_30.png)

![img_31.png](img_31.png)

![img_32.png](img_32.png)

![img_33.png](img_33.png)

![img_34.png](img_34.png)


# Hash Map

imp-> Map size below code is 5 But should be 4 the reason is we are using a Custom Object(Student) as a key
          in a Map and we have same object 2 times which is wrong.
Sol -  While Custom obj as a Key in Map Need to Override HashCode & Equals method
Note - By default String/Integer override hashcode() and equals() method because String and Integer class is extending an
          Object class and Object class is having hashcode/equals method override.

![img_35.png](img_35.png)

![img_36.png](img_36.png)


![img_59.png](img_59.png)

![img.png](img_61.png)
![img_51.png](img_51.png)
![img_52.png](img_52.png)
![img_53.png](img_53.png)
![img_54.png](img_54.png)
![img_55.png](img_55.png)
![img_56.png](img_56.png)
![img_57.png](img_57.png)


![img_58.png](img_58.png)


# ------------
Without Hash Code and Equlas Method it store 6 object in a map even 2 object is equals

![img_60.png](img_60.png)

After Implementation hashCode() and equals() method properly it will give 5 objects which is expected

![img_62.png](img_62.png)


# Hash Table

In case of HashMap while using multiple threads to store data in a map
both thread at a same time blocking each other tp put elements in a map 
so while trying to store 200 records we can find only 290 records got inserted
in case of HashMap.so HashMap will not give expected results in a multi threaded
environments. let's try same using HashTable.

![img_37.png](img_37.png)

Let's try using HashTable....all 200 stored into a map

![img_38.png](img_38.png)

-> HashTable is Synchronised so It's a Thread-Safe.
-> No Null Key and No Null Value
-> It's Legacy class and get replaced with ConcurrentHashMap.
-> Slower than HashMap because synchronised multiple threads trying to get access.
-> HashTable key and value should not be null will throw an exception.
-> The system how its getting used in hashmap like finding hashcode and calculating bucket
    Index same things happening in hashtable.
-> In hashmap after reaches at certain threshold will use binary search tree if collision occurred
    but here only linked list once collision occurred.

![img_39.png](img_39.png)
![img_40.png](img_40.png)



# Identity HashMap

![img_41.png](img_41.png)

![img_42.png](img_42.png)

![img_43.png](img_43.png)

![img_44.png](img_44.png)

![img_45.png](img_45.png)


# Immutable Map
     
![img_46.png](img_46.png)

![img_47.png](img_47.png)

![img_48.png](img_48.png)

![img_49.png](img_49.png)


# Linked HashMap

![img_50.png](img_50.png)

![img_63.png](img_63.png)

![img_64.png](img_64.png)


# LRU Cache

![img_65.png](img_65.png)

![img_66.png](img_66.png)

![img_67.png](img_67.png)

![img_68.png](img_68.png)

![img_69.png](img_69.png)


# Navigable Map

![img_70.png](img_70.png)

![img_71.png](img_71.png)

![img_72.png](img_72.png)

![img_73.png](img_73.png)

-> Tree Map class Implements Navigable Map
-> Navigable Map extends Sorted Map.
-> Sorted Map extends Map.



# Sorted Map

![img_74.png](img_74.png)

![img_75.png](img_75.png)

![img_76.png](img_76.png)

![img_77.png](img_77.png)

![img_78.png](img_78.png)

![img_79.png](img_79.png)

![img_80.png](img_80.png)

![img_81.png](img_81.png)

![img_82.png](img_82.png)

![img_83.png](img_83.png)

![img_84.png](img_84.png)

![img_85.png](img_85.png)

![img_86.png](img_86.png)

![img_87.png](img_87.png)


# Weak Hash Map

![img_88.png](img_88.png)

![img_89.png](img_89.png)

---------------------------------------------

![img_90.png](img_90.png)

![img_91.png](img_91.png)

----------------------------------------------

![img_92.png](img_92.png)

![img_93.png](img_93.png)

![img_94.png](img_94.png)

![img_95.png](img_95.png)

![img_96.png](img_96.png)

![img_97.png](img_97.png)

![img_98.png](img_98.png)

![img_99.png](img_99.png)

![img_100.png](img_100.png)


