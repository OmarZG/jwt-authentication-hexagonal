package org.zgo.auth.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.zgo.auth.application.port.in.AuthUseCase;
import org.zgo.auth.application.port.in.RefreshTokenUseCase;
import org.zgo.auth.application.port.out.UserPersistencePort;
import org.zgo.auth.dto.request.LoginRequest;
import org.zgo.auth.dto.request.RefreshTokenRequest;
import org.zgo.auth.dto.request.RegisterRequest;
import org.zgo.auth.dto.request.RevokeRefreshRequest;
import org.zgo.auth.dto.response.AuthenticationResponse;
import org.zgo.auth.dto.response.UserResponse;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    private final AuthUseCase authUseCase;
    private final UserPersistencePort userPort;
    private final RefreshTokenUseCase refreshTokenUseCase;

    public AuthenticationController(AuthUseCase authUseCase, UserPersistencePort userPort, RefreshTokenUseCase refreshTokenUseCase) {
        this.authUseCase = authUseCase;
        this.userPort = userPort;
        this.refreshTokenUseCase = refreshTokenUseCase;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthenticationResponse response = authUseCase.register(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthenticationResponse response = authUseCase.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthenticationResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        AuthenticationResponse response = authUseCase.refreshAccessToken(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/revoke")
    public ResponseEntity<?> revoke(@Valid @RequestBody RevokeRefreshRequest request) {
        if (request.getRefreshToken() == null || request.getRefreshToken().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        refreshTokenUseCase.revokeRefreshToken(request.getRefreshToken());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(Authentication authentication) {
        if (authentication == null) return ResponseEntity.notFound().build();
        String username = authentication.getName();
        return userPort.findByUsername(username)
                .map(u -> {
                    UserResponse ur = new UserResponse(u.getId(), u.getUsername(), u.getEmail(), u.getRoles().stream().map(Enum::name).toList());
                    return ResponseEntity.ok(ur);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/admin")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<String> adminOnly() {
        return ResponseEntity.ok("Hello, admin!");
    }
}