package ai.ml.core_concepts.spring_transactions.transaction_propagation.code.repository;
import ai.ml.core_concepts.spring_transactions.transaction_propagation.code.entity.AuditLog;
import org.springframework.data.repository.CrudRepository;

public interface AuditLogRepository extends CrudRepository<AuditLog, Long> {

}
