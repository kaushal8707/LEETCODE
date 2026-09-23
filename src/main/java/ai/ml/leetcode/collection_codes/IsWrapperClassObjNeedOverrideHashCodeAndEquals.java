package ai.ml.leetcode.collection_codes;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

//No Wrapper class By Default Implements hashcode() and equals() methods


public class IsWrapperClassObjNeedOverrideHashCodeAndEquals {

	public static void main(String[] args) {
		Map<String, Integer> map = new HashMap<>();
		map.put("kaushal", 111);
		map.put("kanishk", 222);
		map.put("kaushal", 111);
		Iterator<Entry<String, Integer>> iterator = map.entrySet().iterator();
		while(iterator.hasNext()) {
			Entry<String, Integer> entry = iterator.next();
			System.out.println("Key : "+entry.getKey()+"       "+"  Value : "+entry.getValue());
		}
	}

}
