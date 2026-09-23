package ai.ml.leetcode.programming_questions.strings;

public class CountingVowelsAndConsonentInString {

	public static void main(String[] args) {
		String sentence="How are you";
		char A[]=sentence.toCharArray();
		int vCount=0;
		int cCount=0;
		
		for(int i=0;i<A.length;i++) {
			if(A[i]=='a' || A[i]=='e' || A[i]=='i'|| A[i]=='o' || A[i]=='u' ||
					A[i]=='A' || A[i]=='E' || A[i]=='I'|| A[i]=='O' || A[i]=='U') {
				vCount++;
				
			}else if((A[i]>=65 && A[i]<=90) ||
						(A[i]>=90 && A[i]<=122)){
				cCount++;
			}
		}
		
		System.out.println("Count of Vowels = "+vCount);
		System.out.println("Count of Consonents = "+cCount);

	}

}
