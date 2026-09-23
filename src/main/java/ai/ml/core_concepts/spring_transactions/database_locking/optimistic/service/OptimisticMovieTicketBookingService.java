package ai.ml.core_concepts.spring_transactions.database_locking.optimistic.service;
import ai.ml.core_concepts.spring_transactions.database_locking.entity.Seat;
import ai.ml.core_concepts.spring_transactions.database_locking.repository.SeatRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OptimisticMovieTicketBookingService {

    private final SeatRepository seatRepository;

    public OptimisticMovieTicketBookingService(SeatRepository seatRepository) {
        this.seatRepository = seatRepository;
    }

    @Transactional
    public Seat bookSeat(Long seatId) {
        //fetch the existing seat by id
        Seat seat =  seatRepository.findById(seatId)
                .orElseThrow(() -> new RuntimeException("Seat not found with id " + seatId));

        System.out.println(Thread.currentThread().getName() + " fetched seat with version " + seat.getVersion());

        if (seat.isBooked()) {
            throw new RuntimeException("Seat already booked !");
        }
        //booking seat
        seat.setBooked(true);
        //version check will occur here
        return seatRepository.save(seat);
    }
}
