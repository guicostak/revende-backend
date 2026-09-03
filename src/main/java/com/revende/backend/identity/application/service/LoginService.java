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

@Service
@RequiredArgsConstructor
public class LoginService implements LoginUseCase {

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
        if (user == null || !senhaConfere || user.getStatus() != AccountStatus.ACTIVE) {
            throw new InvalidCredentialsException();
        }

        return sessionIssuer.issueFor(user);
    }
}
