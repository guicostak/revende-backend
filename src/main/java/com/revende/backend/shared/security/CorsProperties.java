package com.revende.backend.shared.security;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Origens autorizadas a chamar a API pelo navegador. Sem curinga, de propósito. */
@ConfigurationProperties(prefix = "revende.cors")
public record CorsProperties(List<String> allowedOrigins) {}
