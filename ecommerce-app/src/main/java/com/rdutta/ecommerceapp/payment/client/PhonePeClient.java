package com.rdutta.ecommerceapp.payment.client;

import com.phonepe.sdk.pg.common.models.response.OrderStatusResponse; // SDK Class
import com.rdutta.ecommerceapp.payment.dto.PaymentVerifyResponse;

public interface PhonePeClient {

    // Keep your internal record for initiation if needed
    PhonePeOrderResponse createOrder(String merchantOrderId, long amount);

    // FIX: Return the SDK's full response object
    OrderStatusResponse getStatus(String merchantOrderId);

    String getCheckoutUrl(String merchantOrderId, long amount, String redirectUrl);

    PaymentVerifyResponse checkStatus(String orderId);

    record PhonePeOrderResponse(
            String orderId,
            String token,
            String state
    ) {}
}