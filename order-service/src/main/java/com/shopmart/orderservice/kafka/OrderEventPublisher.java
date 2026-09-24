package com.shopmart.orderservice.kafka;

import com.shopmart.orderservice.event.OrderEvent;

public interface OrderEventPublisher {
    void sendOrderEvent(OrderEvent event);
}
