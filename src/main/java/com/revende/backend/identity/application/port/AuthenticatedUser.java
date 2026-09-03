package com.revende.backend.identity.application.port;

/**
 * Resultado de cadastro, login ou renovação: quem é o usuário e os dois tokens.
 *
 * <p>{@code token} é o access token, de vida curta, mandado em toda requisição.
 * {@code refreshToken} é opaco, de vida longa, e só aparece em {@code /api/auth/refresh}.
 *
 * <p>Deliberadamente não expõe a entidade {@code User}: o que sai da aplicação é o mínimo
 * que a borda precisa, e sem o hash de senha, que nunca deve atravessar camada.
 */
public record AuthenticatedUser(String token, String refreshToken, Long userId, String name, String email) {}
