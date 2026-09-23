package ai.ml.leetcode.Recursion;

public class StaticVariable_in_Recursion {
	static int x=0;
	 static int fun(int n) {
		if(n>0) {
			x++;
			return fun(n-1)+x;
		}
		return 0;
	}
	 
	 
	public static void main(String[] args) {
		int a = 5;
		 System.out.println(fun(a));
	}
}
