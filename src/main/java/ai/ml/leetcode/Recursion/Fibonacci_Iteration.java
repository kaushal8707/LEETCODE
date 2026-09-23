package ai.ml.leetcode.Recursion;

public class Fibonacci_Iteration {
	static void febonacci(int n) {
		int a=0,c;
		int b=1;
		int count=n-2;
		System.out.print(a+" "+b+" ");
		for(int i=0;i<count;i++) {
			c=a+b;
			a=b;
			b=c;
			System.out.print(c+" ");
		}
	}
	public static void main(String[] args) {
		System.out.println("How many fibonacci number u want to print");
		febonacci(10); 
	}

}
