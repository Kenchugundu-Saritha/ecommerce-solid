package com.example.ecommerce_solid.payment;

import java.util.UUID;

import org.springframework.stereotype.Component;

@Component("COD")
public class CashOnDeliveryStrategy implements PaymentStrategy {

    @Override
    public String pay(double amount) {

        System.out.println(
                "Cash on Delivery selected for ₹" + amount);

        return "COD-" + UUID.randomUUID();
    }
}