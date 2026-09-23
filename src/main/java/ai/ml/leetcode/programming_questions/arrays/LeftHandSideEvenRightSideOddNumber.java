package ai.ml.leetcode.programming_questions.arrays;

public class LeftHandSideEvenRightSideOddNumber {

	public static void main(String[] args) {

		int arr[]= {1,2,3,4,5,6,7,8,9,10};
		int modifiedArr[]=new int[arr.length];
		int l=0,r=arr.length-1; // l for lest side tracing r for right side tracing
		for(int i=0;i<arr.length;i++) {
			if(arr[i]%2==0) {
				modifiedArr[l]=arr[i];
				l++;
			}else if(arr[i]%2!=0) {
				modifiedArr[r]=arr[i];
				r--;
			}
		}
		for(int m: modifiedArr) {
			System.out.print(m+" ");
		}
	}

}
