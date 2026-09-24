package com.shopmart.inventoryservice.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopmart.inventoryservice.event.OrderEvent;
import com.shopmart.inventoryservice.service.InventoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class InventoryKafkaListener {

    private static final Logger log = LoggerFactory.getLogger(InventoryKafkaListener.class);
    private final InventoryService inventoryService;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public InventoryKafkaListener(InventoryService inventoryService, KafkaTemplate<String, String> kafkaTemplate) {
        this.inventoryService = inventoryService;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "order-events", groupId = "inventory-group")
    public void handleOrderEvent(String message) {
        try {
            OrderEvent event = objectMapper.readValue(message, OrderEvent.class);
            log.info("--> [INVENTORY KAFKA CONSUME] Received event: {} for orderId: {}", event.getEventType(), event.getOrderId());

            if ("ORDER_CREATED".equals(event.getEventType())) {
                boolean reserved = inventoryService.reserveStock(event.getProductId(), event.getQuantity());
                if (reserved) {
                    event.setEventType("INVENTORY_RESERVED");
                    event.setStatus("INVENTORY_RESERVED");
                    log.info("--> [SAGA INVENTORY SUCCESS] Publishing INVENTORY_RESERVED event for orderId: {}", event.getOrderId());
                    kafkaTemplate.send("inventory-events", event.getOrderId(), objectMapper.writeValueAsString(event));
                } else {
                    event.setEventType("INVENTORY_FAILED");
                    event.setStatus("FAILED");
                    log.error("--> [SAGA INVENTORY FAILED] Publishing INVENTORY_FAILED event for orderId: {}", event.getOrderId());
                    kafkaTemplate.send("inventory-events", event.getOrderId(), objectMapper.writeValueAsString(event));
                }
            }
        } catch (Exception e) {
            log.error("Error processing order event in InventoryListener", e);
        }
    }

    @KafkaListener(topics = "payment-events", groupId = "inventory-group")
    public void handlePaymentEvent(String message) {
        try {
            OrderEvent event = objectMapper.readValue(message, OrderEvent.class);
            log.info("--> [INVENTORY KAFKA CONSUME] Received payment event: {} for orderId: {}", event.getEventType(), event.getOrderId());

            if ("PAYMENT_FAILED".equals(event.getEventType())) {
                log.warn("--> [SAGA COMPENSATING TRIGGERED] Payment failed for orderId: {}. Releasing inventory!", event.getOrderId());
                inventoryService.releaseStock(event.getProductId(), event.getQuantity());
            }
        } catch (Exception e) {
            log.error("Error processing payment event in InventoryListener", e);
        }
    }
}
