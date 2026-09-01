package com.revende.backend.identity.application.port.in;

/**
 * Resultado de um cadastro ou login bem-sucedido: quem é o usuário e o token que
 * comprova isso nas próximas chamadas.
 *
 * <p>Deliberadamente não expõe a entidade {@code User}. O que sai da aplicação é o
 * mínimo que a borda precisa para responder — e sem o hash de senha, que nunca deve
 * atravessar camada.
 */
public record AuthenticatedUser(String token, Long userId, String name, String email) {}
