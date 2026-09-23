![img_1.png](img_1.png)

Blocking Queue are of 4 Types:

1. Array Blocking Queue  - BlockingQueue
2. Linked Blocking QUeue  -BlockingQueue
3. Priority Blocking Queue  -BlockingQueue
4. Synchronous Queue  -BlockingQueue
5. Delay Queue - Kind of Blocking Queue

![img_3.png](img_3.png)

![img_4.png](img_4.png)

![img_5.png](img_5.png)

![img_6.png](img_6.png)

![img_7.png](img_7.png)

![img_8.png](img_8.png)

# Synchronous Blocking Queue

![img_9.png](img_9.png)

![img_10.png](img_10.png)

![img_11.png](img_11.png)


# Priority Blocking Queue

![img_12.png](img_12.png)

![img_13.png](img_13.png)

![img_14.png](img_14.png)

![img_15.png](img_15.png)

![img_16.png](img_16.png)

![img_17.png](img_17.png)

![img_18.png](img_18.png)

![img_19.png](img_19.png)

Examples-

**we can use a PriorityBlockingQueue in order to block a thread until some items have been added to it, and also that 
we are able to process those items based on their priority.**

![img_20.png](img_20.png)

![img_21.png](img_21.png)

![img_22.png](img_22.png)

![img_23.png](img_23.png)

![img_24.png](img_24.png)

![img_25.png](img_25.png)


# Array Blocking Queue / Linked Blocking Queue

![img_26.png](img_26.png)

![img_27.png](img_27.png)

![img_28.png](img_28.png)

![img_29.png](img_29.png)

![img_30.png](img_30.png)

Example -

![img_31.png](img_31.png)

![img_32.png](img_32.png)

![img_33.png](img_33.png)

![img_34.png](img_34.png)


# Delay Queue

![img_35.png](img_35.png)

![img_36.png](img_36.png)

![img_37.png](img_37.png)

![img_38.png](img_38.png)

![img_39.png](img_39.png)

![img_40.png](img_40.png)

![img_41.png](img_41.png)



# Dequeue

Imp Points:
-Deque is an Interface
-There are 2 Implementations of Dequeue Interface
   -Array Dequeue
   -LinkedList

   -double ended queue
   -allows insertion and deletion of elements from both ends.
   -versatile than regular queues and stacks because they support all the operations of both

![img_42.png](img_42.png)

![img_43.png](img_43.png)

   - Array Deque : This class is likely to be faster than Stack when used as a stack, and faster than LinkedList when used as a queue.
   - ArrayDeque & LinkedList both are a double-ended Queue.

=> ArrayDeque- Faster Access bcz of contiguous memory allocation
=> Low memory bcz no store pointer, no null allowed ...recommended to use ArrayDeque instead of Linked List.
=> Circular head and tail
=> No need to shift element just shift head and tail

Array Dequeue Imp : 

=>Resizable-array implementation of the Deque interface. Array deques have no capacity restrictions; they grow as necessary to support usage.
=>They are not thread-safe; in the absence of external synchronization, they do not support concurrent access by multiple threads.
=>Null elements are prohibited.
=> This class is likely to be faster than Stack when used as a stack, and faster than LinkedList when used as a queue.

=> In a ArrayDeque If we remove an element from starting then do we shift an element.

=> No, Because here the Array is use that is Circular Array.

=> which means there will be 2 pointers Head and Tail that pointer we move.

=> actually no need to move an element.

=> If we insert at starting then head-- if we insert at end tail++

=> here actually its not Circular but we make circular with play with indexes.

=> Wrap around will happen while we use module formula. if we have 4 index....0%4=0,1%4=1, 2%4=2,3%4=3,4%4=0....

=> If size is full means Head=tail then size will be double.

![img_49.png](img_49.png)


Dequeue Implementation-

        Array Dequeue
        LinkedList

![img_44.png](img_44.png)

![img_45.png](img_45.png)

=> ArrayDeque & LinkedList both are a double-ended Queue.

![img_46.png](img_46.png)

    Reason:

=>In case of Queue (ArrayDeque) It's Fast. Internally we have an Array. so Faster Iteration bcz Array is having Contiguous Memory Allocation. low memory bcz we are not 
     storing any pointer no null allowed

=> I recommended to use ArrayDeque only, If you have to Implement Deque unless if you have a specific requirements of linked list.

