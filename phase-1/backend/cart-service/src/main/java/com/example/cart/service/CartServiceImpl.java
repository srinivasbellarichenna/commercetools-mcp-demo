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

    private Mono<Cart> executeUpdateWithRetry(String cartId, java.util.function.Function<Cart, Mono<Cart>> updateFunction) {
        return Mono.defer(() -> getCartById(cartId)
                .flatMap(updateFunction)
                .retryWhen(reactor.util.retry.Retry.max(3)
                        .filter(throwable -> {
                            if (throwable instanceof io.vrap.rmf.base.client.error.ConcurrentModificationException) {
                                return true;
                            }
                            if (throwable.getCause() instanceof io.vrap.rmf.base.client.error.ConcurrentModificationException) {
                                return true;
                            }
                            return false;
                        })
                ));
    }

    @Override
    public Mono<Cart> addLineItemToCart(String cartId, String sku, Long quantity) {
        return executeUpdateWithRetry(cartId, cart ->
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
        return executeUpdateWithRetry(cartId, cart ->
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
        return executeUpdateWithRetry(cartId, cart ->
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
        return executeUpdateWithRetry(cartId, cart ->
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
        if (customerId == null || customerId.trim().isEmpty()) {
            return executeUpdateWithRetry(cartId, cart ->
                Mono.fromFuture(() -> apiRoot.carts()
                    .withId(cartId)
                    .post(CartUpdateBuilder.of()
                            .version(cart.getVersion())
                            .plusActions(actionBuilder -> actionBuilder.setCustomerIdBuilder()
                                    .customerId(null))
                            .build())
                    .execute()
                    .thenApply(io.vrap.rmf.base.client.ApiHttpResponse::getBody))
            );
        }

        return Mono.fromFuture(() -> apiRoot.customers()
                .withId(customerId)
                .get()
                .execute()
                .thenApply(io.vrap.rmf.base.client.ApiHttpResponse::getBody))
                .flatMap(customer -> {
                    String email = customer.getEmail();
                    return executeUpdateWithRetry(cartId, cart ->
                        Mono.fromFuture(() -> apiRoot.carts()
                            .withId(cartId)
                            .post(CartUpdateBuilder.of()
                                    .version(cart.getVersion())
                                    .plusActions(actionBuilder -> actionBuilder.setCustomerIdBuilder()
                                            .customerId(customerId))
                                    .plusActions(actionBuilder -> actionBuilder.setCustomerEmailBuilder()
                                            .email(email))
                                    .build())
                            .execute()
                            .thenApply(io.vrap.rmf.base.client.ApiHttpResponse::getBody))
                    );
                })
                .onErrorResume(throwable -> {
                    return executeUpdateWithRetry(cartId, cart ->
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
                });
    }

    @Override
    public Mono<Cart> removeLineItem(String cartId, String lineItemId) {
        return executeUpdateWithRetry(cartId, cart ->
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
        return executeUpdateWithRetry(cartId, cart ->
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
