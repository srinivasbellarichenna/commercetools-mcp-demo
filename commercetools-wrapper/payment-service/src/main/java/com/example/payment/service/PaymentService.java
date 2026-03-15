package com.example.payment.service;

public interface PaymentService {
    String createCheckoutSession(String cartId, String successUrl, String cancelUrl);
    com.stripe.model.checkout.Session getSessionDetails(String sessionId);
    com.commercetools.api.models.payment.Payment createPayment(String cartId, String amount, String currency, String paymentMethod);
}
