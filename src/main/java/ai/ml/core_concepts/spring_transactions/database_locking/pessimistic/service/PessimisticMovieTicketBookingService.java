package ai.ml.core_concepts.spring_transactions.database_locking.pessimistic.service;

import ai.ml.core_concepts.spring_transactions.database_locking.entity.Seat;
import ai.ml.core_concepts.spring_transactions.database_locking.repository.SeatRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PessimisticMovieTicketBookingService {

    private final SeatRepository seatRepository;

    public PessimisticMovieTicketBookingService(SeatRepository seatRepository) {
        this.seatRepository = seatRepository;
    }

    @Transactional
    public void bookSeatWithPessimistic(Long seatId) {

        System.out.println(Thread.currentThread().getName() + " is attempting to fetch the seat");

        //fetch the seat with Pessimistic lock
        Seat seat = seatRepository.findByIdAndLock(seatId);

        System.out.println(Thread.currentThread().getName() + " acquired the lock for seat id " + seatId);

        if (seat.isBooked()) {
            System.out.println(Thread.currentThread().getName() + " failed Seat Id " + seatId + " is already booked ");
            throw new RuntimeException("Seat already booked !");
        }
        //booking seat
        System.out.println(Thread.currentThread().getName() + " booking the seat " + seatId);

        seat.setBooked(true);
        //version check will occurs here
        seatRepository.save(seat);
        System.out.println(Thread.currentThread().getName() + " successfully book the seat with ID " + seatId);
    }
}
