package ai.ml.leetcode.programming_questions.arrays;
//always first element we need to take as a min and max then we need to scan with 2nd elements

public class FindingMaxAndMinInASingleScan {

	public static void main(String[] args) {
		int arr[]= {5,8,3,9,6,2,10,7,-1,4};
		int min=arr[0]; //5
		int max=arr[0]; //5
		
		for(int i=1;i<arr.length;i++) {
			if(arr[i]<min) {
				min=arr[i];
			}else if(arr[i]>max) {
				max=arr[i];
			}
		}
		
		System.out.println("Min = "+min);
		System.out.println("Max = "+max);

	}

}
