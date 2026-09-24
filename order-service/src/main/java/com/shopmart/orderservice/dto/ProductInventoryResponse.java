package com.shopmart.orderservice.dto;

public class ProductInventoryResponse {
    private Long id;
    private String productId;
    private String productName;
    private Integer availableQuantity;
    private Integer reservedQuantity;

    public ProductInventoryResponse() {}

    public ProductInventoryResponse(Long id, String productId, String productName, Integer availableQuantity, Integer reservedQuantity) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.availableQuantity = availableQuantity;
        this.reservedQuantity = reservedQuantity;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public Integer getAvailableQuantity() { return availableQuantity; }
    public void setAvailableQuantity(Integer availableQuantity) { this.availableQuantity = availableQuantity; }

    public Integer getReservedQuantity() { return reservedQuantity; }
    public void setReservedQuantity(Integer reservedQuantity) { this.reservedQuantity = reservedQuantity; }
}
