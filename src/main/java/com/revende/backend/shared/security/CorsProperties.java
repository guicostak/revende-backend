package com.revende.backend.shared.security;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "revende.cors")
public record CorsProperties(List<String> allowedOrigins) {}
