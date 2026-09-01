package com.revende.backend.identity.adapter.out.security;

import com.revende.backend.identity.application.port.out.TokenIssuerPort;
import com.revende.backend.shared.security.JwtProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

/** Emite o JWT assinado em HMAC-SHA256 que o frontend guarda e reenvia no header. */
@Component
public class JwtTokenIssuer implements TokenIssuerPort {

    private final SecretKey chave;
    private final long validadeMs;

    public JwtTokenIssuer(JwtProperties properties) {
        // A chave é derivada uma vez, na construção. Fazer isso a cada emissão seria
        // trabalho repetido, e a jjwt valida o tamanho mínimo aqui — falha na subida da
        // aplicação, não na primeira tentativa de login em produção.
        this.chave = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
        this.validadeMs = properties.expirationMs();
    }

    @Override
    public String issueFor(Long userId, String email) {
        Instant agora = Instant.now();
        return Jwts.builder()
                // `subject` é o id, não o e-mail: e-mail pode mudar, id não. Identidade
                // no token precisa apontar para algo estável.
                .subject(String.valueOf(userId))
                .claim("email", email)
                .issuedAt(Date.from(agora))
                .expiration(Date.from(agora.plusMillis(validadeMs)))
                .signWith(chave)
                .compact();
    }
}
