package com.revende.backend.identity.application.port;

/**
 * Hash do refresh token, para que o banco guarde só o hash.
 *
 * <p>Separado do {@link PasswordHasherPort} de propósito. Senha humana precisa de hash
 * deliberadamente lento (BCrypt) porque tem pouca entropia e é chutável por dicionário.
 * Refresh token é aleatório de 256 bits: não existe dicionário que o alcance, e usar
 * BCrypt a cada renovação só custaria latência sem comprar segurança.
 */
public interface TokenHasherPort {

    String hash(String rawToken);
}
