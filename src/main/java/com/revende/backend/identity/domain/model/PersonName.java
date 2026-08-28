package com.revende.backend.identity.domain.model;

import com.revende.backend.shared.domain.exception.ValidationException;

/** Nome de exibição. Não pretende ser nome civil: é o que os outros usuários veem. */
public record PersonName(String value) {

    private static final int MIN_LENGTH = 2;
    private static final int MAX_LENGTH = 120;

    public PersonName {
        if (value == null || value.isBlank()) {
            throw new ValidationException("Nome é obrigatório");
        }
        value = value.trim().replaceAll("\\s{2,}", " ");
        if (value.length() < MIN_LENGTH || value.length() > MAX_LENGTH) {
            throw new ValidationException("Nome deve ter entre " + MIN_LENGTH + " e " + MAX_LENGTH + " caracteres");
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
