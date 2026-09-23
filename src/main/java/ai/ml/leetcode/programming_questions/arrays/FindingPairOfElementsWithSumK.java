package ai.ml.leetcode.programming_questions.arrays;
//this code is almost same like checking duplicate elements
// i for pointing and j for scanning and tracing

public class FindingPairOfElementsWithSumK
{
	public static void main(String[] args) 
	{
		int arr[]= {6,3,8,10,16,7,5,2,9,14};
		int k=10;
		
		for(int i=0;i<arr.length-1;i++) {
			for(int j=i+1;j<arr.length;j++) {
				if(arr[i]+arr[j]==k) {
					System.out.println("("+arr[i]+","+arr[j]+")"+"="+k);
				}
			}
		}
	}
}
