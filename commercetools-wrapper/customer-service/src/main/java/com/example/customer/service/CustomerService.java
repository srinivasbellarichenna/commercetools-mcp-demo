package com.example.customer.service;

import com.commercetools.api.models.customer.Customer;
import com.commercetools.api.models.customer.CustomerSignInResult;
import com.commercetools.api.models.common.BaseAddress;

public interface CustomerService {
    CustomerSignInResult registerCustomer(String email, String password, String firstName, String lastName);
    CustomerSignInResult loginCustomer(String email, String password);
    Customer getCustomerById(String customerId);
    Customer addAddress(String customerId, BaseAddress address);
    Customer removeAddress(String customerId, String addressId);
    Customer changeAddress(String customerId, String addressId, BaseAddress address);
    Customer updateProfile(String customerId, String email, String firstName, String lastName);
    Customer addPaymentMethod(String customerId, String paymentToken, String last4, String brand);
}
