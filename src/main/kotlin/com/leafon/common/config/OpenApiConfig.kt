package com.leafon.common.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {
    @Bean
    fun leafOnOpenApi(): OpenAPI =
        OpenAPI()
            .info(
                Info()
                    .title("LeafON API")
                    .description("API para gerenciamento de usuarios, smart pots, rotinas, telemetria e alertas.")
                    .version("0.0.1"),
            )
            .components(
                Components()
                    .addSecuritySchemes(
                        BEARER_AUTH,
                        SecurityScheme()
                            .name(BEARER_AUTH)
                            .type(SecurityScheme.Type.HTTP)
                            .scheme("bearer")
                            .bearerFormat("JWT")
                            .description("Informe o JWT de autenticacao no formato Bearer."),
                    ),
            )

    companion object {
        const val BEARER_AUTH = "bearerAuth"
    }
}
