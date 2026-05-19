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
    public Mono<ProductProjectionPagedSearchResponse> getProducts(String text, String sort, int limit, int offset) {
        return Mono.fromFuture(() -> {
            var request = apiRoot.productProjections()
                    .search()
                    .post()
                    .withStaged(false)
                    .withLimit(limit)
                    .withOffset(offset);
                    
            if (text != null && !text.isEmpty()) {
                request = request.withText("en-US", text);
            }
            if (sort != null && !sort.isEmpty()) {
                request = request.withSort(sort);
            }
            
            return request.execute()
                    .thenApply(io.vrap.rmf.base.client.ApiHttpResponse::getBody);
        }).doOnError(e -> log.error("Error fetching products with search: {}", e.getMessage(), e));
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
