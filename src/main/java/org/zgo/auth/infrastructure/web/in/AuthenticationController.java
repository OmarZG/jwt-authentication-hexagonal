package org.zgo.auth.infrastructure.web.in;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.zgo.auth.application.port.in.AuthUseCase;
import org.zgo.auth.application.port.in.RefreshTokenUseCase;
import org.zgo.auth.application.port.out.UserPersistencePort;
import org.zgo.auth.infrastructure.web.dto.request.LoginRequest;
import org.zgo.auth.infrastructure.web.dto.request.RefreshTokenRequest;
import org.zgo.auth.infrastructure.web.dto.request.RegisterRequest;
import org.zgo.auth.infrastructure.web.dto.request.RevokeRefreshRequest;
import org.zgo.auth.infrastructure.web.dto.response.AuthenticationResponse;
import org.zgo.auth.infrastructure.web.dto.response.UserResponse;
import org.zgo.auth.infrastructure.web.mapper.AuthMapper;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    private final AuthUseCase authUseCase;
    private final UserPersistencePort userPort;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final AuthMapper mapper;

    public AuthenticationController(AuthUseCase authUseCase, UserPersistencePort userPort, RefreshTokenUseCase refreshTokenUseCase, AuthMapper mapper) {
        this.authUseCase = authUseCase;
        this.userPort = userPort;
        this.refreshTokenUseCase = refreshTokenUseCase;
        this.mapper = mapper;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(@Valid @RequestBody RegisterRequest request) {
        var data = mapper.toRegisterUserData(request);
        var result = authUseCase.register(data);
        AuthenticationResponse response = mapper.toAuthenticationResponse(result);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@Valid @RequestBody LoginRequest request) {
        var parameters = mapper.toLoginParameters(request);
        var result = authUseCase.login(parameters);
        AuthenticationResponse response = mapper.toAuthenticationResponse(result);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthenticationResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        var result = authUseCase.refreshAccessToken(request.getRefreshToken());
        AuthenticationResponse response = mapper.toAuthenticationResponse(result);
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
                .map(mapper::toUserResult)
                .map(mapper::toUserResponse)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/admin")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<String> adminOnly() {
        return ResponseEntity.ok("Hello, admin!");
    }
}