package com.revende.backend.identity.application.port.out;

/**
 * Transformação de senha em hash.
 *
 * <p>É port, e não uma chamada direta ao {@code PasswordEncoder} do Spring Security, para
 * que a aplicação não dependa do framework de segurança — e para que o teste de caso de
 * uso não precise de contexto Spring só para hashear string.
 */
public interface PasswordHasherPort {

    String hash(String rawPassword);
}
