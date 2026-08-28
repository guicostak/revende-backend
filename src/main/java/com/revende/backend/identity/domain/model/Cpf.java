package com.revende.backend.identity.domain.model;

import com.revende.backend.shared.domain.exception.ValidationException;

/**
 * CPF do vendedor, armazenado sem máscara. Exigido pelo gateway no cadastro do recebedor,
 * não pela plataforma — por isso vive dentro de {@link SellerProfile} e não no usuário.
 */
public record Cpf(String value) {

    private static final int LENGTH = 11;

    public Cpf {
        if (value == null || value.isBlank()) {
            throw new ValidationException("CPF é obrigatório");
        }
        value = value.replaceAll("\\D", "");
        if (value.length() != LENGTH) {
            throw new ValidationException("CPF deve ter " + LENGTH + " dígitos");
        }
        if (allDigitsEqual(value) || !checkDigitsMatch(value)) {
            throw new ValidationException("CPF inválido");
        }
    }

    private static boolean allDigitsEqual(String digits) {
        return digits.chars().distinct().count() == 1;
    }

    private static boolean checkDigitsMatch(String digits) {
        return computeCheckDigit(digits, 9) == charAt(digits, 9) && computeCheckDigit(digits, 10) == charAt(digits, 10);
    }

    private static int computeCheckDigit(String digits, int upTo) {
        int sum = 0;
        int weight = upTo + 1;
        for (int i = 0; i < upTo; i++) {
            sum += charAt(digits, i) * weight--;
        }
        int remainder = sum % LENGTH;
        return remainder < 2 ? 0 : LENGTH - remainder;
    }

    private static int charAt(String digits, int index) {
        return digits.charAt(index) - '0';
    }

    /** Só os quatro últimos dígitos, para exibição. */
    public String masked() {
        return "***.***." + value.substring(6, 9) + "-" + value.substring(9);
    }

    @Override
    public String toString() {
        return masked();
    }
}
