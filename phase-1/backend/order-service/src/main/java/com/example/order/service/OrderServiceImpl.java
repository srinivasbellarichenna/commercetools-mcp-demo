package com.example.order.service;

import com.commercetools.api.client.ProjectApiRoot;
import com.commercetools.api.models.order.Order;
import com.commercetools.api.models.order.OrderFromCartDraftBuilder;
import com.commercetools.api.models.order.OrderPagedQueryResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import java.util.Map;

@Service
public class OrderServiceImpl implements OrderService {

    private final ProjectApiRoot apiRoot;
    private final WebClient webClient;

    public OrderServiceImpl(ProjectApiRoot apiRoot, @Value("${services.payment-service.url}") String paymentServiceUrl) {
        this.apiRoot = apiRoot;
        this.webClient = WebClient.builder().baseUrl(paymentServiceUrl).build();
    }

    @Override
    public Mono<Order> createOrderFromCart(String cartId, Long cartVersion, String sessionId) {
        return getOrderByCartId(cartId)
                .flatMap(response -> {
                    if (response.getResults() != null && !response.getResults().isEmpty()) {
                        return Mono.just(response.getResults().get(0));
                    }

                    if (sessionId == null || sessionId.trim().isEmpty()) {
                        return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Stripe checkout session ID is required to create a new order."));
                    }

                    return webClient.get()
                            .uri("/api/payments/session/{sessionId}", sessionId)
                            .retrieve()
                            .bodyToMono(Map.class)
                            .flatMap(session -> {
                                String paymentStatus = (String) session.get("payment_status");
                                String clientReferenceId = (String) session.get("client_reference_id");

                                if (!"paid".equals(paymentStatus)) {
                                    return Mono.error(new IllegalStateException("Stripe payment session status is not paid: " + paymentStatus));
                                }
                                if (clientReferenceId == null || !clientReferenceId.equals(cartId)) {
                                    return Mono.error(new IllegalArgumentException("Stripe session clientReferenceId (" + clientReferenceId + ") does not match cartId (" + cartId + ")"));
                                }

                                return Mono.fromFuture(() -> apiRoot.orders()
                                        .post(OrderFromCartDraftBuilder.of()
                                                .id(cartId)
                                                .version(cartVersion)
                                                .build())
                                        .execute()
                                        .thenApply(io.vrap.rmf.base.client.ApiHttpResponse::getBody));
                            });
                });
    }

    @Override
    public Mono<Order> getOrderById(String orderId) {
        return Mono.fromFuture(() -> apiRoot.orders().withId(orderId).get().execute().thenApply(io.vrap.rmf.base.client.ApiHttpResponse::getBody));
    }

    @Override
    public Mono<OrderPagedQueryResponse> getOrdersByCustomerId(String customerId, Integer limit, Integer offset) {
        com.commercetools.api.client.ByProjectKeyOrdersGet query = apiRoot.orders()
                .get()
                .withWhere("customerId = :customerId", "customerId", customerId);
        if (limit != null) {
            query = query.withLimit(limit);
        }
        if (offset != null) {
            query = query.withOffset(offset);
        }
        final com.commercetools.api.client.ByProjectKeyOrdersGet finalQuery = query;
        return Mono.fromFuture(() -> finalQuery.execute().thenApply(io.vrap.rmf.base.client.ApiHttpResponse::getBody));
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
