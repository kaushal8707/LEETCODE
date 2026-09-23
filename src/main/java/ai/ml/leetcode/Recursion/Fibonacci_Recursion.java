package ai.ml.leetcode.Recursion;

public class Fibonacci_Recursion {
	static int febonacci(int n) {
		if(n<=1) {
			return n;
		}else {
			return febonacci(n-2)+febonacci(n-1);
		}
	}
	public static void main(String[] args) {
		System.out.println(febonacci(10));
	}
}
