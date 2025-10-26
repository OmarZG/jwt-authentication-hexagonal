package org.zgo.auth.application.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.zgo.auth.application.port.in.*;
import org.zgo.auth.application.port.out.RefreshTokenPersistencePort;
import org.zgo.auth.application.port.out.UserPersistencePort;
import org.zgo.auth.domain.exception.custom.InvalidCredentialsException;
import org.zgo.auth.domain.exception.custom.UserAlreadyExistsException;
import org.zgo.auth.domain.model.RefreshToken;
import org.zgo.auth.domain.model.Role;
import org.zgo.auth.domain.model.User;
import org.zgo.auth.infrastructure.service.JwtService;
import org.zgo.auth.infrastructure.service.UserDetailsServiceImpl;

import java.time.Instant;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
public class AuthServiceImpl implements AuthUseCase {

    private final UserPersistencePort userPersistence;
    private final RefreshTokenPersistencePort refreshTokenPersistence;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserDetailsServiceImpl userDetailsService;

    public AuthServiceImpl(UserPersistencePort userPersistence,
                           RefreshTokenPersistencePort refreshTokenPersistence,
                           RefreshTokenUseCase refreshTokenUseCase,
                           PasswordEncoder passwordEncoder,
                           AuthenticationManager authenticationManager,
                           JwtService jwtService,
                           UserDetailsServiceImpl userDetailsService) {
        this.userPersistence = userPersistence;
        this.refreshTokenPersistence = refreshTokenPersistence;
        this.refreshTokenUseCase = refreshTokenUseCase;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public AuthResult register(RegisterUserData data) {
        userPersistence.findByUsername(data.username()).ifPresent(u -> {
            throw new UserAlreadyExistsException("username already exists");
        });

        userPersistence.findByEmail(data.email()).ifPresent(u -> {
            throw new UserAlreadyExistsException("email already exists");
        });

        Set<Role> roles = new HashSet<>();
        roles.add(Role.ROLE_USER);
        if (data.roles() != null && data.roles().contains("ADMIN")) {
            roles.add(Role.ROLE_ADMIN);
        }

        User user = new User();
        user.setUsername(data.username());
        user.setEmail(data.email());
        user.setPassword(passwordEncoder.encode(data.password()));
        user.setRoles(roles);

        User saved = userPersistence.save(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername(saved.getUsername());
        String accessToken = jwtService.generateToken(userDetails, Map.of());
        RefreshToken refreshToken = refreshTokenUseCase.createRefreshToken(saved.getUsername());

        return new AuthResult(accessToken, refreshToken.getToken());
    }

    @Override
    public AuthResult login(LoginParameters parameters) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(parameters.username(), parameters.password()) // Usa parameters
            );
        } catch (Exception ex) {
            throw new InvalidCredentialsException("Invalid username or password");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(parameters.username()); // Usa parameters
        String accessToken = jwtService.generateToken(userDetails, Map.of());
        RefreshToken refreshToken = refreshTokenUseCase.createRefreshToken(userDetails.getUsername());

        return new AuthResult(accessToken, refreshToken.getToken());
    }

    @Override
    public AuthResult refreshAccessToken(String refreshToken) {
        RefreshToken rt = refreshTokenPersistence.findByToken(refreshToken)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid refresh token"));

        if (rt.isRevoked() || rt.getExpiresAt().isBefore(Instant.now())) {
            throw new InvalidCredentialsException("Refresh token is invalid or expired");
        }

        refreshTokenUseCase.revokeRefreshToken(rt.getToken());
        RefreshToken newRt = refreshTokenUseCase.createRefreshToken(rt.getUsername());

        UserDetails userDetails = userDetailsService.loadUserByUsername(rt.getUsername());
        String accessToken = jwtService.generateToken(userDetails, Map.of());

        return new AuthResult(accessToken, newRt.getToken());
    }
}