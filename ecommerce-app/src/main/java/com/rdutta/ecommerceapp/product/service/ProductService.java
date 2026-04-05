package com.rdutta.ecommerceapp.product.service;

import com.rdutta.ecommerceapp.product.dao.ProductDao;
import com.rdutta.ecommerceapp.product.dto.*;
import com.rdutta.ecommerceapp.product.entity.Product;
import com.rdutta.ecommerceapp.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductService implements ProductDao {

    private final ProductRepository productRepository;

    // 🔹 CREATE
    @Override
    @Transactional
    public ProductResponse createProduct(CreateProductRequest request) {

        Product product = Product.create(
                request.name(),
                request.price(),
                request.quantity()
        );

        Product saved = productRepository.save(product);

        return mapToResponse(saved);
    }

    // 🔹 GET
    @Override
    @Transactional(readOnly = true)
    public Product getProduct(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }

    // 🔥 STEP 1: RESERVE (ATOMIC)
    @Override
    @Transactional
    public boolean reserveStock(UUID productId, int quantity) {

        int updatedRows = productRepository.reserveStock(productId, quantity);

        return updatedRows > 0;
    }

    // 🔥 STEP 2: CONFIRM
    @Override
    @Transactional
    public void confirmStock(UUID productId, int quantity) {
        // ✅ Already deducted during reserve
        // Optional: audit/log
    }

    // 🔥 STEP 3: RELEASE (ROLLBACK)
    @Override
    @Transactional
    public void releaseStock(UUID productId, int quantity) {

        productRepository.releaseStock(productId, quantity);
    }

    // ⚠️ LEGACY (can remove later)
    @Override
    @Transactional
    public void reduceStock(UUID productId, int quantity) {

        int updatedRows = productRepository.reserveStock(productId, quantity);

        if (updatedRows == 0) {
            throw new RuntimeException("Insufficient stock");
        }
    }

    private ProductResponse mapToResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getAvailableQuantity()
        );
    }
}