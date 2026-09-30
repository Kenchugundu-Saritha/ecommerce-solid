package com.example.ecommerce_solid.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.ecommerce_solid.dto.CartRequest;
import com.example.ecommerce_solid.dto.OrderItemResponse;
import com.example.ecommerce_solid.dto.OrderResponse;
import com.example.ecommerce_solid.exception.ProductNotFoundException;
import com.example.ecommerce_solid.model.Cart;
import com.example.ecommerce_solid.model.CartItem;
import com.example.ecommerce_solid.model.Order;
import com.example.ecommerce_solid.model.OrderItem;
import com.example.ecommerce_solid.model.OrderStatus;
import com.example.ecommerce_solid.model.Product;
import com.example.ecommerce_solid.model.User;
import com.example.ecommerce_solid.repository.CartRepository;
import com.example.ecommerce_solid.repository.OrderRepository;
import com.example.ecommerce_solid.repository.ProductRepository;
import com.example.ecommerce_solid.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    public CartService(
            CartRepository cartRepository,
            UserRepository userRepository,
            ProductRepository productRepository,
            OrderRepository orderRepository) {

        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
    }

    public Cart addToCart(CartRequest request, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found with email: " + email));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() ->
                        new ProductNotFoundException(
                                request.getProductId()));

        if (product.getQuantity() < request.getQuantity()) {
            throw new RuntimeException("Insufficient product stock");
        }

        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    return newCart;
                });

        CartItem existingItem = cart.getItems()
                .stream()
                .filter(item ->
                        item.getProduct().getId().equals(product.getId()))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {

            int newQuantity =
                    existingItem.getQuantity()
                            + request.getQuantity();

            if (product.getQuantity() < newQuantity) {
                throw new RuntimeException("Insufficient product stock");
            }

            existingItem.setQuantity(newQuantity);

        } else {

            CartItem cartItem = new CartItem();
            cartItem.setProduct(product);
            cartItem.setQuantity(request.getQuantity());
            cartItem.setCart(cart);

            cart.getItems().add(cartItem);
        }

        return cartRepository.save(cart);
    }

    public Cart getCart(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found with email: " + email));

        return cartRepository.findByUserId(user.getId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Cart not found for user: " + user.getId()));
    }
    
    public void removeItem(String email, Long productId) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found with email: " + email));

        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Cart not found for user: " + user.getId()));

        cart.getItems().removeIf(item ->
                item.getProduct().getId().equals(productId));

        cartRepository.save(cart);
    }

    public void clearCart(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found with email: " + email));

        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Cart not found for user: " + user.getId()));

        cart.getItems().clear();

        cartRepository.save(cart);
    }
    
    @Transactional
    public OrderResponse checkout(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found with email: " + email));

        Long userId = user.getId();

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Cart not found for user: " + userId));

        // 2. Check cart is not empty
        if (cart.getItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        // 3. Create new order
        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.PLACED);

        double totalAmount = 0;

        // 4. Process every cart item
        for (CartItem cartItem : cart.getItems()) {

            Product product = productRepository.findById(
                    cartItem.getProduct().getId()
            ).orElseThrow(() ->
                    new ProductNotFoundException(
                            cartItem.getProduct().getId()));

            // 5. Check stock
            if (product.getQuantity() < cartItem.getQuantity()) {
                throw new RuntimeException(
                        "Insufficient stock for product: "
                        + product.getName());
            }

            // 6. Create order item
            OrderItem orderItem = new OrderItem();

            orderItem.setProduct(product);
            orderItem.setQuantity(cartItem.getQuantity());

            // Store current price
            orderItem.setPrice(product.getPrice());

            orderItem.setOrder(order);

            // Add item to order
            order.getItems().add(orderItem);

            // 7. Calculate total
            totalAmount +=
                    product.getPrice() * cartItem.getQuantity();

            // 8. Reduce stock
            product.setQuantity(
                    product.getQuantity()
                            - cartItem.getQuantity());

            productRepository.save(product);
        }

        // 9. Set order total
        order.setTotalAmount(totalAmount);

        // 10. Save order
        Order savedOrder = orderRepository.save(order);

        cart.getItems().clear();
        cartRepository.save(cart);

        return convertToOrderResponse(savedOrder);
    }
    
    private OrderResponse convertToOrderResponse(Order order) {

        OrderResponse response = new OrderResponse();

        response.setId(order.getId());
        response.setTotalAmount(order.getTotalAmount());
        response.setStatus(order.getStatus());

        if (order.getUser() != null) {
            response.setUserId(order.getUser().getId());
            response.setUserName(order.getUser().getName());
            response.setUserEmail(order.getUser().getEmail());
        }

        List<OrderItemResponse> itemResponses = new ArrayList<>();

        for (OrderItem item : order.getItems()) {

            OrderItemResponse itemResponse = new OrderItemResponse();

            itemResponse.setProductId(item.getProduct().getId());
            itemResponse.setProductName(item.getProduct().getName());
            itemResponse.setQuantity(item.getQuantity());
            itemResponse.setPrice(item.getPrice());

            itemResponses.add(itemResponse);
        }

        response.setItems(itemResponses);

        return response;
    }
}