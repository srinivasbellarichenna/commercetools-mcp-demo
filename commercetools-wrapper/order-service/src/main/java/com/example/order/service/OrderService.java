package com.example.order.service;

import com.commercetools.api.models.order.Order;
import com.commercetools.api.models.order.OrderPagedQueryResponse;

public interface OrderService {
    Order createOrderFromCart(String cartId, Long cartVersion);
    Order getOrderById(String orderId);
    OrderPagedQueryResponse getOrdersByCustomerId(String customerId);
}
