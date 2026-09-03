package com.revende.backend.identity.application.port;

public interface TokenIssuerPort {

    String issueAccessToken(Long userId, String email);
}
