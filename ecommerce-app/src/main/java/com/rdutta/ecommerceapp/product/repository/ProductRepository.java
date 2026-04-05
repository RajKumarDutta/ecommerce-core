package com.rdutta.ecommerceapp.product.repository;

import com.rdutta.ecommerceapp.product.entity.Product;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    /**
     * Reduces the stock for a specific product if sufficient quantity is available.
     * Returns 1 if successful, 0 if insufficient stock or ID not found.
     */
    @Modifying
    @Query("""
        UPDATE Product p 
        SET p.availableQuantity = p.availableQuantity - :qty 
        WHERE p.id = :id AND p.availableQuantity >= :qty
        """)
    int reduceStock(@Param("id") UUID id, @Param("qty") int qty);

    /**
     * Reserves stock for a product. Logical equivalent to reduceStock.
     */
    @Modifying
    @Query("""
        UPDATE Product p 
        SET p.availableQuantity = p.availableQuantity - :quantity 
        WHERE p.id = :productId AND p.availableQuantity >= :quantity
        """)
    int reserveStock(@Param("productId") UUID productId, @Param("quantity") int quantity);

    /**
     * Increases the stock back (e.g., on order cancellation or timeout).
     */
    @Modifying
    @Query("""
        UPDATE Product p 
        SET p.availableQuantity = p.availableQuantity + :quantity 
        WHERE p.id = :productId
        """)
    int releaseStock(@Param("productId") UUID productId, @Param("quantity") int quantity);

    // --- Derived Query Methods ---

    List<Product> findByAvailableQuantityGreaterThan(int quantity);

    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE) // 🔥 This is the "Senior" move for consistency
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Optional<Product> findByIdWithLock(@Param("id") UUID id);
}