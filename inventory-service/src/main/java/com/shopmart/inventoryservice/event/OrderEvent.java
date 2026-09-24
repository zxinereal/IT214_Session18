package com.shopmart.inventoryservice.event;

import java.io.Serializable;

public class OrderEvent implements Serializable {
    private String orderId;
    private String productId;
    private Integer quantity;
    private Double totalAmount;
    private String status;
    private String eventType;
    private String paymentMethod;

    public OrderEvent() {}

    public OrderEvent(String orderId, String productId, Integer quantity, Double totalAmount, String status, String eventType, String paymentMethod) {
        this.orderId = orderId;
        this.productId = productId;
        this.quantity = quantity;
        this.totalAmount = totalAmount;
        this.status = status;
        this.eventType = eventType;
        this.paymentMethod = paymentMethod;
    }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public Double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
}
