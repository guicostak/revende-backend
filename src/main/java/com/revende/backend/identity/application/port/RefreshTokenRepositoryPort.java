package com.revende.backend.identity.application.port;

import com.revende.backend.identity.entity.RefreshToken;
import java.time.Instant;
import java.util.Optional;

public interface RefreshTokenRepositoryPort {

    RefreshToken save(RefreshToken token);

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    void revokeAllForUser(Long userId, Instant momento);
}
