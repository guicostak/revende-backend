package com.revende.backend.identity.application.port;

/** Resultado de cadastro, login ou renovação: identidade e o par de tokens. */
public record AuthenticatedUser(String token, String refreshToken, Long userId, String name, String email) {}
