package com.revende.backend.identity.application;

/** Refresh token inexistente, vencido ou já usado. Vira 401: a sessão acabou. */
public class InvalidRefreshTokenException extends RuntimeException {

    public InvalidRefreshTokenException() {
        super("Sessão expirada. Entre novamente.");
    }
}
