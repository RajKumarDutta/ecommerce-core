package com.rdutta.ecommerceapp.order.controller;

import com.rdutta.ecommerceapp.common.dao.IdempotencyDao;
import com.rdutta.ecommerceapp.common.dto.ApiResponse;
import com.rdutta.ecommerceapp.common.service.IdempotencyService;
import com.rdutta.ecommerceapp.order.dao.OrderDao;
import com.rdutta.ecommerceapp.order.dto.CreateOrderRequest;
import com.rdutta.ecommerceapp.order.dto.OrderResponse;
import com.rdutta.ecommerceapp.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderDao orderService;
    private final IdempotencyDao idempotencyService;

    @PostMapping
    public ApiResponse<OrderResponse> createOrder(
            @RequestHeader("Idempotency-Key") String key,
            @Valid @RequestBody CreateOrderRequest request) {

        OrderResponse response = idempotencyService.execute(
                key,
                OrderResponse.class,
                () -> orderService.createOrder(request.items())
        );

        return ApiResponse.success(response, "Order created successfully");
    }

    @PatchMapping("/{id}/confirm")
    public ApiResponse<OrderResponse> confirmOrder(@PathVariable UUID id) {
        OrderResponse orderResponse = orderService.confirmOrder(id);

        return ApiResponse.success(orderResponse, "Order confirmed successfully");
    }
}