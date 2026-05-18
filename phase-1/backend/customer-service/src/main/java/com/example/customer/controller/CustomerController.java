package com.example.customer.controller;

import com.commercetools.api.models.customer.Customer;
import com.commercetools.api.models.customer.CustomerSignInResult;
import com.example.customer.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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
    public ResponseEntity<Object> getCustomerById(
            @Parameter(description = "The unique identifier of the customer") @PathVariable String customerId) {
        Customer customer = customerService.getCustomerById(customerId);
        return ResponseEntity.ok(convertToMap(customer));
    }

    @Operation(summary = "Search Customer by Email")
    @GetMapping("/search")
    public ResponseEntity<Object> getCustomerByEmail(
            @Parameter(description = "The email address of the customer") @RequestParam String email) {
        Customer customer = customerService.getCustomerByEmail(email);
        return ResponseEntity.ok(convertToMap(customer));
    }

    private Map<String, Object> convertToMap(Customer customer) {
        if (customer == null) return null;
        Map<String, Object> map = new HashMap<>();
        map.put("id", customer.getId());
        map.put("email", customer.getEmail());
        map.put("firstName", customer.getFirstName());
        map.put("lastName", customer.getLastName());
        map.put("version", customer.getVersion());
        map.put("defaultShippingAddressId", customer.getDefaultShippingAddressId());
        map.put("defaultBillingAddressId", customer.getDefaultBillingAddressId());
        
        if (customer.getAddresses() != null) {
            List<Map<String, Object>> addressMaps = customer.getAddresses().stream()
                    .map(this::addressToMap)
                    .collect(Collectors.toList());
            map.put("addresses", addressMaps);
        }
        
        return map;
    }

    private Map<String, Object> addressToMap(com.commercetools.api.models.common.Address address) {
        if (address == null) return null;
        Map<String, Object> map = new HashMap<>();
        map.put("id", address.getId());
        map.put("firstName", address.getFirstName());
        map.put("lastName", address.getLastName());
        map.put("streetName", address.getStreetName());
        map.put("streetNumber", address.getStreetNumber());
        map.put("postalCode", address.getPostalCode());
        map.put("city", address.getCity());
        map.put("region", address.getRegion());
        map.put("state", address.getState());
        map.put("country", address.getCountry());
        map.put("company", address.getCompany());
        map.put("department", address.getDepartment());
        map.put("building", address.getBuilding());
        map.put("apartment", address.getApartment());
        map.put("phone", address.getPhone());
        map.put("mobile", address.getMobile());
        map.put("email", address.getEmail());
        return map;
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

    @Operation(summary = "Get all Customers")
    @GetMapping
    public ResponseEntity<com.commercetools.api.models.customer.CustomerPagedQueryResponse> getCustomers() {
        return ResponseEntity.ok(customerService.getCustomers());
    }
}
