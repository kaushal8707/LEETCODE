package ai.ml.leetcode.Recursion;

public class Power_Recursion {
	static int power(int m, int n) {
		if(n==0) {
			return 1;
		}else {
			return power(m,n-1)*m;
		}
	}
	
	public static void main(String[] args) {
		int r=power(2,9);
		 System.out.println(r);
	}
}
