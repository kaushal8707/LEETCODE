package ai.ml.core_concepts.spring_transactions.transaction_isolation.code.service;//package com.zero.to.hero.springtransaction.transaction_isolation.code.service;
//
//import com.zero.to.hero.springtransaction.transaction_isolation.code.entity.Product;
//import com.zero.to.hero.springtransaction.transaction_isolation.code.handler.*;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Isolation;
//import org.springframework.transaction.annotation.Propagation;
//import org.springframework.transaction.annotation.Transactional;
//import com.zero.to.hero.springtransaction.transaction_isolation.code.entity.Order;
//
//@Service
//public class OrderProcessingService {
//
//    private final InventoryHandler inventoryHandler;
//    private final OrderHandler orderHandler;
//    private final AuditLogHandler auditLogHandler;
//    private final PaymentValidatorHandler paymentValidatorHandler;
//    private final NotificationHandler notificationHandler;
//    private final ProductRecommendationHandler productRecommendationHandler;
//
//    public OrderProcessingService(InventoryHandler inventoryHandler, OrderHandler orderHandler,
//                                  AuditLogHandler auditLogHandler, PaymentValidatorHandler paymentValidatorHandler,
//                                  NotificationHandler notificationHandler, ProductRecommendationHandler productRecommendationHandler) {
//        this.inventoryHandler = inventoryHandler;
//        this.orderHandler = orderHandler;
//        this.auditLogHandler = auditLogHandler;
//        this.paymentValidatorHandler=paymentValidatorHandler;
//        this.notificationHandler=notificationHandler;
//        this.productRecommendationHandler=productRecommendationHandler;
//    }
//
//    /** In Transaction ISOLATION controls the visibility of changes made by one transaction to another transaction */
//    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT)
//    public Order placeAnOrder(Order order) {
//
//        //get product inventory
//        Product product = inventoryHandler.getProduct(order.getProductId());
//        //validate product availability < (5)
//        validateStockAvailability(order, product);
//        //update total price in order entity
//        double totalPrice = product.getPrice() * order.getQuantity();
//        order.setTotalPrice(totalPrice);
//        Order savedOrder=null;
//        try {
//            //save order
//            savedOrder = orderHandler.saveOrder(order);
//            //update stock in inventory
//            updateInventoryStock(order, product);
//            auditLogHandler.logAuditDetails(order, "order placement succeeded");
//        } catch (Exception ex) {
//            auditLogHandler.logAuditDetails(order, "order placement failed");
//        }
//
//        //  paymentValidatorHandler.validatePayments(order);
//        //  notificationHandler.sendOrderConfirmationNotification(order);
//        //  productRecommendationHandler.getRecommendation();
////        getCustomerDetails();
//        return savedOrder;
//    }
//
//    @Transactional(propagation = Propagation.SUPPORTS)
//    public void getCustomerDetails() {
//        System.out.println("Customer details fetched !!");
//    }
//
//    /**call this method after placeAnOrder is successfully completed */
//    public void processOrder(Order order){
//
//        //Step 1: Place an Order & update Inventory
//        Order Order  = placeAnOrder(order);
//
//        //step2: send notification(non-transactional)
//        notificationHandler.sendOrderConfirmationNotification(order);
//    }
//
//    private void validateStockAvailability(Order order, Product product) {
//        int availableProducts = product.getStockQuantity();
//        if(order.getQuantity() > availableProducts) {
//            throw new RuntimeException("Insufficient Stock !!");
//        }
//    }
//
//    private void updateInventoryStock(Order order, Product product) {
//        int availableStock = product.getStockQuantity()- order.getQuantity();
//        product.setStockQuantity(availableStock);
//        inventoryHandler.updateProductDetails(product);
//    }
//}
