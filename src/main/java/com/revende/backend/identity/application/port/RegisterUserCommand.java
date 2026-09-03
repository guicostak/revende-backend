package com.revende.backend.identity.application.port;

public record RegisterUserCommand(String name, String email, String rawPassword, String phone) {}
