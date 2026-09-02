package com.revende.backend.identity.adapter.persistence;

import com.revende.backend.identity.application.port.RefreshTokenRepositoryPort;
import com.revende.backend.identity.entity.RefreshToken;
import java.time.Instant;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RefreshTokenPersistenceAdapter implements RefreshTokenRepositoryPort {

    private final RefreshTokenJpaRepository repository;

    @Override
    public RefreshToken save(RefreshToken token) {
        return repository.save(token);
    }

    @Override
    public Optional<RefreshToken> findByTokenHash(String tokenHash) {
        return repository.findByTokenHash(tokenHash);
    }

    @Override
    public boolean revokeIfActive(Long tokenId, Instant momento) {
        return repository.revokeIfActive(tokenId, momento) == 1;
    }

    @Override
    public void revokeAllForUser(Long userId, Instant momento) {
        repository.revokeAllForUser(userId, momento);
    }
}
