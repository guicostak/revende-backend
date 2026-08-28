package com.revende.backend.identity.domain.model;

import com.revende.backend.shared.domain.exception.ValidationException;

/**
 * Hash BCrypt da senha. O domínio nunca vê a senha em claro — a política de força e o
 * algoritmo de hash vivem no adapter, e só o resultado atravessa a fronteira.
 */
public record PasswordHash(String value) {

    public PasswordHash {
        if (value == null || value.isBlank()) {
            throw new ValidationException("Hash de senha é obrigatório");
        }
        if (!value.startsWith("$2")) {
            throw new ValidationException("Hash de senha não está em formato BCrypt");
        }
    }

    /** Impede que o hash apareça em log, stack trace ou mensagem de erro. */
    @Override
    public String toString() {
        return "PasswordHash[protegido]";
    }
}
