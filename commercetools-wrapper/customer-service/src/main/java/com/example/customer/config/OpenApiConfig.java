package com.example.customer.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customerServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Customer Service API")
                        .description("Microservice for managing customer registration, login, and profiles.")
                        .version("1.0")
                        .contact(new Contact()
                                .name("Commercetools Wrapper Team")
                                .email("support@example.com")));
    }
}
