package ai.ml.leetcode.Recursion;

public class LocalVariable_in_Recursion {
	 static int fun(int n) {
		if(n>0) {
			return fun(n-1)+n;
		}
		return 0;
	}
	 
	 
	public static void main(String[] args) {
		int a = 5;
		 System.out.println(fun(a));
	}
}
