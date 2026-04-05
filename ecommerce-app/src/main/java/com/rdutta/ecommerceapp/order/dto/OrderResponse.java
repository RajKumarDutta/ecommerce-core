package com.rdutta.ecommerceapp.order.dto;

import com.rdutta.ecommerceapp.order.enums.OrderStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        OrderStatus status,
        BigDecimal totalAmount
) {}