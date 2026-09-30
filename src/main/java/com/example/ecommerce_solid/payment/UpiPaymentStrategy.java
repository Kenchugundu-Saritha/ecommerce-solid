package com.example.ecommerce_solid.payment;

import java.util.UUID;

import org.springframework.stereotype.Component;

@Component("UPI")
public class UpiPaymentStrategy implements PaymentStrategy {

    @Override
    public String pay(double amount) {

        System.out.println("Processing UPI payment of ₹" + amount);

        return "UPI-" + UUID.randomUUID();
    }
}