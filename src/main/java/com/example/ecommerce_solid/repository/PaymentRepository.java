package com.example.ecommerce_solid.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.ecommerce_solid.model.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}