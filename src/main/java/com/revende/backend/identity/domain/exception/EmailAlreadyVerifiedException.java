package com.revende.backend.identity.domain.exception;

public class EmailAlreadyVerifiedException extends IdentityDomainException {

    public EmailAlreadyVerifiedException(String message) {
        super(message);
    }
}
