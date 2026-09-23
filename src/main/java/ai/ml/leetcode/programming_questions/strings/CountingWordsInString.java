package ai.ml.leetcode.programming_questions.strings;

public class CountingWordsInString {

	public static void main(String[] args) {
		String sentence="How are you";
		int sCount=0;			//space count so 1 space means 2 word, 2 space means 3 words
		
		for(int i=0;i<sentence.length();i++) {
			
			if(sentence.charAt(i)==' ') {
				sCount++;
			}
		}
		System.out.println("No. of words present "+(sCount+1));

	}

}
