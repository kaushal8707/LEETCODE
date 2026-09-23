package ai.ml.leetcode.Recursion;//	sum(7) = 1+2+3+4+5+6+7
//  sum(n) = 1+2+3+4+......(n-1)+n
//  sum(n) = (n-1) + n
//  sum(n) = sum(n-1) + n

public class SumOfNaturalNumber_Recursive {
	static int sumOfNaturalNum(int n) {
		if(n==0) {
		   return 0;	
		}else {
			return sumOfNaturalNum(n-1)+n; 
		}
	}
	public static void main(String[] args) {
		int n = 7;
		System.out.println(sumOfNaturalNum(n));
	}
}
