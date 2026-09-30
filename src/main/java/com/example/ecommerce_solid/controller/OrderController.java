package com.example.ecommerce_solid.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.example.ecommerce_solid.dto.OrderRequest;
import com.example.ecommerce_solid.dto.OrderResponse;
import com.example.ecommerce_solid.dto.OrderStatusRequest;
import com.example.ecommerce_solid.model.Order;
import com.example.ecommerce_solid.service.OrderService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // Create Order
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Order createOrder(
            @Valid @RequestBody OrderRequest request) {

        return orderService.createOrder(request);
    }

    // Get one order
    @GetMapping("/{id}")
    public OrderResponse getOrderById(
            @PathVariable Long id) {

        return orderService.getOrderById(id);
    }

    // Get all orders
    @GetMapping
    public List<OrderResponse> getAllOrders() {

        return orderService.getAllOrders();
    }

    // Get orders of a customer
    @GetMapping("/user/{userId}")
    public List<OrderResponse> getOrdersByUser(
            @PathVariable Long userId) {

        return orderService.getOrdersByUser(userId);
    }

    // Update order status
    @PutMapping("/{id}/status")
    public OrderResponse updateOrderStatus(
            @PathVariable Long id,
            @Valid @RequestBody OrderStatusRequest request) {

        return orderService.updateOrderStatus(
                id,
                request.getStatus());
    }
    @GetMapping("/my-orders")
    public List<OrderResponse> getMyOrders(
            Authentication authentication) {

        return orderService.getMyOrders(
                authentication.getName());
    }
}