package com.shopmart.inventoryservice.service;

import com.shopmart.inventoryservice.entity.ProductInventory;
import com.shopmart.inventoryservice.repository.ProductInventoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class InventoryService {

    private static final Logger log = LoggerFactory.getLogger(InventoryService.class);
    private final ProductInventoryRepository repository;

    public InventoryService(ProductInventoryRepository repository) {
        this.repository = repository;
    }

    @Cacheable(value = "product_inventory", key = "#productId", unless = "#result == null")
    public ProductInventory getProductInventory(String productId) {
        log.info("--> [DB QUERY] Fetching inventory for productId: {} from Database", productId);
        return repository.findByProductId(productId).orElse(null);
    }

    @CachePut(value = "product_inventory", key = "#productInventory.productId")
    @Transactional
    public ProductInventory updateProductStock(ProductInventory productInventory) {
        log.info("--> [CACHE PUT] Updating inventory cache for productId: {}", productInventory.getProductId());
        return repository.save(productInventory);
    }

    @CacheEvict(value = "product_inventory", key = "#productId")
    public void evictCache(String productId) {
        log.info("--> [CACHE EVICT] Evicting inventory cache for productId: {}", productId);
    }

    @Transactional
    public boolean reserveStock(String productId, int quantity) {
        log.info("--> [SAGA STEP 1] Reserving stock for productId: {}, quantity: {}", productId, quantity);
        Optional<ProductInventory> optional = repository.findByProductId(productId);
        if (optional.isPresent()) {
            ProductInventory inv = optional.get();
            if (inv.getAvailableQuantity() >= quantity) {
                inv.setAvailableQuantity(inv.getAvailableQuantity() - quantity);
                inv.setReservedQuantity(inv.getReservedQuantity() + quantity);
                repository.save(inv);
                log.info("--> [SAGA STEP 1 SUCCESS] Reserved {} units of productId: {}. Remaining available: {}", 
                        quantity, productId, inv.getAvailableQuantity());
                return true;
            }
        }
        log.error("--> [SAGA STEP 1 FAILED] Insufficient stock for productId: {}", productId);
        return false;
    }

    @Transactional
    public void releaseStock(String productId, int quantity) {
        log.info("--> [SAGA COMPENSATING] Restoring stock for productId: {}, quantity: {}", productId, quantity);
        Optional<ProductInventory> optional = repository.findByProductId(productId);
        if (optional.isPresent()) {
            ProductInventory inv = optional.get();
            inv.setAvailableQuantity(inv.getAvailableQuantity() + quantity);
            inv.setReservedQuantity(Math.max(0, inv.getReservedQuantity() - quantity));
            repository.save(inv);
            log.info("--> [SAGA COMPENSATING SUCCESS] Released {} units back to available stock. New available: {}", 
                    quantity, inv.getAvailableQuantity());
        } else {
            log.warn("--> [SAGA COMPENSATING WARNING] ProductId: {} not found during compensation", productId);
        }
    }
}
