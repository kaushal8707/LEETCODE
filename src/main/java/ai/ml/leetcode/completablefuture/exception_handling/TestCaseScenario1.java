package ai.ml.leetcode.completablefuture.exception_handling;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class TestCaseScenario1 {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        CompletableFuture.supplyAsync(()->{
            return "Hello World";
        }).thenApply(input->{
            if(input.startsWith("H")) {
               input =  "Hello New World";
            }
            return input;
        }).thenApply(inp->{
            check();
            return "I S Error";
        }).get();   // to start execution we must either have to give get() or join()
    }

    public static void check(){
        throw new RuntimeException("App is down");
    }
}
