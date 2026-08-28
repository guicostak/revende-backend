package com.revende.backend.identity.domain.model;

import com.revende.backend.identity.domain.exception.InvalidTokenException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Objects;

/**
 * Token de uso único para verificação de e-mail e redefinição de senha.
 *
 * <p>Guarda o hash, nunca o valor: um dump do banco não produz links utilizáveis. A
 * comparação é em tempo constante para não vazar informação por diferença de tempo de
 * resposta.
 */
public record OneTimeToken(String hash, Instant expiresAt, Instant usedAt) {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int TOKEN_BYTES = 32;

    public OneTimeToken {
        Objects.requireNonNull(hash, "hash do token é obrigatório");
        Objects.requireNonNull(expiresAt, "expiração do token é obrigatória");
    }

    /** Gera um token novo e devolve o par: o valor em claro e o registro a ser guardado. */
    public static Issued issue(Duration validity, Instant now) {
        byte[] raw = new byte[TOKEN_BYTES];
        RANDOM.nextBytes(raw);
        String plainText = Base64.getUrlEncoder().withoutPadding().encodeToString(raw);
        return new Issued(plainText, new OneTimeToken(sha256(plainText), now.plus(validity), null));
    }

    public boolean isValid(String plainText, Instant now) {
        if (usedAt != null || !now.isBefore(expiresAt)) {
            return false;
        }
        return MessageDigest.isEqual(
                sha256(plainText).getBytes(StandardCharsets.UTF_8), hash.getBytes(StandardCharsets.UTF_8));
    }

    public OneTimeToken markUsed(Instant now) {
        if (usedAt != null) {
            throw new InvalidTokenException("Token já utilizado");
        }
        return new OneTimeToken(hash, expiresAt, now);
    }

    private static String sha256(String value) {
        try {
            return HexFormat.of()
                    .formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponível nesta JVM", e);
        }
    }

    /** O valor em claro existe uma única vez, para ser enviado ao usuário. */
    public record Issued(String plainText, OneTimeToken token) {}
}
