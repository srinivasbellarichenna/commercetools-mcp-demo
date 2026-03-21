package com.example.customer.service;

import com.commercetools.api.client.ProjectApiRoot;
import com.commercetools.api.models.customer.Customer;
import com.commercetools.api.models.customer.CustomerDraftBuilder;
import com.commercetools.api.models.customer.*;
import com.commercetools.api.models.type.*;
import com.commercetools.api.models.common.BaseAddress;
import com.commercetools.api.models.common.LocalizedString;
import com.commercetools.api.models.common.LocalizedStringBuilder;
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
    public Customer getCustomerByEmail(String email) {
        return apiRoot.customers()
                .get()
                .withWhere("email = :email", "email", email)
                .executeBlocking()
                .getBody()
                .getResults()
                .stream()
                .findFirst()
                .orElse(null);
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
        ensureCustomTypeExists();
        Customer customer = getCustomerById(customerId);
        String paymentData = String.format("{\"token\":\"%s\", \"last4\":\"%s\", \"brand\":\"%s\"}", paymentToken, last4, brand);
        
        // If customer has no custom type, or it's different, we set the type
        if (customer.getCustom() == null || !customer.getCustom().getType().getObj().getKey().equals("customer-extra-info")) {
            return apiRoot.customers()
                    .withId(customerId)
                    .post(CustomerUpdateBuilder.of()
                            .version(customer.getVersion())
                            .plusActions(actionBuilder -> actionBuilder.setCustomTypeBuilder()
                                    .type(typeResourceIdentifierBuilder -> typeResourceIdentifierBuilder.key("customer-extra-info"))
                                    .fields(fieldsBuilder -> fieldsBuilder.addValue("savedPaymentMethods", paymentData)))
                            .build())
                    .executeBlocking()
                    .getBody();
        } else {
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

    private void ensureCustomTypeExists() {
        try {
            apiRoot.types().withKey("customer-extra-info").get().executeBlocking();
        } catch (Exception e) {
            // Type doesn't exist, create it
            LocalizedString name = LocalizedStringBuilder.of().addValue("en", "Customer Extra Info").build();
            apiRoot.types()
                    .post(TypeDraftBuilder.of()
                            .key("customer-extra-info")
                            .name(name)
                            .resourceTypeIds(List.of(ResourceTypeId.CUSTOMER))
                            .fieldDefinitions(List.of(
                                    FieldDefinitionBuilder.of()
                                            .name("savedPaymentMethods")
                                            .label(LocalizedStringBuilder.of().addValue("en", "Saved Payment Methods").build())
                                            .required(false)
                                            .type(CustomFieldStringTypeBuilder.of().build())
                                            .build()
                            ))
                            .build())
                    .executeBlocking();
        }
    }

    @Override
    public com.commercetools.api.models.customer.CustomerPagedQueryResponse getCustomers() {
        return apiRoot.customers().get().executeBlocking().getBody();
    }
}
