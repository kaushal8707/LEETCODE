package ai.ml.leetcode.multi_threading;
import java.util.ArrayList;
import java.util.List;

public class ProducerConsumerProblem2
{
    public static void main(String[] args) throws InterruptedException
    {
        List<Integer> list=new ArrayList<Integer>();
        Consumer1 consumer=new Consumer1(list);
        Producer1 producer=new Producer1(list);
        Thread consumerThread=new Thread(consumer);
        Thread producerThread=new Thread(producer);
        producerThread.start();
        producerThread.join();
        consumerThread.start();
    }
}
