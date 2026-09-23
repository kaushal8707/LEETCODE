synchronized won't differentiate between read and write operations because of that there might be some unnecessary blocking occurred.

# ReadWriteLock

If you want to differentiate between read and write operations then you must have to use ReentrantReadWriteLock. 
ReadWrite Lock allows multiple threads to read resources concurrently as long as no threads is writing to it. 
It ensures exclusive access for write operations. 
we have created 2 Read and Write Tasks. 
now we have 2 tasks one is to write and another is to read. 
one write thread which will increment the counter value and 2 read threads which will read the counter value.

Threads only can acquire Read lock when Write lock not get acquired with any another threads. 
because readLock and writeLock both are related to each other because both are the part of ReadWriteLock. 
so they can communicate with each other. so they will check Is write lock not acquired with any other threads 
If acquired then none of the threads will acquired read lock. 
If write lock not there then any number of threads can come and read.


Writer threads has incremented 10 times. 
when writerThread.start() run then in a loop 10 times it will lock and 10 times it will unlock. 
when it will be running then none of the read locks can acquire. 
when 1st time writer lock run and then writer lock got unlocked then 1 reader thread read the data which mean getCount() read and print. 
and when we were trying to read and print in that duration writer thread 10 times got run. 
and when writer thread 10 times run then reader thread ran 9 times and getting the updated count 10.
everything depends on CPU.


if you want first writer thread to write then reader threads to read just use sleep in increment() method after increment the count.
The main things for this topic is we have a read and write strategy in threads in locking. if we will not use this then unnecessary locking will occur in read lock as well.[ReadWriteCounter.java](../../../../../../../../../../../../../../../multithreading-bootcamp/multithreading-bootcamp/src/main/java/locks/read_write_locks/ReadWriteCounter.java)
If there will be normal lock then it might be a chance get same response but here no un-necessary locking is occurring.

![img.png](img.png)

![img_1.png](img_1.png)

![img_2.png](img_2.png)

![img_3.png](img_3.png)

