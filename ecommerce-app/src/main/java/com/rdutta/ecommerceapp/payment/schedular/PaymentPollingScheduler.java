package com.rdutta.ecommerceapp.payment.schedular;

import com.rdutta.ecommerceapp.payment.entity.Payment;
import com.rdutta.ecommerceapp.payment.enums.PaymentStatus;
import com.rdutta.ecommerceapp.payment.repository.PaymentRepository;
import com.rdutta.ecommerceapp.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentPollingScheduler {

    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;

    @Scheduled(fixedDelay = 15000)
    public void pollActivePayments() {
        // FIX: Poll everything that isn't COMPLETED or FAILED
        List<PaymentStatus> activeStatuses = List.of(
                PaymentStatus.CREATED,
                PaymentStatus.INITIATED,
                PaymentStatus.PENDING
        );

        List<Payment> pendingPayments = paymentRepository.findByStatusIn(activeStatuses);

        if (!pendingPayments.isEmpty()) {
            log.info("📊 Found {} payments to check", pendingPayments.size());
        }

        for (Payment payment : pendingPayments) {
            try {
                paymentService.checkAndUpdateStatus(payment.getMerchantOrderId());
            } catch (Exception e) {
                log.error("❌ Polling failed for {}", payment.getMerchantOrderId());
            }
        }
    }
}