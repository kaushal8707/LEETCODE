package ai.ml.leetcode.programming_questions.strings;

// using a single loop we are going to use 2 pointer
// one from left hand side another from right hand side and 
// we will swap the elements till left side cursor is less than right side cursor

public class ReversingString_2
{
	public static void main(String[] args) 
	{
		String word="python";
		char A[]=word.toCharArray();
		
		for(int i=0,j=A.length-1;i<j;i++,j--) {
			char temp=A[i];
			A[i]=A[j];
			A[j]=temp;
		}
		
//		Using while loop
		
//		int i=0;
//		int j=A.length-1;
//		while(i<j) {
//			char temp=A[i];
//			A[i]=A[j];
//			A[j]=temp;
//			i++;
//			j--;
//		}
		System.out.println(A);
		
	}
}
