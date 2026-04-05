package com.rdutta.ecommerceapp.product.service;

import com.rdutta.ecommerceapp.common.dto.PageResponse;
import com.rdutta.ecommerceapp.product.dao.ProductSearch;
import com.rdutta.ecommerceapp.product.dto.ProductResponse;
import com.rdutta.ecommerceapp.product.entity.Product;
import com.rdutta.ecommerceapp.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductQueryService implements ProductSearch {

    private final ProductRepository productRepository;

    @Override
    public ProductResponse getById(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        return map(product);
    }


    @Override
    public List<ProductResponse> getAvailableProducts() {

        return productRepository.findByAvailableQuantityGreaterThan(0)
                .stream()
                .map(this::map)
                .toList();
    }

    @Override
    public PageResponse<ProductResponse> searchByName(String name, Pageable pageable) {

        var page = productRepository
                .findByNameContainingIgnoreCase(name, pageable)
                .map(this::map);

        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
    private ProductResponse map(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getAvailableQuantity()
        );
    }
}