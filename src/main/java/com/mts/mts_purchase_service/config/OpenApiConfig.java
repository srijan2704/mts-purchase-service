package com.mts.mts_purchase_service.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

/**
 * Central OpenAPI metadata configuration for Swagger UI.
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "MTS Purchase Service API",
                version = "v1",
                description = "APIs for sellers, product catalog/variants, purchase orders, and reports.",
                contact = @Contact(name = "MTS Purchase Service Team"),
                license = @License(name = "Internal Use")
        ),
        servers = {
                @Server(url = "http://localhost:8080", description = "Local")
        }
)
public class OpenApiConfig {
}
