package com.revende.backend.identity.adapter.persistence;

import com.revende.backend.identity.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data puro. Fica no adapter para que {@code application} não veja JPA. */
public interface UserJpaRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);
}
