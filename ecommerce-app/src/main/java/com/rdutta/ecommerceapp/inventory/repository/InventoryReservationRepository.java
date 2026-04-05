package com.rdutta.ecommerceapp.inventory.repository;

import com.rdutta.ecommerceapp.inventory.entity.InventoryReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface InventoryReservationRepository
        extends JpaRepository<InventoryReservation, UUID> {

    List<InventoryReservation> findByOrderId(UUID orderId);
}