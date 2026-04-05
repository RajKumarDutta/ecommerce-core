package com.rdutta.ecommerceapp.payment.strategy;

import java.math.BigDecimal;
import java.util.List;

public interface PaymentStrategy {

    PaymentResult initiate(String merchantOrderId, BigDecimal amount);

    // FIX: Return a structured result instead of just a String
    StatusResult checkStatus(String merchantOrderId);

    String getProvider();

    record PaymentResult(
            String providerOrderId,
            String checkoutUrl,
            String status
    ) {}

    // NEW: Added to capture reconciliation data from the gateway
    record StatusResult(
            String state,           // e.g., COMPLETED, PENDING, FAILED
            String pgTransactionId, // e.g., T240405...
            String paymentMode,     // e.g., UPI_INTENT, CARD
            String rawResponse      // Full JSON for the audit trail
    ) {}
}