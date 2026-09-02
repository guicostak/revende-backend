package com.revende.backend.identity.application.port.in;

/** Caso de uso de autenticação por e-mail e senha. */
public interface LoginUseCase {

    /**
     * @throws com.revende.backend.identity.application.InvalidCredentialsException se o
     *     e-mail não existe, a senha não confere, ou a conta está bloqueada — sempre a
     *     mesma exceção, para que a resposta não diferencie os três casos
     */
    AuthenticatedUser login(LoginCommand command);
}
