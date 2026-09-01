package com.revende.backend.identity.application.service;

import com.revende.backend.identity.application.EmailAlreadyRegisteredException;
import com.revende.backend.identity.application.port.in.AuthenticatedUser;
import com.revende.backend.identity.application.port.in.RegisterUserCommand;
import com.revende.backend.identity.application.port.in.RegisterUserUseCase;
import com.revende.backend.identity.application.port.out.PasswordHasherPort;
import com.revende.backend.identity.application.port.out.TokenIssuerPort;
import com.revende.backend.identity.application.port.out.UserRepositoryPort;
import com.revende.backend.identity.entity.User;
import com.revende.backend.identity.model.AccountStatus;
import java.time.Instant;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Orquestra o cadastro: unicidade do e-mail, hash da senha, persistência e token. */
@Service
@RequiredArgsConstructor
public class RegisterUserService implements RegisterUserUseCase {

    private final UserRepositoryPort users;
    private final PasswordHasherPort passwordHasher;
    private final TokenIssuerPort tokenIssuer;

    @Override
    @Transactional
    public AuthenticatedUser register(RegisterUserCommand command) {
        String email = normalize(command.email());

        // Checagem antecipada para responder 409 com mensagem clara. Ela NÃO substitui a
        // constraint UNIQUE do banco: entre o `exists` e o `save` cabe outra requisição
        // com o mesmo e-mail. A constraint é quem garante; isto é só a mensagem boa.
        if (users.existsByEmail(email)) {
            throw new EmailAlreadyRegisteredException();
        }

        Instant agora = Instant.now();
        User user = User.builder()
                .name(command.name().trim())
                .email(email)
                .passwordHash(passwordHasher.hash(command.rawPassword()))
                .phone(blankToNull(command.phone()))
                .status(AccountStatus.ACTIVE)
                // Conta nasce não verificada. Quem cadastra entra e navega; o que exigir
                // e-mail confirmado é decisão de cada caso de uso, não do cadastro.
                .emailVerified(false)
                .createdAt(agora)
                .updatedAt(agora)
                .build();

        User salvo = users.save(user);

        return new AuthenticatedUser(
                tokenIssuer.issueFor(salvo.getId(), salvo.getEmail()),
                salvo.getId(),
                salvo.getName(),
                salvo.getEmail());
    }

    /**
     * E-mail é case-insensitive na prática, mas {@code UNIQUE} no Postgres não é. Sem
     * normalizar, "Ana@x.com" e "ana@x.com" viram duas contas — e o login depois falha
     * dependendo de como a pessoa digitou.
     */
    private String normalize(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    /** String vazia vinda de formulário é ausência de dado, e no banco isso é {@code NULL}. */
    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
