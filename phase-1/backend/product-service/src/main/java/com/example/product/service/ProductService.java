package com.example.product.service;

import com.commercetools.api.models.product.ProductProjectionPagedQueryResponse;
import com.commercetools.api.models.product.ProductProjectionPagedSearchResponse;

public interface ProductService {
    ProductProjectionPagedQueryResponse getProducts(int limit, int offset);
    ProductProjectionPagedSearchResponse searchProducts(String keyword, int limit, int offset);
    com.commercetools.api.models.product.ProductProjection getProductById(String id);
}
