package com.revende.backend.identity.application.port;

/** Emissão do access token de vida curta. */
public interface TokenIssuerPort {

    String issueAccessToken(Long userId, String email);
}
