package ai.ml.leetcode.multi_threading;
import java.util.concurrent.CountDownLatch;

class MyRunnable8 implements Runnable
{
    CountDownLatch latch;
    public MyRunnable8(CountDownLatch downLatch) {
        this.latch = downLatch;
    }
    @Override
    public void run()
    {
        for(int i=3;i>=0;i--)
        {
            latch.countDown();
            System.out.println("Count has decreased with "+i);
        }
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

public class CountDownLatchExample
{
    public static void main(String[] args) throws InterruptedException {
        CountDownLatch count=new CountDownLatch(4);
        System.out.println("Count Down Latch created with all count = "+4);
        new Thread(new MyRunnable8(count)).start();
        count.await();
        System.out.println("Count Down Latch reached to count = 0");
        System.out.println(Thread.currentThread().getName()+"  E N D E D ");
    }
}
