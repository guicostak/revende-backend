package com.revende.backend.identity.application.service;

import com.revende.backend.identity.application.InvalidCredentialsException;
import com.revende.backend.identity.application.port.AuthenticatedUser;
import com.revende.backend.identity.application.port.LoginCommand;
import com.revende.backend.identity.application.port.LoginUseCase;
import com.revende.backend.identity.application.port.PasswordHasherPort;
import com.revende.backend.identity.application.port.UserRepositoryPort;
import com.revende.backend.identity.entity.User;
import com.revende.backend.identity.model.AccountStatus;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Autentica por e-mail e senha e abre uma sessão. */
@Service
@RequiredArgsConstructor
public class LoginService implements LoginUseCase {

    /**
     * Hash descartável usado quando o e-mail não existe.
     *
     * <p>Sem isto, e-mail inexistente responderia sem passar pelo BCrypt e voltaria muito
     * mais rápido que senha errada — e essa diferença de tempo é suficiente para enumerar
     * quais e-mails têm conta. Conferir contra um hash falso iguala o custo dos dois casos.
     */
    private static final String HASH_DESCARTAVEL =
            "{bcrypt}$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";

    private final UserRepositoryPort users;
    private final PasswordHasherPort passwordHasher;
    private final SessionIssuer sessionIssuer;

    @Override
    @Transactional
    public AuthenticatedUser login(LoginCommand command) {
        Optional<User> encontrado = users.findByEmail(EmailNormalizer.normalize(command.email()));

        boolean senhaConfere = passwordHasher.matches(
                command.rawPassword(), encontrado.map(User::getPasswordHash).orElse(HASH_DESCARTAVEL));

        User user = encontrado.orElse(null);
        // As três condições viram a mesma exceção de propósito: dizer qual delas falhou
        // entregaria de graça a informação de quais e-mails existem e quais estão ativos.
        if (user == null || !senhaConfere || user.getStatus() != AccountStatus.ACTIVE) {
            throw new InvalidCredentialsException();
        }

        return sessionIssuer.issueFor(user);
    }
}
