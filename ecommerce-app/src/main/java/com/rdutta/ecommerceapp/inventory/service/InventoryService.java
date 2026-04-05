package com.rdutta.ecommerceapp.inventory.service;

import com.rdutta.ecommerceapp.inventory.entity.InventoryReservation;
import com.rdutta.ecommerceapp.inventory.repository.InventoryReservationRepository;
import com.rdutta.ecommerceapp.order.entity.OrderItem;
import com.rdutta.ecommerceapp.product.dao.ProductClient;
import com.rdutta.ecommerceapp.product.entity.Product;
import com.rdutta.ecommerceapp.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryReservationRepository reservationRepository;
    private final ProductClient productClient;
    private final ProductRepository productRepository;

    @Transactional
    public void reserve(UUID orderId, List<OrderItem> items) {

        for (OrderItem item : items) {

            boolean reserved = productClient.reserveStock(
                    item.getProductId(),
                    item.getQuantity()
            );

            if (!reserved) {
                throw new RuntimeException("Stock not available");
            }

            reservationRepository.save(
                    new InventoryReservation(
                            item.getProductId(),
                            orderId,
                            item.getQuantity()
                    )
            );
        }
    }

    @Transactional
    public void confirm(UUID orderId) {

        var reservations = reservationRepository.findByOrderId(orderId);

        for (var r : reservations) {
            productClient.confirmStock(r.getProductId(), r.getQuantity());
            r.confirm();
        }
    }

    @Transactional
    public void deduct(UUID orderId, List<OrderItem> items) {
        log.info("Deducting inventory for order: {}", orderId);

        for (OrderItem item : items) {
            // 1. Fetch with a Write Lock (Pessimistic)
            // This prevents other threads from reading/writing until this finishes
            Product product = productRepository.findByIdWithLock(UUID.fromString(item.getProductId()))
                    .orElseThrow(() -> new RuntimeException("Product " + item.getProductId() + " not found"));

            // 2. Safety Check (Validation)
            if (product.getAvailableQuantity() < item.getQuantity()) {
                log.error("Stock insufficient for product {}. Required: {}, Available: {}",
                        product.getId(), item.getQuantity(), product.getAvailableQuantity());
                throw new RuntimeException("Insufficient stock for product: " + product.getName());
            }

            // 3. Atomic Deduction
            int newQuantity = product.getAvailableQuantity() - item.getQuantity();
            product.setAvailableQuantity(newQuantity);

            // 4. Save (Triggers the update within the transaction)
            productRepository.saveAndFlush(product);
        }
    }

    @Transactional
    public void release(UUID orderId) {

        var reservations = reservationRepository.findByOrderId(orderId);

        for (var r : reservations) {
            productClient.releaseStock(r.getProductId(), r.getQuantity());
            r.release();
        }
    }
}