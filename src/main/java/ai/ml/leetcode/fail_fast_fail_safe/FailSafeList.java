package ai.ml.leetcode.fail_fast_fail_safe;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class FailSafeList {

	public static void main(String[] args) {
		List<String> list = new CopyOnWriteArrayList<>();
		//Set<String> list = new CopyOnWriteArraySet<>();

		list.add("a");
		list.add("b");
		list.add("c");

		Iterator<String> itr = list.iterator();
		while(itr.hasNext()) {
			String element = itr.next();
			list.add("d");
		}
		System.out.println(list);

	}

}
