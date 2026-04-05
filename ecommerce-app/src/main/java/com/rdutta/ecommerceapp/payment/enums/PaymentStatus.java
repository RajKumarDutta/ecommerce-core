package com.rdutta.ecommerceapp.payment.enums;

public enum PaymentStatus {
    // 🔹 1. Initial record created in our DB
    CREATED,

    // 🔹 2. Checkout URL generated, user redirected to PhonePe
    INITIATED,

    // 🔹 3. User has interacted (e.g., entered UPI ID, waiting for OTP)
    PENDING,

    // 🔹 4. Final Success
    COMPLETED,

    // 🔹 5. Final Failures
    FAILED,
    CANCELLED,
    EXPIRED
}