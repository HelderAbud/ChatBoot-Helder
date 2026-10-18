package com.chatboot.identity;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

@Configuration
public class JwtConfig {
    @Bean
    JwtEncoder jwtEncoder(@Value("${chatboot.jwt.secret}") String secret) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(JwtSecret.require(secret)));
    }
}
