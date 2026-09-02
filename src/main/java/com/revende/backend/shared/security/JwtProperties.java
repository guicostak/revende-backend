package com.revende.backend.shared.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Configuração dos tokens. O segredo vem de REVENDE_JWT_SECRET. */
@ConfigurationProperties(prefix = "revende.jwt")
public record JwtProperties(String secret, long expirationMs, long refreshExpirationMs) {}
