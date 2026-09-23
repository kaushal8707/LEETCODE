package ai.ml.core_concepts.spring_transactions.transaction_isolation.code.service;//package com.zero.to.hero.springtransaction.transaction_isolation.code.service;
//
//import com.zero.to.hero.springtransaction.transaction_isolation.code.entity.Product;
//import com.zero.to.hero.springtransaction.transaction_isolation.code.repository.InventoryRepository;
//import jakarta.persistence.EntityManager;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//import org.springframework.transaction.annotation.Isolation;
//import org.springframework.transaction.annotation.Transactional;
//
//@Component
//public class ProductService {
//
//    // Hibernate might cache certain queries.
//    // Flush after each query using entityManager.flush().
//    @Autowired
//    private EntityManager entityManager;
//
//    @Autowired
//    private InventoryRepository inventoryRepository;
//
//    @Transactional(isolation = Isolation.SERIALIZABLE)
//    public void updateStock(int productId, int stock) throws InterruptedException {
//        // Retrieve the product and update its stock
//        Product product =inventoryRepository.findById(productId)
//                .orElseThrow(()-> new RuntimeException("product not found"));
//        product.setStockQuantity(stock);
//        inventoryRepository.save(product);     // Ensure the update is sent to the DB
//        entityManager.flush();
//        // Simulate a long-running transaction (does not commit yet)
//        System.out.println("Transaction A: Stock updated to " + stock);
//        Thread.sleep(5000);
//        System.out.println("Transaction A: Committed the update");
//
//        //2nd scenario simulate after transaction B read the data then transaction A rolling back their changes
//        //first test above scenarios then uncomment and test this below code scenarios and just comment the above sys statement of commit
////        System.out.println("Transaction A: Rolling back the update" );
////        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
//    }
//
//    // Transaction B: Read stock
//    @Transactional(isolation = Isolation.SERIALIZABLE)
//    public int checkStock(int productId) {
//        // Retrieve the product and read its stock (potentially dirty read)
//        Product product = inventoryRepository.findById(productId)
//                .orElseThrow(() -> new RuntimeException("Product not found"));
//        System.out.println("Transaction B: Read stock as " + product.getStockQuantity());
//        return product.getStockQuantity();
//    }
//
//
//    // Transaction B: Read stock multiple times
//    @Transactional(isolation = Isolation.REPEATABLE_READ)
//    public void fetchStock(int productId) {
//        // First read
//        Product product1 = inventoryRepository.findById(productId)
//                .orElseThrow(() -> new RuntimeException("Product not found"));
//        System.out.println("Transaction B: First read stock as " + product1.getStockQuantity());//50
//
//        // Simulate a delay to allow Transaction A to update the stock
//        try {
//            Thread.sleep(3000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
//        // Second read
//        Product product2 = inventoryRepository.findById(productId)
//                .orElseThrow(() -> new RuntimeException("Product not found"));
//        System.out.println("Transaction B: Second read stock as " + product2.getStockQuantity());//50
//    }
//
//}
