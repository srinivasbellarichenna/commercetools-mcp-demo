package com.example.order.service;

import com.commercetools.api.models.order.Order;
import com.commercetools.api.models.order.OrderPagedQueryResponse;
import reactor.core.publisher.Mono;

public interface OrderService {
    Mono<Order> createOrderFromCart(String cartId, Long cartVersion, String sessionId);
    Mono<Order> getOrderById(String orderId);
    Mono<OrderPagedQueryResponse> getOrdersByCustomerId(String customerId, Integer limit, Integer offset);
    Mono<OrderPagedQueryResponse> getOrderByCartId(String cartId);
}
