package com.example.ecommerce_solid.dto;

import com.example.ecommerce_solid.model.OrderStatus;

import jakarta.validation.constraints.NotNull;

public class OrderStatusRequest {

    @NotNull(message = "Status is required")
    private OrderStatus status;

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }
}