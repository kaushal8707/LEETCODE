package ai.ml.leetcode.programming_questions.arrays;

// comments
// since this is a sorted array so left hand side will be small elements and right hand side will be a greatest elements
// so we will take 2 pointer i and j, i at position 0 and j at position last
// we will add arr[i] and arr[j] if the sum is equals to 10 then we will increase i and decrease j both
// if the sum is greater than 10 decrease j
// if the sum is less than 10 then increase i

// since we are not incrementing i every time and not decrementing j every time so we can not use for loop
// bcz for loop will always to i++ and j-- so it will perform always but we don't want always..
// so to make it simple i ll use while loop

public class FindingPairOfElementsWithSumK_InSortedArray
{
	public static void main(String[] args) 
	{
		int arr[]= {1,3,4,5,6,8,9,10,12,14};
		int k=10;
		int i=0;
		int j=arr.length-1;
		
		while(i<j) {
			if(arr[i]+arr[j]==k) {
				System.out.println("("+arr[i]+","+arr[j]+")"+"="+k);
				i++;
				j--;
			}else if(arr[i]+arr[j]<k) {
				i++;
			}else if(arr[i]+arr[j]>k) {
				j--;
			}
		}
	}
}
