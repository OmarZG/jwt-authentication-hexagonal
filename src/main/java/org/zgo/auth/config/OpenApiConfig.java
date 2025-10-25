package org.zgo.auth.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI securityHexOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("Security Hex API").version("v1")
                        .description("API for authentication with JWT (RSA) and refresh tokens"));
    }
}