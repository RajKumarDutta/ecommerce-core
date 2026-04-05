package com.rdutta.ecommerceapp.payment.controller;

import com.rdutta.ecommerceapp.checkout.dto.CheckoutRequest;
import com.rdutta.ecommerceapp.checkout.service.CheckoutService;
import com.rdutta.ecommerceapp.common.dto.ApiResponse;
import com.rdutta.ecommerceapp.common.service.IdempotencyService;
import com.rdutta.ecommerceapp.payment.dto.PaymentResponse;
import com.rdutta.ecommerceapp.payment.dto.PaymentVerifyResponse;
import com.rdutta.ecommerceapp.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final IdempotencyService idempotencyService;
    private final CheckoutService checkoutService;

    // 🔥 INITIATE PAYMENT
    @PostMapping("checkout")
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

    @GetMapping("/status/{orderId}")
    public ApiResponse<PaymentVerifyResponse> checkStatus(@PathVariable String orderId) {
        // 1. Logic is delegated to the service
        PaymentVerifyResponse status = paymentService.verifyPaymentStatus(orderId);

        // 2. Return a standardized response
        return ApiResponse.success(status, "Status retrieved successfully");
    }
}