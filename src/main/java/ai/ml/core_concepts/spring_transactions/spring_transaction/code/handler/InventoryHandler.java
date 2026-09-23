package ai.ml.core_concepts.spring_transactions.spring_transaction.code.handler;//package com.zero.to.hero.springtransaction.spring_transaction.code.handler;
import com.zero.to.hero.springtransaction.spring_transaction.code.entity.Product;
import com.zero.to.hero.springtransaction.spring_transaction.code.repository.InventoryRepository;
import org.springframework.stereotype.Service;

@Service
public class InventoryHandler {

    private final InventoryRepository inventoryRepository;

    public InventoryHandler(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    public Product updateProductDetails(Product product) {

        /** happy scenarios just comment below conditional check to break the flow or transaction checkjust uncomment it out and
         * do we want to throw an error while update product details after order detail table insertion */
        // forcefully throwing an exception to simulate use of tx
        /** if there is any issue with inventory product table then you must have to revert or
         * rollback changes from order table as well -> to prevent use Transaction- > @Transactional
         */
        if(product.getPrice() > 5000) {
            throw new RuntimeException("DB Crashed");
        }
        return inventoryRepository.save(product);
    }

    public Product getProduct(long id) {
        return inventoryRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("Product not available with id - "+id));
    }
}
