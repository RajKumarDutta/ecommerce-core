package com.rdutta.ecommerceapp.product.dao;

import com.rdutta.ecommerceapp.product.dto.CreateProductRequest;
import com.rdutta.ecommerceapp.product.dto.ProductResponse;
import com.rdutta.ecommerceapp.product.entity.Product;

import java.util.UUID;

public interface ProductDao {

    ProductResponse createProduct(CreateProductRequest request);

    Product getProduct(UUID id);

    // 🔥 NEW METHODS
    boolean reserveStock(UUID productId, int quantity);

    void confirmStock(UUID productId, int quantity);

    void releaseStock(UUID productId, int quantity);

    // ⚠️ Optional (legacy)
    void reduceStock(UUID productId, int quantity);
}