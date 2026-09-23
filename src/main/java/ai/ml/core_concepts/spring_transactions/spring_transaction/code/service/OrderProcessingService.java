package ai.ml.core_concepts.spring_transactions.spring_transaction.code.service;//package com.zero.to.hero.springtransaction.spring_transaction.code.service;
import ai.ml.core_concepts.spring_transactions.spring_transaction.code.entity.Order;
import ai.ml.core_concepts.spring_transactions.spring_transaction.code.entity.Product;
import ai.ml.core_concepts.spring_transactions.spring_transaction.code.handler.InventoryHandler;
import ai.ml.core_concepts.spring_transactions.spring_transaction.code.handler.OrderHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


@Service
public class OrderProcessingService {

    private final InventoryHandler inventoryHandler;
    private final OrderHandler orderHandler;

    public OrderProcessingService(InventoryHandler inventoryHandler, OrderHandler orderHandler) {
        this.inventoryHandler = inventoryHandler;
        this.orderHandler = orderHandler;
    }

    /** By Default readOnly is false */
    /** Propagation deals with How Multiple Transactions can communicate with each other */
    /** Isolation controls How one Transaction can see the changes made by another Transaction whether u want to read committed
     * or un-committed data */
    // Below is the default behavior
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
    public Order placeAnOrder(Order order) {

        //use-cases - steps to perform
        //get product inventory
        Product product = inventoryHandler.getProduct(order.getProductId());
        //validate product availability < (5)
        validateStockAvailability(order, product);
        //update total price in order entity
        double totalPrice = product.getPrice() * order.getQuantity();
        order.setTotalPrice(totalPrice);
        //save order
        Order savedOrder = orderHandler.saveOrder(order);
        //update stock in inventory
        updateInventoryStock(order, product);

        return savedOrder;
    }

    private void validateStockAvailability(Order order, Product product) {
        int availableProducts = product.getStockQuantity();
        if(order.getQuantity() > availableProducts) {
            throw new RuntimeException("Insufficient Stock !!");
        }
    }

    private void updateInventoryStock(Order order, Product product) {
        int availableStock = product.getStockQuantity()- order.getQuantity();
        product.setStockQuantity(availableStock);
        inventoryHandler.updateProductDetails(product);
    }
}
