package ai.ml.leetcode.collection_codes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CustomUnModifiableList {
	public static void main(String[] args) {
		List<Integer> list = new ArrayList();
		list.add(23);
		list.add(24);
		list.add(25);
		//System.out.println("List => "+list);
		
		list = Collections.unmodifiableList(list);
		
		list.add(26);
		System.out.println("Updated List => "+list);	
	}
}
