package com.rdutta.ecommerceapp.payment.controller;

import com.rdutta.ecommerceapp.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/webhook")
@RequiredArgsConstructor
public class PaymentWebhookController {

    private final PaymentService paymentService;

    @PostMapping("/phonepe")
    public String webhook(@RequestBody Map<String, Object> payload) {

        String merchantOrderId = (String) payload.get("merchantOrderId");
        String state = (String) payload.get("state");

        paymentService.handleWebhook(merchantOrderId, state);

        return "OK";
    }
}