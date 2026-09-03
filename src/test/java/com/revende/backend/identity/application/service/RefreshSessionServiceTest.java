package com.revende.backend.identity.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.revende.backend.identity.application.InvalidRefreshTokenException;
import com.revende.backend.identity.application.port.AuthenticatedUser;
import com.revende.backend.identity.application.port.RefreshTokenCodecPort;
import com.revende.backend.identity.application.port.RefreshTokenRepositoryPort;
import com.revende.backend.identity.application.port.UserRepositoryPort;
import com.revende.backend.identity.entity.RefreshToken;
import com.revende.backend.identity.entity.User;
import com.revende.backend.identity.model.AccountStatus;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RefreshSessionServiceTest {

    private static final String TOKEN_PURO = "refresh-em-texto-puro";
    private static final String TOKEN_HASH = "hash-do-refresh";

    @Mock
    private RefreshTokenRepositoryPort refreshTokens;

    @Mock
    private UserRepositoryPort users;

    @Mock
    private RefreshTokenCodecPort refreshTokenCodec;

    @Mock
    private SessionIssuer sessionIssuer;

    private RefreshSessionService service;

    @BeforeEach
    void setUp() {
        service = new RefreshSessionService(refreshTokens, users, refreshTokenCodec, sessionIssuer);
    }

    private RefreshToken tokenValido() {
        return RefreshToken.builder()
                .id(1L)
                .userId(7L)
                .tokenHash(TOKEN_HASH)
                .expiresAt(Instant.now().plus(7, ChronoUnit.DAYS))
                .createdAt(Instant.now())
                .build();
    }

    private User usuarioAtivo() {
        return User.builder()
                .id(7L)
                .name("Ana")
                .email("ana@exemplo.com")
                .status(AccountStatus.ACTIVE)
                .build();
    }

    @Test
    void shouldRotateTokenAndIssueNewSession() {
        RefreshToken guardado = tokenValido();
        when(refreshTokenCodec.hash(TOKEN_PURO)).thenReturn(TOKEN_HASH);
        when(refreshTokens.findByTokenHash(TOKEN_HASH)).thenReturn(Optional.of(guardado));
        when(users.findById(7L)).thenReturn(Optional.of(usuarioAtivo()));
        when(refreshTokens.revokeIfActive(eq(1L), any(Instant.class))).thenReturn(true);
        when(sessionIssuer.issueFor(any(User.class)))
                .thenReturn(new AuthenticatedUser("novo-access", "novo-refresh", 7L, "Ana", "ana@exemplo.com"));

        AuthenticatedUser resultado = service.refresh(TOKEN_PURO);

        assertThat(resultado.token()).isEqualTo("novo-access");
        assertThat(resultado.refreshToken()).isEqualTo("novo-refresh");

        // A rotação sai por UPDATE condicional, não por save da entidade lida — é o
        // banco que decide quem chegou primeiro.
        verify(refreshTokens).revokeIfActive(eq(1L), any(Instant.class));
    }

    @Test
    void shouldQueryByHashNeverByRawToken() {
        when(refreshTokenCodec.hash(TOKEN_PURO)).thenReturn(TOKEN_HASH);
        when(refreshTokens.findByTokenHash(TOKEN_HASH)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.refresh(TOKEN_PURO)).isInstanceOf(InvalidRefreshTokenException.class);

        // O banco guarda hash. Consultar pelo texto puro nunca acharia nada — e se achasse,
        // significaria que o token está gravado em claro.
        verify(refreshTokens, never()).findByTokenHash(TOKEN_PURO);
    }

    @Test
    void shouldRevokeEveryTokenOfUserWhenAlreadyUsedTokenReappears() {
        RefreshToken jaRevogado = tokenValido();
        jaRevogado.setRevokedAt(Instant.now().minus(1, ChronoUnit.MINUTES));
        when(refreshTokenCodec.hash(anyString())).thenReturn(TOKEN_HASH);
        when(refreshTokens.findByTokenHash(TOKEN_HASH)).thenReturn(Optional.of(jaRevogado));

        assertThatThrownBy(() -> service.refresh(TOKEN_PURO)).isInstanceOf(InvalidRefreshTokenException.class);

        // Token já usado reaparecendo é sinal de roubo: a sessão inteira cai, não só ele.
        verify(refreshTokens).revokeAllForUser(eq(7L), any(Instant.class));
        verify(sessionIssuer, never()).issueFor(any());
    }

    @Test
    void shouldRejectExpiredTokenWithoutRevokingTheWholeSession() {
        RefreshToken vencido = tokenValido();
        vencido.setExpiresAt(Instant.now().minus(1, ChronoUnit.DAYS));
        when(refreshTokenCodec.hash(anyString())).thenReturn(TOKEN_HASH);
        when(refreshTokens.findByTokenHash(TOKEN_HASH)).thenReturn(Optional.of(vencido));

        assertThatThrownBy(() -> service.refresh(TOKEN_PURO)).isInstanceOf(InvalidRefreshTokenException.class);

        // Vencer é o curso normal das coisas, não indício de roubo — não derruba o resto.
        verify(refreshTokens, never()).revokeAllForUser(any(), any());
    }

    @Test
    void shouldRejectWhenAccountIsNoLongerActive() {
        when(refreshTokenCodec.hash(anyString())).thenReturn(TOKEN_HASH);
        when(refreshTokens.findByTokenHash(TOKEN_HASH)).thenReturn(Optional.of(tokenValido()));
        User bloqueado = usuarioAtivo();
        bloqueado.setStatus(AccountStatus.BLOCKED);
        when(users.findById(7L)).thenReturn(Optional.of(bloqueado));

        // É aqui que o bloqueio passa a valer de fato: o access token anterior segue válido
        // até vencer, e é a recusa da renovação que encerra a sessão.
        assertThatThrownBy(() -> service.refresh(TOKEN_PURO)).isInstanceOf(InvalidRefreshTokenException.class);

        verify(sessionIssuer, never()).issueFor(any());
    }

    @Test
    void shouldTreatLostRotationRaceAsReuse() {
        when(refreshTokenCodec.hash(anyString())).thenReturn(TOKEN_HASH);
        when(refreshTokens.findByTokenHash(TOKEN_HASH)).thenReturn(Optional.of(tokenValido()));
        when(users.findById(7L)).thenReturn(Optional.of(usuarioAtivo()));
        // Outra requisição rotacionou o mesmo token entre a leitura e o UPDATE.
        when(refreshTokens.revokeIfActive(eq(1L), any(Instant.class))).thenReturn(false);

        assertThatThrownBy(() -> service.refresh(TOKEN_PURO)).isInstanceOf(InvalidRefreshTokenException.class);

        verify(refreshTokens).revokeAllForUser(eq(7L), any(Instant.class));
        verify(sessionIssuer, never()).issueFor(any());
    }
}
