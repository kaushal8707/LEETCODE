package ai.ml.leetcode.collection_codes;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MapAllowNullKeyValueButConcurrentHashMapNot {

	public static void main(String[] args) {
		//HashMap allow null key and value
		Map<String, String> map = new HashMap<>();
		map.put(null, null);
		System.out.println(map);
		
		//ConcurrentHashMap not allow null key and value and throw NullPointerException
		Map<String, String> syncmap = new ConcurrentHashMap<>();
		syncmap.put(null, null);
		System.out.println(syncmap);
		

	}

}
