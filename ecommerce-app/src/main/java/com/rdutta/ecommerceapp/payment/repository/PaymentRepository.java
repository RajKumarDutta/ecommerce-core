package com.rdutta.ecommerceapp.payment.repository;

import com.rdutta.ecommerceapp.payment.entity.Payment;
import com.rdutta.ecommerceapp.payment.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    Optional<Payment> findByMerchantOrderId(String merchantOrderId);
    List<Payment> findByStatus(PaymentStatus status);
    List<Payment> findByStatusIn(List<PaymentStatus> activeStatuses);
}