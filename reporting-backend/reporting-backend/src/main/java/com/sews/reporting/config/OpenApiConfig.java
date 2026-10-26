package com.sews.reporting.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/**
 * Documentation API (Swagger UI / OpenAPI 3).
 *
 * Une fois l'application demarree :
 *   - UI interactive : http://localhost:8080/swagger-ui.html
 *   - Spec JSON brute : http://localhost:8080/v3/api-docs
 *
 * Tous les endpoints proteges par JWT sont documentes avec le schema de securite
 * "bearerAuth" ci-dessous : dans Swagger UI, cliquer sur "Authorize" et coller
 * le token JWT (sans le prefixe "Bearer ").
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Reporting Backend API - SEWS Cabind",
                version = "1.0.0",
                description = "API de la plateforme d'automatisation de generation de rapports industriels. "
                        + "Couvre l'authentification JWT, l'import Excel, la generation de rapports "
                        + "(PDF/Excel), l'autorisation d'acces aux rapports, la planification des envois "
                        + "email et l'integration SIGIP.",
                contact = @Contact(name = "Equipe Reporting - SEWS Cabind")
        ),
        security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class OpenApiConfig {
}
