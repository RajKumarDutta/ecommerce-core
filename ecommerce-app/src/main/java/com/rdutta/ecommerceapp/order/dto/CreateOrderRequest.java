package com.rdutta.ecommerceapp.order.dto;


import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateOrderRequest(

        @NotNull(message = "Items is required")
        List<OrderItemRequest> items

) {}