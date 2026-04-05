package com.rdutta.ecommerceapp.payment.service;

import com.rdutta.ecommerceapp.inventory.service.InventoryService;
import com.rdutta.ecommerceapp.order.service.OrderService;
import com.rdutta.ecommerceapp.order.repository.OrderRepository;
import com.rdutta.ecommerceapp.payment.client.PhonePeClient;
import com.rdutta.ecommerceapp.payment.dto.PaymentResponse;
import com.rdutta.ecommerceapp.payment.dto.PaymentVerifyResponse;
import com.rdutta.ecommerceapp.payment.entity.Payment;
import com.rdutta.ecommerceapp.payment.enums.PaymentProvider;
import com.rdutta.ecommerceapp.payment.enums.PaymentStatus;
import com.rdutta.ecommerceapp.payment.repository.PaymentRepository;
import com.rdutta.ecommerceapp.payment.strategy.PaymentStrategy;
import com.rdutta.ecommerceapp.payment.util.PaymentStrategyFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentStrategyFactory factory;
    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private final InventoryService inventoryService;
    private final PhonePeClient phonePeClient;

    @Retryable(
            retryFor = { ObjectOptimisticLockingFailureException.class },
            maxAttempts = 3,
            backoff = @Backoff(delay = 300)
    )
    @Transactional
    public void handleWebhook(String merchantOrderId, String newState) {
        Payment payment = paymentRepository.findByMerchantOrderId(merchantOrderId)
                .orElseThrow(() -> new RuntimeException("Payment record not found for webhook"));

        // IDEMPOTENCY CHECK: If the Scheduler already finished this, stop here.
        if (payment.getStatus() == PaymentStatus.COMPLETED || payment.getStatus() == PaymentStatus.FAILED) {
            log.info("Webhook ignored: Payment {} already in final state {}", merchantOrderId, payment.getStatus());
            return;
        }

        log.info("Webhook processing for {}: New State {}", merchantOrderId, newState);

        if ("COMPLETED".equalsIgnoreCase(newState)) {
            // Note: For a Webhook, you should still ideally call 'checkStatus'
            // to get the pgTransactionId and Mode before completing.
            checkAndUpdateStatus(merchantOrderId);
        } else if ("FAILED".equalsIgnoreCase(newState)) {
            handleFailure(payment);
        }
    }

    public PaymentResponse initiate(UUID orderId, PaymentProvider provider) {
        var order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // STEP 1: Save as CREATED and Commit immediately
        Payment payment = saveAndFlushNewPayment(orderId, order.getTotalAmount(), provider.name());

        try {
            PaymentStrategy strategy = factory.get(provider.name());

            // STEP 2: External API Call (Outside main transaction)
            var result = strategy.initiate(payment.getMerchantOrderId(), order.getTotalAmount());

            // STEP 3: Update to INITIATED
            updatePaymentStatus(payment.getId(), PaymentStatus.INITIATED);

            return new PaymentResponse(
                    payment.getId(),
                    payment.getMerchantOrderId(),
                    provider.name(),
                    result.checkoutUrl(),
                    PaymentStatus.INITIATED
            );
        } catch (Exception e) {
            log.error("Payment initiation failed for {}", orderId, e);
            throw new RuntimeException("Gateway unreachable");
        }
    }

    @Retryable(
            retryFor = { ObjectOptimisticLockingFailureException.class },
            maxAttempts = 3,
            backoff = @Backoff(delay = 500, multiplier = 2)
    )
    @Transactional
    public void checkAndUpdateStatus(String merchantOrderId) {
        Payment payment = paymentRepository.findByMerchantOrderId(merchantOrderId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found"));

        // Don't process if already in a final state
        if (payment.getStatus() == PaymentStatus.COMPLETED || payment.getStatus() == PaymentStatus.FAILED) {
            return;
        }

        PaymentStrategy strategy = factory.get(payment.getProvider());
        PaymentStrategy.StatusResult result = strategy.checkStatus(merchantOrderId);

        log.info("Gateway status for {}: {}", merchantOrderId, result.state());

        switch (result.state().toUpperCase()) {
            case "COMPLETED" -> handleSuccess(payment, result);
            case "FAILED" -> handleFailure(payment);
            case "PENDING" -> {
                if (payment.getCreatedAt().isBefore(Instant.now().minus(15, ChronoUnit.MINUTES))) {
                    handleFailure(payment);
                } else {
                    payment.markPending();
                    paymentRepository.save(payment);
                }
            }
            default -> log.warn("Unhandled gateway state: {}", result.state());
        }
    }

    @Recover
    public void recover(ObjectOptimisticLockingFailureException e, String merchantOrderId) {
        log.warn("All retry attempts failed for Payment {}. Process aborted safely.", merchantOrderId);
    }


    private void handleSuccess(Payment payment, PaymentStrategy.StatusResult result) {
        // Update Payment with reconciliation data FIRST
        payment.complete(
                result.pgTransactionId(),
                result.paymentMode(),
                result.rawResponse()
        );
        paymentRepository.saveAndFlush(payment);

        // Update Order and Inventory
        inventoryService.confirm(payment.getOrderId());
        orderService.confirmOrder(payment.getOrderId());

        log.info("✅ Payment & Order confirmed: {} | Mode: {} | TID: {}",
                payment.getMerchantOrderId(), result.paymentMode(), result.pgTransactionId());
    }

    private void handleFailure(Payment payment) {
        payment.markFailed();
        paymentRepository.saveAndFlush(payment);

        inventoryService.release(payment.getOrderId());
        orderService.cancelOrder(payment.getOrderId());
        log.error("❌ Payment failed for Order: {}", payment.getOrderId());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Payment saveAndFlushNewPayment(UUID orderId, BigDecimal amount, String provider) {
        Payment payment = new Payment(orderId, amount, provider);
        return paymentRepository.saveAndFlush(payment);
    }

    @Transactional
    public void updatePaymentStatus(UUID id, PaymentStatus status) {
        paymentRepository.findById(id).ifPresent(p -> {
            switch (status) {
                case INITIATED -> p.markInitiated();
                case PENDING   -> p.markPending();
                case FAILED    -> p.markFailed();
                case COMPLETED -> log.warn("Use complete() method for success transitions");
                default -> log.info("No transition defined for status: {}", status);
            }
            paymentRepository.save(p);
        });
    }

    public PaymentVerifyResponse verifyPaymentStatus(String orderId) {
        // 1. Call PhonePe's SDK or API to get the real-time status
        PaymentVerifyResponse response = phonePeClient.checkStatus(orderId);

        // 2. Update our local database so we stay in sync
        paymentRepository.findByMerchantOrderId(orderId).ifPresent(payment -> {
            payment.setStatus(PaymentStatus.valueOf(response.state())); // e.g., "COMPLETED", "FAILED"
            paymentRepository.save(payment);
        });

        return response;
    }
}