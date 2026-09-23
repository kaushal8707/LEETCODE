package ai.ml.core_concepts.spring_transactions.transaction_propagation.code.handler;
import ai.ml.core_concepts.spring_transactions.transaction_propagation.code.entity.Order;
import ai.ml.core_concepts.spring_transactions.transaction_propagation.code.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderHandler {

    private final OrderRepository orderRepository;

    public OrderHandler(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public Order saveOrder(Order order) {

        return orderRepository.save(order);
    }
}
