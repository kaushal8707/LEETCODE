package ai.ml.leetcode.collection_codes;
import java.util.ArrayList;

//	How Can I write Custom ArrayList where I don’t want to allow duplicate 


public class CustomArrayList extends ArrayList{

	@Override
	public boolean add(Object obj) {
		if(this.contains(obj)) {
			return true;
		}else {
			return super.add(obj);
		}
	}

	public static void main(String[] args) {
		CustomArrayList list = new CustomArrayList();
		list.add("a");
		list.add("a");
		list.add("b");
		System.out.println(list); 
	}
}
