package com.example.cart.service;

import com.commercetools.api.models.cart.Cart;
import com.commercetools.api.models.common.Address;
import com.commercetools.api.models.shipping_method.ShippingMethod;
import java.util.List;

import reactor.core.publisher.Mono;

public interface CartService {
    Mono<Cart> getCartById(String cartId);
    Mono<Cart> createCart(String currencyCode, String country);
    Mono<Cart> addLineItemToCart(String cartId, String sku, Long quantity);
    Mono<Cart> setShippingAddress(String cartId, Address address);
    Mono<Cart> setBillingAddress(String cartId, Address address);
    Mono<Cart> setShippingMethod(String cartId, String shippingMethodId);
    Mono<List<ShippingMethod>> getShippingMethods(String cartId);
    Mono<Cart> setCustomerId(String cartId, String customerId);
    Mono<Cart> removeLineItem(String cartId, String lineItemId);
    Mono<Cart> addPayment(String cartId, String paymentId);
}
