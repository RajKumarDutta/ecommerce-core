package com.rdutta.ecommerceapp.payment.client;

import com.phonepe.sdk.pg.Env;
import com.phonepe.sdk.pg.common.models.response.OrderStatusResponse;
import com.phonepe.sdk.pg.payments.v2.StandardCheckoutClient;
import com.phonepe.sdk.pg.payments.v2.models.request.CreateSdkOrderRequest;
import com.phonepe.sdk.pg.payments.v2.models.request.StandardCheckoutPayRequest;
import com.phonepe.sdk.pg.payments.v2.models.response.CreateSdkOrderResponse;
import com.phonepe.sdk.pg.payments.v2.models.response.StandardCheckoutPayResponse;
import com.rdutta.ecommerceapp.payment.dto.PaymentVerifyResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Map;

@Slf4j
@Component
public class PhonePeClientImpl implements PhonePeClient {
    private final StandardCheckoutClient client;
    public PhonePeClientImpl(
            @Value("${phonepe.client.id}") String clientId,
            @Value("${phonepe.client.secret}") String clientSecret) {

        int clientVersion = 1;

        this.client = StandardCheckoutClient.getInstance(
                clientId,
                clientSecret,
                clientVersion,
                Env.SANDBOX
        );

    }

    @Override
    public PhonePeOrderResponse createOrder(String merchantOrderId, long amount) {

        try {
            CreateSdkOrderRequest request = CreateSdkOrderRequest
                    .StandardCheckoutBuilder()
                    .merchantOrderId(merchantOrderId)
                    .amount(amount)
                    .build();

            CreateSdkOrderResponse response = client.createSdkOrder(request);

            return new PhonePeOrderResponse(
                    response.getOrderId(),
                    response.getToken(),
                    response.getState()
            );

        } catch (Exception e) {
            throw new RuntimeException("PhonePe order creation failed", e);
        }
    }

    @Override
    public String getCheckoutUrl(String merchantOrderId, long amount, String redirectUrl) {

        try {

            StandardCheckoutPayRequest request =
                    StandardCheckoutPayRequest.builder()
                            .merchantOrderId(merchantOrderId)
                            .amount(amount)
                            .redirectUrl(redirectUrl)
                            .build();

            StandardCheckoutPayResponse response = client.pay(request);

            return response.getRedirectUrl(); // 🔥 THIS IS WHAT YOU NEED

        } catch (Exception e) {
            throw new RuntimeException("PhonePe checkout failed", e);
        }
    }

    @Override
    public PaymentVerifyResponse checkStatus(String merchantOrderId) {
        try {
            OrderStatusResponse response = client.getOrderStatus(merchantOrderId);

            // paymentDetails is a list — get the latest attempt
            var paymentDetails = response.getPaymentDetails();

            if (paymentDetails != null && !paymentDetails.isEmpty()) {
                // Last item = most recent payment attempt
                var latest = paymentDetails.get(paymentDetails.size() - 1);

                return new PaymentVerifyResponse(
                        merchantOrderId,
                        latest.getTransactionId(),
                        latest.getState(),        // COMPLETED | PENDING | FAILED
                        response.getErrorCode(),              // PAYMENT_SUCCESS — still on root
                        response.getAmount(),            // amount is on root response
                        latest.getPaymentMode().name(),
                        latest.getTimestamp(),
                        latest.getErrorCode(),
                        latest.getDetailedErrorCode()
                );
            }

            // Order exists but no payment attempt yet (user closed the page)
            return new PaymentVerifyResponse(
                    merchantOrderId, null, "PENDING", "NO_PAYMENT_ATTEMPT",
                    0, null, null, null, null
            );

        } catch (Exception e) {
            log.error("PhonePe Status Check Failed for Order: {}", merchantOrderId, e);
        }
        return new PaymentVerifyResponse(
                merchantOrderId, null, "FAILED", "ERROR",
                0, "UNKNOWN", null, null, null
        );
    }

    @Override
    public OrderStatusResponse getStatus(String merchantOrderId) {

        try {
            return client.getOrderStatus(merchantOrderId);
        } catch (Exception e) {
            throw new RuntimeException("PhonePe status check failed", e);
        }
    }
}