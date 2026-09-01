package com.revende.backend.shared.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuração de emissão do JWT.
 *
 * <p>{@code secret} vem de {@code REVENDE_JWT_SECRET}. O default do
 * {@code application.yml} serve só ao ambiente local — em produção é segredo do Secret
 * Manager, injetado pelo Cloud Run.
 *
 * @param secret chave HMAC. Precisa de 256 bits (32 bytes) para HS256; menos que isso e a
 *     jjwt recusa assinar, o que é a checagem certa acontecendo no lugar certo.
 * @param expirationMs validade do token em milissegundos.
 */
@ConfigurationProperties(prefix = "revende.jwt")
public record JwtProperties(String secret, long expirationMs) {}
