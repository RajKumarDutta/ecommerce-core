package com.rdutta.ecommerceapp.payment.entity;

import com.rdutta.ecommerceapp.payment.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payments")
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class Payment {
    @Id private UUID id;
    private UUID orderId;

    @Column(unique = true)
    private String merchantOrderId;

    private String pgTransactionId;
    private String paymentMode;
    private String provider;

    @Enumerated(EnumType.STRING)
    @Setter
    private PaymentStatus status;
    private BigDecimal amount;

    @Column(columnDefinition = "TEXT")
    private String rawResponse;
    private Instant createdAt;
    @Version
    private Integer version;

    // Fixed Constructor
    public Payment(UUID orderId, BigDecimal amount, String provider) {
        this.id = UUID.randomUUID();
        this.orderId = orderId;
        this.amount = amount;
        this.provider = provider;
        // Generate unique merchant order ID for PhonePe
        this.merchantOrderId = "MT" + System.currentTimeMillis() + orderId.toString().substring(0, 5);
        this.status = PaymentStatus.CREATED;
        this.createdAt = Instant.now();
    }

    public void markInitiated() {
        this.status = PaymentStatus.INITIATED;
    }

    public void markPending() {
        this.status = PaymentStatus.PENDING;
    }

    public void complete(String pgTid, String mode, String json) {
        this.pgTransactionId = pgTid;
        this.paymentMode = mode;
        this.rawResponse = json;
        this.status = PaymentStatus.COMPLETED;
    }

    public void markFailed() {
        this.status = PaymentStatus.FAILED;
    }
}