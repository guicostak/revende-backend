package com.revende.backend.identity.application.port;

/** Hash e conferência de senha. Port para que a aplicação não dependa do Spring Security. */
public interface PasswordHasherPort {

    String hash(String rawPassword);

    /** Compara em tempo constante: `equals` vazaria pelo tempo quantos caracteres bateram. */
    boolean matches(String rawPassword, String passwordHash);
}
