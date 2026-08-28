package com.revende.backend.identity.domain.exception;

public class InvalidEmailException extends IdentityDomainException {

    public InvalidEmailException(String message) {
        super(message);
    }
}
