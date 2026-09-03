package com.revende.backend.identity.application;

public class InvalidRefreshTokenException extends RuntimeException {

    public InvalidRefreshTokenException() {
        super("Sessão expirada. Entre novamente.");
    }
}
