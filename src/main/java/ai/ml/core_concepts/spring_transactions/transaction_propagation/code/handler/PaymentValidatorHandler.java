package ai.ml.core_concepts.spring_transactions.transaction_propagation.code.handler;
import ai.ml.core_concepts.spring_transactions.transaction_propagation.code.entity.AuditLog;
import ai.ml.core_concepts.spring_transactions.transaction_propagation.code.entity.Order;
import ai.ml.core_concepts.spring_transactions.transaction_propagation.code.repository.AuditLogRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
public class PaymentValidatorHandler {

    private final AuditLogRepository auditLogRepository;

    public PaymentValidatorHandler(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional(propagation=Propagation.MANDATORY)
    public void validatePayments(Order order) {
        //assume payment processing happen here
        boolean paymentSuccessful = false;
        if(!paymentSuccessful) {

            AuditLog paymentFailureLog = new AuditLog();
            paymentFailureLog.setOrderId(Long.valueOf(order.getId()));
            paymentFailureLog.setAction("payment failed for order id - " + order.getId());
            paymentFailureLog.setTimeStamp(LocalDateTime.now());

            //save the payment failure log
            auditLogRepository.save(paymentFailureLog);
            if(order.getTotalPrice() > 5000) {
                throw new RuntimeException("DB Crashed");
            }

        }
    }
}
