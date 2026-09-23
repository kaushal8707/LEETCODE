package ai.ml.leetcode.programming_questions.arrays;

public class FindingMultipleMissingNumberInSortedArray 
{
	public static void main(String[] args) 
	{

		int arr[]= {6,7,8,9,11,12,15,16,17,18,19};
		int l=arr[0];
		int diff=l-0;
		for(int i=0;i<arr.length;i++) {
			if((arr[i]-i)!=diff) {
				while(diff<arr[i]-i) {
					System.out.print(i+diff+"  ");
					diff++;
				}
			}
		}
}
}
