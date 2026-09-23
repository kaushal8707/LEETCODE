package ai.ml.leetcode.collection_codes;
import java.util.Map;
import java.util.TreeMap;

//Now what happened already one entry exist key as a and value as a ghvz now whenever you will
//try to add the second entry or second object as a key what tree map internally do it will 
//simply check  “m”.compareTo(“a”). m is nothing just a second key compared to the first key 
//which is a. now this compareTo method will always return integer. now “m”.compareTo(“a”) if it
//is greater than then it will just add to the right side and it will return +positive one. 
//If it is less than then it will add to the left side. How it will know that is less than or greater
//than based on the return type either +ve or -ve. 
//So, it will just compare each object or each key if compareTo method return +1 
//then it will simply add in right side if it will return -1 then it will simply add in left side. So, this is how it will work.

public class TreeMapInternalWork {
	public static void main(String[] args) {
		Map<String, String> map = new TreeMap<>();
		map.put("a", "ghvz");
		map.put("m", "vgfr");
		map.put("p", "lopi");
		
		System.out.println("Tree Map = "+map);
		
		// "m".compareTo("a");
		// "p".compareTo("m");
		// "a", "m", "p"
		
		//>    ---->  right side(+1)
		//<    ---->  left  side(-1)
	}
}