package com.rdutta.ecommerceapp.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record OrderItemRequest(

        @NotNull
        String productId,

        @Min(1)
        int quantity
) {}
