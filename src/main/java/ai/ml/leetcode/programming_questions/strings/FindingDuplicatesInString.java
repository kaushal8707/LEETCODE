package ai.ml.leetcode.programming_questions.strings;

public class FindingDuplicatesInString {

	public static void main(String[] args) {
		String str="finding";
		char A[]=str.toCharArray();
		
		for(int i=0;i<A.length-1;i++) {
			int count=1;
			if(A[i]!=0) {
				for(int j=i+1;j<A.length;j++) {
					if(A[i]==A[j]) {
						count++;
						A[j]=0;
					}
				}
			}
			if(count>1) {
				System.out.println(A[i]+" comes at times : "+count);
			}
		}
	}
}
