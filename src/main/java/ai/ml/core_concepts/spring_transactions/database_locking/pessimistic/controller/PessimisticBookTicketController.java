package ai.ml.core_concepts.spring_transactions.database_locking.pessimistic.controller;

import ai.ml.core_concepts.spring_transactions.database_locking.pessimistic.service.PessimisticSeatBookingTestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/booking")
public class PessimisticBookTicketController {

    @Autowired
    private PessimisticSeatBookingTestService pessimisticSeatBookingTestService;

    @GetMapping("/pessimistic/{seatId}")
    public String testPessimistic(@PathVariable Long seatId) throws InterruptedException {
        pessimisticSeatBookingTestService.testPessimisticLocking(seatId);
        return "Pessimistic locking test started! Check logs for results.";
    }
}
