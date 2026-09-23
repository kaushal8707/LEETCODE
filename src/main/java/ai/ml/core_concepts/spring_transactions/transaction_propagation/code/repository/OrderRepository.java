package ai.ml.core_concepts.spring_transactions.transaction_propagation.code.repository;

import ai.ml.core_concepts.spring_transactions.transaction_propagation.code.entity.Order;
import org.springframework.data.repository.CrudRepository;

public interface OrderRepository extends CrudRepository<Order, Integer> {

}
