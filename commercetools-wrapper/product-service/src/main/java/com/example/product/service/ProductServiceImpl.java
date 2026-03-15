package com.example.product.service;

import com.commercetools.api.client.ProjectApiRoot;
import com.commercetools.api.models.product.ProductProjectionPagedQueryResponse;
import com.commercetools.api.models.product.ProductProjectionPagedSearchResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProjectApiRoot apiRoot;

    @Override
    public ProductProjectionPagedQueryResponse getProducts(int limit, int offset) {
        return apiRoot.productProjections()
                .get()
                .withLimit(limit)
                .withOffset(offset)
                .executeBlocking()
                .getBody();
    }

    @Override
    public ProductProjectionPagedSearchResponse searchProducts(String keyword, int limit, int offset) {
        return apiRoot.productProjections()
                .search()
                .get()
                .withText("en", keyword)
                .withLimit(limit)
                .withOffset(offset)
                .executeBlocking()
                .getBody();
    }

    @Override
    public com.commercetools.api.models.product.ProductProjection getProductById(String id) {
        return apiRoot.productProjections()
                .withId(id)
                .get()
                .executeBlocking()
                .getBody();
    }
}
