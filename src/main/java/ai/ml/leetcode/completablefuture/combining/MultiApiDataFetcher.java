package ai.ml.leetcode.completablefuture.combining;

import java.util.concurrent.CompletableFuture;

/**
 *   combine multiple independent future (more than 2) -> allOf(n task)
 *   -it will wait for all tasks to complete
 */
public class MultiApiDataFetcher {
    public CompletableFuture<String> fetchWeatherData(){
        return CompletableFuture.supplyAsync(()->{
            simulateDelay(2000); // Simulate network delay
            return "Weather: Sunny, 25c";
        });
    }
    public CompletableFuture<String> fetchNewsHeadlines() {
        return CompletableFuture.supplyAsync(() -> {
            simulateDelay(63000); // Simulate network delay -> I have given 60 sec to see if rest all task done then also allOf will process after 60sec bcz this task takes 60 sec to process
            return "News: Java 23 Released!";
        });
    }
    public CompletableFuture<String> fetchStockPrices() {
        return CompletableFuture.supplyAsync(() -> {
            simulateDelay(1500); // Simulate network delay
            return "Stocks: AAPL - $150, GOOGL - $2800";
        });
    }

    private void simulateDelay(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void main(String[] args) {

        MultiApiDataFetcher fetcher=new MultiApiDataFetcher();
        //combine multiple independent future (more than 2) -> allOf(n task)
        //-> weatherDetailsAPI
        CompletableFuture<String> weatherFuture = fetcher.fetchNewsHeadlines();
        //-> news apis
        CompletableFuture<String> newsFuture = fetcher.fetchNewsHeadlines();
        //-> stockPrice apis
        CompletableFuture<String> stockPriceFuture = fetcher.fetchStockPrices();

        //wait for all future to complete
        CompletableFuture<Void> allFuture = CompletableFuture.allOf(weatherFuture, newsFuture, stockPriceFuture);
        System.out.println(allFuture);

        //process results after all future are completed
        allFuture.thenRun(()->{
            String weather = weatherFuture.join();   //Returns the result value when complete, or throws an (unchecked) exception if completed exceptionally
            String news = newsFuture.join();         //Returns the result value when complete, or throws an (unchecked) exception if completed exceptionally
            String stock = stockPriceFuture.join();  //Returns the result value when complete, or throws an (unchecked) exception if completed exceptionally
            System.out.println("Aggregated Data : ");
            System.out.println(weather);
            System.out.println(news);
            System.out.println(stock);
        }).join();    //Returns the result value when complete, or throws an (unchecked) exception if completed exceptionally.

    }
}
