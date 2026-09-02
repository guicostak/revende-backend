package com.revende.backend.identity.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Sessão de longa duração. Guarda o hash do token, nunca o token.
 *
 * <p>{@code userId} é campo simples e não {@code @ManyToOne}: a validação do refresh
 * precisa do id do dono e de mais nada, e uma associação traria o agregado inteiro do
 * usuário para uma operação que não olha nenhum outro campo dele.
 */
@Entity
@Table(name = "refresh_tokens")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public boolean isRevoked() {
        return revokedAt != null;
    }

    public boolean isExpiredAt(Instant momento) {
        return expiresAt.isBefore(momento);
    }

    /** Utilizável é o que não foi revogado e ainda não venceu. As duas coisas, sempre. */
    public boolean isUsableAt(Instant momento) {
        return !isRevoked() && !isExpiredAt(momento);
    }
}
