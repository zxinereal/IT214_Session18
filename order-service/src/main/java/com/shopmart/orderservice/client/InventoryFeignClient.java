package com.shopmart.orderservice.client;

import com.shopmart.orderservice.dto.ProductInventoryResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "inventory-service")
public interface InventoryFeignClient {

    Logger log = LoggerFactory.getLogger(InventoryFeignClient.class);

    @GetMapping("/api/inventory/product/{productId}")
    @CircuitBreaker(name = "inventoryService", fallbackMethod = "getProductStockFallback")
    ProductInventoryResponse getProductStock(@PathVariable("productId") String productId);

    default ProductInventoryResponse getProductStockFallback(String productId, Throwable throwable) {
        log.warn("--> [CIRCUIT BREAKER FALLBACK] inventory-service call failed for productId: {}. Reason: {}", 
                productId, throwable.getMessage());
        return new ProductInventoryResponse(-1L, productId, "Fallback Product (Inventory Service Unavailable)", 0, 0);
    }
}
