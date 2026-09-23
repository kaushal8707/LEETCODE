package ai.ml.core_concepts.spring_transactions.internal_working.code.repository;

import ai.ml.core_concepts.spring_transactions.internal_working.code.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {
}
