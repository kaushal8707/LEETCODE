package ai.ml.leetcode.programming_questions.strings;

public class CheckingPalindrome {

	public static void main(String[] args) {
		String str="azza";
		boolean flag=false;
		for(int i=0,j=str.length()-1;i<str.length()/2;i++,j--) {
			if(str.charAt(i)!=str.charAt(j)) {
				flag=true;
			}
		}
		if(flag) {
			System.out.println("Not P");
		}else {
			System.out.println("P");
		}
	}

}
