package com.revende.backend.identity.application.port;

public record AuthenticatedUser(String token, String refreshToken, Long userId, String name, String email) {}
