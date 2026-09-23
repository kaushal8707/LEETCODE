package ai.ml.leetcode.Recursion;

public class Factorial_Recursion {

	static int fact(int n)
	{
		 if(n==0)
			 return 1;
		 else
			 return fact(n-1)*n;
	}
	
	public static void main(String[] args) {
		 int r=fact(5);
		 System.out.println(r); 
	}
}
