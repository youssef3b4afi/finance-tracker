package com.youssef3b4afi.financetracker.common;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Describes the API for springdoc, which builds the rest of the spec by scanning the controllers.
 * Swagger UI: /api/docs (redirects to /api/swagger-ui/index.html)   Raw spec: /api/docs/openapi.json
 */
@Configuration
public class OpenApiConfig {

    private static final String BEARER = "bearerAuth";

    @Bean
    OpenAPI financeTrackerOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Finance Tracker API")
                        .version("v1")
                        .description("Log in with POST /api/auth/login, then press Authorize and paste the token."))
                // Declares "Bearer JWT" as a way to authenticate; this is what adds the Authorize button.
                .components(new Components().addSecuritySchemes(BEARER, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")))
                // Applies it to every endpoint, so Swagger UI sends the token once you have authorized.
                // The public endpoints (login, register, health) simply ignore the header.
                .addSecurityItem(new SecurityRequirement().addList(BEARER));
    }
}
