package org.zgo.auth.infrastructure.config.properties;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * Configuration properties for security settings.
 * Externalize configuration to application.yml/yaml
 */
@Data
@Validated
@Configuration
@ConfigurationProperties(prefix = "security")
public class SecurityProperties {

    /**
     * JWT related properties
     */
    private Jwt jwt = new Jwt();

    /**
     * Public endpoints that don't require authentication
     */
    @NotEmpty
    private List<String> publicEndpoints = List.of(
            "/api/auth/register",
            "/api/auth/login",
            "/api/auth/refresh"
    );

    /**
     * Swagger/OpenAPI endpoints
     */
    @NotEmpty
    private List<String> swaggerEndpoints = List.of(
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/swagger-resources/**",
            "/webjars/**"
    );

    /**
     * Development endpoints (should be disabled in production)
     */
    private List<String> devEndpoints = List.of("/h2-console/**");

    /**
     * BCrypt password encoder strength
     */
    @Positive
    private int passwordEncoderStrength = 10;

    @Data
    public static class Jwt {
        /**
         * JWT access token expiration in milliseconds (default: 15 minutes)
         */
        @NotNull
        @Positive
        private Long accessTokenExpiration = 900000L;

        /**
         * JWT refresh token expiration in milliseconds (default: 7 days)
         */
        @NotNull
        @Positive
        private Long refreshTokenExpiration = 604800000L;

        /**
         * Path to RSA private key
         */
        @NotEmpty
        private String privateKeyPath = "classpath:jwtKeys/private.key.pem";

        /**
         * Path to RSA public key
         */
        @NotEmpty
        private String publicKeyPath = "classpath:jwtKeys/public.key.pem";

        /**
         * JWT issuer claim
         */
        private String issuer = "zgo-auth-service";

        /**
         * JWT audience claim
         */
        private String audience = "zgo-api";
    }
}