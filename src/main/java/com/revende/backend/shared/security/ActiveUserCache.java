package com.revende.backend.shared.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.revende.backend.identity.application.port.UserRepositoryPort;
import com.revende.backend.identity.entity.User;
import com.revende.backend.identity.model.AccountStatus;
import java.time.Duration;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ActiveUserCache {

    private static final Duration TTL = Duration.ofSeconds(60);
    private static final int CAPACIDADE = 10_000;

    private final UserRepositoryPort users;

    private final Cache<Long, AuthenticatedPrincipal> cache =
            Caffeine.newBuilder().expireAfterWrite(TTL).maximumSize(CAPACIDADE).build();

    public Optional<AuthenticatedPrincipal> find(Long userId) {
        AuthenticatedPrincipal cacheado = cache.getIfPresent(userId);
        if (cacheado != null) {
            return Optional.of(cacheado);
        }

        Optional<AuthenticatedPrincipal> encontrado = users.findById(userId)
                .filter(user -> user.getStatus() == AccountStatus.ACTIVE)
                .map(this::toPrincipal);

        encontrado.ifPresent(principal -> cache.put(userId, principal));
        return encontrado;
    }

    public void invalidate(Long userId) {
        cache.invalidate(userId);
    }

    private AuthenticatedPrincipal toPrincipal(User user) {
        return new AuthenticatedPrincipal(user.getId(), user.getEmail());
    }
}
