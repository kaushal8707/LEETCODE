package ai.ml.core_concepts.java_8_api.Practise_Set_MiscellaneousExamples.models;

public class Product {
    int id;
    String name;
    String productType;
    int quantity;
    double price;
    String supplierName;
//    public Product(int id, String name, String productType, int quantity, double price, String supplierName) {
//        this.id = id;
//        this.name = name;
//        this.productType = productType;
//        this.quantity = quantity;
//        this.price = price;
//        this.supplierName = supplierName;
//    }

    public Product() {
    }

    public Product(InventoryProducts inventoryProducts) {
        this.id=inventoryProducts.id;
        this.productType=inventoryProducts.productType;
        this.name=inventoryProducts.name;
        this.price=inventoryProducts.price;
        this.quantity=inventoryProducts.quantity;
        this.supplierName=inventoryProducts.supplierName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }
}
