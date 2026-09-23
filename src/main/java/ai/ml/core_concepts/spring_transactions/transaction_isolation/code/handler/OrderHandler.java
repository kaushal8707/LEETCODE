package ai.ml.core_concepts.spring_transactions.transaction_isolation.code.handler;//package com.zero.to.hero.springtransaction.transaction_isolation.code.handler;
//
//import com.zero.to.hero.springtransaction.transaction_isolation.code.entity.Order;
//import com.zero.to.hero.springtransaction.transaction_isolation.code.repository.OrderRepository;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Propagation;
//import org.springframework.transaction.annotation.Transactional;
//
//@Service
//public class OrderHandler {
//
//    private final OrderRepository orderRepository;
//
//    public OrderHandler(OrderRepository orderRepository) {
//        this.orderRepository = orderRepository;
//    }
//
//    @Transactional(propagation = Propagation.REQUIRED)
//    public Order saveOrder(Order order) {
//
//        return orderRepository.save(order);
//    }
//}
