package com.rdutta.ecommerceapp.order.entity;

import com.rdutta.ecommerceapp.order.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class Order {

    @Id
    @Getter
    private UUID id;

    @OneToMany(
            mappedBy = "order",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @Getter
    private List<OrderItem> items = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Getter
    private OrderStatus status;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    @Getter
    private BigDecimal totalAmount;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Version
    private Long version;

    // 🔥 SINGLE source of creation
    public static Order create(List<OrderItem> items) {

        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Order must have at least one item");
        }

        Order order = new Order();

        order.id = UUID.randomUUID();
        order.status = OrderStatus.CREATED;
        order.createdAt = Instant.now();

        for (OrderItem item : items) {
            order.addItem(item);
        }

        order.totalAmount = order.calculateTotal();

        return order;
    }

    // 🔥 Domain logic for total
    private BigDecimal calculateTotal() {
        return items.stream()
                .map(OrderItem::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // 🔥 State transitions
    public void confirm() {
        if (this.status != OrderStatus.CREATED) {
            throw new IllegalStateException("Only CREATED orders can be confirmed");
        }
        this.status = OrderStatus.CONFIRMED;
    }

    public void cancel() {
        this.status = OrderStatus.CANCELLED;
    }

    // 🔥 Relationship management
    public void addItem(OrderItem item) {
        item.setOrder(this);
        this.items.add(item);
    }
}