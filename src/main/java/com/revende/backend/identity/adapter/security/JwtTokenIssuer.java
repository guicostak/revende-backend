package com.revende.backend.identity.adapter.security;

import com.revende.backend.identity.application.port.TokenIssuerPort;
import com.revende.backend.shared.security.JwtProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenIssuer implements TokenIssuerPort {

    private final SecretKey chave;
    private final long validadeMs;

    public JwtTokenIssuer(JwtProperties properties) {
        this.chave = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
        this.validadeMs = properties.expirationMs();
    }

    @Override
    public String issueAccessToken(Long userId, String email) {
        Instant agora = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("email", email)
                .issuedAt(Date.from(agora))
                .expiration(Date.from(agora.plusMillis(validadeMs)))
                .signWith(chave)
                .compact();
    }
}
