package com.example.product.controller;

import com.commercetools.api.models.product.ProductProjectionPagedQueryResponse;
import com.commercetools.api.models.product.ProductProjectionPagedSearchResponse;
import com.example.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "Get a list of Commercetools products", description = "Fetches a paginated list of published products from the Commercetools catalog.")
    @GetMapping
    public Mono<ResponseEntity<ProductProjectionPagedQueryResponse>> getProducts(
            @Parameter(description = "The maximum number of products to return") @RequestParam(defaultValue = "20") int limit,
            @Parameter(description = "The number of products to skip for pagination") @RequestParam(defaultValue = "0") int offset) {
        log.info("Received request to fetch products: limit={}, offset={}", limit, offset);
        return productService.getProducts(limit, offset)
                .doOnNext(response -> log.info("Successfully fetched {} products", response.getCount()))
                .map(ResponseEntity::ok)
                .doOnError(e -> log.error("Error fetching products: {}", e.getMessage(), e));
    }

    @Operation(summary = "Search Commercetools products by keyword", description = "Searches the Commercetools catalog for products matching a given keyword.")
    @GetMapping("/search")
    public Mono<ResponseEntity<ProductProjectionPagedSearchResponse>> searchProducts(
            @Parameter(description = "The text keyword to search the catalog for") @RequestParam String keyword,
            @Parameter(description = "The maximum number of products to return") @RequestParam(defaultValue = "20") int limit,
            @Parameter(description = "The number of products to skip for pagination") @RequestParam(defaultValue = "0") int offset) {
        return productService.searchProducts(keyword, limit, offset).map(ResponseEntity::ok);
    }

    @Operation(summary = "Get a single product by ID", description = "Fetches a single product projection from the Commercetools catalog.")
    @GetMapping("/{id}")
    public Mono<ResponseEntity<com.commercetools.api.models.product.ProductProjection>> getProductById(
            @Parameter(description = "The unique identifier of the product") @PathVariable String id) {
        log.info("Received request to fetch product by ID: {}", id);
        return productService.getProductById(id)
                .doOnNext(product -> log.info("Successfully fetched product: {}", id))
                .map(ResponseEntity::ok)
                .doOnError(e -> log.error("Error fetching product {}: {}", id, e.getMessage()));
    }
}
