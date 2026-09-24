package com.shopmart.paymentservice.controller;

import com.shopmart.paymentservice.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/simulate-failure")
    public ResponseEntity<String> toggleSimulateFailure(@RequestParam boolean fail) {
        paymentService.setSimulateFailure(fail);
        return ResponseEntity.ok("Payment failure simulation set to: " + fail);
    }

    @GetMapping("/status")
    public ResponseEntity<String> getStatus() {
        return ResponseEntity.ok("Payment service active. Simulate failure: " + paymentService.isSimulateFailure());
    }
}
