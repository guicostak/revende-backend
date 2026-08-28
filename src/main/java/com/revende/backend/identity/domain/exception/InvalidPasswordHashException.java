package com.revende.backend.identity.domain.exception;

public class InvalidPasswordHashException extends IdentityDomainException {

    public InvalidPasswordHashException(String message) {
        super(message);
    }
}
