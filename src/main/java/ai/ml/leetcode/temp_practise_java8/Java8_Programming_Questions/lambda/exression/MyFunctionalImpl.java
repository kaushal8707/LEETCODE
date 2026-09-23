package ai.ml.leetcode.temp_practise_java8.Java8_Programming_Questions.lambda.exression;

public class MyFunctionalImpl {

	public static void main(String[] args) {

		MyFunctionalInterface mfi = 
							()-> System.out.println("Inside method m1");
		mfi.m1();
		mfi.m2();
		MyFunctionalInterface.m4();
	}

}
