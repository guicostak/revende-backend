package com.revende.backend.identity.application.port;

/** Rotaciona o refresh token: o apresentado é revogado e um novo é emitido. */
public interface RefreshSessionUseCase {

    AuthenticatedUser refresh(String rawRefreshToken);
}
