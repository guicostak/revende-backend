package com.revende.backend.identity.application.port;

/**
 * Transformação e conferência de senha.
 *
 * <p>É port, e não uma chamada direta ao {@code PasswordEncoder} do Spring Security, para
 * que a aplicação não dependa do framework de segurança — e para que o teste de caso de
 * uso não precise de contexto Spring só para hashear string.
 */
public interface PasswordHasherPort {

    String hash(String rawPassword);

    /**
     * Confere a senha contra o hash guardado.
     *
     * <p>A implementação compara em tempo constante. Comparar hash com {@code equals}
     * vazaria, pelo tempo de resposta, quantos caracteres iniciais bateram.
     */
    boolean matches(String rawPassword, String passwordHash);
}
