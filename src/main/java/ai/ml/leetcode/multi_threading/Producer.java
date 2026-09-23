package ai.ml.leetcode.multi_threading;
import java.util.List;

public class Producer implements Runnable
{
    List<Integer> list;
    Consumer consumer;

    public Producer(List<Integer> list, Consumer consumer) {
        this.list = list;
        this.consumer = consumer;
    }

    @Override
    public void run()
    {
        synchronized (this.consumer)
        {
            System.out.println("Producer start producing...");
            for(int i=1;i<=10;i++)
            {
                list.add(i);
            }
        }

    }
}
