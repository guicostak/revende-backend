package com.revende.backend.identity.adapter.out.persistence;

import com.revende.backend.identity.entity.RefreshToken;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RefreshTokenJpaRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    /**
     * Revogação em massa por UPDATE único. Carregar as entidades para revogar uma a uma
     * custaria N queries num caminho que só roda quando já há suspeita de roubo — momento
     * em que a resposta precisa ser rápida e completa.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
            """
            update RefreshToken t
               set t.revokedAt = :momento
             where t.userId = :userId
               and t.revokedAt is null
            """)
    void revokeAllForUser(@Param("userId") Long userId, @Param("momento") Instant momento);
}
