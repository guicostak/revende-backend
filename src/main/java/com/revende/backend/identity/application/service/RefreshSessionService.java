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

    /**
     * {@code noRollbackFor} é obrigatório aqui: a detecção de reuso grava a revogação em
     * massa e logo em seguida lança. Sem isto o rollback padrão desfaz o UPDATE e a reação
     * ao roubo vira um no-op — o atacante segue com a sessão viva.
     */
    @Override
    @Transactional(noRollbackFor = InvalidRefreshTokenException.class)
    public AuthenticatedUser refresh(String rawRefreshToken) {
        Instant agora = Instant.now();

        RefreshToken guardado = refreshTokens
                .findByTokenHash(refreshTokenCodec.hash(rawRefreshToken))
                .orElseThrow(InvalidRefreshTokenException::new);

        if (guardado.isRevoked()) {
            throw reusoDetectado(guardado.getUserId(), agora);
        }

        if (guardado.isExpiredAt(agora)) {
            throw new InvalidRefreshTokenException();
        }

        User user = users.findById(guardado.getUserId()).orElseThrow(InvalidRefreshTokenException::new);

        if (user.getStatus() != AccountStatus.ACTIVE) {
            throw new InvalidRefreshTokenException();
        }

        // UPDATE condicional: é o banco que decide quem rotacionou primeiro. Ler e depois
        // gravar deixaria duas requisições concorrentes passarem as duas pela checagem de
        // `isRevoked` acima, e o token single-use valeria duas vezes.
        if (!refreshTokens.revokeIfActive(guardado.getId(), agora)) {
            throw reusoDetectado(guardado.getUserId(), agora);
        }

        return sessionIssuer.issueFor(user);
    }

    /** Token apresentado duas vezes: ou foi roubado, ou vazou. A sessão inteira cai. */
    private InvalidRefreshTokenException reusoDetectado(Long userId, Instant momento) {
        refreshTokens.revokeAllForUser(userId, momento);
        return new InvalidRefreshTokenException();
    }
}
