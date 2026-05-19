package com.example.payment.controller;

import com.example.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

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
    public Mono<ResponseEntity<Map<String, String>>> createCheckoutSession(
            @Parameter(description = "ID of the Commercetools Cart") @RequestParam String cartId,
            @Parameter(description = "Success redirect URL") @RequestParam(required = false) String successUrl,
            @Parameter(description = "Cancel redirect URL") @RequestParam(required = false) String cancelUrl) {
            
        String effectiveSuccessUrl = (successUrl != null && !successUrl.isEmpty()) ? successUrl : frontendUrl + "/checkout/success";
        String effectiveCancelUrl = (cancelUrl != null && !cancelUrl.isEmpty()) ? cancelUrl : frontendUrl + "/cart";
            
        return paymentService.createCheckoutSession(cartId, effectiveSuccessUrl, effectiveCancelUrl)
                .map(url -> ResponseEntity.ok(Map.of("url", url)));
    }

    @Operation(summary = "Get Stripe Session Details")
    @GetMapping("/session/{sessionId}")
    public Mono<ResponseEntity<com.stripe.model.checkout.Session>> getSessionDetails(
            @PathVariable String sessionId) {
        return paymentService.getSessionDetails(sessionId).map(ResponseEntity::ok);
    }

    @Operation(summary = "Create Commercetools Payment")
    @PostMapping
    public Mono<ResponseEntity<com.commercetools.api.models.payment.Payment>> createPayment(
            @RequestParam String cartId,
            @RequestParam String amount,
            @RequestParam String currency,
            @RequestParam String paymentMethod) {
        return paymentService.createPayment(cartId, amount, currency, paymentMethod).map(ResponseEntity::ok);
    }
}
