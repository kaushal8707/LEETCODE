package ai.ml.core_concepts.multithreading.additional;
import java.util.List;

public class Consumer1 implements Runnable
{
    List<Integer> list;
    public Consumer1(List<Integer> list) {
        this.list = list;
    }

    @Override
    public void run()
    {
            System.out.println("Consumer start consuming.......");
            for(int i : list)
            {
                System.out.println(i);
            }
    }
}
