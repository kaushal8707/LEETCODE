package ai.ml.leetcode.fail_fast_fail_safe;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class FailFastMap {
	public static void main(String[] args) {

		Map<Integer, String> map = new HashMap<>();
		map.put(1, "one");
		map.put(2, "two");
		map.put(4, "four");
		
		Iterator<Integer> itr = map.keySet().iterator();
		while(itr.hasNext()) {
			int key = itr.next();
			System.out.println("Key = "+key+"      "+"Value = "+map.get(key)); 
			map.put(6, "six");
		}
		System.out.println("Map = "+map);  

		
	}

}
