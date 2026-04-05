package com.rdutta.ecommerceapp.payment.dto;


import com.rdutta.ecommerceapp.payment.enums.PaymentStatus;

import java.util.UUID;

public record PaymentResponse(
        UUID paymentId,
        String merchantOrderId,
        String provider,
        String checkoutUrl, // 🔥 instead of token
        PaymentStatus status
) {}