package com.example.ecommerce_solid.payment;

import java.util.UUID;

import org.springframework.stereotype.Component;

@Component("CARD")
public class CardPaymentStrategy implements PaymentStrategy {

    @Override
    public String pay(double amount) {

        System.out.println("Processing Card payment of ₹" + amount);

        return "CARD-" + UUID.randomUUID();
    }
}