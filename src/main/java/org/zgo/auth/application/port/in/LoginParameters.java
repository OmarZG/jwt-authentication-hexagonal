package org.zgo.auth.application.port.in;

public record LoginParameters(
        String username,
        String password
) {}