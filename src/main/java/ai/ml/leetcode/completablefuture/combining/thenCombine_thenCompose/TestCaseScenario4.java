package ai.ml.leetcode.completablefuture.combining.thenCombine_thenCompose;
import ai.ml.leetcode.completablefuture.exception_handling.Inventory;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
// thenCompose()

public class TestCaseScenario4 {
    public static void main(String[] args) {
        getProductsFromBangalore()
                .thenCompose(TestCaseScenario4::getSumPrice)
                .thenAccept(res->{
                    System.out.println("Result : "+res);
                }).join();
    }

    static CompletableFuture<List<Inventory>> getProductsFromBangalore() {
        return CompletableFuture.supplyAsync(()->{
            return Inventory.getInventoryDetails()
                    .stream().filter(inv-> inv.getStoreAddress().equals("bangalore"))
                    .collect(Collectors.toList());
        });
    }

    static CompletableFuture<Double> getSumPrice(List<Inventory> inventory) {
        return CompletableFuture.supplyAsync(()->{
           return inventory. stream()
                   .mapToDouble(Inventory::getPrice)
                   .sum();
        });
    }
}
