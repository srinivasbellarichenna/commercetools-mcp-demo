package com.example.payment.service;

import com.commercetools.api.client.ProjectApiRoot;
import com.commercetools.api.models.cart.Cart;
import com.stripe.Stripe;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import jakarta.annotation.PostConstruct;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final ProjectApiRoot apiRoot;

    @Value("${stripe.secretKey}")
    private String stripeSecretKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
    }

    @Override
    public Mono<String> createCheckoutSession(String cartId, String successUrl, String cancelUrl) {
        return Mono.fromFuture(() -> apiRoot.carts().withId(cartId).get().execute().thenApply(io.vrap.rmf.base.client.ApiHttpResponse::getBody))
                .flatMap(cart -> Mono.fromCallable(() -> {
                    long amount = cart.getTotalPrice().getCentAmount();
                    String currency = cart.getTotalPrice().getCurrencyCode();

                    SessionCreateParams params = SessionCreateParams.builder()
                            .setMode(SessionCreateParams.Mode.PAYMENT)
                            .setSuccessUrl(successUrl + "?cartId=" + cartId + "&cartVersion=" + cart.getVersion() + "&sessionId={CHECKOUT_SESSION_ID}")
                            .setCancelUrl(cancelUrl)
                            .addLineItem(SessionCreateParams.LineItem.builder()
                                    .setQuantity(1L)
                                    .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                            .setCurrency(currency)
                                            .setUnitAmount(amount)
                                            .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                    .setName("Agentic Commerce Acquisition")
                                                    .build())
                                            .build())
                                    .build())
                            .build();

                    try {
                        Session session = Session.create(params);
                        return session.getUrl();
                    } catch (Exception e) {
                        throw new RuntimeException("Error creating Stripe session", e);
                    }
                }).subscribeOn(Schedulers.boundedElastic()));
    }
    
    @Override
    public Mono<Session> getSessionDetails(String sessionId) {
        return Mono.fromCallable(() -> {
            try {
                return Session.retrieve(sessionId);
            } catch (Exception e) {
                throw new RuntimeException("Error retrieving Stripe session", e);
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<com.commercetools.api.models.payment.Payment> createPayment(String cartId, String amount, String currency, String paymentMethod) {
        return Mono.fromFuture(() -> apiRoot.payments()
                .post(com.commercetools.api.models.payment.PaymentDraftBuilder.of()
                        .amountPlanned(com.commercetools.api.models.common.CentPrecisionMoneyDraftBuilder.of()
                                .centAmount(Long.parseLong(amount))
                                .currencyCode(currency)
                                .fractionDigits(2)
                                .build())
                        .paymentMethodInfo(com.commercetools.api.models.payment.PaymentMethodInfoBuilder.of()
                                .method(paymentMethod)
                                .paymentInterface("Stripe")
                                .build())
                        .build())
                .execute()
                .thenApply(io.vrap.rmf.base.client.ApiHttpResponse::getBody));
    }
}
