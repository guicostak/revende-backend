package com.revende.backend.identity.domain.model;

import com.revende.backend.shared.domain.exception.ValidationException;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * E-mail normalizado. A normalização acontece na construção, então não existe caminho
 * no sistema para dois endereços equivalentes virarem contas distintas.
 */
public record EmailAddress(String value) {

    private static final Pattern FORMAT = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]{2,}$");
    private static final int MAX_LENGTH = 320; // RFC 5321

    public EmailAddress {
        if (value == null || value.isBlank()) {
            throw new ValidationException("E-mail é obrigatório");
        }
        value = value.trim().toLowerCase(Locale.ROOT);
        if (value.length() > MAX_LENGTH) {
            throw new ValidationException("E-mail excede " + MAX_LENGTH + " caracteres");
        }
        if (!FORMAT.matcher(value).matches()) {
            throw new ValidationException("E-mail em formato inválido");
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
