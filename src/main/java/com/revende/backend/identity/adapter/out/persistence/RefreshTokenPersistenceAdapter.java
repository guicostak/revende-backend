package com.revende.backend.identity.adapter.out.persistence;

import com.revende.backend.identity.application.port.out.RefreshTokenRepositoryPort;
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
    public void revokeAllForUser(Long userId, Instant momento) {
        repository.revokeAllForUser(userId, momento);
    }
}
