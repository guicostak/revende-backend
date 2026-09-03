package com.revende.backend.identity.application.port;

public interface RefreshSessionUseCase {

    AuthenticatedUser refresh(String rawRefreshToken);
}
