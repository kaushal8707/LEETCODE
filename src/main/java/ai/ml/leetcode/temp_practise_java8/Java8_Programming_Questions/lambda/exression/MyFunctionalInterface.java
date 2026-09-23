package ai.ml.leetcode.temp_practise_java8.Java8_Programming_Questions.lambda.exression;

@FunctionalInterface
public interface MyFunctionalInterface {

	void m1();
	default void m2() {
		System.out.println("Inside method m2");
	}
	default void m3() {
		System.out.println("Inside method m3");
	}
	static void m4() {
		System.out.println("Inside method m4");
	}
}
