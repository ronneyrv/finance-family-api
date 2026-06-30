package com.ronney.finance.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(
                        new Info()
                                .title("Finance API")
                                .version("1.0.0")
                                .description("""
                                        Intelligent Personal Finance Management API.
                                        
                                        Main Features:
                                        - JWT Authentication
                                        - Financial Transactions
                                        - Dashboard
                                        - Financial Goals
                                        - Credit Cards
                                        - Installment Purchases
                                        - Monthly Invoices
                                        """)
                                .contact(new Contact()
                                        .name("Ronney Rocha")
                                        .url("https://github.com/ronneyrv")
                                )
                                .license(new License()
                                        .name("MIT License")
                                )
                )
                .addSecurityItem( new SecurityRequirement().addList(securitySchemeName))
                .schemaRequirement(securitySchemeName, new SecurityScheme()
                        .name(securitySchemeName)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                );
    }
}
