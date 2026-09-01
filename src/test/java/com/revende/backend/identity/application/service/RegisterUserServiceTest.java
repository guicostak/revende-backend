package com.revende.backend.identity.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.revende.backend.identity.application.EmailAlreadyRegisteredException;
import com.revende.backend.identity.application.port.in.AuthenticatedUser;
import com.revende.backend.identity.application.port.in.RegisterUserCommand;
import com.revende.backend.identity.application.port.out.PasswordHasherPort;
import com.revende.backend.identity.application.port.out.TokenIssuerPort;
import com.revende.backend.identity.application.port.out.UserRepositoryPort;
import com.revende.backend.identity.entity.User;
import com.revende.backend.identity.model.AccountStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RegisterUserServiceTest {

    @Mock
    private UserRepositoryPort users;

    @Mock
    private PasswordHasherPort passwordHasher;

    @Mock
    private TokenIssuerPort tokenIssuer;

    private RegisterUserService service;

    @BeforeEach
    void setUp() {
        service = new RegisterUserService(users, passwordHasher, tokenIssuer);
    }

    private RegisterUserCommand comando() {
        return new RegisterUserCommand("Ana Souza", "ana@exemplo.com", "senha123", "11999998888");
    }

    private void aceitaCadastro() {
        when(users.existsByEmail(anyString())).thenReturn(false);
        when(passwordHasher.hash(anyString())).thenReturn("{bcrypt}$2a$10$hash");
        when(users.save(any(User.class))).thenAnswer(invocation -> {
            User recebido = invocation.getArgument(0);
            recebido.setId(42L);
            return recebido;
        });
        when(tokenIssuer.issueFor(anyLong(), anyString())).thenReturn("token-jwt");
    }

    @Test
    void shouldReturnAuthenticatedUserWhenRegistrationSucceeds() {
        aceitaCadastro();

        AuthenticatedUser resultado = service.register(comando());

        assertThat(resultado.userId()).isEqualTo(42L);
        assertThat(resultado.token()).isEqualTo("token-jwt");
        assertThat(resultado.name()).isEqualTo("Ana Souza");
        assertThat(resultado.email()).isEqualTo("ana@exemplo.com");
    }

    @Test
    void shouldNeverPersistRawPassword() {
        aceitaCadastro();

        service.register(comando());

        ArgumentCaptor<User> capturado = ArgumentCaptor.forClass(User.class);
        verify(users).save(capturado.capture());
        assertThat(capturado.getValue().getPasswordHash())
                .isEqualTo("{bcrypt}$2a$10$hash")
                .isNotEqualTo("senha123");
    }

    @Test
    void shouldCreateAccountActiveAndUnverified() {
        aceitaCadastro();

        service.register(comando());

        ArgumentCaptor<User> capturado = ArgumentCaptor.forClass(User.class);
        verify(users).save(capturado.capture());
        User salvo = capturado.getValue();
        assertThat(salvo.getStatus()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(salvo.isEmailVerified()).isFalse();
        assertThat(salvo.getCreatedAt()).isNotNull();
        assertThat(salvo.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldLowercaseAndTrimEmailBeforePersisting() {
        aceitaCadastro();

        service.register(new RegisterUserCommand("Ana", "  Ana@Exemplo.COM  ", "senha123", null));

        ArgumentCaptor<User> capturado = ArgumentCaptor.forClass(User.class);
        verify(users).save(capturado.capture());
        // Sem normalizar, "Ana@Exemplo.COM" e "ana@exemplo.com" viram duas contas: o
        // UNIQUE do Postgres é sensível a maiúsculas.
        assertThat(capturado.getValue().getEmail()).isEqualTo("ana@exemplo.com");
    }

    @Test
    void shouldStoreNullPhoneWhenBlank() {
        aceitaCadastro();

        service.register(new RegisterUserCommand("Ana", "ana@exemplo.com", "senha123", "   "));

        ArgumentCaptor<User> capturado = ArgumentCaptor.forClass(User.class);
        verify(users).save(capturado.capture());
        assertThat(capturado.getValue().getPhone()).isNull();
    }

    @Test
    void shouldRejectWhenEmailAlreadyRegistered() {
        when(users.existsByEmail("ana@exemplo.com")).thenReturn(true);

        assertThatThrownBy(() -> service.register(comando())).isInstanceOf(EmailAlreadyRegisteredException.class);

        verify(users, never()).save(any());
        verify(tokenIssuer, never()).issueFor(anyLong(), anyString());
    }

    @Test
    void shouldCheckEmailUniquenessAlreadyNormalized() {
        when(users.existsByEmail("ana@exemplo.com")).thenReturn(true);

        // Se a checagem usasse o valor cru, o e-mail em maiúsculas escaparia do 409 e só
        // seria barrado pela constraint, virando 500.
        assertThatThrownBy(() -> service.register(new RegisterUserCommand("Ana", "ANA@EXEMPLO.COM", "senha123", null)))
                .isInstanceOf(EmailAlreadyRegisteredException.class);
    }
}
