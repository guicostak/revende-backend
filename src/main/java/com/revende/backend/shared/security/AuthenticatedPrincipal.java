package com.revende.backend.shared.security;

/**
 * Identidade de quem fez a requisição.
 *
 * <p>Existe porque o CLAUDE.md §1.4.4 exige que a identidade chegue como tipo próprio, e
 * não como {@code String email} tirado de {@code Authentication.getName()}. Com um tipo,
 * o caso de uso recebe {@code userId} tipado e não precisa reconsultar o banco só para
 * descobrir quem está falando.
 */
public record AuthenticatedPrincipal(Long userId, String email) {}
