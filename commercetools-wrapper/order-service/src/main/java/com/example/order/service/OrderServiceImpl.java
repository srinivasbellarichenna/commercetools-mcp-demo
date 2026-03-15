package com.example.order.service;

import com.commercetools.api.client.ProjectApiRoot;
import com.commercetools.api.models.order.Order;
import com.commercetools.api.models.order.OrderFromCartDraftBuilder;
import com.commercetools.api.models.order.OrderPagedQueryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final ProjectApiRoot apiRoot;

    @Override
    public Order createOrderFromCart(String cartId, Long cartVersion) {
        return apiRoot.orders()
                .post(OrderFromCartDraftBuilder.of()
                        .id(cartId)
                        .version(cartVersion)
                        .build())
                .executeBlocking()
                .getBody();
    }

    @Override
    public Order getOrderById(String orderId) {
        return apiRoot.orders().withId(orderId).get().executeBlocking().getBody();
    }

    @Override
    public OrderPagedQueryResponse getOrdersByCustomerId(String customerId) {
        return apiRoot.orders()
                .get()
                .withWhere("customerId = :customerId", "customerId", customerId)
                .executeBlocking()
                .getBody();
    }
}
