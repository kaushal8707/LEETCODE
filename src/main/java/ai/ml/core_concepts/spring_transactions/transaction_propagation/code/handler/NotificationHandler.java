package ai.ml.core_concepts.spring_transactions.transaction_propagation.code.handler;
import ai.ml.core_concepts.spring_transactions.transaction_propagation.code.entity.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationHandler {

    @Transactional(propagation = Propagation.NEVER)
    public void sendOrderConfirmationNotification(Order order) {

        //send mail for order confirmation
        System.out.println("Order has been placed!!");
    }

}
