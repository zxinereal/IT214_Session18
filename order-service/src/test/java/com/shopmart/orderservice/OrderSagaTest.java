package com.shopmart.orderservice;

import com.shopmart.orderservice.client.InventoryFeignClient;
import com.shopmart.orderservice.dto.ProductInventoryResponse;
import com.shopmart.orderservice.entity.Order;
import com.shopmart.orderservice.event.OrderEvent;
import com.shopmart.orderservice.kafka.OrderEventPublisher;
import com.shopmart.orderservice.repository.OrderRepository;
import com.shopmart.orderservice.service.OrderService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class OrderSagaTest {

    private OrderRepository orderRepository;
    private OrderEventPublisher eventPublisher;
    private OrderService orderService;
    private InventoryFeignClient inventoryFeignClient;

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepository.class);
        eventPublisher = mock(OrderEventPublisher.class);
        inventoryFeignClient = mock(InventoryFeignClient.class);

        orderService = new OrderService(orderRepository, eventPublisher);

        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(inventoryFeignClient.getProductStockFallback(anyString(), any())).thenCallRealMethod();
    }

    @Test
    @DisplayName("Test Order Creation Initiates Saga (PENDING state)")
    void testCreateOrderInitiatesSaga() {
        Order order = orderService.createOrder("P1001", 2, 2999.0, "CREDIT_CARD");

        Assertions.assertNotNull(order);
        Assertions.assertEquals("PENDING", order.getStatus());
        Assertions.assertEquals("P1001", order.getProductId());
        Assertions.assertEquals(2, order.getQuantity());

        verify(orderRepository, times(1)).save(any(Order.class));
        verify(eventPublisher, times(1)).sendOrderEvent(any(OrderEvent.class));
    }

    @Test
    @DisplayName("Test Saga Rollback: Payment Failed -> Order Status CANCELLED")
    void testSagaRollbackOnPaymentFailure() {
        String orderId = "ORD-TEST-123";
        Order existingOrder = new Order(orderId, "P1001", 2, 2999.0, "PENDING", "FAIL");

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(existingOrder));

        // Simulate receiving PAYMENT_FAILED compensation trigger
        orderService.updateOrderStatus(orderId, "CANCELLED");

        Assertions.assertEquals("CANCELLED", existingOrder.getStatus());
        verify(orderRepository, times(1)).save(existingOrder);
    }

    @Test
    @DisplayName("Test Saga Success: Payment Processed -> Order Status APPROVED")
    void testSagaSuccessOnPaymentProcessed() {
        String orderId = "ORD-TEST-456";
        Order existingOrder = new Order(orderId, "P1001", 1, 1200.0, "PENDING", "CREDIT_CARD");

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(existingOrder));

        orderService.updateOrderStatus(orderId, "APPROVED");

        Assertions.assertEquals("APPROVED", existingOrder.getStatus());
        verify(orderRepository, times(1)).save(existingOrder);
    }

    @Test
    @DisplayName("Test Circuit Breaker Fallback Logic for Inventory Client")
    void testInventoryCircuitBreakerFallback() {
        ProductInventoryResponse fallbackResponse = inventoryFeignClient.getProductStockFallback("P9999", new RuntimeException("Service Down"));

        Assertions.assertNotNull(fallbackResponse);
        Assertions.assertEquals(-1L, fallbackResponse.getId());
        Assertions.assertTrue(fallbackResponse.getProductName().contains("Fallback"));
    }
}
