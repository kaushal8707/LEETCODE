package ai.ml.leetcode.completablefuture.exception_handling;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class ExceptionHandlingUsecase4 {
    public static void main(String[] args) throws ExecutionException, InterruptedException {

//        CompletableFuture.supplyAsync(() -> {
//            // Code which might throw an exception
//            gracefullyShutDown("");
//            return "Some result";
//        }).thenApply(result -> {
//            return "processed result";
//        }).thenApply(result -> {
//            return "result after further processing";
//        }).thenAccept(result -> {
//            // do something with the final result
//        });

        // update employee DB

        CompletableFuture<String> employeeDataFuture = CompletableFuture
                .supplyAsync(() -> {
                    gracefullyShutDown("Employee");
                    return "Employee information update in DB";
                });

        CompletableFuture<String> employeeDocumentFuture = CompletableFuture
                .supplyAsync(() -> {
                    gracefullyShutDown("S3");
                    return "Employee document update in S3";
                });

        //flow 3

        //flow 4

        CompletableFuture<String> combineFuture = employeeDataFuture
                .thenCombine(employeeDocumentFuture, (result1, result2) -> {
                    return result1 + "\n" + result2;
                })
                //Global Exception Handling
                .handle((res, ex) -> {
                    if (ex != null) {
                        System.out.println("An error occurred during processing employee data " + ex.getMessage());
                        return "Operation Failed ! ";
                    }
                    return res;
                });


        System.out.println(combineFuture.get());

        //update employee document to S3


    }

    private static void gracefullyShutDown(String apiName) {
        throw new RuntimeException(apiName + " service temporarily unavailable. Please try again later.");
    }
}
