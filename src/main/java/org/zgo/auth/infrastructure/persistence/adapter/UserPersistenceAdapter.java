package org.zgo.auth.infrastructure.persistence.adapter;

import org.springframework.stereotype.Component;
import org.zgo.auth.application.port.out.UserPersistencePort;
import org.zgo.auth.domain.model.User;
import org.zgo.auth.infrastructure.persistence.mapper.UserMapper;
import org.zgo.auth.infrastructure.persistence.repository.UserRepository;
import java.util.Optional;

@Component
public class UserPersistenceAdapter implements UserPersistencePort {

    private final UserRepository repository;
    private final UserMapper mapper;

    public UserPersistenceAdapter(UserRepository repository, UserMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public User save(User user) {
        var entity = mapper.toEntity(user);
        var saved = repository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return repository.findByUsername(username).map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return repository.findByEmail(email).map(mapper::toDomain);
    }
}