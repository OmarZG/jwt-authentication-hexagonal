package org.zgo.auth.application.port.out;

import org.zgo.auth.domain.model.User;

import java.util.Optional;

public interface UserPersistencePort {
    User save(User user);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);
}