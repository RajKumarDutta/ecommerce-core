package com.rdutta.ecommerceapp.product.dao;

import java.math.BigDecimal;

public interface ProductClient {

    ProductInfo getProduct(String productId);

    boolean reserveStock(String productId, int quantity);

    void confirmStock(String productId, int quantity);

    void releaseStock(String productId, int quantity);

    void reduceStock(String productId, int quantity); // optional (legacy)

    record ProductInfo(
            String productId,
            BigDecimal price,
            int availableQuantity
    ) {}
}