package com.example.cart.config;

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
        System.out.println("Initializing Commercetools client for cart-service:");
        System.out.println("Project Key: " + projectKey);
        System.out.println("Auth URL (raw): " + authUrl);
        System.out.println("Auth URL (token): " + authUrl + "/oauth/token");
        System.out.println("API URL: " + apiUrl);

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
