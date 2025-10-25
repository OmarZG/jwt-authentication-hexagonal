package org.zgo.auth.service;

import org.zgo.auth.infrastructure.web.dto.request.RegisterRequest;
import org.zgo.auth.infrastructure.web.dto.request.RefreshTokenRequest;
import org.zgo.auth.infrastructure.web.dto.request.RevokeRefreshRequest;
import org.zgo.auth.infrastructure.persistence.entity.RefreshTokenEntity;
import org.zgo.auth.infrastructure.persistence.repository.RefreshTokenRepository;
import org.zgo.auth.infrastructure.persistence.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthServiceIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    public void setup() {
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    public void testRefreshFlow_withRotation() throws Exception {
        // Register
        RegisterRequest reg = new RegisterRequest();
        reg.setUsername("refreshUser");
        reg.setEmail("refresh@example.com");
        reg.setPassword("password");
        reg.setRoles(List.of("USER"));

        String regResp = mvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(reg)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode regJson = mapper.readTree(regResp);
        String originalRefreshToken = regJson.get("refreshToken").asText();
        assertThat(originalRefreshToken).isNotEmpty();

        // Use refresh endpoint -> expect rotated refresh token (different)
        RefreshTokenRequest rreq = new RefreshTokenRequest();
        rreq.setRefreshToken(originalRefreshToken);

        String refreshResp = mvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(rreq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andReturn().getResponse().getContentAsString();

        JsonNode refreshJson = mapper.readTree(refreshResp);
        String newRefreshToken = refreshJson.get("refreshToken").asText();
        String accessToken = refreshJson.get("accessToken").asText();

        assertThat(newRefreshToken).isNotBlank();
        assertThat(newRefreshToken).isNotEqualTo(originalRefreshToken);

        // original token must be revoked in DB
        Optional<RefreshTokenEntity> originalEntity = refreshTokenRepository.findByToken(originalRefreshToken);
        assertThat(originalEntity).isPresent();
        assertThat(originalEntity.get().isRevoked()).isTrue();

        // new token exists and not revoked
        Optional<RefreshTokenEntity> newEntity = refreshTokenRepository.findByToken(newRefreshToken);
        assertThat(newEntity).isPresent();
        assertThat(newEntity.get().isRevoked()).isFalse();

        // Call /me with new token
        mvc.perform(get("/api/auth/me")
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("refreshUser"));
    }

    @Test
    public void testRevokeEndpoint_revokesToken() throws Exception {
        // Register
        RegisterRequest reg = new RegisterRequest();
        reg.setUsername("revoker");
        reg.setEmail("revoker@example.com");
        reg.setPassword("password");
        reg.setRoles(List.of("USER"));

        String regResp = mvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(reg)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode regJson = mapper.readTree(regResp);
        String refreshToken = regJson.get("refreshToken").asText();

        // Revoke via endpoint
        RevokeRefreshRequest revokeReq = new RevokeRefreshRequest();
        revokeReq.setRefreshToken(refreshToken);

        mvc.perform(post("/api/auth/revoke")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(revokeReq)))
                .andExpect(status().isNoContent());

        // Check DB: revoked == true
        Optional<RefreshTokenEntity> e = refreshTokenRepository.findByToken(refreshToken);
        assertThat(e).isPresent();
        assertThat(e.get().isRevoked()).isTrue();
    }

    @Test
    public void testAdminAccessControl() throws Exception {
        // Create admin
        RegisterRequest adminReg = new RegisterRequest();
        adminReg.setUsername("adminUser");
        adminReg.setEmail("admin@example.com");
        adminReg.setPassword("adminpass");
        adminReg.setRoles(List.of("ADMIN"));

        String adminRegResp = mvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(adminReg)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode adminJson = mapper.readTree(adminRegResp);
        String adminAccess = adminJson.get("accessToken").asText();

        // Admin should access /admin
        mvc.perform(get("/api/auth/admin")
                .header("Authorization", "Bearer " + adminAccess))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello, admin!"));

        // Create normal user
        RegisterRequest userReg = new RegisterRequest();
        userReg.setUsername("simpleUser");
        userReg.setEmail("user@example.com");
        userReg.setPassword("userpass");
        userReg.setRoles(List.of("USER"));

        String userRegResp = mvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(userReg)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode userJson = mapper.readTree(userRegResp);
        String userAccess = userJson.get("accessToken").asText();

        // User should NOT access /admin
        mvc.perform(get("/api/auth/admin")
                .header("Authorization", "Bearer " + userAccess))
                .andExpect(status().isForbidden());
    }
}