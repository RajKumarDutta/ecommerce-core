package com.rdutta.ecommerceapp.checkout.service;

import com.rdutta.ecommerceapp.checkout.dto.CheckoutRequest;
import com.rdutta.ecommerceapp.order.dto.OrderResponse;
import com.rdutta.ecommerceapp.order.service.OrderService;
import com.rdutta.ecommerceapp.payment.dto.PaymentResponse;
import com.rdutta.ecommerceapp.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CheckoutService {

    private final OrderService orderService;
    private final PaymentService paymentService;

    // We do NOT use @Transactional here so that order and payment
    // commit their own internal states independently.
    public PaymentResponse checkout(CheckoutRequest request) {
        // 1. Create and commit order
        OrderResponse order = orderService.createOrder(request.items());

        // 2. Initiate payment
        return paymentService.initiate(order.id(), request.provider());
    }
}