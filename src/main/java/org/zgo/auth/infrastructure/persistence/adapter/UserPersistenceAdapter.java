package org.zgo.auth.infrastructure.persistence.adapter;

import org.springframework.stereotype.Component;
import org.zgo.auth.application.port.out.UserPersistencePort;
import org.zgo.auth.domain.model.Role;
import org.zgo.auth.domain.model.User;
import org.zgo.auth.infrastructure.persistence.entity.UserEntity;
import org.zgo.auth.infrastructure.persistence.repository.UserRepository;

import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class UserPersistenceAdapter implements UserPersistencePort {

    private final UserRepository repository;

    public UserPersistenceAdapter(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public User save(User user) {
        UserEntity entity = new UserEntity();
        entity.setId(user.getId());
        entity.setUsername(user.getUsername());
        entity.setEmail(user.getEmail());
        entity.setPassword(user.getPassword());
        entity.setRoles(user.getRoles().stream().map(Enum::name).collect(Collectors.toSet()));

        UserEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return repository.findByUsername(username).map(this::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return repository.findByEmail(email).map(this::toDomain);
    }

    private User toDomain(UserEntity e) {
        User u = new User();
        u.setId(e.getId());
        u.setUsername(e.getUsername());
        u.setEmail(e.getEmail());
        u.setPassword(e.getPassword());
        u.setRoles(e.getRoles().stream().map(Role::valueOf).collect(Collectors.toSet()));
        return u;
    }
}