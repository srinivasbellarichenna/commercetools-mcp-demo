package com.example.product.service;

import com.commercetools.api.client.ProjectApiRoot;
import com.commercetools.api.models.product.ProductProjectionPagedQueryResponse;
import com.commercetools.api.models.product.ProductProjectionPagedSearchResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProjectApiRoot apiRoot;

    @Override
    public Mono<ProductProjectionPagedQueryResponse> getProducts(int limit, int offset) {
        return Mono.fromFuture(() -> apiRoot.productProjections()
                .get()
                .withLimit(limit)
                .withOffset(offset)
                .execute()
                .thenApply(io.vrap.rmf.base.client.ApiHttpResponse::getBody));
    }

    @Override
    public Mono<ProductProjectionPagedSearchResponse> searchProducts(String keyword, int limit, int offset) {
        log.info("Searching products with keyword: {}, limit: {}, offset: {}", keyword, limit, offset);
        return Mono.fromFuture(() -> apiRoot.productProjections()
                .search()
                .post()
                .withText("en-US", keyword)
                .withStaged(false)
                .withLimit(limit)
                .withOffset(offset)
                .execute()
                .thenApply(io.vrap.rmf.base.client.ApiHttpResponse::getBody))
                .doOnError(e -> log.error("Error searching products: {}", e.getMessage(), e));
    }

    @Override
    public Mono<com.commercetools.api.models.product.ProductProjection> getProductById(String id) {
        return Mono.fromFuture(() -> apiRoot.productProjections()
                .withId(id)
                .get()
                .execute()
                .thenApply(io.vrap.rmf.base.client.ApiHttpResponse::getBody));
    }
}
