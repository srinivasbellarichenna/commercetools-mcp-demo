package com.example.cart.service;

import com.commercetools.api.models.cart.Cart;
import com.commercetools.api.models.common.Address;
import com.commercetools.api.models.shipping_method.ShippingMethod;
import java.util.List;

public interface CartService {
    Cart getCartById(String cartId);
    Cart createCart(String currencyCode, String country);
    Cart addLineItemToCart(String cartId, String sku, Long quantity);
    Cart setShippingAddress(String cartId, Address address);
    Cart setBillingAddress(String cartId, Address address);
    Cart setShippingMethod(String cartId, String shippingMethodId);
    List<ShippingMethod> getShippingMethods(String cartId);
    Cart setCustomerId(String cartId, String customerId);
    Cart removeLineItem(String cartId, String lineItemId);
    Cart addPayment(String cartId, String paymentId);
}
