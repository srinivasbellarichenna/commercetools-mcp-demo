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
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final ProjectApiRoot apiRoot;
    private final java.util.concurrent.atomic.AtomicReference<String> cachedTypeId = new java.util.concurrent.atomic.AtomicReference<>(null);

    @Override
    public Mono<CustomerSignInResult> registerCustomer(String email, String password, String firstName, String lastName) {
        return Mono.fromFuture(() -> apiRoot.customers()
                .post(CustomerDraftBuilder.of()
                        .email(email)
                        .password(password)
                        .firstName(firstName)
                        .lastName(lastName)
                        .build())
                .execute()
                .thenApply(io.vrap.rmf.base.client.ApiHttpResponse::getBody));
    }

    @Override
    public Mono<CustomerSignInResult> loginCustomer(String email, String password) {
        return Mono.fromFuture(() -> apiRoot.login()
                .post(CustomerSigninBuilder.of()
                        .email(email)
                        .password(password)
                        .build())
                .execute()
                .thenApply(io.vrap.rmf.base.client.ApiHttpResponse::getBody));
    }

    @Override
    public Mono<Customer> getCustomerById(String customerId) {
        return Mono.fromFuture(() -> apiRoot.customers().withId(customerId).get().execute().thenApply(io.vrap.rmf.base.client.ApiHttpResponse::getBody));
    }

    @Override
    public Mono<Customer> getCustomerByEmail(String email) {
        return Mono.fromFuture(() -> apiRoot.customers()
                .get()
                .withWhere("email = :email", "email", email)
                .execute()
                .thenApply(resp -> resp.getBody().getResults().stream().findFirst().orElse(null)));
    }

    @Override
    public Mono<Customer> addAddress(String customerId, BaseAddress address) {
        return getCustomerById(customerId).flatMap(customer -> 
            Mono.fromFuture(() -> apiRoot.customers()
                .withId(customerId)
                .post(CustomerUpdateBuilder.of()
                        .version(customer.getVersion())
                        .plusActions(actionBuilder -> actionBuilder.addAddressBuilder()
                                .address(address))
                        .build())
                .execute()
                .thenApply(io.vrap.rmf.base.client.ApiHttpResponse::getBody))
        );
    }

    @Override
    public Mono<Customer> removeAddress(String customerId, String addressId) {
        return getCustomerById(customerId).flatMap(customer -> 
            Mono.fromFuture(() -> apiRoot.customers()
                .withId(customerId)
                .post(CustomerUpdateBuilder.of()
                        .version(customer.getVersion())
                        .plusActions(actionBuilder -> actionBuilder.removeAddressBuilder()
                                .addressId(addressId))
                        .build())
                .execute()
                .thenApply(io.vrap.rmf.base.client.ApiHttpResponse::getBody))
        );
    }

    @Override
    public Mono<Customer> updateProfile(String customerId, String email, String firstName, String lastName) {
        return getCustomerById(customerId).flatMap(customer -> {
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

            return Mono.fromFuture(() -> apiRoot.customers()
                    .withId(customerId)
                    .post(CustomerUpdateBuilder.of()
                            .version(customer.getVersion())
                            .actions(actions)
                            .build())
                    .execute()
                    .thenApply(io.vrap.rmf.base.client.ApiHttpResponse::getBody));
        });
    }

    @Override
    public Mono<Customer> changeAddress(String customerId, String addressId, BaseAddress address) {
        return getCustomerById(customerId).flatMap(customer -> 
            Mono.fromFuture(() -> apiRoot.customers()
                .withId(customerId)
                .post(CustomerUpdateBuilder.of()
                        .version(customer.getVersion())
                        .plusActions(actionBuilder -> actionBuilder.changeAddressBuilder()
                                .addressId(addressId)
                                .address(address))
                        .build())
                .execute()
                .thenApply(io.vrap.rmf.base.client.ApiHttpResponse::getBody))
        );
    }

    @Override
    public Mono<Customer> addPaymentMethod(String customerId, String paymentToken, String last4, String brand) {
        return ensureCustomTypeExists().then(getCustomerById(customerId)).flatMap(customer -> {
            String paymentData = String.format("{\"token\":\"%s\", \"last4\":\"%s\", \"brand\":\"%s\"}", paymentToken, last4, brand);
            
            boolean hasTargetType = false;
            if (customer.getCustom() != null && customer.getCustom().getType() != null) {
                String currentTypeId = customer.getCustom().getType().getId();
                String targetTypeId = cachedTypeId.get();
                if (currentTypeId != null && currentTypeId.equals(targetTypeId)) {
                    hasTargetType = true;
                }
            }

            if (!hasTargetType) {
                return Mono.fromFuture(() -> apiRoot.customers()
                        .withId(customerId)
                        .post(CustomerUpdateBuilder.of()
                                .version(customer.getVersion())
                                .plusActions(actionBuilder -> actionBuilder.setCustomTypeBuilder()
                                        .type(typeResourceIdentifierBuilder -> typeResourceIdentifierBuilder.key("customer-extra-info"))
                                        .fields(fieldsBuilder -> fieldsBuilder.addValue("savedPaymentMethods", paymentData)))
                                .build())
                        .execute()
                        .thenApply(io.vrap.rmf.base.client.ApiHttpResponse::getBody));
            } else {
                return Mono.fromFuture(() -> apiRoot.customers()
                        .withId(customerId)
                        .post(CustomerUpdateBuilder.of()
                                .version(customer.getVersion())
                                .plusActions(actionBuilder -> actionBuilder.setCustomFieldBuilder()
                                        .name("savedPaymentMethods")
                                        .value(paymentData))
                                .build())
                        .execute()
                        .thenApply(io.vrap.rmf.base.client.ApiHttpResponse::getBody));
            }
        });
    }

    private Mono<Void> ensureCustomTypeExists() {
        if (cachedTypeId.get() != null) {
            return Mono.empty();
        }
        return Mono.fromFuture(() -> apiRoot.types().withKey("customer-extra-info").get().execute())
                .map(response -> response.getBody().getId())
                .doOnNext(cachedTypeId::set)
                .then()
                .onErrorResume(e -> {
                    LocalizedString name = LocalizedStringBuilder.of().addValue("en", "Customer Extra Info").build();
                    return Mono.fromFuture(() -> apiRoot.types()
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
                            .execute())
                            .map(response -> response.getBody().getId())
                            .doOnNext(cachedTypeId::set)
                            .then();
                });
    }

    @Override
    public Mono<com.commercetools.api.models.customer.CustomerPagedQueryResponse> getCustomers() {
        return Mono.fromFuture(() -> apiRoot.customers().get().execute().thenApply(io.vrap.rmf.base.client.ApiHttpResponse::getBody));
    }
}
