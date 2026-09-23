package ai.ml.leetcode.completablefuture.practiceset_3;

import ai.ml.leetcode.completablefuture.database.EmployeeDataBase;
import ai.ml.leetcode.completablefuture.model.Employee;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class SupplyAsyncDemo {
	public List<Employee> getEmployees() throws InterruptedException, ExecutionException{
		Executor executor = Executors.newCachedThreadPool();
		CompletableFuture<List<Employee>> listCompletableFuture = CompletableFuture
				.supplyAsync(()->{
					System.out.println("Executed By : "+Thread.currentThread().getName());
		   return EmployeeDataBase.fetchEmployee();
		}, executor);
		
		return listCompletableFuture.get(); 
	}
	
	public static void main(String[] args) throws InterruptedException, ExecutionException {
		SupplyAsyncDemo supplyAsyncDemo = new SupplyAsyncDemo();
		List<Employee> employees = supplyAsyncDemo.getEmployees();
		employees.stream().forEach(System.out::println);
	}
}
