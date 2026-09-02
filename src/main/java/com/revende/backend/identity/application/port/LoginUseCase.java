package com.revende.backend.identity.application.port;

/** Autenticação por e-mail e senha. Lança InvalidCredentialsException nos três casos de falha. */
public interface LoginUseCase {

    /**
     * @throws com.revende.backend.identity.application.InvalidCredentialsException se o
     *     e-mail não existe, a senha não confere, ou a conta está bloqueada — sempre a
     *     mesma exceção, para que a resposta não diferencie os três casos
     */
    AuthenticatedUser login(LoginCommand command);
}
