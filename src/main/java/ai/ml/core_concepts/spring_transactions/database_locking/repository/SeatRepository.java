package ai.ml.core_concepts.spring_transactions.database_locking.repository;

import ai.ml.core_concepts.spring_transactions.database_locking.entity.Seat;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SeatRepository extends CrudRepository<Seat, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Seat s WHERE s.id= :seatId")
    Seat findByIdAndLock(Long seatId);
}