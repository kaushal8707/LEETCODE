package ai.ml.leetcode.Recursion;

public class SumOfNaturalNumber_Iteration {
	static int sumOfNaturalNum(int n) {
	int i, s=0;
	for(i=1;i<=n;i++) {
		s=s+i;
	}
		return s;	
	}
	public static void main(String[] args) {
		int n = 7;
		System.out.println(sumOfNaturalNum(n));
	}
}
