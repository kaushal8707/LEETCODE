package ai.ml.core_concepts.spring_transactions.spring_transaction.code.handler;//package com.zero.to.hero.springtransaction.spring_transaction.code.handler;
import ai.ml.core_concepts.spring_transactions.spring_transaction.code.entity.Order;
import ai.ml.core_concepts.spring_transactions.spring_transaction.code.repository.OrderRepository;
import org.springframework.stereotype.Service;

@Service
public class OrderHandler {
    private final OrderRepository orderRepository;

    public OrderHandler(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }
    public Order saveOrder(Order order) {
        return orderRepository.save(order);
    }
}
