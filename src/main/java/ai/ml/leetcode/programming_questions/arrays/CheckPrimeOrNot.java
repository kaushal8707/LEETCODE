package ai.ml.leetcode.programming_questions.arrays;

public class CheckPrimeOrNot {

	public static void main(String[] args) {
		int num=4;
        for(int i=1; i<=50; i++) {
            boolean flag = false;
            for (int j = 2; j <= num / 2; j++) {
                if (i % j == 0) {
                    flag = true;
                }
            }
            if(!flag){
                System.out.println(i);
            }
        }
	}
}


