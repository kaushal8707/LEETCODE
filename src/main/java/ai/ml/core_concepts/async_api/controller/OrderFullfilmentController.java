package ai.ml.core_concepts.async_api.controller;
import ai.ml.core_concepts.async_api.model.Order;
import ai.ml.core_concepts.async_api.service.OrderFulfillmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderFullfilmentController {
    private final OrderFulfillmentService service;

    @PostMapping
    public ResponseEntity<Order> processOrder(@RequestBody Order order) throws InterruptedException {
        service.processOrder(order);  // synchronous
        service.notifyUser(order);   // asynchronous
        service.assignVendor(order);  // asynchronous
        service.packaging(order);  // asynchronous
        service.assignDeliveryPartner(order);  // asynchronous
        service.assignTrailerAndDispatch(order);  // asynchronous
        return ResponseEntity.ok(order);
    }
}
