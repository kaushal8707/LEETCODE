package ai.ml.leetcode.Recursion;

public class HeadRecursion {

	public static void fun(int x) {
		if(x>0) {
			fun(x-1);
			System.out.print(x+" ");
		}
	}
	
	public static void main(String[] args) {

		int x=3;
		fun(x);
	}
}
