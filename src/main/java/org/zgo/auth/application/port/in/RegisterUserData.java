package org.zgo.auth.application.port.in;

import java.util.Set;

public record RegisterUserData(
        String username,
        String email,
        String password,
        Set<String> roles
) {}