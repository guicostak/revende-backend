package com.revende.backend.identity.adapter.out.security;

import com.revende.backend.identity.application.port.out.PasswordHasherPort;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Hash e conferência de senha com o {@code PasswordEncoder} do Spring Security.
 *
 * <p>O bean é {@code DelegatingPasswordEncoder} (ver {@code SecurityConfig}): o hash sai
 * prefixado com o algoritmo, {@code {bcrypt}$2a$...}. É isso que permite trocar de
 * algoritmo depois sem invalidar as senhas já gravadas — cada hash diz como foi feito.
 */
@Component
@RequiredArgsConstructor
public class BCryptPasswordHasher implements PasswordHasherPort {

    private final PasswordEncoder passwordEncoder;

    @Override
    public String hash(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    @Override
    public boolean matches(String rawPassword, String passwordHash) {
        // O `matches` do BCrypt compara em tempo constante — não vaza pelo tempo de
        // resposta quantos caracteres do hash bateram.
        return passwordEncoder.matches(rawPassword, passwordHash);
    }
}
