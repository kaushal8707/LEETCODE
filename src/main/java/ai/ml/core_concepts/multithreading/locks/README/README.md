
# Scenario 1 (synchronization)

![img.png](img.png)

![img_1.png](img_1.png)

![img_2.png](img_2.png)

![img_3.png](img_3.png)

![img_4.png](img_4.png)

generally In every Object's there is an in build lock available which is synchronized.

# Synchronized Drawbacks

![img_5.png](img_5.png)



# Drawbacks with Synchronized(Implicit lock) and why Lock(Explicit lock or manual lock) came into picture

![img_6.png](img_6.png)


# which thread acquired this lock that only can access a critical section.

![img_7.png](img_7.png)











# Scenario 2 ( tryLock(), lock(), unlock() and interrupt())

dormant - not active for some time

![img_1.png](img_1.png)

![img_8.png](img_8.png)

here you can see Thread-1 couldn't acquired the lock will try again later !!
because Thread 1 will wait for 1 second so he won't get the lock so he came in else block
because the reason is Thread 2 is taking 3 seconds to proceed the withdraw process.
here Thread 1 not get a chance to acquire a lock so he not perform withdrawn.

# step processing

1. Thread2 come he run tryLock() he got the lock
2. so, Thread2 started processing for 3 seconds...
3. within that time Thread1 also came and trying to get lock, he wait for 1 sec but he didn't get the lock so he not run
4. so for Thread 1 got printed "Thread-1 couldn't acquired the lock will try again later !!"
5. and when Thread2 has been proceed for 3 seconds then balance got deducted.
6. and then Thread2 got completed.

# why should not use lock() or why lock.lock() is not much useful

![img_2.png](img_2.png)

# Important Notes on Exception / InterruptedException

![img_9.png](img_9.png)

![img_3.png](img_3.png)

![img_10.png](img_10.png)

=> But In below again we again re-stored their states so that it easy to find out this thread was Interrupted.
earlier we just logging so Logging is not enough.

=> you have to store that state which will tell this Thread was interrupted again you have to re-store it
so that other threads will get to know or If any monitoring tool is running they can found out which thread got interrupted. so its a good practise.

![img_11.png](img_11.png)


# Now after Interrupt if u want to do something

![img_12.png](img_12.png)

Now after Interrupt if u want to do something like logging and all you can do like this. But In that case you can't do because  you lost the state of that Thread.

Now In catch block you manually re-stored the state of that thread. that interrupt that thread so that if we want to do any clean up code  or any maintenance code will run.  



# Scenario 3 ( tryLock(), lock(), unlock() and interrupt())

![img_16.png](img_16.png)








