package com.revende.backend.identity.domain.model;

import com.revende.backend.shared.domain.exception.ValidationException;
import java.util.regex.Pattern;

/**
 * Chave Pix de recebimento. A validação aqui é de formato apenas — confirmar que a chave
 * pertence ao CPF do vendedor é consulta ao DICT, e quem faz isso é o gateway no cadastro
 * do recebedor.
 */
public record PixKey(PixKeyType type, String value) {

    private static final Pattern RANDOM_KEY =
            Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");

    public PixKey {
        if (type == null) {
            throw new ValidationException("Tipo da chave Pix é obrigatório");
        }
        if (value == null || value.isBlank()) {
            throw new ValidationException("Chave Pix é obrigatória");
        }
        value = value.trim();
        switch (type) {
            case CPF -> new Cpf(value);
            case EMAIL -> new EmailAddress(value);
            case PHONE -> new PhoneNumber(value);
            case RANDOM -> {
                if (!RANDOM_KEY.matcher(value).matches()) {
                    throw new ValidationException("Chave aleatória deve estar em formato UUID");
                }
            }
        }
    }
}
