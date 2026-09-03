package com.revende.backend.shared.security;

public record AuthenticatedPrincipal(Long userId, String email) {}
