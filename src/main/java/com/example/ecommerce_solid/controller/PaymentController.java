package com.example.ecommerce_solid.controller;

import org.springframework.web.bind.annotation.*;

import com.example.ecommerce_solid.dto.PaymentRequest;
import com.example.ecommerce_solid.model.Payment;
import com.example.ecommerce_solid.service.PaymentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public Payment makePayment(
            @Valid @RequestBody PaymentRequest request) {

        return paymentService.makePayment(request);
    }
}