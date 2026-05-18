package com.example.order.controller;

import com.commercetools.api.models.order.Order;
import com.example.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "Create an Order from a Cart")
    @PostMapping("/from-cart")
    public ResponseEntity<Order> createOrderFromCart(
            @Parameter(description = "ID of the cart to convert") @RequestParam String cartId,
            @Parameter(description = "Current version of the cart") @RequestParam Long cartVersion) {
        return ResponseEntity.ok(orderService.createOrderFromCart(cartId, cartVersion));
    }

    @Operation(summary = "Get Order by ID")
    @GetMapping("/{orderId}")
    public ResponseEntity<Order> getOrderById(
            @Parameter(description = "The unique identifier of the order") @PathVariable String orderId) {
        return ResponseEntity.ok(orderService.getOrderById(orderId));
    }

    @Operation(summary = "Get Orders by Customer ID")
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<com.commercetools.api.models.order.OrderPagedQueryResponse> getOrdersByCustomerId(
            @Parameter(description = "The unique identifier of the customer") @PathVariable String customerId) {
        return ResponseEntity.ok(orderService.getOrdersByCustomerId(customerId));
    }

    @Operation(summary = "Get Order by Cart ID")
    @GetMapping("/cart/{cartId}")
    public ResponseEntity<com.commercetools.api.models.order.OrderPagedQueryResponse> getOrderByCartId(
            @Parameter(description = "The unique identifier of the cart") @PathVariable String cartId) {
        return ResponseEntity.ok(orderService.getOrderByCartId(cartId));
    }
}
