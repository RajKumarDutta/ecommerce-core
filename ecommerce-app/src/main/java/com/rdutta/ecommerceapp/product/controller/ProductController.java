package com.rdutta.ecommerceapp.product.controller;

import com.rdutta.ecommerceapp.common.dto.ApiResponse;
import com.rdutta.ecommerceapp.common.dto.PageResponse;
import com.rdutta.ecommerceapp.product.dao.ProductDao;
import com.rdutta.ecommerceapp.product.dao.ProductSearch;
import com.rdutta.ecommerceapp.product.dto.CreateProductRequest;
import com.rdutta.ecommerceapp.product.dto.ProductResponse;
import com.rdutta.ecommerceapp.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class ProductController {

    private final ProductDao productService;
    private final ProductSearch productSearch;

    @PostMapping
    public ApiResponse<ProductResponse> createProduct(
            @Valid @RequestBody CreateProductRequest request) {

        ProductResponse response = productService.createProduct(request);

        return ApiResponse.success(response, "Product created successfully");
    }

    @GetMapping("/{id}")
    public ApiResponse<ProductResponse> getProduct(@PathVariable UUID id) {

        return ApiResponse.success(
                productSearch.getById(id),
                "Product fetched successfully"
        );
    }


    @GetMapping("/available")
    public ApiResponse<List<ProductResponse>> getAvailableProducts() {

        return ApiResponse.success(
                productSearch.getAvailableProducts(),
                "Available products fetched"
        );
    }

    @GetMapping("/search")
    public ApiResponse<PageResponse<ProductResponse>> search(
            @RequestParam String name,
            Pageable pageable) {

        return ApiResponse.success(
                productSearch.searchByName(name, pageable),
                "Product fetched successfully"
        );
    }
}