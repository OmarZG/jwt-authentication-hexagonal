package org.zgo.auth.application.service;

import org.springframework.stereotype.Service;
import org.zgo.auth.application.port.in.RefreshTokenUseCase;
import org.zgo.auth.application.port.out.RefreshTokenPersistencePort;
import org.zgo.auth.domain.model.RefreshToken;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class RefreshTokenServiceImpl implements RefreshTokenUseCase {

    private final RefreshTokenPersistencePort persistence;

    public RefreshTokenServiceImpl(RefreshTokenPersistencePort persistence) {
        this.persistence = persistence;
    }

    @Override
    public RefreshToken createRefreshToken(String username) {
        RefreshToken rt = new RefreshToken();
        rt.setToken(UUID.randomUUID().toString());
        rt.setUsername(username);
        rt.setExpiresAt(Instant.now().plus(30, ChronoUnit.DAYS));
        rt.setRevoked(false);
        return persistence.save(rt);
    }

    @Override
    public boolean validateRefreshToken(String token) {
        return persistence.findByToken(token)
                .map(rt -> !rt.isRevoked() && rt.getExpiresAt().isAfter(Instant.now()))
                .orElse(false);
    }

    @Override
    public void revokeRefreshToken(String token) {
        persistence.findByToken(token).ifPresent(rt -> {
            rt.setRevoked(true);
            persistence.save(rt);
        });
    }
}