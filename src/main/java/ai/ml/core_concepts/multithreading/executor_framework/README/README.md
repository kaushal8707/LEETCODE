

# Executor Framework


![img.png](img.png)

![img_1.png](img_1.png)

![img_2.png](img_2.png)

![img_3.png](img_3.png)

![img_4.png](img_4.png)

![img_5.png](img_5.png)

![img_6.png](img_6.png)

![img_7.png](img_7.png)

![img_8.png](img_8.png)

![img_9.png](img_9.png)

![img_10.png](img_10.png)

![img_11.png](img_11.png)

![img_12.png](img_12.png)



# How Thread Pool Works ????

![img_13.png](img_13.png)

![img_14.png](img_14.png)

![img_15.png](img_15.png)



# Fixed Thread Pool 

![img_16.png](img_16.png)

# How to Create and Use a Fixed Thread Pool

![img_17.png](img_17.png)
![img_18.png](img_18.png)
![img_19.png](img_19.png)
![img_20.png](img_20.png)
![img_21.png](img_21.png)
![img_22.png](img_22.png)


# Cache Thread Pool

![img_23.png](img_23.png)
![img_24.png](img_24.png)
![img_25.png](img_25.png)

It uses a SynchronousQueue, meaning tasks are handed off directly to threads rather than being stored in a waiting buffer.
If a thread becomes idle, it is kept in a "cache" to be reused by future tasks.
Idle threads are terminated and removed from the cache if they remain unused for 60 seconds.
It has no fixed upper limit (technically Integer.MAX_VALUE), which can lead to resource exhaustion if too many long-running tasks are submitted.


Suitable for I/O Bound Task But Not Suitable for Intensive tasks

Intensive tasks:-
Intensive tasks are operations demanding significant computational resources(High CPU, GPU, Memoryor RAM usages) and long execution tims.
CPU-Intensive= High processor usages such as algorithms, sorting, searching and compilation.
GPU-Intensive= Graphics heavy operations including 4k video playback and 3D modeling.
Memory-Intensive(RAM)= processing huge datasets or large, complex spreadsheets.
Data-Intensive(I/O)= Heavy data transfer such as big data analytics and database management.

I/O Bound tasks:-
An I/O bound task is a computer process that spends most of its time waiting for input/output operations such as
reading/writing files, database queries or network request and API calls - to complete rather than utilizing the CPU.



# Scheduled Thread Pool

In Java, a Scheduled Thread Pool is a specialized implementation of the ExecutorService designed to execute tasks after a specific delay or periodically. 
It is primarily managed via the ScheduledThreadPoolExecutor class

![img_26.png](img_26.png)

![img_27.png](img_27.png)

![img_28.png](img_28.png)

![img_29.png](img_29.png)

![img_30.png](img_30.png)

![img_31.png](img_31.png)

![img_32.png](img_32.png)


# SingleThreadExecutor

![img_34.png](img_34.png)

![img_35.png](img_35.png)

![img_36.png](img_36.png)

![img_33.png](img_33.png)

![img_37.png](img_37.png)


# Virtual Thread

Virtual Threads (Java 21+)

Virtual threads are extremely lightweight, JVM-managed threads designed to replace traditional pooling for I/O-bound tasks. 

![img_38.png](img_38.png)

![img_39.png](img_39.png)


# To migrate from a CachedThreadPool or FixedThreadPool to Virtual Threads

![img_40.png](img_40.png)

![img_41.png](img_41.png)

![img_42.png](img_42.png)

![img_43.png](img_43.png)

![img_44.png](img_44.png)


# Use Case : Virtual Threads

![img_45.png](img_45.png)

![img_46.png](img_46.png)

![img_47.png](img_47.png)

![img_48.png](img_48.png)