package ai.ml.core_concepts.spring_transactions.transaction_propagation.code.handler;

import ai.ml.core_concepts.spring_transactions.transaction_propagation.code.entity.AuditLog;
import ai.ml.core_concepts.spring_transactions.transaction_propagation.code.entity.Order;
import ai.ml.core_concepts.spring_transactions.transaction_propagation.code.repository.AuditLogRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Component
public class AuditLogHandler {

    private final AuditLogRepository auditLogRepository;

    public AuditLogHandler(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    /** log audit details ( runs in an independent transaction ) */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAuditDetails(Order order, String action) {

        AuditLog auditLog=new AuditLog();
        auditLog.setOrderId(Long.valueOf(order.getId()));
        auditLog.setAction(action);
        auditLog.setTimeStamp(LocalDateTime.now());

        //save audit log
        auditLogRepository.save(auditLog);
    }
}
