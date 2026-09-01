package com.revende.backend.identity.adapter.out.persistence;

import com.revende.backend.identity.application.port.out.UserRepositoryPort;
import com.revende.backend.identity.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Implementa o port de saída com Spring Data. Sem regra de negócio, só tradução. */
@Component
@RequiredArgsConstructor
public class UserPersistenceAdapter implements UserRepositoryPort {

    private final UserJpaRepository repository;

    @Override
    public boolean existsByEmail(String email) {
        return repository.existsByEmail(email);
    }

    @Override
    public User save(User user) {
        return repository.save(user);
    }
}
