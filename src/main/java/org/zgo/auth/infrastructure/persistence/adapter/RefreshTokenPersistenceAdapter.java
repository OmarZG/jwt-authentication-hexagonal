package org.zgo.auth.infrastructure.persistence.adapter;

import org.springframework.stereotype.Component;
import org.zgo.auth.application.port.out.RefreshTokenPersistencePort;
import org.zgo.auth.domain.model.RefreshToken;
import org.zgo.auth.infrastructure.persistence.entity.RefreshTokenEntity;
import org.zgo.auth.infrastructure.persistence.repository.RefreshTokenRepository;

import java.util.Optional;

@Component
public class RefreshTokenPersistenceAdapter implements RefreshTokenPersistencePort {

    private final RefreshTokenRepository repository;

    public RefreshTokenPersistenceAdapter(RefreshTokenRepository repository) {
        this.repository = repository;
    }

    @Override
    public RefreshToken save(RefreshToken refreshToken) {
        RefreshTokenEntity e = new RefreshTokenEntity();
        e.setId(refreshToken.getId());
        e.setToken(refreshToken.getToken());
        e.setUsername(refreshToken.getUsername());
        e.setExpiresAt(refreshToken.getExpiresAt());
        e.setRevoked(refreshToken.isRevoked());
        RefreshTokenEntity saved = repository.save(e);
        return toDomain(saved);
    }

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        return repository.findByToken(token).map(this::toDomain);
    }

    @Override
    public void delete(RefreshToken refreshToken) {
        // optional
        repository.findByToken(refreshToken.getToken()).ifPresent(repository::delete);
    }

    private RefreshToken toDomain(RefreshTokenEntity e) {
        RefreshToken rt = new RefreshToken();
        rt.setId(e.getId());
        rt.setToken(e.getToken());
        rt.setUsername(e.getUsername());
        rt.setExpiresAt(e.getExpiresAt());
        rt.setRevoked(e.isRevoked());
        return rt;
    }
}