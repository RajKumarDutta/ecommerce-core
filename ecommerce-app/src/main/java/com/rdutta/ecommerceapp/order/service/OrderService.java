package com.rdutta.ecommerceapp.order.service;

import com.rdutta.ecommerceapp.order.dao.OrderDao;
import com.rdutta.ecommerceapp.order.dto.OrderItemRequest;
import com.rdutta.ecommerceapp.order.dto.OrderResponse;
import com.rdutta.ecommerceapp.order.entity.Order;
import com.rdutta.ecommerceapp.order.entity.OrderItem;
import com.rdutta.ecommerceapp.order.enums.OrderStatus;
import com.rdutta.ecommerceapp.order.repository.OrderRepository;
import com.rdutta.ecommerceapp.product.dao.ProductClient;
import com.rdutta.ecommerceapp.inventory.service.InventoryService;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService implements OrderDao {

    private final OrderRepository orderRepository;
    private final ProductClient productClient;
    private final InventoryService inventoryService;

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    @Transactional
    @Override
    public OrderResponse createOrder(List<OrderItemRequest> items) {

        List<OrderItem> orderItems = new ArrayList<>();

        for (OrderItemRequest request : items) {

            var product = productClient.getProduct(request.productId());

            if (product == null) {
                throw new RuntimeException("Product not found");
            }

            orderItems.add(new OrderItem(
                    request.productId(),
                    request.quantity(),
                    product.price()
            ));
        }

        Order order = Order.create(orderItems);

        Order saved = orderRepository.save(order);

        log.info("Order saved (about to commit): {}", saved.getId());

        // 🔥 Reserve inventory
//        inventoryService.reserve(saved.getId(), saved.getItems());

        return mapToResponse(saved);
    }



    @Override
    public OrderResponse confirmOrder(UUID orderId) {
        return retryConfirm(orderId);
    }

    @Retryable(
            retryFor = OptimisticLockException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    @Transactional
    public OrderResponse retryConfirm(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // 🔥 Deduct stock ONLY when we are 100% sure the money is ours
        inventoryService.deduct(order.getId(), order.getItems());

        order.confirm(); // Changes status to COMPLETED/PAID
        return mapToResponse(order);
    }

    @Transactional
    @Override
    public void cancelOrder(UUID orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getStatus() == OrderStatus.CANCELLED) return;

        order.cancel();

        log.info("Order cancelled {}", orderId);
    }

    private OrderResponse mapToResponse(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getStatus(),
                order.getTotalAmount()
        );
    }
}