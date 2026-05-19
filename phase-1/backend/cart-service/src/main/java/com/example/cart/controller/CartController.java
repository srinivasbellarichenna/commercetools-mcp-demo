package com.example.cart.controller;

import com.commercetools.api.models.cart.Cart;
import com.example.cart.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import com.example.cart.dto.AddressRequestDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/carts")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @Operation(summary = "Get a Commercetools cart by ID")
    @GetMapping("/{cartId}")
    public ResponseEntity<Cart> getCartById(
            @Parameter(description = "The unique identifier of the cart") @PathVariable String cartId) {
        return ResponseEntity.ok(cartService.getCartById(cartId));
    }

    @PostMapping
    public ResponseEntity<Cart> createCart(
            @Parameter(description = "The 3-letter currency code (e.g., USD, EUR)") @RequestParam(defaultValue = "EUR") String currencyCode,
            @Parameter(description = "The 2-letter country code (e.g., US, DE)") @RequestParam(defaultValue = "DE") String country) {
        return ResponseEntity.ok(cartService.createCart(currencyCode, country));
    }

    @Operation(summary = "Add Item to Cart")
    @PostMapping("/{cartId}/items")
    public ResponseEntity<Cart> addItemToCart(
            @PathVariable String cartId,
            @RequestParam String sku,
            @RequestParam(defaultValue = "1") Long quantity) {
        return ResponseEntity.ok(cartService.addLineItemToCart(cartId, sku, quantity));
    }

    @Operation(summary = "Set Shipping Address")
    @PostMapping("/{cartId}/shipping-address")
    public ResponseEntity<Cart> setShippingAddress(
            @PathVariable String cartId,
            @Valid @RequestBody AddressRequestDTO addressDto) {
        return ResponseEntity.ok(cartService.setShippingAddress(cartId, mapToAddress(addressDto)));
    }

    @Operation(summary = "Set Billing Address")
    @PostMapping("/{cartId}/billing-address")
    public ResponseEntity<Cart> setBillingAddress(
            @PathVariable String cartId,
            @Valid @RequestBody AddressRequestDTO addressDto) {
        return ResponseEntity.ok(cartService.setBillingAddress(cartId, mapToAddress(addressDto)));
    }

    private com.commercetools.api.models.common.BaseAddress mapToAddress(AddressRequestDTO dto) {
        return com.commercetools.api.models.common.BaseAddress.builder()
                .country(dto.getCountry())
                .id(dto.getId())
                .key(dto.getKey())
                .title(dto.getTitle())
                .salutation(dto.getSalutation())
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .streetName(dto.getStreetName())
                .streetNumber(dto.getStreetNumber())
                .additionalStreetInfo(dto.getAdditionalStreetInfo())
                .postalCode(dto.getPostalCode())
                .city(dto.getCity())
                .region(dto.getRegion())
                .state(dto.getState())
                .company(dto.getCompany())
                .department(dto.getDepartment())
                .building(dto.getBuilding())
                .apartment(dto.getApartment())
                .pOBox(dto.getPOBox())
                .phone(dto.getPhone())
                .mobile(dto.getMobile())
                .email(dto.getEmail())
                .fax(dto.getFax())
                .additionalAddressInfo(dto.getAdditionalAddressInfo())
                .externalId(dto.getExternalId())
                .build();
    }

    @Operation(summary = "Set Shipping Method")
    @PostMapping("/{cartId}/shipping-method")
    public ResponseEntity<Cart> setShippingMethod(
            @PathVariable String cartId,
            @RequestParam String shippingMethodId) {
        return ResponseEntity.ok(cartService.setShippingMethod(cartId, shippingMethodId));
    }

    @Operation(summary = "Get Shipping Methods")
    @GetMapping("/{cartId}/shipping-methods")
    public ResponseEntity<java.util.List<com.commercetools.api.models.shipping_method.ShippingMethod>> getShippingMethods(
            @PathVariable String cartId) {
        return ResponseEntity.ok(cartService.getShippingMethods(cartId));
    }

    @Operation(summary = "Assign Customer to Cart")
    @PostMapping("/{cartId}/customer")
    public ResponseEntity<Cart> setCustomerId(
            @PathVariable String cartId,
            @RequestParam String customerId) {
        return ResponseEntity.ok(cartService.setCustomerId(cartId, customerId));
    }

    @Operation(summary = "Remove Item from Cart")
    @DeleteMapping("/{cartId}/items/{lineItemId}")
    public ResponseEntity<Cart> removeLineItem(
            @PathVariable String cartId,
            @PathVariable String lineItemId) {
        return ResponseEntity.ok(cartService.removeLineItem(cartId, lineItemId));
    }

    @Operation(summary = "Add Payment to Cart")
    @PostMapping("/{cartId}/payments")
    public ResponseEntity<Cart> addPayment(
            @PathVariable String cartId,
            @RequestParam String paymentId) {
        return ResponseEntity.ok(cartService.addPayment(cartId, paymentId));
    }
}
