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

/**
 * Cache local de quem está ativo, consultado na validação de cada access token.
 *
 * <p>Local à instância: N instâncias, N cópias que não se falam. Tolerável aqui, porque o
 * pior efeito é um bloqueio demorar até o TTL para pegar. Seria errado para refresh token,
 * onde a instância A revogaria e a B continuaria aceitando — por isso ele fica no banco.
 */
@Component
@RequiredArgsConstructor
public class ActiveUserCache {

    private static final Duration TTL = Duration.ofSeconds(60);
    private static final int CAPACIDADE = 10_000;

    private final UserRepositoryPort users;

    private final Cache<Long, AuthenticatedPrincipal> cache = Caffeine.newBuilder()
            // `afterWrite`, não `afterAccess`: com este, usuário ativo renovaria a entrada
            // para sempre e o bloqueio nunca pegaria.
            .expireAfterWrite(TTL)
            .maximumSize(CAPACIDADE)
            .build();

    /** Vazio para conta inexistente ou bloqueada. Nenhum dos dois é cacheado. */
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

    /** Chame ao bloquear conta ou trocar e-mail, para não esperar o TTL. */
    public void invalidate(Long userId) {
        cache.invalidate(userId);
    }

    private AuthenticatedPrincipal toPrincipal(User user) {
        return new AuthenticatedPrincipal(user.getId(), user.getEmail());
    }
}
