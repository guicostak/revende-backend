package com.revende.backend.identity.adapter.security;

import com.revende.backend.identity.application.port.TokenGeneratorPort;
import java.security.SecureRandom;
import java.util.Base64;
import org.springframework.stereotype.Component;

/** Refresh token opaco: 256 bits de aleatoriedade criptográfica, em Base64 URL-safe. */
@Component
public class SecureRandomTokenGenerator implements TokenGeneratorPort {

    private static final int BYTES = 32;

    // SecureRandom é thread-safe e caro de instanciar. Um por aplicação, não um por chamada.
    private final SecureRandom random = new SecureRandom();
    private final Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();

    @Override
    public String generate() {
        byte[] bytes = new byte[BYTES];
        random.nextBytes(bytes);
        // URL-safe e sem padding porque o token viaja em JSON e pode acabar em query de
        // log ou de ferramenta de debug; `+`, `/` e `=` obrigariam escapar em cada trecho.
        return encoder.encodeToString(bytes);
    }
}
