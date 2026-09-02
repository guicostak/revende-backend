package com.revende.backend.identity.application.service;

import java.util.Locale;

/**
 * Normalização de e-mail, em um lugar só.
 *
 * <p>E-mail é case-insensitive na prática, mas {@code UNIQUE} no Postgres não é. Sem
 * normalizar, "Ana@x.com" e "ana@x.com" viram duas contas — e o login depois falha
 * dependendo de como a pessoa digitou.
 *
 * <p>Cadastro e login precisam aplicar exatamente a mesma regra. Se divergissem, daria
 * para cadastrar com um formato e nunca mais conseguir entrar.
 */
final class EmailNormalizer {

    private EmailNormalizer() {}

    static String normalize(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
