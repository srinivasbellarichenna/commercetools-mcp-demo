package com.example.payment.service;

import reactor.core.publisher.Mono;

public interface PaymentService {
    Mono<String> createCheckoutSession(String cartId, String successUrl, String cancelUrl);
    Mono<com.stripe.model.checkout.Session> getSessionDetails(String sessionId);
    Mono<com.commercetools.api.models.payment.Payment> createPayment(String cartId, String amount, String currency, String paymentMethod);
}
