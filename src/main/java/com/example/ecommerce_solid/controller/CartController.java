package com.example.ecommerce_solid.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.example.ecommerce_solid.dto.CartRequest;
import com.example.ecommerce_solid.dto.OrderResponse;
import com.example.ecommerce_solid.model.Cart;
import com.example.ecommerce_solid.model.Order;
import com.example.ecommerce_solid.service.CartService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/cart")
public class CartController {

private final CartService cartService;

public CartController(CartService cartService) {
    this.cartService = cartService;
}

// Add product to logged-in customer's cart
@PostMapping("/add")
public Cart addToCart(
        @Valid @RequestBody CartRequest request,
        Authentication authentication) {

    return cartService.addToCart(
            request,
            authentication.getName());
}

// Get logged-in customer's cart
@GetMapping
public Cart getCart(Authentication authentication) {

    return cartService.getCart(
            authentication.getName());
}

// Remove product from logged-in customer's cart
@DeleteMapping("/remove/{productId}")
public String removeItem(
        @PathVariable Long productId,
        Authentication authentication) {

    cartService.removeItem(
            authentication.getName(),
            productId);

    return "Product removed from cart";
}

// Clear logged-in customer's cart
@DeleteMapping("/clear")
public String clearCart(Authentication authentication) {

    cartService.clearCart(
            authentication.getName());

    return "Cart cleared successfully";
}

// Checkout logged-in customer's cart
@PostMapping("/checkout")
public OrderResponse checkout(Authentication authentication) {

    return cartService.checkout(
            authentication.getName());
}
}
