package ai.ml.leetcode.completablefuture.exception_handling;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class TestCaseScenario2 {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        CompletableFuture.supplyAsync(()->{
            return "Hello World";
        }).thenApply(input->{
            if(input.startsWith("H")) {
               input =  "Hello New World";
                System.out.println("First manipulation done for "+input);
            }
            return input;
        }).thenApply(inp->{
            System.out.println("started doing Second manipulation for "+inp);
            check();
            return "I S Error";
        }).exceptionally(exception->{
            System.out.println("unable to do string manipulation");
            return "INTERNAL SERVER ERROR";
        }).thenAccept(output->{
            System.out.println("Output : "+output);
        }).get();   // to start execution we must either have to give get() or join()
    }

    public static void check(){
        throw new RuntimeException("App is down");
    }
}
