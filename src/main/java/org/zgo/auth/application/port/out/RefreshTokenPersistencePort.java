package org.zgo.auth.application.port.out;

import org.zgo.auth.domain.model.RefreshToken;

import java.util.Optional;

public interface RefreshTokenPersistencePort {
    RefreshToken save(RefreshToken refreshToken);

    Optional<RefreshToken> findByToken(String token);

    void delete(RefreshToken refreshToken);
}