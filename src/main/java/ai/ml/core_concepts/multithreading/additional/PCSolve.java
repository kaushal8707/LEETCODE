package ai.ml.core_concepts.multithreading.additional;

import java.util.ArrayList;
import java.util.List;

public class PCSolve {

	public static void main(String[] args) throws InterruptedException
	{
	  List<Integer> list=new ArrayList<Integer>();
      Consumer consumer=new Consumer(list);
      Producer producer=new Producer(list,consumer);
      Thread c=new Thread(consumer, "consumer");
      Thread p=new Thread(producer, "producer");
      c.start();
      Thread.sleep(3000); 
      p.start(); 

	}

}
