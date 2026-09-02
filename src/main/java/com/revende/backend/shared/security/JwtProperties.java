package com.revende.backend.shared.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuração dos tokens.
 *
 * <p>{@code secret} vem de {@code REVENDE_JWT_SECRET}. O default do
 * {@code application.yml} serve só ao ambiente local — em produção é segredo do Secret
 * Manager, injetado pelo Cloud Run.
 *
 * @param secret chave HMAC. Precisa de 256 bits (32 bytes) para HS256; menos que isso e a
 *     jjwt recusa assinar, o que é a checagem certa acontecendo na subida da aplicação.
 * @param expirationMs validade do access token. Curta de propósito: ele não é revogável,
 *     então a janela de estrago de um vazamento é exatamente esta.
 * @param refreshExpirationMs validade do refresh token. Longa, e isso é seguro porque ele
 *     mora no banco e pode ser revogado a qualquer momento.
 */
@ConfigurationProperties(prefix = "revende.jwt")
public record JwtProperties(String secret, long expirationMs, long refreshExpirationMs) {}
