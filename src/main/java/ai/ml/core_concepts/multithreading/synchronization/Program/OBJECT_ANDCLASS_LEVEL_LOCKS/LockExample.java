package ai.ml.core_concepts.multithreading.synchronization.Program.OBJECT_ANDCLASS_LEVEL_LOCKS;

    public class LockExample {
        // Object Level Lock: synchronized non-static method
        public synchronized void syncObjectMethod(String threadName) {
            System.out.println(threadName + " acquired object lock on " + this.toString());
            try {
                // Simulate some work
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(threadName + " released object lock on " + this.toString());
        }

        // Class Level Lock: synchronized static method
        public static synchronized void syncClassMethod(String threadName) {
            System.out.println(threadName + " acquired class lock on LockExample.class");
            try {
                // Simulate some work
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(threadName + " released class lock on LockExample.class");
        }
}
