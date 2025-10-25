package org.zgo.auth.application.port.in;

import org.zgo.auth.domain.model.RefreshToken;

public interface RefreshTokenUseCase {
    RefreshToken createRefreshToken(String username);

    boolean validateRefreshToken(String token);

    void revokeRefreshToken(String token);
}