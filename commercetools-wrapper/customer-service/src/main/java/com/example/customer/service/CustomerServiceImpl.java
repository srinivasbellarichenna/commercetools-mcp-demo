package com.example.customer.service;

import com.commercetools.api.client.ProjectApiRoot;
import com.commercetools.api.models.customer.Customer;
import com.commercetools.api.models.customer.CustomerDraftBuilder;
import com.commercetools.api.models.customer.*;
import com.commercetools.api.models.common.BaseAddress;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final ProjectApiRoot apiRoot;

    @Override
    public CustomerSignInResult registerCustomer(String email, String password, String firstName, String lastName) {
        return apiRoot.customers()
                .post(CustomerDraftBuilder.of()
                        .email(email)
                        .password(password)
                        .firstName(firstName)
                        .lastName(lastName)
                        .build())
                .executeBlocking()
                .getBody();
    }

    @Override
    public CustomerSignInResult loginCustomer(String email, String password) {
        return apiRoot.login()
                .post(CustomerSigninBuilder.of()
                        .email(email)
                        .password(password)
                        .build())
                .executeBlocking()
                .getBody();
    }

    @Override
    public Customer getCustomerById(String customerId) {
        return apiRoot.customers().withId(customerId).get().executeBlocking().getBody();
    }

    @Override
    public Customer addAddress(String customerId, BaseAddress address) {
        Customer customer = getCustomerById(customerId);
        return apiRoot.customers()
                .withId(customerId)
                .post(CustomerUpdateBuilder.of()
                        .version(customer.getVersion())
                        .plusActions(actionBuilder -> actionBuilder.addAddressBuilder()
                                .address(address))
                        .build())
                .executeBlocking()
                .getBody();
    }

    @Override
    public Customer removeAddress(String customerId, String addressId) {
        Customer customer = getCustomerById(customerId);
        return apiRoot.customers()
                .withId(customerId)
                .post(CustomerUpdateBuilder.of()
                        .version(customer.getVersion())
                        .plusActions(actionBuilder -> actionBuilder.removeAddressBuilder()
                                .addressId(addressId))
                        .build())
                .executeBlocking()
                .getBody();
    }

    @Override
    public Customer updateProfile(String customerId, String email, String firstName, String lastName) {
        Customer customer = getCustomerById(customerId);
        List<CustomerUpdateAction> actions = new ArrayList<>();

        if (email != null) {
            actions.add(CustomerChangeEmailActionBuilder.of().email(email).build());
        }
        if (firstName != null) {
            actions.add(CustomerSetFirstNameActionBuilder.of().firstName(firstName).build());
        }
        if (lastName != null) {
            actions.add(CustomerSetLastNameActionBuilder.of().lastName(lastName).build());
        }

        return apiRoot.customers()
                .withId(customerId)
                .post(CustomerUpdateBuilder.of()
                        .version(customer.getVersion())
                        .actions(actions)
                        .build())
                .executeBlocking()
                .getBody();
    }
    @Override
    public Customer changeAddress(String customerId, String addressId, BaseAddress address) {
        Customer customer = getCustomerById(customerId);
        return apiRoot.customers()
                .withId(customerId)
                .post(CustomerUpdateBuilder.of()
                        .version(customer.getVersion())
                        .plusActions(actionBuilder -> actionBuilder.changeAddressBuilder()
                                .addressId(addressId)
                                .address(address))
                        .build())
                .executeBlocking()
                .getBody();
    }

    @Override
    public Customer addPaymentMethod(String customerId, String paymentToken, String last4, String brand) {
        Customer customer = getCustomerById(customerId);
        String paymentData = String.format("{\"token\":\"%s\", \"last4\":\"%s\", \"brand\":\"%s\"}", paymentToken, last4, brand);
        
        return apiRoot.customers()
                .withId(customerId)
                .post(CustomerUpdateBuilder.of()
                        .version(customer.getVersion())
                        .plusActions(actionBuilder -> actionBuilder.setCustomFieldBuilder()
                                .name("savedPaymentMethods")
                                .value(paymentData))
                        .build())
                .executeBlocking()
                .getBody();
    }
}
