package ai.ml.core_concepts.multithreading.additional;
import java.util.List;

public class Producer1 implements Runnable
{
    List<Integer> list;
    Consumer consumer;

    public Producer1(List<Integer> list) {
        this.list = list;
    }

    @Override
    public void run()
    {
            System.out.println("Producer start producing...");
            for(int i=1;i<=10;i++)
            {
                System.out.println(i);
                list.add(i);
            }
        }
}
