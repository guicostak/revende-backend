package com.revende.backend.identity.application.port.in;

/**
 * Dados de entrada do cadastro, já livres de HTTP.
 *
 * <p>Não é o DTO da web: o {@code RegisterRequest} carrega Bean Validation e vocabulário de
 * borda, e para de existir no controller. Aqui só chega o que o caso de uso precisa.
 *
 * @param rawPassword senha em texto puro, viva apenas até o hash. Nunca é persistida,
 *     nunca entra em log.
 * @param phone opcional — pode ser {@code null}.
 */
public record RegisterUserCommand(String name, String email, String rawPassword, String phone) {}
