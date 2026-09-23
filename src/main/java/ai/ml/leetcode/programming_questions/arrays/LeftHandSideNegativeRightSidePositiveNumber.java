package ai.ml.leetcode.programming_questions.arrays;

public class LeftHandSideNegativeRightSidePositiveNumber {

	public static void main(String[] args) {

		int arr[]= {-2,-5,12,13,19,1,-10};
		int modifiedArr[]=new int[arr.length];

		//need to take 2 pointer to check both condition one
		int l=0,r=arr.length-1; // l for lest side tracing r for right side tracing
		for(int i=0;i<arr.length;i++) {
			if(arr[i]<0) {
				modifiedArr[l]=arr[i];
				l++;
			}else if(arr[i]>0) {
				modifiedArr[r]=arr[i];
				r--;
			}
		}
		for(int m: modifiedArr) {
			System.out.print(m+" ");
		}
	}

}
