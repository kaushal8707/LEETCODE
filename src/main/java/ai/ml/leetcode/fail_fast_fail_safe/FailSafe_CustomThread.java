package ai.ml.leetcode.fail_fast_fail_safe;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public class FailSafe_CustomThread implements Runnable{
	Map<Integer, String> map;
	public FailSafe_CustomThread(Map<Integer, String> map) {
		this.map=map;
	}
	@Override
	public void run() {
		System.out.println("Thread: "+ Thread.currentThread().getName());
		
		//Child Thread Trying to add
		map.put(19, "nineteen");
	}
	
	
	
	public static void main(String[] args) throws InterruptedException {
		Map<Integer, String> synchronizeMap = new ConcurrentHashMap<>();
		synchronizeMap.put(11, "eleven");
		synchronizeMap.put(22, "twentytwo");
		synchronizeMap.put(14, "fourteen");
		Thread thread = new Thread(new FailSafe_CustomThread(synchronizeMap), "MyThread");
		thread.start();
		
		Iterator<Entry<Integer, String>> itr = synchronizeMap.entrySet().iterator();
		
		//Main Thread is Iterating
		while(itr.hasNext()) {
			Entry<Integer, String> entry = itr.next();
			Thread.sleep(1000); 
		}
		System.out.println("Map = "+synchronizeMap); 
	}
}
