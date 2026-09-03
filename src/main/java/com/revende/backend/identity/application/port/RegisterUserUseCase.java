package com.revende.backend.identity.application.port;

public interface RegisterUserUseCase {

    AuthenticatedUser register(RegisterUserCommand command);
}
