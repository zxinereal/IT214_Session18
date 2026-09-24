package com.shopmart.orderservice.service;

import com.shopmart.orderservice.entity.Order;
import com.shopmart.orderservice.event.OrderEvent;
import com.shopmart.orderservice.kafka.OrderEventPublisher;
import com.shopmart.orderservice.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.UUID;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final OrderEventPublisher eventPublisher;
    private final Sinks.Many<OrderEvent> eventSink = Sinks.many().multicast().onBackpressureBuffer();

    public OrderService(OrderRepository orderRepository, OrderEventPublisher eventPublisher) {
        this.orderRepository = orderRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public Order createOrder(String productId, Integer quantity, Double totalAmount, String paymentMethod) {
        String orderId = "ORD-" + UUID.randomUUID().toString().substring(0, 8);
        Order order = new Order(orderId, productId, quantity, totalAmount, "PENDING", paymentMethod);
        Order savedOrder = orderRepository.save(order);

        log.info("--> [SAGA INITIATED] Order created with ID: {} in PENDING state", orderId);

        OrderEvent event = new OrderEvent(orderId, productId, quantity, totalAmount, "PENDING", "ORDER_CREATED", paymentMethod);
        emitEvent(event);

        if (eventPublisher != null) {
            eventPublisher.sendOrderEvent(event);
        }

        return savedOrder;
    }

    @Transactional
    public void updateOrderStatus(String orderId, String status) {
        orderRepository.findById(orderId).ifPresent(order -> {
            order.setStatus(status);
            orderRepository.save(order);
            log.info("--> [SAGA FINALIZED] Order ID: {} status updated to: {}", orderId, status);
        });
    }

    public Order getOrder(String orderId) {
        return orderRepository.findById(orderId).orElse(null);
    }

    public void emitEvent(OrderEvent event) {
        eventSink.tryEmitNext(event);
    }

    public Flux<OrderEvent> getEventStream() {
        return eventSink.asFlux();
    }
}
