package ai.ml.leetcode.programming_questions.arrays;

//delete from position 5;
//Steps


public class DeleteFromPosition {
	public static void main(String[] args) {
		int arr[]= {5,2,9,3,6,4,1,8,7};
		int modifiedArr[]=new int[arr.length-1];
		int pos=5;   // position from to be deleted
		
		for(int i=0;i<arr.length;i++) {
			if(i<pos-1) {
				modifiedArr[i]=arr[i];
			}else if(i>pos-1) {
				modifiedArr[i-1]=arr[i];
			}
		}
		for(int k: modifiedArr) {
			System.out.print(k+" ");
		}
		
	}

}
