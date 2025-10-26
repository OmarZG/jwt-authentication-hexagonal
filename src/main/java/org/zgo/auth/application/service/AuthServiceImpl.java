package org.zgo.auth.application.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.zgo.auth.application.port.in.AuthUseCase;
import org.zgo.auth.application.port.in.RefreshTokenUseCase;
import org.zgo.auth.application.port.out.RefreshTokenPersistencePort;
import org.zgo.auth.application.port.out.UserPersistencePort;
import org.zgo.auth.domain.exception.custom.InvalidCredentialsException;
import org.zgo.auth.domain.exception.custom.UserAlreadyExistsException;
import org.zgo.auth.domain.model.RefreshToken;
import org.zgo.auth.domain.model.Role;
import org.zgo.auth.domain.model.User;
import org.zgo.auth.infrastructure.web.dto.request.LoginRequest;
import org.zgo.auth.infrastructure.web.dto.request.RefreshTokenRequest;
import org.zgo.auth.infrastructure.web.dto.request.RegisterRequest;
import org.zgo.auth.infrastructure.web.dto.response.AuthenticationResponse;
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
    public AuthenticationResponse register(RegisterRequest request) {
        userPersistence.findByUsername(request.getUsername()).ifPresent(u -> {
            throw new UserAlreadyExistsException("username already exists");
        });

        userPersistence.findByEmail(request.getEmail()).ifPresent(u -> {
            throw new UserAlreadyExistsException("email already exists");
        });

        Set<Role> roles = new HashSet<>();
        roles.add(Role.ROLE_USER);
        if (request.getRoles() != null && request.getRoles().contains("ADMIN")) {
            roles.add(Role.ROLE_ADMIN);
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRoles(roles);

        User saved = userPersistence.save(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername(saved.getUsername());
        String accessToken = jwtService.generateToken(userDetails, Map.of());
        RefreshToken refreshToken = refreshTokenUseCase.createRefreshToken(saved.getUsername());

        return new AuthenticationResponse(accessToken, refreshToken.getToken());
    }

    @Override
    public AuthenticationResponse login(LoginRequest request) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
        } catch (Exception ex) {
            throw new InvalidCredentialsException("Invalid username or password");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        String accessToken = jwtService.generateToken(userDetails, Map.of());
        RefreshToken refreshToken = refreshTokenUseCase.createRefreshToken(userDetails.getUsername());

        return new AuthenticationResponse(accessToken, refreshToken.getToken());
    }

    @Override
    public AuthenticationResponse refreshAccessToken(RefreshTokenRequest request) {
        RefreshToken rt = refreshTokenPersistence.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid refresh token"));

        if (rt.isRevoked() || rt.getExpiresAt().isBefore(Instant.now())) {
            throw new InvalidCredentialsException("Refresh token is invalid or expired");
        }

        // rotate: revoke old and create a new refresh token
        refreshTokenUseCase.revokeRefreshToken(rt.getToken());
        RefreshToken newRt = refreshTokenUseCase.createRefreshToken(rt.getUsername());

        UserDetails userDetails = userDetailsService.loadUserByUsername(rt.getUsername());
        String accessToken = jwtService.generateToken(userDetails, Map.of());

        return new AuthenticationResponse(accessToken, newRt.getToken());
    }
}