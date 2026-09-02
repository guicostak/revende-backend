package com.revende.backend.shared.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.revende.backend.identity.application.port.out.UserRepositoryPort;
import com.revende.backend.identity.entity.User;
import com.revende.backend.identity.model.AccountStatus;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ActiveUserCacheTest {

    @Mock
    private UserRepositoryPort users;

    private ActiveUserCache cache;

    @BeforeEach
    void setUp() {
        cache = new ActiveUserCache(users);
    }

    private User usuario(AccountStatus status) {
        return User.builder()
                .id(7L)
                .name("Ana")
                .email("ana@exemplo.com")
                .status(status)
                .build();
    }

    @Test
    void shouldHitDatabaseOnlyOnceForRepeatedLookups() {
        when(users.findById(7L)).thenReturn(Optional.of(usuario(AccountStatus.ACTIVE)));

        cache.find(7L);
        cache.find(7L);
        cache.find(7L);

        // É o ponto inteiro do cache: o access token é conferido em toda requisição, e sem
        // isto cada uma abriria uma consulta ao banco.
        verify(users, times(1)).findById(7L);
    }

    @Test
    void shouldReturnPrincipalWithTypedIdentity() {
        when(users.findById(7L)).thenReturn(Optional.of(usuario(AccountStatus.ACTIVE)));

        Optional<AuthenticatedPrincipal> encontrado = cache.find(7L);

        assertThat(encontrado).contains(new AuthenticatedPrincipal(7L, "ana@exemplo.com"));
    }

    @Test
    void shouldNotReturnBlockedAccount() {
        when(users.findById(7L)).thenReturn(Optional.of(usuario(AccountStatus.BLOCKED)));

        assertThat(cache.find(7L)).isEmpty();
    }

    @Test
    void shouldNotCacheMisses() {
        when(users.findById(99L)).thenReturn(Optional.empty());

        cache.find(99L);
        cache.find(99L);

        // Cachear negativa daria a um id inválido o poder de ocupar espaço no cache — e o
        // teto de memória existe justamente para o container de 1 GB não estourar.
        verify(users, times(2)).findById(99L);
    }

    @Test
    void shouldGoBackToDatabaseAfterInvalidation() {
        when(users.findById(7L)).thenReturn(Optional.of(usuario(AccountStatus.ACTIVE)));

        cache.find(7L);
        cache.invalidate(7L);
        cache.find(7L);

        // Sem invalidação explícita, um bloqueio só passaria a valer no fim do TTL.
        verify(users, times(2)).findById(7L);
    }
}
