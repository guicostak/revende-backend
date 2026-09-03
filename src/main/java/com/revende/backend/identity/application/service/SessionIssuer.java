package com.revende.backend.identity.application.service;

import com.revende.backend.identity.application.port.AuthenticatedUser;
import com.revende.backend.identity.application.port.RefreshTokenRepositoryPort;
import com.revende.backend.identity.application.port.TokenGeneratorPort;
import com.revende.backend.identity.application.port.TokenHasherPort;
import com.revende.backend.identity.application.port.TokenIssuerPort;
import com.revende.backend.identity.entity.RefreshToken;
import com.revende.backend.identity.entity.User;
import com.revende.backend.shared.security.JwtProperties;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Emite o par de tokens e persiste a sessão.
 *
 * <p>Existe porque cadastro, login e renovação terminam exatamente do mesmo jeito. Sem
 * isto, a regra de "gera opaco, guarda o hash, devolve o texto puro" ficaria copiada em
 * três lugares — e bastaria um deles esquecer de hashear para vazar sessão.
 */
@Component
@RequiredArgsConstructor
public class SessionIssuer {

    private final TokenIssuerPort tokenIssuer;
    private final TokenGeneratorPort tokenGenerator;
    private final TokenHasherPort tokenHasher;
    private final RefreshTokenRepositoryPort refreshTokens;
    private final JwtProperties jwtProperties;

    public AuthenticatedUser issueFor(User user) {
        Instant agora = Instant.now();

        // O texto puro existe só nesta variável e só até virar resposta HTTP. O que vai
        // para o banco é o hash.
        String refreshTokenPuro = tokenGenerator.generate();

        refreshTokens.save(RefreshToken.builder()
                .userId(user.getId())
                .tokenHash(tokenHasher.hash(refreshTokenPuro))
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
