package ai.ml.leetcode.programming_questions.arrays;

public class ReversingAnArray {

	public static void main(String[] args) {
		int arr[]= {5,2,9,3,6,4,1,8,7};
        int i=0;
        int j=arr.length-1;
		for(;i<arr.length/2; i++,j--){
            int temp = arr[i];
            arr[i] = arr[j];
            arr[j] = temp;
        }
		for(int m:arr) {
			System.out.print(m+" ");
		}

	}

}
