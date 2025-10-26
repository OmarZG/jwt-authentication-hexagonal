package org.zgo.auth.application.port.in;

public interface AuthUseCase {
    AuthResult register(RegisterUserData data);

    AuthResult login(LoginParameters parameters);

    AuthResult refreshAccessToken(String refreshToken);
}