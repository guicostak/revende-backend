package com.revende.backend.shared.domain;

import java.security.SecureRandom;
import java.util.UUID;

/**
 * Gerador de UUID versão 7: 48 bits de timestamp em milissegundos seguidos de bits
 * aleatórios. Diferente do v4, é ordenável por tempo — o que preserva a localidade
 * do índice no banco em vez de espalhar escritas por toda a árvore.
 *
 * <p>O Java 21 não gera v7 nativamente.
 */
public final class UuidV7 {

    private static final SecureRandom RANDOM = new SecureRandom();

    private UuidV7() {}

    public static UUID generate() {
        byte[] value = new byte[16];
        RANDOM.nextBytes(value);

        long timestamp = System.currentTimeMillis();
        value[0] = (byte) (timestamp >>> 40);
        value[1] = (byte) (timestamp >>> 32);
        value[2] = (byte) (timestamp >>> 24);
        value[3] = (byte) (timestamp >>> 16);
        value[4] = (byte) (timestamp >>> 8);
        value[5] = (byte) timestamp;

        value[6] = (byte) ((value[6] & 0x0F) | 0x70); // versão 7
        value[8] = (byte) ((value[8] & 0x3F) | 0x80); // variante RFC 4122

        long high = 0;
        long low = 0;
        for (int i = 0; i < 8; i++) {
            high = (high << 8) | (value[i] & 0xFF);
        }
        for (int i = 8; i < 16; i++) {
            low = (low << 8) | (value[i] & 0xFF);
        }
        return new UUID(high, low);
    }
}
