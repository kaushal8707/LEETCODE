package ai.ml.leetcode.collection_codes;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

// If you are using Set with a primitive data type or Wrapper Class, then its fine no need to override equals() and hashcode() methods. 
// But if you are using any Custom object then you must need to override equals() and hashCode() methods.


public class CustomExamleSetAllowsDuplicate {

	public static void main(String[] args) {
		Set<Employee> set = new HashSet();
		set.add(new Employee(1, "kaushal"));
		set.add(new Employee(2, "kanishk"));
		set.add(new Employee(1, "kaushal"));
		Iterator<Employee> iterator = set.iterator();
		while(iterator.hasNext()) {
			Employee emp = iterator.next();
			System.out.println("Employee: "+emp);
		}
	}
}
