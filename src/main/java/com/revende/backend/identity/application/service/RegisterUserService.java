package com.revende.backend.identity.application.service;

import com.revende.backend.identity.application.EmailAlreadyRegisteredException;
import com.revende.backend.identity.application.port.AuthenticatedUser;
import com.revende.backend.identity.application.port.PasswordHasherPort;
import com.revende.backend.identity.application.port.RegisterUserCommand;
import com.revende.backend.identity.application.port.RegisterUserUseCase;
import com.revende.backend.identity.application.port.UserRepositoryPort;
import com.revende.backend.identity.entity.User;
import com.revende.backend.identity.model.AccountStatus;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RegisterUserService implements RegisterUserUseCase {

    private final UserRepositoryPort users;
    private final PasswordHasherPort passwordHasher;
    private final SessionIssuer sessionIssuer;

    @Override
    @Transactional
    public AuthenticatedUser register(RegisterUserCommand command) {
        String email = EmailNormalizer.normalize(command.email());

        if (users.existsByEmail(email)) {
            throw new EmailAlreadyRegisteredException();
        }

        Instant agora = Instant.now();
        User salvo = users.save(User.builder()
                .name(command.name().trim())
                .email(email)
                .passwordHash(passwordHasher.hash(command.rawPassword()))
                .phone(blankToNull(command.phone()))
                .status(AccountStatus.ACTIVE)
                .emailVerified(false)
                .createdAt(agora)
                .updatedAt(agora)
                .build());

        return sessionIssuer.issueFor(salvo);
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
