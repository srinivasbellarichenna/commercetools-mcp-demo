package com.example.cart.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI cartServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Cart Service API")
                        .description("Microservice for managing shopping carts, line items, and addresses.")
                        .version("1.0")
                        .contact(new Contact()
                                .name("Commercetools Wrapper Team")
                                .email("support@example.com")));
    }
}
