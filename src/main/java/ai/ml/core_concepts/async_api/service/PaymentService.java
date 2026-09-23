package ai.ml.core_concepts.async_api.service;
import ai.ml.core_concepts.async_api.model.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PaymentService {
    public void processingPayment(Order order) throws InterruptedException {
        log.info("initiate payment for order {}: ", order.getProductId());
        //call actual payment gateway
        Thread.sleep(2000);
        log.info("completed payment for order {}", order.getProductId());
    }
}
