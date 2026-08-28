package com.revende.backend.identity.domain.model;

import com.revende.backend.shared.domain.exception.ValidationException;
import java.util.regex.Pattern;

/** Telefone em formato E.164, sem máscara. Ex.: +5531999998888 */
public record PhoneNumber(String value) {

    private static final Pattern E164 = Pattern.compile("^\\+[1-9]\\d{7,14}$");

    public PhoneNumber {
        if (value == null || value.isBlank()) {
            throw new ValidationException("Telefone é obrigatório");
        }
        value = value.replaceAll("[\\s()\\-.]", "");
        if (!E164.matcher(value).matches()) {
            throw new ValidationException("Telefone deve estar em formato E.164, como +5531999998888");
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
