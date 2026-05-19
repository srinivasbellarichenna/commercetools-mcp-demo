package com.example.cart.service;

import com.commercetools.api.client.ProjectApiRoot;
import com.commercetools.api.models.cart.Cart;
import com.commercetools.api.models.cart.CartDraftBuilder;
import com.commercetools.api.models.cart.CartUpdateBuilder;
import com.commercetools.api.models.common.Address;
import com.commercetools.api.models.shipping_method.ShippingMethod;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final ProjectApiRoot apiRoot;

    @Override
    public Mono<Cart> getCartById(String cartId) {
        return Mono.fromFuture(() -> apiRoot.carts().withId(cartId).get().execute().thenApply(io.vrap.rmf.base.client.ApiHttpResponse::getBody));
    }

    @Override
    public Mono<Cart> createCart(String currencyCode, String country) {
        return Mono.fromFuture(() -> apiRoot.carts()
                .post(CartDraftBuilder.of()
                        .currency(currencyCode)
                        .country(country)
                        .build())
                .execute()
                .thenApply(io.vrap.rmf.base.client.ApiHttpResponse::getBody));
    }

    @Override
    public Mono<Cart> addLineItemToCart(String cartId, String sku, Long quantity) {
        return getCartById(cartId).flatMap(cart -> 
            Mono.fromFuture(() -> apiRoot.carts()
                .withId(cartId)
                .post(CartUpdateBuilder.of()
                        .version(cart.getVersion())
                        .plusActions(actionBuilder -> actionBuilder.addLineItemBuilder()
                                .sku(sku)
                                .quantity(quantity))
                        .build())
                .execute()
                .thenApply(io.vrap.rmf.base.client.ApiHttpResponse::getBody))
        );
    }

    @Override
    public Mono<Cart> setShippingAddress(String cartId, Address address) {
        return getCartById(cartId).flatMap(cart -> 
            Mono.fromFuture(() -> apiRoot.carts()
                .withId(cartId)
                .post(CartUpdateBuilder.of()
                        .version(cart.getVersion())
                        .plusActions(actionBuilder -> actionBuilder.setShippingAddressBuilder()
                                .address(address))
                        .build())
                .execute()
                .thenApply(io.vrap.rmf.base.client.ApiHttpResponse::getBody))
        );
    }

    @Override
    public Mono<Cart> setBillingAddress(String cartId, Address address) {
        return getCartById(cartId).flatMap(cart -> 
            Mono.fromFuture(() -> apiRoot.carts()
                .withId(cartId)
                .post(CartUpdateBuilder.of()
                        .version(cart.getVersion())
                        .plusActions(actionBuilder -> actionBuilder.setBillingAddressBuilder()
                                .address(address))
                        .build())
                .execute()
                .thenApply(io.vrap.rmf.base.client.ApiHttpResponse::getBody))
        );
    }

    @Override
    public Mono<Cart> setShippingMethod(String cartId, String shippingMethodId) {
        return getCartById(cartId).flatMap(cart -> 
            Mono.fromFuture(() -> apiRoot.carts()
                .withId(cartId)
                .post(CartUpdateBuilder.of()
                        .version(cart.getVersion())
                        .plusActions(actionBuilder -> actionBuilder.setShippingMethodBuilder()
                                .shippingMethod(shippingMethodBuilder -> shippingMethodBuilder.id(shippingMethodId)))
                        .build())
                .execute()
                .thenApply(io.vrap.rmf.base.client.ApiHttpResponse::getBody))
        );
    }

    @Override
    public Mono<List<ShippingMethod>> getShippingMethods(String cartId) {
        return Mono.fromFuture(() -> apiRoot.shippingMethods()
                .matchingCart()
                .get()
                .withCartId(cartId)
                .execute()
                .thenApply(resp -> resp.getBody().getResults()));
    }

    @Override
    public Mono<Cart> setCustomerId(String cartId, String customerId) {
        return getCartById(cartId).flatMap(cart -> 
            Mono.fromFuture(() -> apiRoot.carts()
                .withId(cartId)
                .post(CartUpdateBuilder.of()
                        .version(cart.getVersion())
                        .plusActions(actionBuilder -> actionBuilder.setCustomerIdBuilder()
                                .customerId(customerId))
                        .build())
                .execute()
                .thenApply(io.vrap.rmf.base.client.ApiHttpResponse::getBody))
        );
    }

    @Override
    public Mono<Cart> removeLineItem(String cartId, String lineItemId) {
        return getCartById(cartId).flatMap(cart -> 
            Mono.fromFuture(() -> apiRoot.carts()
                .withId(cartId)
                .post(CartUpdateBuilder.of()
                        .version(cart.getVersion())
                        .plusActions(actionBuilder -> actionBuilder.removeLineItemBuilder()
                                .lineItemId(lineItemId))
                        .build())
                .execute()
                .thenApply(io.vrap.rmf.base.client.ApiHttpResponse::getBody))
        );
    }

    @Override
    public Mono<Cart> addPayment(String cartId, String paymentId) {
        return getCartById(cartId).flatMap(cart -> 
            Mono.fromFuture(() -> apiRoot.carts()
                .withId(cartId)
                .post(CartUpdateBuilder.of()
                        .version(cart.getVersion())
                        .plusActions(actionBuilder -> actionBuilder.addPaymentBuilder()
                                .payment(paymentBuilder -> paymentBuilder.id(paymentId)))
                        .build())
                .execute()
                .thenApply(io.vrap.rmf.base.client.ApiHttpResponse::getBody))
        );
    }
}
