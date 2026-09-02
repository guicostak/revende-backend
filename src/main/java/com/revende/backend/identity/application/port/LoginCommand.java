package com.revende.backend.identity.application.port;

/** Entrada do login. {@code rawPassword} vive só até a conferência. */
public record LoginCommand(String email, String rawPassword) {}
