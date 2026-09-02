package com.revende.backend.identity.application.port;

import com.revende.backend.identity.entity.RefreshToken;
import java.time.Instant;
import java.util.Optional;

/** Guarda e revoga sessões de longa duração. */
public interface RefreshTokenRepositoryPort {

    RefreshToken save(RefreshToken token);

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    /**
     * Revoga o token só se ainda estiver ativo, em um UPDATE condicional único.
     *
     * @return {@code false} se outra requisição já o revogou — ou seja, este token está
     *     sendo apresentado pela segunda vez
     */
    boolean revokeIfActive(Long tokenId, Instant momento);

    /** Reação ao reuso: derruba a sessão inteira e obriga login novo. */
    void revokeAllForUser(Long userId, Instant momento);
}
