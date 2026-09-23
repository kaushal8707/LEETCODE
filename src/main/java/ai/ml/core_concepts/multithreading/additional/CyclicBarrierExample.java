package ai.ml.core_concepts.multithreading.additional;
import java.util.concurrent.CyclicBarrier;

class MyRunnable7 implements Runnable
{
    CyclicBarrier barrier;
    public MyRunnable7(CyclicBarrier bar) {
        this.barrier = bar;
    }
    @Override
    public void run()
    {
        System.out.println(Thread.currentThread().getName()+"  started ");
        try {
            barrier.await();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}

public class CyclicBarrierExample
{
    public static void main(String[] args) throws InterruptedException
    {
        CyclicBarrier cyclicBarrier=new CyclicBarrier(4,new TriggeredEvent());
        new Thread(new MyRunnable7(cyclicBarrier),"Thread1")
                .start();
        new Thread(new MyRunnable7(cyclicBarrier),"Thread2")
                .start();
        new Thread(new MyRunnable7(cyclicBarrier),"Thread3")
                .start();
        new Thread(new MyRunnable7(cyclicBarrier),"Thread4")
                .start();
        System.out.println(Thread.currentThread().getName()+"  E N D E D ");
    }
}