package ai.ml.core_concepts.spring_transactions.transaction_isolation.code.controller;//package com.zero.to.hero.springtransaction.transaction_isolation.code.controller;
//import com.zero.to.hero.springtransaction.transaction_isolation.code.service.OrderProcessingService;
//import com.zero.to.hero.springtransaction.transaction_isolation.code.service.isolation.ReadCommittedDemo;
//import com.zero.to.hero.springtransaction.transaction_isolation.code.service.isolation.ReadUncommittedDemo;
//import com.zero.to.hero.springtransaction.transaction_isolation.code.service.isolation.RepeatableReadDemo;
//import com.zero.to.hero.springtransaction.transaction_isolation.code.service.isolation.SerializableIsolationDemo;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//import com.zero.to.hero.springtransaction.transaction_isolation.code.entity.Order;
//
//@RestController
//@RequestMapping("/api/orders")
//public class OrderProcessingController {
//
//    private final OrderProcessingService orderProcessingService;
//    private final ReadUncommittedDemo readUncommittedDemo;
//    private final ReadCommittedDemo readCommittedDemo;
//    private final RepeatableReadDemo repeatableReadDemo;
//    private final SerializableIsolationDemo serializableIsolationDemo;
//
//
//    public OrderProcessingController(OrderProcessingService orderProcessingService,
//                                     ReadUncommittedDemo readUncommittedDemo,
//                                     ReadCommittedDemo readCommittedDemo,
//                                     RepeatableReadDemo repeatableReadDemo,
//                                     SerializableIsolationDemo serializableIsolationDemo) {
//        this.orderProcessingService = orderProcessingService;
//        this.readUncommittedDemo=readUncommittedDemo;
//        this.readCommittedDemo=readCommittedDemo;
//        this.repeatableReadDemo=repeatableReadDemo;
//        this.serializableIsolationDemo = serializableIsolationDemo;
//    }
//
//    @PostMapping
//    public ResponseEntity<?> placeOrder(Order order) {
//        return ResponseEntity.ok(orderProcessingService.placeAnOrder(order));
//    }
//
//    @GetMapping("/isolation")
//    public String testIsolation() throws InterruptedException {
//       // readUncommittedDemo.testReadUncommitted(5);
//       // readCommittedDemo.testReadCommitted(5);
//       // repeatableReadDemo.demonstrateRepeatableRead(5);
//        serializableIsolationDemo.testSerializableIsolation(5);
//        return "success";
//    }
//}
