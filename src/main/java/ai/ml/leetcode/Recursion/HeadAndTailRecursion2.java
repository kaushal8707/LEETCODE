package ai.ml.leetcode.Recursion;

public class HeadAndTailRecursion2 {
	public static void fun(int x) {
		if(x>0) {
			System.out.print(x+" ");
			fun(x-1);
			System.out.print(x+" ");
		}
	}
	
	public static void main(String[] args) {

		int x=3;
		fun(x);
	}
}
