package com.revende.backend.identity.application.port;

import com.revende.backend.identity.entity.User;
import java.util.Optional;

public interface UserRepositoryPort {

    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    Optional<User> findById(Long id);

    User save(User user);
}
