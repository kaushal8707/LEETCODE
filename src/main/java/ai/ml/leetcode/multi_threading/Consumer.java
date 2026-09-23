package ai.ml.leetcode.multi_threading;
import java.util.List;

public class Consumer implements Runnable
{
    List<Integer> list;
    public Consumer(List<Integer> list) {
        this.list = list;
    }

    @Override
    public void run()
    {
        synchronized (this)
        {
            System.out.println("Consumer waiting to complete production...");
            try {
                this.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Consumer start consuming.......");
            for(int i : list)
            {
                System.out.println(i);
            }
        }
    }


}
