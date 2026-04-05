package com.rdutta.ecommerceapp.common.entity;

import com.rdutta.ecommerceapp.common.enums.IdempotencyStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "idempotency_keys")
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@Getter
public class Idempotency {

    @Id
    private String id;

    @Column(nullable = true)
    private String response;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IdempotencyStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    // 🔥 Constructor for PROCESSING
    public Idempotency(String id) {
        this.id = id;
        this.status = IdempotencyStatus.PROCESSING;
        this.createdAt = Instant.now();
    }

    // 🔥 Mark as completed
    public void markCompleted(String response) {
        this.response = response;
        this.status = IdempotencyStatus.COMPLETED;
    }
}