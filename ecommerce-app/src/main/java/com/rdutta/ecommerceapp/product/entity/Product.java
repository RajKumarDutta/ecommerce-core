package com.rdutta.ecommerceapp.product.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "products")
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class Product {

    @Id
    @Getter
    private UUID id;

    @Column(nullable = false)
    @Getter
    private String name;

    @Column(nullable = false, precision = 12, scale = 2)
    @Getter
    private BigDecimal price;

    @Column(name = "available_quantity", nullable = false)
    @Getter
    @Setter
    private int availableQuantity;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    private Product(String name, BigDecimal price, int quantity) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.price = price;
        this.availableQuantity = quantity;
        this.createdAt = Instant.now();
    }

    // 🔥 Factory
    public static Product create(String name, BigDecimal price, int quantity) {

        if (price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be positive");
        }

        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }

        return new Product(name, price, quantity);
    }

    // 🔥 Business logic
    public void reduceStock(int quantity) {
        if (this.availableQuantity < quantity) {
            throw new IllegalStateException("Insufficient stock");
        }
        this.availableQuantity -= quantity;
    }

    public void increaseStock(int quantity) {
        this.availableQuantity += quantity;
    }
}