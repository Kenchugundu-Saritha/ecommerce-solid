package com.example.ecommerce_solid.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.ecommerce_solid.dto.OrderItemResponse;
import com.example.ecommerce_solid.dto.OrderRequest;
import com.example.ecommerce_solid.dto.OrderResponse;
import com.example.ecommerce_solid.exception.OrderNotFoundException;
import com.example.ecommerce_solid.exception.ProductNotFoundException;
import com.example.ecommerce_solid.model.Order;
import com.example.ecommerce_solid.model.OrderItem;
import com.example.ecommerce_solid.model.OrderStatus;
import com.example.ecommerce_solid.model.Product;
import com.example.ecommerce_solid.model.User;
import com.example.ecommerce_solid.repository.OrderRepository;
import com.example.ecommerce_solid.repository.ProductRepository;
import com.example.ecommerce_solid.repository.UserRepository;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public OrderService(
            OrderRepository orderRepository,
            ProductRepository productRepository,
            UserRepository userRepository) {

        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    // Create Order
    public Order createOrder(OrderRequest request) {

        Product product = productRepository.findById(
                request.getProductId()
        ).orElseThrow(() ->
                new ProductNotFoundException(
                        request.getProductId()));

        if (product.getQuantity() < request.getQuantity()) {
            throw new RuntimeException("Insufficient product stock");
        }

        Order order = new Order();

        OrderItem orderItem = new OrderItem();

        orderItem.setProduct(product);
        orderItem.setQuantity(request.getQuantity());
        orderItem.setPrice(product.getPrice());
        orderItem.setOrder(order);

        order.getItems().add(orderItem);

        double totalAmount =
                product.getPrice() * request.getQuantity();

        order.setTotalAmount(totalAmount);
        order.setStatus(OrderStatus.PLACED);

        product.setQuantity(
                product.getQuantity()
                        - request.getQuantity());

        productRepository.save(product);

        return orderRepository.save(order);
    }

    // Get order by ID
    public OrderResponse getOrderById(Long id) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() ->
                        new OrderNotFoundException(id));

        return convertToResponse(order);
    }

    // Get all orders
    public List<OrderResponse> getAllOrders() {

        List<Order> orders = orderRepository.findAll();

        return orders.stream()
                .map(this::convertToResponse)
                .toList();
    }

    // Get orders by customer
    public List<OrderResponse> getOrdersByUser(Long userId) {

        List<Order> orders =
                orderRepository.findByUserId(userId);

        return orders.stream()
                .map(this::convertToResponse)
                .toList();
    }

    // Update order status
    public OrderResponse updateOrderStatus(
            Long orderId,
            OrderStatus newStatus) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new OrderNotFoundException(orderId));

        OrderStatus currentStatus = order.getStatus();

        boolean validTransition =
                (currentStatus == OrderStatus.PLACED
                        && (newStatus == OrderStatus.CONFIRMED
                            || newStatus == OrderStatus.CANCELLED))

                || (currentStatus == OrderStatus.CONFIRMED
                        && newStatus == OrderStatus.SHIPPED)

                || (currentStatus == OrderStatus.SHIPPED
                        && newStatus == OrderStatus.DELIVERED);

        if (!validTransition) {
            throw new RuntimeException(
                    "Invalid order status transition from "
                    + currentStatus
                    + " to "
                    + newStatus);
        }

        order.setStatus(newStatus);

        Order savedOrder =
                orderRepository.save(order);

        return convertToResponse(savedOrder);
    }

    // Convert Entity → DTO
    private OrderResponse convertToResponse(Order order) {

        OrderResponse response = new OrderResponse();

        response.setId(order.getId());
        response.setTotalAmount(order.getTotalAmount());
        response.setStatus(order.getStatus());

        if (order.getUser() != null) {

            response.setUserId(
                    order.getUser().getId());

            response.setUserName(
                    order.getUser().getName());

            response.setUserEmail(
                    order.getUser().getEmail());
        }

        List<OrderItemResponse> itemResponses =
                new ArrayList<>();

        for (OrderItem item : order.getItems()) {

            OrderItemResponse itemResponse =
                    new OrderItemResponse();

            itemResponse.setProductId(
                    item.getProduct().getId());

            itemResponse.setProductName(
                    item.getProduct().getName());

            itemResponse.setQuantity(
                    item.getQuantity());

            itemResponse.setPrice(
                    item.getPrice());

            itemResponses.add(itemResponse);
        }

        response.setItems(itemResponses);

        return response;
    }
    
    public List<OrderResponse> getMyOrders(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        return orderRepository.findByUserId(user.getId())
                .stream()
                .map(this::convertToResponse)
                .toList();
    }
}