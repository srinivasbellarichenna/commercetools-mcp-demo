package com.example.product.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI productServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Product Service API")
                        .description("Microservice for managing and searching products in the Commercetools catalog.")
                        .version("1.0")
                        .contact(new Contact()
                                .name("Commercetools Wrapper Team")
                                .email("support@example.com")));
    }
}
