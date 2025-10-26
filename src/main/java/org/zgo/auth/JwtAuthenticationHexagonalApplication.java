package org.zgo.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.zgo.auth.infrastructure.config.properties.SecurityProperties;

@SpringBootApplication
@EnableConfigurationProperties(SecurityProperties.class)
public class JwtAuthenticationHexagonalApplication {

    public static void main(String[] args) {
        SpringApplication.run(JwtAuthenticationHexagonalApplication.class, args);
    }
}