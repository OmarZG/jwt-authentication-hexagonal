package org.zgo.auth.application.port.in;

import java.util.List;

public record UserResult(
        Long id,
        String username,
        String email,
        List<String> roles
) {}