package com.rdutta.ecommerceapp.inventory.entity;

import com.rdutta.ecommerceapp.inventory.enums.ReservationStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "inventory_reservations")
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class InventoryReservation {

    @Id
    private UUID id;

    private String productId;

    private UUID orderId;

    private int quantity;

    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    private Instant createdAt;

    public InventoryReservation(String productId, UUID orderId, int quantity) {
        this.id = UUID.randomUUID();
        this.productId = productId;
        this.orderId = orderId;
        this.quantity = quantity;
        this.status = ReservationStatus.RESERVED;
        this.createdAt = Instant.now();
    }

    public void confirm() {
        this.status = ReservationStatus.CONFIRMED;
    }

    public void release() {
        this.status = ReservationStatus.RELEASED;
    }
}