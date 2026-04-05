package com.rdutta.ecommerceapp.common.repository;

import com.rdutta.ecommerceapp.common.entity.Idempotency;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface IdempotencyRepository extends JpaRepository<Idempotency, String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Idempotency i WHERE i.id = :id")
    Optional<Idempotency> findByIdForUpdate(String id);
}