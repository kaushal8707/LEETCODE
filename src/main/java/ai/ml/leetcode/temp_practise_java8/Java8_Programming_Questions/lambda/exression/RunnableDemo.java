package ai.ml.leetcode.temp_practise_java8.Java8_Programming_Questions.lambda.exression;

public class RunnableDemo {

	public static void main(String[] args) {
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				System.out.println("run method"+"--"+Thread.currentThread().getName());
			}
		};
		new Thread(runnable).start();
		
		Runnable runnable1 = 
				()->System.out.println("run method"+"--"+Thread.currentThread().getName());		
		runnable1.run();
	}

}