=> In ArrayDeque there is an Internal Array, If there size is full it would be double.

=> LinkedList we can only use while want to insert delete in middle of the position.

![img_47.png](img_47.png)

![img_48.png](img_48.png)

![img_50.png](img_50.png)


# Linked List (In Java LinkedList is an Implementation class of a Deque Interface, Dequeue Interface Extends Queue Interface)

![img_51.png](img_51.png)

**Very Imp** : LinkedList Implements Deque and List Interface.

Imp - **Linked list can act as a stack. we are adding elements at top of the stack. similarly LinkedList can act as a Queue also.**

**Queue**- A Data Structure that works on FIFO (First In First Out) Principle.

Stack - LIFO
Queue - FIFO

Queue - Elements are added from end and removed from front.

# Linked List can act as a Stack and Queue
# Linked can be use to implement Stack & Queue both.

![img_52.png](img_52.png)

![img_53.png](img_53.png)

![img_54.png](img_54.png)

=> Queue is an Interface so we can't Instantiate directly. so for that we have an Implementation classes...like LinkedList and PriorityQueue.

=> Interface Queue Impl Class LinkedList, PriorityQueue

# Queue Operations-

-> enqueue: 
        Is a process to add an elements 
        method: 
            add() - throw exception if not able to add, return true if added 
        core method: 
            offer() - return false, if not able to add, return true if added

-> dequeue: 
        Is a process to remove an elements 
        method: 
            remove() - throw exception if empty 
        core method: 
            poll() - better, return null if empty

-> peek: 
        Iis a process to view an element at first 
        method: 
            peek() - better, return null if empty 
        core method: 
            element() - throw exception if empty

LinkedList: Queue Implementation

![img_56.png](img_56.png)

![img_55.png](img_55.png)

![img_57.png](img_57.png)

![img_58.png](img_58.png)


Linked List has No fixed Capacity because it dynamically grow but Array List have because it uses static contagious Array.

=> So, better to define Fixed Size Queue Implementation we can use ArrayBlockingQueue.

![img_59.png](img_59.png)

exact same result with ArrayBlockingQueue and LinkedBlockingQueue

![img_60.png](img_60.png)


# Priority Queue

![img_61.png](img_61.png)

        /** we don't care about queue other element's in which order they are in queue
         * we just worry about the head on priority element
          */

![img_62.png](img_62.png)

![img_63.png](img_63.png)

![img_64.png](img_64.png)

![img_65.png](img_65.png)

![img_66.png](img_66.png)

![img_67.png](img_67.png)

![img_68.png](img_68.png)

![img_69.png](img_69.png)

![img_70.png](img_70.png)

![img_71.png](img_71.png)

![img_72.png](img_72.png)

![img_73.png](img_73.png)

![img_74.png](img_74.png)

![img_75.png](img_75.png)

![img_76.png](img_76.png)

![img_77.png](img_77.png)

![img_78.png](img_78.png)

![img_79.png](img_79.png)

![img_80.png](img_80.png)


# Non-Blocking Thread Safe Queue

Non-Blocking Thread Safe Queue
    - ConcurrentLinkedQueue
    - ConcurrentLinkedDeQueue

# ConcurrentLinkedDeQueue

**BlockingQueue only needed when two threads wants to communicate with each other and there are some situations
where we don't want to block the threads but want to access the queue, this is where non-blocking thread safe queue come into
picture.**

 ConcurrentLinkedDeQueue - An Implementation of the Queue interface that supports lock-free, thread-safe operations
                                      - Concurrent version of Dequeue
                                      - Non Blocking Thread Safe Double Ended Queue
                                      -Uses Compare and Swap technique

->ConcurrentLinkedDeQueue implements DeQueue
->ConcurrentLinkedQueue implements Queue
->Dequeue extends Queue

-> Queue can perform operations from one end only **add/offer** from rear end and **remove/poll** from fron end
-> Dequeue can perform operations from both end
            Is a process to add an elements
                method:
                    add() - throw exception if not able to add, return true if added
                core method:
                    offer() - return false, if not able to add, return true if added
            Is a process to remove an elements
                method:
                    remove() - throw exception if empty
                core method:
                    poll() - better, return null if empty
                    pop() - Throwing exception + removing operations
            Iis a process to view an element at first
                method:
                    peek() - better, return null if empty
                core method:
                    element() - throw exception if empty

![img_81.png](img_81.png)


