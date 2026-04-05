package com.rdutta.ecommerceapp.payment.dto;


/**
 * A concise, immutable DTO for payment verification.
 * 'state' represents the transaction lifecycle (COMPLETED, FAILED, PENDING).
 * 'responseCode' represents the business outcome (SUCCESS, AUTHORIZATION_FAILED, etc).
 */
public record PaymentVerifyResponse(
        String merchantOrderId,
        String transactionId,
        String state,
        String responseCode,
        long amountInPaisa,
        String paymentMode,
        Long timestamp,
        String errorCode,
        String detailedErrorCode
) {}