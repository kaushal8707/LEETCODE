package ai.ml.leetcode.completablefuture.practiceset_2;
import ai.ml.leetcode.completablefuture.database.EmployeeDataBase;
import ai.ml.leetcode.completablefuture.model.Employee;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class RunAsyncDemo {
    //anonymous implementation of Runnable
    public Void saveEmployees(File jsonFile) throws InterruptedException, ExecutionException {
        ObjectMapper mapper = new ObjectMapper();
        CompletableFuture<Void> runAsyncFuture = CompletableFuture.runAsync(new Runnable() {

            @Override
            public void run() {
                System.out.println("fetch employee records : " + Thread.currentThread().getName());
                List<Employee> employees = EmployeeDataBase.fetchEmployee();
                //write logic to save list of employee to database
                //repository.saveAll(employees)
                System.out.println("Thread : "+Thread.currentThread().getName());
                //employees.stream().forEach(System.out::println);
                System.out.println("Total Employees : "+employees.size());
            }
        });
        return runAsyncFuture.get();
    }

    //converted to lambda
    public Void saveEmployeesWithCustomExecutor(File jsonFile) throws InterruptedException, ExecutionException {
        ObjectMapper mapper = new ObjectMapper();
        Executor executor = Executors.newFixedThreadPool(5);
        CompletableFuture<Void> runAsyncFuture = CompletableFuture.runAsync(
                ()-> {
                    System.out.println("fetch employee records : " + Thread.currentThread().getName());
                    List<Employee> employees = EmployeeDataBase.fetchEmployee();
                    //write logic to save list of employee to database
                    //repository.saveAll(employees)
                    System.out.println("Thread : "+Thread.currentThread().getName());
                    System.out.println("Total Employees : "+employees.size());
                }, executor);
        runAsyncFuture.toCompletableFuture();
        return runAsyncFuture.get();
    }

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        RunAsyncDemo asyncDemo = new RunAsyncDemo();
        asyncDemo.saveEmployees(new File("employee.json"));
        asyncDemo.saveEmployeesWithCustomExecutor(new File("employee.json"));
    }
}