# ConcurrentLinkedQueue

An Implementation of the Queue Interface that supposrts lock-free thread-safe operations.
ConcurrentLinkedQueue also uses compare and swap technique internally
In a Queue LinkedList and PriorityQueue both are not thread safe => In a DeQueue ArrayDequeue and LinkedList both are not thread safe

In case of Blocking Queue there will be 2 cases
   1. Is put() and put() can run together  -> No
   2. Is put() and take() can run together  -> No

But In ConcurrentLinkedQueue we can do both above operations together because of this ConcurrentLinkedQueue  will have high throughput.

Use case -
    We are creating a Task Submission System where multiple producers continuously adding a tasks and multiple consumers processing the tasks.
    We have to perform things consurrently without slowing each other.
    Now In Blocking What put will do here it will put a lock . when he putting elements he put a lock so, no other threads can access the queue. 
    In some situations we need blocking but it would be delay because of lock
    In this TaskSubmission System, while submitting a task why are you blocking a consumers you should allow him 
    to consume the tasks from the queue, so to achieve this lock-free thread safe we will use ConcurrentLinkedQueue.

# Code Comparison Between BlockingQueue & NonBlockingQueue

**Code Example - NonBlockingQueue :  ConcurrentLinkedQueue**

    public class ConcurrentLinkedQueueDemo {
    private static ConcurrentLinkedQueue<String> taskQueue = new ConcurrentLinkedQueue<>();

    public static void main(String[] args) {
        Thread producer = new Thread(() -> {
            while (true) {
                try{
                    taskQueue.add("Task: "+System.currentTimeMillis());
                    System.out.println("Submitted Task : "+System.currentTimeMillis());
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
        Thread consumer = new Thread(() -> {
            while (true) {
                try{
                    String task = taskQueue.poll();
                    System.out.println("processing... : "+task);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
        producer.start();
        consumer.start();
    }
}

![img_82.png](img_82.png)





    public class ConcurrentLinkedQueueDemo {
    private static ConcurrentLinkedQueue<String> taskQueue = new ConcurrentLinkedQueue<>();

    public static void main(String[] args) {
        Thread producer = new Thread(() -> {
            while (true) {
                try{
                    taskQueue.add("Task: "+System.currentTimeMillis());
                    System.out.println("Submitted Task : "+System.currentTimeMillis());
                    Thread.sleep(1000);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
        Thread consumer = new Thread(() -> {
            while (true) {
                try{
                    String task = taskQueue.poll();
                    System.out.println("processing... : "+task);
                    Thread.sleep(1000);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
        producer.start();
        consumer.start();
    }
}

![img_84.png](img_84.png)

**Code Example - BlockingQueue :  LinkedBlockingQueue**

    public class ConcurrentLinkedQueueDemo {
    private static BlockingQueue<String> taskQueue = new LinkedBlockingQueue<>();

    public static void main(String[] args) {
        Thread producer = new Thread(() -> {
            while (true) {
                try{
                    taskQueue.put("Task: "+System.currentTimeMillis());  /** LinkedBlockingQueue - add tasks(will use lock internally)*/
                    System.out.println("Submitted Task : "+System.currentTimeMillis());
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
        });
        Thread consumer = new Thread(() -> {
            while (true) {
                try{
                    String task = taskQueue.take();  /** LinkedBlockingQueue - take tasks(will use lock internally)*/
                    System.out.println("processing... : "+task);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
        });
        producer.start();
        consumer.start();
    }
}

![img_83.png](img_83.png)


    public class ConcurrentLinkedBlockingQueueDemo {
    private static BlockingQueue<String> taskQueue = new LinkedBlockingQueue<>();

    public static void main(String[] args) {
        Thread producer = new Thread(() -> {
            while (true) {
                try{
                    taskQueue.put("Task: "+System.currentTimeMillis());  /** LinkedBlockingQueue - add tasks(will use lock internally)*/
                    System.out.println("Submitted Task : "+System.currentTimeMillis());
                    Thread.sleep(1000);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
        });
        Thread consumer = new Thread(() -> {
            while (true) {
                try{
                    String task = taskQueue.take();  /** LinkedBlockingQueue - take tasks(will use lock internally)*/
                    System.out.println("processing... : "+task);
                    Thread.sleep(1000);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
        });
        producer.start();
        consumer.start();
    }
}

![img_85.png](img_85.png)

