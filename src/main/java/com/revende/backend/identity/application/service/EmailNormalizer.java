package com.revende.backend.identity.application.service;

import java.util.Locale;

/** Normalização de e-mail. Cadastro e login precisam aplicar a mesma regra. */
final class EmailNormalizer {

    private EmailNormalizer() {}

    static String normalize(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
