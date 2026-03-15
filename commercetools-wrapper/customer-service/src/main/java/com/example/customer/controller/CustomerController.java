package com.example.customer.controller;

import com.commercetools.api.models.customer.Customer;
import com.commercetools.api.models.customer.CustomerSignInResult;
import com.example.customer.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @Operation(summary = "Register a new Customer")
    @PostMapping("/register")
    public ResponseEntity<CustomerSignInResult> registerCustomer(
            @Parameter(description = "User's email address") @RequestParam String email,
            @Parameter(description = "User's password") @RequestParam String password,
            @Parameter(description = "User's first name") @RequestParam(required = false) String firstName,
            @Parameter(description = "User's last name") @RequestParam(required = false) String lastName) {
        return ResponseEntity.ok(customerService.registerCustomer(email, password, firstName, lastName));
    }

    @Operation(summary = "Login an existing Customer")
    @PostMapping("/login")
    public ResponseEntity<CustomerSignInResult> loginCustomer(
            @Parameter(description = "User's email address") @RequestParam String email,
            @Parameter(description = "User's password") @RequestParam String password) {
        return ResponseEntity.ok(customerService.loginCustomer(email, password));
    }

    @Operation(summary = "Get Customer Profile")
    @GetMapping("/{customerId}")
    public ResponseEntity<Customer> getCustomerById(
            @Parameter(description = "The unique identifier of the customer") @PathVariable String customerId) {
        return ResponseEntity.ok(customerService.getCustomerById(customerId));
    }

    @Operation(summary = "Add Address to Customer Profile")
    @PostMapping("/{customerId}/addresses")
    public ResponseEntity<Customer> addAddress(
            @PathVariable String customerId,
            @RequestBody com.commercetools.api.models.common.BaseAddress address) {
        return ResponseEntity.ok(customerService.addAddress(customerId, address));
    }

    @Operation(summary = "Remove Address from Customer Profile")
    @DeleteMapping("/{customerId}/addresses/{addressId}")
    public ResponseEntity<Customer> removeAddress(
            @PathVariable String customerId,
            @PathVariable String addressId) {
        return ResponseEntity.ok(customerService.removeAddress(customerId, addressId));
    }

    @Operation(summary = "Update Customer Profile")
    @PatchMapping("/{customerId}/profile")
    public ResponseEntity<Customer> updateProfile(
            @PathVariable String customerId,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName) {
        return ResponseEntity.ok(customerService.updateProfile(customerId, email, firstName, lastName));
    }

    @Operation(summary = "Update Address in Customer Profile")
    @PatchMapping("/{customerId}/addresses/{addressId}")
    public ResponseEntity<Customer> changeAddress(
            @PathVariable String customerId,
            @PathVariable String addressId,
            @RequestBody com.commercetools.api.models.common.BaseAddress address) {
        return ResponseEntity.ok(customerService.changeAddress(customerId, addressId, address));
    }

    @Operation(summary = "Add Payment Method to Customer Profile")
    @PostMapping("/{customerId}/payments")
    public ResponseEntity<Customer> addPaymentMethod(
            @PathVariable String customerId,
            @RequestParam String paymentToken,
            @RequestParam String last4,
            @RequestParam String brand) {
        return ResponseEntity.ok(customerService.addPaymentMethod(customerId, paymentToken, last4, brand));
    }
}
