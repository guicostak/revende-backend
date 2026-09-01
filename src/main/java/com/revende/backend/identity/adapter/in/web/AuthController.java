package com.revende.backend.identity.adapter.in.web;

import com.revende.backend.identity.adapter.in.web.dto.AuthResponse;
import com.revende.backend.identity.adapter.in.web.dto.RegisterRequest;
import com.revende.backend.identity.application.port.in.AuthenticatedUser;
import com.revende.backend.identity.application.port.in.RegisterUserCommand;
import com.revende.backend.identity.application.port.in.RegisterUserUseCase;
import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Borda HTTP da identidade. Recebe, valida, delega e mapeia — sem regra de negócio. */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    // Depende do port de entrada, nunca da classe concreta.
    private final RegisterUserUseCase registerUser;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthenticatedUser user = registerUser.register(
                new RegisterUserCommand(request.name(), request.email(), request.password(), request.phone()));

        // 201 com Location, como manda o CLAUDE.md §2.2. O corpo traz o token porque quem
        // acabou de se cadastrar já está autenticado — não faz sentido pedir login em
        // seguida para a mesma pessoa que acabou de provar quem é.
        return ResponseEntity.created(URI.create("/api/users/" + user.userId())).body(AuthResponse.from(user));
    }
}
