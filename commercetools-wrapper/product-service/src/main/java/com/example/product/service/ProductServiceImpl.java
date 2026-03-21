package com.example.product.service;

import com.commercetools.api.client.ProjectApiRoot;
import io.vrap.rmf.base.client.ApiHttpException;
import com.commercetools.api.models.product.ProductProjectionPagedQueryResponse;
import com.commercetools.api.models.product.ProductProjectionPagedSearchResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
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
        log.info("Searching products with keyword: {}, limit: {}, offset: {}", keyword, limit, offset);
        try {
            return apiRoot.productProjections()
                    .search()
                    .post()
                    .withText("en-US", keyword)
                    .withStaged(false)
                    .withLimit(limit)
                    .withOffset(offset)
                    .executeBlocking()
                    .getBody();
        } catch (ApiHttpException e) {
            log.error("Commercetools Search API error: {} - {}", e.getStatusCode(), e.getBody());
            throw e;
        } catch (Exception e) {
            log.error("Generic search error: {}", e.getMessage(), e);
            throw e;
        }
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
