package com.revende.backend.identity.application.port;

/** Caso de uso de cadastro. Lança EmailAlreadyRegisteredException se o e-mail já existe. */
public interface RegisterUserUseCase {

    /** Cadastra e já autentica: quem acabou de se cadastrar não deve precisar logar. */
    AuthenticatedUser register(RegisterUserCommand command);
}
