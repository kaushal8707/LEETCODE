package ai.ml.leetcode.programming_questions.arrays;

public class CountingDuplicatesInSortedArray {

	public static void main(String[] args) {
		int arr[]= {3,6,8,8,10,12,15,15,15,20};
		for(int i=0;i<arr.length-1;i++) {
			if(arr[i]==arr[i+1]){
				int count=i+1;
				while(arr[count]==arr[i]) {
					count++;
				}
				System.out.println(arr[i]+" is appearing times ="+(count-i));
                i=count-1;
			}
		}
	}

}
