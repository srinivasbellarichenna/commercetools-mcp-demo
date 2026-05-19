package com.example.customer.controller;

import com.commercetools.api.models.customer.Customer;
import com.commercetools.api.models.customer.CustomerSignInResult;
import com.example.customer.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.example.customer.dto.AddressRequestDTO;
import com.example.customer.dto.CustomerLoginRequestDTO;
import com.example.customer.dto.CustomerRegisterRequestDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @Operation(summary = "Register a new Customer")
    @PostMapping("/register")
    public Mono<ResponseEntity<CustomerSignInResult>> registerCustomer(
            @Valid @RequestBody CustomerRegisterRequestDTO request) {
        return customerService.registerCustomer(
                request.getEmail(), request.getPassword(), request.getFirstName(), request.getLastName())
                .map(ResponseEntity::ok);
    }

    @Operation(summary = "Login an existing Customer")
    @PostMapping("/login")
    public Mono<ResponseEntity<CustomerSignInResult>> loginCustomer(
            @Valid @RequestBody CustomerLoginRequestDTO request) {
        return customerService.loginCustomer(request.getEmail(), request.getPassword())
                .map(ResponseEntity::ok);
    }

    @Operation(summary = "Get Customer Profile")
    @GetMapping("/{customerId}")
    public Mono<ResponseEntity<Customer>> getCustomerProfile(
            @PathVariable String customerId) {
        return customerService.getCustomerById(customerId)
                .map(ResponseEntity::ok);
    }

    @Operation(summary = "Get Customer by Email")
    @GetMapping("/email")
    public Mono<ResponseEntity<Customer>> getCustomerByEmail(
            @RequestParam String email) {
        return customerService.getCustomerByEmail(email)
                .map(customer -> {
                    if (customer == null) {
                        return ResponseEntity.notFound().build();
                    }
                    return ResponseEntity.ok(customer);
                });
    }

    @Operation(summary = "Update Customer Profile")
    @PatchMapping("/{customerId}")
    public Mono<ResponseEntity<Customer>> updateProfile(
            @PathVariable String customerId,
            @RequestBody Map<String, String> updates) {
        return customerService.updateProfile(
                customerId,
                updates.get("email"),
                updates.get("firstName"),
                updates.get("lastName"))
                .map(ResponseEntity::ok);
    }

    @Operation(summary = "Get Customer Addresses")
    @GetMapping("/{customerId}/addresses")
    public Mono<ResponseEntity<List<com.commercetools.api.models.common.Address>>> getAddresses(
            @PathVariable String customerId) {
        return customerService.getCustomerById(customerId)
                .map(Customer::getAddresses)
                .map(ResponseEntity::ok);
    }

    @Operation(summary = "Add Address to Customer Profile")
    @PostMapping("/{customerId}/addresses")
    public Mono<ResponseEntity<Customer>> addAddress(
            @PathVariable String customerId,
            @Valid @RequestBody AddressRequestDTO addressDto) {
        return customerService.addAddress(customerId, mapToAddress(addressDto))
                .map(ResponseEntity::ok);
    }

    @Operation(summary = "Remove Address from Customer Profile")
    @DeleteMapping("/{customerId}/addresses/{addressId}")
    public Mono<ResponseEntity<Customer>> removeAddress(
            @PathVariable String customerId,
            @PathVariable String addressId) {
        return customerService.removeAddress(customerId, addressId)
                .map(ResponseEntity::ok);
    }

    @Operation(summary = "Update Address in Customer Profile")
    @PatchMapping("/{customerId}/addresses/{addressId}")
    public Mono<ResponseEntity<Customer>> changeAddress(
            @PathVariable String customerId,
            @PathVariable String addressId,
            @Valid @RequestBody AddressRequestDTO addressDto) {
        return customerService.changeAddress(customerId, addressId, mapToAddress(addressDto))
                .map(ResponseEntity::ok);
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

    @Operation(summary = "Add Payment Method to Customer Profile")
    @PostMapping("/{customerId}/payment-methods")
    public Mono<ResponseEntity<Customer>> addPaymentMethod(
            @PathVariable String customerId,
            @RequestBody Map<String, String> payload) {
        return customerService.addPaymentMethod(
                customerId, 
                payload.get("paymentToken"), 
                payload.get("last4"), 
                payload.get("brand"))
                .map(ResponseEntity::ok);
    }

    @Operation(summary = "Get Saved Payment Methods")
    @GetMapping("/{customerId}/payment-methods")
    public Mono<ResponseEntity<List<Map<String, String>>>> getPaymentMethods(
            @PathVariable String customerId) {
        return customerService.getCustomerById(customerId)
                .map(customer -> {
                    if (customer.getCustom() == null || customer.getCustom().getFields() == null) {
                        return ResponseEntity.ok(List.of());
                    }
                    Object savedMethodsObj = customer.getCustom().getFields().values().get("savedPaymentMethods");
                    if (savedMethodsObj == null) {
                        return ResponseEntity.ok(List.of());
                    }
                    
                    try {
                        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                        String jsonString = savedMethodsObj.toString();
                        // This is a naive implementation, usually you'd store an array of objects
                        Map<String, String> paymentMap = mapper.readValue(jsonString, Map.class);
                        return ResponseEntity.ok(List.of(paymentMap));
                    } catch (Exception e) {
                        return ResponseEntity.ok(List.of());
                    }
                });
    }

    @Operation(summary = "Get all Customers")
    @GetMapping
    public Mono<ResponseEntity<com.commercetools.api.models.customer.CustomerPagedQueryResponse>> getCustomers() {
        return customerService.getCustomers().map(ResponseEntity::ok);
    }
}
