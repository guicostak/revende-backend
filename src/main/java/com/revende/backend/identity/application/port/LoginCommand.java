package com.revende.backend.identity.application.port;

/** @param rawPassword senha em texto puro, viva só até a conferência. Nunca entra em log. */
public record LoginCommand(String email, String rawPassword) {}
