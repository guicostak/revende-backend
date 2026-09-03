package com.revende.backend.identity.application.port;

public record LoginCommand(String email, String rawPassword) {}
