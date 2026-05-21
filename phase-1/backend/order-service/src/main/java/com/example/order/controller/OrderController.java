package com.example.order.controller;

import com.commercetools.api.models.order.Order;
import com.example.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "Create an Order from a Cart")
    @PostMapping("/from-cart")
    public Mono<ResponseEntity<Order>> createOrderFromCart(
            @Parameter(description = "ID of the cart to convert") @RequestParam String cartId,
            @Parameter(description = "Current version of the cart") @RequestParam Long cartVersion,
            @Parameter(description = "Stripe checkout session ID") @RequestParam(required = false) String sessionId) {
        return orderService.createOrderFromCart(cartId, cartVersion, sessionId).map(ResponseEntity::ok);
    }

    @Operation(summary = "Get Order by ID")
    @GetMapping("/{orderId}")
    public Mono<ResponseEntity<Order>> getOrderById(
            @Parameter(description = "The unique identifier of the order") @PathVariable String orderId) {
        return orderService.getOrderById(orderId).map(ResponseEntity::ok);
    }

    @Operation(summary = "Get Orders by Customer ID")
    @GetMapping("/customer/{customerId}")
    public Mono<ResponseEntity<com.commercetools.api.models.order.OrderPagedQueryResponse>> getOrdersByCustomerId(
            @Parameter(description = "The unique identifier of the customer") @PathVariable String customerId,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) Integer offset) {
        return orderService.getOrdersByCustomerId(customerId, limit, offset).map(ResponseEntity::ok);
    }

    @Operation(summary = "Get Order by Cart ID")
    @GetMapping("/cart/{cartId}")
    public Mono<ResponseEntity<com.commercetools.api.models.order.OrderPagedQueryResponse>> getOrderByCartId(
            @Parameter(description = "The unique identifier of the cart") @PathVariable String cartId) {
        return orderService.getOrderByCartId(cartId).map(ResponseEntity::ok);
    }
}
