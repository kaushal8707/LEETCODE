package ai.ml.core_concepts.multithreading.additional;
import java.util.concurrent.CountDownLatch;

class MyRunnable1 implements Runnable
{
    CountDownLatch latch;
    public MyRunnable1(CountDownLatch downLatch) {
        this.latch = downLatch;
    }
    @Override
    public void run()
    {
        System.out.println(Thread.currentThread().getName()+"  started ");
        latch.countDown();
        System.out.println(Thread.currentThread().getName()+"  ended "+" cound is now ="+latch.getCount());

    }
}

public class CountDownLatchExample1
{
    public static void main(String[] args) throws InterruptedException {
        CountDownLatch count=new CountDownLatch(4);
        System.out.println("Count Down Latch created with all count = "+4);
        new Thread(new MyRunnable1(count),"Thread1")
                .start();
        new Thread(new MyRunnable1(count),"Thread2")
                .start();
        new Thread(new MyRunnable1(count),"Thread3")
                .start();
        new Thread(new MyRunnable1(count),"Thread4")
                .start();
        count.await();
        System.out.println("Count Down Latch reached to count = 0");
        System.out.println(Thread.currentThread().getName()+"  E N D E D ");
    }
}