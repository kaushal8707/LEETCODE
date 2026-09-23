package ai.ml.leetcode.programming_questions.arrays;

public class RemoveDuplicateFromUnSortedArray {
	public static void main(String[] args) {
		int arr[] = { 1, 2, 3, 4, 5, 4, 5, 6, 5, 6, 7, 8 ,2, 1};

		for(int i=0;i<arr.length-1;i++) {
			for(int j=i+1;j<arr.length;j++) {
				if(arr[i]==arr[j]) {
					arr[j]=-1;
				}
			}
		}
		for(int k:arr) {
			if(k!=-1) {
				System.out.print(k+" ");
			}
		}
		
	}
}
