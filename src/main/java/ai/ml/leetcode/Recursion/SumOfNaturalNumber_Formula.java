package ai.ml.leetcode.Recursion;

public class SumOfNaturalNumber_Formula {

	static int sumOfNaturalNum(int n) {
		int result = n*(n+1)/2; 
		return result;
	}
	public static void main(String[] args) {

		int n = 7;
		System.out.println(sumOfNaturalNum(n));
	}
}
