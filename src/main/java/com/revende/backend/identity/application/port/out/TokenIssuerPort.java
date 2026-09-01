package com.revende.backend.identity.application.port.out;

/**
 * Emissão do token que autentica as chamadas seguintes.
 *
 * <p>A aplicação pede "um token para este usuário" e não sabe que hoje isso é um JWT
 * assinado com HMAC. Trocar por token opaco com sessão em Redis é reimplementar o adapter.
 */
public interface TokenIssuerPort {

    String issueFor(Long userId, String email);
}
