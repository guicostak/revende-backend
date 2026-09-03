package com.revende.backend.identity.application.port;

/**
 * Gera o refresh token opaco entregue ao cliente.
 *
 * <p>Port para que a aplicação não instancie {@code SecureRandom} — e para que o teste de
 * caso de uso possa devolver um valor fixo em vez de lidar com aleatoriedade.
 */
public interface TokenGeneratorPort {

    String generate();
}
