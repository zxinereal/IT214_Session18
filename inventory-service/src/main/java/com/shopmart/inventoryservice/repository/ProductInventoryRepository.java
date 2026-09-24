package com.shopmart.inventoryservice.repository;

import com.shopmart.inventoryservice.entity.ProductInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ProductInventoryRepository extends JpaRepository<ProductInventory, Long> {
    Optional<ProductInventory> findByProductId(String productId);
}
