package org.zgo.auth.application.port.in;

import org.zgo.auth.infrastructure.web.dto.request.LoginRequest;
import org.zgo.auth.infrastructure.web.dto.request.RefreshTokenRequest;
import org.zgo.auth.infrastructure.web.dto.request.RegisterRequest;
import org.zgo.auth.infrastructure.web.dto.response.AuthenticationResponse;

public interface AuthUseCase {
    AuthenticationResponse register(RegisterRequest request);

    AuthenticationResponse login(LoginRequest request);

    AuthenticationResponse refreshAccessToken(RefreshTokenRequest request);
}