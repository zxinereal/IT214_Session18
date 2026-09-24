package com.shopmart.paymentservice.service;

import com.shopmart.paymentservice.entity.Payment;
import com.shopmart.paymentservice.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);
    private final PaymentRepository repository;
    private boolean globalSimulateFailure = false;

    public PaymentService(PaymentRepository repository) {
        this.repository = repository;
    }

    public void setSimulateFailure(boolean simulate) {
        this.globalSimulateFailure = simulate;
        log.info("--> Payment globalSimulateFailure flag set to: {}", simulate);
    }

    public boolean isSimulateFailure() {
        return globalSimulateFailure;
    }

    @Transactional
    public boolean processPayment(String orderId, Double amount, String paymentMethod) {
        log.info("--> [SAGA STEP 2] Processing payment for orderId: {}, amount: {}, method: {}", orderId, amount, paymentMethod);

        if (globalSimulateFailure || "FAIL".equalsIgnoreCase(paymentMethod) || (amount != null && amount < 0)) {
            log.error("--> [SAGA STEP 2 FAILED] Simulated payment failure for orderId: {}", orderId);
            Payment payment = new Payment(orderId, amount, "FAILED", paymentMethod);
            repository.save(payment);
            return false;
        }

        Payment payment = new Payment(orderId, amount, "SUCCESS", paymentMethod);
        repository.save(payment);
        log.info("--> [SAGA STEP 2 SUCCESS] Payment processed successfully for orderId: {}", orderId);
        return true;
    }
}
