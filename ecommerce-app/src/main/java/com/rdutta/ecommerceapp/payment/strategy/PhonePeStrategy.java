package com.rdutta.ecommerceapp.payment.strategy;

import com.phonepe.sdk.pg.common.models.response.OrderStatusResponse;
import com.rdutta.ecommerceapp.payment.client.PhonePeClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component
@RequiredArgsConstructor
public class PhonePeStrategy implements PaymentStrategy {

    private final PhonePeClient phonePeClient;
    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;
    @Override
    public PaymentResult initiate(String merchantOrderId, BigDecimal amount) {
        long paisa = amount.multiply(BigDecimal.valueOf(100)).longValue();

        // Get redirect URL for PhonePe Standard Checkout

        String redirectUrl = frontendUrl + "/payment-success?id=" + merchantOrderId;
        String checkoutUrl = phonePeClient.getCheckoutUrl(merchantOrderId, paisa, redirectUrl);
        return new PaymentResult(
                merchantOrderId,
                checkoutUrl,
                "INITIATED" // Standard starting state for external redirect
        );
    }

    @Override
    public StatusResult checkStatus(String merchantOrderId) {
        // 1. Get the rich response object
        OrderStatusResponse response = phonePeClient.getStatus(merchantOrderId);

        String pgTransactionId = null;
        String paymentMode = null;

        // 2. Access the internal list (SDK provides getPaymentDetails())
        if (response.getPaymentDetails() != null && !response.getPaymentDetails().isEmpty()) {
            var detail = response.getPaymentDetails().get(0);
            pgTransactionId = detail.getTransactionId();
            paymentMode = String.valueOf(detail.getPaymentMode());
        }

        return new StatusResult(
                response.getState(),
                pgTransactionId,
                paymentMode,
                response.toString() // For your raw_response audit column
        );
    }

    @Override
    public String getProvider() {
        return "PHONEPE";
    }
}