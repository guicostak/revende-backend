package com.revende.backend.identity.application.port;

/**
 * Gera e hasheia o refresh token opaco.
 *
 * <p>Hash separado do {@link PasswordHasherPort}: senha humana pede hash lento (BCrypt)
 * porque tem pouca entropia; refresh token é aleatório de 256 bits, e hash lento a cada
 * renovação custaria latência sem comprar segurança.
 */
public interface RefreshTokenCodecPort {

    String generate();

    String hash(String rawToken);
}
