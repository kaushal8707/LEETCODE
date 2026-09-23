package ai.ml.core_concepts.java_8_api.Practice_Set__Collectors;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

//Method Reference vs Constructor Reference
public class PractiseSet_4 {
    public static void main(String[] args) {
        List<String> list = Arrays.asList("Item1", "Item3", "Item5", "Item2", "Item4");
        List<Shop> shopList = list.stream()
                .map(Shop::new)
                .collect(Collectors.toUnmodifiableList());
        System.out.println(shopList);


        //sum of all products value available whose price is less than 500
        List<Product> products = Arrays.asList(new Product("Prd1", 123),
                new Product("Prd2", 514),
                new Product("Prd3", 881),
                new Product("Prd4", 11));
        long sum = products.stream()
                .filter(p-> p.productValue < 500)
                .mapToInt(p -> p.productValue)
                .summaryStatistics()
                .getSum();
        System.out.println(sum);
    }
}

class Shop
{
    String item;

    public Shop(String item) {
        this.item = item;
    }

    @Override
    public String toString() {
        return "Shop{" +
                "item='" + item + '\'' +
                '}';
    }
}

class Product{
    String productName;
    int productValue;
    Product(String productName, int productValue){
        this.productName=productName;
        this.productValue=productValue;
    }
}