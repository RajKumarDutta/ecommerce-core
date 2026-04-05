package com.rdutta.ecommerceapp.product.adapter;

import com.rdutta.ecommerceapp.product.dao.ProductClient;
import com.rdutta.ecommerceapp.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ProductServiceAdapter implements ProductClient {

    private final ProductService productService;

    @Override
    public ProductInfo getProduct(String productId) {

        UUID id = UUID.fromString(productId);

        var product = productService.getProduct(id);

        return new ProductInfo(
                product.getId().toString(),
                product.getPrice(),
                product.getAvailableQuantity()
        );
    }

    // 🔥 STEP 1: RESERVE STOCK
    @Override
    public boolean reserveStock(String productId, int quantity) {

        UUID id = UUID.fromString(productId);

        return productService.reserveStock(id, quantity);
    }

    // 🔥 STEP 2: CONFIRM STOCK (FINAL DEDUCTION)
    @Override
    public void confirmStock(String productId, int quantity) {

        UUID id = UUID.fromString(productId);

        productService.confirmStock(id, quantity);
    }

    // 🔥 STEP 3: RELEASE STOCK (ROLLBACK)
    @Override
    public void releaseStock(String productId, int quantity) {

        UUID id = UUID.fromString(productId);

        productService.releaseStock(id, quantity);
    }

    // ⚠️ Legacy (optional)
    @Override
    public void reduceStock(String productId, int quantity) {

        UUID id = UUID.fromString(productId);

        productService.reduceStock(id, quantity);
    }
}