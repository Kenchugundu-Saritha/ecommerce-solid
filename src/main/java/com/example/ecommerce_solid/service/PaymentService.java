package com.example.ecommerce_solid.service;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.ecommerce_solid.dto.PaymentRequest;
import com.example.ecommerce_solid.model.Order;
import com.example.ecommerce_solid.model.Payment;
import com.example.ecommerce_solid.model.PaymentStatus;
import com.example.ecommerce_solid.payment.PaymentStrategy;
import com.example.ecommerce_solid.repository.OrderRepository;
import com.example.ecommerce_solid.repository.PaymentRepository;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    private final Map<String, PaymentStrategy> paymentStrategies;

    public PaymentService(
            PaymentRepository paymentRepository,
            OrderRepository orderRepository,
            Map<String, PaymentStrategy> paymentStrategies) {

        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.paymentStrategies = paymentStrategies;
    }

    public Payment makePayment(PaymentRequest request) {

        // 1. Find order
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Order not found with id: "
                                        + request.getOrderId()));

        // 2. Convert payment method to uppercase
        String method =
                request.getPaymentMethod().toUpperCase();

        // 3. Find correct strategy
        PaymentStrategy strategy =
                paymentStrategies.get(method);

        if (strategy == null) {
            throw new RuntimeException(
                    "Unsupported payment method: " + method);
        }

        // 4. Process payment
        String transactionId =
                strategy.pay(order.getTotalAmount());

        // 5. Create payment record
        Payment payment = new Payment();

        payment.setOrderId(order.getId());
        payment.setAmount(order.getTotalAmount());
        payment.setPaymentMethod(method);
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setTransactionId(transactionId);

        // 6. Save payment
        return paymentRepository.save(payment);
    }
}