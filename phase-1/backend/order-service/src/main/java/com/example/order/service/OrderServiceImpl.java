package com.example.order.service;

import com.commercetools.api.client.ProjectApiRoot;
import com.commercetools.api.models.order.Order;
import com.commercetools.api.models.order.OrderFromCartDraftBuilder;
import com.commercetools.api.models.order.OrderPagedQueryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final ProjectApiRoot apiRoot;

    @Override
    public Mono<Order> createOrderFromCart(String cartId, Long cartVersion) {
        return Mono.fromFuture(() -> apiRoot.orders()
                .post(OrderFromCartDraftBuilder.of()
                        .id(cartId)
                        .version(cartVersion)
                        .build())
                .execute()
                .thenApply(io.vrap.rmf.base.client.ApiHttpResponse::getBody));
    }

    @Override
    public Mono<Order> getOrderById(String orderId) {
        return Mono.fromFuture(() -> apiRoot.orders().withId(orderId).get().execute().thenApply(io.vrap.rmf.base.client.ApiHttpResponse::getBody));
    }

    @Override
    public Mono<OrderPagedQueryResponse> getOrdersByCustomerId(String customerId) {
        return Mono.fromFuture(() -> apiRoot.orders()
                .get()
                .withWhere("customerId = :customerId", "customerId", customerId)
                .execute()
                .thenApply(io.vrap.rmf.base.client.ApiHttpResponse::getBody));
    }

    @Override
    public Mono<OrderPagedQueryResponse> getOrderByCartId(String cartId) {
        return Mono.fromFuture(() -> apiRoot.orders()
                .get()
                .withWhere("cart(id = :cartId)", "cartId", cartId)
                .execute()
                .thenApply(io.vrap.rmf.base.client.ApiHttpResponse::getBody));
    }
}
