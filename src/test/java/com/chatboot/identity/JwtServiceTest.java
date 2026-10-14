package com.chatboot.identity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

class JwtServiceTest {

    @Test
    void issuesTokenThatExpiresInThirtyMinutesAndCarriesWorkspace() {
        String secret = "chatboot-test-secret-must-be-32bytes!";
        SecretKey key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        JwtEncoder encoder = new NimbusJwtEncoder(new ImmutableSecret<>(key));
        UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID workspaceId = UUID.fromString("22222222-2222-2222-2222-222222222222");

        String token = new JwtService(encoder).issue(userId, workspaceId);

        Jwt jwt = NimbusJwtDecoder.withSecretKey(key).build().decode(token);
        assertEquals(userId.toString(), jwt.getSubject());
        assertEquals(workspaceId.toString(), jwt.getClaimAsString("workspaceId"));
        assertEquals(1800, Duration.between(jwt.getIssuedAt(), jwt.getExpiresAt()).getSeconds());
    }
}
