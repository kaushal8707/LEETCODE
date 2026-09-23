package ai.ml.core_concepts.multithreading.threadsafe_volatile_atomics_synchronized.Programs.VOLATILE_ATOMIC.The_Compound_Operation_Problem.SOLUTION;

public class SolutionForAtomicity_Synchronized {

    private int count = 0;

    public synchronized void increment()
    {
        count++;
    }

    public synchronized  int getCount() {
        return count;
    }

    public static void main(String[] args) throws InterruptedException {
        SolutionForAtomicity_Synchronized obj=new SolutionForAtomicity_Synchronized();
        Runnable runnable=new Runnable() {
            @Override
            public void run() {
                for(int i=1;i<=1000;i++){
                    obj.increment();                  // 1000 tasks are processing by threads
                }
            }
        };
        Thread t1=new Thread(runnable);
        Thread t2=new Thread(runnable);

        t1.start();
        t2.start();

        t1.join();
        t2.join();

        System.out.println("counter result : "+obj.getCount());

    }
}
