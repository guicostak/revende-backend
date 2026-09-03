package com.revende.backend.identity.application.port;

/**
 * Caso de uso de cadastro. O controller depende desta interface, nunca da implementação.
 */
public interface RegisterUserUseCase {

    /**
     * Cadastra um usuário e já o autentica — quem acabou de se cadastrar não deve
     * precisar fazer login em seguida.
     *
     * @throws com.revende.backend.identity.application.EmailAlreadyRegisteredException se o
     *     e-mail já pertence a outra conta
     */
    AuthenticatedUser register(RegisterUserCommand command);
}
