package ai.ml.leetcode.programming_questions.arrays;

public class FindingMissingNumberInUnSortedArray {

	public static void main(String[] args) {
		int arr[]= {3,7,4,9,6,1,11,2,10};
        int min_num=1;
        int max_num=11;
        int ca[]=new int[max_num+1];
        for(int i=0; i<arr.length; i++){
            ca[arr[i]]++;
        }
        for(int i=min_num; i<ca.length; i++){
            if(ca[i]==0){
                System.out.println(i);
            }
        }
	}
}
