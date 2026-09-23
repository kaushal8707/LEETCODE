package ai.ml.core_concepts.multithreading.thread_methods;

/** Methods of Thread class
        start()
        run()
        sleep()
        join()
        setPriority()
        interrupt()
        yield()
        setDaemon()
    */
public class MyThread extends Thread{
    public MyThread(String tName) {
        super(tName);
    }

    @Override
    public void run() {       /** run() */  // overridden method, actual task start performed in new thread(MyThread)
        for(int i=1;i<=5;i++){
            System.out.println(Thread.currentThread().getName()+" - Priority - "+Thread.currentThread().getPriority()+"- count - "+i);
            try {
                Thread.sleep(1000);  /** sleep()*/  //Causes the currently executing thread to sleep (temporarily cease execution) for the specified number of milliseconds. The thread does not lose ownership of any monitors.
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println(i);
        }
    }

    public static void main(String[] args) throws InterruptedException {

        MyThread t1 = new MyThread("k8707");
        t1.start();         /** start() */ // while this execute JVM will start executing run() method of MyThread class
        t1.join();         /** join() */  // who is calling this join() which is Main Thread in this case will wait for t1 to finish then only Main thread will start execute
                                          // Waits for this thread to die. An invocation of this method behaves in exactly the same way as the invocation //join(0)
        MyThread l = new MyThread("Low Priority Thread");
        MyThread m = new MyThread("Medium Priority Thread");
        MyThread n = new MyThread("High Priority Thread");
        l.setPriority(Thread.MIN_PRIORITY);       /** setPriority() */  // Changes the priority of this thread. High - 10, Norm - 5, Low - 1
        m.setPriority(Thread.NORM_PRIORITY);       // we can set the priority of a thread how JVM is going to execute that thread on a priority basis
        n.setPriority(Thread.MAX_PRIORITY);
        l.start();
        m.start();
        n.start();
        System.out.println("HELLO");

        //interrupt
        TempThread t2=new TempThread("Temp");
        t2.start();
        t2.interrupt();         /** interrupr() */ //interrupt this thread. If u call interrupt () on a thread you are asking to stop there where you are doing
                                                   // either waiting or sleeping or performing task just stop there so line- "Temp Thread is running" not printed
                                                   // printed - Thread is Interruptedjava.lang.InterruptedException: sleep interrupted bcz we interrupted while he went to sleep
        //yield
        DemoThread dt1 = new DemoThread("demo thread-t1");
        DemoThread dt2 = new DemoThread("demo thread-t2");
        dt1.start();
        dt2.start();
                                    //o/p -> after yield method used
                                    //demo thread-t2 is running !!
                                    //demo thread-t2 is running !!
                                    //demo thread-t1 is running !!
                                    //demo thread-t2 is running !!
                                    //demo thread-t1 is running !!
                                    //demo thread-t1 is running !!
                                    //demo thread-t1 is running !!
                                    //demo thread-t2 is running !!
                                    //demo thread-t2 is running !!
                                    //demo thread-t1 is running !!

        //daemon
        DThread dThread = new DThread();
        dThread.setDaemon(true);
        dThread.start();                   // Main Thread has finished there work then also Hello world is printing infinite loop
        System.out.println("Main Done");   // JVM is waiting for DThread to finish which will not in this case bcz DThread is a User Thread now. user tread-actual work thread, daemon thread - who runs in background
                                           // But when Daemon thread is running JVM will not wait for that it will check if main thread done then JVM stop running doesn't matter either daemon thread running or not
    }

      //for interrupt example
      static class TempThread extends Thread{

          public TempThread(String temp) {
              super(temp);
          }
          @Override
          public void run() {
              try {
                  Thread.sleep(5000);
                  System.out.println("Temp Thread is running");
              } catch (InterruptedException e) {
                  System.out.println("Thread is Interrupted"+e);
              }
          }
      }

    //for yield example
    static class DemoThread extends Thread{

        public DemoThread(String demo) {
            super(demo);
        }
        @Override
        public void run() {
           for(int i=1; i<=5;i++){
               System.out.println(Thread.currentThread().getName()+" is running !!");
               Thread.yield();   /** yield */  // A hint to the scheduler that the current thread is willing to yield its current use of a processor. The scheduler is free to ignore this hint.
                                               // want to give a chance to another thread as well to run
                                 /** yield() use case
                                  *
                                  * Yield is a heuristic attempt to improve relative progression between threads that would otherwise over-utilise a CPU.
                                  * Its use should be combined with detailed profiling and benchmarking to ensure that it actually has the desired effect.
                                  It is rarely appropriate to use this method. It may be useful for debugging or testing purposes, where it may help to reproduce bugs due
                                  to race conditions. It may also be useful when designing concurrency control constructs such as the ones in the java. util. concurrent. locks package.
                                  */
           }
        }
    }

    //for Daemon Thread example
    static class DThread extends Thread{
        @Override
        public void run() {
            while(true){
                System.out.println("Hello World !!");
            }
        }
    }

}
