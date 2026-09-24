package com.shopmart.paymentservice.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopmart.paymentservice.event.OrderEvent;
import com.shopmart.paymentservice.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class PaymentKafkaListener {

    private static final Logger log = LoggerFactory.getLogger(PaymentKafkaListener.class);
    private final PaymentService paymentService;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public PaymentKafkaListener(PaymentService paymentService, KafkaTemplate<String, String> kafkaTemplate) {
        this.paymentService = paymentService;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "inventory-events", groupId = "payment-group")
    public void handleInventoryEvent(String message) {
        try {
            OrderEvent event = objectMapper.readValue(message, OrderEvent.class);
            log.info("--> [PAYMENT KAFKA CONSUME] Received inventory event: {} for orderId: {}", event.getEventType(), event.getOrderId());

            if ("INVENTORY_RESERVED".equals(event.getEventType())) {
                boolean success = paymentService.processPayment(event.getOrderId(), event.getTotalAmount(), event.getPaymentMethod());
                if (success) {
                    event.setEventType("PAYMENT_PROCESSED");
                    event.setStatus("APPROVED");
                    log.info("--> [SAGA PAYMENT SUCCESS] Publishing PAYMENT_PROCESSED event for orderId: {}", event.getOrderId());
                    kafkaTemplate.send("payment-events", event.getOrderId(), objectMapper.writeValueAsString(event));
                } else {
                    event.setEventType("PAYMENT_FAILED");
                    event.setStatus("FAILED");
                    log.error("--> [SAGA PAYMENT FAILED] Publishing PAYMENT_FAILED event for orderId: {}", event.getOrderId());
                    kafkaTemplate.send("payment-events", event.getOrderId(), objectMapper.writeValueAsString(event));
                }
            }
        } catch (Exception e) {
            log.error("Error processing inventory event in PaymentKafkaListener", e);
        }
    }
}
