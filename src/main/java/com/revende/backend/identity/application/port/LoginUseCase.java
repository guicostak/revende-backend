package com.revende.backend.identity.application.port;

public interface LoginUseCase {

    AuthenticatedUser login(LoginCommand command);
}
