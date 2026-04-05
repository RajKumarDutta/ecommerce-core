package com.rdutta.ecommerceapp.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreatePaymentRequest(

        @NotNull(message = "orderId is required")
        UUID orderId,

        @NotBlank(message = "provider is required")
        String provider

) {}