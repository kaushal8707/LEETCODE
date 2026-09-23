package ai.ml.leetcode.Recursion;

public class TaylorSeries_Recursion {
	//here p for power value storing and f for factorial value storing
	static double p=1,f=1;

	static double e(int x, int n)
	{
		double r;
		 if(n==0) {
			 return 1;
		 }else {
			 r=e(x,n-1);
			 p=p*x;
			 f=f*n;
			 return r+p/f;
		 }
	}
	public static void main(String[] args) {
		System.out.println(e(4,10)); 
	}
}
