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
