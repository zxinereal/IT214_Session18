package com.shopmart.orderservice.controller;

import com.shopmart.orderservice.client.InventoryFeignClient;
import com.shopmart.orderservice.dto.ProductInventoryResponse;
import com.shopmart.orderservice.entity.Order;
import com.shopmart.orderservice.event.OrderEvent;
import com.shopmart.orderservice.service.OrderService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.Map;

@RestController
@RequestMapping("/api/order")
public class OrderController {

    private final OrderService orderService;
    private final InventoryFeignClient inventoryFeignClient;

    public OrderController(OrderService orderService, InventoryFeignClient inventoryFeignClient) {
        this.orderService = orderService;
        this.inventoryFeignClient = inventoryFeignClient;
    }

    @PostMapping("/create")
    public ResponseEntity<Order> createOrder(@RequestBody Map<String, Object> payload) {
        String productId = (String) payload.get("productId");
        Integer quantity = Integer.parseInt(payload.get("quantity").toString());
        Double totalAmount = Double.parseDouble(payload.get("totalAmount").toString());
        String paymentMethod = payload.containsKey("paymentMethod") ? (String) payload.get("paymentMethod") : "CREDIT_CARD";

        Order order = orderService.createOrder(productId, quantity, totalAmount, paymentMethod);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<Order> getOrder(@PathVariable String orderId) {
        Order order = orderService.getOrder(orderId);
        if (order != null) {
            return ResponseEntity.ok(order);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/check-inventory/{productId}")
    public ResponseEntity<ProductInventoryResponse> checkInventory(@PathVariable String productId) {
        ProductInventoryResponse response = inventoryFeignClient.getProductStock(productId);
        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/events/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<OrderEvent> streamOrderEvents() {
        return orderService.getEventStream();
    }
}
