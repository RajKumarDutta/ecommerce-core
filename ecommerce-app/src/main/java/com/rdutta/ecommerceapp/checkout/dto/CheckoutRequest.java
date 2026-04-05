package com.rdutta.ecommerceapp.checkout.dto;

import com.rdutta.ecommerceapp.order.dto.OrderItemRequest;
import com.rdutta.ecommerceapp.payment.enums.PaymentProvider;

import java.util.List;

public record CheckoutRequest(
        List<OrderItemRequest> items,
        PaymentProvider provider
) {}