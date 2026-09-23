package ai.ml.core_concepts.spring_transactions.spring_transaction.code.repository;//package com.zero.to.hero.springtransaction.spring_transaction.code.repository;

import  com.zero.to.hero.springtransaction.spring_transaction.code.entity.Order;
import org.springframework.data.repository.CrudRepository;

public interface OrderRepository extends CrudRepository<Order, Integer> {

}
