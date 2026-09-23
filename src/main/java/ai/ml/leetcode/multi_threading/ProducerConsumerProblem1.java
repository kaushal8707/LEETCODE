package ai.ml.leetcode.multi_threading;
import java.util.ArrayList;
import java.util.List;

public class ProducerConsumerProblem1
{
    public static void main(String[] args)
    {
        List<Integer> list=new ArrayList<Integer>();
        Consumer consumer=new Consumer(list);
        Producer producer=new Producer(list,consumer);
        Thread consumerThread=new Thread(consumer);
        Thread producerThread=new Thread(producer);
        consumerThread.start();
        producerThread.start();
    }
}
