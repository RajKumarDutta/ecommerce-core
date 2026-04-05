package com.rdutta.ecommerceapp.product.dao;

import com.rdutta.ecommerceapp.common.dto.PageResponse;
import com.rdutta.ecommerceapp.product.dto.ProductResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface ProductSearch {
    ProductResponse getById(UUID id);

    List<ProductResponse> getAvailableProducts();

    PageResponse<ProductResponse> searchByName(String name, Pageable pageable);
}
