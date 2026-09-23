package ai.ml.leetcode.fail_fast_fail_safe;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FailFastList {

	public static void main(String[] args) {

		List<String> list = new ArrayList<>();
		list.add("a");
		list.add("b");
		
		Iterator<String> itr = list.iterator();
		while(itr.hasNext()) {
			String element = itr.next();
			System.out.println(element);
			list.add("c");
		}
	}

}
