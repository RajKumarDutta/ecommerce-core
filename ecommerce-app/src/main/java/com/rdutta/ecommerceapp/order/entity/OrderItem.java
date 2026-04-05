package com.rdutta.ecommerceapp.order.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    @Getter
    private String productId;

    @Getter
    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    protected OrderItem() {}

    public OrderItem(String productId, int quantity, BigDecimal price) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        this.productId = productId;
        this.quantity = quantity;
        this.price = price;
    }

    public BigDecimal getTotal() {
        return price.multiply(BigDecimal.valueOf(quantity));
    }

    void setOrder(Order order) {
        this.order = order;
    }
}