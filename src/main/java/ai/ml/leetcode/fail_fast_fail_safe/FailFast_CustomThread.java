package ai.ml.leetcode.fail_fast_fail_safe;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class FailFast_CustomThread implements Runnable {
	Map<Integer, String> map;
	public FailFast_CustomThread(Map<Integer, String> map) {
		this.map=map;
	}
	@Override
	public void run() {
		System.out.println("Thread: "+ Thread.currentThread().getName());
		
		//Child Thread Trying to add
		map.put(19, "nineteen");
	}
	
	
	public static void main(String[] args) throws InterruptedException {
		Map<Integer, String> map = new HashMap<>();
		map.put(11, "eleven");
		map.put(22, "twentytwo");
		map.put(14, "fourteen");
		Thread thread = new Thread(new FailFast_CustomThread(map), "MyThread");
		thread.start();
		
		//Main Thread is Iterating
		Iterator<Entry<Integer, String>> itr = map.entrySet().iterator();
		while(itr.hasNext()) {
			Entry<Integer, String> entry = itr.next();
			Thread.sleep(1000); 
		}
		System.out.println("Map = "+map); 
	}


}
