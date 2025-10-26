package org.zgo.auth.infrastructure.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class JwtService {

    // Fallback resources (used only if env vars are not present)
    @Value("classpath:jwtKeys/private.key.pem")
    private Resource privateKeyResource;

    @Value("classpath:jwtKeys/public.key.pem")
    private Resource publicKeyResource;

    @Value("${app.jwt.expiration-ms:900000}") // 15 min default
    private Long jwtExpirationMs;

    private PrivateKey privateKey;
    private PublicKey publicKey;

    @PostConstruct
    public void init() {
        try {
            String privatePem = System.getenv("APP_JWT_PRIVATE_KEY");
            String publicPem = System.getenv("APP_JWT_PUBLIC_KEY");

            if (privatePem == null || privatePem.isBlank()) {
                privatePem = new String(privateKeyResource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            }
            if (publicPem == null || publicPem.isBlank()) {
                publicPem = new String(publicKeyResource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            }

            this.privateKey = getPrivateKeyFromPem(privatePem);
            this.publicKey = getPublicKeyFromPem(publicPem);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to load RSA keys", ex);
        }
    }

    public String generateToken(UserDetails userDetails, Map<String, Object> extraClaims) {
     var claimsBuilder = Jwts.claims();

     if (extraClaims != null) {
            claimsBuilder.add(extraClaims);
        }

        List<String> roles = userDetails.getAuthorities().stream().map(a -> a.getAuthority()).toList();
        claimsBuilder.add("roles", roles);

        Instant now = Instant.now();
        Instant expiry = now.plusMillis(jwtExpirationMs);

        return Jwts.builder()
                .claims(claimsBuilder.build())
                .subject(userDetails.getUsername())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();
    }

    public String extractUsername(String token) {
        return getClaims(token).getSubject();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
        } catch (Exception ex) {
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        Date expiry = getClaims(token).getExpiration();
        return expiry.before(new Date());
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .setSigningKey(publicKey)
                .build()
                .parseClaimsJws(token)
                .getPayload();
    }

    // PEM parsing helpers - tolerant with different PEM header/footer variants and whitespace
    private PrivateKey getPrivateKeyFromPem(String pem) throws Exception {
        String clean = pem
                .replaceAll("-----BEGIN [^-]+-----", "")
                .replaceAll("-----END [^-]+-----", "")
                .replaceAll("\\s", "");
        byte[] decoded = Base64.getDecoder().decode(clean);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decoded);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(keySpec);
    }

    private PublicKey getPublicKeyFromPem(String pem) throws Exception {
        String clean = pem
                .replaceAll("-----BEGIN [^-]+-----", "")
                .replaceAll("-----END [^-]+-----", "")
                .replaceAll("\\s", "");
        byte[] decoded = Base64.getDecoder().decode(clean);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(spec);
    }
}