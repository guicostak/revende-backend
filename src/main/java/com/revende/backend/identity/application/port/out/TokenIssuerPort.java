package com.revende.backend.identity.application.port.out;

/**
 * Emissão do access token — o de vida curta, apresentado a cada requisição.
 *
 * <p>A aplicação pede "um token para este usuário" e não sabe que hoje isso é um JWT
 * assinado em HMAC. O refresh token não passa por aqui: ele é opaco e sua validade mora
 * no banco, não dentro do próprio token.
 */
public interface TokenIssuerPort {

    String issueAccessToken(Long userId, String email);
}
