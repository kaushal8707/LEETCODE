package ai.ml.core_concepts.spring_transactions.spring_transaction.code.controller;//package com.zero.to.hero.springtransaction.spring_transaction.code.controller;

import ai.ml.core_concepts.spring_transactions.spring_transaction.code.entity.Order;
import ai.ml.core_concepts.spring_transactions.spring_transaction.code.service.OrderProcessingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderProcessingController {

    private final OrderProcessingService orderProcessingService;

    public OrderProcessingController(OrderProcessingService orderProcessingService) {
        this.orderProcessingService = orderProcessingService;
    }

    @PostMapping
    public ResponseEntity<?> placeOrder(Order order) {
        return ResponseEntity.ok(orderProcessingService.placeAnOrder(order));
    }
}
