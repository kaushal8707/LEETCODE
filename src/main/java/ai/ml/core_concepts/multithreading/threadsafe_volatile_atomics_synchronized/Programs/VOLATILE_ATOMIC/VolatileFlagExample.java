package ai.ml.core_concepts.multithreading.threadsafe_volatile_atomics_synchronized.Programs.VOLATILE_ATOMIC;

public class VolatileFlagExample {

    private volatile boolean stopRequested=false;            // The 'volatile' keyword ensures that the 'stopRequested' variable is always
                                                                                    // read from and written to main memory, preventing threads from using
                                                                                    // a stale, cached value in their local CPU caches.
    public  void startWorkerThread() {

      new Thread(new Runnable() {
          @Override
          public void run() {
              int count=0;
              while(!stopRequested){
                  count++;
                  System.out.println(Thread.currentThread().getName()+"  "+count);
                  try {
                      Thread.sleep(1000);
                  } catch (InterruptedException e) {
                      throw new RuntimeException(e);
                  }
              }
          }
      }).start();
    }

    public void stopRequested() {
        stopRequested=true;           // This writes to the volatile variable is immediately visible to the worker thread.
        System.out.println(Thread.currentThread().getName()+" requested to stop !!");
    }

    public static void main(String[] args) throws InterruptedException {
        VolatileFlagExample volatileExample=new VolatileFlagExample();
        volatileExample.startWorkerThread();

        // Give the worker thread a moment to start
        Thread.sleep(6000);

        // Signal the worker thread to stop
        volatileExample.stopRequested();
    }
}
