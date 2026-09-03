package com.revende.backend.identity.application.service;

import com.revende.backend.identity.application.port.RefreshTokenRepositoryPort;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class SessionRevoker {

    private final RefreshTokenRepositoryPort refreshTokens;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void revokeAllForUser(Long userId, Instant momento) {
        refreshTokens.revokeAllForUser(userId, momento);
    }
}
