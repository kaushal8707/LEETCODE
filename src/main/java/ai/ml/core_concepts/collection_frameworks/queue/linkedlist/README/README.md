

![img.png](img.png)
  
![img_1.png](img_1.png)

![img_2.png](img_2.png)

![img_3.png](img_3.png)

![img_4.png](img_4.png)



 Queue we can Implement in a multiple ways:-
 -------------------------------------------


as we know Linked list can act as a stack. we are adding elements at top of the stack. similarly LinkedList can
act as a Queue also.

![img_6.png](img_6.png)

=> Linked List we can use as a Stack & Queue as well. so there is a complexity like addLast removefirst...so Java Introduced one Interface called Queue.

=> LinkedList is an Implementation class of Queue Interface. LinkedList Implements DeQueue. Dequeue extends Queue Interface.

![img_7.png](img_7.png)

![img_8.png](img_8.png)

![img_9.png](img_9.png)


=> ![img_10.png](img_10.png)

![img_11.png](img_11.png)


=> Queue is an Interface so we can't Instantiate directly. so for that we have an Implementation classes...like 
   LinkedList and PriorityQueue.

=>   Interface       Queue
     Impl Class      LinkedList, PriorityQueue



Queue Operations
================

enqueue: is a process to add an elements
     method:         add()                    - throw exception if not able to add, return true if added
     cors method:    offer()                  - return false, if not able to add, return true if added

dequeue: is a process to remove an elements
     method:         remove()                 - throw exception if empty
     cors method:    poll()                   - better, return null if empty

peek:    is a process to view an element at first
     method:         peek()                   - better, return null if empty
     cors method:    element()                - throw exception if empty


![img_12.png](img_12.png)



Queue 1st Implementation class :  LinkedList
---------------------------------------------



![img_13.png](img_13.png)


**** Work is same just error Handling is different**********



Queue 2nd Implementation class :  PriorityQueue
---------------------------------------------




