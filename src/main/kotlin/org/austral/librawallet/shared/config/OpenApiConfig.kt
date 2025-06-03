package org.austral.librawallet.shared.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.servers.Server
import org.springframework.boot.info.BuildProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig(
    private val buildProperties: BuildProperties,
) {

    @Bean
    fun customOpenAPI(): OpenAPI {
        return OpenAPI()
            .info(
                Info()
                    .title("Libra Wallet API")
                    .description("A comprehensive digital wallet API for managing accounts, transfers, transactions, and payments")
                    .version(buildProperties.version)
                    .contact(
                        Contact()
                            .name("Austral Team")
                            .email("support@austral.org"),
                    )
                    .license(
                        License()
                            .name("MIT License")
                            .url("https://opensource.org/licenses/MIT"),
                    ),
            )
            .servers(
                listOf(
                    Server()
                        .url("http://localhost:8080")
                        .description("Development server"),
                    // Server()
                    //     .url("https://api.librawallet.austral.org")
                    //     .description("Production server"),
                ),
            )
            .components(
                Components()
                    .addSecuritySchemes(
                        "bearerAuth",
                        SecurityScheme()
                            .type(SecurityScheme.Type.HTTP)
                            .scheme("bearer")
                            .bearerFormat("JWT")
                            .description("JWT token for authentication. Obtain it from the /api/auth/login endpoint."),
                    ),
            )
    }
}
