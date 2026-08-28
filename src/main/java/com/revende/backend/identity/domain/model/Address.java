package com.revende.backend.identity.domain.model;

import com.revende.backend.identity.domain.exception.InvalidAddressException;
import java.util.Locale;
import java.util.regex.Pattern;

/** Endereço do vendedor. Exigido pelo gateway no cadastro do recebedor. */
public record Address(
        String street, String number, String complement, String district, String city, String state, String zipCode) {

    private static final Pattern ZIP_CODE = Pattern.compile("^\\d{8}$");
    private static final Pattern STATE = Pattern.compile("^[A-Z]{2}$");

    public Address {
        street = required(street, "Logradouro");
        number = required(number, "Número");
        district = required(district, "Bairro");
        city = required(city, "Cidade");

        complement = complement == null ? null : complement.trim();

        state = required(state, "Estado").toUpperCase(Locale.ROOT);
        if (!STATE.matcher(state).matches()) {
            throw new InvalidAddressException("Estado deve ser a sigla de duas letras");
        }

        zipCode = required(zipCode, "CEP").replaceAll("\\D", "");
        if (!ZIP_CODE.matcher(zipCode).matches()) {
            throw new InvalidAddressException("CEP deve ter 8 dígitos");
        }
    }

    private static String required(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new InvalidAddressException(field + " é obrigatório");
        }
        return value.trim();
    }
}
