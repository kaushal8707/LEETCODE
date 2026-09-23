
 In a Multithreaded environment, threads often need to communicate and coordinate with each other to
 accomplish a task
 

 Without proper communication mechanism, threads might end up in inefficient busy-waiting states,
 leading to wastage of CPU resources and potential deadlocks.

-> wait()
-> notify()
-> notifyAll()

=> These methods (wait, notify and notifyALL) introduced for Inter-Thread-Communication

# problems without proper thread communication

    without proper thread communication threads need to check conditions continuously in which CPU would be wastage

    assume there is a producer which is producing the data and a consumer which is consuming the data

    without proper inter thread communication consumer always need to check when data is coming ...when data is coming..so on..

    so this is the wasting of CPU. so it would be nice if producer only can notify and say data has been produced...

    these 3 methods (wait, notify, notifyALl) only can be called within a synchronized context..so only you can call these methods 
    either within a synchronized methods or within a synchronized blocks.

# wait()

    wait will release the lock from current thread and wait untill another thread on same object will run either notify or notifyAll methods.

# notify

    notify wakes up a single thread.

# notifyAll

    wakes up all threads that are waiting...

# Code Explanation

    There is a producer and a consumer

    There is 2 threads 1 will produce the data and another will consume the data
    
    let's see how they will communicate with each other

        we want producer to produce data but if hasData is true then he has to wait, consume consume data only in case hasData true 
        when consumer will consume the data when the data is present similarly if the data is already there in buffer then producer 
        won't produce he should wait.
        now thread is communication with the help of hasData flag.

img4

# How notify works

    here we have putted the lock on sharedResources object, when we run notify() here the another thread which is waiting and trying to get
    lock or which is trying to get access same object that object we are trying to notify that you can access now there might be he were 
    waiting so he get to know producer has notified me so he is ready to consume because hasData he made as a true.

# notifyAll
    
    If there is only 1 producer and multiple consumers in that case you can use notifyAll()