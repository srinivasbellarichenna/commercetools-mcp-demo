package com.example.customer.config;

import com.commercetools.api.client.ProjectApiRoot;
import com.commercetools.api.defaultconfig.ApiRootBuilder;
import io.vrap.rmf.base.client.oauth2.ClientCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommercetoolsConfig {

    @Value("${commercetools.projectKey}")
    private String projectKey;

    @Value("${commercetools.clientId}")
    private String clientId;

    @Value("${commercetools.clientSecret}")
    private String clientSecret;

    @Value("${commercetools.authUrl}")
    private String authUrl;

    @Value("${commercetools.apiUrl}")
    private String apiUrl;

    @Bean
    public ProjectApiRoot projectApiRoot() {
        return ApiRootBuilder.of()
                .defaultClient(ClientCredentials.of()
                                .withClientId(clientId)
                                .withClientSecret(clientSecret)
                                .build(),
                        authUrl + "/oauth/token",
                        apiUrl)
                .build(projectKey);
    }
}
