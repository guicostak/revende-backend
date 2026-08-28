package com.revende.backend.identity.domain.exception;

/** Raiz das violações de regra do contexto de identidade. */
public abstract class IdentityDomainException extends RuntimeException {

    protected IdentityDomainException(String message) {
        super(message);
    }
}
