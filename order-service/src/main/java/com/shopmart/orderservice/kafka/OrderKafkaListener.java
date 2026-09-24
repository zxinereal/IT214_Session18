package com.shopmart.orderservice.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopmart.orderservice.event.OrderEvent;
import com.shopmart.orderservice.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderKafkaListener {

    private static final Logger log = LoggerFactory.getLogger(OrderKafkaListener.class);
    private final OrderService orderService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OrderKafkaListener(OrderService orderService) {
        this.orderService = orderService;
    }

    @KafkaListener(topics = "inventory-events", groupId = "order-group")
    public void handleInventoryEvent(String message) {
        try {
            OrderEvent event = objectMapper.readValue(message, OrderEvent.class);
            log.info("--> [ORDER KAFKA CONSUME] Inventory event: {} for orderId: {}", event.getEventType(), event.getOrderId());
            orderService.emitEvent(event);

            if ("INVENTORY_FAILED".equals(event.getEventType())) {
                log.error("--> [SAGA ROLLBACK COMPLETE] Inventory failed. Updating orderId: {} status to CANCELLED", event.getOrderId());
                orderService.updateOrderStatus(event.getOrderId(), "CANCELLED");
            }
        } catch (Exception e) {
            log.error("Error handling inventory event in OrderKafkaListener", e);
        }
    }

    @KafkaListener(topics = "payment-events", groupId = "order-group")
    public void handlePaymentEvent(String message) {
        try {
            OrderEvent event = objectMapper.readValue(message, OrderEvent.class);
            log.info("--> [ORDER KAFKA CONSUME] Payment event: {} for orderId: {}", event.getEventType(), event.getOrderId());
            orderService.emitEvent(event);

            if ("PAYMENT_PROCESSED".equals(event.getEventType())) {
                log.info("--> [SAGA COMPLETE APPROVED] Payment succeeded! Updating orderId: {} status to APPROVED", event.getOrderId());
                orderService.updateOrderStatus(event.getOrderId(), "APPROVED");
            } else if ("PAYMENT_FAILED".equals(event.getEventType())) {
                log.error("--> [SAGA ROLLBACK COMPLETE] Payment failed! Updating orderId: {} status to CANCELLED", event.getOrderId());
                orderService.updateOrderStatus(event.getOrderId(), "CANCELLED");
            }
        } catch (Exception e) {
            log.error("Error handling payment event in OrderKafkaListener", e);
        }
    }
}
