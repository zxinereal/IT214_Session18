package com.shopmart.inventoryservice;

import com.shopmart.inventoryservice.entity.ProductInventory;
import com.shopmart.inventoryservice.repository.ProductInventoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableDiscoveryClient
@EnableCaching
public class InventoryServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(InventoryServiceApplication.class, args);
    }

    @Bean
    public CommandLineRunner initData(ProductInventoryRepository repository) {
        return args -> {
            if (repository.findByProductId("P1001").isEmpty()) {
                repository.save(new ProductInventory("P1001", "iPhone 15 Pro", 50, 0));
            }
            if (repository.findByProductId("P1002").isEmpty()) {
                repository.save(new ProductInventory("P1002", "MacBook Pro M3", 20, 0));
            }
        };
    }
}
