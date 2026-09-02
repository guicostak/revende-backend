package com.revende.backend.identity.application.port;

/** Entrada do cadastro, já sem HTTP. {@code rawPassword} vive só até virar hash. */
public record RegisterUserCommand(String name, String email, String rawPassword, String phone) {}
