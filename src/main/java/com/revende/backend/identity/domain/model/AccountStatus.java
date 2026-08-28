package com.revende.backend.identity.domain.model;

/**
 * Situação de moderação da conta. Independe da verificação de e-mail: conta bloqueada
 * e depois reativada não perde a verificação que já tinha.
 */
public enum AccountStatus {
    ACTIVE,
    BLOCKED
}
