package com.revende.backend.identity.application.service;

import com.revende.backend.identity.application.InvalidRefreshTokenException;
import com.revende.backend.identity.application.port.AuthenticatedUser;
import com.revende.backend.identity.application.port.RefreshSessionUseCase;
import com.revende.backend.identity.application.port.RefreshTokenRepositoryPort;
import com.revende.backend.identity.application.port.TokenHasherPort;
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
    private final TokenHasherPort tokenHasher;
    private final SessionIssuer sessionIssuer;

    @Override
    @Transactional
    public AuthenticatedUser refresh(String rawRefreshToken) {
        Instant agora = Instant.now();

        RefreshToken guardado = refreshTokens
                .findByTokenHash(tokenHasher.hash(rawRefreshToken))
                .orElseThrow(InvalidRefreshTokenException::new);

        // Token já revogado reaparecendo é o sinal clássico de roubo: o legítimo já
        // rotacionou, então quem está apresentando este é outra pessoa — ou o dono com uma
        // cópia vazada circulando. Nos dois casos a resposta segura é a mesma: derrubar a
        // sessão inteira e obrigar login novo. Um refresh token vale uma vez só.
        if (guardado.isRevoked()) {
            refreshTokens.revokeAllForUser(guardado.getUserId(), agora);
            throw new InvalidRefreshTokenException();
        }

        if (guardado.isExpiredAt(agora)) {
            throw new InvalidRefreshTokenException();
        }

        User user = users.findById(guardado.getUserId()).orElseThrow(InvalidRefreshTokenException::new);

        // Conta bloqueada depois do login não pode renovar. É aqui que o bloqueio começa a
        // valer de fato: o access token anterior ainda vale até vencer, e é a recusa da
        // renovação que encerra a sessão.
        if (user.getStatus() != AccountStatus.ACTIVE) {
            throw new InvalidRefreshTokenException();
        }

        // Rotação: o apresentado morre agora, e o novo nasce no `issueFor`.
        guardado.setRevokedAt(agora);
        refreshTokens.save(guardado);

        return sessionIssuer.issueFor(user);
    }
}
