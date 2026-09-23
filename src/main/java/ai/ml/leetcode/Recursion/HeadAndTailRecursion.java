package ai.ml.leetcode.Recursion;

public class HeadAndTailRecursion {
	public static void fun(int x) {
		if(x>0) {
			fun(x-1);
			System.out.print(x+" ");
			fun(x-1);
		}
	}
	
	public static void main(String[] args) {

		int x=3;
		fun(x);
	}
}
