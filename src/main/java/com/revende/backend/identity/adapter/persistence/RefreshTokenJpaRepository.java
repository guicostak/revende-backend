package com.revende.backend.identity.adapter.persistence;

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
     * O {@code and t.revokedAt is null} é o que torna a rotação atômica: o banco decide
     * quem chegou primeiro. Devolve 1 para quem revogou e 0 para quem chegou depois.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
            """
            update RefreshToken t
               set t.revokedAt = :momento
             where t.id = :id
               and t.revokedAt is null
            """)
    int revokeIfActive(@Param("id") Long id, @Param("momento") Instant momento);

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
