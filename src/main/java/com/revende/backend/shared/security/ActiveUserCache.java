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
 * Cache local, por instância, de quem está ativo — consultado na validação de cada access
 * token.
 *
 * <p>É aqui que o cache paga: o access token é conferido em <b>toda</b> requisição, e sem
 * isto cada uma abriria uma consulta ao banco só para saber se a conta segue ativa. O
 * refresh, por contraste, acontece uma vez por hora.
 *
 * <p><b>Por que aqui e não no refresh token.</b> Este cache é local à instância: com três
 * instâncias existem três cópias que não se falam. Isso é tolerável para status de conta,
 * onde ficar desatualizado por até {@code TTL} significa que um bloqueio demora no máximo
 * esse tempo para pegar. Seria <b>errado</b> para refresh token: a instância A revogaria e
 * a B continuaria aceitando, e como o balanceamento é não-determinístico a falha apareceria
 * de forma intermitente. Por isso o refresh token mora no banco, e só ele.
 *
 * <p>O TTL é curto justamente para limitar essa janela. Aumentar economiza consultas e
 * atrasa o efeito de um bloqueio na mesma proporção.
 */
@Component
@RequiredArgsConstructor
public class ActiveUserCache {

    private static final Duration TTL = Duration.ofSeconds(60);
    private static final int CAPACIDADE = 10_000;

    private final UserRepositoryPort users;

    private final Cache<Long, AuthenticatedPrincipal> cache = Caffeine.newBuilder()
            // `expireAfterWrite`, não `expireAfterAccess`: o que importa é a idade do dado
            // lido do banco. Com `afterAccess`, um usuário ativo renovaria a entrada para
            // sempre e um bloqueio nunca pegaria.
            .expireAfterWrite(TTL)
            // Teto de memória. Sem ele, uma varredura de ids inflaria o cache até o OOM —
            // e o container tem 1 GB.
            .maximumSize(CAPACIDADE)
            .build();

    /**
     * Devolve o principal se a conta existe e está ativa.
     *
     * <p>Conta bloqueada e conta inexistente resultam ambas em vazio, e nenhuma das duas é
     * cacheada: são o caminho de exceção, e cachear negativa daria a um id inválido o poder
     * de ocupar espaço no cache.
     */
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

    /** Invalida a entrada. Chame ao bloquear conta ou trocar e-mail, para não esperar o TTL. */
    public void invalidate(Long userId) {
        cache.invalidate(userId);
    }

    private AuthenticatedPrincipal toPrincipal(User user) {
        return new AuthenticatedPrincipal(user.getId(), user.getEmail());
    }
}
