package com.miempresa.productos.config;



import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@SecurityScheme(
    name = "apiKey",
    type = SecuritySchemeType.APIKEY,
    in = SecuritySchemeIn.HEADER,
    paramName = "x-api-key"
)
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("API de Productos")
                .version("1.0")
                .description("Microservicio de gestión de productos con seguridad por API Key"))
            .addSecurityItem(new SecurityRequirement().addList("apiKey"))
            .components(new Components()
                .addSecuritySchemes("apiKey",
                    new io.swagger.v3.oas.models.security.SecurityScheme()
                        .name("x-api-key")
                        .type(io.swagger.v3.oas.models.security.SecurityScheme.Type.APIKEY)
                        .in(io.swagger.v3.oas.models.security.SecurityScheme.In.HEADER)
                )
            );
    }
}
