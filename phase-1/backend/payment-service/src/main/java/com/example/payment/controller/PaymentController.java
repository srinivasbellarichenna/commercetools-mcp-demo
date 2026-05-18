package com.example.payment.controller;

import com.example.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    
    @Value("${frontend.url}")
    private String frontendUrl;

    @Operation(summary = "Create a Stripe Checkout Session")
    @PostMapping("/checkout")
    public ResponseEntity<Map<String, String>> createCheckoutSession(
            @Parameter(description = "ID of the Commercetools Cart") @RequestParam String cartId,
            @Parameter(description = "Success redirect URL") @RequestParam(required = false) String successUrl,
            @Parameter(description = "Cancel redirect URL") @RequestParam(required = false) String cancelUrl) {
            
        String effectiveSuccessUrl = (successUrl != null && !successUrl.isEmpty()) ? successUrl : frontendUrl + "/checkout/success";
        String effectiveCancelUrl = (cancelUrl != null && !cancelUrl.isEmpty()) ? cancelUrl : frontendUrl + "/cart";
            
        String checkoutUrl = paymentService.createCheckoutSession(cartId, effectiveSuccessUrl, effectiveCancelUrl);
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
