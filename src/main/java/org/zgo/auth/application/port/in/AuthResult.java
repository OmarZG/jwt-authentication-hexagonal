package org.zgo.auth.application.port.in;

public record AuthResult(
        String accessToken,
        String refreshToken
) {}