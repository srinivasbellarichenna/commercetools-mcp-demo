package com.example.product.service;

import com.commercetools.api.models.product.ProductProjectionPagedQueryResponse;
import com.commercetools.api.models.product.ProductProjectionPagedSearchResponse;
import reactor.core.publisher.Mono;

public interface ProductService {
    Mono<ProductProjectionPagedSearchResponse> getProducts(String text, String sort, int limit, int offset);
    Mono<ProductProjectionPagedSearchResponse> searchProducts(String keyword, int limit, int offset);
    Mono<com.commercetools.api.models.product.ProductProjection> getProductById(String id);
}
