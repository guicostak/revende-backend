package com.revende.backend.identity.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.revende.backend.identity.application.InvalidCredentialsException;
import com.revende.backend.identity.application.port.AuthenticatedUser;
import com.revende.backend.identity.application.port.LoginCommand;
import com.revende.backend.identity.application.port.PasswordHasherPort;
import com.revende.backend.identity.application.port.UserRepositoryPort;
import com.revende.backend.identity.entity.User;
import com.revende.backend.identity.model.AccountStatus;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    @Mock
    private UserRepositoryPort users;

    @Mock
    private PasswordHasherPort passwordHasher;

    @Mock
    private SessionIssuer sessionIssuer;

    private LoginService service;

    @BeforeEach
    void setUp() {
        service = new LoginService(users, passwordHasher, sessionIssuer);
    }

    private User usuario(AccountStatus status) {
        return User.builder()
                .id(7L)
                .name("Ana")
                .email("ana@exemplo.com")
                .passwordHash("{bcrypt}$2a$10$hash")
                .status(status)
                .build();
    }

    @Test
    void shouldIssueSessionWhenCredentialsAreValid() {
        when(users.findByEmail("ana@exemplo.com")).thenReturn(Optional.of(usuario(AccountStatus.ACTIVE)));
        when(passwordHasher.matches("senha123", "{bcrypt}$2a$10$hash")).thenReturn(true);
        when(sessionIssuer.issueFor(any(User.class)))
                .thenReturn(new AuthenticatedUser("access", "refresh", 7L, "Ana", "ana@exemplo.com"));

        AuthenticatedUser resultado = service.login(new LoginCommand("ana@exemplo.com", "senha123"));

        assertThat(resultado.token()).isEqualTo("access");
        assertThat(resultado.refreshToken()).isEqualTo("refresh");
    }

    @Test
    void shouldNormalizeEmailBeforeLookup() {
        when(users.findByEmail("ana@exemplo.com")).thenReturn(Optional.of(usuario(AccountStatus.ACTIVE)));
        when(passwordHasher.matches(anyString(), anyString())).thenReturn(true);
        when(sessionIssuer.issueFor(any(User.class)))
                .thenReturn(new AuthenticatedUser("access", "refresh", 7L, "Ana", "ana@exemplo.com"));

        // Cadastro grava normalizado. Se o login não normalizasse igual, quem digitasse
        // com maiúscula nunca conseguiria entrar.
        service.login(new LoginCommand("  ANA@Exemplo.COM  ", "senha123"));

        verify(users).findByEmail("ana@exemplo.com");
    }

    @Test
    void shouldRejectWhenPasswordDoesNotMatch() {
        when(users.findByEmail(anyString())).thenReturn(Optional.of(usuario(AccountStatus.ACTIVE)));
        when(passwordHasher.matches(anyString(), anyString())).thenReturn(false);

        assertThatThrownBy(() -> service.login(new LoginCommand("ana@exemplo.com", "errada")))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(sessionIssuer, never()).issueFor(any());
    }

    @Test
    void shouldStillHashWhenEmailDoesNotExist() {
        when(users.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordHasher.matches(anyString(), anyString())).thenReturn(false);

        assertThatThrownBy(() -> service.login(new LoginCommand("nao-existe@exemplo.com", "senha123")))
                .isInstanceOf(InvalidCredentialsException.class);

        // Sem conferir contra um hash descartável, e-mail inexistente responderia sem
        // passar pelo BCrypt e voltaria muito mais rápido que senha errada — e essa
        // diferença de tempo é suficiente para enumerar quais e-mails têm conta.
        verify(passwordHasher).matches(anyString(), anyString());
    }

    @Test
    void shouldRejectBlockedAccountWithTheSameErrorAsWrongPassword() {
        when(users.findByEmail(anyString())).thenReturn(Optional.of(usuario(AccountStatus.BLOCKED)));
        when(passwordHasher.matches(anyString(), anyString())).thenReturn(true);

        // Mesma exceção de senha errada: dizer "sua conta está bloqueada" confirmaria a
        // existência da conta para quem só tem o e-mail.
        assertThatThrownBy(() -> service.login(new LoginCommand("ana@exemplo.com", "senha123")))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(sessionIssuer, never()).issueFor(any());
    }
}
