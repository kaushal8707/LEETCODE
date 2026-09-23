package ai.ml.core_concepts.multithreading.synchronization.Program.OBJECT_ANDCLASS_LEVEL_LOCKS;

public class ThreadDemo {
    public static void main(String[] args) {
        LockExample obj1 = new LockExample();
        LockExample obj2 = new LockExample();

        // --- Demonstrate Object Locks ---
        // Threads t1 and t2 can run concurrently because they lock on different objects (obj1 and obj2)
        System.out.println("--- Object Lock Demonstration ---");
        Thread t1 = new Thread(() -> obj1.syncObjectMethod("Thread 1 (obj1)"));
        Thread t2 = new Thread(() -> obj2.syncObjectMethod("Thread 2 (obj2)"));
        t1.start();
        t2.start();

        try {
            t1.join();
            t2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // --- Demonstrate Class Locks ---
        // Threads t3 and t4 cannot run concurrently because they both lock on the same Class object (LockExample.class)
        System.out.println("\n--- Class Lock Demonstration ---");
        Thread t3 = new Thread(() -> LockExample.syncClassMethod("Thread 3 (static)"));
        Thread t4 = new Thread(() -> LockExample.syncClassMethod("Thread 4 (static)"));
        t3.start();
        t4.start();
    }
}
