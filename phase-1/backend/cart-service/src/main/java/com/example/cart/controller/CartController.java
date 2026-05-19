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
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/carts")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @Operation(summary = "Get a Commercetools cart by ID")
    @GetMapping("/{cartId}")
    public Mono<ResponseEntity<Cart>> getCartById(
            @Parameter(description = "The unique identifier of the cart") @PathVariable String cartId) {
        return cartService.getCartById(cartId).map(ResponseEntity::ok);
    }

    @PostMapping
    public Mono<ResponseEntity<Cart>> createCart(
            @Parameter(description = "The 3-letter currency code (e.g., USD, EUR)") @RequestParam(defaultValue = "EUR") String currencyCode,
            @Parameter(description = "The 2-letter country code (e.g., US, DE)") @RequestParam(defaultValue = "DE") String country) {
        return cartService.createCart(currencyCode, country).map(ResponseEntity::ok);
    }

    @Operation(summary = "Add Item to Cart")
    @PostMapping("/{cartId}/items")
    public Mono<ResponseEntity<Cart>> addItemToCart(
            @PathVariable String cartId,
            @RequestParam String sku,
            @RequestParam(defaultValue = "1") Long quantity) {
        return cartService.addLineItemToCart(cartId, sku, quantity).map(ResponseEntity::ok);
    }

    @Operation(summary = "Set Shipping Address")
    @PostMapping("/{cartId}/shipping-address")
    public Mono<ResponseEntity<Cart>> setShippingAddress(
            @PathVariable String cartId,
            @Valid @RequestBody AddressRequestDTO addressDto) {
        return cartService.setShippingAddress(cartId, mapToAddress(addressDto)).map(ResponseEntity::ok);
    }

    @Operation(summary = "Set Billing Address")
    @PostMapping("/{cartId}/billing-address")
    public Mono<ResponseEntity<Cart>> setBillingAddress(
            @PathVariable String cartId,
            @Valid @RequestBody AddressRequestDTO addressDto) {
        return cartService.setBillingAddress(cartId, mapToAddress(addressDto)).map(ResponseEntity::ok);
    }

    private com.commercetools.api.models.common.Address mapToAddress(AddressRequestDTO dto) {
        return com.commercetools.api.models.common.Address.builder()
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
    public Mono<ResponseEntity<Cart>> setShippingMethod(
            @PathVariable String cartId,
            @RequestParam String shippingMethodId) {
        return cartService.setShippingMethod(cartId, shippingMethodId).map(ResponseEntity::ok);
    }

    @Operation(summary = "Get Shipping Methods")
    @GetMapping("/{cartId}/shipping-methods")
    public Mono<ResponseEntity<java.util.List<com.commercetools.api.models.shipping_method.ShippingMethod>>> getShippingMethods(
            @PathVariable String cartId) {
        return cartService.getShippingMethods(cartId).map(ResponseEntity::ok);
    }

    @Operation(summary = "Assign Customer to Cart")
    @PostMapping("/{cartId}/customer")
    public Mono<ResponseEntity<Cart>> setCustomerId(
            @PathVariable String cartId,
            @RequestParam String customerId) {
        return cartService.setCustomerId(cartId, customerId).map(ResponseEntity::ok);
    }

    @Operation(summary = "Remove Item from Cart")
    @DeleteMapping("/{cartId}/items/{lineItemId}")
    public Mono<ResponseEntity<Cart>> removeLineItem(
            @PathVariable String cartId,
            @PathVariable String lineItemId) {
        return cartService.removeLineItem(cartId, lineItemId).map(ResponseEntity::ok);
    }

    @Operation(summary = "Add Payment to Cart")
    @PostMapping("/{cartId}/payments")
    public Mono<ResponseEntity<Cart>> addPayment(
            @PathVariable String cartId,
            @RequestParam String paymentId) {
        return cartService.addPayment(cartId, paymentId).map(ResponseEntity::ok);
    }
}
