package com.revende.backend.identity.application.port;

import com.revende.backend.identity.entity.RefreshToken;
import java.time.Instant;
import java.util.Optional;

/** Guarda e revoga sessões de longa duração. */
public interface RefreshTokenRepositoryPort {

    RefreshToken save(RefreshToken token);

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    /**
     * Revoga tudo do usuário de uma vez. É a reação ao reuso detectado: se um token já
     * revogado reaparece, ou ele foi roubado ou o cliente está confuso — nos dois casos a
     * resposta segura é derrubar a sessão inteira e obrigar um login novo.
     */
    void revokeAllForUser(Long userId, Instant momento);
}
