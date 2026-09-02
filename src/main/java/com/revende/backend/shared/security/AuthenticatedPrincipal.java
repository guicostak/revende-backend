package com.revende.backend.shared.security;

/** Identidade de quem fez a requisição. Tipo próprio, como exige o CLAUDE.md §1.4.4. */
public record AuthenticatedPrincipal(Long userId, String email) {}
