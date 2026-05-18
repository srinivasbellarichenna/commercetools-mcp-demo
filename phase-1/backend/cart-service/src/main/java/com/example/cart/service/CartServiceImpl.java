package com.example.cart.service;

import com.commercetools.api.client.ProjectApiRoot;
import com.commercetools.api.models.cart.Cart;
import com.commercetools.api.models.cart.CartDraftBuilder;
import com.commercetools.api.models.cart.CartUpdateBuilder;
import com.commercetools.api.models.common.Address;
import com.commercetools.api.models.shipping_method.ShippingMethod;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final ProjectApiRoot apiRoot;

    @Override
    public Cart getCartById(String cartId) {
        return apiRoot.carts().withId(cartId).get().executeBlocking().getBody();
    }

    @Override
    public Cart createCart(String currencyCode, String country) {
        return apiRoot.carts()
                .post(CartDraftBuilder.of()
                        .currency(currencyCode)
                        .country(country)
                        .build())
                .executeBlocking()
                .getBody();
    }

    @Override
    public Cart addLineItemToCart(String cartId, String sku, Long quantity) {
        Cart cart = getCartById(cartId);
        return apiRoot.carts()
                .withId(cartId)
                .post(CartUpdateBuilder.of()
                        .version(cart.getVersion())
                        .plusActions(actionBuilder -> actionBuilder.addLineItemBuilder()
                                .sku(sku)
                                .quantity(quantity))
                        .build())
                .executeBlocking()
                .getBody();
    }

    @Override
    public Cart setShippingAddress(String cartId, Address address) {
        Cart cart = getCartById(cartId);
        return apiRoot.carts()
                .withId(cartId)
                .post(CartUpdateBuilder.of()
                        .version(cart.getVersion())
                        .plusActions(actionBuilder -> actionBuilder.setShippingAddressBuilder()
                                .address(address))
                        .build())
                .executeBlocking()
                .getBody();
    }

    @Override
    public Cart setBillingAddress(String cartId, Address address) {
        Cart cart = getCartById(cartId);
        return apiRoot.carts()
                .withId(cartId)
                .post(CartUpdateBuilder.of()
                        .version(cart.getVersion())
                        .plusActions(actionBuilder -> actionBuilder.setBillingAddressBuilder()
                                .address(address))
                        .build())
                .executeBlocking()
                .getBody();
    }

    @Override
    public Cart setShippingMethod(String cartId, String shippingMethodId) {
        Cart cart = getCartById(cartId);
        return apiRoot.carts()
                .withId(cartId)
                .post(CartUpdateBuilder.of()
                        .version(cart.getVersion())
                        .plusActions(actionBuilder -> actionBuilder.setShippingMethodBuilder()
                                .shippingMethod(shippingMethodBuilder -> shippingMethodBuilder.id(shippingMethodId)))
                        .build())
                .executeBlocking()
                .getBody();
    }

    @Override
    public List<ShippingMethod> getShippingMethods(String cartId) {
        return apiRoot.shippingMethods()
                .matchingCart()
                .get()
                .withCartId(cartId)
                .executeBlocking()
                .getBody()
                .getResults();
    }

    @Override
    public Cart setCustomerId(String cartId, String customerId) {
        Cart cart = getCartById(cartId);
        return apiRoot.carts()
                .withId(cartId)
                .post(CartUpdateBuilder.of()
                        .version(cart.getVersion())
                        .plusActions(actionBuilder -> actionBuilder.setCustomerIdBuilder()
                                .customerId(customerId))
                        .build())
                .executeBlocking()
                .getBody();
    }

    @Override
    public Cart removeLineItem(String cartId, String lineItemId) {
        Cart cart = getCartById(cartId);
        return apiRoot.carts()
                .withId(cartId)
                .post(CartUpdateBuilder.of()
                        .version(cart.getVersion())
                        .plusActions(actionBuilder -> actionBuilder.removeLineItemBuilder()
                                .lineItemId(lineItemId))
                        .build())
                .executeBlocking()
                .getBody();
    }

    @Override
    public Cart addPayment(String cartId, String paymentId) {
        Cart cart = getCartById(cartId);
        return apiRoot.carts()
                .withId(cartId)
                .post(CartUpdateBuilder.of()
                        .version(cart.getVersion())
                        .plusActions(actionBuilder -> actionBuilder.addPaymentBuilder()
                                .payment(paymentBuilder -> paymentBuilder.id(paymentId)))
                        .build())
                .executeBlocking()
                .getBody();
    }
}
