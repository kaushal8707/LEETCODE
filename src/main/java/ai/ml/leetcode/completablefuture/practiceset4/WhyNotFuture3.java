package ai.ml.leetcode.completablefuture.practiceset4;

/** In case of exception occurred we can't hande the exception in future **/

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

public class WhyNotFuture3 {
	public static void main(String[] args) throws InterruptedException, ExecutionException  {
		ExecutorService service = Executors.newFixedThreadPool(10);
		Future<List<Integer>> future = service.submit(()->{
			//you are doing API call
			System.out.println("Thread : "+Thread.currentThread().getName()); 
			System.out.println(10/0); 
			return Arrays.asList(1,2,3,4,5);
		});
		
		List<Integer> integers = future.get();
		System.out.println(integers); 
		
		CompletableFuture<String> completableFuture=new CompletableFuture<>();
		completableFuture.get();
		completableFuture.complete("return some dummy data !!");
	}
}
