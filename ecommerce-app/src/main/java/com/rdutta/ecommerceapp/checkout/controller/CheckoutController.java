package com.rdutta.ecommerceapp.checkout.controller;

import com.rdutta.ecommerceapp.checkout.dto.CheckoutRequest;
import com.rdutta.ecommerceapp.common.service.IdempotencyService;
import com.rdutta.ecommerceapp.payment.dto.PaymentResponse;
import com.rdutta.ecommerceapp.common.dto.ApiResponse;
import com.rdutta.ecommerceapp.checkout.service.CheckoutService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/checkout")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class CheckoutController {

    private final CheckoutService checkoutService;
    private final IdempotencyService idempotencyService;

    @PostMapping
    public ApiResponse<PaymentResponse> checkout(
            @RequestHeader("Idempotency-Key") String key,
            @RequestBody CheckoutRequest request) {

        PaymentResponse response = idempotencyService.execute(
                key,
                PaymentResponse.class,
                () -> checkoutService.checkout(request)
        );

        return ApiResponse.success(response, "Checkout successful");
    }
}