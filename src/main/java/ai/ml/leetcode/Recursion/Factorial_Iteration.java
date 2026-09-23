package ai.ml.leetcode.Recursion;

public class Factorial_Iteration {

	static int fact(int n)
	{
		int i=1, fact=1;
		for(i=1;i<=n;i++) {
			fact = fact*i;
		}
		return fact;
	}
	
	public static void main(String[] args) {
		 int r=fact(5);
		 System.out.println(r); 
	}
}
