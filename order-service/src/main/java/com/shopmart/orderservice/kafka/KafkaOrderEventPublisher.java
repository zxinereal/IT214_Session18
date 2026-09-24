package com.shopmart.orderservice.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopmart.orderservice.event.OrderEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaOrderEventPublisher implements OrderEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(KafkaOrderEventPublisher.class);
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public KafkaOrderEventPublisher(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void sendOrderEvent(OrderEvent event) {
        try {
            kafkaTemplate.send("order-events", event.getOrderId(), objectMapper.writeValueAsString(event));
            log.info("--> [KAFKA PRODUCED] Published event {} to 'order-events' for orderId: {}", event.getEventType(), event.getOrderId());
        } catch (Exception e) {
            log.error("Failed to publish event for orderId: {}", event.getOrderId(), e);
        }
    }
}
