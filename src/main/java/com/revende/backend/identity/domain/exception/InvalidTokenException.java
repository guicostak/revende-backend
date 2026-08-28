package com.revende.backend.identity.domain.exception;

public class InvalidTokenException extends IdentityDomainException {

    public InvalidTokenException(String message) {
        super(message);
    }
}
