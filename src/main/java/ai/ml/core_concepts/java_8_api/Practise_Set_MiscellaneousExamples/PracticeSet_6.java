package ai.ml.core_concepts.java_8_api.Practise_Set_MiscellaneousExamples;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zero.to.hero.java_8_api.Practise_Set_MiscellaneousExamples.models.InventoryProducts;
import com.zero.to.hero.java_8_api.Practise_Set_MiscellaneousExamples.models.Product;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Find All List of Products based on Product Types ( Electronics, Groceries, Kitchen & Home ) whose price lesser than 50K **/

public class PracticeSet_6 {
    public static void main(String[] args) throws JsonProcessingException {
        Map<String, List<Product>> groupedProductTypeBasedAndFilter = getListOfInventoryProducts().stream()
                .map(Product::new)    // here Inventory product need constructor injection in Product
                .filter(product -> product.getPrice() <= 50000 && product.getProductType() != null)
                .collect(Collectors.groupingBy(Product::getProductType));
        String groupedProductTypeBasedAndFilterAsString = new ObjectMapper().writeValueAsString(groupedProductTypeBasedAndFilter);
        System.out.println("grouped Product Type Based And Filter As String"+groupedProductTypeBasedAndFilterAsString);

    }

    private static List<InventoryProducts> getListOfInventoryProducts() {
        return Stream.of(new InventoryProducts(1,"Mixer Grinder","Kitchen & Home",1,6654.11,"SUPS"),
                        new InventoryProducts(2,"Mobile","Electronics",2,99126,"KUISP"),
                        new InventoryProducts(3,"Fruits","Groceries",2,88712.12,"MNPS"),
                        new InventoryProducts(4,"Juicer","Kitchen & Home",1,6654.11,"APSD"),
                        new InventoryProducts(5,"Laptop","Electronics",3,142126,"NNBAV"),
                        new InventoryProducts(7,"Charger","Electronics",5,992126,"OOAYT"),
                        new InventoryProducts(6,"Vegetables","Groceries",4,32712.12,"OIUA"))
                .collect(Collectors.toList());
    }
}
