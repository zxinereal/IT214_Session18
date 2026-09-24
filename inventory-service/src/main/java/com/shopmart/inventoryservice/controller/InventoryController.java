package com.shopmart.inventoryservice.controller;

import com.shopmart.inventoryservice.entity.ProductInventory;
import com.shopmart.inventoryservice.service.InventoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<ProductInventory> getProductStock(@PathVariable String productId) {
        ProductInventory inventory = inventoryService.getProductInventory(productId);
        if (inventory != null) {
            return ResponseEntity.ok(inventory);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/seed")
    public ResponseEntity<ProductInventory> seedProductStock(@RequestBody ProductInventory inventory) {
        ProductInventory saved = inventoryService.updateProductStock(inventory);
        return ResponseEntity.ok(saved);
    }

    @PostMapping("/evict/{productId}")
    public ResponseEntity<String> evictCache(@PathVariable String productId) {
        inventoryService.evictCache(productId);
        return ResponseEntity.ok("Cache evicted for productId: " + productId);
    }

    @PostMapping("/reserve")
    public ResponseEntity<Boolean> reserveStock(@RequestParam String productId, @RequestParam int quantity) {
        boolean success = inventoryService.reserveStock(productId, quantity);
        return ResponseEntity.ok(success);
    }
}
