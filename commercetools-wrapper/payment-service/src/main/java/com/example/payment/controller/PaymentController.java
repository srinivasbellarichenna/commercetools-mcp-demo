package com.example.payment.controller;

import com.example.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(summary = "Create a Stripe Checkout Session")
    @PostMapping("/checkout")
    public ResponseEntity<Map<String, String>> createCheckoutSession(
            @Parameter(description = "ID of the Commercetools Cart") @RequestParam String cartId,
            @Parameter(description = "Success redirect URL") @RequestParam(defaultValue = "http://localhost:5173/checkout/success") String successUrl,
            @Parameter(description = "Cancel redirect URL") @RequestParam(defaultValue = "http://localhost:5173/cart") String cancelUrl) {
            
        String checkoutUrl = paymentService.createCheckoutSession(cartId, successUrl, cancelUrl);
        return ResponseEntity.ok(Map.of("url", checkoutUrl));
    }

    @Operation(summary = "Get Stripe Session Details")
    @GetMapping("/session/{sessionId}")
    public ResponseEntity<com.stripe.model.checkout.Session> getSessionDetails(
            @PathVariable String sessionId) {
        return ResponseEntity.ok(paymentService.getSessionDetails(sessionId));
    }

    @Operation(summary = "Create Commercetools Payment")
    @PostMapping
    public ResponseEntity<com.commercetools.api.models.payment.Payment> createPayment(
            @RequestParam String cartId,
            @RequestParam String amount,
            @RequestParam String currency,
            @RequestParam String paymentMethod) {
        return ResponseEntity.ok(paymentService.createPayment(cartId, amount, currency, paymentMethod));
    }
}
