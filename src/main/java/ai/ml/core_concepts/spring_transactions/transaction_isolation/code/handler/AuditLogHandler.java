package ai.ml.core_concepts.spring_transactions.transaction_isolation.code.handler;//package com.zero.to.hero.springtransaction.transaction_isolation.code.handler;
//
//import com.zero.to.hero.springtransaction.transaction_isolation.code.entity.AuditLog;
//import com.zero.to.hero.springtransaction.transaction_isolation.code.entity.Order;
//import com.zero.to.hero.springtransaction.transaction_isolation.code.repository.AuditLogRepository;
//import org.springframework.stereotype.Component;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Propagation;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDateTime;
//
//@Component
//public class AuditLogHandler {
//
//    private final AuditLogRepository auditLogRepository;
//
//    public AuditLogHandler(AuditLogRepository auditLogRepository) {
//        this.auditLogRepository = auditLogRepository;
//    }
//
//    /** log audit details ( runs in an independent transaction ) */
//    @Transactional(propagation = Propagation.REQUIRES_NEW)
//    public void logAuditDetails(Order order, String action) {
//
//        AuditLog auditLog=new AuditLog();
//        auditLog.setOrderId(Long.valueOf(order.getId()));
//        auditLog.setAction(action);
//        auditLog.setTimeStamp(LocalDateTime.now());
//
//        //save audit log
//        auditLogRepository.save(auditLog);
//    }
//}
