package com.revende.backend.identity.application.service;

import com.revende.backend.identity.application.port.AuthenticatedUser;
import com.revende.backend.identity.application.port.RefreshTokenCodecPort;
import com.revende.backend.identity.application.port.RefreshTokenRepositoryPort;
import com.revende.backend.identity.application.port.TokenIssuerPort;
import com.revende.backend.identity.entity.RefreshToken;
import com.revende.backend.identity.entity.User;
import com.revende.backend.shared.security.JwtProperties;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Emite o par de tokens e persiste a sessão. Compartilhado por cadastro, login e refresh. */
@Component
@RequiredArgsConstructor
public class SessionIssuer {

    private final TokenIssuerPort tokenIssuer;
    private final RefreshTokenCodecPort refreshTokenCodec;
    private final RefreshTokenRepositoryPort refreshTokens;
    private final JwtProperties jwtProperties;

    public AuthenticatedUser issueFor(User user) {
        Instant agora = Instant.now();
        String refreshTokenPuro = refreshTokenCodec.generate();

        refreshTokens.save(RefreshToken.builder()
                .userId(user.getId())
                .tokenHash(refreshTokenCodec.hash(refreshTokenPuro))
                .expiresAt(agora.plusMillis(jwtProperties.refreshExpirationMs()))
                .createdAt(agora)
                .build());

        return new AuthenticatedUser(
                tokenIssuer.issueAccessToken(user.getId(), user.getEmail()),
                refreshTokenPuro,
                user.getId(),
                user.getName(),
                user.getEmail());
    }
}
