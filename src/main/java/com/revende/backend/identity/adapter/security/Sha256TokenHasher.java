package com.revende.backend.identity.adapter.security;

import com.revende.backend.identity.application.port.TokenHasherPort;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import org.springframework.stereotype.Component;

/**
 * SHA-256 do refresh token, para o banco guardar só o hash.
 *
 * <p>SHA-256 e não BCrypt: o token é aleatório de 256 bits, então não há dicionário nem
 * força bruta viável, e o hash lento do BCrypt só custaria latência a cada renovação. Sem
 * sal pelo mesmo motivo — sal existe para senhas repetidas entre usuários, e dois tokens
 * aleatórios de 256 bits nunca colidem.
 *
 * <p>Determinístico de propósito: a busca é por hash, e hash salgado não seria consultável.
 */
@Component
public class Sha256TokenHasher implements TokenHasherPort {

    @Override
    public String hash(String rawToken) {
        try {
            // MessageDigest não é thread-safe; uma instância por chamada é o uso correto,
            // e SHA-256 é barato o bastante para isso não pesar.
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(rawToken.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 é obrigatório em toda JVM. Se faltar, a instalação está quebrada —
            // e falhar alto é melhor que degradar silenciosamente a segurança da sessão.
            throw new IllegalStateException("SHA-256 indisponível nesta JVM", e);
        }
    }
}
