package com.example.payment.service;

public interface PaymentService {
    String createCheckoutSession(String cartId, String successUrl, String cancelUrl);
}
