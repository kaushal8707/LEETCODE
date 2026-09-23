package ai.ml.leetcode.completablefuture.exception_handling;

import lombok.*;

import java.util.Arrays;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Data
@Setter@ToString
public class Inventory {
    String productName;
    double price;
    String storeAddress;

    public static List<Inventory> getInventoryDetails() {
        return Arrays.asList(new Inventory("mobile", 23142, "pune"),
                new Inventory("chair", 11142, "bangalore"),
                new Inventory("laptop", 65114.11, "hyderabad"),
                new Inventory("mouse", 312, "hyderabad"),
                new Inventory("headset", 1121, "bangalore"));
    }
}
