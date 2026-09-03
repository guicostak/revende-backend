package com.revende.backend.identity.application.port;

/** Troca um refresh token válido por um par novo de tokens. */
public interface RefreshSessionUseCase {

    /**
     * Rotaciona: o token apresentado é revogado e um novo é emitido. Um refresh token vale
     * uma vez só — assim, se um vazar e for usado, a próxima tentativa do dono legítimo
     * denuncia o roubo em vez de conviver com ele.
     *
     * @throws com.revende.backend.identity.application.InvalidRefreshTokenException se o
     *     token não existe, venceu ou já foi usado
     */
    AuthenticatedUser refresh(String rawRefreshToken);
}
