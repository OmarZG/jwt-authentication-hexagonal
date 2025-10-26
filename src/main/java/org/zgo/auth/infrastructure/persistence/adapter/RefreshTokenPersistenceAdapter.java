package org.zgo.auth.infrastructure.persistence.adapter;
import org.springframework.stereotype.Component;
import org.zgo.auth.application.port.out.RefreshTokenPersistencePort;
import org.zgo.auth.domain.model.RefreshToken;
import org.zgo.auth.infrastructure.persistence.mapper.RefreshTokenMapper; // Nueva importación
import org.zgo.auth.infrastructure.persistence.repository.RefreshTokenRepository;

import java.util.Optional;

@Component
public class RefreshTokenPersistenceAdapter implements RefreshTokenPersistencePort {

    private final RefreshTokenRepository repository;
    private final RefreshTokenMapper mapper;

    public RefreshTokenPersistenceAdapter(RefreshTokenRepository repository, RefreshTokenMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public RefreshToken save(RefreshToken refreshToken) {
        var entity = mapper.toEntity(refreshToken);
        var saved = repository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        return repository.findByToken(token).map(mapper::toDomain);
    }

    @Override
    public void delete(RefreshToken refreshToken) {
        repository.findByToken(refreshToken.getToken()).ifPresent(repository::delete);
    }
}