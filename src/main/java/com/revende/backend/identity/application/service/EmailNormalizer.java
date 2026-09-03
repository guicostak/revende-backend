package com.revende.backend.identity.application.service;

import java.util.Locale;

final class EmailNormalizer {

    private EmailNormalizer() {}

    static String normalize(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
