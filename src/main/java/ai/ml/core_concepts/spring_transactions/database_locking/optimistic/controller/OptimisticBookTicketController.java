package ai.ml.core_concepts.spring_transactions.database_locking.optimistic.controller;

import ai.ml.core_concepts.spring_transactions.database_locking.optimistic.service.OptimisticSeatBookingTestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/booking")
public class OptimisticBookTicketController {

    @Autowired
    private OptimisticSeatBookingTestService optimisticSeatBookingTestService;

    @GetMapping("/optimistic/{seatId}")
    public String testOptimistic(@PathVariable Long seatId) throws InterruptedException {
        optimisticSeatBookingTestService.testOptimisticLocking(seatId);
        return "Optimistic locking test started! Check logs for results.";
    }
}
