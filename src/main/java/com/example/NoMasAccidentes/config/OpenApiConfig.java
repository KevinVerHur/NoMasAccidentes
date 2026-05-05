package com.example.NoMasAccidentes.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configura la documentación interactiva en Swagger UI (/swagger-ui.html).
 * Define el esquema de seguridad "bearerAuth" para que se pueda probar
 * cualquier endpoint protegido pegando el JWT.
 */
@Configuration
public class OpenApiConfig {

    private static final String NOMBRE_ESQUEMA = "bearerAuth";

    @Bean
    public OpenAPI noMasAccidentesOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API No Más Accidentes")
                        .description("Sistema de gestión para consultoras de prevención de riesgos laborales (Ley 16.744)")
                        .version("v1")
                        .contact(new Contact().name("Equipo No Más Accidentes")))
                .addSecurityItem(new SecurityRequirement().addList(NOMBRE_ESQUEMA))
                .components(new Components()
                        .addSecuritySchemes(NOMBRE_ESQUEMA,
                                new SecurityScheme()
                                        .name(NOMBRE_ESQUEMA)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }
}
