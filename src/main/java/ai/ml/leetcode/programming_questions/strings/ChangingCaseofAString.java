package ai.ml.leetcode.programming_questions.strings;

public class ChangingCaseofAString {
	public static void main(String[] args) {
		String word="WELCOME";
		String word2="sentence";
		char arr[]=word2.toCharArray();
		
		for(int i=0;i<arr.length;i++) 
		{
			if(arr[i]>=65 && arr[i]<=90) {
				arr[i]+=32;
			}else if(arr[i]>=97 && arr[i]<=122) {
				arr[i]-=32;
			}
		}
		System.out.println(arr); 
	}
}
