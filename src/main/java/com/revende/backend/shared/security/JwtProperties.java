package com.revende.backend.shared.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "revende.jwt")
public record JwtProperties(String secret, long expirationMs, long refreshExpirationMs) {}
