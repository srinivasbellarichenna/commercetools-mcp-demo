package com.example.customer.service;

import com.commercetools.api.models.customer.Customer;
import com.commercetools.api.models.customer.CustomerSignInResult;
import com.commercetools.api.models.common.BaseAddress;
import reactor.core.publisher.Mono;

public interface CustomerService {
    Mono<CustomerSignInResult> registerCustomer(String email, String password, String firstName, String lastName);
    Mono<CustomerSignInResult> loginCustomer(String email, String password);
    Mono<Customer> getCustomerById(String customerId);
    Mono<Customer> getCustomerByEmail(String email);
    Mono<Customer> addAddress(String customerId, BaseAddress address);
    Mono<Customer> removeAddress(String customerId, String addressId);
    Mono<Customer> changeAddress(String customerId, String addressId, BaseAddress address);
    Mono<Customer> updateProfile(String customerId, String email, String firstName, String lastName);
    Mono<Customer> addPaymentMethod(String customerId, String paymentToken, String last4, String brand);
    Mono<com.commercetools.api.models.customer.CustomerPagedQueryResponse> getCustomers();
}
