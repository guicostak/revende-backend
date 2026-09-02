package com.revende.backend.identity.application.service;

import com.revende.backend.identity.application.InvalidRefreshTokenException;
import com.revende.backend.identity.application.port.AuthenticatedUser;
import com.revende.backend.identity.application.port.RefreshSessionUseCase;
import com.revende.backend.identity.application.port.RefreshTokenCodecPort;
import com.revende.backend.identity.application.port.RefreshTokenRepositoryPort;
import com.revende.backend.identity.application.port.UserRepositoryPort;
import com.revende.backend.identity.entity.RefreshToken;
import com.revende.backend.identity.entity.User;
import com.revende.backend.identity.model.AccountStatus;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Troca um refresh token por um par novo, com rotação e detecção de reuso. */
@Service
@RequiredArgsConstructor
public class RefreshSessionService implements RefreshSessionUseCase {

    private final RefreshTokenRepositoryPort refreshTokens;
    private final UserRepositoryPort users;
    private final RefreshTokenCodecPort refreshTokenCodec;
    private final SessionIssuer sessionIssuer;

    @Override
    @Transactional
    public AuthenticatedUser refresh(String rawRefreshToken) {
        Instant agora = Instant.now();

        RefreshToken guardado = refreshTokens
                .findByTokenHash(refreshTokenCodec.hash(rawRefreshToken))
                .orElseThrow(InvalidRefreshTokenException::new);

        // Token já revogado reaparecendo é sinal de roubo: o legítimo já rotacionou. Cai a
        // sessão inteira, não só este token.
        if (guardado.isRevoked()) {
            refreshTokens.revokeAllForUser(guardado.getUserId(), agora);
            throw new InvalidRefreshTokenException();
        }

        if (guardado.isExpiredAt(agora)) {
            throw new InvalidRefreshTokenException();
        }

        User user = users.findById(guardado.getUserId()).orElseThrow(InvalidRefreshTokenException::new);

        if (user.getStatus() != AccountStatus.ACTIVE) {
            throw new InvalidRefreshTokenException();
        }

        guardado.setRevokedAt(agora);
        refreshTokens.save(guardado);

        return sessionIssuer.issueFor(user);
    }
}
