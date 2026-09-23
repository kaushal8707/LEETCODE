package ai.ml.leetcode.programming_questions.arrays;

//insert at position 5;
//Steps
//First get the element to be inserted, say x
//Then get the position at which this element is to be inserted, say pos
//Create a new array with the size one greater than the previous size
//Copy all the elements from previous array into the new array till the position pos
//Insert the element x at position pos
//Insert the rest of the elements from the previous array into the new array after the pos

public class InsertAtPosition {
	public static void main(String[] args) {
		int arr[]= {5,2,9,3,6,4,1,8,7};
		int modifiedArr[]=new int[arr.length+1];
		int pos=5;   // position to be inserted
		int x=50;    // value to be inserted
		
		for(int i=0;i<arr.length;i++) {
			if(i<pos-1) {
				modifiedArr[i]=arr[i];
			}else if(i==pos-1) {
				modifiedArr[i]=x;
			}else {
				modifiedArr[i+1]=arr[i];
			}
		}
		
		for(int k: modifiedArr) {
			System.out.print(k+" ");
		}
	}

}
