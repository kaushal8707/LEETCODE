package ai.ml.leetcode.completablefuture.combining;

import ai.ml.leetcode.completablefuture.combining.database.EmployeeDatabase;
import ai.ml.leetcode.completablefuture.combining.dto.Employee;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class CombineCompletableFutureDemo {
    public CompletableFuture<Employee> getEmployeeDetails(){
        return CompletableFuture.supplyAsync(()->{
            System.out.println("getEmployeeDetails() "+Thread.currentThread().getName());
            return EmployeeDatabase.fetchEmployee()
                    .stream()
                    .filter(emp-> "79-021-3776".equals(emp.getEmployeeId()))
                    .findAny().orElse(null);
        });
    }

    public CompletableFuture<Integer> getRatings(Employee employee){
        return CompletableFuture.supplyAsync(()->{
            System.out.println("getRatings() "+Thread.currentThread().getName());
            return employee.getRating();
        });
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        /** combine dependent future (2) -> thenCompose */
        //employee -> ratings
        CombineCompletableFutureDemo cf = new CombineCompletableFutureDemo();
        CompletableFuture<Integer> thenComposeResult = cf.getEmployeeDetails().thenCompose(cf::getRatings);
        System.out.println("ratings: "+thenComposeResult.get());
        System.out.println("----------------------------------------------------");
        /** combine independent future (2) -> thenCombine  */
        //1-  group employee based on gender and counts
        CompletableFuture<Map<String, Long>> mapCompletableFuture = CompletableFuture.supplyAsync(() -> {
            return EmployeeDatabase.fetchEmployee()
                    .stream()
                    .collect(Collectors.groupingBy(Employee::getGender, Collectors.counting()));
        });
        //2- get all emails
        CompletableFuture<List<String>> emailsFuture = CompletableFuture.supplyAsync(() -> {
            return EmployeeDatabase.fetchEmployee()
                    .stream()
                    .map(Employee::getEmail)
                    .collect(Collectors.toList());
        });
        CompletableFuture<String> thenCombineResults = mapCompletableFuture.thenCombine(emailsFuture, (empMap, emails) -> empMap + " - " + emails);
        System.out.println(thenCombineResults.get());

        //combine multiple independent future (more than 2) -> allOf(n task)    -it will wait for all tasks to complete
        //-> weatherDetailsAPI
        //-> news apis
        //-> stockPrice apis
        //Example - MultiApiDataFetcher


        //combine multiple independent future (more than 2) -> anyOf(n task) (no need to wait for all)
        //Location -> X Y -> result
        //stock price -> x ,y , z ->
        //Example - StockPriceDataFetcher


    }
}
