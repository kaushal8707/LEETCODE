package ai.ml.core_concepts.java_8_api.Practise_Set_MiscellaneousExamples.models;

public class InventoryProducts {
    int id;
    String name;
    String productType;
    int quantity;
    double price;
    String supplierName;

    public InventoryProducts(int id, String name, String productType, int quantity, double price, String supplierName) {
        this.id = id;
        this.name = name;
        this.productType = productType;
        this.quantity = quantity;
        this.price = price;
        this.supplierName = supplierName;
    }

    public InventoryProducts() {
    }
}