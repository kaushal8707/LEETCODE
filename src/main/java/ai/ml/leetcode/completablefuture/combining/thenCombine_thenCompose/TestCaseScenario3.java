package ai.ml.leetcode.completablefuture.combining.thenCombine_thenCompose;
import ai.ml.leetcode.completablefuture.exception_handling.Inventory;

import java.util.concurrent.CompletableFuture;
// thenCombine() : you need to check how much is the inventory price difference from bangalore and hyderabad

public class TestCaseScenario3 {
    public static void main(String[] args) {     // then compose take asynchronous task it might take time hyd location
        getProductsFromBangalore()                // then compose take Function so better use thenCombine for 2 diff completable future
                .thenCombine(getProductsFromHyderabad(), (amtBang, amtHyd)->{
                    return amtHyd-amtBang;
                }).thenAccept(result -> {
                    System.out.println("Ans : "+result);
                }).join();
    }

    static CompletableFuture<Double> getProductsFromBangalore() {
        return CompletableFuture.supplyAsync(()->{
            return Inventory.getInventoryDetails()
                    .stream().filter(inv-> inv.getStoreAddress().equals("bangalore"))
                    .mapToDouble(Inventory::getPrice)
                    .sum();
        });
    }

    static CompletableFuture<Double> getProductsFromHyderabad() {
        return CompletableFuture.supplyAsync(()->{
            return Inventory.getInventoryDetails()
                    .stream().filter(inv-> inv.getStoreAddress().equals("hyderabad"))
                    .toList();
        }).thenApply(inventoryFromHyd-> {
                   return inventoryFromHyd.stream()
                           .mapToDouble(Inventory::getPrice)
                           .sum();
        });
    }
}
